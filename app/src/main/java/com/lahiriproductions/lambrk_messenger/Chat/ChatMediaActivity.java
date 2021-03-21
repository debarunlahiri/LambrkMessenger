package com.lahiriproductions.lambrk_messenger.Chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.lahiriproductions.lambrk_messenger.R;

import java.util.ArrayList;
import java.util.List;

public class ChatMediaActivity extends AppCompatActivity {

    private Context mContext;

    private CardView cvChatMediaBack;
    private ViewPager vpChatMedia;
    private ChatMediaViewPagerAdapter chatMediaViewPagerAdapter;
    private List<Chat> chatList = new ArrayList<>();

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    private String user_id, chat_id, sender_user_id, receiver_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_multiple_media);

        mContext = ChatMediaActivity.this;

        Bundle bundle = getIntent().getExtras();
        chat_id = bundle.getString("chat_id");
        sender_user_id = bundle.getString("sender_user_id");
        receiver_user_id = bundle.getString("receiver_user_id");

        cvChatMediaBack = findViewById(R.id.cvChatMediaBack);
        vpChatMedia = findViewById(R.id.vpChatMedia);
        chatMediaViewPagerAdapter = new ChatMediaViewPagerAdapter(chatList, mContext);
        vpChatMedia.setAdapter(chatMediaViewPagerAdapter);


        mDatabase = FirebaseDatabase.getInstance(mContext.getString(R.string.firebase_url)).getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl(mContext.getString(R.string.storage_reference_url));

        user_id = currentUser.getUid();

        initChatMedias();

        cvChatMediaBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void initChatMedias() {
        mDatabase.child("chats").child(sender_user_id).child(receiver_user_id).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Chat chat = snapshot.getValue(Chat.class);
                if (chat.getMedia_type() != null) {
                    chatList.add(chat);
                }
                chatMediaViewPagerAdapter.notifyDataSetChanged();
                setViewPagerPosition();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setViewPagerPosition() {
        for (int i=0; i<chatList.size(); i++) {
            if (chatList.get(i).getChat_id().equals(chat_id)) {
                vpChatMedia.setCurrentItem(i);
                break;
            }
        }
    }
}