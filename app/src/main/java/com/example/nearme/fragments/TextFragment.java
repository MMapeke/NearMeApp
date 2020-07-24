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

import com.example.nearme.EndlessRecyclerViewScrollListener;
import com.example.nearme.R;
import com.example.nearme.models.Post;
import com.example.nearme.models.PostAdapter;
import com.example.nearme.models.QueryManager;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class TextFragment extends Fragment {

    public static final String TAG = "TextFragment";
    private QueryManager queryManager;
    List<Post> posts;
    PostAdapter postAdapter;
    RecyclerView recyclerView;
    TextView emptyMSG;
    private SwipeRefreshLayout swipeRefreshLayout;
    private EndlessRecyclerViewScrollListener scrollListener;

    Boolean havePrevBounds = false;
    ParseGeoPoint swBound;
    ParseGeoPoint neBound;

    public TextFragment() {
        // Required empty public constructor
    }

    // Creates a TextFragment given 2 LatLng Bounds (southwest,northeast)
    public static Fragment newInstance(QueryManager qm) {
        TextFragment tFragment =  new TextFragment();
        Bundle args = new Bundle();

        args.putParcelable("qm", Parcels.wrap(qm));

        tFragment.setArguments(args);
        return tFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Get Bound Arguments if Exist
        Bundle bundle = getArguments();
        if(bundle != null){
            queryManager = Parcels.unwrap(bundle.getParcelable("qm"));

            swBound = queryManager.getSwBound();
            neBound = queryManager.getNeBound();
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
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.i(TAG,"loading more posts");
                queryMorePosts(totalItemsCount);
            }
        };

        recyclerView.addOnScrollListener(scrollListener);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG,"swipe fresh triggered");
                queryPosts();
            }
        });
        queryPosts();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            Log.i(TAG,"No Longer Hidden");
            queryPosts();
        }
    }

    public void queryPosts() {
        Log.i(TAG,"Querying posts");
        posts.clear();

        queryManager.getQuery(QueryManager.TEXT_FRAGMENT_SETTINGS)
                .findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if(e == null){
                    posts.addAll(objects);
                    postAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);

                    showEmptyMessage(objects.isEmpty());

                    Log.i(TAG,"Posts queried: " + objects.size());
                }else{
                    Log.e(TAG,"error while querying posts",e);
                }
            }
        });
    }

    private void showEmptyMessage(boolean empty) {
        if(empty){
            emptyMSG.setVisibility(View.VISIBLE);
        }else{
            emptyMSG.setVisibility(View.GONE);
        }
    }

    private void queryMorePosts(int totalItemsCount) {
        queryManager.getQuery(QueryManager.TEXT_FRAGMENT_SETTINGS).setSkip(totalItemsCount)
                .findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if(e == null){
                    posts.addAll(objects);
                    postAdapter.notifyDataSetChanged();

                    showEmptyMessage(objects.isEmpty());

                    Log.i(TAG,"More Posts queried: " + objects.size());
                }else{
                    Log.e(TAG,"error while querying more posts",e);
                }
            }
        });
    }
}