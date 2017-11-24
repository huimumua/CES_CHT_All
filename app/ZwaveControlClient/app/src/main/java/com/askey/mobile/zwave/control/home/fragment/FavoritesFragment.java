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
import com.askey.mobile.zwave.control.deviceContr.dao.DeviceDao;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.IotMqttManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.IotMqttMessageCallback;
import com.askey.mobile.zwave.control.deviceContr.model.DeviceInfo;
import com.askey.mobile.zwave.control.home.activity.FavoriteEditActivity;
import com.askey.mobile.zwave.control.home.activity.HomeActivity;
import com.askey.mobile.zwave.control.home.adapter.FavoriteAdapter;
import com.askey.mobile.zwave.control.home.adapter.RecentlyAdapter;
import com.askey.mobile.zwave.control.util.ImageUtils;
import com.askey.mobile.zwave.control.util.Logg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavoritesFragment extends Fragment implements View.OnClickListener, FavoriteAdapter.OnItemClickListener, RecentlyAdapter.OnItemClickListener
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

       /* if(Const.isRemote){
            initIotMqttMessage();
            if(HomeActivity.shadowTopic!=null && !HomeActivity.shadowTopic.equals("")){
//                MqttService mqttService = MqttService.getInstance();
//                mqttService.publishMqttMessage(HomeActivity.shadowTopic,  LocalMqttData.getDeviceListCommand("ALL") );

                MqttDesiredJStrBuilder builder = new MqttDesiredJStrBuilder(Const.subscriptionTopic);
                builder.setJsonString(  CloudIotData.getDeviceListCommand("ALL") );
                AskeyIoTService.getInstance(ZwaveClientApplication.getInstance()).publishDesiredMessage(HomeActivity.shadowTopic, builder);

            }
        }else{
            MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
            //获取灯泡状态
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getDeviceListCommand("ALL"));

            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getRecentDeviceList());
        }*/


        return view;
    }


/*    MqttMessageArrived mMqttMessageArrived = new MqttMessageArrived() {
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
    };*/

    private void initIotMqttMessage() {

        IotMqttManagement.getInstance().setIotMqttMessageCallback(new IotMqttMessageCallback() {
            @Override
            public void receiveMqttMessage(String s, String s1, String s2) {
                //处理结果
                Logg.i(TAG, "==IotMqttMessageCallback======s=" + s);
                Logg.i(TAG, "==IotMqttMessageCallback======s1=" + s1);
                Logg.i(TAG, "==IotMqttMessageCallback======s2=" + s2);
                if(s2.contains("desired")){
                    return;
                }
                mqttMessageResult(s2);
            }

        });

/*        //以下这句为注册监听
        AskeyIoTService.getInstance(getContext()).setShadowReceiverListener(new ShadowReceiveListener() {
            @Override
            public void receiveShadowDocument(String s, String s1, String s2) {
                Logg.i(TAG, "======setShadowReceiverListener==s=" + s);
                Logg.i(TAG, "======setShadowReceiverListener==s1=" + s1);
                Logg.i(TAG, "======setShadowReceiverListener==s2=" + s2);
                IotMqttManagement.getInstance().receiveMqttMessage(s,s1,s2);

                mqttMessageResult(s2);
            }
        });*/
    }

    private void mqttMessageResult(String s2) {


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
        GridLayoutManager layoutManager_f = new GridLayoutManager(getActivity(), 3);
        GridLayoutManager layoutManager_r = new GridLayoutManager(getActivity(), 3);
//        LinearLayoutManager layoutManager_f = new LinearLayoutManager(getActivity());
//        LinearLayoutManager layoutManager_r = new LinearLayoutManager(getActivity());
//        layoutManager_f.setOrientation(LinearLayoutManager.HORIZONTAL);
//        layoutManager_r.setOrientation(LinearLayoutManager.HORIZONTAL);
        favorites_recycler.setLayoutManager(layoutManager_f);
        recently_recycler.setLayoutManager(layoutManager_r);
    }

    private void initData() {
        deviceInfoList = new ArrayList<>();
//        deviceInfoList = DeviceDao.getAllDeviceInfo();

        favoriteDeviceList = new ArrayList<>();
        for (DeviceInfo deviceInfo : deviceInfoList) {
            if ("1".equals(deviceInfo.getIsFavorite())) {
                favoriteDeviceList.add(deviceInfo);
            }
        }
        favoriteAdapter = new FavoriteAdapter(favoriteDeviceList);
        favoriteAdapter.setOnItemClickListener(this);
        favorites_recycler.setAdapter(favoriteAdapter);

        recentlyDeviceList = new ArrayList<>();
        recentlyDeviceList = sortDeviceByUseTime(deviceInfoList);
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
    }

    @Override
    public void onStop() {
        super.onStop();
        Logg.i(TAG,"===onStop=====");
        unrigister();
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
/*       if(mMqttMessageArrived!=null){
            MQTTManagement.getSingInstance().unrigister(mMqttMessageArrived);
        }*/
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
        Collections.sort(list);
        return list;
    }

    @Override
    public void onItemClick(View view, DeviceInfo deviceInfo) {
        Log.d("click", deviceInfo.toString());
    }

    @Override
    public void addFavoriteClick(int position) {

    }

    @Override
    public void removeFavoriteClick(int position) {

    }

    @Override
    public void editFavoriteClick(List<DeviceInfo> list) {
        deviceInfoList.clear();
        deviceInfoList.addAll(list);
        favoriteDeviceList.clear();
        for (DeviceInfo deviceInfo : list) {
            if ("1".equals(deviceInfo.getIsFavorite())) {
               favoriteDeviceList.add(deviceInfo);
            }
        }
        favoriteAdapter.notifyDataSetChanged();
    }
    public void register(){

    }
    public void unRegister(){

    }
}
