package com.charles.warmwords.util

import android.app.ActivityManager
import android.content.Context

fun Context.getAvailableRamMb(): Long {
    val memoryInfo = ActivityManager.MemoryInfo()
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    activityManager.getMemoryInfo(memoryInfo)
    return memoryInfo.availMem / (1024 * 1024)
}

fun Context.getTotalRamMb(): Long {
    val memoryInfo = ActivityManager.MemoryInfo()
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    activityManager.getMemoryInfo(memoryInfo)
    return memoryInfo.totalMem / (1024 * 1024)
}

fun Context.isLowRamDevice(): Boolean {
    return try {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.isLowRamDevice
    } catch (e: Exception) {
        false
    }
}
