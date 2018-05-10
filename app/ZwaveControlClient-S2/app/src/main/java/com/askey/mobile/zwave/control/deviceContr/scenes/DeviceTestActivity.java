package com.askey.mobile.zwave.control.deviceContr.scenes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.adapter.CommandsAdapter;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.model.ProvisionInfo;
import com.askey.mobile.zwave.control.deviceContr.model.ScenesInfo;
import com.askey.mobile.zwave.control.home.activity.addDevice.DeleteDeviceActivity;
import com.askey.mobile.zwave.control.home.adapter.CommandListAdapter;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DeviceTestActivity extends BaseActivity {
    private final String TAG = DeviceTestActivity.class.getSimpleName();
    RecyclerView commList;
    CommandListAdapter adapter;
    ProvisionInfo provisionInfo;
    List<String> interfaceClass;
    String securityStatus,nodeId,homeId,deviceType;
    private TextView homeIdTextView, nodeIdTextView, securityStatusTextView, deviceTypeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_plug_test);

        commList = (RecyclerView) findViewById(R.id.comm_list);
        TextView title = (TextView)findViewById(R.id.command_class_title);

        homeIdTextView = (TextView)findViewById(R.id.home_id);
        nodeIdTextView = (TextView)findViewById(R.id.node_id);
        securityStatusTextView = (TextView)findViewById(R.id.security_status);
        deviceTypeTextView = (TextView)findViewById(R.id.device_type);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        commList.setLayoutManager(layoutManager);
        title.setText(getIntent().getStringExtra("deviceName"));

        nodeId = getIntent().getStringExtra("nodeId");

        interfaceClass = new ArrayList<>();

//        interfaceClass.add("Network Health Check功能");
//        interfaceClass.add("Smart Start相关API");

        adapter = new CommandListAdapter(interfaceClass);
        commList.setAdapter(adapter);
        adapter.setOnItemClickListener(new CommandListAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, String str) {
                Log.i("onItemClick", "====str="+str);
                Intent intent = new Intent();
                intent.setClass(mContext,APITestActivity.class);
                intent.putExtra("title",str);

                intent.putExtra("nodeId",getIntent().getStringExtra("nodeId"));
                startActivity(intent);
            }
        });

//test
//        interfaceClass.add("COMMAND_CLASS_BATTERY");
//        interfaceClass.add("COMMAND_CLASS_BASIC");
//        interfaceClass.add("COMMAND_CLASS_SWITCH_MULTILEVEL");
//        interfaceClass.add("COMMAND_CLASS_CONFIGURATION");
//        interfaceClass.add("COMMAND_CLASS_POWERLEVEL");
//        interfaceClass.add("COMMAND_CLASS_SWITCH_ALL");
//        interfaceClass.add("COMMAND_CLASS_SWITCH_BINARY");
//        interfaceClass.add("COMMAND_CLASS_SENSOR_BINARY");
//        interfaceClass.add("COMMAND_CLASS_METER");
//        interfaceClass.add("COMMAND_CLASS_WAKE_UP");
//        interfaceClass.add("COMMAND_CLASS_DOOR_LOCK");
//        interfaceClass.add("COMMAND_CLASS_USER_CODE");
//        interfaceClass.add("COMMAND_CLASS_PROTECTION");
//        interfaceClass.add("COMMAND_CLASS_INDICATOR");
//        interfaceClass.add("COMMAND_CLASS_DOOR_LOCK_LOOGING");
//        interfaceClass.add("COMMAND_CLASS_LANGUAGE");
//        interfaceClass.add("COMMAND_CLASS_SWITCH_COLOR");
//        interfaceClass.add("COMMAND_CLASS_BARRIER_OPERATOR");
//        interfaceClass.add("COMMAND_CLASS_BASIC_TARIFF_INFO");
//        interfaceClass.add("COMMAND_CLASS_ASSOCIATION");
//        interfaceClass.add("COMMAND_CLASS_NOTIFICATION_VERSION");
//        interfaceClass.add("COMMAND_CLASS_CENTRAL_SCENE_VERSION");
//        interfaceClass.add("COMMAND_CLASS_SCENE_ACTUATOR");
//        interfaceClass.add("COMMAND_CLASS_FIRMWARE_UPGRADE_MD");
//        interfaceClass.add("COMMAND_CLASS_MUTILCMD");
//        interfaceClass.add("COMMAND_CLASS_SENSOR_MULTILEVEL");

       getCmdList();
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
        MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getSpecifyDeviceInfo(nodeId));

    }

    private void mqttMessageResult(String mqttResult) {

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(mqttResult);
            String reported = jsonObject.optString("reported");
            JSONObject reportedObject = new JSONObject(reported);
            String Interface = reportedObject.optString("Interface");
            String messageType = reportedObject.optString("MessageType");

            if ("Specify Node Info".equals(messageType)) {
                JSONArray detailInfo = reportedObject.optJSONArray("Detialed Node Info");
                for (int i = 0; i < detailInfo.length(); i++) {
                    JSONObject detailTmp = detailInfo.getJSONObject(i);
                    securityStatus = detailTmp.optString("Node security inclusion status");
                    homeId = detailTmp.optString("Home id");
                    deviceType = detailTmp.optString("Device type"); //TODO:需要确认huilin有没有加这个参数

                    JSONArray endpointList = detailTmp.getJSONArray("EndPoint List");
                    for (int j=0 ; j < endpointList.length();j++) {
                        JSONObject endpointTmp = endpointList.getJSONObject(j);
                        JSONArray interfaceList = endpointTmp.getJSONArray("Interface List");
                        for (int k=0; k < interfaceList.length(); k++) {
                            JSONObject interfaceTmp = interfaceList.getJSONObject(k);
                            Log.i(TAG, "--------------interface class:"+interfaceTmp.optString("Interface Class") );
                            interfaceClass.add(interfaceTmp.optString("Interface Class"));
                        }
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        homeIdTextView.setText(String.format(getString(R.string.home_id), homeId));
                        nodeIdTextView.setText(String.format(getString(R.string.node_id), nodeId));
                        securityStatusTextView.setText(String.format(getString(R.string.security_status), securityStatus));
                        deviceTypeTextView.setText(String.format(getString(R.string.device_type_s), deviceType));

                        adapter.notifyDataSetChanged();
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}


