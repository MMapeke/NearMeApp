package com.example.nearme.models;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseGeoPoint;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static com.example.nearme.fragments.MapFragment.DEFAULT_MARKER;

/**
 * Class responsible for holding connections between Posts and Markers in MapFragment
 */
public class PostsAndMarkers {

    public static final String TAG = "PostsAndMarkers";

    //idToPost is needed to confirm reQueired post is the same or different
    private HashMap<String, Post> mIDToPost;
    private HashMap<Marker, String> mMarkerToPostID;
    private HashMap<String, Marker> mPostIDToMarker;
    private GoogleMap mMap;

    public PostsAndMarkers(GoogleMap map) {
        mMarkerToPostID = new HashMap<>();
        mPostIDToMarker = new HashMap<>();
        mIDToPost = new HashMap<>();
        this.mMap = map;
    }

    public Post getPost(Marker marker) {
        String postID = mMarkerToPostID.get(marker);
        return mIDToPost.get(postID);
    }

    public void addNewAndDeleteOldMarkers(List<Post> objects) {
        addNewMarkers(objects);
        deleteOldMarkers(objects);
        Log.i(TAG, "Done adding new and deleting old markers");
    }

    /**
     * Adds new markers to Google Map based on new posts
     *
     * @param objects - List of Posts, representing posts queried
     */
    private void addNewMarkers(List<Post> objects) {
        for (Post post : objects) {
            //if post does not have marker already
            if (!mPostIDToMarker.containsKey(post.getObjectId())) {
                String description = post.getDescription();
                String username = post.getUser().getUsername();
                ParseGeoPoint location = post.getLocation();
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .snippet(description)
                        .title("@" + username)
                        .icon(DEFAULT_MARKER));

                mIDToPost.put(post.getObjectId(), post);

                mPostIDToMarker.put(post.getObjectId(), marker);
                mMarkerToPostID.put(marker, post.getObjectId());

                Log.i(TAG, "Created new marker from post: " + post.getDescription());
            }
        }
        Log.i(TAG, "New Markers Added");
    }

    /**
     * Deletes Old Markers from Google Map
     *
     * @param inp - List of Posts, representing posts queried
     */
    private void deleteOldMarkers(List<Post> inp) {
        HashSet<String> newPostsID = new HashSet<>();
        for (Post post : inp) {
            newPostsID.add(post.getObjectId());
        }
        HashSet<Marker> oldMarkers = new HashSet<>(mMarkerToPostID.keySet());

        for (Marker marker : oldMarkers) {
            String postIDWithMarker = mMarkerToPostID.get(marker);
            Post postWithMarker = mIDToPost.get(postIDWithMarker);
            //Old Marker Not Associated w/ newly loaded post
            if (!newPostsID.contains(postIDWithMarker)) {
                mIDToPost.remove(postIDWithMarker);
                mPostIDToMarker.remove(postIDWithMarker);
                mMarkerToPostID.remove(marker);

                marker.remove();
                Log.i(TAG, "Deleted with marker from post: " + postWithMarker.getDescription());
            }
        }
        Log.i(TAG, "Old Markers Deleted");
    }
}
