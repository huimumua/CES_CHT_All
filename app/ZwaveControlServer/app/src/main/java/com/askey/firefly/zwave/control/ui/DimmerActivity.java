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
import android.widget.SeekBar;
import android.widget.TextView;

import com.askey.firefly.zwave.control.R;
import com.askey.firefly.zwave.control.service.ZwaveControlService;
import com.askey.firefly.zwave.control.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 项目名称：ZwaveControlClient
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2017/8/15 9:40
 * 修改人：skysoft
 * 修改时间：2017/8/15 9:40
 * 修改备注：
 */
public class DimmerActivity extends BaseActivity implements View.OnClickListener {
    private static String LOG_TAG = DimmerActivity.class.getSimpleName();
    private CheckBox cbSwitch, dimmerSwitch;
    private String nodeId;
    private TextView txBrightness;
    private SeekBar brightness_change;
    private int brightnessLevel;
    private boolean SeekBarFlg = false;

    private ZwaveControlService zwaveService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dimmer);

        setTopLayout(true,"Dimmer Manage");

        // bind service
        Intent serviceIntent = new Intent(this, ZwaveControlService.class);
        this.bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);

        initView();
        Intent intent = getIntent();
        nodeId = intent.getStringExtra("NodeId");
        Log.i(LOG_TAG,"Blub nodeId = "+nodeId);

        cbSwitch = (CheckBox) findViewById(R.id.cb_switch);
        cbSwitch.setOnClickListener(this);
        dimmerSwitch = (CheckBox) findViewById(R.id.dimmer_switch);
        dimmerSwitch.setOnClickListener(this);
    }

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
                zwaveService.setSwitchMultiLevel(Integer.parseInt(nodeId),brightnessLevel,1);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                zwaveService.getBasic(Integer.parseInt(nodeId));
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
        zwaveService.unregister(mCallback);

        try {
            this.unbindService(conn);
        } catch (Exception e) {
            Log.d(LOG_TAG, e.toString());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.cb_switch:
                if (cbSwitch.isChecked()) {
                  Log.i(LOG_TAG,"===setConfiguration===open===");
                    try {
                        zwaveService.setConfiguration("1",Integer.parseInt(nodeId),7,1,0,255);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.i(LOG_TAG,"===setConfiguration===close===");
                    try {
                        zwaveService.setConfiguration("1",Integer.parseInt(nodeId),7,1,0,0);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.dimmer_switch:
                if (dimmerSwitch.isChecked()) {
                    Log.i(LOG_TAG,"===setSwitchAllOn======");
                    zwaveService.setSwitchAllOn(Integer.parseInt(nodeId));
                    zwaveService.getBasic(Integer.parseInt(nodeId));
                } else {
                    Log.i(LOG_TAG,"===setSwitchAllOff======");
                    zwaveService.setSwitchAllOff(Integer.parseInt(nodeId));

                    txBrightness.setText("Brightness : 00h");
                    brightness_change.setProgress(0);
                    SeekBarFlg=false;
                }
                break;
            default:

                break;
        }
    }


    //zwave callback result
    private void zwCBResult(String result) {

        if (Utils.isGoodJson(result)) {

            try {
                final JSONObject jsonObject = new JSONObject(result);

                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String messageType = jsonObject.optString("MessageType");
                        if ("Basic Information".equals(messageType)) {
                            String value = jsonObject.optString("value");
                            txBrightness.setText("Brightness : " + value);
                            if (value.equals("00h")) {
                                //turn off
                                dimmerSwitch.setChecked(false);
                            } else {
                                //turn on
                                dimmerSwitch.setChecked(true);

                                //change Hex string to Interger
                                String tmpValue = value.substring(0,value.length()-1);
                                int setValue = Integer.valueOf(tmpValue,16);

                                if (!SeekBarFlg && value!="00h"){
                                    brightness_change.setProgress(setValue);
                                    SeekBarFlg = true;
                                }
                            }
                        }else if ("Configuration Get Information".equals(messageType)) {
                            String parameter = jsonObject.optString("Configuration parameter");
                            String value = jsonObject.optString("Configuration value");
                            if(parameter.equals("7") && value.equals("-1")){
                                cbSwitch.setChecked(true);
                            }else if(parameter.equals("7") && !value.equals("-1")){
                                cbSwitch.setChecked(false);
                            }
                        }
                    }
                });
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

                zwaveService.getConfiguration("1",Integer.parseInt(nodeId),1,0,1,10);
                zwaveService.getBasic(Integer.parseInt(nodeId));
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
                    || className.equals("setSwitchMultiLevel") ||className.equals("getSwitchMultiLevel")
                    || className.equals("setBasic") ||className.equals("getBasic")){

                zwCBResult(result);
            }
        }
    };

}
