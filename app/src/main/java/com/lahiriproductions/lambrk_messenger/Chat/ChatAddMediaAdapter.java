package com.lahiriproductions.lambrk_messenger.Chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lahiriproductions.lambrk_messenger.R;

import java.util.List;

public class ChatAddMediaAdapter extends RecyclerView.Adapter<ChatAddMediaAdapter.ViewHolder> {

    private Context mContext;
    private List<ChatAddMedia> chatAddMediaList;

    public ChatAddMediaAdapter(Context mContext, List<ChatAddMedia> chatAddMediaList) {
        this.mContext = mContext;
        this.chatAddMediaList = chatAddMediaList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_add_chat_media_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatAddMedia chatAddMedia = chatAddMediaList.get(position);

        Glide.with(mContext).load(chatAddMedia.getImage()).into(holder.ivChatAddMediaIcon);
        holder.tvChatAddMediaName.setText(chatAddMedia.getName());
    }

    @Override
    public int getItemCount() {
        return chatAddMediaList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivChatAddMediaIcon;
        private TextView tvChatAddMediaName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivChatAddMediaIcon = itemView.findViewById(R.id.ivChatAddMediaIcon);
            tvChatAddMediaName = itemView.findViewById(R.id.tvChatAddMediaName);
        }
    }
}
