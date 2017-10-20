package com.ecen5673.diana.hereiam;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;

/**
 * Created by diana on 9/30/16.
 *
 * Receiver for detected activity
 */
public class DetectedActivityBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the most probably result from the broadcast
        DetectedActivity detectedActivity = intent.getParcelableExtra(Constants.ACTIVITY_EXTRA);
        Log.d("DetectedActivity", "New activity = " + Constants.getActivityString(context, detectedActivity.getType()));
        if (MainActivity.user != null) {
            MainActivity.user.setActivity(Constants.getActivityString(context, detectedActivity.getType()));

            // Update list views and map views as necessary, as well as writing to the database
            MainActivity.updateUserUI(context);
            MainActivity.updateUserActivityDatabase();
        }
        // Check if we're running this for the notification activity
        else if (NotificationActivity.user != null) {
            NotificationActivity.user.setActivity(Constants.getActivityString(context, detectedActivity.getType()));

            // Update list views and map views as necessary, as well as writing to the database
            NotificationActivity.updateUserActivityDatabase();
        } else if (NotificationActivity.started = true) {
            NotificationActivity.currentActivity = Constants.getActivityString(context, detectedActivity.getType());
        }
        else {
            // Temporarily store the result until the user is loaded
            MainActivity.currentActivity = Constants.getActivityString(context, detectedActivity.getType());
        }
    }
}
