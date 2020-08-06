package com.example.nearme.models;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.nearme.DisplayMultiplePosts;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Handles Logic of Finding Recommendations
 */
public class GrabRecommendations {
    public static final String TAG = "GrabRecommendations";

    private static double sDistanceWeight = 0.5;
    private static double sTimeWeight = 0.5;

    private static double sMaxDistanceInMeters = 15 * 1609.34;
    private static Long sMaxTimeAgoInMilliSeconds = Long.valueOf(30 * 24 * 60 * 60) * Long.valueOf(1000);

    private HashMap<String, Post> mIdToPost;
    private HashMap<String, Double> mPostIdToDistance;
    private HashMap<String, Double> mPostIdToTimeAgo;
    private ParseUser mParseUser;
    private LatLng mCenter;

    private ArrayList<Post> mPostsRec;
    private Context mContext;

    public GrabRecommendations(Context context) {
        this.mContext = context;
        this.mIdToPost = new HashMap<>();
        this.mPostIdToDistance = new HashMap<>();
        this.mPostIdToTimeAgo = new HashMap<>();
        this.mPostsRec = new ArrayList<>();
        this.mParseUser = ParseUser.getCurrentUser();

        ParseGeoPoint parseGeoPoint = mParseUser.getParseGeoPoint("location");
        this.mCenter = new LatLng(parseGeoPoint.getLatitude(), parseGeoPoint.getLongitude());
    }

    public void showRecommendations() {
        queryAllPosts();
    }

    /**
     * queries all posts
     */
    private void queryAllPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        //Posts not made by current user
        query.whereNotEqualTo(Post.KEY_USER, mParseUser);

        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if (e == null) {
                    //Filtering out posts
                    initializeValuesToRecommendWith(objects);
                    //Create a Ranking List, using a Heap
                    createRanking();
                    //Display the Posts
                    displayPosts();
                } else {
                    Log.e(TAG, "error while querying", e);
                }
            }
        });
    }

    /**
     * initializes all values for posts that satisfy filters
     *
     * @param objects - list of posts, representing posts
     */
    private void initializeValuesToRecommendWith(List<Post> objects) {
        int numOfPostsAdded = 0;
        //Needed to normalize later
        Double sumOfDistancesAdded = 0.0;
        Double sumOfTimesAdded = 0.0;

        for (Post post : objects) {
            boolean passesFilter = true;

            //Checking Distance
            passesFilter = passesFilter && withinDistance(post);

            //Checking Time
            passesFilter = passesFilter && withinTime(post);

            if (passesFilter) {
                numOfPostsAdded++;
                String postID = post.getObjectId();
                mIdToPost.put(postID, post);

                //Adding to Distance Map
                Double distanceBetween = calcDistance(post);
                mPostIdToDistance.put(postID, distanceBetween);
                sumOfDistancesAdded += distanceBetween;

                //Adding to Time Map
                Long timeAgo = calcTime(post);
                mPostIdToTimeAgo.put(postID, Double.valueOf(timeAgo));
                sumOfTimesAdded += timeAgo;

                Log.i(TAG, "Post Added: " + post.getDescription());
            }
        }
        Log.i(TAG, "TOTAL Number of Posts Added: " + numOfPostsAdded);

        //Normalize everything
        normalizeDistances(sumOfDistancesAdded);
        normalizeTimes(sumOfTimesAdded);
    }


    /**
     * checks if post is within distance filter
     *
     * @param post - Post, representing post
     * @return - boolean representing if post is within filter distance
     */
    private boolean withinDistance(Post post) {
        return calcDistance(post) < sMaxDistanceInMeters;
    }

    /**
     * calculates distance between center and post location
     *
     * @param post - Post, representing post
     * @return Double, representing distance in meters
     */
    private Double calcDistance(Post post) {
        ParseGeoPoint parseGeoPoint = post.getLocation();
        LatLng postLocation = new LatLng(parseGeoPoint.getLatitude(), parseGeoPoint.getLongitude());

        //Calculating distance
        Double distanceBetween = SphericalUtil.computeDistanceBetween(mCenter, postLocation);

        return distanceBetween;
    }

    /**
     * normalizes distance value of all posts, so sum of all is 1
     *
     * @param sum - Double, representing sum of all distance values
     */
    private void normalizeDistances(Double sum) {
        Double normalizer = 1D / sum;
        Log.i(TAG, "distance normalizer value: " + String.valueOf(normalizer));

        for (String postID : mPostIdToDistance.keySet()) {

            Double defaultValue = mPostIdToDistance.get(postID);
            Double normalizedValue = defaultValue * normalizer;

            //Replacing with normalized value
            mPostIdToDistance.put(postID, normalizedValue);
        }

        //Checking normalized value sum
        Double checkSum = 0.0;
        for (Double val : mPostIdToDistance.values()) {
            checkSum += val;
//            Log.i(TAG,"new dist value is: " + String.valueOf(val));
        }
        Log.i(TAG, "Normalized Sum of Distances is: " + checkSum);
    }

    /**
     * checks if post creation time is within filter
     *
     * @param post - Post, representing post
     * @return - boolean,indicating if post is within time fitler
     */
    private boolean withinTime(Post post) {
        return calcTime(post) < sMaxTimeAgoInMilliSeconds;
    }

    /**
     * calculates time between not and when post was created
     *
     * @param post - Post, representing post
     * @return Long, representing time in milliseconds between current time and when post was created
     */
    private Long calcTime(Post post) {
        Long dateNow = new Date().getTime();

        Long date = post.getCreatedAt().getTime();
        Long timeAgoInMS = dateNow - date;

        return timeAgoInMS;
    }

    /**
     * normalizes time value for all posts so sum is 1
     *
     * @param sum - Double, representing sum of all timePassed with posts
     */
    private void normalizeTimes(Double sum) {
        Double normalizer = 1D / sum;
        Log.i(TAG, "time normalizer: " + String.valueOf(normalizer));

        for (String postID : mPostIdToTimeAgo.keySet()) {

            Double defaultValue = mPostIdToTimeAgo.get(postID);
            Double newTime = normalizer * defaultValue;

            //Replacing with normalized value
            mPostIdToTimeAgo.put(postID, newTime);
        }

        //Check normalized value sum
        Double checkingSum = 0D;
        for (Double i : mPostIdToTimeAgo.values()) {
            checkingSum += i;
            Log.i(TAG, "new time val is: " + i);
        }
        //should be v close or equal to 1
        Log.i(TAG, "Normalized Sum of Times is: " + checkingSum);
    }

    /**
     * Creates rankings between posts and grabs up to 3 lowest rated
     */
    private void createRanking() {
        PriorityQueue<PostAndRanking> rankingsPQ = new PriorityQueue<>();

        //Initializing rankings
        for (String id : mIdToPost.keySet()) {
            Double postDistance = mPostIdToDistance.get(id);
            Double postTime = mPostIdToTimeAgo.get(id);
            Post post = mIdToPost.get(id);

            Double ranking = (postDistance * sDistanceWeight) + (postTime * sTimeWeight);

            PostAndRanking postAndRanking = new PostAndRanking(post, ranking);

            rankingsPQ.add(postAndRanking);
        }

        PostAndRanking lowestRanking = rankingsPQ.peek();

        //Grabbing up to 3
        int iterate = (rankingsPQ.size() < 3) ? rankingsPQ.size() : 3;
        for (int i = 0; i < iterate; i++) {
            PostAndRanking top = rankingsPQ.poll();
            Post postFromTop = top.getPost();

            mPostsRec.add(postFromTop);
        }
    }

    private void displayPosts() {
        //send intent and info to stories activity.
        for (Post post : mPostsRec) {
            Log.i(TAG, "recommendatons workks., DESC: " + post.getDescription());
        }

        Intent intent = new Intent(mContext, DisplayMultiplePosts.class);

        intent.putParcelableArrayListExtra("posts", mPostsRec);
//        intent.putExtra("posts",mPostsRec);

        mContext.startActivity(intent);
    }

    /**
     * Class to represent posts and their rankings for priority queue
     */
    private class PostAndRanking implements Comparable<PostAndRanking> {
        private Post post;
        private Double ranking;

        public PostAndRanking(Post post, Double ranking) {
            this.post = post;
            this.ranking = ranking;
        }

        public Post getPost() {
            return post;
        }

        public void setPost(Post post) {
            this.post = post;
        }

        public Double getRanking() {
            return ranking;
        }

        public void setRanking(Double ranking) {
            this.ranking = ranking;
        }

        @Override
        public int compareTo(PostAndRanking postAndRanking) {
            return this.ranking.compareTo(postAndRanking.getRanking());
        }
    }
}
