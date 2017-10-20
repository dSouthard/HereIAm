package com.ecen5673.diana.hereiam;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by diana on 11/8/16.
 *
 * Extends Instance ID Service
 */
public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDS";

    /*
    Called if InstanceID token is updated. This may occur is the secuity of the previous token
    is compromised. Note that this is called when the InstanceID token is initially generated so this
    is where we retrieve the token.
     */
    @Override
    public void onTokenRefresh(){
        // Get updated InstanceID token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
    }
}
