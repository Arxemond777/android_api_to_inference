package com.example.myapplication.components;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

public class VisualizationDataFromResponse {
    private final HttpURLConnection connection;
    public VisualizationDataFromResponse(HttpURLConnection connection) {

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

//            printResult(json.get("result").toString());
            return (String) json.get("result");

        } else {
            System.err.println(json.get("status"));
            throw new Error("There is not status");
        }
    }
}
