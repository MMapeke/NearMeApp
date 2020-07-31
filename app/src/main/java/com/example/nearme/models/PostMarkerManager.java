package com.example.nearme.models;

import android.util.Log;

import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterManager;
import com.parse.ParseGeoPoint;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Class responsible for managing connections between posts and markers in mapfragmnt
 */
public class PostMarkerManager {

    public static final String TAG = "PostMarkerManager";
    private HashMap<String,PostMarker> mPostIdToPostMrker;
    private ClusterManager<PostMarker> mClusterManager;

    public PostMarkerManager(ClusterManager<PostMarker> mClusterManager){
        this.mClusterManager = mClusterManager;

        mPostIdToPostMrker = new HashMap<>();
    }

    public void updateMarkers(List<Post> posts){
        addNewPostMarkers(posts);
        deletePostMarkersNotOnScreen(posts);
    }

    /**
     * Adds new PostMarkers
     * @param posts - list of posts to be added
     */
    private void addNewPostMarkers(List<Post> posts){
        for(Post post: posts){
            String postID = post.getObjectId();

            //If Marker Does Not Exist for Post
            if(!mPostIdToPostMrker.containsKey(postID)){
                ParseGeoPoint geoPoint = post.getLocation();

                PostMarker postMarker = new PostMarker(geoPoint.getLatitude(),geoPoint.getLongitude(),post);
                mClusterManager.addItem(postMarker);

                //Create reference in HashMap
                mPostIdToPostMrker.put(postID,postMarker);

                Log.i(TAG,"Created new marker from post: " + post.getDescription());
            }
        }
        Log.i(TAG,"New Markers Added");
        mClusterManager.cluster();
    }

    /**
     * deletes old postmarkers from map and cluster
     * @param posts - list of posts to be kept on map
     */
    private void deletePostMarkersNotOnScreen(List<Post> posts){
        //Grabbing ID of all posts on screen
        HashSet<String> newPostsID = new HashSet<>();
        for (Post post : posts) {
            newPostsID.add(post.getObjectId());
        }

        HashMap<String,PostMarker> oldMarkers = (HashMap<String, PostMarker>) mPostIdToPostMrker.clone();

        for(String postMarkerID: oldMarkers.keySet()){
            if(!newPostsID.contains(postMarkerID)){
                //Old PostMarker not visible now
                PostMarker postMarker = mPostIdToPostMrker.get(postMarkerID);
                mClusterManager.removeItem(postMarker);
                mPostIdToPostMrker.remove(postMarkerID);

                Log.i(TAG,"Deleted Marker from post: " + postMarker.getmPost().getDescription());
            }
        }
        Log.i(TAG,"Old Markers Deleted");
        mClusterManager.cluster();
    }
}
