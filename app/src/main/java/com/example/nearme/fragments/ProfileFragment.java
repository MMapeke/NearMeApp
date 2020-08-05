package com.example.nearme.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.nearme.R;
import com.example.nearme.models.Post;
import com.example.nearme.models.ProfileAdapter;
import com.gaurav.cdsrecyclerview.CdsItemTouchCallback;
import com.gaurav.cdsrecyclerview.CdsRecyclerView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;


/**
 * Fragment responsible for viewing current user profile
 */
public class ProfileFragment extends Fragment {

    public static final String TAG = "ProfileFragment";
    public final static int PICK_PHOTO_CODE = 1046;

    private ParseUser mParseUser;

    private ImageView mProfilePic;
    private TextView mUsername;

    private TextView mNumberPosts;
    private TextView mAccountCreated;

    private CdsRecyclerView mRvPosts;
    private ProfileAdapter mProfileAdapter;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mParseUser = ParseUser.getCurrentUser();
        mProfilePic = view.findViewById(R.id.profile_pic);
        mUsername = view.findViewById(R.id.profile_username);

        mRvPosts = view.findViewById(R.id.profile_rvPosts);

        mNumberPosts = view.findViewById(R.id.profile_num_posts);
        mAccountCreated = view.findViewById(R.id.profile_created);

        mProfileAdapter = new ProfileAdapter(getContext(), new ArrayList<Post>(), true,getView());
        mRvPosts.setAdapter(mProfileAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRvPosts.setLayoutManager(linearLayoutManager);

        mRvPosts.enableItemSwipe();
        mRvPosts.setItemSwipeCompleteListener(new CdsItemTouchCallback.ItemSwipeCompleteListener() {
            @Override
            public void onItemSwipeComplete(int i) {
                //auto calls remove method in adapter
                Log.i(TAG,"swipe complete");
            }
        });

        //Setting Account Creation
        Date date = mParseUser.getCreatedAt();
        mAccountCreated.setText((String) DateUtils.getRelativeTimeSpanString(date.getTime()));

        //Setting Username
        mUsername.setText( mParseUser.getUsername().toUpperCase());

        //Setting Profile Pic
        loadProfilePic();

        mProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                 Create intent for picking a photo from the gallery
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
                // So as long as the result is not null, it's safe to use the intent.
                if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                    // Bring up gallery to select a photo
                    startActivityForResult(intent, PICK_PHOTO_CODE);
                }
            }
        });

        queryAllUserPosts();
    }


    /**
     * Loads profile pic into imageView
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


    private Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            // check version of Android on device
            if (Build.VERSION.SDK_INT > 27) {
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(getContext().getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            Uri photoUri = data.getData();

            // Load the image located at photoUri into selectedImage
            Bitmap selectedImage = loadFromUri(photoUri);

            //Making bitmap into ParseFile
            // Convert it to byte
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            // Compress image to lower quality scale 1 - 100
            selectedImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] image = stream.toByteArray();
            ParseFile parseFile = new ParseFile("profile_pic.png", image);

            // Update User w/ the selected image into Parse
            ParseUser parseUser = ParseUser.getCurrentUser();
            parseUser.put("profilePic", parseFile);
            parseUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "uploaded pc no work", e);
                        loadProfilePic();
                    }
                    Log.i(TAG, "pic worked");
                    Toast.makeText(getContext(), "Profile Picture Changed", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    /**
     * Loads all posts of current user
     */
    private void queryAllUserPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        query.whereEqualTo(Post.KEY_USER, ParseUser.getCurrentUser());

        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if (e == null) {
                    mProfileAdapter.addAll(objects);
                    mNumberPosts.setText(String.valueOf(objects.size()));
                    Log.i(TAG, "query successful");
                } else {
                    Log.e(TAG, "error while querying", e);
                }
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            Log.i(TAG, "No Longer Hidden");
            queryAllUserPosts();
        }
    }
}