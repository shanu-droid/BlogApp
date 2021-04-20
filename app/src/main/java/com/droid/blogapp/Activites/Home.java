package com.droid.blogapp.Activites;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.droid.blogapp.Models.post;
import com.droid.blogapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
private DrawerLayout drawer;
FirebaseUser currentUser;
FirebaseAuth mAuth;
Dialog popAddPost;
ImageView popupUserImage, popupPostImage, popupAddBtn;
TextView popupTitle, popupDescription;
ProgressBar popupClickProgress;
    static private final int PReqCode = 1;
    static private final int REQUESTCODE = 1;
    private Uri pickedImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        //ini popup
        iniPopup();
        setupPopupImageClick();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer =findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        updateNavHeader();


        FloatingActionButton fab = findViewById(R.id.fab_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popAddPost.show();
            }
        });


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // set home fragment as default one
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment__container,new HomeFragment()).commit();
    }


    private void iniPopup() {
        popAddPost = new Dialog(this);
        popAddPost.setContentView(R.layout.popup_add_post);
        popAddPost.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popAddPost.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
        popAddPost.getWindow().getAttributes().gravity = Gravity.TOP;

        popupUserImage = popAddPost.findViewById(R.id.popup_userimg);
        popupPostImage = popAddPost.findViewById(R.id.popup_img);
        popupTitle = popAddPost.findViewById(R.id.postTitle);
        popupDescription = popAddPost.findViewById(R.id.popup_description);
        popupAddBtn = popAddPost.findViewById(R.id.popup_add);
        popupClickProgress = popAddPost.findViewById(R.id.popup_progressBar);

        if(currentUser.getPhotoUrl() != null) {
            Glide.with(Home.this).load(currentUser.getPhotoUrl()).into(popupUserImage);
        }else{
            Glide.with(Home.this).load(R.drawable.nullprofile).into(popupUserImage);
        }

        popupAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupAddBtn.setVisibility(View.INVISIBLE);
                popupClickProgress.setVisibility(View.VISIBLE);
                Log.e("INFO", "Button is clicked");

                //check no filed is empty
                if(!popupTitle.getText().toString().isEmpty() && !popupDescription.getText().toString().isEmpty() && pickedImageUri != null){
                    //TODO create Post Object and add it to firebase database

                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("blog_image");
                    final StorageReference imageFilePath = storageReference.child(pickedImageUri.getLastPathSegment());
                    imageFilePath.putFile(pickedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageDownloadLink = uri.toString();

                                    if( currentUser.getPhotoUrl() != null){
                                        post mypost = new post(popupTitle.getText().toString(),
                                                popupDescription.getText().toString(),
                                                imageDownloadLink, currentUser.getUid(),
                                                currentUser.getPhotoUrl().toString(),
                                                currentUser.getDisplayName());
                                        addPost(mypost);

                                    }else{
                                        post mypost = new post(popupTitle.getText().toString(),
                                                popupDescription.getText().toString(),
                                                imageDownloadLink, currentUser.getUid(),
                                               null,
                                                currentUser.getDisplayName());
                                        addPost(mypost);

                                    }


                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // something went wrong

                                    showMessage(e.getMessage());
                                    popupClickProgress.setVisibility(View.INVISIBLE);
                                    popupAddBtn.setVisibility(View.VISIBLE);


                                }
                            });
                        }
                    });

                }else{
                    showMessage("Please verify all input field and choose post image");
                    popupClickProgress.setVisibility(View.INVISIBLE);
                    popupAddBtn.setVisibility(View.VISIBLE);
                }

            }
        });

    }

    private void setupPopupImageClick() {
        popupPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
           checkAndRequestforPermisson();
            }
        });
    }

    public void addPost(post post){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Posts").push();

        String key = myRef.getKey();
        post.setPostKey(key);

        myRef.setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
              showMessage("Post Added Successfully");
              popupClickProgress.setVisibility(View.INVISIBLE);
              popupAddBtn.setVisibility(View.VISIBLE);
              popAddPost.dismiss();
            }
        });

    }


    private void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.nav_home:
                getSupportActionBar().setTitle("Home");
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment__container, new HomeFragment()).commit();
                break;
            case R.id.nav_profile:
                getSupportActionBar().setTitle("Profile");
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment__container, new ProfileFragment()).commit();
                break;
            case R.id.nav_setting:
                getSupportActionBar().setTitle("Settings");
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment__container, new SettingFragment()).commit();
                break;
            case R.id.nav_signout:
                FirebaseAuth.getInstance().signOut();
                Intent loginActivity = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(loginActivity);
                finish();
                break;
//            case R.id.nav_share:
//                Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show();
//                break;
//            case R.id.nav_logout:
//                Toast.makeText(this, "Log Out", Toast.LENGTH_SHORT).show();
//                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){ // if navigation drawer is in right side then GravityCompat.END
            drawer.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }
    }
    public void updateNavHeader(){
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headView = navigationView.getHeaderView(0);
        TextView navUsername = headView.findViewById(R.id.user_name);
        TextView navUserMail = headView.findViewById(R.id.user_mail);
        ImageView navUserImage = headView.findViewById(R.id.user_photo);

        navUsername.setText(currentUser.getDisplayName());
        navUserMail.setText(currentUser.getEmail());

        if(currentUser.getPhotoUrl() != null){
            // user Glide to load image of user
            Glide.with(this).load(currentUser.getPhotoUrl()).into(navUserImage);
        }else{
            Glide.with(this).load(R.drawable.nullprofile).into(navUserImage);
        }



    }
    private void checkAndRequestforPermisson() {
        if(ContextCompat.checkSelfPermission(Home.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(Home.this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                Toast.makeText(Home.this, "Please accept for required permission", Toast.LENGTH_SHORT).show();
            }else{
                ActivityCompat.requestPermissions(Home.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PReqCode);
            }
        }else{
            openGallery();
        }

    }
    private void openGallery() {
        //TODO: open gallery instant and wait for user to pick an image
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUESTCODE);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == REQUESTCODE && data!= null){
            // user has successfully select the image
            // we have to save it referred to a Uri variable
            pickedImageUri = data.getData();
            popupPostImage.setImageURI(pickedImageUri);
        }
    }
}