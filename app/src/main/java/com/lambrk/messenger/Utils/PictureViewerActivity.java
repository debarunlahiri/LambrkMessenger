package com.lambrk.messenger.Utils;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.lambrk.messenger.R;
import com.jsibbold.zoomage.ZoomageView;

public class PictureViewerActivity extends AppCompatActivity {

    private ZoomageView pictureviewerIV;

    private String post_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_viewer);

        Bundle bundle = getIntent().getExtras();
        post_image = bundle.get("post_image").toString();

        pictureviewerIV = findViewById(R.id.pictureviewerIV);

        Glide.with(getApplicationContext()).load(post_image).into(pictureviewerIV);
    }
}
