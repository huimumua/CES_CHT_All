package com.askey.firefly.zwave.control.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
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

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlugActivity extends BaseActivity implements View.OnClickListener {
    private static String LOG_TAG = PlugActivity.class.getSimpleName();
    private CheckBox powerSwitch,ledSwitch;
    private int nodeId;
    private TextView txSWMode,txParameter,txValue,txParameter1,txValue1;
    private ZwaveControlService zwaveService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plug);

        setTopLayout(true, "Plug Manage");

        initView();

        // bind service
        Intent serviceIntent = new Intent(this, ZwaveControlService.class);
        this.bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);

        Intent intent = getIntent();
        nodeId = Integer.parseInt(intent.getStringExtra("NodeId"));
        Log.i(LOG_TAG,"Plug nodeId = "+nodeId);
    }

    private void initView() {
        ledSwitch = (CheckBox) findViewById(R.id.led_switch);
        ledSwitch.setOnClickListener(this);
        powerSwitch = (CheckBox) findViewById(R.id.power_switch);
        powerSwitch.setOnClickListener(this);
        txSWMode = (TextView) findViewById(R.id.txSWMode);
        txParameter = (TextView) findViewById(R.id.txParameter);
        txValue = (TextView) findViewById(R.id.txValue);
        txParameter1 = (TextView) findViewById(R.id.txParameter1);
        txValue1 = (TextView) findViewById(R.id.txValue1);
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

                new initDeviceTask().execute(zwaveService);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            zwaveService = null;
        }
    };

    private ZwaveControlService.zwaveCallBack mCallback = new ZwaveControlService.zwaveCallBack() {

        @Override
        public void zwaveControlResultCallBack(String className, String result) {

            Log.i(LOG_TAG, "class name = " + className);

            if (className.equals("setConfiguration") || className.equals("getConfiguration")
                    || className.equals("setSwitchMultiLevel") || className.equals("getSwitchMultiLevel")
                    || className.equals("setBasic") || className.equals("getBasic")
                    || className.equals("setSwitchAllOn") || className.equals("setSwitchAllOff")
                    || className.equals("getMeter")) {

                if (Utils.isGoodJson(result)) {
                    ((Activity) mContext).runOnUiThread(new PlugActivity.CallbackRunnable(nodeId, result));
                }

            }
        }
    };

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
                final JSONObject jsonObject = new JSONObject(result);
                String getNodeId = jsonObject.optString("Node id");
                if (getNodeId.equals(String.valueOf(nodeId))) {

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
                        if (txUnit.equals("W")) {
                            while (matcher.find()) {
                                txParameter.setText(matcher.group() + " :");
                            }
                            txValue.setText(txMeterReading + " " + txUnit);
                        } else if (txUnit.equals("KWh")) {
                            while (matcher.find()) {
                                txParameter1.setText(matcher.group() + " :");
                            }
                            txValue1.setText(txMeterReading + " " + txUnit);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class initDeviceTask extends AsyncTask<ZwaveControlService, Void, Void> {

        @Override
        protected Void doInBackground(ZwaveControlService... params) {
            params[0].getBasic(nodeId);
            params[0].getMeter(nodeId,0x02);
            params[0].getMeter(nodeId,0x00);
            params[0].getConfiguration(nodeId,0,7,0,0);
            return null;
        }
    }
}