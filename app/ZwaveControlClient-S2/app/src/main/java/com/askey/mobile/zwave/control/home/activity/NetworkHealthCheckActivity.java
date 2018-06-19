package com.askey.mobile.zwave.control.home.activity;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.model.NetworkHealthInfo;
import com.askey.mobile.zwave.control.deviceContr.net.SocketTransceiver;
import com.askey.mobile.zwave.control.deviceContr.net.TCPReceive;
import com.askey.mobile.zwave.control.deviceContr.net.TcpClient;
import com.askey.mobile.zwave.control.home.adapter.NetworkHealthAdapter;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NetworkHealthCheckActivity extends AppCompatActivity {
    private static final String LOG_TAG = NetworkHealthCheckActivity.class.getSimpleName();
    private TextView networkStatus, alwaysOnDeviceException;
    private RecyclerView networkHealthListView;
    private List<NetworkHealthInfo> networkHealthInfoList;
    private NetworkHealthAdapter networkHealthAdapter;
    private NetworkHealthInfo networkHealthInfo;
    private ProgressBar progressBar;

    /*
     * ??Network Health Check?????node, ???????node???????????node????,
     * ???????????node??,???listView?Item??
     */
    private String lastNodeId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_health);
        networkStatus = (TextView) findViewById(R.id.network_health_status);
        alwaysOnDeviceException = (TextView) findViewById(R.id.always_on_device);
        progressBar = (ProgressBar) findViewById(R.id.network_health_progress_bar);
        networkHealthListView = (RecyclerView) findViewById(R.id.network_health_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        networkHealthListView.setLayoutManager(layoutManager);

        networkHealthInfoList = new ArrayList<>();
        networkHealthAdapter = new NetworkHealthAdapter(networkHealthInfoList);
        networkHealthListView.setAdapter(networkHealthAdapter);

        MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setMqttDataJson("startNetworkHealthCheck"));

    }

    MqttMessageArrived mMqttMessageArrived = new MqttMessageArrived() {
        @Override
        public void mqttMessageArrived(String topic, MqttMessage message) {
            final String result = new String(message.getPayload());
            Logg.i(LOG_TAG, "=mqttMessageArrived=>=topic=" + topic);
            Logg.i(LOG_TAG, "=mqttMessageArrived=>=message=" + result);
            if (result.contains("desired")) {
                return;
            }
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
            if ("Network IMA Info Report".equals(messageType)) {
                final String nodeId = reportedObject.optString("Direct nodeid");
                final String netHealth = reportedObject.optString("Network Health");
                final String rsValue = reportedObject.optString("RSSI hops value");

                Log.i(LOG_TAG, "----------Network IMA Info Report");
                if (!lastNodeId.equals(nodeId)) {
                    Log.i(LOG_TAG, "-----------lastNodeId != nodeId: " + nodeId + "lastNodeId: " + lastNodeId);
                    lastNodeId = nodeId;
                    networkHealthInfo = new NetworkHealthInfo();

                    networkHealthInfo.setDirectNodeId(nodeId);
                    networkHealthInfo.setNetworkHealth(netHealth);
                    networkHealthInfo.setRssiHopsValue(rsValue);

                    networkHealthInfoList.add(networkHealthInfo);
                    networkHealthAdapter.notifyDataSetChanged();

                }


            } else if ("Network Health Check".equals(messageType)) {

                if (reported.contains("Error")) { //??????0???,?? -17 ?-13

                    progressBar.setVisibility(View.GONE);//???????progressBar
                    int errorCode = reportedObject.optInt("Error");
                    if (errorCode == -17) {
                        showPromptDialog(getResources().getString(R.string.prompt_try_again));
                    } else if (errorCode == -13) {//????????,???? -13
                        //There is no always on node exists in network.
                        showPromptDialog(getResources().getString(R.string.node_not_exists_network));
                    } else {
                        showPromptDialog(String.valueOf(errorCode));
                    }

                } else {
                    String status = reportedObject.optString("Status");
                    networkStatus.setText(status);
                    if(status.equals("OP Done")){ //OP Done????????
                        progressBar.setVisibility(View.GONE);
                        if(networkHealthInfoList.size()<1){
                            alwaysOnDeviceException.setVisibility(View.VISIBLE);
                        }
                    }
                }

            } else if ("Network RSSI Info Report".equals(messageType)) {
                //这种Device很少见，暂时不处理
            }

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

    private void showPromptDialog(String message) {
        final AlertDialog.Builder addDialog = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.dialog_normal_layout, null);
        addDialog.setView(view);
        final AlertDialog alertDialog = addDialog.create();
        alertDialog.setCanceledOnTouchOutside(false);

        TextView title = (TextView) view.findViewById(R.id.title);
        TextView promptMessage = (TextView) view.findViewById(R.id.message);
        title.setText("Warning");
        promptMessage.setText(message);
        TextView positiveButton = (TextView) view.findViewById(R.id.positiveButton);
        TextView negativeButton = (TextView) view.findViewById(R.id.negativeButton);
        positiveButton.setText("retry");
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setMqttDataJson("startNetworkHealthCheck"));
                alertDialog.dismiss();
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //????,????
                if (TcpClient.getInstance().isConnected()) {
                    TcpClient.getInstance().getTransceiver().send("mobile_zwave:stopRemoveDevice:Zwave"); //?????????
                }
                alertDialog.dismiss();
                finish();

            }
        });

        alertDialog.show();
    }

}
