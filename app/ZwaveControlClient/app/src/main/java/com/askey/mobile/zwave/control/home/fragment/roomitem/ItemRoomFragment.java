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
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.IotMqttManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.model.DeviceInfo;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.BulbActivity;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.ExtenderDeviceActivity;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.PlugActivity;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.WallMoteLivingActivity;
import com.askey.mobile.zwave.control.home.activity.addDevice.DeleteDeviceActivity;
import com.askey.mobile.zwave.control.home.activity.addDevice.SelectBrandActivity;
import com.askey.mobile.zwave.control.home.adapter.DeviceAdapter;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;
import com.askey.mobile.zwave.control.widget.MyDialog;
import com.askeycloud.webservice.sdk.iot.callback.ShadowReceiveListener;
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
public class ItemRoomFragment extends Fragment implements DeviceAdapter.OnItemClickListener, View.OnClickListener {
    public static String LOG_TAG = "ItemRoomFragment";
    private RecyclerView recyclerView;
    private DeviceAdapter adapter;
    private LinearLayout notify_layout, edit_layout, no_device_layout;
    private ImageView add_first_device;
    private TextView room_name;
    private final static String ROOM_ID = "roomId";
    private final static String ROOM_NAME = "roomName";
    private String roomName;
    private Integer roomId;
    private Bundle bundle;
    private String mqttResult;
    private MyDialog myDialog;
    private List<DeviceInfo> deviceInfoList;
    private boolean isFirstLoadData = true;
    private int clickPosition;

    public ItemRoomFragment() {
        // Required empty public constructor
    }

    public static ItemRoomFragment newInstance(int id, String name) {
        ItemRoomFragment itemFragment = new ItemRoomFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ROOM_ID, id);
        bundle.putString(ROOM_NAME, name);
        itemFragment.setArguments(bundle);
        return itemFragment;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Logg.i(LOG_TAG, "===setUserVisibleHint=====");
        if (isVisibleToUser) {
            Log.d(LOG_TAG, "正常加载,fragment可见" + roomName);
            MQTTManagement.getSingInstance().rigister(mqttMessageArrived);
            /*
                在第一次点击底部tab跳转到房间页面的时候会初始化所有fragment，此时My Home页面是可见的，其他子页面是不可见的。这里处理的是本页面第一次
                可见时发送消息，若后续通过滑动再次回到本页面则不会发送消息，这样做是为了避免快速滑动时本页面的消息被其他页面接收到。
             */
            if (isFirstLoadData) {
                isFirstLoadData = false;
                if (Const.isRemote) {

                } else {

                    MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getDeviceListCommand(roomName));
                }
            }
        } else {
            Log.d(LOG_TAG, "正常加载,fragment不可见");
            unrigister();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Logg.i(LOG_TAG, "===onAttach=====");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logg.i(LOG_TAG, "===onCreate=====");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Logg.i(LOG_TAG, "===onCreateView=====");
        View view = inflater.inflate(R.layout.fragment_item, container, false);
        initView(view);
        initData();
        return view;
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
//        Toast.makeText(getActivity(), roomName+", size: "+deviceInfoList.size(), Toast.LENGTH_SHORT).show();
/*       new Thread(new Runnable() {
            @Override
            public void run() {
                if(Const.isRemote){
                    initIotMqttMessage();
//                    MqttDesiredJStrBuilder builder = new MqttDesiredJStrBuilder(Const.subscriptionTopic);
                    MqttDesiredJStrBuilder builder = new MqttDesiredJStrBuilder(HomeActivity.shadowTopic);
                    builder.setJsonString( CloudIotData.getDeviceListCommand(roomName));
                    AskeyIoTService.getInstance(ZwaveClientApplication.getInstance()).publishDesiredMessage(HomeActivity.shadowTopic, builder);
                }else{
                    Logg.i(LOG_TAG,"===getScene=====");
//                    TcpClient.getInstance().rigister(tcpReceive);
//                    MQTTManagement.getSingInstance().rigister( mqttMessageArrived);
                }

            }
        }).start();*/
    }

    @Override
    public void onPause() {
        super.onPause();
        Logg.i(LOG_TAG, "===onPause=====" + roomName);
        unrigister();
    }

    private void unrigister() {
        if (mqttMessageArrived != null) {
            MQTTManagement.getSingInstance().unrigister(mqttMessageArrived);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Logg.i(LOG_TAG, "===onStop=====");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Logg.i(LOG_TAG, "===onDestroyView=====");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logg.i(LOG_TAG, "onDestroy" + roomId);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Logg.i(LOG_TAG, "onDetach" + roomId);
    }

    MqttMessageArrived mqttMessageArrived = new MqttMessageArrived() {
        @Override
        public void mqttMessageArrived(String topic, MqttMessage message) {
            mqttResult = new String(message.getPayload());
            Logg.i(LOG_TAG, "=mqttMessageArrived=>=topic=" + topic);
            Logg.i(LOG_TAG, "=mqttMessageArrived=>=message=" + mqttResult);
            if (mqttResult.contains("desired")) {
                return;
            }
            if (mqttResult.contains(roomName)) {
                Logg.i(LOG_TAG, "=mqttMessageArrived=>=message=" + mqttResult);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isHaveDevice(true);
                    }
                });
                getSceneResult(mqttResult);
            }
        }
    };


    private void initIotMqttMessage() {
        //以下这句为注册监听
        AskeyIoTService.getInstance(getContext()).setShadowReceiverListener(new ShadowReceiveListener() {
            @Override
            public void receiveShadowDocument(String s, String s1, String s2) {
                Logg.i(LOG_TAG, "==IotMqttMessageCallback====setShadowReceiverListener==s=" + s);
                Logg.i(LOG_TAG, "==IotMqttMessageCallback====setShadowReceiverListener==s1=" + s1);
                Logg.i(LOG_TAG, "==IotMqttMessageCallback====setShadowReceiverListener==s2=" + s2);
                IotMqttManagement.getInstance().receiveMqttMessage(s, s1, s2);
                if (s2.contains("desired")) {
                    return;
                }
                getSceneResult(s2);
            }
        });
    }

    private void getSceneResult(String mqttResult) {
        final JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(mqttResult);
            String reported = jsonObject.optString("reported");
            JSONObject reportedObject = new JSONObject(reported);
            String Interface = reportedObject.optString("Interface");
            String DeviceList = reportedObject.optString("deviceList");
            JSONArray columnInfo = new JSONArray(DeviceList);
            int size = columnInfo.length();
            Logg.i(LOG_TAG, "columnInfo.length" + size);
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
                    Logg.i(LOG_TAG, "==getDeviceResult=JSONArray===devName==" + name);
//                    String nodeInfo = info.getString("nodeInfo");
                    DeviceInfo deviceInfo = new DeviceInfo();
                    deviceInfo.setDeviceId(nodeId);
                    deviceInfo.setDisplayName(name);
                    deviceInfo.setDeviceType(category);
                    deviceInfo.setRooms(Room);
                    deviceInfo.setIsFavorite(isFavorite);
//                    deviceInfo.setNodeInfo(nodeInfo);
                    deviceInfoList.add(deviceInfo);

                    String nodeTopic = Const.subscriptionTopic + "Zwave" + nodeId;
                    // 订阅新设备的topic为 sn + nodeId
                    MQTTManagement.getSingInstance().subscribeToTopic(nodeTopic, null);
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

    private void initView(View view) {

        Bundle bundle = getArguments();
        roomName = bundle.getString(ROOM_NAME);
        roomId = bundle.getInt(ROOM_ID);
        Log.d("info", roomId + ", " + roomId);

        room_name = (TextView) view.findViewById(R.id.room_name);
        room_name.setText(roomName);
        notify_layout = (LinearLayout) view.findViewById(R.id.notify_layout);
        edit_layout = (LinearLayout) view.findViewById(R.id.edit_layout);
        no_device_layout = (LinearLayout) view.findViewById(R.id.no_device_layout);
        add_first_device = (ImageView) view.findViewById(R.id.add_first_device);
        add_first_device.setOnClickListener(this);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        isHaveDevice(false);
    }


    private void initData() {
        deviceInfoList = new ArrayList<>();

        adapter = new DeviceAdapter();
        adapter.setDeviceInfoList(deviceInfoList);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    public void setCurrentMode(int flag) {
        switch (flag) {
            case DeviceAdapter.NORMAL_MODE:
                if (deviceInfoList.size() == 0) {
                    isHaveDevice(false);
                }
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

    public void notifyFragmentData(DeviceInfo deviceInfo) {
        if (deviceInfoList.size() == 0) {
            isHaveDevice(true);
        }
        deviceInfoList.add(deviceInfo);
        adapter.notifyDataSetChanged();
    }

    public void removeDevice() {
        if (deviceInfoList.size() > 0) {
            deviceInfoList.remove(clickPosition);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(View view, DeviceInfo deviceInfo) {
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
        }
        intent.putExtra("nodeId", deviceInfo.getDeviceId());
        intent.putExtra("type", deviceInfo.getDeviceType());
        intent.putExtra("room", deviceInfo.getRooms());
        intent.putExtra("displayName", deviceInfo.getDisplayName());
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(View view, DeviceInfo deviceInfo) {
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
        }
        intent.putExtra("nodeId", deviceInfo.getDeviceId());
        intent.putExtra("type", deviceInfo.getDeviceType());
        intent.putExtra("room", deviceInfo.getRooms());
        intent.putExtra("displayName", deviceInfo.getDisplayName());
        startActivity(intent);
    }

    @Override
    public void deleteItemClick(final int position) {
//        Toast.makeText(getActivity(), position+" ,size: "+deviceInfoList.size(), Toast.LENGTH_SHORT).show();
        DeviceInfo info = deviceInfoList.get(position);
        //deviceId就是nodeId
        showDialog(getActivity(), info.getDisplayName(), info.getDeviceId());
        clickPosition = position;
    }

    @Override
    public void addItemClick() {
        startActivity(new Intent(getActivity(), SelectBrandActivity.class));
        Const.currentRoomName = roomName;
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_first_device:
                startActivity(new Intent(getActivity(), SelectBrandActivity.class));
                Const.currentRoomName = roomName;
                break;
        }
    }

    public void isHaveDevice(boolean tag) {
        if (tag) {
            recyclerView.setVisibility(View.VISIBLE);
            no_device_layout.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            no_device_layout.setVisibility(View.VISIBLE);
        }
    }

    public List<DeviceInfo> getDeviceList() {
        return deviceInfoList;
    }
}
