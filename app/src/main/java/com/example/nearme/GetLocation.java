package com.example.nearme;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Arrays;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

/**
 * Class/Activity responsible for collecting user location
 */
@RuntimePermissions
public class GetLocation extends AppCompatActivity {

    public static final String TAG = "GetLocation";
    public static final int AUTOCOMPLETE_REQUEST_CODE = 100;

    private Button mBtnCurrentLocation;
    private Button mBtnChooseLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_location);

        //Init Places
        Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_api_key));

        mBtnCurrentLocation = findViewById(R.id.btnCurrentLocation);
        mBtnChooseLocation = findViewById(R.id.btnChooseLocation);

        mBtnCurrentLocation.setClickable(false);
        mBtnChooseLocation.setClickable(false);

        mBtnChooseLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Initialize place field list
                List<Place.Field> fieldList = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG);

                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fieldList)
                        .build(GetLocation.this);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
            }
        });

        mBtnCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn curr location clicked");
                GetLocationPermissionsDispatcher.getLastLocationWithPermissionCheck(GetLocation.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "Place received: " + place.getName());
                onLocation(place.getLatLng());
            } else {
                Log.e(TAG, "error on activity result");
            }
        }
    }

    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    public void getLastLocation() {
        // Get last known recent location using new Google Play Services SDK (v11+)
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            Log.i(TAG, "location found " + location.toString());
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            onLocation(latLng);
                        } else {
                            Log.e(TAG, "location null");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error trying to get last GPS location");
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        GetLocationPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    /**
     * Saves location in the background for the user
     *
     * @param inp - LatLng, representing location
     */
    private void onLocation(LatLng inp) {
        //Disalbing buttons
        mBtnChooseLocation.setClickable(false);
        mBtnCurrentLocation.setClickable(false);

        String msg = "Location: " + (inp.latitude) + "," + (inp.longitude);
        Log.i(TAG, msg);

        //Grab Current User
        ParseUser parseUser = ParseUser.getCurrentUser();
        //Update GeoPoint
        ParseGeoPoint parseGeoPoint = new ParseGeoPoint(inp.latitude, inp.longitude);
        parseUser.put("location", parseGeoPoint);
        parseUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.i(TAG, "location updated");
                    //Go to Main Activity
                    goMainActivity();
                } else {
                    Log.e(TAG, "location not updated");
                }
            }
        });
    }

    /**
     * Navigates User to MainActivity
     */
    private void goMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}