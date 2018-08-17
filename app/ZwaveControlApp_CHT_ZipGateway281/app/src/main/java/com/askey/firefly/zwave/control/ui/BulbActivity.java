package com.askey.firefly.zwave.control.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.askey.firefly.zwave.control.ui.BaseActivity;
import com.askey.firefly.zwave.control.ui.ColorPickView;
import com.askey.firefly.zwave.control.R;
import com.askey.firefly.zwave.control.service.ZwaveControlService;
import com.askey.firefly.zwave.control.utils.Const;
import com.askey.firefly.zwave.control.utils.Utils;

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
public class BulbActivity extends BaseActivity implements View.OnClickListener {
    private static String LOG_TAG = BulbActivity.class.getSimpleName();
    private final String zwaveType = "Zwave";
    private CheckBox cbSwitch;
    private int nodeId,basicValue;
    private String nodeInfo;
    private TextView brightness;
    private SeekBar brightness_change;
    private int brightnessLevel = 0;

    private TextView txtColor;
    private ColorPickView myView;
    private LinearLayout Colorlayout;
    private RelativeLayout colorPickerLayout;
    private RadioGroup rGroup;

    private ZwaveControlService zwaveService;
    private static int colorSet = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bulb);
        setTopLayout(true, "Bulb Manage");

        initView();

        // bind service
        new Thread(bindzwaveservice).start();


        Intent intent = getIntent();
        nodeId = Integer.parseInt(intent.getStringExtra("NodeId"));
        nodeInfo = intent.getStringExtra("NodeInfoList");

        if (!nodeInfo.contains("COMMAND_CLASS_SWITCH_COLOR")){
            Log.i(LOG_TAG," not support color adjustment!");
        } else {
            Colorlayout.setVisibility(View.VISIBLE);
            enableColorPicker();
        }

        cbSwitch = (CheckBox) findViewById(R.id.cb_switch);
        cbSwitch.setSelected(true);
        cbSwitch.requestFocus();
        cbSwitch.setOnClickListener(this);
    }

    public Runnable bindzwaveservice = new Runnable() {
        @Override
        public void run() {
            Intent serviceIntent = new Intent(BulbActivity.this, ZwaveControlService.class);
            bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);
        }
    };

    private RadioGroup.OnCheckedChangeListener listener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            // TODO Auto-generated method stub
            int count = group.getChildCount();
            switch (checkedId) {
                case R.id.warmWhite:
                    //set the color to warmwhite
                    warmWhite();
                    colorPickerLayout.setVisibility(View.GONE);
                    colorSet = 0;
                    break;
                case R.id.coldWite:
                    //disable warmwhite color
                    coldWite();
                    colorPickerLayout.setVisibility(View.GONE);
                    colorSet = 1;
                    break;
                case R.id.RGBColor:
                    // enable color picker view
                    colorPickerLayout.setVisibility(View.VISIBLE);
                    colorSet = 2;
                    break;
            }
        }
    };

    private void initView() {

        Colorlayout = (LinearLayout) findViewById(R.id.colorLinear);
        colorPickerLayout = (RelativeLayout) findViewById(R.id.colorPickerLayout);
        myView = (ColorPickView) findViewById(R.id.color_picker_view);
        txtColor = (TextView) findViewById(R.id.txt_color);

        brightness = (TextView) findViewById(R.id.brightness);
        brightness_change = (SeekBar) findViewById(R.id.brightness_change);

        rGroup = (RadioGroup) findViewById(R.id.radiogroup);
        rGroup.setOnCheckedChangeListener(listener);

        brightness_change.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                brightnessLevel = i*10;

                setSwitchMultiLevel();
                if (brightnessLevel!=0 && cbSwitch.isChecked()==false){
                    cbSwitch.setChecked(true);
                }else if (brightnessLevel==0){
                    cbSwitch.setChecked(false);
                }
                brightness.setText("Brightness : "+brightnessLevel+" %");
                basicValue = brightnessLevel;

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //zwaveService.setSwitchMultiLevel(zwaveType, nodeId, brightnessLevel, 1);
                setSwitchMultiLevel();
                if (brightnessLevel!=0 && cbSwitch.isChecked()==false){
                    cbSwitch.setChecked(true);
                }
                brightness.setText("Brightness : "+brightnessLevel+" %");
                basicValue = brightnessLevel;

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void enableColorPicker(){

        myView.setOnColorChangedListener(new ColorPickView.OnColorChangedListener() {

            @Override
            public void onColorChange(int color) {
                txtColor.setTextColor(color);
                setLampColor(Color.red(color),Color.green(color),Color.blue(color));
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
        switch (v.getId()) {

            case R.id.cb_switch:
                if (cbSwitch.isChecked()) {
                    if (basicValue ==0) {
                        turnOn();
                    }else {
                        setSwitchMultiLevel();
                    }
                    getSwitchMultiLevel();
                } else {
                    turnOff();
                    brightness.setText("Brightness : 0 %");
                    brightness_change.setProgress(0);
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

            //Log.e(LOG_TAG,"mCallBack = "+className+" | result = "+result);

        if (className.equals("setBasic")|| className.equals("getBasic")
            || className.equals("setSwitchAllOn") ||className.equals("setSwitchAllOn")
            || className.equals("getLampColor") || className.equals("setLampColor")
            || className.equals("setConfiguration") || className.equals("getConfiguration")
            || className.equals("setSwitchMultiLevel") || className.equals("getSwitchMultiLevel") ){

            //zwCBResult(result);
            if (Utils.isGoodJson(result)) {
                ((Activity) mContext).runOnUiThread(new BulbActivity.CallbackRunnable(nodeId, result));
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
                final int getNodeId = jsonObject.optInt("Node id");
                final int getNode = jsonObject.optInt("Node");

                if (getNodeId == nodeId || getNode == nodeId){

                    String Interface = jsonObject.optString("Interface");

                    if ("getBrightness".equals(Interface)) {
                        String switchStatus = jsonObject.optString("switchStatus");
                        if (switchStatus.equals("off")) {
                            //turn off
                            cbSwitch.setChecked(false);
                        } else {
                            //turn on
                            cbSwitch.setChecked(true);
                        }

                        String value = jsonObject.optString("brightness");
                        brightness.setText("Brightness : " + value +" %");
                        basicValue = Integer.valueOf(value);
                        brightness_change.setProgress((Integer.valueOf(value))/10);
                    }
                    else if ("getLampColor".equals(Interface)) {
                        String txParamater = jsonObject.optString("component id");
                        String txValue = jsonObject.optString("value");

                        Log.i(LOG_TAG, "Parameter = " + txParamater + " | value = " + txValue);

                        if (txParamater.equals("Warm Write") && (txValue.equals("255"))) {
                            colorSet = 0;
                            ((RadioButton)rGroup.findViewById(R.id.warmWhite)).setChecked(true);
                        } else if (txParamater.equals("Cold Write") && (txValue.equals("255")) && colorSet!=0) {
                            ((RadioButton)rGroup.findViewById(R.id.coldWite)).setChecked(true);
                            colorSet = 1;
                        } else if (!txValue.equals("0") && colorSet>1) {
                            ((RadioButton)rGroup.findViewById(R.id.RGBColor)).setChecked(true);
                            colorPickerLayout.setVisibility(View.VISIBLE);
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

            getSwitchMultiLevel();
            params[0].getLampColor(zwaveType,nodeId);

            try {
                params[0].setConfiguration(nodeId,1,1,0,1);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
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

    private void setSwitchMultiLevel() {
        new Thread(){
            @Override
            public void run(){
                zwaveService.setSwitchMultiLevel(zwaveType, nodeId, brightnessLevel, 1);
            }
        }.start();
    }

    private void coldWite() {
        new Thread(){
            @Override
            public void run(){
                zwaveService.setLampToColdWhite(zwaveType,nodeId);
            }
        }.start();
    }

    private void warmWhite() {
        new Thread(){
            @Override
            public void run(){
                zwaveService.setLampToWarmWhite(zwaveType,nodeId);
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

    private void setLampColor(final int red, final int green, final int blue) {
        new Thread(){
            @Override
            public void run(){
                zwaveService.setLampColor(zwaveType,nodeId,red,green,blue);
            }
        }.start();
    }
}