package com.lambrk.messenger.Settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.lambrk.messenger.R;
import com.lambrk.messenger.StartActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar settingstoolbar;

    private CardView generalSettingsCV, privacySettingsCV, aboutSettingsCV, logoutSettingsCV;
    private TextView textView4, tvUserId;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    private String user_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settingstoolbar = findViewById(R.id.settingstoolbar);
        settingstoolbar.setTitle("Settings");
        setSupportActionBar(settingstoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        settingstoolbar.setNavigationIcon(getResources().getDrawable(R.mipmap.black_back));
        settingstoolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        generalSettingsCV = findViewById(R.id.generalSettingsCV);
        privacySettingsCV = findViewById(R.id.privacySettingsCV);
        logoutSettingsCV = findViewById(R.id.logoutSettingsCV);
        aboutSettingsCV = findViewById(R.id.aboutSettingsCV);
        textView4 = findViewById(R.id.textView4);
        tvUserId = findViewById(R.id.tvUserId);

        mDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_url)).getReference();;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl(getString(R.string.storage_reference_url));

        user_id = currentUser.getUid();

        aboutSettingsCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent aboutIntent = new Intent(SettingsActivity.this, AboutActivity.class);
                startActivity(aboutIntent);
            }
        });

        generalSettingsCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent generalIntent = new Intent(SettingsActivity.this, GeneralSettingsActivity.class);
                startActivity(generalIntent);
            }
        });

        privacySettingsCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent privacyIntent = new Intent(SettingsActivity.this, PrivacySettingsActivity.class);
                startActivity(privacyIntent);
            }
        });

        logoutSettingsCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabase.child("users").child(user_id).child("token_id").removeValue();
                mAuth.signOut();
                Intent loginIntent = new Intent(SettingsActivity.this, StartActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginIntent);
                finish();
            }
        });

        tvUserId.setText(user_id);


    }
}