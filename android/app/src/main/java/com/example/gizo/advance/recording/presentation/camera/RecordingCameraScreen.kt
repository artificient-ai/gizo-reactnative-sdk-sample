package com.example.gizo.advance.recording.presentation.camera

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.gizo.advance.R
import com.example.gizo.advance.designsystem.component.GizoAlertDialog
import com.example.gizo.advance.designsystem.theme.AppTheme
import com.example.gizo.advance.designsystem.theme.redesignRedColor
import com.example.gizo.advance.designsystem.theme.urbanistFontFamily
import com.example.gizo.advance.recording.presentation.RecordingUiState
import com.example.gizo.advance.recording.presentation.RecordingViewModel
import com.example.gizo.advance.recording.presentation.component.AlertComponent
import com.example.gizo.advance.recording.presentation.component.GpsDialog
import com.example.gizo.advance.recording.presentation.component.RotateComponent

@Composable
fun RecordingScreen(
    viewModel: RecordingViewModel,
    onAttachPreview: (PreviewView) -> Unit,
    onClose: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current

    val previewView: PreviewView = remember { PreviewView(context) }

    LaunchedEffect(previewView) {
        previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        onAttachPreview(previewView)
    }

    BackHandler {
        if (uiState.isRecording.not())
            onClose()
    }

    if (uiState.showPreview)
        previewView.let { preview ->
            AndroidView(
                factory = { preview },
                modifier = Modifier.fillMaxSize()
            )
        }

    if (uiState.isGravityAlign.not()) {
        RotateComponent(modifier = Modifier)
    } else
        RecordingScreenBody(
            uiState = uiState,
            onPreviewClick = { viewModel.togglePreview(previewView) },
            onRecordingClick = { viewModel.startRecordingVideo() },
            onCloseClick = { onClose() },
        )

    if (uiState.danger)
        AlertComponent()

    if (uiState.needGps) {
        GpsDialog(onConfirm = {
            viewModel.gpsNeedConfirm()
            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }, onDismiss = {
            viewModel.gpsNeedCancel()
        })
    }

    if (uiState.lowBatteryDialog.show)
        GizoAlertDialog(
            onClose = { viewModel.onAlertDismiss() },
            title = stringResource(R.string.gizo_low_battery),
            description = stringResource(R.string.gizo_low_battery_description),
            icon = painterResource(id = R.drawable.gizo_ic_low_battery)
        )

    if (uiState.overheatingDialog.show)
        GizoAlertDialog(
            onClose = { viewModel.onAlertDismiss() },
            title = stringResource(R.string.gizo_overheating),
            description = stringResource(R.string.gizo_overheating_description),
            icon = painterResource(id = R.drawable.gizo_ic_overheating)
        )
}

@Composable
fun RecordingScreenBody(
    uiState: RecordingUiState,
    onPreviewClick: () -> Unit,
    onRecordingClick: () -> Unit,
    onCloseClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        RecordingTools(
            modifier = Modifier.align(Alignment.TopCenter),
            uiState = uiState,
            onPreviewClick = onPreviewClick,
            onRecordingClick = onRecordingClick,
            onCloseClick = onCloseClick,
        )

        LogAnalysis(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 21.dp),
            uiState = uiState
        )
    }
}

@Composable
fun LogAnalysis(modifier: Modifier = Modifier, uiState: RecordingUiState) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        uiState.preview?.let { DepthImage(modifier = Modifier, it) }
        Spacer(modifier = Modifier.height(12.dp))
        uiState.log.entries.forEach {
            Text(
                text = "${it.key}: ${it.value}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Normal,
                    fontFamily = urbanistFontFamily,
                    fontSize = 18.sp
                ),
                color = redesignRedColor,
            )
        }
    }
}

@Composable
fun RecordingTools(
    modifier: Modifier,
    uiState: RecordingUiState,
    onPreviewClick: () -> Unit,
    onRecordingClick: () -> Unit,
    onCloseClick: () -> Unit,
) {
    val recording = uiState.isRecording
    val preview = uiState.showPreview
    val close = uiState.isRecording.not()
    val warning = uiState.warning
    val speedNotSafe = uiState.speedNotSafe
    val speed = uiState.speed
    val speedLimit = uiState.limitSpeed

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                if (speedLimit != null)
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color.White,
                                shape = CircleShape
                            )
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            modifier = Modifier
                                .border(
                                    BorderStroke(
                                        width = 5.dp,
                                        color = Color(0xFFDF4444)
                                    ),
                                    shape = CircleShape
                                )
                                .clip(CircleShape)
                                .size(55.dp)
                                .wrapContentHeight(align = Alignment.CenterVertically),
                            text = "$speedLimit",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.W700,
                                fontSize = 24.sp
                            ),
                            color = Color.Black
                        )
                    }
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(
                modifier = Modifier
                    .align(alignment = Alignment.CenterVertically)
                    .width(230.dp)
                    .clip(
                        shape = RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomEnd = 16.dp,
                            bottomStart = 16.dp
                        )
                    )
                    .background(color = Color.Black.copy(alpha = .35f)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (speed == null) "-" else "$speed",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.W400,
                        fontSize = 54.sp
                    ),
                    color = if (speedNotSafe) Color(0xFFDC2525) else Color.White
                )
                Text(
                    text = "km/h",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.W700,
                        fontStyle = FontStyle.Italic,
                        fontSize = 18.sp,
                    ),
                    color = if (speedNotSafe) Color(0xFFDC2525) else Color.White
                )
            }

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
            ) {
                Spacer(modifier = Modifier.width(16.dp))
                if (warning)
                    Image(
                        painter = painterResource(id = R.drawable.gizo_transportation_circle),
                        contentDescription = "warning",
                        modifier = Modifier
                            .width(56.dp),
                    )
            }

        }

        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                painter = painterResource(id = if (recording) R.drawable.gizo_ic_stop else R.drawable.gizo_ic_record),
                contentDescription = "record",
                modifier = Modifier
                    .size(64.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onRecordingClick() },
            )
            Spacer(modifier = Modifier.size(12.dp))
            Image(
                painter = painterResource(id = if (preview) R.drawable.gizo_ic_preview_hide else R.drawable.gizo_ic_preview_show),
                contentDescription = "toggle preview",
                modifier = Modifier
                    .size(40.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onPreviewClick() },
            )
        }

        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (close)
                Image(
                    painter = painterResource(id = R.drawable.gizo_close),
                    contentDescription = "close",
                    modifier = Modifier
                        .size(50.dp)
                        .padding(8.dp)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onCloseClick() },
                )
            Spacer(modifier = Modifier.width(16.dp))

        }
    }
}

@Composable
fun DepthImage(modifier: Modifier, depthImage: ImageBitmap) {
    Box(
        modifier = modifier
    ) {
        Image(
            bitmap = depthImage,
            contentDescription = "Depth estimation result",
            modifier = Modifier
                .align(Alignment.Center)
        )
    }
}

@Preview(device = Devices.DEFAULT, widthDp = 720, heightDp = 320)
@Composable
fun RecordingScreenPreview() {
    AppTheme {
        RecordingScreenBody(
            uiState = RecordingUiState(),
            onPreviewClick = {},
            onRecordingClick = {},
            onCloseClick = {},
        )
    }
}

@Preview(device = Devices.DEFAULT, widthDp = 720, heightDp = 320)
@Composable
fun RecordingScreenRecordingPreview() {
    AppTheme {
        RecordingScreenBody(
            uiState = RecordingUiState(
                isRecording = true,
                warning = true,
                speed = 10,
                limitSpeed = 20,
            ),
            onPreviewClick = {},
            onRecordingClick = {},
            onCloseClick = {},
        )
    }
}

