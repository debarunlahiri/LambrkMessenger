package com.lahiriproductions.lambrk_messenger;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
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

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.lahiriproductions.lambrk_messenger.Explore.ExploreFragment;
import com.lahiriproductions.lambrk_messenger.Group.GroupFragment;
import com.lahiriproductions.lambrk_messenger.Inbox.InboxFragment;
import com.lahiriproductions.lambrk_messenger.SetupUser.SetupActivity;
import com.lahiriproductions.lambrk_messenger.Shortcuts.ShortcutsFragment;
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
import com.lahiriproductions.lambrk_messenger.Utils.Variables;


import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
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
    private FirebaseFirestore firebaseFirestore;

    private String user_id;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ShortcutsFragment shortcutsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        MobileAds.initialize(this);


        sharedPreferences = getSharedPreferences("setupUser", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        main_frame = findViewById(R.id.main_frame);

        mDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_url)).getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl(getString(R.string.storage_reference_url));
        firebaseFirestore = FirebaseFirestore.getInstance();

        if (currentUser == null) {
            sendToLogin();
        } else {
            firebaseFirestore.collection("constants").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            boolean is_force_update_promt = (boolean) queryDocumentSnapshot.getData().get("force_update_promt");
                            if (is_force_update_promt) {
                                Intent updateIntent = new Intent(MainActivity.this, UpdateAppActivity.class);
                                startActivity(updateIntent);
                                finishAffinity();
                            }
                        }
                    }
                }
            });
            user_id = currentUser.getUid();
            mDatabase.child("users").child(user_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                if (task.isSuccessful()) {
                                    Variables.token_id = task.getResult().getToken();
                                    mDatabase.child("users").child(user_id).child("token_id").setValue(Variables.token_id);
                                }
                            }
                        });

                        mDatabase.child("users").child(user_id).child("privacy").addListenerForSingleValueEvent(new ValueEventListener() {
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
                        beginFragmentTransaction();
                    } else if (sharedPreferences.contains("name")) {
                        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                        startActivity(setupIntent);
                        finishAffinity();
                    } else {
                        mAuth.signOut();
                        Intent setupUserIntent = new Intent(MainActivity.this, StartActivity.class);
                        setupUserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(setupUserIntent);
                        finishAffinity();
                        Toast.makeText(getApplicationContext(), "Login Again", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        startActivity(new Intent(this, BillingActivity.class));
    }

    private void beginFragmentTransaction() {

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        inboxFragment = new InboxFragment();
        groupFragment = new GroupFragment();
        storiesFragment = new StoriesFragment();
        exploreFragment = new ExploreFragment();
        shortcutsFragment = new ShortcutsFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment[] active = {inboxFragment};

        fragmentManager.beginTransaction().add(R.id.main_frame, inboxFragment, "1").commit();
        fragmentManager.beginTransaction().add(R.id.main_frame, groupFragment, "2").hide(groupFragment).commit();
        fragmentManager.beginTransaction().add(R.id.main_frame, exploreFragment, "3").hide(exploreFragment).commit();
        fragmentManager.beginTransaction().add(R.id.main_frame, shortcutsFragment, "3").hide(shortcutsFragment).commit();

        fragmentManager.beginTransaction().hide(active[0]).show(inboxFragment).commit();
        active[0] = inboxFragment;

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home_home_menu_list_item:
                        fragmentManager.beginTransaction().hide(active[0]).show(inboxFragment).commit();
                        active[0] = inboxFragment;
                        return true;

                    case R.id.group_home_menu_list_item:
                        fragmentManager.beginTransaction().hide(active[0]).show(groupFragment).commit();
                        active[0] = groupFragment;
                        return true;

                    case R.id.explore_home_menu_list_item:
                        fragmentManager.beginTransaction().hide(active[0]).show(exploreFragment).commit();
                        active[0] = exploreFragment;
                        return true;

                    case R.id.shortcuts_home_menu_list_item:
                        fragmentManager.beginTransaction().hide(active[0]).show(shortcutsFragment).commit();
                        active[0] = shortcutsFragment;
                        return true;

                    default:
                        return false;
                }
            }
        });

        String notificationIntent = getIntent().getStringExtra("messageNotification");

        if (notificationIntent != null) {

            // Here we can decide what do to -- perhaps load other parameters from the intent extras such as IDs, etc
            if (notificationIntent.equals("messageNotification")) {
                fragmentManager.beginTransaction().hide(active[0]).show(inboxFragment).commit();
                active[0] = inboxFragment;
            }
        }

//        AHBottomNavigation bottomNavigation = (AHBottomNavigation) findViewById(R.id.mainAHBottomNavigation2);
//        // Create items
//        AHBottomNavigationItem item1 = new AHBottomNavigationItem("Home", R.drawable.home, R.color.md_black_1000);
//        AHBottomNavigationItem item2 = new AHBottomNavigationItem("Group", R.drawable.group, R.color.md_black_1000);
//        AHBottomNavigationItem item3 = new AHBottomNavigationItem("Explore", R.drawable.ic_explore_black_24dp, R.color.md_black_1000);
//        // Add items
//        bottomNavigation.addItem(item1);
//        bottomNavigation.addItem(item2);
//        bottomNavigation.addItem(item3);
//
//        // Set background color
//        bottomNavigation.setDefaultBackgroundColor(Color.WHITE);
//
//        // Change colors
//        bottomNavigation.setAccentColor(Color.BLACK);
//        bottomNavigation.setInactiveColor(Color.GRAY);
//
//        // Force to tint the drawable (useful for font with icon for example)
//        bottomNavigation.setForceTint(true);
//
//        // Manage titles
//        //bottomNavigation.setTitleState(AHBottomNavigation.TitleState.SHOW_WHEN_ACTIVE);
//        //bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
//        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_HIDE);
//
//        // Set current item programmatically
//        bottomNavigation.setCurrentItem(0);
//
//        // Customize notification (title, background, typeface)
//        bottomNavigation.setNotificationBackgroundColor(Color.parseColor("#F63D2B"));
//
//        // Add or remove notification for each item
//        //bottomNavigation.setNotification("0", 2);
//
//        // Enable / disable item & set disable color
//        //bottomNavigation.enableItemAtPosition(2);
//        //bottomNavigation.disableItemAtPosition(1);
//        //bottomNavigation.setItemDisableColor(Color.parseColor("#3A000000"));
////
//        inboxFragment = new InboxFragment();
//        groupFragment = new GroupFragment();
//        storiesFragment = new StoriesFragment();
//        exploreFragment = new ExploreFragment();
////
//
//
//        fragmentManager.beginTransaction().add(R.id.main_frame, inboxFragment, "1").commit();
//        fragmentManager.beginTransaction().add(R.id.main_frame, groupFragment, "2").hide(groupFragment).commit();
//        fragmentManager.beginTransaction().add(R.id.main_frame, exploreFragment, "3").hide(exploreFragment).commit();
//
//
//
//        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
//            @Override
//            public boolean onTabSelected(int position, boolean wasSelected) {
//                switch (position) {
//
//                    case 0:
//                        fragmentManager.beginTransaction().hide(active[0]).show(inboxFragment).commit();
//                        active[0] = inboxFragment;
//                        return true;
//
//                    case 1:
//                        fragmentManager.beginTransaction().hide(active[0]).show(groupFragment).commit();
//                        active[0] = groupFragment;
//                        return true;
//
//                    case 2:
//                        fragmentManager.beginTransaction().hide(active[0]).show(exploreFragment).commit();
//                        active[0] = exploreFragment;
//                        return true;
//
//                    default:
//                        return false;
//                }
//            }
//        });
    }

    private void sendToLogin() {
        Intent loginIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(loginIntent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Glide.with(MainActivity.this).pauseAllRequests();
    }


}
