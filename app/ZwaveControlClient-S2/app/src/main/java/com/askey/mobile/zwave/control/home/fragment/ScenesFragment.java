package com.askey.mobile.zwave.control.home.fragment;


import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseFragment;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.model.DeviceInfo;
import com.askey.mobile.zwave.control.deviceContr.model.ProvisionInfo;
import com.askey.mobile.zwave.control.deviceContr.model.ScenesInfo;
import com.askey.mobile.zwave.control.deviceContr.net.SocketTransceiver;
import com.askey.mobile.zwave.control.deviceContr.net.TCPReceive;
import com.askey.mobile.zwave.control.deviceContr.net.TcpClient;
import com.askey.mobile.zwave.control.deviceContr.scenes.DeviceTestActivity;
import com.askey.mobile.zwave.control.deviceContr.scenes.DeviceTestEditActivity;
import com.askey.mobile.zwave.control.home.activity.HomeActivity;
import com.askey.mobile.zwave.control.home.activity.addDevice.AddSmartStartActivity;
import com.askey.mobile.zwave.control.home.activity.addDevice.DeleteDeviceActivity;
import com.askey.mobile.zwave.control.home.adapter.DeviceAdapter;
import com.askey.mobile.zwave.control.home.adapter.RecentlyAdapter;
import com.askey.mobile.zwave.control.home.adapter.ScenesAdapter;
import com.askey.mobile.zwave.control.interf.NetworkRole;
import com.askey.mobile.zwave.control.interf.NetworkRoleCallback;
import com.askey.mobile.zwave.control.qrcode.CaptureActivity;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;
import com.askey.mobile.zwave.control.util.ToastShow;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScenesFragment extends BaseFragment implements View.OnClickListener, ScenesAdapter.OnItemClickListener {
    private static boolean ADD_SUCCESS = false;
    private final String TAG = ScenesFragment.class.getSimpleName();
    private ImageView menu, voice, edit, firstAddImageView;
    private List<ProvisionInfo> dataList;
    private RecyclerView scene_recycler;
    private ScenesAdapter adapter;
    private int clickPosition;
    private TextView networkRole;
    private boolean isFirstLoad = true;
    private boolean isVisibleToUser;
    private boolean isFirst2onResume = true;

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
        adapter.setOnItemClickListener(this);

        if (Const.RESET_PROVISION) {
            Const.RESET_PROVISION = false;
            networkRole.setText(Const.NETWORK_ROLE);
            if (dataList.size() > 0) {
                dataList.clear();
                adapter.notifyDataSetChanged();
            }
        }

//        ProvisionInfo provisionInfo;
//        DeviceInfo deviceInfo;
////        for (int i = 0; i < 7; i++) {
////            provisionInfo = new ProvisionInfo();
////            provisionInfo.setDsk("45106");
////            provisionInfo.setDeviceName("dog");
////            provisionInfo.setNodeId(String.valueOf(i));
////            dataList.add(provisionInfo);
//////            deviceInfo = new DeviceInfo();
//////            deviceInfo.setDeviceId(String.valueOf(i));
//////            deviceInfoList.add(deviceInfo);
////        }
        if(dataList.size() == 0){
            setFirstAddImageView(true);
        } else {
            setFirstAddImageView(false);
        }

        return view;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);解决fragment重叠的问题
    }

    private void initView(View view) {
        menu = (ImageView) view.findViewById(R.id.menu_btn);
        menu.setOnClickListener(this);
        menu.setVisibility(View.VISIBLE);

        voice = (ImageView) view.findViewById(R.id.voice);
        voice.setVisibility(View.GONE);

        edit = (ImageView) view.findViewById(R.id.edit);
        edit.setOnClickListener(this);
        edit.setVisibility(View.GONE);

        firstAddImageView =(ImageView) view.findViewById(R.id.dsk_add_first_device);
        firstAddImageView.setOnClickListener(this);

        scene_recycler = (RecyclerView) view.findViewById(R.id.scene_recycler);
        scene_recycler.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        networkRole = (TextView) view.findViewById(R.id.network_role);

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
        ADD_SUCCESS = true;
        Log.d(TAG, "======requestProvisionList=======");
//        MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
//        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getAllProvisionListEntry());
    }

    private void mqttMessageResult(String mqttResult) {
        dataList.clear();
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
//            {"reported":{"MessageType":"All Provision List Report","Error":"No list entry found"}}
            if (mqttResult.contains("No list entry found")) {
                ToastShow.showToastOnUiThread(mContext, reportedObject.optString("Error"));
                return;
            }

            if (Interface.equals("All Provision List Report")) {
                ProvisionInfo provisionInfo;
                JSONArray provisionList = reportedObject.optJSONArray("Detial provision list");


                //返回的消息为：{"reported":{"MessageType":"All Provision List Report","Error":"No list entry"}}
                if(mqttResult.contains("Error")) {
                    Log.i(TAG, "```````````````````````Error");

                    ((Activity) getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dataList.clear();
                            adapter.notifyDataSetChanged();
                            setFirstAddImageView(true);
                        }
                    });
                }

                if(provisionList != null)
                {
                    for (int i = 0; i < provisionList.length(); i++) {
                        JSONObject temp = provisionList.getJSONObject(i);
                        String deviceType = temp.getString("Device type info");
                        String deviceId = temp.getString("Device id info");
                        String networkState = temp.getString("Network inclusion state");

                        JSONObject deviceTypeObject = new JSONObject(deviceType);
                        String genericCls = deviceTypeObject.getString("Generic Cls");
                        String specificCls = deviceTypeObject.getString("Specific Cls");
                        String iconType = deviceTypeObject.getString("Icon Type");

                        JSONObject deviceIdObject = new JSONObject(deviceId);
                        String manufacturerId = deviceIdObject.getString("Manufacturer Id");
                        String productType = deviceIdObject.getString("Product Type");
                        String productId = deviceIdObject.getString("Product Id");
                        String appVersion = deviceIdObject.getString("App Version");
                        String appSubVer = deviceIdObject.getString("App Sub Ver");

                        JSONObject networkStateObject = new JSONObject(networkState);
                        String nodeId = networkStateObject.getString("Node Id");
                        String status = networkStateObject.getString("Status");
                        Log.i(TAG, "NODE " + nodeId);
                        provisionInfo = new ProvisionInfo();
                        provisionInfo.setDsk(temp.getString("DSK"));
                        provisionInfo.setDeviceName(temp.getString("Device Name"));
                        provisionInfo.setDeviceBootMode(temp.getString("Device Boot Mode"));
                        provisionInfo.setDeviceLocation(temp.getString("Device Location"));
                        provisionInfo.setDeviceInclusionState(temp.getString("Device Inclusion state"));
                        provisionInfo.setNodeId(nodeId);
                        Const.removedDeviceNodeId = nodeId;
                        provisionInfo.setNetworkInclusionState(status);
                        dataList.add(provisionInfo);
                        ((Activity) getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();

                                if(dataList.size() == 0){
                                    setFirstAddImageView(true);
                                }else {
                                    setFirstAddImageView(false);
                                }
                                stopWaitDialog();
                            }
                        });
                    }
                }
                else
                {
                    ((Activity) getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            stopWaitDialog();
                        }
                    });
                }
            }

            String rmInterface = reportedObject.optString("Interface");
            if (rmInterface.equals("rmProvisionListEntry")) {
                String result = reportedObject.optString("result");
                if (result.equals("true")) {

                    Log.i(TAG, "==============dataList :"+dataList.size());
                    ((Activity) getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            removeDevice();
                            adapter.notifyDataSetChanged();

                            if(dataList.size() == 0){
                                setFirstAddImageView(true);
                            }else {
                                setFirstAddImageView(false);
                            }
                        }
                    });

                    removeDeviceHint();

                }
            } else if(rmInterface.equals("rmAllProvisionListEntry")) {
                String result = reportedObject.optString("result");
                if (result.equals("true")) {
                    Log.i(TAG, "------rmAllProvisionListEntry:success");
                    ((Activity) getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dataList.clear();
                            adapter.notifyDataSetChanged();
                            setFirstAddImageView(true);
                        }
                    });
                } else if(result.equals("failed")){
                    removeFailedDialog();
                }
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
        Logg.i(TAG,"======onResume=====");
        if (isVisibleToUser && isFirst2onResume) {
            showWaitingDialog();
            isFirst2onResume = false;
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getAllProvisionListEntry());
        }

        if (Const.RESET_PROVISION) {
            Const.RESET_PROVISION = false;
            networkRole.setText(Const.NETWORK_ROLE);
            if (dataList.size() > 0) {
                dataList.clear();
                adapter.notifyDataSetChanged();
            }

        }
        if (ADD_SUCCESS) {
            showWaitingDialog();
            ADD_SUCCESS = false;// Acitvity刷新的时候，Fragment都会执行onResume()，所以当判断了ADD_SUCCESS为true后就要给他赋值为false，以免误执行下面的代码。
            MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getAllProvisionListEntry());
        }
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
            case R.id.edit:
                Log.i(TAG, "~~~~~~~~~~~~~~~~~~~~~~edit");
                if (adapter.getMode() == 0) {
                    adapter.setMode(1);//删除
                    edit.setImageResource(R.drawable.ic_yes);
                } else {
                    adapter.setMode(0);//正常
                    edit.setImageResource(R.drawable.ic_edit);
                }
                ((Activity) getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
                break;
            case R.id.dsk_add_first_device:
                Intent intent = new Intent(getActivity(), AddSmartStartActivity.class);
                startActivityForResult(intent, 2);
                break;
        }
    }

    @Override
    public void onItemClick(View view, ProvisionInfo provisionInfo) {
//        Intent intent = new Intent(getActivity(), DeviceTestActivity.class);
//        intent.putExtra("provisionInfo", provisionInfo);
//        Log.i(TAG, "nodeid" + provisionInfo.getNodeId());
//        startActivity(intent);
        Intent intent = new Intent(getActivity(), DeviceTestEditActivity.class);
        intent.putExtra("provisionInfo", provisionInfo);
        startActivity(intent);

    }

    @Override
    public void onItemLongClick(View view, ProvisionInfo provisionInfo) {
//        Intent intent = new Intent(getActivity(), DeviceTestEditActivity.class);
//        intent.putExtra("provisionInfo", provisionInfo);
//        startActivity(intent);
    }

    @Override
    public void addItemClick() {
        Intent intent = new Intent(getActivity(), AddSmartStartActivity.class);
        startActivityForResult(intent, 2);
    }

    @Override
    public void deleteItemClick(int position) {
        Log.i(TAG, "~~~~~~~~~~~~~~deleteItemClick: " + position);
        ProvisionInfo itemInfo = dataList.get(position);
        String itemDesk = itemInfo.getDsk();
        Const.removedDeviceNodeId = itemInfo.getNodeId();
        showDeleteDeviceDialog(itemDesk);
        clickPosition = position;
    }

    /**
     *
     * @param tag 如果dataList.size > 0 就填false,表示有DSK列表，不显示第一次添加的按钮
     */
    private void setFirstAddImageView(boolean tag){
        if(tag){
            firstAddImageView.setVisibility(View.VISIBLE);
            scene_recycler.setVisibility(View.GONE);
        } else {
            firstAddImageView.setVisibility(View.GONE);
            scene_recycler.setVisibility(View.VISIBLE);
            Log.i(TAG, "````````scene_recycler.setVisibility(View.VISIBLE)");
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

    /**
     * 删除Dsk的提示框
     * @param dsk
     */
    protected void showDeleteDeviceDialog(final String dsk) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        final AlertDialog alertDialog = alertDialogBuilder.show();
        alertDialog.setCancelable(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_delete_smart_start_device, null);
        ImageView icon = (ImageView) view.findViewById(R.id.iv_icon);
        icon.setImageResource(R.drawable.vector_drawable_ic_92);
        alertDialog.setContentView(view);
        Button cancel = (Button) view.findViewById(R.id.btn_cancel);
        Button proceed = (Button) view.findViewById(R.id.btn_proceed);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.rmProvisionListEntry("rmProvisionListEntry", dsk));
                showWaitingDialog();
                Log.i(TAG, "~~~~~~~~~showDeleteDeviceDialog:" + dsk);
            }
        });
    }

    /**
     * 提示用户Device是否存在
     * 接口返回的监听调用了此方法，接口监听处于子线程中，直接调用BaseAcitivity的mContext无效，需要用getContext()
     * @param stringId
     */
    protected void removedDeviceHintDialog(final int stringId) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        final AlertDialog alertDialog = alertDialogBuilder.show();
        alertDialog.setCancelable(false);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_removed_device_hint, null);
        alertDialog.setContentView(view);

        TextView hintView = (TextView) view.findViewById(R.id.remove_device_hint_text);
        hintView.setText(getResources().getString(stringId));


        Button ok = (Button) view.findViewById(R.id.remove_device_hint_button);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
    }

    /**
     * 删除失败的提示框
     */
    protected void removeFailedDialog() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        final AlertDialog alertDialog = alertDialogBuilder.show();
        alertDialog.setCancelable(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_normal_layout, null);
        TextView title = (TextView) view.findViewById(R.id.title);
        TextView promptMessage = (TextView) view.findViewById(R.id.message);
        title.setText("Prompt");
        promptMessage.setText("Remove device failed!");
        alertDialog.setContentView(view);
        TextView positiveButton = (TextView) view.findViewById(R.id.positiveButton);
        TextView negativeButton = (TextView) view.findViewById(R.id.negativeButton);
        positiveButton.setText("retry");
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setMqttDataJson("rmAllProvisionListEntry"));
                alertDialog.dismiss();
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
    }

    public void removeDevice() {

        if (dataList.size() > 0) {
            dataList.remove(clickPosition);
        }
        ((Activity) getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
        if (dataList.size()==0) {
            adapter.setMode(0);
        }
    }

    /**
     * HomeActivity的监听onNavigationItemSelected 调用了此函数,
     * 相应的menu执行了get all dsk/remove all dsk
     * 调用方式：通过ScenesFragment的实例 ScenesFragment.newInstance().responseMenu(int action);
     * @param action 相应的动作
     */
    public void responseMenu(int action){

        if(action == Const.ADD_DSK){

        } else if(action == Const.GET_ALL_DSK) {
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getAllProvisionListEntry());
            showWaitingDialog();
        } else if(action == Const.REMOVE_ALL_DSK) {
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setMqttDataJson("rmAllProvisionListEntry"));
        }
    }

    /**
     * 判断dsk相对应的nodeId是否为“0”， 如果为“0”，表示该Device未添加
     */
    private void removeDeviceHint() {
        //List<ProvisionInfo> dataList;
        Log.i(TAG, "~~~~~~~~~~~~~removedDeviceNodeId:"+Const.removedDeviceNodeId+"-");
        ((Activity) getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Const.removedDeviceNodeId.equals("0")) { //NodeId = "0" 表示设备不存在
                    Log.i(TAG, "removeDeviceHint:460");
                    removedDeviceHintDialog(R.string.smart_start_device_not_exist);//设备不存在
                } else {
                    Log.i(TAG, "removeDeviceHint:464");
                    removedDeviceHintDialog(R.string.device_not_removed);//设备未移除
                }
            }
        });
    }

    /**
     * 监听从AddSmartStartActivity返回的Intent,这个intent主要是：添加smart start是否成功的result
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            String result = data.getExtras().getString("ADD_SMART_START_RESULT");
            if (result.equals("true")) {
                requestProvisionList();
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser = isVisibleToUser;
        if (isFirstLoad && !isVisibleToUser) {
            Log.d(TAG, "第一次加载,fragment不可见");
            isFirstLoad = false;
            networkRole.setText(Const.NETWORK_ROLE);
            return;
        }
        if (isVisibleToUser) {
            Log.d(TAG, "正常加载,fragment可见");
            //这里仅仅是注册，发送消息在onResum里面
            MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
            if(!isFirst2onResume)
            {
                showWaitingDialog();
                MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getAllProvisionListEntry());
            }
        } else {
            Log.d(TAG, "正常加载,fragment不可见");
            unrigister();
        }
    }

    public void register() {
        Logg.i(TAG,"===MQTTManagement===rigister=====");
        MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
        setUserVisibleHint(true);
        if (Const.getIsDataChange()) {
            Const.setIsDataChange(false);
            showWaitingDialog();
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getAllProvisionListEntry());
        }
    }

    public void addDskResult(){
        Log.i(TAG, "-----------addDskResult: ");
        ADD_SUCCESS = true;
    }
}
