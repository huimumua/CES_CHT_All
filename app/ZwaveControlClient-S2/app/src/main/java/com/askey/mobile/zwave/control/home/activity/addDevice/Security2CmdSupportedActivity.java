package com.askey.mobile.zwave.control.home.activity.addDevice;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.net.TcpClient;
import com.askey.mobile.zwave.control.home.adapter.CommandListAdapter;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;
import com.askey.mobile.zwave.control.util.PreferencesUtils;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 如果没有cmd ， 提示 ：The request cmd interface not found, make sure this node supported it indeed
 * Created by skysoft on 2018/3/30.
 */

public class Security2CmdSupportedActivity extends BaseActivity {
    private static final String TAG = "Security2CmdSupportedActivity";
    private Button enterDskBtn;
    private String nodeId;
    private TextView nodeIdTextView;
    private RecyclerView cmdSupportList;
    CommandListAdapter cmdAdapter;
    List<String> cmdSupClass;
    private CommandListAdapter adapter;
    private String resetActivityNetworkRole;
    private TextView isSecondary;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_s2_cmd_support);
        init();
    }

    private void init() {
        nodeId = getIntent().getStringExtra("nodeId");
        nodeIdTextView = (TextView) findViewById(R.id.cmd_node_id);
        isSecondary = (TextView) findViewById(R.id.isSecondary);
        cmdSupportList = (RecyclerView) findViewById(R.id.cmd_support_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        cmdSupportList.setLayoutManager(layoutManager);

        cmdSupClass = new ArrayList<>();
        adapter = new CommandListAdapter(cmdSupClass);
        cmdSupportList.setAdapter(adapter);

        enterDskBtn = (Button) findViewById(R.id.exit);
        enterDskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        resetActivityNetworkRole = (String) PreferencesUtils.get(this, "resetrole", "");
        Log.i("==resetActi33", "" + resetActivityNetworkRole);
        //Secondary
        if (resetActivityNetworkRole != null & resetActivityNetworkRole.equals("SECONDARY")) {
            cmdSupportList.setVisibility(View.GONE);
            isSecondary.setVisibility(View.VISIBLE);

        } else {
            isSecondary.setVisibility(View.GONE);
            cmdSupportList.setVisibility(View.VISIBLE);
        }
        MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setMqttDataJson("getSecurity2CmdSupported", nodeId));
//        showWaitingDialog();
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
//            stopWaitDialog();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    message(result);
                }
            });
        }
    };

    private void message(String result) {

        try {
            JSONObject jsonObject = new JSONObject(result);
            String reported = jsonObject.optString("reported");
            JSONObject reportedObject = new JSONObject(reported);
            String messageType = reportedObject.optString("MessageType");
            if ("Supported S2 Cmd Report".equals(messageType)) {
                String nodeId = reportedObject.optString("Node id");
                String cmdInfo = reportedObject.optString("Cmdclass");

                nodeIdTextView.setText(String.format(getResources().getString(R.string.node_id), nodeId));
                String[] cmdClassArray = cmdInfo.split(",");

                cmdSupClass.clear();
                for (int i = 0; i < cmdClassArray.length; i++) {
                    cmdSupClass.add(cmdClassArray[i]);
                }
            }
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unrigister();
    }

    private void unrigister() {
        if (mMqttMessageArrived != null) {
            MQTTManagement.getSingInstance().unrigister(mMqttMessageArrived);
        }
    }
}
