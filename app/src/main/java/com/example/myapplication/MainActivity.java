package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;

import com.example.myapplication.components.UploadFileToServerTask;
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
    private final static String  TAG = ":( >>>>>";


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

        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_CAMERA);
        } else { // get the WRITE_EXTERNAL_STORAGE permition at first time
            String[] permissions = {"android.permission.WRITE_EXTERNAL_STORAGE"};
            requestPermissions(permissions, REQUEST_CAMERA);
        }


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
            System.out.println(">>>>>>>>>Environment.getExternalStorageDirectory()" + Environment.getExternalStorageDirectory());
            File destination = new File(Environment.getExternalStorageDirectory(), picNameHash);
            FileOutputStream fo;
            try {
                fo = new FileOutputStream(destination);
                fo.write(bytes.toByteArray());
                fo.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }

            final AsyncTask<String, String, Object> execute = new UploadFileToServerTask().execute(destination.getAbsolutePath());

            try {
                final Object o = execute.get();

                System.out.println("+++++++");
                System.out.println(o);

                if (!o.toString().equals("false"))
                    printResult((String) o);
                else {
                    Log.e(TAG, "something went wrong");
                }

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
