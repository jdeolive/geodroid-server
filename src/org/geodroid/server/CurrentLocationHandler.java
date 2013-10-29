package org.geodroid.server;

import static org.geodroid.server.GeodroidServer.TAG;
import static org.jeo.nano.NanoHTTPD.HTTP_OK;
import static org.jeo.nano.NanoHTTPD.MIME_PLAINTEXT;

import org.jeo.geojson.GeoJSONWriter;
import org.jeo.geom.Geom;
import org.jeo.nano.Handler;
import org.jeo.nano.NanoHTTPD.Response;
import org.jeo.nano.NanoServer;
import org.jeo.nano.Request;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import com.vividsolutions.jts.geom.Point;

public class CurrentLocationHandler extends Handler {

    static final String HTTP_UNAVAIABLE = "503 Service Unavailable";
    
    static final String MIME_JSON = "application/json";

    LocationManager locMgr;

    public CurrentLocationHandler(LocationManager locMgr) {
        this.locMgr = locMgr;
    }

    @Override
    public boolean canHandle(Request request, NanoServer server) {
        return "/location".equalsIgnoreCase(request.getUri());
    }
    
    @Override
    public Response handle(Request request, NanoServer server) {
        LocationProvider locProvider = lookupLocationProvider(locMgr);
        
        if (locProvider == null) {
            Log.w(TAG, "Location manager not available");
            return null;
        }

        Listener l = new Listener();

        Looper.prepare();
        locMgr.requestSingleUpdate(locProvider.getName(), l, Looper.myLooper());

        Looper.loop();

        Location loc = l.getLocation();
        if (loc == null) {
            return new Response(HTTP_UNAVAIABLE, MIME_PLAINTEXT, "Location unavailable");
        }
        
        Point p = Geom.point(loc.getLongitude(), loc.getLatitude());
        return new Response(HTTP_OK, MIME_JSON, GeoJSONWriter.toString(p));
    }

    static class Listener implements LocationListener {

        Location location;
    
        @Override
        public void onLocationChanged(Location location) {
            this.location = location;
            Looper.myLooper().quit();
        }
    
        public Location getLocation() {
            return location;
        }
    
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    
        @Override
        public void onProviderEnabled(String provider) {
        }
    
        @Override
        public void onProviderDisabled(String provider) {
        }
    }

    LocationProvider lookupLocationProvider(LocationManager locMgr) {
        if (locMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return locMgr.getProvider(LocationManager.GPS_PROVIDER);
        }
        else if (locMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return locMgr.getProvider(LocationManager.NETWORK_PROVIDER);
        }
        return null;
    }
}
