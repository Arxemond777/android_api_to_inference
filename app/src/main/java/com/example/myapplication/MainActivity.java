package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class MainActivity extends Activity implements OnClickListener {
    public static final String ADD_SERVER_FOR_UPLOAD = "http://arxemond.ru/server_for_android/receive_an_img.php";
    static final int REQUEST_CAMERA = 1;

    private Button mTakePhoto;
    private WebView mWebView;

    private String TAG;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mTakePhoto = findViewById(R.id.takeAPhoto);
        mTakePhoto.setOnClickListener(this);

    }

    // invoke the camera
    @Override
    public void onClick(View v) {
//        String[] permissions = {"android.permission.WRITE_EXTERNAL_STORAGE"};
//        requestPermissions(permissions, REQUEST_CAMERA);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private static final String PIC_NAME = "temp.jpg";
    String picNameHash = PIC_NAME;
    private String getFullPathToPic () {
        return "http://arxemond.ru/server_for_android/img/" + picNameHash;
    }

    // get image from the camera
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAMERA) {
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

            picNameHash = UUID.randomUUID() + PIC_NAME;
            System.out.println(">>>>>>>>>new val " + picNameHash);
            System.out.println(">>>>>>>>>new val  fullPathToPic" + getFullPathToPic());
            File destination = new File(Environment.getExternalStorageDirectory(), picNameHash);
            FileOutputStream fo;
            try {
                fo = new FileOutputStream(destination);
                fo.write(bytes.toByteArray());
                fo.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            final AsyncTask<String, String, Object> execute = new UploadFileToServerTask().execute(destination.getAbsolutePath());

            try {
                final Object o = execute.get();

                System.out.println("+++++++");
                System.out.println(o);

                printResult((String) o);

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Upload the fetch img from a camera to a server
     */
    private class UploadFileToServerTask extends AsyncTask<String, String, Object> {
        private final static String TAG = "img/";

        @Override
        protected String doInBackground(String... args) {
            try {
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                @SuppressWarnings("PointlessArithmeticExpression")
                int maxBufferSize = 1 * 1024 * 1024;


                java.net.URL url = new URL(ADD_SERVER_FOR_UPLOAD);
                Log.d(TAG, "url " + url);
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


                    return printResultFromTheServer(connection);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_CAMERA);
        }
    }

    private String printResultFromTheServer(HttpURLConnection connection) throws IOException, JSONException {
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
            System.err.println("FAAAACCCK");
            System.err.println(json.get("status"));
            throw new Error("FAAAACCCK");
        }
    }

    private void printResult(String urls) {
        ImageView ivBasicImage1 = findViewById(R.id.image_view_1);
        ImageView ivBasicImage2 = findViewById(R.id.image_view_2);
        ImageView ivBasicImage3 = findViewById(R.id.image_view_3);
        ImageView ivBasicImage4 = findViewById(R.id.image_view_4);
        ImageView ivBasicImage5 = findViewById(R.id.image_view_5);

        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(urls);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String[] arr=new String[jsonArray.length()];
        for(int i=0; i<arr.length; i++)
            arr[i]=jsonArray.optString(i);


        ImageView my_photo = findViewById(R.id.my_photo);
//        Picasso.with(this).load("http://arxemond.ru/server_for_android/img/temp.jpg").into(my_photo);
        System.out.println(">>>>>>>>>new val " + picNameHash);
        System.out.println(">>>>>>>>>new val  fullPathToPic" + getFullPathToPic());
        Picasso.with(this).load(getFullPathToPic()).into(my_photo);

        Picasso.with(this).load(arr[0]).into(ivBasicImage1);
        Picasso.with(this).load(arr[1]).into(ivBasicImage2);
        Picasso.with(this).load(arr[2]).into(ivBasicImage3);
        Picasso.with(this).load(arr[3]).into(ivBasicImage4);
        Picasso.with(this).load(arr[4]).into(ivBasicImage5);
    }

}
