package com.lahiriproductions.lambrk_messenger.Explore;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.curioustechizen.ago.RelativeTimeTextView;
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
import com.google.gson.Gson;
import com.lahiriproductions.lambrk_messenger.Group.GroupActivity;
import com.lahiriproductions.lambrk_messenger.Group.GroupPostCommentActivity;
import com.lahiriproductions.lambrk_messenger.Group.GroupPostsAdapter;
import com.lahiriproductions.lambrk_messenger.OverallProfileActivity;
import com.lahiriproductions.lambrk_messenger.R;
import com.lahiriproductions.lambrk_messenger.Utils.PictureViewerActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class ExploreAdapter extends RecyclerView.Adapter<ExploreAdapter.ViewHolder> {

    private static final String TAG = ExploreAdapter.class.getSimpleName();
    private Context mContext;
    private List<Explore> exploreList;
    private List<Explore> exploreUsersList = new ArrayList<>();

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    private String user_id;


    public static final int GROUP_POST_TYPE_TEXT = 0;
    public static final int GROUP_POST_TYPE_IMAGE = 1;
    public static final int GROUP_POST_TYPE_IMAGE_WITH_NOTEXT = 2;
    private static final int USERS_VIEW = 3;


    public ExploreAdapter(Context mContext, List<Explore> exploreList, String for_explore) {
        this.mContext = mContext;
        this.exploreList = exploreList;
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
        } else if (viewType == USERS_VIEW) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_explore_users_layout, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_posts_list_item, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Explore explore = exploreList.get(position);
        mDatabase = FirebaseDatabase.getInstance(mContext.getString(R.string.firebase_url)).getReference();;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl(mContext.getString(R.string.storage_reference_url));

        if (position == 2) {
            initUsersList(holder);

            holder.tvExploreUsersViewAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        } else {
            if (explore != null) {
                setPostDetails(holder, explore);
                setUserDetails(holder, explore);
                setPostLikesCounter(holder, explore);
                setPostsCommentCounter(holder, explore);
                setPostLike(holder, explore);
                setGroupDetails(holder, explore);

                if (explore.getPost_image() != null) {
                    Glide.with(mContext).load(explore.getPost_image()).thumbnail(0.1f).into(holder.postIV);

                    holder.postImageCV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent postImageIntent = new Intent(mContext, PictureViewerActivity.class);
                            postImageIntent.putExtra("post_image", explore.getPost_image());
                            mContext.startActivity(postImageIntent);
                        }
                    });
                }
            }


        /*
        if (explore.getPost_image() != null && explore.getBody().equals("")) {
            holder.tvPostsDesc.setVisibility(View.GONE);
        } */

            holder.grouppostCommentIB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent commentIntent = new Intent(mContext, GroupPostCommentActivity.class);
                    commentIntent.putExtra("group_id", explore.getGroup_id());
                    commentIntent.putExtra("group_post_id", explore.getPost_id());
                    mContext.startActivity(commentIntent);
                }
            });

            holder.userpostCIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent profileIntent = new Intent(mContext, OverallProfileActivity.class);
                    profileIntent.putExtra("searched_user_id", explore.getUser_id());
                    mContext.startActivity(profileIntent);
                }
            });

            holder.cvGroupTag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent groupIntent = new Intent(mContext, GroupActivity.class);
                    groupIntent.putExtra("group_id", explore.getGroup_id());
                    mContext.startActivity(groupIntent);
                }
            });
        }



    }

    private void initUsersList(ViewHolder holder) {
        ExploreUsersAdapter exploreUsersAdapter = new ExploreUsersAdapter(mContext, exploreUsersList);
        holder.rvExploreUsers.setAdapter(exploreUsersAdapter);
        holder.rvExploreUsers.setLayoutManager(new LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false));
        ViewCompat.setNestedScrollingEnabled(holder.rvExploreUsers, false);

//        for (int i=0; i<5; i++) {
//            Explore explore = new Explore();
//            exploreUsersList.add(explore);
//        }
//        exploreUsersAdapter.setExploreList(exploreUsersList);

        mDatabase.child("users").limitToLast(5).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
//                    if (snapshot.getChildrenCount() >= 5) {
//                        holder.tvExploreUsersViewAll.setVisibility(View.VISIBLE);
//                    } else {
//                        holder.tvExploreUsersViewAll.setVisibility(View.GONE);
//                    }
                    Explore explore = snapshot.getValue(Explore.class);
                    exploreUsersList.add(explore);
                    exploreUsersAdapter.notifyDataSetChanged();
                }
                Collections.shuffle(exploreUsersList);
                Log.e(TAG, "initUsersList: " + new Gson().toJson(exploreUsersList));
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

    private void setGroupDetails(ViewHolder holder, Explore explore) {
        holder.cvGroupTag.setVisibility(View.VISIBLE);
        if (explore.getGroup_id() != null) {
            mDatabase.child("groups").child(explore.getGroup_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String photo = snapshot.child("group_profile_image").getValue().toString();
                        String group_cover_image = snapshot.child("group_cover_image").getValue().toString();
                        String group_name = snapshot.child("group_name").getValue().toString();

                        holder.tvGroupTagName.setText(group_name);
                        Glide.with(mContext).load(photo).placeholder(R.drawable.profile_background_gradient).into(holder.civGroupTagPic);
                        Glide.with(mContext).load(group_cover_image).apply(RequestOptions.bitmapTransform(new BlurTransformation(16, 6))).placeholder(R.drawable.profile_background_gradient).into(holder.ivGroupTagBg);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void setPostLike(ViewHolder holder, Explore explore) {
        if (explore.getGroup_id() != null) {
            mDatabase.child("groups").child(explore.getGroup_id()).child("posts").child(explore.getPost_id()).child("likes").child(user_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        explore.setUserHasliked(true);
                        holder.grouppostLikeIB.setBackgroundResource(R.mipmap.hearted);
                        holder.grouppostLikeIB.setColorFilter(Color.rgb(251, 57, 88));
                    } else {
                        explore.setUserHasliked(false);
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
                    if (!explore.isUserHasliked()) {
                        HashMap<String, Object> mGroupPostLikeDataMap = new HashMap<>();
                        mGroupPostLikeDataMap.put("user_id", user_id);
                        mGroupPostLikeDataMap.put("group_id", explore.getGroup_id());
                        mGroupPostLikeDataMap.put("post_id", explore.getPost_id());
                        mGroupPostLikeDataMap.put("timestamp", System.currentTimeMillis());
                        mDatabase.child("groups").child(explore.getGroup_id()).child("posts").child(explore.getPost_id()).child("likes").child(user_id).setValue(mGroupPostLikeDataMap);
                        holder.grouppostLikeIB.setBackgroundResource(R.mipmap.hearted);
                        holder.grouppostLikeIB.setColorFilter(Color.rgb(251, 57, 88));
                        holder.grouppostLikeIB.setColorFilter(Color.BLACK);
                    } else if (explore.isUserHasliked()) {
                        mDatabase.child("groups").child(explore.getGroup_id()).child("posts").child(explore.getPost_id()).child("likes").child(user_id).removeValue();
                        holder.grouppostLikeIB.setBackgroundResource(R.mipmap.heart);
                        holder.grouppostLikeIB.setColorFilter(Color.BLACK);
                    }
                }
            });
        }

    }

    private void setPostsCommentCounter(ViewHolder holder, Explore explore) {
        if (explore.getGroup_id() != null) {
            mDatabase.child("groups").child(explore.getGroup_id()).child("posts").child(explore.getPost_id()).child("comments").addValueEventListener(new ValueEventListener() {
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
    }

    private void setPostLikesCounter(ViewHolder holder, Explore explore) {
        if (explore.getGroup_id() != null) {
            mDatabase.child("groups").child(explore.getGroup_id()).child("posts").child(explore.getPost_id()).child("likes").addValueEventListener(new ValueEventListener() {
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
    }

    private void setUserDetails(ViewHolder holder, Explore explore) {
        if (explore.getUser_id() != null) {
            mDatabase.child("users").child(explore.getUser_id()).child("user_data").addValueEventListener(new ValueEventListener() {
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
    }

    private void setPostDetails(ViewHolder holder, Explore explore) {
        if (explore.getBody() != null && !explore.getBody().equals("")) {
            holder.tvPostsDesc.setText(explore.getBody());
        }

        holder.tvPostTime.setReferenceTime(explore.getTimestamp());
    }

    @Override
    public int getItemViewType(int position) {
        Explore explore = exploreList.get(position);
        mDatabase = FirebaseDatabase.getInstance(mContext.getString(R.string.firebase_url)).getReference();;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl(mContext.getString(R.string.storage_reference_url));
        user_id = currentUser.getUid();

        if (explore.getPost_image() == null && explore.getBody() != null) {
            return GROUP_POST_TYPE_TEXT;
        } else if (explore.getPost_image() != null && explore.getBody().equals("")) {
            return GROUP_POST_TYPE_IMAGE_WITH_NOTEXT;
        } else if (exploreList.size() > 2 && position == 2) {
            return USERS_VIEW;
        } else {
            return GROUP_POST_TYPE_IMAGE;
        }
    }

    @Override
    public int getItemCount() {
        return exploreList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivExploreBG;
        private CircleImageView civExploreProfilePic;
        private TextView tvPersonName, tvPersonUsername;

        private TextView tvPostName, tvPostsDesc;
        private RelativeTimeTextView tvPostTime;
        private CircleImageView userpostCIV, civGroupTagPic;
        private CardView postImageCV, cvGroupTag;
        private ImageView postIV, ivGroupTagBg;

        private ImageButton grouppostLikeIB, grouppostCommentIB;
        private TextView tvGroupPostLikeCounter, tvGroupPostCommentCounter, tvGroupTagName, tvExploreUsersViewAll;

        private RecyclerView rvExploreUsers;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivExploreBG = itemView.findViewById(R.id.ivExploreBG);
            civExploreProfilePic = itemView.findViewById(R.id.civExploreProfilePic);
            tvPersonName = itemView.findViewById(R.id.tvPersonName);
            tvPersonUsername = itemView.findViewById(R.id.tvPersonUsername);

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
            cvGroupTag = itemView.findViewById(R.id.cvGroupTag);
            civGroupTagPic = itemView.findViewById(R.id.civGroupTagPic);
            tvGroupTagName = itemView.findViewById(R.id.tvGroupTagName);
            ivGroupTagBg = itemView.findViewById(R.id.ivGroupTagBg);


            rvExploreUsers = itemView.findViewById(R.id.rvExploreUsers);
            tvExploreUsersViewAll = itemView.findViewById(R.id.tvExploreUsersViewAll);
        }
    }
}
