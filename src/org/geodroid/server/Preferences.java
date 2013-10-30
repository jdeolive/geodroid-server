package org.geodroid.server;

import java.io.File;

import org.jeo.nano.NanoServer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.preference.PreferenceManager;

public class Preferences {

    static String PORT = "pref_port"; 

    static String DATA_DIR = "pref_data_dir";

    static String APPS_DIR = "pref_apps_dir";

    static String WWW_DIR = "pref_www_dir";

    static String NUM_THREADS = "pref_num_threads";

    SharedPreferences pref;

    public Preferences(Context context) {
        pref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public int getPort() {
        return getInt(PORT, 8000);
    }


    public int getNumThreads() {
        return getInt(NUM_THREADS, NanoServer.DEFAULT_NUM_THREADS);
    }

    public File getDataDirectory() {
        return getFile(DATA_DIR, 
            new File(Environment.getExternalStorageDirectory().getPath(), "Geodata"));
    }

    public File getAppsDirectory() {
        return getFile(APPS_DIR, new File(getWWWDirectory(), "apps"));
    }

    public File getWWWDirectory() {
        return getFile(WWW_DIR, 
            new File(Environment.getExternalStorageDirectory().getPath(), "Geodroid"));
    }

    File getFile(String key, File def) {
        return pref.contains(key) ? new File(pref.getString(key, null)) : def;
    }

    boolean putFile(String key, File file) {
        Editor e = pref.edit();
        e.putString(key, file.getAbsolutePath());
        return e.commit();
    }

    int getInt(String key, int def) {
        try {
            return Integer.parseInt(pref.getString(key, String.valueOf(def)));
        }
        catch(NumberFormatException e) {
            return def;
        }
    }
}
