package com.example.nearme.fragments;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.nearme.FilterChanged;
import com.example.nearme.PostDetails;
import com.example.nearme.R;
import com.example.nearme.models.Post;
import com.example.nearme.models.PostsAndMarkers;
import com.example.nearme.models.QueryManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;

import org.parceler.Parcels;

import java.util.List;

/**
 * Fragment
 */
public class MapFragment extends Fragment implements FilterChanged {

    public static final String TAG = "MapFragment";
    public static BitmapDescriptor DEFAULT_MARKER;

    private QueryManager mQueryManager;
    private GoogleMap mMap;
    private PostsAndMarkers mPostsAndMarkers;
    private SupportMapFragment mMapFragment;
    private Marker mLastOpenedMarker = null;

    public MapFragment() {
        // Required empty public constructor
    }

    public void setQueryManager(QueryManager mQueryManager) {
        this.mQueryManager = mQueryManager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Do a null check to confirm that we have not already instantiated the map.
        if (mMapFragment == null) {
            mMapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map));
            // Check if we were successful in obtaining the map.
            if (mMapFragment != null) {
                mMapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap map) {
                        try {
                            // Customise the styling of the base map using a JSON object defined
                            // in a raw resource file.
                            map.setMapStyle(
                                    MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.style_json));
                        } catch (Resources.NotFoundException e) {
                            Log.e(TAG, "Can't find style. Error: ", e);
                        }
                        mPostsAndMarkers = new PostsAndMarkers(map);
                        loadMap(map);
                    }
                });
            } else {
                Log.e(TAG, "error mapfragment not found");
            }
        }
    }


    private void loadMap(GoogleMap map) {
        mMap = map;
        DEFAULT_MARKER = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);

        ParseGeoPoint sw = mQueryManager.getSwBound();
        ParseGeoPoint ne = mQueryManager.getNeBound();

        LatLng swBound = new LatLng(sw.getLatitude(), sw.getLongitude());
        LatLng neBound = new LatLng(ne.getLatitude(), ne.getLongitude());

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(swBound);
        builder.include(neBound);

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;

        //TODO: Technically may not want to use fullscreen, check docs
        LatLngBounds newBounds = builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(newBounds, width, height, 16));

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            public boolean onMarkerClick(Marker marker) {
                // Check if there is an open info window
                if (mLastOpenedMarker != null) {
                    // Close the info window
                    mLastOpenedMarker.hideInfoWindow();

                    // Is the marker the same marker that was already open
                    if (mLastOpenedMarker.equals(marker)) {
                        // Nullify the lastOpenned object
                        mLastOpenedMarker = null;
                        // Return so that the info window isn't openned again
                        return true;
                    }
                }
                // Open the info window for the marker
                marker.showInfoWindow();
                // Re-assign the last openned such that we can close it later
                mLastOpenedMarker = marker;

                // Event was handled by our code do not launch default behaviour.
                return true;
            }
        });


        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Log.i(TAG, "info window clicked");

                Post post = mPostsAndMarkers.getPost(marker);

                Intent intent = new Intent(getActivity(), PostDetails.class);
                intent.putExtra("post", Parcels.wrap(post));

                startActivity(intent);
            }
        });

        map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                Log.i(TAG, "Camera Idle");

                QueryManager.Filter currState = mQueryManager.getCurrentState();

                if (currState == QueryManager.Filter.VIEWALL) {
                    //TODO: Communicate in ViewAll Map Can't Be Moved
                    mMap.getUiSettings().setAllGesturesEnabled(false);
                } else if (currState == QueryManager.Filter.DEFAULT) {
                    mMap.getUiSettings().setAllGesturesEnabled(true);
                    LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                    LatLng ne = bounds.northeast;
                    LatLng sw = bounds.southwest;
                    ParseGeoPoint northeast = new ParseGeoPoint(ne.latitude, ne.longitude);
                    ParseGeoPoint southwest = new ParseGeoPoint(sw.latitude, sw.longitude);

                    mQueryManager.setSwBound(southwest);
                    mQueryManager.setNeBound(northeast);

                    queryPosts();
                }
            }
        });
    }

    /**
     * Querys posts to display in mapFragment
     */
    private void queryPosts() {
        Log.i(TAG, "Querying Posts");
        mQueryManager.getQuery(100)
                .findInBackground(new FindCallback<Post>() {
                    @Override
                    public void done(List<Post> objects, ParseException e) {
                        if (e == null) {

                            if (mQueryManager.getCurrentState() == QueryManager.Filter.VIEWALL) {
                                zoomMapOutForMarkers(objects);
                            }

                            mPostsAndMarkers.addNewAndDeleteOldMarkers(objects);

                            Log.i(TAG, "Posts queried: " + objects.size());
                        } else {
                            Log.e(TAG, "error while querying", e);
                        }
                    }
                });
    }

    /**
     * REcenters Map View on Last View stored in queryManager
     */
    private void reCenter() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        ParseGeoPoint prevNE = mQueryManager.getNeBound();
        ParseGeoPoint prevSW = mQueryManager.getSwBound();
        LatLng ne = new LatLng(prevNE.getLatitude(), prevNE.getLongitude());
        LatLng sw = new LatLng(prevSW.getLatitude(), prevSW.getLongitude());

        builder.include(ne);
        builder.include(sw);

        LatLngBounds bounds = builder.build();

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 16));
    }

    /**
     * Zooms out the map view to fit all points on map
     *
     * @param objects - List of Posts, representing posts queried and to be shown
     */
    private void zoomMapOutForMarkers(List<Post> objects) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (Post post : objects) {
            ParseGeoPoint parseGeoPoint = post.getLocation();
            LatLng latLng = new LatLng(parseGeoPoint.getLatitude(), parseGeoPoint.getLongitude());
            builder.include(latLng);
        }

        LatLngBounds allBounds = builder.build();

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(allBounds, 64));
        Log.i(TAG, "Zoom out to view all markers");
    }

    @Override
    public void filterChanged() {
        QueryManager.Filter currentFilter = mQueryManager.getCurrentState();

        if (currentFilter == QueryManager.Filter.VIEWALL) {
            queryPosts();
        }

        if (currentFilter == QueryManager.Filter.DEFAULT) {
            reCenter();
            queryPosts();
        }
    }
}