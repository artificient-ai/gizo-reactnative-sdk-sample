package com.example.gizo.advance.recording.presentation

import android.util.Log
import androidx.camera.view.PreviewView
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.artificient.gizo.sdk.Gizo
import de.artificient.gizo.sdk.GizoAnalysis
import de.artificient.gizo.sdk.model.BatteryStatus
import de.artificient.gizo.sdk.model.TTCAlert
import de.artificient.gizo.sdk.model.UserActivity
import de.artificient.gizo.sdk.model.UserActivityTransitionType
import de.artificient.gizo.sdk.model.UserActivityType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Timer
import kotlin.concurrent.fixedRateTimer

class RecordingViewModel @AssistedInject constructor() : ViewModel() {

    companion object {
        val TAG = RecordingViewModel::class.simpleName
        private val DrivingActivityType = UserActivityType.IN_VEHICLE
    }

    private val gizoAnalysis: GizoAnalysis = Gizo.app.gizoAnalysis

    private val _event = Channel<RecordingUiEvent>()
    val event = _event.receiveAsFlow()

    private val _notificationEvent = MutableSharedFlow<NotificationEvent>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val notificationEvent = _notificationEvent.asSharedFlow()

    private val _uiState = MutableStateFlow(RecordingUiState())
    val uiState = _uiState.asStateFlow()

    private val _recordingNoCameraUiState = MutableStateFlow(RecordingNoCameraUiState())
    val noCameraUiState = _recordingNoCameraUiState.asStateFlow()

    private val _recordingState = MutableStateFlow(RecordingState.STILL)
    private val recordingState = _recordingState.asStateFlow()

    private val currentRecordingState
        get() = recordingState.value

    private var lastUserActivity = UserActivityType.STILL

    private var stillJob: Job? = null

    private var recordingNoCameraTimerJob: Job? = null
    private var recordingBackgroundTimerJob: Job? = null

    private var notifyInVehicleTimerJob: Job? = null
    private var notifyRecordingFullQuestionTimerJob: Job? = null

    private var tripTimer: Timer? = null

    private var isAutoStop = false

    private val gpsRequested = false

    private val ttcAlertFlow = MutableStateFlow(TTCAlert.None)

    @OptIn(ExperimentalCoroutinesApi::class)
    val ttcDangerFlow = ttcAlertFlow
        .flatMapConcat { alert ->
            flow {
                if (alert == TTCAlert.Collision) {
                    emit(true)
                    delay(2000)
                    emit(false)
                    delay(15000)
                } else
                    emit(false)
            }
        }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    val ttcWarningFlow = ttcAlertFlow
        .flatMapConcat { alert ->
            flow {
                if (alert == TTCAlert.Tailgating) {
                    emit(true)
                    delay(2000)
                } else
                    emit(false)
            }
        }.distinctUntilChanged()

    val cameraInitialized
        get() = gizoAnalysis.cameraInitialized


    init {

        gizoAnalysis.onBatteryStatusChange = { status ->
            _uiState.update {
                it.copy(
                    log = it.log.apply {
                        put("Battery", status.name)
                    },
                )
            }
            when (status) {
                BatteryStatus.LOW_BATTERY_STOP -> {
                    if (recordingState.value == RecordingState.Full) {
                        _uiState.update {
                            it.copy(
                                lowBatteryDialog = LowBatteryDialogState(
                                    show = true
                                ),
                            )
                        }
                        stopRecordingVideo()
                    }

                    lockAnalysis(true)
                }

                BatteryStatus.LOW_BATTERY_WARNING -> {
                    _event.trySend(RecordingUiEvent.Alert("Battery is low, we will stop analysis"))
                    lockAnalysis(true)
                }

                BatteryStatus.NORMAL -> {
                    lockAnalysis(false)

                }
            }

        }

        gizoAnalysis.ttcCalculator { frontObject, speed, ttc ->
            ttc
        }

        gizoAnalysis.ttcStatusCalculator { ttc, speed, ttcStatus ->
            ttcStatus
        }

        gizoAnalysis.onAnalysisResult = { preview,
                                          ttc,
                                          ttcStatus,
                                          frontObject,
                                          speed,
                                          gpsTime ->

            ttcAlertFlow.tryEmit(ttcStatus)
            _uiState.update {
                it.copy(
                    preview = preview?.asImageBitmap(),
                )
            }

            _uiState.update {
                it.copy(
                    log = it.log.apply {
                        put(
                            "TTC ", ttc.toString()
                        )
                    },
                )
            }
        }

        gizoAnalysis.onVideoSessionStatus = { isVideoRecording, previewAttached ->
            _uiState.update {
                it.copy(
                    isRecording = isVideoRecording,
                    showPreview = previewAttached
                )
            }
        }

        gizoAnalysis.onSessionStatus = { inProgress ->

        }

        gizoAnalysis.onLocationChange = { location, isGpsOn ->
            if (isGpsOn == false && gpsRequested.not())
                _uiState.update {
                    it.copy(
                        needGps = true,
                    )
                }
        }

        gizoAnalysis.onSpeedChange = { speedLimitKph, speedKph ->
            _uiState.update {
                it.copy(
                    limitSpeed = speedLimitKph,
                    speed = speedKph,
                    speedNotSafe = speedLimitKph != null && speedLimitKph > 0 && speedKph > 0 && speedKph > speedLimitKph
                )
            }
        }

        ttcWarningFlow.onEach { warning ->
            _uiState.update {
                it.copy(
                    warning = warning,
                )
            }
        }.launchIn(viewModelScope)


        ttcDangerFlow.onEach { danger ->
            _uiState.update {
                it.copy(
                    danger = danger,
                )
            }
        }.launchIn(viewModelScope)

        gizoAnalysis.onAccelerationSensor = { sensorEvent ->
        }

        gizoAnalysis.onLinearAccelerationSensor = { sensorEvent ->
        }

        gizoAnalysis.onAccelerationUncalibratedSensor = { sensorEvent ->
        }

        gizoAnalysis.onGyroscopeSensor = { sensorEvent ->
        }

        gizoAnalysis.onMagneticSensor = { sensorEvent ->
        }

        gizoAnalysis.onGravitySensor = { sensorEvent ->
        }

        gizoAnalysis.onImuSensor =
            { accelerationEvent, linearAccelerationEvent, accelerationUncalibratedEvent, gyroscopeEvent, magneticEvent, gravityEvent ->

            }

        gizoAnalysis.onGravityAlignmentChange = { isAlign ->
            if (isAlign == true)
                _uiState.update {
                    it.copy(isGravityAlign = isAlign)
                }
        }

        gizoAnalysis.onMicrophoneSensor = { event ->
        }

        gizoAnalysis.onTelephonySensor = { event ->
        }

        gizoAnalysis.onScreenLock = { event ->
        }

        gizoAnalysis.onDeviceEvent = { event, value ->
        }

        gizoAnalysis.onActiveApp = { appPackage, category, label, system ->
        }

        ttcWarningFlow.onEach { warning ->
            _uiState.update {
                it.copy(
                    warning = warning,
                )
            }
        }.launchIn(viewModelScope)


        ttcDangerFlow.onEach { danger ->
            _uiState.update {
                it.copy(
                    danger = danger,
                )
            }
        }.launchIn(viewModelScope)

        gizoAnalysis.onUserActivity = { time, activities ->

            activities.forEach { activity ->
                Log.d(
                    TAG,
                    "UserActivity: " + activity.userActivity?.name + "-" + activity.transition?.name
                )
                _uiState.update {
                    it.copy(
                        log = it.log.apply {
                            put(
                                "UserActivity",
                                activity.userActivity?.name + "-" + activity.transition?.name
                            )
                        },
                    )
                }
            }
            onUserActivityTransitionChange(activities)
        }

        gizoAnalysis.onTemperatureChange = { temp ->
            Log.d(TAG, "checkTemperature $temp")
            _uiState.update {
                it.copy(
                    log = it.log.apply {
                        put("Temperature", "${if (temp == 0) 0f else temp / 10f} °C")
                    },
                )
            }

            if (temp >= 440 && _uiState.value.isRecording) {
                _uiState.update {
                    it.copy(
                        overheatingDialog = OverheatingDialogState(
                            show = true
                        ),
                    )
                }
                stopRecordingVideo()
            }

            if (temp >= 440 && _recordingNoCameraUiState.value.isRecording) {
                _recordingNoCameraUiState.update {
                    it.copy(
                        overheatingDialog = OverheatingDialogState(
                            show = true
                        ),
                    )
                }

                stopRecording()
            }
        }
        gizoAnalysis.onThermalStatusChange = { status ->
            _uiState.update {
                it.copy(
                    log = it.log.apply {
                        put("Thermal", status.name)
                    },
                )
            }
        }

    }

    private fun onUserActivityTransitionChange(activityList: List<UserActivity>) {
        val startedTransition =
            activityList.lastOrNull { it.transition == UserActivityTransitionType.STARTED }?.userActivity
                ?: lastUserActivity
        val stoppedTransition =
            activityList.lastOrNull { it.transition == UserActivityTransitionType.STOPPED }?.userActivity

        val isNewTransition = startedTransition != lastUserActivity

        when (currentRecordingState) {
            RecordingState.STILL -> {
                when {
                    isNewTransition.not() -> {}
                    startedTransition == DrivingActivityType -> {
                        stillPauseCancel()
                        notifyInVehicle()
                    }

                    startedTransition != UserActivityType.STILL -> {
                        stillPauseCancel()
                        changeRecordState(RecordingState.Background)
                    }

                }
            }

            RecordingState.Background -> {
                when {
                    isNewTransition.not() -> {}

                    startedTransition == DrivingActivityType -> {
                        stillPauseCancel()
                        notifyInVehicle()
                    }

                    startedTransition == UserActivityType.STILL -> {
                        stillPauseStart {
                            if (lastUserActivity == UserActivityType.STILL)
                                changeRecordState(RecordingState.STILL)
                        }
                    }

                    else -> {
                        stillPauseCancel()
                    }

                }
            }

            RecordingState.NoCamera -> {
                when {
                    isNewTransition.not() -> {}
                    startedTransition == DrivingActivityType -> {
                        stillPauseCancel()
                    }

                    startedTransition != DrivingActivityType -> {
                        if (isStillPauseRunning().not()) {
                            if (startedTransition == UserActivityType.STILL) {
                                stillPauseStart {
                                    if (lastUserActivity == UserActivityType.STILL)
                                        changeRecordState(RecordingState.STILL)
                                    else {
                                        changeRecordState(RecordingState.Background)
                                    }
                                }
                            } else {
                                stillPauseStart(shortDuration = true) {
                                    if (lastUserActivity == UserActivityType.STILL)
                                        changeRecordState(RecordingState.STILL)
                                    else {
                                        changeRecordState(RecordingState.Background)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            else -> {}
        }

        lastUserActivity = startedTransition

    }

    private fun notifyInVehicle() {
        changeRecordState(RecordingState.NoCamera)
    }

    fun notifyInVehicleNotified() {
        notifyInVehicleTimerJob?.cancel()
        if (currentRecordingState == RecordingState.STILL)
            changeRecordState()
        _notificationEvent.tryEmit(NotificationEvent.BackgroundDefault)
    }

    fun notifyRecordingFullNotified() {
        notifyRecordingFullQuestionTimerJob?.cancel()
        _notificationEvent.tryEmit(NotificationEvent.BackgroundDefault)
    }

    fun notifyAcceptRecordingNoCamera() {
        _notificationEvent.tryEmit(NotificationEvent.BackgroundDefault)
        if (currentRecordingState != RecordingState.NoCamera)
            changeRecordState(RecordingState.NoCamera)
    }

    fun notifiedRecordingFull() {
        notifyInVehicleTimerJob?.cancel()
        notifyRecordingFullQuestionTimerJob?.cancel()
        _notificationEvent.tryEmit(NotificationEvent.BackgroundDefault)
    }

    fun notifiedRecordingSelect() {
        notifyInVehicleTimerJob?.cancel()
        notifyRecordingFullQuestionTimerJob?.cancel()
        _notificationEvent.tryEmit(NotificationEvent.BackgroundDefault)
    }

    fun actionDrivingYes() {
        notifyInVehicleTimerJob?.cancel()
        changeRecordState(RecordingState.NoCamera)
    }

    fun attachPreview(previewView: PreviewView) {
        gizoAnalysis.attachPreview(previewView)
    }

    fun setBackgroundAnalysisAlive(isAlive: Boolean) {

        _uiState.update {
            it.copy(isAlive = isAlive)
        }
    }


    private fun lockAnalysis(isLock: Boolean) {
        gizoAnalysis.lockAnalysis(isLock)
    }

    private fun getBatteryStatus() =
        gizoAnalysis.batteryLastStatus

    fun togglePreview(previewView: PreviewView) {
        if (uiState.value.showPreview) {
            gizoAnalysis.lockPreview()
        } else {
            gizoAnalysis.unlockPreview(previewView)
        }
    }

    fun stopCamera() {
        gizoAnalysis.stopCamera()
    }

    fun startRecordingVideo() {
        Log.d(TAG, "startRecordingVideo")
        viewModelScope.launch {
            if (uiState.value.isRecording.not()) {
                if (getBatteryStatus() == BatteryStatus.LOW_BATTERY_STOP) {
                    _uiState.update {
                        it.copy(
                            lowBatteryDialog = LowBatteryDialogState(
                                show = true
                            ),
                        )
                    }
                    return@launch
                }
                stopRecording().join()
                _notificationEvent.tryEmit(NotificationEvent.BackgroundNoCamera)
                Log.d(TAG, "startRecordingVideo start")
                attachMapNavigation()
                Gizo.setup { option ->
                    option.toBuilder()
                        .analysisSetting(
                            option.analysisSetting.toBuilder()
                                .saveMatrixFile(true)
                                .saveTtcCsvFile(true)
                                .build()
                        )
                        .build()
                }
                gizoAnalysis.startSavingSession(videoRecording = true, gpsRecording = true)
                _recordingState.tryEmit(RecordingState.Full)
            } else {
                Log.d(TAG, "startRecordingVideo stop")
                stopRecordingVideo()
            }
        }
    }

    fun stopRecordingVideo() =
        viewModelScope.launch {
            Log.d(TAG, "stopRecordingVideo")
            gizoAnalysis.stopSavingSession()
            changeRecordState().join()
        }


    fun start(
        lifecycleOwner: LifecycleOwner
    ) {
        gizoAnalysis.start(lifecycleOwner = lifecycleOwner) {
            gizoAnalysis.bindMapNavigation(lifecycleOwner = lifecycleOwner)
        }
    }

    fun stop() {
        changeRecordState(RecordingState.STILL)
        gizoAnalysis.stop()
    }

    fun startCamera(
        lifecycleOwner: LifecycleOwner,
        onDone: (() -> Unit)
    ) {
        _uiState.update {
            it.copy(isGravityAlign = false)
        }
        gizoAnalysis.startCamera(lifecycleOwner = lifecycleOwner) {
            attachMapNavigation()
            onDone()
        }

    }

    private fun detachMapNavigation() = gizoAnalysis.detachMapNavigation()

    fun attachMapNavigation() = gizoAnalysis.attachMapNavigation()

    fun gpsNeedConfirm() {
        _uiState.update {
            it.copy(
                needGps = false,
            )
        }
    }

    fun gpsNeedCancel() {
        _uiState.update {
            it.copy(
                needGps = false,
            )
        }
    }

    fun startRecordingNoCamera(autoStop: Boolean = false) {
        Log.d(TAG, "startRecordingNoCamera autoStop: $autoStop ")

        viewModelScope.launch {
            if (autoStop.not() && getBatteryStatus() == BatteryStatus.LOW_BATTERY_STOP) {
                _recordingNoCameraUiState.update {
                    it.copy(
                        lowBatteryDialog = LowBatteryDialogState(
                            show = true
                        ),
                    )
                }
                return@launch
            }

            stopRecording().join()
            attachMapNavigation()
            isAutoStop = autoStop
            val startTrip = Date(System.currentTimeMillis())
            _notificationEvent.tryEmit(NotificationEvent.BackgroundNoCamera)
            Log.d(TAG, "startRecordingNoCamera")
            _recordingState.emit(RecordingState.NoCamera)
            Gizo.setup { option ->
                option.toBuilder()
                    .analysisSetting(
                        option.analysisSetting.toBuilder()
                            .saveMatrixFile(false)
                            .saveTtcCsvFile(false)
                            .build()
                    )
                    .build()
            }
            gizoAnalysis.startSavingSession(videoRecording = false, gpsRecording = true)
            recordingNoCameraTimerJob?.cancel()
            recordingNoCameraTimerJob = viewModelScope.launch {
                tripTimer = fixedRateTimer(
                    name = "tripTimer",
                    period = 1000,
                    initialDelay = 0
                ) {
                    val dateNow = Date(System.currentTimeMillis())

                    val timeDifference = dateNow.time - startTrip.time
                    _recordingNoCameraUiState.update { state ->
                        state.copy(
                            hour = (timeDifference / 3600000).toInt(),
                            minute = ((timeDifference % 3600000) / 60000).toInt(),
                            second = ((timeDifference % 60000) / 1000).toInt(),
                            isRecording = true
                        )
                    }
                }
            }
        }
    }

    fun onAlertDismiss() {
        _uiState.update {
            it.copy(
                lowBatteryDialog = LowBatteryDialogState(
                    show = false
                ),
                overheatingDialog = OverheatingDialogState(
                    show = false
                ),
            )
        }

        _recordingNoCameraUiState.update {
            it.copy(
                lowBatteryDialog = LowBatteryDialogState(
                    show = false
                ),
                overheatingDialog = OverheatingDialogState(
                    show = false
                ),
            )
        }
    }

    private fun startRecordingBackground() {
        Log.d(TAG, "startRecordingBackground")
        stopRecording()
        recordingBackgroundTimerJob?.cancel()
        recordingBackgroundTimerJob = viewModelScope.launch {
            while (true) {
                gizoAnalysis.stopSavingSession()
                var delayTime = 1000L * 60 * 60
                val midnight = calculateTimeDifferenceUntilMidNight()
                Log.d(
                    TAG,
                    "startRecordingBackground delayTime:$delayTime  midnight:$midnight "
                )
                if (midnight in 1 until delayTime)
                    delayTime = midnight + 1000L
                _notificationEvent.tryEmit(NotificationEvent.BackgroundDefault)
                attachMapNavigation()
                _recordingState.emit(RecordingState.Background)
                Gizo.setup { option ->
                    option.toBuilder()
                        .analysisSetting(
                            option.analysisSetting.toBuilder()
                                .saveMatrixFile(false)
                                .saveTtcCsvFile(false)
                                .build()
                        )
                        .build()
                }
                gizoAnalysis.startSavingSession(
                    videoRecording = false,
                    gpsRecording = true
                )
                delay(delayTime)
            }
        }
    }

    private fun stillPauseStart(shortDuration: Boolean = false, onDone: () -> Unit) {
        Log.d(TAG, "stillPause Start")
        stillJob?.cancel()
        stillJob = viewModelScope.launch {
            delay(if (shortDuration) 30 * 1000 else 5 * 60 * 1000)
            onDone()
        }
    }

    private fun changeRecordState(): Job {
        Log.d(TAG, "changeRecordState empty lastUserActivity:$lastUserActivity")
        return if (lastUserActivity == UserActivityType.STILL)
            changeRecordState(RecordingState.STILL)
        else
            changeRecordState(RecordingState.Background)
    }

    private fun changeRecordState(newRecordingState: RecordingState) =
        viewModelScope.launch {
            Log.d(TAG, "changeRecordState : ${newRecordingState.name}")
            when (newRecordingState) {
                RecordingState.STILL -> {
                    stopRecording()
                    if (_uiState.value.isAlive.not()) {
                        detachMapNavigation()
                    }
                    _recordingState.tryEmit(RecordingState.STILL)
                    _notificationEvent.tryEmit(NotificationEvent.BackgroundStill)
                }

                RecordingState.Background -> {
                    startRecordingBackground()
                }

                RecordingState.NoCamera -> {
                    startRecordingNoCamera(autoStop = true)
                }

                else -> {

                }
            }
        }

    private fun isStillPauseRunning() =
        stillJob?.isActive == true


    private fun stillPauseCancel() {
        Log.d(TAG, "stillPause Cancel")
        stillJob?.cancel()
    }

    private fun calculateTimeDifferenceUntilMidNight(): Long {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val currentTime =
            SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val midnightTime = "23:59:59"
        val currentDate = sdf.parse(currentTime)
        val midnightDate = sdf.parse(midnightTime)
        return (midnightDate?.time ?: 0) - (currentDate?.time ?: 0)
    }

    private fun stopRecording() = viewModelScope.launch {
        Log.d(TAG, "stopRecording")
        recordingBackgroundTimerJob?.cancel()
        gizoAnalysis.stopSavingSession()
        recordingNoCameraTimerJob?.cancel()
        isAutoStop = false
        tripTimer?.cancel()
        _recordingNoCameraUiState.update { state ->
            state.copy(
                hour = 0,
                minute = 0,
                second = 0,
                isRecording = false
            )
        }
    }

    fun endTripRecordingNoCamera() {
        changeRecordState()
    }


    class Factory(
        private val assistedFactory: ViewModelAssistedFactory,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return assistedFactory.create() as T
        }
    }

    @AssistedFactory
    interface ViewModelAssistedFactory {
        fun create(): RecordingViewModel
    }

}
