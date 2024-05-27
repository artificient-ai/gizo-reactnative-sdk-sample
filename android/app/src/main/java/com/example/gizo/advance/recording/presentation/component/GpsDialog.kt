package com.example.gizo.advance.recording.presentation.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.example.gizo.advance.R
import com.example.gizo.advance.designsystem.component.GizoAlertDialog
import com.example.gizo.advance.designsystem.theme.AppTheme

@Composable
fun GpsDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    GizoAlertDialog(
        title = {
            Text(
                text = stringResource(R.string.gizo_enable_gps),
                style = MaterialTheme.typography.titleMedium
            )
        },
        content = {
            Text(
                text = stringResource(R.string.gizo_gps_required),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmText = stringResource(R.string.gizo_enable),
        dismissText = stringResource(R.string.gizo_dismiss),
        onConfirm = {
            onConfirm()
        },
        onDismiss = {
            onDismiss()
        })
}

@Preview(device = Devices.DEFAULT, widthDp = 720, heightDp = 320)
@Composable
fun GpsDialogPreview() {
    AppTheme {
        GpsDialog({}, {})
    }
}

