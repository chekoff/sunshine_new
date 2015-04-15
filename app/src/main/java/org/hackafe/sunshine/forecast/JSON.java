package org.hackafe.sunshine.forecast;

import android.os.StrictMode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class JSON {
    JSONObject jsonObject;

    public JSON(String uri, String checkType) {
        URL url;
        int TIMEOUT_VALUE = 10000;
        StringBuilder dataBuilder = new StringBuilder();

        final StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            url = new URL(uri);

            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(TIMEOUT_VALUE);
            httpURLConnection.setReadTimeout(TIMEOUT_VALUE);
            httpURLConnection.connect();

            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStreamReader iReader = new InputStreamReader(httpURLConnection.getInputStream());
                BufferedReader bReader = new BufferedReader(iReader);

                String inputLine;
                while ((inputLine = bReader.readLine()) != null) {
                    dataBuilder.append(inputLine);
                }

                jsonObject = new JSONObject(dataBuilder.toString());


                if (checkType.equals("location")) {

                    String message = jsonObject.getString("message");

                    if (message.length() == 0)
                        jsonObject = null;
                    else {
                        int count = jsonObject.getInt("count");

                        if (count == 0)
                            jsonObject = null;
                    }
                }

                if (checkType.equals("forecast")) {
                    String cod = jsonObject.getString("cod");

                    if (!cod.equals("200"))
                        jsonObject = null;
                }

            } else {
                jsonObject = null;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            jsonObject = null;
        } catch (IOException e) {
            e.printStackTrace();
            jsonObject = null;
        } catch (JSONException e) {
            e.printStackTrace();
            jsonObject = null;
        }
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }
}
