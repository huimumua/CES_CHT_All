package com.askey.firefly.zwave.control.service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.askey.firefly.zwave.control.ui.WelcomeActivity;
import com.askey.firefly.zwave.control.utils.Logg;

/**
 * 项目名称：ZwaveControl
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2017/7/10 11:39
 * 修改人：skysoft
 * 修改时间：2017/7/10 11:39
 * 修改备注：
 */
public class BootBroadcastReceiver extends BroadcastReceiver{
    private final String TAG = "BootBroadcastReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("android.intent.action.USER_PRESENT")) {
            Logg.i(TAG, "=========USER_PRESENT===========");
            ServiceConnection serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {

                }
            };
            Intent service = new Intent(context, ZwaveControlService.class);
            context. bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
        }

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Log.i(TAG, "=========BOOT_COMPLETED===========");

            Intent activity = new Intent(context, WelcomeActivity.class);
            activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activity);
        }

    }
}
