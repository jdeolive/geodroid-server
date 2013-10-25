package org.geodroid.server;

import java.util.Arrays;
import java.util.List;

import org.geodroid.server.GeodroidServer.Status;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.AndroidRuntimeException;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class StatusSpinnerAdapter extends BaseAdapter {

    final static List<Status> STATE = Arrays.asList(Status.ONLINE, Status.OFFLINE);

    Status status = Status.UNKNOWN;

    Activity context;

    public StatusSpinnerAdapter(Activity context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return Status.values().length;
    }

    @Override
    public Status getItem(int position) {
        return Status.values()[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Status stat = getItem(position);

        View view = convertView;
        if (view == null) {
            view = new TextView(context);
        }

        Resources res = context.getResources();

        TextView txt = (TextView)view;
        txt.setTextColor(res.getColor(android.R.color.white));
        txt.setText(stat.name());
        txt.setPadding(10, 10, 10, 10);

        switch(stat) {
        case ONLINE:
            view.setBackgroundColor(res.getColor(android.R.color.holo_green_dark));
            break;
        case OFFLINE:
            view.setBackgroundColor(res.getColor(android.R.color.background_dark));
            break;
        case ERROR:
            view.setBackgroundColor(res.getColor(android.R.color.holo_red_dark));
        }
        return view;
    }

//    Status[] states() {
//        switch(status) {
//        case ONLINE:
//            return ON;
//        case OFFLINE:
//            return OFF;
//        case ERROR:
//            return ERR;
//        case UNKNOWN:
//            return NULL;
//        default:
//            throw new IllegalStateException();
//        }
//    }
}
