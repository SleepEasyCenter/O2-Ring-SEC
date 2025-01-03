package com.sleepeasycenter.o2ring_app.utils

import com.lepu.blepro.ext.oxy.OxyFile
import java.time.Instant

/**
 * Retrieves data at point in time. Will linearly interpolate if needed.
 *
 *  @param time_point Point in time in seconds. ( 0 = Start)
 *  @param oxyfile The dataset
 */
public fun getDataAtPercent(percent: Float, oxyfile: OxyFile): OxyFile.EachData {
    val index = percent * (oxyfile.data.size - 1);


    val lowerIndex = Math.floor((index).toDouble()).toInt()
    // Handle cases where the point in time is out of bounds
    if (lowerIndex < 0 || lowerIndex >= oxyfile.data.size - 1) {
        throw IllegalArgumentException("Percent in time $percent - $lowerIndex is out of the data set range.")
    }
    return oxyfile.data[lowerIndex];
}

public fun convertToCsv(oxyfile: OxyFile): String{
    // Number of seconds per data point
    val resolution = oxyfile.recordingTime / oxyfile.data.size;

    var rows: ArrayList<String> = arrayListOf();
    rows += "Time,Oxygen Level,Pulse Rate,Motion,O2 Reminder,PR Reminder";
    // We want data every for second
    // Total points represents the total number of data points to collect
    val totalPoints = oxyfile.recordingTime / 4;

    for (index in 0 until totalPoints){
        val target_time = index * 4;
        val percent = index.toFloat() / totalPoints.toFloat();
        val data = getDataAtPercent(percent, oxyfile);
        val time_epoch = oxyfile.startTime + target_time;
        val datetime = Instant.ofEpochSecond(time_epoch)
        rows += "$datetime,${data.spo2},${data.pr},${data.vector},0,0";
    }

    return rows.joinToString("\n");
}