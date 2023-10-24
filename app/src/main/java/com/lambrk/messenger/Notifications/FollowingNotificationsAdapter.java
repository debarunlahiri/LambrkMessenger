package com.lambrk.messenger.Notifications;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lambrk.messenger.OverallProfileActivity;
import com.lambrk.messenger.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class FollowingNotificationsAdapter extends RecyclerView.Adapter<FollowingNotificationsAdapter.ViewHolder> {

    private Context mContext;
    private List<FollowingNotifications> followingNotificationsList;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    private String user_id;

    public FollowingNotificationsAdapter(Context mContext, List<FollowingNotifications> followingNotificationsList) {
        this.mContext = mContext;
        this.followingNotificationsList = followingNotificationsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_notification_following_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FollowingNotifications followingNotifications = followingNotificationsList.get(position);

        mDatabase = FirebaseDatabase.getInstance(mContext.getString(R.string.firebase_url)).getReference();;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl(mContext.getString(R.string.storage_reference_url));

        if (currentUser != null) {
            user_id = currentUser.getUid();

            mDatabase.child("users").child(followingNotifications.getUser_id()).child("user_data").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String username = dataSnapshot.child("username").getValue().toString();
                        String profile_image = dataSnapshot.child("profile_image").getValue().toString();

                        Glide.with(mContext).load(profile_image).into(holder.civPersonProfilePic);
                        String sourceString = "</b>" + username + "</b>" + " has followed you";
                        holder.tvNotificationMessage.setText(Html.fromHtml(sourceString));

                        Calendar calendar = Calendar.getInstance();
                        TimeZone tz = TimeZone.getDefault();
                        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault());
                        java.util.Date currenTimeZone=new java.util.Date((long)followingNotifications.getTimestamp());
                        holder.tvNotificationTime.setText(sdf.format(currenTimeZone));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            holder.cvFollowingNotification.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent profileIntent = new Intent(mContext, OverallProfileActivity.class);
                    profileIntent.putExtra("searched_user_id", followingNotifications.getUser_id());
                    mContext.startActivity(profileIntent);
                }
            });

            if (followingNotifications.isHasUserRead()) {
                holder.cvFollowingNotification.setBackgroundColor(mContext.getResources().getColor(R.color.colorWhite));
            } else {
                holder.cvFollowingNotification.setBackgroundColor(mContext.getResources().getColor(R.color.colorVeryLightBlue));
                mDatabase.child("notifications").child("following").child(followingNotifications.getSearched_user_id())
                        .child(followingNotifications.getUser_id()).child("hasUserRead").setValue(true);
            }
        }
    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time * 1000);
        String date = DateFormat.format("dd MMMM YYYY hh:mm a", cal).toString();
        return date;
    }

    @Override
    public int getItemCount() {
        return followingNotificationsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView civPersonProfilePic;
        private TextView tvNotificationMessage, tvNotificationTime;
        private FrameLayout cvFollowingNotification;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            civPersonProfilePic = itemView.findViewById(R.id.civPersonProfilePic);
            tvNotificationMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvNotificationTime = itemView.findViewById(R.id.tvNotificationTime);
            cvFollowingNotification = itemView.findViewById(R.id.cvFollowingNotification);
        }
    }
}
