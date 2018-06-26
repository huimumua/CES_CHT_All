package com.askey.mobile.zwave.control.home.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
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

public class LearnModeActivity extends AppCompatActivity {
    private static final String LOG_TAG = LearnModeActivity.class.getSimpleName();
    private TextView learnMode,controllerAttribute;
    private TextView tvDsk,dsk;
    private RelativeLayout redLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_mode);
        learnMode = (TextView) findViewById(R.id.learn_mode);
        controllerAttribute = (TextView) findViewById(R.id.controller_attribute);
        tvDsk = (TextView) findViewById(R.id.tv_dsk);
        dsk = (TextView) findViewById(R.id.dsk);
        redLine = (RelativeLayout) findViewById(R.id.red_line);

        MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.startLearnMode());
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
                        if(messageType.equals("Controller Init Status")){
                            final String status = reportedObject.optString("Status");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    learnMode.setText(status);
                                }
                            });
                            if(status.equals("Success")){
                            }

                        } else if ("Controller Attribute".equals(messageType)) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    controllerAttribute.setText(result);
                                }
                            });
                        } else if ("Controller DSK Report".equals(messageType)) {
                            final String tmp = reportedObject.optString("DSK");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvDsk.setVisibility(View.VISIBLE);

                                    SpannableString ss = new SpannableString(tmp);
                                    //设置DSK前5个字符的字体颜色红色高亮。接口：setSpan(color, 字符启始位,字符结束位,前后包含);setSpan(Object what, int start, int end, int flags);
                                    ss.setSpan(new ForegroundColorSpan(Color.parseColor("#ff0000")), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    dsk.setText(ss);
                                    //显示下划线
                                    redLine.setVisibility(View.VISIBLE);
                                }
                            });
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            Const.setIsDataChange(true);
            finish();
            return false;
        }
        else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
