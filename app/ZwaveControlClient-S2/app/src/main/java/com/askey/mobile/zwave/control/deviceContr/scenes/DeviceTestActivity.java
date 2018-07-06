package com.askey.mobile.zwave.control.deviceContr.scenes;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.adapter.ListViewAdapter;
import com.askey.mobile.zwave.control.deviceContr.adapter.MyExpandableListViewAdapter;
import com.askey.mobile.zwave.control.deviceContr.adapter.SpinnerAdapter;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.model.Info;
import com.askey.mobile.zwave.control.deviceContr.model.ProvisionInfo;
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
    private ExpandableListView expandableListView;
    private List<Info> data;
    private MyExpandableListViewAdapter myExpandableListViewAdapter;
    private Spinner spinner;
    private List<String> spinnerData;
    private SpinnerAdapter spinnerAdapter;
    private TextView endpointId_textview;
    private PopupWindow pw;
    private JSONArray interfaceList;
    private JSONArray endpointList;
    private JSONArray endpointList2;
    private List<String> security = new ArrayList<>();
    private List<String> unsecurity = new ArrayList<>();
    private JSONArray endpointList1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_plug_test);

        commandClassLayout = (LinearLayout) findViewById(R.id.command_class_layout);
        deviceNodeIsFailedLayout = (LinearLayout) findViewById(R.id.failed_node_layout);
        remove = (Button) findViewById(R.id.remove_failed_node);
        replace = (Button) findViewById(R.id.replace_failed_node);
        spinner = (Spinner) findViewById(R.id.spinner);
        expandableListView = (ExpandableListView) findViewById(R.id.expandlistview);
        spinnerData = new ArrayList<>();
        endpointId_textview = (TextView) findViewById(R.id.endpointId_text);

        endpointId_textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View myView = getLayoutInflater().inflate(R.layout.pop, null);
                //通过view 和宽·高，构造PopopWindow
                pw = new PopupWindow(myView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                //popupWindow.setBackgroundDrawable(new BitmapDrawable());
//        pw.setBackgroundDrawable(getResources().getColor(
//                //此处为popwindow 设置背景，同事做到点击外部区域，popwindow消失
//                R.drawable.diaolog_bg));
                //设置焦点为可点击
//        pw.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
                pw.setTouchable(true); // 设置popupwindow可点击
                pw.setOutsideTouchable(true); // 设置popupwindow外部可点击
                pw.setBackgroundDrawable(new BitmapDrawable(null, ""));
                pw.setFocusable(true);//可以试试设为false的结果
                // pw.setAnimationStyle(R.style.AnimTools);
                //将window视图显示在myButton下面
                pw.showAsDropDown(endpointId_textview);
                ListView lv = (ListView) myView.findViewById(R.id.lv_pop);
                lv.setAdapter(new ListViewAdapter(DeviceTestActivity.this, spinnerData));
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        pw.dismiss();
                        data.clear();
                        interfaceClass.clear();
                        security.clear();
                        unsecurity.clear();
                        endpointId_textview.setText("" + spinnerData.get(position));
                        JSONObject endpointTmp = null;
                        try {
                            endpointTmp = endpointList1.getJSONObject(position);
                            String EndpointId = endpointTmp.optString("Endpoint id");
                            //   spinnerData.add("EndpointId" + EndpointId);

                            Log.i("=====EndpointId2", "" + EndpointId);

                            deviceType = endpointTmp.optString("ZWave+ device type"); //设备类型

                            interfaceList = endpointTmp.getJSONArray("Interface List");

                            for (int k = 0; k < interfaceList.length(); k++) {
                                JSONObject interfaceTmp = interfaceList.getJSONObject(k);
                                Log.i("====解析", "--------------interface class:" + interfaceTmp.optString("Interface Class"));
                                // level代表是unsecure 还是S0 Supported S2Supported

                                String level = interfaceTmp.optString("Interface security level");
                                String interfaceclass = interfaceTmp.optString("Interface Class");
//                            if (level.equals("unsecure")){
//                                unsecure.add(interfaceclass);
//
//                            }
                                if (level.equals("Secure")) {
                                    security.add(interfaceclass);

                                }
                                if (level.equals("UnSecure")) {
                                    unsecurity.add(interfaceclass);

                                }
                                interfaceClass.add(interfaceTmp.optString("Interface Class"));
                            }

                            if (security.size() > 0) {
                                Info info = new Info();
                                info.setName("Secure");
                                info.setData(security);
                                data.add(info);
                            }

                            if (unsecurity.size() > 0) {
                                Info info = new Info();
                                info.setName("UnSecure");
                                info.setData(unsecurity);
                                data.add(info);
                            }
//                if (unsecure.size()>0){
//                    Info info = new Info();
//                    info.setName("unsecure");
//                    info.setData(unsecure);
//                    data.add(info);
//                }
                            myExpandableListViewAdapter.notifyDataSetChanged();
                            Log.i("=====sieze为", "list1" + security.size() + "===" + "" + unsecurity.size() + "===" + interfaceClass.size());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        });


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
        data = new ArrayList<>();
        myExpandableListViewAdapter = new MyExpandableListViewAdapter(this, data);

        expandableListView.setAdapter(myExpandableListViewAdapter);
        myExpandableListViewAdapter.setNodeId(nodeId);

        expandableListView.setGroupIndicator(null);
        title.setText(String.format(getResources().getString(R.string.device_name_2), deviceName));

        interfaceClass = new ArrayList<>();

//        interfaceClass.add("Network Health Check功能");
//        interfaceClass.add("Smart Start相关API");

        adapter = new CommandListAdapter(interfaceClass);
        commList.setAdapter(adapter);

        if (nodeId.equals("1")) {
            adapter.setOnItemClickListener(null);
        } else {
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
        }

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

        if (!nodeId.equals("1")) {
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.checkNodeIsFailed(nodeId));
        } else {
            commandClassLayout.setVisibility(View.VISIBLE);
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getSpecifyDeviceInfo(nodeId));
        }

    }

    private void mqttMessageResult(String mqttResult) {

//        s0Supported = new ArrayList<>();
//        s2Supported = new ArrayList<>();

        Log.i("====result", "" + mqttResult);
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(mqttResult);
            String reported = jsonObject.optString("reported");
            JSONObject reportedObject = new JSONObject(reported);
            String Interface = reportedObject.optString("Interface");
            String messageType = reportedObject.optString("MessageType");

            if ("Specify Node Info".equals(messageType)) {
                JSONArray detailInfo = reportedObject.optJSONArray("Detialed Node Info");
                if (interfaceClass.size() > 0) {
                    interfaceClass.clear();
                }
                for (int i = 0; i < detailInfo.length(); i++) {
                    JSONObject detailTmp = detailInfo.getJSONObject(i);
                    securityStatus = detailTmp.optString("Node security inclusion status");
                    homeId = detailTmp.optString("Home id");

                    endpointList1 = detailTmp.getJSONArray("EndPoint List");
                    Log.i("====endpointList", "" + endpointList1.length());


                    for (int j = 0; j < endpointList1.length(); j++) {
                        JSONObject endpointTmp = endpointList1.getJSONObject(j);
                        deviceType = endpointTmp.optString("ZWave+ device type"); //设备类型
                        String EndpointId = endpointTmp.optString("Endpoint id");
                        spinnerData.add("EndpointId:" + EndpointId);
                        JSONArray interfaceList = endpointTmp.getJSONArray("Interface List");
                        if (j == 0) {
                            for (int k = 0; k < interfaceList.length(); k++) {
                                JSONObject interfaceTmp = interfaceList.getJSONObject(k);
                                Log.i(TAG, "--------------interface class:" + interfaceTmp.optString("Interface Class"));
                                // level代表是unsecure 还是S0 Supported S2Supported

                                String level = interfaceTmp.optString("Interface security level");
                                String interfaceclass = interfaceTmp.optString("Interface Class");
//                            if (level.equals("unsecure")){
//                                unsecure.add(interfaceclass);
//
//                            }
                                if (level.equals("UnSecure")) {
                                    unsecurity.add(interfaceclass);

                                }
                                if (level.equals("Secure")) {
                                    security.add(interfaceclass);

                                }
                                interfaceClass.add(interfaceTmp.optString("Interface Class"));
                            }
                        }

                    }
                }

                if (security.size() > 0) {
                    Info info = new Info();
                    info.setName("Secure");
                    info.setData(security);
                    data.add(info);
                }
//                if (unsecure.size()>0){
//                    Info info = new Info();
//                    info.setName("unsecure");
//                    info.setData(unsecure);
//                    data.add(info);
//                }
                if (unsecurity.size() > 0) {
                    Info info = new Info();
                    info.setName("UnSecure");
                    info.setData(unsecurity);
                    data.add(info);

                }
                myExpandableListViewAdapter.notifyDataSetChanged();

                homeIdTextView.setText(String.format(getString(R.string.home_id), homeId));
                nodeIdTextView.setText(String.format(getString(R.string.node_id), nodeId));
                securityStatusTextView.setText(String.format(getString(R.string.security_status), securityStatus));

                if (deviceType.equals("")) {

                    if (nodeId.equals("1")) {
                        deviceTypeTextView.setVisibility(View.VISIBLE);
                        deviceTypeTextView.setText(String.format(getString(R.string.device_type_s), "Gateway"));
                    } else {
                        deviceTypeTextView.setVisibility(View.INVISIBLE);
                    }
                } else {
                    deviceTypeTextView.setVisibility(View.VISIBLE);
                    deviceTypeTextView.setText(String.format(getString(R.string.device_type_s), deviceType));
                }
                adapter.notifyDataSetChanged();

            } else if ("Node Is Failed Check Report".equals(messageType)) { //检查node所对应Device是否活着
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getSpecifyDeviceInfo(nodeId));
                if (reported.contains("Status")) {
                    String checkNodeStatus = reportedObject.optString("Status");
                    Log.i(TAG, "Node Is Failed Check Report Status:" + checkNodeStatus);
                    //Alive 显示command class，Down显示replace按钮
                    if (checkNodeStatus.equals("Alive")) {  //Alive活着 / Down(failed)死了
                        commandClassLayout.setVisibility(View.VISIBLE);
                    } else if (checkNodeStatus.equals("Down(failed)")) {
                        nodeIsFailed.setVisibility(View.VISIBLE);
                        deviceNodeIsFailedLayout.setVisibility(View.VISIBLE);
                    }
                } else if (reported.contains("Error")) {
                    int error = reportedObject.optInt("Error");
                    Log.i(TAG, "Node Is Failed Check Report :" + error);
                    commandClassLayout.setVisibility(View.VISIBLE);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
