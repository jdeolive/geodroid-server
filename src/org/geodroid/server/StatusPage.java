package org.geodroid.server;

import java.io.File;
import java.util.ArrayDeque;

import org.jeo.util.Pair;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.AndroidRuntimeException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class StatusPage extends PageFragment {

    static final int BYTES_PER_MB = 1048576;
    
    View progress;

    @Override
    protected void doCreateView(
        LayoutInflater inflater, ViewGroup container, Preferences p, Bundle state) {
        
        View v = inflater.inflate(R.layout.page_status, container);

        progress = v.findViewById(R.id.status_storage_progress);

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

        File dataDir = p.getDataDirectory();
        File wwwDir = p.getWebDirectory();

        setDir(R.id.status_dataDir, v, dataDir);
        setDir(R.id.status_wwwDir, v, wwwDir);

        new DirSize().execute(Pair.of(dataDir, R.id.status_dataDirSize), 
            Pair.of(wwwDir, R.id.status_wwwDirSize));
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

    class DirSize extends AsyncTask<Pair<File,Integer>, Void, Exception> {

        @Override
        protected void onPreExecute() {
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Exception doInBackground(Pair<File,Integer>... args) {
            try {
                for (Pair<File, Integer> p : args){
                    calc(p.first(), p.second());
                }
                return null;
            }
            catch(Exception e) {
                return e;
            }
        }
    
        void calc(File root, Integer view) {
            ArrayDeque<File> stack = new ArrayDeque<File>();
            stack.push(root);

            long size = 0;
            while(!stack.isEmpty()) {
                File f = stack.pop();
                if (!f.exists()) {
                    continue;
                }
                if (f.isFile()) {
                    size += f.length();
                }
                else {
                    File[] list = f.listFiles();
                    if (list != null ) {
                        for (File g : list) {
                            stack.push(g);
                        }
                    }
                }
            }

            final Long mb = size / BYTES_PER_MB;
            final TextView text = (TextView) getView().findViewById(view);
            if (text != null) {
                text.post(new Runnable() {
                    @Override
                    public void run() {
                        text.setText(String.format("%d MB", mb));
                    }
                });
            }
        }

        @Override
        protected void onPostExecute(Exception result) {
            progress.setVisibility(View.INVISIBLE);

            if (result != null) {
                ErrorDialog.show(result, getActivity());
            }
        }

    }
}
