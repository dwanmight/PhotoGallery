package com.junior.dwan.photogallery;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Might on 05.01.2017.
 */

public class PollService extends IntentService {
    public static final String TAG = "TAGTAGTAG";
    public static final int POLL_INTERVAL = 1000 * 5; //5c
    public static final String PREF_IS_ALARM_ON = "isAlarmOn";
    public static final String ACTION_SHOW_NOTIFICATION = "com.junior.dwan.photogallery.SHOW_NOTIFICATION";
    public static final String PERM_PRIVATE = "com.junior.dwan.photogallery.PRIVATE";

    public PollService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressWarnings("deprecation")
        boolean isNetworkAvailable = cm.getBackgroundDataSetting() && cm.getActiveNetworkInfo() != null;

        if (!isNetworkAvailable) return;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String query = sharedPreferences.getString(FlickrFetchr.PREF_SEARCH_QUERY, null);
        String lastResultId = sharedPreferences.getString(FlickrFetchr.PREF_LAST_RESULT_ID, null);

        ArrayList<GalleryItem> items;
        if (query != null) {
            items = new FlickrFetchr().search(query);
        } else {
            items = new FlickrFetchr().fetchItems();
        }

        if (items.size() == 0)
            return;
        String resultId = items.get(0).getId();
        if (!resultId.equals(lastResultId)) {
            Log.i(TAG, "Got a new Result: " + resultId);
            Resources r = getResources();
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, PhotoGalleryActivity.class), 0);

            Notification notification = new NotificationCompat.Builder(this)
                    .setTicker(r.getString(R.string.new_pictures_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(r.getString(R.string.new_pictures_title))
                    .setContentText(r.getString(R.string.new_pictures_text))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();

//            NotificationManager notificationManager =(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
//            notificationManager.notify(0,notification);
//
////            sendBroadcast(new Intent(ACTION_SHOW_NOTIFICATION));
//            sendBroadcast(new Intent(ACTION_SHOW_NOTIFICATION),PERM_PRIVATE);

            showBackgroundNotification(0, notification);

        } else {
            Log.i(TAG, "Got an old result: " + resultId);
        }

        sharedPreferences.edit().putString(FlickrFetchr.PREF_LAST_RESULT_ID, resultId);

        Log.i(TAG, "Received on itent: " + intent);
    }

    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent i = new Intent(context, PollService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, i, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (isOn) {
            alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), POLL_INTERVAL, pendingIntent);
        } else {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }

        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_IS_ALARM_ON, isOn)
                .apply();
    }

    public static boolean isServiceAlarmOn(Context context) {
        Intent i = new Intent(context, PollService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pendingIntent != null;
    }

    void showBackgroundNotification(int requestCode, Notification notification) {
        Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra("REQUEST_CODE", requestCode);
        i.putExtra("NOTIFICATIONS", notification);

        sendOrderedBroadcast(i, PERM_PRIVATE, null, null, Activity.RESULT_OK, null, null);
    }
}
