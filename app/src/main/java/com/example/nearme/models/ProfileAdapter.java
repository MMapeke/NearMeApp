package com.example.nearme.models;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nearme.PostDetails;
import com.example.nearme.R;

import org.parceler.Parcels;
import org.w3c.dom.Text;

import java.util.Date;
import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {

    private List<Post> posts;
    Context context;

    public ProfileAdapter(Context context, List<Post> posts){
        this.posts = posts;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.profile_post,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView preview;
        private TextView createdAgo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            preview = itemView.findViewById(R.id.profile_post_preview);
            createdAgo = itemView.findViewById(R.id.profile_post_imageTimeAgo);

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
        }

        public void bind(Post post) {
            //Setting Image
            Glide.with(context)
                    .load(post.getImage().getUrl())
                    .into(preview);

            //Setting Timestamp
            Date date = post.getCreatedAt();
            createdAgo.setText((String) DateUtils.getRelativeTimeSpanString(date.getTime()));
        }
    }
}
