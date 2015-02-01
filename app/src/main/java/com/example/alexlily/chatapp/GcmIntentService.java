package com.example.alexlily.chatapp;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }
    public static final String TAG = "GCM Demo";

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString(), null, null, null);
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " + extras.toString(), null, null, null);
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                String messageStatus = extras.getString("messageStatus");
                Log.i(TAG, "messageStatus is " + messageStatus);
                // messageStatus is going to be
                /*
                * login -> open up contact page
                * contactpage
                * new conversation -> open up main activity
                * new message -> open up main activity
                * */
                if (messageStatus.equals("new message") || messageStatus.equals("new conversation")){
                    Intent i = new Intent(getApplicationContext(),MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra(getString(R.string.message_label),intent.getStringExtra("message"));
                    i.putExtra(getString(R.string.message_type_label), intent.getStringExtra("messageStatus"));
                    getApplicationContext().startActivity(i);
                }
//                else if (messageStatus.equals("new conversation")) {
//                    Log.i(TAG, "new conversation");
//                    Intent i = new Intent(getApplicationContext(),MainActivity.class);
//                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    i.putExtra(getString(R.string.message_label),intent.getStringExtra("message"));
//                    i.putExtra(getString(R.string.message_type_label), intent.getStringExtra("messageStatus"));
//                    getApplicationContext().startActivity(i);
//                }
                else{

                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra(getString(R.string.message_label), "a message");
                    i.putExtra(getString(R.string.message_type_label), getString(R.string.new_convo_label));
                    i.putExtra(getString(R.string.username_label), extras.getString("username"));
                    i.putExtra(getString(R.string.contact_label), extras.getString("contact"));
                    i.putExtra(getString(R.string.site_label), extras.getString("site"));
                    //Log.i(TAG, extras.getString("username") + extras.getString("contact") + extras.getString("site") + "s;dlfjfd");
                    getApplicationContext().startActivity(i);

                }
                // Post notification of received message.
//                String message = extras.getString("message");
//                String messageStatus = extras.getString("messageStatus");
//                String username = extras.getString("username");
//                String contact = extras.getString("contact");
//                sendNotification(message, messageStatus, username, contact);
//                Log.i(TAG, extras.toString());

            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String message, String messageStatus, String username, String contact) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra(getString(R.string.new_message_label), "old messages"); // the old messages, when it's a new/reopened convo, the new message when it's within a conversation already
        i.putExtra(getString(R.string.username_label), username);
        i.putExtra(getString(R.string.contact_label), contact);
        i.putExtra(getString(R.string.message_type_label), "new conversation");

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                i, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("GCM Notification")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("message " + message + " username " + username + " contact " + contact + " messageStatus " + messageStatus))
                        .setContentText("message " + message + " username " + username + " contact " + contact + " messageStatus " + messageStatus);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}