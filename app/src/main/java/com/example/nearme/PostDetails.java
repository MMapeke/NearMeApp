package com.example.nearme;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.nearme.models.LikeManager;
import com.example.nearme.models.Post;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Date;

//TODO: add likes

/**
 * Class/Activity responsible for showing more details about posts
 */
public class PostDetails extends AppCompatActivity {

    public static final String TAG = "PostDetails";

    private Post mPost;
    private ArrayList<String> mLikedBy;

    private ImageView mProfilePic;
    private TextView mUsername;
    private TextView mRelativeTime;
    private ImageView mPicture;
    private TextView mDescription;

    private ImageView mLikeBtn;
    private TextView mLikeCount;

    private View mViewPalette;
    private String checkFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        mProfilePic = findViewById(R.id.details_pfp);
        mUsername = findViewById(R.id.details_username);
        mRelativeTime = findViewById(R.id.details_time);
        mPicture = findViewById(R.id.details_pic);
        mDescription = findViewById(R.id.details_desc);
        mViewPalette = findViewById(R.id.details_palette);

        mLikeBtn = findViewById(R.id.details_like_button);
        mLikeCount = findViewById(R.id.details_num_likes);

        Intent intent = getIntent();
        mPost = Parcels.unwrap(intent.getParcelableExtra("post"));
        checkFlag = intent.getStringExtra("flag");

        if (mPost != null) {
            //Setting username
            mUsername.setText(mPost.getUser().getUsername().toUpperCase());

            //Setting relative time
            Date date = mPost.getCreatedAt();
            mRelativeTime.setText((String) DateUtils.getRelativeTimeSpanString(date.getTime()));

            //Setting description
            mDescription.setText(mPost.getDescription());

            // Define an asynchronous listener for image loading
            CustomTarget<Bitmap> target = new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    //Instruct Glide to load the bitmap into the `holder.ivProfile` profile image view
                    mPicture.setImageBitmap(resource);
                    //Use generate() method from the Palette API to get the vibrant color from the bitmap
                    Palette.Builder palette = Palette.from(resource).maximumColorCount(24);

                    palette.generate(new Palette.PaletteAsyncListener() {
                        @Override
                        public void onGenerated(@Nullable Palette palette) {
                            Log.i(TAG,"Palette generated");
                            //Setting color for bottom
                            mViewPalette.setBackgroundColor(palette.getDarkMutedColor(000000));

                        }
                    });
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                    //leaving empty
                }
            };

            // Instruct Glide to load the bitmap into the asynchronous target defined above
            Glide.with(this).asBitmap()
                    .load(mPost.getImage().getUrl())
                    .into(target);

            //Loading profile pic
            loadProfilePic();
        } else {
            Log.e(TAG, "post was null");
            finish();
        }

        View.OnClickListener gotoProfile =  new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToUserProfile();
            }
        };

        mUsername.setOnClickListener(gotoProfile);
        mProfilePic.setOnClickListener(gotoProfile);

        final LikeManager likeManager = new LikeManager(
                ParseUser.getCurrentUser(),
                mPost,
                mLikeBtn,
                mLikeCount
        );

        mLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                likeManager.likePost();
            }
        });
    }

    /**
     * Loads profile pic into imageView
     */
    private void loadProfilePic() {
        ParseFile pfp = mPost.getUser().getParseFile("profilePic");
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
        Log.i(TAG,"pfp loaded/updated");
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

                finish();
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