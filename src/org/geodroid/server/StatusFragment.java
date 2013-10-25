package org.geodroid.server;

import java.io.File;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class StatusFragment extends DetailFragment {

    @Override
    protected void doCreateView(
        LayoutInflater inflater, ViewGroup container, Preferences p, Bundle state) {
        
        View v = inflater.inflate(R.layout.status, container);

        GeodroidServer app = getApp();

        //.setText(String.valueOf(p.getPort()));
        TextView status = (TextView)v.findViewById(R.id.status_status_detail);
        switch(app.getStatus()) {
        case ONLINE:
            Resources res = getResources();
            status.setText(String.format(res.getString(R.string.status_online), app.getPort()));
            break;
        case OFFLINE:
            status.setText(R.string.status_offline);
            break;
        case ERROR:
            status.setText(R.string.status_error);
        }

        setDir(R.id.status_dataDir, v, p.getDataDirectory());
        setDir(R.id.status_wwwDir, v, p.getWWWDirectory());
        setDir(R.id.status_appsDir, v, p.getAppsDirectory());
    }

    void setDir(int id, View v, final File dir) {
        String path = dir.getAbsolutePath();
        
        /*File sdcard = Environment.getExternalStorageDirectory();
        if (path.startsWith(sdcard.getAbsolutePath())) {
            path = path.replaceFirst(sdcard.getAbsolutePath(), "SDCard");
        }*/

        TextView text = (TextView) v.findViewById(id);
        text.setText(path);
        //TODO: there seems to be no portable way to open the file browser on android
        /*text.setClickable(true);
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromFile(dir));
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                startActivity(intent);
            }
        });*/
    }

}
