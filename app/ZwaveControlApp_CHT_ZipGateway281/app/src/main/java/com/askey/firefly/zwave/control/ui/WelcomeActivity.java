package com.askey.firefly.zwave.control.ui;

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
import com.askey.firefly.zwave.control.service.MQTTBroker;
import com.askey.firefly.zwave.control.service.ZwaveControlService;
import com.askey.firefly.zwave.control.utils.Const;
import com.askey.firefly.zwave.control.utils.DeviceInfo;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by chiapin on 2017/9/7.
 */

public class WelcomeActivity extends BaseActivity {
    private static String LOG_TAG = WelcomeActivity.class.getSimpleName();
    private BroadcastReceiver usbReceiver = null;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private ZwaveControlService zwaveService;
    private static AlertDialog alertDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        WelcomeActivity subconst = new WelcomeActivity();
        //new Const(this);
        process();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // =========================================================init zwave and mqtt service  ====================================================================

    private void process() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                mHandler.sendEmptyMessage(1);   //show init dialog

                Intent mqttService = new Intent(WelcomeActivity.this, MQTTBroker.class);
                startService(mqttService);

                Intent zwaveService = new Intent(WelcomeActivity.this, ZwaveControlService.class);
                startService(zwaveService);
                bindService(zwaveService, conn, Context.BIND_AUTO_CREATE);

                for(int i=0;i<36;i++){
                    if(DeviceInfo.isOpenControllerFinish == 0) {
                        i = 36;
                        mHandler.sendEmptyMessage(0);   //finish
                        DeviceInfo.isOpenControllerFinish = 0;
                    } else if(DeviceInfo.isOpenControllerFinish == -1){
                        mHandler.sendEmptyMessage(-1);  //show fail dialog
                        DeviceInfo.isOpenControllerFinish = 99;
                    } else if (DeviceInfo.isOpenControllerFinish == 99) {
                        mHandler.sendEmptyMessage(99);  //show wait dialog
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(DeviceInfo.isOpenControllerFinish != 0)
                        getDeviceInfo(); //check opencontrol is OK
                }
                if(DeviceInfo.isOpenControllerFinish == 99)
                    mHandler.sendEmptyMessage(881);     //show time out dialog
            }
        }).start();
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            zwaveService = ((ZwaveControlService.MyBinder)iBinder).getService();
            //register mCallback
            if (zwaveService != null) {
                requestControlUSBPermission(); //if usb permission is not ready , zwave service is not start
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            zwaveService = null;
        }
    };
    // =========================================================UI update  ====================================================================

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    unbindService(conn);
                    Intent intent = new Intent();
                    intent.setClass(mContext, MainActivity.class);
                    hideProgressDialog();
                    mContext.startActivity(intent);
                    finish();
                    break;
                case 1:
                    showProgressDialog(mContext, "Initializing ,Open Zwave Controller...");
                    break;
                case -1:
                    hideProgressDialog();
                    showZwaveControlFailDialog("Zwave OpenController Fail");
                    break;
                case 99:
                    hideProgressDialog();
                    showProgressDialog(mContext,"Loading ,please wait a moment..........");
                    break;
                case 881:
                    hideProgressDialog();
                    timeOutDialog("Zwave OpenController Time out");
                    break;
            }
        }
    };

    // =========================================================Show Dialog ====================================================================

    private void timeOutDialog(String titleStr) {
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

            positiveButton.setText("leave");
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent zwaveService = new Intent(WelcomeActivity.this, ZwaveControlService.class);
                    stopService(zwaveService);
                    Log.d(LOG_TAG,"gino stopService");
                    alertDialogCancel();
                    finish();
                    System.exit(0);
                }
            });

            negativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //tap cancel button and exit to main screen
                    alertDialogCancel();
                }
            });

        }
    }

    private void showZwaveControlFailDialog(String titleStr) {
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
                    alertDialogCancel();
                    DeviceInfo.isMQTTInitFinish = false;
                    DeviceInfo.isOpenControllerFinish = 99;
                    DeviceInfo.isZwaveInitFinish = false;
                    System.exit(0);
                }
            });
        }
    }

    private static void alertDialogCancel() {
        if(alertDialog!=null){
            alertDialog.dismiss();
            alertDialog=null;
        }
    }

    // =========================================================USB permission ====================================================================

    private void requestControlUSBPermission() {
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                            PendingIntent mPendingIntent = PendingIntent.getBroadcast(WelcomeActivity.this, 0, new Intent(ACTION_USB_PERMISSION), 0);
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
        }).start();
    }

    public class usbReceiver extends BroadcastReceiver {
        public void onReceive(Context context, final Intent intent) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String action = intent.getAction();
                    if (ACTION_USB_PERMISSION.equals(action)) {
                        synchronized (this) {
                            UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                                if (device != null) {
                                    openController();
                                }
                            } else {
                                Log.d(LOG_TAG,"USB"+ "permission denied for device " + device);
                                System.exit(0);
                            }
                        }
                    }
                }
            }).start();
        }
    }

    // =========================================================Zwave Service ====================================================================

    private void getDeviceInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int result = zwaveService.getDeviceInfo();
                if(result == 0)
                    DeviceInfo.isOpenControllerFinish = 0;
                else
                    DeviceInfo.isOpenControllerFinish = 99;
                Log.d(LOG_TAG,"gino getDeviceInfo = " + result);
            }
        }).start();
    }

    private void openController() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(LOG_TAG,"gino opencontroller");
                zwaveService.doOpenController();
            }
        }).start();
    }
}
