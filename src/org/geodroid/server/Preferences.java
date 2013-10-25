package org.geodroid.server;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;

public class Preferences {

    static String PORT = "port"; 

    static String DATA_DIR = "dataDirectory";

    static String APPS_DIR = "appsDirectory";

    static String WWW_DIR = "wwwDirectory";

    SharedPreferences pref;

    public Preferences(Context context) {
        pref = context.getSharedPreferences("geodroid", 0);
    }

    public int getPort() {
        return pref.getInt(PORT, 8000);
    }

    public boolean setPort(int port) {
        Editor e = pref.edit();
        e.putInt(PORT, port);
        return e.commit();
    }

    public File getDataDirectory() {
        return getFile(DATA_DIR, 
            new File(Environment.getExternalStorageDirectory().getPath(), "GeoData"));
    }

    public boolean setDataDirectory(File dir) {
        return putFile(DATA_DIR, dir);
    }

    public File getAppsDirectory() {
        return getFile(APPS_DIR, new File(getWWWDirectory(), "apps"));
    }

    public boolean setAppsDirectory(File dir) {
        return putFile(APPS_DIR, dir);
    }

    public File getWWWDirectory() {
        return getFile(WWW_DIR, 
            new File(Environment.getExternalStorageDirectory().getPath(), "GeoDroid"));
    }

    public boolean setWWWDirectory(File dir) {
        return putFile(WWW_DIR, dir);
    }

    File getFile(String key, File def) {
        return pref.contains(key) ? new File(pref.getString(key, null)) : def;
    }

    boolean putFile(String key, File file) {
        Editor e = pref.edit();
        e.putString(key, file.getAbsolutePath());
        return e.commit();
    }
}
