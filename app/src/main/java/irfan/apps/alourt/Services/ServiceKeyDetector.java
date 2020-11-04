/*
 * Copyright (c) 2020 Irfan S.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Syed Irfan Ahmed <irfansa1@ymail.com>
 */

package irfan.apps.alourt.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import irfan.apps.alourt.Home;
import irfan.apps.alourt.R;

public class ServiceKeyDetector extends Service {

    private final String NOTIFICATION_CHANNEL_ID = "10001";
    private final String TAG = "SKDetector";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        Notification n;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Intent myintent = new Intent(this, Home.class);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                myintent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setColor(8421504);
        builder.setContentIntent(contentIntent);
        builder.setSmallIcon(R.drawable.common_google_signin_btn_icon_dark);//R.mipmap.posturect_notification)builder.setLargeIcon(icon);
        builder.setContentTitle("Alourt");
        builder.setContentText("Alourt is active");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Alourt";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            builder.setChannelId(NOTIFICATION_CHANNEL_ID);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        n = builder.build();
        n.flags |= Notification.FLAG_ONGOING_EVENT;

        int service_Id = 12;
        Log.d(TAG, "Notification built and service setting up, launching...");
        startForeground(service_Id, n);
        Log.d(TAG, "Notification and service active.");
        return START_STICKY;
    }

    /**
     * Method to restart Posturect service if device RAM is cleared by user.
     *
     * @param rootIntent
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {

    }

    @Override
    public void onDestroy() {

    }


}
