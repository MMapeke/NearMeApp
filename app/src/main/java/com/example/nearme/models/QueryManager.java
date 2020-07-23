package com.example.nearme.models;

import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import org.parceler.Parcel;


@Parcel
public class QueryManager {

    public static final String TAG = "QueryManager";
    public static final String TEXT_FRAGMENT_SETTINGS = "text";
    public static final String MAP_FRAGMENT_SETTINGS = "map";

    ParseGeoPoint swBound;
    ParseGeoPoint neBound;
    int hoursWithn;

    public QueryManager(){
        //empty constructor for Parceler library
    }

    public QueryManager(ParseGeoPoint swBound, ParseGeoPoint neBound){
        this.swBound = swBound;
        this.neBound = neBound;
    }

    public ParseQuery<Post> getQuery(String settings){
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);

        query.whereWithinGeoBox(Post.KEY_LOCATION,swBound,neBound);

        if(settings.equals(TEXT_FRAGMENT_SETTINGS)){
            query.addDescendingOrder(Post.KEY_CREATED_AT);
            query.setLimit(10);
        }

        return query;
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
}
