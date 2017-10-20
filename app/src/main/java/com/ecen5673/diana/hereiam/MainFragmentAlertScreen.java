package com.ecen5673.diana.hereiam;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class MainFragmentAlertScreen extends Fragment {

    private static final String TAG = "AlertScreen";
    protected static User closestFriend;

    // Used to maintain closest friend for messaging
    static TextView closestFriendTextView;
    static TextView alertMessage;

    public MainFragmentAlertScreen() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_main_alert, viewGroup, false);

        // Set up text view
        alertMessage = (TextView) rootView.findViewById(R.id.alertMessageField);
        closestFriendTextView = (TextView) rootView.findViewById(R.id.closestFriendField);

        // Set up buttons
        Button alertClosestFriendButton = (Button) rootView.findViewById(R.id.alertClosestFriendButton);
        Button alertAllFriendsButton = (Button) rootView.findViewById(R.id.alertAllFriendsButton);
        Button updateAlertMessageButton = (Button) rootView.findViewById(R.id.updateAlertMessageButton);

        // Set up button on click listeners
        alertClosestFriendButton.setOnClickListener(new OnClickAlertListener(getActivity(), false));
        alertAllFriendsButton.setOnClickListener(new OnClickAlertListener(getActivity(), true));
        updateAlertMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "updateAlertMessageButton pressed.");
                changeAlertMessage();
            }
        });

        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    protected static void setText(){
//        Set alert_icon message text
        if (MainActivity.user == null) return;
        if (alertMessage != null)
            alertMessage.setText(MainActivity.user.getAlertMessage());

        // find closest friend
        findClosestFriend();
    }

    protected static void findClosestFriend(){
        Log.d(TAG, "Finding closest friend.");
        if (MainActivity.friendsArrayList.isEmpty() && closestFriendTextView!= null){
            closestFriendTextView.setText(R.string.no_friends);
        }
        else {
            Location userLocation = locationFromString(MainActivity.user.getLocation());
            float distance = -1;
            for (User friend: MainActivity.friendsHashMap.values()){
                float newDistance = userLocation.distanceTo(locationFromString(friend.getLocation()));
                if (distance == -1 || newDistance < distance) {
                    distance = newDistance;
                    closestFriend = friend;
                }
            }

            // Set closest friend
            closestFriendTextView.setText(closestFriend.getUserName());
        }
    }

    private static Location locationFromString(String locationString){
        String parts[] = locationString.split(",");
        Location returnLocation = new Location(LocationManager.GPS_PROVIDER);
        returnLocation.setLatitude(Double.parseDouble(parts[0]));
        returnLocation.setLongitude(Double.parseDouble(parts[1]));
        return returnLocation;
    }

    // Start dialog to change user's alert message
    private void changeAlertMessage(){

        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(this.getContext());
        View promptsView = layoutInflater.inflate(R.layout.dialog_change_display_name, (ViewGroup)null);

        // Creating alert Dialog with one Button
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this.getContext());
        alertDialog.setView(promptsView);

        // Setting Dialog Title
        alertDialog.setTitle("Change Alert Message");

        // Setting Dialog Message
        final EditText input = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);
        input.setText(MainActivity.user.getAlertMessage());

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.alert_icon);

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        MainActivity.user.setAlertMessage(input.getText().toString());
                        MainActivity.currentUserDatabaseReference.child("alertMessage").setValue(MainActivity.user.getAlertMessage());
                        MainActivity.updateUserUI(MainFragmentAlertScreen.this.getActivity());
                    }
                });
        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog
                        dialog.cancel();
                    }
                });

        // Showing Alert Message
        alertDialog.show();
    }


}
