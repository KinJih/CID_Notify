package com.cid_notify.cid_notify.Util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.cid_notify.cid_notify.Activity.MainActivity;
import com.cid_notify.cid_notify.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static int id = 0;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d("MT", "From: " + remoteMessage.getFrom());
        SharedPreferences pref = getSharedPreferences("Settings", MODE_PRIVATE);

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0 && pref.getBoolean("Notification",true)) {
            Log.d("MT", "Message data payload: " + remoteMessage.getData());
            String body = String.format(getString(R.string.arrow),remoteMessage.getData().get("phoneNum"),remoteMessage.getData().get("CallTo"));
            String title = String.format(getString(R.string.new_call),remoteMessage.getData().get("number_info"));
            sendNotification(body,title);
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null && pref.getBoolean("Notification",true)) {
            Log.d("MT", "Message Notification Body: " + remoteMessage.getNotification().getBody());
            sendNotification(remoteMessage.getNotification().getBody(),remoteMessage.getNotification().getTitle());
        }

    }

    private NotificationManager getManager(){
        return (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private void sendNotification(String body,String title) {

        Intent intent = new Intent(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0/*Request ID*/, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"CID")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channelCID = new NotificationChannel("CID", "Channel CID_Notify",
                    NotificationManager.IMPORTANCE_HIGH);
            channelCID.setDescription("For CID_Notify");
            channelCID.enableLights(true);
            channelCID.enableVibration(true);
            getManager().createNotificationChannel(channelCID);
        }
        if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.LOLLIPOP) builder.setVisibility(Notification.VISIBILITY_PUBLIC);
        getManager().notify(id++/*ID of notification*/, builder.build());
    }

}
