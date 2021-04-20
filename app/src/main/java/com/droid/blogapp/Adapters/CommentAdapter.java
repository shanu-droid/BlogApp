package com.droid.blogapp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.droid.blogapp.Models.Comment;
import com.droid.blogapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private Context mContext;
    private List<Comment>  mData;
    String PostKey;
    String myUid;

    public CommentAdapter(Context mContext, List<Comment> mData, String PostKey) {
        this.mContext = mContext;
        this.mData = mData;
        this.PostKey = PostKey;
    }

    @NonNull
    @Override
    public CommentAdapter.CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(mContext).inflate(R.layout.row_comment, parent, false);
        return new CommentViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.CommentViewHolder holder, int position) {
        String userPhoto = mData.get(position).getUimg();
        if(userPhoto != null){
        Glide.with(mContext).load(userPhoto).into(holder.img_user);
        }else{
            Glide.with(mContext).load(R.drawable.nullprofile).into(holder.img_user);
        }
        holder.tv_username.setText(mData.get(position).getUname());
        holder.tv_content.setText(mData.get(position).getContent());
        holder.tv_date.setText(timeStampToString((long)mData.get(position).getTimestamp()));
        myUid = FirebaseAuth.getInstance().getUid();
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder{
        ImageView img_user;
        TextView tv_username, tv_content,tv_date;
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            int position = getAdapterPosition();
            img_user = itemView.findViewById(R.id.comment_user_image);
            tv_username = itemView.findViewById(R.id.comment_username);
            tv_content = itemView.findViewById(R.id.comment);
            tv_date = itemView.findViewById(R.id.comment_date);
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
                        dialogBuilder.setTitle("Delete!")
                                .setIcon(R.drawable.ic_delete);
                        dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                DatabaseReference dR =  FirebaseDatabase.getInstance().getReference("Comment").child(PostKey);
                                        dR.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot snaps : snapshot.getChildren()) {
                                                    String comment = "" + snaps.child("content").getValue();
                                                    String uid = "" + snaps.child("uid").getValue();
                                                    if (comment.equals(mData.get(position).getContent()) && uid.equals(myUid)) {
                                                        String key = snaps.getKey();
                                                        deleteThroughKey(key);
                                                    }
                                                }

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                            }

                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(mContext.getApplicationContext(), "No, Comment is deleted."
                                        , Toast.LENGTH_SHORT).show();
                            }
                        });

                        dialogBuilder.create().show();

                        return true;
                    }
                });
            }
        }


    private void deleteThroughKey(String key) {
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("Comment").child(PostKey).child(key);
        dR.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(mContext, "Comment deleted Successfully!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private  String timeStampToString(long time){

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        String date = DateFormat.format("hh:mm",calendar).toString();
        return date;
    }
}
