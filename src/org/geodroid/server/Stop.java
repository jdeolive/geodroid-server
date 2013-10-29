package org.geodroid.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Stop extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.stopService(new Intent(context, GeodroidServerService.class));
    }

}
