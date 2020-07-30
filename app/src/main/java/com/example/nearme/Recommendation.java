package com.example.nearme;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.example.nearme.models.Post;
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

public class Recommendation extends AppCompatActivity {

    //TODO: May add options for differnt distance amounts

    public static final String TAG = "Recommendation";

    private static double sDistanceWeight = 0.5;
    private static double sTimeWeight = 0.5;

    private static double sMaxDistanceInMeters = 8 * 1609.34;
    private static Long sMaxTimeAgoInMilliSeconds = Long.valueOf(30 * 24 * 60 * 60) * Long.valueOf(1000);

    private HashMap<String,Post>  mIdToPost;
    private HashMap<String, Double> mPostIdToDistance;
    private HashMap<String, Double> mPostIdToTimeAgo;
    private ParseUser mParseUser;
    private LatLng mCenter;

    private List<Post> mPostsRec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendation);

        mIdToPost = new HashMap<>();
        mPostIdToDistance = new HashMap<>();
        mPostIdToTimeAgo = new HashMap<>();
        mPostsRec = new ArrayList<>();
        mParseUser = ParseUser.getCurrentUser();

        ParseGeoPoint parseGeoPoint = mParseUser.getParseGeoPoint("location");
        mCenter = new LatLng(parseGeoPoint.getLatitude(), parseGeoPoint.getLongitude());

        queryAllPosts();
    }


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
                } else {
                    Log.e(TAG, "error while querying", e);
                }
            }
        });
    }

    private void initializeValuesToRecommendWith(List<Post> objects) {
        int numOfPostsAdded = 0;
        //Needed to normalize later
        Double sumOfDistancesAdded = 0.0;
        Double sumOfTimesAdded = 0.0;

        for(Post post: objects){
            boolean passesFilter = true;

            //Checking Distance
            passesFilter = passesFilter && withinDistance(post);

            //Checking Time
            passesFilter =  passesFilter && withinTime(post);

            if(passesFilter){
                numOfPostsAdded++;
                String postID = post.getObjectId();
                mIdToPost.put(postID,post);

                //Adding to Distance Map
                Double distanceBetween = calcDistance(post);
                mPostIdToDistance.put(postID, distanceBetween);
                sumOfDistancesAdded += distanceBetween;

                //Adding to Time Map
                Long timeAgo = calcTime(post);
                mPostIdToTimeAgo.put(postID, Double.valueOf(timeAgo));
                sumOfTimesAdded += timeAgo;

                Log.i(TAG,"Post Added: " + post.getDescription());
            }
        }
        Log.i(TAG,"TOTAL Number of Posts Added: " + numOfPostsAdded);

        //Normalize everything
        normalizeDistances(sumOfDistancesAdded);
        normalizeTimes(sumOfTimesAdded);
    }

    private boolean withinDistance(Post post) {
        return calcDistance(post) < sMaxDistanceInMeters;
    }

    private Double calcDistance(Post post){
        ParseGeoPoint parseGeoPoint = post.getLocation();
        LatLng postLocation = new LatLng(parseGeoPoint.getLatitude(), parseGeoPoint.getLongitude());

        //Calculating distance
        Double distanceBetween = SphericalUtil.computeDistanceBetween(mCenter, postLocation);

        return  distanceBetween;
    }

    private void normalizeDistances(Double sum) {
        Double normalizer =  1D/sum;
        Log.i(TAG,"distance normalizer value: " + String.valueOf(normalizer));

        for(String postID: mPostIdToDistance.keySet()){

            Double defaultValue = mPostIdToDistance.get(postID);
            Double normalizedValue = defaultValue * normalizer;

            //Replacing with normalized value
            mPostIdToDistance.put(postID,normalizedValue);
        }

        //Checking normalized value sum
        Double checkSum = 0.0;
        for(Double val: mPostIdToDistance.values()){
            checkSum += val;
//            Log.i(TAG,"new dist value is: " + String.valueOf(val));
        }
        Log.i(TAG,"Normalized Sum of Distances is: " + checkSum);
    }

    private boolean withinTime(Post post){
        return calcTime(post) < sMaxTimeAgoInMilliSeconds;
    }

    private Long calcTime(Post post){
        Long dateNow = new Date().getTime();

        Long date = post.getCreatedAt().getTime();
        Long timeAgoInMS = dateNow - date;

        return timeAgoInMS;
    }

    private void normalizeTimes(Double sum) {
        Double normalizer = 1D/sum;
        Log.i(TAG,"time normalizer: " + String.valueOf(normalizer));

        for(String postID: mPostIdToTimeAgo.keySet()){

            Double defaultValue = mPostIdToTimeAgo.get(postID);
            Double newTime = normalizer * defaultValue;

            //Replacing with normalized value
            mPostIdToTimeAgo.put(postID,newTime);
        }

        //Check normalized value sum
        Double checkingSum = 0D;
        for(Double i : mPostIdToTimeAgo.values()){
            checkingSum += i;
            Log.i(TAG,"new time val is: " + i);
        }
        //should be v close or equal to 1
        Log.i(TAG,"Normalized Sum of Times is: " + checkingSum);
    }

    private void createRanking(){
        PriorityQueue<PostAndRanking> rankingsPQ = new PriorityQueue<>();

        //Initializing rankings
        for(String id: mIdToPost.keySet()){
            Double postDistance = mPostIdToDistance.get(id);
            Double postTime = mPostIdToTimeAgo.get(id);
            Post post = mIdToPost.get(id);

            Double ranking = (postDistance * sDistanceWeight) + (postTime * sTimeWeight);

            PostAndRanking postAndRanking = new PostAndRanking(post,ranking);

            rankingsPQ.add(postAndRanking);
        }

        PostAndRanking lowestRanking = rankingsPQ.peek();

        //Grabbing up to 3
        int iterate = (rankingsPQ.size() < 3) ? rankingsPQ.size() : 3;
        for(int i = 0; i < iterate; i++){
            PostAndRanking top = rankingsPQ.poll();
            Post postFromTop = top.getPost();

            mPostsRec.add(postFromTop);
        }
    }

    public class PostAndRanking implements Comparable<PostAndRanking>{
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