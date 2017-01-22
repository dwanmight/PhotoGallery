package com.junior.dwan.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by Might on 07.01.2017.
 */

public class StartupReceiver extends BroadcastReceiver {
    public static final String TAG="TAGTAGTAG";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"Received broadcast intent: "+intent.getAction());

        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(context);
        boolean isOn=preferences.getBoolean(PollService.PREF_IS_ALARM_ON,false);
        PollService.setServiceAlarm(context,isOn);
    }
}
