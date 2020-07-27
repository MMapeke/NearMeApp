package com.example.nearme.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.nearme.EndlessRecyclerViewScrollListener;
import com.example.nearme.FilterChanged;
import com.example.nearme.R;
import com.example.nearme.models.Post;
import com.example.nearme.models.PostAdapter;
import com.example.nearme.models.QueryManager;
import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

public class TextFragment extends Fragment implements FilterChanged {

    public static final String TAG = "TextFragment";
    private QueryManager queryManager;
    PostAdapter postAdapter;
    RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private EndlessRecyclerViewScrollListener scrollListener;

    public TextFragment() {
        // Required empty public constructor
    }

    public void setQueryManager(QueryManager queryManager) {
        this.queryManager = queryManager;
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
        swipeRefreshLayout = view.findViewById(R.id.text_swipeContainer);
        postAdapter = new PostAdapter(getContext(), new ArrayList<Post>());

        recyclerView.setAdapter(postAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.i(TAG, "loading more posts");
                queryMorePosts(totalItemsCount);
            }
        };

        recyclerView.addOnScrollListener(scrollListener);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG, "swipe fresh triggered");
                queryPosts();
            }
        });
        queryPosts();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            Log.i(TAG, "No Longer Hidden");
            queryPosts();
        }
    }

    public void queryPosts() {
        Log.i(TAG, "Querying posts");
        postAdapter.clearAll();

        queryManager.getQuery(10)
                .findInBackground(new FindCallback<Post>() {
                    @Override
                    public void done(List<Post> objects, ParseException e) {
                        if (e == null) {
                            postAdapter.addAll(objects);

                            swipeRefreshLayout.setRefreshing(false);

                            Log.i(TAG, "Posts queried: " + objects.size());
                            if (objects.isEmpty()) {
                                Toast.makeText(getActivity(), "No Posts to Show", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "error while querying posts", e);
                        }
                    }
                });
    }

    private void queryMorePosts(int totalItemsCount) {
        queryManager.getQuery(10)
                .setSkip(totalItemsCount)
                .findInBackground(new FindCallback<Post>() {
                    @Override
                    public void done(List<Post> objects, ParseException e) {
                        if (e == null) {
                            postAdapter.addAll(objects);

                            Log.i(TAG, "More Posts queried: " + objects.size());
                        } else {
                            Log.e(TAG, "error while querying more posts", e);
                        }
                    }
                });
    }

    @Override
    public void filterChanged() {
        QueryManager.Filter currentFilter = queryManager.getCurrentState();

        if (currentFilter == QueryManager.Filter.VIEWALL) {
            queryPosts();
        }

        if (currentFilter == QueryManager.Filter.DEFAULT) {
            queryPosts();
        }
    }
}