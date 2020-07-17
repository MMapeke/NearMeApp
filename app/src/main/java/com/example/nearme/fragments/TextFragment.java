package com.example.nearme.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.nearme.R;
import com.example.nearme.models.Post;
import com.example.nearme.models.PostAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.FadeInUpAnimator;

public class TextFragment extends Fragment {

    public static final String TAG = "TextFragment";
    List<Post> posts;
    PostAdapter postAdapter;
    RecyclerView recyclerView;

    public TextFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_text, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.rvPosts);
        posts = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(),posts);

        recyclerView.setAdapter(postAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        queryPosts();
    }

    private void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);

        //query based on distance
//        Double maxDistance = (Double) 0.1;
//        ParseGeoPoint lastLocation = ParseUser.getCurrentUser().getParseGeoPoint("location");
//        query.whereWithinMiles("location",lastLocation,maxDistance);

        //Most recently created at top
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        query.setLimit(10);

        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if(e == null){
                    posts.addAll(objects);
                    for(Post i:objects){
                        Log.i(TAG,i.getDescription() + " by: " + i.getUser().getUsername());
                    }
                    Log.i(TAG,"Posts queried");
                    postAdapter.notifyDataSetChanged();
                }else{
                    Log.e(TAG,"error while quering posts",e);
                }
            }
        });

    }
}