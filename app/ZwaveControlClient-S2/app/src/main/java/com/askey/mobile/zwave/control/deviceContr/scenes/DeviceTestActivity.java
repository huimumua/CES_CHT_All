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
import android.widget.LinearLayout;
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
import com.askey.mobile.zwave.control.home.activity.addDevice.RemoveFailActivity;
import com.askey.mobile.zwave.control.home.activity.addDevice.ReplaceFailActivity;
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
    String securityStatus, nodeId, homeId, deviceType, deviceName;
    private TextView homeIdTextView, nodeIdTextView, securityStatusTextView, deviceTypeTextView, title, nodeIsFailed;
    private LinearLayout commandClassLayout, deviceNodeIsFailedLayout;
    private Button remove, replace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_plug_test);

        commandClassLayout = (LinearLayout)findViewById(R.id.command_class_layout);
        deviceNodeIsFailedLayout = (LinearLayout)findViewById(R.id.failed_node_layout);
        remove = (Button) findViewById(R.id.remove_failed_node);
        replace = (Button) findViewById(R.id.replace_failed_node);

        commList = (RecyclerView) findViewById(R.id.comm_list);
        title = (TextView) findViewById(R.id.command_class_title);

        homeIdTextView = (TextView) findViewById(R.id.home_id);
        nodeIdTextView = (TextView) findViewById(R.id.node_id);
        securityStatusTextView = (TextView) findViewById(R.id.security_status);
        deviceTypeTextView = (TextView) findViewById(R.id.device_type);
        nodeIsFailed = (TextView) findViewById(R.id.node_is_failed);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        commList.setLayoutManager(layoutManager);

        deviceName = getIntent().getStringExtra("deviceName");
        nodeId = getIntent().getStringExtra("nodeId");

        title.setText(String.format(getResources().getString(R.string.device_name_2), deviceName));

        interfaceClass = new ArrayList<>();

//        interfaceClass.add("Network Health Check功能");
//        interfaceClass.add("Smart Start相关API");

        adapter = new CommandListAdapter(interfaceClass);
        commList.setAdapter(adapter);
        adapter.setOnItemClickListener(new CommandListAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, String str) {
                Log.i("onItemClick", "====str=" + str);
                Intent intent = new Intent();
                intent.setClass(mContext, APITestActivity.class);
                intent.putExtra("title", str);

                intent.putExtra("nodeId", getIntent().getStringExtra("nodeId"));
                startActivity(intent);
            }
        });

        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, RemoveFailActivity.class);
                intent.putExtra("nodeId", nodeId);
                intent.putExtra("roomName", "MyHome");
                startActivity(intent);
                finish();
            }
        });

        replace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ReplaceFailActivity.class);
                intent.putExtra("nodeId", nodeId);
                intent.putExtra("roomName", "MyHome");
                startActivity(intent);
                finish();
            }
        });

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

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mqttMessageResult(result);
                }
            });

        }
    };

    private void getCmdList() {
        Log.d(TAG, "======getCmdList=======");
        MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);

        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.checkNodeIsFailed(nodeId));
        //MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getSpecifyDeviceInfo(nodeId));

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

                    JSONArray endpointList = detailTmp.getJSONArray("EndPoint List");
                    for (int j = 0; j < endpointList.length(); j++) {
                        JSONObject endpointTmp = endpointList.getJSONObject(j);
                        deviceType = endpointTmp.optString("ZWave+ device type"); //设备类型
                        JSONArray interfaceList = endpointTmp.getJSONArray("Interface List");
                        for (int k = 0; k < interfaceList.length(); k++) {
                            JSONObject interfaceTmp = interfaceList.getJSONObject(k);
                            Log.i(TAG, "--------------interface class:" + interfaceTmp.optString("Interface Class"));
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
            } else if ("Node Is Failed Check Report".equals(messageType)) { //检查node所对应Device是否活着
                String checkNodeStatus = reportedObject.optString("Status");
                Log.i(TAG, "Node Is Failed Check Report Status:"+checkNodeStatus);
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getSpecifyDeviceInfo(nodeId));
                //Alive 显示command class，Down显示replace按钮
                if (checkNodeStatus.equals("Alive")) {  //Alive活着 / Down(failed)死了
                    commandClassLayout.setVisibility(View.VISIBLE);
                } else if (checkNodeStatus.equals("Down(failed)")) {
                    nodeIsFailed.setVisibility(View.VISIBLE);
                    deviceNodeIsFailedLayout.setVisibility(View.VISIBLE);
                } else {
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
