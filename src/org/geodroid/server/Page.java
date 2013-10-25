package org.geodroid.server;

import android.util.AndroidRuntimeException;

public enum Page {
    STATUS(StatusFragment.class, R.string.status), 
    //DATA(DataFragment.class), 
    LAYERS(LayersFragment.class, R.string.layers)
    //APPS(AppsFragment.class), 
    //SETTINGS(SettingsFragment.class), 
    //LOGS(LogsFragment.class);
    ;
    private final Class<? extends DetailFragment> fragment;
    private final int title;
    
    private Page(Class<? extends DetailFragment> fragment, int title) {
        this.fragment = fragment;
        this.title = title;
    }

    <T extends DetailFragment> T newFragment() {
        try {
            return (T) fragment.newInstance();
        } 
        catch(Exception e) {
            throw new AndroidRuntimeException(e);
        }
    }

    int title() {
        return title;
    }
}
