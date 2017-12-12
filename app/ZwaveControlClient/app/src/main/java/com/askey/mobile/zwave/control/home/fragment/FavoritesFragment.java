package com.askey.mobile.zwave.control.home.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.application.ZwaveClientApplication;
import com.askey.mobile.zwave.control.base.BaseFragment;
import com.askey.mobile.zwave.control.data.CloudIotData;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.IotMqttManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.model.DeviceInfo;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.BulbActivity;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.ExtenderDeviceActivity;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.PlugActivity;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.WallMoteLivingActivity;
import com.askey.mobile.zwave.control.home.activity.FavoriteEditActivity;
import com.askey.mobile.zwave.control.home.activity.HomeActivity;
import com.askey.mobile.zwave.control.home.adapter.FavoriteAdapter;
import com.askey.mobile.zwave.control.home.adapter.RecentlyAdapter;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.ImageUtils;
import com.askey.mobile.zwave.control.util.Logg;
import com.askeycloud.webservice.sdk.iot.callback.ShadowReceiveListener;
import com.askeycloud.webservice.sdk.iot.message.builder.MqttDesiredJStrBuilder;
import com.askeycloud.webservice.sdk.service.iot.AskeyIoTService;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavoritesFragment extends BaseFragment implements View.OnClickListener, FavoriteAdapter.OnItemClickListener, RecentlyAdapter.OnItemClickListener
        , FavoriteEditActivity.EditFavoriteListener {
    public static String TAG = "FavoritesFragment";
    private ImageView menu, edit, voice;
    private RecyclerView favorites_recycler, recently_recycler;
    private List<DeviceInfo> deviceInfoList, favoriteDeviceList, recentlyDeviceList;
    private FavoriteAdapter favoriteAdapter;
    private RecentlyAdapter recentlyAdapter;

    public static FavoritesFragment newInstance() {
        return new FavoritesFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Logg.i(TAG, "==onAttach=======");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logg.i(TAG, "==onCreate=======");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Logg.i(TAG,"===onCreateView=====");
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        initView(view);
        initData();

        showWaitingDialog();
        if(Const.isRemote){
            initIotMqttMessage();
            Logg.i(TAG,"===Const.isRemote====="+Const.isRemote);
            if(HomeActivity.shadowTopic!=null && !HomeActivity.shadowTopic.equals("")){
                MqttDesiredJStrBuilder builder = new MqttDesiredJStrBuilder(Const.subscriptionTopic);
                builder.setJsonString(  CloudIotData.getDeviceListCommand("ALL") );
                Logg.i(TAG,"===setJsonString====="+CloudIotData.getFavoriteList());
//                builder.setJsonString(  CloudIotData.getDeviceListCommand("My Home") );
                AskeyIoTService.getInstance(ZwaveClientApplication.getInstance()).publishDesiredMessage(HomeActivity.shadowTopic, builder);

            }
        }else{
            //获取灯泡状态
            Logg.i(TAG,"===publishMessage_local=====");
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getDeviceListCommand("ALL"));
        }
        return view;
    }


    MqttMessageArrived mMqttMessageArrived = new MqttMessageArrived() {
        @Override
        public void mqttMessageArrived(String topic, MqttMessage message) {
            String result = new String(message.getPayload());
            Logg.i(TAG,"=mqttMessageArrived=>=topic="+topic);
            Logg.i(TAG,"=mqttMessageArrived=>=message="+result);
            //这里边可以获取全部设备列表，从中检索出喜欢的和最近使用的设备
            if(result.contains("desired")){
                return;
            }
            mqttMessageResult(result);

        }
    };

    private void initIotMqttMessage() {
        //以下这句为注册监听
        AskeyIoTService.getInstance(getContext()).setShadowReceiverListener(new ShadowReceiveListener() {
            @Override
            public void receiveShadowDocument(String s, String s1, String s2) {
                Logg.i(TAG, "======setShadowReceiverListener==s=" + s);
                Logg.i(TAG, "======setShadowReceiverListener==s1=" + s1);
                Logg.i(TAG, "======setShadowReceiverListener==s2=" + s2);
                IotMqttManagement.getInstance().receiveMqttMessage(s,s1,s2);

                mqttMessageResult(s2);
            }
        });
    }

    private void mqttMessageResult(String mqttResult) {
        final JSONObject jsonObject;
//        {"reported":{"Interface":"getDeviceList","deviceList":[
//                {"brand":"","nodeId":"1","deviceType":"","name":"1","Room":""},
//            {"brand":"","nodeId":"2","deviceType":"Zwave","name":"ColorBulb","category":"BULB","Room":"Living Room","isFavorite":"1","timestamp":1511515706101},
//            {"brand":"","nodeId":"4","deviceType":"Zwave","name":"plug","category":"PLUG","Room":"My Home","isFavorite":"0","timestamp":1511517666527},
//            {"brand":"","nodeId":"5","deviceType":"","name":"5","Room":""}]}}

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
                    favoriteDeviceList.clear();
                    for(int i=0;i<size;i++){
                        JSONObject info=columnInfo.getJSONObject(i);
                        String nodeId = info.getString("nodeId");
                        String brand = info.getString("brand");
                        String devType = info.getString("deviceType");
                        String category = info.getString("category");
                        String Room = info.getString("Room");
                        String isFavorite = info.getString("isFavorite");
                        String name = info.getString("name");
                        Logg.i(TAG,"==getDeviceResult=JSONArray===devName=="+name);
                        DeviceInfo deviceInfo = new DeviceInfo();
                        deviceInfo.setDeviceId(nodeId);
                        deviceInfo.setDisplayName(name);
                        deviceInfo.setDeviceType(category);
                        deviceInfo.setRooms(Room);
                        deviceInfo.setIsFavorite(isFavorite);
                        deviceInfoList.add(deviceInfo);
                        if ("1".equals(isFavorite)) {
                            favoriteDeviceList.add(deviceInfo);
                        }

//                        String nodeTopic = Const.subscriptionTopic+"Zwave" + nodeId;
//                        // 订阅新设备的topic为 sn + nodeId
//                        MQTTManagement.getSingInstance().subscribeToTopic(nodeTopic, null);
                    }
                }

                ((Activity) getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopWaitDialog();
                        favoriteAdapter.notifyDataSetChanged();
                    }
                });

                if(Const.isRemote){
                    initIotMqttMessage();
                    if(HomeActivity.shadowTopic!=null && !HomeActivity.shadowTopic.equals("")){
                        MqttDesiredJStrBuilder builder = new MqttDesiredJStrBuilder(Const.subscriptionTopic);
                        builder.setJsonString( CloudIotData.getRecentDeviceList() );
                        AskeyIoTService.getInstance(ZwaveClientApplication.getInstance()).publishDesiredMessage(HomeActivity.shadowTopic, builder);
                    }
                }else{
                    //获取最近使用设备
                    MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getRecentDeviceList());
                }

            }else if(Interface.equals("getRecentDeviceList")){

                String DeviceList = reportedObject.optString("deviceList");
                JSONArray columnInfo = new JSONArray(DeviceList);
                int size = columnInfo.length();
                if(size > 0){
                    recentlyDeviceList.clear();
                    for(int i=0;i<size;i++){
                        JSONObject info=columnInfo.getJSONObject(i);
                        String nodeId = info.getString("nodeId");
                        String brand = info.getString("brand");
                        String devType = info.getString("deviceType");
                        String category = info.getString("category");
                        String Room = info.getString("Room");
                        String isFavorite = info.getString("isFavorite");
                        String name = info.getString("name");
                        Logg.i(TAG,"==getDeviceResult=JSONArray===devName=="+name);
                        DeviceInfo deviceInfo = new DeviceInfo();
                        deviceInfo.setDeviceId(nodeId);
                        deviceInfo.setDisplayName(name);
                        deviceInfo.setDeviceType(category);
                        deviceInfo.setRooms(Room);
                        deviceInfo.setIsFavorite(isFavorite);
                        recentlyDeviceList.add(deviceInfo);

//                        String nodeTopic = Const.subscriptionTopic+"Zwave" + nodeId;
//                        // 订阅新设备的topic为 sn + nodeId
//                        MQTTManagement.getSingInstance().subscribeToTopic(nodeTopic, null);
                    }
                }

                ((Activity) getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopWaitDialog();
                        recentlyAdapter.notifyDataSetChanged();
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void initView(View view) {
        menu = (ImageView) view.findViewById(R.id.menu_btn);
        edit = (ImageView) view.findViewById(R.id.edit);
        voice = (ImageView) view.findViewById(R.id.voice);
        menu.setOnClickListener(this);
        edit.setOnClickListener(this);
        voice.setOnClickListener(this);
        FavoriteEditActivity.setEditFavoriteListener(this);

        favorites_recycler = (RecyclerView) view.findViewById(R.id.favorites);
        recently_recycler = (RecyclerView) view.findViewById(R.id.recently);
        //取消recyclerview的滑动，使Scrollview惯性滑动正常
        GridLayoutManager layoutManager_f = new GridLayoutManager(getActivity(), 3){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        GridLayoutManager layoutManager_r = new GridLayoutManager(getActivity(), 3){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        favorites_recycler.setLayoutManager(layoutManager_f);
        recently_recycler.setLayoutManager(layoutManager_r);
    }

    private void initData() {
        deviceInfoList = new ArrayList<>();
//        deviceInfoList = DeviceDao.getAllDeviceInfo();
        favoriteDeviceList = new ArrayList<>();
//        for (DeviceInfo deviceInfo : deviceInfoList) {
//            if ("1".equals(deviceInfo.getIsFavorite())) {
//                favoriteDeviceList.add(deviceInfo);
//            }
//        }
        favoriteAdapter = new FavoriteAdapter(favoriteDeviceList);
        favoriteAdapter.setOnItemClickListener(this);
        favorites_recycler.setAdapter(favoriteAdapter);

        recentlyDeviceList = new ArrayList<>();
//        recentlyDeviceList = sortDeviceByUseTime(deviceInfoList);
        recentlyAdapter = new RecentlyAdapter(recentlyDeviceList);
        recentlyAdapter.setOnItemClickListener(this);
        recently_recycler.setAdapter(recentlyAdapter);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logg.i(TAG,"===onActivityCreated=====");
    }

    @Override
    public void onStart() {
        super.onStart();
        Logg.i(TAG,"===onStart=====");
    }

    @Override
    public void onResume() {
        super.onResume();
        Logg.i(TAG,"===onResume=====");
    }

    @Override
    public void onPause() {
        super.onPause();
        Logg.i(TAG,"===onPause=====");
        unrigister();
    }

    @Override
    public void onStop() {
        super.onStop();
        Logg.i(TAG,"===onStop=====");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Logg.i(TAG,"===onDestroyView=====");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logg.i(TAG,"===onDestroy=====");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Logg.i(TAG,"===onDetach=====");
    }

    private void unrigister() {
        if(mMqttMessageArrived!=null){
            MQTTManagement.getSingInstance().unrigister(mMqttMessageArrived);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu_btn:
                Activity activity = getActivity();
                if (activity instanceof HomeActivity) {
                    ((HomeActivity) activity).toggleDrawerLayout();
                }
                break;
            case R.id.edit:

                //截屏当前activity并进行高斯模糊
                Bitmap bitmap = ImageUtils.fastBlur(ImageUtils.shortImage(getActivity()),0.125f,15);
                byte[] bytes = ImageUtils.Bitmap2Bytes(bitmap);

                Bundle bundle = new Bundle();
                Log.d("tag4",deviceInfoList.size()+"");
                bundle.putSerializable("data", (Serializable) deviceInfoList);
                bundle.putByteArray("bg",bytes);
                Intent intent = new Intent(getActivity(), FavoriteEditActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.voice:
                break;
        }
    }

    /**
     * DeviceInfo实现Comparable接口,按时间降序排列
     *
     * @param list
     * @return
     */
    private List<DeviceInfo> sortDeviceByUseTime(List<DeviceInfo> list) {
        Log.d("tag6",list.size()+"");
        Collections.sort(list);
        return list;
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
        }  else if ("EXTENDER".equals(deviceInfo.getDeviceType())) {
            intent = new Intent(getActivity(), ExtenderDeviceActivity.class);
        } else {
            intent = new Intent(getActivity(), BulbActivity.class);//原有的经验证过的可以正常使用
            intent.putExtra("uniqueid",deviceInfo.getUniqueId());
        }
        intent.putExtra("nodeId",deviceInfo.getDeviceId());
        intent.putExtra("type", deviceInfo.getDeviceType());
        intent.putExtra("room", deviceInfo.getRooms());
        intent.putExtra("displayName", deviceInfo.getDisplayName());
        startActivity(intent);

    }

    @Override
    public void addFavoriteClick(int position) {

    }

    @Override
    public void removeFavoriteClick(int position) {

    }

    @Override
    public void move2EditActivity() {
        Bitmap bitmap = ImageUtils.fastBlur(ImageUtils.shortImage(getActivity()),0.125f,15);
        byte[] bytes = ImageUtils.Bitmap2Bytes(bitmap);

        Bundle bundle = new Bundle();
        bundle.putSerializable("data", (Serializable) deviceInfoList);
        bundle.putByteArray("bg",bytes);
        Intent intent = new Intent(getActivity(), FavoriteEditActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void editFavoriteClick(List<DeviceInfo> list) {
        deviceInfoList.clear();
        deviceInfoList.addAll(list);
        Log.d("tag5",deviceInfoList.size()+"");
        favoriteDeviceList.clear();
        for (DeviceInfo deviceInfo : list) {
            if ("1".equals(deviceInfo.getIsFavorite())) {
                favoriteDeviceList.add(deviceInfo);
            }
        }
        favoriteAdapter.notifyDataSetChanged();
    }

    public void register(){
        if(!Const.isRemote){
            MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);

            if (Const.getIsDataChange()) {
                Const.setIsDataChange(false);
                Logg.i(TAG,"===publishMessage_register=====");
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getDeviceListCommand("ALL"));
            }
        }
    }

    public void unRegister(){
        unrigister();
    }


}
