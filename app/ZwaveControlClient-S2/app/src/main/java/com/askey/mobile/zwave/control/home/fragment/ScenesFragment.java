package com.askey.mobile.zwave.control.home.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseFragment;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.model.ScenesInfo;
import com.askey.mobile.zwave.control.deviceContr.scenes.NewScenceActivity;
import com.askey.mobile.zwave.control.home.activity.HomeActivity;
import com.askey.mobile.zwave.control.home.adapter.ScenesAdapter;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScenesFragment extends BaseFragment implements View.OnClickListener, ScenesAdapter.OnItemClickListener {
    private final String TAG = ScenesFragment.class.getSimpleName();
    private ImageView menu, voice, edit;
    private List<ScenesInfo> dataList;
    private RecyclerView scene_recycler;
    private ScenesAdapter adapter;

    public static ScenesFragment newInstance() {
        return new ScenesFragment();
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
        View view = inflater.inflate(R.layout.fragment_scenes, container, false);
        initView(view);
        initData();
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);解决fragment重叠的问题
    }

    private void initView(View view) {
        menu = (ImageView) view.findViewById(R.id.menu_btn);
        menu.setOnClickListener(this);

        scene_recycler = (RecyclerView) view.findViewById(R.id.scene_recycler);
        scene_recycler.setLayoutManager(new GridLayoutManager(getActivity(), 3));

    }

    MqttMessageArrived mMqttMessageArrived = new MqttMessageArrived() {
        @Override
        public void mqttMessageArrived(String topic, MqttMessage message) {
            final String result = new String(message.getPayload());
            Logg.i(TAG, "=mqttMessageArrived=>=topic=" + topic);
            Logg.i(TAG, "=mqttMessageArrived=>=message=" + result);

            if (result.contains("desired")) {
                return;
            }
//            mqttMessageResult(result);

        }
    };

//    private void mqttMessageResult(String mqttResult) {
//        JSONObject jsonObject = null;
//        try {
//            jsonObject = new JSONObject(mqttResult);
//            String reported = jsonObject.optString("reported");
//            JSONObject reportedObject = new JSONObject(reported);
//            String Interface = reportedObject.optString("Interface");
//         if(Interface.equals("getSceneList")){
//
//            JSONArray scenes = reportedObject.optJSONArray("scene");
//             for (int i = 0;i < scenes.length();i++) {
//                 JSONArray conditions = reportedObject.optJSONArray("condition");
//                 for (int j = 0;j < conditions.length();j++) {
//                     ScenesInfo scenesInfo = new ScenesInfo();
//                     scenesInfo.setScenesName(scenes.getJSONObject(i).getString("sceneName"));
//                     scenesInfo.setIconName(scenes.getJSONObject(i).getString("iconName"));
//                     scenesInfo.setCurrentStatus(scenes.getJSONObject(i).getString("iconName"));
//                 }
//             }
//             JSONArray conditon =
//            JSONArray columnInfo = new JSONArray(DeviceList);
//            int size = columnInfo.length();
//            if(size > 0){
//                recentlyDeviceList.clear();
//                for(int i=0;i<size;i++){
//                    JSONObject info=columnInfo.getJSONObject(i);
//                    String nodeId = info.getString("nodeId");
//                    String brand = info.getString("brand");
//                    String devType = info.getString("deviceType");
//                    String category = info.getString("category");
//                    String Room = info.getString("Room");
//                    String isFavorite = info.getString("isFavorite");
//                    String name = info.getString("name");
//                    Logg.i(TAG,"==getDeviceResult=JSONArray===devName=="+name);
//                    DeviceInfo deviceInfo = new DeviceInfo();
//                    deviceInfo.setDeviceId(nodeId);
//                    deviceInfo.setDisplayName(name);
//                    deviceInfo.setDeviceType(category);
//                    deviceInfo.setRooms(Room);
//                    deviceInfo.setIsFavorite(isFavorite);
//                    recentlyDeviceList.add(deviceInfo);
//
////                        String nodeTopic = Const.subscriptionTopic+"Zwave" + nodeId;
////                        // 订阅新设备的topic为 sn + nodeId
////                        MQTTManagement.getSingInstance().subscribeToTopic(nodeTopic, null);
//                }
//            }
//
//            ((Activity) getContext()).runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    stopWaitDialog();
//                    recentlyAdapter.notifyDataSetChanged();
//                }
//            });
//        }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//    }

    private void initData() {
        dataList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ScenesInfo scenesInfo = new ScenesInfo();
            scenesInfo.setNodeId(String.valueOf(i + 1));
            scenesInfo.setScenesName("SceneTest");

            dataList.add(scenesInfo);
        }


        //获取灯泡状态
        Logg.i(TAG, "===publishMessage_local=====");
        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getScene());

        adapter = new ScenesAdapter(dataList);
        adapter.setOnItemClickListener(this);
        scene_recycler.setAdapter(adapter);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu_btn:
                Activity activity = getActivity();
                if (activity instanceof HomeActivity) {
                    ((HomeActivity) activity).toggleDrawerLayout();
                }
                break;
        }
    }

    @Override
    public void onItemClick(View view, ScenesInfo scenesInfo) {
        Toast.makeText(getActivity(), "item click", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onItemLongClick(View view, ScenesInfo scenesInfo) {
        Intent intent = new Intent(getActivity(), NewScenceActivity.class);
        startActivity(intent);
    }

    @Override
    public void addItemClick() {
        Intent intent = new Intent(getActivity(), NewScenceActivity.class);
        startActivity(intent);
    }


    public void register() {
        if (!Const.isRemote) {
            MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);

            if (Const.getIsDataChange()) {
                Const.setIsDataChange(false);
                Logg.i(TAG, "===publishMessage_register=====");
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getScene());
            }
        }
    }

    public void unRegister() {
        unrigister();
    }

    private void unrigister() {
        if (mMqttMessageArrived != null) {
            MQTTManagement.getSingInstance().unrigister(mMqttMessageArrived);
        }
    }
}
