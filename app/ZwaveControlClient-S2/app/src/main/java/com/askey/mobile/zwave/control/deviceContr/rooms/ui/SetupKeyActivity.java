package com.askey.mobile.zwave.control.deviceContr.rooms.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.adapter.WallMoteActionAdapter;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.model.WallMoteActionInfo;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;
import com.askey.mobile.zwave.control.util.ToastShow;
import com.askeycloud.sdk.device.response.IoTDeviceInfoResponse;
import com.yanzhenjie.recyclerview.swipe.SwipeItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class SetupKeyActivity extends BaseActivity implements View.OnClickListener{
    private static final String TAG = SetupKeyActivity.class.getSimpleName();
    private SwipeMenuRecyclerView mRecycleView;
    private ImageView ivAddAction;
    private WallMoteActionAdapter swipeMenuAdapter;
    private List<WallMoteActionInfo> wallMoteActionList;
    private LinearLayoutManager mLayoutManager;
    private String mNodeId,endpointId,groupId;
    private String name,type,icon,nodeInfo;
    private ArrayList<String> nodeInterFaceList;
    private SwipeMenuBridge mSwipeMenuBridge;
    private IoTDeviceInfoResponse ioTDeviceInfoResponse;
    private  String shadowTopic = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_key);

        wallMoteActionList = new ArrayList<>();
//      initData();//获取信息
        initView();

        mNodeId = getIntent().getStringExtra("nodeId");
        endpointId = getIntent().getStringExtra("endpointId");
        groupId = getIntent().getStringExtra("groupId");
        icon = getIntent().getStringExtra("icon");
        type = getIntent().getStringExtra("type");
        name = getIntent().getStringExtra("name");
        nodeInfo = getIntent().getStringExtra("nodeInfo");
        shadowTopic = getIntent().getStringExtra("shadowTopic");
        nodeInterFaceList = getIntent().getStringArrayListExtra("nodeInterFaceList");

        Logg.i(TAG,"====nodeId===="+mNodeId);
        Logg.i(TAG,"====endpointId===="+endpointId);
        Logg.i(TAG,"====groupId===="+groupId);
        Logg.i(TAG,"====icon===="+icon);
        Logg.i(TAG,"====type===="+type);
        Logg.i(TAG,"====name===="+name);


        showWaitingDialog();
        MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
        //获取灯泡状态
        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+"Zwave"+mNodeId, LocalMqttData.getGroupInfo(mNodeId,0+"",9+""));
    }

    private void iotMessageResult(String s2) {
//        {"state":{"reported":{"deviceid":"e9491da46ac285b8Zwave48","tstamp":"1513763245448","data":{"Interface":"getGroupInfo","NodeId":"48","devType":"Zwave","GroupInfo":[{"Group id":4,"Group members":[{"controlNodeId":"41"}],"Max Supported endpoints":"5","endpoint id":"0"}]}}},"clientToken":"e9491da46ac285b8Zwave48"}
        try {
            JSONObject  jsonObject = new JSONObject(s2);
            String state = jsonObject.optString("state");
            JSONObject stateObject = new JSONObject(state);
            String reported = stateObject.optString("reported");
            JSONObject reportedObject = new JSONObject(reported);
            String data = reportedObject.optString("data");
            JSONObject dataObject = new JSONObject(data);
            String Interface = dataObject.optString("Interface");
            if(Interface.equals("getGroupInfo")){

                parsingGetGroupInfo(dataObject);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    MqttMessageArrived mMqttMessageArrived = new MqttMessageArrived() {
        @Override
        public void mqttMessageArrived(String topic, MqttMessage message) {
            final String result = new String(message.getPayload());
            Logg.i(TAG,"=mqttMessageArrived=>=topic="+topic);
            Logg.i(TAG,"=mqttMessageArrived=>=message="+result);
            if(result.contains("desired")){
                return;
            }
            stopWaitDialog();
            mqttMessageResult(result);

        }
    };

    private void mqttMessageResult(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            String reported = jsonObject.optString("reported");
            JSONObject reportedObject = new JSONObject(reported);
            String Interface = reportedObject.optString("Interface");
            if(Interface.equals("getGroupInfo")){

                parsingGetGroupInfo(reportedObject);

            }else if(Interface.equals("removeEndpointsFromGroup")){
                String res = reportedObject.optString("result");
                if(res.equals("true")){
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int direction = mSwipeMenuBridge.getDirection(); // 左侧还是右侧菜单。
                            int adapterPosition = mSwipeMenuBridge.getAdapterPosition(); // RecyclerView的Item的position。
                            wallMoteActionList.remove(adapterPosition);
                            swipeMenuAdapter.notifyItemRemoved(adapterPosition);
                        }
                    });
                }else {
                    ToastShow.showToastOnUiThread(mContext,"removeEndpointsFromGroup Fail");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void parsingGetGroupInfo(JSONObject reportedObject) {
        try {
            // String messageType = reportedObject.optString("MessageType");
            String nodeId = reportedObject.optString("NodeId");
            String groupInfo = reportedObject.optString("GroupInfo");
            Logg.i(TAG,"=getGroupInfo=>=groupInfo="+groupInfo);
            if(!groupInfo.equals("")){
                Logg.i(TAG,"=getGroupInfo=>=groupInfo=1="+groupInfo);
                JSONArray columnInfo = new JSONArray(groupInfo);
                int size = columnInfo.length();
                if (size > 0) {
                    for (int i = 0; i < size; i++) {
                        JSONObject info = columnInfo.getJSONObject(i);
                        String groupId = info.optString("Group id");
                        String groupMember = info.optString("Group members");
                        String maxEndpoints = info.optString("Max Supported endpoints");
                        String endpointId = info.optString("endpoint id");
                        Logg.i(TAG,"=getGroupInfo=>=groupId="+groupId);
                        Logg.i(TAG,"=getGroupInfo=>=maxEndpoints="+maxEndpoints);
                        Logg.i(TAG,"=getGroupInfo=>=endpointId="+endpointId);
                        //这里需要订阅设备列表
                        ArrayList<String> nodeInterFaceList =new ArrayList<>();
                        JSONArray groupMemberObject = new JSONArray(groupMember);
                        int groupMemberSize = groupMemberObject.length();
                        if (groupMemberSize > 0) {
                            for (int m = 0; m < groupMemberSize; m++) {
                                JSONObject controlNodeIdStr = groupMemberObject.getJSONObject(m);
                                String controlNodeId = controlNodeIdStr.optString("controlNodeId");
                                Logg.i(TAG,"=getGroupInfo=>=controlNodeId="+controlNodeId);
                                //这里需要订阅设备列表
                                nodeInterFaceList.add(controlNodeId);
                            }
                        }

                        WallMoteActionInfo wallMoteActionInfo = new WallMoteActionInfo();
                        wallMoteActionInfo.setGroupId(groupId);
                        wallMoteActionInfo.setEndpointId(endpointId);
                        wallMoteActionInfo.setNodeInterFaceList(nodeInterFaceList);
                        wallMoteActionList.add(wallMoteActionInfo);

                    }
                }
            }

            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
/*                        if ("WallMoteDeviceActivity".equals( getIntent().getStringExtra("from"))) {
                            WallMoteActionInfo wallMoteActionInfo = new WallMoteActionInfo();
                            wallMoteActionInfo.setType(type);
                            wallMoteActionInfo.setName(name);
                            wallMoteActionInfo.setGroupId(groupId);
                            wallMoteActionInfo.setEndpointId(endpointId);
                            wallMoteActionInfo.setNodeId(mNodeId);
                            wallMoteActionList.add(wallMoteActionInfo);
                            //需要通过wallMoteActionInfo获取设备名称
                            Logg.i(TAG,"====wallMoteActionList.size()==="+wallMoteActionList.size());
                        }*/

                    swipeMenuAdapter.notifyDataSetChanged();
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void initView() {
        mNodeId = getIntent().getStringExtra("nodeId");

        mRecycleView = (SwipeMenuRecyclerView) findViewById(R.id.rv_available_commands);
        mLayoutManager = new LinearLayoutManager(this);
        mRecycleView.setLayoutManager(mLayoutManager);
        mRecycleView.setHasFixedSize(true);
        ivAddAction = (ImageView) findViewById(R.id.iv_add_action);
        ivAddAction.setOnClickListener(this);
        mRecycleView.setSwipeItemClickListener(new SwipeItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                Intent intent = new Intent(mContext,WallMoteActionActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("wallMoteActionInfo", (Serializable) wallMoteActionList.get(position));
                bundle.putString("shadowTopic",shadowTopic);
                intent.putExtras(bundle);
                startActivity(intent);

            }
        });

// 创建菜单：
        SwipeMenuCreator mSwipeMenuCreator = new SwipeMenuCreator() {
            @Override
            public void onCreateMenu(SwipeMenu leftMenu, SwipeMenu rightMenu, int viewType) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(mContext)
                        .setBackground(R.color.white)
                        .setImage(R.drawable.ic_close)
                        .setWidth(100) // 宽度。
                        .setHeight(MATCH_PARENT); // 高度。
                 // 各种文字和图标属性设置。
                leftMenu.addMenuItem(deleteItem); // 在Item左侧添加一个菜单。
            }
        };


        SwipeMenuItemClickListener mMenuItemClickListener = new SwipeMenuItemClickListener() {
            @Override
            public void onItemClick(SwipeMenuBridge menuBridge) {
                // 任何操作必须先关闭菜单，否则可能出现Item菜单打开状态错乱。
                menuBridge.closeMenu();
                nodeInterFaceList = wallMoteActionList.get(menuBridge.getPosition()).getNodeInterFaceList();
                String groupId = wallMoteActionList.get(menuBridge.getAdapterPosition()).getGroupId();
                String str = getResources().getString(R.string.wall_mote_add_action_toast);
                showWaitingDialog(str);

                MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+"Zwave"+mNodeId, LocalMqttData.removeEndpointsFromGroup(mNodeId,0+"",groupId,nodeInterFaceList));

                mSwipeMenuBridge = menuBridge;
//                int direction = menuBridge.getDirection(); // 左侧还是右侧菜单。
//                int adapterPosition = menuBridge.getAdapterPosition(); // RecyclerView的Item的position。
//                int menuPosition = menuBridge.getPosition(); // 菜单在RecyclerView的Item中的Position。
//                wallMoteActionList.remove(menuBridge.getPosition());
//                swipeMenuAdapter.notifyItemRemoved(adapterPosition);
            }
        };

        // 设置监听器。
        mRecycleView.setSwipeMenuCreator(mSwipeMenuCreator);
        // 菜单点击监听。
        mRecycleView.setSwipeMenuItemClickListener(mMenuItemClickListener);
        swipeMenuAdapter = new WallMoteActionAdapter(this,wallMoteActionList);
        mRecycleView.setAdapter(swipeMenuAdapter);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_add_action:
                Intent intent = new Intent(mContext,WallMoteActionActivity.class);
                intent.putExtra("from", SetupKeyActivity.class.getSimpleName());
                intent.putExtra("nodeId",mNodeId);
                intent.putExtra("shadowTopic",shadowTopic);
                startActivity(intent);
                finish();
              break;
    }        }

    @Override
    protected void onStop() {
        super.onStop();
        Logg.i(TAG,"====onStop=unrigister===");
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
