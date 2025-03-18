package com.sleepeasycenter.o2ring_app.fragments

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.lepu.blepro.ext.oxy.*
import com.lepu.blepro.observer.BleChangeObserver
import com.sleepeasycenter.o2ring_app.OximetryDeviceController
import com.sleepeasycenter.o2ring_app.databinding.FragmentHomeDashboardBinding
import com.sleepeasycenter.o2ring_app.R
import com.sleepeasycenter.o2ring_app.utils.readPatientOxyBaseline
import com.sleepeasycenter.o2ring_app.utils.readPatientPRBaseline
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.sleepeasycenter.o2ring_app.MainActivity

/**
 * A simple [Fragment] subclass.
 * Use the [HomeDeviceDashboard.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeDeviceDashboard : Fragment(), BleChangeObserver {

    public val TAG: String = "HomeDashboard"
    private var _binding: FragmentHomeDashboardBinding? = null;
    private val binding get() = _binding!!;

    private lateinit var spo2Chart: LineChart
    private lateinit var prChart: LineChart
    private lateinit var motionChart: LineChart
    private val oxyEntries = ArrayList<Entry>()
    private val pulseEntries = ArrayList<Entry>()
    private val motionEntries = ArrayList<Entry>()

    private var timeIndex = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeDashboardBinding.inflate(inflater, container, false);

        // Inflate the layout for this fragment
        val view = binding.root;

        OximetryDeviceController.instance.rtTask.start()
        OximetryDeviceController.instance.rtTask.run()


        // set up charts
        spo2Chart = binding.spo2Chart
        prChart = binding.prChart
        motionChart = binding.motionChart

        setupLineChart(spo2Chart, 70f, 100f, readPatientPRBaseline(activity as Activity), false)
        setupLineChart(prChart, 40f, 160f, readPatientOxyBaseline(activity as Activity), false)
        setupLineChart(motionChart, 0f, null, null, true)

        initEventBus()

        return view;
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
        xAxis.setDrawLabels(false)

        val yAxis = chart.axisLeft
        yAxis.setDrawGridLines(!hideGridLabels)
        yAxis.setDrawLabels(!hideGridLabels)

        minY?.let { yAxis.axisMinimum = it }
        maxY?.let { yAxis.axisMaximum = it }

        limitValue?.let {
            val limitLine = LimitLine(it.toFloat(), "").apply {
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
            val entry = Entry(timeIndex, it)
            entries.add(Entry(timeIndex, it))
            timeIndex += 1

            val data = chart.data
            if (data == null) {
                val dataSet = LineDataSet(entries, label).apply {
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

            val visibleRange = 1000f

            chart.setVisibleXRangeMaximum(visibleRange)

            chart.moveViewToX(timeIndex + (visibleRange / 2))

            chart.invalidate()
        }
    }

    private fun initEventBus() {
        OximetryDeviceController.instance.oxyLevel.observe(viewLifecycleOwner) { value ->
            binding.tvOxy.text = value ?: "--"
            determineTextColor(value)
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
            addLineEntry(
                prChart,
                value.toFloatOrNull(),
                pulseEntries,
                "Pulse Rate",
                Color.parseColor("#bf1728"),
                R.drawable.gradient_fill_pr
            )
        }

        OximetryDeviceController.instance.oxyPi.observe(viewLifecycleOwner) { value ->
            binding.tvPi.text = ("PI: ${value ?: "--"}")
        }

        OximetryDeviceController.instance.motion.observe(viewLifecycleOwner) { value ->
            addLineEntry(
                motionChart,
                value.toFloatOrNull(),
                motionEntries,
                "Motion",
                Color.parseColor("#ff8624"),
                R.drawable.gradient_fill_motion
            )
        }

    }

    private fun determineTextColor(dataVal: String) {
        val value = dataVal.toFloatOrNull()
        val baseline = readPatientOxyBaseline(requireActivity())?.toFloat()

        if (value != null && baseline != null) {
            if (value < (baseline - 4f)) {
                binding.tvOxy.setTextColor(Color.parseColor("#ed2e11")) // critical low
            } else if (value in ((baseline - 4f) .. (baseline + 4f))) {
                binding.tvOxy.setTextColor(Color.parseColor("#f7d00c")) // warning
            } else {
                binding.tvOxy.setTextColor(Color.parseColor("#06d656")) // normal
            }
        } else {
            binding.tvOxy.setTextColor(Color.GRAY) // Default color if null
        }
    }

    fun updateLimitLine(chart: LineChart,newBaseline: Float?) {

        if (newBaseline == null) return

        val yAxis = chart.axisLeft
        yAxis.removeAllLimitLines()

        val limitLine = LimitLine(newBaseline, "").apply {
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

    // IMPORTANT: only 1 rtTask can run at all times, start/stop it as needed when leaving/returning to this fragment
    override fun onResume() {
        super.onResume()

        // start oxy parameter tracking when returning to this fragment
        OximetryDeviceController.instance.rtTask.start()

        updateLimitLine(spo2Chart, readPatientOxyBaseline(requireActivity())?.toFloat())
        updateLimitLine(prChart, readPatientPRBaseline(requireActivity())?.toFloat())
    }

    override fun onPause() {
        super.onPause()

        // stop any parameter tracking when leaving (pausing) this fragment
        OximetryDeviceController.instance.rtTask.stop()
    }

}