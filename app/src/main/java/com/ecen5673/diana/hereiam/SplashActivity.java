package com.ecen5673.diana.hereiam;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by diana on 9/28/16.
 * <p/>
 * Splash activity to load the user and direct app to either the new user activity or to the normal main activity
 */
public class SplashActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        String TAG = "SplashActivity";
        Log.d(TAG, "Started SplashActivity");

        // Check if there is a current user logged in
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser == null) {
            // Not signed in, launch the Sign In activity
            Log.d(TAG, "No previous users, going to WelcomeTutorialActivity");
            startActivity(new Intent(this, WelcomeTutorialActivity.class));
            finish();
        } else {
            Log.d(TAG, "Found saved user, going to MainActivity");
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

}
