package com.askey.mobile.zwave.control.home.activity.addDevice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.net.SocketTransceiver;
import com.askey.mobile.zwave.control.deviceContr.net.TCPReceive;
import com.askey.mobile.zwave.control.deviceContr.net.TcpClient;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;
import com.askey.mobile.zwave.control.util.ToastShow;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class SmartStartDeviceAddActivity extends BaseActivity implements View.OnClickListener {
    private static String TAG = "SmartStartDeviceAddActivity";
    private TextView statusTextView;
    private ProgressBar progressBar;
    private Button exit;


    private String smartStartAddStatus, smartStartAddResult, newAdded, smartStartNodeId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_start_auto_add);

        //brand = getIntent().getStringExtra("brand");
        //deviceType = getIntent().getStringExtra("deviceType");
        statusTextView = (TextView) findViewById(R.id.smart_start_add_status);
        progressBar = (ProgressBar) findViewById(R.id.smart_start_add_progress);
        exit = (Button) findViewById(R.id.exit_smart_start_add);
        exit.setOnClickListener(this);

        MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);

        Logg.i(TAG, "onCreate- > ---- ");

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /*
     * 此处为安装成功的跳转，若安装失败则弹出一个dialog
    */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.exit_smart_start_add:
                finish();
                break;
        }
    }

    MqttMessageArrived mMqttMessageArrived = new MqttMessageArrived() {
        @Override
        public void mqttMessageArrived(String topic, MqttMessage message) {
            final String result = new String(message.getPayload());
            Logg.i(TAG, "=mqttMessageArrived=>=topic=" + topic);
            Logg.i(TAG, "=mqttMessageArrived=>=message=" + result);
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {

                        JSONObject jsonObject = new JSONObject(result);
                        String reported = jsonObject.optString("reported");
                        JSONObject reportedObject = new JSONObject(reported);
                        if (reported.contains("Node Add Status")) {
                            Log.i(TAG, "========Node Add Status=========");

                            String messageType = reportedObject.optString("MessageType");

                            smartStartAddStatus = reportedObject.optString("Status");

                            if (smartStartAddStatus.equals("failde")) {
                                Log.i(TAG, "-------107---------");
                                progressBar.setVisibility(View.GONE);
                                statusTextView.setText(getResources().getString(R.string.add_failed));
                                exit.setVisibility(View.VISIBLE);
                            } else {
                                statusTextView.setText(smartStartAddStatus);
                            }

                            if (reported.contains("NewAdded")) {
                                newAdded = reportedObject.optString("NewAdded");
                                Log.i(TAG, "==========newAdded==" + newAdded);
                            }

                            Log.i(TAG, "==========smartStartAddStatus==" + smartStartAddStatus);

                        }else if(reported.contains("addDevice")){
                            smartStartNodeId = reportedObject.optString("NodeId");
                            Log.i(TAG, "========addDevice========NodeId="+smartStartNodeId);
                            smartStartAddResult = reportedObject.optString("result");
                            if(smartStartAddResult.equals("true")){
                                Intent intent = new Intent();
                                intent.setClass(mContext, InstallSuccessActivity.class);
                                intent.putExtra("brand", ""); //由于smart start自动添加，UI没有选择设备类型，所以这里写为空
                                intent.putExtra("deviceType", "");//由于smart start自动添加，UI没有选择设备类型，所以这里写为空
                                intent.putExtra("nodeId", smartStartNodeId);

                                startActivity(intent);
                            }
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unrigister();
        Logg.i(TAG, "===onDestroy=====");
    }

    private void unrigister() {
        if (mMqttMessageArrived != null) {
            MQTTManagement.getSingInstance().unrigister(mMqttMessageArrived);
        }
    }

    /**
     * 监听Back键按下事件,方法2:
     * 注意:
     * 返回值表示:是否能完全处理该事件
     * 在此处返回false,所以会继续传播该事件.
     * 在具体项目中此处的返回值视情况而定.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            finish();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
            Logg.i(TAG, "onTouchEvent=====");
            return true;
        }
        return true;
    }


}
