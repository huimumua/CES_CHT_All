package com.askey.firefly.zwave.control.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

import com.askey.firefly.zwave.control.R;
import com.askey.firefly.zwave.control.service.ZwaveControlService;
import com.askey.firefly.zwave.control.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SensorActivity extends BaseActivity {
    private static String LOG_TAG = SensorActivity.class.getSimpleName();
    private String nodeInfo;
    int nodeId;

    private static ArrayList<textViewArray> txViewArray = new ArrayList<>();
    private TextView txName1,txName2,txName3,txValue1,txValue2,txValue3;

    private ZwaveControlService zwaveService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        setTopLayout(true, "Sensor Manage");

        // bind service
        Intent serviceIntent = new Intent(this, ZwaveControlService.class);
        this.bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);

        initView();
        Intent intent = getIntent();
        nodeId = Integer.parseInt(intent.getStringExtra("NodeId"));
        nodeInfo = intent.getStringExtra("NodeInfoList");
        Log.i(LOG_TAG,"sensor nodeId = "+nodeId + " | nodeInfo = " +nodeInfo );

    }

    private void initView() {

        txName1 = (TextView) findViewById(R.id.txName1);
        txName2 = (TextView) findViewById(R.id.txName2);
        txName3 = (TextView) findViewById(R.id.txName3);
        txValue1 = (TextView) findViewById(R.id.txValue1);
        txValue2 = (TextView) findViewById(R.id.txValue2);
        txValue3 = (TextView) findViewById(R.id.txValue3);
    }

    //zwave callback result
    private void zwCBResult(String result) {

        if (Utils.isGoodJson(result)) {

            try {
                final JSONObject jsonObject = new JSONObject(result);

                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        /*
                        String messageType = jsonObject.optString("Node List Report");
                        if ("Interface Class".equals(messageType)) {
                            String sSupportCommandClass = jsonObject.optString("Interface Class");
                            Log.i(LOG_TAG,"support class = "+sSupportCommandClass);
                        }
                        */
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        zwaveService.unregister(mCallback);

        try {
            this.unbindService(conn);
        } catch (Exception e) {
            Log.d(LOG_TAG, e.toString());
        }
    }

    // bind service with zwave control service
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            zwaveService = ((ZwaveControlService.MyBinder) iBinder).getService();
            //register mCallback
            if (zwaveService != null) {
                zwaveService.register(mCallback);

                if (nodeInfo.contains("COMMAND_CLASS_BATTERY")) {
                    Log.i(LOG_TAG,"BATTTTTERY");
                    zwaveService.getPowerLevel(nodeId);
                    txName1.setText("Battery : ");
                    txViewArray.add(new textViewArray(txName1,txValue1,"COMMAND_CLASS_BATTERY"));
                }
                if (nodeInfo.contains("COMMAND_CLASS_SENSOR_BINARY")){
                    txName2.setText("Sensor Binary : ");
                    zwaveService.getSensorBasic(nodeId,0x0);
                    txViewArray.add(new textViewArray(txName2,txValue2,"COMMAND_CLASS_SENSOR_BINARY"));
                }
                if (nodeInfo.contains("COMMAND_CLASS_SENSOR_MULTILEVEL")){
                    txName3.setText("COMMAND_CLASS_SENSOR_MULTILEVEL : ");
                    try {
                        zwaveService.getSensorMultiLevel(nodeId);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    txViewArray.add(new textViewArray(txName3,txValue3,"COMMAND_CLASS_SENSOR_MULTILEVEL"));
                }
                if (nodeInfo.contains("COMMAND_CLASS_NOTIFICATION")){

                }

                zwaveService.getMeterSupported(nodeId);
                zwaveService.GetSensorBinarySupportedSensor(nodeId);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            zwaveService = null;
        }
    };

    public ZwaveControlService.zwaveCallBack mCallback = new ZwaveControlService.zwaveCallBack() {

        @Override
        public void zwaveControlResultCallBack(String className, String result) {

            if (className.equals("getSensorMultiLevel")){

                try {
                    final JSONObject jsonObject = new JSONObject(result);

                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String messageType = jsonObject.optString("MessageType");
                            if ("Sensor Information".equals(messageType)) {
                                String txType = jsonObject.optString("type");
                                String txUnit = jsonObject.optString("unit");
                                String txValue = jsonObject.optString("value");

                                txName3.setText(txType + " : ");
                                txValue3.setText(txValue + " "+txUnit);
                            }
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }else if (className.equals("getPowerLevel")) {

                int idx = txViewArray.indexOf("COMMAND_CLASS_BATTERY");
                try {
                    final JSONObject jsonObject = new JSONObject(result);

                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String messageType = jsonObject.optString("MessageType");
                            if ("Sensor Information".equals(messageType)) {
                                String txType = jsonObject.optString("type");
                                String txUnit = jsonObject.optString("unit");
                                String txValue = jsonObject.optString("value");

                                txName3.setText(txType + " : ");
                                txValue3.setText(txValue + " "+txUnit);
                            }
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private class textViewArray{
        private TextView name;
        private TextView value;
        private String cmdClass;

        public textViewArray(TextView name, TextView value,String cmdClass) {
            super();
            this.name = name;
            this.value = value;
            this.cmdClass = cmdClass;
        }
    }
}