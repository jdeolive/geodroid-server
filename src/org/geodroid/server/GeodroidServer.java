package org.geodroid.server;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.geodroid.app.GeoApplication;
import org.geodroid.app.GeoDataRegistry;
import org.jeo.data.Registry;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;

public class GeodroidServer extends GeoApplication {

    public static enum Status {
        ONLINE, OFFLINE, ERROR, UNKNOWN;
    }

    public interface StatusCallback {
        
        void onStatusUpdate(Status status);
    }

    public static GeodroidServer get(Activity activity) {
        return (GeodroidServer) GeoApplication.get(activity);
    }

    public static BuildInfo getBuildInfo(Context context) {
        return new BuildInfo(context.getResources().openRawResource(R.raw.version), context);
    }
    
    static final String TAG = "GeodroidServer";

    Status status;
    Exception error;
    List<StatusCallback> callbacks;

    ScheduledExecutorService executor; 

    public GeodroidServer() {
        status = Status.UNKNOWN;
        callbacks = new ArrayList<StatusCallback>();

        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                status = ping();
                dispatch(status);
            }
        }, 0, 4, TimeUnit.SECONDS);

    }

    
    public Exception getError() {
        return error;
    }

    public void setError(Exception error) {
        this.error = error;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public Registry createDataRegistry() {
        Preferences p = new Preferences(this);
        File dataDir = p.getDataDirectory();
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        return new GeoDataRegistry(dataDir, null);
    }

    public void start() {
        startService(new Intent(this, GeodroidServerService.class));
    }

    public void stop() {
        stopService(new Intent(this, GeodroidServerService.class));
    }

    public void bind(StatusCallback callback) {
        callbacks.add(callback);
    }

    public void unbind(StatusCallback callback) {
        callbacks.remove(callback);
    }

    public int getPort() {
        return new Preferences(this).getPort();
    }

    Status ping() {
        boolean srvRunning = false;

        ActivityManager actMgr = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo srvInfo : actMgr.getRunningServices(Integer.MAX_VALUE)) {
            if (GeodroidServerService.class.getName().equals(srvInfo.service.getClassName())) {
                srvRunning = true;
            }
        }

        if (srvRunning) {
            // try to connect
            

            StringBuilder sb = new StringBuilder("http://localhost:");
            sb.append(getPort());
            sb.append("/ping");

            try {
                URL u = new URL(sb.toString());
                URLConnection cx = u.openConnection();
                cx.setConnectTimeout(2000);
                cx.connect();
                return Status.ONLINE;
            }
            catch(Exception e) {
                // service started but http server not running
                return Status.ERROR;
            }
        }
        else {
            return Status.OFFLINE;
        }
    }

    void dispatch(Status status) {
        for (StatusCallback callback : callbacks) {
            callback.onStatusUpdate(status);
        }
    }
}
