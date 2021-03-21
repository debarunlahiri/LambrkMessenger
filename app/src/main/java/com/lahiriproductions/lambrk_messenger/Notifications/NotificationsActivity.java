package com.lahiriproductions.lambrk_messenger.Notifications;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.GsonBuilder;
import com.lahiriproductions.lambrk_messenger.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationsActivity extends AppCompatActivity {

    private static final String TAG = NotificationsActivity.class.getSimpleName();
    private Toolbar inboxnotificationstoolbar;

    private TextView textView40;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    private String user_id;

    private RecyclerView inboxnotificationsRV;
    private Context mContext;
    private List<Notifications> notificationsList = new ArrayList<>();
    private List<FollowingNotifications> followingNotificationsList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private NotificationsAdapter notificationsAdapter;
//    private FollowingNotificationsAdapter followingNotificationsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        mContext = NotificationsActivity.this;

        inboxnotificationstoolbar = findViewById(R.id.inboxnotificationstoolbar);
        inboxnotificationstoolbar.setTitle("Notifications");
        setSupportActionBar(inboxnotificationstoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        inboxnotificationstoolbar.setNavigationIcon(getResources().getDrawable(R.mipmap.black_back));
        inboxnotificationstoolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        textView40 = findViewById(R.id.textView40);

        inboxnotificationsRV = findViewById(R.id.inboxnotificationsRV);
        notificationsAdapter = new NotificationsAdapter(mContext, notificationsList);
//        followingNotificationsAdapter = new FollowingNotificationsAdapter(mContext, followingNotificationsList);
        linearLayoutManager = new LinearLayoutManager(mContext);
//        inboxnotificationsRV.setAdapter(notificationsAdapter);
        inboxnotificationsRV.setAdapter(notificationsAdapter);
        inboxnotificationsRV.setLayoutManager(linearLayoutManager);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        mDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_url)).getReference();;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl(getString(R.string.storage_reference_url));

        user_id = currentUser.getUid();

        mDatabase.child("notifications").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                FollowingNotifications followingNotifications = dataSnapshot.getValue(FollowingNotifications.class);
//                followingNotificationsList.add(followingNotifications);
//                followingNotificationsAdapter.notifyDataSetChanged();

                Notifications notifications = dataSnapshot.getValue(Notifications.class);
                if (!(notifications.getNotification_type().equalsIgnoreCase("following_notification") && notifications.getFrom_user_id().equals(user_id))) {
                    if (notifications.getTo_user_id().equals(user_id) && !notifications.getFrom_user_id().equals(user_id)) {
                        notificationsList.add(notifications);

                    }
                }
                notificationsAdapter.notifyDataSetChanged();
                Log.d(TAG, "notificationList: " + new GsonBuilder().setPrettyPrinting().create().toJson(notificationsList));
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




    }



    @Override
    protected void onStop() {
        super.onStop();
    }
}
