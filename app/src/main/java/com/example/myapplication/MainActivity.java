package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.myapplication.components.internet.UploadFileToServerTask;
import com.example.myapplication.components.ui.DrawUI;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class MainActivity extends Activity implements OnClickListener {
    static final int REQUEST_CAMERA = 1;
    public final static String  TAG = ":( >>>>>";
    private static final String PIC_NAME = "temp.jpeg";
    private String picNameHash = PIC_NAME;

    public void openBrowser(View view) {

        //Get url from tag
        String url = (String)view.getTag();

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);

        //pass the url to intent data
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button mTakePhoto = findViewById(R.id.takeAPhoto);
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

    // get image from the camera
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAMERA) {
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

            picNameHash = UUID.randomUUID() + PIC_NAME;
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

                if (!o.toString().equals("false"))
                    new DrawUI(this, picNameHash).printResult((String) o);
                else {
                    Log.e(TAG, "something went wrong");
                }

            } catch (Exception e) {
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

}
