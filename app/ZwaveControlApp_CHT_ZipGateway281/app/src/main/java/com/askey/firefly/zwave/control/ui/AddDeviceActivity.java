package com.askey.firefly.zwave.control.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.askey.firefly.zwave.control.R;
import com.askey.firefly.zwave.control.dao.ZwaveDevice;
import com.askey.firefly.zwave.control.dao.ZwaveDeviceManager;
import com.askey.firefly.zwave.control.service.ZwaveControlService;
import com.askey.firefly.zwave.control.utils.Const;
import com.askey.firefly.zwave.control.utils.DeviceInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by chiapin on 2017/9/28.
 */

public class AddDeviceActivity extends BaseActivity implements View.OnClickListener {

    private static String LOG_TAG = AddDeviceActivity.class.getSimpleName();
    private ImageView ivBack;
    private ProgressBar proBar;
    private Button btnCancel;
    private TextView tvStatus;
    private Timer timer;
    private ZwaveDeviceManager zwDevManager;
    private ZwaveControlService zwaveService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);

        zwDevManager = ZwaveDeviceManager.getInstance(this);

        ivBack = (ImageView) findViewById(R.id.iv_back);
        proBar = (ProgressBar) findViewById(R.id.proBar);
        tvStatus = (TextView) findViewById(R.id.tv_status);

        tvStatus.setText("Please wait a moment...");
        proBar.setIndeterminate(true);
        btnCancel = (Button) findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(this);
        ivBack.setOnClickListener(this);

        timer = new Timer(true);
        timer.schedule(new AddDeviceActivity.mTimerTask(), 1000 * 65); //延时1000ms后执行，1000ms执行一次

        // bind service
        new Thread(bindzwaveservice).start();


    }

    public Runnable bindzwaveservice = new Runnable() {
        @Override
        public void run() {

            Intent serviceIntent = new Intent(AddDeviceActivity.this, ZwaveControlService.class);
            bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                timerCancel();
                stopAddDevice();
                finish();
                break;
            case R.id.btn_cancel:
                showStopAddDialog();
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        zwaveUnregister();
    }

    // bind service with zwave control service
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            zwaveService = ((ZwaveControlService.MyBinder)iBinder).getService();
            //register mCallback
            if (zwaveService != null) {
                zwaveService.register(mCallback);
                addDevice();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            zwaveService = null;
        }
    };



    private Handler mHandler = new Handler() {
        // 重写handleMessage()方法，此方法在UI线程运行
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2001:
                    hideProgressDialog();
                    showFailedAddZaveDialog("Add Device Timeout");
                    break;

                case 2002:
                    Log.i(LOG_TAG,"2002");
                    timerCancel();
                    hideProgressDialog();
                    initSensorfunc(msg.getData().getInt("nodeId"));
                    showAddDialog(String.valueOf(msg.getData().getInt("nodeId")));
                    break;
            }
        }
    };

    class mTimerTask extends TimerTask {
        public void run() {
            Log.d(LOG_TAG,"timer on schedule");
            Message message = new Message();
            timerCancel();
            message.what = 2001;
            mHandler.sendMessage(message);
        }
    }

    private void timerCancel() {
        if(timer!=null){
            timer.cancel();
            timer=null;
        }
    }

    public void showFailedAddZaveDialog(final String titleStr) {

        final android.support.v7.app.AlertDialog.Builder addDialog = new android.support.v7.app.AlertDialog.Builder(mContext);
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.dialog_normal_layout, null);
        addDialog.setView(view);
        final android.support.v7.app.AlertDialog alertDialog = addDialog.create();

        TextView title = (TextView) view.findViewById(R.id.title);
        TextView message = (TextView) view.findViewById(R.id.message);
        title.setText("Prompt");
        message.setText(titleStr);

        Button retryeButton = (Button) view.findViewById(R.id.positiveButton);
        Button negativeButton = (Button) view.findViewById(R.id.negativeButton);
        retryeButton.setText("retry");
        retryeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //tap retry button
                addDevice();
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void showAddDialog(final String nodeId) {

        final AlertDialog.Builder addDialog = new AlertDialog.Builder(AddDeviceActivity.this);
        LayoutInflater layoutInflater = LayoutInflater.from(AddDeviceActivity.this);

        View view = layoutInflater.inflate(R.layout.dialog_add_layout, null , true);
        addDialog.setView(view);

        final AlertDialog alertDialog = addDialog.create();
        alertDialog.show();
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText("Add Device Success");

        // type spinner
        final EditText message = (EditText) view.findViewById(R.id.message);

        final Spinner spDevType = (Spinner) view.findViewById(R.id.spDevType);
        Button positiveButton = (Button) view.findViewById(R.id.positiveButton);
        Button negativeButton = (Button) view.findViewById(R.id.negativeButton);

        ArrayAdapter<String> devTypeList = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                DeviceInfo.deviceType);

        spDevType.setAdapter(devTypeList);

        spDevType.setSelected(true);
        spDevType.requestFocus();
        // room spinner

        final Spinner spRoom = (Spinner) view.findViewById(R.id.spAllRoom);

        ArrayAdapter<String> roomList = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                DeviceInfo.allRoomName);

        spRoom.setAdapter(roomList);
        message.setText(nodeId);

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            reName("AeoTech" , Integer.parseInt(nodeId), message.getText().toString(),
                    Const.zwaveType, spDevType.getSelectedItem().toString(),
                    spRoom.getSelectedItem().toString());

            alertDialog.dismiss();
            backToHomeActivity();
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            alertDialog.dismiss();
            finish();
            }
        });

        alertDialog.show();

    }

    private void initSensorfunc(final int nodeId){
        Log.d(LOG_TAG,"nodeId = " + nodeId);
        new Thread(){
            @Override
            public void run(){
                ZwaveDevice zwSensor = zwDevManager.queryZwaveDevices(nodeId);
                String devNodeInfo = zwSensor.getNodeInfo();

                if (devNodeInfo.contains("COMMAND_CLASS_BATTERY")) {
                    Log.i(LOG_TAG, "BATTERY");
                    zwaveService.getDeviceBattery(Const.zwaveType, nodeId);
                }

                if (devNodeInfo.contains("COMMAND_CLASS_NOTIFICATION")) {
                    try {
                        zwaveService.getSensorNotification(nodeId, 0x00, 0x01, 0x00);
                        zwaveService.getSensorNotification(nodeId, 0x00, 0x05, 0x00);
                        zwaveService.getSensorNotification(nodeId, 0x00, 0x06, 0x00);
                        zwaveService.getSensorNotification(nodeId, 0x00, 0x07, 0x00);
                        /*
                        JSONObject jsonObject = new JSONObject(devNodeInfo);
                        if (jsonObject.getString("Product id").equals("001F")) {
                            //Water
                            zwaveService.getSensorNotification(nodeId, 0x00, 0x05, 0x00);
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
                        */
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (devNodeInfo.contains("COMMAND_CLASS_SENSOR_MULTILEVEL")){
                    try {
                        zwaveService.getSensorMultiLevel(Const.zwaveType, nodeId);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

                zwaveService.getMeterSupported(nodeId);
                zwaveService.GetSensorBinarySupportedSensor(nodeId);
            }
        }.start();
    }

    private void reName(String brandId ,int nodeId, String newName,String devType,String devCate,String roomName) {
        zwaveService.editNodeInfo(brandId,nodeId,newName,devType,devCate,roomName,"1");
    }

    /*
    private void showAddFailDialog() {
        final AlertDialog.Builder addDialog = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.dialog_normal_layout, null);

        final AlertDialog alertDialog = addDialog.create();
        addDialog.setView(view);

        TextView title = (TextView) view.findViewById(R.id.title);
        TextView message = (TextView) view.findViewById(R.id.message);
        title.setText("Prompt");
        message.setText("Add faild");

        Button retryButton = (Button) view.findViewById(R.id.positiveButton);
        Button backButton = (Button) view.findViewById(R.id.negativeButton);
        retryButton.setText("retry");
        retryButton.setSelected(true);
        retryButton.requestFocus();

        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            //tap retry button
            proBar.setIndeterminate(true);

            zwaveService.addDevice(Const.zwaveType);
            alertDialog.dismiss();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            //tap back button
            backToHomeActivity();
            }
        });

        alertDialog.show();
    }
    */

    private void showStopAddDialog() {
        final AlertDialog.Builder addDialog = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.dialog_normal_layout, null);
        addDialog.setView(view);

        final AlertDialog alertDialog = addDialog.create();
        alertDialog.show();
        TextView title = (TextView) view.findViewById(R.id.title);
        TextView message = (TextView) view.findViewById(R.id.message);
        title.setText("Prompt");
        message.setText("Stop Add Device ?");
        Button positiveButton = (Button) view.findViewById(R.id.positiveButton);
        Button negativeButton = (Button) view.findViewById(R.id.negativeButton);
        positiveButton.setText("OK");
        positiveButton.setSelected(true);
        positiveButton.requestFocus();
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            timerCancel();
            if (zwaveService!=null) {
                stopAddDevice();
            }
            alertDialog.dismiss();

            backToHomeActivity();

            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            alertDialog.dismiss();

            }
        });
    }


    private void addDeviceResult(String result) {
        try {

            final JSONObject jsonObject = new JSONObject(result);

                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    String messageType = jsonObject.optString("MessageType");
                    String status = jsonObject.optString("Status");
                    if ("Node Add Status".equals(messageType)) {
                        if ("Success".equals(status)) {
                            timerCancel();
                            tvStatus.setText("Success, Please wait a moment to rename");
                            proBar.setIndeterminate(false);

                        } else if ("Failed".equals(status)) {
                            showFailedAddZaveDialog("Add Device Fail");
                            timerCancel();
                            proBar.setIndeterminate(false);

                        } else if("Learn Ready".equals(status)){
                            tvStatus.setText("Please press the trigger button of the device");

                        }else{
                            tvStatus.setText(status);
                        }
                    }
                    }
                });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public ZwaveControlService.zwaveCallBack mCallback = new ZwaveControlService.zwaveCallBack() {

        @Override
        public void zwaveControlResultCallBack(String className, String result) {

        if (className.equals("addDevice")){

            if (result.contains("addDevice:")){
                String[] tokens = result.split(":");
                if (tokens.length<3){
                    Log.i(LOG_TAG,"AIDLResult addDevice : wrong format "+result);
                } else {
                    String tHomeId = tokens[1];
                    String tNodeId = tokens[2];

                    Message message = new Message();
                    Bundle data = new Bundle();
                    data.putInt("nodeId", Integer.parseInt(tNodeId));
                    data.putString("homeId", tHomeId);
                    message.setData(data);
                    message.what = 2002;
                    mHandler.sendMessage(message);
                }
            }else {
                addDeviceResult(result);
            }
        }
        }
    };

    private void addDevice() {
        new Thread(){
            @Override
            public void run(){
                zwaveService.addDevice(Const.zwaveType);
            }
        }.start();
    }

    private void stopAddDevice() {
        new Thread(){
            @Override
            public void run(){
                zwaveService.stopAddDevice(Const.zwaveType);
            }
        }.start();
    }

    private void zwaveUnregister() {
        new Thread(){
            @Override
            public void run(){
                zwaveService.unregister(mCallback);
                unbindService(conn);
            }
        }.start();
    }
}