package com.example.gizo.advance.recording.presentation.camera

import android.app.Activity
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.example.gizo.advance.designsystem.theme.AppTheme
import com.example.gizo.advance.recording.presentation.RecordingViewModel
import com.example.gizo.advance.recording.presentation.service.RecordingCameraService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class RecordingCameraActivity : ComponentActivity() {
    @Inject
    lateinit var assistedFactory: RecordingViewModel.ViewModelAssistedFactory
    private val viewModel: RecordingViewModel by lazy {
        ViewModelProvider(
            applicationContext as ViewModelStoreOwner, RecordingViewModel.Factory(
                assistedFactory
            )
        )[RecordingViewModel::class.java]
    }

    private var recordingCameraService: RecordingCameraService? = null

    private var previewView: PreviewView?=null

    override fun onNewIntent(intent: Intent) {
        viewModel.notifyRecordingFullNotified()
        super.onNewIntent(intent)
    }

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    RecordingScreen(viewModel = viewModel,
                        onAttachPreview = { preview ->
                            previewView=preview
                            onServiceBound(recordingCameraService)
                        },
                        onClose = { finishAction() })
                }
            }
        }
    }

    private fun finishAction() {
        stopService()
        setResult(Activity.RESULT_OK)
        super.finish()
    }

    private fun bindService() {
        val intent = Intent(this, RecordingCameraService::class.java)
        intent.action = RecordingCameraService.ACTION_START_WITH_PREVIEW
        startService(intent)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun stopService() {
        val intent = Intent(this, RecordingCameraService::class.java)
        unbindService(serviceConnection)
        stopService(intent)
    }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            recordingCameraService =
                (service as RecordingCameraService.RecordingServiceBinder).getService()
            onServiceBound(recordingCameraService)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }

    private fun onServiceBound(recordingCameraService: RecordingCameraService?) {
        recordingCameraService?.bindPreviewUseCase(previewView)
    }

    public override fun onStart() {
        super.onStart()
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        bindService()
    }
    public override fun onStop() {
        recordingCameraService?.startRunningInForeground()
        window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onStop()
    }

}