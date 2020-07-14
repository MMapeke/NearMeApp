package com.example.nearme.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Post")
public class Post extends ParseObject {

    public static final String KEY_LOCATION = "location";
    public static final String KEY_IMAGE = "imageMedia";
    public static final String KEY_DESCRIPTION = "desc";
    public static final String KEY_USER = "user";

    public Post(){
        super();
    }

   public ParseGeoPoint getLocation(){
        return getParseGeoPoint(KEY_LOCATION);
   }

   public ParseFile getImage(){
        return getParseFile(KEY_IMAGE);
   }

   public String getDescription(){
        return getString(KEY_DESCRIPTION);
   }

    public ParseUser getUser(){
        return getParseUser(KEY_USER);
   }

   public void setLocation(ParseGeoPoint inp){
        put(KEY_LOCATION,inp);
   }

   public void setImage(ParseFile inp){
        put(KEY_IMAGE,inp);
   }

   public void setDescription(String inp){
        put(KEY_DESCRIPTION,inp);
   }

   public void setUser(ParseUser inp){
        put(KEY_USER,inp);
   }
}
