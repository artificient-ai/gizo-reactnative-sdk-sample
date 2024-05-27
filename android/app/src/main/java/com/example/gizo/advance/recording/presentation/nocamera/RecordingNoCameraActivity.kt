package com.example.gizo.advance.recording.presentation.nocamera

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.example.gizo.advance.designsystem.theme.AppTheme
import com.example.gizo.advance.recording.presentation.RecordingViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@AndroidEntryPoint
class RecordingNoCameraActivity : ComponentActivity() {

    @Inject
    lateinit var assistedFactory: RecordingViewModel.ViewModelAssistedFactory
    private val viewModel: RecordingViewModel by lazy {
        ViewModelProvider(
            applicationContext as ViewModelStoreOwner, RecordingViewModel.Factory(
                assistedFactory
            )
        )[RecordingViewModel::class.java]
    }

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            val systemUiController = rememberSystemUiController()
            AppTheme {
                SideEffect {
                    systemUiController.setStatusBarColor(
                        color = Color.Transparent,
                        darkIcons = false
                    )
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        containerColor = MaterialTheme.colorScheme.onBackground,
                        contentColor = MaterialTheme.colorScheme.onBackground,
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),

                        ) { paddingValues ->
                        RecordingRouteScreen(viewModel = viewModel,
                            modifier = Modifier
                                .padding(paddingValues),
                            onBack = {
                                finish()
                            })
                    }
                }
            }
        }
    }
}