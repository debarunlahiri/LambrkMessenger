package com.lahiriproductions.lambrk_messenger.Explore;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lahiriproductions.lambrk_messenger.OverallProfileActivity;
import com.lahiriproductions.lambrk_messenger.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ExploreAdapter extends RecyclerView.Adapter<ExploreAdapter.ViewHolder> {

    private Context mContext;
    private List<Explore> exploreList;
    private List<String> userKeysList;

    public ExploreAdapter(Context mContext, List<Explore> exploreList, List<String> userKeysList) {
        this.mContext = mContext;
        this.exploreList = exploreList;
        this.userKeysList = userKeysList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_explore_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Explore explore = exploreList.get(position);
        String user_id = userKeysList.get(position);

        setUserData(explore, holder);

        holder.cvExplore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(mContext, OverallProfileActivity.class);
                profileIntent.putExtra("searched_user_id", user_id);
                mContext.startActivity(profileIntent);
            }
        });

    }

    private void setUserData(Explore explore, ViewHolder holder) {
        holder.tvPersonName.setText(explore.getName());
        holder.tvPersonUsername.setText("@" + explore.getUsername());
        Glide.with(mContext).load(explore.getProfile_image()).into(holder.civExploreProfilePic);

        if (explore.getProfile_background_image() != null) {
            Glide.with(mContext).load(explore.getProfile_background_image()).into(holder.ivExploreBG);
        }
    }

    @Override
    public int getItemCount() {
        return exploreList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivExploreBG;
        private CircleImageView civExploreProfilePic;
        private TextView tvPersonName, tvPersonUsername;
        private CardView cvExplore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivExploreBG = itemView.findViewById(R.id.ivExploreBG);
            civExploreProfilePic = itemView.findViewById(R.id.civExploreProfilePic);
            tvPersonName = itemView.findViewById(R.id.tvPersonName);
            tvPersonUsername = itemView.findViewById(R.id.tvPersonUsername);
            cvExplore = itemView.findViewById(R.id.cvExplore);
        }
    }
}
