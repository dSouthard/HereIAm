package com.ecen5673.diana.hereiam;

import android.content.Context;

import com.google.android.gms.location.DetectedActivity;

/**
 * Created by diana on 9/30/16.
 * Stores shared constant values for app
 */
public class Constants {

    // Permission Constants
    public static final int PERMISSION_LOCATION_ACCESS = 50;

    public static final int SEND_NOTIFICATION_REQUEST_CODE = 99;
    public static final String FRIEND_EXTRA = "friend";

    public static final String PACKAGE_NAME = "com.ecen5673.diana.hereiam";
    public static final String BROADCAST_ACTION = PACKAGE_NAME + ".BROADCAST_ACTION";
    public static final String ACTIVITY_EXTRA = PACKAGE_NAME + ".ACTIVITY_EXTRA";

    // Result code for Address Receiving Intents
    public static final int ADDRESS_SUCCESS_RESULT = 0;
    public static final int ADDRESS_FAIL_RESULT = 1;
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String ADDRESS_RESULT_KEY = PACKAGE_NAME + ".ADDRESS_RESULT_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";

    // How often to check for location/activity updates
    public static int UPDATE_MINUTES = 1;
    public static int MIN_UPDATE_TIME = UPDATE_MINUTES * 60 * 1000;
    public static int MIN_UPDATE_DISTANCE = 15;

    // Default map configurations
    public static int DEFAULT_MAP_ZOOM = 15;

    // Profile image constants
    public static final String PROFILE_STRING = "profile.png";
    public static final int PROFILE_HEIGHT = 150;
    public static final int PROFILE_WIDTH = 150;

    // Returns a human readable String corresponding to a detected activity type
    public static String getActivityString(Context context, int detectedActivityType){
        switch (detectedActivityType){
            case DetectedActivity.IN_VEHICLE:
                return context.getString(R.string.driving);
            case DetectedActivity.ON_FOOT:
                return context.getString(R.string.onFoot);
            case DetectedActivity.RUNNING:
                return context.getString(R.string.running);
            case DetectedActivity.WALKING:
                return context.getString(R.string.walking);
            case DetectedActivity.STILL:
                return context.getString(R.string.notMoving);
            case DetectedActivity.TILTING:
                return context.getString(R.string.tilting);
            case DetectedActivity.UNKNOWN:
                return context.getString(R.string.unknown);
            default:
                return context.getString(R.string.unidentifiableActivity) + String.valueOf(detectedActivityType);
        }
    }
}
