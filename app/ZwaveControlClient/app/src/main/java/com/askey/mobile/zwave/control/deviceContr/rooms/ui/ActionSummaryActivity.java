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
import com.askey.mobile.zwave.control.deviceContr.localMqtt.IotMqttManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.scenes.SceneActionInfo;
import com.askey.mobile.zwave.control.deviceContr.scenes.SceneActionsActivity;
import com.askey.mobile.zwave.control.util.Logg;
import com.askeycloud.webservice.sdk.iot.callback.ShadowReceiveListener;
import com.askeycloud.webservice.sdk.service.iot.AskeyIoTService;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;


public class ActionSummaryActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String LOG_TAG = ActionSummaryActivity.class.getSimpleName();
    private RelativeLayout one,second,third,four;
    private Button btnDone;
    private String nodeId;
    private String name;
    private String type;
    private String action;
    private String timer;
    private ImageView iconOne,iconSecond,iconThird,iconFour;
    private TextView tvAction;
    private TextView tvTimer;
    private TextView tvName;
    private Intent fromIntent;
    private String fromActivity;
    private Context mContext;
    private SceneActionInfo sceneActionInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_summary);

        fromIntent = getIntent();
        fromActivity = fromIntent.getStringExtra("from");
        initView();
        processExtraData();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        processExtraData();
        sceneActionInfo = getIntent().getParcelableExtra("sceneActionInfo");

    }

    private void initIotMqttMessage() {
       //以下这句为注册监听
        AskeyIoTService.getInstance(this).setShadowReceiverListener(new ShadowReceiveListener() {
            @Override
            public void receiveShadowDocument(String s, String s1, String s2) {
                Logg.i(LOG_TAG, "==IotMqttMessageCallback====setShadowReceiverListener==s=" + s);
                Logg.i(LOG_TAG, "==IotMqttMessageCallback====setShadowReceiverListener==s1=" + s1);
                Logg.i(LOG_TAG, "==IotMqttMessageCallback====setShadowReceiverListener==s2=" + s2);
                IotMqttManagement.getInstance().receiveMqttMessage(s,s1,s2);
                if(s2.contains("desired")){
                    return;
                }
                mqttMessageResult(s2);//要验s2格式
            }
        });
    }

    MqttMessageArrived mMqttMessageArrived = new MqttMessageArrived() {
        @Override
        public void mqttMessageArrived(String topic, MqttMessage message) {
            final String result = new String(message.getPayload());
            Logg.i(LOG_TAG,"=mqttMessageArrived=>=topic="+topic);
            Logg.i(LOG_TAG,"=mqttMessageArrived=>=message="+result);

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
        sceneActionInfo = getIntent().getParcelableExtra("sceneActionInfo");
        mContext = this;

        tvAction = (TextView) findViewById(R.id.tv_action);
        tvTimer = (TextView) findViewById(R.id.tv_timer);
        tvName = (TextView) findViewById(R.id.tv_name);
        one = (RelativeLayout) findViewById(R.id.rl_one);
//        second = (RelativeLayout) findViewById(R.id.rl_second);
        third = (RelativeLayout) findViewById(R.id.rl_third);
        four = (RelativeLayout) findViewById(R.id.rl_four);
        btnDone = (Button) findViewById(R.id.btn_done);

        iconOne = (ImageView) findViewById(R.id.iv_icon_one);
//        iconSecond = (ImageView) findViewById(R.id.iv_icon_second);
        iconThird = (ImageView) findViewById(R.id.iv_icon_third);
        iconFour = (ImageView) findViewById(R.id.iv_icon_four);

        one.setOnClickListener(this);
//        second.setOnClickListener(this);
        third.setOnClickListener(this);
        four.setOnClickListener(this);
        btnDone.setOnClickListener(this);
    }



    private void processExtraData() {

        int[] icon = new int[]{R.drawable.vector_drawable_ic_117,
                R.drawable.vector_drawable_ic_80,
                R.drawable.vector_drawable_ic_81,
                R.drawable.vector_drawable_ic_device_79,
                R.drawable.vector_drawable_ic_65};

        if ("BULB".equals(sceneActionInfo.getType())) {
            iconOne.setImageResource(icon[3]);
        } else if ("PLUG".equals(sceneActionInfo.getType())) {
            iconOne.setImageResource(icon[1]);
        } else {
            iconOne.setImageResource(icon[1]);
        }

        tvAction.setText(sceneActionInfo.getAction());
        tvName.setText(sceneActionInfo.getName());
        tvTimer.setText("Timer - " + sceneActionInfo.getTimer());

    }


    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.rl_one:
                intent = new Intent(this, ChooseDeviceActivity.class);
                break;
//            case R.id.rl_second:
//                intent = new Intent(this, ToggleActivity.class);
//                break;
            case R.id.rl_third:
                intent = new Intent(this, DoActionActivity.class);
                break;
            case R.id.rl_four:
                intent = new Intent(this, TimerActivity.class);
                break;
            case R.id.btn_done:

//                int ZwController_addEndpointsToGroup(int deviceId, int groupId, int[] arr);
//                if(Const.isRemote){
//                    initIotMqttMessage();
//                    if(HomeActivity.shadowTopic!=null && !HomeActivity.shadowTopic.equals("")){
////                        MqttService mqttService = MqttService.getInstance();
////                        mqttService.publishMqttMessage(HomeActivity.shadowTopic,
////                                "mobile_zwave:addEndpointsToGroup:" + nodeId + ":" + groupId + ":" + arr + ":" + endpointId + ":" + timer);
//
//                        //  arr  需要从nodeInfo中获取
//                        ArrayList nodeInterFaceList = new ArrayList();
//                        nodeInterFaceList.add("arr");
//
//                        MqttDesiredJStrBuilder builder = new MqttDesiredJStrBuilder(Const.subscriptionTopic+"Zwave"+nodeId);
//                        builder.setJsonString( CloudIotData.addEndpointsToGroup(nodeId,endpointId,groupId,nodeInterFaceList) );
//                        AskeyIoTService.getInstance(ZwaveClientApplication.getInstance()).publishDesiredMessage(HomeActivity.shadowTopic, builder);
//                    }
//                }else{
//                    MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
//
//                    ArrayList nodeInterFaceList = new ArrayList();
//                    nodeInterFaceList.add("arr");
//
//                    MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic +"Zwave"+ nodeId, LocalMqttData.addEndpointsToGroup(nodeId,endpointId,groupId,nodeInterFaceList));
////                    MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+"Zwave"+nodeId,
////                            "mobile_zwave:addEndpointsToGroup:" + nodeId + ":" + groupId + ":" + arr + ":" + endpointId + ":" + timer);
//                }
//                mobile_zwave:addEndpointsToGroup:null:3:arr:null:null

                intent = new Intent(this,SceneActionsActivity.class);
                break;

        }

        intent.putExtra("from",ActionSummaryActivity.class.getSimpleName());

        intent.putExtra("sceneActionInfo", sceneActionInfo);

        Log.i(LOG_TAG, "=====getType===" + sceneActionInfo.getType());
        Log.i(LOG_TAG, "=====getAction===" + sceneActionInfo.getAction());
        Log.i(LOG_TAG, "=====getLightValue===" + sceneActionInfo.getLightValue());
        Log.i(LOG_TAG, "=====getName===" + sceneActionInfo.getName());
        Log.i(LOG_TAG, "=====getNodeId===" + sceneActionInfo.getNodeId());
        Log.i(LOG_TAG, "=====getTimer===" + sceneActionInfo.getTimer());
        Log.i(LOG_TAG, "=====getActionId===" + sceneActionInfo.getActionId() + "");
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
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
