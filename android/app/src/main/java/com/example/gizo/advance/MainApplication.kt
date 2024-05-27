package com.example.gizo.advance

import android.app.Application
import android.util.Log
import androidx.camera.video.Quality
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.facebook.react.PackageList
import com.facebook.react.ReactApplication
import com.facebook.react.ReactHost
import com.facebook.react.ReactNativeHost
import com.facebook.react.ReactPackage
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.load
import com.facebook.react.defaults.DefaultReactHost.getDefaultReactHost
import com.facebook.react.defaults.DefaultReactNativeHost
import com.facebook.soloader.SoLoader
import dagger.hilt.android.HiltAndroidApp
import de.artificient.gizo.sdk.Gizo
import de.artificient.gizo.sdk.model.AnalysisDelegateType
import de.artificient.gizo.sdk.setting.GizoAnalysisSettings
import de.artificient.gizo.sdk.setting.GizoAppOptions
import de.artificient.gizo.sdk.setting.GizoBatterySetting
import de.artificient.gizo.sdk.setting.GizoDeviceEventSetting
import de.artificient.gizo.sdk.setting.GizoGpsSetting
import de.artificient.gizo.sdk.setting.GizoImuSetting
import de.artificient.gizo.sdk.setting.GizoOrientationSetting
import de.artificient.gizo.sdk.setting.GizoUserActivitySetting
import de.artificient.gizo.sdk.setting.GizoVideoSetting

@HiltAndroidApp
class MainApplication : Application(), ReactApplication, ViewModelStoreOwner {

    override val reactNativeHost: ReactNativeHost =
        object : DefaultReactNativeHost(this) {
            override fun getPackages(): List<ReactPackage> =
                PackageList(this).packages.apply {
                    // Packages that cannot be autolinked yet can be added manually here, for example:
                    // add(MyReactNativePackage())
                    add(GizoPackage())
                }

            override fun getJSMainModuleName(): String = "index"

            override fun getUseDeveloperSupport(): Boolean = BuildConfig.DEBUG

            override val isNewArchEnabled: Boolean = BuildConfig.IS_NEW_ARCHITECTURE_ENABLED
            override val isHermesEnabled: Boolean = BuildConfig.IS_HERMES_ENABLED
        }

    override val reactHost: ReactHost
        get() = getDefaultReactHost(applicationContext, reactNativeHost)

    override fun onCreate() {
        super.onCreate()
        SoLoader.init(this, false)
        if (BuildConfig.IS_NEW_ARCHITECTURE_ENABLED) {
            // If you opted-in for the New Architecture, we load the native entry point for this app.
            load()
        }

        Gizo.initialize(
            this,
            GizoAppOptions.Builder(getString(R.string.gizo_access_token))
                .debug(true)
                .folderName("GizoSample")
                .analysisSetting(
                    GizoAnalysisSettings.Builder()
                        .allowAnalysis(true)
                        .modelName("arti_sense.data")
                        .loadDelegate(AnalysisDelegateType.Auto)
                        .saveMatrixFile(true)
                        .saveTtcCsvFile(true)
                        .build()
                )
                .imuSetting(
                    GizoImuSetting.Builder()
                        .allowAccelerationSensor(true)
                        .allowMagneticSensor(true)
                        .allowGyroscopeSensor(true)
                        .saveCsvFile(true)
                        .saveDataTimerPeriod(5000L)
                        .build()
                )
                .gpsSetting(
                    GizoGpsSetting.Builder()
                        .allowGps(true)
                        .mapBoxKey(getString(R.string.mapbox_access_token))
                        .saveCsvFile(true)
                        .build()
                )
                .videoSetting(
                    GizoVideoSetting.Builder()
                        .allowRecording(true)
                        .quality(Quality.LOWEST)
                        .build()
                )
                .batterySetting(
                    GizoBatterySetting.Builder()
                        .checkBattery(true)
                        .checkThermal(true)
                        .checkTemperature(true)
                        .saveCsvFile(true)
                        .build()
                )
                .orientationSetting(
                    GizoOrientationSetting.Builder()
                        .allowGravitySensor(true)
                        .build()
                )
                .deviceEventSetting(
                    GizoDeviceEventSetting.Builder()
                        .allowDeviceEvent(true)
                        .saveCsvFile(true)
                        .build()
                )
                .userActivitySetting(
                    GizoUserActivitySetting.Builder()
                        .allowUserActivity(true)
                        .saveCsvFile(true)
                        .build()
                )
                .build()
        )

        StartUpInitializer.initialize(context = this)

        Gizo.app.setLoadModelObserver { status ->
            Log.d("LoadModelStatus", "status:" + status.name)
        }

        Gizo.app.loadModel()

    }

    private val appViewModelStore: ViewModelStore by lazy {
        ViewModelStore()
    }
    override val viewModelStore: ViewModelStore
        get() = appViewModelStore
}
