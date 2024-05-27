package com.example.gizo.advance.recording.presentation.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.gizo.advance.MainActivity
import com.example.gizo.advance.R
import com.example.gizo.advance.recording.presentation.NotificationEvent
import com.example.gizo.advance.recording.presentation.RecordingViewModel
import com.example.gizo.advance.recording.presentation.camera.RecordingCameraActivity
import com.example.gizo.advance.recording.presentation.nocamera.RecordingNoCameraActivity
import com.example.gizo.advance.util.hasPermission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RecordingBackgroundService : LifecycleService() {

    companion object {
        private val TAG = RecordingBackgroundService::class.simpleName
        const val ACTION_START: String = "ACTION_START"

        const val ACTION_DRIVING_YES: String = "ACTION_DRIVING_YES"
        const val ACTION_DRIVING_NO: String = "ACTION_DRIVING_NO"
        const val ACTION_RECORDING_NO_CAMERA: String = "ACTION_RECORDING_NO_CAMERA"
        const val ACTION_RECORDING_FULL: String = "ACTION_RECORDING_FULL"
        const val ACTION_RECORDING_SELECT: String = "ACTION_RECORDING_SELECT"
        const val CHANNEL_NAME: String = "background recording service"
        const val CHANNEL_ID: String = "background_recording_service"
        const val Intent_Recording_Select: String = "Intent_Recording_Select"
        const val Intent_Action: String = "Intent_Action"
        const val ONGOING_NOTIFICATION_ID: Int = 2355

        fun startService(context: Context) {
            try {
                if (hasPermissionToStart(context)) {
                    val serviceIntent = Intent(context, RecordingBackgroundService::class.java)
                    serviceIntent.action = ACTION_START
                    ContextCompat.startForegroundService(context, serviceIntent)
                    Log.d(TAG, "startService")
                }
            } catch (e: Exception) {
                Log.e(TAG, "startService", e)
            }
        }

        private fun hasPermissionToStart(context: Context) =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.hasPermission(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ).also {
                    Log.d(TAG, "hasPermissionToStart $it")
                }
            } else {
                true
            }

        fun performAction(context: Context, action: String) {
            try {
                if (hasPermissionToStart(context)) {
                    val serviceIntent = Intent(context, RecordingBackgroundService::class.java)
                    serviceIntent.action = action
                    ContextCompat.startForegroundService(context, serviceIntent)
                    Log.d(TAG, "performAction $action")
                }
            } catch (e: Exception) {
                Log.e(TAG, "performAction", e)
            }
        }

        fun isRunning(context: Context): Boolean {
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
                if (RecordingBackgroundService::class.java.name == service.service.className) {
                    Log.d(TAG, "isRunning")
                    return true
                }
            }
            return false
        }
    }

    private var notificationCheckAliveJob: Job? = null
    private var currentNotificationEvent: NotificationEvent = NotificationEvent.BackgroundDefault

    @Inject
    lateinit var assistedFactory: RecordingViewModel.ViewModelAssistedFactory
    private val viewModel: RecordingViewModel by lazy {
        ViewModelProvider(
            applicationContext as ViewModelStoreOwner, RecordingViewModel.Factory(
                assistedFactory
            )
        )[RecordingViewModel::class.java]
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()
        viewModel.start(lifecycleOwner = this)

        lifecycleScope.launch {
            viewModel.notificationEvent
                .flowWithLifecycle(lifecycle)
                .collect { event ->
                    updateNotificationEvent(event)
                }
        }

    }

    private fun updateNotificationEvent(event: NotificationEvent) {
        currentNotificationEvent = event
        when (event) {
            is NotificationEvent.BackgroundDefault -> {
                updateNotification()
            }

            is NotificationEvent.BackgroundStill -> {
                updateNotification()
            }

            is NotificationEvent.BackgroundNoCamera -> {
                updateNotification(noCameraAction = true)
            }

            is NotificationEvent.RecordingFullQuestion -> {
                updateNotification(
                    title = "It seems you are in a vehicle.",
                    content = "Do you want recording video?",
                    haveRecordingWithCamera = true,
                    haveRecordingWithoutCamera = true,
                    needRecordingPermission = event.needPermission,
                    onlyAlertOnce = false
                )
            }

            is NotificationEvent.InVehicleQuestion -> {
                updateNotification(
                    title = "It seems you are in a vehicle.",
                    content = " Are you driving?",
                    haveDrivingYesAction = true,
                    needRecordingPermission = event.needPermission,
                    haveDrivingNoAction = true,
                    onlyAlertOnce = false
                )

            }
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "onStartCommand action:" + intent?.action)
        when (intent?.action) {
            ACTION_START -> {
                startRunningInForeground()
            }

            ACTION_DRIVING_YES -> {
                viewModel.actionDrivingYes()
            }

            ACTION_DRIVING_NO -> {
                viewModel.notifyInVehicleNotified()
            }

            ACTION_RECORDING_NO_CAMERA -> {
                viewModel.notifyAcceptRecordingNoCamera()
            }

            ACTION_RECORDING_FULL -> {
                viewModel.notifiedRecordingFull()
            }

            ACTION_RECORDING_SELECT -> {
                viewModel.notifiedRecordingSelect()
            }

            else -> {}
        }

        return START_STICKY
    }


    @SuppressLint("MissingPermission")
    private fun updateNotification(
        title: String = "Sample Analysis",
        content: String = "Background activity analysis",
        onlyAlertOnce: Boolean = true,
        noCameraAction: Boolean = false,
        haveDrivingYesAction: Boolean = false,
        haveDrivingNoAction: Boolean = false,
        haveRecordingWithCamera: Boolean = false,
        haveRecordingWithoutCamera: Boolean = false,
        needRecordingPermission: Boolean = false,
    ) {
        val notification: Notification =
            buildNotification(
                title = title,
                content = content,
                noCameraAction = noCameraAction,
                onlyAlertOnce = onlyAlertOnce,
                haveDrivingYesAction = haveDrivingYesAction,
                haveDrivingNoAction = haveDrivingNoAction,
                haveRecordingWithCamera = haveRecordingWithCamera,
                haveRecordingWithoutCamera = haveRecordingWithoutCamera,
                needRecordingPermission = needRecordingPermission,
            )
        with(NotificationManagerCompat.from(this)) {
            notify(ONGOING_NOTIFICATION_ID, notification)
        }
    }

    private fun isNotificationVisible(): Boolean {
        val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notifications = mNotificationManager.activeNotifications
        for (notification in notifications) {
            if (notification.id == ONGOING_NOTIFICATION_ID) {
                return true
            }
        }
        return false
    }

    private fun buildNotification(
        title: String = "Sample Analysis",
        content: String = "Background activity analysis",
        onlyAlertOnce: Boolean = true,
        noCameraAction: Boolean = false,
        haveDrivingYesAction: Boolean = false,
        haveDrivingNoAction: Boolean = false,
        haveRecordingWithCamera: Boolean = false,
        haveRecordingWithoutCamera: Boolean = false,
        needRecordingPermission: Boolean = false,
    ): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .setContentIntent(
                if (noCameraAction) getPendingActivity(
                    parent = MainActivity::class.java,
                    activity = RecordingNoCameraActivity::class.java
                ) else
                    getPendingActivity(
                        activity = MainActivity::class.java
                    )
            )
            .setPriority(PRIORITY_HIGH)
            .setOnlyAlertOnce(onlyAlertOnce)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .apply {
                if (haveDrivingYesAction) {
                    addAction(
                        0,
                        "Yes",
                        if (needRecordingPermission.not())
                            getPending(ACTION_DRIVING_YES)
                        else
                            getPendingActivity(
                                activity = MainActivity::class.java,
                                extraBundle = bundleOf(
                                    "initialScreen" to 1,
                                    Intent_Recording_Select to true,
                                    Intent_Action to ACTION_RECORDING_SELECT
                                )
                            )
                    )

                }

                if (haveDrivingNoAction) {
                    addAction(
                        0,
                        "No",
                        getPending(ACTION_DRIVING_NO)
                    )
                }

                if (haveRecordingWithCamera) {
                    addAction(
                        0,
                        "Yes",
                        if (needRecordingPermission.not())
                            getPendingActivity(
                                parent = Class.forName("de.artificient.gizo.ui.MainActivity"),
                                activity = RecordingCameraActivity::class.java
                            )
                        else
                            getPendingActivity(
                                activity = Class.forName("de.artificient.gizo.ui.MainActivity"),
                                extraBundle = bundleOf(
                                    "initialScreen" to 1,
                                    Intent_Recording_Select to true,
                                    Intent_Action to ACTION_RECORDING_SELECT
                                )
                            )
                    )
                }

                if (haveRecordingWithoutCamera) {
                    addAction(
                        0,
                        "NO",
                        getPending(ACTION_RECORDING_NO_CAMERA)
                    )
                }

            }
            .build()
    }

    private fun getPending(intentAction: String): PendingIntent {
        val recordingIntent = Intent(this, RecordingBackgroundService::class.java).apply {
            action = intentAction
        }
        return PendingIntent.getForegroundService(
            this,
            0,
            recordingIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getPendingActivity(
        parent: Class<*>? = null, activity: Class<*>,
        extraBundle: Bundle = bundleOf(),
    ): PendingIntent? {

        if (parent == null) {
            val notifyIntent = Intent(this, activity).apply {
                putExtras(extraBundle)
            }
            return PendingIntent.getActivity(
                this, 0, notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        } else {
            val parentStack = TaskStackBuilder.create(this)
            parentStack.addParentStack(parent)
            parentStack.addNextIntent(Intent(this, activity).apply {
                putExtras(extraBundle)
            })
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                parentStack.getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
            } else {
                parentStack.getPendingIntent(0, 0)
            }
        }
    }

    private fun startRunningInForeground() {
        Log.d(TAG, "startRunningInForeground")
        val channel =
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
        with(NotificationManagerCompat.from(this)) {
            createNotificationChannel(channel)
        }

        val notification: Notification = buildNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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

        notificationCheckAlive()
    }

    private fun notificationCheckAlive() {
        notificationCheckAliveJob?.cancel()
        notificationCheckAliveJob = lifecycleScope.launch {
            delay(5000)
            while (isActive) {
                if (isNotificationVisible().not()) {
                    updateNotificationEvent(currentNotificationEvent)
                }
                delay(1000)
            }
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        viewModel.stop()
        notificationCheckAliveJob?.cancel()
        super.onDestroy()
    }

}