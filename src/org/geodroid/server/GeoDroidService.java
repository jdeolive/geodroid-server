package org.geodroid.server;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.jeo.android.GeoDataRegistry;
import org.jeo.android.graphics.Renderer;
import org.jeo.data.CachedRegistry;
import org.jeo.data.Registry;
import org.jeo.map.Map;
import org.jeo.map.View;
import org.jeo.nano.AppsHandler;
import org.jeo.nano.FeatureHandler;
import org.jeo.nano.MapHandler;
import org.jeo.nano.MapRenderer;
import org.jeo.nano.NanoServer;
import org.jeo.nano.TileHandler;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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

    static final String TAG = "GeoDroidServer";

    Registry reg;
    NanoServer server;

    @Override
    public void onCreate() {
        File wwwRoot = new File(Environment.getExternalStorageDirectory(), "GeoDroid");
        if (!wwwRoot.exists()) {
            try {
                if (wwwRoot.mkdir()) {
                    throw new IOException("unable to create directory" + wwwRoot.getPath());
                }
            }
            catch(IOException e) {
                Log.w(TAG, "Unable to create www root directory", e);
            }
        }
        LocationManager locMgr = 
            (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

        try {
            reg = new CachedRegistry(new GeoDataRegistry());
            server = new NanoServer(8000, wwwRoot, reg, new CurrentLocationHandler(locMgr), 
                new TileHandler(), new FeatureHandler(), new MapHandler(new MapRenderer() {
                    @Override
                    public void render(Map map, OutputStream out) throws IOException {
                        View view = map.getView();
                        final Bitmap img = 
                            Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
                            
                        Renderer r = new Renderer(new Canvas(img));
                        r.init(view);
                        r.render();

                        img.compress(Bitmap.CompressFormat.PNG, 90, out);
                    }
                }), new AppsHandler());
        }
        catch(IOException e) {
            Log.wtf(TAG, "NanoHTTPD did not start", e);
        }

        Log.i(TAG, "GeoDroid Server started");
        notifyStarted();
    }

    void notifyStarted() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://localhost:8000/"));
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

        Log.i(TAG, "GeoDroid Server stopped");
        notifyStopped();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

}
