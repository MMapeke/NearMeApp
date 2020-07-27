package com.example.nearme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.nearme.fragments.ComposeFragment;
import com.example.nearme.fragments.MapFragment;
import com.example.nearme.fragments.ProfileFragment;
import com.example.nearme.fragments.TextFragment;
import com.example.nearme.models.QueryManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.ParseUser;

import org.parceler.Parcels;

//TODO: [BUG] If By Default Load Text Fragment + Load Map In Background -> View All, crashes
//TODO: [BUG] If GO to Location w/ no Posts, Text Fragment shows empty toast

public class MainActivity extends AppCompatActivity{

    public static final String TAG = "MainActivity";

    private BottomNavigationView bottomNavigationView;
    FloatingActionButton btnEditLocation;


    private QueryManager queryManager;

    final FragmentManager fragmentManager = getSupportFragmentManager();
    private int fragmentContainer;

    private MapFragment mapFragment;
    private TextFragment textFragment;
    private ComposeFragment composeFragment;
    private ProfileFragment profileFragment;

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
            queryManager = new QueryManager(parseUser.getParseGeoPoint("location"));
        } else {
            queryManager = oldQueryManager;
        }

        fragmentContainer = R.id.frameContainer;
        textFragment = (TextFragment) TextFragment.newInstance(queryManager);
        mapFragment = (MapFragment) MapFragment.newInstance(queryManager);
        profileFragment = (ProfileFragment) new ProfileFragment();
        composeFragment = (ComposeFragment) new ComposeFragment();

        //Loading up Text Fragment to so settings don't break
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(fragmentContainer,textFragment,"Text");
        fragmentTransaction.hide(textFragment);

        fragmentTransaction.commit();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_text:
                        displayTextFragment();
                        break;
                    case R.id.action_map:
                        displayMapFragment();
                        break;
                    case R.id.action_profile:
                        displayProfileFragment();
                        break;
                    case R.id.action_post:
                        default:
                            displayComposeFragment();
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
                Intent intent =  new Intent(MainActivity.this,GetLocation.class);
                startActivity(intent);
            }
        });
    }

    //TODO: REfactor display + hiding with one method w/ arg for frag
    //may have to make fragment transaction local each time
    private void displayTextFragment() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if(textFragment.isAdded()){
            fragmentTransaction.show(textFragment);
        } else {
            fragmentTransaction.add(fragmentContainer,textFragment,"Text");
        }

        //Hide Other Fragments
        if(mapFragment.isAdded()) fragmentTransaction.hide(mapFragment);
        if(profileFragment.isAdded()) fragmentTransaction.hide(profileFragment);
        if(composeFragment.isAdded()) fragmentTransaction.hide(composeFragment);

        fragmentTransaction.commit();
    }

    private void displayMapFragment(){
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if(mapFragment.isAdded()){
            fragmentTransaction.show(mapFragment);
        } else {
            fragmentTransaction.add(fragmentContainer,mapFragment,"Map");
        }

        //Hide Other Fragments
        if(textFragment.isAdded()) fragmentTransaction.hide((textFragment));
        if(profileFragment.isAdded()) fragmentTransaction.hide(profileFragment);
        if(composeFragment.isAdded()) fragmentTransaction.hide(composeFragment);

        fragmentTransaction.commit();
    }

    private void displayProfileFragment(){
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if(profileFragment.isAdded()){
            fragmentTransaction.show(profileFragment);
        } else {
            fragmentTransaction.add(fragmentContainer,profileFragment,"Profile");
        }

        //Hide Other Fragments
        if(textFragment.isAdded()) fragmentTransaction.hide(textFragment);
        if(mapFragment.isAdded()) fragmentTransaction.hide(mapFragment);
        if(composeFragment.isAdded()) fragmentTransaction.hide(composeFragment);

        fragmentTransaction.commit();
    }

    private void displayComposeFragment(){
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if(composeFragment.isAdded()){
            fragmentTransaction.show(composeFragment);
        } else {
            fragmentTransaction.add(fragmentContainer,composeFragment,"Compose");
        }

        //Hide Other Fragments
        if(textFragment.isAdded()) fragmentTransaction.hide(textFragment);
        if(mapFragment.isAdded()) fragmentTransaction.hide(mapFragment);
        if(profileFragment.isAdded()) fragmentTransaction.hide(profileFragment);

        fragmentTransaction.commit();
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
            case R.id.recommend:
                return true;
            case R.id.viewAll:
                viewAll();
                return true;
            case R.id.defaultView:
                defaultView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void defaultView() {
        queryManager.setCurrentState(QueryManager.Filter.DEFAULT);

        textFragment.queryPosts();
        mapFragment.reCenter();
        mapFragment.queryPosts();
    }

    private void viewAll(){
        queryManager.setCurrentState(QueryManager.Filter.VIEWALL);

        textFragment.queryPosts();
        mapFragment.queryPosts();
    }
}