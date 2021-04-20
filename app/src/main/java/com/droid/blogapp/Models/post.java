package com.droid.blogapp.Models;

import com.google.firebase.database.ServerValue;

import java.util.Objects;

public class post {

    private String title;
    private String postKey;
    private String description;
    private String pictures;
    private String userId;
    private String userName;
    private String userPhotos;
    private Object timeStamp;

    public post(String title, String description, String pictures, String userId, String userPhotos, String userName){
        this.title = title;
        this.description = description;
        this.pictures = pictures;
        this.userId = userId;
        this.userPhotos = userPhotos;
        this.userName = userName;
        this.timeStamp = ServerValue.TIMESTAMP;

    }
//constructor
    public post() {
    }
// getter

    public String getPostKey() {
        return postKey;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPictures() {
        return pictures;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserPhotos() {
        return userPhotos;
    }

    public Object getTimeStamp() {
        return timeStamp;
    }

//setter
    public void setUserName(String userName) {
        this.userName = userName;
    }
public void setPostKey(String postKey) {
    this.postKey = postKey;
}

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPictures(String pictures) {
        this.pictures = pictures;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserPhotos(String userPhotos) {
        this.userPhotos = userPhotos;
    }

    public void setTimeStamp(Object timeStamp) {
        this.timeStamp = timeStamp;
    }
}
