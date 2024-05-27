package com.example.gizo.advance.recording.presentation.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.gizo.advance.recording.presentation.service.RecordingBackgroundService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            if (context != null) {
                RecordingBackgroundService.startService(context)
            }
        }
    }
}