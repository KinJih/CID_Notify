package com.cid_notify.cid_notify.Util;



import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.cid_notify.cid_notify.Activity.MainActivity;
import com.cid_notify.cid_notify.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d("MT", "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d("MT", "Message data payload: " + remoteMessage.getData());
            String body = getString(R.string.from)+" : "+remoteMessage.getData().get("phoneNum")+"\n"+getString(R.string.to)+" : "+remoteMessage.getData().get("CallTo");
            String title = getString(R.string.new_calls)+" : "+remoteMessage.getData().get("number_info");
            sendNotification(body,title);
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d("MT", "Message Notification Body: " + remoteMessage.getNotification().getBody());
            //sendNotification(remoteMessage.getNotification().getBody(),remoteMessage.getNotification().getTitle());
        }

    }

    private void sendNotification(String body,String title) {

        Intent intent = new Intent(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0/*Request ID*/, intent, PendingIntent.FLAG_ONE_SHOT);
//        set Sound of Notification
        Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notifiBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(notificationSound)
                .setContentIntent(pendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notifiBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(body)
                    .setBigContentTitle(title));
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0/*ID of notification*/, notifiBuilder.build());
    }

}
