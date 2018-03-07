package com.askey.mobile.zwave.control.deviceContr.rooms.ui;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class SensorActivity extends BaseDeviceActivity {
    private static final String TAG = SensorActivity.class.getSimpleName();
    private TextView mTypeOne;
    private TextView mTypeSecond;
    private TextView mTypeThird;
    private TextView mTypeFour;
    private TextView mValueOne;
    private TextView mValueSecond;
    private TextView mValueThird;
    private TextView mValueFour;
    private TextView mUnitOne;
    private TextView mUnitSecond;
    private TextView mUnitThird;
    private TextView mUnitFour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        nodeId = getIntent().getStringExtra("nodeId");
        MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+"Zwave"+nodeId, LocalMqttData.getSensorMultiLevel(nodeId));

        mTypeOne = (TextView) findViewById(R.id.tv_type);
        mValueOne = (TextView) findViewById(R.id.tv_value);
        mUnitOne = (TextView) findViewById(R.id.tv_unit);

        mTypeSecond = (TextView) findViewById(R.id.tv_type_second);
        mValueSecond = (TextView) findViewById(R.id.tv_value_second);
        mUnitSecond = (TextView) findViewById(R.id.tv_unit_second);

        mTypeThird = (TextView) findViewById(R.id.tv_type_third);
        mValueThird = (TextView) findViewById(R.id.tv_value_third);
        mUnitThird = (TextView) findViewById(R.id.tv_unit_third);

        mTypeFour = (TextView) findViewById(R.id.tv_type_four);
        mValueFour = (TextView) findViewById(R.id.tv_value_four);
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
//            if(Interface.equals("getSensorMultiLevel")){
                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if ("Luminance sensor".equals(reportedObject.optString("type"))) {//亮度
                            mTypeOne.setText(reportedObject.optString("type"));
                            mValueOne.setText(reportedObject.optString("value"));
                            mUnitOne.setText(reportedObject.optString("unit"));
                        } else if ("Temperature sensor".equals(reportedObject.optString("type"))) {
                            mTypeSecond.setText(reportedObject.optString("type"));
                            mValueSecond.setText(reportedObject.optString("value"));
                            mUnitSecond.setText(reportedObject.optString("unit"));
                        } else if ("Relative humidity sensor".equals(reportedObject.optString("type"))) {//湿度
                            mTypeThird.setText(reportedObject.optString("type"));
                            mValueThird.setText(reportedObject.optString("value"));
                            mUnitThird.setText(reportedObject.optString("unit"));
                        }
                        if (reportedObject.optString("Battery Value") != null && (!"".equals(reportedObject.optString("Battery Value")))) {
                            mTypeFour.setText("Battery Value");
                            mValueFour.setText(reportedObject.optString("Battery Value"));
                        }
                    }
                });
//            }
  
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
