package com.lambrk.messenger.Inbox;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lambrk.messenger.Chat.ChatActivity;
import com.lambrk.messenger.R;
import com.lambrk.messenger.Utils.TimeAgo;
import com.github.curioustechizen.ago.RelativeTimeTextView;
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

public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.ViewHolder> {

    private static final String TAG = InboxAdapter.class.getSimpleName();
    private List<Inbox> inboxList;
    private Context mContext;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    private String user_id;
    public boolean isOnline = false;

    public InboxAdapter(List<Inbox> inboxList, Context mContext) {
        this.inboxList = inboxList;
        this.mContext = mContext;
    }
    //check

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.inbox_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Inbox inbox = inboxList.get(position);

        mDatabase = FirebaseDatabase.getInstance(mContext.getString(R.string.firebase_url)).getReference();;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl(mContext.getString(R.string.storage_reference_url));

        user_id = currentUser.getUid();

        setInbox(holder, position);
        setUserDetails(holder, position);
        checkUserOnlineOrNot(holder, position);

        holder.inboxCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inbox.isHas_seen() == false) {
                    mDatabase.child("chats").child(inbox.getSender_user_id()).child(inbox.getReceiver_user_id()).child(inbox.getChat_id()).child("has_seen").setValue(true);
                    mDatabase.child("chats").child(inbox.getReceiver_user_id()).child(inbox.getSender_user_id()).child(inbox.getChat_id()).child("has_seen").setValue(true);
                    //Toast.makeText(mContext, inbox.getSender_user_id() + "   " + inbox.getReceiver_user_id(), Toast.LENGTH_LONG).show();
                }
                removeChatNotifications(holder, inbox);
                Intent chatIntent = new Intent(mContext, ChatActivity.class);
                chatIntent.putExtra("searched_user_id", inbox.getUser_key());
                mContext.startActivity(chatIntent);
            }
        });

        holder.inboxShowTimeAgoCV.setVisibility(View.GONE);

    }

    private void removeChatNotifications(ViewHolder holder, Inbox inbox) {
        if (inbox.getReceiver_user_id().equals(user_id)) {
            mDatabase.child("notifications").child(inbox.getReceiver_user_id()).child(inbox.getSender_user_id()).removeValue();
        }

    }

    private void checkUserOnlineOrNot(ViewHolder holder, int position) {
        Inbox inbox = inboxList.get(position);
        mDatabase.child("active").child(inbox.getUser_key()).addValueEventListener(new ValueEventListener() {
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

    private void setWhoMessagedIdentification(ViewHolder holder, int position) {
        Inbox inbox = inboxList.get(position);

    }

    private void setUserDetails(ViewHolder holder, int position) {
        Inbox inbox = inboxList.get(position);
        mDatabase.child("users").child(inbox.getUser_key()).child("user_data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String profile_image = dataSnapshot.child("profile_image").getValue().toString();
                    holder.tvInboxName.setText(name);
                    Glide.with(mContext).load(profile_image).thumbnail(0.1f).into(holder.inboxprofileCIV);
                } else {
                    holder.tvInboxName.setText("Unknown user");
                    Glide.with(mContext).load(R.drawable.default_profile_pic).into(holder.inboxprofileCIV);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setInbox(ViewHolder holder, int position) {
        Inbox inbox = inboxList.get(position);
        if (inbox.isHas_seen() == false && inbox.getReceiver_user_id().equals(user_id)) {
            holder.tvInboxMessage.setTypeface(null, Typeface.BOLD);
            holder.inboxUnreadMessageIndicatorIV.setVisibility(View.VISIBLE);
        } else {
            holder.tvInboxMessage.setTypeface(null, Typeface.NORMAL);
            holder.inboxUnreadMessageIndicatorIV.setVisibility(View.GONE);
        }
        mDatabase.child("chats").child(inbox.getSender_user_id()).child(inbox.getReceiver_user_id()).child(inbox.getChat_id()).limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if (inbox.getSender_user_id().equals(user_id)) {
            if (inbox.getMedia_type() != null) {
                holder.tvInboxMessage.setText("You: " + inbox.getMedia_type());
            } else {
                holder.tvInboxMessage.setText("You: " + inbox.getMessage());
            }
        } else {
            if (inbox.getMedia_type() != null) {
                holder.tvInboxMessage.setText(inbox.getMedia_type());
            } else {
                holder.tvInboxMessage.setText(inbox.getMessage());
            }
        }
        holder.tvInboxTime.setReferenceTime(inbox.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return inboxList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView inboxprofileCIV;
        private TextView tvInboxName, tvInboxMessage, tvInboxShowShortTimeAgo;
        private RelativeTimeTextView tvInboxTime;
        private CardView inboxCV, inboxuseronlineCV, inboxShowTimeAgoCV;
        private ImageView inboxUnreadMessageIndicatorIV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            inboxprofileCIV = itemView.findViewById(R.id.inboxprofileCIV);
            tvInboxName = itemView.findViewById(R.id.tvInboxName);
            tvInboxMessage = itemView.findViewById(R.id.tvInboxMessage);
            tvInboxTime = itemView.findViewById(R.id.tvInboxTime);
            inboxCV = itemView.findViewById(R.id.inboxCV);
            inboxuseronlineCV = itemView.findViewById(R.id.inboxuseronlineCV);
            inboxUnreadMessageIndicatorIV = itemView.findViewById(R.id.inboxUnreadMessageIndicatorIV);
            inboxShowTimeAgoCV = itemView.findViewById(R.id.inboxShowTimeAgoCV);
            tvInboxShowShortTimeAgo = itemView.findViewById(R.id.tvInboxShowShortTimeAgo);

        }
    }
}
