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
public class BulbActivity extends BaseActivity implements View.OnClickListener {
    private static String LOG_TAG = BulbActivity.class.getSimpleName();
    private CheckBox cbSwitch;
    private String nodeId;
    private TextView brightness;
    private SeekBar brightness_change;
    private int brightnessLevel = 0;
    private boolean SeekBarFlg = false;

    private ZwaveControlService zwaveService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bulb);

        setTopLayout(true, "Bulb Manage");

        // bind service
        Intent serviceIntent = new Intent(this, ZwaveControlService.class);
        this.bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);

        initView();
        Intent intent = getIntent();
        nodeId = intent.getStringExtra("NodeId");
        Log.i(LOG_TAG,"Blub nodeId = "+nodeId);

        cbSwitch = (CheckBox) findViewById(R.id.cb_switch);
        cbSwitch.setOnClickListener(this);
    }

    private void initView() {

        brightness = (TextView) findViewById(R.id.brightness);
        brightness_change = (SeekBar) findViewById(R.id.brightness_change);

        brightness_change.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                brightnessLevel = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                zwaveService.setSwitchMultiLevel(Integer.parseInt(nodeId), brightnessLevel, 1);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                zwaveService.getBasic(Integer.parseInt(nodeId));
            }
        });
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
                            brightness.setText("Brightness : " + value);
                            if (value.equals("00h")) {
                                //turn off
                                cbSwitch.setChecked(false);
                            } else {
                                //turn on
                                cbSwitch.setChecked(true);

                                //change Hex string to Interger
                                String tmpValue = value.substring(0,value.length()-1);
                                int setValue = Integer.valueOf(tmpValue,16);

                                if (!SeekBarFlg && value!="00h"){
                                    brightness_change.setProgress(setValue);
                                    SeekBarFlg = true;
                                }
                            }

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

            case R.id.cb_switch:
                if (cbSwitch.isChecked()) {
                    Log.i(LOG_TAG, "===setSwitchAllOn======");
                    zwaveService.setSwitchAllOn(Integer.parseInt(nodeId));
                    zwaveService.getBasic(Integer.parseInt(nodeId));
                } else {
                    Log.i(LOG_TAG, "===setSwitchAllOff======");
                    zwaveService.setSwitchAllOff(Integer.parseInt(nodeId));
                    brightness.setText("Brightness : 00h");
                    brightness_change.setProgress(0);
                    SeekBarFlg=false;
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

            if (className.equals("setSwitchMultiLevel") || className.equals("setBasic")
                || className.equals("getBasic") ||className.equals("setSwitchAllOn")
                || className.equals("setSwitchAllOff") ||className.equals("replaceFailedDevice")){

                zwCBResult(result);
            }
        }
    };

}