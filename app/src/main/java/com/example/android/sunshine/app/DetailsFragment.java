package com.example.android.sunshine.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailsFragment extends Fragment {

    public DetailsFragment() {
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
}
