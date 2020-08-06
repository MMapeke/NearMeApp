package com.example.nearme.models;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nearme.MainActivity;
import com.example.nearme.OtherProfile;
import com.example.nearme.PostDetails;
import com.example.nearme.R;
import com.gaurav.cdsrecyclerview.CdsRecyclerViewAdapter;
import com.google.android.material.snackbar.Snackbar;
import com.parse.DeleteCallback;
import com.parse.ParseException;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Adapter responsible for controlling and displaying posts in Any User Profile
 */
public class ProfileAdapter extends CdsRecyclerViewAdapter<Post, ProfileAdapter.ViewHolder> {

    public static final String TAG = "ProfileAdapter";

    private List<Post> mPosts;
    private Context mContext;
    //used for controlling nav and flow
    private Boolean mViewingOwnProfile;
    //for displaying snackbar
    private View view;

    public ProfileAdapter(Context context, List<Post> posts, boolean viewingOwnProfile) {
        super(context, posts);
        this.mPosts = posts;
        this.mContext = context;
        this.mViewingOwnProfile = viewingOwnProfile;
    }

    public ProfileAdapter(Context context, List<Post> posts, boolean viewingOwnProfile, View view) {
        super(context, posts);
        this.mPosts = posts;
        this.mContext = context;
        this.view = view;
        this.mViewingOwnProfile = viewingOwnProfile;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.profile_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }


    @Override
    public void bindHolder(ViewHolder holder, int position) {
        Post post = mPosts.get(position);
        holder.bind(post);
    }

    @Override
    public void removeItem(final int position) {
        final Post post = mPosts.get(position);
        mPosts.remove(position);
        notifyDataSetChanged();

        //Deleting post in parse after a delay
        final Timer timer = new Timer("Timer");
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                post.deleteInBackground(new DeleteCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Log.i(TAG, "post deleted successfully");
                        } else {
                            Log.e(TAG, "post not deleted correctly", e);
                        }
                    }
                });
            }
        };

        timer.schedule(task, 3500L);
        Snackbar.make(view, "Post Deleted", Snackbar.LENGTH_SHORT)
                .setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        timer.cancel();
                        mPosts.add(position, post);
                        notifyDataSetChanged();
                    }
                })
                .show();
    }

    public void addAll(List<Post> inp) {
        mPosts.clear();
        mPosts.addAll(inp);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView mPreview;
        private TextView mCreatedAgo;
        private TextView mNumLikes;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mPreview = itemView.findViewById(R.id.profile_post_preview);
            mCreatedAgo = itemView.findViewById(R.id.profile_post_imageTimeAgo);
            mNumLikes = itemView.findViewById(R.id.profile_post_likes);

            mPreview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Post post = mPosts.get(getAdapterPosition());

                    Intent intent = new Intent(mContext, PostDetails.class);
                    intent.putExtra("post", Parcels.wrap(post));

                    if (!mViewingOwnProfile) {
                        intent.putExtra("flag", OtherProfile.TAG);
                    } else {
                        intent.putExtra("flag", MainActivity.TAG);
                    }

                    mContext.startActivity(intent);
                }
            });
        }

        public void bind(Post post) {
            //Setting Image
            Glide.with(mContext)
                    .load(post.getImage().getUrl())
                    .into(mPreview);

            //Setting Timestamp
            Date date = post.getCreatedAt();
            mCreatedAgo.setText((String) DateUtils.getRelativeTimeSpanString(date.getTime()));

            //Setting number of likes
            ArrayList<String> postLikedBy = new ArrayList<>();
            if (post.getLikes() != null) {
                postLikedBy = post.getLikes();
            }

            String numLikes = "\u25BA" + "Liked By " + String.valueOf(postLikedBy.size());
            mNumLikes.setText(numLikes);
        }

    }

}
