package com.ecen5673.diana.hereiam;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;


/**
 * Created by diana on 9/30/16.
 *
 * Launches intent to detect current actiivty
 */
public class DetectedActivitiesIntentService extends IntentService {

    protected static final String TAG = "DetectActivityIS";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public DetectedActivitiesIntentService() {
        super(TAG);
    }

    /*
    Handles incoming intents.
    @param intent The intent is provided (inside a PendingIntent) when requestActivityUpdates() is called
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        Intent localIntent = new Intent(Constants.BROADCAST_ACTION);

        // Get the most probably activity result
        DetectedActivity detectedActivity = result.getMostProbableActivity();
        Log.d(TAG, "onHandleIntent: Most probable activity = " + detectedActivity.toString());

        // Broadcast the list of detected activities
        localIntent.putExtra(Constants.ACTIVITY_EXTRA, detectedActivity);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}
