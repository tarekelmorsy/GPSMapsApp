package com.example.gpsmapsapp;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.test.mock.MockPackageManager;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES =  15000; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;
    private boolean stopped = false;

    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;


    private Location location; // location
    double latitude, longitude; // longitude

    private static final int REQUEST_CODE_PERMISSION = 2;
    String locationPermission = Manifest.permission.ACCESS_FINE_LOCATION;

    private GoogleMap mMap;
    private static int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().
                findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


//    @Override
//    protected void onResume() {
//        super.onResume();
//    }

    @Override
    protected void onResume() {
        super.onResume();
        if(stopped) {
            drawMyLocation();
            stopped = false;
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        stopped = true;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */



    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == MockPackageManager.PERMISSION_GRANTED) {


                startMapOperation(googleMap);

            }else{

                ActivityCompat.requestPermissions(this, new String[]{locationPermission},
                        REQUEST_CODE_PERMISSION);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void startMapOperation(GoogleMap googleMap) {
        mMap = googleMap;
        Toast.makeText(this,"Map is Ready", Toast.LENGTH_LONG).show();
        // Add a marker in Sydney and move the camera
        drawMyLocation();

    }

    private void drawMyLocation() {
        location = getLocation();
        if(location != null) {
            LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());


            mMap.addMarker(new MarkerOptions().position(myLocation).title("I am Here"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(8.0f));
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        drawMyLocation();
        Toast.makeText(this,"location changed", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {

                showSettingsAlert();
            } else {
//                this.canGetLocation = true;
                // First get location from Network Provider
                if (isGPSEnabled) {
                    if (ContextCompat.checkSelfPermission(MapsActivity.this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
//                        boolean mLocationPermissionGranted = true;



                        if (location == null) {
                            locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, (LocationListener) this);

                            Log.d("GPS Enabled", "GPS Enabled");

                            if (locationManager != null) {
                                location = locationManager
                                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);

                            }

                        }

                    } else {
                        ActivityCompat.requestPermissions(this,
                                new String[]{locationPermission},
                                REQUEST_CODE_PERMISSION);
                    }

                }

                // if GPS Enabled get lat/long using GPS Services
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    public void showSettingsAlert(){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle(getString(R.string.title_dialog));

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_PERMISSION || requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION){

            drawMyLocation();
        }
    }
}