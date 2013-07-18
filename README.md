# GeoDroid Server

Simple application providing an embedded HTTP server for serving up GeoPackage
and MBTiles packages through simple Feature and Tile services. 

## Building

Building the application from sources requires:

* [Android SDK](http://developer.android.com/sdk/index.html)
* [Apache Maven](http://maven.apache.org/)

See the [GeoDroid README](https://github.com/jdeolive/geodroid) for more 
information about setting up the Android SDK.

Start by doing a submodule update to bring in the core GeoDroid library.

    git submodule --init update

Navigate to the ``geodroid`` directory, update the project and build.

    cd geodroid
    android update project -p .
    mvn install

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




