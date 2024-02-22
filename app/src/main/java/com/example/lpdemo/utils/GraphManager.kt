package com.example.lpdemo.utils
import android.os.Handler
import com.example.lpdemo.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

data class OxygenData(val value: Float, val timestamp: Long)

class GraphManager(private val lineChart: LineChart) {
    private val oxygenValues = ArrayList<OxygenData>()

    init {
        generateLineChart()
    }

    fun startUpdatingChart(externalValue: Float) {
        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                // Add a new value to the graph every 4 seconds (for demonstration)
                updateGraph(externalValue)

                // Repeat the process after 4 seconds
                handler.postDelayed(this, 4000)
            }
        }, 4000) // Start after 4 seconds
    }

    private fun updateGraph(newValue: Float) {
        // Generate a new timestamp
        val timestamp = System.currentTimeMillis()

        // Add the new value with its timestamp to the list of oxygen concentration values
        oxygenValues.add(OxygenData(newValue, timestamp))

        // Remove data older than 40 seconds
        val currentTime = System.currentTimeMillis()
        oxygenValues.removeAll { it.timestamp < currentTime - 40000 }

        // Update the line chart with the new data
        generateLineChart()
    }

    private fun generateLineChart() {
        // Limit the number of visible entries on the chart to 60 seconds
        lineChart.setVisibleXRangeMaximum(60000f)

        // Configure X-axis
        val xAxis = lineChart.xAxis
        xAxis.isEnabled = false // Disable drawing x-axis labels
        xAxis.setDrawGridLines(false) // Disable drawing grid lines
        xAxis.setDrawAxisLine(false) // Disable drawing axis line

        // Create entries for the chart
        val entries = ArrayList<Entry>()
        for (data in oxygenValues) {
            // Calculate the x-axis position based on the difference between current time and data timestamp
            val x = (System.currentTimeMillis() - data.timestamp).toFloat()
            entries.add(Entry(x, data.value))
        }

        // Create a gradient fill drawable
        val drawable = lineChart.context.getDrawable(R.drawable.gradient_fill)

        // Create a LineDataSet and configure it
        val dataSet = LineDataSet(entries, "Oxygen Concentration")
        dataSet.fillDrawable = drawable // Set the fill drawable to the gradient

        // Create a LineData object and set it to the chart
        val lineData = LineData(dataSet)
        lineChart.data = lineData

        // Refresh the chart
        lineChart.invalidate()
    }
}
