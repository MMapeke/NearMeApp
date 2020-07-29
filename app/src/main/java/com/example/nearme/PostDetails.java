package com.example.nearme;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
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

    public static final String TAG = "PostDetails";

    private Post mPost;
    private TextView mUsername;
    private TextView mRelativeTime;
    private ImageView mPicture;
    private TextView mDescription;
    private String checkFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        mUsername = findViewById(R.id.details_username);
        mRelativeTime = findViewById(R.id.details_time);
        mPicture = findViewById(R.id.details_pic);
        mDescription = findViewById(R.id.details_desc);

        Intent intent = getIntent();
        mPost = Parcels.unwrap(intent.getParcelableExtra("post"));
        checkFlag = intent.getStringExtra("flag");

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

            if (checkFlag.equals(OtherProfile.TAG)) {
                //preventing stacking of profile and post detail activiies
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            }

            intent.putExtra("user", Parcels.wrap(parseUser));
            startActivity(intent);

//            if (checkFlag.equals(MainActivity.TAG)) {
                //improving activity flow for user
                finish();
//            }
        } else {
            //If Clicked on Own Profile/Username
            if(checkFlag.equals(MainActivity.TAG)){
                Log.i(TAG,"Clicked on own profile");
                //Nav back to mainactivity
                Intent intent = new Intent(this,MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra("nav",R.id.action_profile);

                startActivity(intent);
                finish();
            }
        }

    }
}