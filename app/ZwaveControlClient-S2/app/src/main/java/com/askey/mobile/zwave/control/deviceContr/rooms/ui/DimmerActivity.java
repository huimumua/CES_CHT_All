package com.askey.mobile.zwave.control.deviceContr.rooms.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class DimmerActivity extends BaseDeviceActivity implements View.OnClickListener{
    private static final String TAG = DimmerActivity.class.getSimpleName();
    private CheckBox dimmerSwitch;
    private SeekBar dimmerSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dimmer);

        nodeId = getIntent().getStringExtra("nodeId");

        dimmerSwitch = (CheckBox) findViewById(R.id.cb_switch);
        dimmerSeekBar = (SeekBar) findViewById(R.id.seekBar);
        dimmerSwitch.setOnClickListener(this);

        MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+"Zwave"+nodeId,LocalMqttData.getBrigtness(nodeId));
        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+"Zwave"+nodeId,LocalMqttData.getSwitchStatus(nodeId));

        dimmerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Logg.i(TAG,"==mSeekBar=="+String.valueOf(dimmerSeekBar.getProgress()));
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+"Zwave"+nodeId,
                        LocalMqttData.setBrigtness(nodeId,String.valueOf(dimmerSeekBar.getProgress())));
//                    MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic + nodeId, "mobile_zwave:setBasic:" + nodeId + ":FF");
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cb_switch:
                if (dimmerSwitch.isChecked()) {
                    MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic + "Zwave" + nodeId, LocalMqttData.setSwitch(nodeId, "on"));
                } else {
                    MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic +"Zwave"+ nodeId, LocalMqttData.setSwitch(nodeId, "off"));
                }
                break;

        }
    }

    MqttMessageArrived mMqttMessageArrived = new MqttMessageArrived() {
        @Override
        public void mqttMessageArrived(String topic, MqttMessage message) {
            final String result = new String(message.getPayload());
            Logg.i(TAG,"=mqttMessageArrived=>=topic="+topic);
            Logg.i(TAG,"=mqttMessageArrived=>=message="+result);

            if(result.contains("desired")){
                return;
            }
            mqttMessageResult(result);
        }
    };

    //mqtt调用返回结果
    private void mqttMessageResult(String result) {
        try {
//            {"reported":{"MessageType":"Switch Color Report","Node id":27,"component id":"Blue","value":0,"Interface":"getLampColor","devType":"Zwave"}}
            JSONObject jsonObject = new JSONObject(result);
            String reported = jsonObject.optString("reported");
            final JSONObject reportedObject = new JSONObject(reported);
            String Interface = reportedObject.optString("Interface");
            if(Interface.equals("getSwitchStatus")){
                String switchStatus = reportedObject.optString("switchStatus");
                setSwitchUiStatus(switchStatus);
            }else if(Interface.equals("getBrightness")){
                final String brigtness = reportedObject.optString("brightness");
                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dimmerSeekBar.setProgress(Integer.valueOf(brigtness));
                    }
                });

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void setSwitchUiStatus(final String switchStatus) {
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(switchStatus.equals("on")){
                    dimmerSwitch.setChecked(true);
                }else{
                    dimmerSwitch.setChecked(false);
                }
            }
        });
    }
}
