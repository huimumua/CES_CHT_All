package com.askey.mobile.zwave.control.home.activity.addDevice;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.interf.DeleteDeviceListener;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by skysoft on 2018/4/11.
 */

public class RemoveFailActivity extends BaseActivity {
    private static final String LOG_TAG = RemoveFailActivity.class.getSimpleName();
    private static DeleteDeviceListener deleteDeviceListener;
    TextView title;
    TextView status;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_fail);
        title = (TextView) findViewById(R.id.title);
        status = (TextView) findViewById(R.id.status);
        title.setText("removeFailDevice");
        MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);

        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.removeFailDevice(getIntent().getStringExtra("nodeId")));
    }


    MqttMessageArrived mMqttMessageArrived = new MqttMessageArrived() {
        @Override
        public void mqttMessageArrived(String topic, MqttMessage message) {
            final String mqttResult = new String(message.getPayload());
            Logg.i(LOG_TAG, "=mqttMessageArrived=>=topic=" + topic);
            Logg.i(LOG_TAG, "=mqttMessageArrived=>=topic=" + mqttResult);
            if (mqttResult.contains("desired")) {
                return;
            }
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject jsonObject = new JSONObject(mqttResult);
                        String reported = jsonObject.optString("reported");
                        JSONObject reportedObject = new JSONObject(reported);
                        String messageType = reportedObject.optString("MessageType");
                        if (messageType.equals("Remove Failed Node")) {
                            String tmp = reportedObject.optString("Status");
                            status.setText(tmp);
                            if ("Success".equals(tmp)) {
                                DeleteDevice.deleteSuccess(getIntent().getStringExtra("roomName"));
                                finish();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Logg.i(LOG_TAG, "errorJson------>" + mqttResult);
                    }
                }
            });
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unrigister();
    }

    private void unrigister() {
        if (mMqttMessageArrived != null) {
            MQTTManagement.getSingInstance().unrigister(mMqttMessageArrived);
        }
    }


}