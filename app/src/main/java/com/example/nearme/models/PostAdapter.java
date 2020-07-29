package com.example.nearme.models;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Adapter responsible for controlling and displaying posts on TextFragment
 */
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

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
        private ParseUser mParseUser;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mPreview = itemView.findViewById(R.id.post_preview);
            mUsername = itemView.findViewById(R.id.post_user);
            mDescription = itemView.findViewById(R.id.post_desc);

            mPreview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    Post post = mPosts.get(position);

                    Intent intent = new Intent(mContext, PostDetails.class);
                    intent.putExtra("post", Parcels.wrap(post));
                    intent.putExtra("flag", MainActivity.TAG);
                    mContext.startActivity(intent);
                }
            });

            mUsername.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goToUserProfile();
                }
            });
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
            }else{
                //If clicked on own profile
                ((MainActivity) mContext).setSelectedBottomNav(R.id.action_profile);
            }
        }

        public void bind(Post post) {
            mParseUser = post.getUser();

            mUsername.setText("@" + mParseUser.getUsername().toUpperCase());
            mDescription.setText(post.getDescription());

            int radius = 40; // corner radius, higher value = more rounded
            int margin = 5; // crop margin, set to 0 for corners with no crop
            int blur = 6;

            Glide.with(mContext)
                    .load(post.getImage().getUrl())
                    .transform(new MultiTransformation<>(
                            new CenterCrop(),
                            new BlurTransformation(blur),
                            new RoundedCornersTransformation(radius, margin)
                    ))
                    .into(mPreview);
        }
    }
}
