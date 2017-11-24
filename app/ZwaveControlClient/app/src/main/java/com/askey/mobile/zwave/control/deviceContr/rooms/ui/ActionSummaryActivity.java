package com.askey.mobile.zwave.control.deviceContr.rooms.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.deviceContr.adapter.ChooseActionAdapter;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.IotMqttManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.IotMqttMessageCallback;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.home.activity.HomeActivity;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;
import com.askeycloud.webservice.sdk.iot.MqttService;
import com.askeycloud.webservice.sdk.iot.callback.ShadowReceiveListener;
import com.askeycloud.webservice.sdk.service.iot.AskeyIoTService;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;

public class ActionSummaryActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = ActionSummaryActivity.class.getSimpleName();
    private RelativeLayout one,second,third,four;
    private Button btnDone;
    private String nodeId;
    private String endpointId;
    private String groupId;
    private String arr;
    private String type;
    private String action;
    private String timer;
    private ImageView iconOne,iconSecond,iconThird,iconFour;
    private TextView tvAction;
    private TextView tvTimer;
    private Intent fromIntent;
    private String fromActivity;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_summary);

        getAction();
        initView();

    }


    private void initIotMqttMessage() {

        IotMqttManagement.getInstance().setIotMqttMessageCallback(new IotMqttMessageCallback() {
            @Override
            public void receiveMqttMessage(String s, String s1, String s2) {
                //处理结果
                Logg.i(TAG, "==IotMqttMessageCallback====setIotMqttMessageCallback==s=" + s);
                Logg.i(TAG, "==IotMqttMessageCallback====setIotMqttMessageCallback==s1=" + s1);
                Logg.i(TAG, "==IotMqttMessageCallback====setIotMqttMessageCallback==s2=" + s2);
                ///\\\\\\
                if(s2.contains("desired")){
                    return;
                }
                mqttMessageResult(s2);//要验s2格式

            }

        });

/*        //以下这句为注册监听
        AskeyIoTService.getInstance(this).setShadowReceiverListener(new ShadowReceiveListener() {
            @Override
            public void receiveShadowDocument(String s, String s1, String s2) {
                Logg.i(TAG, "==IotMqttMessageCallback====setIotMqttMessageCallback==s=" + s);
                Logg.i(TAG, "==IotMqttMessageCallback====setIotMqttMessageCallback==s1=" + s1);
                Logg.i(TAG, "==IotMqttMessageCallback====setIotMqttMessageCallback==s2=" + s2);
                IotMqttManagement.getInstance().receiveMqttMessage(s,s1,s2);
            }
        });*/
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

        }
    };

    //mqtt调用返回结果
    private void mqttMessageResult(String result) {
        try {
            final JSONObject jsonObject = new JSONObject(result);

            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String messageType = jsonObject.optString("MessageType");
                    if("messageType".equals(messageType)){ //messageType需要改
                       Intent intent = new Intent(ActionSummaryActivity.this, SetupKeyActivity.class);
                        startActivity(intent);

                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void initView() {
        mContext = this;

        tvAction = (TextView) findViewById(R.id.tv_action);
        tvTimer = (TextView) findViewById(R.id.tv_timer);
        one = (RelativeLayout) findViewById(R.id.rl_one);
        second = (RelativeLayout) findViewById(R.id.rl_second);
        third = (RelativeLayout) findViewById(R.id.rl_third);
        four = (RelativeLayout) findViewById(R.id.rl_four);
        btnDone = (Button) findViewById(R.id.btn_done);

        iconOne = (ImageView) findViewById(R.id.iv_icon_one);
        iconSecond = (ImageView) findViewById(R.id.iv_icon_second);
        iconThird = (ImageView) findViewById(R.id.iv_icon_third);
        iconFour = (ImageView) findViewById(R.id.iv_icon_four);

        one.setOnClickListener(this);
        second.setOnClickListener(this);
        third.setOnClickListener(this);
        four.setOnClickListener(this);
        btnDone.setOnClickListener(this);

        int[] icon = new int[]{ R.drawable.vector_drawable_ic_99,R.drawable.ic_launcher,R.drawable.vector_drawable_ic_100,R.drawable.vector_drawable_ic_106,
                R.drawable.vector_drawable_ic_105,R.drawable.vector_drawable_ic_107,R.drawable.vector_drawable_ic_108};
        if ("3".equals(endpointId) && "2".equals(groupId)) {
            iconOne.setImageResource(icon[0]);
        } else if ("4".equals(endpointId) && "2".equals(groupId)) {
            iconOne.setImageResource(icon[1]);
        } else if ("1".equals(endpointId) && "3".equals(groupId)) {
            iconOne.setImageResource(icon[2]);
        } else if ("2".equals(endpointId) && "3".equals(groupId)) {
            iconOne.setImageResource(icon[3]);
        } else if ("3".equals(endpointId) && "3".equals(groupId)) {
            iconOne.setImageResource(icon[4]);
        } else if ("1".equals(endpointId) && "2".equals(groupId)) {
            iconOne.setImageResource(icon[5]);
        } else if ("2".equals(endpointId) && "2".equals(groupId)) {
            iconOne.setImageResource(icon[6]);
        }

        if ("bulb".equals(type)) {
            iconSecond.setImageResource(R.drawable.vector_drawable_ic_device_79);
        }
            tvAction.setText(action);
            tvTimer.setText("Timer - " + timer);


    }

    private void getAction() {
        fromIntent = getIntent();
        fromActivity = fromIntent.getStringExtra("from");
/*        if (fromActivity != null) {
            if (fromActivity.equals(ActionChooseActivity.class.getSimpleName())) {
            } else if (fromActivity.equals(ToggleActivity.class.getSimpleName())) {

            } else if (fromActivity.equals(DoActionActivity.class.getSimpleName())) {

            } else if (fromActivity.equals(TimerActivity.class.getSimpleName())) {

            } else {

            }
        }*/
        nodeId =fromIntent.getStringExtra("nodeId");
        endpointId = fromIntent.getStringExtra("endpointId");
        groupId = fromIntent.getStringExtra("groupId");
        arr = fromIntent.getStringExtra("arr");
        type = fromIntent.getStringExtra("type");
        action = fromIntent.getStringExtra("action");
        timer = fromIntent.getStringExtra("timer");
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.rl_one:
                intent = new Intent(this, ActionChooseActivity.class);
                break;
            case R.id.rl_second:
                intent = new Intent(this, ToggleActivity.class);
                break;
            case R.id.rl_third:
                intent = new Intent(this, DoActionActivity.class);
                break;
            case R.id.rl_four:
                intent = new Intent(this, TimerActivity.class);
                break;
            case R.id.btn_done:

//                int ZwController_addEndpointsToGroup(int deviceId, int groupId, int[] arr);
                if(Const.isRemote){
                    initIotMqttMessage();
                    if(HomeActivity.shadowTopic!=null && !HomeActivity.shadowTopic.equals("")){
                        MqttService mqttService = MqttService.getInstance();
                        mqttService.publishMqttMessage(HomeActivity.shadowTopic,
                                "mobile_zwave:addEndpointsToGroup:" + nodeId + ":" + groupId + ":" + arr + ":" + endpointId + ":" + timer);
                    }
                }else{
                    MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
                    MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+"Zwave"+nodeId,
                            "mobile_zwave:addEndpointsToGroup:" + nodeId + ":" + groupId + ":" + arr + ":" + endpointId + ":" + timer);
                }
//                mobile_zwave:addEndpointsToGroup:null:3:arr:null:null
                Log.i("aaaaaaaaaaaaaa","mobile_zwave:addEndpointsToGroup:" + nodeId + ":" + groupId + ":" + arr + ":" + endpointId + ":" + timer);
                break;

        }

        if (intent != null) {
            intent.putExtra("from",ActionSummaryActivity.class.getSimpleName());
            intent.putExtra("nodeId",nodeId);
            intent.putExtra("endpointId",endpointId);
            intent.putExtra("groupId",groupId);
            intent.putExtra("arr",arr);
            intent.putExtra("type",type);
            intent.putExtra("action",action);
            intent.putExtra("timer",timer);
            startActivity(intent);
        }
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
    }
}
