package com.example.nearme.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.nearme.R;
import com.example.nearme.models.Post;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import permissions.dispatcher.RuntimePermissions;

public class MapFragment extends Fragment{

    public static final String TAG = "MapFragment";
    private BitmapDescriptor defaultMarker;
    private GoogleMap mMap;
    private LatLng currLocation;
    private List<Post> posts;
    SupportMapFragment mapFragment;


    public MapFragment() {
        // Required empty public constructor
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

        posts = new ArrayList<>();

        //Grab Current User
        ParseUser parseUser = ParseUser.getCurrentUser();
        ParseGeoPoint parseGeoPoint = parseUser.getParseGeoPoint("location");

        //Store Last Location
        currLocation = new LatLng(parseGeoPoint.getLatitude(),parseGeoPoint.getLongitude());

        // Do a null check to confirm that we have not already instantiated the map.
        if (mapFragment == null) {
            mapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map));
            // Check if we were successful in obtaining the map.
            if (mapFragment != null) {
                mapFragment.getMapAsync(new OnMapReadyCallback() {
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
                        loadMap(map);
                    }
                });
            }
        }
    }

    private void loadMap(GoogleMap map) {
        mMap = map;
        defaultMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);

        //setting max and min zoom levels
        mMap.setMinZoomPreference(10f);
        mMap.setMaxZoomPreference(20f);

        //center on curr location
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currLocation,17f));

        mMap.addMarker(new MarkerOptions()
                .position(currLocation)
                .title("Current Location")
                .icon(defaultMarker));

        queryPosts();
    }

    private void addMarkers() {
        for(Post post: posts){
            String description = post.getDescription();
            String username = post.getUser().getUsername();
            ParseGeoPoint location = post.getLocation();
            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

            mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .snippet(description)
                .title("@" + username)
                .icon(defaultMarker));
        }
    }

    private void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        query.setLimit(10);

        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if(e == null){
                    posts.addAll(objects);
                    for(Post i:objects){
                        Log.i(TAG,i.getDescription() + " by: " + i.getUser().getUsername());
                    }
                    Log.i(TAG,"Posts queried");
                    addMarkers();
                }else{
                    Log.e(TAG,"error while quering posts",e);
                }
            }
        });
    }
}