import * as Application from "expo-application";
import * as IntentLauncher from "expo-intent-launcher";
import { Platform, PermissionsAndroid, NativeModules } from "react-native";

import {
  checkLocationAccuracy,
  checkMultiple,
  PERMISSIONS,
  RESULTS,
  LocationAccuracy,
  request,
  Permission
} from "react-native-permissions";


import constants from "./constants";
const {
  ANDROID: {
    READ_PHONE_STATE,
    RECORD_AUDIO,
    ACTIVITY_RECOGNITION,
    WRITE_EXTERNAL_STORAGE,
    READ_EXTERNAL_STORAGE,
    ACCESS_FINE_LOCATION,
    ACCESS_COARSE_LOCATION,
    ACCESS_BACKGROUND_LOCATION,
  },
  IOS: { LOCATION_WHEN_IN_USE, LOCATION_ALWAYS, MOTION },
} = PERMISSIONS;

interface PermissionProps {
  "ios.permission.LOCATION_ALWAYS": string;
  "ios.permission.LOCATION_WHEN_IN_USE": string;
  "ios.permission.MOTION": string;
  "android.permission.READ_PHONE_STATE": string;
  "android.permission.RECORD_AUDIO": string;
  "android.permission.ACTIVITY_RECOGNITION": string;
  "android.permission.WRITE_EXTERNAL_STORAGE": string;
  "android.permission.READ_EXTERNAL_STORAGE": string;
  "android.permission.ACCESS_FINE_LOCATION": string;
  "android.permission.ACCESS_COARSE_LOCATION": string;
  "android.permission.ACCESS_BACKGROUND_LOCATION": string;
}

export const checkPermissions = async () => {
  let results;
  if (Platform.OS === "ios") {
    const states = await checkMultiple([
      LOCATION_WHEN_IN_USE,
      LOCATION_ALWAYS,
      MOTION,
    ]);
    let locationAccuracy;
    if (states[LOCATION_ALWAYS] === RESULTS.GRANTED) {
      locationAccuracy = await checkLocationAccuracy();
    }
    results = {
      ...states,
      locationAccuracy: locationAccuracy as LocationAccuracy,
    };
  } else {

    const permissions: Permission[] = [
      READ_PHONE_STATE,
      RECORD_AUDIO,
      ACTIVITY_RECOGNITION,
      ACCESS_FINE_LOCATION,
      ACCESS_COARSE_LOCATION,
      ACCESS_BACKGROUND_LOCATION,
    ];

    if (Number(Platform.Version) < 29) {
      permissions.push(
        WRITE_EXTERNAL_STORAGE,
        READ_EXTERNAL_STORAGE
      );
    }

    const states = await checkMultiple(permissions);

    results = {
      ...states,
    };
  }
  return results;
};

export const getLocationStatus = (responsePermission: PermissionProps) => {
  if (Platform.OS === "ios") {
    if (responsePermission[LOCATION_ALWAYS] === constants.PERMISSION_DENIED) {
      return "";
    } else {
      return responsePermission[LOCATION_ALWAYS];
    }
  } else {
    if (
      responsePermission[ACCESS_FINE_LOCATION] === constants.PERMISSION_DENIED
    ) {
      return "";
    } else {
      return responsePermission[ACCESS_FINE_LOCATION];
    }
  }
};

export const getMotionStatus = (responsePermission: any) => {
  if (Platform.OS === "ios") {
    if (responsePermission[MOTION] === constants.PERMISSION_DENIED) {
      return "";
    } else {
      return responsePermission[MOTION];
    }
  } else {
    if (
      responsePermission[ACTIVITY_RECOGNITION] === constants.PERMISSION_DENIED
    ) {
      return "";
    } else {
      return responsePermission[ACTIVITY_RECOGNITION];
    }
  }
};

// request permission for motion
export const permissionMotionRequest = async () => {
  if (Platform.OS === "ios") {
    await request(PERMISSIONS.IOS.MOTION);
  } else {
    // ANDROID
    await request(PERMISSIONS.ANDROID.ACTIVITY_RECOGNITION);
  }
};

// request permission for location
export const permissionLocationRequest = async () => {
  if (Platform.OS === "ios") {
    await request(PERMISSIONS.IOS.LOCATION_WHEN_IN_USE);
    await request(PERMISSIONS.IOS.LOCATION_ALWAYS);
  } else {
    // ANDROID
    await request(PERMISSIONS.ANDROID.ACCESS_FINE_LOCATION);
    await request(PERMISSIONS.ANDROID.ACCESS_BACKGROUND_LOCATION);
  }
};

// Request camera permission
export const permissionCameraRequest = async () => {
  if (Platform.OS === "ios") {
  } else {
    await request(PERMISSIONS.ANDROID.CAMERA);
  }
};

// Request microphone permission
export const permissionMicrophoneRequest = async () => {
  if (Platform.OS === "ios") {
  } else {
    await request(PERMISSIONS.ANDROID.RECORD_AUDIO);
  }
};

// Request phone state permission
export const permissionPhoneStateRequest = async () => {
  if (Platform.OS === "android") {
    await request(PERMISSIONS.ANDROID.READ_PHONE_STATE);
  }
};

export const permissionNotificationRequest = async () => {
  if (Platform.OS === "android") {
    const sdkVersion = Platform.constants.Release; // Get the Android version
    try {
      // Check if the SDK version is 33 or above
      if (parseInt(sdkVersion, 10) >= 13) {
        // Android 13+ (API 33+)
        const hasPermission = await PermissionsAndroid.check(
          PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS,
        );

        if (!hasPermission) {
          const granted = await PermissionsAndroid.request(
            PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS,
            {
              title: "Notification Permission",
              message:
                "App needs access to your notifications " +
                "to send you important updates.",
              buttonNeutral: "Ask Me Later",
              buttonNegative: "Cancel",
              buttonPositive: "OK",
            },
          );

          if (granted === PermissionsAndroid.RESULTS.GRANTED) {
            console.log("Notification permission granted");
          } else {
            console.log("Notification permission denied");
          }
        } else {
          console.log("Notification permission already granted");
        }
      } else {
        console.log(
          "POST_NOTIFICATIONS permission not required below Android 13",
        );
      }
    } catch (err) {
      console.error("Notification permission error:", err);
    }
  }
};

export const hasUsageStatsPermission = async () => {
  try {
    if (NativeModules.UsageStatsPermission) {
      console.log("UsageStatsPermission module is available");
    } else {
      console.error("UsageStatsPermission module is not available");
    }
  } catch (error) {
    console.error("Error checking Usage Stats Permission:", error);
    return false;
  }
};

export const requestUsageAccess = async () => {
  IntentLauncher.startActivityAsync(
    IntentLauncher.ActivityAction.USAGE_ACCESS_SETTINGS,
  );
};

export const requestBatteryOptimization = async () => {
  const packageName = Application.applicationId;

  IntentLauncher.startActivityAsync(
    IntentLauncher.ActivityAction.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
    {
      data: `package:${packageName}`,
    },
  );
};

export const permissionText = (
  locationStatus: string,
  motionStatus: string,
) => {
  if (
    locationStatus === "denied" ||
    (locationStatus === "blocked" && motionStatus === "granted")
  ) {
    return "Please provide “Always”  permission";
  } else if (locationStatus === "granted" && motionStatus !== "granted") {
    return "Grant motion permission to increase the quality of detections";
  } else {
    return "Grant permissions to start SDK";
  }
};
