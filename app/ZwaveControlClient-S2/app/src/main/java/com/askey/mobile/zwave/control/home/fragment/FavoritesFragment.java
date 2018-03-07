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
import com.askey.mobile.zwave.control.base.BaseFragment;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.model.DeviceInfo;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.BulbActivity;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.DimmerActivity;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.PlugActivity;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.SensorActivity;
import com.askey.mobile.zwave.control.deviceContr.rooms.ui.WallMoteLivingActivity;
import com.askey.mobile.zwave.control.home.activity.FavoriteEditActivity;
import com.askey.mobile.zwave.control.home.activity.HomeActivity;
import com.askey.mobile.zwave.control.home.adapter.FavoriteAdapter;
import com.askey.mobile.zwave.control.home.adapter.RecentlyAdapter;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.ImageUtils;
import com.askey.mobile.zwave.control.util.Logg;
import com.askey.mobile.zwave.control.util.ToastShow;
import com.askeycloud.sdk.device.response.IoTDeviceInfoResponse;

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
    private IoTDeviceInfoResponse ioTDeviceInfoResponse;
    private String shadowTopic = "";

    public static FavoritesFragment newInstance() {
        return new FavoritesFragment();
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
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        initView(view);
        initData();

        showWaitingDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getDeviceListCommand("ALL"));

            }
        }).start();

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);解决fragment重叠的问题
    }

    MqttMessageArrived mMqttMessageArrived = new MqttMessageArrived() {
        @Override
        public void mqttMessageArrived(String topic, MqttMessage message) {
            String result = new String(message.getPayload());
            Logg.i(TAG, "=mqttMessageArrived=>=message=" + result);
            //这里边可以获取全部设备列表，从中检索出喜欢的和最近使用的设备
            if (result.contains("desired")) {
                return;
            }
            mqttMessageResult(result);

        }
    };

    private void mqttMessageResult(String mqttResult) {
        final JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(mqttResult);
            String reported = jsonObject.optString("reported");
            JSONObject reportedObject = new JSONObject(reported);
            String Interface = reportedObject.optString("Interface");
            if (Interface.equals("getDeviceList")) {
                String deviceList = reportedObject.optString("deviceList");
                parsingGetDeviceList(deviceList);
            } else if (Interface.equals("getRecentDeviceList")) {
                stopWaitDialog();
                String DeviceList = reportedObject.optString("deviceList");
                JSONArray columnInfo = new JSONArray(DeviceList);
                int size = columnInfo.length();
                if (size > 0) {
                    recentlyDeviceList.clear();
                    for (int i = 0; i < size; i++) {
                        JSONObject info = columnInfo.getJSONObject(i);
                        String nodeId = info.getString("nodeId");
                        String brand = info.getString("brand");
                        String devType = info.getString("deviceType");
                        String category = info.getString("category");
                        String Room = info.getString("Room");
                        String isFavorite = info.getString("isFavorite");
                        String name = info.getString("name");
                        DeviceInfo deviceInfo = new DeviceInfo();
                        deviceInfo.setDeviceId(nodeId);
                        deviceInfo.setDisplayName(name);
                        deviceInfo.setDeviceType(category);
                        deviceInfo.setRooms(Room);
                        deviceInfo.setIsFavorite(isFavorite);
                        recentlyDeviceList.add(deviceInfo);
                    }
                }

                ((Activity) getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recentlyAdapter.notifyDataSetChanged();
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private void parsingGetDeviceList(String deviceList) {
        try {
            JSONArray columnInfo = new JSONArray(deviceList);
            int size = columnInfo.length();
            if (size > 0) {
                deviceInfoList.clear();
                favoriteDeviceList.clear();
                for (int i = 0; i < size; i++) {
                    JSONObject info = columnInfo.getJSONObject(i);
                    String nodeId = info.getString("nodeId");
                    String brand = info.getString("brand");
                    String devType = info.getString("deviceType");
                    String category = info.getString("category");
                    String Room = info.getString("Room");
                    String isFavorite = info.getString("isFavorite");
                    String name = info.getString("name");
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

                    if( !devType.equals("unknown") && !devType.equals("") && devType!=null){
                        iotSubscribeMqtt(deviceInfo,nodeId);
                    }
                }
            }

            ((Activity) getContext()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    favoriteAdapter.notifyDataSetChanged();
                }
            });

            getRecentDeviceListComm();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void iotSubscribeMqtt(DeviceInfo deviceInfo,String nodeId) {
        String nodeTopic = Const.subscriptionTopic + "Zwave" + nodeId;
        // 订阅新设备的topic为 sn + nodeId
        MQTTManagement.getSingInstance().subscribeToTopic(nodeTopic, null);
    }

    private void getRecentDeviceListComm() {
        //获取最近使用设备
        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getRecentDeviceList());
    }

    private void initView(View view) {
        menu = (ImageView) view.findViewById(R.id.menu_btn);
        edit = (ImageView) view.findViewById(R.id.edit);
        voice = (ImageView) view.findViewById(R.id.voice);
        voice.setVisibility(View.INVISIBLE);
        menu.setOnClickListener(this);
        edit.setOnClickListener(this);
        voice.setOnClickListener(this);
        FavoriteEditActivity.setEditFavoriteListener(this);

        favorites_recycler = (RecyclerView) view.findViewById(R.id.favorites);
        recently_recycler = (RecyclerView) view.findViewById(R.id.recently);
        //取消recyclerview的滑动，使Scrollview惯性滑动正常
        GridLayoutManager layoutManager_f = new GridLayoutManager(getActivity(), 3) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        GridLayoutManager layoutManager_r = new GridLayoutManager(getActivity(), 3) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        favorites_recycler.setLayoutManager(layoutManager_f);
        favorites_recycler.setNestedScrollingEnabled(false);
        recently_recycler.setLayoutManager(layoutManager_r);
        recently_recycler.setNestedScrollingEnabled(false);
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
        unrigister();
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

    private void unrigister() {
        if (mMqttMessageArrived != null) {
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
                Bitmap bitmap = ImageUtils.fastBlur(ImageUtils.shortImage(getActivity()), 0.125f, 15);
                byte[] bytes = ImageUtils.Bitmap2Bytes(bitmap);

                Bundle bundle = new Bundle();
                Log.d("tag4", deviceInfoList.size() + "");
                bundle.putSerializable("data", (Serializable) deviceInfoList);
                bundle.putByteArray("bg", bytes);
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
        Log.d("tag6", list.size() + "");
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
        } else if ("EXTENDER".equals(deviceInfo.getDeviceType())) {
            ToastShow.showToast(mContext,getResources().getString(R.string.extender_no_contrl));
            return;
        }else if ("DIMMER".equals(deviceInfo.getDeviceType())) {
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
        Bitmap bitmap = ImageUtils.fastBlur(ImageUtils.shortImage(getActivity()), 0.125f, 15);
        byte[] bytes = ImageUtils.Bitmap2Bytes(bitmap);

        Bundle bundle = new Bundle();
        bundle.putSerializable("data", (Serializable) deviceInfoList);
        bundle.putByteArray("bg", bytes);
        Intent intent = new Intent(getActivity(), FavoriteEditActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void editFavoriteClick(List<DeviceInfo> list) {
        deviceInfoList.clear();
        deviceInfoList.addAll(list);
        Log.d("tag5", deviceInfoList.size() + "");
        favoriteDeviceList.clear();
        for (DeviceInfo deviceInfo : list) {
            if ("1".equals(deviceInfo.getIsFavorite())) {
                favoriteDeviceList.add(deviceInfo);
            }
        }
        favoriteAdapter.notifyDataSetChanged();
    }

    public void register() {
            Logg.i(TAG,"===MQTTManagement===rigister=====");
            MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);

            if (Const.getIsDataChange()) {
                Const.setIsDataChange(false);
                showWaitingDialog();
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getDeviceListCommand("ALL"));
            }
    }

    public void unRegister() {
        unrigister();
    }


}
