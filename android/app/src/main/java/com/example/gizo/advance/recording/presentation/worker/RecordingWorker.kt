package com.example.gizo.advance.recording.presentation.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import com.example.gizo.advance.recording.presentation.service.RecordingBackgroundService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@HiltWorker
class RecordingWorker @AssistedInject constructor(
    @Assisted  val context: Context,
    @Assisted params: WorkerParameters,
    ) : CoroutineWorker(context, params) {


    override suspend fun doWork(): Result  = withContext(Dispatchers.IO){
        Log.d(TAG, "do RecordingWorker")
        if (RecordingBackgroundService.isRunning(applicationContext).not()) {
            RecordingBackgroundService.startService(applicationContext)
        }
        Result.success()
    }

    companion object {
        private val TAG = RecordingWorker::class.simpleName

        fun builder() =
            PeriodicWorkRequestBuilder<DelegatingWorker>(15, TimeUnit.MINUTES).setInputData(RecordingWorker::class.delegatedData())

    }
}