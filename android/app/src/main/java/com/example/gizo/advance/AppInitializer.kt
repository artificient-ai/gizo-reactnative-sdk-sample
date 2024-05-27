package com.example.gizo.advance

import android.content.Context
import androidx.startup.AppInitializer
import androidx.startup.Initializer
import androidx.work.WorkManager
import androidx.work.WorkManagerInitializer
import com.example.gizo.advance.recording.presentation.worker.RecordingWorker

object StartUpInitializer{
    fun initialize(context: Context) {
        AppInitializer.getInstance(context)
            .initializeComponent(WorkerInitializer::class.java)
    }
}

class WorkerInitializer : Initializer<StartUpInitializer> {
    override fun create(context: Context): StartUpInitializer {
        WorkManager.getInstance(context).apply {
            enqueue(
                RecordingWorker.builder().build(),
            )
        }
        return StartUpInitializer
    }

    override fun dependencies(): List<Class<out Initializer<*>>> =
        listOf(WorkManagerInitializer::class.java)
}