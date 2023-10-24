package com.lambrk.messenger.SetupUser;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.lambrk.messenger.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class SetupFinalActivity extends AppCompatActivity {

    private ProgressBar setupfinalPB;
    private TextView tvSetupFinalMessage;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    private String user_id;
    private String name, gender, bio, stringProfileImageURI, username, age;
    private Uri profileImageUri = null;
    private Bitmap mCompressedProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_final);

        Bundle bundle = getIntent().getExtras();
        name = bundle.get("name").toString();
        gender = bundle.get("gender").toString();
        username = bundle.get("username").toString();
        age = bundle.get("age").toString();
        bio = bundle.get("bio").toString();
        stringProfileImageURI = bundle.get("profileImageURI").toString();
        profileImageUri = Uri.parse(stringProfileImageURI);
        Toast.makeText(getApplicationContext(), stringProfileImageURI, Toast.LENGTH_LONG).show();
        setupfinalPB = findViewById(R.id.setupfinalPB);
        tvSetupFinalMessage = findViewById(R.id.tvSetupFinalMessage);

        mDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_url)).getReference();;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl(getString(R.string.storage_reference_url));

        user_id = currentUser.getUid();

        uploadData();
    }

    private void uploadData() {

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Toast.makeText(getApplicationContext(), "Saving user details. Cannot go back", Toast.LENGTH_LONG).show();
    }
}
