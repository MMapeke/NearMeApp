package com.example.nearme.fragments;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.nearme.PostDetails;
import com.example.nearme.R;
import com.example.nearme.models.Post;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;


import org.parceler.Parcels;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MapFragment extends Fragment{

    public static final String TAG = "MapFragment";
    private QueryManager queryManager;
    private BitmapDescriptor defaultMarker;
    private GoogleMap mMap;
    private LatLng currLocation;

    private HashMap<String,Post> idToPost;

    private HashMap<Marker,String> markerToPost;
    private HashMap<String,Marker> postToMarker;

    private SupportMapFragment mapFragment;
    Marker lastOpenned = null;

    //Bounds of View
    LatLng swBound;
    LatLng neBound;

    public MapFragment() {
        // Required empty public constructor
    }

    // Creates a new  MapFragment given 2 LatLng Bounds (southwest,northeast)
    public static MapFragment newInstance(QueryManager qm) {
        MapFragment mFragment = new MapFragment();
        Bundle args = new Bundle();

        args.putParcelable("qm", Parcels.wrap(qm));

        mFragment.setArguments(args);
        return mFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Get Bound Argument If Exist
        Bundle bundle = getArguments();
        if(bundle != null){
            queryManager = Parcels.unwrap(bundle.getParcelable("qm"));

            ParseGeoPoint sw = queryManager.getSwBound();
            ParseGeoPoint ne = queryManager.getNeBound();

            swBound = new LatLng(sw.getLatitude(),sw.getLongitude());
            neBound = new LatLng(ne.getLatitude(),ne.getLongitude());
        }
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

        idToPost = new HashMap<>();
        markerToPost = new HashMap<>();
        postToMarker = new HashMap<>();

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

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(swBound);
        builder.include(neBound);

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;

        LatLngBounds newBounds = builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(newBounds,width,height,16));

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            public boolean onMarkerClick(Marker marker) {
                // Check if there is an open info window
                if (lastOpenned != null) {
                    // Close the info window
                    lastOpenned.hideInfoWindow();

                    // Is the marker the same marker that was already open
                    if (lastOpenned.equals(marker)) {
                        // Nullify the lastOpenned object
                        lastOpenned = null;
                        // Return so that the info window isn't openned again
                        return true;
                    }
                }

                // Open the info window for the marker
                marker.showInfoWindow();
                // Re-assign the last openned such that we can close it later
                lastOpenned = marker;

                // Event was handled by our code do not launch default behaviour.
                return true;
            }
        });


        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Log.i(TAG,"info window clicked");
                String postID = markerToPost.get(marker);
                Post post = idToPost.get(postID);

                Intent intent = new Intent(getActivity(), PostDetails.class);
                intent.putExtra("post", Parcels.wrap(post));

                startActivity(intent);
            }
        });

        map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                Log.i(TAG,"Camera Idle");

                QueryManager.Filter currState = queryManager.getCurrentState();

                if(currState == QueryManager.Filter.VIEWALL){
                    mMap.getUiSettings().setAllGesturesEnabled(false);
                } else if(currState == QueryManager.Filter.DEFAULT) {
                    mMap.getUiSettings().setAllGesturesEnabled(true);
                    LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                    LatLng ne = bounds.northeast;
                    LatLng sw = bounds.southwest;
                    ParseGeoPoint northeast = new ParseGeoPoint(ne.latitude, ne.longitude);
                    ParseGeoPoint southwest = new ParseGeoPoint(sw.latitude, sw.longitude);

                    queryManager.setSwBound(southwest);
                    queryManager.setNeBound(northeast);

                    queryPosts();
                }
            }
        });
    }

    public void queryPosts(){
        Log.i(TAG,"Querying Posts");
        queryManager.getQuery(QueryManager.MAP_FRAGMENT_SETTINGS)
                .findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if(e == null){

                    if(queryManager.getCurrentState() == QueryManager.Filter.VIEWALL){
                        zoomMapOutForMarkers(objects);
                    }

                    addMarkers(objects);
                    deleteOldMarkers(objects);
                    Log.i(TAG,"Posts queried: " + objects.size());
                }else{
                    Log.e(TAG,"error while querying",e);
                }
            }
        });
    }

    public void reCenter(){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        ParseGeoPoint prevNE = queryManager.getNeBound();
        ParseGeoPoint prevSW = queryManager.getSwBound();
        LatLng ne = new LatLng(prevNE.getLatitude(),prevNE.getLongitude());
        LatLng sw = new LatLng(prevSW.getLatitude(),prevSW.getLongitude());

        builder.include(ne);
        builder.include(sw);

        LatLngBounds bounds = builder.build();

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,16));
    }

    //zoom map out to fit all markers in view
    private void zoomMapOutForMarkers(List<Post> objects) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for(Post post: objects) {
            ParseGeoPoint parseGeoPoint = post.getLocation();
            LatLng latLng = new LatLng(parseGeoPoint.getLatitude(),parseGeoPoint.getLongitude());
            builder.include(latLng);
        }

        LatLngBounds allBounds = builder.build();

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(allBounds,64));
        Log.i(TAG,"Zoom out to view all markers");

        ParseGeoPoint sw = new ParseGeoPoint(allBounds.southwest.latitude
                                ,allBounds.southwest.longitude);
        ParseGeoPoint ne = new ParseGeoPoint(allBounds.northeast.latitude,
                                allBounds.northeast.longitude);
    }

    private void deleteOldMarkers(List<Post> inp) {
        HashSet<String> newPostsID = new HashSet<>();
        for(Post post: inp){
            newPostsID.add(post.getObjectId());
        }
        HashSet<Marker> oldMarkers = new HashSet<>(markerToPost.keySet());

        for(Marker marker: oldMarkers){
            String postIDWithMarker = markerToPost.get(marker);
            Post postWithMarker = idToPost.get(postIDWithMarker);
            //Old Marker Not Associated w/ newly loaded post
            if(!newPostsID.contains(postIDWithMarker)){
                idToPost.remove(postIDWithMarker);
                postToMarker.remove(postIDWithMarker);
                markerToPost.remove(marker);

                marker.remove();
                Log.i(TAG,"Deleted with marker from post: " + postWithMarker.getDescription());
            }
        }
        Log.i(TAG,"Old Markers Deleted");
    }

    private void addMarkers(List<Post> inp) {
        for(Post post: inp){
            //if post does not have marker already
            if(!postToMarker.containsKey(post.getObjectId())) {
                String description = post.getDescription();
                String username = post.getUser().getUsername();
                ParseGeoPoint location = post.getLocation();
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .snippet(description)
                        .title("@" + username)
                        .icon(defaultMarker));

                idToPost.put(post.getObjectId(),post);

                postToMarker.put(post.getObjectId(), marker);
                markerToPost.put(marker, post.getObjectId());

                Log.i(TAG,"Created new marker from post: " + post.getDescription());
            }
        }
        Log.i(TAG,"New Markers Added");
    }
}