package com.lambrk.messenger.Group;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lambrk.messenger.OverallProfileActivity;
import com.lambrk.messenger.R;
import com.lambrk.messenger.Utils.PictureViewerActivity;
import com.github.curioustechizen.ago.RelativeTimeTextView;
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
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupPostsAdapter extends RecyclerView.Adapter<GroupPostsAdapter.ViewHolder> {

    private List<GroupPosts> groupPostsList;
    private Context mContext;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    private String user_id;


    public static final int GROUP_POST_TYPE_TEXT = 0;
    public static final int GROUP_POST_TYPE_IMAGE = 1;
    public static final int GROUP_POST_TYPE_IMAGE_WITH_NOTEXT = 2;

    public GroupPostsAdapter(List<GroupPosts> groupPostsList, Context mContext) {
        this.groupPostsList = groupPostsList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == GROUP_POST_TYPE_TEXT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_text_post_list_item, parent, false);
            return new ViewHolder(view);
        } else if (viewType == GROUP_POST_TYPE_IMAGE_WITH_NOTEXT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_notext_withimage_post_list_item, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_posts_list_item, parent, false);
            return new ViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupPosts groupPosts = groupPostsList.get(position);

        mDatabase = FirebaseDatabase.getInstance(mContext.getString(R.string.firebase_url)).getReference();;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl(mContext.getString(R.string.storage_reference_url));

        setPostDetails(holder, position);
        setUserDetails(holder, position);
        setPostLikesCounter(holder, groupPosts);
        setPostsCommentCounter(holder, groupPosts);
        setPostLike(holder, groupPosts);

        if (groupPosts.getPost_image() != null) {
            Glide.with(mContext).load(groupPosts.getPost_image()).thumbnail(0.1f).into(holder.postIV);

            holder.postImageCV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent postImageIntent = new Intent(mContext, PictureViewerActivity.class);
                    postImageIntent.putExtra("post_image", groupPosts.getPost_image());
                    mContext.startActivity(postImageIntent);
                }
            });
        }

        /*
        if (groupPosts.getPost_image() != null && groupPosts.getBody().equals("")) {
            holder.tvPostsDesc.setVisibility(View.GONE);
        } */

        holder.grouppostCommentIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent commentIntent = new Intent(mContext, GroupPostCommentActivity.class);
                commentIntent.putExtra("group_id", groupPosts.getGroup_id());
                commentIntent.putExtra("group_post_id", groupPosts.getPost_id());
                mContext.startActivity(commentIntent);
            }
        });

        holder.userpostCIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(mContext, OverallProfileActivity.class);
                profileIntent.putExtra("searched_user_id", groupPosts.getUser_id());
                mContext.startActivity(profileIntent);
            }
        });

    }

    private void setPostLike(ViewHolder holder, GroupPosts groupPosts) {
        mDatabase.child("groups").child(groupPosts.getGroup_id()).child("posts").child(groupPosts.getPost_id()).child("likes").child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    groupPosts.setUserHasliked(true);
                    holder.grouppostLikeIB.setBackgroundResource(R.mipmap.hearted);
                    holder.grouppostLikeIB.setColorFilter(Color.rgb(251, 57, 88));
                } else {
                    groupPosts.setUserHasliked(false);
                    holder.grouppostLikeIB.setBackgroundResource(R.mipmap.heart);
                    holder.grouppostLikeIB.setColorFilter(Color.BLACK);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        holder.grouppostLikeIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!groupPosts.isUserHasliked()) {
                    HashMap<String, Object> mGroupPostLikeDataMap = new HashMap<>();
                    mGroupPostLikeDataMap.put("user_id", user_id);
                    mGroupPostLikeDataMap.put("group_id", groupPosts.getGroup_id());
                    mGroupPostLikeDataMap.put("post_id", groupPosts.getPost_id());
                    mGroupPostLikeDataMap.put("timestamp", System.currentTimeMillis());
                    mDatabase.child("groups").child(groupPosts.getGroup_id()).child("posts").child(groupPosts.getPost_id()).child("likes").child(user_id).setValue(mGroupPostLikeDataMap);
                    holder.grouppostLikeIB.setBackgroundResource(R.mipmap.hearted);
                    holder.grouppostLikeIB.setColorFilter(Color.rgb(251, 57, 88));
                    holder.grouppostLikeIB.setColorFilter(Color.BLACK);
                } else if (groupPosts.isUserHasliked()) {
                    mDatabase.child("groups").child(groupPosts.getGroup_id()).child("posts").child(groupPosts.getPost_id()).child("likes").child(user_id).removeValue();
                    holder.grouppostLikeIB.setBackgroundResource(R.mipmap.heart);
                    holder.grouppostLikeIB.setColorFilter(Color.BLACK);
                }
            }
        });
    }

    private void setPostsCommentCounter(ViewHolder holder, GroupPosts groupPosts) {
        mDatabase.child("groups").child(groupPosts.getGroup_id()).child("posts").child(groupPosts.getPost_id()).child("comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int comment_counter = (int) dataSnapshot.getChildrenCount();
                holder.tvGroupPostCommentCounter.setText(String.valueOf(comment_counter));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setPostLikesCounter(ViewHolder holder, GroupPosts groupPosts) {
        mDatabase.child("groups").child(groupPosts.getGroup_id()).child("posts").child(groupPosts.getPost_id()).child("likes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int like_counter = (int) dataSnapshot.getChildrenCount();
                holder.tvGroupPostLikeCounter.setText(String.valueOf(like_counter));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setUserDetails(ViewHolder holder, int position) {
        GroupPosts groupPosts = groupPostsList.get(position);
        mDatabase.child("users").child(groupPosts.getUser_id()).child("user_data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("profile_image").getValue().toString() == null) {
                    Glide.with(mContext).load(R.drawable.default_profile_pic).thumbnail(0.1f).into(holder.userpostCIV);
                } else {
                    String profile_image = dataSnapshot.child("profile_image").getValue().toString();
                    Glide.with(mContext).load(profile_image).thumbnail(0.1f).into(holder.userpostCIV);
                }
                String name = dataSnapshot.child("name").getValue().toString();

                holder.tvPostName.setText(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void setPostDetails(ViewHolder holder, int position) {
        GroupPosts groupPosts = groupPostsList.get(position);
        if (!groupPosts.getBody().equals("")) {
            holder.tvPostsDesc.setText(groupPosts.getBody());
        }

        holder.tvPostTime.setReferenceTime(groupPosts.getTimestamp());
    }

    @Override
    public int getItemViewType(int position) {
        GroupPosts groupPosts = groupPostsList.get(position);
        mDatabase = FirebaseDatabase.getInstance(mContext.getString(R.string.firebase_url)).getReference();;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl(mContext.getString(R.string.storage_reference_url));
        user_id = currentUser.getUid();

        if (groupPosts.getPost_image() == null && groupPosts.getBody() != null) {
            return GROUP_POST_TYPE_TEXT;
        } else if (groupPosts.getPost_image() != null && groupPosts.getBody().equals("")) {
            return GROUP_POST_TYPE_IMAGE_WITH_NOTEXT;
        } else {
            return GROUP_POST_TYPE_IMAGE;
        }
    }

    @Override
    public int getItemCount() {
        return groupPostsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvPostName, tvPostsDesc;
        private RelativeTimeTextView tvPostTime;
        private CircleImageView userpostCIV;
        private CardView postImageCV;
        private ImageView postIV;

        private ImageButton grouppostLikeIB, grouppostCommentIB;
        private TextView tvGroupPostLikeCounter, tvGroupPostCommentCounter;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvPostName = itemView.findViewById(R.id.tvPostName);
            tvPostsDesc = itemView.findViewById(R.id.tvPostsDesc);
            tvPostTime = itemView.findViewById(R.id.tvPostTime);
            userpostCIV = itemView.findViewById(R.id.userpostCIV);
            postImageCV = itemView.findViewById(R.id.postImageCV);
            postIV = itemView.findViewById(R.id.postIV);
            grouppostLikeIB = itemView.findViewById(R.id.grouppostLikeIB);
            grouppostCommentIB = itemView.findViewById(R.id.grouppostCommentIB);
            tvGroupPostLikeCounter = itemView.findViewById(R.id.tvGroupPostLikeCounter);
            tvGroupPostCommentCounter = itemView.findViewById(R.id.tvGroupPostCommentCounter);
        }
    }
}
