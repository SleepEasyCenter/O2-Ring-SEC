package com.example.lpdemo.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.lepu.blepro.ext.oxy.OxyFile
import com.opencsv.CSVWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


object FileUtils {

    private const val PREF_NAME = "OxyFilePref"
    private const val KEY_OXY_FILE_DATA = "oxyFileData"
    private val gson = Gson()

    fun initiateDownload(dataList: OxyFile, context: Context) {
        // Get the internal storage directory
        val internalDir = context.filesDir

        // Specify the directory name
        val directoryName = "data"


        // Create the directory if it doesn't exist
        val directory = File(internalDir, directoryName)
        if (!directory.exists()) {
            directory.mkdirs()
        }

        // Create a file in the directory
        val downloadDirectory = File(directory, "O2_Ring  ${dataList.day}${dataList.month}${dataList.year}")
        try {
            // Create a CSV writer
            val writer = CSVWriter(FileWriter(downloadDirectory))

            // Write headers
            writer.writeNext(
                arrayOf(
                    "Time",
                    "Oxygen Level",
                    "Pulse Rate",
                    "Motion"
                )
            )
            var startTime = Calendar.getInstance().apply {
                set(dataList.year, dataList.month - 1, dataList.day, dataList.hour, dataList.minute, dataList.second)
            }

            for (i in dataList.data.indices) {
                val dataPoint = dataList.data[i]
                // Accessing specific properties of each data point
                val time = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(startTime.time)

                // Update the start time by adding 4 seconds
                startTime.add(Calendar.SECOND, 4)
                val pr = dataPoint.pr
                val spo2 = dataPoint.spo2
                val motion = dataPoint.vector;

                writer.writeNext(arrayOf(time, pr.toString(), spo2.toString(), motion.toString()))
            }

            Log.d(
                "Download Notice",
                "CSV file successfully created at: ${downloadDirectory.absolutePath}"
            )

            Toast.makeText(
                context,
                "CSV file saved at: ${downloadDirectory.absolutePath}",
                Toast.LENGTH_LONG
            ).show()

            openCsvFile(downloadDirectory, context)


            // Close the writer
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()

            Log.e("Download Error", "Error creating CSV file: ${e.message}")
        }
    }
    private fun openCsvFile(file: File, context: Context) {
        try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "com.example.lpdemo.fileprovider",
                file
            )

            // Create an intent to view the CSV file
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "text/csv")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()

            // Log an error message if opening the file fails
            Log.e("File CSV Error", "Error opening CSV file: ${e.message}")
            Toast.makeText(context, "Failed to open CSV file", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveOxyFileData(context: Context, data: OxyFile) {
        val editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
        val jsonString = gson.toJson(data)
        editor.putString(KEY_OXY_FILE_DATA, jsonString)
        editor.apply()
    }

    fun getOxyFileData(context: Context): OxyFile? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_OXY_FILE_DATA, null)
        return if (jsonString != null) {
            gson.fromJson(jsonString, OxyFile::class.java)
        } else {
            null
        }
    }

}
