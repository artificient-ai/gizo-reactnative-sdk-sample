package com.anonymous.gizoreactnativesdksample

import android.app.Application
import android.content.res.Configuration

import com.facebook.react.PackageList
import com.facebook.react.ReactApplication
import com.facebook.react.ReactNativeHost
import com.facebook.react.ReactPackage
import com.facebook.react.ReactHost
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.load
import com.facebook.react.defaults.DefaultReactNativeHost
import com.facebook.react.soloader.OpenSourceMergedSoMapping
import com.facebook.soloader.SoLoader

import expo.modules.ApplicationLifecycleDispatcher
import expo.modules.ReactNativeHostWrapper
import expo.modules.gizosdk.GizoSdkModule

class MainApplication : Application(), ReactApplication {

  override val reactNativeHost: ReactNativeHost = ReactNativeHostWrapper(
        this,
        object : DefaultReactNativeHost(this) {
          override fun getPackages(): List<ReactPackage> {
            val packages = PackageList(this).packages
            // Packages that cannot be autolinked yet can be added manually here, for example:
            // packages.add(new MyReactNativePackage());
            return packages
          }

          override fun getJSMainModuleName(): String = ".expo/.virtual-metro-entry"

          override fun getUseDeveloperSupport(): Boolean = BuildConfig.DEBUG

          override val isNewArchEnabled: Boolean = BuildConfig.IS_NEW_ARCHITECTURE_ENABLED
          override val isHermesEnabled: Boolean = BuildConfig.IS_HERMES_ENABLED
      }
  )

  override val reactHost: ReactHost
    get() = ReactNativeHostWrapper.createReactHost(applicationContext, reactNativeHost)

  override fun onCreate() {
    super.onCreate()
    SoLoader.init(this, OpenSourceMergedSoMapping)
    if (BuildConfig.IS_NEW_ARCHITECTURE_ENABLED) {
      // If you opted-in for the New Architecture, we load the native entry point for this app.
      load()
    }
    ApplicationLifecycleDispatcher.onApplicationCreate(this)
    GizoSdkModule.initialize(context = this, license = "eyJpZCI6IjQzYjRkNDE3LTVmM2YtNDYzOC02ODRjLTA4ZGQxOTM5MGM4ZCIsImxpY2Vuc2UtdHlwZSI6MSwiZXhwaXJhdGlvbi1kYXRlIjoiMjAyNS0xMi0xMFQyMzo1OTo1OSIsInBhY2thZ2UtbmFtZSI6ImNvbS5hbm9ueW1vdXMuZ2l6by1yZWFjdG5hdGl2ZS1zZGstc2FtcGxlIiwic2lnbmF0dXJlIjoiY1p1TGxQVnI1UE5uOUdRaDY2dFhhVU9LVnVZUUxFN0F6SkhnOWY3Ynp1SG5KajlaWk5aWVAzT2RpSnhmSmg4TEJ2ZGR2OWhqWDBSR2tiZlQxVkh2Q0toSU1zRzBnUFZKaU4wNGZZcVJ4Y1U3dCtQRituR0JXWitpUys5Z3hzR2Z4TkIwWU8rams0ZTF1ai9DYUhvUHpqaDJ3WGlpUm5GTlZhVk1iV2gyN25uczF1UjZpNyt4cC9nS2xvTWFqdlhrWGREb1huTFRDSE50UWJNQ2FLYmQxRVZDRnlJbmNaUEU5dmp4TnRqRVZKRjR3OUlGT2ZzZzNHNVJFNGJENmhIc0RwdDhMSUtJUTM3NnpZK1J6SnhyK1lpcW9oTHo5OFNjY0NtSE5QRWxHcURBZWVpejJYbm1qc2hpbXo5UytzVmZoRmxIaC9FR1pBNDNDZWNQY01pZ3lnPT0ifQ==")
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    ApplicationLifecycleDispatcher.onConfigurationChanged(this, newConfig)
  }
}
