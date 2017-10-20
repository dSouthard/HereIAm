package com.ecen5673.diana.hereiam;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by diana on 10/14/16.
 *
 * Service to fetch address
 */
public class FetchAddressIntentService extends IntentService {
    private static String TAG = "FetchAddressIntentService";

    // The detectedActivityReceiver were results are forwarded from this service
    protected ResultReceiver receiver;

    // Constructor
    public FetchAddressIntentService(){
        // Use the TAG to name the worker thread
        super(TAG);
    }

    /* Tries to get the location adress using a Geocoder. If successful, send an address
    to the result detectedActivityReceiver. Otherwise, sends an error message instead. the ResultReceiver
    in the Map Fragment is used to process content sent from this service.

    This service calls this method from the default worker thread with the intent that
    started this service. When this method returns, the service automatically stops.
     */
    @Override
    protected void onHandleIntent(Intent intent){
        String errorMessage = "";

        receiver = intent.getParcelableExtra(Constants.RECEIVER);

        // Check if the detectedActivityReceiver was properly registered
        if (receiver == null){
            Log.wtf(TAG, "No address detectedActivityReceiver received. There is nowhere to send the results.");
            return;
        }

        // Get the location passed to this service through an extra
        Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);

        // Make sure that the location data was really sent over through an extra
        if (location == null){
            errorMessage = "No location data was provided.";
            Log.wtf(TAG, errorMessage);
            deliverResultToReceiver(Constants.ADDRESS_FAIL_RESULT, errorMessage);
            return;
        }

        /* Errors could still arise from using the Geocoder. (For example, if there is no
        connectivity, of if the Geocoder is given illegal location data.) Or the Geocoder
        may not have found an address. In all these cases, we communicate a failure.

        The Geocoder used in this file has responses localized for the given Locate.
         */
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        // Address found using the Geocoder
        List<Address> addresses = null;

        try {
            /* Using getFromLocation() returns an array of addresses for the area immediately surrounding
            the given lat/long. The results are a best guess and aren't guaranteed to be accurate.
             */
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    // Just get a single address
                    1);

        } catch (IOException exception) {
            // Catch network or other I/O problems.
            errorMessage = "IO Exception in getFromLocation()";
            Log.e("ComplaintLocation", errorMessage, exception);
        } catch (IllegalArgumentException exception){
            // Catch invalid latitude or longitude values.
            errorMessage = "Illegal arguments " +
                    Double.toString(location.getLatitude()) + ", " +
                    Double.toString(location.getLongitude()) +
                    " passed to address service.";
            Log.e("LocationActivity", errorMessage, exception);
        }

        // Handle case where no address was found
        if (addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "No address found.";
                Log.e(TAG, errorMessage);
            }
            deliverResultToReceiver(Constants.ADDRESS_FAIL_RESULT, errorMessage);
        }
        else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<>();

            /* Fetch the address lines using getAddressLine, join them, and send them to the thread.
             */
            for (int i = 0; i < address.getMaxAddressLineIndex(); i++){
                addressFragments.add(address.getAddressLine(i));
            }
            Log.i(TAG, "Address found");
            deliverResultToReceiver(Constants.ADDRESS_SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"), addressFragments));
        }
    }

    private void deliverResultToReceiver(int resultCode, String message){
        Bundle bundle = new Bundle();
        bundle.putString(Constants.ADDRESS_RESULT_KEY, message);
        receiver.send(resultCode, bundle);
    }
}
