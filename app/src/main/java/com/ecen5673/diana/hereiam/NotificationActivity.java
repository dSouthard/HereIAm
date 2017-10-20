package com.ecen5673.diana.hereiam;

import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by diana on 11/17/16.
 *
 * Activity launched when an alert is received
 */
public class NotificationActivity extends AppCompatActivity
        implements OnMapReadyCallback, View.OnClickListener, GoogleApiClient.ConnectionCallbacks,
        LocationListener, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "NotificationActivity";

    String friendID;

    // Map Variables
    protected static GoogleMap map;
    private RouteMaker routeMaker;
    LatLng friendLocation;
    GoogleApiClient client;
    User friend;
    protected static User user;

    // Buttons
    Button sendAckButton, dismissButton, directionsButton, turnByTurnButton, helpButton;

    // TextViews
    TextView recordUserName;
    TextView recordLocation;
    TextView recordAddress;
    TextView recordUpdateTime;
    TextView recordActivity;
    TextView recordAlertMessage;
    ImageView recordProfile;

    // Maintain updates to friend's locations
    StorageReference storageReference;
    protected static DatabaseReference userDatabase;
    DatabaseReference friendDatabase;
    boolean tracking = false, locationUpdates = false, listenerAttached = false;
    protected static boolean started;

    // Maintain updates to user's information
    // Setup for the activity detector
    protected DetectedActivityBroadcastReceiver detectedActivityReceiver;

    // Activity detection listeners
    private FetchAddressResultReceiver addressResultReceiver;
    protected static String currentActivity;
    Location currentLocation;
    String updateLocation, updateTime;
    protected static String currentAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        started = true;

        // If a notification message is tapped, any data accompanying the notification
        // message is available in the intent extras. In this sample the launcher
        // intent is fired when the notification is tapped, so any accompanying data would
        // be handled here. If you want a different intent fired, set the click_action
        // field of the notification message to the desired intent. The launcher intent
        // is used when no click_action is specified.
        //
        // Handle possible data accompanying notification message.
        friendID = getIntent().getStringExtra(Constants.FRIEND_EXTRA);
        Log.d(TAG, "Alert from friend: " + friendID);

        // Setup friend's information
        recordUserName = (TextView) findViewById(R.id.recordNameField);
        recordLocation = (TextView) findViewById(R.id.recordLocationField);
        recordAddress = (TextView) findViewById(R.id.recordAddress);
        recordUpdateTime = (TextView) findViewById(R.id.recordUpdateTimeField);
        recordActivity = (TextView) findViewById(R.id.recordActivity);
        recordProfile = (ImageView) findViewById(R.id.recordUserProfile);
        recordAlertMessage = (TextView) findViewById(R.id.alertMessageField);

        // Set up Google client
        client = new GoogleApiClient.Builder(this)
                .addApi(AppInvite.API)              // For sending/receiving invites to/from friendsHashMap
                .addApi(LocationServices.API)       // For tracking this user's location
                .addApi(ActivityRecognition.API)    // Recognizing this user's activity
                .addOnConnectionFailedListener(this) // What to do when a connection fails
                .enableAutoManage(this, this)       // Client enables automatic lifecycle mgmt
                .addConnectionCallbacks(this)
                .build();           // Build this client

        // Set up receiver for broadcasts from activity detection service
        detectedActivityReceiver = new DetectedActivityBroadcastReceiver();

        // Set up receiver for broadcasts from addresses fetching service
        addressResultReceiver = new FetchAddressResultReceiver(new Handler());

        // Get user and friend info:
        firebaseSetup();

        // Map setup
        Log.d(TAG, "Fetching Map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.notificationMap);
        mapFragment.getMapAsync(this);

        // Button setup
        sendAckButton = (Button) findViewById(R.id.sendAckButton);
        dismissButton = (Button) findViewById(R.id.dismissButton);
        directionsButton = (Button) findViewById(R.id.directionsButton);
        turnByTurnButton = (Button) findViewById(R.id.turnByTurnButton);
        helpButton = (Button) findViewById(R.id.helpButton);

        sendAckButton.setOnClickListener(this);
        dismissButton.setOnClickListener(this);
        directionsButton.setOnClickListener(this);
        turnByTurnButton.setOnClickListener(this);
        helpButton.setOnClickListener(this);

        routeMaker = new RouteMaker();

    }

    private void dismissAlert() {
        // Delete the message in your alert queue
        Log.d(TAG, "Delete the message in your alert queue");
        DatabaseReference alertsReference = FirebaseDatabase.getInstance().getReference().child("alerts");
        DatabaseReference friendAlert = alertsReference.child(user.getUserID()).child(friend.getUserID());
        friendAlert.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "Returning to main activity");
                startActivity(new Intent(NotificationActivity.this, MainActivity.class));
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        map = googleMap;

        // Set view type (Satellite, hybrid, geographic
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        // Enable my location
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsRevoked();
        }

        map.setMyLocationEnabled(true);

        if (friendLocation != null){
            // Zoom map to current location
            map.moveCamera(CameraUpdateFactory.newLatLng(friendLocation));
            map.animateCamera(CameraUpdateFactory.zoomTo(Constants.DEFAULT_MAP_ZOOM));
        }
        // Add friendsHashMap' markers
        addFriendsMarkers();

        // Set up custom info window
        map.setInfoWindowAdapter(new UserInfoWindowAdapter(this));
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // show info window with most up-to-date information
                marker.showInfoWindow();
                return true;
            }
        });
    }


    @Override
    public void onPause() {
        super.onPause();
//        mapView.onPause();

        //stop location updates when Activity is no longer active
        if (client != null && client.isConnected()) {
            Log.d(TAG, "Client connected? " + client.isConnected());
            stopLocationUpdates();
        }

        // Take off friend value listener
        if (friendDatabase != null && listenerAttached){
            friendDatabase.removeEventListener(FriendListener);
            listenerAttached = false;
        }
    }

    @Override
    public void onResume(){
        super.onResume();
//        if (mapView != null)
//            mapView.onResume();

        if (client != null && client.isConnected() && !locationUpdates){
            startLocationUpdates();
        }

        if (friendDatabase != null) {
            friendDatabase.addValueEventListener(FriendListener);
            listenerAttached = true;
        }

    }

    protected void addFriendsMarkers() {
        Log.d(TAG, "addFriendsMarkers");
        if (map == null || friend == null)
            // Map or friend isn't ready yet
            return;

        // Clear previous markers as they may be inaccurate
        map.clear();

        // Set up markers for friendsHashMap and show locations
        Marker marker = map.addMarker(new MarkerOptions()
                .position(friendLocation)
                .title(friend.getUserName()));
        marker.setTag(friend);


        // If tracking, clear the route and update it
        if (tracking) {
            clearRoute();
            drawRoute();
        }
    }

    // Location permissions should have been granted during welcome tutorial, but this may have been revoked by user
    public void permissionsRevoked() {
        Toast.makeText(this, "Location permissions were revoked, cannot use this app", Toast.LENGTH_SHORT).show();
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, Constants.PERMISSION_LOCATION_ACCESS);
    }

    // Recreate a LatLng from a formatted string
    private static LatLng latLngFromString(String string) {
        if (string == null){
            Log.d(TAG, "Latlngfromstring returning null string");
            return null;
        }
        String parts[] = string.split(",");
        return new LatLng(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
    }

    void drawRoute() {
        routeMaker.drawRoute(map, this, new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), friendLocation, false, "en");
    }

    void clearRoute() {
        routeMaker.clearRoute();
    }

    @Override
    public void onClick(View view) {
        int buttonID = view.getId();
        switch (buttonID) {
            case R.id.sendAckButton:
                Log.d(TAG, "sendAckButton pressed");
                sendAcknowledgement();
                break;
            case R.id.dismissButton:
                Log.d(TAG, "dismissButton pressed");
                dismissAlert();
                break;
            case R.id.directionsButton:
                Log.d(TAG, "directionsButton pressed");
                if (tracking) {
                    Log.d(TAG, "Turning OFF tracking");
                    tracking = false;
                    directionsButton.setText(R.string.directions_on);

                    // Display route on our map
                    clearRoute();
                } else {
                    Log.d(TAG, "Turning ON tracking");
                    tracking = true;
                    directionsButton.setText(R.string.directions_off);

                    // Remove route from our map
                    drawRoute();
                }
                break;
            case R.id.helpButton:
                Log.d(TAG, "helpButton pressed");
                launchHelpDialog();
                break;
            case R.id.turnByTurnButton:
                Log.d(TAG, "turnByTurnButton pressed");
                launchTurnByTurnIntent();
                break;

        }
    }

    private void sendAcknowledgement() {
        DatabaseReference alertsReference = FirebaseDatabase.getInstance().getReference().child("alerts");

        Log.d(TAG, "Delete the message in your alert queue");
        // Delete the message in your alert queue
        DatabaseReference friendAlert = alertsReference.child(user.getUserID()).child(friend.getUserID());
        friendAlert.removeValue();

        Log.d(TAG, "Add acknowledgement message to friend's queue");
        // Add acknowledgement message to friend's queue
        friendAlert = alertsReference.child(friend.getUserID()).child(user.getUserID());
        friendAlert.setValue("OK, " + user.getUserName());
    }

    private void launchHelpDialog() {
        // Launch alert dialog with help message
        String message = "Acknowledging the alert will send a message to your friend that you've seen their alert. \n" +
                "Clicking the direction button will generate directions from your current location to your friend's location. \n" +
                "Clicking the turn-by-turn button will open Google's turn-by-turn navigational system.\n" +
                "Dismissing the alert will ignore the alert and return you to the normal app.";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Instructions")
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builder.show();
    }

    // Launch intent to show directions from user's current location to friend's location
    private void launchTurnByTurnIntent() {
        String uriString = String.format(Locale.ENGLISH, "http://maps.google.com/maps?daddr=%f,%f (%s)", friendLocation.latitude, friendLocation.longitude, friend.getUserName());

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
        // Use google maps
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            try {
                Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
                startActivity(unrestrictedIntent);
            } catch (ActivityNotFoundException innerEx) {
                Toast.makeText(this, "Please install a maps application", Toast.LENGTH_LONG).show();
            }
        }
    }


    protected void setText() {
        if (friend == null) return;

        // Populate the data into the template using the data object
        recordUserName.setText(friend.getUserName());
        recordLocation.setText(friend.getLocation());
        recordAddress.setText(friend.getAddress());
        recordUpdateTime.setText(friend.getUpdateTime());
        recordActivity.setText(friend.getActivity());
        recordAlertMessage.setText(friend.getAlertMessage());
        Glide.with(this).using(new FirebaseImageLoader())
                .load(storageReference)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(recordProfile);
    }

    ValueEventListener FriendListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            // Load new data
            Log.d(TAG, "FriendListener: DataSnapshot = " + dataSnapshot.toString());
            friend = dataSnapshot.getValue(User.class);
            // Update UI
            setText();
            Log.d(TAG, "Friend location changed: " + friend.getLocation());
            friendLocation = latLngFromString(friend.getLocation());
            addFriendsMarkers();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, "Error reading friend's updated location");
        }
    };

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Client connected");
        startLocationUpdates();
    }

    void startLocationUpdates(){
        // Setup location requests
        // Set up location requests
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setMaxWaitTime(Constants.MIN_UPDATE_TIME );   // Update at least every UPDATE_MINUTES minutes
        request.setSmallestDisplacement(Constants.MIN_UPDATE_DISTANCE);   // Update every UPDATE_METERS meters

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsRevoked();
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(client, request, this);

        // Setup activity detection requests
        // Create a PendingIntent to check for the user's activity with the same update time as the location updates
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(client,Constants.MIN_UPDATE_TIME, getActivityDetectionPendingIntent());

        locationUpdates = true;
    }

    protected void stopLocationUpdates() {
        Log.d(TAG, "Location update stopped .......................");

        if (!locationUpdates)
            // Not connected yet
            return;

        LocationServices.FusedLocationApi.removeLocationUpdates(client, this);

        // Stop activity recognition updates
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(client, getActivityDetectionPendingIntent());

        locationUpdates = false;
    }

    private PendingIntent getActivityDetectionPendingIntent(){
        Intent intent = new Intent(this, DetectedActivitiesIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we can get the same pending intent back
        // when called requestActivityUpdates() and removeActivityUpdates()
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    protected void startFetchAddressIntentService() {
        Log.d(TAG, "startFetchAddressIntentService" );
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(this, FetchAddressIntentService.class);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(Constants.RECEIVER, addressResultReceiver);

        // Pass the location data as an extra to the service.
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, currentLocation);

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        startService(intent);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location)
    {
        currentLocation = location;
        // Launch intent to display new location's address
        startFetchAddressIntentService();

        //move map camera
        if (map != null) {
            map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
            map.animateCamera(CameraUpdateFactory.zoomTo(Constants.DEFAULT_MAP_ZOOM));
        }

        // Update user on database, in case someone is tracking me
        updateLocation = String.format(Locale.US,"%.6f", location.getLatitude()) + ", " + String.format(Locale.US, "%.6f", location.getLongitude());
        updateTime = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date());
        if (user != null){
            user.setLocation(updateLocation);
            user.setUpdateTime(updateTime);
            updateUserLocationDatabase();
        }

        if (tracking){
            // if tracking: update route
            clearRoute();
            drawRoute();
        }
    }

    protected static void updateUserLocationDatabase(){
        userDatabase.child("location").setValue(user.getLocation());
        userDatabase.child("updateTime").setValue(user.getUpdateTime());
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void firebaseSetup() {

        // Get instances of the database
        DatabaseReference usersDatabase = FirebaseDatabase.getInstance().getReference().child("user");

        // Read database to load user and to track friends' changes
        usersDatabase.addListenerForSingleValueEvent(LoadUserListener);
    }

    ValueEventListener LoadUserListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            // Get current user
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (dataSnapshot.hasChild(firebaseUser.getUid())) {
                // Load the user
                Log.d(TAG, "Loading the user.");
                user = dataSnapshot.child(firebaseUser.getUid()).getValue(User.class);

                // Setup user location reference
                userDatabase = FirebaseDatabase.getInstance().getReference().child("user").child(user.getUserID());

                // Check if there was a location update while the user was loading
                if (currentLocation != null){
                    user.setLocation(updateLocation);
                    user.setUpdateTime(updateTime);
                    updateUserLocationDatabase();
                }

                currentLocation = new Location("");
                LatLng latLng = latLngFromString(user.getLocation());
                currentLocation.setLatitude(latLng.latitude);
                currentLocation.setLongitude(latLng.longitude);

                Log.d(TAG, "Loaded user: " + user.toString());
            } else {
                // Add this user as a new user
                Log.d(TAG, "User not found, go back to main activity");
                dismissAlert();
            }

            // Load the desired friend
            if (dataSnapshot.hasChild(friendID)){
                Log.d(TAG, "Loading the friend");
                friend = dataSnapshot.child(friendID).getValue(User.class);

                Log.d(TAG, "Loaded friend: " + friend.toString());

                friendDatabase = FirebaseDatabase.getInstance().getReference().child("user").child(friendID);
                friendDatabase.addValueEventListener(FriendListener);
                listenerAttached = true;

                storageReference = FirebaseStorage.getInstance().getReference().child(friend.getUserID()).child(Constants.PROFILE_STRING);

                // Update UI
                setText();
                friendLocation = latLngFromString(friend.getLocation());
                if (map != null) {
                    // Zoom map to current location
                    map.moveCamera(CameraUpdateFactory.newLatLng(friendLocation));
                    map.animateCamera(CameraUpdateFactory.zoomTo(Constants.DEFAULT_MAP_ZOOM));
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, "Error loading users");
        }
    };

    protected static void updateUserActivityDatabase(){
        userDatabase.child("activity").setValue(user.getActivity());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.PERMISSION_LOCATION_ACCESS)
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Continue doing what you where doing
            } else {
                Toast.makeText(this, "This app relies on using your location data, and cannot function without it. Please reconsider not allowing location access", Toast.LENGTH_LONG).show();
                System.exit(0);
            }
    }
}
