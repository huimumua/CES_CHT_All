package com.askey.mobile.zwave.control.guideSetting.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;
import com.askey.mobile.zwave.control.util.PreferencesUtils;
import com.askey.mobile.zwave.control.util.ToastShow;
import com.askey.mobile.zwave.control.welcome.udp.UdpConnect;
import com.askeycloud.sdk.device.response.UserIoTDevice;
import com.askeycloud.sdk.device.response.UserIoTDeviceListResponse;
import com.askeycloud.webservice.sdk.model.ServicePreference;
import com.askeycloud.webservice.sdk.model.auth.v3.OAuthResultModel;
import com.askeycloud.webservice.sdk.service.device.AskeyIoTDeviceService;

import java.util.List;

public class WpsFindingActivity extends BaseActivity {
    private static final String TAG = WpsFindingActivity.class.getSimpleName();
    private UdpConnect mUdpConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wps_finding);

        showWaitingDialog();
        if(Const.isRemote) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (checkIsBinding()) {
                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                stopWaitDialog();
                                Intent intent = new Intent(mContext,WpsFoundActivity.class);
                                startActivity(intent);
                            }
                        });
                    } else {
                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                stopWaitDialog();
                                Intent intent = new Intent(mContext,WpsNotFoundActivity.class);
                                startActivity(intent);
                            }
                        });
                    }
                }
            }).start();
        }else{
            mUdpConnect = new UdpConnect();
            mUdpConnect.search(this, onGatwayFindRunnable, oncanNotFindGWayRunnable);
        }

    }


    private boolean checkIsBinding() {
        UserIoTDeviceListResponse response = AskeyIoTDeviceService.getInstance(mContext).userIoTDeviceList();
        Logg.i(TAG, "===checkIsBinding==getCode==" + response.getCode());
        Logg.i(TAG, "===checkIsBinding==getMessage==" + response.getMessage());
        List<UserIoTDevice> devices = response.getDevices();
        for (UserIoTDevice userIoTDevice : devices) {
            Logg.i(TAG, "== userIoTDevice.getDeviceid()==" + userIoTDevice.getDeviceid());
            Logg.i(TAG, "== userIoTDevice.getDevicemodel()==" + userIoTDevice.getDevicemodel());
            Logg.i(TAG, "== userIoTDevice.getDisplayname()==" + userIoTDevice.getDisplayname());
            Logg.i(TAG, "== userIoTDevice.getUniqueid()==" + userIoTDevice.getUniqueid());
            if (userIoTDevice.getDevicemodel().equals("FIREFLY")) {
                return true;
            }
        }
        return false;
    }


    public Runnable onGatwayFindRunnable = new Runnable() {
        @Override
        public void run() {
            //udp广播已获取到相关信息
            Logg.i(TAG, "=====onGatwayFindRunnable========");
            stopWaitDialog();
            Intent intent = new Intent(mContext,WpsFoundActivity.class);
            startActivity(intent);

        }
    };

    Runnable oncanNotFindGWayRunnable = new Runnable() {
        @Override
        public void run() {
            Logg.i(TAG, "=====oncanNotFindGWayRunnable========");
            stopWaitDialog();
            Intent intent = new Intent(mContext,WpsNotFoundActivity.class);
            startActivity(intent);
        }
    };


}
