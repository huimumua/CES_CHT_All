package com.askey.mobile.zwave.control.home.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.application.ZwaveClientApplication;
import com.askey.mobile.zwave.control.data.CloudIotData;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.IotMqttManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.model.DeviceInfo;
import com.askey.mobile.zwave.control.deviceContr.model.RoomInfo;
import com.askey.mobile.zwave.control.deviceContr.net.TcpClient;
import com.askey.mobile.zwave.control.home.activity.HomeActivity;
import com.askey.mobile.zwave.control.home.activity.TakePictureActivity;
import com.askey.mobile.zwave.control.home.activity.addDevice.DeleteDeviceActivity;
import com.askey.mobile.zwave.control.home.activity.addDevice.InstallSuccessActivity;
import com.askey.mobile.zwave.control.home.activity.addRoom.AddRoomActivity;
import com.askey.mobile.zwave.control.home.adapter.DeviceAdapter;
import com.askey.mobile.zwave.control.home.adapter.RoomsAdapter;
import com.askey.mobile.zwave.control.home.fragment.roomitem.ItemRoomFragment;
import com.askey.mobile.zwave.control.home.fragment.roomitem.MyHomeRoomFragment;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;
import com.askey.mobile.zwave.control.widget.MyViewPager;
import com.askeycloud.webservice.sdk.iot.callback.ShadowReceiveListener;
import com.askeycloud.webservice.sdk.iot.message.builder.MqttDesiredJStrBuilder;
import com.askeycloud.webservice.sdk.service.iot.AskeyIoTService;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class RoomsFragment extends Fragment implements View.OnClickListener, InstallSuccessActivity.AddDeviceSuccessListener
        , ViewPager.OnPageChangeListener, AddRoomActivity.ModifyRoomListener, DeleteDeviceActivity.DeleteDeviceListener {
    public static String LOG_TAG = "RoomsFragment";
    private MyViewPager roomPager;
    private List<Fragment> fragmentList;
    private Button add, deltet;
    private RoomsAdapter adapter;
    private ImageView menu, voice, edit;
    public static List<RoomInfo> roomInfoList;
    private Object o;
    private String mqttResult,mRoomName;

    public static RoomsFragment newInstance() {
        return new RoomsFragment();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(LOG_TAG,"setUserVisibleHint"+" ,"+isVisibleToUser);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logg.i(LOG_TAG, "===onCreate=====");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Logg.i(LOG_TAG, "===onCreateView=====");
        View view = inflater.inflate(R.layout.fragment_rooms, container, false);
        initView(view);
        initFragment();

        return view;
    }


    private void initView(View view) {
        roomPager = (MyViewPager) view.findViewById(R.id.room_pager);
        add = (Button) view.findViewById(R.id.add);
        deltet = (Button) view.findViewById(R.id.delete);
        menu = (ImageView) view.findViewById(R.id.menu_btn);
        edit = (ImageView) view.findViewById(R.id.edit);
        voice = (ImageView) view.findViewById(R.id.voice);
    }

    private void initFragment() {
        getRoomList();
        fragmentList = new ArrayList<>();
        for (int i = 0; i < roomInfoList.size(); i++) {
            if (roomInfoList.get(i).getRoomName().equals("My Home")) {
                fragmentList.add(MyHomeRoomFragment.newInstance(roomInfoList.get(i).getRoomId(), roomInfoList.get(i).getRoomName()));
            } else {
                fragmentList.add(ItemRoomFragment.newInstance(roomInfoList.get(i).getRoomId(), roomInfoList.get(i).getRoomName()));
            }
        }
        adapter = new RoomsAdapter(getChildFragmentManager(), fragmentList);
        roomPager.setAdapter(adapter);
        //保持所有的fragment实例和结构视图不被销毁，真是环境可以不要，这里只是为了模拟
        roomPager.setOffscreenPageLimit(adapter.getCount() - 1);
        roomPager.addOnPageChangeListener(this);
        deltet.setOnClickListener(this);
        add.setOnClickListener(this);

        menu.setOnClickListener(this);
        edit.setOnClickListener(this);
        voice.setOnClickListener(this);
        InstallSuccessActivity.setAddDeviceListener(this);
        AddRoomActivity.setModifyRoomListener(this);
        DeleteDeviceActivity.setDeleteDeviceListener(this);
        o = adapter.instantiateItem(roomPager, roomPager.getCurrentItem());
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
    }

    @Override
    public void onPause() {
        super.onPause();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            //添加房间
//            case R.id.add:
//                fragmentList.add(new ItemRoomFragment());
//                adapter.notifyDataSetChanged();
//                break;
//            //移除房间
//            case R.id.delete:
//                if (roomPager.getCurrentItem() != 0) {
//                    fragmentList.remove(roomPager.getCurrentItem());
//                    adapter.notifyDataSetChanged();
//                }
//                break;
            case R.id.menu_btn:
                if (o instanceof ItemRoomFragment) {
                    ItemRoomFragment itemFragment = (ItemRoomFragment) o;
                    switch (itemFragment.getCurrentMode()) {
                        case DeviceAdapter.NORMAL_MODE:
                            Activity activity = getActivity();
                            if (activity instanceof HomeActivity) {
                                ((HomeActivity) activity).toggleDrawerLayout();
                            }
                            break;
                        case DeviceAdapter.EDIT_MODE:
                            startActivity(new Intent(getActivity(), AddRoomActivity.class));
                            itemFragment.setCurrentMode(DeviceAdapter.NORMAL_MODE);
                            titleMode2Normal();
                            break;
                    }
                }
                if (o instanceof MyHomeRoomFragment) {
                    MyHomeRoomFragment itemFragment = (MyHomeRoomFragment) o;
                    switch (itemFragment.getCurrentMode()) {
                        case DeviceAdapter.NORMAL_MODE:
                            Activity activity = getActivity();
                            if (activity instanceof HomeActivity) {
                                ((HomeActivity) activity).toggleDrawerLayout();
                            }
                            break;
                        case DeviceAdapter.EDIT_MODE:
                            startActivity(new Intent(getActivity(), AddRoomActivity.class));
                            itemFragment.setCurrentMode(DeviceAdapter.NORMAL_MODE);
                            titleMode2Normal();
                            break;
                    }
                }
                break;
            case R.id.edit:
                o = adapter.instantiateItem(roomPager,roomPager.getCurrentItem());
                if (o instanceof MyHomeRoomFragment) {
                    MyHomeRoomFragment myHomeRoomFragment = (MyHomeRoomFragment) o;
                    switch (myHomeRoomFragment.getCurrentMode()) {
                        case DeviceAdapter.NORMAL_MODE:
                            myHomeRoomFragment.setCurrentMode(DeviceAdapter.EDIT_MODE);
                            titleMode2Edit();
                            break;
                        case DeviceAdapter.EDIT_MODE:
                            myHomeRoomFragment.setCurrentMode(DeviceAdapter.NORMAL_MODE);
                            titleMode2Normal();
                            break;
                    }
                }
                if (o instanceof ItemRoomFragment) {
                    ItemRoomFragment itemFragment = (ItemRoomFragment) o;
                    switch (itemFragment.getCurrentMode()) {

                        case DeviceAdapter.NORMAL_MODE:
                            itemFragment.setCurrentMode(DeviceAdapter.EDIT_MODE);
                            titleMode2Edit();
                            break;
                        case DeviceAdapter.EDIT_MODE:
                            itemFragment.setCurrentMode(DeviceAdapter.NORMAL_MODE);
                            titleMode2Normal();
                            break;
                    }
                }
                break;
            case R.id.voice:
                if (o instanceof MyHomeRoomFragment) {
                    MyHomeRoomFragment myHomeRoomFragment = (MyHomeRoomFragment) o;
                    switch (myHomeRoomFragment.getCurrentMode()) {
                        case DeviceAdapter.NORMAL_MODE:
                            Toast.makeText(getActivity(), "voice", Toast.LENGTH_SHORT).show();
                            break;
                        case DeviceAdapter.EDIT_MODE:
                            startActivity(new Intent(getActivity(), TakePictureActivity.class));
                            break;
                    }
                }
                if (o instanceof ItemRoomFragment) {
                    ItemRoomFragment itemFragment = (ItemRoomFragment) o;
                    switch (itemFragment.getCurrentMode()) {
                        case DeviceAdapter.NORMAL_MODE:
                            Toast.makeText(getActivity(), "voice", Toast.LENGTH_SHORT).show();
                            break;
                        case DeviceAdapter.EDIT_MODE:
                            startActivity(new Intent(getActivity(), TakePictureActivity.class));
                            break;
                    }
                }
                break;
        }
    }

    private void titleMode2Normal(){
        edit.setImageResource(R.drawable.ic_edit);
        voice.setImageResource(R.drawable.ic_voice);
        menu.setImageResource(R.drawable.ic_menu_btn);
        roomPager.setPagingEnable(true);
    }
    private void titleMode2Edit(){
        edit.setImageResource(R.drawable.ic_yes);
        voice.setImageResource(R.drawable.ic_camera);
        menu.setImageResource(R.drawable.ic_room_btn);
        roomPager.setPagingEnable(false);
    }

    @Override
    public void addDeviceSuccess(String roomName, DeviceInfo deviceInfo) {
        Logg.i(LOG_TAG, "==addDeviceSuccess=deviceInfo.getDisplayName()=====" + deviceInfo.getDisplayName());
        Logg.d("addDeviceSuccess", roomName);
        for (Fragment roomFragment : fragmentList) {
            if (roomFragment instanceof ItemRoomFragment) {
                if (((ItemRoomFragment) roomFragment).getRoomName().equals(roomName)) {
                    ((ItemRoomFragment) roomFragment).notifyFragmentData(deviceInfo);
                    break;
                }
            }
            if (roomFragment instanceof MyHomeRoomFragment) {
                if (((MyHomeRoomFragment) roomFragment).getRoomName().equals(roomName)) {
                    ((MyHomeRoomFragment) roomFragment).notifyFragmentData(deviceInfo);
                    break;
                }
            }
        }
    }
    @Override
    public void deleteSuccess(String roomName) {
        for (Fragment roomFragment : fragmentList) {
            if (roomFragment instanceof ItemRoomFragment) {
                if (((ItemRoomFragment) roomFragment).getRoomName().equals(roomName)) {
                    ((ItemRoomFragment) roomFragment).removeDevice();
                    break;
                }
            }
            if (roomFragment instanceof MyHomeRoomFragment) {
                if (((MyHomeRoomFragment) roomFragment).getRoomName().equals(roomName)) {
                    ((MyHomeRoomFragment) roomFragment).deleteDevice();
                    break;
                }
            }
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        Logg.i(LOG_TAG, "==onPageScrolled=====");

    }

    @Override
    public void onPageSelected(int position) {
        Logg.i(LOG_TAG, "==onPageSelected==position===" + position);
        o = adapter.instantiateItem(roomPager, roomPager.getCurrentItem());
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        Logg.i(LOG_TAG, "==onPageScrollStateChanged==state===" + state);
    }

    @Override
    public void addRoom(final String roomName) {

        mRoomName = roomName;
        if (Const.isRemote) {
            initIotMqttMessage();

            MqttDesiredJStrBuilder builder = new MqttDesiredJStrBuilder(Const.subscriptionTopic);
            builder.setJsonString(  CloudIotData.editNodeInfo("",roomName,"","","") );
            AskeyIoTService.getInstance(ZwaveClientApplication.getInstance()).publishDesiredMessage(HomeActivity.shadowTopic, builder);

        } else {
            MQTTManagement.getSingInstance().rigister(mqttMessageArrived);
//            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, "mobile_zwave:addRoom:" + roomName);
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.editNodeInfo("",roomName,"","","") );

        }


    }

    @Override
    public void deleteRoom(int position) {

    }

    private void initIotMqttMessage() {
       //以下这句为注册监听
        AskeyIoTService.getInstance(getContext()).setShadowReceiverListener(new ShadowReceiveListener() {
            @Override
            public void receiveShadowDocument(String s, String s1, String s2) {
                Logg.i(LOG_TAG, "======setShadowReceiverListener==s=" + s);
                Logg.i(LOG_TAG, "======setShadowReceiverListener==s1=" + s1);
                Logg.i(LOG_TAG, "======setShadowReceiverListener==s2=" + s2);
                IotMqttManagement.getInstance().receiveMqttMessage(s,s1,s2);
                if(s2.contains("desired")){
                    return;
                }
                mqttMessageResult(s2);

            }
        });
    }

    MqttMessageArrived mqttMessageArrived = new MqttMessageArrived() {
        @Override
        public void mqttMessageArrived(String topic, MqttMessage message) {
            mqttResult = new String(message.getPayload());
            Logg.i(LOG_TAG, "=mqttMessageArrived=>=topic=" + topic);
            Logg.i(LOG_TAG, "=mqttMessageArrived=>=message=" + mqttResult);

            //contains里面的参数是什么意思？
            mqttMessageResult(mqttResult);

        }
    };

    private void mqttMessageResult(final String mqttResult) {
        if(mqttResult.contains("editNodeInfo")){
            ((Activity) getContext()).runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    final JSONObject jsonObject;
                    try {
                        jsonObject = new JSONObject(mqttResult);
                        String reported = jsonObject.optString("reported");
                        JSONObject reportedObject = new JSONObject(reported);
                        String result = reportedObject.optString("Result");
                        if(result.equals("true")){
                            Log.d("addRoom","111111111");
                            fragmentList.add(ItemRoomFragment.newInstance(3, mRoomName));
                            adapter.notifyDataSetChanged();

                            RoomInfo info = new RoomInfo();
                            info.setRoomId(3);
                            info.setRoomName(mRoomName);
                            roomInfoList.add(info);
                        }else{
                            ((Activity)  getContext()).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText( getContext(),"Add Room Fail ! ",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
    public void register(){
        Log.d(LOG_TAG,"register");
        if (o instanceof MyHomeRoomFragment) {
            ((MyHomeRoomFragment)o).setUserVisibleHint(true);
        }
        if (o instanceof ItemRoomFragment) {
            ((ItemRoomFragment)o).setUserVisibleHint(true);
        }
    }
    public void unRegister(){
        Log.d(LOG_TAG,"unregister");
        MQTTManagement.getSingInstance().clearMessageArrived();
        unrigister();
    }

    private void unrigister() {
        if(mqttMessageArrived!=null){
            MQTTManagement.getSingInstance().unrigister(mqttMessageArrived);
        }
    }
    public static void getRoomList() {
        roomInfoList = new ArrayList<>();

        RoomInfo info = new RoomInfo();
        info.setRoomId(1);
        info.setRoomName("My Home");
        roomInfoList.add(info);

        RoomInfo info1 = new RoomInfo();
        info1.setRoomId(2);
        info1.setRoomName("Living Room");
        roomInfoList.add(info1);

        RoomInfo info2 = new RoomInfo();
        info2.setRoomId(3);
        info2.setRoomName("Kitchen Room");
//        info2.setRoomName("Bedroom");
        roomInfoList.add(info2);
    }

}


