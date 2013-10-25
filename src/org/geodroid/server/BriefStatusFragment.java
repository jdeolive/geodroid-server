package org.geodroid.server;

import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class BriefStatusFragment extends Fragment {

    static enum Ping {
        ON, OFF, ERR
    }

    ScheduledExecutorService executor;

    Ping status;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        executor = Executors.newScheduledThreadPool(1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        View v = inflater.inflate(R.layout.breif_status, container, true);
        final Button btn = (Button) v.findViewById(R.id.status_onoff_btn);
//        Spinner btn = (Spinner) v.findViewById(R.id.status_onoff_btn);
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
//            getActivity(), R.array.onoff, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        btn.setAdapter(adapter);
        // handler to turn server on / off
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                if (status == null) {
                    // do nothing, not initialized yet
                    return;
                }

                Activity a = getActivity();
                switch(status) {
                case OFF:
                    // turn it on
                    a.startService(new Intent(a, GeoDroidService.class));
                    break;
                case ERR:
                case ON:
                    // turn it off
                    a.stopService(new Intent(a, GeoDroidService.class));
                    break;
                }

                //clear status
                status = null;

                btn.setText("...");
            }
        });

        // background job that pings the server and updates state 
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                final Ping ping = pingServer();
                btn.post(new Runnable() {
                  @Override
                  public void run() {
                      status = ping; 
                      switch(status) {
                      case ON:
                          btn.setText("ON");
                          break;
                      case OFF:
                          btn.setText("OFF");
                          break;
                      case ERR:
                          btn.setText("ERR");
                          break;
                      }
                  }
                });
            }
        }, 0, 4, TimeUnit.SECONDS);

        return v;
    }

    @Override
    public void onDestroyView() {
        executor.shutdown();
        super.onDestroyView();
    }

    Ping pingServer() {
        boolean srvRunning = false;

        ActivityManager actMgr = 
            (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo srvInfo : actMgr.getRunningServices(Integer.MAX_VALUE)) {
            if (GeoDroidService.class.getName().equals(srvInfo.service.getClassName())) {
                srvRunning = true;
            }
        }

        if (srvRunning) {
            // try to connect
            Preferences pref = new Preferences(getActivity());

            StringBuilder sb = new StringBuilder("http://localhost:");
            sb.append(pref.getPort());
            sb.append("/ping");

            try {
                URL u = new URL(sb.toString());
                URLConnection cx = u.openConnection();
                cx.setConnectTimeout(2000);
                cx.connect();
                return Ping.ON;
            }
            catch(Exception e) {
                // service started but http server not running
                return Ping.ERR;
            }
        }
        else {
            return Ping.OFF;
        }
      
    }
}
