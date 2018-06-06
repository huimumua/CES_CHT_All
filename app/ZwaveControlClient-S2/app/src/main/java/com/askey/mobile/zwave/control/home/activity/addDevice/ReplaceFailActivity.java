package com.askey.mobile.zwave.control.home.activity.addDevice;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.net.SocketTransceiver;
import com.askey.mobile.zwave.control.deviceContr.net.TCPReceive;
import com.askey.mobile.zwave.control.deviceContr.net.TcpClient;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by skysoft on 2018/4/11.
 */

public class ReplaceFailActivity extends BaseActivity {
    private static final String LOG_TAG = ReplaceFailActivity.class.getSimpleName();
    TextView title;
    TextView status;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_fail);
        title = (TextView) findViewById(R.id.title);
        status = (TextView) findViewById(R.id.status);
        title.setText("replaceFailDevice");
        MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
        TcpClient.getInstance().rigister(tcpReceive);//注册TCP监听

        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.replaceFailDevice(getIntent().getStringExtra("nodeId")));
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
                        if (messageType.equals("Replace Failed Node")) {
                            String tmp = reportedObject.optString("Status");
                            status.setText(tmp);
                            if ("Success".equals(tmp)) {
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

    TCPReceive tcpReceive = new TCPReceive() {
        @Override
        public void onConnect(SocketTransceiver transceiver) {

        }

        @Override
        public void onConnectFailed() {

        }

        @Override
        public void receiveMessage(SocketTransceiver transceiver, String tcpMassage) {
            //处理结果
            removeDeviceResult(tcpMassage);

        }

        @Override
        public void onDisconnect(SocketTransceiver transceiver) {

        }

    };

    private void removeDeviceResult(final String result) {

        Log.i(LOG_TAG, "====Tcp Result:" + result);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final JSONObject jsonObject = new JSONObject(result);

                    if (jsonObject == null) return;

                    String messageType = jsonObject.optString("MessageType");
                    if ("Replace Failed Node".equals(messageType)) {
                        String status = jsonObject.optString("Status");
                        if ("-17".equals(status)) {
                            showPromptDialog(getResources().getString(R.string.prompt_try_again));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Logg.i(LOG_TAG, "errorJson------>" + result);
                }
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unrigister();
    }

    private void unrigister() {
        if (mMqttMessageArrived != null) {
            MQTTManagement.getSingInstance().unrigister(mMqttMessageArrived);
        }
        if (tcpReceive != null) {
            TcpClient.getInstance().unrigister(tcpReceive);
        }
    }

    private void showPromptDialog(String message) {
        final AlertDialog.Builder addDialog = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.dialog_normal_layout, null);
        addDialog.setView(view);
        final AlertDialog alertDialog = addDialog.create();

        TextView title = (TextView) view.findViewById(R.id.title);
        TextView promptMessage = (TextView) view.findViewById(R.id.message);
        title.setText("Prompt");
        promptMessage.setText(message);
        TextView positiveButton = (TextView) view.findViewById(R.id.positiveButton);
        TextView negativeButton = (TextView) view.findViewById(R.id.negativeButton);
        positiveButton.setText("retry");
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击重试，返回添加设备界面，再次执行添加设备
                //预留的接口mqtt
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.replaceFailDevice(getIntent().getStringExtra("nodeId")));
                alertDialog.dismiss();
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击取消，返回主页
                if (TcpClient.getInstance().isConnected()) {
                    TcpClient.getInstance().getTransceiver().send("mobile_zwave:stopRemoveDevice:Zwave"); //停止底层的所有操作
                }
                alertDialog.dismiss();
                finish();

            }
        });

        alertDialog.show();
    }

    /**
     * ??????,????????TCP:stopAddDevice?????
     * ???stopAddDevice??????????api??
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (TcpClient.getInstance().isConnected()) {
                Logg.i(LOG_TAG, "TcpClient -> send -> mobile_zwave:stopAddDevice:Zwave");
                TcpClient.getInstance().getTransceiver().send("mobile_zwave:stopAddDevice:Zwave");
            }
            finish();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

}