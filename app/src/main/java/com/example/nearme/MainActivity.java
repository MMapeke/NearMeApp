package com.example.nearme;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.nearme.fragments.ComposeFragment;
import com.example.nearme.fragments.MapFragment;
import com.example.nearme.fragments.ProfileFragment;
import com.example.nearme.fragments.TextFragment;
import com.example.nearme.models.QueryManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.ParseUser;


//TODO: Read about array types parse/Recommendation

//TODO: FAB -> toolbar
//TODO: Improve REadMe + better description of feature


/**
 * Main Entry for app, responsible for setting up and handling nav between everything
 */
public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private final FragmentManager sFragmentManager = getSupportFragmentManager();


    private BottomNavigationView mBottomNavView;
    private FloatingActionButton mBtnEditLocation;

    private QueryManager mQueryManager;
    private ParseUser mParseUser = ParseUser.getCurrentUser();

    private int mFragmentContainer;

    private MapFragment mMapFragment;
    private TextFragment mTextFragment;
    private ComposeFragment mComposeFragment;
    private ProfileFragment mProfileFragment;

    private FilterChanged mCurrentFragmentWithFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG,"new main activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtnEditLocation = findViewById(R.id.main_btnLocation);
        mBottomNavView = findViewById(R.id.bottom_navigation);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //If User has no location set
        if (mParseUser.getParseGeoPoint("location") == null) {
            goLocationActivity();
        }

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
                        break;
                    case R.id.action_map:
                        displayFragment(mMapFragment);
                        mCurrentFragmentWithFilter = mMapFragment;
                        break;
                    case R.id.action_profile:
                        displayFragment(mProfileFragment);
                        mCurrentFragmentWithFilter = null;
                        break;
                    case R.id.action_post:
                    default:
                        displayFragment(mComposeFragment);
                        mCurrentFragmentWithFilter = null;
                        break;
                }
                return true;
            }
        });

        //setting default bottom nav view
        mBottomNavView.setSelectedItemId(R.id.action_map);

        mBtnEditLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, GetLocation.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void setSelectedBottomNav(int inp){
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
        Log.i(TAG,"RESUMED");
        int navigateTo = getIntent().getIntExtra("nav",0);

        if(navigateTo != 0){
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

        MenuItem menuItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search Here!");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.recommend:
                return true;
            case R.id.viewAll:
                viewAll();
                return true;
            case R.id.defaultView:
                defaultView();
                return true;
            case R.id.logout:
                logOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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