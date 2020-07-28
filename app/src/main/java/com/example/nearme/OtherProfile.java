package com.example.nearme;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nearme.models.Post;
import com.example.nearme.models.ProfileAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

/**
 * Class/Activity responsible for controlling/displaying other User profiles
 */
public class OtherProfile extends AppCompatActivity {

    public static final String TAG = "OtherProfile";

    private ParseUser mParseUser;

    private ImageView mProfilePic;
    private TextView mUsername;

    private RecyclerView mRvPosts;
    private ProfileAdapter mProfileAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_profile);

        //Grab User from Intent
        this.mParseUser = Parcels.unwrap(getIntent().getParcelableExtra("user"));

        mProfilePic = findViewById(R.id.other_profile_pic);
        mUsername = findViewById(R.id.other_profile_username);

        mRvPosts = findViewById(R.id.other_profile_rvPosts);

        mProfileAdapter = new ProfileAdapter(this, new ArrayList<Post>(), false);
        mRvPosts.setAdapter(mProfileAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRvPosts.setLayoutManager(linearLayoutManager);

        //Setting Username
        mUsername.setText(mParseUser.getUsername());

        //Setting Profile Pic
        loadProfilePic();

        queryAllUserPosts();
    }

    /**
     * Loads profile picture into image view
     */
    private void loadProfilePic() {
        ParseFile pfp = mParseUser.getParseFile("profilePic");

        if (pfp == null) {
            Glide.with(this)
                    .load(R.drawable.default_pic)
                    .circleCrop()
                    .into(mProfilePic);
        } else {
            Glide.with(this)
                    .load(pfp.getUrl())
                    .circleCrop()
                    .into(mProfilePic);
        }
    }

    /**
     * queries all posts for user selected
     */
    private void queryAllUserPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        query.whereEqualTo(Post.KEY_USER, mParseUser);

        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if (e == null) {
                    mProfileAdapter.addAll(objects);
                    Log.i(TAG, "query successful");
                } else {
                    Log.e(TAG, "error while querying", e);
                }
            }
        });
    }
}