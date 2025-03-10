package com.sleepeasycenter.o2ring_app.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.oxy.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.lepu.blepro.observer.BleChangeObserver
import com.sleepeasycenter.o2ring_app.OximetryDeviceController
import com.sleepeasycenter.o2ring_app.databinding.FragmentHomeDashboardBinding
import com.sleepeasycenter.o2ring_app.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.sleepeasycenter.o2ring_app.utils.bleState

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
    private lateinit var piChart: LineChart
    private val oxyEntries = ArrayList<Entry>()
    private val pulseEntries = ArrayList<Entry>()
    private val piEntries = ArrayList<Entry>()


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

        OximetryDeviceController.instance.rtTask.run()

        // set up line charts
        spo2Chart = binding.spo2Chart
        prChart = binding.prChart

        setupChart(spo2Chart,60f, 100f, 95f)
        setupChart(prChart, 40f, 160f, 50f)

        initView()
        initEventBus()

        return view;
    }

    private fun setupChart(chart: LineChart, minY: Float? = null, maxY: Float? = null, limitValue: Float? = null) {
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
        yAxis.setDrawGridLines(true)

        minY?.let { yAxis.axisMinimum = it }
        maxY?.let { yAxis.axisMaximum = it }

        limitValue?.let {
            val limitLine = LimitLine(it, "").apply{
                lineWidth = 1f
                lineColor = Color.DKGRAY
                enableDashedLine(10f,10f,0f)
                textSize= 12f
            }
            yAxis.addLimitLine(limitLine)
        }


        chart.axisRight.isEnabled = false
    }

    private fun addEntry(chart: LineChart, value: Float?, entries: ArrayList<Entry>, label: String, color: Int, gradient: Int ) {
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

    fun initView() {
        binding.bleName.text = OximetryDeviceController.instance.deviceName
        bleState.observe(viewLifecycleOwner) {
            if (it) {
                binding.oxyBleState.setImageResource(R.mipmap.bluetooth_ok)
            } else {
                binding.oxyBleState.setImageResource(R.mipmap.bluetooth_error)
            }
        }
    }

    private fun initEventBus() {
        OximetryDeviceController.instance.oxyLevel.observe(viewLifecycleOwner) { value ->
            binding.tvOxy.text = value ?: "N/A"
            determineTextColor(value)
            addEntry(spo2Chart, value.toFloatOrNull(), oxyEntries,"Oxygen Level", Color.BLUE, R.drawable.gradient_fill_spo2)
        }

        OximetryDeviceController.instance.pulseRate.observe(viewLifecycleOwner) { value ->
            binding.tvPr.text = value ?: "N/A"
            addEntry(prChart, value.toFloatOrNull(), pulseEntries, "Pulse Rate", Color.RED, R.drawable.gradient_fill_pr)
        }

        OximetryDeviceController.instance.oxyPi.observe(viewLifecycleOwner) { value ->
            binding.tvPi.text = value ?: "N/A"
        }
    }

    private fun determineTextColor(dataVal: String) {
        val value = dataVal.toFloatOrNull()
        if (value != null) {
            if (value < 90) { // Adjust threshold as needed
                binding.tvOxy.setTextColor(Color.RED) // Critical low
            } else if (value in 90f..94f) {
                binding.tvOxy.setTextColor(Color.YELLOW) // Warning
            } else {
                binding.tvOxy.setTextColor(Color.GREEN) // Normal
            }
        } else {
            binding.tvOxy.setTextColor(Color.GRAY) // Default color if null
        }
    }

    override fun onBleStateChanged(model: Int, state: Int) {
        TODO("Not yet implemented")
    }


}