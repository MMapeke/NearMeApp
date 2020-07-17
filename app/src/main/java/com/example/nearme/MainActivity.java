package com.example.nearme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.ListFragment;

import android.content.Intent;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.example.nearme.fragments.ComposeFragment;
import com.example.nearme.fragments.MapFragment;
import com.example.nearme.fragments.ProfileFragment;
import com.example.nearme.fragments.TextFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity implements MapFragment.MapFragmentListener{

    public static final String TAG = "MainActivity";

    final FragmentManager fragmentManager = getSupportFragmentManager();
    private BottomNavigationView bottomNavigationView;
    LatLngBounds latLngBounds;
    ImageButton btnProfile;
    ImageButton btnLocation;

    //Bounds of View
    boolean boundsStored = false;
//    double sw_lat;
//    double sw_lng;
//    double ne_lat;
//    double ne_lng;
    LatLng swBound;
    LatLng neBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnLocation = findViewById(R.id.btnLocation);
        btnProfile = findViewById(R.id.btnProfile);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        //If User has no location set
        ParseUser parseUser = ParseUser.getCurrentUser();
        if(parseUser.getParseGeoPoint("location") == null){
            goLocationActivity();
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment;
                switch (menuItem.getItemId()) {
                    case R.id.action_text:
                        if(boundsStored){
                            fragment = TextFragment.newInstance(swBound,neBound);
                        }else {
                            fragment = new TextFragment();
                        }
                        break;
                    case R.id.action_map:
                        if(boundsStored){
                            fragment = MapFragment.newInstance(swBound,neBound);
                        }else {
                            fragment = new MapFragment();
                        }
                        break;
                     default:
                        fragment = new ComposeFragment();
                        break;
                }
                if(fragment != null) {
                    fragmentManager.beginTransaction().replace(R.id.frameContainer, fragment).commit();
                }
                return true;
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new ProfileFragment();
                fragmentManager.beginTransaction().replace(R.id.frameContainer, fragment).commit();
            }
        });

        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =  new Intent(MainActivity.this,GetLocation.class);
                startActivity(intent);
            }
        });

        //setting default bottom nav view
        bottomNavigationView.setSelectedItemId(R.id.action_text);

    }

    private void goLocationActivity(){
        Intent intent = new Intent(this,GetLocation.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void viewBoundChanged(LatLng swBound, LatLng neBound) {
        this.swBound = swBound;
        this.neBound = neBound;

        boundsStored = true;
    }
}