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
import android.widget.SeekBar;
import android.widget.TextView;

import com.askey.firefly.zwave.control.R;
import com.askey.firefly.zwave.control.service.ZwaveControlService;
import com.askey.firefly.zwave.control.utils.Const;
import com.askey.firefly.zwave.control.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 项目名称：ZwaveControlClient
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2017/8/15 9:40
 * 修改人：chiapin
 * 修改时间：2017/10/20
 * 修改备注：
 */
public class DimmerActivity extends BaseActivity implements View.OnClickListener {
    private static String LOG_TAG = DimmerActivity.class.getSimpleName();
    private CheckBox cbSwitch, dimmerSwitch;
    private int nodeId;
    private TextView txBrightness;
    private SeekBar brightness_change;
    private int brightnessLevel;

    private ZwaveControlService zwaveService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dimmer);

        setTopLayout(true,"Dimmer Manage");

        // bind service
        new Thread(bindzwaveservice).start();


        initView();
        Intent intent = getIntent();
        nodeId = Integer.parseInt(intent.getStringExtra("NodeId"));

        cbSwitch = (CheckBox) findViewById(R.id.cb_switch);
        cbSwitch.setOnClickListener(this);
        dimmerSwitch = (CheckBox) findViewById(R.id.dimmer_switch);
        dimmerSwitch.setOnClickListener(this);
    }

    public Runnable bindzwaveservice = new Runnable() {
        @Override
        public void run() {

            Intent serviceIntent = new Intent(DimmerActivity.this, ZwaveControlService.class);
            bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);
        }
    };

    private void initView() {
        txBrightness = (TextView) findViewById(R.id.txBrightness);
        brightness_change = (SeekBar) findViewById(R.id.brightness_change);
        brightness_change.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                brightnessLevel = i ;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                zwaveService.setSwitchMultiLevel(Const.zwaveType,nodeId,brightnessLevel,1);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                zwaveService.getBasic(Const.zwaveType,nodeId);
            }

        });

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
        switch (v.getId()){
            case R.id.cb_switch:
                if (cbSwitch.isChecked()) {
                    Log.i(LOG_TAG,"===setConfiguration===open===");
                    setConfiguration(255);
                } else {
                    Log.i(LOG_TAG, "===setConfiguration===close===");
                    setConfiguration(0);
                }
                break;
            case R.id.dimmer_switch:
                if (dimmerSwitch.isChecked()) {
                    //turn on
                   turnOn();
                } else {
                    //turn off
                   turnOff();
                   txBrightness.setText("Brightness : 0 %");
                   brightness_change.setProgress(0);
                }
                break;

            default:
                break;
        }
    }

    /*private Runnable getDevStatus = new Runnable() {
        @Override
        public void run() {
            zwaveService.getBasic(nodeId);

            zwaveService.getConfiguration(nodeId,1,0,1,10);
            zwaveService.getBasic(nodeId);
        }
    };*/

    //zwave callback result
    private void zwCBResult(String result) {

        if (Utils.isGoodJson(result)) {

            try {
                final JSONObject jsonObject = new JSONObject(result);
                final int getNodeId = jsonObject.optInt("Node id");

                if (getNodeId == nodeId){

                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            String messageType = jsonObject.optString("MessageType");
                            if ("Basic Information".equals(messageType)) {

                                String value = jsonObject.optString("value");
                                txBrightness.setText("Brightness : " + Integer.parseInt(value.substring(0,2), 16) +" %");

                                if (value.equals("00h")) {
                                    //turn off
                                    dimmerSwitch.setChecked(false);
                                } else {
                                    //turn on
                                    dimmerSwitch.setChecked(true);

                                    //change Hex string to Interger
                                    String tmpValue = value.substring(0,value.length()-1);
                                    int setValue = Integer.valueOf(tmpValue,16);

                                    brightness_change.setProgress(setValue);

                                }
                            }else if ("Configuration Get Information".equals(messageType)) {

                                String parameter = jsonObject.optString("Parameter number");
                                String value = jsonObject.optString("Parameter value");

                                if(parameter.equals("7") && value.equals("-1")){
                                    cbSwitch.setChecked(true);
                                }else if(parameter.equals("7") && !value.equals("-1")){
                                    cbSwitch.setChecked(false);
                                }
                            }
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
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

                //new Thread(getDevStatus).start();
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

        if (className.equals("setConfiguration") || className.equals("getConfiguration")
            || className.equals("setSwitchMultiLevel") ||className.equals("getSwitchMultiLevel")
            || className.equals("setBasic") ||className.equals("getBasic")){

            zwCBResult(result);
        }
        }
    };

    private class initDeviceTask extends AsyncTask<ZwaveControlService, Void, Void> {

        @Override
        protected Void doInBackground(ZwaveControlService... params) {
            params[0].getBasic(Const.zwaveType,nodeId);
            //zwaveService.getConfiguration(nodeId,1,0,1,10);
            params[0].getConfiguration(nodeId,0,7,0,0);
            params[0].getBasic(Const.zwaveType,nodeId);
            return null;
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

    private void getSwitchMultiLevel() {
        new Thread(){
            @Override
            public void run(){
                zwaveService.getSwitchMultiLevel(Const.zwaveType,nodeId);
            }
        }.start();
    }


    private void turnOn() {
        new Thread(){
            @Override
            public void run(){
                zwaveService.setBasic(Const.zwaveType,nodeId,255);
                zwaveService.getBasic(Const.zwaveType,nodeId);
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

    private void setConfiguration(final int value) {
        new Thread(){
            @Override
            public void run(){
                try {
                    zwaveService.setConfiguration(nodeId,7,1,0,value);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
