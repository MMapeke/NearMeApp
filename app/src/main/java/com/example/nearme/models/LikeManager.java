package com.example.nearme.models;

import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.nearme.R;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;

public class LikeManager {

    public static final String TAG = "LikeManager";
    private ParseUser mCurrUser;
    private Post mPost;
    private ArrayList<String> mLikedBy;
    private ImageView mLikeBtn;
    private TextView mLikeCount;

    public LikeManager(ParseUser mCurrUser, Post mPost, ImageView mLikedBtn, TextView mLikeCount) {
        this.mCurrUser = mCurrUser;
        this.mPost = mPost;
        this.mLikeBtn = mLikedBtn;
        this.mLikeCount = mLikeCount;

        mLikedBy = new ArrayList<>();
        ArrayList<String> grabPostLikes = mPost.getLikes();

        if (grabPostLikes != null) {
            mLikedBy = grabPostLikes;
        }

        //Initial setup
        updateNumLikes();
        checkIfPostLiked();
    }

    public LikeManager(ParseUser mCurrUser, Post mPost, ImageView mLikedBtn) {
        this.mCurrUser = mCurrUser;
        this.mPost = mPost;
        this.mLikeBtn = mLikedBtn;

        mLikedBy = new ArrayList<>();
        ArrayList<String> grabPostLikes = mPost.getLikes();

        if (grabPostLikes != null) {
            mLikedBy = grabPostLikes;
        }

        //Initial Setup
        checkIfPostLiked();
    }

    /**
     * Likes/Unlikes post user clicked on
     */
    public void likePost() {
        ParseUser currUser = ParseUser.getCurrentUser();
        final String currUserID = currUser.getObjectId();

        if (!hasUserLikedPost()) {
            this.mLikedBy.add(currUserID);
            mPost.put(Post.KEY_LIKED, mLikedBy);
            mPost.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        checkIfPostLiked();
                        updateNumLikes();
                        Log.i(TAG, "User liked post");
                    } else {
                        mLikedBy.remove(currUserID);
                        Log.e(TAG, "was not able to like post", e);
                    }
                }
            });
        } else {
            removeCurrUserFromLikes();
            mPost.put(Post.KEY_LIKED, mLikedBy);
            mPost.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        checkIfPostLiked();
                        updateNumLikes();
                        Log.i(TAG, "User unliked post");
                    } else {
                        Log.e(TAG, "was not able to unlike post", e);
                    }
                }
            });
        }
    }

    /**
     * removes curr user from likes
     */
    private void removeCurrUserFromLikes() {
        String currUserID = mCurrUser.getObjectId();

        String toRemove = null;
        for (String i : mLikedBy) {
            if (currUserID.equals(i)) {
                toRemove = i;
            }
        }

        if (mLikedBy == null) {
            Log.e(TAG, "shoudlnt be null if removing like");
        } else {
            mLikedBy.remove(toRemove);
            Log.i(TAG, "removed user from likes");
        }
    }

    /**
     * checks if user has liked post
     *
     * @return - boolean, representing if user has liked post
     */
    private Boolean hasUserLikedPost() {
        String currUserID = mCurrUser.getObjectId();
        for (String i : mLikedBy) {
            if (currUserID.equals(i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Controls thumbs up image based on if liked or not
     */
    public void checkIfPostLiked() {
        if (hasUserLikedPost()) {
            mLikeBtn.setImageResource(R.drawable.ic_baseline_thumb_up_filled_24);
        } else {
            mLikeBtn.setImageResource(R.drawable.ic_outline_thumb_up_24);
        }
    }

    public void updateNumLikes() {
        if (mLikeCount != null) {
            mLikeCount.setText(String.valueOf(mLikedBy.size()));
        }
    }
}
