package org.geodroid.srv;

import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.Menu;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

@SuppressLint("NewApi")
public class GeoDroidServer extends Activity {

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final Switch s = (Switch) findViewById(R.id.onoff_switch);
        s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                new AsyncTask() {

                    protected void onPreExecute() {
                        s.setEnabled(false);
                    };

                    @Override
                    protected Object doInBackground(Object... params) {

                        Boolean start = (Boolean) params[0];
                        if (start != isServerOnline()) {
                            if (start) {
                                new Start().onReceive(getApplicationContext(), null);
                            }
                            else {
                                new Stop().onReceive(getApplicationContext(), null);
                            }
                        }

                        return null;
                    }

                    protected void onPostExecute(Object result) {
                        s.setEnabled(true);
                    };
                    
                }.execute(isChecked);
            }
        });
        

        final TextView t = (TextView) findViewById(R.id.hello);
        final Handler h = new Handler();

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                h.post(new Runnable() {
                    public void run() {
                        new AsyncTask() {
                            @Override
                            protected Object doInBackground(Object... params) {
                                return isServerOnline();
                            }
                            protected void onPostExecute(Object result) {
                                t.setText(((Boolean)result) ? R.string.online : R.string.offline);
                            }
                        }.execute();
                    }
                });
            }
        };
        timer.schedule(task, 0, 5000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    boolean isServerOnline() {
        try {
            URL u = new URL("http://localhost:8000");
            URLConnection cx = u.openConnection();
            cx.setConnectTimeout(3000);
            cx.connect();
            return true;
        }
        catch(Exception e) {
            return false;
        }
    }
}
