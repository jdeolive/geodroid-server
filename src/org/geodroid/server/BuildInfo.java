package org.geodroid.server;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class BuildInfo {

    Context context;
    Properties props;

    public BuildInfo(InputStream in, Context context) {
        this.context = context;
        props = new Properties();
        try {
            props.load(in);
            in.close();
        } catch (IOException e) {
            Log.w(GeodroidServer.TAG, "Unable to load build info", e);
        }
    }

    public String getVersion() {
        PackageManager pkgMgr = context.getPackageManager();
        try {
            return pkgMgr.getPackageInfo(context.getPackageName(), 0).versionName;
            
        } catch (NameNotFoundException e) {
            Log.w(GeodroidServer.TAG, "Unable to load version info", e);
        }
        return null;
    }

    public String getBuildRev() {
        return props.getProperty("build.rev");
    }

    public String getBuildRevShort() {
        return props.getProperty("build.rev.short");
    }

    public String getBuildTimeHuman() {
        return props.getProperty("build.time.human");
    }
}
