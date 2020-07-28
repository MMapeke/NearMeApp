package com.example.nearme;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.nearme.models.Post;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.Date;

/**
 * Class/Activity responsible for showing more details about posts
 */
public class PostDetails extends AppCompatActivity {

    private Post mPost;
    private TextView mUsername;
    private TextView mRelativeTime;
    private ImageView mPicture;
    private TextView mDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        mUsername = findViewById(R.id.details_username);
        mRelativeTime = findViewById(R.id.details_time);
        mPicture = findViewById(R.id.details_pic);
        mDescription = findViewById(R.id.details_desc);
        mPost = Parcels.unwrap(getIntent().getParcelableExtra("post"));

        if (mPost != null) {
            mUsername.setText("@" + mPost.getUser().getUsername());
            Date date = mPost.getCreatedAt();
            mRelativeTime.setText((String) DateUtils.getRelativeTimeSpanString(date.getTime()));
            Glide.with(this)
                    .load(mPost.getImage().getUrl())
                    .into(mPicture);
            mDescription.setText(mPost.getDescription());
        }

        mUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToUserProfile();
            }
        });
    }

    /**
     * Navigates to User Profile, if it is a different user than current
     */
    private void goToUserProfile() {
        ParseUser parseUser = mPost.getUser();

        if (!parseUser.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
            //If Profile Clicked on Is Not Own
            Intent intent = new Intent(this, OtherProfile.class);
            intent.putExtra("user", Parcels.wrap(parseUser));
            startActivity(intent);
        }

    }
}