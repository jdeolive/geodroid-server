package org.geodroid.server;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import static org.geodroid.server.GeodroidServer.TAG;

public class FilesHelper {

    public static void ensureFilesExist(Context context) {
        Preferences p = new Preferences(context);
        File[] files = new File[] {
            p.getWebDirectory(),
            p.getAppsDirectory(),
            p.getDataDirectory()
        };
        String[] paths = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            paths[i] = files[i].getAbsolutePath();
        }

        // scan the media to refresh anything created from MTP
        // see http://code.google.com/p/android/issues/detail?id=38282
        android.media.MediaScannerConnection.scanFile(context, paths, null, null);

        for (int i = 0; i < files.length; i++) {
            initDir(files[i]);
        }

        // and again to ensure the created directories are visible on MTP
        android.media.MediaScannerConnection.scanFile(context, paths, null, null);
    }

    private static File initDir(File dir) {
        if (!dir.exists()) {
            try {
                if (!dir.mkdirs()) {
                    throw new IOException("unable to create directory" + dir.getPath());
                }
            }
            catch(IOException e) {
                Log.w(TAG, "Unable to create directory", e);
            }
        } else {
            Log.i(TAG, "Already exists: " + dir.getAbsolutePath());
        }
        return dir;
    }

}
