package com.example.nearme.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.example.nearme.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

/**
 * Responsible for how clusters rendered
 * @param <PostMarker>
 */
public class CustomRenderer<PostMarker extends ClusterItem> extends DefaultClusterRenderer<PostMarker> {

    private final Context mContext;
    private final IconGenerator mClusterIconGenerator;

    public CustomRenderer(Context context, GoogleMap map, ClusterManager<PostMarker> clusterManager) {
        super(context, map, clusterManager);
        this.mContext = context;

        mClusterIconGenerator = new IconGenerator(mContext.getApplicationContext());
    }

    @Override
    protected boolean shouldRenderAsCluster(@NonNull Cluster<PostMarker> cluster) {
        return cluster.getSize() > 1;
    }

    @Override
    protected void onBeforeClusterItemRendered(@NonNull PostMarker item, @NonNull MarkerOptions markerOptions) {
//        final BitmapDescriptor markerDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);

        final BitmapDescriptor markerDescriptor = BitmapDescriptorFactory.fromBitmap(
                getBitmapFromVectorDrawable(mContext,R.drawable.ic_round_priority_high_24)
        );

        markerOptions.icon(markerDescriptor).title(item.getTitle()).snippet(item.getSnippet());
    }

    @Override
    protected int getColor(int clusterSize) {
        return Color.parseColor("#3700B3");
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
