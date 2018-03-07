package com.askey.mobile.zwave.control.home.fragment.roomitem;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseFragment;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.model.DeviceInfo;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.BulbActivity;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.PlugActivity;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.WallMoteLivingActivity;
import com.askey.mobile.zwave.control.home.activity.addDevice.SelectBrandActivity;
import com.askey.mobile.zwave.control.home.adapter.DeviceAdapter;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;
import com.askey.mobile.zwave.control.util.ToastShow;
import com.askey.mobile.zwave.control.widget.MyDialog;
import com.askeycloud.sdk.device.response.IoTDeviceInfoResponse;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.askey.mobile.zwave.control.home.fragment.RoomsFragment.roomInfoList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ItemRoomFragment extends BaseFragment implements DeviceAdapter.OnItemClickListener, View.OnClickListener {
    public static String LOG_TAG = "ItemRoomFragment";
    private RecyclerView recyclerView;
    private DeviceAdapter adapter;
    private LinearLayout notify_layout, no_device_layout;
    private RelativeLayout edit_layout;
    private ImageView add_first_device;
    private TextView room_name;
    private final static String ROOM_ID = "roomId";
    private final static String ROOM_NAME = "roomName";
    private String roomName;
    private Integer roomId;
    private Bundle bundle;
    private String mqttResult;
    private EditText edt_name;
    private MyDialog myDialog;
    private List<DeviceInfo> deviceInfoList;
    private boolean isFirstLoadData = true;
    private int clickPosition;
    private String currentRoomName;
    private IoTDeviceInfoResponse ioTDeviceInfoResponse;
    private String shadowTopic = "";

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
        if (isVisibleToUser) {
            Log.d(LOG_TAG, "正常加载,fragment可见" + roomName);
            MQTTManagement.getSingInstance().rigister(mqttMessageArrived);
            /*
                在第一次点击底部tab跳转到房间页面的时候会初始化所有fragment，此时My Home页面是可见的，其他子页面是不可见的。这里处理的是本页面第一次
                可见时发送消息，若后续通过滑动再次回到本页面则不会发送消息，这样做是为了避免快速滑动时本页面的消息被其他页面接收到。
             */
            if (isFirstLoadData) {
                isFirstLoadData = false;
                showWaitingDialog();
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getDeviceListCommand(roomName));
            }
        } else {
            Log.d(LOG_TAG, "正常加载,fragment不可见");
            unrigister();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_item, container, false);
        initView(view);
        initData();
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);解决fragment重叠的问题
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
    }

    @Override
    public void onPause() {
        super.onPause();
//        unrigister();
    }

    private void unrigister() {
        if (mqttMessageArrived != null) {
            MQTTManagement.getSingInstance().unrigister(mqttMessageArrived);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    MqttMessageArrived mqttMessageArrived = new MqttMessageArrived() {
        @Override
        public void mqttMessageArrived(String topic, MqttMessage message) {
            mqttResult = new String(message.getPayload());
            Logg.i(LOG_TAG, "=mqttMessageArrived=>=message=" + mqttResult);
            if (mqttResult.contains("desired")) {
                return;
            }
            if (mqttResult.contains(roomName)) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isHaveDevice(true);
                        stopWaitDialog();
                    }
                });
                getSceneResult(mqttResult);
            }
            if (mqttResult.contains("editRoom")){
                getEditRoomNameResult(mqttResult);
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(LOG_TAG,"1111111111111111");
                    stopWaitDialog();//页面可能会收到其他的一些消息，在这里停止可能会有问题
                }
            });
            sensorChange();
        }
    };

    private void getEditRoomNameResult(String mqttResult) {
        stopWaitDialog();
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(mqttResult);
            String reported = jsonObject.optString("reported");
            JSONObject reportedObject = new JSONObject(reported);
            final String result = reportedObject.optString("status");
            ((Activity) getContext()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (result.equals("Success")) {
                        room_name.setText(currentRoomName);
                        for (int i = 0; i < roomInfoList.size(); i++) {
                            if (roomInfoList.get(i).getRoomName().equals(roomName)) {
                                roomInfoList.get(i).setRoomName(currentRoomName);
                                break;
                            }
                        }
                        roomName = currentRoomName;
                    } else {
                        Toast.makeText(getActivity(), "modify failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

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

    private void parsingGetDeviceList(String deviceList) {
        try {
            JSONArray columnInfo = new JSONArray(deviceList);
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
//                    String nodeInfo = info.getString("nodeInfo");
                    DeviceInfo deviceInfo = new DeviceInfo();
                    deviceInfo.setDeviceId(nodeId);
                    deviceInfo.setDisplayName(name);
                    deviceInfo.setDeviceType(category);
                    deviceInfo.setRooms(Room);
                    deviceInfo.setIsFavorite(isFavorite);
//                    deviceInfo.setNodeInfo(nodeInfo);
                    deviceInfoList.add(deviceInfo);

//                    if(devType.equals("PLUG") || devType.equals("BULB")){
//                        if (Const.isRemote) {
//                            if(devType.equals("PLUG") || devType.equals("BULB")){
//                                String nodeTopic = Const.subscriptionTopic + "Zwave" + nodeId;
//                                remoteIotComm(nodeTopic, CloudIotData.getSwitchStatus(nodeId));
//                            }
//                        }else{
//                            String nodeTopic = Const.subscriptionTopic + "Zwave" + nodeId;
//                            MQTTManagement.getSingInstance().publishMessage(nodeTopic, LocalMqttData.getSwitchStatus(nodeId));
//                        }
//                    }
                    if ("SENSOR".equals(category)) {
                        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+"Zwave"+nodeId, LocalMqttData.getSensorMultiLevel(nodeId));
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

    private void changeLuminance() {
        for (DeviceInfo deviceInfo:deviceInfoList) {
            if ("BULB".equals(deviceInfo.getDeviceType())) {
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic +"Zwave"+ deviceInfo.getDeviceId(), LocalMqttData.setBrigtness(deviceInfo.getDeviceId(),"70"));
            }
        }
    }

    private void openAllBulb() {
        for (DeviceInfo deviceInfo:deviceInfoList) {
            if ("BULB".equals(deviceInfo.getDeviceType())) {
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic +"Zwave"+ deviceInfo.getDeviceId(), LocalMqttData.setSwitch(deviceInfo.getDeviceId(),"on"));
            }
        }
    }

    private void getSceneResult(String mqttResult) {
        final JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(mqttResult);
            String reported = jsonObject.optString("reported");
            JSONObject reportedObject = new JSONObject(reported);
            String Interface = reportedObject.optString("Interface");
            if (Interface.equals("getDeviceList")) {
                String deviceList = reportedObject.optString("deviceList");
                parsingGetDeviceList(deviceList);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initView(View view) {

        Bundle bundle = getArguments();
        roomName = bundle.getString(ROOM_NAME);
        roomId = bundle.getInt(ROOM_ID);
        Log.d("info", roomName + ", " + roomId);

        room_name = (TextView) view.findViewById(R.id.room_name);
        room_name.setText(roomName);
        edt_name = (EditText) view.findViewById(R.id.edt_name);
        edt_name.setText(roomName);
        notify_layout = (LinearLayout) view.findViewById(R.id.notify_layout);
        edit_layout = (RelativeLayout) view.findViewById(R.id.edit_layout);
        no_device_layout = (LinearLayout) view.findViewById(R.id.no_device_layout);
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

    public void setCurrentMode(int flag) {
        Log.d(LOG_TAG,roomName);
        switch (flag) {
            case DeviceAdapter.NORMAL_MODE:
                notify_layout.setVisibility(View.VISIBLE);
                edit_layout.setVisibility(View.GONE);
                if (deviceInfoList.size() == 0) {
                    isHaveDevice(false);
                }
                currentRoomName = edt_name.getText().toString();
                if (currentRoomName.equals(roomName)) {

                } else if (currentRoomName.equals("")) {
                    Toast.makeText(getActivity(), "Room Name can not be null", Toast.LENGTH_SHORT).show();
                } else {
                    for (int i = 0; i < roomInfoList.size(); i++) {
                        if (currentRoomName.equals(roomInfoList.get(i).getRoomName())) {
                            Toast.makeText(getActivity(), "Current room name already existed", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        if (currentRoomName.equals("My Home") || currentRoomName.equals("MyHome")) {
                            Toast.makeText(getActivity(), "Room Name can not be \"My Home\"", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                    showWaitingDialog();
                    modifyRoomName(mqttMessageArrived, roomName, currentRoomName);
                }
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
            ToastShow.showToast(mContext, getResources().getString(R.string.extender_no_contrl));
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
            ToastShow.showToast(mContext, getResources().getString(R.string.extender_no_contrl));
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

        startActivity(intent);
    }

    @Override
    public void deleteItemClick(final int position) {
//        Toast.makeText(getActivity(), position+" ,size: "+deviceInfoList.size(), Toast.LENGTH_SHORT).show();
        DeviceInfo info = deviceInfoList.get(position);
        //deviceId就是nodeId
        showDeleteDeviceDialog(getActivity(), roomName, info.getDeviceId());
        clickPosition = position;
    }

    @Override
    public void addItemClick() {
        startActivity(new Intent(getActivity(), SelectBrandActivity.class));
        Const.currentRoomName = roomName;
    }

//    void showDialog(final Context context, final String name, final String nodeId) {
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
//                intent.putExtra("roomName", name);
//                startActivity(intent);
//                alertDialog.dismiss();
//            }
//        });
//
//    }

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
