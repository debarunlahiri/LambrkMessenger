package com.lambrk.messenger.SetupUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.lambrk.messenger.MainActivity;
import com.lambrk.messenger.R;
import com.lambrk.messenger.StartActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.lambrk.messenger.Utils.Variables;

import java.util.Date;
import java.util.HashMap;

public class SetupActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Toolbar setuptoolbar;

    private EditText etDialogAge, etDialogUsername;
    private Button bDialogSaveDetails;
    private ProgressBar pbDialogUserDetails;
    private Spinner setupusergender;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    public String ds_username, name, profile_image, thumb_profile_image;
    private String gender;
    private String user_id;
    private long acc_create_timestamp;
    public boolean hasUsername = false;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        sharedPreferences = getSharedPreferences("setupUser", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        if (sharedPreferences.contains("name")) {
            name = sharedPreferences.getString("name", "");
            profile_image = sharedPreferences.getString("profile_image", "");
            thumb_profile_image = sharedPreferences.getString("thumb_profile_image", "");
            acc_create_timestamp = sharedPreferences.getLong("acc_create_timestamp", 0);
            Variables.token_id = sharedPreferences.getString("token_id", "");
        }

        setuptoolbar = findViewById(R.id.setuptoolbar);
        setuptoolbar.setTitle("Setup User");
        setSupportActionBar(setuptoolbar);

        etDialogAge = findViewById(R.id.etDialogAge);
        etDialogUsername = findViewById(R.id.etDialogUsername);
        bDialogSaveDetails = findViewById(R.id.bDialogSaveDetails);
        pbDialogUserDetails = findViewById(R.id.pbDialogUserDetails);
        setupusergender = findViewById(R.id.setupusergender);

        mDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_url)).getReference();;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl(getString(R.string.storage_reference_url));


        final ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(SetupActivity.this, R.array.gender, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        setupusergender.setAdapter(genderAdapter);
        setupusergender.setOnItemSelectedListener(SetupActivity.this);

        pbDialogUserDetails.setVisibility(View.GONE);

        mDatabase.child("users").child(currentUser.getUid()).child("user_data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("age").exists()) {
                        etDialogAge.setText(dataSnapshot.child("age").getValue().toString());
                    }

                    if (dataSnapshot.child("gender").exists()) {
                        int position = genderAdapter.getPosition(dataSnapshot.child("gender").getValue().toString());
                        setupusergender.setSelection(position);
                    }

                    if (dataSnapshot.child("username").exists()) {
                        etDialogUsername.setText(dataSnapshot.child("username").getValue().toString());
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        bDialogSaveDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pbDialogUserDetails.setVisibility(View.VISIBLE);
                bDialogSaveDetails.setVisibility(View.GONE);
                String age = etDialogAge.getText().toString();
                String username = etDialogUsername.getText().toString();

                if (age.isEmpty()) {
                    etDialogAge.setError("Please enter your age");
                    pbDialogUserDetails.setVisibility(View.GONE);
                    bDialogSaveDetails.setVisibility(View.VISIBLE);
                } else if (username.isEmpty()) {
                    etDialogUsername.setError("Please enter your username");
                    pbDialogUserDetails.setVisibility(View.GONE);
                    bDialogSaveDetails.setVisibility(View.VISIBLE);
                } else if (Integer.parseInt(age) < 13) {
                    Toast.makeText(getApplicationContext(), "Registration Failed", Toast.LENGTH_LONG).show();
                } else if (username.contains(" ")) {
                    Toast.makeText(getApplicationContext(), "No spaces allowed in username", Toast.LENGTH_LONG).show();
                    pbDialogUserDetails.setVisibility(View.GONE);
                    bDialogSaveDetails.setVisibility(View.VISIBLE);
                } else if (gender.equals("Select Gender")) {
                    Toast.makeText(getApplicationContext(), "Please select your gender", Toast.LENGTH_LONG).show();
                    pbDialogUserDetails.setVisibility(View.GONE);
                    bDialogSaveDetails.setVisibility(View.VISIBLE);
                } else {
                    mDatabase.child("usernames").child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                hasUsername = true;
                                Toast.makeText(getApplicationContext(), "Username already exists", Toast.LENGTH_LONG).show();
                                etDialogUsername.requestFocus();
                                pbDialogUserDetails.setVisibility(View.GONE);
                                bDialogSaveDetails.setVisibility(View.VISIBLE);
                            } else {
                                hasUsername = false;
                                //Toast.makeText(getApplicationContext(), "insert username", Toast.LENGTH_LONG).show();
                                //mDatabase.child("usernames").child("username").setValue(username);
                                HashMap<String, Object> mUserDataMap = new HashMap<>();
                                mUserDataMap.put("age", age);
                                mUserDataMap.put("bio", "");
                                mUserDataMap.put("gender", gender);
                                mUserDataMap.put("username", username);
                                mUserDataMap.put("name", name);
                                mUserDataMap.put("profile_image", profile_image);
                                mUserDataMap.put("thumb_profile_image", thumb_profile_image);
                                mUserDataMap.put("acc_create_timestamp", acc_create_timestamp);

                                mDatabase.child("users").child(currentUser.getUid()).child("user_data").updateChildren(mUserDataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            mDatabase.child("users").child(currentUser.getUid()).child("user_id").setValue(currentUser.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        mDatabase.child("users").child(currentUser.getUid()).child("token_id").setValue(Variables.token_id).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    mDatabase.child("usernames").child(username).child("username").setValue(username).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                mDatabase.child("users").child(currentUser.getUid()).child("username").setValue(username).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()) {
                                                                                            mDatabase.child("users").child(currentUser.getUid()).child("user_data").child("age_change_time_period").setValue(new Date(System.currentTimeMillis() + 14L * 24 * 60 * 60 * 1000));
                                                                                            new Handler().postDelayed(new Runnable() {
                                                                                                @Override
                                                                                                public void run() {
                                                                                                    editor.clear();
                                                                                                    editor.apply();
                                                                                                    Toast.makeText(getApplicationContext(), "Profile created successfully", Toast.LENGTH_LONG).show();
                                                                                                    Intent finalSetupUserIntent = new Intent(SetupActivity.this, MainActivity.class);
                                                                                                    startActivity(finalSetupUserIntent);
                                                                                                    finishAffinity();
                                                                                                }
                                                                                            }, 1000);
                                                                                        }
                                                                                    }
                                                                                });
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        });

                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        gender = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.setup_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout_setup_menu_list_item) {
            mAuth.signOut();
            Intent setupUserIntent = new Intent(SetupActivity.this, StartActivity.class);
            setupUserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(setupUserIntent);
            finishAffinity();
        }
        return true;
    }
}
