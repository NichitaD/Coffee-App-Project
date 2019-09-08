package com.example.wert;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import com.example.wert.services.LocationTracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    int ERROR_DIALOG_REQUEST = 1;
    final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2;
    int PERMISSIONS_REQUEST_ENABLE_GPS = 3;
    private static final int LOCATION_UPDATE_INTERVAL = 3000;
    private int locationUpdateNumber = 1;
    private boolean mLocationPermissionGranted = false;
    private static final String TAG = "MapsActivityLog : ";
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    public Marker MyLocation;
    public LatLng myLocation;
    private LocationTracker locationTracker = new LocationTracker();


    //////// Creating some fake Coffee Shops,

    CoffeeShop fiveToGo = new CoffeeShop("5 to go", new LatLng(46.544868,24.560155));
    CoffeeShop captainBean = new CoffeeShop("Captain Bean", new LatLng(46.5420565,24.5542164));
    CoffeeShop roots = new CoffeeShop("Roots", new LatLng(46.5436924, 24.5359489));

    ///// Creating the map activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(checkMapServices()){
            if(mLocationPermissionGranted){
                getDeviceLocation();
            } else{
                buildAlertMessageNoGps();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        customizeMap(googleMap);

        if (checkMapServices()) {
            getDeviceLocation();
            startLocationService();

            Marker FiveToGo = mMap.addMarker(new MarkerOptions().position(fiveToGo.getPosition())
                   .icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap("coffee",135,127)))
                    .title(fiveToGo.getName()));
            Marker CaptainBean = mMap.addMarker(new MarkerOptions().position(captainBean.getPosition())
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap("coffee",135,127)))
                    .title(captainBean.getName()));
            Marker Roots = mMap.addMarker(new MarkerOptions().position(roots.getPosition())
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap("coffee",135,127)))
                    .title(roots.getName()));
            mMap.setMyLocationEnabled(true);
        }
    }

    ////// Requesting Permission

    public boolean isServicesOK() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable( MapsActivity.this );
        if (available == ConnectionResult.SUCCESS) {
            Log.d( TAG, "isServicesOK: Google Play Services is working" );
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError( available )) {
            Log.d( TAG, "isServicesOK: an error occurred but we can fix it" );
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog
                    ( MapsActivity.this, available,ERROR_DIALOG_REQUEST );
            dialog.show();
        } else {
            Toast.makeText( this, "You can't make map requests", Toast.LENGTH_SHORT ).show();
        }
        return false;
    }

    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        }
        Log.d(TAG, "isMapsEnabled: Maps is enabled");
        getLocationPermission();
        return true;
    }

    private  void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This app requires GPS, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            getDeviceLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case  PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (mLocationPermissionGranted) {
                    getDeviceLocation();
                } else {
                    getLocationPermission();
                }
            }
        }
    }

    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }

    ////////Getting initial location

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting device current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionGranted = true) {
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location " + locationUpdateNumber);
                            ++locationUpdateNumber;
                            Location currentLocation = (Location)task.getResult();
                            if(currentLocation != null) {
                                Log.d(TAG, currentLocation.toString());
                                myLocation = (new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));    // location
                                MyLocation = mMap.addMarker(new MarkerOptions().position(myLocation)   // Marker
                                        .icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap("placeholder", 60, 65)))
                                        .title("Your location!"));
                                moveCamera(myLocation);
                            }
                            startUserLocationsRunnable();
                        } else {
                            Log.d(TAG, "onComplete current location is null");
                            Toast.makeText(MapsActivity.this, "Didn't work mate",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.d(TAG, "getDeviceLocation : Security Exception: " + e.getMessage());
        }
    }

    public void moveCamera (LatLng location) {
        String pozitie = location.toString();
        Log.d(TAG, pozitie);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,13f));
    }

    //////////Initiating location service

    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(this, LocationTracker.class);

                Log.d(TAG, " starting service");
                this.startService(serviceIntent);
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.codingwithmitch.googledirectionstest.services.LocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }

    ///////// Updating Location

    private void startUserLocationsRunnable(){
        Log.d(TAG, "startUserLocationsRunnable: starting runnable for retrieving updated locations.");
        mHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Runnable running");
                updateMyLocation(); // Marker
                mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    public void updateMyLocation() {
          Log.d(TAG, "Managed to obtain updated location :" + locationTracker.getUpdatedLocation());
          myLocation =  locationTracker.getUpdatedLocation();
          Log.d(TAG, "Setting location to" + myLocation);
          MyLocation.setPosition(myLocation);

    }

    /////// Adding a custom map

    private void customizeMap(GoogleMap googleMap){
        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.mapstyle));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
    }

    /////// Creating a BitMapDescriptor for the custom coffee icon

    public Bitmap resizeBitmap(String drawableName,int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(drawableName, "drawable", getPackageName()));
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }
}
