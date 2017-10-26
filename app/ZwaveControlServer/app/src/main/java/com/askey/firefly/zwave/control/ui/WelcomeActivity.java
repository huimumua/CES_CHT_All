package com.askey.firefly.zwave.control.ui;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.askey.firefly.zwave.control.R;
import com.askey.firefly.zwave.control.mqtt.MQTTBroker;
import com.askey.firefly.zwave.control.service.ZwaveControlService;
import com.askey.firefly.zwave.control.thirdparty.usbserial.driver.UsbSerialDriver;
import com.askey.firefly.zwave.control.thirdparty.usbserial.driver.UsbSerialPort;
import com.askey.firefly.zwave.control.thirdparty.usbserial.driver.UsbSerialProber;
import com.askey.firefly.zwave.control.utils.DeviceInfo;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by chiapin on 2017/9/7.
 */

public class WelcomeActivity extends BaseActivity{

    private static String LOG_TAG = WelcomeActivity.class.getSimpleName();

    private AlertDialog alertDialog;
    private Timer timer;

    private ZwaveControlService zwaveService;
    private BroadcastReceiver usbReceiver = null;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        mContext = this;

        showProgressDialog(mContext, "Initializing，Open Zwave Controller...");

        Intent MqttIntent = new Intent(WelcomeActivity.this, MQTTBroker.class);
        startService(MqttIntent);

        // bind service
        Intent serviceIntent = new Intent(this, ZwaveControlService.class);
        this.bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);

        new Thread(checkInitStatus).start();
    }

    public Runnable checkInitStatus = new Runnable() {
        @Override
        public void run() {

        timer = new Timer(true);
        timer.schedule(new mTimerTask(), 1000 * 120); //延时1000ms后执行，1000ms执行一次
            while (DeviceInfo.isZwaveInitFinish == false || DeviceInfo.isMQTTInitFinish == false ) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            initZwave();
        }
    };

    private void initZwave(){
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {

            timerCancel();

            Intent intent = new Intent();
            intent.setClass(mContext, HomeActivity.class);
            hideProgressDialog();
            mContext.startActivity(intent);
            finish();
            }
        });
    }

    private void showZwaveControlTimeOutDialog(String titleStr) {
        if(alertDialog == null) {
            AlertDialog.Builder addDialog = new AlertDialog.Builder(mContext);
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            View view = layoutInflater.inflate(R.layout.dialog_retry_layout, null);
            addDialog.setView(view);
            alertDialog = addDialog.create();
            alertDialog.show();

            TextView title = (TextView) view.findViewById(R.id.title);
            TextView message = (TextView) view.findViewById(R.id.message);
            title.setText("Prompt");
            message.setText(titleStr);
            Button positiveButton = (Button) view.findViewById(R.id.positiveButton);
            Button negativeButton = (Button) view.findViewById(R.id.negativeButton);

            positiveButton.setText("retry");
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //retry to opencontroller

                    String openResult = zwaveService.openController();
                    if (openResult.contains(":0")){
                        DeviceInfo.isOpenControllerFinish = true;
                    }

                    showProgressDialog(mContext, "OpenController....");
                    alertDialogCancel();
                }
            });

            negativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                //tap cancel button and exit to main screen
                finish();
                timerCancel();

                alertDialogCancel();
                DeviceInfo.isMQTTInitFinish = false;
                DeviceInfo.isOpenControllerFinish = false;
                DeviceInfo.isZwaveInitFinish = false;
                System.exit(0);
                }
            });
        }
    }

    public Handler mHandler = new Handler() {
        // 重写handleMessage()方法，此方法在UI线程运行
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2001:
                    timerCancel();
                    hideProgressDialog();
                    showZwaveControlTimeOutDialog("Zwave OpenController Timeout");
                    break;
            }
        }
    };

    class mTimerTask extends TimerTask {
        public void run() {
            Log.d(LOG_TAG,"timer on schedule");
            Message message = new Message();
            message.what = 2001;
            mHandler.sendMessage(message);
            timerCancel();
        }
    }

    private void alertDialogCancel() {
        if(alertDialog!=null){
            alertDialog.dismiss();
            alertDialog=null;
        }
    }

    private void timerCancel() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    // bind service with zwave control service
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i(LOG_TAG,"onServiceConnected....");
            zwaveService = ((ZwaveControlService.MyBinder)iBinder).getService();
            //register mCallback
            if (zwaveService != null) {
                zwaveService.register(mCallback);
                requestControlUSBPermission();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            zwaveService = null;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        zwaveService.unregister(mCallback);
        if (usbReceiver != null)
            unregisterReceiver(usbReceiver);
        try {
            this.unbindService(conn);
        } catch (Exception e) {
            Log.d(LOG_TAG, e.toString());
        }
    }

    public ZwaveControlService.zwaveCallBack mCallback = new ZwaveControlService.zwaveCallBack() {

        @Override
        public void zwaveControlResultCallBack(String className, String result) {

        }
    };

    private void requestControlUSBPermission() {
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        final List<UsbSerialDriver> drivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);

        boolean found = false;

        for (final UsbSerialDriver driver : drivers) {
            if (found) break;

            final List<UsbSerialPort> ports = driver.getPorts();

            for (final UsbSerialPort port : ports) {
                if(!usbManager.hasPermission(port.getDriver().getDevice())) {
                    PendingIntent mPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(port.getDriver().getDevice(), mPendingIntent);
                    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                    usbReceiver = new usbReceiver();
                    registerReceiver(usbReceiver, filter);
                } else {
                    openController();
                }
                found = true;
                break;
            }
        }
    }

    private class usbReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            openController();
                        }
                    } else {
                        Log.d("USB", "permission denied for device " + device);
                        finish();
                    }
                }
            }
        }
    };

    private void openController() {
        String openResult = zwaveService.openController();
        if (openResult.contains(":0")){
            DeviceInfo.isOpenControllerFinish = true;
        }
    }
}
