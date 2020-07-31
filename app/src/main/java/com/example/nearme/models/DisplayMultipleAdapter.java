package com.example.nearme.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.example.nearme.R;

import java.util.List;

/**
 * Adapter for viewpager on recommendation activity
 */
public class DisplayMultipleAdapter extends PagerAdapter {

    private List<Post> posts;
    private LayoutInflater layoutInflater;
    private Context context;

    public DisplayMultipleAdapter(List<Post> posts, Context context) {
        this.posts = posts;
        this.context = context;
    }

    @Override
    public int getCount() {
        return posts.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.multiple_post_item, container, false);

        ImageView imageView;
        TextView desc;

        imageView = view.findViewById(R.id.rec_item_image);
        desc = view.findViewById(R.id.rec_item_text);

        //Set Stuff Here
        Post post = posts.get(position);

        Glide.with(context)
                .load(post.getImage().getUrl())
                .into(imageView);

        desc.setText(post.getDescription());

        container.addView(view, 0);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
