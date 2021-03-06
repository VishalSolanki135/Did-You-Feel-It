package com.example.didyoufeelit;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

public final class Utils {
    public static final String LOG_TAG = Utils.class.getSimpleName();

    public static Event fetchEarthquakeData(String requestUrl) {
        URL url = createURL(requestUrl);

        //perform a http request and getting a response back from the server in the form of json
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error", e);
        }

        Event earthquake = extractFeatureFromJson(jsonResponse);
        return earthquake;
    }

    private static URL createURL(String stringURL) {
        URL url = null;
        try {
            url = new URL(stringURL);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with crearting URL", e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url ) throws IOException{
        String jsonResponse = "";
        if(url==null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try{
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if(urlConnection.getResponseCode()==200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromInputStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG,"Error getting the data from the server.", e);
        } finally {
            if(urlConnection!=null) {
                urlConnection.disconnect();
            }
            if(inputStream!=null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if(inputStream!=null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = bufferedReader.readLine();
            while(line!=null) {
                output.append(line);
                line = bufferedReader.readLine();
            }
        }
        return output.toString();
    }

    private static Event extractFeatureFromJson(String earthquakeJson) {
        if (earthquakeJson.isEmpty()) {
            return null;
        }
        try {
            JSONObject baseJsonResponse = new JSONObject(earthquakeJson);
            JSONArray featureArray = baseJsonResponse.getJSONArray("features");

            if(featureArray.length()>0) {
                JSONObject firstFeature = featureArray.getJSONObject(0);
                JSONObject properties = firstFeature.getJSONObject("properties");

                String title = properties.getString("title");
                String numOfPeople = properties.getString("felt");
                String strength = properties.getString("cdi");

                return new Event(title, numOfPeople, strength);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error while extracting the features", e);
        }
        return null;
    }
}
