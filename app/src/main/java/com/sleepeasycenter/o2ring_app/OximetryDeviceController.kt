package com.sleepeasycenter.o2ring_app

import android.content.Context
import android.util.Log
import android.widget.Toast
import android.os.Handler
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.mikephil.charting.data.Entry
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.ext.oxy.DeviceInfo
import com.lepu.blepro.ext.oxy.OxyFile
import com.lepu.blepro.ext.oxy.RtParam
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import com.sleepeasycenter.o2ring_app.api.SleepEasyAPI
import com.sleepeasycenter.o2ring_app.utils.OxyCsvData
import com.sleepeasycenter.o2ring_app.utils.bleState
import com.sleepeasycenter.o2ring_app.utils.convertToCsv
import com.sleepeasycenter.o2ring_app.utils.readPatientId
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.yield
import no.nordicsemi.android.ble.observer.ConnectionObserver
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class Status {
    NEUTRAL, DOWNLOADING, CONVERTING, UPLOADING
}

class OximetryDeviceController// Private constructor prevents instantiation
private constructor() : BleChangeObserver {
    var _connected: MutableLiveData<Boolean> = MutableLiveData(false)
    var connected: LiveData<Boolean> = _connected

    var connected_device: Bluetooth? = null
        private set
        get() {
            if (connected.value != true) return null
            return field
        }

    var _filenames: MutableLiveData<Array<String>> = MutableLiveData(arrayOf())
    var filenames: LiveData<Array<String>> = _filenames
    var _csvfiles: MutableLiveData<Array<OxyCsvData>> = MutableLiveData(arrayOf())
    val csvfiles: LiveData<Array<OxyCsvData>> get() = _csvfiles


    var status: MutableLiveData<Status> = MutableLiveData(Status.NEUTRAL)

    var progress: MutableLiveData<Int> = MutableLiveData(0)
    var progress_min: MutableLiveData<Int> = MutableLiveData(0)
    var progress_max: MutableLiveData<Int> = MutableLiveData(0)

    var oxyLevel: MutableLiveData<String> = MutableLiveData()
    var pulseRate: MutableLiveData<String> = MutableLiveData()
    var motion: MutableLiveData<String> = MutableLiveData()
    var oxyPi: MutableLiveData<String> = MutableLiveData()
    var currentState: MutableLiveData<Int> = MutableLiveData(1)

    var deviceName: String = ""

    val TAG: String = "OxiController"

    val oxyEntries = ArrayList<Entry>()
    val pulseEntries = ArrayList<Entry>()

    private var currentFileIndex: Int = 0;
    var timeIndex = 0f

    // vars to do real-time data collection
    private var model = 0
    private var rtHandler = Handler()
    public var rtTask = RtTask()

    // call this to get real-time data
    inner class RtTask: Runnable {
        private var isRunning: Boolean = false

        override fun run() {
            if (isRunning) {
                rtHandler.post(this)
                BleServiceHelper.BleServiceHelper.oxyGetRtParam(model)
            }
        }

        fun start() {
            if (!isRunning) {
                isRunning = true
                rtHandler.post(this)
                Log.d(TAG, "rtTask started.")
            }
        }

        fun stop() {
            if (isRunning) {
                isRunning = false
                rtHandler.removeCallbacks(this)
                Log.d(TAG, "rtTask stopped.")
            }
        }
    }


    // Initialises the event bus for this class
    fun initEventBus(mainActivity: MainActivity) {

        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyRtParamData)
            .observe(mainActivity) {
                val data = it.data as RtParam
                Log.d("RTDATA", "Received Data: SpO2=${data.spo2}, PR=${data.pr}, PI=${data.pi}, Motion=${data.vector}")


                if ((data.spo2 in 1..149) || (data.pr in 1..349)) {
                    oxyLevel.value = data.spo2.toString()
                    pulseRate.value = data.pr.toString()
                    oxyPi.value = data.pi.toString()
                }
                else {
                    rtTask.stop()
                    oxyLevel.value = "--"
                    pulseRate.value = "--"
                    oxyPi.value = "--"
                }

                motion.value = data.vector.toString()
            }

        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxySyncDeviceInfo)
            .observe(mainActivity) {

                val types = it.data as Array<String>
                for (type in types) {
                    Log.d(TAG, "$type success")
                }
            }

        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyInfo).observe(mainActivity) {
            val data = it.data as DeviceInfo
            val list = data.fileList.split(",")
            val filtered = list.filter { it != ""; }.toTypedArray()

            Log.d(TAG, "Current state: ${data.curState}")
            currentState.value = data.curState
            _filenames.postValue(filtered)
            Log.d("FILENAMES", filtered.toString())
            Log.d(TAG, "Found files: " + (filtered.joinToString(",") ?: ""))

            // Start reading files
            currentFileIndex = 0;
            status.postValue(Status.DOWNLOADING)
            BleServiceHelper.BleServiceHelper.oxyReadFile(
                connected_device!!.model,
                filtered[currentFileIndex]
            )
        }

        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Oxy.EventOxyReadFileComplete)
            .observe(mainActivity) {

                val data = it.data as OxyFile;

                val array: Array<OxyCsvData> = _csvfiles.value ?: arrayOf();
                // Immediately convert to CSV. the it.data instance is reused by the LiveEventBUs
                val newArray = array.plusElement(OxyCsvData(convertToCsv(data), data.startTime));
                _csvfiles.postValue(newArray)
                val totalFiles = (filenames.value?.size ?: 0)
                Log.d(TAG, "Read files " + (currentFileIndex + 1) + " of " + totalFiles)
                if (connected.value!!) {
                    if (currentFileIndex < totalFiles - 1) {
                        currentFileIndex++
                        val nextFile = _filenames.value!![currentFileIndex]
                        Log.d(TAG, "Start Reading file: $nextFile...");
                        // Read next file
                        status.postValue(Status.DOWNLOADING)
                        BleServiceHelper.BleServiceHelper.oxyReadFile(
                            connected_device!!.model,
                            nextFile
                        )
                                progress.postValue(currentFileIndex)
                                progress_min.postValue(0)
                        progress_max.postValue(totalFiles + 1)

                    } else {
                        status.postValue(Status.NEUTRAL)
                        // Finished reading files
                        progress.postValue(totalFiles + 1)
                    }
                }

            }

        LiveEventBus.get<Int>(EventMsgConst.Ble.EventBleDeviceDisconnectReason)
            .observe(mainActivity) {
                // ConnectionObserver.REASON_NOT_SUPPORTED: SDK will not auto reconnect device, services error, try to reboot device
                val reason = when (it) {
                    ConnectionObserver.REASON_UNKNOWN -> "The reason of disconnection is unknown."
                    ConnectionObserver.REASON_SUCCESS -> "The disconnection was initiated by the user."
                    ConnectionObserver.REASON_TERMINATE_LOCAL_HOST -> "The local device initiated disconnection."
                    ConnectionObserver.REASON_TERMINATE_PEER_USER -> "The remote device initiated graceful disconnection."
                    ConnectionObserver.REASON_LINK_LOSS -> "This reason will only be reported when ConnectRequest.shouldAutoConnect() was called and connection to the device was lost. Android will try to connect automatically."
                    ConnectionObserver.REASON_NOT_SUPPORTED -> "The device does not hav required services."
                    ConnectionObserver.REASON_TIMEOUT -> "The connection timed out. The device might have reboot, is out of range, turned off or doesn't respond for another reason."
                    else -> "disconnect"
                }
                Toast.makeText(mainActivity, reason, Toast.LENGTH_SHORT).show()
                connected_device = null;
                _connected.value = false;
            }




    }

    fun connectDevice(
        device: Bluetooth,
        serviceHelper: BleServiceHelper,
        appCtx: Context,
        lifecycle: Lifecycle
    ) {
        serviceHelper.setInterfaces(device.model)
        // add observer(ble state)
        lifecycle.addObserver(BIOL(this, intArrayOf(device.model)))
        serviceHelper.stopScan()
        serviceHelper.connect(appCtx, device.model, device.device, false)

        this.connected_device = device
        device.name = deviceName
        model = device.model
    }

    fun refreshFiles() {
        if (connected.value == true && connected_device != null) {
            Log.d(TAG, "Refreshing files...")

            // Clear existing files and reset state
            _filenames.value = emptyArray()
            _csvfiles.value = emptyArray()
            currentFileIndex = 0


            // Trigger the file detection process
            BleServiceHelper.BleServiceHelper.oxyGetInfo(connected_device!!.model)

        } else {
            Log.d(TAG, "Cannot refresh files: Device is not connected.")
        }
    }

    override fun onBleStateChanged(model: Int, state: Int) {
        Log.d(TAG, "model $model, state: $state")

        val connected =state == Ble.State.CONNECTED;
        _connected.value = connected
        Log.d(TAG, "Connected? " + connected)
        if (connected){
            BleServiceHelper.BleServiceHelper.oxyGetInfo(model);
        }
    }

    private fun uploadFile(
        file: File,
        activity: FragmentActivity,
        onError: (() -> Unit)?,
        onSuccess: (() -> Unit)?
    ) {
        val filePart = MultipartBody.Part.createFormData(
            "csv",
            file.name,
            file.asRequestBody("text/csv".toMediaTypeOrNull())
        )

        readPatientId(activity)?.let { patient_id ->
            Log.d(TAG, "Read patient id!")
            val patientIdPart = MultipartBody.Part.createFormData("patient_id", patient_id);
            val call = SleepEasyAPI.getService().uploadO2RingData(filePart, patientIdPart)
            val context = this;
            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, res: Response<ResponseBody>) {
                    res.errorBody()?.let { errBody ->
                        val s = errBody.string();
                        if (res.code() == 409) {

                        } else {
                            Log.d(TAG, "Upload Error Response:\n" + s)
                            Toast.makeText(
                                activity,
                                "Error while uploading data:\n" + s,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    onSuccess?.invoke()

                }

                override fun onFailure(call: Call<ResponseBody>, err: Throwable) {
                    Log.e(TAG, "Unexpected error while uploading data:\n" + err.toString(), err);
                    Toast.makeText(
                        activity,
                        "Unexpected error while uploading data:\n" + err.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                    onError?.invoke()
                }

            })

            return
        } ?: run {
            onError?.invoke();
            Toast.makeText(activity, "No patient id set!", Toast.LENGTH_LONG).show();
        }


    }

    suspend fun uploadToServer(activity: FragmentActivity) {
        Log.d(TAG, "Converting to csv...")
        val csvfile_data = _csvfiles.value!!;
        var csvFiles: ArrayList<File> = arrayListOf();
        status.postValue(Status.CONVERTING)
        progress.postValue(0)
        progress_min.postValue(0)
        progress_max.postValue(csvfile_data.size)
        Thread.sleep(1000)
        for ((index, oxyCsvData) in csvfile_data.withIndex()) {
            Log.d(TAG, "Converting to csv... ${index} / ${csvfile_data.size}")
            progress.postValue(index)
            val contents = oxyCsvData.csv
            val date = Instant.ofEpochSecond(oxyCsvData.startTime);
            LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            val date_s = formatter.format(LocalDateTime.ofInstant(date, ZoneId.systemDefault()));

            val externalDir = activity.getExternalFilesDir(null)
            var file = File(externalDir, "O2 Ring_${date_s}.csv")
            file.createNewFile()
            file.writeText(contents)
            csvFiles += file;
            progress.postValue(index + 1)
        }
        status.postValue(Status.UPLOADING)
        var remaining = csvFiles.size
        var failed = 0;
        for (csvFile in csvFiles) {
            Log.d(TAG, "Uploading file ${csvFile.name}")
            uploadFile(csvFile, activity, { remaining--; Log.d(TAG,"Upload failed! Remaining: $remaining"); failed++; }, { remaining-- ; Log.d(TAG,"Upload success! Remaining: $remaining")});
        }
        while (remaining > 0) {
            yield()
        }
        status.postValue(Status.NEUTRAL)
        if (failed == 0){
            Toast.makeText(activity, "Upload completed successfully!", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        var TAG: String = "OximetryDeviceManager(Singleton)"
        private var _instance: OximetryDeviceController? = null
            // Public method to provide access to the instance
            get() {
                if (field == null) {
                    field = OximetryDeviceController()
                }
                return field
            }
        val instance: OximetryDeviceController get() = _instance!!;
    }
}
