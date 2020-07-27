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

//TODO: Field Naming
//TODO: JavaDocs for all classes + private,non self-explanatory methods
//TODO: FAB -> toolbar
//TODO: Profiles

/**
 * Main Entry for app, responsible for setting up and handling nav between everything
 */
public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    private BottomNavigationView bottomNavigationView;
    FloatingActionButton btnEditLocation;


    private QueryManager queryManager;
    ParseUser parseUser = ParseUser.getCurrentUser();

    final FragmentManager fragmentManager = getSupportFragmentManager();
    private int fragmentContainer;

    private MapFragment mapFragment;
    private TextFragment textFragment;
    private ComposeFragment composeFragment;
    private ProfileFragment profileFragment;

    private FilterChanged currFragmentWithFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnEditLocation = findViewById(R.id.main_btnLocation);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //If User has no location set
        if (parseUser.getParseGeoPoint("location") == null) {
            goLocationActivity();
        }

        queryManager = new QueryManager(parseUser.getParseGeoPoint("location"));

        initFragments();
        //Preloading fragments to improve UI/flow
        addAndHideAllFragments();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_text:
                        displayFragment(textFragment);
                        currFragmentWithFilter = textFragment;
                        break;
                    case R.id.action_map:
                        displayFragment(mapFragment);
                        currFragmentWithFilter = mapFragment;
                        break;
                    case R.id.action_profile:
                        displayFragment(profileFragment);
                        currFragmentWithFilter = null;
                        break;
                    case R.id.action_post:
                    default:
                        displayFragment(composeFragment);
                        currFragmentWithFilter = null;
                        break;
                }
                return true;
            }
        });

        //setting default bottom nav view
        bottomNavigationView.setSelectedItemId(R.id.action_map);

        btnEditLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, GetLocation.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initFragments() {
        fragmentContainer = R.id.frameContainer;
        textFragment = (TextFragment) new TextFragment();
        textFragment.setQueryManager(queryManager);
        mapFragment = (MapFragment) new MapFragment();
        mapFragment.setQueryManager(queryManager);
        profileFragment = (ProfileFragment) new ProfileFragment();
        composeFragment = (ComposeFragment) new ComposeFragment();
    }

    private void addAndHideAllFragments() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.add(fragmentContainer, textFragment, "Text");
        fragmentTransaction.hide(textFragment);

        fragmentTransaction.add(fragmentContainer, profileFragment, "Profile");
        fragmentTransaction.hide(profileFragment);

        fragmentTransaction.add(fragmentContainer, mapFragment, "Map");
        fragmentTransaction.hide(mapFragment);

        fragmentTransaction.add(fragmentContainer, composeFragment, "Compose");
        fragmentTransaction.hide(composeFragment);

        fragmentTransaction.commit();
    }

    private void displayFragment(Fragment inp) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.show(inp);


        //Hiding Other Fragments
        if (inp != textFragment) fragmentTransaction.hide(textFragment);
        if (inp != mapFragment) fragmentTransaction.hide(mapFragment);
        if (inp != profileFragment) fragmentTransaction.hide(profileFragment);
        if (inp != composeFragment) fragmentTransaction.hide(composeFragment);

        fragmentTransaction.commit();
        Log.i(TAG, "New Fragment Displayed");
    }

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

    private void logOut() {
        ParseUser.logOut();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void defaultView() {
        queryManager.setCurrentState(QueryManager.Filter.DEFAULT);

        if (currFragmentWithFilter != null) {
            currFragmentWithFilter.filterChanged();
        }
    }

    private void viewAll() {
        queryManager.setCurrentState(QueryManager.Filter.VIEWALL);

        if (currFragmentWithFilter != null) {
            currFragmentWithFilter.filterChanged();
        }
    }
}