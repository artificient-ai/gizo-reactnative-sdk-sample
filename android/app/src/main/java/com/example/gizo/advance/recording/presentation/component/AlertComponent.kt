package com.example.gizo.advance.recording.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gizo.advance.R
import com.example.gizo.advance.designsystem.theme.redesignRedColor


@Composable
fun AlertComponent(modifier: Modifier = Modifier) {
    val largeRadialGradient = object : ShaderBrush() {
        override fun createShader(size: Size): Shader {
            val biggerDimension = maxOf(size.height, size.width)
            return RadialGradientShader(
                colors = listOf(redesignRedColor.copy(alpha = 0f), redesignRedColor),
                center = size.center,
                radius = biggerDimension / 2f,
                colorStops = listOf(0f, 0.95f)
            )
        }
    }

    Surface(
        modifier,
        color = Color.Black.copy(alpha = 0.6f),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(largeRadialGradient),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = redesignRedColor,
                shape = CircleShape,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.CenterHorizontally),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.gizo_car_brake),
                    contentDescription = "car break",
                    alignment = Alignment.Center,
                    modifier = Modifier
                        .padding(20.dp)
                        .size(96.dp)
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            Text(
                text = stringResource(id = R.string.gizo_ttc_danger_message),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.W700
                ),
                color = Color.White
            )
        }
    }
}

@Preview(device = Devices.DEFAULT, widthDp = 720, heightDp = 360)
@Composable
fun AlertDangerPreview() {
    AlertComponent()
}