package com.example.gizo.advance.recording.presentation

import androidx.compose.ui.graphics.ImageBitmap

data class RecordingUiState(
    val isRecording: Boolean = false,
    val isAlive: Boolean = false,
    val showPreview: Boolean = true,
    val isGravityAlign: Boolean = false,
    val needGps: Boolean = false,
    val preview: ImageBitmap? = null,
    val log: HashMap<String, String> = hashMapOf(),
    val limitSpeed: Int? = null,
    val speed: Int? = null,
    val speedNotSafe: Boolean = false,
    val warning: Boolean = false,
    val danger: Boolean = false,
    val reportAccident: Boolean = false,
    val overheatingDialog: OverheatingDialogState = OverheatingDialogState(),
    val lowBatteryDialog: LowBatteryDialogState = LowBatteryDialogState()
)

data class RecordingNoCameraUiState(
    val hour: Int = 0,
    val minute: Int = 0,
    val second: Int = 0,
    val isRecording: Boolean = false,
    val isLowBattery: Boolean = true,
    val isOverheating: Boolean = true,
    val log: HashMap<String, String> = hashMapOf(),
    val overheatingDialog: OverheatingDialogState = OverheatingDialogState(),
    val lowBatteryDialog: LowBatteryDialogState = LowBatteryDialogState(),
    val notifyInVehicle: Boolean = false,
)

enum class RecordingState {
    STILL,
    Background,
    Full,
    NoCamera,
}

data class OverheatingDialogState(
    val show: Boolean = false,
)

data class LowBatteryDialogState(
    val show: Boolean = false,
)

sealed class RecordingUiEvent {
    data class Error(val message: String) : RecordingUiEvent()
    data class Alert(val message: String) : RecordingUiEvent()
}

sealed class NotificationEvent {
    data object BackgroundDefault : NotificationEvent()
    data object BackgroundStill : NotificationEvent()
    data object BackgroundNoCamera : NotificationEvent()
    data class InVehicleQuestion(val needPermission: Boolean) : NotificationEvent()
    data class RecordingFullQuestion(val needPermission: Boolean) : NotificationEvent()
}