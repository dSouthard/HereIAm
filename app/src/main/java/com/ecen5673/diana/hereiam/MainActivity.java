package com.ecen5673.diana.hereiam;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import static com.google.android.gms.location.LocationServices.FusedLocationApi;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, LocationListener {

    private static String TAG = "MainActivity";
    private int LISTVIEW = 0;

    // The pager widget, which handles animation and allows swiping horizontally to
    // access previous and next wizard steps
    private ViewPager pager;

    // Instance of Firebase database to read/write data
    private final static String USER_REFERENCE = "user";
    private final static String FRIEND_LIST_REFERENCE = "friends";

    // Firebase instance variables
    protected FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    // Create a reference to the user database
    protected static DatabaseReference usersDatabase;
    protected static DatabaseReference currentUserDatabaseReference;
    protected DatabaseReference friendsListDatabase;
    boolean listening = false;
    protected HashMap<String, DatabaseReference> friendsDatabase = new HashMap<>();
    protected DatabaseReference alertsDatabase;

    // Local variables for the entire app
    protected static User user;
    protected static Bitmap userBitmap;

    protected static HashMap<String, User> friendsHashMap = new HashMap<>(); // Keep track of user details of friendsHashMap

    // Layout items for the ListView screen
    protected static UserDetailsListViewAdapter friendsAdapter;
    protected static ArrayList<User> friendsArrayList = new ArrayList<>();

    // Google Client
    protected static GoogleApiClient client;

    // Location listeners variables
    Location currentLocation;
    String updateTime;
    protected static String currentAddress;
    protected boolean requestingUpdates = false;

    // Setup for the activity detector
    protected DetectedActivityBroadcastReceiver detectedActivityReceiver;

    // Activity detection listeners
    private FetchAddressResultReceiver addressResultReceiver;
    protected static String currentActivity;

    // Setup for retrieving profile images
    protected static StorageReference storageRef;
    static StorageReference userProfileRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set up adapters
        friendsAdapter = new UserDetailsListViewAdapter(this, friendsArrayList);

        // Set up Google client
        client = new GoogleApiClient.Builder(this)
                .addApi(AppInvite.API)              // For sending/receiving invites to/from friendsHashMap
                .addApi(LocationServices.API)       // For tracking this user's location
                .addApi(ActivityRecognition.API)    // Recognizing this user's activity
                .addOnConnectionFailedListener(this) // What to do when a connection fails
                .enableAutoManage(this, this)       // Client enables automatic lifecycle mgmt
                .addConnectionCallbacks(this)
                .build();           // Build this client

        // Setup Firebase
        firebaseSetup();

        // Set up receiver for broadcasts from activity detection service
        detectedActivityReceiver = new DetectedActivityBroadcastReceiver();

        // Set up receiver for broadcasts from addresses fetching service
        addressResultReceiver = new FetchAddressResultReceiver(new Handler());

        // Finish setting up views
        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(pager);

        // Instantiate a ViewPager and a PagerAdapter
        pager = (ViewPager) findViewById(R.id.mainViewPager);

        // Creates the fragments here
        PagerAdapter pagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);

        // Set up page transition animation
        pager.setPageTransformer(true, new ZoomOutPageTransformer());

        // Set up offscreen page limit
        pager.setOffscreenPageLimit(3);

        // Set pager to first page, attaching the fragment
        pager.setCurrentItem(LISTVIEW);

        // Fix for tabs not showing
        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(pager);
            }
        });

        // Setup floating ALERT action button
        FloatingActionButton alertButton = (FloatingActionButton) findViewById(R.id.alert_fab);
        alertButton.setOnClickListener(new OnClickAlertListener(this, true));
    }

    @Override
    public void onBackPressed() {
        if (pager.getCurrentItem() == LISTVIEW) {
            // If the user is currently looking at the first step, ask if they want to exit the app
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Closing App")
                    .setMessage("Are you sure you want to exit the app?")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create();
            alertDialog.show();
        } else {
            // Otherwise, select the previous step
            pager.setCurrentItem(pager.getCurrentItem() - 1);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    /*
    Check for App Invite invitations and launch deep-link activity if possible. Requires that an activity
    is registered in the Android Manifest to handle deep-link URLs
     */
    private void checkPendingInvites() {
        AppInvite.AppInviteApi.getInvitation(client, this, true);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed: " + connectionResult);
        Toast.makeText(this, "Failure connecting to Google Play Services", Toast.LENGTH_SHORT).show();
    }

    // Attach authentication listener
    @Override
    public void onStart() {
        Log.d(TAG, "OnStart");
        super.onStart();

        // Start up the client
        client.connect();

        // Add Authentication Listener
        firebaseAuth.addAuthStateListener(authenticationStatusListener);

        // Check for pending invites
        checkPendingInvites();
    }

    // Detach authentication listener
    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();

        // Remove Authentication Listener
        if (authenticationStatusListener != null) {
            firebaseAuth.removeAuthStateListener(authenticationStatusListener);
        }

        // Remove listeners to friends' values
        for (DatabaseReference friend : friendsDatabase.values()) {
            friend.removeEventListener(FriendDatabaseListener);
        }

        // Remove listener to user's friends list
        if (friendsListDatabase != null)
            friendsListDatabase.removeEventListener(FriendsListListener);

        listening = false;

        // Disconnect Google client
        if (client.isConnected())
            client.disconnect();

//        if (alertsDatabase != null)
//            alertsDatabase.removeEventListener(AlertsListener);
    }


    private void firebaseSetup() {
        // Get shared instance of the FirebaseAuth object, to track authentication changes
        firebaseAuth = FirebaseAuth.getInstance();

        // Get current user
        firebaseUser = firebaseAuth.getCurrentUser();

        // load database/storage references
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();

        // Get instances of the database
        usersDatabase = database.child(USER_REFERENCE);
        Log.d(TAG, "userDatabase = " + usersDatabase.toString());

        // Read database to load user and to track friends' changes
        usersDatabase.addListenerForSingleValueEvent(LoadUserListener);
    }

    private void firstTimeSetup() {
        Log.d(TAG, "Attempting first time setup");
        // This user is the first one to use the app, so save him and get the correct data structure going on
        Log.d(TAG, "Creating user: name = " + firebaseUser.getDisplayName() + ", id = " + firebaseUser.getUid());
        user = new User(firebaseUser.getDisplayName(), firebaseUser.getUid());

        // Add the user to the database
        Log.d(TAG, "Writing new user to the database.");
        usersDatabase.child(user.getUserID()).setValue(user);

        // Setup default user image
        FirebaseStorageManager.setupDefaultProfileImage(storageRef);

        // Ask if user wants to update their profile picture
        MainFragmentMenuScreen.profileImagePicker(this);
    }

    protected static void updateUserUI(Context activity) {
        Log.d(TAG, "Updating User UI..........................");

        Log.d(TAG, "Updating List View");
        // Update list view UI
        MainFragmentListViewScreen.setText(activity);

        Log.d(TAG, "Updating Menu");
        // Update menu UI
        MainFragmentMenuScreen.setText(activity);

        Log.d(TAG, "Updating Alert View");
        // Update alert_icon UI
        MainFragmentAlertScreen.setText();
    }

    protected static void updateFriendUI(){
        Log.d(TAG, "Updating Friends UI..........................");
        // Update friends' list on list view UI
        friendsAdapter.notifyDataSetChanged();

        // Update friends' markers on map UI
        if (MainFragmentMapViewScreen.map != null) {
            MainFragmentMapViewScreen.addFriendsMarkers();
        }
    }

    protected static void updateUserLocationDatabase(){
        // User information has been updated, write back to the database
        currentUserDatabaseReference.child("location").setValue(user.getLocation());
        currentUserDatabaseReference.child("updateTime").setValue(user.getUpdateTime());
        currentUserDatabaseReference.child("address").setValue(user.getAddress());
    }

    protected static void updateUserActivityDatabase(){
        currentUserDatabaseReference.child("activity").setValue(user.getActivity());
    }

    protected static void updateUserUI(){
        Log.d(TAG, "Updating List View");
        // Update list view UI
        MainFragmentListViewScreen.setText();

        Log.d(TAG, "Updating Menu");
        // Update menu UI
        MainFragmentMenuScreen.setText();
    }


    /********************
     * Firebase Value/Authentication Events Listeners
     ********************************/

    ValueEventListener LoadUserListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            // Get current user
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            assert firebaseUser != null;
            if (dataSnapshot.hasChild(firebaseUser.getUid())) {
                // Load the user
                Log.d(TAG, "Loading the user.");
                user = dataSnapshot.child(firebaseUser.getUid()).getValue(User.class);
            } else {
                // Add this user as a new user
                Log.d(TAG, "User not found, add into database");
                firstTimeSetup();
            }

            // Setup database reference for updates to this user
            currentUserDatabaseReference = usersDatabase.child(user.getUserID());

            // Check if there was a location update while the user was loading
            if (currentLocation != null){
                user.setLocation(stringFromLocation(currentLocation));
                user.setUpdateTime(updateTime);
                user.setAddress(currentAddress);
                updateUserLocationDatabase();
            }
            // Check if there was an activity update to this user
            if (currentActivity != null){
                user.setActivity(currentActivity);
                updateUserActivityDatabase();
            }

            // Load the user's profile image
            userProfileRef = storageRef.child(user.getUserID()).child(Constants.PROFILE_STRING);

            updateUserUI(MainActivity.this);

            Log.d(TAG, "LoadUserListener ------------------------- User loaded: " + user.toString());

            // Initialize friendsListDatabase reference, now that user is created
            friendsListDatabase = usersDatabase.child(user.getUserID()).child(FRIEND_LIST_REFERENCE);

            // Attach listener to user's friends list
            friendsListDatabase.addValueEventListener(FriendsListListener);

            // Load up friends' data
            for (String friend : user.getFriends().values()) {
                if (dataSnapshot.hasChild(friend)) {
                    Log.d(TAG, "User " + friend + " found and added.");
                    friendsHashMap.put(friend, dataSnapshot.child(friend).getValue(User.class));
                    friendsArrayList.add(dataSnapshot.child(friend).getValue(User.class));
                } else {
                    Log.d(TAG, "Friend not found: " + friend);
                }
            }

            // Initialize alerts database
            Log.d(TAG, "Added alerts listener");
            alertsDatabase = FirebaseDatabase.getInstance().getReference().child("alerts").child(user.getUserID());

            // Read any current alerts
            alertsDatabase.addListenerForSingleValueEvent(AlertsLoaderListener);

            alertsDatabase.addChildEventListener(AlertsListener);

            listening = true;
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Reading from users database failed, log a message
            Log.w(TAG, "Reading userDatabase failed: " + databaseError);
        }
    };

    ValueEventListener AlertsLoaderListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG, "Loading current alerts: " + dataSnapshot);
            for (DataSnapshot alerts : dataSnapshot.getChildren()){
                Log.d(TAG, "Alert from: " + alerts);
                String value = alerts.getValue(String.class);
                Log.d(TAG, "Alert id = " + value);
                if (value.contains("OK")){
                    // This is an acknowledgement of an alert you generated
                    String[] split = value.split(",");
                    Toast.makeText(MainActivity.this, "Your alert has been acknowledged by " + split[1], Toast.LENGTH_LONG).show();

                    // Delete this value to prevent future repeated notifications
                    Log.d(TAG, "Removing alert");
                    alerts.getRef().removeValue();
                } else{
                    Log.d(TAG,"Creating a new notification");
                    MyFirebaseMessagingService.createNewNotification(value, MainActivity.this);
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, "Error lading current alerts");
        }
    };

    // Listen for alerts from friends
    ChildEventListener AlertsListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot newChild, String s) {
            Log.d(TAG, "Got a new alert");

            String value = newChild.getValue(String.class);
            Log.d(TAG, "Alert ID = " + value);
            if (value.contains("OK")){
                // This is an acknowledgement of an alert you generated
                String[] split = value.split(",");
                Toast.makeText(MainActivity.this, "Your alert has been acknowledged by " + split[1], Toast.LENGTH_LONG).show();
                Log.d(TAG, "Removing alert");

                // Delete this value to prevent future repeated notifications
                newChild.getRef().removeValue();
            } else{
                Log.d(TAG,"Creating a new notification");
                MyFirebaseMessagingService.createNewNotification(value, MainActivity.this);
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.d(TAG, "Alerts Listener: Child Removed");
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, "Alerts Listener: Error reading database: " + databaseError);
        }
    };

    // Listen for changes to the friends list. If friends are added, new friends will have to be added to the friends hash map. Vice versa if friends are removed.
    ValueEventListener FriendsListListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d("FriendsListListener", "Updating friend list information: ");
            ArrayList<String> newFriendIDs = new ArrayList<>();
            for (DataSnapshot newFriendsList : dataSnapshot.getChildren()) {
                // Get new list of tracked friends
                newFriendIDs.add(newFriendsList.getValue(String.class));
            }

            // Find difference between old and new list
            // Look for new friends
            for (String newFriend : newFriendIDs) {
                if (!user.getFriends().containsKey(newFriend)) {
                    // Need to add friend to current copy of user (already written to the database)
                    Log.d("FriendsListListener", "Adding new friend: " + newFriend);
                    user.addFriend(newFriend);

                    // Add to tracked references, this listener will put a current copy of the friend into the friend hash map
                    DatabaseReference newFriendDatabase = usersDatabase.child(newFriend).getRef();
                    newFriendDatabase.addValueEventListener(FriendDatabaseListener);
                    friendsDatabase.put(newFriend, newFriendDatabase);
                }
            }

            // Check for removed friends
            for (String removedFriend : user.getFriends().values()) {
                if (!newFriendIDs.contains(removedFriend)) {
                    // Need to remove friend from current copy of user (already removed from database)
                    user.removeFriend(removedFriend);
                    Log.d("FriendsListListener", "Removing old friend: " + removedFriend);

                    // Remove from tracked references
                    DatabaseReference removedFriendDatabase = friendsDatabase.get(removedFriend);
                    if (removedFriend != null) {
                        removedFriendDatabase.removeEventListener(FriendDatabaseListener); // remove listener for this ex-friend
                        friendsDatabase.remove(removedFriend);
                    }

                    // Remove current copy of friend from hash map and array list
                    friendsArrayList.remove(friendsHashMap.remove(removedFriend));

                    // Update friends' UI
                    updateFriendUI();
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Reading from users database failed, log a message
            Log.w(TAG, "Reading values from user's friends list failed: " + databaseError);
        }
    };

    // Listen for changes to the friends values.
    ValueEventListener FriendDatabaseListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            // Put or overwrite data in the friends hash map
            User friend = dataSnapshot.getValue(User.class);

            Log.d("FriendDatabaseListener", "Updating friend information: " + friend.toString());
            // Overwrites any previous information, if this is an update
            friendsHashMap.put(friend.getUserID(), friend);

            boolean isUpdate = false;
            for (User listFriend : friendsArrayList){
                if (Objects.equals(listFriend.getUserID(), friend.getUserID())){
                    // Update previous information
                    listFriend.updateUser(friend);
                    isUpdate = true;
                }
            }

            if (!isUpdate) {
                // friend is a new entry, put into arraylist
                friendsArrayList.add(friend);
            }

            updateFriendUI();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Reading from users database failed, log a message
            Log.w(TAG, "Reading friends' values from user database failed: " + databaseError);
        }
    };

    // Authentication Status Listener
    FirebaseAuth.AuthStateListener authenticationStatusListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            // Double check status of current user
            firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser != null) {
                // User is signed in, should never be called since user logs in during previous activity
                Log.d(TAG, "onAuthStateChanged:signed_in:" + firebaseUser.getUid());
            } else {
                // User is signed out
                Log.d(TAG, "onAuthStateChanged:signed_out");
                // Reopen the WelcomeTutorial activity and close this one
                startActivity(new Intent(MainActivity.this, WelcomeTutorialActivity.class));
                MainActivity.this.finish();
            }
        }
    };

    /********************* Google Client Methods *********************/
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Google API Client connected");

        // Start location updates
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Google API Client disconnected");
        client.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();

        // Unregister the activity receiver that was registered during onResume()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(detectedActivityReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (client.isConnected()  && !requestingUpdates ) {
            startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");
        }

        // Register the broadcast receiver that informs this activity of the DetectedActivity
        // object broadcast sent by the intent service.
        LocalBroadcastManager.getInstance(this).registerReceiver(detectedActivityReceiver,
                new IntentFilter(Constants.BROADCAST_ACTION));

        // Check if listeners need to be added
        if (friendsListDatabase != null && !listening){
            // Listen for adding/removing friends
            friendsListDatabase.addValueEventListener(FriendsListListener);

            // Listen for friends' updates
            for (DatabaseReference friend : friendsDatabase.values()){
                friend.addValueEventListener(FriendDatabaseListener);
            }

            // Listen for alerts
            alertsDatabase.addChildEventListener(AlertsListener);

            listening = true;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged.................... New location = " + location.toString());

        // Update information
        currentLocation = location;
        updateTime = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date());

        if (user != null) {
            user.setLocation(stringFromLocation(currentLocation));
            user.setUpdateTime(updateTime);
            MainActivity.updateUserLocationDatabase();
        }

        // Launch intent to display new location's address
        startFetchAddressIntentService();

        updateUserUI();

        // Move map view to current location
        MainFragmentMapViewScreen.map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), Constants.DEFAULT_MAP_ZOOM));
    }

    // Pull out just the string from the location
    private String stringFromLocation(Location location){
        return String.format(Locale.US, "%.6f", location.getLatitude()) + ", " + String.format(Locale.US, "%.6f", location.getLongitude());
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsRevoked();
        }

        // Set up location requests
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setMaxWaitTime(Constants.MIN_UPDATE_TIME );   // Update at least every UPDATE_MINUTES minutes
        request.setSmallestDisplacement(Constants.MIN_UPDATE_DISTANCE);   // Update every UPDATE_METERS meters

        // Start location listening
        FusedLocationApi.requestLocationUpdates(MainActivity.client, request, this);

        // Create a PendingIntent to check for the user's activity with the same update time as the location updates
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(client,Constants.MIN_UPDATE_TIME, getActivityDetectionPendingIntent());

        requestingUpdates = true;

        Log.d(TAG, "Location update started ..............: ");
    }

    protected void stopLocationUpdates() {
        Log.d(TAG, "Location update stopped .......................");

        if (!requestingUpdates)
            // Not connected yet
            return;

        LocationServices.FusedLocationApi.removeLocationUpdates(client, this);

        // Stop activity recognition updates
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(MainActivity.client, getActivityDetectionPendingIntent());

        requestingUpdates = false;

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;

        if (requestCode == MainFragmentMenuScreen.REQUEST_INVITE){
            // Get the invitation IDs of all sent messages
            String [] ids = AppInviteInvitation.getInvitationIds(requestCode, data);
            if (ids != null)
                for (String id : ids){
                    Log.d(TAG, "onActivityResult: sent invitation " + id);

                    // Put this user in the database for pending alert
                    DatabaseReference invitation = FirebaseDatabase.getInstance().getReference().child("pendingFriends").child(id);
                    invitation.setValue(MainActivity.user.getUserID());

                }
            return;
        }

        Uri selectedImage = null;

        // Selected profile image from the gallery
        if (requestCode == FirebaseStorageManager.REQUEST_CODE_PICK_IMAGE) {
            Log.d(TAG, "Got image from gallery.");
            // Get returned image
            selectedImage = data.getData();
        } else if (requestCode == FirebaseStorageManager.REQUEST_CODE_CAMERA_CAPTURE) {
            Log.d(TAG, "Got image from camera.");
            selectedImage = Uri.fromFile(FirebaseStorageManager.getTempFile(this));
        }

        Log.d(TAG, "Selected image: " + selectedImage);

        // Scale image as necessary, then display
        if (selectedImage != null) {
            try {
                // Convert to bitmap
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedImage);

                // Scale as necessary
                userBitmap = FirebaseStorageManager.resizeBitmap(bitmap, Constants.PROFILE_WIDTH, Constants.PROFILE_HEIGHT);
                updateUserUI();

                // Store in firebase
                FirebaseStorageManager.storeImageToFirebase(userProfileRef, userBitmap);

            } catch (IOException exception){
                Log.d(TAG, "Error retrieving bitmap");
                exception.printStackTrace();
            }
        }
    }

    protected void permissionsRevoked(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, Constants.PERMISSION_LOCATION_ACCESS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FirebaseStorageManager.PERMISSION_READ_WRITE_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Ask again where to get image from
                MainFragmentMenuScreen.profileImagePicker(this);
            } else {
                Toast.makeText(this, "Using default profile image then.", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (requestCode == Constants.PERMISSION_LOCATION_ACCESS)
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Continue doing what you where doing
                    Log.d(TAG, "Location permission granted");
                } else {
                    Toast.makeText(this, "This app relies on using your location data, and cannot function without it. Please reconsider not allowing location access", Toast.LENGTH_LONG).show();
                    System.exit(0);
                }
        }
    }
}
