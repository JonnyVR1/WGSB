package com.jonny.wgsb;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import static com.jonny.wgsb.CommonUtilities.displayMessage;

public class GCMIntentService extends IntentService {
    private static final int NOTIFICATION_ID = 1;
    private DatabaseHandler dbhandler = DatabaseHandler.getInstance(this);

    public GCMIntentService() {
        super("GCMIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);
        if (!extras.isEmpty()) {
            String date = extras.getString("date");
            String title = extras.getString("title");
            String message = extras.getString("message");
            Integer id;
            if (dbhandler.getNotificationsCount() > 0) {
                id = dbhandler.getNotificationsCount();
                id++;
            } else {
                id = 1;
            }
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                Integer read = 0;
                dbhandler.addNotification(new Notifications(id, title, date, message, read));
                sendNotification(id, title, message);
                displayMessage(this, message);
            }
        }
        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(Integer id, String title, String message) {
        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(this);
        Intent notificationIntent = new Intent();
        notificationIntent.setClass(this, MainActivity.class);
        notificationIntent.putExtra("id", id).putExtra("notification", true);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        /*NotificationCompat.Action markAsReadAction = new NotificationCompat.Action.Builder(R.drawable.ic_action,
                "Mark as read", dbhandler.updateNotifications(new Notifications(id, 1)))
                .build();*/
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.push_icon)
                .setContentTitle("WGSB - " + title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentText(message)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE)
                /*.extend(new NotificationCompat.WearableExtender().addAction(markAsReadAction))*/;
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}