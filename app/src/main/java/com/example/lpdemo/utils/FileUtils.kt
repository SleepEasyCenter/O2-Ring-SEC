package com.example.lpdemo.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.nfc.Tag
import android.os.Environment
import android.util.Log
import com.lepu.blepro.ext.oxy.OxyFile
import com.opencsv.CSVWriter
import java.io.FileWriter
import java.io.File
import java.io.IOException

object FileUtils {
//    fun initiateDownload(dataList: OxyFile, context: Context) {
//        // Get the internal storage directory
//        val internalDir = context.filesDir
//
//        // Specify the directory name
//        val directoryName = "data"
//
//        // Create the directory if it doesn't exist
//        val directory = File(internalDir, directoryName)
//        if (!directory.exists()) {
//            directory.mkdirs()
//        }
//
//        // Create a file in the directory
//        val downloadDirectory = File(directory, "data.csv")
//
//        // Create a CSV writer
//        val writer = CSVWriter(FileWriter(downloadDirectory))
//
//        // Write headers
//        writer.writeNext(arrayOf("Pulse Rate", "Oxygen Level"))
//
//        for (i in dataList.data.indices) {
//            val dataPoint = dataList.data[i]
//            // Accessing specific properties of each data point
//            val pr = dataPoint.pr
//            val spo2 = dataPoint.spo2
//            // Do whatever you need with 'pr' and 'spo2' here
//            writer.writeNext(arrayOf(dataPoint.pr.toString(), dataPoint.spo2.toString()))
//        }
//
//        // Close the writer
//        writer.close()
//    }

    fun initiateDownload(dataList: OxyFile, context: Context) {
        // Create a file in the local directory
        val downloadDirectory = File(context.filesDir, "data.csv")
        Log.d("Directory", "$downloadDirectory");
        try {
            val writer = CSVWriter(FileWriter(downloadDirectory))

            // Create data to be written to the CSV file
            val data = listOf(
                arrayOf("Country", "Capital"),
                arrayOf("India", "New Delhi"),
                arrayOf("United States", "Washington D.C"),
                arrayOf("Germany", "Berlin")
            )

            writer.writeAll(data)

            // Close the writer
            writer.close()

            // Initiate download using DownloadManager
            val uri = Uri.fromFile(downloadDirectory)
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(uri).apply {
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                setTitle("Downloading CSV File")
                setDescription("Downloading CSV file to ${downloadDirectory.absolutePath}")
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "data.csv")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            }

            downloadManager.enqueue(request)


            // Call a function to handle the file after writing
            // Replace `callRead()` with the appropriate function call
            // callRead()

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}