package com.ecen5673.diana.hereiam;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

/**
 * Created by diana on 10/14/16.
 *
 * Receiver to return address
 */
@SuppressLint("ParcelCreator")
public class FetchAddressResultReceiver extends ResultReceiver {

    // Default constructor
    public FetchAddressResultReceiver(Handler handler){
        super(handler);
    }

    // Receives data sent from FetchAddressIntentService and updates the user details
    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData){
        if (MainActivity.user != null) {
            MainActivity.user.setAddress(resultData.getString(Constants.ADDRESS_RESULT_KEY));
            MainActivity.updateUserUI();
            MainActivity.updateUserLocationDatabase();
        }
        else if (NotificationActivity.user != null) {
            NotificationActivity.user.setAddress(resultData.getString(Constants.ADDRESS_RESULT_KEY));
            NotificationActivity.updateUserLocationDatabase();
        }
        else if (NotificationActivity.started){
            NotificationActivity.currentAddress = resultData.getString(Constants.ADDRESS_RESULT_KEY);
        }
        else {
            MainActivity.currentAddress = resultData.getString(Constants.ADDRESS_RESULT_KEY);
        }
        Log.d("FetchAddyReceiver", "Fetched new address: " + resultData.getString(Constants.ADDRESS_RESULT_KEY));
    }
}
