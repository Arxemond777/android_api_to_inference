package com.example.myapplication.components.internet;

import android.os.AsyncTask;
import android.util.Log;

import com.example.myapplication.components.internet.http.ParseDataFromResponse;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.example.myapplication.config.Config.ADD_SERVER_FOR_UPLOAD;


/**
 * Upload the fetch img from a camera to a server
 */
public class UploadFileToServerTask extends AsyncTask<String, String, Object> {
    private final static String TAG = "img/";
    @Override
    protected String doInBackground(String... args) {

        Log.d(TAG, "start >> ");
        try {
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            @SuppressWarnings("PointlessArithmeticExpression")
            int maxBufferSize = 1 * 1024 * 1024;


            java.net.URL url = new URL(ADD_SERVER_FOR_UPLOAD);
            Log.d(TAG, "url >> " + url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Allow Inputs &amp; Outputs.
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            // Set HTTP method to POST.
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            FileInputStream fileInputStream;
            DataOutputStream outputStream;
            {
                outputStream = new DataOutputStream(connection.getOutputStream());

                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                String filename = args[0];
                outputStream.writeBytes("Content-Disposition: form-data; name=\"image\";filename=\"" + filename + "\"" + lineEnd);
                outputStream.writeBytes(lineEnd);
                Log.d(TAG, "filename " + filename);

                fileInputStream = new FileInputStream(filename);

                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);

                buffer = new byte[bufferSize];

                // Read file
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    outputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            }

            int serverResponseCode = connection.getResponseCode();
            String serverResponseMessage = connection.getResponseMessage();
            Log.d("serverResponseCode", "" + serverResponseCode);
            Log.d("serverResponseMessage", "" + serverResponseMessage);

            fileInputStream.close();
            outputStream.flush();
            outputStream.close();


            if (serverResponseCode >= 200 && serverResponseCode <= 299) {


                return new ParseDataFromResponse(connection).printResultFromTheServer();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "false";
    }

    @Override
    protected void onPostExecute(Object result) {

    }
}
