package com.example.nearme.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Used to represent a marker for cluster management
 */
public class PostMarker implements ClusterItem {
    private final LatLng mPosition;
    private final Post mPost;

    public PostMarker(double lat, double lng, Post post) {
        this.mPost = post;
        this.mPosition = new LatLng(lat,lng);
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Nullable
    @Override
    public String getTitle() {
        return mPost.getUser().getUsername();
    }

    @Nullable
    @Override
    public String getSnippet() {
        return mPost.getDescription();
    }

    public Post getmPost() {
        return mPost;
    }
}
