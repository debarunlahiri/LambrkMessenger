package com.lahiriproductions.lambrk_messenger.Settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.lahiriproductions.lambrk_messenger.BuildConfig;
import com.lahiriproductions.lambrk_messenger.R;
import com.lahiriproductions.lambrk_messenger.WebViewActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AboutActivity extends AppCompatActivity {

    private Toolbar abouttoolbar;
    private TextView textView7, tvBurnabAboutVersion, tvUserId, tvAboutPrivacy, tvAboutTerms;
    private Button bCrash;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    private String user_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        abouttoolbar = findViewById(R.id.abouttoolbar);
        abouttoolbar.setTitle("About");
        //abouttoolbar.setTitleTextColor(Color.WHITE);
        //abouttoolbar.setBackgroundColor(Color.BLACK);

        setSupportActionBar(abouttoolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        abouttoolbar.setNavigationIcon(getResources().getDrawable(R.mipmap.black_back));
        abouttoolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AboutActivity.super.onBackPressed();
            }
        });

        textView7 = findViewById(R.id.tvLoginForgortPassword);
        tvUserId = findViewById(R.id.tvUserId);
        tvBurnabAboutVersion = findViewById(R.id.tvAddStoryDone);
        tvAboutPrivacy = findViewById(R.id.tvAboutPrivacy);
        tvAboutTerms = findViewById(R.id.tvAboutTerms);
        bCrash = findViewById(R.id.bCrash);
        //textView7.setShadowLayer(11, 3, 6, Color.GRAY);

        mDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_url)).getReference();;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl("gs://lambrk-messenger-74403.appspot.com");

        user_id = currentUser.getUid();

        tvBurnabAboutVersion.setText("v" + BuildConfig.VERSION_NAME);
        tvUserId.setText(user_id);

        tvAboutPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent webIntent = new Intent(AboutActivity.this, WebViewActivity.class);
                webIntent.putExtra("name", "Privacy Policy");
                webIntent.putExtra("url", "https://lahiriproductions.com/quirky/privacy.html");
                startActivity(webIntent);
            }
        });

        tvAboutTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent webIntent = new Intent(AboutActivity.this, WebViewActivity.class);
                webIntent.putExtra("name", "Terms & Conditions");
                webIntent.putExtra("url", "https://lahiriproductions.com/quirky/terms.html");
                startActivity(webIntent);
            }
        });

//        forceCrash(bCrash);
    }

//    public void forceCrash(View view) {
//        throw new RuntimeException("This is a crash");
//    }

}
