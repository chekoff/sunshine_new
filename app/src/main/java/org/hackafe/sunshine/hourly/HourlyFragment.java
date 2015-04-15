package org.hackafe.sunshine.hourly;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.hackafe.sunshine.R;
import org.hackafe.sunshine.forecast.ForecastAdapter;
import org.hackafe.sunshine.forecast.JSON;
import org.hackafe.sunshine.keys.PREFS;
import org.hackafe.sunshine.models.Forecast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HourlyFragment extends ListFragment implements
        SwipeRefreshLayout.OnRefreshListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    LayoutInflater mInflater;
    SharedPreferences mSharedPreferences;
    SharedPreferences.OnSharedPreferenceChangeListener mPreferenceListener;
    String currentLocationName = PREFS.KEY_CURRENT_LOCATION_NAME;
    String currentUnitSystem = PREFS.KEY_CURRENT_UNIT_SYSTEM;
    SwipeRefreshLayout mSwipeRefreshLayout;
    public long forecastForDate;
    String forecastForDateFormatted;
    SimpleDateFormat forecastForDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
    SimpleDateFormat forecastForDateCompare = new SimpleDateFormat("yyyyMMdd", Locale.US);
    boolean isDataLoading = false;
    boolean changedPreferences = false;
    View rootView;
    List<Forecast> forecastList = new ArrayList<>();
    ForecastAdapter forecastAdapter;
    String errMsg = "";

    public HourlyFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        this.mInflater = inflater;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mPreferenceListener =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                        if (!key.equals(PREFS.KEY_FORECAST_DAYS_COUNT) &&
                                !key.equals(PREFS.KEY_ICON_SET))
                            changedPreferences = true;
                        if (key.equals(PREFS.KEY_ICON_SET))
                            forecastAdapter.notifyDataSetChanged();
                    }
                };
        mSharedPreferences.registerOnSharedPreferenceChangeListener(mPreferenceListener);

        currentLocationName = mSharedPreferences.getString(PREFS.KEY_CURRENT_LOCATION_NAME, getActivity().getString(R.string.sett_location_default));
        Intent intent = getActivity().getIntent();
        forecastForDate = intent.getLongExtra(PREFS.KEY_CURRENT_DATE, System.currentTimeMillis());

        rootView = inflater.inflate(R.layout.hourly_main_list, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.container);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_light);

        //setting current date
        forecastForDateFormatted = forecastForDateFormat.format(forecastForDate * 1000);
        TextView txtCurrentDate = (TextView) rootView.findViewById(R.id.txtCurrentDate);
        txtCurrentDate.setText(forecastForDateFormatted);

        //setting location map
        ImageButton imgButtLocationMap = (ImageButton) rootView.findViewById(R.id.imgButtLocationMap);
        imgButtLocationMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + currentLocationName.toUpperCase()));
                    startActivity(intent);
                } catch (Throwable t) {
                    Toast.makeText(getActivity(), "There is no map application installed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mSwipeRefreshLayout.setRefreshing(true);
        onRefresh();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        currentLocationName = mSharedPreferences.getString(PREFS.KEY_CURRENT_LOCATION_NAME, getActivity().getString(R.string.sett_location_default));

        TextView txtCurrentLocation = (TextView) rootView.findViewById(R.id.txtCurrentLocation);
        txtCurrentLocation.setText(currentLocationName);

        if (changedPreferences) {
            onRefresh();
        }
    }

    @Override
    public void onRefresh() {
        new getForecastTask().execute();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void getForecast() {
        if (isDataLoading)
            return;

        currentLocationName = mSharedPreferences.getString(PREFS.KEY_CURRENT_LOCATION_NAME, getActivity().getString(R.string.sett_location_default));
        currentUnitSystem = mSharedPreferences.getString(PREFS.KEY_CURRENT_UNIT_SYSTEM, getActivity().getString(R.string.sett_unit_system_default));

        errMsg = "";

        JSONObject jsonObject = new JSON(String.format(
                "http://api.openweathermap.org/data/2.5/forecast?q=%s&units=%s&mode=json",
                currentLocationName,
                currentUnitSystem),
                "forecast").getJsonObject();

        if (jsonObject == null) {
            isDataLoading = false;
            errMsg = "Can not fetch forecast data. Try again later.";
        } else {
            //fetching forecast data
            try {
                JSONArray jsonList = jsonObject.getJSONArray("list");
                forecastList.clear();

                for (int i = 0; i < jsonList.length(); i++) {
                    long forDate = jsonList.getJSONObject(i).getLong("dt");

                    if (!forecastForDateCompare.format(forecastForDate * 1000).equals(forecastForDateCompare.format(forDate * 1000)))
                        continue;

                    JSONObject jsonMain = jsonList.getJSONObject(i).getJSONObject("main");
                    double temp = jsonMain.getDouble("temp");
                    double humidity = jsonMain.getInt("humidity");

                    JSONObject jsonWind = jsonList.getJSONObject(i).getJSONObject("wind");
                    double wind = jsonWind.getDouble("speed");

                    JSONObject jsonWeather = jsonList.getJSONObject(i).getJSONArray("weather").getJSONObject(0);
                    String condition = jsonWeather.getString("main");
                    String description = jsonWeather.getString("description");
                    String icon = jsonWeather.getString("icon");

                    forecastList.add(new Forecast(
                            currentLocationName,
                            forDate,
                            temp,
                            temp,
                            humidity,
                            wind,
                            condition,
                            description,
                            icon));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            isDataLoading = false;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    }

    private class getForecastTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Fetching hourly forecast...");
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getForecast();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            forecastAdapter = new ForecastAdapter(mInflater, getActivity(), forecastList, PREFS.KEY_FORECAST_HOURLY);
            setListAdapter(forecastAdapter);

            pDialog.dismiss();

            if (errMsg.length() > 0) {
                Toast.makeText(getActivity(), errMsg, Toast.LENGTH_LONG).show();
            } else {
                if (forecastList.size() == 0)
                    Toast.makeText(getActivity(), "There is no data available.", Toast.LENGTH_LONG).show();
            }
        }
    }

}
