package org.geodroid.srv;

import java.io.File;
import java.io.IOException;

import org.jeo.android.GeoDataRegistry;
import org.jeo.data.CachedRegistry;
import org.jeo.data.DirectoryRegistry;
import org.jeo.data.Registry;
import org.jeo.nano.FeatureHandler;
import org.jeo.nano.NanoJeoServer;
import org.jeo.nano.TileHandler;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
//import android.support.v4.app.NotificationCompat;
//import android.support.v4.app.TaskStackBuilder;
//import android.support.v4.app.NotificationCompat;
//import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class GeoDroidService extends Service {

    Registry reg;
    NanoJeoServer server;

    @Override
    public void onCreate() {
        File wwwRoot = new File(Environment.getExternalStorageDirectory(), "www");
        LocationManager locMgr = 
            (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

        try {
            reg = new CachedRegistry(new GeoDataRegistry());
            server = new NanoJeoServer(8000, wwwRoot, reg, new CurrentLocationHandler(locMgr), 
                new TileHandler(), new FeatureHandler());
        }
        catch(IOException e) {
            Log.wtf("NanoHTTPD did not start", e);
        }

        Log.i("service", "GeoDroid started");
        notifyStarted();
    }

    void notifyStarted() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://localhost:8000/www/"));
        PendingIntent pending = 
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Notification.Builder nBuilder = new Notification.Builder(this)
            .setContentTitle("GeoDroid").setContentText("Server online")
            .setSmallIcon(R.drawable.ic_notify).setContentIntent(pending);

        NotificationManager nMgr = 
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.notify(1, nBuilder.getNotification());
    }

    void notifyStopped() {
        Notification.Builder nBuilder = new Notification.Builder(this)
        .setContentTitle("GeoDroid").setContentText("Server offline")
        .setSmallIcon(R.drawable.ic_notify);
    
        NotificationManager nMgr = 
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.notify(2, nBuilder.getNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (server != null) {
            server.stop();
        }

        reg.close();

        Log.i("service", "GeoDroid stopped");
        notifyStopped();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

}
