package com.askey.mobile.zwave.control.home.activity;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.net.SocketTransceiver;
import com.askey.mobile.zwave.control.deviceContr.net.TCPReceive;
import com.askey.mobile.zwave.control.deviceContr.net.TcpClient;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;
import com.askey.mobile.zwave.control.util.PreferencesUtils;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class ResetActivity extends AppCompatActivity {
    private static final String LOG_TAG = ResetActivity.class.getSimpleName();
    private TextView reset,resetResult;
    private Button doneButton;
    private String nodeId;
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);
        reset = (TextView) findViewById(R.id.reset);
        resetResult = (TextView) findViewById(R.id.reset_result);
        doneButton = (Button) findViewById(R.id.reset_done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
        TcpClient.getInstance().rigister(tcpReceive);//注册TCP监听
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
                        nodeId = reportedObject.optString("nodeId");
                        role = reportedObject.optString("Network Role");
                        Log.i(LOG_TAG,""+"==="+ nodeId +"==="+ role);

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
                                Const.setIsDataChange(true);
                                finish();
                            }
                            else if(status.equals("-17")) {
                                resetResult.setText("Current operation not completed yet, try again later");
                                doneButton.setVisibility(View.VISIBLE);
                            } else {
                                resetResult.setText(getResources().getString(R.string.reset_failed));
                                doneButton.setVisibility(View.VISIBLE);
                            }

                        } else if (messageType.equals("Controller Attribute")){
                            final String tmp = reportedObject.optString("Network Role");
                            Const.NETWORK_ROLE = tmp;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    resetResult.setText(tmp);
                                    if (nodeId!=null&role!=null){
                                        PreferencesUtils.put(ResetActivity.this,"resetid",nodeId);
                                        PreferencesUtils.put(ResetActivity.this,"resetrole",role);
                                    }


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
                                        resetResult.setText("Reset failed!");
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
                    if ("Controller Reset Status".equals(messageType)) {
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
        if(mMqttMessageArrived!=null){
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
        alertDialog.setCanceledOnTouchOutside(false);
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
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setDefault());
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

}
