import * as GizoSdk from "@gizo-sdk/core";
import React, { useEffect, useState } from "react";
import { View, Text, Button, Platform } from "react-native";
import { openSettings } from "react-native-permissions";
import AsyncStorage from '@react-native-async-storage/async-storage';

import Timer from "./timer";
import {
  checkPermissions,
  getLocationStatus,
  getMotionStatus,
  permissionLocationRequest,
  permissionMicrophoneRequest,
  permissionMotionRequest,
  permissionPhoneStateRequest,
  permissionNotificationRequest,
  permissionText,
  requestUsageAccess,
  requestAlarmScheduler,
  requestBatteryOptimization,
} from "../helpers/permissions";


export default function Dashboard() {
  const [responsePermission, setResponsePermission] = useState<any>({});
  const [loging, setLoging] = useState<boolean>(false);
  const [isRunning, setIsRunning] = useState<boolean>(false);
  const [userId, setUserId] = useState<number>(0);
  const clientId = "";
  const clientSecret = "";

  useEffect(() => {
    async function checkAllPermission() {
      const response = await checkPermissions();
      setResponsePermission(response);
      console.log(response);
    }
    checkAllPermission();
  }, []);

  useEffect(() => {
    GizoSdk.onUpdateLocationListener((event) => {
      const { latitude, longitude, timestamp } = event.location;
      console.log(
        `Latitude: ${latitude}, Longitude: ${longitude}, Timestamp: ${timestamp}`,
      );
    });

    GizoSdk.onStartRecordingListener(() => {
      console.log("Recording started");
      setIsRunning(true);
    });

    GizoSdk.onStopRecordingListener(() => {
      console.log("Recording stoped");
      setIsRunning(false);
    });

    GizoSdk.onStartUploadTrip((event) => {
      console.log(`onStartUploadTrip ${event.tripId}`);
    });

    GizoSdk.onCompleteUploadTrip((event) => {
      console.log(`onCompleteUploadTrip ${event.tripId}`);
    });
    
    init();
  }, []);

  const openPermissions = async () => {
    if (Platform.OS === "ios") {
      if (!getLocationStatus(responsePermission)) {
        await permissionLocationRequest();
        permissionMotionRequest();
      } else {
        openSettings();
      }
    } else {
      console.log("openPermissions");
      await permissionPhoneStateRequest();
      await permissionMicrophoneRequest();
      await permissionMotionRequest();
      // await permissionCameraRequest();
      await permissionNotificationRequest();
      await permissionLocationRequest();
    }
  };

  const init = async () => {
    console.log("init");
    setIsRunning(true);
    GizoSdk.changeUploadSetting({
      isMobileDataEnabled: true,
      isRoamingDataEnabled: true,
      romingDataUsageLimit: 100,
      mobileDataUsageLimit: 100,
    });

    GizoSdk.setToken(clientId, clientSecret);

    try {
        const key = 'user-id'
        var userIdValue = await AsyncStorage.getItem(key);
        if(userIdValue==null)
        {
          const userId = await GizoSdk.createUser();
          if (userId != null) {
            console.log(userId);
            setUserId(userId);
            await authenticateUser(userId)
            await AsyncStorage.setItem(key, userId.toString());
          }
        }
        else{
          var userid = Number(userIdValue);
          setUserId(userid);
          await authenticateUser(userid)
        }
    } catch (e) {
      // saving error
    } 
  };

  const authenticateUser =async (userId:number)  => {
    try {
      // Instructions on setting a userId
      // https://artificient-ai.gitbook.io/gizo-react-native-sdk-documentation/sdk-documentation/usage/authentication-and-user-management
      const authenticated = await GizoSdk.setUserId(userId);
      if (authenticated) {
        console.log(authenticated);
        setLoging(authenticated);
      }
    } catch {
      console.log(
        "Could not authenticate the user. Set a valid userId. For furthur information visit the GIZO SDK Documentation.",
      );
    }
  }

  const startRecording = async () => {
    setIsRunning(true);
    GizoSdk.startRecording({
      mode: "NoCamera",
      stopRecordingSetting: {
        stopRecordingOnLowBatteryLevel: 15,
      },
    });
  };

  const stopRecording = async () => {
    GizoSdk.stopRecording();
    setIsRunning(false);
  };

  return (
    <View>
      <Timer 
        isRunning={isRunning} 
        startTrip={new Date()} 
      />
      <View style={{ paddingBottom: 20 }} />
      <Text>UserId: {userId}</Text>
      <Text>Authenticated: {loging ? "true" : "false"} </Text>
      <Text>
        {permissionText(
          getLocationStatus(responsePermission),
          getMotionStatus(responsePermission),
        )}
      </Text>
      <Button onPress={openPermissions} title="Permission" />
      {Platform.OS === "android" && (
        <>
          <Button onPress={requestUsageAccess} title="Usage Access Settings" />
          {Platform.Version >= 31 && (
            <>
              <Button
                onPress={requestAlarmScheduler}
                title="Alarm Scheduler Settings"
              />
            </>
          )}
          <Button
            onPress={requestBatteryOptimization}
            title="Battery Optimization Settings"
          />
        </>
      )}

      <Button onPress={startRecording} title="StartRecording" />
      <Button onPress={stopRecording} title="StopRecording" />
    </View>
  );
}
