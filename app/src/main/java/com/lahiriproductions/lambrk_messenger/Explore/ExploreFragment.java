package com.lahiriproductions.lambrk_messenger.Explore;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.lahiriproductions.lambrk_messenger.Group.GroupPosts;
import com.lahiriproductions.lambrk_messenger.Group.GroupPostsAdapter;
import com.lahiriproductions.lambrk_messenger.R;
import com.lahiriproductions.lambrk_messenger.Search.SearchActivity;
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
public class ExploreFragment extends Fragment {

    private Context mContext;

    private CircleImageView groupfragmentprofileCIV;
    private ImageView inboxsearchIB;

    private RecyclerView rvExplore;
    private List<Explore> exploreList = new ArrayList<>();
    private ExploreAdapter exploreAdapter;
    private LinearLayoutManager linearLayoutManager;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser = null;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    public ExploreFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_explore, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mContext = getActivity();

        groupfragmentprofileCIV = view.findViewById(R.id.groupfragmentprofileCIV);
        inboxsearchIB = view.findViewById(R.id.inboxsearchIB);

        mDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_url)).getReference();;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl(getString(R.string.storage_reference_url));

        rvExplore = view.findViewById(R.id.rvExplore);
        exploreAdapter = new ExploreAdapter(mContext, exploreList, "for_explore");
        linearLayoutManager = new LinearLayoutManager(mContext);
        rvExplore.setAdapter(exploreAdapter);
        linearLayoutManager.setReverseLayout(true);
        rvExplore.setLayoutManager(linearLayoutManager);

        if (currentUser != null) {
            String user_id = currentUser.getUid();
            mDatabase.child("users").child(user_id).child("user_data").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.child("profile_image").exists()) {
                            String profile_image = dataSnapshot.child("profile_image").getValue().toString();
                            if (getActivity() != null) {
                                Glide.with(mContext).load(profile_image).into(groupfragmentprofileCIV);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            initExplore();


        }
        inboxsearchIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent searchIntent = new Intent(getActivity(), SearchActivity.class);
                searchIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(searchIntent);
            }
        });
    }

    private void initExplore() {
        mDatabase.child("group_posts").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    Explore explore = dataSnapshot.getValue(Explore.class);
                    exploreList.add(explore);
                    exploreAdapter.notifyDataSetChanged();
                } else {
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
