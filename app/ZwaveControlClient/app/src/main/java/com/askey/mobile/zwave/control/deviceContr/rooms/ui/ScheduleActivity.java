package com.askey.mobile.zwave.control.deviceContr.rooms.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.application.ZwaveClientApplication;
import com.askey.mobile.zwave.control.data.CloudIotData;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.IotMqttManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.model.ScheduleInfo;
import com.askey.mobile.zwave.control.deviceContr.rooms.schedule.CalendarScheduleView;
import com.askey.mobile.zwave.control.deviceContr.rooms.schedule.OnDateClick;
import com.askey.mobile.zwave.control.home.activity.HomeActivity;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;
import com.askeycloud.webservice.sdk.iot.callback.ShadowReceiveListener;
import com.askeycloud.webservice.sdk.iot.message.builder.MqttDesiredJStrBuilder;
import com.askeycloud.webservice.sdk.service.iot.AskeyIoTService;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ScheduleActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = ScheduleActivity.class.getSimpleName();
    private ToggleButton btnSchedule;
    private TextView tvScheduleStatus;
    private CalendarScheduleView mSchedule;
    private String nodeId = "";
    public Context mContext;
    private List<ScheduleInfo> mScheduleInfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        mContext = this;

        btnSchedule = (ToggleButton) findViewById(R.id.togBtn_schedule);
        tvScheduleStatus = (TextView) findViewById(R.id.tv_schedule_status);
        btnSchedule.setOnClickListener(this);
        initView();

        nodeId = getIntent().getStringExtra("nodeId");


        if (Const.isRemote) {
            initIotMqttMessage();
            if (HomeActivity.shadowTopic != null && !HomeActivity.shadowTopic.equals("")) {
//                MqttDesiredJStrBuilder builder = new MqttDesiredJStrBuilder(Const.subscriptionTopic+"Zwave"+nodeId);
//                builder.setJsonString(CloudIotData.getScheduleActive(nodeId));
//                AskeyIoTService.getInstance(ZwaveClientApplication.getInstance()).publishDesiredMessage(HomeActivity.shadowTopic, builder);

                MqttDesiredJStrBuilder builder1 = new MqttDesiredJStrBuilder(Const.subscriptionTopic + "Zwave" + nodeId);
                builder1.setJsonString(CloudIotData.getScheduleList(nodeId));
                AskeyIoTService.getInstance(ZwaveClientApplication.getInstance()).publishDesiredMessage(HomeActivity.shadowTopic, builder1);
            }
        } else {
            MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
            //获取灯泡状态
//            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+"Zwave"+nodeId, LocalMqttData.getScheduleActive(nodeId));

            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic + "Zwave" + nodeId, LocalMqttData.getScheduleList(nodeId));
            //进来获取active状态呢 没接口

        }
    }

    MqttMessageArrived mMqttMessageArrived = new MqttMessageArrived() {
        @Override
        public void mqttMessageArrived(String topic, MqttMessage message) {
            final String result = new String(message.getPayload());
            Logg.i(TAG, "=mqttMessageArrived=>=topic=" + topic);
            Logg.i(TAG, "=mqttMessageArrived=>=message=" + result);

            if (result.contains("desired")) {
                return;
            }
            mqttMessageResult(result);

        }
    };

    private void initIotMqttMessage() {
        //以下这句为注册监听
        AskeyIoTService.getInstance(this).setShadowReceiverListener(new ShadowReceiveListener() {
            @Override
            public void receiveShadowDocument(String s, String s1, String s2) {
                Logg.i(TAG, "==IotMqttMessageCallback====setShadowReceiverListener==s=" + s);
                Logg.i(TAG, "==IotMqttMessageCallback====setShadowReceiverListener==s1=" + s1);
                Logg.i(TAG, "==IotMqttMessageCallback====setShadowReceiverListener==s2=" + s2);
                IotMqttManagement.getInstance().receiveMqttMessage(s, s1, s2);
                if (s2.contains("desired")) {
                    return;
                }
                mqttMessageResult(s2);//要验s2格式
            }
        });
    }

    private void mqttMessageResult(String result) {
        try {

            JSONObject jsonObject = new JSONObject(result);
            String reported = jsonObject.optString("reported");
            final JSONObject reportedObject = new JSONObject(reported);
            String Interface = reportedObject.optString("Interface");
            if (Interface.equals("getScheduleList")) {
                final String active = reportedObject.getString("active");


                mScheduleInfoList.clear();
                ScheduleInfo scheduleInfo;
                JSONArray days = reportedObject.getJSONArray("day");
                for (int i = 0; i < days.length(); i++) {
                    JSONObject day = days.getJSONObject(i);
                    scheduleInfo = new ScheduleInfo();
                    String dayOfWeek =day.optString("dayOfWeek");
                    if(!dayOfWeek.equals("")){
                        scheduleInfo.setDateName(day.optString("dayOfWeek"));
                        scheduleInfo.setStartTime(day.optString("StartTime"));
                        scheduleInfo.setEndTime(day.optString("EndTime"));
                        mScheduleInfoList.add(scheduleInfo);
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSchedule.setScheduleDate(mScheduleInfoList);
                        mSchedule.isFirst = true;
                        if ("false".equals(active)) {
                            btnSchedule.setChecked(false);
                        } else {
                            btnSchedule.setChecked(true);
                        }
                    }
                });

            }
            }catch(JSONException e){
                e.printStackTrace();
            }

        }






    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.togBtn_schedule:
                //暂存在这个位置
                if(Const.isRemote){
                    initIotMqttMessage();
                    if(HomeActivity.shadowTopic!=null && !HomeActivity.shadowTopic.equals("")){
                        MqttDesiredJStrBuilder builder = new MqttDesiredJStrBuilder(Const.subscriptionTopic+"Zwave"+nodeId);
                        builder.setJsonString(CloudIotData.setScheduleActive(nodeId,btnSchedule.isChecked()));
                        AskeyIoTService.getInstance(ZwaveClientApplication.getInstance()).publishDesiredMessage(HomeActivity.shadowTopic, builder);
                    }
                }else{
                    MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+"Zwave"+nodeId, LocalMqttData.setScheduleActive(nodeId,btnSchedule.isChecked()));
                }
                break;
        }
    }


    private void initView() {
        mScheduleInfoList = new ArrayList<>();

        mSchedule = (CalendarScheduleView) findViewById(R.id.schedule);

        mSchedule.setOnDateClick(new OnDateClick() {
            @Override
            public void onClick(int year, int month, int data) {
//                Log.e("TestActivity", "日历onClick点击事件可用" + data);
                    setCommand(null, mSchedule.currentWeek, mSchedule.mStartTime, mSchedule.mEndTime);

            }
        });

        mSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("TestActivity", "普通onClick点击事件可用");
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
    }

    public void setCommand(String light,String week,int startTime,int endTime) {
        if(Const.isRemote){
            initIotMqttMessage();
            if(HomeActivity.shadowTopic!=null && !HomeActivity.shadowTopic.equals("")){
                MqttDesiredJStrBuilder builder1 = new MqttDesiredJStrBuilder(Const.subscriptionTopic+"Zwave"+nodeId);
                builder1.setJsonString(CloudIotData.setSchedule(nodeId,"155",week,startTime + ":00",endTime + ":00"));
                AskeyIoTService.getInstance(ZwaveClientApplication.getInstance()).publishDesiredMessage(HomeActivity.shadowTopic, builder1);
            }
        }else{
            if (btnSchedule.isChecked()) {
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic + "Zwave" + nodeId,
                        LocalMqttData.setSchedule(nodeId, "155",
                                week,
                                startTime + ":00",
                                endTime + ":00",
                                "true"));
            } else {
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+"Zwave"+nodeId,
                        LocalMqttData.setSchedule(nodeId,"155",
                                week,
                                startTime + ":00",
                                endTime + ":00",
                                "false"));
            }

        }
    }

    public void removeCommand(String week) {
        if(Const.isRemote){
            initIotMqttMessage();
            if(HomeActivity.shadowTopic!=null && !HomeActivity.shadowTopic.equals("")){
                MqttDesiredJStrBuilder builder = new MqttDesiredJStrBuilder(Const.subscriptionTopic+"Zwave"+nodeId);
                builder.setJsonString(CloudIotData.removeSchedule(nodeId,week));
                AskeyIoTService.getInstance(ZwaveClientApplication.getInstance()).publishDesiredMessage(HomeActivity.shadowTopic, builder);

            }
        }else{
            //获取灯泡状态
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+"Zwave"+nodeId, LocalMqttData.removeSchedule(nodeId,week));

        }
    }

}
