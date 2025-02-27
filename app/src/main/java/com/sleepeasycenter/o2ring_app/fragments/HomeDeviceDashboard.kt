package com.sleepeasycenter.o2ring_app.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.oxy.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.lepu.blepro.observer.BleChangeObserver
import com.sleepeasycenter.o2ring_app.OximetryDeviceController
import com.sleepeasycenter.o2ring_app.Status
import com.sleepeasycenter.o2ring_app.adapters.DeviceFileListViewAdapter
import com.sleepeasycenter.o2ring_app.databinding.FragmentHomeDashboardBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.launch

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

    private var adapter = DeviceFileListViewAdapter(arrayListOf())

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

        spo2Chart = binding.spo2Chart
        prChart = binding.prChart

        setupChart(spo2Chart,50f, 100f)
        setupChart(prChart, 40f, 170f)

        initEventBus()

        return view;
    }

    private fun setupChart(chart: LineChart, minY: Float? = null, maxY: Float? = null) {
        chart.description.isEnabled = false
        chart.setTouchEnabled(false)
        chart.isDragEnabled = false
        chart.setScaleEnabled(false)
        chart.setPinchZoom(false)

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(true)

        val yAxis = chart.axisLeft
        yAxis.setDrawGridLines(false)

        minY?.let { yAxis.axisMinimum = it }
        maxY?.let { yAxis.axisMaximum = it }

        chart.axisRight.isEnabled = false
    }

    private fun addEntry(chart: LineChart, value: Float?, entries: ArrayList<Entry>, label: String, color: Int) {
        value?.let {
            entries.add(Entry(timeIndex, it))
            timeIndex += 1  // Increment time index

            val dataSet = LineDataSet(entries, label)
            dataSet.color = color
            dataSet.valueTextSize = 10f
            dataSet.setDrawCircles(false)
            dataSet.setDrawValues(false)

            val lineData = LineData(dataSet)
            chart.data = lineData
            chart.notifyDataSetChanged()
            chart.invalidate()
        }
    }


    private fun initEventBus() {
        OximetryDeviceController.instance.oxyLevel.observe(viewLifecycleOwner) { value ->
            binding.tvOxy.text = value ?: "N/A"
            addEntry(spo2Chart, value.toFloatOrNull(), oxyEntries, "Oxygen Level", ColorTemplate.COLORFUL_COLORS[0])
        }

        OximetryDeviceController.instance.pulseRate.observe(viewLifecycleOwner) { value ->
            binding.tvPr.text = value ?: "N/A"
            addEntry(prChart, value.toFloatOrNull(), pulseEntries, "Pulse Rate", ColorTemplate.COLORFUL_COLORS[1])
        }

        OximetryDeviceController.instance.oxyPi.observe(viewLifecycleOwner) { value ->
            binding.tvPi.text = value ?: "N/A"
        }
    }




    override fun onBleStateChanged(model: Int, state: Int) {
        TODO("Not yet implemented")
    }

//     fun processAndUploadCsvFiles(){
//         CoroutineScope(MainScope()).launch {
//             var oxyfiles = OximetryDeviceController.instance.oxyfiles.value
//
//             var csvFiles: ArrayList<String> = arrayListOf();
//
//             for ((index, oxyFile) in oxyfiles!!.withIndex()) {
//                 binding.txtStatusText.setText("Converting to csv... (${index + 1} / ${oxyfiles.size})")
//                 binding.barStatusProgress.min = index;
//                 binding.barStatusProgress.max = oxyfiles.size;
//                 binding.barStatusProgress.progress = index;
//
//                 csvFiles += convertToCsv(oxyFile)
//                 binding.barStatusProgress.progress = index + 1;
//             }
//         }
//    }
}