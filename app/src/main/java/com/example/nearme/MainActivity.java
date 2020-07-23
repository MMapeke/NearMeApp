package com.example.nearme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.nearme.fragments.ComposeFragment;
import com.example.nearme.fragments.FilterDialog;
import com.example.nearme.fragments.MapFragment;
import com.example.nearme.fragments.ProfileFragment;
import com.example.nearme.fragments.TextFragment;
import com.example.nearme.models.Post;
import com.example.nearme.models.QueryManager;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.SphericalUtil;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.List;

//TODO: Bug: Empty Message Not Showing
//TODO: UI keeping only 0-24 hours choice, or view all?
//TODO: Visual Display of TimeWithin
//TODO: Distance Filter
//TODO: Refactor more

public class MainActivity extends AppCompatActivity
        implements MapFragment.MapFragmentListener, FilterDialog.FilterDialogListener {

    public static final String TAG = "MainActivity";

    final FragmentManager fragmentManager = getSupportFragmentManager();
    private BottomNavigationView bottomNavigationView;
    FloatingActionButton btnEditLocation;


    private QueryManager queryManager;
    private final float defaultViewRadiusInMeters = 175.0f;

    private Fragment fragment;


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
        ParseUser parseUser = ParseUser.getCurrentUser();
        if(parseUser.getParseGeoPoint("location") == null){
            goLocationActivity();
        }

        QueryManager oldQueryManager = Parcels.unwrap(getIntent().getParcelableExtra("qm"));

        //Initialize Query Settings
        if(oldQueryManager == null){
            initQueryManager();
        } else {
            queryManager = oldQueryManager;
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_text:
                            fragment = TextFragment.newInstance(queryManager);
                        break;
                    case R.id.action_map:
                            fragment = MapFragment.newInstance(queryManager);
                        break;
                    case R.id.action_profile:
                        fragment = new ProfileFragment();
                        break;
                    case R.id.action_post:
                        default:
                        fragment = new ComposeFragment();
                        break;
                }
                if(fragment != null) {
                    fragmentManager.beginTransaction()
                            .replace(R.id.frameContainer, fragment)
                            .commit();
                }
                return true;
            }
        });

        //setting default bottom nav view
        bottomNavigationView.setSelectedItemId(R.id.action_text);

        btnEditLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(MainActivity.this,GetLocation.class);
                startActivity(intent);
            }
        });
    }

    private void initQueryManager() {
        //Find Default GeoPoint Bounds
        ParseUser parseUser = ParseUser.getCurrentUser();
        ParseGeoPoint location = parseUser.getParseGeoPoint("location");
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

        LatLngBounds latLngBounds = calculateBounds(latLng,defaultViewRadiusInMeters);
        LatLng southwest = latLngBounds.southwest;
        LatLng northeast = latLngBounds.northeast;

        //Set Equal to QueryManager Fields
        ParseGeoPoint swBound = new ParseGeoPoint(southwest.latitude,southwest.longitude);
        ParseGeoPoint neBound = new ParseGeoPoint(northeast.latitude,northeast.longitude);
        this.queryManager = new QueryManager(swBound,neBound);
    }

    public LatLngBounds calculateBounds(LatLng center, double radiusInMeters) {
        double distanceFromCenterToCorner = radiusInMeters * Math.sqrt(2.0);
        LatLng southwestCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0);
        LatLng northeastCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0);
        return new LatLngBounds(southwestCorner, northeastCorner);
    }

    private void goLocationActivity(){
        Intent intent = new Intent(this,GetLocation.class);
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
        switch(item.getItemId()) {
            case R.id.sort:
                openFilterDialog();
                return true;
            case R.id.recommend:
                return true;
            case R.id.viewAll:
                viewAll();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void viewAll() {
        //Querying All Posts to Make Bounds that include all posts
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);

        query.findInBackground(new FindCallback<Post>() {
                    @Override
                    public void done(List<Post> objects, ParseException e) {

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();

                        for(Post post: objects) {
                            ParseGeoPoint parseGeoPoint = post.getLocation();
                            LatLng latLng = new LatLng(parseGeoPoint.getLatitude(),parseGeoPoint.getLongitude());
                            builder.include(latLng);
                        }

                        LatLngBounds allBounds = builder.build();
                        ParseGeoPoint sw = new ParseGeoPoint(allBounds.southwest.latitude
                                ,allBounds.southwest.longitude);
                        ParseGeoPoint ne = new ParseGeoPoint(allBounds.northeast.latitude,
                                allBounds.northeast.longitude);

                        queryManager.setSwBound(sw);
                        queryManager.setNeBound(ne);

                        refreshMainActivity();
                    }
                });
    }

    private void refreshMainActivity() {
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent().putExtra("qm", Parcels.wrap(queryManager)));
        overridePendingTransition(0, 0);

        Log.i(TAG,"Main Activity Refreshed");
    }

    private void openFilterDialog() {
        FilterDialog filterDialog = new FilterDialog();
        filterDialog.show(fragmentManager,"filter dialog");
    }

    @Override
    public void applyFilter(int hours) {
        //Update QueryManager to same hours
        queryManager.setHoursWithn(hours);
        //Refresh page
        refreshMainActivity();

        Toast.makeText(this ,"Now Filtering within: " + hours + " hours",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void settingsChanged(QueryManager queryManager) {
        this.queryManager = queryManager;
    }
}