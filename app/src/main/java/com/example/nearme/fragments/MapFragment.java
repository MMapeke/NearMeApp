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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import permissions.dispatcher.RuntimePermissions;

public class MapFragment extends Fragment{

    public static final String TAG = "MapFragment";
    private GoogleMap mMap;
    private LatLng currLocation;
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
        //setting max and min zoom levels
        mMap.setMinZoomPreference(10f);
        mMap.setMaxZoomPreference(20f);

        // Add a marker in Sydney and move the camera
        mMap.addMarker(new MarkerOptions().position(currLocation).title("Current Location"));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currLocation,17f));
    }
}