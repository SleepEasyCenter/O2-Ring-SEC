package com.sleepeasycenter.o2ring_app.fragments

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.lepu.blepro.ext.oxy.*
import com.lepu.blepro.observer.BleChangeObserver
import com.sleepeasycenter.o2ring_app.OximetryDeviceController
import com.sleepeasycenter.o2ring_app.OximetryIIDeviceController
import com.sleepeasycenter.o2ring_app.PpgSnapshot
import com.sleepeasycenter.o2ring_app.R
import com.sleepeasycenter.o2ring_app.databinding.FragmentHomeDashboardBinding
import com.sleepeasycenter.o2ring_app.utils.readPatientOxyBaseline
import com.sleepeasycenter.o2ring_app.utils.readPatientPRBaseline
import kotlin.math.max

/**
 * y[n] = a*(y[n-1] + x[n]
 * - x[n-1]) ; tau ~ 0.8 s keeps pulse, removes Direct Current (DC)
 */
class HighPass(fs: Float, tauSec: Float = 0.8f) {
    private val a = (tauSec / (tauSec + 1f / fs))
    private var yPrev = 0f
    private var xPrev = 0f
    fun filter(x: Float): Float {
        val y = a * (yPrev + x - xPrev)
        yPrev = y
        xPrev = x
        return y
    }
}

/**
 * One-pole Low-Pass filter Keeps slow changes (heart waveform), removes sharp noise. y[n] = y[n-1]
 * + alpha * (x[n]
 * - y[n-1]) alpha = dt / (RC + dt), RC = 1 / (2π fc)
 */
class LowPass(private val fs: Float, cutoffHz: Float = 8f) {
    private val alpha: Float
    private var yPrev = 0f

    init {
        val dt = 1f / fs
        val rc = 1f / (2f * Math.PI.toFloat() * cutoffHz)
        alpha = dt / (rc + dt)
    }

    fun filter(x: Float): Float {
        yPrev += alpha * (x - yPrev)
        return yPrev
    }
}

/**
 * A simple [Fragment] subclass. Use the [HomeDeviceDashboard.newInstance] factory method to create
 * an instance of this fragment.
 */
class HomeDeviceDashboard : Fragment(), BleChangeObserver {

    public val TAG: String = "HomeDashboard"
    private var _binding: FragmentHomeDashboardBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var spo2Chart: LineChart
    private lateinit var prChart: LineChart
    private lateinit var motionChart: LineChart
    private lateinit var ppgChart: LineChart
    private lateinit var bpChart: LineChart
    private val ppgEntries = ArrayList<Entry>()
    private val oxyEntries = ArrayList<Entry>()
    private val pulseEntries = ArrayList<Entry>()
    private val motionEntries = ArrayList<Entry>()
    private val bpEntries = ArrayList<Entry>()

    private var lastPlottedN = 0
    private var xCursor = 0f // seconds
    private val fs = 200f // from docs
    private val hp = HighPass(fs)
    private val lp = LowPass(fs)

    private var startTime = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeDashboardBinding.inflate(inflater, container, false)

        // Inflate the layout for this fragment
        val view = binding.root

        // set up charts
        spo2Chart = binding.spo2Chart
        prChart = binding.prChart
        motionChart = binding.motionChart
        ppgChart = binding.ppgChart
        bpChart = binding.bpChart

        setupLineChart(spo2Chart, 70f, 100f, readPatientPRBaseline(activity as Activity), false)
        setupLineChart(prChart, 40f, 160f, readPatientOxyBaseline(activity as Activity), false)
        setupLineChart(motionChart, 0f, null, null, true)
        setupPpgChart(ppgChart)
        setupBPChart(bpChart, 40f, 180f, null, false)

        initEventBus()

        return view
    }

        private fun setupLineChart(
            chart: LineChart,
            minY: Float? = null,
            maxY: Float? = null,
            limitValue: String? = null,
            hideGridLabels: Boolean
    ) {
        chart.description.isEnabled = false
        chart.setTouchEnabled(false)
        chart.isDragEnabled = false
        chart.setScaleEnabled(false)
        chart.setPinchZoom(false)

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawLabels(true)
        xAxis.granularity = 1f

        val yAxis = chart.axisLeft
        yAxis.setDrawGridLines(!hideGridLabels)
        yAxis.setDrawLabels(!hideGridLabels)

        minY?.let { yAxis.axisMinimum = it }
        maxY?.let { yAxis.axisMaximum = it }

        limitValue?.let {
            val limitLine =
                    LimitLine(it.toFloat(), "").apply {
                        lineWidth = 1f
                        lineColor = Color.DKGRAY
                        enableDashedLine(20f, 20f, 0f)
                        textSize = 12f
                    }
            yAxis.addLimitLine(limitLine)
        }

        chart.axisRight.isEnabled = false
        chart.legend.isEnabled = false
    }

    private fun setupBPChart(
            chart: LineChart,
            minY: Float? = null,
            maxY: Float? = null,
            limitValue: String? = null,
            hideGridLabels: Boolean
    ) {
        chart.description.isEnabled = false
        chart.setTouchEnabled(false)
        chart.isDragEnabled = false
        chart.setScaleEnabled(false)
        chart.setPinchZoom(false)

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawLabels(true)
        xAxis.granularity = 1f

        val yAxis = chart.axisLeft
        yAxis.setDrawGridLines(!hideGridLabels)
        yAxis.setDrawLabels(!hideGridLabels)

        minY?.let { yAxis.axisMinimum = it }
        maxY?.let { yAxis.axisMaximum = it }

        limitValue?.let {
            val limitLine =
                    LimitLine(it.toFloat(), "").apply {
                        lineWidth = 1f
                        lineColor = Color.DKGRAY
                        enableDashedLine(20f, 20f, 0f)
                        textSize = 12f
                    }
            yAxis.addLimitLine(limitLine)
        }

        chart.axisRight.isEnabled = false
        chart.legend.isEnabled = false
    }

    private fun setupPpgChart(chart: LineChart) {
        chart.description.isEnabled = false
        chart.setTouchEnabled(false)
        chart.setScaleEnabled(false)
        chart.setPinchZoom(false)
        chart.axisRight.isEnabled = false
        chart.legend.isEnabled = false

        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.granularity = 0.1f

        chart.axisLeft.setDrawGridLines(true)
        chart.axisLeft.setDrawLabels(true)

        chart.axisLeft.axisMinimum = -400f
        chart.axisLeft.axisMaximum = 400f
    }

    // ADDED: ensure we have (or create) a dataset by label with the right style
    private fun ensureDataSet(
            chart: LineChart,
            label: String,
            color: Int,
            gradient: Int? = null
    ): LineDataSet {
        val data = chart.data ?: LineData().also { chart.data = it }
        // Try find by label
        for (i in 0 until data.dataSetCount) {
            val ds = data.getDataSetByIndex(i)
            if (ds.label == label && ds is LineDataSet) return ds
        }
        // Create new
        return LineDataSet(ArrayList<Entry>(), label)
                .apply {
                    this.color = color
                    lineWidth = 2.5f
                    setDrawCircles(false)
                    setDrawValues(false)
                    setDrawFilled(gradient != null)
                    gradient?.let { fillDrawable = ContextCompat.getDrawable(requireContext(), it) }
                }
                .also { data.addDataSet(it) }
    }

    // ADDED: add one time point containing both SBP & DBP on the same chart
    private fun addBpPoint(sbp: Float, dbp: Float, atMillis: Long = System.currentTimeMillis()) {
        val elapsedSec = (atMillis - startTime) / 1000f

        val sbpDs =
                ensureDataSet(
                        bpChart,
                        label = "SBP",
                        color = Color.parseColor("#e74c3c"), // red-ish for SBP
                        gradient = R.drawable.gradient_fill_pr // reuse any gradient you like
                )
        val dbpDs =
                ensureDataSet(
                        bpChart,
                        label = "DBP",
                        color = Color.parseColor("#3498db"), // blue-ish for DBP
                        gradient = R.drawable.gradient_fill_spo2
                )

        sbpDs.addEntry(Entry(elapsedSec, sbp))
        dbpDs.addEntry(Entry(elapsedSec, dbp))

        val data = bpChart.data!!
        data.notifyDataChanged()
        bpChart.notifyDataSetChanged()

        // show ~2 minutes, adjust if you want
        bpChart.setVisibleXRangeMaximum(120f)
        bpChart.moveViewToX(elapsedSec)
        bpChart.invalidate()
    }

    // adds real-time data values into dashboard linecharts
    private fun addLineEntry(
            chart: LineChart,
            value: Float?,
            entries: ArrayList<Entry>,
            label: String,
            color: Int,
            gradient: Int
    ) {
        value?.let {
            val elapsedSec = (System.currentTimeMillis() - startTime) / 1000f
            val entry = Entry(elapsedSec, it)
            entries.add(entry)

            val data = chart.data
            if (data == null) {
                val dataSet =
                        LineDataSet(entries, label).apply {
                            this.color = color
                            lineWidth = 2.5f
                            valueTextSize = 10f
                            setDrawCircles(false)
                            setDrawValues(false)
                            setDrawFilled(true)
                            fillDrawable = ContextCompat.getDrawable(requireContext(), gradient)
                        }

                chart.data = LineData(dataSet)
            } else {
                val dataSet = data.getDataSetByIndex(0) as LineDataSet
                dataSet.addEntry(entry)

                data.notifyDataChanged()
                chart.notifyDataSetChanged()
            }

            chart.setVisibleXRangeMaximum(15f)
            chart.moveViewToX(elapsedSec)
            chart.invalidate()
        }
    }

    private fun initEventBus() {
        OximetryDeviceController.instance.oxyLevel.observe(viewLifecycleOwner) { value ->
            binding.tvOxy.text = value ?: "--"
            determineTextColor(value)
            if (value != null)
                    addLineEntry(
                            spo2Chart,
                            value.toFloatOrNull(),
                            oxyEntries,
                            "Oxygen Level",
                            Color.parseColor("#07a4d9"),
                            R.drawable.gradient_fill_spo2
                    )
        }

        OximetryDeviceController.instance.pulseRate.observe(viewLifecycleOwner) { value ->
            binding.tvPr.text = value ?: "--"
            if (value != null)
                    addLineEntry(
                            prChart,
                            value.toFloatOrNull(),
                            pulseEntries,
                            "Pulse Rate",
                            Color.parseColor("#bf1728"),
                            R.drawable.gradient_fill_pr
                    )
        }

        OximetryDeviceController.instance.motion.observe(viewLifecycleOwner) { value ->
            if (value != null)
                    addLineEntry(
                            motionChart,
                            value.toFloatOrNull(),
                            motionEntries,
                            "Motion",
                            Color.parseColor("#ff8624"),
                            R.drawable.gradient_fill_motion
                    )
        }

        OximetryIIDeviceController.instance.ppgSnapshot.observe(viewLifecycleOwner) {
                snap: PpgSnapshot ->
            val n = snap.ir.size
            if (n <= lastPlottedN) return@observe

            // Lazy init dataset
            val data = (ppgChart.data ?: LineData().also { ppgChart.data = it })
            val dataSet =
                    if (data.dataSetCount == 0) {
                        LineDataSet(ppgEntries, "PPG (IR)")
                                .apply {
                                    color = Color.parseColor("#1f77b4")
                                    lineWidth = 1.5f
                                    setDrawCircles(false)
                                    setDrawValues(false)
                                    setDrawFilled(false) // important for “blue-style” line
                                }
                                .also { data.addDataSet(it) }
                    } else data.getDataSetByIndex(0) as LineDataSet

            // Plot every k-th sample to keep ~80 points/sec on the chart
            val k = maxOf(1, (fs / 80f).toInt()) // ~80 points/sec

            for (i in lastPlottedN until n step k) {
                val raw = snap.ir[i].toFloat()

                // High-pass to remove DC, then Low-pass to remove sharp noise  => band-pass
                val ac = hp.filter(raw)
                val bp = lp.filter(ac)

                val y = ac / 250f // tweak divisor to taste (150–400)

                xCursor += (k / fs) // advance time by k samples
                dataSet.addEntry(Entry(xCursor, y))
            }

            lastPlottedN = n

            data.notifyDataChanged()
            ppgChart.notifyDataSetChanged()

            val left = max(0f, xCursor - 10f)
            ppgChart.xAxis.axisMinimum = left
            ppgChart.xAxis.axisMaximum = left + 10f

            ppgChart.moveViewToX(xCursor)
            ppgChart.invalidate()

            // Optional numeric readout of the raw last value
            binding.tvPpg.text = snap.ir.lastOrNull()?.toString() ?: "--"
        }

        // Observe averaged BP and plot SBP + DBP on the same chart
        OximetryIIDeviceController.instance.bp4sAvg.observe(viewLifecycleOwner) { avg ->
            if (avg != null && avg.sbp != 0 && avg.dbp != 0) {
                // one point for SBP and one for DBP at the same timestamp
                addBpPoint(avg.sbp.toFloat(), avg.dbp.toFloat())
                binding.tvSbpUnit.text = "SBP: ${avg.sbp} mmHg"
                binding.tvDbpUnit.text = "DBP: ${avg.dbp} mmHg"
            }
        }
    }

    private fun determineTextColor(dataVal: String? = null) {
        if (dataVal == null) return
        val value = dataVal.toFloatOrNull()
        val baseline = readPatientOxyBaseline(requireActivity())?.toFloat()

        if (value != null && baseline != null) {
            if (value < (baseline - 4f)) {
                binding.tvOxy.setTextColor(Color.parseColor("#ed2e11")) // critical low
            } else if (value in ((baseline - 4f)..(baseline + 4f))) {
                binding.tvOxy.setTextColor(Color.parseColor("#f7d00c")) // warning
            } else {
                binding.tvOxy.setTextColor(Color.parseColor("#06d656")) // normal
            }
        } else {
            binding.tvOxy.setTextColor(Color.GRAY) // Default color if null
        }
    }

    fun updateLimitLine(chart: LineChart, newBaseline: Float?) {

        if (newBaseline == null) return

        val yAxis = chart.axisLeft
        yAxis.removeAllLimitLines()

        val limitLine =
                LimitLine(newBaseline, "").apply {
                    lineWidth = 1f
                    lineColor = Color.DKGRAY
                    enableDashedLine(20f, 20f, 0f)
                    textSize = 12f
                }
        yAxis.addLimitLine(limitLine)
        spo2Chart.invalidate()
    }

    override fun onBleStateChanged(model: Int, state: Int) {
        Log.d(TAG, "model $model, state: $state")
    }

    // IMPORTANT: only 1 rtTask can run at all times, start/stop it as needed when leaving/returning
    // to this fragment
    override fun onResume() {
        super.onResume()

        // start parameter tracking for the appropriate controller
        if (OximetryIIDeviceController.instance.connected.value == true) {
            OximetryIIDeviceController.instance.rtTask.start()
        } else {
            OximetryDeviceController.instance.rtTask.start()
        }

        updateLimitLine(spo2Chart, readPatientOxyBaseline(requireActivity())?.toFloat())
        updateLimitLine(prChart, readPatientPRBaseline(requireActivity())?.toFloat())
    }

    override fun onPause() {
        super.onPause()

        // stop any parameter tracking when leaving (pausing) this fragment
        OximetryDeviceController.instance.rtTask.stop()
        OximetryIIDeviceController.instance.rtTask.stop()
    }
}
