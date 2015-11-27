package com.example.android.sunshine.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailsFragment extends Fragment {
    private final String LOG_TAG = DetailsFragment.class.getSimpleName();
    private static final String FORECAST_SHARE_HASHTAG = "#SunshineApp";
    private String mForecastString;

    //action provider
    private ShareActionProvider shareActionProvider;

    public DetailsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Handling menu events
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

//        the DetailsActivity called via intent ,inspect intent for forecast data
        Intent intent = getActivity().getIntent();
        View rootView = inflater.inflate(R.layout.fragment_details,container,false);

//        Checking the intent first
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {

            mForecastString = intent.getStringExtra(Intent.EXTRA_TEXT);
//            Setting the TextView
            ((TextView) rootView.findViewById(R.id.detailscontent))
                    .setText(mForecastString);
        }
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){

        inflater.inflate(R.menu.detailfragment, menu);

        //Locate item with shareActionProvider
        MenuItem shareItem = menu.findItem(R.id.action_share);

        //Fetch and store shareactionprovider
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);

        //Attach an intent to this action provider
        //this part could be placed in a method to be called whenever we want
        //to change the intent (e.g when an ListView item is clicked)
        if (null != shareActionProvider){

            shareActionProvider.setShareIntent(createShareForecastIntent());
        } else {

            Log.d(LOG_TAG, "shareActionPovider is null!");
        }
    }

    private Intent createShareForecastIntent(){

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        /*
        prevent the application from being placed on to the activity stack
         */
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                mForecastString + FORECAST_SHARE_HASHTAG);

        return shareIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_settings:
                Intent intent = new Intent(getActivity(),SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
