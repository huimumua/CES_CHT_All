package com.askey.mobile.zwave.control.home.fragment.roomitem;


import android.app.Activity;
import android.content.Context;
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
import android.widget.TextView;
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
public class ItemRoomFragment extends Fragment implements DeviceAdapter.OnItemClickListener{
    public static String LOG_TAG = "ItemRoomFragment";
    private RecyclerView recyclerView;
    private DeviceAdapter adapter;
    private LinearLayout notify_layout, edit_layout;
    private TextView room_name;
    private final static String ROOM_ID = "roomId";
    private final static String ROOM_NAME = "roomName";
    private String roomName;
    private Integer roomId;
    private Bundle bundle;
    private String  mqttResult;
    private MyDialog myDialog;
    private List<DeviceInfo> deviceInfoList;
    private boolean isFirstLoadData = true;
    private static int clickPositon;

    public ItemRoomFragment() {
        // Required empty public constructor
    }

    public static ItemRoomFragment newInstance(int id, String name){
        ItemRoomFragment itemFragment = new ItemRoomFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ROOM_ID,id);
        bundle.putString(ROOM_NAME,name);
        itemFragment.setArguments(bundle);
        return itemFragment;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Logg.i(LOG_TAG,"===setUserVisibleHint=====");
        if (isVisibleToUser) {
            Log.d(LOG_TAG,"正常加载,fragment可见"+roomName);
            TcpClient.getInstance().rigister(tcpReceive);
            MQTTManagement.getSingInstance().rigister( mqttMessageArrived);
            if (isFirstLoadData) {
                isFirstLoadData = false;
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getDeviceListCommand(roomName));
            }
        } else {
            Log.d("taggg","正常加载,fragment不可见");
            unrigister();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Logg.i(LOG_TAG,"===onAttach=====");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logg.i(LOG_TAG,"===onCreate=====");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Logg.i(LOG_TAG,"===onCreateView=====");
        View view = inflater.inflate(R.layout.fragment_item, container, false);
        initView(view);
        initData();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logg.i(LOG_TAG,"===onActivityCreated=====");
    }

    @Override
    public void onStart() {
        super.onStart();
        Logg.i(LOG_TAG,"===onStart=====");
    }

    @Override
    public void onResume() {
        super.onResume();
        Logg.i(LOG_TAG,"===onResume=====");
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
        Logg.i(LOG_TAG,"===onPause=====");
        unrigister();
    }

    private void unrigister() {
        if(mqttMessageArrived!=null){
            MQTTManagement.getSingInstance().unrigister(mqttMessageArrived);
        }
        if (tcpReceive != null) {
            TcpClient.getInstance().unrigister(tcpReceive);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Logg.i(LOG_TAG,"===onStop=====");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Logg.i(LOG_TAG,"===onDestroyView=====");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logg.i(LOG_TAG,"onDestroy"+roomId);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Logg.i(LOG_TAG,"onDetach"+roomId);
    }

    MqttMessageArrived mqttMessageArrived = new MqttMessageArrived() {
        @Override
        public void mqttMessageArrived(String topic, MqttMessage message) {
            mqttResult = new String(message.getPayload());
            Logg.i(LOG_TAG,"=mqttMessageArrived=>=topic="+topic);
            if(mqttResult.contains("desired")){
                return;
            }
            if (mqttResult.contains(roomName)) {
                Logg.i(LOG_TAG,"=mqttMessageArrived=>=message="+mqttResult);
                getSceneResult(mqttResult);
            }
        }
    };


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

        //以下这句为注册监听
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
            if( size > 0 ){
                deviceInfoList.clear();
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initView(View view) {

        Bundle bundle = getArguments();
        roomName = bundle.getString(ROOM_NAME);
        roomId = bundle.getInt(ROOM_ID);
        Log.d("info",roomId+", "+roomId);

        room_name = (TextView) view.findViewById(R.id.room_name);
        room_name.setText(roomName);
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

    public String getRoomName(){
        return roomName;
    }

    public void notifyFragmentData(DeviceInfo deviceInfo){
        deviceInfoList.add(deviceInfo);

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(View view, DeviceInfo deviceInfo) {
        Toast.makeText(getActivity(), "item", Toast.LENGTH_SHORT).show();
        Intent intent = null;
        if ("SENSOR".equals(deviceInfo.getDeviceType())) {
            intent = new Intent(getActivity(), SensorManageActivity.class);
            intent.putExtra("roomName",deviceInfo.getRooms());
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
        }  else {
            intent = new Intent(getActivity(), BulbActivity.class);//原有的经验证过的可以正常使用
            intent.putExtra("uniqueid",deviceInfo.getUniqueId());
        }
        intent.putExtra("nodeId",deviceInfo.getDeviceId());
        intent.putExtra("displayName",deviceInfo.getDisplayName());
        startActivity(intent);
    }

    @Override
    public void deleteItemClick(final int position) {
/*        myDialog = new MyDialog(getActivity());
        myDialog.setYesOnclickListener("Proceed", new MyDialog.onYesOnclickListener() {
            @Override
            public void onYesClick() {
                if (TcpClient.getInstance().isConnected()) {
                    Logg.i(LOG_TAG,"=removeDevice="+"mobile_zwave:removeDevice");
                    String nodeId = deviceInfoList.get(position).getDeviceId();
                    TcpClient.getInstance().getTransceiver().send("mobile_zwave:removeDevice"+nodeId);
//                    MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.removeDevice(nodeId));
                }
                deviceInfoList.remove(position);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, deviceInfoList.size());
                myDialog.dismiss();
            }
        });

        myDialog.show();
        Toast.makeText(getActivity(), position + "", Toast.LENGTH_SHORT).show();*/

        myDialog = new MyDialog(getActivity());
        myDialog.setYesOnclickListener("Proceed", new MyDialog.onYesOnclickListener() {
            @Override
            public void onYesClick() {
                if (TcpClient.getInstance().isConnected()) {
                    String nodeId = deviceInfoList.get(position).getDeviceId();
                    Logg.i(LOG_TAG, "=removeDevice=" +"mobile_zwave:removeDevice:Zwave:"+ nodeId);
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
//        Toast.makeText(getActivity(), roomId+"", Toast.LENGTH_SHORT).show();
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
            Logg.i(LOG_TAG,"==tcpMassage=="+tcpMassage);
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
