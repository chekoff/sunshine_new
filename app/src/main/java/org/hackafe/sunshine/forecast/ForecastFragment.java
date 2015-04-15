package org.hackafe.sunshine.forecast;

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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.hackafe.sunshine.R;
import org.hackafe.sunshine.hourly.HourlyActivity;
import org.hackafe.sunshine.keys.PREFS;
import org.hackafe.sunshine.models.Forecast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ForecastFragment extends ListFragment implements
        SwipeRefreshLayout.OnRefreshListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    LayoutInflater mInflater;
    SharedPreferences mSharedPreferences;
    SharedPreferences.OnSharedPreferenceChangeListener mPreferenceListener;
    String currentLocationName = PREFS.KEY_CURRENT_LOCATION_NAME;
    String currentUnitSystem = PREFS.KEY_CURRENT_UNIT_SYSTEM;
    String forecastDaysCount = PREFS.KEY_FORECAST_DAYS_COUNT;
    SwipeRefreshLayout mSwipeRefreshLayout;
    boolean isDataLoading = false;
    boolean changedPreferences = false;
    View rootView;
    List<Forecast> forecastList = new ArrayList<>();
    ForecastAdapter forecastAdapter;
    String errMsg = "";


    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.mInflater = inflater;

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mPreferenceListener =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                        if (!key.equals(PREFS.KEY_ICON_SET))
                            changedPreferences = true;
                        else
                            forecastAdapter.notifyDataSetChanged();
                    }
                };
        mSharedPreferences.registerOnSharedPreferenceChangeListener(mPreferenceListener);

        currentLocationName = mSharedPreferences.getString(PREFS.KEY_CURRENT_LOCATION_NAME, getActivity().getString(R.string.sett_location_default));

        rootView = inflater.inflate(R.layout.forecast_main_list, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.container);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_light);

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
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Intent intent = new Intent(getActivity(), HourlyActivity.class);
        intent.putExtra(PREFS.KEY_CURRENT_DATE, forecastList.get(position).getForDate());
        startActivity(intent);
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

        isDataLoading = true;

        currentLocationName = mSharedPreferences.getString(PREFS.KEY_CURRENT_LOCATION_NAME, getActivity().getString(R.string.sett_location_default));
        currentUnitSystem = mSharedPreferences.getString(PREFS.KEY_CURRENT_UNIT_SYSTEM, getActivity().getString(R.string.sett_unit_system_default));
        forecastDaysCount = mSharedPreferences.getString(PREFS.KEY_FORECAST_DAYS_COUNT, getActivity().getString(R.string.sett_forecast_period_default));

        errMsg = "";

        JSONObject jsonObject = new JSON(String.format(
                "http://api.openweathermap.org/data/2.5/forecast/daily?q=%s&units=%s&cnt=%s&mode=json",
                currentLocationName,
                currentUnitSystem,
                forecastDaysCount),
                "forecast").getJsonObject();

        if (jsonObject == null) {
            isDataLoading = false;
            changedPreferences = false;
            errMsg = "Can not fetch forecast data. Try again later.";
        } else {
            //fetching forecast data
            try {
                JSONArray jsonList = jsonObject.getJSONArray("list");
                forecastList.clear();

                for (int i = 0; i < jsonList.length(); i++) {
                    int forDate = jsonList.getJSONObject(i).getInt("dt");
                    double humidity = jsonList.getJSONObject(i).getInt("humidity");
                    double wind = jsonList.getJSONObject(i).getDouble("speed");

                    JSONObject jsonTemp = jsonList.getJSONObject(i).getJSONObject("temp");
                    double tempMin = jsonTemp.getDouble("min");
                    double tempMax = jsonTemp.getDouble("max");

                    JSONObject jsonWeather = jsonList.getJSONObject(i).getJSONArray("weather").getJSONObject(0);
                    String condition = jsonWeather.getString("main");
                    String description = jsonWeather.getString("description");
                    String icon = jsonWeather.getString("icon");

                    forecastList.add(new Forecast(
                            currentLocationName,
                            forDate,
                            tempMin,
                            tempMax,
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
            changedPreferences = false;
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
            pDialog.setMessage("Fetching forecast...");
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

            forecastAdapter = new ForecastAdapter(mInflater, getActivity(), forecastList, PREFS.KEY_FORECAST_DAILY);
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
