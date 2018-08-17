package com.askey.firefly.zwave.control.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.askey.firefly.zwave.control.R;
import com.askey.firefly.zwave.control.service.ZwaveControlService;
import com.askey.firefly.zwave.control.utils.Const;
import com.askey.firefly.zwave.control.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ControlActivity extends BaseActivity {
    private static String LOG_TAG = ControlActivity.class.getSimpleName();
    private String nodeInfo;
    int nodeId;

    private static ArrayList<textViewArray> txViewArray = new ArrayList<>();
    private TextView txNodeId,txMaxGroup;
    private EditText edGroupID,edEndpointItfId,edEndpointId;
    private Button btSet;

    private ZwaveControlService zwaveService;
    private JSONObject jsonObject = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        setTopLayout(true, "Sensor Manage");

        // bind service
        new Thread(bindzwaveservice).start();


        initView();
        Intent intent = getIntent();
        nodeId = Integer.parseInt(intent.getStringExtra("NodeId"));


        txNodeId.setText(intent.getStringExtra("NodeId"));

        //zwaveService.getMaxSupportedGroups(nodeId,0);

        nodeInfo = intent.getStringExtra("NodeInfoList");

        try {
            jsonObject = new JSONObject(nodeInfo);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        btSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int GropuId = Integer.valueOf(edGroupID.getText().toString());
                int arrEnd = Integer.valueOf(edEndpointItfId.getText().toString());
                int EndpointId = Integer.valueOf(edEndpointId.getText().toString());

                int arr[] = {arrEnd,0};

                addEndpointsToGroup(GropuId,arr,EndpointId);

            /*
                java.util.Date date = new java.util.Date();
                long datetime = date.getTime();
                Log.i(LOG_TAG,"2current timestamp = "+datetime);
            */
            }
        });

    }

    public Runnable bindzwaveservice = new Runnable() {
        @Override
        public void run() {

            Intent serviceIntent = new Intent(ControlActivity.this, ZwaveControlService.class);
            bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);
        }
    };

    private void initView() {
        txNodeId = (TextView) findViewById(R.id.txNodeId);
        txMaxGroup = (TextView) findViewById(R.id.txMaxGroup);

        edGroupID = (EditText) findViewById(R.id.edGroupId);
        edEndpointItfId = (EditText) findViewById(R.id.edArr);
        edEndpointId = (EditText) findViewById(R.id.edEndpoint);

        btSet = (Button) findViewById(R.id.btnSet);
    }

    //zwave callback result
    private void zwCBResult(String result) {

        if (Utils.isGoodJson(result)) {

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

            zwaveService = ((ZwaveControlService.MyBinder) iBinder).getService();
            //register mCallback
            if (zwaveService != null) {
                zwaveService.register(mCallback);

                /*
                if (nodeInfo.contains("COMMAND_CLASS_BATTERY")) {
                    Log.i(LOG_TAG, "BATTERY");
                    zwaveService.getDeviceBattery(nodeId);
                    //txViewArray.add(new textViewArray(txName1,txValue1,"COMMAND_CLASS_BATTERY"));
                }
                //if (nodeInfo.contains("COMMAND_CLASS_SENSOR_BINARY")) {
                //Water
                //zwaveService.getSensorBasic(Integer.parseInt(nodeId), 0x06);
                //Temperature
                //zwaveService.getSensorBasic(Integer.parseInt(nodeId), 0x01);
                //Humidity
                //zwaveService.getSensorBasic(Integer.parseInt(nodeId), 0x05);
                //txViewArray.add(new textViewArray(txName2,txValue2,"COMMAND_CLASS_SENSOR_BINARY"));
                //}
                if (nodeInfo.contains("COMMAND_CLASS_NOTIFICATION")) {
                    try {
                        //JSONObject jsonObject = new JSONObject(nodeInfo);
                        if (jsonObject.getString("Product id").equals("001F")) {
                            //Water
                            zwaveService.getSensorNotification(nodeId, 0x00, 0x05, 0x00);
                        } else if (jsonObject.getString("Product id").equals("000C")) {
                            //Motion
                            zwaveService.getSensorNotification(nodeId, 0x00, 0x07, 0x00);
                            //Door/Window
                            zwaveService.getSensorNotification(nodeId, 0x00, 0x06, 0x00);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (nodeInfo.contains("COMMAND_CLASS_SENSOR_MULTILEVEL")){
                    try {
                        zwaveService.getSensorMultiLevel(nodeId);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    //txViewArray.add(new textViewArray(txName3,txValue3,"COMMAND_CLASS_SENSOR_MULTILEVEL"));
                }

                zwaveService.getMeterSupported(nodeId);
                zwaveService.GetSensorBinarySupportedSensor(nodeId);

                */
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
            Log.d("className", className);
            Log.d("result", result);

            ((Activity) mContext).runOnUiThread(new CallbackRunnable(nodeId, result));
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

    private class CallbackRunnable implements Runnable {

        private int nodeId;
        private String result;

        public CallbackRunnable(int nodeId, String result) {
            this.nodeId = nodeId;
            this.result = result;
        }

        @Override
        public void run() {
            try {
                //Gson gson = new Gson();
                //ArrayList messageList = gson.fromJson(result, new TypeToken<ArrayList>(){}.getType());

                JSONObject jsonObject = new JSONObject(result);
                String messageType = jsonObject.optString("MessageType");
                int nodeId = jsonObject.optInt("Node id");
                if (this.nodeId == nodeId) {
                    /*
                    if (messageType.equals("Sensor Info Report")) {
                        String txType = jsonObject.optString("type");
                        String txValue = jsonObject.optString("value");
                        if (txType.equals("Temperature sensor")) {
                            float temper = (Float.valueOf(txValue) - 32) * 5 / 9;
                            txValue1.setText(String.format("%.02f", temper) + " " + (char) 0x00B0 + "C");
                        } else if (txType.equals("Relative humidity sensor")) {
                            txValue2.setText(txValue + " %");
                        } else if (txType.equals("Luminance sensor")) {
                            txValue3.setText(txValue + " %");
                        }
                    }
                    if (messageType.equals("Notification Get Information")) {
                        String notificationType = jsonObject.optString("Notification-type");
                        String notificationEvent = jsonObject.optString("Notification-event");
                        if (notificationType.equals("Water alarm")) {
                            if (notificationEvent.contains("detected")){
                                txValue4.setText("Water leak detected");
                            }
                            else{
                                txValue4.setText("State idle");
                            }

                        } else if (notificationType.equals("Home security")) {
                            if (!notificationEvent.equals("Tampering. Product covering removed")) {
                                txValue5.setText(notificationEvent);
                            }
                        } else if (notificationType.equals("Access control")) {
                            txValue6.setText(notificationEvent);
                        } else if (notificationType.equals("Smoke alarm")) {
                            txValue6.setText(notificationEvent);
                        }
                    }
                    if (messageType.equals("Node Battery Value")) {
                        String batteryValue = jsonObject.optString("Battery Value");
                        txValue7.setText(batteryValue + " %");
                    }
                    */
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

    private void addEndpointsToGroup(final int GropuId, final int[] arr, final int EndpointId) {
        new Thread(){
            @Override
            public void run(){
                zwaveService.addEndpointsToGroup(Const.zwaveType,nodeId,GropuId,arr,EndpointId);
            }
        }.start();
    }
}