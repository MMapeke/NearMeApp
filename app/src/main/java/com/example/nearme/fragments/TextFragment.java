package com.example.nearme.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.nearme.R;
import com.example.nearme.models.Post;
import com.example.nearme.models.PostAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class TextFragment extends Fragment {

    public static final String TAG = "TextFragment";
    List<Post> posts;
    PostAdapter postAdapter;
    RecyclerView recyclerView;
    TextView emptyMSG;
    private SwipeRefreshLayout swipeRefreshLayout;

    Boolean havePrevBounds = false;
    ParseGeoPoint swBound;
    ParseGeoPoint neBound;

    public TextFragment() {
        // Required empty public constructor
    }

    // Creates a TextFragment given 2 LatLng Bounds (southwest,northeast)
    public static Fragment newInstance(LatLng sw, LatLng ne) {
        TextFragment tFragment =  new TextFragment();
        Bundle args = new Bundle();

        args.putParcelable("sw",sw);
        args.putParcelable("ne",ne);

        tFragment.setArguments(args);
        return tFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Get Bound Arguments if Exist
        Bundle bundle = getArguments();
        if(bundle != null){
            LatLng sw = bundle.getParcelable("sw");
            LatLng ne = bundle.getParcelable("ne");

            swBound = new ParseGeoPoint(sw.latitude,sw.longitude);
            neBound = new ParseGeoPoint(ne.latitude,ne.longitude);

            havePrevBounds = true;
        }
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

        emptyMSG = view.findViewById(R.id.text_EmptyMessage);
        recyclerView = view.findViewById(R.id.rvPosts);
        swipeRefreshLayout = view.findViewById(R.id.text_swipeContainer);
        posts = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(),posts);

        recyclerView.setAdapter(postAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG,"swipe fresh triggered");
                posts.clear();
                queryPosts();
            }
        });

        queryPosts();
    }

    private void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);

        if(havePrevBounds){
            //Query within Bounds
            query.whereWithinGeoBox(Post.KEY_LOCATION, swBound, neBound);
        }

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

                    //Showing default message if view empty
                    if(objects.isEmpty()){
                        emptyMSG.setVisibility(View.VISIBLE);
                    }else{
                        emptyMSG.setVisibility(View.GONE);
                    }

                    swipeRefreshLayout.setRefreshing(false);
                }else{
                    Log.e(TAG,"error while quering posts",e);
                }
            }
        });

    }
}