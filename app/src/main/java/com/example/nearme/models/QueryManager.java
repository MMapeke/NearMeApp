package com.example.nearme.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import org.parceler.Parcel;

import java.util.Date;

@Parcel
public class QueryManager {

    public enum Filter {
        VIEWALL,
        DEFAULT
    }

    private static final String TAG = "QueryManager";
    public static final String TEXT_FRAGMENT_SETTINGS = "text";
    public static final String MAP_FRAGMENT_SETTINGS = "map";


    Filter currentState;
    ParseGeoPoint userLocation;

    ParseGeoPoint swBound;
    ParseGeoPoint neBound;
    Integer hoursWithn;

    public final float defaultBoundsRadiusInMeters = 175.0f;

    public QueryManager(){
        //empty constructor for Parceler library
    }

    public QueryManager(ParseGeoPoint userLocation){
        this.userLocation = userLocation;
        this.currentState = Filter.DEFAULT;
        initDefaultBounds();
    }

    private void initDefaultBounds() {
        LatLng latLng = new LatLng(userLocation.getLatitude(),userLocation.getLongitude());

        LatLngBounds latLngBounds = calculateBounds(latLng,defaultBoundsRadiusInMeters);
        LatLng southwest = latLngBounds.southwest;
        LatLng northeast = latLngBounds.northeast;

        this.swBound = new ParseGeoPoint(southwest.latitude,southwest.longitude);
        this.neBound = new ParseGeoPoint(northeast.latitude,northeast.longitude);
    }

    public LatLngBounds calculateBounds(LatLng center, double radiusInMeters) {
        double distanceFromCenterToCorner = radiusInMeters * Math.sqrt(2.0);
        LatLng southwestCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0);
        LatLng northeastCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0);
        return new LatLngBounds(southwestCorner, northeastCorner);
    }

    public ParseQuery<Post> getQuery(String settings){
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);

        if(currentState != Filter.VIEWALL) {
            query.whereWithinGeoBox(Post.KEY_LOCATION, swBound, neBound);
        }

        if(settings.equals(TEXT_FRAGMENT_SETTINGS)){
            query.addDescendingOrder(Post.KEY_CREATED_AT);
            query.setLimit(10);
        }
        return query;
    }

    private Date getEarliestDate(){
        Date now = new Date();
        long nowMillis = now.getTime();
        long timeWithinInMills = hoursWithn * 3600000;
        Date withinTimeFrame = new Date(nowMillis - timeWithinInMills);

        return withinTimeFrame;
    }


    public ParseGeoPoint getSwBound() {
        return swBound;
    }

    public void setSwBound(ParseGeoPoint swBound) {
        this.swBound = swBound;
    }

    public ParseGeoPoint getNeBound() {
        return neBound;
    }

    public void setNeBound(ParseGeoPoint neBound) {
        this.neBound = neBound;
    }

    public int getHoursWithn() {
        return hoursWithn;
    }

    public void setHoursWithn(int hoursWithn) {
        this.hoursWithn = hoursWithn;
    }

    public Filter getCurrentState() {
        return currentState;
    }

    public void setCurrentState(Filter currentState) {
        this.currentState = currentState;
    }
}
