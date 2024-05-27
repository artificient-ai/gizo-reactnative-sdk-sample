package com.example.gizo.advance.recording.presentation.nocamera

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gizo.advance.R
import com.example.gizo.advance.designsystem.component.GizoAlertDialog
import com.example.gizo.advance.designsystem.component.GizoAppBar
import com.example.gizo.advance.designsystem.component.GizoButtonDefaults
import com.example.gizo.advance.designsystem.component.GizoOutlinedButton
import com.example.gizo.advance.designsystem.theme.AppTheme
import com.example.gizo.advance.designsystem.theme.urbanistFontFamily
import com.example.gizo.advance.recording.presentation.RecordingNoCameraUiState
import com.example.gizo.advance.recording.presentation.RecordingViewModel

@Composable
fun RecordingRouteScreen(
    viewModel: RecordingViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}

) {

    val uiState by viewModel.noCameraUiState.collectAsState()

    BackHandler {
        if (uiState.isRecording.not())
            onBack()
    }

    RecordingRoute(
        modifier = modifier,
        uiState = uiState,
        onStartTrip = {
            viewModel.startRecordingNoCamera()
        },
        onEndTrip = {
            viewModel.endTripRecordingNoCamera()
        },
        onBack = {
            if (uiState.isRecording.not())
                onBack()
        }
    )

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
fun RecordingRoute(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onStartTrip: () -> Unit = {},
    onEndTrip: () -> Unit = {},
    uiState: RecordingNoCameraUiState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        GizoAppBar {
            onBack()
        }
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.padding(start = 24.dp, end = 24.dp)
        ) {

            Text(
                text = "Drive Safely", modifier = modifier
                    .padding(bottom = 8.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.W700,
                    fontFamily = urbanistFontFamily,
                    lineHeight = 40.sp,
                    fontSize = 32.sp,
                    textAlign = TextAlign.Center,
                    color = Color.White
                ),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.size(11.dp))

            Text(
                text = "Your safety is our priority, and your driving data is recorded for improving your driving experience.\nPlease DO NOT use your mobile phone while driving.",
                modifier = modifier
                    .padding(bottom = 8.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Normal,
                    fontFamily = urbanistFontFamily,
                    lineHeight = 26.sp,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = Color.White
                ),
                textAlign = TextAlign.Start
            )
        }

        Image(

            painter = painterResource(id = R.drawable.gizo_record_car),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)

        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp)
        ) {
            Image(

                painter = painterResource(id = R.drawable.gizo_record_route_loader),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.size(width = 48.dp, height = 48.dp)
            )

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = String.format("%02d", uiState.hour),
                    modifier = Modifier
                        .background(
                            color = Color(0x33D9D9D9),
                            shape = RoundedCornerShape(size = 10.dp)
                        )
                        .padding(10.dp)
                        .requiredWidth(48.dp),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.W500,
                        fontFamily = urbanistFontFamily,
                        lineHeight = 40.sp,
                        fontSize = 32.sp,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        letterSpacing = 2.sp,
                    ),
                )
                Spacer(modifier = Modifier.size(8.dp))

                Text(
                    text = "h",
                    modifier = Modifier
                        .padding(bottom = 10.dp),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.W500,
                        fontFamily = urbanistFontFamily,
                        lineHeight = 20.sp,
                        fontSize = 16.sp,
                        color = Color(0xFF979797),
                        letterSpacing = 2.sp,
                    ),
                )

                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = String.format("%02d", uiState.minute),
                    modifier = Modifier
                        .background(
                            color = Color(0x33D9D9D9),
                            shape = RoundedCornerShape(size = 10.dp)
                        )
                        .padding(10.dp)
                        .requiredWidth(48.dp),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.W500,
                        fontFamily = urbanistFontFamily,
                        lineHeight = 40.sp,
                        fontSize = 32.sp,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        letterSpacing = 2.sp,
                    ),
                )
                Spacer(modifier = Modifier.size(8.dp))

                Text(
                    text = "m",
                    modifier = Modifier
                        .padding(bottom = 10.dp),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.W500,
                        fontFamily = urbanistFontFamily,
                        lineHeight = 20.sp,
                        fontSize = 16.sp,
                        color = Color(0xFF979797),
                        letterSpacing = 2.sp,
                    ),
                )

                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = String.format("%02d", uiState.second),
                    modifier = Modifier
                        .background(
                            color = Color(0x33D9D9D9),
                            shape = RoundedCornerShape(size = 10.dp)
                        )
                        .padding(10.dp)
                        .requiredWidth(48.dp),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.W500,
                        fontFamily = urbanistFontFamily,
                        lineHeight = 40.sp,
                        fontSize = 32.sp,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        letterSpacing = 2.sp,
                    ),
                )
                Spacer(modifier = Modifier.size(8.dp))

                Text(
                    text = "s",
                    modifier = Modifier
                        .padding(bottom = 10.dp),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.W500,
                        fontFamily = urbanistFontFamily,
                        lineHeight = 20.sp,
                        fontSize = 16.sp,
                        color = Color(0xFF979797),
                        letterSpacing = 2.sp,
                    ),
                )

                Spacer(modifier = Modifier.size(8.dp))
            }

        }
        Spacer(modifier = Modifier.size(12.dp))

        if (uiState.isRecording) {
            EndTrip(onClick = {
                onEndTrip()
            })
        } else {
            StartTrip(onClick = {
                onStartTrip()
            })
        }

        Spacer(modifier = Modifier.size(12.dp))
    }

}

@Composable
fun EndTrip(onClick: () -> Unit) {
    GizoOutlinedButton(

        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .height(56.dp),
        onClick = {
            onClick()
        },
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xB2F44336)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color(0x1AF44336),
            contentColor = Color(0x1AF44336),
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color(0xFFDB4324).copy(
                alpha = GizoButtonDefaults.DisabledButtonContentAlpha
            ),
        ),
        shape = 10.dp,
    ) {
        Text(
            text = "End trip",

            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                fontFamily = urbanistFontFamily,
                fontSize = 16.sp
            ),
            color = Color(0xFFDB4324),
            maxLines = 1
        )
    }
}


@Composable
fun StartTrip(onClick: () -> Unit) {
    GizoOutlinedButton(

        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .height(56.dp),
        onClick = {
            onClick()
        },
        border = BorderStroke(
            width = 1.dp,
            color = Color.White
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color.White.copy(
                alpha = GizoButtonDefaults.DisabledButtonContentAlpha
            ),
        ),
        shape = 10.dp,
    ) {
        Text(
            text = "Start trip",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                fontFamily = urbanistFontFamily,
                fontSize = 16.sp
            ),
            color = Color.White,
            maxLines = 1
        )
    }
}

@Preview
@Composable
fun RecordingRouteScreenPreview() {
    AppTheme {
        RecordingRoute(uiState = RecordingNoCameraUiState(hour = 0, minute = 2, second = 44))
    }
}