package com.ecen5673.diana.hereiam;

/**
 * Created by diana on 11/14/16.
 *
 * Response to getting an invitation
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Activity for displaying information about a receive App Invite invitation.  This activity
 * displays as a Dialog over the MainActivity and does not cover the full screen.
 */
public class InvitationActivity extends AppCompatActivity implements
        View.OnClickListener {

    private static final String TAG = "InvitationActivity";
    User friend;

    DatabaseReference friendDatabase;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation);

        // Button click listener
        findViewById(R.id.okButton).setOnClickListener(this);
        findViewById(R.id.declineButton).setOnClickListener(this);
    }

    // [START deep_link_on_start]
    @Override
    protected void onStart() {
        super.onStart();

        // Check if the intent contains an AppInvite and then process the referral information.
        Intent intent = getIntent();
        if (AppInviteReferral.hasReferral(intent)) {
            processReferralIntent(intent);
        }
    }
    // [END deep_link_on_start]

    // [START process_referral_intent]
    private void processReferralIntent(Intent intent) {
        // Extract referral information from the intent
        final String invitationId = AppInviteReferral.getInvitationId(intent);

        DatabaseReference invitation = FirebaseDatabase.getInstance().getReference().child("pendingFriends");
        invitation.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(invitationId)) {
                    String friendID = dataSnapshot.child(invitationId).getValue(String.class);

                    continueSetup(friendID);

                    // Remove value now that we've got it
                    dataSnapshot.child(invitationId).getRef().removeValue();
                }
                else {
                    Log.d(TAG, "Didn't find invite");
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void continueSetup(String friendID){
        friendDatabase = FirebaseDatabase.getInstance().getReference().child("user").child(friendID);
        friendDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Loading friend");
                friend = dataSnapshot.getValue(User.class);
                updateUI();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Error reading friend's entry");
            }
        });
    }

    private void updateUI(){
        TextView name = (TextView) findViewById(R.id.friendNameDeepLink);
        ImageView profileImage = (ImageView) findViewById(R.id.friendProfileImageDeepLink);

        name.setText(friend.getUserName());

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(friend.getUserID()).child(Constants.PROFILE_STRING);
        Glide.with(this).using(new FirebaseImageLoader())
                .load(storageReference)
                .into(profileImage);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.okButton) {
            // User wants to be friends
            final DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference().child("user");
            userDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (firebaseUser == null){
                        Log.d(TAG, "Couldn't find user ID");
                        finish();
                    }

                    // Add user to friend's list
                    if (dataSnapshot.hasChild(firebaseUser.getUid())) {
                        User user = dataSnapshot.child(firebaseUser.getUid()).getValue(User.class);
                        user.addFriend(friend.getUserID());
                    } else {
                        Log.d(TAG, "New user: creating new entry");
                        User user = new User(firebaseUser.getDisplayName(), firebaseUser.getUid());
                        user.addFriend(friend.getUserID());
                        userDatabase.child(user.getUserID()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                // Add user ID to my friend's list
                                friend.addFriend(firebaseUser.getUid());
                                friendDatabase.child("friend").setValue(friend.getFriends()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Log.d(TAG, "Finished adding new friend, returning to Main Menu");
                                        finish();
                                    }
                                });
                            }
                        });
                    }



                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "Error reading user database");
                }
            });


        } else if (i == R.id.declineButton){
            finish();
        }
    }
}