package com.example.nearme.models;

import android.util.Log;

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
    private HashMap<String, PostMarker> mPostIdToPostMrker;
    private ClusterManager<PostMarker> mClusterManager;

    public PostMarkerManager(ClusterManager<PostMarker> mClusterManager) {
        this.mClusterManager = mClusterManager;

        mPostIdToPostMrker = new HashMap<>();
    }

    public void updateMarkers(List<Post> posts) {

        deletePostMarkers();
        addNewPostMarkers(posts);

        Log.i(TAG, "NUMBER OF POSTS: " +
                mClusterManager.getAlgorithm().getItems().size());
    }

    /**
     * Adds new PostMarkers
     *
     * @param posts - list of posts to be added
     */
    private void addNewPostMarkers(List<Post> posts) {
        for (Post post : posts) {
            String postID = post.getObjectId();

            ParseGeoPoint geoPoint = post.getLocation();

            PostMarker postMarker = new PostMarker(geoPoint.getLatitude(), geoPoint.getLongitude(), post);
            mClusterManager.addItem(postMarker);

            //Create reference in HashMap
            mPostIdToPostMrker.put(postID, postMarker);

            Log.i(TAG, "Created new marker from post: " + post.getDescription());
        }
        Log.i(TAG, "New Markers Added");
        mClusterManager.cluster();
    }

    /**
     * deletes all old postmarkers from map and cluster
     */
    private void deletePostMarkers() {
        mClusterManager.clearItems();
        mPostIdToPostMrker.clear();
        Log.i(TAG, "Old Markers Deleted");
        mClusterManager.cluster();
    }
}
