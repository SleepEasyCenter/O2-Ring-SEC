// OximetryIIDeviceController.kt
package com.sleepeasycenter.o2ring_app

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.ext.oxy2.RtPpg
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import com.sleepeasycenter.o2ring_app.utils.readPatientDBPCalib
import com.sleepeasycenter.o2ring_app.utils.readPatientSPCalib
import kotlin.math.roundToInt

data class PpgSnapshot(val sampleIndex: IntArray, val ir: IntArray, val red: IntArray)

data class BpAvg(val sbp: Int, val dbp: Int, val count: Int)

private data class Bp(val sbp: Int, val dbp: Int)

class PpgAccumulator(private val maxSamples: Int = 60000) {
  private val irBuf = ArrayList<Int>(maxSamples)
  private val redBuf = ArrayList<Int>(maxSamples)

  // Append new chunk
  fun add(ir: IntArray, red: IntArray) {
    // Append
    for (v in ir) irBuf.add(v)
    for (v in red) redBuf.add(v)

    // Keep same length (drop tail of the longer one)
    val n = minOf(irBuf.size, redBuf.size)
    if (irBuf.size != n) while (irBuf.size > n) irBuf.removeAt(irBuf.lastIndex)
    if (redBuf.size != n) while (redBuf.size > n) redBuf.removeAt(redBuf.lastIndex)

    // Cap buffer
    if (irBuf.size > maxSamples) {
      val drop = irBuf.size - maxSamples
      repeat(drop) {
        irBuf.removeAt(0)
        redBuf.removeAt(0)
      }
    }
  }

  fun clear() {
    irBuf.clear()
    redBuf.clear()
  }

  fun snapshot(): PpgSnapshot {
    val n = minOf(irBuf.size, redBuf.size)
    val irArr = IntArray(n) { irBuf[it] }
    val redArr = IntArray(n) { redBuf[it] }
    val idx = IntArray(n) { it }
    return PpgSnapshot(idx, irArr, redArr)
  }
}

// 4s window aggregator that ignores zeros, averages valid samples, then resets
class BpWindowAggregator(private val windowMs: Long = 4000L) {
  private val buf = ArrayList<Bp>(1024)
  private var winStart = SystemClock.elapsedRealtime()

  /** Add one sample; returns an average when window ends, else null */
  fun add(sbp: Int, dbp: Int, now: Long = SystemClock.elapsedRealtime()): BpAvg? {
    if (sbp != 0 && dbp != 0) buf.add(Bp(sbp, dbp))
    return maybeFlush(now)
  }

  /** Force a flush if window elapsed; returns average or null if empty */
  private fun maybeFlush(now: Long): BpAvg? {
    if (now - winStart < windowMs) return null

    val n = buf.size
    val res =
            if (n > 0) {
              val meanSbp = buf.sumOf { it.sbp }.toDouble() / n
              val meanDbp = buf.sumOf { it.dbp }.toDouble() / n
              BpAvg(meanSbp.roundToInt(), meanDbp.roundToInt(), n)
            } else null

    buf.clear()
    winStart = now
    return res
  }

  fun reset() {
    buf.clear()
    winStart = SystemClock.elapsedRealtime()
  }
}

class OximetryIIDeviceController private constructor() : BleChangeObserver {
  val ppgSnapshot = MutableLiveData<PpgSnapshot>()

  // 4s averaged BP LiveData so UI can observe final result
  val bp4sAvg: MutableLiveData<BpAvg> = MutableLiveData()

  companion object {
    val instance: OximetryIIDeviceController by lazy { OximetryIIDeviceController() }
  }

  private val TAG = "OxiIIController"

  var deviceName: String = ""

  // connection state
  private val _connected = MutableLiveData(false)
  val connected: LiveData<Boolean> = _connected

  var connected_device: Bluetooth? = null
    private set
    get() {
      if (connected.value != true) return null
      return field
    }

  // real-time task
  private var model = 0
  private val rtHandler = Handler(Looper.getMainLooper())
  val rtTask = RtTask()

  inner class RtTask : Runnable {
    private var isRunning = false
    private val periodMs = 200L

    override fun run() {
      if (!isRunning) return
      if (connected.value != true || connected_device == null) {
        stop()
        return
      }
      BleServiceHelper.BleServiceHelper.oxyIIGetRtPpg(model)
      rtHandler.postDelayed(this, periodMs)
    }

    fun start() {
      if (isRunning) return
      if (connected.value != true || connected_device == null) {
        Log.d(TAG, "rtTask start requested but not connected; ignoring.")
        return
      }
      isRunning = true
      rtHandler.post(this)
      Log.d(TAG, "rtTask started.")
    }

    fun stop() {
      if (!isRunning) return
      isRunning = false
      rtHandler.removeCallbacks(this)
      Log.d(TAG, "rtTask stopped.")
    }
  }

  private val accumulator = PpgAccumulator()
  // ADDED: instance of our 1s aggregator
  private val bpAgg = BpWindowAggregator(4000L)

  fun initEventBus(activity: MainActivity) {
    // Observe real-time PPG packets
    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIRtPpg).observe(activity) { evt
      ->
      val data = evt.data as RtPpg
      // Guard: empty packet (you have some with size=0)
      if (data.irArray.isEmpty()) return@observe

      // --- static params (adjust to your device if needed) ---
      val ax: Short = 0
      val ay: Short = 0
      val az: Short = 0
      // Get calibration values from settings, fallback to defaults if not set
      val sbpCalib: Byte = (readPatientSPCalib(activity)?.toIntOrNull() ?: 0).toByte()
      val dbpCalib: Byte = (readPatientDBPCalib(activity)?.toIntOrNull() ?: 0).toByte()
      val useAcc = false
      val sampHz: Byte = 150.toByte() // 150 hz

      // --- streaming: feed every sample in this chunk ---
      for (ppgValue in data.irArray) {
        val out =
                BloodPressureNative.calcBp(
                        ppg = ppgValue,
                        acc1 = ax,
                        acc2 = ay,
                        acc3 = az,
                        sbpCalib = sbpCalib,
                        dbpCalib = dbpCalib,
                        useAcc = useAcc,
                        sampRate = sampHz,
                        reset = false
                )

        val sbp = out[0].toInt() and 0xFF
        val dbp = out[1].toInt() and 0xFF

        val avg = bpAgg.add(sbp, dbp)
        if (avg != null) {
          Log.d(TAG, "4s AVG => SBP: ${avg.sbp}, DBP: ${avg.dbp} (n=${avg.count})")
          bp4sAvg.value = avg
        }
      }

      // Keep your plotting buffer in sync (optional, for charts)
      accumulator.add(data.irArray, data.redArray)
      ppgSnapshot.value = accumulator.snapshot()
    }
  }

  fun getPpgSnapshot(): PpgSnapshot = accumulator.snapshot()
  fun clearPpgBuffer() = accumulator.clear()

  fun connectDevice(
          device: Bluetooth,
          serviceHelper: BleServiceHelper,
          appCtx: Context,
          lifecycle: Lifecycle
  ) {
    serviceHelper.setInterfaces(device.model)

    lifecycle.addObserver(BIOL(this, intArrayOf(device.model)))
    serviceHelper.stopScan()
    serviceHelper.connect(appCtx, device.model, device.device, false)

    this.connected_device = device
    device.name = deviceName
    model = device.model
  }

  override fun onBleStateChanged(model: Int, state: Int) {
    val connected = state == Ble.State.CONNECTED
    _connected.value = connected
    if (connected) {
      bpAgg.reset()
      rtTask.start()
    } else {
      rtTask.stop()
      bpAgg.reset()
    }
  }
}
