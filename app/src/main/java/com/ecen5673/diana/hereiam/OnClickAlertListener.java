package com.ecen5673.diana.hereiam;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * Created by diana on 9/29/16.
 *
 * Listener to display alert dialog
 */
public class OnClickAlertListener implements View.OnClickListener {

    Activity activity = null;
    boolean send = true;
    UserDetailsListViewAdapter arrayAdapter;
    ArrayList<String> sendIDs = new ArrayList<>();

    public OnClickAlertListener(Activity activity, boolean send){
        this.activity = activity;
        this.send = send;
    }

    @Override
    public void onClick(View view) {
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        @SuppressLint("InflateParams")
        View promptsView = layoutInflater.inflate(R.layout.dialog_send_alert, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        // Set dialog layout to builder
        builder.setView(promptsView);

        // Setting Icon to Dialog
        builder.setIcon(R.drawable.alert_icon);

        // Set up alert_icon message text
        TextView alertMessage = (TextView) promptsView.findViewById(R.id.alertDialogText);
        alertMessage.setText(MainActivity.user.getAlertMessage());

        // Get list of people to send message
        ListView listView = (ListView) promptsView.findViewById(R.id.alertListView);

        if (send) {
            arrayAdapter = new UserDetailsListViewAdapter(activity, MainActivity.friendsArrayList);
            for (User friend : MainActivity.friendsArrayList){
                sendIDs.add(friend.getUserID());
            }
        }
        else {
            ArrayList<User> list = new ArrayList<>();
            // check if closest friend has been found, then put in array list
            if (MainFragmentAlertScreen.closestFriend == null){
                MainFragmentAlertScreen.findClosestFriend();
            }
            list.add(MainFragmentAlertScreen.closestFriend);
            arrayAdapter = new UserDetailsListViewAdapter(activity, list);
            sendIDs.add(MainFragmentAlertScreen.closestFriend.getUserID());
        }

        // set to list view
        listView.setAdapter(arrayAdapter);

        // Set Dialog Message
        builder.setCancelable(true)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int id){
                                sendAlerts();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int id){
                                dialogInterface.dismiss();
                            }
                        });

        // Create alert_icon dialog
        AlertDialog alertDialog = builder.create();

        // Show dialog
        alertDialog.show();
    }

    private void sendAlerts(){
        String TAG = "OnClickAlertListener";
        Log.i(TAG, "Sending Alerts");

        if (sendIDs.isEmpty()){
            Log.d(TAG, "No friends to send to");
            return;
        }

        String myID = MainActivity.user.getUserID();
        for (String friendID : sendIDs) {
            Log.d(TAG, "Sending alert to ID " + friendID);
            sendNotificationToUser(friendID, myID);
        }
    }

    public static void sendNotificationToUser(String userID, String myID) {
        // Push alert onto friends' alert databases
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(userID);
        ref.child(myID).setValue(myID);
    }
}
