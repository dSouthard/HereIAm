package com.ecen5673.diana.hereiam;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


public class WelcomeTutorialActivity extends AppCompatActivity
{

    private static String TAG = "WelcomeTutorialActivity";

    //    // Firebase instance variables
    private FirebaseAuth firebaseAuth;
    //    private FirebaseUser firebaseUser;
//
//    // Authentication Listener
    private FirebaseAuth.AuthStateListener authStateListener;
//
//    private  GoogleApiClient client;

    // The pager widget, which handles animation and allows swiping horizontally to
    // access previous and next wizard steps
    private ViewPager pager;

    // UserDetail to save new user information
//    public static User user;

    // Request code when requesting location services
    protected final int REQUEST_LOCATION_PERMISSION = 121;
    protected final int REQUEST_GOOGLE_SIGNIN = 122;

//    private static final String DEFAULT_WEB_CLIENT_ID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_tutorial);

        // Instantiate a ViewPager and a PagerAdapter
        pager = (ViewPager) findViewById(R.id.tutorialViewPager);
        PagerAdapter pagerAdapter = new WelcomeTutorialPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);

        // Set up page transition animation
        pager.setPageTransformer(true, new ZoomOutPageTransformer());

        // initialize authentication
        firebaseAuth = FirebaseAuth.getInstance();

        // Start authentication listener
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
//                if (user != null){
                // User is signed in
                if (firebaseUser != null) {
                    Log.i(TAG, "onAuthStateChanged: signed_in: " + firebaseUser.getUid() );
//                    }
                    Log.i(TAG, "Moving on to Main Activity");
                    startActivity(new Intent(WelcomeTutorialActivity.this, MainActivity.class));
                    WelcomeTutorialActivity.this.finish();
                } else {
                    Log.i(TAG, "onAuthStateChanged: signed_out");
                    // A toast is generated elsewhere for unsuccessful login attempts
                }
            }
        };

        // Setup floating SKIP action button to skip tutorial and log in using Google
        FloatingActionButton skipButton = (FloatingActionButton) findViewById(R.id.skipTutorialFAB);
        skipButton.setOnClickListener(new OnClickUserListener(this));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tutorialTabs);
        tabLayout.setupWithViewPager(pager);

        // Check for required permissions
        requestPermissions();
    }

    @Override
    public void onBackPressed() {
        if (pager.getCurrentItem() > 0){
            // Select the previous step
            pager.setCurrentItem(pager.getCurrentItem() - 1);
        }
    }

    public void requestPermissions(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Asking for permissions");
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
        else {
            Log.d(TAG, "Permissions already granted");
        }
    }

    private void closeApp(){
        this.finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // Permission Granted
                    Log.d(TAG, "Permission Granted");
                    return;
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(this)
                            .setTitle("Permission Required")
                            .setMessage("Location must be enabled to use this application. If location permissions are denied, this app will close. Are you sure you won't allow location tracking?")
                            .setPositiveButton("I'd like to be asked again.", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    requestPermissions();
                                }
                            })
                            .setNegativeButton("No, I don't want to allow location tracking", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.d(TAG, "Permission Denied");
                                    closeApp();
                                }
                            })
                            .create();

                    alertDialog.show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//        Log.i(TAG, "onConnectionFailed: "+ connectionResult);
//        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
//    }
//
    @Override
    public void onStart(){
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    public void onStop(){
        super.onStop();
        if (authStateListener != null)
            firebaseAuth.removeAuthStateListener(authStateListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching intent from GoogleSignInApi
        if (requestCode == REQUEST_GOOGLE_SIGNIN) {
            GoogleSignInResult signInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (signInResult.isSuccess()) {
                // Google Sign in was successful, authenticate with Firebase
                GoogleSignInAccount account = signInResult.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else{
                // google Sign in failed
                Log.i(TAG, "signInResult: failure!");
            }
        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle: " + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.i(TAG, "signInWithCredential: onComplete: " + task.isSuccessful());

                        // If a sign in fails, display a message to the user.
                        // If a sign in succeeds, the listener will handle logic
                        if (!task.isSuccessful()){
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(WelcomeTutorialActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(WelcomeTutorialActivity.this, "Welcome!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
