package com.sleepeasycenter.o2ring_app.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.sleepeasycenter.o2ring_app.OximetryDeviceController
import com.sleepeasycenter.o2ring_app.R
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DetailedSessionFragment : Fragment() {

    private lateinit var detailedSessionName: TextView
    private lateinit var oxygenLevelChart: LineChart
    private lateinit var pulseRateChart: LineChart
    private var filename: String? = null
    val TAG: String = "DetailedSession"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detailed_session, container, false)
        detailedSessionName = view.findViewById(R.id.detailedSessionHeader)
        oxygenLevelChart = view.findViewById(R.id.oxygenLevelChart)
        pulseRateChart = view.findViewById(R.id.pulseRateChart)

        // Retrieve the argument using Safe Args
        filename = arguments?.let { DetailedSessionFragmentArgs.fromBundle(it).csvFilename }


        observeCSVData()

        return view
    }


    private fun observeCSVData() {
        OximetryDeviceController.instance.csvfiles.observe(viewLifecycleOwner) { csvDataArray ->
            if (csvDataArray.isNotEmpty()) {

                val timestamp = filename?.substringAfterLast("_")?.substringBeforeLast(".")

                val csvData = csvDataArray.find { data ->
                    // Convert startTime to a formatted string and compare
                    val date = Instant.ofEpochSecond(data.startTime)
                    val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                    val dateString =
                        formatter.format(LocalDateTime.ofInstant(date, ZoneId.systemDefault()))
                    dateString == timestamp
                }
                if (csvData != null) {
                    Log.d(TAG, csvData.csv)
                    detailedSessionName.text = filename

                    val (oxygenLevelEntries, pulseRateEntries) = parseCSVData(csvData.csv)

                    // Set up Oxygen Level chart
                    setupChart(
                        oxygenLevelChart,
                        oxygenLevelEntries,
                        80f,
                        100f,
                        "Oxygen Level (%)",
                        Color.parseColor("#07a4d9"),
                        R.drawable.gradient_fill_spo2
                    )

                    // Set up Pulse Rate chart
                    setupChart(
                        pulseRateChart,
                        pulseRateEntries,
                        null,
                        null,
                        "Pulse Rate (bpm)",
                        Color.parseColor("#bf1728"),
                        R.drawable.gradient_fill_pr
                    )
                } else {
                    detailedSessionName.text = "No matching CSV data found."
                }
            } else {
                detailedSessionName.text = "No CSV data available."
            }
        }
    }

    private fun parseCSVData(csvData: String): Pair<List<Entry>, List<Entry>> {
        val oxygenLevelEntries = ArrayList<Entry>()
        val pulseRateEntries = ArrayList<Entry>()

        val lines = csvData.split("\n")
        var timeIndex = 0f

        // Skip the header line
        for (i in 1 until lines.size) {
            val columns = lines[i].split(",")
            if (columns.size >= 3) {
                val oxygenLevel = columns[1].toFloatOrNull()
                val pulseRate = columns[2].toFloatOrNull()

                if (oxygenLevel != null && pulseRate != null) {
                    oxygenLevelEntries.add(Entry(timeIndex, oxygenLevel))
                    pulseRateEntries.add(Entry(timeIndex, pulseRate))
                    timeIndex += 1f // Increment time index
                }
            }
        }

        return Pair(oxygenLevelEntries, pulseRateEntries)
    }

    private fun setupChart(
        chart: LineChart,
        entries: List<Entry>,
        minY: Float? = null,
        maxY: Float? = null,
        label: String,
        color: Int,
        gradient: Int
    ) {
        val dataSet = LineDataSet(entries, label).apply {
            this.color = color
            valueTextColor = Color.BLACK
            lineWidth = 2.5f
            setCircleColor(Color.BLACK)
            circleRadius = 2f
            setDrawCircleHole(true)
            setDrawFilled(true)
            fillDrawable = ContextCompat.getDrawable(requireContext(), gradient)
        }

        val lineData = LineData(dataSet)
        chart.data = lineData

        // Customize X-axis
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f

        // Customize Y-axis
        val yAxis = chart.axisLeft
        yAxis.setDrawGridLines(false)

        minY?.let { yAxis.axisMinimum = minY }
        maxY?.let { yAxis.axisMaximum = maxY }

        // Disable right Y-axis
        chart.axisRight.isEnabled = false

        // Refresh the chart
        chart.invalidate()
    }


}

