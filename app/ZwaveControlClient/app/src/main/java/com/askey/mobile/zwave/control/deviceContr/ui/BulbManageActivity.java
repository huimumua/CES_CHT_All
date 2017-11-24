package com.askey.mobile.zwave.control.deviceContr.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.application.ZwaveClientApplication;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.data.CloudIotData;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.IotMqttManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.IotMqttMessageCallback;
import com.askey.mobile.zwave.control.deviceContr.model.DeviceList;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.home.activity.HomeActivity;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;
import com.askeycloud.webservice.sdk.iot.MqttService;
import com.askeycloud.webservice.sdk.iot.callback.ShadowReceiveListener;
import com.askeycloud.webservice.sdk.iot.message.builder.MqttDesiredJStrBuilder;
import com.askeycloud.webservice.sdk.service.iot.AskeyIoTService;

import org.eclipse.paho.client.mqttv3.MqttMessage;
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
public class BulbManageActivity extends BaseActivity implements View.OnClickListener {
    public static String TAG = "BulbManageActivity";
    private CheckBox cbSwitch;
    private DeviceList.NodeInfoList nodeInfoList;
    private String nodeId,uniqueid;
    private Button btnGetPower,btnStartChange;
    private TextView tvPowerLevel,tvProgress,brightness;
    private EditText etDuration,etSecChangeDir,etSecStep;
    private SeekBar lightLevel,brightness_change;
    private RadioButton dim,brighten;
    private int brightnessLevel = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bulb);

        setTopLayout(true,"Bulb Manage",false);

        initView();
        Intent intent = getIntent();
        nodeInfoList = intent.getParcelableExtra("nodeInfoList");
        if (nodeInfoList != null) {
            nodeId = nodeInfoList.getNodeId();
        }else{
            nodeId = intent.getStringExtra("nodeId");
            uniqueid = intent.getStringExtra("uniqueid");
            String str[] = uniqueid.split("@");
            if(str!=null && str.length > 0){
                nodeId = str[1];
            }
        }

        if(Const.isRemote){
            initIotMqttMessage();
            if(HomeActivity.shadowTopic!=null && !HomeActivity.shadowTopic.equals("")){
//                MqttService mqttService = MqttService.getInstance();
//                mqttService.publishMqttMessage(HomeActivity.shadowTopic, "mobile_zwave:getBasic:" + nodeId );
                MqttDesiredJStrBuilder builder = new MqttDesiredJStrBuilder(Const.subscriptionTopic+"Zwave"+nodeId);
                builder.setJsonString( CloudIotData.getSwitchStatus(nodeId) );
                AskeyIoTService.getInstance(ZwaveClientApplication.getInstance()).publishDesiredMessage(HomeActivity.shadowTopic, builder);
            }
        }else{
            MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
            //获取灯泡状态
//            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+nodeId,"mobile_zwave:getBasic:" + nodeId );
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+nodeId, LocalMqttData.getSwitchStatus(nodeId));
        }

        cbSwitch = (CheckBox) findViewById(R.id.cb_switch);
       cbSwitch.setOnClickListener(this);

    }

    private void initIotMqttMessage() {

        IotMqttManagement.getInstance().setIotMqttMessageCallback(new IotMqttMessageCallback() {
            @Override
            public void receiveMqttMessage(String s, String s1, String s2) {
                //处理结果
                Logg.i(TAG, "==IotMqttMessageCallback====setIotMqttMessageCallback==s=" + s);
                Logg.i(TAG, "==IotMqttMessageCallback====setIotMqttMessageCallback==s1=" + s1);
                Logg.i(TAG, "==IotMqttMessageCallback====setIotMqttMessageCallback==s2=" + s2);

                if(s2.contains("setSwitchAllOn") || s2.contains("setSwitchAllOff") || s2.contains("startStopSwitchLevelChange") || s2.contains("getPowerLevel")|| s2.contains("getBasic")
                        || s2.contains("setBasic") || s2.contains("setSwitchMultiLevel") || s2.contains("setConfiguration")  ){
                    return;
                }
//                String str[]  = s2.split("#");
//                if(str.length>1){
//                    mqttMessageResult(str[1]);
//                }
                mqttMessageResult(s2);
            }

        });

        //以下这句为注册监听
        AskeyIoTService.getInstance(appContext).setShadowReceiverListener(new ShadowReceiveListener() {
            @Override
            public void receiveShadowDocument(String s, String s1, String s2) {
                Logg.i(TAG, "==IotMqttMessageCallback====setShadowReceiverListener==s=" + s);
                Logg.i(TAG, "==IotMqttMessageCallback====setShadowReceiverListener==s1=" + s1);
                Logg.i(TAG, "==IotMqttMessageCallback====setShadowReceiverListener==s2=" + s2);
                IotMqttManagement.getInstance().receiveMqttMessage(s,s1,s2);

                if(s2.contains("setSwitchAllOn") || s2.contains("setSwitchAllOff") || s2.contains("startStopSwitchLevelChange") || s2.contains("getPowerLevel")|| s2.contains("getBasic")
                        || s2.contains("setBasic") || s2.contains("setSwitchMultiLevel") || s2.contains("setConfiguration")  ){
                    return;
                }
//                String str[]  = s2.split("#");
//                if(str.length>1){
//                    mqttMessageResult(str[1]);
//                }
                mqttMessageResult(s2);
            }
        });
    }


    private void initView() {
        lightLevel = (SeekBar) findViewById(R.id.sbr_light_level);
        etDuration = (EditText) findViewById(R.id.duration_Edit);
        etSecChangeDir = (EditText) findViewById(R.id.secChangeDir_Edit);
        etSecStep = (EditText) findViewById(R.id.secStep_Edit);
        tvPowerLevel = (TextView) findViewById(R.id.tv_power_level);
        tvProgress = (TextView) findViewById(R.id.tv_progress);
        brightness = (TextView) findViewById(R.id.brightness);
        dim = (RadioButton) findViewById(R.id.dim);
        brighten = (RadioButton) findViewById(R.id.brighten);
        brighten.setChecked(true);

        btnGetPower = (Button) findViewById(R.id.btn_get_power);
        btnStartChange = (Button) findViewById(R.id.btn_start_change);
        btnGetPower.setOnClickListener(this);
        btnStartChange.setOnClickListener(this);
        brightness_change = (SeekBar) findViewById(R.id.brightness_change);


        lightLevel.setProgress(15);
        lightLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tvProgress.setText(String.valueOf(i));
                if(Const.isRemote){
                    if(HomeActivity.shadowTopic!=null && !HomeActivity.shadowTopic.equals("")){
                        MqttService mqttService = MqttService.getInstance();
                        mqttService.publishMqttMessage(HomeActivity.shadowTopic,"mobile_zwave:setBasic:" + nodeId + ":"+i );

//                        MqttDesiredJStrBuilder builder = new MqttDesiredJStrBuilder(Const.subscriptionTopic);
//                        builder.setJsonString( CloudIotData.getSwitchStatus(nodeId) );
//                        AskeyIoTService.getInstance(ZwaveClientApplication.getInstance()).publishDesiredMessage(HomeActivity.shadowTopic+nodeId, builder);
                    }
                }else{
                    MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+nodeId,"mobile_zwave:setBasic:" + nodeId + ":"+i);
                }


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

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
                if(Const.isRemote){
                    if(HomeActivity.shadowTopic!=null && !HomeActivity.shadowTopic.equals("")){
                        MqttService mqttService = MqttService.getInstance();
                        mqttService.publishMqttMessage(HomeActivity.shadowTopic,"mobile_zwave:setSwitchMultiLevel:" + nodeId + ":"+brightnessLevel +":1" );
                    }
                }else{
                    MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+nodeId,"mobile_zwave:setSwitchMultiLevel:" + nodeId + ":"+brightnessLevel +":1");
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(Const.isRemote){
                    if(HomeActivity.shadowTopic!=null && !HomeActivity.shadowTopic.equals("")){
                        MqttService mqttService = MqttService.getInstance();
                        mqttService.publishMqttMessage(HomeActivity.shadowTopic,"mobile_zwave:getBasic:" + nodeId );
                    }
                }else{
//                    MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+nodeId,"mobile_zwave:getBasic:" + nodeId );
                    MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getSwitchStatus(nodeId));
                }

            }
        });

        findViewById(R.id.full_brigtness).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Const.isRemote){
                    if(HomeActivity.shadowTopic!=null && !HomeActivity.shadowTopic.equals("")){
                        MqttService mqttService = MqttService.getInstance();
                        mqttService.publishMqttMessage(HomeActivity.shadowTopic,"mobile_zwave:setConfiguration:1:" + nodeId + ":1:1:0:0");
                    }
                }else{
                    MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+nodeId,"mobile_zwave:setConfiguration:1:" + nodeId + ":1:1:0:0" );
                }

            }
        });

        findViewById(R.id.brigtness_last_setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Const.isRemote){
                    if(HomeActivity.shadowTopic!=null && !HomeActivity.shadowTopic.equals("")){
                        MqttService mqttService = MqttService.getInstance();
                        mqttService.publishMqttMessage(HomeActivity.shadowTopic,"mobile_zwave:setConfiguration:1:" + nodeId + ":1:1:0:1");
                    }
                }else{
                    MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+nodeId,"mobile_zwave:setConfiguration:1:" + nodeId + ":1:1:0:1" );
                }
            }
        });



    }


    MqttMessageArrived mMqttMessageArrived = new MqttMessageArrived() {
        @Override
        public void mqttMessageArrived(String topic, MqttMessage message) {
            final String result = new String(message.getPayload());
            Logg.i(TAG,"=mqttMessageArrived=>=topic="+topic);
            Logg.i(TAG,"=mqttMessageArrived=>=message="+result);
            if(result.contains("setSwitchAllOn") || result.contains("setSwitchAllOff") || result.contains("startStopSwitchLevelChange") || result.contains("getPowerLevel")|| result.contains("getBasic")
                    || result.contains("setBasic") || result.contains("setSwitchMultiLevel") || result.contains("setConfiguration")  ){
                return;
            }
            String str[]  = result.split("#");
            if(str.length>1){
                mqttMessageResult(str[1]);
            }

        }
    };



    //mqtt调用返回结果
    private void mqttMessageResult(String result) {
        try {
            final JSONObject jsonObject = new JSONObject(result);

            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String messageType = jsonObject.optString("MessageType");
                    if ("Power Level Get Information".equals(messageType)) {
                        String powerLevel = jsonObject.optString("Power Level");
                          tvPowerLevel.setText(powerLevel);
                    }else if("Basic Information".equals(messageType)){
                        String value = jsonObject.optString("value");
                        brightness.setText("Brightness : " + value);
                        if(value.equals("00h")){
                            //关闭
                            cbSwitch.setChecked(false);
                        }else{
                            //打开
                            cbSwitch.setChecked(true);
                        }

                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void openMemu(Context context) {
//        super.openMemu(context);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unrigister();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void unrigister() {
        if(mMqttMessageArrived!=null){
            MQTTManagement.getSingInstance().unrigister(mMqttMessageArrived);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_get_power:
                //获取功率
                if(Const.isRemote){
                    if(HomeActivity.shadowTopic!=null && !HomeActivity.shadowTopic.equals("")){
                        MqttService mqttService = MqttService.getInstance();
                        mqttService.publishMqttMessage(HomeActivity.shadowTopic,"mobile_zwave:getPowerLevel:" + nodeId);
                    }
                }else{
                    MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+nodeId,"mobile_zwave:getPowerLevel:" + nodeId);
                }

                break;
            case R.id.btn_start_change:
                int startlevel = lightLevel.getProgress();
                String duration = etDuration.getText().toString();
                String pmyChangeDir = "";
               if( brighten.isChecked()){
                   pmyChangeDir = "0";
               }else if( dim.isChecked()){
                   pmyChangeDir = "1";
               }
                String secChangeDir = etSecChangeDir.getText().toString();
                String secStep = etSecStep.getText().toString();
                if ("".equals(duration)) {
                    duration = "5";
                }
                if(Const.isRemote){
                    if(HomeActivity.shadowTopic!=null && !HomeActivity.shadowTopic.equals("")){
                        MqttService mqttService = MqttService.getInstance();
                        mqttService.publishMqttMessage(HomeActivity.shadowTopic,"mobile_zwave:startStopSwitchLevelChange:1:" + nodeId + ":" + startlevel + ":" + duration+ ":" + pmyChangeDir + ":3:0");
                    }
                }else{
                    //  MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+nodeId,"mobile_zwave:startStopSwitchLevelChange:" + nodeId + ":" + startlevel + ":" + duration+ ":" + pmyChangeDir + ":" +secChangeDir + ":" + secStep);
                    MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+nodeId,"mobile_zwave:startStopSwitchLevelChange:1:" + nodeId + ":" + startlevel + ":" + duration+ ":" + pmyChangeDir + ":3:0");
                }

                break;
            case R.id.cb_switch:
                if (cbSwitch.isChecked()) {
                    Logg.i(TAG,"===setSwitchAllOn======");
                    if(Const.isRemote){
                        if(HomeActivity.shadowTopic!=null && !HomeActivity.shadowTopic.equals("")){
                            MqttService mqttService = MqttService.getInstance();
                            mqttService.publishMqttMessage(HomeActivity.shadowTopic,"mobile_zwave:setSwitchAllOn:" + nodeId);
                        }
                    }else{
                        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+nodeId,"mobile_zwave:setSwitchAllOn:" + nodeId);
                    }

                } else {
                    Logg.i(TAG,"===setSwitchAllOff======");
                    if(Const.isRemote){
                        if(HomeActivity.shadowTopic!=null && !HomeActivity.shadowTopic.equals("")){
                            MqttService mqttService = MqttService.getInstance();
                            mqttService.publishMqttMessage(HomeActivity.shadowTopic,"mobile_zwave:setSwitchAllOff:" + nodeId);
                        }
                    }else{
                        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+nodeId,"mobile_zwave:setSwitchAllOff:" + nodeId);
                    }

                }
                break;
            default:

                break;
        }
    }




}
