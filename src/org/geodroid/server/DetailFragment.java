package org.geodroid.server;

import org.jeo.data.Registry;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.AndroidRuntimeException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;

/**
 * A fragment representing a single Page detail screen. This fragment is either
 * contained in a {@link AdminActivity} in two-pane mode (on tablets) or a
 * {@link DetailActivity} on handsets.
 */
public abstract class DetailFragment extends Fragment {

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DetailFragment() {
    }

    protected GeodroidServer getApp() {
        return GeodroidServer.get(getActivity());
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        ViewGroup rootView = 
            (ViewGroup) inflater.inflate(R.layout.fragment_page_detail, container, false);
        rootView.removeAllViews();

        doCreateView(inflater, rootView, new Preferences(getActivity()), state);
        return rootView;
    }

    protected Registry getDataRegistry() {
        Activity a = getActivity();
        if (!(a instanceof GeodroidServerActivity)) {
            a = a.getParent();
        }

        if (a instanceof GeodroidServerActivity) {
            return ((GeodroidServerActivity) a).getDataRegistry();
        }
        else {
            throw new AndroidRuntimeException("Could not find GeoDroidServer in activity stack");
        }
    }

    protected Preferences getPreferences() {
        return new Preferences(getActivity());
    }

    protected abstract void doCreateView(
        LayoutInflater inflater, ViewGroup container, Preferences p, Bundle state);

    protected boolean doHandleRefresh(MenuItem item) {
        return false;
    }
}
