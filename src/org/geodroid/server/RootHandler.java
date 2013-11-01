package org.geodroid.server;

import static org.jeo.nano.NanoHTTPD.*;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jeo.nano.Handler;
import org.jeo.nano.NanoHTTPD.Response;
import org.jeo.nano.NanoHTTPD;
import org.jeo.nano.NanoServer;
import org.jeo.nano.Request;
import org.jeo.util.Pair;

import android.content.Context;
import android.content.res.Resources;

public class RootHandler extends Handler {

    static Map<String,Pair<Integer,String>> RESOURCES = 
            new LinkedHashMap<String, Pair<Integer,String>>();
    static {
        RESOURCES.put("/", Pair.of(R.raw.index, MIME_HTML));
        RESOURCES.put("/index.html", Pair.of(R.raw.index, MIME_HTML));
        RESOURCES.put("/bootstrap.min.css", Pair.of(R.raw.bootstrap, MIME_CSS));
        RESOURCES.put("/logo.png", Pair.of(R.raw.logo, MIME_PNG));
    }

    Resources res;

    public RootHandler(Context context) {
        res = context.getResources();
    }

    @Override
    public boolean canHandle(Request req, NanoServer srv) {
        return RESOURCES.containsKey(req.getUri());
    }

    @Override
    public Response handle(Request req, NanoServer srv) throws Exception {
        Pair<Integer,String> r = RESOURCES.get(req.getUri());
        if (r == null) {
            return new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, "");
        }

        InputStream in = res.openRawResource(r.first());
        return new Response(HTTP_OK, r.second(), in);
    }

}
