package org.geodroid.server;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.MenuItem;

/**
 * An activity representing a single Page detail screen. This activity is only
 * used on handset devices. On tablet-size devices, item details are presented
 * side-by-side with a list of items in a {@link AdminActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link PageFragment}.
 */
public class DetailActivity extends Activity {

    /**
     * The fragment argument representing the item ID that this fragment represents.
     */
    public static final String ARG_PAGE = "page";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        setContentView(R.layout.activity_detail);
        setTitle(null);

        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);
    
        // state is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (state == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Page page = (Page)getIntent().getExtras().get(ARG_PAGE);
            Fragment frag = page.newFragment();
            getFragmentManager().beginTransaction().add(R.id.detail_container, frag).commit();
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //

            finish();
            //startActivity(new Intent(this, GeodroidServerActivity.class));
            //NavUtils.navigateUpTo(this, new Intent(this, AdminActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
