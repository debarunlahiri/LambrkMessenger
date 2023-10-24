package com.lambrk.messenger.Inbox;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lambrk.messenger.AddStoryActivity;
import com.lambrk.messenger.Notifications.NotificationsActivity;
import com.lambrk.messenger.OverallProfileActivity;
import com.lambrk.messenger.R;
import com.lambrk.messenger.Search.SearchActivity;
import com.lambrk.messenger.Stories.Stories;
import com.lambrk.messenger.Stories.StoriesActivity;
import com.lambrk.messenger.Stories.StoriesAdapter;
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
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class InboxFragment extends Fragment {

    private CircleImageView circleImageView, civStoryProfilePic;
    private TextView tvNoMessages, tvInboxChat, tvInboxHideStories;
    private ImageButton inboxsearchIB, inboxnotificationIB;
    private ProgressBar inboxPB;
    private ImageView inboxnotificationsnotifyIV, ivStory;
    private NestedScrollView nsvInboxStories;
    private CardView cvAddStory, cvAdd, cvStory;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    private RecyclerView inboxRV, rvStories;
    private InboxAdapter inboxAdapter;
    private StoriesAdapter storiesAdapter;
    private List<Inbox> inboxList = new ArrayList<>();
    private List<String> mKeys = new ArrayList<>();
    private List<Stories> storiesList = new ArrayList<>();
    private Context mContext;
    private LinearLayoutManager linearLayoutManager, storiesLinearLayoutManager;

    private String user_id;

    public InboxFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inbox, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mContext = getActivity();

        circleImageView = view.findViewById(R.id.groupfragmentprofileCIV);
        tvInboxChat = view.findViewById(R.id.tvInboxChat);
        inboxsearchIB = view.findViewById(R.id.inboxsearchIB);
        tvNoMessages = view.findViewById(R.id.tvNoMessages);
        inboxPB = view.findViewById(R.id.inboxPB);
        inboxnotificationIB = view.findViewById(R.id.inboxnotificationIB);
        inboxnotificationsnotifyIV = view.findViewById(R.id.inboxnotificationsnotifyIV);
        tvInboxHideStories = view.findViewById(R.id.tvInboxHideStories);
        nsvInboxStories = view.findViewById(R.id.nsvInboxStories);
        cvAddStory = view.findViewById(R.id.cvAddStory);
        ivStory = view.findViewById(R.id.ivStory);
        civStoryProfilePic = view.findViewById(R.id.civStoryProfilePic);
        cvAdd = view.findViewById(R.id.cvAdd);
        cvStory = view.findViewById(R.id.cvStory);

        inboxRV = view.findViewById(R.id.inboxRV);
        inboxAdapter = new InboxAdapter(inboxList, mContext);
        linearLayoutManager = new LinearLayoutManager(mContext);
        inboxRV.setAdapter(inboxAdapter);
        inboxRV.setLayoutManager(linearLayoutManager);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        rvStories = view.findViewById(R.id.rvStories);
        storiesAdapter = new StoriesAdapter(storiesList, mContext);
        storiesLinearLayoutManager = new LinearLayoutManager(mContext);
        storiesLinearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        rvStories.setAdapter(storiesAdapter);
        rvStories.setLayoutManager(storiesLinearLayoutManager);
        storiesLinearLayoutManager.setStackFromEnd(true);
        storiesLinearLayoutManager.setReverseLayout(false);

        mDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_url)).getReference();;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl(getString(R.string.storage_reference_url));

        user_id = currentUser.getUid();
        circleImageView.setVisibility(View.GONE);
        inboxPB.setVisibility(View.VISIBLE);
        tvNoMessages.setVisibility(View.GONE);

        fetchInbox();
        fetchStories();

        inboxnotificationsnotifyIV.setVisibility(View.GONE);
        mDatabase.child("requests").child("following").child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    inboxnotificationsnotifyIV.setVisibility(View.VISIBLE);
                } else {
                    inboxnotificationsnotifyIV.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mDatabase.child("notifications").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.child("to_user_id").equals(user_id)) {
                        boolean hasUserRead = (boolean) ds.child("hasUserRead").getValue();
                        if (!hasUserRead) {
                            inboxnotificationsnotifyIV.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        tvInboxChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(getActivity(), OverallProfileActivity.class);
                profileIntent.putExtra("searched_user_id", user_id);
                startActivity(profileIntent);
            }
        });

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(getActivity(), OverallProfileActivity.class);
                profileIntent.putExtra("searched_user_id", user_id);
                startActivity(profileIntent);
            }
        });

        inboxsearchIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent searchIntent = new Intent(getActivity(), SearchActivity.class);
                searchIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(searchIntent);
            }
        });

        inboxnotificationIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent notificationIntent = new Intent(getActivity(), NotificationsActivity.class);
                startActivity(notificationIntent);
            }
        });

        cvAddStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent notificationIntent = new Intent(getActivity(), AddStoryActivity.class);
                startActivity(notificationIntent);
            }
        });

        cvStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent storyIntent = new Intent(mContext, StoriesActivity.class);
                storyIntent.putExtra("story_user_id", user_id);
                mContext.startActivity(storyIntent);
            }
        });

        inboxPB.setVisibility(View.GONE);

        SharedPreferences hideStoriesSharedPrefs = getActivity().getSharedPreferences("hideStories", Context.MODE_PRIVATE);
        SharedPreferences.Editor hideStoriesSharedPrefsEdit = getActivity().getSharedPreferences("hideStories", Context.MODE_PRIVATE).edit();
        tvInboxHideStories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences hideStoriesSharedPrefs = getActivity().getSharedPreferences("hideStories", Context.MODE_PRIVATE);
                SharedPreferences.Editor hideStoriesSharedPrefsEdit = getActivity().getSharedPreferences("hideStories", Context.MODE_PRIVATE).edit();
                if (hideStoriesSharedPrefs.contains("visibility")) {
                    if (hideStoriesSharedPrefs.getString("visibility", "").equals("gone")) {
//                        nsvInboxStories.setVisibility(View.VISIBLE);
                        hideStoriesSharedPrefsEdit.putString("visibility", "visible");
                        hideStoriesSharedPrefsEdit.apply();
                        tvInboxHideStories.setText("Hide Stories");
                        expand(nsvInboxStories);
                    } else if (hideStoriesSharedPrefs.getString("visibility", "").equals("visible")) {
//                        nsvInboxStories.setVisibility(View.GONE);
                        hideStoriesSharedPrefsEdit.putString("visibility", "gone");
                        hideStoriesSharedPrefsEdit.apply();
                        tvInboxHideStories.setText("Show Stories");
                        collapse(nsvInboxStories);
                    }
                } else {
//                    nsvInboxStories.setVisibility(View.GONE);
                    hideStoriesSharedPrefsEdit.putString("visibility", "gone");
                    hideStoriesSharedPrefsEdit.apply();
                    tvInboxHideStories.setText("Show Stories");
                    collapse(nsvInboxStories);
                }
            }
        });

        if (hideStoriesSharedPrefs.contains("visibility")) {
            if (hideStoriesSharedPrefs.getString("visibility", "").equals("gone")) {
                tvInboxHideStories.setText("Show Stories");
                nsvInboxStories.setVisibility(View.GONE);
            } else if (hideStoriesSharedPrefs.getString("visibility", "").equals("visible")) {
                tvInboxHideStories.setText("Hide Stories");
                nsvInboxStories.setVisibility(View.VISIBLE);
            }
        } else {
            tvInboxHideStories.setText("Hide Stories");
            nsvInboxStories.setVisibility(View.VISIBLE);
        }
    }

    private void fetchStories() {
        mDatabase.child("stories").child(currentUser.getUid()).limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    Stories stories = dataSnapshot.getValue(Stories.class);
                    Glide.with(mContext).load(stories.getStory_image()).into(ivStory);
                    cvAdd.setVisibility(View.GONE);
                } else {
                    cvAdd.setVisibility(View.VISIBLE);
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
        mDatabase.child("following").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String key = ds.getKey();
                    mDatabase.child("stories").child(key).limitToLast(1).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            if (dataSnapshot.exists()) {
                                Stories stories = dataSnapshot.getValue(Stories.class);
                                storiesList.add(stories);
                                storiesAdapter.notifyDataSetChanged();

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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void expand(final View v) {
        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) v.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ConstraintLayout.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Expansion speed of 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Collapse speed of 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    private void fetchInbox() {
        mDatabase.child("chats").child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                inboxList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String user_key = ds.getKey();

                    mDatabase.child("chats").child(user_id).child(user_key).limitToLast(1).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            if (dataSnapshot.exists()) {
                                tvNoMessages.setVisibility(View.GONE);
                                inboxPB.setVisibility(View.GONE);
                                Inbox inbox = dataSnapshot.getValue(Inbox.class);
                                inboxList.add(inbox);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    inboxList.sort((e1, e2) -> new Long(e1.getTimestamp()).compareTo(new Long(e2.getTimestamp())));
                                }
                                inbox.setUser_key(user_key);
                                inboxAdapter.notifyDataSetChanged();
                            } else {
                                tvNoMessages.setVisibility(View.VISIBLE);
                                inboxPB.setVisibility(View.GONE);
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        getUserDetails();
    }

    private void getUserDetails() {
        mDatabase.child("users").child(user_id).child("user_data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isAdded()) {
                    if (dataSnapshot.child("profile_image").exists()) {
                        if (dataSnapshot.child("profile_image").getValue().toString() == null) {
                            Glide.with(mContext).load(R.drawable.default_profile_pic).thumbnail(0.1f).into(circleImageView);
                            Glide.with(mContext).load(R.drawable.default_profile_pic).thumbnail(0.1f).into(civStoryProfilePic);
                        } else {
                            String profile_image = dataSnapshot.child("profile_image").getValue().toString();
                            Glide.with(mContext).load(profile_image).thumbnail(0.1f).into(circleImageView);
                            Glide.with(mContext).load(profile_image).thumbnail(0.1f).into(civStoryProfilePic);
                        }
                        circleImageView.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.fadein));
                        circleImageView.setVisibility(View.VISIBLE);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        user_id = currentUser.getUid();
        mDatabase.child("chats").child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    tvNoMessages.setVisibility(View.GONE);
                } else {
                    tvNoMessages.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        user_id = currentUser.getUid();
        mDatabase.child("chats").child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    tvNoMessages.setVisibility(View.GONE);
                } else {
                    tvNoMessages.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
