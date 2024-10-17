package com.sleepeasycenter.o2ring_app.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.math.BigInteger
import java.security.MessageDigest
public val SHARED_PREF_KEY = "SleepEasyClinic-O2RingApp-Preferences"
public fun hashString(str: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    val bigInt = BigInteger(1, md.digest(str.toByteArray(Charsets.UTF_8)))
    return String.format("%032x", bigInt)
}

public val SharedPref_PatientID_Key = "patient_id";
public fun getAppSharedPref(activity: Activity): SharedPreferences{
    return activity.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
}

public fun readPatientId(any_activity: Activity): String?{
    var sharedPref = getAppSharedPref(any_activity as Activity);
    val patient_id = sharedPref.getString(SharedPref_PatientID_Key, null);
    return patient_id
}

public fun setPatientId(any_activity: Activity, patient_id: String?) {
    var sharedPref = getAppSharedPref(any_activity as Activity);
    sharedPref.edit().putString(SharedPref_PatientID_Key, patient_id).apply()
}

public fun fileFromContentUri(context: Context, contentUri: Uri): File {

    val fileExtension = getFileExtension(context, contentUri)

    val fileName = contentUri.lastPathSegment

    val tempFile = File(context.cacheDir, fileName)
    tempFile.createNewFile()

    try {
        val oStream = FileOutputStream(tempFile)
        val inputStream = context.contentResolver.openInputStream(contentUri)

        inputStream?.let {
            copy(inputStream, oStream)
        }

        oStream.flush()
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return tempFile
}

public fun getFileExtension(context: Context, uri: Uri): String? {
    val fileType: String? = context.contentResolver.getType(uri)
    return MimeTypeMap.getSingleton().getExtensionFromMimeType(fileType)
}

@Throws(IOException::class)
public fun copy(source: InputStream, target: OutputStream) {
    val buf = ByteArray(8192)
    var length: Int
    while (source.read(buf).also { length = it } > 0) {
        target.write(buf, 0, length)
    }
}