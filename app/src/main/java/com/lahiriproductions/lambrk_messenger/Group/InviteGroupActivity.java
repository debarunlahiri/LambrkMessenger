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

import com.lahiriproductions.lambrk_messenger.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class InviteGroupActivity extends AppCompatActivity {

    private Toolbar invitegrouptoolbar;

    private Context mContext;
    private List<InviteGroup> inviteGroupList = new ArrayList<>();
    private RecyclerView rvInvitePeople;
    private InviteGroupAdapter inviteGroupAdapter;
    private LinearLayoutManager linearLayoutManager;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    private String user_id, group_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_group);

        mContext = InviteGroupActivity.this;

        group_id = getIntent().getStringExtra("group_id");

        invitegrouptoolbar = findViewById(R.id.invitegrouptoolbar);
        invitegrouptoolbar.setTitle("Invite People");
        setSupportActionBar(invitegrouptoolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        invitegrouptoolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp));
        invitegrouptoolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_url)).getReference();;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl(getString(R.string.storage_reference_url));

        user_id = currentUser.getUid();

        rvInvitePeople = findViewById(R.id.rvInvitePeople);
        inviteGroupAdapter = new InviteGroupAdapter(mContext, inviteGroupList,"none", group_id);
        linearLayoutManager = new LinearLayoutManager(mContext);
        rvInvitePeople.setLayoutManager(linearLayoutManager);
        rvInvitePeople.setAdapter(inviteGroupAdapter);


        initFollowingPeople();
    }

    private void initFollowingPeople() {
        mDatabase.child("following").child(user_id).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                InviteGroup inviteGroup = dataSnapshot.getValue(InviteGroup.class);
                inviteGroupList.add(inviteGroup);
                inviteGroupAdapter = new InviteGroupAdapter(mContext, inviteGroupList, "none", "following");
                inviteGroupAdapter.notifyDataSetChanged();
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

        mDatabase.child("follower").child(user_id).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                InviteGroup inviteGroup = dataSnapshot.getValue(InviteGroup.class);
                inviteGroupList.add(inviteGroup);
                inviteGroupAdapter = new InviteGroupAdapter(mContext, inviteGroupList, "none", "follower");
                inviteGroupAdapter.notifyDataSetChanged();
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
}
