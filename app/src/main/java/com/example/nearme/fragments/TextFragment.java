package com.example.nearme.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

    private QueryManager mQueryManager;
    private PostAdapter mPostAdapter;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mEmptyMsg;
    private EndlessRecyclerViewScrollListener mScrollListener;

    public TextFragment() {
        // Required empty public constructor
    }

    public void setQueryManager(QueryManager queryManager) {
        this.mQueryManager = queryManager;
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

        mRecyclerView = view.findViewById(R.id.rvPosts);
        mSwipeRefreshLayout = view.findViewById(R.id.text_swipeContainer);
        mEmptyMsg = view.findViewById(R.id.text_empty_msg);
        mPostAdapter = new PostAdapter(getContext(), new ArrayList<Post>());

        mRecyclerView.setAdapter(mPostAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mScrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.i(TAG, "loading more posts");
                queryMorePosts(totalItemsCount);
            }
        };

        mRecyclerView.addOnScrollListener(mScrollListener);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG, "swipe fresh triggered");
                queryPosts();
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            Log.i(TAG, "No Longer Hidden");
            queryPosts();
        }
    }

    /**
     * Queries posts for Text Fragment
     */
    public void queryPosts() {
        Log.i(TAG, "Querying posts");

        mQueryManager.getQuery(25)
                .findInBackground(new FindCallback<Post>() {
                    @Override
                    public void done(List<Post> objects, ParseException e) {
                        if (e == null) {
                            mPostAdapter.clearAll();
                            mPostAdapter.addAll(objects);

                            mSwipeRefreshLayout.setRefreshing(false);

                            Log.i(TAG, "Posts queried: " + objects.size());
                            if (objects.isEmpty()) {
                                mEmptyMsg.setVisibility(View.VISIBLE);
                            } else {
                                mEmptyMsg.setVisibility(View.GONE);
                            }
                        } else {
                            Log.e(TAG, "error while querying posts", e);
                        }
                    }
                });
    }

    /**
     * Queries more posts for Text Fragment
     *
     * @param totalItemsCount
     */
    private void queryMorePosts(int totalItemsCount) {
        mQueryManager.getQuery(10)
                .setSkip(totalItemsCount)
                .findInBackground(new FindCallback<Post>() {
                    @Override
                    public void done(List<Post> objects, ParseException e) {
                        if (e == null) {
                            mPostAdapter.addAll(objects);

                            Log.i(TAG, "More Posts queried: " + objects.size());
                        } else {
                            Log.e(TAG, "error while querying more posts", e);
                        }
                    }
                });
    }

    @Override
    public void filterChanged() {
        QueryManager.Filter currentFilter = mQueryManager.getCurrentState();

        if (currentFilter == QueryManager.Filter.VIEWALL) {
            queryPosts();
        }

        if (currentFilter == QueryManager.Filter.DEFAULT) {
            queryPosts();
        }
    }
}