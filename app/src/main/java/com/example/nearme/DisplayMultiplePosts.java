package com.example.nearme;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.nearme.models.DisplayMultipleAdapter;
import com.example.nearme.models.Post;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator;

/**
 * Class/Activity Handling Displaying a List of Posts
 */
public class DisplayMultiplePosts extends AppCompatActivity {

    public static final String TAG = "DisplayMultiplePosts";

    private List<Post> mPostsToShow;

    private CircleIndicator mIndicator;
    private ViewPager mViewPager;
    private DisplayMultipleAdapter mDisplayMultipleAdapter;
    private TextView mEmptyMsg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_multiple);

        mIndicator = findViewById(R.id.multiple_indicator);
        mEmptyMsg = findViewById(R.id.multiple_empty_msg);
        mViewPager = findViewById(R.id.viewPager);

        mPostsToShow = new ArrayList<>();
        Intent intent = getIntent();

        ArrayList<Post> parcelizedPosts = intent.getParcelableArrayListExtra("posts");
        if (parcelizedPosts == null) {
            Log.e(TAG, "MUST PASS posts or not getting found");
        } else {
            for (Post post : parcelizedPosts) {
                mPostsToShow.add(post);
            }
        }

        if (mPostsToShow.isEmpty()) {
            //No recs found/grabbed
            mEmptyMsg.setVisibility(View.VISIBLE);
            mIndicator.setVisibility(View.GONE);
        } else {
            displayPosts();
            mEmptyMsg.setVisibility(View.GONE);
            mIndicator.setVisibility(View.VISIBLE);
        }
    }

    /**
     * handles displaying up to 3 top posts to user
     */
    private void displayPosts() {
        mDisplayMultipleAdapter = new DisplayMultipleAdapter(mPostsToShow, this);

        mViewPager.setAdapter(mDisplayMultipleAdapter);
        mIndicator.setViewPager(mViewPager);
    }
}