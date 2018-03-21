package com.askey.mobile.zwave.control.deviceContr.scenes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.adapter.CommandsAdapter;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.model.ProvisionInfo;
import com.askey.mobile.zwave.control.deviceContr.model.ScenesInfo;
import com.askey.mobile.zwave.control.home.adapter.CommandListAdapter;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.List;

public class DeviceTestActivity extends BaseActivity {
    private final String TAG = DeviceTestActivity.class.getSimpleName();
    RecyclerView commList;
    CommandListAdapter adapter;
    List<String> test;
    ProvisionInfo provisionInfo;
    String nodeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_plug_test);

        commList = (RecyclerView) findViewById(R.id.comm_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        commList.setLayoutManager(layoutManager);
        provisionInfo = getIntent().getParcelableExtra("provisionInfo");
        nodeId = provisionInfo.getNodeId();
        test = new ArrayList<>();
        test.add("Command Queue相关接口");
        test.add("Network Health Check功能");
        test.add("Smart Start相关API");
        test.add("Controller相关接口");
        test.add("Command Class Battery");
        test.add("Command Class Basic ver 1~2");
        test.add("Command Class Switch Multi-Level");
        test.add("Command Class Configuration");
        test.add("Command Class Power Level");
        test.add("Command Class Switch All");
        test.add("Command Class Switch Binary ver 1~2");
        test.add("Command Class Sensor Binary v2");
        test.add("Command Class Meter v3");
        test.add("Command Class Wake Up");
        test.add("Command Class Door Lock");
        test.add("Command Class User Code");
        test.add("Command Class Protection v1-v3");
        test.add("Command Class Indicator v1");
        test.add("Command Class Door Lock Looging");
        test.add("Command Class Language");
        test.add("Command Class Switch Color");
        test.add("Command Class Barrier Operator");
        test.add("Command Class Basic Tariff Info");
        test.add("Command Class Association & Multi-Channel Association");
        test.add("Command Class Notification version 4");
        test.add("Command Class Central Scene version 2");
        test.add("Command Class Scene Actuator Conf ver 1");
        test.add("Command Class Firmware Update Md");
        test.add("Command Class Multi Cmd");



        adapter = new CommandListAdapter(test);
        commList.setAdapter(adapter);
        adapter.setOnItemClickListener(new CommandListAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, String str) {
                Log.i("onItemClick", "====str="+str);
                Intent intent = new Intent();
                intent.setClass(mContext,APITestActivity.class);
                intent.putExtra("title",str);
                startActivity(intent);
            }
        });
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

    private void getCmdList() {
        Log.d(TAG, "======getCmdList=======");
        MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getDeviceInfo(nodeId));

    }

    private void mqttMessageResult(String mqttResult) {


    }

}

