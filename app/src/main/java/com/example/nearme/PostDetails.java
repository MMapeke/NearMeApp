package com.example.nearme;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.nearme.models.Post;

import org.parceler.Parcels;

import java.util.Date;

public class PostDetails extends AppCompatActivity {

    private Post post;
    private TextView username;
    private TextView relativeTime;
    private ImageView picture;
    private TextView description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        username = findViewById(R.id.details_username);
        relativeTime = findViewById(R.id.details_time);
        picture = findViewById(R.id.details_pic);
        description = findViewById(R.id.details_desc);
        post = Parcels.unwrap(getIntent().getParcelableExtra("post"));

        if (post != null) {
            username.setText("@" + post.getUser().getUsername());
            Date date = post.getCreatedAt();
            relativeTime.setText((String) DateUtils.getRelativeTimeSpanString(date.getTime()));
            Glide.with(this)
                    .load(post.getImage().getUrl())
                    .into(picture);
            description.setText(post.getDescription());
        }
    }
}