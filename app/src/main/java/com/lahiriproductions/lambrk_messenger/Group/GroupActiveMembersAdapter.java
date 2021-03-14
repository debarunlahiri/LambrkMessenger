package com.lahiriproductions.lambrk_messenger.Group;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.lahiriproductions.lambrk_messenger.Inbox.Inbox;
import com.lahiriproductions.lambrk_messenger.Inbox.InboxAdapter;
import com.lahiriproductions.lambrk_messenger.R;
import com.lahiriproductions.lambrk_messenger.Utils.TimeAgo;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupActiveMembersAdapter extends RecyclerView.Adapter<GroupActiveMembersAdapter.ViewHolder> {

    private static final String TAG = GroupActiveMembersAdapter.class.getSimpleName();
    private Context mContext;
    private List<ActiveGroupMembers> activeGroupMembersList;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    private String user_id;
    public boolean isOnline = false;

    public GroupActiveMembersAdapter(Context mContext, List<ActiveGroupMembers> activeGroupMembersList) {
        this.mContext = mContext;
        this.activeGroupMembersList = activeGroupMembersList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_members_online_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActiveGroupMembers activeGroupMembers = activeGroupMembersList.get(position);

        mDatabase = FirebaseDatabase.getInstance(mContext.getString(R.string.firebase_url)).getReference();;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl(mContext.getString(R.string.storage_reference_url));

        user_id = currentUser.getUid();

        setUserDetails(holder, activeGroupMembers);
        checkUserOnlineOrNot(holder, activeGroupMembers);


    }

    private void setUserDetails(ViewHolder holder, ActiveGroupMembers activeGroupMembers) {
        mDatabase.child("users").child(activeGroupMembers.getUser_id()).child("user_data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String username = dataSnapshot.child("username").getValue().toString();
                    String profile_image = dataSnapshot.child("profile_image").getValue().toString();
                    holder.tvMemberPersonName.setText(name);
                    holder.tvMemberPersonUsername.setText("@" + username);
                    Glide.with(mContext).load(profile_image).thumbnail(0.1f).into(holder.civMembersProfilePic);
                } else {
                    holder.tvMemberPersonName.setText("Unknown user");
                    Glide.with(mContext).load(R.drawable.default_profile_pic).into(holder.civMembersProfilePic);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkUserOnlineOrNot(ViewHolder holder, ActiveGroupMembers activeGroupMembers) {
        mDatabase.child("groups").child(activeGroupMembers.getGroup_id()).child("active").child(activeGroupMembers.getUser_id()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    isOnline = (boolean) dataSnapshot.child("isOnline").getValue();
                    long timestamp = (long) dataSnapshot.child("timestamp").getValue();
                    //86400000 milliseconds  = 1 day
                    if (Math.abs(timestamp - System.currentTimeMillis()) > 86400000) {
                        holder.inboxuseronlineCV.setVisibility(View.GONE);
                        holder.inboxShowTimeAgoCV.setVisibility(View.GONE);
                    } else {
                        if (isOnline == false) {
                            holder.inboxShowTimeAgoCV.setVisibility(View.VISIBLE);
                            holder.inboxuseronlineCV.setVisibility(View.GONE);
                            if (TimeAgo.getTimeAgo(timestamp) == null) {
                                holder.tvInboxShowShortTimeAgo.setText("now");
                            } else {
                                holder.tvInboxShowShortTimeAgo.setText(TimeAgo.getTimeAgo(timestamp));
                            }
                            Log.e(TAG, "onDataChange: "+ TimeAgo.getTimeAgo(timestamp));
                        } else if (isOnline == true) {
                            holder.inboxuseronlineCV.setVisibility(View.VISIBLE);
                            holder.inboxShowTimeAgoCV.setVisibility(View.GONE);
                        }
                    }
                } else {
                    holder.inboxuseronlineCV.setVisibility(View.GONE);
                    holder.inboxShowTimeAgoCV.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return activeGroupMembersList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvMemberPersonName, tvMemberPersonUsername, tvInboxShowShortTimeAgo;
        private CircleImageView civMembersProfilePic;
        private CardView inboxCV, inboxuseronlineCV, inboxShowTimeAgoCV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvMemberPersonName = itemView.findViewById(R.id.tvMemberPersonName);
            tvMemberPersonUsername = itemView.findViewById(R.id.tvMemberPersonUsername);
            civMembersProfilePic = itemView.findViewById(R.id.civMembersProfilePic);
            inboxuseronlineCV = itemView.findViewById(R.id.inboxuseronlineCV);
            inboxShowTimeAgoCV = itemView.findViewById(R.id.inboxShowTimeAgoCV);
            tvInboxShowShortTimeAgo = itemView.findViewById(R.id.tvInboxShowShortTimeAgo);
        }
    }
}
