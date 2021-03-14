package com.lahiriproductions.lambrk_messenger.Group;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.lahiriproductions.lambrk_messenger.R;

import java.util.ArrayList;
import java.util.List;

public class GroupActiveMembersActivity extends AppCompatActivity {

    private Toolbar activeMembersToolbar;

    private Context mContext;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    private RecyclerView rvActiveMembers;
    private GroupActiveMembersAdapter groupActiveMembersAdapter;
    private List<ActiveGroupMembers> activeGroupMembersList = new ArrayList<>();


    private String group_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_active_members);

        mContext = GroupActiveMembersActivity.this;

        group_id = getIntent().getExtras().getString("group_id");

        activeMembersToolbar = findViewById(R.id.activeMembersToolbar);
        activeMembersToolbar.setTitle("Online Members");
        setSupportActionBar(activeMembersToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        activeMembersToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp));
        activeMembersToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        rvActiveMembers = findViewById(R.id.rvActiveMembers);
        groupActiveMembersAdapter = new GroupActiveMembersAdapter(mContext, activeGroupMembersList);
        rvActiveMembers.setLayoutManager(new LinearLayoutManager(mContext));;
        rvActiveMembers.setAdapter(groupActiveMembersAdapter);

        mDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_url)).getReference();;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl(getString(R.string.storage_reference_url));



        initMembersList();
    }

    private void initMembersList() {
        mDatabase.child("groups").child(group_id).child("active").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ActiveGroupMembers activeGroupMembers = snapshot.getValue(ActiveGroupMembers.class);
                activeGroupMembersList.add(activeGroupMembers);
                groupActiveMembersAdapter.notifyDataSetChanged();
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
}