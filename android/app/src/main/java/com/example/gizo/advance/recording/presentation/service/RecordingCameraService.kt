package com.example.gizo.advance.recording.presentation.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.camera.view.PreviewView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.CATEGORY_SERVICE
import androidx.core.app.NotificationCompat.PRIORITY_MAX
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.app.TaskStackBuilder
import androidx.lifecycle.*
import com.example.gizo.advance.R
import com.example.gizo.advance.recording.presentation.RecordingUiEvent
import com.example.gizo.advance.recording.presentation.RecordingViewModel
import com.example.gizo.advance.recording.presentation.camera.RecordingCameraActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class RecordingCameraService : LifecycleService() {

    companion object {
        private val TAG = RecordingCameraService::class.simpleName
        const val ACTION_START_WITH_PREVIEW: String = "ACTION_START_WITH_PREVIEW"
        const val ACTION_STOP_RECORDING: String = "ACTION_STOP_RECORDING"
        const val ACTION_START_RECORDING: String = "ACTION_START_RECORDING"
        const val BIND_USE_CASE: String = "bind_use_case"
        const val CHANNEL_NAME: String = "recording service"
        const val CHANNEL_ID: String = "recording_service"
        const val ONGOING_NOTIFICATION_ID: Int = 2345
    }

    private val pendingActions: HashMap<String, Runnable> = hashMapOf()

    private lateinit var dangerPlayer: MediaPlayer

    class RecordingServiceBinder(private val service: RecordingCameraService) : Binder() {
        fun getService(): RecordingCameraService {
            return service
        }
    }

    @Inject
    lateinit var assistedFactory: RecordingViewModel.ViewModelAssistedFactory
    private val viewModel: RecordingViewModel by lazy {
        ViewModelProvider(
            applicationContext as ViewModelStoreOwner, RecordingViewModel.Factory(
                assistedFactory
            )
        )[RecordingViewModel::class.java]
    }

    private var isItHasNotification = false

    private lateinit var recordingServiceBinder: RecordingServiceBinder

    private var startTime: Long = Date().time

    private var backgroundTime: Long? = null
    override fun onCreate() {
        super.onCreate()
        viewModel.setBackgroundAnalysisAlive(true)
        startTime = Date().time
        Log.d(TAG, "service onCreate")
        recordingServiceBinder = RecordingServiceBinder(this)
        dangerPlayer = MediaPlayer.create(this, R.raw.gizo_alert_danger)
        lifecycleScope.launch {
            viewModel.uiState
                .flowWithLifecycle(lifecycle)
                .collect {
                    updateNotification(isRecording = it.isRecording)
                }
        }
        lifecycleScope.launch {
            viewModel.ttcDangerFlow
                .flowWithLifecycle(lifecycle)
                .collect {
                    if (it) {
                        if (dangerPlayer.isPlaying.not())
                            dangerPlayer.start()
                    }
                }
        }
        lifecycleScope.launch {
            viewModel.event
                .flowWithLifecycle(lifecycle)
                .collect { event ->
                    when (event) {
                        is RecordingUiEvent.Error -> {
                            Toast.makeText(applicationContext, event.message, Toast.LENGTH_SHORT)
                                .show()
                        }

                        is RecordingUiEvent.Alert -> {
                            Toast.makeText(applicationContext, event.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "service onStartCommand action: " + intent?.action)
        viewModel.attachMapNavigation()
        when (intent?.action) {
            ACTION_START_WITH_PREVIEW -> {
                if (viewModel.cameraInitialized.not()) {
                    initializeCamera()
                }
            }

            ACTION_START_RECORDING -> {
                if (viewModel.cameraInitialized)
                    viewModel.startRecordingVideo()

            }

            ACTION_STOP_RECORDING -> {

                if (viewModel.cameraInitialized)
                    viewModel.stopRecordingVideo()
            }
        }
        return START_NOT_STICKY
    }

    private fun initializeCamera() {
        Log.d(TAG, "service initializeCamera")
        Log.d(TAG, "service startCamera")
        viewModel.startCamera(lifecycleOwner = this) {
            Log.d(TAG, "service startCamera Done")
            val action = pendingActions[BIND_USE_CASE]
            action?.run()
            pendingActions.remove(BIND_USE_CASE)
        }
    }

    fun bindPreviewUseCase(previewView: PreviewView?) {
        Log.d(TAG, "service bindPreviewUseCase")
        if (viewModel.cameraInitialized) {
            bindInternal(previewView)
        } else {
            pendingActions[BIND_USE_CASE] = Runnable {
                bindInternal(previewView)
            }
        }
    }

    private fun stopForeground() {
        backgroundTime?.let { start ->


            Log.d(TAG, "service stopForeground duration :" + (Date().time - start))
            backgroundTime = null
        }

        Log.d(TAG, "service stopForeground")

        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_DETACH)
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(ONGOING_NOTIFICATION_ID)
        isItHasNotification = false
    }

    private fun bindInternal(previewView: PreviewView?) {
        stopForeground()
        Log.d(TAG, "service bindInternal")
        if (previewView != null) {
            Log.d(TAG, "service bindInternal has preview")
            viewModel.attachPreview(previewView)
        }
    }


    private fun buildNotification(isRecording: Boolean): Notification {
        val parentStack = TaskStackBuilder.create(this)
            .addNextIntentWithParentStack(Intent(this, RecordingCameraActivity::class.java))

        val recordingIntent = Intent(this, RecordingCameraService::class.java).apply {
            action = if (isRecording) ACTION_STOP_RECORDING else ACTION_START_RECORDING
        }
        val recordingPendingIntent = PendingIntent.getForegroundService(
            this,
            0,
            recordingIntent,
            PendingIntent.FLAG_IMMUTABLE
        )


        val actionText = if (isRecording) "Stop Recording" else "Start Recording"
        val actionIconRes = 0


        val contentTitle = "GIZO Analysis"
        val contentText =
            if (isRecording) "Video recording in background" else "Video Analysis in background"

        val pendingIntent1 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            parentStack.getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
        } else {
            parentStack.getPendingIntent(0, 0)
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setOngoing(true)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent1)
            .setPriority(PRIORITY_MAX)
            .setOnlyAlertOnce(true)
            .setCategory(CATEGORY_SERVICE)
            .setAutoCancel(false)
            .addAction(
                actionIconRes, actionText,
                recordingPendingIntent
            )
            .build()
    }

    @SuppressLint("MissingPermission")
    private fun updateNotification(isRecording: Boolean) {
        if (isItHasNotification) {
            val notification: Notification = buildNotification(isRecording = isRecording)
            with(NotificationManagerCompat.from(this)) {
                notify(ONGOING_NOTIFICATION_ID, notification)
            }

        }
    }


    fun startRunningInForeground() {
        Log.d(TAG, "service startRunningInForeground")
        backgroundTime = Date().time

        val channel =
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
        with(NotificationManagerCompat.from(this)) {
            createNotificationChannel(channel)
        }
        val notification: Notification =
            buildNotification(isRecording = viewModel.uiState.value.isRecording)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ServiceCompat.startForeground(
                this,
                ONGOING_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION or ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                ONGOING_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            ServiceCompat.startForeground(
                this,
                ONGOING_NOTIFICATION_ID,
                notification,
                0
            )
        }
        isItHasNotification = true
    }

    override fun onDestroy() {
        viewModel.setBackgroundAnalysisAlive(false)
        Log.d(TAG, "service onDestroy")
        pendingActions.clear()
        stopForeground()
        viewModel.stopCamera()
        dangerPlayer.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        Log.d(TAG, "service onBind")
        return recordingServiceBinder
    }
}