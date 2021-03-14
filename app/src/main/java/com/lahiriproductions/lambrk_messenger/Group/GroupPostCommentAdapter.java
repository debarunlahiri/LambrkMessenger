package com.lahiriproductions.lambrk_messenger.Group;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lahiriproductions.lambrk_messenger.R;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.android.gms.tasks.OnFailureListener;
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

public class GroupPostCommentAdapter extends RecyclerView.Adapter<GroupPostCommentAdapter.ViewHolder> {

    private List<GroupPostComment> groupPostCommentList;
    private Context mContext;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    private String user_id;

    public GroupPostCommentAdapter(List<GroupPostComment> groupPostCommentList, Context mContext) {
        this.groupPostCommentList = groupPostCommentList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupPostComment groupPostComment = groupPostCommentList.get(position);

        mDatabase = FirebaseDatabase.getInstance(mContext.getString(R.string.firebase_url)).getReference();;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl(mContext.getString(R.string.storage_reference_url));

        user_id = currentUser.getUid();

        fetchCommentDetails(holder, groupPostComment);
        fetchUserDetails(holder, groupPostComment);
        countLikes(holder, groupPostComment);

        mDatabase.child("groups").child(groupPostComment.getGroup_id()).child("posts").child(groupPostComment.getGroup_post_id()).child("comments")
                .child(groupPostComment.getComment_id()).child("likes").child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    groupPostComment.setUserHasLikedComment(true);
                    holder.tvCommentLike.setText("Liked");
                } else {
                    groupPostComment.setUserHasLikedComment(false);
                    holder.tvCommentLike.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.tvCommentLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (groupPostComment.isUserHasLikedComment()) {
                    mDatabase.child("groups").child(groupPostComment.getGroup_id()).child("posts").child(groupPostComment.getGroup_post_id())
                            .child("comments").child(groupPostComment.getComment_id()).child("likes")
                            .child(user_id).removeValue();
                    holder.tvCommentLike.setText("Like");
                } else if (!groupPostComment.isUserHasLikedComment()) {
                    HashMap<String, Object> mGroupPostCommentLikeDataMap = new HashMap<>();
                    mGroupPostCommentLikeDataMap.put("group_id", groupPostComment.getGroup_id());
                    mGroupPostCommentLikeDataMap.put("group_post_id", groupPostComment.getGroup_post_id());
                    mGroupPostCommentLikeDataMap.put("group_post_comment_id", groupPostComment.getComment_id());
                    mGroupPostCommentLikeDataMap.put("timestamp", System.currentTimeMillis());
                    mGroupPostCommentLikeDataMap.put("user_id", user_id);
                    mGroupPostCommentLikeDataMap.put("comment_id", groupPostComment.getComment_id());
                    mDatabase.child("groups").child(groupPostComment.getGroup_id()).child("posts").child(groupPostComment.getGroup_post_id())
                            .child("comments").child(groupPostComment.getComment_id()).child("likes").child(user_id).setValue(mGroupPostCommentLikeDataMap).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(mContext, "Failure: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                    holder.tvCommentLike.setText("Liked");
                }
            }
        });
    }

    private void countLikes(ViewHolder holder, GroupPostComment groupPostComment) {
        mDatabase.child("groups").child(groupPostComment.getGroup_id()).child("posts").child(groupPostComment.getGroup_post_id())
                .child("comments").child(groupPostComment.getComment_id()).child("likes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count_likes = (int) dataSnapshot.getChildrenCount();
                if (count_likes == 1) {
                    holder.tvCommentLikeCounter.setText(count_likes + " Like");
                } else {
                    holder.tvCommentLikeCounter.setText(count_likes + " Likes");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchUserDetails(ViewHolder holder, GroupPostComment groupPostComment) {
        mDatabase.child("users").child(groupPostComment.getUser_id()).child("user_data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("username").getValue().toString();
                holder.tvCommentName.setText(username);
                if (dataSnapshot.child("profile_image") == null) {
                    Glide.with(mContext).load(R.drawable.default_profile_pic).into(holder.commentprofileCIV);
                } else {
                    String profile_image = dataSnapshot.child("profile_image").getValue().toString();
                    Glide.with(mContext).load(profile_image).thumbnail(0.1f).into(holder.commentprofileCIV);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchCommentDetails(ViewHolder holder, GroupPostComment groupPostComment) {
        holder.tvCommentBody.setText(groupPostComment.getBody());
        holder.tvCommentPosted.setReferenceTime(groupPostComment.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return groupPostCommentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CardView commentCV;
        private TextView tvCommentName, tvCommentBody, tvCommentLike, tvCommentLikeCounter;
        private RelativeTimeTextView tvCommentPosted;
        private CircleImageView commentprofileCIV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            commentCV = itemView.findViewById(R.id.commentCV);
            tvCommentName = itemView.findViewById(R.id.tvCommentName);
            tvCommentBody = itemView.findViewById(R.id.tvCommentBody);
            tvCommentLike = itemView.findViewById(R.id.tvCommentLike);
            tvCommentLikeCounter = itemView.findViewById(R.id.tvCommentLikeCounter);
            tvCommentPosted = itemView.findViewById(R.id.tvCommentPosted);
            commentprofileCIV = itemView.findViewById(R.id.commentprofileCIV);
        }
    }
}
