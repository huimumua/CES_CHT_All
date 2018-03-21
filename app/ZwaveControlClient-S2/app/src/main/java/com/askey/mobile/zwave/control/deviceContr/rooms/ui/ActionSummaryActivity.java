package com.askey.mobile.zwave.control.deviceContr.rooms.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.model.ScenesInfo;
import com.askey.mobile.zwave.control.deviceContr.scenes.SceneActionsActivity;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class ActionSummaryActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String LOG_TAG = ActionSummaryActivity.class.getSimpleName();
    private RelativeLayout one,second,third,four;
    private Button btnDone;
    private ImageView iconOne,iconSecond,iconThird,iconFour;
    private TextView tvAction;
    private TextView tvTimer;
    private TextView tvName;
    private Intent fromIntent;
    private String fromActivity;
    private Context mContext;
    private ArrayList<ScenesInfo> scenesInfos;
    private ScenesInfo scenesInfo;

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
        scenesInfo = getIntent().getParcelableExtra("scenesInfo");
        processExtraData();

    }

    MqttMessageArrived mMqttMessageArrived = new MqttMessageArrived() {
        @Override
        public void mqttMessageArrived(String topic, MqttMessage message) {
            final String result = new String(message.getPayload());
            Logg.i(LOG_TAG,"=mqttMessageArrived=>=topic="+topic);
            Logg.i(LOG_TAG,"=mqttMessageArrived=>=message="+result);

            Intent intent = new Intent(ActionSummaryActivity.this,SceneActionsActivity.class);
            intent.putExtra("from",ActionSummaryActivity.class.getSimpleName());
            intent.putExtra("scenesInfo", scenesInfo);
            startActivity(intent);
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
        scenesInfo = getIntent().getParcelableExtra("scenesInfo");
        mContext = this;

        scenesInfos = new ArrayList<>();

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

        if ("BULB".equals(scenesInfo.getCategory())) {
            iconOne.setImageResource(icon[3]);
        } else if ("PLUG".equals(scenesInfo.getCategory())) {
            iconOne.setImageResource(icon[1]);
        } else {
            iconOne.setImageResource(icon[1]);
        }

        tvAction.setText(scenesInfo.getTargetSatus());
        tvName.setText(scenesInfo.getDeviceName());
        tvTimer.setText("Timer - " + scenesInfo.getTimer());

    }


    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.rl_one:
                intent = new Intent(this, ChooseDeviceActivity.class);
                intent.putExtra("from",ActionSummaryActivity.class.getSimpleName());
                intent.putExtra("scenesInfo", scenesInfo);
                startActivity(intent);
                break;
//            case R.id.rl_second:
//                intent = new Intent(this, ToggleActivity.class);
//                break;
            case R.id.rl_third:
                intent = new Intent(this, DoActionActivity.class);

                break;
            case R.id.rl_four:
                intent = new Intent(this, TimerActivity.class);
                intent.putExtra("from",ActionSummaryActivity.class.getSimpleName());
                intent.putExtra("scenesInfo", scenesInfo);
                break;
            case R.id.btn_done:
                scenesInfos.add(scenesInfo);
                MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
//                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+"Zwave"+scenesInfo.getNodeId(),
//                        LocalMqttData.setSceneAction(scenesInfo.getScenesName(),scenesInfo.getIconName(),scenesInfos));
                break;

        }



        Log.i(LOG_TAG, "=====getType===" + scenesInfo.getCategory());
        Log.i(LOG_TAG, "=====getAction===" + scenesInfo.getTargetSatus());
        Log.i(LOG_TAG, "=====getLightValue===" + scenesInfo.getTargetColor());
        Log.i(LOG_TAG, "=====getName===" + scenesInfo.getDeviceName());
        Log.i(LOG_TAG, "=====getNodeId===" + scenesInfo.getNodeId());
        Log.i(LOG_TAG, "=====getTimer===" + scenesInfo.getTimer());
        Log.i(LOG_TAG, "=====getActionId===" + scenesInfo.getScenesId() + "");

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
