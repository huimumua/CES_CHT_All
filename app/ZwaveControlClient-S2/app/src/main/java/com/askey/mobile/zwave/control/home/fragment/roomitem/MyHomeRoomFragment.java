package com.askey.mobile.zwave.control.home.fragment.roomitem;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseFragment;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.model.DeviceInfo;
import com.askey.mobile.zwave.control.deviceContr.scenes.DeviceTestActivity;
import com.askey.mobile.zwave.control.home.activity.HomeActivity;
import com.askey.mobile.zwave.control.home.activity.addDevice.Security2CmdSupportedActivity;
import com.askey.mobile.zwave.control.home.activity.addDevice.SelectBrandActivity;
import com.askey.mobile.zwave.control.home.activity.addDevice.SmartStartDeviceAddActivity;
import com.askey.mobile.zwave.control.home.adapter.DeviceAdapter;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;
import com.askey.mobile.zwave.control.util.PreferencesUtils;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MyHomeRoomFragment extends BaseFragment implements DeviceAdapter.OnItemClickListener, View.OnClickListener {

    public static String LOG_TAG = "MyHomeRoomFragment";
    private RecyclerView recyclerView;
    private DeviceAdapter adapter;
    private LinearLayout notify_layout;
    private RelativeLayout edit_layout, no_device_layout;
    private ImageView add_first_device;
    private String mqttResult;
    private final static String ROOM_ID = "roomId";
    private final static String ROOM_NAME = "roomName";
    private String roomName;
    private TextView room_name;
    private Integer roomId;
    private EditText edt_name;
    private int clickPosition;
    private List<DeviceInfo> deviceInfoList;
    private boolean isFirstLoad = true;
    private boolean isVisibleToUser;
    private boolean isFirst2onResume = true;
    private String clickNodeId;
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

    public static MyHomeRoomFragment newInstance() {
        MyHomeRoomFragment itemFragment = new MyHomeRoomFragment();

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

            if (!isFirst2onResume) {
                showWaitingDialog();
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getDeviceListCommand(roomName));
            }

//            if (Const.RESET_ROOMS) {
//               // MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getDeviceListCommand(roomName));
//                if (deviceInfoList.size() > 0) {
//                    deviceInfoList.clear();
//                }
//                adapter.notifyDataSetChanged();
//                Const.RESET_ROOMS = false;
//            }

        } else {
            Log.d(LOG_TAG, "正常加载,fragment不可见");
            unrigister();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("=======ddddd", "" + "onCreateView");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_home_room, container, false);
        initView(view);
        initData();
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);解决fragment重叠的问题
    }

    private void initView(View view) {

        Bundle bundle = getArguments();
        roomName = bundle.getString(ROOM_NAME);
        roomId = bundle.getInt(ROOM_ID);

        room_name = (TextView) view.findViewById(R.id.room_name);
        edt_name = (EditText) view.findViewById(R.id.edt_name);
        edt_name.setText(roomName);
        notify_layout = (LinearLayout) view.findViewById(R.id.notify_layout);
        edit_layout = (RelativeLayout) view.findViewById(R.id.edit_layout);
        no_device_layout = (RelativeLayout) view.findViewById(R.id.no_device_layout);
        add_first_device = (ImageView) view.findViewById(R.id.add_first_device);
        add_first_device.setOnClickListener(this);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        isHaveDevice(false);
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
            Logg.i(LOG_TAG, "=mqttMessageArrived=>=topic=" + mqttResult);
            if (mqttResult.contains("desired")) {
                return;
            }
            stopWaitDialog();
            getDeviceList(mqttResult);
            sensorChange();
        }
    };

    private void sensorChange() {
        final JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(mqttResult);
            String reported = jsonObject.optString("reported");
            JSONObject reportedObject = new JSONObject(reported);
            String Interface = reportedObject.optString("Interface");

           /*else if ("getSensorMultiLevel".equals(Interface)) {*/
            if ("Luminance sensor".equals(reportedObject.optString("type"))) {//亮度
                Log.i(LOG_TAG, "value" + reportedObject.optString("value"));
                if (Integer.valueOf(reportedObject.optString("value")) < 50) {
                    //调整亮度
                    Log.i(LOG_TAG, "changeLuminance");
                    changeLuminance();
                }
            }
//            }

            if ("Notification Get Information".equals(reportedObject.optString("MessageType"))) {
                if ("Window/door is open".equals(reportedObject.optString("Notification-event"))) {
                    openAllBulb();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void getDeviceList(String mqttResult) {

        final JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(mqttResult);
            String reported = jsonObject.optString("reported");
            JSONObject reportedObject = new JSONObject(reported);
            String messageType = reportedObject.optString("MessageType");
//            if (mqttResult.contains(roomName)) {
//                Logg.i(LOG_TAG, "=mqttMessageArrived=>=message=" + mqttResult);
////                deviceInfoList.clear();
////                deviceInfoList = LocalJsonParsing. getDeviceList(mqttResult);
////                ((Activity) getContext()).runOnUiThread(new Runnable() {
////                    @Override
////                    public void run() {
////                        adapter.notifyDataSetChanged();
////                    }
////                });
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        isHaveDevice(true);//修改UI
//                    }
//                });

            String Interface = reportedObject.optString("Interface");

            if (Interface.equals("sendNodeInformation")) {

                String result = reportedObject.optString("result");
                if (result.equals("true")) {
                    Const.isSendNIF=result;
                }
            }

            if (Interface.equals("getDeviceList")) {
                String DeviceList = reportedObject.optString("deviceList");
                JSONArray columnInfo = new JSONArray(DeviceList);
                int size = columnInfo.length();
//                    if (size > 0) {
                deviceInfoList.clear();
                for (int i = 0; i < size; i++) {
                    JSONObject info = columnInfo.getJSONObject(i);
                    String nodeId = info.getString("nodeId");
                    String brand = info.getString("brand");
                    String devType = info.getString("deviceType");
                    String category = info.getString("category");
                    String Room = info.getString("room");
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
//                        if(devType.equals("PLUG") || devType.equals("BULB")){
//                            String nodeTopic = Const.subscriptionTopic + "Zwave" + nodeId;
//                            MQTTManagement.getSingInstance().publishMessage(nodeTopic, LocalMqttData.getSwitchStatus(nodeId));
//                        }
                    if ("SENSOR".equals(category)) {
                        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic + "Zwave" + nodeId, LocalMqttData.getSensorMultiLevel(nodeId));
                    }

                }
//                    }
                ((Activity) getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        if (deviceInfoList.size() > 0) {
                            isHaveDevice(true);//修改UI
                        } else {
                            isHaveDevice(false);//修改UI
                        }
                        Log.i(LOG_TAG, "deviceInfoList=" + deviceInfoList.size());
                    }
                });
            }
//            }

//            if ("Node Is Failed Check Report".equals(messageType)) {
//                final String status = reportedObject.optString("Status");
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if ("Alive".equals(status)) {
//                            showDeleteDeviceDialog(getActivity(), roomName, clickNodeId);
//                        } else {
//                            showFailDeleteDeviceDialog(getActivity(), roomName, clickNodeId);
//                        }
//                    }
//                });
//            }

            if ("Node Add Status".equals(messageType)) {
                Log.i(LOG_TAG, "----------Node Add Status---------");
                Intent smartStartIntent = new Intent(getContext(), SmartStartDeviceAddActivity.class);
                startActivity(smartStartIntent);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * HomeActivity的监听onNavigationItemSelected 调用了此函数,
     * 相应的menu执行了get all dsk/remove all dsk
     * 调用方式：通过ScenesFragment的实例 ScenesFragment.newInstance().responseMenu(int action);
     *
     * @param action 相应的动作
     */
    public void responseMenu(int action) {

        if (action == 4) {
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.sendNodeInformation("1"));
        }


    }

    private void changeLuminance() {
        for (DeviceInfo deviceInfo : deviceInfoList) {
            if ("BULB".equals(deviceInfo.getDeviceType())) {
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic + "Zwave" + deviceInfo.getDeviceId(), LocalMqttData.setBrigtness(deviceInfo.getDeviceId(), "70"));
            }
        }
    }

    private void openAllBulb() {
        for (DeviceInfo deviceInfo : deviceInfoList) {
            if ("BULB".equals(deviceInfo.getDeviceType())) {
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic + "Zwave" + deviceInfo.getDeviceId(), LocalMqttData.setSwitch(deviceInfo.getDeviceId(), "on"));
            }
        }
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

    @Override
    public void onItemClick(View view, DeviceInfo deviceInfo) {

        Intent intent = new Intent(getActivity(), DeviceTestActivity.class);
        intent.putExtra("nodeId", deviceInfo.getDeviceId());
        intent.putExtra("deviceName", deviceInfo.getDisplayName());
        startActivity(intent);

/*        Intent intent = null;
        if ("BULB".equals(deviceInfo.getDeviceType())) {
            intent = new Intent(getActivity(), BulbActivity.class);
        } else if ("PLUG".equals(deviceInfo.getDeviceType())) {
            intent = new Intent(getActivity(), PlugActivity.class);
        } else if ("WALLMOTE".equals(deviceInfo.getDeviceType())) {
            intent = new Intent(getActivity(), WallMoteLivingActivity.class);
        } else if ("EXTENDER".equals(deviceInfo.getDeviceType())) {
            ToastShow.showToast(mContext, getResources().getString(R.string.extender_no_contrl));
            return;
        } else if ("DIMMER".equals(deviceInfo.getDeviceType())) {
            intent = new Intent(getActivity(), DimmerActivity.class);
        } else if ("SENSOR".equals(deviceInfo.getDeviceType())) {
            intent = new Intent(getActivity(), SensorActivity.class);
        }
        if(intent == null){
            ToastShow.showShort(mContext,getResources().getString(R.string.device_not_exist));
            return;
        }
        intent.putExtra("nodeId", deviceInfo.getDeviceId());
        intent.putExtra("type", deviceInfo.getDeviceType());
        intent.putExtra("room", deviceInfo.getRooms());
        intent.putExtra("shadowTopic", deviceInfo.getTopic());
        intent.putExtra("displayName", deviceInfo.getDisplayName());
        startActivity(intent);*/

//        String nodeId = deviceInfo.getDeviceId();
//        if (mOnOff.isChecked()) {
//            //需验证ff 和 00  set无返回
//            if (Const.isRemote) {
//                remoteIotComm(Const.subscriptionTopic+"Zwave"+nodeId,CloudIotData.setSwitch(nodeId,"on"));
//            } else {
//                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+"Zwave"+nodeId, LocalMqttData.setSwitch(nodeId,"on"));
//            }
//        } else {
//            if (Const.isRemote) {
//                remoteIotComm(Const.subscriptionTopic+"Zwave"+nodeId,CloudIotData.setSwitch(nodeId,"off"));
//            } else {
//                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+"Zwave"+nodeId, LocalMqttData.setSwitch(nodeId, "off"));
//            }
//        }

    }

    @Override
    public void onItemLongClick(View view, DeviceInfo deviceInfo) {
/*//        SENSOR/BULB/DIMMER/PLUG
        Intent intent = null;
        if ("BULB".equals(deviceInfo.getDeviceType())) {
            intent = new Intent(getActivity(), BulbActivity.class);
        } else if ("PLUG".equals(deviceInfo.getDeviceType())) {
            intent = new Intent(getActivity(), PlugActivity.class);
        } else if ("WALLMOTE".equals(deviceInfo.getDeviceType())) {
            intent = new Intent(getActivity(), WallMoteLivingActivity.class);
        } else if ("EXTENDER".equals(deviceInfo.getDeviceType())) {
            ToastShow.showToast(mContext,getResources().getString(R.string.extender_no_contrl));
            return;
        }
        if(intent == null){
            ToastShow.showShort(mContext,getResources().getString(R.string.device_not_exist));
            return;
        }
        intent.putExtra("nodeId", deviceInfo.getDeviceId());
        intent.putExtra("type", deviceInfo.getDeviceType());
        intent.putExtra("room", deviceInfo.getRooms());
        intent.putExtra("shadowTopic", deviceInfo.getTopic());
        intent.putExtra("displayName", deviceInfo.getDisplayName());
        startActivity(intent);*/
        if (!deviceInfo.getDeviceId().equals("1")) {
            Intent intent = new Intent(getActivity(), Security2CmdSupportedActivity.class);
            intent.putExtra("nodeId", deviceInfo.getDeviceId());
            intent.putExtra("deviceName", deviceInfo.getDisplayName());
            startActivity(intent);
        }
    }

    @Override
    public void deleteItemClick(final int position) {
        DeviceInfo info = deviceInfoList.get(position);
        //deviceId就是nodeId
        //MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.checkNodeIsFailed(info.getDeviceId()));
        clickNodeId = info.getDeviceId();
        clickPosition = position;
    }

//    void showDialog(final Context context, String deviceName, final String nodeId) {
//        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
//        final AlertDialog alertDialog = alertDialogBuilder.show();
//        alertDialog.setCancelable(false);
//        View view = LayoutInflater.from(context).inflate(R.layout.dialog_delete_device, null);
//        ImageView icon = (ImageView) view.findViewById(R.id.iv_icon);
//        icon.setImageResource(R.drawable.ic_del_icon);
//        alertDialog.setContentView(view);
//        Button cancel = (Button) view.findViewById(R.id.btn_cancel);
//        Button proceed = (Button) view.findViewById(R.id.btn_proceed);
//        cancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                alertDialog.dismiss();
//            }
//        });
//        proceed.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(context, DeleteDeviceActivity.class);
//                intent.putExtra("deviceId", nodeId);
//                intent.putExtra("roomName", roomName);
//                startActivity(intent);
//                alertDialog.dismiss();
//            }
//        });
//
//    }

    @Override
    public void addItemClick() {
        startActivity(new Intent(getActivity(), SelectBrandActivity.class));
        Const.currentRoomName = roomName;
    }

    public void notifyFragmentData(DeviceInfo deviceInfo) {
        if (deviceInfoList.size() == 0) {
            isHaveDevice(true);
        }
        if (deviceInfo != null) {
            deviceInfoList.add(deviceInfo);
        }
        adapter.notifyDataSetChanged();
    }

    public void deleteDevice() {
        if (deviceInfoList.size() > 0) {
            deviceInfoList.remove(clickPosition);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        /*
            当fragment可见时才发送命令，为了防止其他界面弹窗导致全部fragment走生命周期，使消息接收错乱，这里仅仅允许此fragment第一次初始化的时候
            在onResume里面发送请求命令
         */
        Log.i(LOG_TAG, "==========onResume==========");
        MQTTManagement.getSingInstance().rigister(mqttMessageArrived);
        if (isVisibleToUser && isFirst2onResume) {
            showWaitingDialog();
            isFirst2onResume = false;
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getDeviceListCommand(roomName));
        }

        if (Const.RESET_ROOMS) {
            // MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getDeviceListCommand(roomName));
            if (deviceInfoList.size() > 0) {
                deviceInfoList.clear();
            }
            adapter.notifyDataSetChanged();
            Const.RESET_ROOMS = false;
        }

        if (Const.getIsDataChange()) {
            Const.setIsDataChange(false);
            showWaitingDialog();
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getDeviceListCommand(roomName));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
//        unrigister();
    }


    @Override
    public void onStop() {
        super.onStop();

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
    }

    @Override
    public void onDetach() {
        super.onDetach();
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
