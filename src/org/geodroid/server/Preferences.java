package org.geodroid.server;

import java.io.File;

import org.jeo.nano.NanoServer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Environment;
import android.preference.PreferenceManager;

/**
 * Helper class for accesing app preferences.
 * 
 * @author Justin Deoliveira, Boundless
 */
public class Preferences {

    SharedPreferences pref;
    Resources res;
    
    public Preferences(Context context) {
        pref = PreferenceManager.getDefaultSharedPreferences(context);
        res = context.getResources();
    }

    public int getPort() {
        return getInt(R.string.pref_port_key, 8000);
    }


    public int getNumThreads() {
        return getInt(R.string.pref_num_threads_key, NanoServer.DEFAULT_NUM_THREADS);
    }

    public File getDataDirectory() {
        return getFile(R.string.pref_data_dir_key, 
            new File(Environment.getExternalStorageDirectory().getPath(), "Geodata"));
    }

    public File getAppsDirectory() {
        return getFile(R.string.pref_apps_dir_key, new File(getWWWDirectory(), "apps"));
    }

    public File getWWWDirectory() {
        return getFile(R.string.pref_www_dir_key, 
            new File(Environment.getExternalStorageDirectory().getPath(), "Geodroid"));
    }

    File getFile(int k, File def) {
        String key = res.getString(k);
        return pref.contains(key) ? new File(pref.getString(key, null)) : def;
    }

    int getInt(int k, int def) {
        try {
            String key = res.getString(k);
            return Integer.parseInt(pref.getString(key, String.valueOf(def)));
        }
        catch(NumberFormatException e) {
            return def;
        }
    }
}
