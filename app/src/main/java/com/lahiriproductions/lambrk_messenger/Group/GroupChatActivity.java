package com.lahiriproductions.lambrk_messenger.Group;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.lahiriproductions.lambrk_messenger.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import com.lahiriproductions.lambrk_messenger.Utils.Controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatActivity extends AppCompatActivity {

    private static final String TAG = GroupChatActivity.class.getSimpleName();
    private CircleImageView groupchatprofileCIV;
    private TextView tvGroupChatName, tvGroupMembersOnline;
    private ImageButton groupchatbackIB;
    private Button groupchatsendbutton;
    private EditText etGroupChatMessage;
    private CardView groupchatbottomCV;

    private RecyclerView groupchatRV;
    private List<GroupChat> groupChatList = new ArrayList<>();
    private List<ActiveGroupMembers> activeGroupMembersList = new ArrayList<>();
    private GroupChatAdapter groupChatAdapter;
    private LinearLayoutManager linearLayoutManager;
    private Context mContext;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    private String group_id;
    private String user_id;
    private int active_members = 0;

    private ChildEventListener childEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        mContext = GroupChatActivity.this;

        Bundle bundle = getIntent().getExtras();
        group_id = bundle.get("group_id").toString();
        mDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_url)).getReference();;

        groupchatprofileCIV = findViewById(R.id.groupchatprofileCIV);
        tvGroupChatName = findViewById(R.id.tvGroupChatName);
        groupchatbackIB = findViewById(R.id.groupchatbackIB);
        etGroupChatMessage = findViewById(R.id.etGroupChatMessage);
        groupchatsendbutton = findViewById(R.id.groupchatsendbutton);
        groupchatbottomCV = findViewById(R.id.groupchatbottomCV);
        tvGroupMembersOnline = findViewById(R.id.tvGroupMembersOnline);

        groupchatRV = findViewById(R.id.groupchatRV);
        groupChatAdapter = new GroupChatAdapter(groupChatList, mContext);
        linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setStackFromEnd(true);
        groupchatRV.setAdapter(groupChatAdapter);
        groupchatRV.setLayoutManager(linearLayoutManager);

        mDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_url)).getReference();;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl(getString(R.string.storage_reference_url));

        user_id = currentUser.getUid();

        fetchGroupDetails();
        fetchMessages();
        checkUserIsInGroupOrNot();
        checkActiveMembers();

        groupchatbackIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        groupchatsendbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message =  etGroupChatMessage.getText().toString();
                if (message.isEmpty()) {
                    etGroupChatMessage.setError("Cannot send empty message");
                } else {
                    sendChat(message);
                }

            }
        });

        groupchatprofileCIV.setElevation(5);

        groupchatprofileCIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent groupIntent = new Intent(GroupChatActivity.this, GroupActivity.class);
                groupIntent.putExtra("group_id", group_id);
                startActivity(groupIntent);
            }
        });

        tvGroupMembersOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent groupIntent = new Intent(GroupChatActivity.this, GroupActiveMembersActivity.class);
                groupIntent.putExtra("group_id", group_id);
                startActivity(groupIntent);
            }
        });
    }

    private void checkActiveMembers() {
        mDatabase.child("groups").child(group_id).child("active").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ActiveGroupMembers activeGroupMembers = snapshot.getValue(ActiveGroupMembers.class);
                activeGroupMembersList.add(activeGroupMembers);

                if (activeGroupMembers.getIsOnline()) {
                    active_members++;
                    tvGroupMembersOnline.setText(String.format("%d online members", active_members));
                }
                Log.e(TAG, "checkActiveMembers: " + new Gson().toJson(activeGroupMembers));
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void checkUserIsInGroupOrNot() {
        mDatabase.child("groups").child(group_id).child("members").child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    groupchatbottomCV.setVisibility(View.VISIBLE);
                } else {
                    groupchatbottomCV.setVisibility(View.INVISIBLE);
                    Toast.makeText(mContext, "You are no longer member of this group", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchMessages() {
        childEventListener = mDatabase.child("groups").child(group_id).child("chats").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                GroupChat groupChat = dataSnapshot.getValue(GroupChat.class);
                groupChatList.add(groupChat);
                groupChatAdapter.notifyDataSetChanged();
                groupchatRV.smoothScrollToPosition(groupChatAdapter.getItemCount()-1);
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

    private void sendChat(String message) {
        String chat_id = mDatabase.child("groups").child(group_id).child("chats").push().getKey();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        String formattedDate = sdf.format(new Date());
        HashMap<String, Object> mChatDataMap = new HashMap<>();
        mChatDataMap.put("message", message.trim());
        mChatDataMap.put("group_id", group_id);
        mChatDataMap.put("chat_id", chat_id);
        mChatDataMap.put("timestamp", System.currentTimeMillis());
        mChatDataMap.put("formatted_date", formattedDate);
        mChatDataMap.put("user_id", user_id);
        mDatabase.child("groups").child(group_id).child("chats").child(chat_id).setValue(mChatDataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    etGroupChatMessage.setText("");
                } else {
                    Toast.makeText(getApplicationContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void fetchGroupDetails() {
        mDatabase.child("groups").child(group_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String group_name = dataSnapshot.child("group_name").getValue().toString();
                String group_profile_image = dataSnapshot.child("group_profile_image").getValue().toString();
                Glide.with(getApplicationContext()).load(group_profile_image).thumbnail(0.1f).into(groupchatprofileCIV);
                tvGroupChatName.setText(group_name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStop() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        String formattedDate = sdf.format(new Date());
        HashMap<String, Object> mActiveDataMap = new HashMap<>();
        mActiveDataMap.put("user_id", user_id);
        mActiveDataMap.put("timestamp", System.currentTimeMillis());
        mActiveDataMap.put("isOnline", false);
        mActiveDataMap.put("formatted_date", formattedDate);
        mActiveDataMap.put("group_id", group_id);
        mDatabase.child("groups").child(group_id).child("active").child(user_id).setValue(mActiveDataMap);
        super.onStop();
    }

    @Override
    protected void onResume() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        String formattedDate = sdf.format(new Date());
        HashMap<String, Object> mActiveDataMap = new HashMap<>();
        mActiveDataMap.put("user_id", user_id);
        mActiveDataMap.put("timestamp", System.currentTimeMillis());
        mActiveDataMap.put("isOnline", true);
        mActiveDataMap.put("formatted_date", formattedDate);
        mActiveDataMap.put("group_id", group_id);
        mDatabase.child("groups").child(group_id).child("active").child(user_id).setValue(mActiveDataMap);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        String formattedDate = sdf.format(new Date());
        HashMap<String, Object> mActiveDataMap = new HashMap<>();
        mActiveDataMap.put("user_id", user_id);
        mActiveDataMap.put("timestamp", System.currentTimeMillis());
        mActiveDataMap.put("isOnline", false);
        mActiveDataMap.put("formatted_date", formattedDate);
        mActiveDataMap.put("group_id", group_id);
        mDatabase.child("groups").child(group_id).child("active").child(user_id).setValue(mActiveDataMap);
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        String formattedDate = sdf.format(new Date());
        HashMap<String, Object> mActiveDataMap = new HashMap<>();
        mActiveDataMap.put("user_id", user_id);
        mActiveDataMap.put("timestamp", System.currentTimeMillis());
        mActiveDataMap.put("isOnline", true);
        mActiveDataMap.put("formatted_date", formattedDate);
        mActiveDataMap.put("group_id", group_id);
        mDatabase.child("groups").child(group_id).child("active").child(user_id).setValue(mActiveDataMap);
        super.onStart();
    }
}
