package com.example.gizo.advance

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings;
import android.os.Process
import com.example.gizo.advance.recording.presentation.camera.RecordingCameraActivity
import com.example.gizo.advance.recording.presentation.nocamera.RecordingNoCameraActivity
import com.facebook.react.bridge.*
import de.artificient.gizo.sdk.Gizo


class GizoAndroidSDKModule(context: ReactApplicationContext) : ReactContextBaseJavaModule(context) {

    override fun getName(): String {
        return "GizoAndroidSDKModule"
    }

    @ReactMethod
    fun startDriveNowActivity() {
        val intent = Intent(currentActivity, RecordingCameraActivity::class.java)
        currentActivity?.startActivity(intent)
    }

    @ReactMethod
    fun startNoCameraActivity() {
        val intent = Intent(currentActivity, RecordingNoCameraActivity::class.java)
        currentActivity?.startActivity(intent)
    }

    @ReactMethod
    fun checkUsageStatsPermission(callback: Callback) {
        val context: Context = reactApplicationContext
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )

        if (mode == AppOpsManager.MODE_ALLOWED) {
            callback.invoke(true)
        } else {
            callback.invoke(false)
        }
    }

    @ReactMethod
    fun neededPermissions(promise: Promise){

        val writableArray = WritableNativeArray()
        for (item in Gizo.app.permissionRequired) {
            writableArray.pushString(item)
        }
        promise.resolve(writableArray)
    }

    @ReactMethod
    fun requestUsageStatsPermission() {
        val intent: Intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        reactApplicationContext.startActivity(intent)
    }

}