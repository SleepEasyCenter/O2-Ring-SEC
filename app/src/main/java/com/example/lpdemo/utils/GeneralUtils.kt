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