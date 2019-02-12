package com.example.myapplication.components.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.widget.ImageView;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Visualization
 */
public class DrawUI {
    private final MainActivity mainActivity;
    private final String picNameHash;

    public DrawUI(MainActivity mainActivity, String picNameHash) {
        this.mainActivity = mainActivity;
        this.picNameHash = picNameHash;
    }

    private ImageView[] getImgsViewsForResult() {
        ImageView ivBasicImage1 = mainActivity.findViewById(R.id.image_view_1);
        ImageView ivBasicImage2 = mainActivity.findViewById(R.id.image_view_2);
        ImageView ivBasicImage3 = mainActivity.findViewById(R.id.image_view_3);
        ImageView ivBasicImage4 = mainActivity.findViewById(R.id.image_view_4);

        ImageView[] myImageViewArray = new ImageView[]{ivBasicImage1, ivBasicImage2, ivBasicImage3, ivBasicImage4};

        Stream.of(myImageViewArray).forEach(idx -> {
            idx.setImageResource(android.R.color.transparent); // reset old imgs
            idx.setTag("");
        });

        return myImageViewArray;
    }

    public void printResult(String urls) {
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(urls);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final ImageView[] imgsViewsForResult = getImgsViewsForResult();
        final JSONArray finalJsonArray = jsonArray;
        IntStream.range(0, jsonArray.length())
                .forEach(idx -> {
                    JSONObject jsonObject = finalJsonArray.optJSONObject(idx);

                    ImageView currImageView = imgsViewsForResult[idx];
                    Picasso.with(mainActivity)
                            .load(jsonObject.optString("url")) // url img for the interface
                            .into(currImageView);

                    currImageView.setTag(jsonObject.optString("urlToAdvert")); // href for which redirect is executed by click a photo event
                });

        printNewPhoto();
    }

    /**
     * print capturing photo
     */
    void printNewPhoto() {
        ImageView myPhoto = mainActivity.findViewById(R.id.my_photo);
        final String pathToMyPhoto = Environment.getExternalStorageDirectory() + "/" + picNameHash;
        File imgFile = new  File(pathToMyPhoto);
        if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            myPhoto.setImageBitmap(myBitmap);
        }
    }
}
