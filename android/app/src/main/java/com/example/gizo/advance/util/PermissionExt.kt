package com.example.gizo.advance.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.core.content.ContextCompat

fun Context.hasPermission(vararg permissions: String): Boolean {
    val result = mutableListOf<Boolean>()
    val sdkVersion29OrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    val sdkVersion31OrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val sdkVersion33OrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    permissions.forEach forEach@{
        if (sdkVersion29OrAbove && it == Manifest.permission.READ_EXTERNAL_STORAGE){
            return@forEach
        }
        if (sdkVersion29OrAbove && it == Manifest.permission.WRITE_EXTERNAL_STORAGE){
            return@forEach
        }
        if (sdkVersion33OrAbove.not() && it == Manifest.permission.POST_NOTIFICATIONS){
            return@forEach
        }
        if (sdkVersion31OrAbove.not() && it ==  Manifest.permission.HIGH_SAMPLING_RATE_SENSORS){
            return@forEach
        }
        val isGranted = ContextCompat.checkSelfPermission(
            this,
            it
        ) == PackageManager.PERMISSION_GRANTED
        result.add(isGranted)
    }
    val predicate: (Boolean) -> Boolean = { !it }
    return !result.any(predicate)
}

fun Context.checkAndRequestPermissions(
    permissions: Array<String>,
    launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    hasPermissionCallback: ()->Unit
) {
    if (
        permissions.all {
            ContextCompat.checkSelfPermission(
                this,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    ) {
        hasPermissionCallback()
        // Use location because permissions are already granted
    } else {
        // Request permissions
        launcher.launch(permissions)
    }
}

fun  Context.hasUsageStatsPermission(): Boolean {
    val appOps = getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
    val mode = appOps.unsafeCheckOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
        android.os.Process.myUid(), packageName)
    return mode == android.app.AppOpsManager.MODE_ALLOWED
}

fun  getUsageStatsPermissionIntent()= Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)

fun Context.getIgnoringBatteryOptimizationsIntent(): Intent = Intent().apply {
    action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
    data = Uri.parse("package:$packageName")
}

fun Context.isIgnoringBatteryOptimizations(): Boolean {
    val pm: PowerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
    return pm.isIgnoringBatteryOptimizations(packageName)
}

fun Context.getApplicationDetailsSettingsActionIntent()=Intent().let {
    it.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts(
        "package",
        packageName, null
    )
    it.setData(uri)
}