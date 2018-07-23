package com.askey.mobile.zwave.control.home.activity.addDevice;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.net.TcpClient;
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
    TextView title, actionTip;
    TextView status;
    Button removeConfirmBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_fail);
        title = (TextView) findViewById(R.id.title);
        actionTip = (TextView) findViewById(R.id.action_tip);
        status = (TextView) findViewById(R.id.status);
        title.setText("removeFailDevice");
        actionTip.setText(getResources().getString(R.string.please_wait_a_moment));
        removeConfirmBtn = (Button) findViewById(R.id.remove_confirm_btn);
        removeConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
                        //remove??????? {"reported":{"Interface":"removeDevice","NodeId":"fail","Result":"fail"}}
                        String result = reportedObject.optString("result");
                        if(result.equals("fail")){
                            status.setText("Remove failed,Please confirm whether the device exists.");
                            removeConfirmBtn.setVisibility(View.VISIBLE);
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

    /**
     * ??????,????????TCP:stopAddDevice?????
     * ???stopAddDevice??????????api??
     * @param keyCode
     * @param event
     * @return
     */
   /* @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (TcpClient.getInstance().isConnected()) {
                Logg.i(LOG_TAG, "TcpClient -> send -> mobile_zwave:stopAddDevice:Zwave");
                TcpClient.getInstance().getTransceiver().send("mobile_zwave:stopAddDevice:Zwave");
            }
            Const.setIsDataChange(true);
            finish();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }*/

}