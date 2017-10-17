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
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.askey.firefly.zwave.control.R;
import com.askey.firefly.zwave.control.service.ZwaveControlService;
import com.askey.firefly.zwave.control.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class PlugActivity extends BaseActivity implements View.OnClickListener {
    private static String LOG_TAG = PlugActivity.class.getSimpleName();
    private CheckBox powerSwitch,ledSwitch;
    private String nodeId;
    private TextView txSWMode;
    private int brightnessLevel = 0;

    private ZwaveControlService zwaveService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plug);

        setTopLayout(true, "Plug Manage");

        // bind service
        Intent serviceIntent = new Intent(this, ZwaveControlService.class);
        this.bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);

        initView();
        Intent intent = getIntent();
        nodeId = intent.getStringExtra("NodeId");
        Log.i(LOG_TAG,"Plug nodeId = "+nodeId);

        ledSwitch = (CheckBox) findViewById(R.id.led_switch);
        ledSwitch.setOnClickListener(this);
        powerSwitch = (CheckBox) findViewById(R.id.power_switch);
        powerSwitch.setOnClickListener(this);

    }

    private void initView() {

        txSWMode = (TextView) findViewById(R.id.txSWMode);

    }

    //zwave callback result
    private void zwCBResult(String result) {

        Log.i(LOG_TAG,"result = "+result);

        if (Utils.isGoodJson(result)) {

            try {
                final JSONObject jsonObject = new JSONObject(result);

                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String messageType = jsonObject.optString("MessageType");
                        if ("Basic Information".equals(messageType)) {
                            String value = jsonObject.optString("value");
                            if (value.equals("00h")) {
                                //turn off
                                powerSwitch.setChecked(false);
                            } else {
                                //turn on
                                powerSwitch.setChecked(true);
                            }
                        }else if ("Configuration Get Information".equals(messageType)){
                            String parameter = jsonObject.optString("Configuration parameter");
                            String value = jsonObject.optString("Configuration value");
                            Log.i(LOG_TAG,"*** Parameter = "+parameter+" | value = "+value);
                            if(parameter.equals("7") && value.equals("1")){
                                ledSwitch.setChecked(true);
                                txSWMode.setText("Show switch stste");
                            }else if(parameter.equals("7") && value.equals("2")){
                                ledSwitch.setChecked(false);
                                txSWMode.setText("Show night mode");
                            }
                        }else if ("Meter report Information".equals((messageType))){
                            String meterValue = jsonObject.optString("Meter reading");
                            Log.i(LOG_TAG,"meter reading = "+meterValue);
                        }
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


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.power_switch:
                if (powerSwitch.isChecked()) {
                    Log.i(LOG_TAG, "===setSwitchAllOn======");
                    //zwaveService.setBasic(Integer.parseInt(nodeId),255);
                    zwaveService.setSwitchAllOn(Integer.parseInt(nodeId));
                    zwaveService.getBasic(Integer.parseInt(nodeId));
                } else {
                    Log.i(LOG_TAG, "===setSwitchAllOff======");
                    //zwaveService.setBasic(Integer.parseInt(nodeId),0);
                    zwaveService.setSwitchAllOff(Integer.parseInt(nodeId));
                }
                break;
            case R.id.led_switch:
                if (ledSwitch.isChecked()) {
                    try {
                        zwaveService.setConfiguration("1",Integer.parseInt(nodeId),7,1,0,1);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    txSWMode.setText("MODE : Show switch mode");

                } else {
                    try {
                        zwaveService.setConfiguration("1",Integer.parseInt(nodeId),7,1,0,2);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    txSWMode.setText("MODE : Show night status");
                }
                break;
            default:
                break;
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

                Log.i(LOG_TAG,"GET PARAMETER");
                zwaveService.getBasic(Integer.parseInt(nodeId));
                zwaveService.getConfiguration("1",Integer.parseInt(nodeId),1,1,7,7);
                zwaveService.getMeter(Integer.parseInt(nodeId),2);
                zwaveService.getMeter(Integer.parseInt(nodeId),0);

                zwaveService.getMeter(Integer.parseInt(nodeId),4);
                zwaveService.getMeter(Integer.parseInt(nodeId),5);
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

            if (className.equals("setConfiguration") || className.equals("getConfiguration")
                    || className.equals("setSwitchMultiLevel") || className.equals("getSwitchMultiLevel")
                    || className.equals("setBasic") || className.equals("getBasic")
                    || className.equals("setSwitchAllOn") || className.equals("setSwitchAllOff")
                    || className.equals("getMeter") ){

                zwCBResult(result);
            }
        }
    };

}