package com.example.lpdemo.utils

import java.math.BigInteger
import java.security.MessageDigest

public fun hashString(str: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    val bigInt = BigInteger(1, md.digest(str.toByteArray(Charsets.UTF_8)))
    return String.format("%032x", bigInt)
}