package com.askey.mobile.zwave.control.home.fragment;


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
import android.widget.ImageView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseFragment;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.model.DeviceInfo;
import com.askey.mobile.zwave.control.deviceContr.model.ProvisionInfo;
import com.askey.mobile.zwave.control.deviceContr.scenes.DeviceTestActivity;
import com.askey.mobile.zwave.control.deviceContr.scenes.DeviceTestEditActivity;
import com.askey.mobile.zwave.control.home.activity.HomeActivity;
import com.askey.mobile.zwave.control.home.activity.addDevice.AddSmartStartActivity;
import com.askey.mobile.zwave.control.home.adapter.RecentlyAdapter;
import com.askey.mobile.zwave.control.home.adapter.ScenesAdapter;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScenesFragment extends BaseFragment implements View.OnClickListener, ScenesAdapter.OnItemClickListener {
    private final String TAG = ScenesFragment.class.getSimpleName();
    private ImageView menu, voice, edit;
    private List<ProvisionInfo> dataList;
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
        dataList = new ArrayList<>();
        adapter = new ScenesAdapter(dataList);
        adapter.setOnItemClickListener(this);
        scene_recycler.setAdapter(adapter);
        requestProvisionList();
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
            mqttMessageResult(result);

        }
    };

    private void requestProvisionList() {
        showWaitingDialog();
        Log.d(TAG, "======requestProvisionList=======");
        MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getAllProvisionListEntry());
    }

    private void mqttMessageResult(String mqttResult) {
        ((Activity) getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopWaitDialog();
            }
        });
        JSONObject jsonObject = null;
        try {
//            {
//                "reported": {
//                "MessageType": "All Provision List Report",
//                        "Detial provision list": [{
//                    "DSK": "51525-35455-41424-34445-31323-33435-21222-32425",
//                            "Device type info": {
//                        "Generic Cls": 16,
//                                "Specific Cls": 1,
//                                "Icon Type": 1792
//                    },
//                    "Device id info": {
//                        "Manufacturer Id": 0,
//                                "Product Type": 3,
//                                "Product Id": 2,
//                                "App Version": 4,
//                                "App Sub Ver": 1
//                    },
//                    "Network inclusion state": {
//                        "Node Id": 0,
//                                "Status": "Not Inclusion"
//                    },
//                    "Device Boot Mode": "Smart Start",
//                            "Device Inclusion state": "State Pending",
//                            "Device Location": "complany",
//                            "Device Name": "skysoft"
//                }]
//            }
//            }
            jsonObject = new JSONObject(mqttResult);
            String reported = jsonObject.optString("reported");
            JSONObject reportedObject = new JSONObject(reported);
            String Interface = reportedObject.optString("MessageType");
         if(Interface.equals("All Provision List Report")){
             ProvisionInfo provisionInfo;
JSONArray provisionList = reportedObject.optJSONArray("Detial provision list");
             for (int i = 0;i < provisionList.length();i++) {
                 JSONObject temp = provisionList.getJSONObject(i);
                 String deviceType = temp.getString("Device type info");
                 String deviceId = temp.getString("Device id info");
                 String networkState = temp.getString("Network inclusion state");

                 JSONObject deviceTypeObject = new JSONObject(deviceType);
                 String  genericCls = deviceTypeObject.getString("Generic Cls");
                 String  specificCls = deviceTypeObject.getString("Specific Cls");
                 String  iconType = deviceTypeObject.getString("Icon Type");

                 JSONObject deviceIdObject = new JSONObject(deviceId);
                 String  manufacturerId = deviceIdObject.getString("Manufacturer Id");
                 String  productType = deviceIdObject.getString("Product Type");
                 String  productId = deviceIdObject.getString("Product Id");
                 String  appVersion = deviceIdObject.getString("App Version");
                 String  appSubVer = deviceIdObject.getString("App Sub Ver");

                 JSONObject networkStateObject = new JSONObject(networkState);
                 String  nodeId = networkStateObject.getString("Node Id");
                 String  status = networkStateObject.getString("Status");

                 provisionInfo = new ProvisionInfo();
                 provisionInfo.setDsk(temp.getString("DSK"));
                 provisionInfo.setDeviceName(temp.getString("Device Name"));
                 provisionInfo.setDeviceBootMode(temp.getString("Device Boot Mode"));
                 provisionInfo.setDeviceLocation(temp.getString("Device Location"));
                 provisionInfo.setDeviceInclusionState(temp.getString("Device Inclusion state"));
                 provisionInfo.setNodeId(nodeId);
                 provisionInfo.setStatus(status);

                 dataList.add(provisionInfo);
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
    public void onItemClick(View view, ProvisionInfo provisionInfo) {
        Intent intent = new Intent(getActivity(), DeviceTestActivity.class);
        intent.putExtra("provisionInfo", provisionInfo);
        startActivity(intent);

    }

    @Override
    public void onItemLongClick(View view, ProvisionInfo provisionInfo) {
        Intent intent = new Intent(getActivity(), DeviceTestEditActivity.class);
        intent.putExtra("provisionInfo", provisionInfo);
        startActivity(intent);
    }

    @Override
    public void addItemClick() {
        Intent intent = new Intent(getActivity(), AddSmartStartActivity.class);
        startActivity(intent);
    }


    public void register() {
        if (!Const.isRemote) {
            MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);

            if (Const.getIsDataChange()) {
                Const.setIsDataChange(false);
                Logg.i(TAG, "===publishMessage_register=====");
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getAllProvisionListEntry());
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
