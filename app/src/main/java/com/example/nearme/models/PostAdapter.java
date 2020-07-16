package com.example.nearme.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.util.Log;
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
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.nearme.R;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;


import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import static androidx.constraintlayout.widget.Constraints.TAG;

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
