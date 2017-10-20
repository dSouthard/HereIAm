package com.ecen5673.diana.hereiam;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 *
 * Created by diana on 9/29/16.
 */
public class OnClickUserListener implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "OnClickUserListener";

    Activity activity;

    // Google client
    private GoogleApiClient client;

    // Request code used for logging in
    protected final int REQUEST_GOOGLE_SIGNIN = 122;
    private static final String DEFAULT_WEB_CLIENT_ID = "348944789907-roqevh226hhq2oqv94cqnkinj3u16pv1.apps.googleusercontent.com";

    public OnClickUserListener(Activity callingActivity){
        this.activity = callingActivity;

        // configure Google Sign In
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(DEFAULT_WEB_CLIENT_ID)
                .requestEmail()
                .build();

        // Build the Google client
        client = new GoogleApiClient.Builder(activity)
//                .enableAutoManage(activity, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();
    }

    @Override
    public void onClick(View view) {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(client);
        activity.startActivityForResult(signInIntent, REQUEST_GOOGLE_SIGNIN);

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed: "+ connectionResult);
        Toast.makeText(activity, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
}
