package com.example.nearme.models;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.example.nearme.MainActivity;
import com.example.nearme.OtherProfile;
import com.example.nearme.PostDetails;
import com.example.nearme.R;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Adapter responsible for controlling and displaying posts on TextFragment
 */
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    public static final String TAG = "PostAdapter";
    private List<Post> mPosts;
    private Context mContext;

    public PostAdapter(Context context, List<Post> posts) {
        this.mPosts = posts;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.text_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = mPosts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public void clearAll() {
        mPosts.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Post> posts) {
        this.mPosts.addAll(posts);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView mPreview;
        private TextView mUsername;
        private TextView mDescription;
        private TextView mTimeStamp;
        private ImageView mThumbsUp;
        private RelativeLayout mRelativeLayout;

        private Post mPost;
        private ArrayList<String> mLikedBy;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mPreview = itemView.findViewById(R.id.post_preview);
            mUsername = itemView.findViewById(R.id.post_user);
            mDescription = itemView.findViewById(R.id.post_desc);
            mTimeStamp = itemView.findViewById(R.id.post_timestamp);
            mThumbsUp = itemView.findViewById(R.id.post_btnLike);
            mRelativeLayout = itemView.findViewById(R.id.post_rl);
            mLikedBy = new ArrayList<>();


            View.OnClickListener moreDetails = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goToMoreDetails();
                }
            };

            mPreview.setOnClickListener(moreDetails);
            mDescription.setOnClickListener(moreDetails);
            mRelativeLayout.setOnClickListener(moreDetails);
            mTimeStamp.setOnClickListener(moreDetails);

            mUsername.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goToUserProfile();
                }
            });
        }

        private void goToMoreDetails() {
            int position = getAdapterPosition();
            Post post = mPosts.get(position);

            Intent intent = new Intent(mContext, PostDetails.class);
            intent.putExtra("post", Parcels.wrap(post));
            intent.putExtra("flag", MainActivity.TAG);
            mContext.startActivity(intent);
        }

        /**
         * Navigates user to clicked on profile
         */
        private void goToUserProfile() {
            Post post = mPosts.get(getAdapterPosition());

            ParseUser parseUser = post.getUser();

            if (!parseUser.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                //If Profile Clicked on Is Not Own
                Intent intent = new Intent(mContext, OtherProfile.class);
                intent.putExtra("user", Parcels.wrap(parseUser));
                mContext.startActivity(intent);
            } else {
                //Notify Main Activity to control back button to allow going back to text fragment
                //like when other profile clicked
                ((MainActivity) mContext).setmBackButtonGoesToLastFragment(true);
                //If clicked on own profile
                ((MainActivity) mContext).setSelectedBottomNav(R.id.action_profile);
            }
        }

        public void bind(Post post) {
            mPost = post;
            ParseUser parseUser = post.getUser();

            final LikeManager likeManager = new LikeManager(
                    ParseUser.getCurrentUser(),
                    mPost,
                    mThumbsUp
            );

            mThumbsUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    likeManager.likePost();
                }
            });

            mUsername.setText(parseUser.getUsername().toUpperCase());
            mDescription.setText(post.getDescription());

            Date date = post.getCreatedAt();
            mTimeStamp.setText((String) DateUtils.getRelativeTimeSpanString(date.getTime()));

            int radius = 40; // corner radius, higher value = more rounded
            int margin = 5; // crop margin, set to 0 for corners with no crop
            int blur = 15;

            Glide.with(mContext)
                    .load(post.getImage().getUrl())
                    .transform(new MultiTransformation<>(
                            new CenterCrop(),
                            new BlurTransformation(blur),
                            new RoundedCornersTransformation(radius, margin)
                    ))
                    .into(mPreview);
        }

        /**
         * Controls thumbs up image based on if liked or not
         */
        private void checkIfPostLiked() {
            if (hasUserLikedPost()) {
                mThumbsUp.setImageResource(R.drawable.ic_baseline_thumb_up_filled_24);
            } else {
                mThumbsUp.setImageResource(R.drawable.ic_outline_thumb_up_24);
            }
        }


        /**
         * Likes/Unlikes post user clicked on
         */
        private void likePost() {
            ParseUser currUser = ParseUser.getCurrentUser();
            final String currUserID = currUser.getObjectId();

            if (!hasUserLikedPost()) {
                mLikedBy.add(currUserID);
                mPost.put(Post.KEY_LIKED, mLikedBy);
                mPost.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            checkIfPostLiked();
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
            ParseUser currUser = ParseUser.getCurrentUser();
            String currUserID = currUser.getObjectId();

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
            ParseUser currUser = ParseUser.getCurrentUser();
            String currUserID = currUser.getObjectId();
            for (String i : mLikedBy) {
                if (currUserID.equals(i)) {
                    return true;
                }
            }
            return false;
        }
    }
}
