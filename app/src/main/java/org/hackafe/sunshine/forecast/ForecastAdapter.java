package org.hackafe.sunshine.forecast;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.hackafe.sunshine.R;
import org.hackafe.sunshine.keys.PREFS;
import org.hackafe.sunshine.models.Forecast;

import java.util.List;

public class ForecastAdapter extends BaseAdapter {
    LayoutInflater mInflater;
    int rowCount;
    List<Forecast> forecastList;
    Context mContext;
    String forecastType;

    public ForecastAdapter(LayoutInflater inflater, Context context, List<Forecast> forecastList, String forecastType) {
        this.mInflater = inflater;
        this.mContext = context;
        this.rowCount = forecastList.size();
        this.forecastList = forecastList;
        this.forecastType = forecastType;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView;
        Forecast forecast;

        if (convertView == null) {
            itemView = mInflater.inflate(R.layout.forecast_main_list_item, parent, false);
        } else {
            itemView = convertView;
        }

        forecast = forecastList.get(position);

        TextView txtWeekDay = (TextView) itemView.findViewById(R.id.txtWeekDay);
        TextView txtDate = (TextView) itemView.findViewById(R.id.txtDate);
        TextView txtTempMaxMin = (TextView) itemView.findViewById(R.id.txtTempMaxMin);
        TextView txtCondition = (TextView) itemView.findViewById(R.id.txtCondition);
        TextView txtDescription = (TextView) itemView.findViewById(R.id.txtDescription);
        TextView txtHumidity = (TextView) itemView.findViewById(R.id.txtHumidity);
        TextView txtWindSpeed = (TextView) itemView.findViewById(R.id.txtWindSpeed);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        String iconSet = sharedPreferences.getString(PREFS.KEY_ICON_SET, mContext.getString(R.string.sett_icon_set_default));

        ImageView imgIcon = (ImageView) itemView.findViewById(R.id.imgIcon);
        imgIcon.setImageResource(mContext.getResources().getIdentifier("ic_" + ((iconSet.equals("r")) ? "" : iconSet + "_") + forecast.getIcon().substring(0, 3), "drawable", mContext.getPackageName()));

        String unitSystem = sharedPreferences.getString(PREFS.KEY_CURRENT_UNIT_SYSTEM, mContext.getString(R.string.sett_unit_system_default));
        String unitsSpeed = sharedPreferences.getString(PREFS.KEY_CURRENT_UNITS_SPEED, mContext.getString(R.string.sett_units_metric_speed_default));
        String degrees = "C";

        double tempMinConverted = 0;
        double tempMaxConverted = 0;
        double windSpeedConverted = 0;

        tempMinConverted = Math.round(forecast.getTempMin());
        tempMaxConverted = Math.round(forecast.getTempMax());

        if (unitSystem.equals("Metric")) {
            degrees = "C";

            if (unitsSpeed.equals("m/s")) {
                windSpeedConverted = Math.round(forecast.getWind() * 100.0) / 100.0;
            }
            if (unitsSpeed.equals("km/h")) {
                windSpeedConverted = Math.round(forecast.getWind() * 100.0 * 3.6) / 100.0;
            }
        }

        if (unitSystem.equals("Imperial")) {
            degrees = "F";

            if (unitsSpeed.equals("fps")) {
                windSpeedConverted = Math.round(forecast.getWind() * 100.0) / 100.0;
            }
            if (unitsSpeed.equals("mph")) {
                windSpeedConverted = Math.round(forecast.getWind() * 100.0 * 0.681818) / 100.0;
            }
        }

        switch (forecastType) {
            case PREFS.KEY_FORECAST_DAILY:
                txtWeekDay.setText("" + forecast.getForDateWeek());
                break;
            case PREFS.KEY_FORECAST_HOURLY:
                txtWeekDay.setText("" + forecast.getHour());
                break;
        }

        txtDate.setText("" + forecast.getDateDate());
        txtTempMaxMin.setText("" + Math.round(tempMaxConverted) + "/" + Math.round(tempMinConverted) + "\u00B0" + degrees);
        txtCondition.setText("" + forecast.getCondition());
        txtDescription.setText("" + forecast.getDescription());
        txtHumidity.setText("Humidity: " + forecast.getHumidity() + "%");
        txtWindSpeed.setText("Wind Speed: " + windSpeedConverted + " " + unitsSpeed);

        return itemView;
    }

    @Override
    public int getCount() {
        return this.rowCount;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
