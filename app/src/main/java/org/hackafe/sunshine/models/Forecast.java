package org.hackafe.sunshine.models;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Forecast {
    String locationName;
    long forDate;
    int tempMin;
    int tempMax;
    double humidity;
    double wind;
    String condition;
    String description;
    String icon;

    public Forecast() {
    }

    public Forecast(String locationName,
                    long forDate,
                    double tempMin,
                    double tempMax,
                    double humidity,
                    double wind,
                    String condition,
                    String description,
                    String icon) {

        this.locationName = locationName;
        this.forDate = forDate;
        this.tempMin = (int) Math.round(tempMin);
        this.tempMax = (int) Math.round(tempMax);
        this.humidity = humidity;
        this.wind = wind;
        this.condition = condition;
        this.description = description;
        this.icon = icon;
    }

    //setters
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public void setForDate(long forDate) {
        this.forDate = forDate;
    }

    public void setTempMin(double tempMin) {
        this.tempMin = (int) Math.round(tempMin);
    }

    public void setTempMax(double tempMax) {
        this.tempMax = (int) Math.round(tempMax);
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public void setWind(double wind) {
        this.wind = wind;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    //getters
    public String getForDateWeek() {
        SimpleDateFormat dateDayOfWeek = new SimpleDateFormat("E", Locale.US);

        return dateDayOfWeek.format(this.forDate * 1000);
    }

    public String getDateDate() {
        SimpleDateFormat date = new SimpleDateFormat("MMM d, yyyy", Locale.US);

        return date.format(this.forDate * 1000);
    }

    public String getHour() {
        SimpleDateFormat date = new SimpleDateFormat("kk:mm", Locale.US);

        return date.format(this.forDate * 1000);
    }

    public String getLocationName() {
        return this.locationName;
    }

    public int getTempMin() {
        return this.tempMin;
    }

    public int getTempMax() {
        return this.tempMax;
    }

    public double getHumidity() {
        return this.humidity;
    }

    public double getWind() {
        return this.wind;
    }

    public String getCondition() {
        return this.condition;
    }

    public String getDescription() {
        return this.description;
    }

    public String getIcon() {
        return this.icon;
    }

    public long getForDate() {
        return forDate;
    }
}
