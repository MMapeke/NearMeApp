package com.example.nearme.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.nearme.PostDetails;
import com.example.nearme.R;
import com.example.nearme.models.Post;
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
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import permissions.dispatcher.RuntimePermissions;

public class MapFragment extends Fragment{

    public static final String TAG = "MapFragment";
    private BitmapDescriptor defaultMarker;
    private GoogleMap mMap;
    private LatLng currLocation;
    private List<Post> posts;
    private HashMap<String,Post> markers;
    private SupportMapFragment mapFragment;

    //Bounds of View
    boolean havePrevBounds = false;
    LatLng swBound;
    LatLng neBound;

//    double sw_lat;
//    double sw_lng;
//    double ne_lat;
//    double ne_lng;

    //Listener from activity instance
    private MapFragmentListener listener;

    public interface MapFragmentListener{
        //Fired with bounds of view are changed
//        public void viewBoundChanged(Double sw_lat, Double sw_lng, Double ne_lat, Double ne_lng);
        public void viewBoundChanged(LatLng swBound, LatLng neBound);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MapFragmentListener) {
            listener = (MapFragmentListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement MapFragment.OnViewChangedListener");
        }
    }

    public MapFragment() {
        // Required empty public constructor
    }

    // Creates a new  MapFragment given 2 LatLng Bounds (southwest,northeast)
    public static MapFragment newInstance(
//            Double sw_lat, Double sw_lng, Double ne_lat, Double ne_lng,
            LatLng sw, LatLng ne) {
        MapFragment mFragment = new MapFragment();
        Bundle args = new Bundle();
//        args.putDouble("sw_lat",sw_lat);
//        args.putDouble("sw_lng",sw_lng);
//        args.putDouble("ne_lat",ne_lat);
//        args.putDouble("ne_lng",ne_lng);

        args.putParcelable("sw", sw);
        args.putParcelable("ne",ne);

        mFragment.setArguments(args);
        return mFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Get Bound Argument If Exist
        Bundle bundle = getArguments();
        if(bundle != null){
//            sw_lat = bundle.getDouble("sw_lat");
//            sw_lng = bundle.getDouble("sw_lng");
//            ne_lat = bundle.getDouble("ne_lat");
//            ne_lng = bundle.getDouble("ne_lng");

            swBound = bundle.getParcelable("sw");
            neBound = bundle.getParcelable("ne");

            havePrevBounds = true;
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

        posts = new ArrayList<>();
        markers = new HashMap<>();

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

        if(havePrevBounds){
//            LatLng southwest = new LatLng(sw_lat,sw_lng);
//            LatLng northeast = new LatLng(ne_lat,ne_lng);

            //building bounds based off previous LatLng values
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(swBound);
            builder.include(neBound);

            LatLngBounds newBounds = builder.build();

            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(newBounds,0));
        }else {
            //center on curr location
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currLocation, 17f));
        }

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Log.i(TAG,"info window clicked");

//                if(!marker.getId().equals(locationMarker)) {
                    Intent intent = new Intent(getActivity(), PostDetails.class);
                    Post post = markers.get(marker.getId());
                    intent.putExtra("post", Parcels.wrap(post));

                    startActivity(intent);
//                }
            }
        });

        //TODO:
        //Add GeoQuerying within Box to TextFragment
        //Fix InfoWindow Click Bug (need to change how storing, and dont delete/recreate markers still there
        //?Default Behvaior b4 storing for Map and Text (when mainactivity created new), may move location to fragment, and add buttons to recenter

        map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                Log.i(TAG,"Camera Idle");
                LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                LatLng ne = bounds.northeast;
                LatLng sw = bounds.southwest;
                ParseGeoPoint northeast = new ParseGeoPoint(ne.latitude,ne.longitude);
                ParseGeoPoint southwest = new ParseGeoPoint(sw.latitude,sw.longitude);

                //Notifying Parent Activity bounds have changed
//                listener.viewBoundChanged(sw.latitude,sw.longitude,ne.latitude,ne.longitude);
                listener.viewBoundChanged(sw,ne);

                queryPostsInView(southwest,northeast);
            }
        });
    }

    private void queryPostsInView(ParseGeoPoint southwest, ParseGeoPoint northeast){
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        query.setLimit(10);

        query.whereWithinGeoBox(Post.KEY_LOCATION,southwest,northeast);

        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if(e == null){

                    //Removing Old Markers and References
                    mMap.clear();
                    posts.clear();
                    markers.clear();

                    posts.addAll(objects);
                    for(Post i:objects){
                        Log.i(TAG,i.getDescription() + " by: " + i.getUser().getUsername());
                    }
                    Log.i(TAG,"Posts queried");
                    addMarkers();
                }else{
                    Log.e(TAG,"error while querying posts",e);
                }
            }
        });
    }

    private void addMarkers() {
        for(Post post: posts){
            String description = post.getDescription();
            String username = post.getUser().getUsername();
            ParseGeoPoint location = post.getLocation();
            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

            Marker marker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .snippet(description)
                .title("@" + username)
                .icon(defaultMarker));

            //associating marker id with post
            markers.put(marker.getId(),post);
        }
        System.out.println("placeholder");
    }


}