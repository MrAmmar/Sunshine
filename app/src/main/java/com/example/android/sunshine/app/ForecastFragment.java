package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {
    private final String LOG_TAG = ForecastFragment.class.getSimpleName();
    public static final String PREF_NAME = "location";

    private ArrayList<String> forecast_array;
    private ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Handling menu events
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main,container,false);

        /**
         * Creating ArrayAdapter
         */
        mForecastAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item_forecast, R.id.list_item_forecast_txtview, new ArrayList<String>());

        /**
         * Initializing listForecast ListView and setting its adapter
         */
        ListView listForecast = (ListView)rootView.findViewById(R.id.listview_forecast);
        listForecast.setAdapter(mForecastAdapter);
        listForecast.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getActivity(),mForecastAdapter.getItem(position),Toast.LENGTH_SHORT).show();
                String forecast = mForecastAdapter.getItem(position);
                /**
                 * Creating an intent to start DetailsActivity
                 */
                Intent intent = new Intent(getActivity(),DetailsActivity.class);
                intent.putExtra(intent.EXTRA_TEXT,forecast);
                startActivity(intent);
            }
        });

        return rootView;
    }


    /**
     * Create FetchWeatherTask
     */

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        private String getReadableDateString(long time){
            /**
             * date/time converter
             */
            SimpleDateFormat shortDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortDateFormat.format(time);
        }

        private String formatHighLows (double high, double low){
            /**
             * Rounding tha min /max , usually the user doesn't care
             * about the tenths of a degree  14.6 => 14
             */
            long roundedHign = Math.round(high);
            long roundedLow = Math.round(low);

            String highlowStr = roundedHign + "/" + roundedLow;
            return highlowStr;
        }

        private String[] getWeatherDataFromJson(String forecastStr , int numDays)
                throws JSONException{

            // These are the names of the JSON objects that need to be extracted.
            String OWM_LIST = "list";
            String OWM_WEATHER = "weather";
            String OWM_TEMPERATURE = "temp";
            String OWM_MAX = "max";
            String OWM_MIN = "min";
            String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
            /**
             *
             */
            Time dayTime = new Time();
            dayTime.setToNow();

            //Setting the start date
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(),dayTime.gmtoff);

            //We initialize dayTime after using it
            dayTime = new Time();

            String[] resultStr = new String[numDays];
            for (int i = 0 ; i<weatherArray.length() ; i++ ){

                /**
                 * The final String structure
                 */
                String day,description,hignAndLow;

                //Get the Json object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                /**
                 * The date/time is returned as a long.  We need to convert that
                 * into something human-readable, since most people won't read "1400356800" as
                 * "this saturday".
                 */

                long dateTime;

                dateTime = dayTime.setJulianDay(julianStartDay + i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                hignAndLow = formatHighLows(high,low);

                resultStr[i] = day + " - " + description + " - " + hignAndLow;
            }

            return resultStr;
        }

        @Override
        protected String[] doInBackground(String ... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast

                //Making the URL more flexible by using the Uri.Buidler
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http");
                builder.authority("api.openweathermap.org");
                builder.appendPath("data");
                builder.appendPath("2.5");
                builder.appendPath("forecast");
                builder.appendPath("daily");
                builder.appendQueryParameter("q", params[0]);
                builder.appendQueryParameter("mode", "json");
                builder.appendQueryParameter("units","metric");
                builder.appendQueryParameter("cnt","7");
                builder.appendQueryParameter("APPID",BuildConfig.OPEN_WEATHER_MAP_API_KEY);
                // Recently , getting Data from openweathermap.org requires an API key
                URL url = new URL(builder.build().toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();

                return getWeatherDataFromJson(forecastJsonStr,7);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            if(strings != null){
                mForecastAdapter.clear();
                for(String dayForecastStr : strings){
                    mForecastAdapter.add(dayForecastStr);
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.forecastfragment,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){

            case R.id.action_ref:

                updateWeather();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateWeather(){

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = sharedPreferences.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));
        new FetchWeatherTask().execute(location);
    }
}
