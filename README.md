#SensorWear

This app is alarm system, which made around android wear device and android
mobile handheld. 

I think it's a great feature for my useless android wear
device, isn't it?

## Running it from Android Studio

#### Clone the repository

`git clone https://github.com/bevkoski/react-native-android-wear-demo.git`

`cd SensorWear`

#### Install dependencies

`yarn` or `npm install`

#### Start the packager

`react-native start`

#### Open the project in Android Studio

1. Start Android Studio
2. Choose "Open an existing Android Studio project"
3. Select the `/SensorWear/android` folder

#### Run the mobile app

1. Connect your Android phone via USB
2. Select the `app` module as a run configuration
3. Run the `app` module
4. Select your phone from the available connected devices

If you get one of the following error messages:

*Could not connect to development server.*

*Could not get BatchedBridge, make sure your bundle is packaged properly.*

Try executing `adb reverse tcp:8081 tcp:8081` from the command line and reloading the app.

#### Run the watch app

1. Connect your Android watch via USB
2. Select the `sensor` module as a run configuration
3. Run the `sensor` module
4. Select your watch from the available connected devices
