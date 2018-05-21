package com.askey.firefly.zwave.control.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.askey.firefly.zwave.control.R;
import com.askey.firefly.zwave.control.dao.ZwaveDeviceManager;
import com.askey.firefly.zwave.control.dao.ZwaveDeviceSceneManager;
import com.askey.firefly.zwave.control.page.zwNodeMember;
import com.askey.firefly.zwave.control.service.ZwaveControlService;
import com.askey.firefly.zwave.control.utils.Const;
import com.askey.firefly.zwave.control.utils.DeviceInfo;
import com.askey.firefly.zwave.control.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by chiapin on 2017/9/22.
 */

public class RoomActivity extends BaseActivity {

    private static String LOG_TAG = RoomActivity.class.getSimpleName();
    private ZwaveDeviceManager zwaveDeviceManager;
    private ZwaveControlService zwaveService;
    private ZwaveDeviceSceneManager zwSceneManager;

    //private FragmentManager manager;
    //private FragmentTransaction transaction;

    private TextView txCondition, txSensorStatus;
    private Spinner spCondition;
    private ImageButton onButton, offButton;
    private GridView gvMember;

    private MemberAdapter adapter;

    private static String roomName, sensorCondition;
    private static String SensorNodeId;
    private static String sensorNodeInfo;
    private static List<zwNodeMember> roomMember = new ArrayList<>();
    private static List<zwNodeMember> sensorList = new ArrayList<>();
    private static ArrayList<String> spContacts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        Intent intent = getIntent();
        roomName = intent.getStringExtra("ScenceName");
        sensorCondition = intent.getStringExtra("RoomCondition");
        SensorNodeId = intent.getStringExtra("SensorNodeId");

        Log.i(LOG_TAG, "roomName = " + roomName + "| sensorNodeId = " + SensorNodeId + "| roomCondition = " + sensorCondition);

        /*
        manager = getSupportFragmentManager();
        transaction = manager.beginTransaction();

        // send roomName to fragment
        Bundle bundle = new Bundle();
        bundle.putString("SceneName",roomName);

        if (roomCondition.equals("null")) {

            Log.i(LOG_TAG,"Launch SelectSensorFragment");
            // user does not set the condition in this room, call selectSensorFragment
            SelectSensorFragment mSelectSensorFragment = new SelectSensorFragment();
            transaction.replace(R.id.sensorLin, mSelectSensorFragment, "SelectSensorFragment");

            mSelectSensorFragment.setArguments(bundle);

        }else{

            Log.i(LOG_TAG,"Launch SensorFragment");
            // have condition, call sensorFragment
            SensorFragment mSensorFragment = new SensorFragment();
            mSensorFragment.setArguments(bundle);
            transaction.replace(R.id.sensorLin, mSensorFragment, "SensorFragment");
        }
        transaction.commit();

        */

        // show devices
        zwaveDeviceManager = ZwaveDeviceManager.getInstance(this);
        zwSceneManager = ZwaveDeviceSceneManager.getInstance(this);

        // bind service
        Intent serviceIntent = new Intent(this, ZwaveControlService.class);
        this.bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);


        initView();
        getSensorList();

        //set spinner default value
        if (spContacts.size() != 0) {
            txCondition.setText("Select your condition :");

            ArrayAdapter<String> spAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, spContacts);
            spAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

            spCondition.setAdapter(spAdapter);
            if (!sensorCondition.equals("null")) {
                int getIdx = spContacts.indexOf(sensorCondition);
                spCondition.setSelection(getIdx, true);
            }

            spCondition.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                    String tmpCondition = spCondition.getSelectedItem().toString();
                    updateSenceDb p1 = new updateSenceDb(roomName, tmpCondition);
                    p1.start();

                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }

            });

            gvMember = (GridView) findViewById(R.id.gvMember);
            List<zwNodeMember> memberList = getMemberList();

            adapter = new MemberAdapter(this, memberList);

            gvMember.setAdapter(adapter);
            gvMember.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    zwNodeMember member = DeviceInfo.memberList.get(position);
                    Log.i(LOG_TAG, "nodeId = " + member.getNodeId());

                }
            });
        } else {
            txCondition.setText("no SENSOR in this room");
            spCondition.setVisibility(View.GONE);
            txSensorStatus.setVisibility(View.GONE);
        }
    }

    class updateSenceDb extends Thread {
        private String tmproomName;
        private String tmpcondition;

        updateSenceDb(String roomName, String condition) {
            tmproomName = roomName;
            tmpcondition = condition;
        }

        @Override
        public void run() {
            int sensorNodeId = 0;

            for (int idx = 0; idx < sensorList.size(); idx++) {
                if (sensorList.get(idx).getName().equals(tmpcondition)) {
                    sensorNodeId = sensorList.get(idx).getNodeId();
                    sensorNodeInfo = sensorList.get(idx).getNodeInfo();
                    break;
                }
            }
            /*
            // update scence in db
            ZwaveDeviceScene tmpScene = zwSceneManager.(roomName);
            tmpScene.setCondition(tmpcondition);
            tmpScene.setSensorNodeId(sensorNodeId);
            SensorNodeId = String.valueOf(sensorNodeId);
            sensorCondition = tmpcondition;
            tmpScene.update();
            //Log.i(LOG_TAG,"update conditino : roomName"+roomName+"|sensorNodeId="+sensorNodeId
            //        +"|condition = "+tmpcondition);
            */
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        zwaveService.unregister(mCallback);

        try {
            this.unbindService(conn);
        } catch (Exception e) {
            Log.d(LOG_TAG, e.toString());
        }
    }

    private void initView() {
        txCondition = (TextView) findViewById(R.id.txCondition);
        txSensorStatus = (TextView) findViewById(R.id.txSensorStatus);
        spCondition = (Spinner) findViewById(R.id.spSensor);
        onButton = (ImageButton) findViewById(R.id.buttonOn);
        offButton = (ImageButton) findViewById(R.id.buttonOff);

        onButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setRoomDevice(true);
            }
        });
        offButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setRoomDevice(false);
            }
        });
    }

    private class MemberAdapter extends BaseAdapter {

        Context context;

        MemberAdapter(Context context, List<zwNodeMember> memberList) {
            this.context = context;
            DeviceInfo.memberList = memberList;
        }

        @Override
        public int getCount() {
            return DeviceInfo.memberList.size();
        }

        @Override
        public View getView(int position, View itemView, ViewGroup parent) {
            if (itemView == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                itemView = layoutInflater.inflate(R.layout.devicestatus_view, parent, false);
            }

            zwNodeMember member = DeviceInfo.memberList.get(position);
            ImageView ivImage = (ImageView) itemView.findViewById(R.id.ivImage);
            CheckBox ckBox = (CheckBox) itemView.findViewById(R.id.checkbox_status);
            TextView tvName = (TextView) itemView.findViewById(R.id.nodeName);

            String tmpType = member.getDeviceType();
            switch (tmpType) {
                case "OTHER":
                    ivImage.setImageResource(R.drawable.unknown);
                    break;
                case "BULB":
                    ivImage.setImageResource(R.drawable.bulb);
                    break;
                case "DIMMER":
                    ivImage.setImageResource(R.drawable.dimmer);
                    break;
                case "PLUG":
                    ivImage.setImageResource(R.drawable.plug);
                    break;

            }

            if (member.getName().equals(String.valueOf(member.getNodeId()))) {
                tvName.setText(member.getDeviceType() + member.getName());
            } else {
                tvName.setText(member.getName());
            }
            ckBox.setChecked(member.getNodeStatus());
            return itemView;
        }

        @Override
        public Object getItem(int position) {
            return DeviceInfo.memberList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return DeviceInfo.memberList.get(position).getNodeId();
        }
    }


    // get sensor list
    private void getSensorList() {

        sensorList.clear();
        spContacts.clear();
/*
        List<ZwaveDevice> tmpList = zwaveDeviceManager.getSceneDevicesList(roomName);

        for (int idx = 0; idx < tmpList.size(); idx++) {

            if (tmpList.get(idx).getDevType().equals("SENSOR")) {

                if (tmpList.get(idx).getNodeInfo().contains("COMMAND_CLASS_NOTIFICATION")) {
                    try {
                        JSONObject jsonObject = new JSONObject(tmpList.get(idx).getNodeInfo());
                        if (jsonObject.getString("Product id").equals("001F")) {
                            //Water
                            sensorList.add(new zwNodeMember(tmpList.get(idx).getNodeId(), tmpList.get(idx).getHomeId(),
                                    tmpList.get(idx).getDevType(), "WATER", "", false, ""));
                            spContacts.add("WATER");
                        } else if (jsonObject.getString("Product id").equals("000C")) {
                            //Motion

                            sensorList.add(new zwNodeMember(tmpList.get(idx).getNodeId(),tmpList.get(idx).getHomeId(),
                                    tmpList.get(idx).getDevType(), "MOTION",""));
                            spContacts.add("MOTION");

                            //Door/Window
                            sensorList.add(new zwNodeMember(tmpList.get(idx).getNodeId(), tmpList.get(idx).getHomeId(),
                                    tmpList.get(idx).getDevType(), "DOOR", "", false, ""));
                            spContacts.add("DOOR");
                            //Luminance
                            sensorList.add(new zwNodeMember(tmpList.get(idx).getNodeId(), tmpList.get(idx).getHomeId(),
                                    tmpList.get(idx).getDevType(), "LUMINANCE", "", false, ""));
                            spContacts.add("LUMINANCE");
                        } else if (jsonObject.getString("Product id").equals("0036")) {

                            //Door/Window
                            sensorList.add(new zwNodeMember(tmpList.get(idx).getNodeId(), tmpList.get(idx).getHomeId(),
                                    tmpList.get(idx).getDevType(), "DOOR", "", false, ""));
                            spContacts.add("DOOR");
                        } else if (jsonObject.getString("Product id").equals("001E")) {

                            //SMOKE
                            sensorList.add(new zwNodeMember(tmpList.get(idx).getNodeId(), tmpList.get(idx).getHomeId(),
                                    tmpList.get(idx).getDevType(), "SMOKE", "", false, ""));
                            spContacts.add("SMOKE");
                        } else if (jsonObject.getString("Product id").equals("0050")) {

                            //MOTION
                            sensorList.add(new zwNodeMember(tmpList.get(idx).getNodeId(), tmpList.get(idx).getHomeId(),
                                    tmpList.get(idx).getDevType(), "MOTION", "", false, ""));
                            spContacts.add("MOTION");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        */
    }

    // get device list
    private List<zwNodeMember> getMemberList() {

        roomMember.clear();
        /*
        List<ZwaveDevice> list = zwaveDeviceManager.getSceneDevicesList(roomName);

        for (int idx = 0; idx < list.size(); idx++) {
            Log.i(LOG_TAG, "*** NodeId = " + list.get(idx).getNodeId() + " | HomeID = " + list.get(idx).getHomeId()
                    + "| devType=" + list.get(idx).getDevType() + " | Name=" + list.get(idx).getName()
                    + "| roomName=" + list.get(idx).getScene() + "| nodeInfo = " + list.get(idx).getNodeInfo());
            if (!list.get(idx).getDevType().equals("SENSOR")) {
                roomMember.add(new zwNodeMember(list.get(idx).getNodeId(), list.get(idx).getHomeId(),
                        list.get(idx).getDevType(), list.get(idx).getName(),
                        list.get(idx).getScene(), false, list.get(idx).getNodeInfo()));
            }
        }
        */
        return roomMember;
    }

    private int getNodeIdPosition(int devNodeId) {
        for (int idx = 0; idx < roomMember.size(); idx++) {
            if (roomMember.get(idx).getNodeId() == devNodeId) {
                return idx;
            }
        }
        return 9999;
    }

    private Runnable getDevStatus = new Runnable() {
        @Override
        public void run() {
            for (int idx = 0; idx < roomMember.size(); idx++) {
                zwaveService.getBasic(Const.zwaveType,roomMember.get(idx).getNodeId());
            }
        }
    };

    // bind service with zwave control service
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            zwaveService = ((ZwaveControlService.MyBinder) iBinder).getService();
            //register mCallback
            if (zwaveService != null) {
                zwaveService.register(mCallback);

                new Thread(getDevStatus).start();

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            zwaveService = null;
        }
    };

    private ZwaveControlService.zwaveCallBack mCallback = new ZwaveControlService.zwaveCallBack() {

        @Override
        public void zwaveControlResultCallBack(String className, String result) {

            if (Utils.isGoodJson(result)) {
                try {
                    final JSONObject jsonObject = new JSONObject(result);
                    String messageType = jsonObject.optString("MessageType");

                    final int nodeId = jsonObject.optInt("Node id");
                    Log.i(LOG_TAG, "sensorNodeId = " + SensorNodeId + " | nodeId = " + nodeId +
                            "|Condition = " + sensorCondition + " | messageType = " + messageType);

                    if (!SensorNodeId.equals("null") && (Integer.valueOf(SensorNodeId) == nodeId)) {

                        if (messageType.equals("Notification Get Information")) {
                            String notificationType = jsonObject.optString("Notification-type");
                            String notificationEvent = jsonObject.optString("Notification-event");

                            if (sensorCondition.equals("DOOR")) {
                                if (notificationType.equals("Access control")) {
                                    updateUi("Access control = " + notificationEvent);
                                    if (notificationEvent.contains("open")) {
                                        //turn on all device
                                        setRoomDevice(true);
                                    } else {
                                        //turn off all device
                                        setRoomDevice(false);
                                    }
                                }
                            } else if (sensorCondition.equals("WATER")) {
                                if (notificationType.equals("Water alarm")) {

                                    if (notificationEvent.contains("detected")) {
                                        //turn on all device
                                        updateUi(notificationType+" :" + "Water leak detected");
                                        setRoomDevice(true);
                                    } else {
                                        //turn off all device
                                        updateUi(notificationType+" :" + "State idle" );
                                        setRoomDevice(false);
                                    }
                                }
                            } else if (sensorCondition.equals("SMOKE")) {
                                if (notificationType.equals("Smoke alarm")) {

                                    if (notificationEvent.contains("detected")) {
                                        //turn on all device
                                        updateUi(notificationType+" :" + "Smoke detected");
                                        setRoomDevice(true);
                                    } else {
                                        //turn off all device
                                        updateUi(notificationType+" :" + "State idle" );
                                        setRoomDevice(false);
                                    }
                                }
                            } else if (sensorCondition.equals("MOTION")) {
                                if (notificationType.equals("Home security")) {

                                    if (notificationEvent.contains("detection")) {
                                        //turn on all device
                                        updateUi("Motion detection");
                                        setRoomDevice(true);
                                    } else {
                                        //turn off all device
                                        updateUi("Motion detection :" + "State idle" );
                                        setRoomDevice(false);
                                    }
                                }
                            }
                        } else if (messageType.equals("Sensor Info Report")) {

                            if (sensorCondition.equals("LUMINANCE")) {
                                String txType = jsonObject.optString("type");
                                if (txType.equals("Luminance sensor")) {

                                    String txValue = jsonObject.optString("value");

                                    updateUi(txType + " : " + txValue + " %");
                                    if (Integer.valueOf(txValue) < 50) {
                                        //turn on all device
                                        setRoomDevice(true);
                                    } else {
                                        //turn off all device
                                        setRoomDevice(false);
                                    }
                                }
                            }
                        }
                    } else if ("Basic Information".equals(messageType)) {
                        String value = jsonObject.optString("value");
                        int position = getNodeIdPosition(nodeId);
                        Log.i(LOG_TAG, "NodeId = " + nodeId + " |Value = " + value + " |posirion = " + position);

                        zwNodeMember tmpNode = roomMember.get(position);

                        if (value.equals("00h")) {
                            tmpNode.setNodeStatus(false);
                        } else {
                            tmpNode.setNodeStatus(true);
                        }

                        //adapter.notifyDataSetChanged();

                        /*
                        if (value.equals("00h")) {
                            //turn off
                            cbSwitch.setChecked(false);
                        } else {
                            //turn on
                            cbSwitch.setChecked(true);

                            //change Hex string to Interger
                            String tmpValue = value.substring(0, value.length() - 1);
                            basicValue = Integer.valueOf(tmpValue, 16);

                            if (adjustFlag && value != "00h") {
                                brightness_change.setProgress(basicValue);
                                adjustFlag = false;
                            }
                        }
                        */
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    };

    /*
    private class CallbackRunnable implements Runnable {

        private int nodeId;
        private String result;

        public CallbackRunnable(String result) {
            this.result = result;
        }

        @Override
        public void run() {

        }
    }
    */

    private void updateUi(final String tmptext) {

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(LOG_TAG, "set txView = " + tmptext);
                txSensorStatus.setText(tmptext);
            }
        });

    }


    private void setRoomDevice(boolean status) {
        /*
        List<ZwaveDevice> roomNode = zwaveDeviceManager.getSceneDevicesList(roomName);
        for (int idx = 0; idx < roomNode.size(); idx++) {
            if (!roomNode.get(idx).getDevType().equals("SENSOR")) {
                if (status) {
                    Log.i(LOG_TAG, "Set device#" + roomNode.get(idx).getNodeId() + " to on");
                    zwaveService.setBasic(roomNode.get(idx).getNodeId(), 255);

                } else {
                    Log.i(LOG_TAG, "Set device#" + roomNode.get(idx).getNodeId() + " to off");
                    zwaveService.setBasic(Const.zwaveType,roomNode.get(idx).getNodeId(), 0);
                }
            }
        }
        */
    }

    private void setRoomDevice(int nodeid, boolean status) {

        if (status) {
            Log.i(LOG_TAG, "Set device#" + nodeid + " to on");
            zwaveService.setBasic(Const.zwaveType,nodeid, 255);

        } else {
            Log.i(LOG_TAG, "Set device#" + nodeid + " to off");
            zwaveService.setBasic(Const.zwaveType,nodeid, 0);
        }
    }
}