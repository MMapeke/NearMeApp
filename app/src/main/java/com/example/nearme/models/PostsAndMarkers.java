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

public class PostsAndMarkers {

    public static final String TAG = "PostsAndMarkers";
    //Need idToPost for easy map keys
    private HashMap<String, Post> idToPost;
    private HashMap<Marker, String> markerToPostID;
    private HashMap<String, Marker> postIDToMarker;
    private GoogleMap mMap;

    public PostsAndMarkers(GoogleMap map) {
        markerToPostID = new HashMap<>();
        postIDToMarker = new HashMap<>();
        idToPost = new HashMap<>();
        this.mMap = map;
    }

    public Post getPost(Marker marker) {
        String postID = markerToPostID.get(marker);
        return idToPost.get(postID);
    }

    public void addNewAndDeleteOldMarkers(List<Post> objects) {
        addNewMarkers(objects);
        deleteOldMarkers(objects);
        Log.i(TAG, "Done adding new and deleting old markers");
    }

    private void addNewMarkers(List<Post> objects) {
        for (Post post : objects) {
            //if post does not have marker already
            if (!postIDToMarker.containsKey(post.getObjectId())) {
                String description = post.getDescription();
                String username = post.getUser().getUsername();
                ParseGeoPoint location = post.getLocation();
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .snippet(description)
                        .title("@" + username)
                        .icon(DEFAULT_MARKER));

                idToPost.put(post.getObjectId(), post);

                postIDToMarker.put(post.getObjectId(), marker);
                markerToPostID.put(marker, post.getObjectId());

                Log.i(TAG, "Created new marker from post: " + post.getDescription());
            }
        }
        Log.i(TAG, "New Markers Added");
    }

    private void deleteOldMarkers(List<Post> inp) {
        HashSet<String> newPostsID = new HashSet<>();
        for (Post post : inp) {
            newPostsID.add(post.getObjectId());
        }
        HashSet<Marker> oldMarkers = new HashSet<>(markerToPostID.keySet());

        for (Marker marker : oldMarkers) {
            String postIDWithMarker = markerToPostID.get(marker);
            Post postWithMarker = idToPost.get(postIDWithMarker);
            //Old Marker Not Associated w/ newly loaded post
            if (!newPostsID.contains(postIDWithMarker)) {
                idToPost.remove(postIDWithMarker);
                postIDToMarker.remove(postIDWithMarker);
                markerToPostID.remove(marker);

                marker.remove();
                Log.i(TAG, "Deleted with marker from post: " + postWithMarker.getDescription());
            }
        }
        Log.i(TAG, "Old Markers Deleted");
    }


}
