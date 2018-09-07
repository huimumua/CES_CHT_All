package com.askey.firefly.zwave.control.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.askey.firefly.zwave.control.R;
import com.askey.firefly.zwave.control.dao.ZwaveDeviceManager;
import com.askey.firefly.zwave.control.service.ZwaveControlService;
import com.askey.firefly.zwave.control.thirdparty.usbserial.UsbSerial;
import com.askey.firefly.zwave.control.utils.DeviceInfo;
import com.askey.firefly.zwave.control.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static String LOG_TAG = WelcomeActivity.class.getSimpleName();

    private ZwaveControlService zwaveService;
    private ZwaveDeviceManager zwDevManager;
    private BroadcastReceiver usbReceiver = null;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private ArrayList<Integer> nodeIdArr = new ArrayList<>();
    private Button btnButton,button,btnAdd,btn_stopAdd,btnRm,btn_stopRm,btnPlug,btnBulb,btnSensor,btn_next;
    private Spinner spNodeIdList,spApiList;
    private TextView txAllMsg,tvtest;
    private EditText editText;
    private int selectNode;
    private boolean reqCallBack =false;
    private boolean lock = true;
    private int res = 0;
    private boolean showText = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new UsbSerial(this);
        setContentView(R.layout.activity_main);
        mContext = this;
        zwDevManager = ZwaveDeviceManager.getInstance(this);
        new Thread(bindzwaveservice).start();
        initBtn();
        getNodeId();

    }

    public Runnable bindzwaveservice = new Runnable() {
        @Override
        public void run() {
            Intent serviceIntent = new Intent(MainActivity.this, ZwaveControlService.class);
            bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);
        }
    };

    private void initBtn() {
        Log.i(LOG_TAG, "initBtn");

        editText = (EditText) findViewById(R.id.editText);
        btnButton = (Button) findViewById(R.id.btnButton);
        button = (Button) findViewById(R.id.button);
        btnPlug = (Button) findViewById(R.id.btnPlug);
        btnBulb = (Button) findViewById(R.id.btnBulb);
        btnSensor = (Button) findViewById(R.id.btnSensor);
        btnAdd = (Button) findViewById(R.id.btnAdd);
        btn_stopAdd = (Button) findViewById(R.id.btn_stopAdd);
        btnRm = (Button) findViewById(R.id.btnRm);
        btn_stopRm = (Button) findViewById(R.id.btn_stopRm);
        btn_next = (Button) findViewById(R.id.btn_next);
        txAllMsg = (TextView) findViewById(R.id.txAllMsg);
        tvtest = (TextView) findViewById(R.id.tvtest);

        spNodeIdList = (Spinner) findViewById(R.id.nodeIdList);
        spApiList = (Spinner) findViewById(R.id.spApiList);
        btnButton.setOnClickListener(this);
        button.setOnClickListener(this);
        btnPlug.setOnClickListener(this);
        btnBulb.setOnClickListener(this);
        btnSensor.setOnClickListener(this);
        btnAdd.setOnClickListener(this);
        btn_stopAdd.setOnClickListener(this);
        btnRm.setOnClickListener(this);
        btn_stopRm.setOnClickListener(this);
        btn_next.setOnClickListener(this);

        AssetManager assetManager = getAssets();
        InputStream inputStream = null;

        //載入assets/ApiName.txt 內容到spinner
        try {
            inputStream = assetManager.open("ApiName.txt");
            String text = loadTextFile(inputStream);
            String[] apiName = text.split(",");

            ArrayAdapter<String> getApiList = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item,apiName);

            spApiList.setAdapter(getApiList);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //read content of ApiName.txt
    private String loadTextFile(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bytes = new byte[4096];
        int len = 0;
        while ((len = inputStream.read(bytes)) > 0) {
            byteArrayOutputStream.write(bytes, 0, len);
        }
        return new String(byteArrayOutputStream.toByteArray(), "UTF8");
    }


    // permanent thread
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2002:
                    txAllMsg.setText("class name : \n" +DeviceInfo.className + "\n\n result : \n " +DeviceInfo.result);
                    showText = false;
                    break;
            }
        }
    };

    @Override
    public void onClick(final View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Log.d(LOG_TAG,"node ID : " + selectNode);
                String editString = String.valueOf(editText.getText());
                final String[] parameter = editString.split(",");

                switch (view.getId()) {
                    case R.id.btnAdd:
                        res = zwaveService.addDevice(DeviceInfo.devType);
                        break;
                    case R.id.btn_stopAdd:
                        showText = true;
                        res = zwaveService.stopAddDevice(DeviceInfo.devType);
                        break;
                    case R.id.btnRm:
                        res = zwaveService.removeDevice(DeviceInfo.devType,0);
                        break;

                    case R.id.btn_stopRm:
                        showText = true;
                        res = zwaveService.stopRemoveDevice(DeviceInfo.devType);
                        break;

                    case R.id.button:
                        inputReqKey();
                        break;

                    case R.id.btn_next:
                        lock = false;
                        break;

                    case R.id.btnButton:
                        Log.d(LOG_TAG,"gino choose spList " + spApiList.getSelectedItem().toString());
                        Log.d(LOG_TAG,"gino param length " + parameter.length);

                        if (spApiList.getSelectedItem().toString().contains("openController")) {
                            showText = true;
                            zwaveService.doOpenController();
                        } else if (spApiList.getSelectedItem().toString().contains("closeController")) {
                            showText = true;
                            res = zwaveService.closeController();
                        } else if (spApiList.getSelectedItem().toString().contains("removeFailedDevice")) {
                            showText = true;
                            res = zwaveService.removeFailedDevice(selectNode);
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ArrayAdapter<Integer> devList = new ArrayAdapter<Integer>(MainActivity.this,
                                            android.R.layout.simple_spinner_dropdown_item,
                                            convertIntegers(nodeIdArr));
                                    spNodeIdList.setAdapter(devList);
                                }
                            });
                        } else if (spApiList.getSelectedItem().toString().contains("replaceFailedDevice")) {
                            showText = true;
                            res = zwaveService.replaceFailedDevice(selectNode);
                        } else if (spApiList.getSelectedItem().toString().contains("startLearnMode")) {
                            showText = true;
                            res = zwaveService.StartLearnMode();
                        } else if (spApiList.getSelectedItem().toString().contains("getAllDeviceList")) {
                            showText = true;
                            getNodeId();
                            res = zwaveService.getAllDeviceList();
                        } else if (spApiList.getSelectedItem().toString().contains("getDeviceInfo")) {
                            showText = true;
                            res = zwaveService.getDeviceInfo();
                        } else if (spApiList.getSelectedItem().toString().contains("getSpecifyDeviceInfo")) {
                            showText = true;
                            res = zwaveService.getSpecifyDeviceInfo(selectNode);
                            getNotificationInfo();
                        } else if (spApiList.getSelectedItem().toString().contains("checkNodeIsFailed")) {
                            showText = true;
                            res = zwaveService.checkNodeIsFailed(selectNode);
                        } else if (spApiList.getSelectedItem().toString().contains("setDefault")) {
                            showText = true;
                            res = zwaveService.setDefault();
                        } else if (spApiList.getSelectedItem().toString().contains("getDeviceBattery")) {
                            showText = true;
                            res = zwaveService.getDeviceBattery(DeviceInfo.devType,selectNode);
                        } else if (spApiList.getSelectedItem().toString().contains("getBasic")) {
                            showText = true;
                            res = zwaveService.getBasic(DeviceInfo.devType, selectNode);
                        } else if (spApiList.getSelectedItem().toString().contains("setBasic")) {
                            //parameter[0] 設置開關 0或255
                            if(parameter.length == 1)
                                res = zwaveService.setBasic(DeviceInfo.devType, selectNode, Integer.valueOf(parameter[0]));
                        } else if (spApiList.getSelectedItem().toString().contains("getMeter")) {
                            showText = true;
                            //parameter[0] 說明書上給定的單位 0x00 KWh, 0x02 W, 0x04 V, 0x05 I, 0x06 PF
                            if(parameter.length == 1)
                                res = zwaveService.getMeter(DeviceInfo.devType,selectNode,Integer.valueOf(parameter[0]));
                        } else if (spApiList.getSelectedItem().toString().contains("getMeterSupported")) {
                            showText = true;
                            res = zwaveService.getMeterSupported(selectNode);
                        } else if (spApiList.getSelectedItem().toString().contains("resetMeter")) {
                            res = zwaveService.resetMeter(DeviceInfo.devType, selectNode);
                        } else if (spApiList.getSelectedItem().toString().contains("getSwitchColor")) {
                            showText = true;
                            //0-8 "Warm Write", "Cold Write", "Red","Green", "Blue","Amber", "Cyan", "Purple", "Indexed Color"
                            res = zwaveService.getSwitchColor(DeviceInfo.devType,selectNode,2); //2代表紅色
                            res = zwaveService.getSwitchColor(DeviceInfo.devType,selectNode,3); //3代表綠色
                            res = zwaveService.getSwitchColor(DeviceInfo.devType,selectNode,4); //4代表藍色
                        } else if (spApiList.getSelectedItem().toString().contains("setSwitchColor")) {
                            //0-8 "Warm Write", "Cold Write", "Red","Green", "Blue","Amber", "Cyan", "Purple", "Indexed Color"
                            //parameter[0] value 0-255
                            //parameter[1] value 0-255
                            //parameter[2] value 0-255
                            if(parameter.length == 3) {
                                res = zwaveService.setSwitchColor(DeviceInfo.devType, selectNode, 2, Integer.valueOf(parameter[0]));//compId 2 red color, value 0-255
                                res = zwaveService.setSwitchColor(DeviceInfo.devType, selectNode, 3, Integer.valueOf(parameter[1]));//compId 3 green color, value 0-255
                                res = zwaveService.setSwitchColor(DeviceInfo.devType, selectNode, 4, Integer.valueOf(parameter[2]));//compId 4 blue color, value 0-255
                                res = zwaveService.setSwitchColor(DeviceInfo.devType, selectNode, 0, 0);//關掉 compId 0 Warm Write , value 0
                                res = zwaveService.setSwitchColor(DeviceInfo.devType, selectNode, 1, 0);//關掉 compId 1 Cold Write , value 0
                            }
                        } else if (spApiList.getSelectedItem().toString().contains("getSupportedSwitchColor")) {
                            showText = true;
                            res = zwaveService.getSupportColor(DeviceInfo.devType, selectNode);
                        } else if (spApiList.getSelectedItem().toString().contains("startStopSwitchColorLevelChange")) {
                            //parameter[0] 0 調亮, 1調暗
                            //parameter[1] 0-8 "Warm Write", "Cold Write", "Red","Green", "Blue","Amber", "Cyan", "Purple", "Indexed Color" 要調亮或是調暗的顏色
                            //ignore 不支援填0即可
                            //start_value 不支援填0即可
                            //調用第一次為start, 調用第二次為stop
                            if(parameter.length == 2)
                                res = zwaveService.startStopColorLevelChange(DeviceInfo.devType, selectNode, Integer.parseInt(parameter[0]),0, Integer.parseInt(parameter[1]), 0);
                        } else if (spApiList.getSelectedItem().toString().contains("getSwitchMultiLevel")) {
                            showText = true;
                            res = zwaveService.getSwitchMultiLevel(DeviceInfo.devType, selectNode);
                        } else if (spApiList.getSelectedItem().toString().contains("setSwitchMultiLevel")) {
                            //parameter[0] 亮度值 0-99
                            //duration 變化時間
                            if(parameter.length == 1)
                                res = zwaveService.setSwitchMultiLevel(DeviceInfo.devType, selectNode, Integer.parseInt(parameter[0]), 1);
                        } else if (spApiList.getSelectedItem().toString().contains("startStopSwitchLevelChange")) {
                            //parameter[0] 1 最暗,99 最亮, 255目前狀態
                            //parameter[1] 變化時間 1表示1秒
                            //parameter[2] 0 變亮 1 變暗  3 不變
                            //secChangeDir 不支援填0即可
                            //secStep 不支援填0即可
                            if(parameter.length == 3)
                                res = zwaveService.startStopSwitchLevelChange(selectNode, Integer.parseInt(parameter[0]), Integer.parseInt(parameter[1]),Integer.parseInt(parameter[2]), 0,0);
                        } else if (spApiList.getSelectedItem().toString().contains("getSensorMultiLevel")) {
                            try {
                                showText = true;
                                res = zwaveService.getSensorMultiLevel(DeviceInfo.devType, selectNode);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        } else if (spApiList.getSelectedItem().toString().contains("getConfiguration")) {
                            //paramMode 不支援填0即可
                            //說明書上給定的參數index
                            //rangeStart不支援填0即可
                            //rangeEnd不支援填0即可
                            showText = true;
                            if(parameter.length == 1)
                                res = zwaveService.getConfiguration(selectNode,0,Integer.parseInt(parameter[0]),0,0);
                        } else if (spApiList.getSelectedItem().toString().contains("setConfiguration")) {
                            //parameter[0] 說明書上給定的參數index
                            //parameter[1] 說明書上給定的參數大小
                            //parameter[2] 預設值 1，parameter[3]無效，若為0， 設置parameter[3]
                            //parameter[3] 說明書上給定的設定值
                            if(parameter.length == 4) {
                                try {
                                    res = zwaveService.setConfiguration(selectNode, Integer.parseInt(parameter[0]), Integer.parseInt(parameter[1]), Integer.parseInt(parameter[2]), Integer.parseInt(parameter[3]));
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if (spApiList.getSelectedItem().toString().contains("getPowerLevel")) {
                            showText = true;
                            res = zwaveService.getPowerLevel(selectNode);
                        } else if (spApiList.getSelectedItem().toString().contains("setBinarySwitchState")) {
                            //parameter[0] 0開 255關
                            if(parameter.length == 1)
                                res = zwaveService.setBinarySwitchState(DeviceInfo.devType, selectNode, Integer.parseInt(parameter[0])); //0關
                        } else if (spApiList.getSelectedItem().toString().contains("getBinarySwitchState")) {
                            showText = true;
                            res = zwaveService.getBinarySwitchState(DeviceInfo.devType, selectNode);
                        } else if (spApiList.getSelectedItem().toString().contains("getSensorBinary")) {
                            //parameter[0] 說明書上給定的參數index
                            showText = true;
                            if(parameter.length == 1)
                                res = zwaveService.getSensorBinary(selectNode, Integer.parseInt(parameter[0]));
                        } else if (spApiList.getSelectedItem().toString().contains("getSensorBinarySupportedSensor")) {
                            res = zwaveService.GetSensorBinarySupportedSensor(selectNode);
                        } else if (spApiList.getSelectedItem().toString().contains("getGroupInfo")) {
                            //parameter[0] 為COMMAND_CLASS_ASSOCIATION_GRP_INFO獲取的值，該資訊可以在node info中查看到
                            //endpointId 個具體的channel(如果設備支援multi-channel)，否則預設為0
                            showText = true;
                            if(parameter.length == 1)
                                res = zwaveService.getGroupInfo(DeviceInfo.devType, selectNode, Integer.parseInt(parameter[0]), 0);
                        } else if (spApiList.getSelectedItem().toString().contains("addEndpointsToGroup")) {
                            //parameter[0] 為COMMAND_CLASS_ASSOCIATION_GRP_INFO獲取的值，該資訊可以在node info中查看到
                            //parameter[1] 被添加的nodeId
                            //endpointId 則表示聯動主設備的某個具體的channel(如果設備支援multi-channel)，否則預設為0
                            if(parameter.length == 2) {
                                String controlNodeId = parameter[1];
                                ArrayList nodeInterFaceList = new ArrayList();
                                nodeInterFaceList.add(controlNodeId);

                                try {
                                    JSONArray jsonArray = new JSONArray();
                                    JSONObject tmpObj = null;
                                    int count = nodeInterFaceList.size();
                                    for (int i = 0; i < count; i++) {
                                        tmpObj = new JSONObject();
                                        tmpObj.put("controlNodeId", nodeInterFaceList.get(i));
                                        jsonArray.put(tmpObj);
                                        //tmpObj = null;
                                    }
                                    String personInfos = jsonArray.toString(); // 将JSONArray转换得到String
                                    DeviceInfo.mqttString = personInfos;   // 获得JSONObject的String
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                JSONArray ja = null;
                                try {
                                    ja = new JSONArray(DeviceInfo.mqttString);
                                    DeviceInfo.arrList = new ArrayList<>();
                                    for (int j = 0; j < ja.length(); j++) {
                                        JSONObject json = ja.getJSONObject(j);
                                        DeviceInfo.arrList.add(Integer.valueOf(json.getString("controlNodeId").toString()));
                                        Log.d(LOG_TAG, "gino controlNodeId = " + Integer.valueOf(json.getString("controlNodeId").toString()));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                res = zwaveService.addEndpointsToGroup(DeviceInfo.devType, selectNode, Integer.parseInt(parameter[0]), Utils.convertIntegers(DeviceInfo.arrList), 0);
                                showRes(res);
                            }
                        } else if (spApiList.getSelectedItem().toString().contains("removeEndpointsFromGroup")) {
                            //parameter[0] 為COMMAND_CLASS_ASSOCIATION_GRP_INFO獲取的值，該資訊可以在node info中查看到
                            //parameter[1] 被添加的nodeId
                            //endpointId 則表示聯動主設備的某個具體的channel(如果設備支援multi-channel)，否則預設為0
                            if(parameter.length == 2) {
                                String controlNodeId = parameter[1];
                                ArrayList nodeInterFaceList = new ArrayList();
                                nodeInterFaceList.add(controlNodeId);

                                try {
                                    JSONArray jsonArray = new JSONArray();
                                    JSONObject tmpObj = null;
                                    int count = nodeInterFaceList.size();
                                    for (int i = 0; i < count; i++) {
                                        tmpObj = new JSONObject();
                                        tmpObj.put("controlNodeId", nodeInterFaceList.get(i));
                                        jsonArray.put(tmpObj);
                                        //tmpObj = null;
                                    }
                                    String personInfos = jsonArray.toString(); // 将JSONArray转换得到String
                                    DeviceInfo.mqttString = personInfos;   // 获得JSONObject的String
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                JSONArray ja = null;
                                try {
                                    ja = new JSONArray(DeviceInfo.mqttString);
                                    DeviceInfo.arrList = new ArrayList<>();
                                    for (int j = 0; j < ja.length(); j++) {
                                        JSONObject json = ja.getJSONObject(j);
                                        DeviceInfo.arrList.add(Integer.valueOf(json.getString("controlNodeId").toString()));
                                        Log.d(LOG_TAG, "gino controlNodeId = " + Integer.valueOf(json.getString("controlNodeId").toString()));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                res = zwaveService.removeEndpointsFromGroup(DeviceInfo.devType, selectNode, Integer.parseInt(parameter[0]), Utils.convertIntegers(DeviceInfo.arrList), 0);
                                showRes(res);
                            }
                        } else if (spApiList.getSelectedItem().toString().contains("getMaxSupportedGroups")) {
                            //parameter[0]最大支持的group數量，如果endpointId為0，則表示整個設備最大支援group數。
                            showText = true;
                            if(parameter.length == 1)
                                res = zwaveService.getMaxSupportedGroups(selectNode, Integer.parseInt(parameter[0]));
                        } else if (spApiList.getSelectedItem().toString().contains("getSpecificGroup")) {
                            //parameter[0]最大支持的group數量，如果endpointId為0，則表示整個設備最大支援group數。
                            showText = true;
                            if(parameter.length == 1)
                                res = zwaveService.getSpecificGroup(selectNode, Integer.parseInt(parameter[0]));
                        } else if (spApiList.getSelectedItem().toString().contains("getNotification")) {
                            //alarm_type 不支援填0x00即可
                            //parameter[0] 說明書上給定的參數index
                            //status 不支援填0x00即可
                            showText = true;
                            if(parameter.length == 1)
                                res = zwaveService.getNotification(selectNode, 0x00, Integer.parseInt(parameter[0]), 0x00);
                        } else if (spApiList.getSelectedItem().toString().contains("setNotification")) {
                            //parameter[0] 說明書上給定的參數index
                            //parameter[1] 0 狀態off, 1 狀態on
                            if(parameter.length == 2) {
                                res = zwaveService.setNotification(selectNode, Integer.parseInt(parameter[0]), Integer.parseInt(parameter[1]));
                            }
                        } else if (spApiList.getSelectedItem().toString().contains("getSupportedNotification")) {
                            res = zwaveService.getSupportedNotification(selectNode);
                        } else if (spApiList.getSelectedItem().toString().contains("getSupportedEventNotification")) {
                            //parameter[0] 說明書上給定的參數index
                            if(parameter.length == 1) {
                                showText = true;
                                res = zwaveService.getSupportedEventNotification(selectNode, Integer.parseInt(parameter[0]));
                            }
                        } else if (spApiList.getSelectedItem().toString().contains("getSupportedCentralScene")) {
                            //parameter[0]最大支持的group數量，如果endpointId為0，則表示整個設備最大支援group數。
                            if(parameter.length == 1) {
                                showText = true;
                                res = zwaveService.getSupportedCentralScene(selectNode, Integer.parseInt(parameter[0])); //插頭不支援
                            }
                        } else if (spApiList.getSelectedItem().toString().contains("startNetworkHealthCheck")) {
                            showText = true;
                            res = zwaveService.startNetworkHealthCheck();
                        } else if (spApiList.getSelectedItem().toString().contains("addProvisionListEntry")) {
                            // dskKey  序號
                            //parameter[1] 添加模式
                            //parameter[2] 添加狀態
                            //qrCodeFlag   是否使用qr code true有使用/false 沒使用
                            if(parameter.length == 3) {
                                String dskKey = "46995-35909-52806-15806-63590-06032-00856-14328" + "\0";
                                byte[] dskNumber = dskKey.getBytes();

                                switch (Integer.parseInt(parameter[0])) {
                                    case 0:
                                        parameter[0] = "Pending";
                                        break;
                                    case 1:
                                        parameter[0] = "Passive";
                                        break;
                                    case 2:
                                        parameter[0] = "Ignored";
                                        break;
                                }

                                switch (Integer.parseInt(parameter[1])) {
                                    case 0:
                                        parameter[1] = "Smart Start";
                                        break;
                                    case 1:
                                        parameter[1] = "Security 2";
                                        break;
                                }
                                boolean qrCodeFlag = false;
                                if (Integer.parseInt(parameter[2]) == 0)
                                    qrCodeFlag = false;
                                else
                                    qrCodeFlag = true;
                                showText = true;
                                res = zwaveService.addProvisionListEntry(DeviceInfo.devType, dskNumber, parameter[0], parameter[1], qrCodeFlag);
                            }
                        } else if (spApiList.getSelectedItem().toString().contains("rmProvisionListEntry")) {
                            // dskKey  序號
                            String dskKey = "46995-35909-52806-15806-63590-06032-00856-14328"+ "\0";
                            byte[] dskNumber = dskKey.getBytes();
                            showText = true;
                            res = zwaveService.rmProvisionListEntry(DeviceInfo.devType, dskNumber);
                        } else if (spApiList.getSelectedItem().toString().contains("getProvisionListEntry")) {
                            // dskKey  序號
                            //String dskKey = editText.getText().toString()+ "\0";
                            String dskKey = "46995-35909-52806-15806-63590-06032-00856-14328"+ "\0";
                            byte[] dskNumber = dskKey.getBytes();
                            showText = true;
                            res = zwaveService.getProvisionListEntry(DeviceInfo.devType, dskNumber);
                        } else if (spApiList.getSelectedItem().toString().contains("getAllProvisionListEntry")) {
                            showText = true;
                            res = zwaveService.getAllProvisionListEntry();
                        } else if (spApiList.getSelectedItem().toString().contains("rmAllProvisionListEntry")) {
                            showText = true;
                            res = zwaveService.rmAllProvisionListEntry();
                        }
                        break;

                    case R.id.btnPlug:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                //=================================================================== set  ===================================================================
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        txAllMsg.setText("");
                                    }
                                });

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("setBasic off");
                                    }
                                });
                                res = zwaveService.setBasic(DeviceInfo.devType, selectNode, 0);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("setBasic on");
                                    }
                                });
                                res = zwaveService.setBasic(DeviceInfo.devType, selectNode, 255);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("setConfiguration 小夜燈OFF");
                                        //parameter[0] 說明書上給定的參數index
                                        //parameter[1] 說明書上給定的參數大小
                                        //parameter[2] 預設值 1，parameter[3]無效，若為0， 設置parameter[3]
                                        //parameter[3] 設定值
                                    }
                                });

                                try {//插頭小夜燈switch mode
                                    res = zwaveService.setConfiguration(selectNode,7,1,0,1);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("setConfiguration 小夜燈On");
                                        //parameter[0] 說明書上給定的參數index
                                        //parameter[1] 說明書上給定的參數大小
                                        //parameter[2] 預設值 1，parameter[3]無效，若為0， 設置parameter[3]
                                        //parameter[3] 設定值
                                    }
                                });
                                try {//插頭小夜燈night mode
                                    res = zwaveService.setConfiguration(selectNode,7,1,0,2);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("setBinarySwitchState off");
                                    }
                                });
                                res = zwaveService.setBinarySwitchState(DeviceInfo.devType, selectNode, 0x00);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("setBinarySwitchState on");
                                    }
                                });
                                res = zwaveService.setBinarySwitchState(DeviceInfo.devType, selectNode, 0xFF);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("resetMeter");
                                    }
                                });
                                res = zwaveService.resetMeter(DeviceInfo.devType, selectNode);
                                showRes(res);lockApi();

                                //=================================================================== get  ===================================================================

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast toast = Toast.makeText(MainActivity.this, "回報類 API 測試", Toast.LENGTH_LONG);
                                        toast.show();
                                    }
                                });
                                lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getBasic");
                                    }
                                });
                                showText = true;
                                res = zwaveService.getBasic(DeviceInfo.devType, selectNode);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getConfiguration");
                                        //parameter[0] 模式說明，當值0時，一次唯讀取一個指定的paramNumber，此時，rangeStart與rangeEnd不起作用，可傳0值；
                                        //             值為1時，讀取一段從rangeStart-rangeEnd之間的所有屬性值。
                                        //param 對照說明書
                                        //parameter[2] rangeStart
                                        //parameter[3] rangeEnd
                                    }
                                });
                                showText = true;
                                //插頭 開關
                                res = zwaveService.getConfiguration(selectNode,0,5,0,0);
                                showRes(res);lockApi();


                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getPowerLevel");
                                    }
                                });
                                showText = true;
                                res = zwaveService.getPowerLevel(selectNode);
                                showRes(res);lockApi();



                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getBinarySwitchState");
                                    }
                                });
                                showText = true;
                                res = zwaveService.getBinarySwitchState(DeviceInfo.devType, selectNode);
                                showRes(res);lockApi();



                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getGroupInfo");
                                        //parameter[0] 為COMMAND_CLASS_ASSOCIATION_GRP_INFO獲取的值，該資訊可以在node info中查看到
                                        //parameter[1] 則表示聯動主設備的某個具體的channel(如果設備支援multi-channel)，否則預設為0
                                    }
                                });
                                showText = true;
                                res = zwaveService.getGroupInfo(DeviceInfo.devType, selectNode, 1, 0);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getMaxSupportedGroups 最大支援數");
                                        //parameter[0]最大支持的group數量，如果endpointId為0，則表示整個設備最大支援group數
                                    }
                                });
                                showText = true;
                                res = zwaveService.getMaxSupportedGroups(selectNode, 0);
                                showRes(res);lockApi();


                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getSpecificGroup 最大支援數");
                                        //parameter[0]最大支持的group數量，如果endpointId為0，則表示整個設備最大支援group數。
                                    }
                                });
                                showText = true;
                                res = zwaveService.getSpecificGroup(selectNode, 0);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getMeter");
                                    }
                                });
                                showText = true;
                                res = zwaveService.getMeter(DeviceInfo.devType,selectNode,0x00); //KWh
                                //res = zwaveService.getMeter(Const.zwaveType,selectNode,0x02); //W
                                //res = zwaveService.getMeter(Const.zwaveType,selectNode,0x04); //V
                                //res = zwaveService.getMeter(Const.zwaveType,selectNode,0x05); //I
                                //res = zwaveService.getMeter(Const.zwaveType,selectNode,0x06); //PF
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getMeterSupported");
                                    }
                                });
                                showText = true;
                                res = zwaveService.getMeterSupported(selectNode);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("End !!!!!!!!!!");
                                        txAllMsg.setText("");
                                    }
                                });

                            }
                        }){}.start();
                        break;
                    case R.id.btnBulb:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                //================================================== set ==================================================

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        txAllMsg.setText("");
                                    }
                                });

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("setBasic off");
                                    }
                                });
                                res = zwaveService.setBasic(DeviceInfo.devType, selectNode, 0);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("setBasic on");
                                    }
                                });
                                res = zwaveService.setBasic(DeviceInfo.devType, selectNode, 255);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("setSwitchMultiLevel 亮度5");
                                    }
                                });
                                res = zwaveService.setSwitchMultiLevel(DeviceInfo.devType, selectNode, 5, 1);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("setSwitchMultiLevel 亮度30");
                                    }
                                });
                                res = zwaveService.setSwitchMultiLevel(DeviceInfo.devType, selectNode, 30, 1);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("setSwitchMultiLevel 亮度最大");
                                    }
                                });
                                res = zwaveService.setSwitchMultiLevel(DeviceInfo.devType, selectNode, 99, 1);
                                showRes(res);lockApi();


                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("startStopSwitchLevelChange 變暗");
                                        //parameter[0] 1-99 起始亮度
                                        //parameter[1] 變化時間
                                        //parameter[2] 0 變亮 1 變暗  3 不變
                                        //parameter[3] 0 變亮 1 變暗  3 不變
                                        //parameter[4] 1-99 亮度
                                    }
                                });
                                res = zwaveService.startStopSwitchLevelChange(selectNode, 99, 5,1, 0,0);
                                showRes(res);lockApi();
                                res = zwaveService.startStopSwitchLevelChange(selectNode, 99, 0,1, 0,0);


                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("startStopSwitchLevelChange 變亮");
                                        //parameter[0] 1-99 起始亮度
                                        //parameter[1] 變化時間
                                        //parameter[2] 0 變亮 1 變暗  3 不變
                                        //parameter[3] 0 變亮 1 變暗  3 不變
                                        //parameter[4] 1-99 亮度
                                    }
                                });
                                res = zwaveService.startStopSwitchLevelChange(selectNode, 1, 5,0, 0,0);
                                showRes(res);lockApi();
                                res = zwaveService.startStopSwitchLevelChange(selectNode, 1, 0,0, 0,0);


                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("setConfiguration");
                                        //parameter[0] 說明書上給定的參數index
                                        //parameter[1] 說明書上給定的參數大小
                                        //parameter[2] 預設值 1，parameter[3]無效，若為0， 設置parameter[3]
                                        //parameter[3] 設定值
                                    }
                                });

                                try {                            //bulb 七彩迷紅燈
                                    res = zwaveService.setConfiguration(selectNode,37,4,0,0x09630000);//disable send humidity
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("setSwitchColor red");
                                        //0-8 "Warm Write", "Cold Write", "Red","Green", "Blue","Amber", "Cyan", "Purple", "Indexed Color"
                                        //parameter[0] RED顏色值 範圍0-255
                                        //parameter[1] GREEN顏色值 範圍0-255
                                        //parameter[2] BLUE顏色值  範圍0-255
                                    }
                                });
                                res = zwaveService.setSwitchColor(DeviceInfo.devType, selectNode, 2, 255);  //RED
                                res = zwaveService.setSwitchColor(DeviceInfo.devType, selectNode, 3, 0);  //GREEN
                                res = zwaveService.setSwitchColor(DeviceInfo.devType, selectNode, 4, 0);  //BLUE
                                res = zwaveService.setSwitchColor(DeviceInfo.devType,selectNode,0,0);
                                res = zwaveService.setSwitchColor(DeviceInfo.devType,selectNode,1,0);
                                showRes(res);lockApi();


                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("setSwitchColor green");
                                        //0-8 "Warm Write", "Cold Write", "Red","Green", "Blue","Amber", "Cyan", "Purple", "Indexed Color"
                                        //parameter[0] RED顏色值 範圍0-255
                                        //parameter[1] GREEN顏色值 範圍0-255
                                        //parameter[2] BLUE顏色值  範圍0-255
                                    }
                                });
                                res = zwaveService.setSwitchColor(DeviceInfo.devType, selectNode, 2, 0);  //RED
                                res = zwaveService.setSwitchColor(DeviceInfo.devType, selectNode, 3, 255);  //GREEN
                                res = zwaveService.setSwitchColor(DeviceInfo.devType, selectNode, 4, 0);  //BLUE
                                res = zwaveService.setSwitchColor(DeviceInfo.devType,selectNode,0,0);
                                res = zwaveService.setSwitchColor(DeviceInfo.devType,selectNode,1,0);
                                showRes(res);lockApi();


                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("setSwitchColor blue");
                                        //0-8 "Warm Write", "Cold Write", "Red","Green", "Blue","Amber", "Cyan", "Purple", "Indexed Color"
                                        //parameter[0] RED顏色值 範圍0-255
                                        //parameter[1] GREEN顏色值 範圍0-255
                                        //parameter[2] BLUE顏色值  範圍0-255
                                    }
                                });
                                res = zwaveService.setSwitchColor(DeviceInfo.devType, selectNode, 2, 0);  //RED
                                res = zwaveService.setSwitchColor(DeviceInfo.devType, selectNode, 3, 0);  //GREEN
                                res = zwaveService.setSwitchColor(DeviceInfo.devType, selectNode, 4, 255);  //BLUE
                                res = zwaveService.setSwitchColor(DeviceInfo.devType,selectNode,0,0);
                                res = zwaveService.setSwitchColor(DeviceInfo.devType,selectNode,1,0);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("startStopSwitchColorLevelChange 顏色調暗");
                                        //parameter[0] 0 調亮 1調暗
                                        //parameter[1] 0/1 是否忽略初始值
                                        //parameter[2] 0-8 "Warm Write", "Cold Write", "Red","Green", "Blue","Amber", "Cyan", "Purple", "Indexed Color"
                                        //parameter[3] 初始值
                                    }
                                });
                                res = zwaveService.startStopColorLevelChange(DeviceInfo.devType, selectNode, 1,0, 4, 0);
                                showRes(res);lockApi();
                                res = zwaveService.startStopColorLevelChange(DeviceInfo.devType, selectNode, 1,0, 0, 0);

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("startStopSwitchColorLevelChange 顏色調亮");
                                        //parameter[0] 0 調亮 1調暗
                                        //parameter[1] 0/1 是否忽略初始值
                                        //parameter[2] 0-8 "Warm Write", "Cold Write", "Red","Green", "Blue","Amber", "Cyan", "Purple", "Indexed Color"
                                        //parameter[3] 初始值
                                    }
                                });
                                res = zwaveService.startStopColorLevelChange(DeviceInfo.devType, selectNode, 0,0, 4, 0);
                                showRes(res);lockApi();
                                res = zwaveService.startStopColorLevelChange(DeviceInfo.devType, selectNode, 0,0, 0, 0);

                                //=================================================================== get  ===================================================================

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast toast = Toast.makeText(MainActivity.this, "回報類 API 測試", Toast.LENGTH_LONG);
                                        toast.show();
                                    }
                                });
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getBasic");
                                    }
                                });
                                showText = true;
                                res = zwaveService.getBasic(DeviceInfo.devType, selectNode);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getSwitchMultiLevel");
                                    }
                                });
                                showText = true;
                                res = zwaveService.getSwitchMultiLevel(DeviceInfo.devType, selectNode);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getConfiguration");
                                        //parameter[0] 模式說明，當值0時，一次唯讀取一個指定的paramNumber，此時，rangeStart與rangeEnd不起作用，可傳0值；
                                        //             值為1時，讀取一段從rangeStart-rangeEnd之間的所有屬性值。
                                        //parameter[1] 說明書上給定的參數index
                                        //parameter[2] rangeStart
                                        //parameter[3] rangeEnd
                                    }
                                });
                                showText = true;
                                //燈 顏色
                                res = zwaveService.getConfiguration(selectNode,0,33,0,0);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getPowerLevel");
                                    }
                                });
                                showText = true;
                                res = zwaveService.getPowerLevel(selectNode);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getGroupInfo");
                                        //parameter[0] 為COMMAND_CLASS_ASSOCIATION_GRP_INFO獲取的值，該資訊可以在node info中查看到
                                        //parameter[1] 則表示聯動主設備的某個具體的channel(如果設備支援multi-channel)，否則預設為0
                                    }
                                });
                                showText = true;
                                res = zwaveService.getGroupInfo(DeviceInfo.devType, selectNode, 1, 0);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getMaxSupportedGroups 最大支援數");
                                        //parameter[0]最大支持的group數量，如果endpointId為0，則表示整個設備最大支援group數
                                    }
                                });
                                showText = true;
                                res = zwaveService.getMaxSupportedGroups(selectNode, 0);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getSpecificGroup 最大支援數");
                                        //parameter[0]最大支持的group數量，如果endpointId為0，則表示整個設備最大支援group數。
                                    }
                                });
                                showText = true;
                                res = zwaveService.getSpecificGroup(selectNode, 0);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getSupportedSwitchColor");
                                    }
                                });
                                showText = true;
                                res = zwaveService.getSupportColor(DeviceInfo.devType, selectNode);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getSwitchColor RED");
                                        //0-8 "Warm Write", "Cold Write", "Red","Green", "Blue","Amber", "Cyan", "Purple", "Indexed Color"
                                    }
                                });
                                showText = true;
                                res = zwaveService.getSwitchColor(DeviceInfo.devType, selectNode, 2);  //RED
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getSwitchColor GREEN");
                                        //0-8 "Warm Write", "Cold Write", "Red","Green", "Blue","Amber", "Cyan", "Purple", "Indexed Color"
                                    }
                                });
                                showText = true;
                                res = zwaveService.getSwitchColor(DeviceInfo.devType, selectNode, 3);  //GREEN
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getSwitchColor BLUE");
                                        //0-8 "Warm Write", "Cold Write", "Red","Green", "Blue","Amber", "Cyan", "Purple", "Indexed Color"
                                    }
                                });
                                showText = true;
                                res = zwaveService.getSwitchColor(DeviceInfo.devType, selectNode, 4);  //BLUE
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("End !!!!!!!!!!");
                                        txAllMsg.setText("");
                                    }
                                });

                            }
                        }){}.start();
                        break;
                    case R.id.btnSensor:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                //================================================== set ==================================================

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        txAllMsg.setText("");
                                    }
                                });

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("setConfiguration");
                                        //parameter[0] 說明書上給定的參數index
                                        //parameter[1] 說明書上給定的參數大小
                                        //parameter[2] 預設值 1，parameter[3]無效，若為0， 設置parameter[3]
                                        //parameter[3] 設定值
                                    }
                                });

                                //關閉回報sensor
                                try {
                                    res = zwaveService.setConfiguration(selectNode,10,1,0,0);
                                    res = zwaveService.setConfiguration(selectNode,11,1,0,0);
                                    res = zwaveService.setConfiguration(selectNode,12,1,0,0);
                                    res = zwaveService.setConfiguration(selectNode,13,1,0,0);
                                    res = zwaveService.setConfiguration(selectNode,20,1,0,0);
                                    res = zwaveService.setConfiguration(selectNode,21,1,0,0);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("setNotification");
                                        //parameter[0] 參考文件26頁
                                        //parameter[1] 參考文件26頁
                                    }
                                });
                                //關閉回報
                                //Water
                                res = zwaveService.setNotification(selectNode,  0x05, 0x01);
                                //Motion
                                res = zwaveService.setNotification(selectNode,  0x07, 0x01);
                                //Door/Window
                                res = zwaveService.setNotification(selectNode,  0x06, 0x01);
                                //Door/Window
                                res = zwaveService.setNotification(selectNode,  0x06, 0x01);
                                //SMOKE
                                res = zwaveService.setNotification(selectNode,  0x01, 0x01);
                                //Motion
                                res = zwaveService.setNotification(selectNode,  0x07, 0x01);
                                showRes(res);lockApi();

                                //================================================== get ==================================================

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast toast = Toast.makeText(MainActivity.this, "回報類 API 測試", Toast.LENGTH_LONG);
                                        toast.show();
                                    }
                                });
                                lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getConfiguration");
                                        //parameter[0] 模式說明，當值0時，一次唯讀取一個指定的paramNumber，此時，rangeStart與rangeEnd不起作用，可傳0值；
                                        //             值為1時，讀取一段從rangeStart-rangeEnd之間的所有屬性值。
                                        //parameter[1] 說明書上給定的參數index
                                        //parameter[2] rangeStart
                                        //parameter[3] rangeEnd
                                    }
                                });
                                showText = true;
                                //DOOR 門開關
                                res = zwaveService.getConfiguration(selectNode,0,11,0,0);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getPowerLevel");
                                    }
                                });
                                showText = true;
                                res = zwaveService.getPowerLevel(selectNode);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getSensorBinary water");
                                        //parameter[0] SNESOR種類，參考文件19頁
                                    }
                                });
                                showText = true;
                                res = zwaveService.getSensorBinary(selectNode, 0x06); //water
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getSensorBinary tamper");
                                        //parameter[0] SNESOR種類，參考文件19頁
                                    }
                                });
                                showText = true;
                                res = zwaveService.getSensorBinary(selectNode, 0x08);  //08tamper
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getSensorBinary door/window");
                                        //parameter[0] SNESOR種類，參考文件19頁
                                    }
                                });
                                showText = true;
                                res = zwaveService.getSensorBinary(selectNode, 0x0A); //0A door/window
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getSensorBinarySupportedSensor");
                                    }
                                });
                                showText = true;
                                res = zwaveService.GetSensorBinarySupportedSensor(selectNode);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getGroupInfo");
                                        //parameter[0] 為COMMAND_CLASS_ASSOCIATION_GRP_INFO獲取的值，該資訊可以在node info中查看到
                                        //parameter[1] 則表示聯動主設備的某個具體的channel(如果設備支援multi-channel)，否則預設為0
                                    }
                                });
                                showText = true;
                                res = zwaveService.getGroupInfo(DeviceInfo.devType, selectNode, 1, 0);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getMaxSupportedGroups 最大支援數");
                                        //parameter[0]最大支持的group數量，如果endpointId為0，則表示整個設備最大支援group數
                                    }
                                });
                                res = zwaveService.getMaxSupportedGroups(selectNode, 0);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getSpecificGroup 最大支援數");
                                        //parameter[0]最大支持的group數量，如果endpointId為0，則表示整個設備最大支援group數。
                                    }
                                });
                                showText = true;
                                res = zwaveService.getSpecificGroup(selectNode, 0);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getNotification SMOKE");
                                    }
                                });
                                showText = true;
                                //SMOKE
                                res = zwaveService.getNotification(selectNode, 0x00, 0x01, 0x00);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getNotification Heat Alarm");
                                    }
                                });
                                showText = true;
                                //Heat Alarm
                                res = zwaveService.getNotification(selectNode, 0x00, 0x04, 0x00);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getNotification Water");
                                    }
                                });
                                showText = true;
                                //Water
                                res = zwaveService.getNotification(selectNode, 0x00, 0x05, 0x00);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getNotification Door/Window");
                                    }
                                });
                                showText = true;
                                //Door/Window Access Control
                                res = zwaveService.getNotification(selectNode, 0x00, 0x06, 0x00);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getNotification Home Security");
                                    }
                                });
                                showText = true;
                                //Home Security
                                res = zwaveService.getNotification(selectNode, 0x00, 0x07, 0x00);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getNotification Power Management");
                                        //parameter[0] 參考文件27頁
                                        //parameter[1] 參考文件27頁
                                    }
                                });
                                showText = true;
                                //Power Management
                                res = zwaveService.getNotification(selectNode, 0x00, 0x08, 0x00);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getNotification Emergency Alarm");
                                    }
                                });
                                showText = true;
                                //Emergency Alarm
                                res = zwaveService.getNotification(selectNode, 0x00, 0x0A, 0x00);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getNotification Water Valve");
                                    }
                                });
                                showText = true;
                                //Water Valve
                                res = zwaveService.getNotification(selectNode, 0x00, 0x0F, 0x00);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getNotification Weather Alarm");
                                    }
                                });
                                showText = true;
                                //Weather Alarm
                                res = zwaveService.getNotification(selectNode, 0x00, 0x10, 0x00);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getSupportedNotification");
                                    }
                                });
                                showText = true;
                                res = zwaveService.getSupportedNotification(selectNode);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getSupportedEventNotification Water");
                                    }
                                });
                                showText = true;
                                res = zwaveService.getSupportedEventNotification(selectNode, 0x05);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getSupportedEventNotification Door/Window");
                                    }
                                });
                                showText = true;
                                res = zwaveService.getSupportedEventNotification(selectNode, 0x06);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("getSupportedEventNotification Home Security");
                                    }
                                });
                                showText = true;
                                res = zwaveService.getSupportedEventNotification(selectNode, 0x07);
                                showRes(res);lockApi();

                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvtest.setText("End !!!!!!!!!!");
                                        txAllMsg.setText("");
                                    }
                                });
                            }
                        }){}.start();
                        break;
                }
            }
        }).start();
    }

    private void showRes(final int result) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast;
                if(result == 0) {
                    toast = Toast.makeText(MainActivity.this, "API Correct ! ", Toast.LENGTH_LONG);
                    toast.show();
                } else if (result == 1) {
                    toast = Toast.makeText(MainActivity.this, "觸發裝置 ! ", Toast.LENGTH_LONG);
                    toast.show();
                } else if (result == -23) {
                    toast = Toast.makeText(MainActivity.this, "not support this API ! ", Toast.LENGTH_LONG);
                    toast.show();
                } else if (result == -1) {
                    toast = Toast.makeText(MainActivity.this, "API Fail ! ", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });
    }

    private void getNotificationInfo() {
        JSONObject jsonObject ,jsonObject2, jsonObject3, jsonObject4, jsonObject5, jsonObject6= null;
        try {
            //Log.d(LOG_TAG,"gino getSpecifyDeviceInfo result " + DeviceInfo.result);
            jsonObject = new JSONObject(DeviceInfo.result);
            JSONArray deviceList = jsonObject.getJSONArray("Detialed Node Info");
            for(int i = 0; i < deviceList.length(); i++) {
                jsonObject2 = deviceList.getJSONObject(i);
                JSONArray EndPointList = jsonObject2.getJSONArray("EndPoint List");
                //Log.d(LOG_TAG,"gino get EndPointList result " + EndPointList);

                for (int j = 0; j < EndPointList.length(); j++) {
                    jsonObject3 = EndPointList.getJSONObject(j);
                    JSONArray InterfaceList = jsonObject3.getJSONArray("Interface List");
                    //Log.d(LOG_TAG,"gino get InterfaceList result " + InterfaceList);

                    for (int k = 0; k < InterfaceList.length(); k++) {
                        jsonObject4 = InterfaceList.getJSONObject(k);
                        String cmd = jsonObject4.optString("Interface Class");
                        if (cmd.equals("COMMAND_CLASS_NOTIFICATION")) {
                            JSONArray NotificationInfo = jsonObject4.getJSONArray("Notification Info");
                            for (int l = 0; l < NotificationInfo.length(); l++) {
                                jsonObject5 = NotificationInfo.getJSONObject(l);
                                for (int m = 0; m < jsonObject5.length(); m++) {
                                    for(int n = 1 ; n <= 5; n++) {
                                        if(jsonObject5.optJSONObject("Related events " + n) == null) {
                                            Log.d(LOG_TAG, "gino get event result null");
                                        } else {
                                            jsonObject6 = jsonObject5.optJSONObject("Related events " + n);
                                            String event = jsonObject6.optString("event[1]");
                                            String event2 = jsonObject6.optString("event[2]");
                                            String event3 = jsonObject6.optString("event[3]");
                                            Log.d(LOG_TAG, "gino get event result " + event + event2 + event3);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getNodeId() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                zwaveService.getDeviceList("ALL");
                JSONObject jsonObject ,jsonObject2 = null;
                try {
                    //Log.d(LOG_TAG,"gino get node result " + DeviceInfo.result);
                    jsonObject = new JSONObject(DeviceInfo.result);
                    JSONArray deviceList = jsonObject.getJSONArray("deviceList");
                    for(int i = 0; i < deviceList.length(); i++) {
                        jsonObject2 = deviceList.getJSONObject(i);
                        int nodeId = Integer.parseInt(jsonObject2.optString("nodeId"));
                        //Log.d(LOG_TAG,"gino node id " + nodeId);
                        if(nodeId != 1)
                            nodeIdArr.add(nodeId);
                    }
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayAdapter<Integer> devList = new ArrayAdapter<Integer>(MainActivity.this,
                                    android.R.layout.simple_spinner_dropdown_item,
                                    convertIntegers(nodeIdArr));

                            spNodeIdList.setAdapter(devList);
                            spNodeIdList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    selectNode = Integer.valueOf(spNodeIdList.getSelectedItem().toString());
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }

                            });
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void inputReqKey() {
        new Thread(){
            @Override
            public void run(){
                Log.d(LOG_TAG,"gino req button");
                DeviceInfo.reqKey = Integer.parseInt(editText.getText().toString());
                DeviceInfo.reqFlag = true;
            }
        }.start();
    }


    // bind service with zwave control service
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i(LOG_TAG,"onServiceConnected....");
            zwaveService = ((ZwaveControlService.MyBinder)iBinder).getService();
            //register mCallback
            zwaveService = ((ZwaveControlService.MyBinder)iBinder).getService();
            //register mCallback
            if (zwaveService != null) {
                zwaveService.register(mCallback);
                zwaveService.register(mReqCallBacks);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            zwaveService = null;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        zwaveService.unregister(mCallback);
        zwaveService.unregister(mReqCallBacks);

        if (usbReceiver != null)
            unregisterReceiver(usbReceiver);
        try {
            this.unbindService(conn);
        } catch (Exception e) {
            Log.d(LOG_TAG, e.toString());
        }
    }

    //callback
    public ZwaveControlService.zwaveCallBack mCallback;
    {
        mCallback = new ZwaveControlService.zwaveCallBack() {

            //監聽關鍵字回報 從ZwaveControlService.jave zwaveCallBack -> zwcontrol_api.c "MessageType"

            @Override
            public void zwaveControlResultCallBack(String className, String result) {

                //for mqtt
                while(DeviceInfo.mqttFlag) {
                    try {
                        Log.d(LOG_TAG,"wait for mqtt finish !!!!!!!!!!!!!!!!!!!!!!!!");
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                DeviceInfo.className = className;
                DeviceInfo.result = result;

                if(DeviceInfo.result.length() > 200) {
                    for(int i=0;i<DeviceInfo.result.length();i+=200){
                        if(i+4000<DeviceInfo.result.length())
                            Log.i(LOG_TAG, "class name = [" + DeviceInfo.className + "] | result = " + DeviceInfo.result.substring(i, i+200));
                        else
                            Log.i(LOG_TAG, "class name = [" + DeviceInfo.className + "] | result = " + DeviceInfo.result.substring(i, DeviceInfo.result.length()));
                    }
                } else {
                    Log.i(LOG_TAG, "class name = [" + DeviceInfo.className + "] | result = " + DeviceInfo.result);
                }

                if(className.equals("Sensor Info Report") || className.equals("Node Battery Value") || className.equals("Notification Get Information")) {
                    DeviceInfo.sensorClassName = className;
                    DeviceInfo.sensorResult = result;
                } else {
                    DeviceInfo.className = className;
                    DeviceInfo.result = result;
                }

                if (className.equals("addDevice") || className.equals("removeDevice")) {

                    addRemoveDevice(result);
                    showText = true;


                } else if (className.equals("All Node Info Report")) {

                    //Log.d(LOG_TAG,result);

                } else if (className.equals("reNameDevice")) {

                    addRemoveDevice(result);
                    showText = true;

                } else if(className.equals("Central Scene Notification")) {
                    showText = true;
                } else if(className.equals("Group Info Report")) {
                    showText = true;
                } else if(className.equals("Replace Failed Node")){
                    showText = true;
                } else if (className.equals("Controller Reset Status")) {
                    showText = true;
                } else if (result.contains("Smart Start Protocol Started")) {
                    Log.d(LOG_TAG,"DeviceInfo.smartStartFlag = true");
                    DeviceInfo.smartStartFlag = true;
                } else if (className.contains("Network IMA Info Report")) {
                    showText = true;
                } else if (className.contains("Network Health Check")) {
                    showText = true;
                } else if (className.contains("Remove Failed Node")) {
                    showText = true;
                    if(result.contains("Success")) {
                        nodeIdArr.remove(Integer.valueOf(selectNode));
                    }
                }

                DeviceInfo.mqttFlag = true;
                if(showText) {
                    mHandler.sendEmptyMessage(2002);
                }
            }
        };
    }

    public ZwaveControlService.zwaveControlReq_CallBack mReqCallBacks;
    {
        mReqCallBacks = new ZwaveControlService.zwaveControlReq_CallBack() {
            @Override
            public void zwaveControlReqResultCallBack(String className, String result) {
                Log.i(LOG_TAG, "class name = [" + className + "]| result = " + result);
                DeviceInfo.className = className;
                DeviceInfo.result = result;
                if (result.contains("Grant Keys Msg")) {


                    String[] grantTmp = result.split(":");
                    //Log.d(LOG_TAG,"Grant Keys number : " +grantTmp[2]);
                    if(grantTmp[2].contains("135")) {
                        DeviceInfo.grantKeyNumber = "135";
                    } else if (grantTmp[2].contains("134")) {
                        DeviceInfo.grantKeyNumber = "134";
                    } else if (grantTmp[2].contains("133")) {
                        DeviceInfo.grantKeyNumber = "133";
                    } else if (grantTmp[2].contains("132")) {
                        DeviceInfo.grantKeyNumber = "132";
                    } else if (grantTmp[2].contains("131")) {
                        DeviceInfo.grantKeyNumber = "131";
                    } else if (grantTmp[2].contains("130")) {
                        DeviceInfo.grantKeyNumber = "130";
                    } else if (grantTmp[2].contains("129")) {
                        DeviceInfo.grantKeyNumber = "129";
                    } else if (grantTmp[2].contains("128")) {
                        DeviceInfo.grantKeyNumber = "128";
                    } else if (grantTmp[2].contains("7")) {
                        DeviceInfo.grantKeyNumber = "7";
                    } else if (grantTmp[2].contains("6")) {
                        DeviceInfo.grantKeyNumber = "6";
                    } else if (grantTmp[2].contains("5")) {
                        DeviceInfo.grantKeyNumber = "5";
                    } else if (grantTmp[2].contains("4")) {
                        DeviceInfo.grantKeyNumber = "4";
                    } else if (grantTmp[2].contains("3")) {
                        DeviceInfo.grantKeyNumber = "3";
                    } else if (grantTmp[2].contains("2")) {
                        DeviceInfo.grantKeyNumber = "2";
                    } else if (grantTmp[2].contains("1")) {
                        DeviceInfo.grantKeyNumber = "1";
                    }
                    //DeviceInfo.reqString = "Grant";
                    showText = true;

                    reqCallBack = true;
                } else if (result.contains("PIN Requested Msg")) {
                    //DeviceInfo.reqKey = 11394;
                    //DeviceInfo.reqString = "PIN";
                    showText = true;
                    reqCallBack = true;
                } else if (result.contains("Client Side Au Msg")) {
                    //DeviceInfo.reqString = "Au";
                    showText = true;
                    reqCallBack = true;
                }

                if(showText) {
                    mHandler.sendEmptyMessage(2002);
                }
            }
        };
    }

    private void lockApi(){
                while (lock){
                    Log.d(LOG_TAG,"被我卡住了 顆顆");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                lock = true;
    }

    private void addRemoveDevice(String result) {
        final String addRemoveResult = result;
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (addRemoveResult.contains("MessageType")){
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(addRemoveResult);
                        String messageType = jsonObject.optString("MessageType");
                        String status = jsonObject.optString("Status");
                        if ("Node Add Status".equals(messageType)) {
                            if ("Success".equals(status)) {
                                String tmpNode = jsonObject.optString("Nodeid");
                                Log.d(LOG_TAG,"gino Nodeid add = " + tmpNode );

                                nodeIdArr.add(Integer.valueOf(tmpNode));
                                updateSpNodeIdList();

                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                //txAllMsg.setText("Success");
                                //res = zwaveService.getDeviceInfo();
                            } else if("Learn Ready".equals(status)){
                                //txAllMsg.setText("Please press the trigger button of the device");
                            }else{
                                //txAllMsg.setText(status);
                            }
                        } else if  ("Node Remove Status".equals(messageType)) {
                            if ("Success".equals(status)) {
                                String tmpNode = jsonObject.optString("Nodeid");
                                Log.d(LOG_TAG, "gino Nodeid remove = " + tmpNode);
                                nodeIdArr.remove(Integer.valueOf(tmpNode));
                                updateSpNodeIdList();
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();

                    }

                }else{

                    String addRemoveMode = "";
                    if (addRemoveResult.contains("addDevice")){
                        addRemoveMode = "addDevice";
                    }else if (addRemoveResult.contains("removeDevice")){
                        addRemoveMode = "removeDevice";
                    }
                    String[] tokens = addRemoveResult.split(":");
                    if (tokens.length<3){
                        Log.i(LOG_TAG,addRemoveMode+" : wrong format "+addRemoveResult);
                    } else {
                        String tHomeId = tokens[1];
                        String tNodeId = tokens[2];
                        Log.i(LOG_TAG,addRemoveMode+" HomeId = "+tHomeId+" | NodeId = "+tNodeId);
                        //txAllMsg.setText(addRemoveMode+" Success " + " | NodeId = "+tNodeId);

                    }
                }
            }
        });
    }

    private void updateSpNodeIdList() {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayAdapter<Integer> devList = new ArrayAdapter<Integer>(MainActivity.this,
                        android.R.layout.simple_spinner_dropdown_item,
                        convertIntegers(nodeIdArr));

                spNodeIdList.setAdapter(devList);
                spNodeIdList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectNode = Integer.valueOf(spNodeIdList.getSelectedItem().toString());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }

                });
            }
        });
    }

    //add & remove node of device to spinner
    private static Integer[] convertIntegers(List<Integer> integers)
    {
        Integer[] ret = new Integer[integers.size()];
        for (int i=0; i < ret.length; i++)
        {
            ret[i] = integers.get(i).intValue();
        }
        return ret;
    }
}

