package com.ecen5673.diana.hereiam;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by diana on 11/8/16.
 *
 *
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MessagingService";

    // Called when message is received
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        /*
        There are two types of messages: data and notification. Data messages are handled here whether
        the app is in the foreground of background, typically used with GCM. Notification messages are
        only received here in onMessageReceived when the app is in the foreground. When the app is in
        the background, an automatically generated notification is displayed. When the user taps on
        the notification, they are returned to the app. Messages containing both notification and data
        payloads are treated as notification messages. The Firebase console always sends notification
        messages.
         */
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload
        if (remoteMessage.getData().size() > 0){
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // check if message contained a notification payload
        if (remoteMessage.getNotification() != null){
            Log.d(TAG, "Message notification body: " + remoteMessage.getNotification().getBody());
        }

        // To generate our own notifications as a result of a received FCM, here is where it should be initialized
        createNotification(remoteMessage.getNotification().getBody());
    }

    private void createNotification( String messageBody) {
        Intent intent = new Intent( this , NotificationActivity. class );
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent resultIntent = PendingIntent.getActivity( this , Constants.SEND_NOTIFICATION_REQUEST_CODE, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri notificationSoundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder( this)
                .setSmallIcon(R.drawable.exclamation)
                .setContentTitle("Here I Am Alert!")
                .setContentText(messageBody)
                .setAutoCancel( true )
                .setSound(notificationSoundURI)
                .setContentIntent(resultIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, mNotificationBuilder.build());
    }

    public static void createNewNotification( String userID, Context context) {
        Intent intent = new Intent( context, NotificationActivity.class );
        intent.putExtra(Constants.FRIEND_EXTRA, userID);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent resultIntent = PendingIntent.getActivity( context , Constants.SEND_NOTIFICATION_REQUEST_CODE, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String messageBody = MainActivity.friendsHashMap.get(userID).getAlertMessage();

        Uri notificationSoundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder( context)
                .setSmallIcon(R.drawable.exclamation)
                .setContentTitle("Here I Am Alert!")
                .setContentText(messageBody)
                .setAutoCancel( true )
                .setSound(notificationSoundURI)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentIntent(resultIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, mNotificationBuilder.build());
    }


}
