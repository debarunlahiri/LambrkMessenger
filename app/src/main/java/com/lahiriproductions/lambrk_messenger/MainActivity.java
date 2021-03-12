package com.lahiriproductions.lambrk_messenger;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.lahiriproductions.lambrk_messenger.Explore.ExploreFragment;
import com.lahiriproductions.lambrk_messenger.Group.GroupFragment;
import com.lahiriproductions.lambrk_messenger.Inbox.InboxFragment;
import com.lahiriproductions.lambrk_messenger.SetupUser.SetupActivity;
import com.lahiriproductions.lambrk_messenger.Stories.StoriesFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    AHBottomNavigation bottomNavigation;
    private FrameLayout main_frame;

    private InboxFragment inboxFragment;
    private GroupFragment groupFragment;
    private StoriesFragment storiesFragment;
    private ExploreFragment exploreFragment;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser = null;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;
    private ValueEventListener valueEventListener, valueEventListener1;

    private String user_id;

    private MediaPlayer mMediaPlayer;
    private int currentPosition = 0;
    private VideoView videoView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);


        bottomNavigation = (AHBottomNavigation) findViewById(R.id.mainAHBottomNavigation2);
        main_frame = findViewById(R.id.main_frame);

        mDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_url)).getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl(getString(R.string.storage_reference_url));

        if (currentUser == null) {
            sendToLogin();
        } else {
            user_id = currentUser.getUid();
            mDatabase.child("users").child(user_id).child("user_id").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        mDatabase.child("users").child(user_id).child("user_id").setValue(user_id);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            mDatabase.child("users").child(user_id).child("user_data").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.child("profile_image").exists() && !dataSnapshot.child("username").exists() && !dataSnapshot.child("name").exists() &&
                            !dataSnapshot.child("gender").exists()) {
                        Intent setupUserIntent = new Intent(MainActivity.this, StartActivity.class);
                        setupUserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(setupUserIntent);
                        finish();
                        mAuth.signOut();
                        Toast.makeText(getApplicationContext(), "Login Again", Toast.LENGTH_LONG).show();
                    } else {
                        if (!dataSnapshot.child("age").exists()) {
                            Intent setupUser2Intent = new Intent(MainActivity.this, SetupActivity.class);
                            startActivity(setupUser2Intent);
                            finish();
//                            Toast.makeText(getApplicationContext(), "Enter your age", Toast.LENGTH_LONG).show();
                        } else if (!dataSnapshot.child("username").exists()) {
                            Intent setupUser3Intent = new Intent(MainActivity.this, SetupActivity.class);
                            startActivity(setupUser3Intent);
                            finish();
//                            Toast.makeText(getApplicationContext(), "Enter your username", Toast.LENGTH_LONG).show();
                        } else if (!dataSnapshot.child("gender").exists()) {
                            Intent setupUser3Intent = new Intent(MainActivity.this, SetupActivity.class);
                            startActivity(setupUser3Intent);
                            finish();
//                            Toast.makeText(getApplicationContext(), "Please select your gender", Toast.LENGTH_LONG).show();
                        } else {
                            mDatabase.child("users").child(user_id).child("privacy").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.exists()) {
                                        HashMap<String, Object> mPrivacyDataMap = new HashMap<>();
                                        mPrivacyDataMap.put("hide_following", false);
                                        mPrivacyDataMap.put("hide_joined_groups", false);
                                        mPrivacyDataMap.put("hide_created_groups", false);
                                        mPrivacyDataMap.put("private_profile", false);
                                        mDatabase.child("users").child(user_id).child("privacy").updateChildren(mPrivacyDataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(getApplicationContext(), "Privacy data set", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(getApplicationContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        beginFragmentTransaction();

        //Toast.makeText(getApplicationContext(), String.valueOf(height) + "x" + String.valueOf(width), Toast.LENGTH_LONG).show();
    }

    private void beginFragmentTransaction() {
        // Create items
        AHBottomNavigationItem item1 = new AHBottomNavigationItem("Home", R.drawable.home, R.color.md_black_1000);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem("Group", R.drawable.group, R.color.md_black_1000);
//        AHBottomNavigationItem item3 = new AHBottomNavigationItem("Explore", R.drawable.ic_explore_black_24dp, R.color.md_black_1000);
        // Add items
        bottomNavigation.addItem(item1);
        bottomNavigation.addItem(item2);
//        bottomNavigation.addItem(item3);

        // Set background color
        bottomNavigation.setDefaultBackgroundColor(Color.WHITE);

        // Change colors
        bottomNavigation.setAccentColor(Color.BLACK);
        bottomNavigation.setInactiveColor(Color.GRAY);

        // Force to tint the drawable (useful for font with icon for example)
        bottomNavigation.setForceTint(true);

        // Manage titles
        //bottomNavigation.setTitleState(AHBottomNavigation.TitleState.SHOW_WHEN_ACTIVE);
        //bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_HIDE);

        // Set current item programmatically
        bottomNavigation.setCurrentItem(0);

        // Customize notification (title, background, typeface)
        bottomNavigation.setNotificationBackgroundColor(Color.parseColor("#F63D2B"));

        // Add or remove notification for each item
        //bottomNavigation.setNotification("0", 2);

        // Enable / disable item & set disable color
        //bottomNavigation.enableItemAtPosition(2);
        //bottomNavigation.disableItemAtPosition(1);
        //bottomNavigation.setItemDisableColor(Color.parseColor("#3A000000"));

        inboxFragment = new InboxFragment();
        groupFragment = new GroupFragment();
        storiesFragment = new StoriesFragment();
        exploreFragment = new ExploreFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment[] active = {inboxFragment};

        fragmentManager.beginTransaction().add(R.id.main_frame, inboxFragment, "1").commit();
        fragmentManager.beginTransaction().add(R.id.main_frame, groupFragment, "2").hide(groupFragment).commit();
//        fragmentManager.beginTransaction().add(R.id.main_frame, exploreFragment, "3").hide(exploreFragment).commit();

        String notificationIntent = getIntent().getStringExtra("messageNotification");

        if (notificationIntent != null) {

            // Here we can decide what do to -- perhaps load other parameters from the intent extras such as IDs, etc
            if (notificationIntent.equals("messageNotification")) {
                fragmentManager.beginTransaction().hide(active[0]).show(inboxFragment).commit();
                active[0] = inboxFragment;
            }
        }

        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                switch (position) {

                    case 0:
                        fragmentManager.beginTransaction().hide(active[0]).show(inboxFragment).commit();
                        active[0] = inboxFragment;
                        return true;

                    case 1:
                        fragmentManager.beginTransaction().hide(active[0]).show(groupFragment).commit();
                        active[0] = groupFragment;
                        return true;

//                    case 2:
//                        fragmentManager.beginTransaction().hide(active[0]).show(exploreFragment).commit();
//                        active[0] = exploreFragment;
//                        return true;

                    default:
                        return false;
                }
            }
        });
    }

    private void sendToLogin() {
        Intent loginIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(loginIntent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


}
