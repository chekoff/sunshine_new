package org.hackafe.sunshine.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import org.hackafe.sunshine.R;
import org.hackafe.sunshine.keys.PREFS;

import java.util.Arrays;

public class SettingsFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        EditTextPreference prefLocation = (EditTextPreference) findPreference(PREFS.KEY_CURRENT_LOCATION_NAME);
        ListPreference prefUnitSystem = (ListPreference) findPreference(PREFS.KEY_CURRENT_UNIT_SYSTEM);
        final ListPreference prefUnitsSpeed = (ListPreference) findPreference(PREFS.KEY_CURRENT_UNITS_SPEED);
        EditTextPreference prefForecastDaysCount = (EditTextPreference) findPreference(PREFS.KEY_FORECAST_DAYS_COUNT);

        //setting summary to current preference values
        prefLocation.setSummary(prefLocation.getText());
        prefUnitSystem.setSummary(prefUnitSystem.getValue());
        prefUnitsSpeed.setSummary(prefUnitsSpeed.getValue());
        prefForecastDaysCount.setSummary(prefForecastDaysCount.getText() + " day/s");

        if (prefUnitSystem.getValue().equals("Metric")) {
            prefUnitsSpeed.setDefaultValue(R.string.sett_units_metric_speed_default);
            prefUnitsSpeed.setEntries(R.array.sett_units_metric_speed_list_values);
            prefUnitsSpeed.setEntryValues(R.array.sett_units_metric_speed_list_values);
        }

        if (prefUnitSystem.getValue().equals("Imperial")) {
            prefUnitsSpeed.setDefaultValue(R.string.sett_units_imperial_speed_default);
            prefUnitsSpeed.setEntries(R.array.sett_units_imperial_speed_list_values);
            prefUnitsSpeed.setEntryValues(R.array.sett_units_imperial_speed_list_values);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        ListPreference prefUnitSystem = (ListPreference) findPreference(PREFS.KEY_CURRENT_UNIT_SYSTEM);
        ListPreference prefUnitsSpeed = (ListPreference) findPreference(PREFS.KEY_CURRENT_UNITS_SPEED);

        switch (key) {
            case PREFS.KEY_CURRENT_LOCATION_NAME:
                EditTextPreference prefLocation = (EditTextPreference) findPreference(key);
                prefLocation.setSummary(sharedPreferences.getString(key, getResources().getString(R.string.sett_location_default)));
                break;

            case PREFS.KEY_CURRENT_UNIT_SYSTEM:
                prefUnitSystem.setSummary(sharedPreferences.getString(key, getResources().getString(R.string.sett_unit_system_default)));

                String unitsSystem = sharedPreferences.getString(key, getResources().getString(R.string.sett_unit_system_default));

                //changing values of prefUnitsSpeed
                if (unitsSystem.equals("Metric")) {
                    String[] unitsSpeedArray = getResources().getStringArray(R.array.sett_units_metric_speed_list_values);
                    if (!Arrays.asList(unitsSpeedArray).contains(prefUnitsSpeed.getValue())) {
                        prefUnitsSpeed.setValue(getString(R.string.sett_units_metric_speed_default));
                        prefUnitsSpeed.setSummary(prefUnitsSpeed.getValue());
                        prefUnitsSpeed.setDefaultValue(getString(R.string.sett_units_metric_speed_default));
                        prefUnitsSpeed.setEntries(R.array.sett_units_metric_speed_list_values);
                        prefUnitsSpeed.setEntryValues(R.array.sett_units_metric_speed_list_values);
                    }
                }

                if (unitsSystem.equals("Imperial")) {
                    String[] unitsSpeedArray = getResources().getStringArray(R.array.sett_units_imperial_speed_list_values);
                    if (!Arrays.asList(unitsSpeedArray).contains(prefUnitsSpeed.getValue())) {
                        prefUnitsSpeed.setValue(getString(R.string.sett_units_imperial_speed_default));
                        prefUnitsSpeed.setSummary(prefUnitsSpeed.getValue());
                        prefUnitsSpeed.setDefaultValue(getString(R.string.sett_units_imperial_speed_default));
                        prefUnitsSpeed.setEntries(R.array.sett_units_imperial_speed_list_values);
                        prefUnitsSpeed.setEntryValues(R.array.sett_units_imperial_speed_list_values);
                    }
                }
                break;

            case PREFS.KEY_CURRENT_UNITS_SPEED:
                if (prefUnitSystem.getValue().equals("Metric"))
                    prefUnitsSpeed.setSummary(sharedPreferences.getString(key, getResources().getString(R.string.sett_units_metric_speed_default)));
                if (prefUnitSystem.getValue().equals("Imperial"))
                    prefUnitsSpeed.setSummary(sharedPreferences.getString(key, getResources().getString(R.string.sett_units_imperial_speed_default)));
                break;

            case PREFS.KEY_FORECAST_DAYS_COUNT:
                EditTextPreference prefForecastDaysCount = (EditTextPreference) findPreference(key);
                prefForecastDaysCount.setSummary(sharedPreferences.getString(key, getResources().getString(R.string.sett_forecast_period_default)) + " day/s");
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
