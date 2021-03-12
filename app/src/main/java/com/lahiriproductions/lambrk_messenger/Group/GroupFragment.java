package com.lahiriproductions.lambrk_messenger.Group;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.lahiriproductions.lambrk_messenger.OverallProfileActivity;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.glide.transformations.BlurTransformation;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupFragment extends Fragment {

    private CircleImageView groupfragmentprofileCIV;
    private ImageButton creategroupIB, inboxsearchIB;
    private TextView tvNoGroupConvo, tvJoinGroupSuggestion;
    private ProgressBar groupConvoPB;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    private RecyclerView fragmentgroupRV;
    private InboxGroupAdapter inboxGroupAdapter;
    private SuggestiveGroupAdapter suggestiveGroupAdapter;
    private List<Group> groupList = new ArrayList<>();
    private List<Group> suggestiveGroupList = new ArrayList<>();
    private Context mContext;
    private LinearLayoutManager linearLayoutManager;

    private String user_id;


    public GroupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mContext = getActivity();

        groupfragmentprofileCIV = view.findViewById(R.id.groupfragmentprofileCIV);
        creategroupIB = view.findViewById(R.id.creategroupIB);
        inboxsearchIB = view.findViewById(R.id.inboxsearchIB);
        tvNoGroupConvo = view.findViewById(R.id.tvNoGroupConvo);
        groupConvoPB = view.findViewById(R.id.groupConvoPB);
        tvJoinGroupSuggestion = view.findViewById(R.id.tvJoinGroupSuggestion);

        mDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_url)).getReference();;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl(getString(R.string.storage_reference_url));

        fragmentgroupRV = view.findViewById(R.id.fragmentgroupRV);
        suggestiveGroupAdapter = new SuggestiveGroupAdapter(groupList, mContext);
        linearLayoutManager = new LinearLayoutManager(mContext);
        fragmentgroupRV.setLayoutManager(linearLayoutManager);

        user_id = currentUser.getUid();
        groupfragmentprofileCIV.setVisibility(View.GONE);
        tvNoGroupConvo.setVisibility(View.GONE);
        groupConvoPB.setVisibility(View.GONE);

        mDatabase.child("group_members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    tvJoinGroupSuggestion.setVisibility(View.GONE);
                    tvNoGroupConvo.setVisibility(View.VISIBLE);
                    tvNoGroupConvo.setText("No groups available now\nYou can create group");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mDatabase.child("group_members").child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    tvJoinGroupSuggestion.setVisibility(View.GONE);
                    fetchGroup();
                } else {
                    tvJoinGroupSuggestion.setVisibility(View.VISIBLE);
                    loadSuggestiveGroups();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        creategroupIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createGroupIntent = new Intent(getActivity(), CreateGroupActivity.class);
                startActivity(createGroupIntent);
            }
        });

        groupfragmentprofileCIV.setOnClickListener(new View.OnClickListener() {
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
                Intent profileIntent = new Intent(getActivity(), GroupSearchActivity.class);
                profileIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(profileIntent);
            }
        });


    }

    private void fetchGroup() {
        mDatabase.child("groups").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                groupList.clear();
                Group group = dataSnapshot.getValue(Group.class);
                mDatabase.child("groups").child(group.getGroup_id()).child("members").child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            inboxGroupAdapter = new InboxGroupAdapter(groupList, mContext);
                            fragmentgroupRV.setAdapter(inboxGroupAdapter);
                            tvNoGroupConvo.setVisibility(View.GONE);
                            //groupConvoPB.setVisibility(View.GONE);
                            groupList.add(group);
                            inboxGroupAdapter.notifyDataSetChanged();
                        } else {
                            //groupConvoPB.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
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
        mDatabase.child("users").child(user_id).child("user_data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isAdded()) {
                    if (dataSnapshot.child("profile_image").exists()) {
                        if (dataSnapshot.child("profile_image").getValue().toString() == null) {
                            Glide.with(mContext).load(R.drawable.default_profile_pic).thumbnail(0.1f).into(groupfragmentprofileCIV);
                        } else {
                            String profile_image = dataSnapshot.child("profile_image").getValue().toString();
                            Glide.with(mContext).load(profile_image).thumbnail(0.1f).into(groupfragmentprofileCIV);
                        }
                        groupfragmentprofileCIV.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.fadein));
                        groupfragmentprofileCIV.setVisibility(View.VISIBLE);
                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadSuggestiveGroups() {
        mDatabase.child("groups").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                suggestiveGroupAdapter = new SuggestiveGroupAdapter(suggestiveGroupList, mContext);
                fragmentgroupRV.setAdapter(suggestiveGroupAdapter);
                Group group = dataSnapshot.getValue(Group.class);
                suggestiveGroupList.add(group);
                suggestiveGroupAdapter.notifyDataSetChanged();
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

    @Override
    public void onStart() {
        super.onStart();
    }

    public class SuggestiveGroupAdapter extends RecyclerView.Adapter<SuggestiveGroupAdapter.ViewHolder> {

        private List<Group> groupList;
        private Context mContext;

        public SuggestiveGroupAdapter(List<Group> groupList, Context mContext) {
            this.groupList = groupList;
            this.mContext = mContext;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.suggest_group_layout_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Group group = groupList.get(position);

            fetchGroupDetails(holder, group);
            countGroupMembers(holder, group);
            countGroupsPosts(holder, group);

            holder.suggestGroupCV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent groupIntent = new Intent(mContext, GroupActivity.class);
                    groupIntent.putExtra("group_id", group.getGroup_id());
                    mContext.startActivity(groupIntent);
                }
            });

            holder.suggestgroupjoinbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Random rnd = new Random();
                    int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                    String formattedDate = sdf.format(new Date());
                    HashMap<String, Object> mGroupJoinDataMap = new HashMap<>();
                    mGroupJoinDataMap.put("user_id", user_id);
                    mGroupJoinDataMap.put("timestamp", System.currentTimeMillis());
                    mGroupJoinDataMap.put("formatted_date", formattedDate);
                    mGroupJoinDataMap.put("chat_color_code", String.valueOf(color));
                    mDatabase.child("groups").child(group.getGroup_id()).child("members").child(user_id).setValue(mGroupJoinDataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mDatabase.child("group_members").child(user_id).child(group.getGroup_id()).child(group.getGroup_id()).setValue(group.getGroup_id());
                                Toast.makeText(mContext, "You have successfully joined the group", Toast.LENGTH_LONG).show();
                                holder.suggestgroupjoinbutton.setText("Group Joined");
                            } else {
                                Toast.makeText(mContext, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            });
        }

        private void countGroupsPosts(ViewHolder holder, Group group) {
            mDatabase.child("groups").child(group.getGroup_id()).child("posts").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int post_count = (int) dataSnapshot.getChildrenCount();

                    if (post_count == 1) {
                        holder.tvSuggestGroupPostsCount.setText(post_count + " post");
                    } else {
                        holder.tvSuggestGroupPostsCount.setText(post_count + " posts");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        private void countGroupMembers(ViewHolder holder, Group group) {
            mDatabase.child("groups").child(group.getGroup_id()).child("members").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int group_members_count = (int) dataSnapshot.getChildrenCount();

                    if (group_members_count == 1) {
                        holder.tvSuggestGroupMembersCount.setText(group_members_count + " member");
                    } else {
                        holder.tvSuggestGroupMembersCount.setText(group_members_count + " members");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        @Override
        public int getItemCount() {
            return groupList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private CircleImageView suggestGroupCIV;
            private TextView tvSuggestGroupName, tvSuggestGroupMembersCount, tvSuggestGroupPostsCount, tvSuggestGroupBody;
            private Button suggestgroupjoinbutton;
            private CardView suggestGroupCV;
            private ImageView suggestGroupBIV;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                suggestGroupCIV = itemView.findViewById(R.id.suggestGroupCIV);
                tvSuggestGroupName = itemView.findViewById(R.id.tvSuggestGroupName);
                tvSuggestGroupMembersCount = itemView.findViewById(R.id.tvSuggestGroupMembersCount);
                tvSuggestGroupPostsCount = itemView.findViewById(R.id.tvSuggestGroupPostsCount);
                suggestgroupjoinbutton = itemView.findViewById(R.id.suggestgroupjoinbutton);
                tvSuggestGroupBody = itemView.findViewById(R.id.tvSuggestGroupBody);
                suggestGroupCV = itemView.findViewById(R.id.suggestGroupCV);
                suggestGroupBIV = itemView.findViewById(R.id.suggestGroupBIV);
            }
        }
    }

    private void fetchGroupDetails(SuggestiveGroupAdapter.ViewHolder holder, Group group) {
        mDatabase.child("groups").child(group.getGroup_id()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String group_name = dataSnapshot.child("group_name").getValue().toString();
                String group_desc = dataSnapshot.child("group_desc").getValue().toString();
                String group_profile_image = dataSnapshot.child("group_profile_image").getValue().toString();
                String group_cover_image = dataSnapshot.child("group_cover_image").getValue().toString();

                Glide.with(mContext).load(group_profile_image).thumbnail(0.1f).into(holder.suggestGroupCIV);
                Glide.with(mContext).load(group_cover_image).apply(RequestOptions.bitmapTransform(new BlurTransformation(24, 5))).into(holder.suggestGroupBIV);
                holder.tvSuggestGroupName.setText(group_name);
                holder.tvSuggestGroupBody.setText(group_desc);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
