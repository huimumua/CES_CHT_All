package com.askey.mobile.zwave.control.home.fragment.roomitem;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.application.ZwaveClientApplication;
import com.askey.mobile.zwave.control.data.CloudIotData;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.IotMqttManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.IotMqttMessageCallback;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.model.DeviceInfo;
import com.askey.mobile.zwave.control.deviceContr.net.SocketTransceiver;
import com.askey.mobile.zwave.control.deviceContr.net.TCPReceive;
import com.askey.mobile.zwave.control.deviceContr.net.TcpClient;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.BulbActivity;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.PlugActivity;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.WallMoteLivingActivity;
import com.askey.mobile.zwave.control.deviceContr.ui.BulbManageActivity;
import com.askey.mobile.zwave.control.deviceContr.ui.DimmerManageActivity;
import com.askey.mobile.zwave.control.deviceContr.ui.SensorManageActivity;
import com.askey.mobile.zwave.control.home.activity.HomeActivity;
import com.askey.mobile.zwave.control.home.activity.addDevice.AddDeviceActivity;
import com.askey.mobile.zwave.control.home.adapter.DeviceAdapter;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;
import com.askey.mobile.zwave.control.widget.MyDialog;
import com.askeycloud.webservice.sdk.iot.message.builder.MqttDesiredJStrBuilder;
import com.askeycloud.webservice.sdk.service.iot.AskeyIoTService;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
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
    private static int clickPositon;
    private List<DeviceInfo> deviceInfoList;
    private boolean isFirstLoad = true;
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

        //
        if (isFirstLoad && !isVisibleToUser) {
            Log.d("taggg","第一次加载,fragment不可见");
            isFirstLoad = false;
            return;
        }
        if (isVisibleToUser) {
            Log.d("taggg","正常加载,fragment可见");
            //这里仅仅是注册，发送消息在onResum里面
            TcpClient.getInstance().rigister(tcpReceive);
            MQTTManagement.getSingInstance().rigister(mqttMessageArrived);
        } else {
            Log.d("taggg","正常加载,fragment不可见");
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

        DefaultItemAnimator defaultItemAnimator = new DefaultItemAnimator();
        defaultItemAnimator.setAddDuration(200);
        defaultItemAnimator.setRemoveDuration(200);
        recyclerView.setItemAnimator(defaultItemAnimator);
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

            if(mqttResult.contains("desired")){
                return;
            }
            if (mqttResult.contains(roomName)) {
                Logg.i(LOG_TAG, "=mqttMessageArrived=>=message=" + mqttResult);
                getSceneResult(mqttResult);
            }
        }
    };


    private void getSceneResult(String mqttResult) {
            final JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(mqttResult);
                String reported = jsonObject.optString("reported");
                JSONObject reportedObject = new JSONObject(reported);
                String Interface = reportedObject.optString("Interface");
                if(Interface.equals("getDeviceList")){
                    String DeviceList = reportedObject.optString("deviceList");
                    JSONArray columnInfo = new JSONArray(DeviceList);
                    int size = columnInfo.length();
                    if(size > 0){
                        deviceInfoList.clear();
                        //{"reported":{"Interface":"getDeviceList","deviceList":[{"brand":"","nodeId":"23","deviceType":"Zwave","name":"bulb25","Room":"My Home","isFaorite":"0"}]}}
                        for(int i=0;i<size;i++){
                            JSONObject info=columnInfo.getJSONObject(i);
                            String nodeId = info.getString("nodeId");
                            String brand = info.getString("brand");
                            String devType = info.getString("deviceType");
                            String category = info.getString("category");
                            String Room = info.getString("Room");
                            String isFavorite = info.getString("isFavorite");
                            String name = info.getString("name");
                            Logg.i(LOG_TAG,"==getDeviceResult=JSONArray===devName=="+name);
                            DeviceInfo deviceInfo = new DeviceInfo();
                            deviceInfo.setDeviceId(nodeId);
                            deviceInfo.setDisplayName(name);
                            deviceInfo.setDeviceType(category);
                            deviceInfo.setRooms(Room);
                            deviceInfo.setIsFavorite(isFavorite);

                            deviceInfoList.add(deviceInfo);

                            String nodeTopic = Const.subscriptionTopic+"Zwave" + nodeId;
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
        Toast.makeText(getActivity(), "item", Toast.LENGTH_SHORT).show();
//        SENSOR/BULB/DIMMER/PLUG
        Intent intent = null;
        if ("SENSOR".equals(deviceInfo.getDeviceType())) {
            intent = new Intent(getActivity(), SensorManageActivity.class);
            intent.putExtra("roomName", deviceInfo.getRooms());
        } else if ("BULB".equals(deviceInfo.getDeviceType())) {
            intent = new Intent(getActivity(), BulbActivity.class);
        } else if ("DIMMER".equals(deviceInfo.getDeviceType())) {
            intent = new Intent(getActivity(), DimmerManageActivity.class);
        } else if ("PLUG".equals(deviceInfo.getDeviceType())) {
            intent = new Intent(getActivity(), PlugActivity.class);
        } else if ("WALLMOTE".equals(deviceInfo.getDeviceType())) {
            intent = new Intent(getActivity(), WallMoteLivingActivity.class);
        } else if ("SWITCH".equals(deviceInfo.getDeviceType())) {
            intent = new Intent(getActivity(), WallMoteLivingActivity.class);
        } else {
            intent = new Intent(getActivity(), BulbManageActivity.class);//原有的经验证过的可以正常使用
            intent.putExtra("uniqueid", deviceInfo.getUniqueId());
            startActivity(intent);
        }
        intent.putExtra("nodeId", deviceInfo.getDeviceId());
        intent.putExtra("displayName", deviceInfo.getDisplayName());
        startActivity(intent);
    }

    @Override
    public void deleteItemClick(final int position) {

        myDialog = new MyDialog(getActivity());
        myDialog.setYesOnclickListener("Proceed", new MyDialog.onYesOnclickListener() {
            @Override
            public void onYesClick() {
                if (TcpClient.getInstance().isConnected()) {
                    String nodeId = deviceInfoList.get(position).getDeviceId();
                    Logg.i(LOG_TAG, "=removeDevice=" + "mobile_zwave:removeDevice:Zwave:"+ nodeId);
                    TcpClient.getInstance().getTransceiver().send("mobile_zwave:removeDevice:Zwave:"+ nodeId);
                }
            }
        });
        clickPositon = position;
        myDialog.show();
    }


    @Override
    public void addItemClick() {
        startActivity(new Intent(getActivity(), AddDeviceActivity.class));
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
        if (Const.isRemote) {
            initIotMqttMessage();
//            getuserIoTDeviceList();
            MqttDesiredJStrBuilder builder = new MqttDesiredJStrBuilder(Const.subscriptionTopic);
            Logg.i(LOG_TAG, "===MqttDesiredJStrBuilder==deviceId==="+Const.subscriptionTopic);
            builder.setJsonString( CloudIotData.getDeviceListCommand(roomName));
            Logg.i(LOG_TAG, "===MqttDesiredJStrBuilder==setJsonString==="+ CloudIotData.getDeviceListCommand(roomName));
            AskeyIoTService.getInstance(ZwaveClientApplication.getInstance()).publishDesiredMessage(HomeActivity.shadowTopic, builder);
        } else {
//            TcpClient.getInstance().rigister(tcpReceive);
//            MQTTManagement.getSingInstance().rigister(mqttMessageArrived);
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getDeviceListCommand(roomName));
        }
    }

    private void initIotMqttMessage() {

        IotMqttManagement.getInstance().setIotMqttMessageCallback(new IotMqttMessageCallback() {
            @Override
            public void receiveMqttMessage(String s, String s1, String s2) {
                //处理结果
                Logg.i(LOG_TAG, "==IotMqttMessageCallback====setIotMqttMessageCallback==s=" + s);
                Logg.i(LOG_TAG, "==IotMqttMessageCallback====setIotMqttMessageCallback==s1=" + s1);
                Logg.i(LOG_TAG, "==IotMqttMessageCallback====setIotMqttMessageCallback==s2=" + s2);
                if(s2.contains("desired")){
                    return;
                }
                getSceneResult(s2);
            }

        });

//        //以下这句为注册监听
//        AskeyIoTService.getInstance(getContext()).setShadowReceiverListener(new ShadowReceiveListener() {
//            @Override
//            public void receiveShadowDocument(String s, String s1, String s2) {
//                Logg.i(LOG_TAG, "==IotMqttMessageCallback====setShadowReceiverListener==s=" + s);
//                Logg.i(LOG_TAG, "==IotMqttMessageCallback====setShadowReceiverListener==s1=" + s1);
//                Logg.i(LOG_TAG, "==IotMqttMessageCallback====setShadowReceiverListener==s2=" + s2);
//                IotMqttManagement.getInstance().receiveMqttMessage(s,s1,s2);
//                getSceneResult(s2);
//            }
//        });
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
        if (tcpReceive != null) {
            TcpClient.getInstance().unrigister(tcpReceive);
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

    TCPReceive tcpReceive = new TCPReceive() {
        @Override
        public void onConnect(SocketTransceiver transceiver) {

        }

        @Override
        public void onConnectFailed() {

        }

        @Override
        public void receiveMessage(SocketTransceiver transceiver, String tcpMassage) {
            //处理结果
            removeDeviceResult(tcpMassage);

        }

        @Override
        public void onDisconnect(SocketTransceiver transceiver) {

        }

    };

    private void removeDeviceResult(final String result) {
        Log.d(LOG_TAG,"======removeDeviceResult======" + result);
        if (result.contains("removeDevice:other")) {
            return;
        }
        ((Activity) getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final JSONObject jsonObject = new JSONObject(result);
                    String messageType = jsonObject.optString("MessageType");
                    if (messageType.equals("Node Remove Status")) {
                        String status = jsonObject.optString("Status");
//            String nodeId = jsonObject.optString("NodeID");
                        if ("Success".equals(status)) {
                            //删除成功则返回主页，否则提示删除失败，返回设备管理界面
                            deviceInfoList.remove(clickPositon);
                            adapter.notifyItemRemoved(clickPositon);
                            adapter.notifyItemRangeChanged(clickPositon, deviceInfoList.size());
                            myDialog.dismiss();
                        } else {
                            Toast.makeText(getActivity(), status, Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }


}
