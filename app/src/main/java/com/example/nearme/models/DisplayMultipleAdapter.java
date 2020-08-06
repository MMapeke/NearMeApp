package com.example.nearme.models;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.nearme.MainActivity;
import com.example.nearme.OtherProfile;
import com.example.nearme.R;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.Date;
import java.util.List;

/**
 * Adapter for viewpager on recommendation activity
 */
public class DisplayMultipleAdapter extends PagerAdapter {

    public static final String TAG = "DisplayMultipleAdapter";
    private List<Post> posts;
    private LayoutInflater layoutInflater;
    private Context context;

    public DisplayMultipleAdapter(List<Post> posts, Context context) {
        this.posts = posts;
        this.context = context;
    }

    @Override
    public int getCount() {
        return posts.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.activity_post_details, container, false);

        //Initialize Views
        ImageView profilePic = view.findViewById(R.id.details_pfp);
        TextView username = view.findViewById(R.id.details_username);
        TextView relativeTime = view.findViewById(R.id.details_time);
        final ImageView picture = view.findViewById(R.id.details_pic);
        TextView description = view.findViewById(R.id.details_desc);
        final View viewPalette = view.findViewById(R.id.details_palette);

        ImageView likeBtn = view.findViewById(R.id.details_like_button);
        TextView likeCount = view.findViewById(R.id.details_num_likes);

        //Get post
        final Post post = posts.get(position);

        //Set Username
        username.setText(post.getUser().getUsername().toUpperCase());

        //Set Description
        description.setText(post.getDescription());

        //Set relative time
        Date date = post.getCreatedAt();
        relativeTime.setText((String) DateUtils.getRelativeTimeSpanString(date.getTime()));

        // Define an asynchronous listener for image loading
        CustomTarget<Bitmap> target = new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                //Instruct Glide to load the bitmap into the `holder.ivProfile` profile image view
                picture.setImageBitmap(resource);
                //Use generate() method from the Palette API to get the vibrant color from the bitmap
                Palette.Builder palette = Palette.from(resource).maximumColorCount(24);

                palette.generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(@Nullable Palette palette) {
                        Log.i(TAG,"Palette generated");
                        //Setting color for bottom
                        viewPalette.setBackgroundColor(palette.getDarkMutedColor(000000));
                    }
                });
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                //leaving empty
            }
        };

        //Getting Palette and Loading Pic
        Glide.with(context)
                .asBitmap()
                .load(post.getImage().getUrl())
                .into(target);

        //Loading profile pic
        ParseFile pfp = post.getUser().getParseFile("profilePic");
        if (pfp == null) {
            Glide.with(context)
                    .load(R.drawable.default_pic)
                    .circleCrop()
                    .into(profilePic);
        } else {
            Glide.with(context)
                    .load(pfp.getUrl())
                    .circleCrop()
                    .into(profilePic);
        }
        Log.i(TAG,"pfp loaded/updated");

        //SET CLICK LISTENERS
        View.OnClickListener goToProfile = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUser parseUser = post.getUser();
                    //If Profile Clicked on Is Not Own
                    Intent intent = new Intent(context, OtherProfile.class);
                    intent.putExtra("user", Parcels.wrap(parseUser));
                    context.startActivity(intent);
            }
        };

        username.setOnClickListener(goToProfile);
        profilePic.setOnClickListener(goToProfile);

        //SETTING LIKE FUNCTIONALITY
        final LikeManager likeManager = new LikeManager(
                ParseUser.getCurrentUser(),
                post,
                likeBtn,
                likeCount);

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                likeManager.likePost();
            }
        });

        container.addView(view, 0);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
