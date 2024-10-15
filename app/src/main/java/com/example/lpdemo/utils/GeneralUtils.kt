package com.example.lpdemo.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
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