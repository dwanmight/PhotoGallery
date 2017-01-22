package com.junior.dwan.photogallery;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Might on 08.01.2017.
 */

public class NotificationsReceiver extends BroadcastReceiver {
    public static final String TAG = "TAGTAGTAG";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "receivedResult in NotificationReceiver: " + getResultCode());
        if (getResultCode() != Activity.RESULT_OK) {
            // Активность переднего плана отменила
            // широковещательную передачу
            return;
        }

        int requestCode = intent.getIntExtra("REQUEST_CODE", 0);
        Notification notification = (Notification) intent.getParcelableExtra("NOTIFICATIONS");
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(requestCode, notification);

    }
}
