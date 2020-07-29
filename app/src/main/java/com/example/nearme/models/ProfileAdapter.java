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
import com.parse.DeleteCallback;
import com.parse.ParseException;

import org.parceler.Parcels;

import java.util.Date;
import java.util.List;

/**
 * Adapter responsible for controlling and displaying posts in Any User Profile
 */
public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {

    public static final String TAG = "ProfileAdapter";

    private List<Post> mPosts;
    private Context mContext;
    private Boolean mViewingOwnProfile;

    public ProfileAdapter(Context context, List<Post> posts, boolean viewingOwnProfile) {
        this.mPosts = posts;
        this.mContext = context;
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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = mPosts.get(position);
        holder.bind(post);
    }

    private void deletePost(int adapterPosition) {
        Post post = mPosts.get(adapterPosition);

        //Delete locally
        mPosts.remove(adapterPosition);
        //Delete from Parse
        post.deleteInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.i(TAG, "deleted succesfully");
                } else {
                    Log.e(TAG, "deleteing error", e);
                }
            }
        });
        //Notify Changed
        notifyDataSetChanged();
    }

    public void addAll(List<Post> inp) {
        mPosts.clear();
        mPosts.addAll(inp);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView preview;
        private ImageView delete;
        private TextView createdAgo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            preview = itemView.findViewById(R.id.profile_post_preview);
            createdAgo = itemView.findViewById(R.id.profile_post_imageTimeAgo);
            delete = itemView.findViewById(R.id.profile_trash);

            if (!mViewingOwnProfile) delete.setVisibility(View.GONE);


            preview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    Post post = mPosts.get(position);

                    Intent intent = new Intent(mContext, PostDetails.class);
                    intent.putExtra("post", Parcels.wrap(post));

                    if(!mViewingOwnProfile){
                        intent.putExtra("flag", OtherProfile.TAG);
                    }else{
                        intent.putExtra("flag", MainActivity.TAG);
                    }

                    mContext.startActivity(intent);
                }
            });

            if (mViewingOwnProfile) {
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deletePost(getAdapterPosition());
                    }
                });
            }
        }

        public void bind(Post post) {
            //Setting Image
            Glide.with(mContext)
                    .load(post.getImage().getUrl())
                    .into(preview);

            //Setting Timestamp
            Date date = post.getCreatedAt();
            createdAgo.setText((String) DateUtils.getRelativeTimeSpanString(date.getTime()));
        }

    }

}
