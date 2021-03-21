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
import com.bumptech.glide.request.RequestOptions;
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

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class ExploreUsersAdapter extends RecyclerView.Adapter<ExploreUsersAdapter.ViewHolder> {

    private Context mContext;
    private List<Explore> exploreList;

    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference storageReference;

    private String user_id;

    public ExploreUsersAdapter(Context mContext, List<Explore> exploreList) {
        this.mContext = mContext;
        this.exploreList = exploreList;
    }

    public void setExploreList(List<Explore> exploreList) {
        this.exploreList = exploreList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_users_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Explore explore = exploreList.get(position);

        mDatabase = FirebaseDatabase.getInstance(mContext.getString(R.string.firebase_url)).getReference();;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReferenceFromUrl(mContext.getString(R.string.storage_reference_url));

        setUsersData(holder, explore);
    }

    private void setUsersData(ViewHolder holder, Explore explore) {
        if (explore.getUser_id() != null) {
            mDatabase.child("users").child(explore.getUser_id()).child("user_data").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String username = snapshot.child("username").getValue().toString();
                        String thumb_profile_image = snapshot.child("thumb_profile_image").getValue().toString();
                        String name = snapshot.child("name").getValue().toString();

                        holder.tvPersonName.setText(name);
                        holder.tvPersonUsername.setText("@" + username);
                        Glide.with(mContext).load(thumb_profile_image).into(holder.civExploreProfilePic);
                        if (snapshot.child("profile_background_image").exists()) {
                            String profile_background_image = snapshot.child("profile_background_image").getValue().toString();
                            Glide.with(mContext).load(profile_background_image).apply(RequestOptions.bitmapTransform(new BlurTransformation(16, 6))).into(holder.ivExploreBG);
                        } else {
                            Glide.with(mContext).load(R.drawable.profile_background_gradient).apply(RequestOptions.bitmapTransform(new BlurTransformation(16, 6))).into(holder.ivExploreBG);
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            holder.cvExplore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent profileIntent = new Intent(mContext, OverallProfileActivity.class);
                    profileIntent.putExtra("searched_user_id", explore.getUser_id());
                    mContext.startActivity(profileIntent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return exploreList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CardView cvExplore;
        private ImageView ivExploreBG;
        private CircleImageView civExploreProfilePic;
        private TextView tvPersonName, tvPersonUsername;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cvExplore = itemView.findViewById(R.id.cvExplore);
            ivExploreBG = itemView.findViewById(R.id.ivExploreBG);
            civExploreProfilePic = itemView.findViewById(R.id.civExploreProfilePic);
            tvPersonName = itemView.findViewById(R.id.tvPersonName);
            tvPersonUsername = itemView.findViewById(R.id.tvPersonUsername);
            cvExplore = itemView.findViewById(R.id.cvExplore);

        }
    }
}
