package com.askey.mobile.zwave.control.home.fragment.roomitem;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.application.ZwaveClientApplication;
import com.askey.mobile.zwave.control.data.CloudIotData;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.model.DeviceInfo;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.BulbActivity;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.ExtenderDeviceActivity;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.PlugActivity;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.WallMoteLivingActivity;
import com.askey.mobile.zwave.control.home.activity.HomeActivity;
import com.askey.mobile.zwave.control.home.activity.addDevice.DeleteDeviceActivity;
import com.askey.mobile.zwave.control.home.activity.addDevice.SelectBrandActivity;
import com.askey.mobile.zwave.control.home.adapter.DeviceAdapter;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;
import com.askey.mobile.zwave.control.widget.MyDialog;
import com.askeycloud.webservice.sdk.iot.callback.ShadowReceiveListener;
import com.askeycloud.webservice.sdk.iot.message.builder.MqttDesiredJStrBuilder;
import com.askeycloud.webservice.sdk.service.iot.AskeyIoTService;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MyHomeRoomFragment extends Fragment implements DeviceAdapter.OnItemClickListener {

    public static String LOG_TAG = "MyHomeRoomFragment";
    private RecyclerView recyclerView;
    private DeviceAdapter adapter;
    private LinearLayout notify_layout, edit_layout;
    private String mqttResult, nodeIndo;
    private final static String ROOM_ID = "roomId";
    private final static String ROOM_NAME = "roomName";
    private String roomName = "ALL";
    private Integer roomId;
    private Bundle bundle;
    private MyDialog myDialog;
    private int clickPosition;
    private List<DeviceInfo> deviceInfoList;
    private boolean isFirstLoad = true;
    private boolean isVisibleToUser;
    private boolean isFirst2onResume = true;
//    private String shadowTopic;

    public MyHomeRoomFragment() {
        // Required empty public constructor
    }


    public static MyHomeRoomFragment newInstance(int id, String name) {
        MyHomeRoomFragment itemFragment = new MyHomeRoomFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ROOM_ID, id);
        bundle.putString(ROOM_NAME, name);
        itemFragment.setArguments(bundle);
        return itemFragment;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser = isVisibleToUser;
        if (isFirstLoad && !isVisibleToUser) {
            Log.d(LOG_TAG, "第一次加载,fragment不可见");
            isFirstLoad = false;
            return;
        }
        if (isVisibleToUser) {
            Log.d(LOG_TAG, "正常加载,fragment可见");
            //这里仅仅是注册，发送消息在onResum里面
            MQTTManagement.getSingInstance().rigister(mqttMessageArrived);
        } else {
            Log.d(LOG_TAG, "正常加载,fragment不可见");
            unrigister();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Logg.i(LOG_TAG, "===onCreateView=====");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_home_room, container, false);
        initView(view);
        initData();
        return view;
    }

    private void initView(View view) {

        Bundle bundle = getArguments();
        roomName = bundle.getString(ROOM_NAME);
        roomId = bundle.getInt(ROOM_ID);

        notify_layout = (LinearLayout) view.findViewById(R.id.notify_layout);
        edit_layout = (LinearLayout) view.findViewById(R.id.edit_layout);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
    }


    private void initData() {
        deviceInfoList = new ArrayList<>();
        adapter = new DeviceAdapter();
        adapter.setDeviceInfoList(deviceInfoList);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }


    MqttMessageArrived mqttMessageArrived = new MqttMessageArrived() {
        @Override
        public void mqttMessageArrived(String topic, MqttMessage message) {
            mqttResult = new String(message.getPayload());
            Logg.i(LOG_TAG, "=mqttMessageArrived=>=topic=" + topic);

            if (mqttResult.contains("desired")) {
                return;
            }
            if (mqttResult.contains(roomName)) {
                Logg.i(LOG_TAG, "=mqttMessageArrived=>=message=" + mqttResult);
//                deviceInfoList.clear();
//                deviceInfoList = LocalJsonParsing. getDeviceList(mqttResult);
//                ((Activity) getContext()).runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        adapter.notifyDataSetChanged();
//                    }
//                });
                getDeviceList(mqttResult);
            }
        }
    };


    private void getDeviceList(String mqttResult) {
        final JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(mqttResult);
            String reported = jsonObject.optString("reported");
            JSONObject reportedObject = new JSONObject(reported);
            String Interface = reportedObject.optString("Interface");
            if (Interface.equals("getDeviceList")) {
                String DeviceList = reportedObject.optString("deviceList");
                JSONArray columnInfo = new JSONArray(DeviceList);
                int size = columnInfo.length();
                if (size > 0) {
                    deviceInfoList.clear();
                    //{"reported":{"Interface":"getDeviceList","deviceList":[{"brand":"","nodeId":"23","deviceType":"Zwave","name":"bulb25","Room":"My Home","isFaorite":"0"}]}}
                    for (int i = 0; i < size; i++) {
                        JSONObject info = columnInfo.getJSONObject(i);
                        String nodeId = info.getString("nodeId");
                        String brand = info.getString("brand");
                        String devType = info.getString("deviceType");
                        String category = info.getString("category");
                        String Room = info.getString("Room");
                        String isFavorite = info.getString("isFavorite");
                        String name = info.getString("name");
//                        String nodeInfo = info.getString("nodeInfo");
//                        Logg.i(LOG_TAG, "==getDeviceResult=JSONArray===nodeInfo==" + nodeInfo);
                        DeviceInfo deviceInfo = new DeviceInfo();
                        deviceInfo.setDeviceId(nodeId);
                        deviceInfo.setDisplayName(name);
                        deviceInfo.setDeviceType(category);
                        deviceInfo.setRooms(Room);
                        deviceInfo.setIsFavorite(isFavorite);
//                        deviceInfo.setNodeInfo(nodeInfo);
                        deviceInfoList.add(deviceInfo);

                        String nodeTopic = Const.subscriptionTopic + "Zwave" + nodeId;
                        // 订阅新设备的topic为 sn + nodeId
                        MQTTManagement.getSingInstance().subscribeToTopic(nodeTopic, null);
                    }
                }

                ((Activity) getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private void getSceneResult1(String s2) {
        final JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(s2);
            String state = jsonObject.optString("state");
            JSONObject stateObject = new JSONObject(state);
            String reported = stateObject.optString("reported");
            JSONObject reportedObject = new JSONObject(reported);
            String data = reportedObject.optString("data");
            JSONObject dataObject = new JSONObject(data);
            String Interface = dataObject.optString("Interface");
            if (Interface.equals("getDeviceList")) {
                String DeviceList = dataObject.optString("deviceList");
                JSONArray columnInfo = new JSONArray(DeviceList);
                int size = columnInfo.length();
                if (size > 0) {
                    deviceInfoList.clear();
                    for (int i = 0; i < size; i++) {
                        JSONObject info = columnInfo.getJSONObject(i);
                        String nodeId = info.getString("nodeId");
                        String brand = info.getString("brand");
                        String devType = info.getString("deviceType");
                        String category = info.getString("category");
                        String Room = info.getString("Room");
                        String isFavorite = info.getString("isFavorite");
                        String name = info.getString("name");
//                        String nodeInfo = info.getString("nodeInfo");
                        Logg.i(LOG_TAG, "==getDeviceResult=JSONArray===devName==" + name);
                        DeviceInfo deviceInfo = new DeviceInfo();
                        deviceInfo.setDeviceId(nodeId);
                        deviceInfo.setDisplayName(name);
                        deviceInfo.setDeviceType(category);
                        deviceInfo.setRooms(Room);
                        deviceInfo.setIsFavorite(isFavorite);
//                        deviceInfo.setNodeInfo(nodeInfo);
                        deviceInfoList.add(deviceInfo);

                        //这里需要订阅设备列表
                        String nodeTopic = Const.subscriptionTopic + "Zwave" + nodeId;



                    }
                }

                ((Activity) getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void setCurrentMode(int flag) {
        switch (flag) {
            case DeviceAdapter.NORMAL_MODE:
                notify_layout.setVisibility(View.VISIBLE);
                edit_layout.setVisibility(View.GONE);
                break;
            case DeviceAdapter.EDIT_MODE:
                notify_layout.setVisibility(View.GONE);
                edit_layout.setVisibility(View.VISIBLE);
                break;
        }
        adapter.setMode(flag);
        adapter.notifyDataSetChanged();
    }

    public int getCurrentMode() {
        return adapter.getMode();
    }

    public String getRoomName() {
        return roomName;
    }

    @Override
    public void onItemClick(View view, DeviceInfo deviceInfo) {
//        SENSOR/BULB/DIMMER/PLUG
        Intent intent = null;
        if ("BULB".equals(deviceInfo.getDeviceType())) {
//            intent = new Intent(getActivity(), WallMoteLivingActivity.class);
            intent = new Intent(getActivity(), BulbActivity.class);
        } else if ("PLUG".equals(deviceInfo.getDeviceType())) {
            intent = new Intent(getActivity(), PlugActivity.class);
//            intent = new Intent(getActivity(), WallMoteLivingActivity.class);
        } else if ("WALLMOTE".equals(deviceInfo.getDeviceType())) {
            intent = new Intent(getActivity(), WallMoteLivingActivity.class);
        } else if ("EXTENDER".equals(deviceInfo.getDeviceType())) {
            intent = new Intent(getActivity(), ExtenderDeviceActivity.class);
        } else {
            intent = new Intent(getActivity(), BulbActivity.class);//原有的经验证过的可以正常使用
            intent.putExtra("uniqueid", deviceInfo.getUniqueId());
            startActivity(intent);
        }
        intent.putExtra("nodeId", deviceInfo.getDeviceId());
        intent.putExtra("type", deviceInfo.getDeviceType());
        intent.putExtra("room", deviceInfo.getRooms());
        intent.putExtra("displayName", deviceInfo.getDisplayName());
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(View view, DeviceInfo deviceInfo) {
//        SENSOR/BULB/DIMMER/PLUG
        Intent intent = null;
        if ("BULB".equals(deviceInfo.getDeviceType())) {
            intent = new Intent(getActivity(), BulbActivity.class);
        } else if ("PLUG".equals(deviceInfo.getDeviceType())) {
            intent = new Intent(getActivity(), PlugActivity.class);
        } else if ("WALLMOTE".equals(deviceInfo.getDeviceType())) {
            intent = new Intent(getActivity(), WallMoteLivingActivity.class);
        } else if ("EXTENDER".equals(deviceInfo.getDeviceType())) {
            intent = new Intent(getActivity(), ExtenderDeviceActivity.class);
        } else {
            intent = new Intent(getActivity(), BulbActivity.class);//原有的经验证过的可以正常使用
            intent.putExtra("uniqueid", deviceInfo.getUniqueId());
            startActivity(intent);
        }
        intent.putExtra("nodeId", deviceInfo.getDeviceId());
        intent.putExtra("type", deviceInfo.getDeviceType());
        intent.putExtra("room", deviceInfo.getRooms());
        intent.putExtra("displayName", deviceInfo.getDisplayName());
        startActivity(intent);
    }

    @Override
    public void deleteItemClick(final int position) {
        DeviceInfo info = deviceInfoList.get(position);
        //deviceId就是nodeId
        showDialog(getActivity(), info.getDisplayName(), info.getDeviceId());
        clickPosition = position;
    }

    void showDialog(final Context context, String deviceName, final String nodeId) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        final AlertDialog alertDialog = alertDialogBuilder.show();
        alertDialog.setCancelable(false);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_delete_device, null);
        ImageView icon = (ImageView) view.findViewById(R.id.iv_icon);
        icon.setImageResource(R.drawable.ic_del_icon);
        alertDialog.setContentView(view);
        Button cancel = (Button) view.findViewById(R.id.btn_cancel);
        Button proceed = (Button) view.findViewById(R.id.btn_proceed);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DeleteDeviceActivity.class);
                intent.putExtra("deviceId", nodeId);
                intent.putExtra("roomName", roomName);
                startActivity(intent);
                alertDialog.dismiss();
            }
        });

    }

    @Override
    public void addItemClick() {
        startActivity(new Intent(getActivity(), SelectBrandActivity.class));
        Const.currentRoomName = roomName;
    }

    public void notifyFragmentData(DeviceInfo deviceInfo) {
        Logg.i(LOG_TAG, "==notifyFragmentData=deviceInfo.getDisplayName()=====" + deviceInfo.getDisplayName());
        if (deviceInfo != null) {
            deviceInfoList.add(deviceInfo);
            Logg.i(LOG_TAG, "==notifyFragmentData=deviceInfo.getDisplayName()=====" + deviceInfoList.get(0).getDisplayName());
        }
        Logg.i(LOG_TAG, "==notifyFragmentData=deviceInfo.getDisplayName()=====" + deviceInfoList.size());
        adapter.notifyDataSetChanged();
    }
    public void deleteDevice() {
        deviceInfoList.remove(clickPosition);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logg.i(LOG_TAG, "===onActivityCreated=====");
    }

    @Override
    public void onStart() {
        super.onStart();
        Logg.i(LOG_TAG, "===onStart=====");
    }

    @Override
    public void onResume() {
        super.onResume();
        Logg.i(LOG_TAG, "===onResume=====");
        /*
            当fragment可见时才发送命令，为了防止其他界面弹窗导致全部fragment走生命周期，使消息接收错乱，这里仅仅允许此fragment第一次初始化的时候
            在onResume里面发送请求命令
         */
        if (isVisibleToUser && isFirst2onResume) {
            isFirst2onResume = false;
            if (Const.isRemote) {
                initIotMqttMessage();
//            getuserIoTDeviceList();
                MqttDesiredJStrBuilder builder = new MqttDesiredJStrBuilder(Const.subscriptionTopic);
                Logg.i(LOG_TAG, "===MqttDesiredJStrBuilder==deviceId===" + Const.subscriptionTopic);
                builder.setJsonString(CloudIotData.getDeviceListCommand(roomName));
                Logg.i(LOG_TAG, "===MqttDesiredJStrBuilder==setJsonString===" + CloudIotData.getDeviceListCommand(roomName));
                AskeyIoTService.getInstance(ZwaveClientApplication.getInstance()).publishDesiredMessage(HomeActivity.shadowTopic, builder);
            } else {
//            MQTTManagement.getSingInstance().rigister(mqttMessageArrived);
//                Toast.makeText(getActivity(), roomName+", size: "+deviceInfoList.size(), Toast.LENGTH_SHORT).show();
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getDeviceListCommand(roomName));
            }
        }
    }

    private void initIotMqttMessage() {
        //以下这句为注册监听
        AskeyIoTService.getInstance(getContext()).setShadowReceiverListener(new ShadowReceiveListener() {
            @Override
            public void receiveShadowDocument(String s, String s1, String s2) {
                Logg.i(LOG_TAG, "==IotMqttMessageCallback====setShadowReceiverListener==s=" + s);
                Logg.i(LOG_TAG, "==IotMqttMessageCallback====setShadowReceiverListener==s1=" + s1);
                Logg.i(LOG_TAG, "==IotMqttMessageCallback====setShadowReceiverListener==s2=" + s2);
//                IotMqttManagement.getInstance().receiveMqttMessage(s,s1,s2);
                if (s2.contains("desired")) {
                    return;
                }

//                deviceInfoList.clear();
//                deviceInfoList = IotJsonParsing. getDeviceList(s2);
//                Logg.i(LOG_TAG, "==IotMqttMessageCallback====setShadowReceiverListener==deviceInfoList.size()=" + deviceInfoList.size());
//                ((Activity) getContext()).runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        adapter.notifyDataSetChanged();
//                    }
//                });
                getSceneResult1(s2);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        unrigister();
        Logg.i(LOG_TAG, "===onPause=====");
    }


    @Override
    public void onStop() {
        super.onStop();
        Logg.i(LOG_TAG, "===onStop=====");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logg.i(LOG_TAG, "===onDestroy=====");
        unrigister();
    }

    private void unrigister() {
        if (mqttMessageArrived != null) {
            MQTTManagement.getSingInstance().unrigister(mqttMessageArrived);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Logg.i(LOG_TAG, "===onDestroyView=====");

    }

    @Override
    public void onDetach() {
        super.onDetach();
        Logg.i(LOG_TAG, "===onDetach=====");
    }
}
