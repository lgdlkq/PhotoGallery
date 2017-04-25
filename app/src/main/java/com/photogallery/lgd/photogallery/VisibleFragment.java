package com.photogallery.lgd.photogallery;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Administrator on 2017/4/24.
 */

public class VisibleFragment extends Fragment {

    private static final String TAG = "VisibleFragment";

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
//        getActivity().registerReceiver(mOnShowNotification, filter);
        getActivity().registerReceiver(mOnShowNotification, filter, PollService.PERM_PRIVATE, null);//使用定义的私有权限
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mOnShowNotification);
    }

    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Toast.makeText(getActivity(), "Got a broadcast: " + intent.getAction(), Toast.LENGTH_LONG).show();
            Log.i(TAG, "canceling notification");
            setResultCode(Activity.RESULT_CANCELED);//返回一个简单的结果码
        }
    };
}