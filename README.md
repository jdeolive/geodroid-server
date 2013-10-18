# GeoDroid Server

Simple application providing an embedded HTTP server for serving up GeoPackage
and MBTiles packages through simple Feature and Tile services. 

## Setting up the Android SDK

Building the library requires the 
[Android SDK](http://developer.android.com/sdk/index.html). 

After installing the SDK some additional packages must be installed through 
the Android SDK Manager. Run the ``android`` command to start the SDK manager
and install the following packages.

* Android SDK Tools
* Android SDK SDK Platform-tools
* Android SDK Build-tools

And finally install the appropriate API package. Currently GeoDroid is built
against "Android 4.0.3 (API 15)". Other APIs may work as well, but were not tested.

## Building

Building the application from sources requires:

* [Apache Maven](http://maven.apache.org/)

See the [GeoDroid README](https://github.com/jdeolive/geodroid/) for
more information about setting up the Android SDK and getting jeo.

### Build the GeoDroid library

Start by doing a submodule update to bring in the core GeoDroid library.

    git submodule update --init

Navigate to the ``geodroid`` directory, update the project and build.

    cd geodroid
    android update project -p .
    mvn install

*Note:* On OSX an error may occur during Maven execution that looks like:

    [ERROR] com.sun.tools.javac.Main is not on the classpath.
    [ERROR] Perhaps JAVA_HOME does not point to the JDK.

In that case try executing Maven with the ``tools.jar`` profile.

    mvn -P tools.jar install

### Build the GeoDroid Server app

Navigate back the root directory, update the project and build.

    cd ..
    android update project -p .
    mvn install

The above should result in a file named ``GeoDroidServer-debug.apk`` being 
created in the ``bin`` directory.

## Installing

Install the app on a connected device with the ``adb`` command.

    cd bin
    adb install GeoDroidServer-debug.apk

## Running

After installation, a GeoDroid application will be available on your device. When started, there will be a slider to turn GeoDroid on or off. When online, there will be a symbol in the notification bar. When tapped, a browser will open to serve data from `/sdcard/www/` on the device storage at `http://localhost:8000/www/`. A simple viewer application is available at https://github.com/ahocevar/geodroid-viewer/.


