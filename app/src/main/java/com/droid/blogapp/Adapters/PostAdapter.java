package com.droid.blogapp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.droid.blogapp.Activites.PostDetailActivity;
import com.droid.blogapp.Models.post;
import com.droid.blogapp.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyViewHolder> {
    static Context mContext;
   static List<post>  mData;

    public PostAdapter(Context mContext, List<post> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(mContext).inflate(R.layout.row_post_item, parent, false);


        return new MyViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
       holder.postTitle.setText(mData.get(position).getTitle());
       holder.postUsername.setText(mData.get(position).getUserName());
        Glide.with(mContext).load(mData.get(position).getPictures()).into(holder.imgPost);
        String userProfile = mData.get(position).getUserPhotos();
        if(userProfile != null) {
            Glide.with(mContext).load(userProfile).into(holder.imgPostProfile);
        }else{
            Glide.with(mContext).load(R.drawable.nullprofile).into(holder.imgPostProfile);
        }

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView postTitle;
        TextView postUsername;
        ImageView imgPost;
        ImageView imgPostProfile;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            postTitle = itemView.findViewById(R.id.row_post_title);
            postUsername = itemView.findViewById(R.id.row_post_username);
            imgPost = itemView.findViewById(R.id.row_post_image);
            imgPostProfile = itemView.findViewById(R.id.raw_post_user_image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent postDetailActivity = new Intent(mContext, PostDetailActivity.class);
                    int position = getAdapterPosition();

                    postDetailActivity.putExtra("title", mData.get(position).getTitle());
                    postDetailActivity.putExtra("postImage", mData.get(position).getPictures());
                    postDetailActivity.putExtra("description", mData.get(position).getDescription());
                    postDetailActivity.putExtra("postKey", mData.get(position).getPostKey());
                    postDetailActivity.putExtra("userPhoto", mData.get(position).getUserPhotos());
                    postDetailActivity.putExtra("userName", mData.get(position).getUserName());

                    long timeStamp = (long) mData.get(position).getTimeStamp();
                    postDetailActivity.putExtra("postDate", timeStamp);
                    mContext.startActivity(postDetailActivity);
                }
            });
           itemView.setOnLongClickListener(new View.OnLongClickListener() {
               @Override
               public boolean onLongClick(View v) {
                   int position = getAdapterPosition();
                   final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
                   dialogBuilder.setTitle("Delete!")
                   .setIcon(R.drawable.ic_delete);
                   dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {

                           DatabaseReference dR = FirebaseDatabase.getInstance().getReference("Posts").child(mData.get(position).getPostKey());
                           dR.removeValue();
                           DatabaseReference cR = FirebaseDatabase.getInstance().getReference("Comment").child(mData.get(position).getPostKey());
                           cR.removeValue();
                       }
                   }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                           Toast.makeText(mContext.getApplicationContext(), "No, Post is deleted."
                                   , Toast.LENGTH_SHORT).show();
                       }
                   });

                   dialogBuilder.create().show();

                   return true;
               }
           });
        }
    }
}
