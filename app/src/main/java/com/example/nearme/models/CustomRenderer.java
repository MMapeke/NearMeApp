package com.example.nearme.models;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

/**
 * Responsible for how clusters rendered
 * @param <PostMarker>
 */
public class CustomRenderer<PostMarker extends ClusterItem> extends DefaultClusterRenderer<PostMarker> {
    public CustomRenderer(Context context, GoogleMap map, ClusterManager<PostMarker> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected boolean shouldRenderAsCluster(@NonNull Cluster<PostMarker> cluster) {
        return cluster.getSize() > 1;
    }
}
