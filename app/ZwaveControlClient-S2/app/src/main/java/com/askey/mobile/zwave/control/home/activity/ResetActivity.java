package com.askey.mobile.zwave.control.home.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.net.TcpClient;
import com.askey.mobile.zwave.control.interf.NetworkRole;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class ResetActivity extends AppCompatActivity {
    private static final String LOG_TAG = ResetActivity.class.getSimpleName();
    private TextView reset,networkRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);
        reset = (TextView) findViewById(R.id.reset);
        networkRole = (TextView) findViewById(R.id.network_role);

        MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setDefault());
    }


    MqttMessageArrived mMqttMessageArrived = new MqttMessageArrived() {
        @Override
        public void mqttMessageArrived(String topic, MqttMessage message) {
            final String result = new String(message.getPayload());
            Logg.i(LOG_TAG,"=mqttMessageArrived=>=topic="+topic);
            Logg.i(LOG_TAG,"=mqttMessageArrived=>=message="+result);
            if (result.contains("desired")) {
                return;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        String reported = jsonObject.optString("reported");
                        JSONObject reportedObject = new JSONObject(reported);
                        String messageType = reportedObject.optString("MessageType");
                        if(messageType.equals("Controller Reset Status")){
                            final String status = reportedObject.optString("Status");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    reset.setText(status);
                                }
                            });
                            if(status.equals("Success")){
                                //删除成功则返回主页，否则提示删除失败，返回设备管理界面
                                Const.RESET_FAVORITE = true;
                                Const.RESET_ROOMS = true;
                                Const.RESET_PROVISION = true;
//                                if (result.contains("Network Role")) {
//                                    String tmp = result.split(":")[1];
//
//                                }
                            }

                        } else if (messageType.equals("Controller Attribute")){
                            final String tmp = reportedObject.optString("Network Role");
                            Const.NETWORK_ROLE = tmp;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    networkRole.setText(tmp);
                                }
                            });
                        } else if (messageType.equals("setDefault")){ //reset的返回结果
                            String result = reportedObject.optString("result");
                            if (result.equals("true")){
                                finish();
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        networkRole.setText("Reset failed!");
                                    }
                                });
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Logg.i(LOG_TAG,"errorJson------>"+result);
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
        if(mMqttMessageArrived!=null){
            MQTTManagement.getSingInstance().unrigister(mMqttMessageArrived);
        }
    }

}
