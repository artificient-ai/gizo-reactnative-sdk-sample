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
    GizoSdkModule.initialize(context = this, license = "eyJpZCI6IjA3M2UyYjliLTA2NjctNDg3Mi00NTc0LTA4ZGQxOWI2OGJiNiIsImxpY2Vuc2UtdHlwZSI6MSwiZXhwaXJhdGlvbi1kYXRlIjoiMjAyNS0xMi0xMVQyMzo1OTo1OSIsInBhY2thZ2UtbmFtZSI6ImNvbS5hbm9ueW1vdXMuZ2l6b3JlYWN0bmF0aXZlc2Rrc2FtcGxlIiwic2lnbmF0dXJlIjoibVN1UkZGVStxUzEyM3JZZUZ5S2E0MDdvVW9RL1AvOHpQM3E0NGVMU0pKWVRwcytiaTRwSmYrL2Q1b2VBSGVZZmZNeVZFRXVzTjRxTGpoK3o2WFJkTHFHY2NkTVZrY29jcWhBZnFjaGxjQ1FHS1B4ZG40NXJkeXU4ZzhLTzVsUjdRZjZFTGxDN1lOQXpLSjZXa1VKWUlocndRZW5lYkF6NVRrNjJJYzV1VzdYdHdzNmVJU0p2dFRBR1lJQllYSnhUci9kT0JrL1Ivb081RWhPOTZYSkZORU84UXY4QkNFTnZWUVdWdzBvanczOTdvTnV3bTVnVEh0bnVMV0xuYmtNYWY3Y0dTR1hFbjk3c2FIb0xRN3hUdUplNUtPZFROSkZLRWU1ZXF2SG82bCsrQ3AxeFlTV3lSY1ZKdk5NYmZNOXpxbXB3NlZqM3MxNkZRVkhzeXcydUdBPT0ifQ==")
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    ApplicationLifecycleDispatcher.onConfigurationChanged(this, newConfig)
  }
}
