package com.junior.dwan.photogallery;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Might on 07.01.2017.
 */

public abstract class VisibleFragment extends Fragment {
    public static final String TAG="TAGTAGTAG";

    private BroadcastReceiver mOnShowNotification=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Toast.makeText(getActivity(),"Got a broadcast: "+intent.getAction(),Toast.LENGTH_SHORT).show();
            Log.i(TAG,"result cancelling");
            getActivity().setResult(Activity.RESULT_CANCELED);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter=new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
//        getActivity().registerReceiver(mOnShowNotification,filter);
        getActivity().registerReceiver(mOnShowNotification,filter,PollService.PERM_PRIVATE,null);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mOnShowNotification);
    }
}
