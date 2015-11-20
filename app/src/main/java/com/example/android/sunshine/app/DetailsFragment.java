package com.example.android.sunshine.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

//        the DetailsActivity called via intent ,inspect instent for forecast data
        Intent intent = getActivity().getIntent();
        View rootView = inflater.inflate(R.layout.fragment_details,container,false);

//        Checking the intent first
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {

            String forecastStr = intent.getStringExtra(Intent.EXTRA_TEXT);
//            Setting the TextView
            ((TextView) rootView.findViewById(R.id.detailscontent))
                    .setText(forecastStr);
        }

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_settings:
                Intent intent = new Intent(getActivity(),SettingsActivity.class);
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
