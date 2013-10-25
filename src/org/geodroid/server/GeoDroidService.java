package org.geodroid.server;

import static org.geodroid.server.GeodroidServer.TAG;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.geodroid.app.GeoApplication;
import org.jeo.android.graphics.Renderer;
import org.jeo.data.Registry;
import org.jeo.map.Map;
import org.jeo.map.View;
import org.jeo.nano.AppsHandler;
import org.jeo.nano.FeatureHandler;
import org.jeo.nano.Handler;
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
import android.os.IBinder;
//import android.support.v4.app.NotificationCompat;
//import android.support.v4.app.TaskStackBuilder;
//import android.support.v4.app.NotificationCompat;
//import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class GeoDroidService extends Service {

    Registry reg;
    NanoServer server;

    @Override
    public void onCreate() {
        Preferences p = new Preferences(this);

        File appsDir = initDir(p.getAppsDirectory());
        File wwwDir = initDir(p.getWWWDirectory());

        LocationManager locMgr = 
            (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

        List<Handler> handlers = new ArrayList<Handler>();
        handlers.add(new CurrentLocationHandler(locMgr));
        handlers.add(new TileHandler());
        handlers.add(new FeatureHandler());
        handlers.add(new MapHandler(new MapRenderer() {
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
        }));

        handlers.add(new AppsHandler(appsDir));

        try {
            reg = GeoApplication.get(this).createDataRegistry();
            server = new NanoServer(p.getPort(), wwwDir , reg, handlers);
        }
        catch(IOException e) {
            Log.wtf(TAG, "NanoHTTPD did not start", e);
        }

        Log.i(TAG, "GeoDroid Server started");
        notifyStarted();
    }

    File initDir(File dir) {
        if (!dir.exists()) {
            try {
                if (dir.mkdir()) {
                    throw new IOException("unable to create directory" + dir.getPath());
                }
            }
            catch(IOException e) {
                Log.w(TAG, "Unable to create directory", e);
            }
        }
        return dir;
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
