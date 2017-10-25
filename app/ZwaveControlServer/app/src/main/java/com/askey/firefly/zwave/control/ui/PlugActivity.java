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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlugActivity extends BaseActivity implements View.OnClickListener {
    private static String LOG_TAG = PlugActivity.class.getSimpleName();
    private CheckBox powerSwitch,ledSwitch;
    private int nodeId;
    private TextView txSWMode,txParameter,txValue;
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
        nodeId = Integer.parseInt(intent.getStringExtra("NodeId"));
        Log.i(LOG_TAG,"Plug nodeId = "+nodeId);

        ledSwitch = (CheckBox) findViewById(R.id.led_switch);
        ledSwitch.setOnClickListener(this);
        powerSwitch = (CheckBox) findViewById(R.id.power_switch);
        powerSwitch.setOnClickListener(this);

    }

    private void initView() {

        txSWMode = (TextView) findViewById(R.id.txSWMode);
        txParameter = (TextView) findViewById(R.id.txParameter);
        txValue = (TextView) findViewById(R.id.txValue);
    }

    private Runnable getDevStatus = new Runnable() {
        @Override
        public void run() {
            zwaveService.getBasic(nodeId);
            zwaveService.getConfiguration(nodeId,1,1,7,7);
            zwaveService.getMeter(nodeId,0x02);
            zwaveService.getMeter(nodeId,0x00);

            zwaveService.getMeter(nodeId,0x04);
            zwaveService.getMeter(nodeId,0x05);
        }
    };

    //zwave callback result
    private void zwCBResult(String result) {

        if (Utils.isGoodJson(result)) {

            try {
                final JSONObject jsonObject = new JSONObject(result);
                String getNodeId = jsonObject.optString("Node id");
                if (getNodeId.equals(String.valueOf(nodeId))) {
                    String messageType = jsonObject.optString("MessageType");
                    if ("Basic Information".equals(messageType) || "Configuration Get Information".equals(messageType)
                            ||"Meter report Information".equals((messageType))){

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

                                    } else if ("Configuration Get Information".equals(messageType)) {

                                        String parameter = jsonObject.optString("Parameter number");
                                        String value = jsonObject.optString("Parameter value");
                                        Log.i(LOG_TAG, "*** Parameter = " + parameter + " | value = " + value);

                                        if (parameter.equals("7") && value.equals("1")) {
                                            ledSwitch.setChecked(true);
                                            txSWMode.setText("Show switch state");
                                        } else if (parameter.equals("7") && value.equals("2")) {
                                            ledSwitch.setChecked(false);
                                            txSWMode.setText("Show night mode");
                                        }

                                    } else if ("Meter report Information".equals((messageType))) {

                                        String txRateType = jsonObject.optString("Rate type");
                                        String txMeterReading = jsonObject.optString("Meter reading");
                                        String txUnit = jsonObject.optString("Meter unit");
                                        Log.i(LOG_TAG, "****** " + txRateType + " = " + txMeterReading + " " + txUnit);

                                        Pattern pattern = Pattern.compile("(（(\n|.)*?）)");
                                        Matcher matcher = pattern.matcher(txRateType);
                                        while(matcher.find()){
                                            txParameter.setText(matcher.group()+" :");
                                        }
                                        txValue.setText(txMeterReading + " "+txUnit);

                                    }
                            }
                        });
                    }
                }
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
                    //turn on
                    zwaveService.setBasic(nodeId,255);
                    zwaveService.getBasic(nodeId);
                } else {
                    //turn off
                    zwaveService.setBasic(nodeId,0);
                }
                break;
            case R.id.led_switch:
                if (ledSwitch.isChecked()) {
                    try {
                        zwaveService.setConfiguration(nodeId,7,1,0,1);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    txSWMode.setText("MODE : Show switch mode");

                } else {
                    try {
                        zwaveService.setConfiguration(nodeId,7,1,0,2);
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

                new Thread(getDevStatus).start();
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

        Log.i(LOG_TAG,"class name = "+className);

        if (className.equals("setConfiguration") || className.equals("getConfiguration")
            || className.equals("setSwitchMultiLevel") || className.equals("getSwitchMultiLevel")
            || className.equals("setBasic") || className.equals("getBasic")
            || className.equals("setSwitchAllOn") || className.equals("setSwitchAllOff")
            || className.equals("getMeter")){

            zwCBResult(result);

        }
        }
    };

}