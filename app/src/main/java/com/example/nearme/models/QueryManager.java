package com.example.nearme.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;
import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import org.parceler.Parcel;

/**
 * Class responsible for controlling and keeping track of query settings
 */
@Parcel
public class QueryManager {

    public enum Filter {
        VIEWALL,
        DEFAULT
    }

    public static final String TAG = "QueryManager";
    private final float sDefaultBoundsRadiusInMeters = 175.0f;

    private Filter mCurrentState;
    private ParseGeoPoint mUserLocation;

    private ParseGeoPoint mSwBound;
    private ParseGeoPoint mNeBound;


    public QueryManager() {
        //empty constructor for Parceler library
    }

    public QueryManager(ParseGeoPoint userLocation) {
        this.mUserLocation = userLocation;
        this.mCurrentState = Filter.DEFAULT;
        initDefaultBounds();
    }

    /**
     * Sets bounds for the query manager with location as center
     */
    private void initDefaultBounds() {
        LatLng latLng = new LatLng(mUserLocation.getLatitude(), mUserLocation.getLongitude());

        LatLngBounds latLngBounds = calculateBounds(latLng, sDefaultBoundsRadiusInMeters);
        LatLng southwest = latLngBounds.southwest;
        LatLng northeast = latLngBounds.northeast;

        this.mSwBound = new ParseGeoPoint(southwest.latitude, southwest.longitude);
        this.mNeBound = new ParseGeoPoint(northeast.latitude, northeast.longitude);
    }

    /**
     * Calculates rectangular bounds based off radius and center point
     *
     * @param center         - LatLng, representing center of bounds
     * @param radiusInMeters - double, representing distance around center to be shown, in meters
     * @return LatLngBounds, representing rectangular bounds that hold all points
     */
    private LatLngBounds calculateBounds(LatLng center, double radiusInMeters) {
        double distanceFromCenterToCorner = radiusInMeters * Math.sqrt(2.0);
        LatLng southwestCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0);
        LatLng northeastCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0);
        return new LatLngBounds(southwestCorner, northeastCorner);
    }

    public ParseQuery<Post> getQuery(int limit) {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.include(Post.KEY_LIKED);
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        query.setLimit(limit);

        if (mCurrentState != Filter.VIEWALL) {
            query.whereWithinGeoBox(Post.KEY_LOCATION, mSwBound, mNeBound);
        }

        return query;
    }

    public ParseGeoPoint getSwBound() {
        return mSwBound;
    }

    public void setSwBound(ParseGeoPoint swBound) {
        this.mSwBound = swBound;
    }

    public ParseGeoPoint getNeBound() {
        return mNeBound;
    }

    public void setNeBound(ParseGeoPoint neBound) {
        this.mNeBound = neBound;
    }

    public Filter getCurrentState() {
        return mCurrentState;
    }

    public void setCurrentState(Filter currentState) {
        this.mCurrentState = currentState;
    }
}
