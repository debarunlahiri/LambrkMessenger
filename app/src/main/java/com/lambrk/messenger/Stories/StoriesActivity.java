package com.lambrk.messenger.Stories;

import android.content.Context;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.lambrk.messenger.R;
import com.lambrk.messenger.Utils.OnSwipeTouchListener;
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

public class StoriesActivity extends AppCompatActivity implements View.OnTouchListener, GestureDetector.OnGestureListener {

    private CardView storiestopCV;
    private TextView tvStoryName;
    private CircleImageView storyuserprofileCIV;
    ViewPager storiesVP;

    private List<Stories> storiesList = new ArrayList<>();
    private Context mContext;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    private String user_id;

    private String story_user_id, story_id;
    Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_stories);

        mContext = StoriesActivity.this;

        bundle = getIntent().getExtras();
        story_user_id = bundle.get("story_user_id").toString();
//        story_id = bundle.get("story_id").toString();

        storiestopCV = findViewById(R.id.storiestopCV);
        tvStoryName = findViewById(R.id.tvStoryName);
        storyuserprofileCIV = findViewById(R.id.storyuserprofileCIV);

        storiesVP = findViewById(R.id.storiesVP);
        StoriesViewPageAdapter storiesViewPageAdapter = new StoriesViewPageAdapter(storiesList, mContext);
        storiesVP.setAdapter(storiesViewPageAdapter);

        storiesVP.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeDown() {
               onBackPressed();
            }
        });



        mDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_url)).getReference();;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl(getString(R.string.storage_reference_url));

        user_id = currentUser.getUid();

        storiestopCV.setBackgroundResource(R.drawable.black_to_transparent);

        mDatabase.child("users").child(story_user_id).child("user_data").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                if (dataSnapshot.child("profile_image").getValue().toString() == null) {
                    Glide.with(mContext).load(R.drawable.default_profile_pic).thumbnail(0.1f).into(storyuserprofileCIV);
                } else {
                    String profile_image = dataSnapshot.child("profile_image").getValue().toString();
                    Glide.with(mContext).load(profile_image).thumbnail(0.1f).into(storyuserprofileCIV);
                }
                tvStoryName.setText(name);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        mDatabase.child("stories").child(story_user_id).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Stories stories = dataSnapshot.getValue(Stories.class);
                storiesList.add(stories);
                storiesViewPageAdapter.notifyDataSetChanged();

                setCurrentPosition();
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

    private void setCurrentPosition() {
        for (int i=0; i<storiesList.size(); i++) {
            if (bundle.get("story_id") != null && storiesList.get(i).getStory_id().equals(bundle.get("story_id").toString())) {
                storiesVP.setCurrentItem(i);
                break;
            } else if (bundle.get("story_id") == null && storiesList.get(i).getSeen() != null && storiesList.get(i).getSeen().getIsHas_seen_story()) {
                storiesVP.setCurrentItem(i);
                break;
            }
        }
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        Toast.makeText(mContext, String.valueOf(motionEvent.getY()), Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {

        return false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        return false;
    }
}
