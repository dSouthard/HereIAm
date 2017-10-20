package com.ecen5673.diana.hereiam;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MainFragmentMenuScreen extends Fragment implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "MFMenuScreen";

    // Code used to request invites be sent to friendsHashMap
    protected static final int REQUEST_INVITE = 10;

    // TextViews
    static TextView recordUserName;
    static TextView recordLocation;
    static TextView recordAddress;
    static TextView recordUpdateTime;
    static TextView recordActivity;
    static ImageView recordProfile;
//    private GoogleApiClient client;

    public MainFragmentMenuScreen() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_main_menu, viewGroup, false);

        // Populate with user info
        recordUserName = (TextView) rootView.findViewById(R.id.recordNameField);
        recordLocation = (TextView) rootView.findViewById(R.id.recordLocationField);
        recordAddress = (TextView) rootView.findViewById(R.id.recordAddress);
        recordUpdateTime = (TextView) rootView.findViewById(R.id.recordUpdateTimeField);
        recordActivity = (TextView) rootView.findViewById(R.id.recordActivity);
        recordProfile = (ImageView) rootView.findViewById(R.id.recordUserProfile);

        // Set up buttons
        Button changeProfileImageBttn = (Button) rootView.findViewById(R.id.changeProfileImageBttn);
        Button inviteFriendBttn = (Button) rootView.findViewById(R.id.inviteFriendBttn);
        Button changeDisplayNameBttn = (Button) rootView.findViewById(R.id.changeDisplayNameBttn);
        Button logOutButtn = (Button) rootView.findViewById(R.id.logOutButtn);

        changeProfileImageBttn.setOnClickListener(this);
        inviteFriendBttn.setOnClickListener(this);
        logOutButtn.setOnClickListener(this);
        changeDisplayNameBttn.setOnClickListener(this);

        // Add radio group
        RadioGroup radioGroup = (RadioGroup) rootView.findViewById(R.id.updateRadioGroup);
        radioGroup.setOnCheckedChangeListener(this);

        // TODO Add click listener for the adapter
        return rootView;
    }

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);

        // Check for app invite invitations and launch deep-link activity if possible
        // Requires that the deep-link activity is registered in Android Manifest
        AppInvite.AppInviteApi.getInvitation(MainActivity.client, this.getActivity(), true);
    }


    // Main Activity has finished loading
    @Override
    public void onResume(){
        super.onResume();
    }

    protected static void setText(Context activity){
        if (MainActivity.user == null) return;
//        Populate the data into the template using the data object
        if (recordUserName != null)
            recordUserName.setText(String.format("You: %s", MainActivity.user.getUserName()));
        if (recordLocation != null)
            recordLocation.setText(MainActivity.user.getLocation());
        if (recordAddress != null)
            recordAddress.setText(MainActivity.user.getAddress());
        if (recordUpdateTime != null)
            recordUpdateTime.setText(MainActivity.user.getUpdateTime());
        if (recordActivity != null)
            recordActivity.setText(MainActivity.user.getActivity());
        if (MainActivity.userBitmap != null)
            recordProfile.setImageBitmap(MainActivity.userBitmap);
        else {
            Glide.with(activity).using(new FirebaseImageLoader())
                    .load(MainActivity.userProfileRef)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true).into(recordProfile);
        }
    }

    protected static void setText(){
        if (MainActivity.user == null) return;
//        Populate the data into the template using the data object
        if (recordUserName != null)
            recordUserName.setText(String.format("You: %s", MainActivity.user.getUserName()));
        if (recordLocation != null)
            recordLocation.setText(MainActivity.user.getLocation());
        if (recordAddress != null)
            recordAddress.setText(MainActivity.user.getAddress());
        if (recordUpdateTime != null)
            recordUpdateTime.setText(MainActivity.user.getUpdateTime());
        if (recordActivity != null)
            recordActivity.setText(MainActivity.user.getActivity());
        if (MainActivity.userBitmap != null)
            recordProfile.setImageBitmap(MainActivity.userBitmap);
    }



    @Override
    public void onClick(View view) {
        int buttonID = view.getId();
        switch (buttonID){
            case R.id.changeProfileImageBttn:
                Log.d(TAG, "changeProfileImageBttn pressed");
                profileImagePicker(this.getActivity());
                break;
            case R.id.inviteFriendBttn:
                Log.d(TAG, "inviteFriendBttn pressed");
                onInviteClicked();
                break;
            case R.id.changeDisplayNameBttn:
                Log.d(TAG, "changeDisplayNameBttn pressed");
                changeName();
                break;
            case R.id.logOutButtn:
                Log.d(TAG, "logOutButtn pressed");
                // sign the user out
                FirebaseAuth.getInstance().signOut();
                break;
        }
    }

    // Start dialog to choose how to pick user image
    protected static void profileImagePicker(final Activity activity){
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    // Capture image from camera
                    FirebaseStorageManager.pickImage(activity, true);
                } else if (items[item].equals("Choose from Library")) {
                    // Choose image from gallery
                    FirebaseStorageManager.pickImage(activity, false);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    // Start dialog to change user name
    private void changeName(){
        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(MainFragmentMenuScreen.this.getContext());
        View promptsView = layoutInflater.inflate(R.layout.dialog_change_display_name, (ViewGroup)null);

        // Creating alert Dialog with one Button
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainFragmentMenuScreen.this.getContext());
        alertDialog.setView(promptsView);

        // Setting Dialog Title
        alertDialog.setTitle("Change Display Name");

        // Setting Dialog Message
        final EditText input = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);
        input.setText(MainActivity.user.getUserName());

        // Setting Icon to Dialog
        alertDialog.setIcon(R.drawable.user_red_icon);

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        MainActivity.user.setUserName(input.getText().toString());
                        MainActivity.currentUserDatabaseReference.child("userName").setValue(MainActivity.user.getUserName());
                        MainActivity.updateUserUI(MainFragmentMenuScreen.this.getActivity());
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

    /**
     * User has clicked the 'Invite' button, launch the invitation UI with the proper
     * title, message, and deep link
     */
    // [START on_invite_clicked]
    private void onInviteClicked() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setDeepLink(Uri.parse(getString(R.string.invitation_deep_link)))
                .setCustomImage(Uri.parse("http://nccworship.net/images/content/hia_main_still.jpg"))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();

//        intent.putExtra("userID", MainActivity.user.getUserID());
        startActivityForResult(intent, REQUEST_INVITE);
    }

    // Get result from deep-link, invitation-send activity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_INVITE){
            if (resultCode == Activity.RESULT_OK){
                // Get the invitation IDs of all sent messages
                String [] ids = AppInviteInvitation.getInvitationIds(requestCode, data);
                if (ids != null)
                    for (String id : ids){
                        Log.d(TAG, "onActivityResult: sent invitation " + id);

                        // Put this user in the database for pending alert
                        DatabaseReference invitation = FirebaseDatabase.getInstance().getReference().child("pendingFriends").child(id);
                        invitation.setValue(MainActivity.user.getUserID());

                    }
            } else {
                // Sending failed or was cancelled, display a failure message
                Toast.makeText(getContext(), "Failure to send invites", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        // Find out which radio button is selected
        switch (checkedId){
            case R.id.fiveMinuteRadioButton:
                Constants.UPDATE_MINUTES = 5;
                break;
            case R.id.fifteenMinuteRadioButton:
                Constants.UPDATE_MINUTES = 15;
                break;
            case R.id.thirtyMinuteRadioButton:
                Constants.UPDATE_MINUTES = 30;
                break;
            case R.id.sixtyMinuteRadioButton:
                Constants.UPDATE_MINUTES = 60;
                break;
        }

        ((MainActivity)getActivity()).stopLocationUpdates();
        ((MainActivity)getActivity()).startLocationUpdates();
    }
}
