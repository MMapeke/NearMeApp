package com.example.nearme;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

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

    private RelativeLayout mRelativeLayout;
    private Button mBtnCurrentLocation;
    private Button mBtnChooseLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_location);

        //Init Places
        Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_api_key));

        mRelativeLayout = findViewById(R.id.get_location_rl);
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
                locationNotFound();
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
                            locationNotFound();
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

    /**
     * Called when location error or not found
     */
    private void locationNotFound() {
        Snackbar.make(mRelativeLayout, "Was not able to grab location. Please try again!", Snackbar.LENGTH_SHORT)
                .setAction("Set Manually", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openManualDialog();
                    }
                })
                .show();
    }

    /**
     * Opens manual dialog for user to enter coords,
     */
    private void openManualDialog() {
        LayoutInflater inflater = this.getLayoutInflater();
        final View alertDialogView = inflater.inflate(R.layout.fragment_manual_location, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(alertDialogView);
        builder.setTitle("Enter Location Manually");
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //nothing to be overriden below
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();


        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText latitude = alertDialogView.findViewById(R.id.manual_location_lat);
                EditText longitude = alertDialogView.findViewById(R.id.manual_location_long);

                if (latitude.getText().toString().isEmpty() || longitude.getText().toString().isEmpty()) {
                    Snackbar.make(alertDialogView, "Latitude and Longitude Can Not Be Empty!", Snackbar.LENGTH_SHORT).show();
                } else {
                    Double lat = Double.valueOf(String.valueOf(latitude.getText()));
                    Double lng = Double.valueOf(String.valueOf(longitude.getText()));

                    if (!areCoordinatesValid(lat, lng)) {
                        Log.i(TAG, "coords weren't valid");
                        Snackbar.make(alertDialogView, "Lat must be between -90 to 90, Long between -180 to 180", Snackbar.LENGTH_SHORT).show();
                    } else {
                        LatLng latLng = new LatLng(lat, lng);
                        Log.i(TAG, latLng.latitude + " " + latLng.longitude + " grabbed from dialog");
                        onLocation(latLng);
                        dialog.dismiss();
                    }
                }
            }
        });
    }

    /**
     * returns boolean determing if coords are valid
     *
     * @param lat - Double, representing latitude
     * @param lng - DOuble, representing longitude
     * @return - boolean telling if coordinates are valid
     */
    private Boolean areCoordinatesValid(Double lat, Double lng) {
        return (lat <= 90) && (lat >= -90) && (lng >= -180) && (lng <= 180);
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