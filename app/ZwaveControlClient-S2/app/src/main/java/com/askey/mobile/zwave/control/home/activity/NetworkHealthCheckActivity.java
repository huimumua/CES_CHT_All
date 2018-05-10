package com.askey.mobile.zwave.control.home.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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

public class NetworkHealthCheckActivity extends AppCompatActivity {
    private static final String LOG_TAG = NetworkHealthCheckActivity.class.getSimpleName();
    private TextView networkHealth,rssiValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_health);
        networkHealth = (TextView) findViewById(R.id.network_health);
        rssiValue = (TextView) findViewById(R.id.rssi_value);

        MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setMqttDataJson("getRssiState", "0"));
    }


    MqttMessageArrived mMqttMessageArrived = new MqttMessageArrived() {
        @Override
        public void mqttMessageArrived(String topic, MqttMessage message) {
            final String result = new String(message.getPayload());
            Logg.i(LOG_TAG, "=mqttMessageArrived=>=topic=" + topic);
            Logg.i(LOG_TAG, "=mqttMessageArrived=>=message=" + result);
            if (result.contains("desired")) {
                return;
            }
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(result);
                String reported = jsonObject.optString("reported");
                JSONObject reportedObject = new JSONObject(reported);
                String messageType = reportedObject.optString("MessageType");
                if ("Network IMA Info Report".equals(messageType)) {
                    final String netHealth = reportedObject.optString("Network Health");
                    final String rsValue = reportedObject.optString("RSSI hops value");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            networkHealth.setText(netHealth);
                            rssiValue.setText(rsValue);
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unrigister();
    }

    private void unrigister() {
        if(mMqttMessageArrived!=null){
            MQTTManagement.getSingInstance().unrigister(mMqttMessageArrived);
        }
    }

}
