package com.droid.blogapp.Activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.droid.blogapp.Adapters.CommentAdapter;
import com.droid.blogapp.Models.Comment;
import com.droid.blogapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {
    ImageView imgPost, imgUserPost, imgCurrentUser;
    TextView txtPostDesc, txtPostDateName, txtPostTitle;
    EditText editTextComment;
    Button  btnAddComment;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    String PostKey;
    RecyclerView RvComment;
    CommentAdapter commentAdapter;
    List<Comment> commentList;
    static  String COMMENT_KEY = "Comment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        // let's set the status bar to transparent
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getSupportActionBar().hide();

        RvComment = findViewById(R.id.rv_comment);


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();

        imgPost = findViewById(R.id.post_detail_image);
        imgUserPost = findViewById(R.id.post_detail_user_img);
        imgCurrentUser = findViewById(R.id.post_detail_currentuser);

        txtPostDesc = findViewById(R.id.post_detail_desc);
        txtPostDateName = findViewById(R.id.post_detail_date_name);
        txtPostTitle = findViewById(R.id.post_detail_title);

        btnAddComment = findViewById(R.id.post_detail_add_button);
        editTextComment = findViewById(R.id.post_detail_comment);

         // on add comment click listener
        btnAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnAddComment.setVisibility(View.INVISIBLE);
                DatabaseReference commentReference = firebaseDatabase.getReference(COMMENT_KEY).child(PostKey).push();
                String comment_content = editTextComment.getText().toString();
                String uid = firebaseUser.getUid();
                String uname = firebaseUser.getDisplayName();
                String uimg = "";
                if(firebaseUser.getPhotoUrl() != null){
                    uimg = firebaseUser.getPhotoUrl().toString();
                }else{
                    uimg = "https://rajcooling.com/wp-content/uploads/sites/270/2016/05/avatar.png";
                }
                Comment comment = new Comment(comment_content, uid, uimg, uname);
                commentReference.setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        showMessage("comment added");
                        editTextComment.setText("");
                        btnAddComment.setVisibility(View.VISIBLE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showMessage("Fail to add comment : " + e.getMessage());
                    }
                });


            }
        });

        String postImage = getIntent().getExtras().getString("postImage");
        Glide.with(this).load(postImage).into(imgPost);

        String userImage = getIntent().getExtras().getString("userPhoto");
        if(userImage != null){
            Glide.with(this).load(userImage).into(imgUserPost);
        }else{
            Glide.with(this).load(R.drawable.nullprofile).into(imgUserPost);
        }

        String postTitle = getIntent().getExtras().getString("title");
        txtPostTitle.setText(postTitle);

        String postDesc = getIntent().getExtras().getString("description");
        txtPostDesc.setText(postDesc);

        if(firebaseUser.getPhotoUrl() != null){
            Glide.with(this).load(firebaseUser.getPhotoUrl()).into(imgCurrentUser);
        }else{
            Glide.with(this).load(R.drawable.nullprofile).into(imgCurrentUser);
        }

        PostKey = getIntent().getExtras().getString("postKey");

        String postDate = timeStampToString(getIntent().getExtras().getLong("postDate"));
        String postUserName = getIntent().getExtras().getString("userName");
        txtPostDateName.setText(String.format("%s | %s", postDate, postUserName));

        iniRvComment();



    }

    private void iniRvComment() {
        RvComment.setLayoutManager(new LinearLayoutManager(this));
        DatabaseReference commentRef = firebaseDatabase.getReference(COMMENT_KEY).child(PostKey);
        commentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                commentList = new ArrayList<>();
                for(DataSnapshot snaps: snapshot.getChildren()){

                    Comment comment = snaps.getValue(Comment.class);
                    Log.e("com",comment.toString());
                    commentList.add(comment);

                }
                commentAdapter = new CommentAdapter(PostDetailActivity.this , commentList, PostKey);
                RvComment.setAdapter(commentAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private  String timeStampToString(long time){

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy",calendar).toString();
        return date;
    }
}