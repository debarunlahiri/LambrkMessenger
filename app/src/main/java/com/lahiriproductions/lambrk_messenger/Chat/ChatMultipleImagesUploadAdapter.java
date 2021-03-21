package com.lahiriproductions.lambrk_messenger.Chat;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lahiriproductions.lambrk_messenger.R;

import java.util.List;

public class ChatMultipleImagesUploadAdapter extends RecyclerView.Adapter<ChatMultipleImagesUploadAdapter.ViewHolder> {

    private Context mContext;
    private List<Uri> chatMultipleImagesUploadList;

    private final OnItemClickListener listener;

    public ChatMultipleImagesUploadAdapter(Context mContext, List<Uri> chatMultipleImagesUploadList, OnItemClickListener listener) {
        this.mContext = mContext;
        this.chatMultipleImagesUploadList = chatMultipleImagesUploadList;
        this.listener = listener;
    }

    public void setChatMultipleImagesUploadList(List<Uri> chatMultipleImagesUploadList) {
        this.chatMultipleImagesUploadList = chatMultipleImagesUploadList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_chat_image_upload_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Uri chatMultipleImagesUpload = chatMultipleImagesUploadList.get(position);

        Glide.with(mContext).load(chatMultipleImagesUpload).into(holder.ivImageUpload);

        holder.cvImageUploadClose.setElevation(10);
        holder.ibImageUploadClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(chatMultipleImagesUpload, position, chatMultipleImagesUploadList);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatMultipleImagesUploadList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageButton ibImageUploadClose;
        private CardView cvImageUpload, cvImageUploadClose;
        private ImageView ivImageUpload;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ibImageUploadClose = itemView.findViewById(R.id.ibImageUploadClose);
            cvImageUpload = itemView.findViewById(R.id.cvImageUpload);
            ivImageUpload = itemView.findViewById(R.id.ivImageUpload);
            cvImageUploadClose = itemView.findViewById(R.id.cvImageUploadClose);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Uri uri, int position, List<Uri> chatMultipleImagesUploadList);
    }
}
