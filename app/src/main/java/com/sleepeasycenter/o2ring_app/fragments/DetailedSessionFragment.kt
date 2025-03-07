package com.sleepeasycenter.o2ring_app.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.sleepeasycenter.o2ring_app.OximetryDeviceController
import com.sleepeasycenter.o2ring_app.R
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DetailedSessionFragment : Fragment() {

    private lateinit var textView: TextView
    private var filename: String? = null
    val TAG: String = "DetailedSession"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detailed_session, container, false)
        textView = view.findViewById(R.id.csv_text_view)

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
                    val dateString = formatter.format(LocalDateTime.ofInstant(date, ZoneId.systemDefault()))
                    dateString == timestamp
                }
                if (csvData != null) {
                    Log.d(TAG, csvData.csv)
                    textView.text = csvData.csv
                } else {
                    textView.text = "No matching CSV data found."
                }
            } else {
                textView.text = "No CSV data available."
            }
        }
    }


}
