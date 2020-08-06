package com.example.nearme;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.nearme.fragments.ComposeFragment;
import com.example.nearme.fragments.MapFragment;
import com.example.nearme.fragments.ProfileFragment;
import com.example.nearme.fragments.TextFragment;
import com.example.nearme.models.GrabRecommendations;
import com.example.nearme.models.QueryManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseUser;

/**
 * Main Entry for app, responsible for setting up and handling nav between everything
 */
public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private final FragmentManager sFragmentManager = getSupportFragmentManager();

    MenuItem viewAll;
    MenuItem defaultView;

    private BottomNavigationView mBottomNavView;

    private QueryManager mQueryManager;
    private ParseUser mParseUser = ParseUser.getCurrentUser();

    private int mFragmentContainer;

    private MapFragment mMapFragment;
    private TextFragment mTextFragment;
    private ComposeFragment mComposeFragment;
    private ProfileFragment mProfileFragment;

    private FilterChanged mCurrentFragmentWithFilter;

    private Boolean mBackButtonGoesToLastFragment = false;
    private Fragment mCurrentFragment;
    private Fragment mLastFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "new main activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBottomNavView = findViewById(R.id.bottom_navigation);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mQueryManager = new QueryManager(mParseUser.getParseGeoPoint("location"));

        initFragments();
        //Preloading fragments to improve UI/flow
        addAndHideAllFragments();

        mBottomNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_text:
                        displayFragment(mTextFragment);
                        mCurrentFragmentWithFilter = mTextFragment;
                        mCurrentFragment = mTextFragment;
                        break;
                    case R.id.action_map:
                        displayFragment(mMapFragment);
                        mCurrentFragmentWithFilter = mMapFragment;
                        mCurrentFragment = mMapFragment;
                        break;
                    case R.id.action_profile:
                        displayFragment(mProfileFragment);
                        mCurrentFragmentWithFilter = null;
                        mCurrentFragment = mProfileFragment;
                        break;
                    case R.id.action_post:
                    default:
                        displayFragment(mComposeFragment);
                        mCurrentFragmentWithFilter = null;
                        mCurrentFragment = mComposeFragment;
                        break;
                }
                return true;
            }
        });

        //setting default bottom nav view
        mBottomNavView.setSelectedItemId(R.id.action_text);
    }

    public void setSelectedBottomNav(int inp) {
        mBottomNavView.setSelectedItemId(inp);
    }

    //Needed to override to allow onResume to accept new intents
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "RESUMED");
        //representing bottom nav option to navigate to
        int navigateTo = getIntent().getIntExtra("nav", 0);

        if (navigateTo != 0) {
            //Notify Main Activity to control back button to allow going back to text fragment
            //like when other profile clicked
            mBackButtonGoesToLastFragment = true;

            setSelectedBottomNav(navigateTo);
            //Removes extra, so behavior doesnt continue next time resuming MainActivity
            getIntent().removeExtra("nav");
        }
    }

    /**
     * Initializes fragments
     */
    private void initFragments() {
        mFragmentContainer = R.id.frameContainer;
        mTextFragment = (TextFragment) new TextFragment();
        mTextFragment.setQueryManager(mQueryManager);
        mMapFragment = (MapFragment) new MapFragment();
        mMapFragment.setQueryManager(mQueryManager);
        mProfileFragment = (ProfileFragment) new ProfileFragment();
        mComposeFragment = (ComposeFragment) new ComposeFragment();
    }

    /**
     * Adds and Hides all fragments to container
     */
    private void addAndHideAllFragments() {
        FragmentTransaction fragmentTransaction = sFragmentManager.beginTransaction();

        fragmentTransaction.add(mFragmentContainer, mTextFragment, "Text");
        fragmentTransaction.hide(mTextFragment);

        fragmentTransaction.add(mFragmentContainer, mProfileFragment, "Profile");
        fragmentTransaction.hide(mProfileFragment);

        fragmentTransaction.add(mFragmentContainer, mMapFragment, "Map");
        fragmentTransaction.hide(mMapFragment);

        fragmentTransaction.add(mFragmentContainer, mComposeFragment, "Compose");
        fragmentTransaction.hide(mComposeFragment);

        fragmentTransaction.commit();
    }

    /**
     * Displays fragment and hides others
     *
     * @param inp - Fragment, fragment to show
     */
    private void displayFragment(Fragment inp) {
        if (inp != mProfileFragment) mBackButtonGoesToLastFragment = false;
        mLastFragment = mCurrentFragment;

        FragmentTransaction fragmentTransaction = sFragmentManager.beginTransaction();
        fragmentTransaction.show(inp);

        //Hiding Other Fragments
        if (inp != mTextFragment) fragmentTransaction.hide(mTextFragment);
        if (inp != mMapFragment) fragmentTransaction.hide(mMapFragment);
        if (inp != mProfileFragment) fragmentTransaction.hide(mProfileFragment);
        if (inp != mComposeFragment) fragmentTransaction.hide(mComposeFragment);

        fragmentTransaction.commit();
        Log.i(TAG, "New Fragment Displayed");
    }

    @Override
    public void onBackPressed() {
        if (mBackButtonGoesToLastFragment) {
            selectNavOptionForFragment(mLastFragment);
            mBackButtonGoesToLastFragment = false;
        } else {
            super.onBackPressed();
        }
    }

    /**
     * selects appropriate bottom nav option for inp fragment
     *
     * @param inp - fragment, representing fragment to select option for
     */
    private void selectNavOptionForFragment(Fragment inp) {
        if (mTextFragment == inp) {
            mBottomNavView.setSelectedItemId(R.id.action_text);
        } else if (mMapFragment == inp) {
            mBottomNavView.setSelectedItemId(R.id.action_map);
        } else if (mProfileFragment == inp) {
            mBottomNavView.setSelectedItemId(R.id.action_profile);
        } else {
            mBottomNavView.setSelectedItemId(R.id.action_post);
        }
    }

    public void setmBackButtonGoesToLastFragment(Boolean mBackButtonGoesToLastFragment) {
        this.mBackButtonGoesToLastFragment = mBackButtonGoesToLastFragment;
    }

    /**
     * Navigates User to Location Activity
     */
    private void goLocationActivity() {
        Intent intent = new Intent(this, GetLocation.class);
        startActivity(intent);
        finish();
    }

    // Menu Icons are Inflated just as they would be with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        viewAll = menu.findItem(R.id.viewAll);
        defaultView = menu.findItem(R.id.defaultView);
        defaultView.setVisible(false);

//        MenuItem menuItem = menu.findItem(R.id.app_bar_search);
//        SearchView searchView = (SearchView) menuItem.getActionView();
//        searchView.setQueryHint("Search Here!");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.editLocation:
                goLocationActivity();
                return true;
            case R.id.recommend:
                goToRecommendation();
                return true;
            case R.id.viewAll:
                viewAll();
                viewAll.setVisible(false);
                defaultView.setVisible(true);

                return true;
            case R.id.defaultView:
                defaultView();
                defaultView.setVisible(false);
                viewAll.setVisible(true);

                return true;
            case R.id.logout:
                logOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * goes to recommendation activity/screen
     */
    private void goToRecommendation() {
//        Intent intent = new Intent(this,Recommendation.class);
//        startActivity(intent);

        GrabRecommendations grabRecommendations = new GrabRecommendations(this);
        grabRecommendations.showRecommendations();
    }

    /**
     * Logs Out User
     */
    private void logOut() {
        ParseUser.logOut();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Switches QueryManager State back to Default
     */
    private void defaultView() {
        mQueryManager.setCurrentState(QueryManager.Filter.DEFAULT);

        if (mCurrentFragmentWithFilter != null) {
            mCurrentFragmentWithFilter.filterChanged();
        }
    }

    /**
     * Switches QueryManager State to viewAll
     */
    private void viewAll() {
        mQueryManager.setCurrentState(QueryManager.Filter.VIEWALL);

        if (mCurrentFragmentWithFilter != null) {
            mCurrentFragmentWithFilter.filterChanged();
        }
    }
}