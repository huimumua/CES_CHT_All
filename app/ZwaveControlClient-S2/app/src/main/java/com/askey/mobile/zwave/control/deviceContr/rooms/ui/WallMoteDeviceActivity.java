package com.askey.mobile.zwave.control.deviceContr.rooms.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.adapter.ChooseActionAdapter;
import com.askey.mobile.zwave.control.deviceContr.adapter.RecyclerAdapter;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.model.WallMoteActionInfo;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;
import com.askey.mobile.zwave.control.util.ToastShow;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WallMoteDeviceActivity extends BaseActivity implements RecyclerAdapter.OnItemClickListener {
    private final String LOG_TAG = WallMoteDeviceActivity.class.getSimpleName();
    private SwipeMenuRecyclerView mRecycleView;
    private LinearLayoutManager mLayoutManager;
    private ChooseActionAdapter mAdapter;
    private List<Map<String, Object>> datas;
    private Intent fromIntent;
    private String fromActivity;
    private TextView mTitle;
    private String nodeId,endpointId,groupId,crrentName,currentType;
    private WallMoteActionInfo wallMoteActionInfo ;
    private   ArrayList nodeInterFaceList = new ArrayList();
    private String shadowTopic;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_choose);

        initView();
        wallMoteActionInfo=(WallMoteActionInfo)fromIntent.getSerializableExtra("wallMoteActionInfo");
        nodeId = fromIntent.getStringExtra("nodeId");
        endpointId = fromIntent.getStringExtra("endpointId");
        groupId = fromIntent.getStringExtra("groupId");
        shadowTopic = fromIntent.getStringExtra("shadowTopic");

        showWaitingDialog();
        try {
            MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
            Logg.i(LOG_TAG, "===onCreate===publishMessage=getDeviceListCommand=");
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getDeviceListCommand("ALL"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    private void initView() {
        fromIntent = getIntent();
        mTitle = (TextView) findViewById(R.id.tv_title);
        mTitle.setText(getResources().getString(R.string.choose_device_title));

        mRecycleView = (SwipeMenuRecyclerView) findViewById(R.id.rv_choose_action);
        mLayoutManager = new LinearLayoutManager(this);
        mRecycleView.setLayoutManager(mLayoutManager);
        mRecycleView.setHasFixedSize(true);

//        initData();
        datas = new ArrayList<>();
        mAdapter = new ChooseActionAdapter(this,datas);
        mRecycleView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);

    }


    @Override
    public void onItemClick(View view, int position) {
        nodeInterFaceList.add((String) datas.get(position).get("nodeId"));
        String str = getResources().getString(R.string.wall_mote_add_action_toast);
        showWaitingDialog(str);

        MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
        //获取灯泡状态
        Logg.i(LOG_TAG,"=====addEndpointsToGroup==json="+LocalMqttData.addEndpointsToGroup(nodeId,endpointId,groupId,nodeInterFaceList));
        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+"Zwave"+nodeId, LocalMqttData.addEndpointsToGroup(nodeId,endpointId,groupId,nodeInterFaceList));
    }



    private void iotMessageResult(String s2) {
//        {"state":{"reported":{"deviceid":"e9491da46ac285b8Zwave48","tstamp":"1513764088369","data":{"Interface":"addEndpointsToGroup","devType":"Zwave","NodeId":48,"result":"true"}}},"clientToken":"e9491da46ac285b8Zwave48"}
        try {
            JSONObject  jsonObject = new JSONObject(s2);
            String state = jsonObject.optString("state");
            JSONObject stateObject = new JSONObject(state);
            String reported = stateObject.optString("reported");
            JSONObject reportedObject = new JSONObject(reported);
            String data = reportedObject.optString("data");
            JSONObject dataObject = new JSONObject(data);
            String Interface = dataObject.optString("Interface");
            if (Interface.equals("getDeviceList")) {
                String DeviceList = dataObject.optString("deviceList");
                parsingGetDeviceList(DeviceList);
            }else if(Interface.equals("addEndpointsToGroup")){
                parsingAddEndpointsToGroup(dataObject);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
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
            stopWaitDialog();
            getDeviceList(result);

        }
    };

    private void getDeviceList(String mqttResult) {
        stopWaitDialog();
        final JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(mqttResult);
            String reported = jsonObject.optString("reported");
            JSONObject reportedObject = new JSONObject(reported);
            String Interface = reportedObject.optString("Interface");
            if (Interface.equals("getDeviceList")) {
                String DeviceList = reportedObject.optString("deviceList");
                parsingGetDeviceList(DeviceList);
            }else if(Interface.equals("addEndpointsToGroup")){
                parsingAddEndpointsToGroup(reportedObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parsingAddEndpointsToGroup(JSONObject reportedObject) {
        String result = reportedObject.optString("result");
        if(result.equals("true")){
            Intent intent = new Intent(mContext, SetupKeyActivity.class);
            intent.putStringArrayListExtra("nodeInterFaceList",nodeInterFaceList);
            intent.putExtra("from", WallMoteDeviceActivity.class.getSimpleName());
            intent.putExtra("nodeId",nodeId );
            intent.putExtra("endpointId",endpointId );
            intent.putExtra("groupId",groupId );
            intent.putExtra("icon", fromIntent.getStringExtra("icon"));
            intent.putExtra("type",currentType);
            intent.putExtra("name",crrentName);
            startActivity(intent);
        }else{
            ToastShow.showToastOnUiThread(mContext,"addEndpointsToGroup Fail");
        }
    }

    private void parsingGetDeviceList(String deviceList) {
        try {
            JSONArray columnInfo = new JSONArray(deviceList);
            int size = columnInfo.length();
            if (size > 0) {
                datas.clear();
                Logg.i(LOG_TAG,"====size====="+size);
                //{"reported":{"Interface":"getDeviceList","deviceList":[{"brand":"","nodeId":"23","deviceType":"Zwave","name":"bulb25","Room":"My Home","isFaorite":"0"}]}}
                for (int i = 0; i < size; i++) {
                    JSONObject info = columnInfo.getJSONObject(i);
                    String nodeId = info.getString("nodeId");
                    Logg.i(LOG_TAG,"=====nodeId==="+nodeId);
                    String brand = info.getString("brand");
                    String devType = info.getString("deviceType");
                    String category = info.getString("category");
                    String Room = info.getString("Room");
                    String isFavorite = info.getString("isFavorite");
                    String name = info.getString("name");

                    Map<String, Object> one = new HashMap<>();
                    Logg.i(LOG_TAG,"=====category==="+category);
                    if ("BULB".equals(category)) {
                        one.put("icon", R.drawable.vector_drawable_ic_device_79);
                    } else if ("PLUG".equals(category)) {
                        one.put("icon", R.drawable.vector_drawable_ic_80);
                    } else if ("WALLMOTE".equals(category)) {
                        one.put("icon", R.drawable.ic__96);
                    } else if ("EXTENDER".equals(category)) {
                        one.put("icon", R.drawable.ic_zwgeneral);
                    }else{
                        one.put("icon", R.drawable.vector_drawable_ic_65);
                    }
                    one.put("nodeId", nodeId);
                    one.put("name", name);
                    one.put("type",category);
                    if( !devType.equals("unknown") && !devType.equals("") && devType!=null){
                        datas.add(one);
                    }
                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    mAdapter = new ChooseActionAdapter(mContext,datas);
//                    mRecycleView.setAdapter(mAdapter);
//                    mAdapter.setOnItemClickListener(WallMoteDeviceActivity.this);
                    Logg.i(LOG_TAG,"=====device=count=="+datas.size());
                    mAdapter.notifyDataSetChanged();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void mqttMessageResult(String result) {
        Logg.i(LOG_TAG,"====mqttMessageResult=====");
        final JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(result);
            String state = jsonObject.optString("state");
            Logg.i(LOG_TAG,"====mqttMessageResult===state==");
            JSONObject stateObject = new JSONObject(state);
            String reported = stateObject.optString("reported");
            Logg.i(LOG_TAG,"====mqttMessageResult==reported===");
            JSONObject reportedObject = new JSONObject(reported);
            String data = reportedObject.optString("data");
            Logg.i(LOG_TAG,"====mqttMessageResult==data===");
            JSONObject dataObject = new JSONObject(data);
            String Interface = dataObject.optString("Interface");
            if (Interface.equals("getDeviceList")) {
                String deviceList = dataObject.optString("deviceList");
                Logg.i(LOG_TAG,"====DeviceList====="+deviceList);
                parsingGetDeviceList(deviceList);
//                {"reported":{"Interface":"addEndpointsToGroup","devType":"Zwave","NodeId":24,"result":"true"}}
            }else if(Interface.equals("addEndpointsToGroup")){
                String res = dataObject.optString("result");
                if(res.equals("true")){
                    Intent intent = new Intent(mContext, SetupKeyActivity.class);
                    intent.putStringArrayListExtra("nodeInterFaceList",nodeInterFaceList);
                    intent.putExtra("from", WallMoteDeviceActivity.class.getSimpleName());
                    intent.putExtra("nodeId",nodeId );
                    intent.putExtra("endpointId",endpointId );
                    intent.putExtra("groupId",groupId );
                    intent.putExtra("icon", fromIntent.getStringExtra("icon"));
                    intent.putExtra("type",currentType);
                    intent.putExtra("name",crrentName);
                    startActivity(intent);
                }else{
                    ToastShow.showToastOnUiThread(mContext,"addEndpointsToGroup Fail");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unrigister();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void unrigister() {
        if(mMqttMessageArrived!=null){
            MQTTManagement.getSingInstance().unrigister(mMqttMessageArrived);
        }
    }



}
