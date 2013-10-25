package org.geodroid.server;

import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

import org.geodroid.app.GeoApplication;
import org.geodroid.server.GeodroidServer.Status;
import org.jeo.data.Registry;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

@SuppressLint("NewApi")
public class GeodroidServerActivity extends Activity implements NavFragment.Callbacks, 
    GeodroidServer.StatusCallback {

    final int TAG_STATUS = 10;
    
    /**
     * tablet vs handset mode
     */
    boolean mTwoPane;

    /** data registry */
    Registry reg;

    Button status;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_nav);
    
        if (findViewById(R.id.detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
    
            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((NavFragment) getFragmentManager().findFragmentById(
                    R.id.nav)).setActivateOnItemClick(true);
        }
    
        GeodroidServer app = app();
        reg = app.createDataRegistry();
        app.bind(this);

        // TODO: If exposing deep links into your app, handle intents here.
    }

    /**
     * callback to update details pane when navigation changed.
     */
    @Override
    public void onItemSelected(Page page) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            
            DetailFragment frag = page.newFragment();
            getFragmentManager().beginTransaction().replace(R.id.detail_container, frag).commit();
    
        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, DetailActivity.class);
            detailIntent.putExtra(DetailActivity.ARG_PAGE, page);
            startActivity(detailIntent);
        }
    }

    @Override
    public void onStatusUpdate(final Status s) {
        if (status != null) {
            status.post(new Runnable() {
                @Override
                public void run() {
                    //status.setSelection(s.ordinal());
                    status.setTag(R.id.status_tag, s);
                    status.setText(s.name());
                    status.setTextColor(getResources().getColor(R.color.text_light));
                    status.setEnabled(true);
                    
                    switch(s) {
                    case ONLINE:
                        status.setBackgroundResource(R.drawable.status_btn_on);
                        break;
                    case OFFLINE:
                        status.setBackgroundResource(R.drawable.status_btn_off);
                        break;
                    case ERROR:
                        status.setBackgroundResource(R.drawable.status_btn_err);
                        break;
                    default:
                        status.setEnabled(false);
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        
        MenuItem item = menu.findItem(R.id.menu_status);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        status = new Button(this);
        status.setEnabled(false);
        status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);

                Status s = (Status) v.getTag(R.id.status_tag);
                if (s != null) {
                    switch(s) {
                    case ONLINE:
                    case ERROR:
                        app().stop();
                        break;
                    case OFFLINE:
                        app().start();
                    }
                }
            }
        });

//        status = (Spinner) getLayoutInflater().inflate(R.layout.status_spinner, null);
//        status.setAdapter(new StatusSpinnerAdapter(this));
//        status.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                GeodroidServer app = app();
//
//                Status s = (Status) status.getAdapter().getItem(position);
//                switch(s) {
//                case OFFLINE:
//                    app.stop();
//                    break;
//                case ONLINE:
//                    app.start();
//                    break;
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                // TODO Auto-generated method stub
//                
//            }
//        
//        });
        
        item.setActionView(status);

        return true;
    }

    public Registry getDataRegistry() {
        return reg;
    }

    @Override
    protected void onDestroy() {
        reg.close();
        GeodroidServer.get(this).unbind(this);
    }

    GeodroidServer app() {
        return GeodroidServer.get(this);
    }

//    @SuppressWarnings("unchecked")
//    @Override
//    protected void onCreate(Bundle state) {
//        super.onCreate(state);
//        setContentView(R.layout.main);
//
//        final Switch s = (Switch) findViewById(R.id.onoff_switch);
//        s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                new AsyncTask() {
//
//                    protected void onPreExecute() {
//                        s.setEnabled(false);
//                    };
//
//                    @Override
//                    protected Object doInBackground(Object... params) {
//
//                        Boolean start = (Boolean) params[0];
//                        if (start != isServerOnline()) {
//                            if (start) {
//                                new Start().onReceive(getApplicationContext(), null);
//                            }
//                            else {
//                                new Stop().onReceive(getApplicationContext(), null);
//                            }
//                        }
//
//                        return null;
//                    }
//
//                    protected void onPostExecute(Object result) {
//                        s.setEnabled(true);
//                    };
//                    
//                }.execute(isChecked);
//            }
//        });
//        
//
//        final TextView t = (TextView) findViewById(R.id.hello);
//        final Handler h = new Handler();
//
//        Timer timer = new Timer();
//        TimerTask task = new TimerTask() {
//            @Override
//            public void run() {
//                h.post(new Runnable() {
//                    public void run() {
//                        new AsyncTask() {
//                            @Override
//                            protected Object doInBackground(Object... params) {
//                                return isServerOnline();
//                            }
//                            protected void onPostExecute(Object result) {
//                                t.setText(((Boolean)result) ? R.string.online : R.string.offline);
//                            }
//                        }.execute();
//                    }
//                });
//            }
//        };
//        timer.schedule(task, 0, 5000);
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
//
//    boolean isServerOnline() {
//        try {
//            URL u = new URL("http://localhost:8000");
//            URLConnection cx = u.openConnection();
//            cx.setConnectTimeout(3000);
//            cx.connect();
//            return true;
//        }
//        catch(Exception e) {
//            return false;
//        }
//    }
    
}
