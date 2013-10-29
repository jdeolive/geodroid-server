package org.geodroid.server;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingsPage extends PreferenceFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.page_settings, null, false);
        super.onCreateView(inflater, view, state);
        return view;
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        addPreferencesFromResource(R.xml.preferences);
    }

}
