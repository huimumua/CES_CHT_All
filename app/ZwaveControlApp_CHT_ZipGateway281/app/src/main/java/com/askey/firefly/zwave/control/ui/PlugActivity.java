package com.askey.firefly.zwave.control.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.askey.firefly.zwave.control.R;
import com.askey.firefly.zwave.control.service.ZwaveControlService;
import com.askey.firefly.zwave.control.utils.Const;
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
        new Thread(bindzwaveservice).start();


        Intent intent = getIntent();
        nodeId = Integer.parseInt(intent.getStringExtra("NodeId"));
        Log.i(LOG_TAG,"Plug nodeId = "+nodeId);
    }

    public Runnable bindzwaveservice = new Runnable() {
        @Override
        public void run() {
            Intent serviceIntent = new Intent(PlugActivity.this, ZwaveControlService.class);
            bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);
        }
    };

    private void initView() {
        ledSwitch = (CheckBox) findViewById(R.id.led_switch);
        ledSwitch.setOnClickListener(this);
        powerSwitch = (CheckBox) findViewById(R.id.power_switch);
        powerSwitch.setOnClickListener(this);
        powerSwitch.setSelected(true);
        powerSwitch.requestFocus();
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
        zwaveUnregister();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.power_switch:
                if (powerSwitch.isChecked()) {
                    //turn on
                    turnOn();
                } else {
                    //turn off
                    turnOff();
                }
                break;
            case R.id.led_switch:
                if (ledSwitch.isChecked()) {
                    setConfiguration(7,1,0,1);
                    txSWMode.setText("MODE : Show switch mode");
                } else {
                    setConfiguration(7,1,0,2);
                    txSWMode.setText("MODE : Show night status");
                }
                break;
            default:
                break;
        }
    }

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    powerSwitch.setChecked(true);
                    break;
                case 2:
                    powerSwitch.setChecked(false);
                    break;
            }
        }
    };


    // bind service with zwave control service
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            zwaveService = ((ZwaveControlService.MyBinder) iBinder).getService();
            //register mCallback
            if (zwaveService != null) {
                zwaveService.register(mCallback);
                //new initDeviceTask().execute(zwaveService);
                getBasic();

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

            Log.i(LOG_TAG, "class name = " + className + " | reuslt =" + result);
            if(className.equals("getBasic")) {
                if (result.contains("on")) {
                    switchStatus(1);
                } else {
                    switchStatus(2);
                }
            }
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
                String getNode = jsonObject.optString("Node");
                String getNodeId = jsonObject.optString("Node id");
                if (getNodeId.equals(String.valueOf(nodeId))|| getNode.equals(String.valueOf(nodeId))) {

                    String messageType = jsonObject.optString("Interface");
                    if ("getSwitchStatus".equals(messageType)) {
                        String value = jsonObject.optString("switchStatus");
                        if (value.equals("off")) {
                            //turn off
                            powerSwitch.setChecked(false);
                        } else {
                            //turn on
                            powerSwitch.setChecked(true);
                        }
                    } else if ("Configuration Get Information".equals(messageType)) {

                        int parameter = jsonObject.optInt("Parameter number");
                        String value = jsonObject.optString("Parameter value");
                        Log.i(LOG_TAG, "*** Parameter = " + parameter + " | value = " + value);

                        if (parameter == 7 && value.equals("1")) {
                            ledSwitch.setChecked(true);
                            txSWMode.setText("MODE : Show switch mode");
                        } else if (parameter == 7 && value.equals("2")) {
                            ledSwitch.setChecked(false);
                            txSWMode.setText("MODE : Show night status");
                        }

                    } else if ("getPower".equals((messageType))) {

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
            params[0].getBasic(Const.zwaveType,nodeId);
            params[0].getConfiguration(nodeId,0,7,0,0);
            params[0].getMeter(Const.zwaveType,nodeId,0x02);
            params[0].getMeter(Const.zwaveType,nodeId,0x04);
            params[0].getMeter(Const.zwaveType,nodeId,0x00);
            return null;
        }
    }

    private void switchStatus(final int value) {
        new Thread(){
            @Override
            public void run(){
                switch (value){
                    case 1:
                        Log.d(LOG_TAG,"status on");
                        mHandler.sendEmptyMessage(1);
                        break;
                    case 2:
                        Log.d(LOG_TAG,"status off");
                        mHandler.sendEmptyMessage(2);
                        break;
                }
            }
        }.start();
    }


    private void getBasic() {
        new Thread(){
            @Override
            public void run(){
                if(zwaveService != null)
                    zwaveService.getBasic(Const.zwaveType,nodeId);
            }
        }.start();
    }


    private void turnOn() {
        new Thread(){
            @Override
            public void run(){
                zwaveService.setBasic(Const.zwaveType,nodeId,255);
            }
        }.start();
    }

    private void turnOff() {
        new Thread(){
            @Override
            public void run(){
                zwaveService.setBasic(Const.zwaveType,nodeId,0);
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

    private void setConfiguration(final int number, final int size, final int useDefault, final int value) {
        new Thread(){
            @Override
            public void run(){
                try {
                    zwaveService.setConfiguration(nodeId,number,size,useDefault,value);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}