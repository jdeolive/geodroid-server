package org.geodroid.server;

import static org.geodroid.server.GeodroidServer.TAG;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class LogsPage extends PageFragment implements OnItemSelectedListener {

    static List<String> TAGS = Arrays.asList("GeodroidServer", "org.jeo.nano.NanoServer");
    
    View progress;

    @Override
    protected void doCreateView(
        LayoutInflater inflater, ViewGroup container, Preferences p, Bundle state) {

        View v = inflater.inflate(R.layout.page_logs, container);

        progress = v.findViewById(R.id.logs_progress);
        progress.setVisibility(View.INVISIBLE);

        Spinner levSpinner = (Spinner) v.findViewById(R.id.logs_log_level);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), 
            R.array.log_levels, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        levSpinner.setAdapter(adapter);
        levSpinner.setOnItemSelectedListener(this);

        v.findViewById(R.id.logs_log_contents).setFocusable(false);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner levSpinner = (Spinner) parent;
        new LogLoader().execute(levSpinner.getSelectedItem());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    class LogLoader extends AsyncTask<Object, Void, String> {

        

        @Override
        protected void onPreExecute() {
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Object... args) {
            char level = args[0].toString().charAt(0);

            StringBuilder buf = new StringBuilder();
            try {
                Process p = Runtime.getRuntime().exec(String.format(
                    "logcat -s -d GeodroidServer:%s org.jeo.nano.NanoServer:%s", level, level));
                
                BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

                String line = "";
                while ((line = in.readLine()) != null) {
                  buf.append(line).append("\n");
                }

                return buf.toString();
            } catch (IOException e) {
                Log.wtf(TAG, "error reading logs", e);
                return null;
            }
        }
    
        @Override
        protected void onPostExecute(String result) {
            TextView text = (TextView) getView().findViewById(R.id.logs_log_contents);
            text.setText(result);

            progress.setVisibility(View.INVISIBLE);
        }
    }
}
