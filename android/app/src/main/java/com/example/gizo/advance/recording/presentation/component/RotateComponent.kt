package com.example.gizo.advance.recording.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gizo.advance.R


@Composable
fun RotateComponent(modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(color = Color.Black.copy(alpha = 0.7f))
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = R.drawable.gizo_rotate),
            contentDescription = "rotation",
            alignment = Alignment.Center,
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Rotate your screen horizontally",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.W700,
                fontSize = 18.sp
            ),
            color = Color.White
        )
    }
}

@Preview(device = Devices.DEFAULT, widthDp = 720, heightDp = 360)
@Composable
fun RotateComponentPreview() {
    RotateComponent()
}