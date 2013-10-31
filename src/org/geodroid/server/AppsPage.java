package org.geodroid.server;

import java.io.File;

import org.geodroid.server.LayersPage.Tag;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class AppsPage extends PageFragment {

    View progress;

    @Override
    protected void doCreateView(LayoutInflater inflater, ViewGroup container,
            Preferences p, Bundle state) {

        View view = inflater.inflate(R.layout.page_apps, container);

        progress = view.findViewById(R.id.apps_progress);
        progress.setVisibility(View.INVISIBLE);

        new LoadApps().execute(getPreferences().getAppsDirectory());
    }

    class LoadApps extends AsyncTask<File, Void, Exception> {

        LoadApps() {
        }

        @Override
        protected void onPreExecute() {
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Exception result) {
            progress.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Exception doInBackground(File... appRoots) {
            for (File appRoot : appRoots) {
                for (final File appDir : appRoot.listFiles()) {
                    if (!appDir.isDirectory()) {
                        continue;
                    }

                    getView().post(new Runnable() {
                        @Override
                        public void run() {
                            createTableRow(appDir);
                        }
                    });
                }
            }
            return null;
        }
    }

    void createTableRow(final File appDir) {
        ViewGroup table = (ViewGroup) getView().findViewById(R.id.apps_table);
        if (table == null) {
            return;
        }

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View row = inflater.inflate(R.layout.apps_table_row, null);

        TextView titleText = (TextView) row.findViewById(R.id.apps_table_title);
        titleText.setText(appDir.getName());

        TextView urlText = (TextView) row.findViewById(R.id.apps_table_url);
        urlText.setText("http://localhost:8000/apps/" + appDir.getName());
        urlText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Preferences p = new Preferences(getActivity());
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    String.format("http://localhost:%d/apps/%s/", p.getPort(), appDir.getName())));
                startActivity(intent);
            }
        });
        TextView dirText = (TextView) row.findViewById(R.id.apps_table_dir);
        dirText.setText(appDir.getAbsolutePath());

        row.requestLayout();
        table.addView(row);
    }
}
