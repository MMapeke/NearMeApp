package com.example.nearme.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.example.nearme.R;
import com.parse.ParseUser;

import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder>{

    private List<Post> posts;
    Context context;

    public PostAdapter(Context context, List<Post> posts) {
        this.posts = posts;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.text_post,parent,false);
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

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView preview;
        private TextView username;
        private TextView description;
        private ParseUser parseUser;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            preview = itemView.findViewById(R.id.post_preview);
            username = itemView.findViewById(R.id.post_user);
            description = itemView.findViewById(R.id.post_desc);
        }

        public void bind(Post post) {
            parseUser = post.getUser();

            username.setText("@" +  parseUser.getUsername().toUpperCase());
            description.setText(post.getDescription());

            int radius = 10; // corner radius, higher value = more rounded
            int margin = 10; // crop margin, set to 0 for corners with no crop
            int blur = 10;

            //TODO: Image seems like not loading unless https
            //TODO: Transformation, rounding, and slight blur
            Glide.with(context)
//                    .load("https://near-me-marc.herokuapp.com/parse/files/near-me-marc/d05aaeb2cb2b8910acc2f5fc8832d1b4_takeout.jpeg")
                    .load(post.getImage().getUrl())
                    .into(preview);
        }
    }
}
