package com.askey.iotcontrol.ui;

import android.app.Activity;
import android.app.ActivityManager;
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
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.askey.iotcontrol.R;
import com.askey.iotcontrol.dao.ZwaveDevice;
import com.askey.iotcontrol.dao.ZwaveDeviceManager;
import com.askey.iotcontrol.service.MQTTBroker;
import com.askey.iotcontrol.service.ZwaveControlService;
import com.askey.iotcontrol.thirdparty.usbserial.UsbSerial;
import com.askey.iotcontrol.utils.Const;
import com.askey.iotcontrol.utils.DeviceInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by chiapin on 2017/9/7.
 */

public class WelcomeActivity extends BaseActivity{

    private static String LOG_TAG = WelcomeActivity.class.getSimpleName();

    private static AlertDialog alertDialog;
    private static Timer timer = new Timer(true);

    private static ZwaveControlService zwaveService;
    private ZwaveDeviceManager zwDevManager;
    private BroadcastReceiver usbReceiver = null;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static boolean mqttBrokerFlag = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        new UsbSerial(this);
        setContentView(R.layout.activity_welcome);
        mContext = this;

        Intent MqttIntent = new Intent(WelcomeActivity.this, MQTTBroker.class);
        if (!isServiceRunning(mContext,MQTTBroker.class)){
            mqttBrokerFlag = true;
            startService(MqttIntent);
        }

        showProgressDialog(mContext, "Initializing，Open Zwave Controller...");

        zwDevManager = ZwaveDeviceManager.getInstance(this);

        // bind service
        Intent serviceIntent = new Intent(this, ZwaveControlService.class);
        this.bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);

        new Thread(checkInitStatus).start();
    }

    public Runnable checkInitStatus = new Runnable() {
        @Override
        public void run() {
            while (!DeviceInfo.isZwaveInitFinish || !DeviceInfo.isMQTTInitFinish) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (mqttBrokerFlag) {initSensorfunc();}
            initZwave();
        }
    };

    private void initZwave() {
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

    public static boolean isServiceRunning(Context context,Class<?> serviceClass){
        final ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            Log.d(LOG_TAG, String.format("Service:%s", runningServiceInfo.service.getClassName()));
            if (runningServiceInfo.service.getClassName().equals(serviceClass.getName())){
                Log.i(LOG_TAG,serviceClass.getName()+" is already running");
                return true;
            }
        }
        return false;
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
                    openController();
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
            zwaveService.closeController();
            Log.d(LOG_TAG,"timer on schedule");
            Message message = new Message();
            message.what = 2001;
            mHandler.sendMessage(message);
            timerCancel();
        }
    }

    private static void alertDialogCancel() {
        if(alertDialog!=null){
            alertDialog.dismiss();
            alertDialog=null;
        }
    }

    private static void timerCancel() {
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

                initSensorfunc();
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

    public static ZwaveControlService.zwaveCallBack mCallback = new ZwaveControlService.zwaveCallBack() {

        @Override
        public void zwaveControlResultCallBack(String className, String result) {

        }
    };

    private void requestControlUSBPermission() {

        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> usbDevices = manager.getDeviceList();

        UsbDevice dev = null;
        int vid = 0;
        int pid = 0;

        for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
            dev = entry.getValue();
            vid = dev.getVendorId();
            pid = dev.getProductId();
            Log.d(LOG_TAG, "Usb Device Vid = "+ Integer.toHexString(vid) +",Pid = "+ Integer.toHexString(pid));
            if ((vid == 0x0658) && (pid == 0x0200)) {
                Log.d(LOG_TAG, "Usb Device Is CDC Device...");
                if (manager.hasPermission(dev)) {
                    Log.d(LOG_TAG, "Usb Permission Ok....");
                    openController();
                    break;
                } else {
                    Log.e(LOG_TAG, "Usb Permission Na,Try To Request Permission....");
                    PendingIntent mPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    manager.requestPermission(dev, mPendingIntent);
                    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                    usbReceiver = new usbReceiver();
                    registerReceiver(usbReceiver, filter);
                }
            }else{
                dev = null;
                Log.d(LOG_TAG, "Usb Device Is Not CDC Device...");
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
                        progressDialog.cancel();
                        finish();
                        System.exit(0);
                    }
                }
            }
        }
    }

    private void openController() {
        timer.schedule(new mTimerTask(), 1000 * 120);
        String openResult = zwaveService.openController();
        if (openResult.contains(":0")){
            DeviceInfo.isOpenControllerFinish = true;
        }
    }

    private void initSensorfunc() {

        List<ZwaveDevice> list = zwDevManager.queryZwaveDeviceList();

        if (list.size() == 1){
            return;
        }

        for (int idx = 1; idx < list.size(); idx++) {

            int nodeId = list.get(idx).getNodeId();
            String devCate = list.get(idx).getCategory();

            Log.i(LOG_TAG,"#"+nodeId+" | devCate = "+devCate);

            if (devCate.equals("SENSOR")) {
                String devNodeInfo = list.get(idx).getNodeInfo();

                if (devNodeInfo.contains("COMMAND_CLASS_BATTERY")) {
                    Log.i(LOG_TAG, "BATTERY");
                    zwaveService.getDeviceBattery(Const.zwaveType,nodeId);
                }

                zwaveService.getSensorNotification(nodeId, 0x00, 0x01, 0x00);
                zwaveService.getSensorNotification(nodeId, 0x00, 0x05, 0x00);
                zwaveService.getSensorNotification(nodeId, 0x00, 0x06, 0x00);
                zwaveService.getSensorNotification(nodeId, 0x00, 0x07, 0x00);
                /*
                if (devNodeInfo.contains("COMMAND_CLASS_NOTIFICATION")) {
                    try {
                        JSONObject jsonObject = new JSONObject(devNodeInfo);
                        if (jsonObject.getString("Product id").equals("001F")) {
                            //Water
                            v
                        } else if (jsonObject.getString("Product id").equals("000C")) {
                            //Motion
                            zwaveService.getSensorNotification(nodeId, 0x00, 0x07, 0x00);
                            //Door/Window
                            zwaveService.getSensorNotification(nodeId, 0x00, 0x06, 0x00);
                        } else if (jsonObject.getString("Product id").equals("0036")) {
                            //Door/Window
                            zwaveService.getSensorNotification(nodeId, 0x00, 0x06, 0x00);
                        } else if (jsonObject.getString("Product id").equals("001E")) {
                            //SMOKE
                            zwaveService.getSensorNotification(nodeId, 0x00, 0x01, 0x00);
                        } else if (jsonObject.getString("Product id").equals("0050")) {
                            //Motion
                            zwaveService.getSensorNotification(nodeId, 0x00, 0x07, 0x00);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                */
                if (devNodeInfo.contains("COMMAND_CLASS_SENSOR_MULTILEVEL")) {
                    try {
                        zwaveService.getSensorMultiLevel(Const.zwaveType,nodeId);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                zwaveService.getMeterSupported(nodeId);
                zwaveService.GetSensorBinarySupportedSensor(nodeId);

            }
        }
    }
}
