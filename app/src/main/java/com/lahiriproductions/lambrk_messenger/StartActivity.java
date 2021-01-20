package com.lahiriproductions.lambrk_messenger;

import android.app.Dialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.lahiriproductions.lambrk_messenger.SetupUser.SetupActivity;
import com.lahiriproductions.lambrk_messenger.Utils.Variables;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;
import java.util.HashMap;

public class StartActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final int RC_SIGN_IN = 1;
    private static final String TAG = "StartActivity";
    private VideoView videoView;
    private Button startloginbutton;
    private TextView startregisterbutton;
    private SignInButton bGoogleSignIn;
    private Spinner setupusergender;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    private MediaPlayer mMediaPlayer;
    private int currentPosition = 0;
    private GoogleSignInClient mGoogleSignInClient;
    private String ds_username;
    private String gender;
    private String user_id;

    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private FirebaseAuth.AuthStateListener authStateListener;
    private AccessTokenTracker accessTokenTracker;

    private static final String FACEBOOK_EMAIL = "email";
    private static final String FACEBOOK_ID = "id";
    private static final String FACEBOOK_NAME = "name";
    private static final String FACEBOOK_PICTURE = "picture";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        FacebookSdk.sdkInitialize(StartActivity.this);
//        AppEventsLogger.activateApp(StartActivity.this);
        callbackManager = CallbackManager.Factory.create();

        mDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_url)).getReference();;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl("gs://lambrk-messenger-74403.appspot.com");

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        startloginbutton = findViewById(R.id.startloginbutton);
        startregisterbutton = findViewById(R.id.startregisterbutton);
        bGoogleSignIn = findViewById(R.id.bGoogleSignIn);
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(FACEBOOK_EMAIL);
        loginButton.setLoginBehavior( LoginBehavior.WEB_ONLY );
        bGoogleSignIn.setSize(SignInButton.SIZE_WIDE);

        startloginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });

        startregisterbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        });

        bGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (task.isSuccessful()) {
                    Variables.token_id = task.getResult().getToken();
                }
            }
        });

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                handleFacebookToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                // App code
                Log.d(TAG, "Facebook login cancelled");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.d(TAG, "facebookLogin: " + exception.getMessage());
            }
        });

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    updateUI(firebaseUser);
                }
            }
        };

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    mAuth.signOut();
                }
            }
        };

    }

    private void handleFacebookToken(AccessToken accessToken) {
        AuthCredential authCredential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    updateUI(firebaseUser);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(getApplicationContext(), "Google sign in failed", Toast.LENGTH_LONG).show();
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            FirebaseInstanceId.getInstance().getToken();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "\"Authentication Failed.", Toast.LENGTH_LONG).show();
//                            Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user == null) {
            Toast.makeText(getApplicationContext(), "Sign In failed", Toast.LENGTH_LONG).show();
        } else {
            Log.d(TAG, "Sign in user details" +
                    "\nUser ID: " + user.getUid() +
                    "\nPhoto Url: " + user.getPhotoUrl() +
                    "\nProvider ID: " + user.getProviderId() +
                    "\nName: " + user.getDisplayName() +
                    "\nEmail: " + user.getEmail());
            user_id = user.getUid();
            mDatabase.child("users").child(user_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        mDatabase.child("users").child(user_id).child("token_id").setValue(Variables.token_id);
                        Intent mainIntent = new Intent(StartActivity.this, MainActivity.class);
                        startActivity(mainIntent);
                        finish();
                    } else {
                        Dialog dialog = new Dialog(StartActivity.this);
                        dialog.setContentView(R.layout.dialog_signup_details_layout);
                        dialog.getWindow().setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
                        dialog.setCancelable(false);
                        dialog.setCanceledOnTouchOutside(false);

                        EditText etDialogAge = dialog.findViewById(R.id.etDialogAge);
                        EditText etDialogUsername = dialog.findViewById(R.id.etDialogUsername);
                        Button bDialogSaveDetails = dialog.findViewById(R.id.bDialogSaveDetails);
                        ProgressBar pbDialogUserDetails = dialog.findViewById(R.id.pbDialogUserDetails);
                        Spinner setupusergender = dialog.findViewById(R.id.setupusergender);

                        final ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(StartActivity.this, R.array.gender, android.R.layout.simple_spinner_item);
                        genderAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
                        setupusergender.setAdapter(genderAdapter);
                        setupusergender.setOnItemSelectedListener(StartActivity.this);

                        pbDialogUserDetails.setVisibility(View.GONE);

                        HashMap<String, Object> mUserDataMap = new HashMap<>();
                        mUserDataMap.put("bio", "");
                        mUserDataMap.put("name", user.getDisplayName());
                        mUserDataMap.put("profile_image", user.getPhotoUrl().toString());
                        mUserDataMap.put("thumb_profile_image", user.getPhotoUrl().toString());
                        mUserDataMap.put("acc_create_timestamp", user.getMetadata().getCreationTimestamp());

                        mDatabase.child("users").child(user.getUid()).child("user_data").updateChildren(mUserDataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    mDatabase.child("users").child("user_id").setValue(user_id);
                                    mDatabase.child("users").child(user_id).child("token_id").setValue(Variables.token_id);
//                                                mDatabase.child("usernames").push().child("username").setValue(username);
//                                    mDatabase.child("users").child(user_id).child("username").setValue(username);
                                    mDatabase.child("users").child(user.getUid()).child("user_data").child("age_change_time_period").setValue(new Date(System.currentTimeMillis()+14L * 24 * 60 * 60 * 1000));
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
//                                            Toast.makeText(getApplicationContext(), "Profile created successfully", Toast.LENGTH_LONG).show();
                                            Intent finalSetupUserIntent = new Intent(StartActivity.this, SetupActivity.class);
                                            startActivity(finalSetupUserIntent);
                                            finish();
                                        }
                                    }, 500);
                                }
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
                                    dialog.dismiss();
                                } else if (username.contains(" ")) {
                                    Toast.makeText(getApplicationContext(), "No spaces allowed in username", Toast.LENGTH_LONG).show();
                                    pbDialogUserDetails.setVisibility(View.GONE);
                                    bDialogSaveDetails.setVisibility(View.VISIBLE);
                                } else if (gender.equals("Select Gender")) {
                                    Toast.makeText(getApplicationContext(), "Please select your gender", Toast.LENGTH_LONG).show();
                                    pbDialogUserDetails.setVisibility(View.GONE);
                                    bDialogSaveDetails.setVisibility(View.VISIBLE);
                                } else {

                                    /*
                                    mDatabase.child("usernames").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                    if (ds.child("username").getValue().equals(username)) {
                                                        ds_username = ds.child("username").getValue().toString();
                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                    if (ds_username != null &&  ds_username.equals(username)) {
                                        Toast.makeText(getApplicationContext(), "Username already exists", Toast.LENGTH_LONG).show();
                                        etDialogUsername.requestFocus();
                                        pbDialogUserDetails.setVisibility(View.GONE);
                                        bDialogSaveDetails.setVisibility(View.VISIBLE);
                                    } else {
                                        //Toast.makeText(getApplicationContext(), "insert username", Toast.LENGTH_LONG).show();
                                        //mDatabase.child("usernames").child("username").setValue(username);
                                        HashMap<String, Object> mUserDataMap = new HashMap<>();
                                        mUserDataMap.put("age", "age");
                                        mUserDataMap.put("bio", "");
                                        mUserDataMap.put("gender", "");
                                        mUserDataMap.put("name", user.getDisplayName());
                                        mUserDataMap.put("username", username);
                                        mUserDataMap.put("profile_image", user.getPhotoUrl().toString());
                                        mUserDataMap.put("thumb_profile_image", user.getPhotoUrl().toString());
                                        mUserDataMap.put("acc_create_timestamp", user.getMetadata().getCreationTimestamp());

                                        mDatabase.child("users").child(user.getUid()).child("user_data").setValue(mUserDataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    mDatabase.child("users").child("user_id").setValue(user_id);
                                                    mDatabase.child("usernames").push().child("username").setValue(username);
                                                    mDatabase.child("users").child(user_id).child("username").setValue(username);
                                                    mDatabase.child("users").child(user.getUid()).child("user_data").child("age_change_time_period").setValue(new Date(System.currentTimeMillis()+14L * 24 * 60 * 60 * 1000));
                                                    new Handler().postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(getApplicationContext(), "Profile created successfully", Toast.LENGTH_LONG).show();
                                                            Intent finalSetupUserIntent = new Intent(StartActivity.this, SetupActivity.class);
                                                            startActivity(finalSetupUserIntent);
                                                            finish();
                                                        }
                                                    }, 2500);
                                                }
                                            }
                                        });
                                    }

                                     */
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


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        gender = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
    }
}
