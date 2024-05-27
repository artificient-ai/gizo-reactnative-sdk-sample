import React, { useEffect, useState } from 'react';
import { Alert, Button, NativeModules, PermissionsAndroid, Platform, SafeAreaView, ScrollView, StatusBar, StyleSheet, useColorScheme, View } from 'react-native';
import { Colors, Header } from 'react-native/Libraries/NewAppScreen';

const { GizoAndroidSDKModule } = NativeModules;

function App(): React.JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';
  const [permissionsGranted, setPermissionsGranted] = useState(false);

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  useEffect(() => {
    if (Platform.OS === 'android') {
      checkUsageStatsPermission();
    }
  }, []);

  const checkAndRequestPermissions = async (permissionStrings: string[]) => {
    const permissions = permissionStrings
      .map(permission => PermissionsAndroid.PERMISSIONS[permission.replace('android.permission.', '')])
      .filter(permission => permission !== undefined);

    console.log('Requesting permissions:', permissions);

    try {
      const grantedPermissions = await PermissionsAndroid.requestMultiple(permissions);
      const allPermissionsGranted = permissions.every(permission => grantedPermissions[permission] === PermissionsAndroid.RESULTS.GRANTED);

      if (allPermissionsGranted) {
        Alert.alert("All permissions granted");
        setPermissionsGranted(true);
      } else {
        Alert.alert("Some permissions are not granted");
        setPermissionsGranted(false);
      }
    } catch (err) {
      console.warn(err);
    }
  };

  const checkUsageStatsPermission = () => {
    GizoAndroidSDKModule.checkUsageStatsPermission((granted: boolean) => {
      if (!granted) {
        Alert.alert(
          'Permission Required',
          'This app requires access to usage stats. Please enable it in the settings.',
          [
            { text: 'Cancel', style: 'cancel' },
            { text: 'Open Settings', onPress: () => GizoAndroidSDKModule.requestUsageStatsPermission() },
          ],
          { cancelable: false }
        );
      } else {
        GizoAndroidSDKModule.neededPermissions()
          .then((permissions: string[]) => {
            checkAndRequestPermissions(permissions);
          })
          .catch((error: any) => {
            console.error(error);
          });
      }
    });
  };

  const handleDriveNowClick = () => {
    if (Platform.OS === 'android') {
      if (permissionsGranted) {
        GizoAndroidSDKModule.startDriveNowActivity();
      } else {
        checkUsageStatsPermission();
      }
    } else if (Platform.OS === 'ios') {
      // Handle iOS case
    }
  };

  const handleDriveNowNoCameraClick = () => {
    if (Platform.OS === 'android') {
      if (permissionsGranted) {
        GizoAndroidSDKModule.startNoCameraActivity();
      } else {
        checkUsageStatsPermission();
      }
    } else if (Platform.OS === 'ios') {
      // Handle iOS case
    }
  };

  return (
    <SafeAreaView style={backgroundStyle}>
      <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />
      <ScrollView contentInsetAdjustmentBehavior="automatic">
        <Header />
        <View>
          <View style={styles.sectionContainer}>
            <Button title="Drive Now" onPress={handleDriveNowClick} />
          </View>
          <View style={styles.sectionContainer}>
            <Button title="Drive Now Without Camera" onPress={handleDriveNowNoCameraClick} />
          </View>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: '600',
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '400',
  },
  highlight: {
    fontWeight: '700',
  },
});

export default App;
