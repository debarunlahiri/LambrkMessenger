package com.lahiriproductions.lambrk_messenger.Group;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lahiriproductions.lambrk_messenger.R;
import com.google.android.gms.tasks.OnFailureListener;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupPostCommentActivity extends AppCompatActivity {

    private Toolbar grouppostcommenttoolbar;

    private TextView tvNoComments;
    private CircleImageView commentProfileCIV;
    private Button commentbutton;
    private EditText etCommentBody;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    private String user_id;
    private String group_id, group_post_id;

    private List<GroupPostComment> groupPostCommentList = new ArrayList<>();
    private GroupPostCommentAdapter groupPostCommentAdapter;
    private RecyclerView grouppostcommentRV;
    private LinearLayoutManager linearLayoutManager;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_post_comment);

        mContext = GroupPostCommentActivity.this;

        Bundle bundle = getIntent().getExtras();
        group_id = bundle.get("group_id").toString();
        group_post_id = bundle.get("group_post_id").toString();

        grouppostcommenttoolbar = findViewById(R.id.grouppostcommenttoolbar);
        grouppostcommenttoolbar.setTitle("Comments");
        setSupportActionBar(grouppostcommenttoolbar);
        grouppostcommenttoolbar.setNavigationIcon(getResources().getDrawable(R.mipmap.black_back));
        grouppostcommenttoolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        tvNoComments = findViewById(R.id.tvNoComments);
        commentProfileCIV = findViewById(R.id.commentProfileCIV);
        etCommentBody = findViewById(R.id.etCommentBody);
        commentbutton = findViewById(R.id.commentbutton);

        grouppostcommentRV = findViewById(R.id.grouppostcommentRV);
        groupPostCommentAdapter = new GroupPostCommentAdapter(groupPostCommentList, mContext);
        linearLayoutManager = new LinearLayoutManager(mContext);
        grouppostcommentRV.setAdapter(groupPostCommentAdapter);
        grouppostcommentRV.setLayoutManager(linearLayoutManager);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        mDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_url)).getReference();;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl(getString(R.string.storage_reference_url));

        user_id = currentUser.getUid();

        checkComments();
        fetchComments();

        mDatabase.child("users").child(user_id).child("user_data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("profile_image").getValue() == null) {
                    Glide.with(mContext).load(R.drawable.default_profile_pic).into(commentProfileCIV);
                } else {
                    String profile_image = dataSnapshot.child("profile_image").getValue().toString();
                    Glide.with(mContext).load(profile_image).thumbnail(0.1f).into(commentProfileCIV);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        commentbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String body = etCommentBody.getText().toString();

                if (body.isEmpty()) {
                    Toast.makeText(mContext, "Cannot post empty comment", Toast.LENGTH_LONG).show();
                } else {
                    etCommentBody.setText("");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                    String formattedDate = sdf.format(new Date());
                    String comment_id = mDatabase.child("groups").child(group_id).child("posts").child(group_post_id).child("comments").push().getKey();
                    HashMap<String, Object> mCommentDataMap = new HashMap<>();
                    mCommentDataMap.put("user_id", user_id);
                    mCommentDataMap.put("body", body.trim());
                    mCommentDataMap.put("group_id", group_id);
                    mCommentDataMap.put("group_post_id", group_post_id);
                    mCommentDataMap.put("formatted_date", formattedDate);
                    mCommentDataMap.put("timestamp", System.currentTimeMillis());
                    mCommentDataMap.put("comment_id", comment_id);
                    mDatabase.child("groups").child(group_id).child("posts").child(group_post_id).child("comments").child(comment_id).setValue(mCommentDataMap).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(mContext, "Failure: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private void checkComments() {
        mDatabase.child("groups").child(group_id).child("posts").child(group_post_id).child("comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    tvNoComments.setVisibility(View.GONE);
                } else {
                    tvNoComments.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchComments() {
        mDatabase.child("groups").child(group_id).child("posts").child(group_post_id).child("comments").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                GroupPostComment groupPostComment = dataSnapshot.getValue(GroupPostComment.class);
                groupPostCommentList.add(groupPostComment);
                groupPostCommentAdapter.notifyDataSetChanged();
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
