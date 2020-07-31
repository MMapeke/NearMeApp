package com.example.nearme;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.nearme.models.Post;
import com.example.nearme.models.DisplayMultipleAdapter;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator;

//TODO: Pass Cluster Info into here throughlist
//TODO: May add options for differnt distance amounts in recommendation

/**
 * Class/Activity Handling Displaying a List of Posts
 */
public class DisplayMultiplePosts extends AppCompatActivity {

    public static final String TAG = "DisplayMultiplePosts";

    private List<Post> mPostsToShow;

    ViewPager mViewPager;
    DisplayMultipleAdapter mDisplayMultipleAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_multiple);

        mPostsToShow = new ArrayList<>();
        Intent intent = getIntent();

        ArrayList<Post> parcelizedPosts = intent.getParcelableArrayListExtra("posts");
        if(parcelizedPosts == null){
            Log.e(TAG,"MUST PASS posts or not getting found");
        }else{
            for(Post post: parcelizedPosts){
                mPostsToShow.add(post);
            }
        }


        displayPosts();
    }

    /**
     * handles displaying up to 3 top posts to user
     */
    private void displayPosts() {

        if (mPostsToShow.isEmpty()) {
            Toast.makeText(this, "no posts, make one?", Toast.LENGTH_SHORT);
        } else {
            mDisplayMultipleAdapter = new DisplayMultipleAdapter(mPostsToShow, this);
            mViewPager = findViewById(R.id.viewPager);
            mViewPager.setAdapter(mDisplayMultipleAdapter);

            CircleIndicator indicator = findViewById(R.id.multiple_indicator);
            indicator.setViewPager(mViewPager);
        }
    }
}