package com.askey.mobile.zwave.control.deviceContr.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.deviceContr.model.DeviceList;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;

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
public class DimmerManageActivity extends BaseActivity implements View.OnClickListener {
    public static String TAG = "DimmerManageActivity";
    private CheckBox cbSwitch, dimmerSwitch , useDefault;
    private DeviceList.NodeInfoList nodeInfoList;
    private String nodeId;
    private Button getConfig,setConfig;
    private EditText paramNum,paramValue;
    private LinearLayout linearParamValue;
    private  String useDefaultValueEdit = "0";
    private SeekBar brightness_change;
    private int brightnessLevel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dimmer);

        setTopLayout(true,"Dimmer Manage",false);

        initView();
        Intent intent = getIntent();
        nodeInfoList = intent.getParcelableExtra("nodeInfoList");
        if (nodeInfoList != null) {
            nodeId = nodeInfoList.getNodeId();
        }else{
            nodeId = intent.getStringExtra("nodeId");
        }

        MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+nodeId,"mobile_zwave:getConfiguration:1:" + nodeId + ":1:0:1:10");

        cbSwitch = (CheckBox) findViewById(R.id.cb_switch);
        cbSwitch.setOnClickListener(this);
        dimmerSwitch = (CheckBox) findViewById(R.id.dimmer_switch);
        dimmerSwitch.setOnClickListener(this);//初始状态还未设置

    }

    private void initView() {
        getConfig = (Button) findViewById(R.id.btn_getConfig);
        setConfig = (Button) findViewById(R.id.btn_setConfig);
        getConfig.setOnClickListener(this);
        setConfig.setOnClickListener(this);

        paramNum = (EditText) findViewById(R.id.param_num_Edit);
        paramValue = (EditText) findViewById(R.id.param_value_Edit);
        linearParamValue = (LinearLayout) findViewById(R.id.param_value);
        useDefault = (CheckBox) findViewById(R.id.use_default);
        useDefault.setChecked(false);

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
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+nodeId,"mobile_zwave:setSwitchMultiLevel:" + nodeId + ":"+brightnessLevel +":1");
            }
        });


        useDefault.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    linearParamValue.setVisibility(View.INVISIBLE);
                    useDefaultValueEdit = "1";
                }else{
                    linearParamValue.setVisibility(View.VISIBLE);
                    useDefaultValueEdit = "0";
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
            if(result.contains("setSwitchAllOn") || result.contains("setSwitchAllOff") || result.contains("getConfiguration") || result.contains("setConfiguration")|| result.contains("setSwitchMultiLevel")){
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
                    if ("Configuration Get Information".equals(messageType)) {
                        String nodeId = jsonObject.optString("Node id");
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
            case R.id.btn_getConfig:
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+nodeId,"mobile_zwave:getConfiguration:1:" + nodeId + ":1:1:1:10");
                break;
            case R.id.btn_setConfig:
                String num = paramNum.getText().toString();
                String value = paramValue.getText().toString();
                if ("".equals(value)) {
                    value = "0";
                }
                if (!"".equals(num) && !"".equals(useDefaultValueEdit)) {
                    MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+nodeId,"mobile_zwave:setConfiguration:1:" + nodeId + ":" + num + ":1:" + useDefaultValueEdit+ ":" + value);
                }
                break;
            case R.id.cb_switch:
                if (cbSwitch.isChecked()) {
                  Logg.i(TAG,"===setConfiguration===open===");
                    MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+nodeId,"mobile_zwave:setConfiguration:1:" + nodeId + ":7:1:0:255");

                } else {
                    Logg.i(TAG,"===setConfiguration===close===");
                    MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+nodeId,"mobile_zwave:setConfiguration:1:" + nodeId + ":7:1:0:0");
                }
                break;
            case R.id.dimmer_switch:
                if (dimmerSwitch.isChecked()) {
                    Logg.i(TAG,"===setSwitchAllOn======");
                    MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+nodeId,"mobile_zwave:setSwitchAllOn:" + nodeId);
                } else {
                    Logg.i(TAG,"===setSwitchAllOff======");
                    MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+nodeId,"mobile_zwave:setSwitchAllOff:" + nodeId);
                }
                break;
            default:

                break;
        }
    }


}
