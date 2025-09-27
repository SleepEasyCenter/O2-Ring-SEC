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

public val SharedPref_PatientID_Key = "patient_id"

public val SharedPref_PatientO2Serial_Key = "patient_o2serial"

public val SharedPref_PatientOxyBaseline_Key = "patient_oxybaseline"

public val SharedPref_PatientPRBaseline_Key = "patient_prbaseline"

public val SharedPref_PatientPPGBaseline_Key = "patient_ppgbaseline"

public val SharedPref_PatientSPCalib_Key = "patient_spcalib"

public val SharedPref_PatientDBPCalib_Key = "patient_dbpcalib"

public val SharedPref_PatientAutoUpload_Key = "patient_autoupload"

public fun getAppSharedPref(activity: Activity): SharedPreferences {
    return activity.getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE)
}

public fun readPatientAutoUpload(any_activity: Activity): Boolean {
    var sharedPref = getAppSharedPref(any_activity as Activity)
    val oxyBaseline = sharedPref.getBoolean(SharedPref_PatientAutoUpload_Key, true)
    return oxyBaseline
}

public fun readPatientOxyBaseline(any_activity: Activity): String? {
    var sharedPref = getAppSharedPref(any_activity as Activity)
    val oxyBaseline = sharedPref.getString(SharedPref_PatientOxyBaseline_Key, "90")
    return oxyBaseline
}

public fun readPatientPRBaseline(any_activity: Activity): String? {
    var sharedPref = getAppSharedPref(any_activity as Activity)
    val oxyBaseline = sharedPref.getString(SharedPref_PatientPRBaseline_Key, "50")
    return oxyBaseline
}

public fun readPatientPPGBaseline(any_activity: Activity): String? {
    var sharedPref = getAppSharedPref(any_activity as Activity)
    val ppgBaseline = sharedPref.getString(SharedPref_PatientPPGBaseline_Key, "0")
    return ppgBaseline
}

public fun readPatientSPCalib(any_activity: Activity): String? {
    var sharedPref = getAppSharedPref(any_activity as Activity)
    val spCalib = sharedPref.getString(SharedPref_PatientSPCalib_Key, "120")
    return spCalib
}

public fun readPatientDBPCalib(any_activity: Activity): String? {
    var sharedPref = getAppSharedPref(any_activity as Activity)
    val dbpCalib = sharedPref.getString(SharedPref_PatientDBPCalib_Key, "80")
    return dbpCalib
}

public fun readPatientId(any_activity: Activity): String? {
    var sharedPref = getAppSharedPref(any_activity as Activity)
    val patient_id = sharedPref.getString(SharedPref_PatientID_Key, "-")
    return patient_id
}

public fun readPatientO2Serial(any_activity: Activity): String? {
    var sharedPref = getAppSharedPref(any_activity as Activity)
    val patient_o2serial = sharedPref.getString(SharedPref_PatientO2Serial_Key, "-")
    return patient_o2serial
}

public fun setPatientAutoUpload(any_activity: Activity, patient_autoupload: Boolean) {
    var sharedPref = getAppSharedPref(any_activity as Activity)
    sharedPref.edit().putBoolean(SharedPref_PatientAutoUpload_Key, patient_autoupload).apply()
}

public fun setPatientOxyBaseline(any_activity: Activity, patient_oxybaseline: String?) {
    var sharedPref = getAppSharedPref(any_activity as Activity)
    sharedPref.edit().putString(SharedPref_PatientOxyBaseline_Key, patient_oxybaseline).apply()
}

public fun setPatientPRBaseline(any_activity: Activity, patient_prbaseline: String?) {
    var sharedPref = getAppSharedPref(any_activity as Activity)
    sharedPref.edit().putString(SharedPref_PatientPRBaseline_Key, patient_prbaseline).apply()
}

public fun setPatientId(any_activity: Activity, patient_id: String?) {
    var sharedPref = getAppSharedPref(any_activity as Activity)
    sharedPref.edit().putString(SharedPref_PatientID_Key, patient_id).apply()
}

public fun setPatientO2Serial(any_activity: Activity, patient_o2serial: String?) {
    var sharedPref = getAppSharedPref(any_activity as Activity)
    sharedPref.edit().putString(SharedPref_PatientO2Serial_Key, patient_o2serial).apply()
}

public fun setPatientPPGBaseline(any_activity: Activity, patient_ppgbaseline: String?) {
    var sharedPref = getAppSharedPref(any_activity as Activity)
    sharedPref.edit().putString(SharedPref_PatientPPGBaseline_Key, patient_ppgbaseline).apply()
}

public fun setPatientSPCalib(any_activity: Activity, patient_spcalib: String?) {
    var sharedPref = getAppSharedPref(any_activity as Activity)
    sharedPref.edit().putString(SharedPref_PatientSPCalib_Key, patient_spcalib).apply()
}

public fun setPatientDBPCalib(any_activity: Activity, patient_dbpcalib: String?) {
    var sharedPref = getAppSharedPref(any_activity as Activity)
    sharedPref.edit().putString(SharedPref_PatientDBPCalib_Key, patient_dbpcalib).apply()
}

public fun fileFromContentUri(context: Context, contentUri: Uri): File {

    val fileExtension = getFileExtension(context, contentUri)

    val fileName = contentUri.lastPathSegment!!

    val tempFile = File(context.cacheDir, fileName)
    tempFile.createNewFile()

    try {
        val oStream = FileOutputStream(tempFile)
        val inputStream = context.contentResolver.openInputStream(contentUri)

        inputStream?.let { copy(inputStream, oStream) }

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
