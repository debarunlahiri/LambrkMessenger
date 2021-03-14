package com.lahiriproductions.lambrk_messenger.Notifications;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.lahiriproductions.lambrk_messenger.OverallProfileActivity;
import com.lahiriproductions.lambrk_messenger.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    private static final int FOLLOWING_VIEW = 1;
    private static final int STORY_LIKED_VIEW = 1;
    private static final String TAG = NotificationsAdapter.class.getSimpleName();
    private Context mContext;
    private List<Notifications> notificationsList;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    private String user_id;

    public NotificationsAdapter(Context mContext, List<Notifications> notificationsList) {
        this.mContext = mContext;
        this.notificationsList = notificationsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == FOLLOWING_VIEW) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_notification_following_list_item, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_notification_following_list_item, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notifications notifications = notificationsList.get(position);

        mDatabase = FirebaseDatabase.getInstance(mContext.getString(R.string.firebase_url)).getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl(mContext.getString(R.string.storage_reference_url));

        if (notifications.getNotification_type().equalsIgnoreCase("following_notification")) {
            setFollowingNotifications(holder, notifications);
        } else if (notifications.getNotification_type().equalsIgnoreCase("story_liked_notification")) {
            setStoryLikedNotification(holder, notifications);
        } else if (notifications.getNotification_type().equalsIgnoreCase("story_comment_notification")) {
            setStoryCommentNotifications(holder, notifications);
        }
    }

    private void setStoryCommentNotifications(ViewHolder holder, Notifications notifications) {
        if (currentUser != null) {
            user_id = currentUser.getUid();

            mDatabase.child("users").child(notifications.getFrom_user_id()).child("user_data").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String username = dataSnapshot.child("username").getValue().toString();
                        String profile_image = dataSnapshot.child("profile_image").getValue().toString();

                        Glide.with(mContext).load(profile_image).into(holder.civPersonProfilePic);

                        SpannableStringBuilder str = new SpannableStringBuilder(username);
                        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        holder.tvNotificationMessage.setText(str);
                        holder.tvNotificationMessage.append(" has commented your story");

                        Calendar calendar = Calendar.getInstance();
                        TimeZone tz = TimeZone.getDefault();
                        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault());
                        java.util.Date currenTimeZone=new java.util.Date((long)notifications.getTimestamp());
                        holder.tvNotificationTime.setText(sdf.format(currenTimeZone));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            mDatabase.child("stories").child(notifications.getTo_user_id()).child(notifications.getStory_id()).child("comments").child(notifications.getStory_comment_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.child("comment").exists()) {
                        String comment = snapshot.child("comment").getValue().toString();
                        SpannableStringBuilder str = new SpannableStringBuilder(comment);
                        str.setSpan(new android.text.style.StyleSpan(Typeface.ITALIC), 0, str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        holder.tvNotificationMessage.append("\n" + '"' + str + '"');
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(TAG, "setStoryCommentNotifications: " + error.getMessage());
                }
            });

//            holder.cvFollowingNotification.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent profileIntent = new Intent(mContext, OverallProfileActivity.class);
//                    profileIntent.putExtra("searched_user_id", notifications.getUser_id());
//                    mContext.startActivity(profileIntent);
//                }
//            });

            if (notifications.isHasUserRead()) {
                holder.cvFollowingNotification.setBackgroundColor(mContext.getResources().getColor(R.color.colorWhite));
            } else {
                holder.cvFollowingNotification.setBackgroundColor(mContext.getResources().getColor(R.color.colorVeryLightBlue));
                if (notifications.getNotification_type().equalsIgnoreCase("following_notification")) {
                    mDatabase.child("notifications").child(notifications.getTo_user_id()).child("hasUserRead").setValue(true);
                } else {
                    mDatabase.child("notifications").child(notifications.getNotification_id()).child("hasUserRead").setValue(true);
                }

            }
        }
    }

    private void setStoryLikedNotification(ViewHolder holder, Notifications notifications) {
        if (currentUser != null) {
            user_id = currentUser.getUid();

            mDatabase.child("users").child(notifications.getFrom_user_id()).child("user_data").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String username = dataSnapshot.child("username").getValue().toString();
                        String profile_image = dataSnapshot.child("profile_image").getValue().toString();

                        Glide.with(mContext).load(profile_image).into(holder.civPersonProfilePic);
                        SpannableStringBuilder str = new SpannableStringBuilder(username);
                        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        holder.tvNotificationMessage.setText(str);
                        holder.tvNotificationMessage.append(" has liked your story");

                        Calendar calendar = Calendar.getInstance();
                        TimeZone tz = TimeZone.getDefault();
                        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault());
                        java.util.Date currenTimeZone=new java.util.Date((long)notifications.getTimestamp());
                        holder.tvNotificationTime.setText(sdf.format(currenTimeZone));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

//            holder.cvFollowingNotification.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent profileIntent = new Intent(mContext, OverallProfileActivity.class);
//                    profileIntent.putExtra("searched_user_id", notifications.getUser_id());
//                    mContext.startActivity(profileIntent);
//                }
//            });

            if (notifications.isHasUserRead()) {
                holder.cvFollowingNotification.setBackgroundColor(mContext.getResources().getColor(R.color.colorWhite));
            } else {
                holder.cvFollowingNotification.setBackgroundColor(mContext.getResources().getColor(R.color.colorVeryLightBlue));
//                mDatabase.child("notifications").child("following").child(notifications.getSearched_user_id())
//                        .child(notifications.getUser_id()).child("hasUserRead").setValue(true);
            }
        }
    }

    private void setFollowingNotifications(ViewHolder holder, Notifications notifications) {
        if (currentUser != null) {
            user_id = currentUser.getUid();

            mDatabase.child("users").child(notifications.getFrom_user_id()).child("user_data").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String username = dataSnapshot.child("username").getValue().toString();
                        String profile_image = dataSnapshot.child("profile_image").getValue().toString();

                        Glide.with(mContext).load(profile_image).into(holder.civPersonProfilePic);
                        SpannableStringBuilder str = new SpannableStringBuilder(username);
                        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        holder.tvNotificationMessage.setText(str);
                        holder.tvNotificationMessage.append(" has followed you");

                        Calendar calendar = Calendar.getInstance();
                        TimeZone tz = TimeZone.getDefault();
                        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault());
                        java.util.Date currenTimeZone=new java.util.Date((long)notifications.getTimestamp());
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
                    profileIntent.putExtra("searched_user_id", notifications.getFrom_user_id());
                    mContext.startActivity(profileIntent);
                }
            });

            if (notifications.isHasUserRead()) {
                holder.cvFollowingNotification.setBackgroundColor(mContext.getResources().getColor(R.color.colorWhite));
            } else {
                holder.cvFollowingNotification.setBackgroundColor(mContext.getResources().getColor(R.color.colorVeryLightBlue));
//                mDatabase.child("notifications").child("following").child(notifications.getSearched_user_id())
//                        .child(notifications.getUser_id()).child("hasUserRead").setValue(true);
            }
        }
    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    @Override
    public int getItemViewType(int position) {
//        if (notificationsList.get(position).getNotification_type().equalsIgnoreCase("following_notification")) {
//            return FOLLOWING_VIEW;
//        } else {
//            return STORY_LIKED_VIEW;
//        }
        return FOLLOWING_VIEW;
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
