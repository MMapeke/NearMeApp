package com.example.nearme;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

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

public class OtherProfile extends AppCompatActivity {

    public static final String TAG = "OtherProfile";
    ParseUser parseUser;

    ImageView pfpPic;
    TextView username;

    RecyclerView rvPosts;
    ProfileAdapter profileAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_profile);

        //Grab User from Intent
        this.parseUser = Parcels.unwrap(getIntent().getParcelableExtra("user"));

        pfpPic = findViewById(R.id.other_profile_pic);
        username = findViewById(R.id.other_profile_username);

        rvPosts = findViewById(R.id.other_profile_rvPosts);

        profileAdapter = new ProfileAdapter(this,new ArrayList<Post>(),false);
        rvPosts.setAdapter(profileAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvPosts.setLayoutManager(linearLayoutManager);

        //Setting Username
        username.setText(parseUser.getUsername());

        //Setting Profile Pic
        loadProfilePic();

        queryAllUserPosts();
    }

    private void loadProfilePic() {
        ParseFile pfp = parseUser.getParseFile("profilePic");

        if (pfp == null) {
            Glide.with(this)
                    .load(R.drawable.default_pic)
                    .circleCrop()
                    .into(pfpPic);
        } else {
            Glide.with(this)
                    .load(pfp.getUrl())
                    .circleCrop()
                    .into(pfpPic);
        }
    }

    private void queryAllUserPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        query.whereEqualTo(Post.KEY_USER, parseUser);

        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if (e == null) {
                    profileAdapter.addAll(objects);
//                    profileAdapter.notifyDataSetChanged();
                    Log.i(TAG, "query successful");
                } else {
                    Log.e(TAG, "error while querying", e);
                }
            }
        });
    }
}