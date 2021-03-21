package com.lahiriproductions.lambrk_messenger.Stories;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.dynamic.IFragmentWrapper;
import com.google.firebase.database.ChildEventListener;
import com.lahiriproductions.lambrk_messenger.AddStoryActivity;
import com.lahiriproductions.lambrk_messenger.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class StoriesAdapter extends RecyclerView.Adapter<StoriesAdapter.ViewHolder> {

    private static final int ADD_STORIES_VIEW = 0;
    private static final int STORIES_VIEW = 1;
    private List<Stories> storiesList;
    private Context mContext;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    private String user_id;

    public StoriesAdapter(List<Stories> storiesList, Context mContext) {
        this.storiesList = storiesList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == ADD_STORIES_VIEW) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_inbox_stories_layout, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stories_list_item, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Stories stories = storiesList.get(position);
        mDatabase = FirebaseDatabase.getInstance(mContext.getString(R.string.firebase_url)).getReference();;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl(mContext.getString(R.string.storage_reference_url));

        user_id = currentUser.getUid();

        if (position == 0) {
            mDatabase.child("users").child(user_id).child("user_data").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String thumb_profile_image = snapshot.child("thumb_profile_image").getValue().toString();
                        Glide.with(mContext).load(thumb_profile_image).placeholder(R.drawable.ic_baseline_account_circle_24).into(holder.civStoryProfilePic);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            fetchStories(holder);

            holder.cvAddStory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent notificationIntent = new Intent(mContext, AddStoryActivity.class);
                    mContext.startActivity(notificationIntent);
                }
            });

            holder.cvStory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent storyIntent = new Intent(mContext, StoriesActivity.class);
                    storyIntent.putExtra("story_user_id", user_id);
                    mContext.startActivity(storyIntent);
                }
            });

        } else {
            setStory(holder, stories);
            holder.storieslistitemprofileCV.setBackgroundResource(R.drawable.story_profile_bg);
            holder.storylistitemCV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent storyIntent = new Intent(mContext, StoriesActivity.class);
                    storyIntent.putExtra("story_user_id", stories.getUser_id());
                    mContext.startActivity(storyIntent);
                }
            });
        }

    }

    private void fetchStories(ViewHolder holder) {
        mDatabase.child("stories").child(currentUser.getUid()).limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    Stories stories = dataSnapshot.getValue(Stories.class);
                    Glide.with(mContext).load(stories.getStory_image()).into(holder.ivStory);
//                    cvAdd.setVisibility(View.GONE);
                } else {
//                    cvAdd.setVisibility(View.VISIBLE);
                }
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

    private void setStory(ViewHolder holder, Stories stories) {
        Glide.with(mContext).load(stories.getStory_image()).thumbnail(0.1f).into(holder.storieslistitemStoryIV);
        mDatabase.child("users").child(stories.getUser_id()).child("user_data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("profile_image").getValue().toString() == null) {
                    Glide.with(mContext).load(R.drawable.default_profile_pic).thumbnail(0.1f).into(holder.storieslistitemuserprofileCIV);
                } else {
                    String profile_image = dataSnapshot.child("profile_image").getValue().toString();
                    Glide.with(mContext).load(profile_image).thumbnail(0.1f).into(holder.storieslistitemuserprofileCIV);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return storiesList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return ADD_STORIES_VIEW;
        } else {
            return STORIES_VIEW;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CardView storylistitemCV, storieslistitemprofileCV, cvAddStory, cvStory;
        private ImageView storieslistitemStoryIV, ivStory;
        private CircleImageView storieslistitemuserprofileCIV, civStoryProfilePic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            storieslistitemprofileCV = itemView.findViewById(R.id.storieslistitemprofileCV);
            storylistitemCV = itemView.findViewById(R.id.storylistitemCV);
            storieslistitemStoryIV = itemView.findViewById(R.id.storieslistitemStoryIV);
            storieslistitemuserprofileCIV = itemView.findViewById(R.id.storieslistitemuserprofileCIV);
            civStoryProfilePic = itemView.findViewById(R.id.civStoryProfilePic);
            ivStory = itemView.findViewById(R.id.ivStory);
            cvAddStory = itemView.findViewById(R.id.cvAddStory);
            cvStory = itemView.findViewById(R.id.cvStory);
        }
    }
}
