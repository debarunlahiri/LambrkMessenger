package com.lambrk.messenger.Chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jsibbold.zoomage.ZoomageView;
import com.lambrk.messenger.R;

import java.util.List;

public class ChatMediaViewPagerAdapter extends PagerAdapter {

    private List<Chat> chatList;
    private Context mContext;
    private LayoutInflater layoutInflater;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    private String user_id;

    public ChatMediaViewPagerAdapter(List<Chat> chatList, Context mContext) {
        this.chatList = chatList;
        this.mContext = mContext;
        layoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return chatList.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = layoutInflater.inflate(R.layout.item_list_chat_media_layout, container, false);
        Chat chat = chatList.get(position);
        ZoomageView ivVPChatMediaImage = view.findViewById(R.id.ivVPChatMediaImage);
        Glide.with(mContext).load(chat.getMedia()).into(ivVPChatMediaImage);
        container.addView(view);
        return view;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
