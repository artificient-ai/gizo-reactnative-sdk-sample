package com.example.gizo.advance.recording.presentation.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.hilt.android.EntryPointAccessors
import kotlin.reflect.KClass

private const val WORKER_CLASS_NAME = "RouterWorkerDelegateClassName"

/**
 * Adds metadata to a WorkRequest to identify what [CoroutineWorker] the [DelegatingWorker] should
 * delegate to
 */
 fun KClass<out CoroutineWorker>.delegatedData() =
    Data.Builder()
        .putString(WORKER_CLASS_NAME, qualifiedName)
        .build()

class DelegatingWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    private val workerClassName =
        workerParams.inputData.getString(WORKER_CLASS_NAME) ?: ""

    private val delegateWorker =
        EntryPointAccessors.fromApplication<HiltWorkerFactoryEntryPoint>(appContext)
            .hiltWorkerFactory()
            .createWorker(appContext, workerClassName, workerParams)
            as? CoroutineWorker
            ?: throw IllegalArgumentException("Unable to find appropriate worker")

    override suspend fun getForegroundInfo(): ForegroundInfo =
        delegateWorker.getForegroundInfo()

    override suspend fun doWork(): Result =
        delegateWorker.doWork()
}
