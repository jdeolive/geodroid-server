package org.geodroid.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jeo.data.DataRepository;
import org.jeo.data.Dataset;
import org.jeo.data.DatasetHandle;
import org.jeo.data.TileDataset;
import org.jeo.data.VectorDataset;
import org.jeo.data.Workspace;
import org.jeo.data.WorkspaceHandle;
import org.jeo.feature.Field;
import org.jeo.feature.Schema;
import org.jeo.geom.Geom;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.vividsolutions.jts.geom.Geometry;

public class LayersPage extends PageFragment {

    static enum Tag {
        ALL (R.string.all, -1), 
        POINT (R.string.point, R.drawable.tab_point), 
        LINE (R.string.linestring, R.drawable.tab_line), 
        POLY (R.string.polygon, R.drawable.tab_poly), 
        TILE (R.string.tile, R.drawable.tab_tile);

        final int label;
        final int icon;

        Tag(int label, int icon) {
            this.label = label;
            this.icon = icon;
        }

    }

    View progress;

    @Override
    protected void doCreateView(LayoutInflater inflater, ViewGroup container,
        Preferences p, Bundle state) {
        
        View v = inflater.inflate(R.layout.page_layers, container);

        progress = v.findViewById(R.id.layers_progress);
        progress.setVisibility(View.INVISIBLE);

        TabHost tabs = (TabHost) v.findViewById(R.id.layers_tabs);
        tabs.setup();

        ViewGroup tableRoot = (ViewGroup) v.findViewById(R.id.layers_table_root);

        newTab(tabs, Tag.ALL, tableRoot);
        newTab(tabs, Tag.POINT, tableRoot);
        newTab(tabs, Tag.LINE, tableRoot);
        newTab(tabs, Tag.POLY, tableRoot);
        newTab(tabs, Tag.TILE, tableRoot);

        TabWidget tw = tabs.getTabWidget();
        for (int i = 0; i < tw.getTabCount(); i++) {
            TextView tv = (TextView) tw.getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(getResources().getColorStateList(R.drawable.tab_text));
        }
    }

    void newTab(TabHost tabs, Tag tag, final ViewGroup contentRoot) {
        TabSpec tab = tabs.newTabSpec(tag.name());
        int label = tag.label;
        int icon = tag.icon;
        
        //TextView text = new TextView(getActivity());
        //text.setText(label);
        //text.setTextSize(16);
        //text.setTextColor(getResources().getColor(R.color.primary_text_dark));
        if (icon != -1) {
            //tab.setIndicator(getResources().getText(label), getResources().getDrawable(icon));
            tab.setIndicator("", getResources().getDrawable(icon));
        }
        else {
            tab.setIndicator(getResources().getText(label));
        }
        
        tab.setContent(new TabContentFactory() {
            @Override
            public View createTabContent(String tag) {
                return doCreateTabContent(tag, contentRoot);
            }
        });
        tabs.addTab(tab);
    }

    @SuppressWarnings("unchecked")
    View doCreateTabContent(String tag, ViewGroup root) {
        root.removeAllViews();

        LayoutInflater inflater = getActivity().getLayoutInflater();
        TableLayout table = (TableLayout) 
            inflater.inflate(R.layout.layers_table, root).findViewById(R.id.layers_table);

        // fire off a background task to process the registry for datasets/layers

        new LoadLayers(table).execute(Tag.valueOf(tag));
        return table;

    }

    void createTableRow(String name, String title, int icon, final String prevLink, TableLayout t) {

        TableRow row = 
            (TableRow) getActivity().getLayoutInflater().inflate(R.layout.layers_table_row, null);

        TextView titleText = (TextView) row.findViewById(R.id.layers_table_title);
        if (title != null) {
            titleText.setText(title);
        }
        else {
            row.removeView(titleText);
        }

        TextView nameText = (TextView) row.findViewById(R.id.layers_table_name);
        nameText.setText(name);

        ImageView typeIcon = (ImageView) row.findViewById(R.id.layers_table_type);
        if (icon != -1) {
            typeIcon.setImageResource(icon);
        }

        ImageView prevImg = (ImageView) row.findViewById(R.id.layers_table_preview);
        prevImg.setClickable(true);
        prevImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(prevLink)));
            }
        });

        row.requestLayout();
        t.addView(row);

        TableRow div = 
            (TableRow) getActivity().getLayoutInflater().inflate(R.layout.layers_table_div, null);
        t.addView(div);
    }

    class LoadLayers extends AsyncTask<Tag, ProgressBar, Exception> {

        TableLayout table;

        public LoadLayers(TableLayout table) {
            this.table = table;
        }

        @Override
        protected void onPreExecute() {
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Exception doInBackground(Tag... args) {
            try {
                Tag t = args[0];
                DataRepository r = getDataRepository();
    
                Predicate<Dataset> filter = Predicates.alwaysTrue();

                switch(t) {
                case ALL:
                    break;
                case TILE:
                    filter = Predicates.and(filter, new DatasetType(t));
                    break;
                default:
                    filter = Predicates.and(filter, new VectorType(t));
                }

                new DatasetVisitor(filter) {
                    protected void error(Exception e, WorkspaceHandle h) {
                        Log.w(GeodroidServer.TAG, "Error loading workspace: " + h.getName(), e);
                    };

                    protected void visit(DatasetHandle ref, WorkspaceHandle parent) throws IOException {
                        try {
                            Dataset data = ref.resolve();
                            final String name = data.getName();
                            final String title = data.getTitle();
                            final int icon;
    
                            if (data instanceof VectorDataset) {
                                //TODO: icons for geometry and collection
                                Schema schema = ((VectorDataset) data).schema();
                                Field geom = schema.geometry();
                                if (geom != null) {
                                    switch(Geom.Type.from(geom.getType())) {
                                    case POINT:
                                    case MULTIPOINT:
                                        icon = R.drawable.ic_point_white;
                                        break;
                                    case LINESTRING:
                                    case MULTILINESTRING:
                                        icon = R.drawable.ic_line_white;
                                        break;
                                    case POLYGON:
                                    case MULTIPOLYGON:
                                        icon = R.drawable.ic_poly_white;
                                        break;
                                        
                                    default:
                                        icon = -1;
                                    }
                                }
                                else {
                                    //TODO: icon for vector data with no geometry
                                    icon = -1;
                                }
                            }
                            else if (data instanceof TileDataset) {
                                icon = R.drawable.ic_tile_white;
                            }
                            else {
                                icon = -1;
                            }
    
                            Preferences pref = getPreferences();
    
                            StringBuilder buf = new StringBuilder("http://localhost:");
                            buf.append(pref.getPort());
    
                            if (data instanceof TileDataset) {
                                buf.append("/tiles");
                            }
                            else {
                                buf.append("/features");
                            }
    
                            if (parent != null) {
                                buf.append("/").append(parent.getName());
                            }
    
                            buf.append("/").append(data.getName()).append(".html");
    
                            final String prevLink = buf.toString();
                            getView().post(new Runnable() {
                                public void run() {
                                    createTableRow(name, title, icon, prevLink, table);
                                }
                            });
                        }
                        catch(Exception e) {
                            Log.w(GeodroidServer.TAG, "Error loading dataset: " + ref.getName(), e);
                        }
                    };
                }.process(r);
            }
            catch(Exception e) {
                return e;
            }

           return null;
        }
    
        @Override
        protected void onPostExecute(Exception result) {
            progress.setVisibility(View.INVISIBLE);

            if (result != null) {
                ErrorDialog.show(result, getActivity());
            }
        }
    }
    
    static abstract class DatasetVisitor {
        protected Predicate<Dataset> filter;

        public DatasetVisitor(Predicate<Dataset> filter) {
            this.filter = filter;
        }

        protected void error(Exception e, WorkspaceHandle h) {
        }

        protected abstract void visit(DatasetHandle dataset, WorkspaceHandle parent) throws IOException;

        public void process(DataRepository reg) throws IOException {
            for (WorkspaceHandle ref : reg.list()) {
                try {
                    Workspace ws = (Workspace) ref.resolve();
                    try {
                        for (DatasetHandle d : ws.list()) {
                            visit(d, ref);
                        }
                    }
                    finally {
                        ws.close();
                    }
                }
                catch(Exception e) {
                    error(e, ref);
                }
            }
        }
    }
    
    static class DatasetType implements Predicate<Dataset> {

        Tag tag;
        
        public DatasetType(Tag tag) {
            this.tag = tag;
        }

        @Override
        public boolean apply(Dataset data) {
            switch(tag) {
            case TILE:
                return data instanceof TileDataset;
            default:
                return data instanceof VectorDataset;
            }
        }
    }

    static class VectorType implements Predicate<Dataset> {

        Tag tag;

        public VectorType(Tag tag) {
            this.tag = tag;
        }

        @Override
        public boolean apply(Dataset data) {
            if (data instanceof VectorDataset) {
                VectorDataset vector = (VectorDataset) data;
                Schema schema;
                try {
                    schema = vector.schema();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                Field geometry = schema.geometry();
                if (geometry == null) {
                    return false;
                }

                Class<? extends Geometry> gtype = (Class<? extends Geometry>) geometry.getType();
                switch(tag) {
                case POINT:
                    switch(Geom.Type.from(gtype)) {
                        case POINT:
                        case MULTIPOINT:
                            return true;
                        default:
                            return false;
                    }
                case LINE:
                    switch(Geom.Type.from(gtype)) {
                        case LINESTRING:
                        case MULTILINESTRING:
                            return true;
                        default:
                            return false;
                    }
                
                case POLY:
                    switch(Geom.Type.from(gtype)) {
                        case POLYGON:
                        case MULTIPOLYGON:
                            return true;
                        default:
                            return false;
                    }
                case ALL:
                    return true;
                }
            }

            return false;
        }
    
    }
}
