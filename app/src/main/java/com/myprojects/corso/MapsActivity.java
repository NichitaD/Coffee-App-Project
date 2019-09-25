package com.myprojects.corso;


import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.maps.model.TravelMode;
import com.myprojects.corso.services.LocationTracker;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import org.w3c.dom.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
        ,GoogleMap.OnInfoWindowClickListener, View.OnClickListener {

    int ERROR_DIALOG_REQUEST = 1;
    final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2;
    int PERMISSIONS_REQUEST_ENABLE_GPS = 3;
    private static final int LOCATION_UPDATE_INTERVAL = 3000;
    private int locationUpdateNumber = 1;
    private boolean mLocationPermissionGranted = false;
    private String info = " Click for more info";
    private static final String TAG = "MapsActivityLog : ";
    private String marker_name;
    private GeoPoint marker_geo_point;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    public LatLng myLocation;
    private LocationTracker locationTracker = new LocationTracker();
    private GeoApiContext mGeoApiContext = null;
    private Polyline oldPolyline;
    private Marker selectedMarker;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private Float testDistance;
    private Float closestDistance = new Float(123456789);
    private String nearestName;
    private LatLng nearestLocation;
    private Location testLocation = new Location("");
    private final Location mine = new Location("");
    private int option;
    private Marker current_clicked_marker;
    private Dialog dialog;
    private EditText review_text;
    private RatingBar rating_bar;


    ///// Creating the map activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Intent intent = getIntent();
        option = intent.getIntExtra("option", 123);
        Log.d("Intent_info", "Recieved option : " + option);
        if (option == 2) {
            setAllMarkers();
        }
        final ImageButton button =  findViewById(R.id.reset);
        button.setEnabled(false);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                resetMap(oldPolyline, selectedMarker);
                button.setEnabled(false);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkMapServices()) {
            if (mLocationPermissionGranted) {
                getDeviceLocation();
            } else {
                buildAlertMessageNoGps();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(true);
        customizeMap(googleMap);
        mMap.setOnInfoWindowClickListener(this);

        if(mGeoApiContext == null){
            mGeoApiContext = new GeoApiContext.Builder().
                    apiKey("AIzaSyAh_mnkmplWNTxhFwUAZuj-WqlZ-oMIn0s")
                    .build();
        }
        if (checkMapServices()) {
            getDeviceLocation();
            startLocationService();
            mMap.setMyLocationEnabled(true);
        }
    }

    ////// Requesting Permission

    public boolean isServicesOK() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapsActivity.this);
        if (available == ConnectionResult.SUCCESS) {
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Log.d(TAG, "isServicesOK: an error occurred but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog
                    (MapsActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
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

    private void buildAlertMessageNoGps() {
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
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (mLocationPermissionGranted) {
                    getDeviceLocation();
                } else {
                    getLocationPermission();
                }
            }
        }
    }
    private boolean checkMapServices() {
        if (isServicesOK()) {
            if (isMapsEnabled()) {
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
                            Location currentLocation = (Location) task.getResult();
                            if (currentLocation != null) {
                                Log.d(TAG, currentLocation.toString());
                                myLocation = (new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));    // location
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,14f));
                                if (option == 1){
                                    setNearestMarker();
                                }
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

    //////////Initiating location service

    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            Intent serviceIntent = new Intent(this, LocationTracker.class);

            Log.d(TAG, " starting service");
            this.startService(serviceIntent);
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.codingwithmitch.googledirectionstest.services.LocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }

    ///////// Updating Location

    private void startUserLocationsRunnable() {
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
        myLocation = locationTracker.getUpdatedLocation();
        Log.d(TAG, "Setting location to" + myLocation);
    }

    /////// Adding a custom map

    private void customizeMap(GoogleMap googleMap) {
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

    public Bitmap resizeBitmap(String drawableName, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(drawableName, "drawable", getPackageName()));
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }

    ///// Making the route dialog popup

    @Override
    public void onInfoWindowClick(Marker marker) {
        LayoutInflater inflater = LayoutInflater.from(this);
        final Dialog dialog1 = new Dialog(this);
        Window window = dialog1.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        wlp.y = 50;
        window.setAttributes(wlp);
        View view1 = inflater.inflate(R.layout.bottom_window, null);
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog1.setContentView(view1);
        TextView title = view1.findViewById(R.id.coffee_name);
        title.setText(marker.getTitle());
        ImageView rating_bar = view1.findViewById(R.id.rating_bar);
        rating_bar.setImageResource(R.drawable.five_starts);
        dialog1.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog1.show();
        current_clicked_marker = marker;
        dialog = dialog1;
    }

    @Override
    public void onClick(View view) {
        int button = view.getId();
        if (button == R.id.directions_button){
            LayoutInflater inflater = LayoutInflater.from(this);
            final Dialog dialog2 = new Dialog(this);
            Window window = dialog2.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            wlp.y = 50;
            window.setAttributes(wlp);
            View view2 = inflater.inflate(R.layout.travel_dialog, null);
            dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.dismiss();
            dialog = dialog2;
            dialog2.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            dialog2.setContentView(view2);
            dialog2.show();
        }
        if(button == R.id.rate_button) {
            if(firebaseAuth.getCurrentUser() == null){
                Toast.makeText(MapsActivity.this, "You need to be logged in to leave a review!",
                        Toast.LENGTH_SHORT).show();
            } else {
                LayoutInflater inflater = LayoutInflater.from(this);
                final Dialog dialog3 = new Dialog(this);
                Window window = dialog3.getWindow();
                WindowManager.LayoutParams wlp = window.getAttributes();
                wlp.gravity = Gravity.CENTER;
                wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                wlp.y = 50;
                window.setAttributes(wlp);
                View view3 = inflater.inflate(R.layout.rating_dialog, null);
                dialog3.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog3.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.dismiss();
                dialog = dialog3;
                dialog3.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                dialog3.setContentView(view3);
                dialog3.show();
                review_text = dialog3.findViewById(R.id.review);
                rating_bar = dialog3.findViewById(R.id.ratingBar);
                rating_bar.setStepSize(1);
            }
        }
        if(button == R.id.review_done){
            Float rating = rating_bar.getRating();
            String review = review_text.getText().toString();
            String user = firebaseAuth.getCurrentUser().getEmail();
            dialog.dismiss();
            addReview(rating.intValue(), review, user);
        }
        if(button == R.id.see_reviews){
            dialog.dismiss();
            Intent marker_intent = new Intent(MapsActivity.this, ReviewsActivity.class);
            marker_intent.putExtra("marker",current_clicked_marker.getTitle());
            MapsActivity.this.startActivity(marker_intent);
        }
        if (button == R.id.walk){
            calculateDirections(current_clicked_marker, 2);
            dialog.dismiss();
        }
        if (button == R.id.car){
            calculateDirections(current_clicked_marker, 1);
            dialog.dismiss();
        }
        if (button == R.id.bike){
            Toast.makeText(MapsActivity.this, "This mode isn't available yet!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /////// Calculating Directions

    private void calculateDirections(Marker marker, Integer option){
        if (dialog != null ) dialog.dismiss();
        Log.d(TAG, "calculateDirections: calculating directions.");
        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        directions.alternatives(false);
        if (option == 1){
            directions.mode(TravelMode.DRIVING);
        } else if (option == 2){
            directions.mode(TravelMode.WALKING);
        } else if (option == 3){
            directions.mode(TravelMode.BICYCLING);
        }
        directions.origin(
                new com.google.maps.model.LatLng(
                       myLocation.latitude,
                       myLocation.longitude
                )
        );
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                addPolylinesToMap(result, marker);
                findViewById(R.id.reset).setEnabled(true);
            }
            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage() );
            }
        });
    }

    ////// Adding polylines

    private void addPolylinesToMap(final DirectionsResult result, Marker marker){
        selectedMarker = marker;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);
                if(oldPolyline != null){
                    oldPolyline.remove();
                }
                for(DirectionsRoute route: result.routes){
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    addInfoWindow(route.legs[0], marker );
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for(com.google.maps.model.LatLng latLng: decodedPath){

//                        Log.d(TAG, "run: latlng: " + latLng.toString());

                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    oldPolyline = polyline;
                    polyline.setColor(R.color.red);
                    polyline.setWidth(16.5f);
                    zoomRoute(polyline.getPoints());
                }
            }
        });
    }

    ////// Adding a InfoWindow for trip

    private void addInfoWindow (DirectionsLeg legs, Marker marker){
       Log.d("INFO" , "Changing snippet");
        marker.hideInfoWindow();
        marker.setSnippet(legs.distance + " away");
        marker.showInfoWindow();

    }

    //////Setting the view on the route

    public void zoomRoute(List<LatLng> lstLatLngRoute) {

        if (mMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 120;
        LatLngBounds latLngBounds = boundsBuilder.build();

        mMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
                600,
                null
        );
    }

    ////// Reseting map

    private void resetMap (Polyline polilyne, Marker marker){

        polilyne.remove();
        marker.hideInfoWindow();
        marker.setSnippet("Click to determine route");
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,14f));

    }

    ///// Adding the markers from the database

    private void setAllMarkers() {
        CollectionReference ref = db.collection("coffee_shops");
        ref.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        marker_name=(String) document.get("name");
                        marker_geo_point = (GeoPoint) document.get("location");
                        Log.d("Marker_info", "Setting marker " + marker_name + " to " + marker_geo_point);
                        mMap.addMarker(new MarkerOptions().position(new LatLng(marker_geo_point.getLatitude(),marker_geo_point.getLongitude()))
                                .icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap("coffee", 135, 127)))
                                .title(marker_name)
                                .snippet(info));
                    }
                } else {
                    Log.d("Database_info", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private void setNearestMarker() {
        mine.setLongitude(myLocation.longitude);
        mine.setLatitude(myLocation.latitude);
        CollectionReference ref = db.collection("coffee_shops");
        ref.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        marker_geo_point = (GeoPoint) document.get("location");
                        testLocation.setLatitude(marker_geo_point.getLatitude());
                        testLocation.setLongitude(marker_geo_point.getLongitude());
                        testDistance = mine.distanceTo(testLocation);
                        Log.d("Comparing","From " + mine + "to :" + testLocation + "=" + testDistance);
                        if(testDistance < closestDistance){
                            closestDistance = testDistance;
                            nearestName = (String) document.get("name");
                            nearestLocation = new LatLng(testLocation.getLatitude(), testLocation.getLongitude());
                        }
                    }
                } else {
                    Log.d("Database_info", "Error getting documents: ", task.getException());
                }
                calculateDirections (mMap.addMarker(new MarkerOptions().position(nearestLocation)
                        .icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap("coffee", 135, 127)))
                        .title(nearestName)
                        .snippet(info)), 2);
            }
        });
    }
/*
    private void addReview (Float rating, String review, String user) {
        String coffee_shop_id = current_clicked_marker.getTitle().toLowerCase().replaceAll("[^A-Za-z0-9]", "_");
        DocumentReference doc_ref = db.collection("coffee_shops").document(coffee_shop_id);
        doc_ref.update("ratings_sum", FieldValue.increment(rating));
        doc_ref.update("number_of_ratings", FieldValue.increment(1));
        doc_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                       Double updated_sum = (Double)document.get("ratings_sum");
                       Long updated_num = (Long)document.get("number_of_ratings");
                       Double new_rating =  updated_sum / updated_num.doubleValue();
                       doc_ref.update("rating", new_rating);
                       doc_ref.update("reviewers",FieldValue.arrayUnion(user));
                       doc_ref.update("reviewers_rating",FieldValue.arrayUnion(rating));
                       doc_ref.update("reviews",FieldValue.arrayUnion(review));
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

 */


    private void addReview(Integer rating, String review, String user){
        String coffee_shop_id = current_clicked_marker.getTitle().toLowerCase().replaceAll("[^A-Za-z0-9]", "_");
        DocumentReference doc_ref = db.collection("coffee_shops").document(coffee_shop_id);
        doc_ref.update("ratings_sum", FieldValue.increment(rating));
        doc_ref.update("number_of_ratings", FieldValue.increment(1));
        db.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(doc_ref);
                ArrayList<String> reviewers = (ArrayList<String>) snapshot.get("reviewers");
                ArrayList<String> reviews = (ArrayList<String>) snapshot.get("reviews");
                ArrayList<Integer> reviewers_rating = (ArrayList<Integer>) snapshot.get("reviewers_rating");
                reviewers.add(user);
                reviews.add(review);
                reviewers_rating.add(rating);
                transaction.update(doc_ref,"reviewers",reviewers);
                transaction.update(doc_ref,"reviewers_rating", reviewers_rating);
                transaction.update(doc_ref,"reviews", reviews);
                // Success
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Transaction success!");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Transaction failure.", e);
                    }
                });
    }


}

