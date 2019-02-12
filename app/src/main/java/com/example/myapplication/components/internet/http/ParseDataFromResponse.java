package com.example.myapplication.components.internet.http;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import static com.example.myapplication.MainActivity.TAG;

public class ParseDataFromResponse {
    private final HttpURLConnection connection;
    public ParseDataFromResponse(HttpURLConnection connection) {

        this.connection = connection;
    }

    public String printResultFromTheServer() throws IOException, JSONException {
        // response body
        BufferedReader br;
        if (200 <= connection.getResponseCode() && connection.getResponseCode() <= 299) {
            br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        } else {
            br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        JSONObject json = new JSONObject(sb.toString());

        if (json.get("status").equals("success")) {

            return json.get("result").toString();

        } else {
            Log.e(TAG, json.get("status").toString());
            throw new Error("There is not status");
        }
    }
}
