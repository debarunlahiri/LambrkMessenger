package com.lahiriproductions.lambrk_messenger.Group;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lahiriproductions.lambrk_messenger.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class InviteGroupAdapter extends RecyclerView.Adapter<InviteGroupAdapter.ViewHolder> {

    private Context mContext;
    private List<InviteGroup> inviteGroupList;
    private String type;
    private String group_id;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    private String user_id;

    public InviteGroupAdapter(Context mContext, List<InviteGroup> inviteGroupList, String type, String group_id) {
        this.mContext = mContext;
        this.inviteGroupList = inviteGroupList;
        this.type = type;
        this.group_id = group_id;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_invite_group_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InviteGroup inviteGroup = inviteGroupList.get(position);

        mDatabase = FirebaseDatabase.getInstance(mContext.getString(R.string.firebase_url)).getReference();;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl(mContext.getString(R.string.storage_reference_url));

        user_id = currentUser.getUid();

        mDatabase.child("users").child(inviteGroup.getUser_id()).child("user_data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String profile_image = dataSnapshot.child("profile_image").getValue().toString();
                    String username = dataSnapshot.child("username").getValue().toString();

                    Glide.with(mContext).load(profile_image).into(holder.civPersonImage);
                    holder.tvPersonName.setText(name);
                    holder.tvUsername.setText("@" + username);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.bInvitePeople.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> mDataMap = new HashMap<>();
                mDataMap.put("group_id", group_id);
                mDataMap.put("user_id", inviteGroup.getUser_id());
                mDataMap.put("timestamp", System.currentTimeMillis());
                mDatabase.child("notifications").child("group_invites").child(inviteGroup.getUser_id()).child(group_id).setValue(mDataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            holder.bInvitePeople.setText("Invited");
                            holder.bInvitePeople.setBackgroundResource(R.drawable.white_button_bg);
                            holder.bInvitePeople.setTextColor(Color.BLACK);
                        } else {
                            Toast.makeText(mContext, "Error: " + task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

    }

    @Override
    public int getItemCount() {
        return inviteGroupList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvPersonName, tvUsername;
        private CircleImageView civPersonImage;
        private Button bInvitePeople;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvPersonName = itemView.findViewById(R.id.tvPersonName);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            civPersonImage = itemView.findViewById(R.id.civPersonImage);
            bInvitePeople = itemView.findViewById(R.id.bInvitePeople);
        }
    }
}
