package com.example.nearme.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MapFragment extends Fragment{

    public static final String TAG = "MapFragment";
    private BitmapDescriptor defaultMarker;
    private GoogleMap mMap;
    private LatLng currLocation;

    private HashMap<String,Post> idToPost;

    private HashMap<Marker,String> markerToPost;
    private HashMap<String,Marker> postToMarker;

    private SupportMapFragment mapFragment;

    //Bounds of View
    boolean havePrevBounds = false;
    LatLng swBound;
    LatLng neBound;

    //Listener from activity instance
    private MapFragmentListener listener;

    public interface MapFragmentListener{
        //Fired with bounds of view are changed
        public void viewBoundChanged(LatLng swBound, LatLng neBound);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MapFragmentListener) {
            listener = (MapFragmentListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement MapFragment.MapFragmentListener");
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

        //setting max and min zoom levels
        mMap.setMinZoomPreference(10f);
        mMap.setMaxZoomPreference(20f);

        if(havePrevBounds){
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
                LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                LatLng ne = bounds.northeast;
                LatLng sw = bounds.southwest;
                ParseGeoPoint northeast = new ParseGeoPoint(ne.latitude,ne.longitude);
                ParseGeoPoint southwest = new ParseGeoPoint(sw.latitude,sw.longitude);

                //Notifying Parent Activity bounds have changed\
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
                    for(Post i:objects){
                        Log.i(TAG,i.getDescription() + " by: " + i.getUser().getUsername());
                    }
                    Log.i(TAG,"Posts queried");
                    addMarkers(objects);
                    deleteOldMarkers(objects);
                    Log.i(TAG,"Markers adjusted");
                }else{
                    Log.e(TAG,"error while querying posts",e);
                }
            }
        });
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
    }


}