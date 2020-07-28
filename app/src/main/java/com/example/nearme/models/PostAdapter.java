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
import com.example.nearme.OtherProfile;
import com.example.nearme.PostDetails;
import com.example.nearme.R;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private List<Post> posts;
    Context context;

    public PostAdapter(Context context, List<Post> posts) {
        this.posts = posts;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.text_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void clearAll() {
        posts.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Post> posts) {
        this.posts.addAll(posts);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView preview;
        private TextView username;
        private TextView description;
        private ParseUser parseUser;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            preview = itemView.findViewById(R.id.post_preview);
            username = itemView.findViewById(R.id.post_user);
            description = itemView.findViewById(R.id.post_desc);

            preview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    Post post = posts.get(position);

                    Intent intent = new Intent(context, PostDetails.class);
                    intent.putExtra("post", Parcels.wrap(post));
                    context.startActivity(intent);
                }
            });

            username.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goToUserProfile();
                }
            });
        }

        private void goToUserProfile() {
            Post post = posts.get(getAdapterPosition());

            ParseUser parseUser = post.getUser();

            if(!parseUser.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())){
                //If Profile Clicked on Is Not Own
                Intent intent = new Intent(context, OtherProfile.class);
                intent.putExtra("user",Parcels.wrap(parseUser));
                context.startActivity(intent);
            }

        }

        public void bind(Post post) {
            parseUser = post.getUser();

            username.setText("@" + parseUser.getUsername().toUpperCase());
            description.setText(post.getDescription());

            int radius = 40; // corner radius, higher value = more rounded
            int margin = 5; // crop margin, set to 0 for corners with no crop
            int blur = 6;

            Glide.with(context)
                    .load(post.getImage().getUrl())
                    .transform(new MultiTransformation<>(
                            new CenterCrop(),
                            new BlurTransformation(blur),
                            new RoundedCornersTransformation(radius, margin)
                    ))
                    .into(preview);
        }
    }
}
