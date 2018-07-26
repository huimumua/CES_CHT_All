package com.askey.firefly.zwave.control.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothClass;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.askey.firefly.zwave.control.R;
import com.askey.firefly.zwave.control.dao.ZwaveDevice;
import com.askey.firefly.zwave.control.dao.ZwaveDeviceManager;
import com.askey.firefly.zwave.control.service.MQTTBroker;
import com.askey.firefly.zwave.control.service.ZwaveControlService;
import com.askey.firefly.zwave.control.thirdparty.usbserial.UsbSerial;
import com.askey.firefly.zwave.control.utils.Const;
import com.askey.firefly.zwave.control.utils.DeviceInfo;
import com.askey.firefly.zwave.control.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by chiapin on 2017/9/7.
 */

public class WelcomeActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static String LOG_TAG = WelcomeActivity.class.getSimpleName();

    private AlertDialog alertDialog;
    private Timer timer = new Timer(true);

    private ZwaveControlService zwaveService;
    private ZwaveDeviceManager zwDevManager;
    private BroadcastReceiver usbReceiver = null;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";


    private ArrayList<Integer> nodeIdArr = new ArrayList<>();
    private ArrayList<String> provisionListArr = new ArrayList<>();
    private Button btnAdd,btnRemove,btnButton,btnAddPorList,btnRmProList,btnEditProvision,btnPassive,btnLearn;
    private Spinner spNodeIdList,spApiList,spProvisionList;
    private TextView txDsk,txAllMsg;
    private EditText editDsk,editSetApiValue;
    private CheckBox cb1,cb2,cb3;
    private String inputDsk;
    private int selectNode;
    private String selectProvisionList;
    private boolean getProvisionListFlag = false;
    private boolean getProvisionNodeFlag = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        new UsbSerial(this);
        setContentView(R.layout.activity_welcome);
        mContext = this;

        Intent MqttIntent = new Intent(WelcomeActivity.this, MQTTBroker.class);
        if (!isServiceRunning(mContext,MQTTBroker.class)){
            startService(MqttIntent);
        }

        showProgressDialog(mContext, "Initializing，Open Zwave Controller...");

        zwDevManager = ZwaveDeviceManager.getInstance(this);

        // bind service
        Intent serviceIntent = new Intent(this, ZwaveControlService.class);
        this.bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);
        this.bindService(serviceIntent, req, Context.BIND_AUTO_CREATE);

        new Thread(checkInitStatus).start();
        new Thread(activityZwaveControlService).start();

        initBtn();
    }

    private void initBtn() {
        Log.i(LOG_TAG, "initBtn");

        editDsk = (EditText) findViewById(R.id.editText);
        editSetApiValue = (EditText) findViewById(R.id.editSetApiValue);

        btnAdd  = (Button) findViewById(R.id.btnAdd);
        btnRemove  = (Button) findViewById(R.id.btnRemove);
        btnLearn = (Button) findViewById(R.id.btnLearn);
        btnButton = (Button) findViewById(R.id.btnButton);
        btnAddPorList = (Button) findViewById(R.id.btnaddProList);
        btnRmProList = (Button) findViewById(R.id.btnrmProList);
        btnEditProvision = (Button) findViewById(R.id.btnEditProvision);
        btnPassive = (Button) findViewById(R.id.btnPassive);

        cb1 = (CheckBox) findViewById(R.id.cb1);
        cb2 = (CheckBox) findViewById(R.id.cb2);
        cb3 = (CheckBox) findViewById(R.id.cb3);

        txDsk = (TextView) findViewById(R.id.txDsk);
        txAllMsg = (TextView) findViewById(R.id.txAllMsg);

        spNodeIdList = (Spinner) findViewById(R.id.nodeIdList);
        spApiList = (Spinner) findViewById(R.id.spApiList);
        spProvisionList = (Spinner) findViewById(R.id.provisionList);

        btnAdd.setOnClickListener(this);
        btnRemove.setOnClickListener(this);
        btnLearn.setOnClickListener(this);
        btnButton.setOnClickListener(this);
        btnAddPorList.setOnClickListener(this);
        btnRmProList.setOnClickListener(this);
        btnEditProvision.setOnClickListener(this);
        btnPassive.setOnClickListener(this);

        cb1.setOnCheckedChangeListener(this);
        cb2.setOnCheckedChangeListener(this);
        cb3.setOnCheckedChangeListener(this);

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

        InputMethodManager imm = (InputMethodManager)getSystemService(WelcomeActivity.this.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editDsk, 0);

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

    // only execute one time
    private Runnable checkInitStatus = new Runnable() {
        @Override
        public void run() {

            while (!DeviceInfo.isMQTTInitFinish || !DeviceInfo.isOpenControllerFinish || !DeviceInfo.isZwaveInitFinish) {

                try {
                    //Log.i(LOG_TAG, "isOpenControllerFinish & isMQTTInitFinish true");
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            initSensorfunc();
            initZwave();

        }
    };

    //init sensor 類別裝置,當sensor裝置改變狀態會自動回報
    private void initSensorfunc() {

        List<ZwaveDevice> list = zwDevManager.queryZwaveDeviceList();

        for (int idx = 1; idx < list.size(); idx++) {

            int nodeId = list.get(idx).getNodeId();
            String devType = list.get(idx).getDevType();

            Log.i(LOG_TAG,"#"+nodeId+" | devType = "+devType);

            if (devType.equals("SENSOR")) {
                String devNodeInfo = list.get(idx).getNodeInfo();

                if (devNodeInfo.contains("COMMAND_CLASS_BATTERY")) {
                    Log.i(LOG_TAG, "BATTERY");
                    zwaveService.getDeviceBattery(Const.zwaveType,nodeId);
                }

                if (devNodeInfo.contains("COMMAND_CLASS_NOTIFICATION")) {
                    try {
                        JSONObject jsonObject = new JSONObject(devNodeInfo);
                        if (jsonObject.getString("Product id").equals("001F")) {
                            //Water
                            zwaveService.getSensorNotification(nodeId, 0x00, 0x05, 0x00);
                        } else if (jsonObject.getString("Product id").equals("000C")) {
                            //Motion
                            zwaveService.getSensorNotification(nodeId, 0x00, 0x07, 0x00);
                            //Door/Window
                            zwaveService.getSensorNotification(nodeId, 0x00, 0x06, 0x00);
                        } else if (jsonObject.getString("Product id").equals("0036")) {
                            //Door/Window
                            zwaveService.getSensorNotification(nodeId, 0x00, 0x06, 0x00);
                        } else if (jsonObject.getString("Product id").equals("001E")) {
                            //SMOKE
                            zwaveService.getSensorNotification(nodeId, 0x00, 0x01, 0x00);
                        } else if (jsonObject.getString("Product id").equals("0050")) {
                            //Motion
                            zwaveService.getSensorNotification(nodeId, 0x00, 0x07, 0x00);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (devNodeInfo.contains("COMMAND_CLASS_SENSOR_MULTILEVEL")) {
                    try {
                        zwaveService.getSensorMultiLevel(Const.zwaveType,nodeId);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                zwaveService.getMeterSupported(nodeId);
                zwaveService.GetSensorBinarySupportedSensor(nodeId);

            }
        }

    }

    private void initZwave() {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                timerCancel();
                //Intent intent = new Intent();
                //intent.setClass(mContext, HomeActivity.class);
                hideProgressDialog();                               //close progress dialog
                //mContext.startActivity(intent);
                //finish();
            }
        });
    }

    public static boolean isServiceRunning(Context context,Class<?> serviceClass){
        final ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            Log.d(LOG_TAG, String.format("Service:%s", runningServiceInfo.service.getClassName()));
            if (runningServiceInfo.service.getClassName().equals(serviceClass.getName())){
                Log.i(LOG_TAG,serviceClass.getName()+" is already running");
                return true;
            }
        }
        return false;
    }


    private Runnable activityZwaveControlService = new Runnable() {
        @Override
        public void run() {
            boolean circle = false;
            while (!circle) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                switch (DeviceInfo.getMqttPayload) {

                    case "addDevice":
                        Log.i(LOG_TAG, "deviceService.addDevice");
                        DeviceInfo.callResult = zwaveService.addDevice(DeviceInfo.devType);
                        if (DeviceInfo.callResult < 0) {
                            //Log.d(LOG_TAG, "deviceService.editNodeInfo true");
                            //Log.i(LOG_TAG, "addDevice : -17 !!!!!!!!!!!!!" + DeviceInfo.callResult);
                            DeviceInfo.resultToMqttBroker = "dongleBusy:addDevice:"+DeviceInfo.callResult;
                        }
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "removeDevice":
                        Log.i(LOG_TAG, "deviceService.removeDevice");
                        DeviceInfo.callResult = zwaveService.removeDevice(DeviceInfo.devType, 1);
                        if (DeviceInfo.callResult < 0) {
                            //Log.d(LOG_TAG, "deviceService.editNodeInfo true");
                            //Log.i(LOG_TAG, "removeDevice : -17 !!!!!!!!!!!!!");
                            DeviceInfo.resultToMqttBroker = "dongleBusy:removeDevice:"+DeviceInfo.callResult;
                        }
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "stopAddDevice":
                        Log.i(LOG_TAG, "deviceService.stopAddDevice");
                        DeviceInfo.callResult = zwaveService.stopAddDevice(DeviceInfo.devType);
                        if (DeviceInfo.callResult < 0) {
                            //Log.d(LOG_TAG, "deviceService.editNodeInfo true");
                            //Log.i(LOG_TAG, "stopAddDevice : -17 !!!!!!!!!!!!!");
                            DeviceInfo.resultToMqttBroker = "dongleBusy:stopAddDevice:"+DeviceInfo.callResult;
                        }
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "stopRemoveDevice":
                        Log.i(LOG_TAG, "deviceService.stopRemoveDevice");
                        DeviceInfo.callResult = zwaveService.stopRemoveDevice(DeviceInfo.devType);
                        if (DeviceInfo.callResult < 0) {
                            //Log.d(LOG_TAG, "deviceService.editNodeInfo true");
                            //Log.i(LOG_TAG, "stopRemoveDevice : -17 !!!!!!!!!!!!!");
                            DeviceInfo.resultToMqttBroker = "dongleBusy:stopRemoveDevice:"+DeviceInfo.callResult;
                        }
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "removeDeviceFromRoom":
                        Log.i(LOG_TAG, "deviceService.removeDeviceFromRoom");
                        zwaveService.removeDeviceFromRoom(DeviceInfo.mqttDeviceId);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getSecurity2CmdSupported": //public channel
                        Log.i(LOG_TAG, "deviceService.getSecurity2CmdSupported");
                        zwaveService.getSecurity2CmdSupported(DeviceInfo.mqttDeviceId);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getDeviceList": //public channel
                        Log.i(LOG_TAG, "deviceService.getDevices tRoom= " + DeviceInfo.room);
                        zwaveService.getDeviceList(DeviceInfo.room);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "editNodeInfo":
                        Log.d(LOG_TAG, "deviceService.editNodeInfo");
                        DeviceInfo.callResult = zwaveService.editNodeInfo("", DeviceInfo.mqttDeviceId, DeviceInfo.mqttString3, DeviceInfo.devType, DeviceInfo.mqttString4, DeviceInfo.mqttString, DeviceInfo.mqttString2);
                        Log.d(LOG_TAG, "deviceService.editNodeInfo : " + DeviceInfo.callResult);
                        if (DeviceInfo.callResult == 1) {
                            //Log.d(LOG_TAG, "deviceService.editNodeInfo true");
                            DeviceInfo.resultToMqttBroker = "editNodeInfoTrue";
                        } else {
                            //Log.d(LOG_TAG, "deviceService.editNodeInfo Fail");
                            DeviceInfo.resultToMqttBroker = "editNodeInfoFail";
                        }
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getRecentDeviceList": //public channel
                        Log.i(LOG_TAG, "deviceService.getRecentDeviceList");
                        zwaveService.getRecentDeviceList(DeviceInfo.mqttTmp);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "addRoom": //public channel
                        Log.i(LOG_TAG, "deviceService.addRoom");
                        zwaveService.addRoom(DeviceInfo.mqttString);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getRooms": //public channel
                        Log.i(LOG_TAG, "deviceService.getRooms");
                        zwaveService.getRooms();
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "editRoom": //public channel
                        Log.i(LOG_TAG, "deviceService.editRoom");
                        zwaveService.editRoom(DeviceInfo.mqttString, DeviceInfo.mqttString2);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "removeRoom": //public channel
                        Log.i(LOG_TAG, "deviceService.removeRoom");
                        zwaveService.removeRoom(DeviceInfo.mqttString);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getBasic":
                        Log.i(LOG_TAG, "deviceService.getBasic" + DeviceInfo.mqttDeviceId);
                        zwaveService.getBasic(DeviceInfo.devType, DeviceInfo.mqttDeviceId);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "setBasic":
                        Log.i(LOG_TAG, "deviceService.setBasic nodeId= " + DeviceInfo.mqttDeviceId + " value = " + DeviceInfo.mqttValue);
                        zwaveService.setBasic(DeviceInfo.devType, DeviceInfo.mqttDeviceId, DeviceInfo.mqttValue);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getSwitchMultilevel":
                        Log.i(LOG_TAG, "deviceService.getSwitchMultilevel" + DeviceInfo.mqttDeviceId);
                        zwaveService.getSwitchMultiLevel(DeviceInfo.devType, DeviceInfo.mqttDeviceId);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "setSwitchMultilevel":
                        Log.i(LOG_TAG, "deviceService.setSwitchMultilevel nodeId= " + DeviceInfo.mqttDeviceId + " value = " + DeviceInfo.mqttValue + "duration " + DeviceInfo.mqttTmp);
                        zwaveService.setSwitchMultiLevel(DeviceInfo.devType, DeviceInfo.mqttDeviceId, DeviceInfo.mqttValue, 1);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "setBrightness":
                        Log.i(LOG_TAG, "deviceService.setBrightness");
                        zwaveService.setSwitchMultiLevel(DeviceInfo.devType, DeviceInfo.mqttDeviceId, DeviceInfo.mqttValue, 1);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getBrightness":
                        Log.i(LOG_TAG, "deviceService.getBrightness" + DeviceInfo.mqttDeviceId);
                        zwaveService.getSwitchMultiLevel(DeviceInfo.devType, DeviceInfo.mqttDeviceId);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getSwitchColor":
                        Log.i(LOG_TAG, "deviceService.getSwitchColor" + DeviceInfo.mqttDeviceId + DeviceInfo.mqttTmp);
                        zwaveService.getSwitchColor(DeviceInfo.devType, DeviceInfo.mqttDeviceId, DeviceInfo.mqttTmp);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "setSwitchColor":
                        Log.i(LOG_TAG, "deviceService.setSwitchColor" + DeviceInfo.mqttDeviceId + DeviceInfo.mqttTmp + DeviceInfo.mqttTmp2);
                        zwaveService.setSwitchColor(DeviceInfo.devType, DeviceInfo.mqttDeviceId, DeviceInfo.mqttTmp, DeviceInfo.mqttTmp2);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "setLampColor":
                        Log.i(LOG_TAG, "deviceService.setLampColor" + DeviceInfo.mqttDeviceId + DeviceInfo.mqttTmp + DeviceInfo.mqttTmp2+DeviceInfo.mqttTmp3);
                        zwaveService.setLampColor(DeviceInfo.devType, DeviceInfo.mqttDeviceId, DeviceInfo.mqttTmp, DeviceInfo.mqttTmp2,DeviceInfo.mqttTmp3);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getLampColor":
                        Log.i(LOG_TAG, "deviceService.getLampColor" + DeviceInfo.mqttDeviceId);
                        zwaveService.getLampColor(DeviceInfo.devType, DeviceInfo.mqttDeviceId);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getSupportedColor":
                        Log.i(LOG_TAG, "deviceService.getSupportedColor" + DeviceInfo.mqttDeviceId);
                        zwaveService.getSupportColor(DeviceInfo.devType, DeviceInfo.mqttDeviceId);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "startStopColorLevelChange":
                        Log.i(LOG_TAG, "deviceService.startStopColorLevelChange" + DeviceInfo.mqttDeviceId + DeviceInfo.mqttTmp + DeviceInfo.mqttTmp2 + DeviceInfo.mqttTmp3 + DeviceInfo.mqttTmp4);
                        zwaveService.startStopColorLevelChange(DeviceInfo.devType, DeviceInfo.mqttDeviceId, DeviceInfo.mqttTmp, DeviceInfo.mqttTmp2, DeviceInfo.mqttTmp3, DeviceInfo.mqttTmp4);
                        DeviceInfo.getMqttPayload = "";
                        break;


                    case "getConfiguration":
                        Log.i(LOG_TAG, "deviceService.getConfiguration");
                        zwaveService.getConfiguration(DeviceInfo.mqttDeviceId, DeviceInfo.mqttTmp, DeviceInfo.mqttTmp2, DeviceInfo.mqttTmp3, DeviceInfo.mqttTmp4);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "setConfiguration":
                        Log.i(LOG_TAG, "deviceService.setConfiguration");
                        try {
                            DeviceInfo.callResult = zwaveService.setConfiguration(DeviceInfo.mqttDeviceId, DeviceInfo.mqttTmp, DeviceInfo.mqttTmp2, DeviceInfo.mqttTmp3, DeviceInfo.mqttTmp4);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        if (DeviceInfo.callResult >= 0) {
                            DeviceInfo.resultToMqttBroker = "setConfigurationTrue";
                        } else {
                            DeviceInfo.resultToMqttBroker = "setConfigurationFail";
                        }
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getMeter":
                        Log.i(LOG_TAG, "deviceService.getMeter" + DeviceInfo.mqttDeviceId + DeviceInfo.mqttTmp);
                        DeviceInfo.callResult = zwaveService.getMeter(DeviceInfo.devType, DeviceInfo.mqttDeviceId, DeviceInfo.mqttTmp);
                        if (DeviceInfo.callResult >= 0) {
                            DeviceInfo.resultToMqttBroker = "getMeterTrue";
                        } else {
                            DeviceInfo.resultToMqttBroker = "getMeterFail";
                        }
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "resetMeter":
                        Log.i(LOG_TAG, "deviceService.resetMeter" + DeviceInfo.mqttDeviceId);
                        DeviceInfo.callResult = zwaveService.resetMeter(DeviceInfo.devType, DeviceInfo.mqttDeviceId);
                        if (DeviceInfo.callResult >= 0) {
                            DeviceInfo.resultToMqttBroker = "resetMeterTrue";
                        } else {
                            DeviceInfo.resultToMqttBroker = "resetMeterFail";
                        }
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getGroupInfo":
                        Log.i(LOG_TAG, "deviceService.getGroupInfo");
                        //zwaveService.getGroupInfo(DeviceInfo.devType, DeviceInfo.mqttDeviceId, DeviceInfo.mqttTmp, DeviceInfo.mqttTmp2);
                        zwaveService.getGroupInfo(DeviceInfo.devType, DeviceInfo.mqttDeviceId, DeviceInfo.mqttTmp, 0);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "addEndpointsToGroup":
                        zwaveService.addEndpointsToGroup(DeviceInfo.devType, DeviceInfo.mqttDeviceId, DeviceInfo.mqttTmp, Utils.convertIntegers(DeviceInfo.arrList), DeviceInfo.mqttTmp2);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "removeEndpointsFromGroup":
                        zwaveService.removeEndpointsFromGroup(DeviceInfo.devType, DeviceInfo.mqttDeviceId, DeviceInfo.mqttTmp, Utils.convertIntegers(DeviceInfo.arrList), DeviceInfo.mqttTmp2);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getMaxSupportedGroups":
                        Log.i(LOG_TAG, "deviceService.getMaxSupportedGroups");
                        DeviceInfo.callResult = zwaveService.getMaxSupportedGroups(DeviceInfo.mqttDeviceId, DeviceInfo.mqttTmp);
                        if (DeviceInfo.callResult >= 0) {
                            DeviceInfo.resultToMqttBroker = "getMaxSupportedGroupsTrue";
                        } else {
                            DeviceInfo.resultToMqttBroker = "getMaxSupportedGroupsFail";
                        }

                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "setScheduleActive":
                        Log.i(LOG_TAG, "deviceService.setScheduleActive " + DeviceInfo.mqttString);
                        zwaveService.setScheduleActive(DeviceInfo.devType, DeviceInfo.mqttDeviceId, DeviceInfo.mqttString);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getScheduleList":
                        Log.i(LOG_TAG, "deviceService.getScheduleList");
                        zwaveService.getScheduleList(DeviceInfo.devType, DeviceInfo.mqttDeviceId);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "removeSchedule":
                        Log.i(LOG_TAG, "deviceService.removeSchedule " + DeviceInfo.mqttString);
                        zwaveService.removeSchedule(DeviceInfo.devType, DeviceInfo.mqttDeviceId, DeviceInfo.mqttString);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "setSchedule":
                        Log.i(LOG_TAG, "deviceService.setSchedule " + DeviceInfo.mqttString);
                        zwaveService.setSchedule(DeviceInfo.devType, DeviceInfo.mqttDeviceId, DeviceInfo.mqttString, DeviceInfo.mqttString4, DeviceInfo.mqttString5, Integer.valueOf(DeviceInfo.mqttString3), DeviceInfo.mqttString2);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "sendNodeInformation":
                        Log.i(LOG_TAG, "deviceService.sendNodeInformation");
                        zwaveService.sendNodeInformationFrame(0, 1);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getFavoriteList": //public channel
                        Log.i(LOG_TAG, "deviceService.getFavoriteList");
                        zwaveService.getFavoriteList();
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "editFavoriteList": //public channel
                        Log.i(LOG_TAG, "deviceService.editFavoriteList");
                        zwaveService.editFavoriteList(DeviceInfo.addList, DeviceInfo.removeList);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "setSceneAction":
                        Log.i(LOG_TAG, "deviceService.setSceneAction ");
                        //zwaveService.setSceneAction();
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getSceneList": //public channel
                        Log.i(LOG_TAG, "deviceService.getScene");
                        zwaveService.getScene();
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "removeSceneAction":
                        Log.i(LOG_TAG, "deviceService.removeSceneAction " + DeviceInfo.mqttString + " | nodeId = " + DeviceInfo.mqttString2);
                        //zwaveService.removeSceneAction();
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "removeScene":
                        Log.i(LOG_TAG, "deviceService.removeScene " + DeviceInfo.mqttString);
                        //zwaveService.removeScene(DeviceInfo.mqttString);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "editScene":
                        Log.i(LOG_TAG, "deviceService.editScene " + DeviceInfo.mqttString + " to " + DeviceInfo.mqttString3 + "" +
                                " |iconName = " + DeviceInfo.mqttString2 + " to " + DeviceInfo.mqttString4);
                        //zwaveService.editScene(DeviceInfo.mqttString);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "executeScene":
                        Log.i(LOG_TAG, "deviceService.removeScene " + DeviceInfo.mqttString2 + " action = " + DeviceInfo.mqttString);
                        //zwaveService.editScene(sceneName);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "addProvisionListEntry":
                        Log.i(LOG_TAG, "deviceService.addProvisionListEntry");
                        zwaveService.addProvisionListEntry("Zwave", DeviceInfo.dskNumber.getBytes(), DeviceInfo.inclusionState, DeviceInfo.bootMode,DeviceInfo.qrCodeFlag);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "rmProvisionListEntry":
                        Log.i(LOG_TAG, "deviceService.rmProvisionListEntry");
                        //dskNumber = DeviceInfo.mqttString.getBytes();
                        zwaveService.rmProvisionListEntry("Zwave", DeviceInfo.mqttString.getBytes());
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "rmAllProvisionListEntry":
                        Log.i(LOG_TAG, "deviceService.rmAllProvisionListEntry");
                        zwaveService.rmAllProvisionListEntry();
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "editProvisionListEntry":
                        Log.i(LOG_TAG, "deviceService.editProvisionListEntry");
                        zwaveService.addProvisionListEntry("Zwave", DeviceInfo.dskNumber.getBytes(), DeviceInfo.inclusionState,DeviceInfo.bootMode,DeviceInfo.qrCodeFlag);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getAllProvisionListEntry":
                        Log.i(LOG_TAG, "deviceService.getAllProvisionListEntry");
                        zwaveService.getAllProvisionListEntry();
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getProvisionListEntry":
                        Log.i(LOG_TAG, "deviceService.getProvisionListEntry");
                        //dskNumber = DeviceInfo.mqttString.getBytes();
                        zwaveService.getProvisionListEntry("Zwave", DeviceInfo.mqttString.getBytes());
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "startNetworkHealthCheck":
                        Log.i(LOG_TAG, "deviceService.startNetworkHealthCheck");
                        DeviceInfo.callResult = zwaveService.startNetworkHealthCheck();
                        if (DeviceInfo.callResult < 0) {
                            //Log.d(LOG_TAG, "deviceService.editNodeInfo true");
                            //Log.i(LOG_TAG, "startNetworkHealthCheck : -17 !!!!!!!!!!!!!");
                            DeviceInfo.resultToMqttBroker = "dongleBusy:startNetworkHealthCheck:"+DeviceInfo.callResult;
                        }
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getBattery":
                        Log.i(LOG_TAG, "deviceService.getBattery" + DeviceInfo.mqttDeviceId);
                        zwaveService.getDeviceBattery(DeviceInfo.devType, DeviceInfo.mqttDeviceId);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getSensorMultilevel":
                        Log.i(LOG_TAG, "deviceService.getSensorMultilevel" + DeviceInfo.mqttDeviceId);
                        try {
                            zwaveService.getSensorMultiLevel(DeviceInfo.devType, DeviceInfo.mqttDeviceId);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getSupportSwitchType":
                        Log.i(LOG_TAG, "deviceService.getSupportSwitchType" + DeviceInfo.mqttDeviceId);
                        zwaveService.getSupportedSwitchType(DeviceInfo.mqttDeviceId);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "startStopSwitchLevelChange":
                        Log.i(LOG_TAG, "deviceService.startStopSwitchLevelChange");
                        DeviceInfo.callResult = zwaveService.startStopSwitchLevelChange(DeviceInfo.mqttDeviceId, DeviceInfo.mqttTmp, DeviceInfo.mqttTmp2, DeviceInfo.mqttTmp3, DeviceInfo.mqttTmp4, DeviceInfo.mqttTmp5);
                        if (DeviceInfo.callResult >= 0) {
                            DeviceInfo.resultToMqttBroker = "startStopSwitchLevelChangeTrue";
                        } else {
                            DeviceInfo.resultToMqttBroker = "startStopSwitchLevelChangeFail";
                        }
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getPowerLevel":
                        Log.i(LOG_TAG, "deviceService.getPowerLevel" + DeviceInfo.mqttDeviceId);
                        zwaveService.getPowerLevel(DeviceInfo.mqttDeviceId);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "switchAllOn":
                        Log.i(LOG_TAG, "deviceService.switchAllOn" + DeviceInfo.mqttDeviceId);
                        DeviceInfo.callResult = zwaveService.setSwitchAllOn(DeviceInfo.devType, DeviceInfo.mqttDeviceId);
                        if (DeviceInfo.callResult >= 0) {
                            DeviceInfo.resultToMqttBroker = "switchAllOnTrue";
                        } else {
                            DeviceInfo.resultToMqttBroker = "switchAllOnFail";
                        }
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "switchAllOff":
                        Log.i(LOG_TAG, "deviceService.switchAllOff" + DeviceInfo.mqttDeviceId);
                        DeviceInfo.callResult = zwaveService.setSwitchAllOff(DeviceInfo.devType, DeviceInfo.mqttDeviceId);
                        if (DeviceInfo.callResult >= 0) {
                            DeviceInfo.resultToMqttBroker = "switchAllOffTrue";
                        } else {
                            DeviceInfo.resultToMqttBroker = "switchAllOffFail";
                        }
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "setSwitchAll":
                        Log.i(LOG_TAG, "deviceService.setSwitchAll" + DeviceInfo.mqttDeviceId);
                        DeviceInfo.callResult = zwaveService.setSwitchAll(DeviceInfo.devType, DeviceInfo.mqttDeviceId, DeviceInfo.mqttTmp);
                        if (DeviceInfo.callResult >= 0) {
                            DeviceInfo.resultToMqttBroker = "setSwitchAllTrue";
                        } else {
                            DeviceInfo.resultToMqttBroker = "setSwitchAllFail";
                        }
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getSwitchAll":
                        Log.i(LOG_TAG, "deviceService.GetSwitchAll" + DeviceInfo.mqttDeviceId);
                        DeviceInfo.callResult = zwaveService.getSwitchAll(DeviceInfo.devType, DeviceInfo.mqttDeviceId);
                        if (DeviceInfo.callResult >= 0) {
                            DeviceInfo.resultToMqttBroker = "getSwitchAllTrue";
                        } else {
                            DeviceInfo.resultToMqttBroker = "getSwitchAllFail";
                        }
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getSensorBinary":
                        Log.i(LOG_TAG, "deviceService.getSensorBinary");
                        zwaveService.getSensorBasic(DeviceInfo.mqttDeviceId, DeviceInfo.mqttTmp);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getSensorBinarySupportedSensor":
                        Log.i(LOG_TAG, "deviceService.getSensorBinarySupportedSensor" + DeviceInfo.mqttDeviceId);
                        zwaveService.GetSensorBinarySupportedSensor(DeviceInfo.mqttDeviceId);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getMeterSupported":
                        Log.i(LOG_TAG, "deviceService.getMeterSupported" + DeviceInfo.mqttDeviceId);
                        DeviceInfo.callResult = zwaveService.getMeterSupported(DeviceInfo.mqttDeviceId);
                        if (DeviceInfo.callResult >= 0) {
                            DeviceInfo.resultToMqttBroker = "getMeterSupportedTrue";
                        } else {
                            DeviceInfo.resultToMqttBroker = "getMeterSupportedFail";
                        }
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getSpecificGroup":
                        Log.i(LOG_TAG, "deviceService.getSpecificGroup" + DeviceInfo.mqttDeviceId + DeviceInfo.mqttTmp);
                        zwaveService.getSpecificGroup(DeviceInfo.mqttDeviceId, DeviceInfo.mqttTmp);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getNotification":
                        Log.i(LOG_TAG, "deviceService.getNotification" + DeviceInfo.mqttDeviceId + DeviceInfo.mqttTmp + DeviceInfo.mqttTmp2 + DeviceInfo.mqttTmp3);
                        zwaveService.getSensorNotification(DeviceInfo.mqttDeviceId, DeviceInfo.mqttTmp, DeviceInfo.mqttTmp2, DeviceInfo.mqttTmp3);
                        DeviceInfo.getMqttPayload = "";
                        break;


                    case "setNotification":
                        Log.i(LOG_TAG, "deviceService.setNotification" + DeviceInfo.mqttDeviceId + DeviceInfo.mqttTmp + DeviceInfo.mqttTmp2);
                        DeviceInfo.callResult = zwaveService.setNotification(DeviceInfo.mqttDeviceId, DeviceInfo.mqttTmp, DeviceInfo.mqttTmp2);
                        if (DeviceInfo.callResult >= 0) {
                            DeviceInfo.resultToMqttBroker = "setNotificationTrue";
                        } else {
                            DeviceInfo.resultToMqttBroker = "setNotificationFail";
                        }
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getSupportedNotification":
                        Log.i(LOG_TAG, "deviceService.getSupportedNotification" + DeviceInfo.mqttDeviceId);
                        zwaveService.getSupportedNotification(DeviceInfo.mqttDeviceId);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getSupportedEventNotification":
                        Log.i(LOG_TAG, "deviceService.getSupportedEventNotification" + DeviceInfo.mqttDeviceId + DeviceInfo.mqttTmp);
                        zwaveService.getSupportedEventNotification(DeviceInfo.mqttDeviceId, DeviceInfo.mqttTmp);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getSpecifyDeviceInfo":
                        Log.i(LOG_TAG, "deviceService.getSpecifyDeviceInfo");
                        Log.i(LOG_TAG, "nodeId: " + DeviceInfo.mqttDeviceId);
                        zwaveService.getSpecifyDeviceInfo(DeviceInfo.mqttDeviceId);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "removeFailDevice":
                        Log.i(LOG_TAG, "deviceService.removeFailDevice");
                        Log.i(LOG_TAG, "DeviceInfo.mqttDeviceId: " + DeviceInfo.mqttDeviceId);
                        DeviceInfo.callResult = zwaveService.removeFailedDevice(DeviceInfo.mqttDeviceId);
                        if (DeviceInfo.callResult < 0) {
                            //Log.d(LOG_TAG, "deviceService.editNodeInfo true");
                            //Log.i(LOG_TAG, "removeFailDevice : -17 !!!!!!!!!!!!!");
                            DeviceInfo.resultToMqttBroker = "dongleBusy:removeFailDevice:"+DeviceInfo.callResult;
                        }
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "checkNodeIsFailed":
                        Log.i(LOG_TAG, "deviceService.checkNodeIsFailed");
                        Log.i(LOG_TAG, "DeviceInfo.mqttDeviceId: " + DeviceInfo.mqttDeviceId);
                        DeviceInfo.callResult = zwaveService.checkNodeIsFailed(DeviceInfo.mqttDeviceId);
                        if (DeviceInfo.callResult < 0) {
                            //Log.d(LOG_TAG, "deviceService.editNodeInfo true");
                            //Log.i(LOG_TAG, "removeFailDevice : -17 !!!!!!!!!!!!!");
                            DeviceInfo.resultToMqttBroker = "dongleBusy:checkNodeIsFailed:"+DeviceInfo.callResult;
                        }
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "setDefault":
                        Log.i(LOG_TAG, "deviceService.setDefault");
                        DeviceInfo.callResult = zwaveService.setDefault();
                        if (DeviceInfo.callResult < 0) {
                            DeviceInfo.resultToMqttBroker = "setDefaultFail17";
                            //Log.i(LOG_TAG, "setDefault : -17 !!!!!!!!!!!!!");
                        }
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "replaceFailDevice":
                        Log.i(LOG_TAG, "deviceService.replaceFailDevice");
                        Log.i(LOG_TAG, "DeviceInfo.mqttDeviceId: " + DeviceInfo.mqttDeviceId);
                        DeviceInfo.callResult = zwaveService.replaceFailedDevice(DeviceInfo.mqttDeviceId);
                        if (DeviceInfo.callResult < 0) {
                            //Log.d(LOG_TAG, "deviceService.editNodeInfo true");
                            Log.i(LOG_TAG, "removeFailDevice : "+ DeviceInfo.callResult);
                            DeviceInfo.resultToMqttBroker = "dongleBusy:replaceFailDevice:"+DeviceInfo.callResult;
                        }
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "startLearnMode":
                        Log.i(LOG_TAG, "deviceService.startLearnMode");
                        DeviceInfo.callResult = zwaveService.StartLearnMode();
                        if (DeviceInfo.callResult < 0) {
                            //Log.d(LOG_TAG, "deviceService.editNodeInfo true");
                            Log.i(LOG_TAG, "startLearnMode : " + DeviceInfo.callResult);
                            DeviceInfo.resultToMqttBroker = "dongleBusy:startLearnMode:"+DeviceInfo.callResult;
                        }
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getSceneActuatorConf":
                        Log.i(LOG_TAG, "deviceService.getSceneActuatorConf");
                        zwaveService.getSceneActuatorConf(DeviceInfo.mqttDeviceId, DeviceInfo.mqttTmp);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getSupportedCentralScene":
                        Log.i(LOG_TAG, "deviceService.getSupportedCentralScene");
                        zwaveService.getSupportedCentralScene(DeviceInfo.mqttDeviceId, DeviceInfo.mqttTmp);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getDoorLockOperation":
                        Log.i(LOG_TAG, "deviceService.getDoorLockOperation");
                        zwaveService.getDoorLockOperation(DeviceInfo.mqttDeviceId);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "setDoorLockOperation":
                        Log.i(LOG_TAG, "deviceService.setDoorLockOperation");
                        DeviceInfo.callResult = zwaveService.setDoorLockOperation(DeviceInfo.mqttDeviceId, DeviceInfo.mqttTmp);
                        if (DeviceInfo.callResult >= 0) {
                            DeviceInfo.resultToMqttBroker = "setDoorLockOperationTrue";
                        } else {
                            DeviceInfo.resultToMqttBroker = "setDoorLockOperationFail";
                        }
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getDoorLockConfig":
                        Log.i(LOG_TAG, "deviceService.getDoorLockConfiguration");
                        zwaveService.getDoorLockConfiguration(DeviceInfo.mqttDeviceId);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "setDoorLockConfig":
                        Log.i(LOG_TAG, "deviceService.setDoorLockConfig");

                        DeviceInfo.callResult = zwaveService.setDoorLockConfiguration(DeviceInfo.mqttDeviceId, DeviceInfo.mqttTmp, DeviceInfo.mqttTmp2, DeviceInfo.mqttTmp3, DeviceInfo.mqttTmp4, DeviceInfo.mqttTmp5);
                        if (DeviceInfo.callResult >= 0) {
                            DeviceInfo.resultToMqttBroker = "setDoorLockConfigTrue";
                        } else {
                            DeviceInfo.resultToMqttBroker = "setDoorLockConfigFail";
                        }
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "setBinarySwitchState":
                        Log.i(LOG_TAG, "deviceService.setBinarySwitchState");
                        DeviceInfo.callResult = zwaveService.setBinarySwitchState(DeviceInfo.devType, DeviceInfo.mqttDeviceId, DeviceInfo.mqttTmp);
                        if (DeviceInfo.callResult >= 0) {
                            DeviceInfo.resultToMqttBroker = "setBinarySwitchStateTrue";
                        } else {
                            DeviceInfo.resultToMqttBroker = "setBinarySwitchStateFail";
                        }
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getBinarySwitchState":
                        Log.i(LOG_TAG, "deviceService.getBinarySwitchState");
                        zwaveService.getBinarySwitchState(DeviceInfo.devType, DeviceInfo.mqttDeviceId);
                        DeviceInfo.getMqttPayload = "";
                        break;

                    case "getDeviceInfo":
                        Log.i(LOG_TAG, "deviceService.getDeviceInfo");
                        zwaveService.getDeviceInfo();
                        DeviceInfo.getMqttPayload = "";
                        break;
                }
            }
        }
    };

    private void showZwaveControlTimeOutDialog(String titleStr) {
        if(alertDialog == null) {
            AlertDialog.Builder addDialog = new AlertDialog.Builder(mContext);
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            View view = layoutInflater.inflate(R.layout.dialog_retry_layout, null);
            addDialog.setView(view);
            alertDialog = addDialog.create();
            alertDialog.show();

            TextView title = (TextView) view.findViewById(R.id.title);
            TextView message = (TextView) view.findViewById(R.id.message);
            title.setText("Prompt");
            message.setText(titleStr);
            Button positiveButton = (Button) view.findViewById(R.id.positiveButton);
            Button negativeButton = (Button) view.findViewById(R.id.negativeButton);

            positiveButton.setText("retry");
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //retry to opencontroller
                    openController();
                    showProgressDialog(mContext, "OpenController....");
                    alertDialogCancel();
                }
            });

            negativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //tap cancel button and exit to main screen
                    finish();
                    timerCancel();

                    alertDialogCancel();
                    DeviceInfo.isMQTTInitFinish = false;
                    DeviceInfo.isOpenControllerFinish = false;
                    DeviceInfo.isZwaveInitFinish = false;
                    System.exit(0);
                }
            });
        }
    }


    // permanent thread
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2001:
                    timerCancel();
                    hideProgressDialog();
                    showZwaveControlTimeOutDialog("Zwave OpenController Timeout");
                    break;
            }
        }
    };

    @Override
    public void onClick(View view) {
        //Log.d(LOG_TAG,"node ID : " + selectNode);
        switch (view.getId()) {
            case R.id.btnAdd:

                if (editDsk.length() != 0 && editDsk.length() == 47) {    // editDsk will 5-digit or full code
                    //Log.i(LOG_TAG, "call zwaveService.addDevice()");
                    DeviceInfo.reqKey = Integer.valueOf(editDsk.getText().toString());
                    zwaveService.addDevice(DeviceInfo.devType);

                } else if (editDsk.length() != 0 && editDsk.length() == 5) {
                    //Log.i(LOG_TAG, "call zwaveService.addDevice()");
                    DeviceInfo.reqKey = Integer.valueOf(editDsk.getText().toString());
                    zwaveService.addDevice(DeviceInfo.devType);
                } else if (editDsk.length() == 0) {
                    zwaveService.addDevice(DeviceInfo.devType);
                } else
                    Toast.makeText(this, "格式錯誤 !", Toast.LENGTH_SHORT).show();
                /*
                inputDsk = "11394" + "\0";
                byte[] dskNumber = inputDsk.getBytes();
                zwaveService.addDevice(DeviceInfo.devType, dskNumber);
                */
                break;

            case R.id.btnRemove:
                Log.i(LOG_TAG, "call zwaveService.removeDevice()");
                zwaveService.removeDevice(DeviceInfo.devType,0);
                break;

            case R.id.btnLearn:
                zwaveService.StartLearnMode();
                break;

            case R.id.btnButton:
                //zwaveService.getGroupInfo(DeviceInfo.devType, Integer.valueOf(spNodeIdList.getSelectedItem().toString()), 0, 0);
                Log.i(LOG_TAG, "call zwaveService.sendNodeInformationFrame()");

                //zwaveService.getSecurity2CmdSupported(Integer.valueOf(spNodeIdList.getSelectedItem().toString()));

                if (spApiList.getSelectedItem().toString().contains("ZwController_startNetworkHealthCheck")) {
                    zwaveService.startNetworkHealthCheck();
                } else if (spApiList.getSelectedItem().toString().contains("ZwController_getProvisionListEntry")) {
                    getProvisionList();
                } else if (spApiList.getSelectedItem().toString().contains("ZwController_getAllProvisionListEntry")) {
                    zwaveService.getAllProvisionListEntry();
                } else if (spApiList.getSelectedItem().toString().contains("ZwController_rmAllProvisionListEntry")) {
                    zwaveService.rmAllProvisionListEntry();
                } else if (spApiList.getSelectedItem().toString().contains("ZwController_getDeviceList")) {
                    zwaveService.getDeviceList("ALL");
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_battery_get")) {
                    zwaveService.getDeviceBattery(DeviceInfo.devType, Integer.valueOf(spNodeIdList.getSelectedItem().toString()));
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_sensor_multilevel_get")) {
                    try {
                        zwaveService.getSensorMultiLevel(DeviceInfo.devType, Integer.valueOf(spNodeIdList.getSelectedItem().toString()));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_Command_Class_get")) {
                    zwaveService.getDeviceInfo();
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_Device_get")) {
                    zwaveService.getDeviceInfo();
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_basic_get")) {
                    zwaveService.getBasic(DeviceInfo.devType, Integer.valueOf(spNodeIdList.getSelectedItem().toString()));
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_basic_set")) {
                    zwaveService.setBasic(DeviceInfo.devType, Integer.valueOf(spNodeIdList.getSelectedItem().toString()), Integer.valueOf(editSetApiValue.getText().toString()));
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_switch_multilevel_get")) {
                    zwaveService.getSwitchMultiLevel(DeviceInfo.devType, Integer.valueOf(spNodeIdList.getSelectedItem().toString()));
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_switch_multilevel_set")) {
                    zwaveService.setSwitchMultiLevel(DeviceInfo.devType, Integer.valueOf(spNodeIdList.getSelectedItem().toString()), Integer.valueOf(editSetApiValue.getText().toString()), 1);
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_get_support_switch_type")) {
                    zwaveService.getSupportedSwitchType(Integer.valueOf(spNodeIdList.getSelectedItem().toString()));
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_start_stop_switchlevel_change")) {
                    //zwaveService.startStopSwitchLevelChange(); many parameter
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_configuration_get")) {
                    zwaveService.getConfiguration(Integer.valueOf(spNodeIdList.getSelectedItem().toString()),0,0,0,0);
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_configuration_set")) {
                    //zwaveService.setConfiguration(); many parameter
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_powerLevel_get")) {
                    zwaveService.getPowerLevel(Integer.valueOf(spNodeIdList.getSelectedItem().toString()));
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_switch_all_on")) {
                    zwaveService.setSwitchAllOn(DeviceInfo.devType, Integer.valueOf(spNodeIdList.getSelectedItem().toString()));
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_switch_all_off")) {
                    zwaveService.setSwitchAllOff(DeviceInfo.devType, Integer.valueOf(spNodeIdList.getSelectedItem().toString()));
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_switch_all_set")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_switch_all_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_switch_all_on_broadcast")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_switch_all_off_broadcast")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_switch_binary_set")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_switch_binary_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_sensor_binary_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_sensor_binary_supported_sensor_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_meter_get")) {
                    zwaveService.getMeter(DeviceInfo.devType, Integer.valueOf(spNodeIdList.getSelectedItem().toString()), 10);
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_meter_supported_get")) {
                    zwaveService.getMeterSupported(Integer.valueOf(spNodeIdList.getSelectedItem().toString()));
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_meter_reset")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_wake_up_interval_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_wake_up_interval_set")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_door_lock_operation_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_door_lock_operation_set")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_door_lock_config_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_door_lock_config_set")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_user_code_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_user_code_set")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_user_code_number_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_protection_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_protection_set")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_supported_protection_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_protection_exclusive_control_node_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_protection_exclusive_control_node_set")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_protection_timeout_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_protection_timeout_set")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_indicator_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_indicator_set")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_door_lock_logging_supported_records_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_door_lock_logging_records_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_language_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_language_set")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_switch_color_get")) {
                    zwaveService.getLampColor(DeviceInfo.devType, Integer.valueOf(spNodeIdList.getSelectedItem().toString()));
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_switch_color_supported_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_switch_color_set")) {
                    String[] temp = editSetApiValue.getText().toString().split(",");
                    zwaveService.setLampColor(DeviceInfo.devType, Integer.valueOf(spNodeIdList.getSelectedItem().toString()), Integer.valueOf(temp[0]), Integer.valueOf(temp[1]), Integer.valueOf(temp[2]));
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_start_stop_color_levelchange")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_barrier_operator_set")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_barrier_operator_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_barrier_operator_signal_set")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_barrier_operator_signal_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_barrier_operator_signal_supported_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_basic_tariff_info_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_get_group_info")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_add_endpoints_to_group")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_remove_endpoints_from_group")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_get_max_supported_groups")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_get_specific_group")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_notification_set")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_notification_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_notification_supported_get")) {
                    zwaveService.getSupportedNotification(Integer.valueOf(spNodeIdList.getSelectedItem().toString()));
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_notification_supported_event_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_central_scene_supported_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("hl_central_scene_notification_report_cb")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_scene_actuator_conf_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_firmwareupdate_info_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_firmwareupdate_request")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_multi_cmd_encap")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("ZwController_getSpecifyDeviceInfo")) {
                    zwaveService.getSpecifyDeviceInfo(Integer.valueOf(spNodeIdList.getSelectedItem().toString()));
                } else if (spApiList.getSelectedItem().toString().contains("ZwController_SetDefault")) {
                    zwaveService.setDefault();
                } else if (spApiList.getSelectedItem().toString().contains("ZwController_checkNodeIsFailed")) {
                    zwaveService.checkNodeIsFailed(Integer.valueOf(spNodeIdList.getSelectedItem().toString()));
                } else if (spApiList.getSelectedItem().toString().contains("ZwController_RemoveFailedDevice")) {
                    zwaveService.removeFailedDevice(Integer.valueOf(spNodeIdList.getSelectedItem().toString()));
                    nodeIdArr.remove(Integer.valueOf(Integer.valueOf(spNodeIdList.getSelectedItem().toString())));

                    ArrayAdapter<Integer> devList = new ArrayAdapter<Integer>(WelcomeActivity.this,
                            android.R.layout.simple_spinner_dropdown_item,
                            convertIntegers(nodeIdArr));
                    spNodeIdList.setAdapter(devList);


                } else if (spApiList.getSelectedItem().toString().contains("ZwController_ReplaceFailedDevice")) {
                    zwaveService.replaceFailedDevice(Integer.valueOf(spNodeIdList.getSelectedItem().toString()));
                }
                break;
            case R.id.btnaddProList:
                //if (editDsk.length() != 0 && editDsk.length() == 47) {    // editDsk will 5-digit or full code
                    //DeviceInfo.InclusionState = true;
                    addProvisionList();
                //} else
                //    Toast.makeText(this, "格式錯誤 !", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnrmProList:
                if (selectProvisionList != null) {    // editDsk will 5-digit or full code
                    rmProvisionList();
                }
                break;
            case R.id.btnEditProvision:
                if (selectProvisionList != null) {
                    editDsk.setText(selectProvisionList);
                    rmProvisionList();
                } else {
                    txAllMsg.setText("Provision List is null");
                }
                break;
            case R.id.btnPassive:
                if (selectProvisionList != null) {
                    //DeviceInfo.InclusionState = false;
                    editDsk.setText(selectProvisionList);
                    rmProvisionList();
                    addProvisionList();
                }
        }
    }

    //load exist provision list
    private void loadProvisionList(String result) {
        if(result.contains("No list entry found"))
        {
            Log.d(LOG_TAG,"No list entry found !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }

        else {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(result);
                String provisinoInfo = jsonObject.optString("Detial provision list");
                String[] dskNumber = provisinoInfo.split(",");
                for (int i = 0; i < dskNumber.length; i++) {
                    //txAllMsg.setText(result);
                    if (dskNumber[i].contains("DSK")) {
                        String[] tmpDsk = dskNumber[i].split("\"");
                        for (int j = 0; j < tmpDsk.length; j++) {
                            if (j % 4 == 0 && getProvisionListFlag == false) {
                                Log.d(LOG_TAG, tmpDsk[j + 3]);
                                provisionListArr.add(tmpDsk[j + 3]); //抓取DSK值
                                //txAllMsg.setText(tmpDsk[j + 3]);
                            }
                        }
                    }
                }

                getProvisionListFlag = true;    //Provision List 載入到 spinner 1次

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    //get Provision List
    private void getProvisionList() {
        getProvisionNodeFlag = false;
        inputDsk =  selectProvisionList + "\0";
        //Log.d(LOG_TAG,inputDsk);
        byte[] dskNumber = inputDsk.getBytes();
        zwaveService.getProvisionListEntry(DeviceInfo.devType,dskNumber);
    }

    //check provision node whether exist
    private void getProvisionNode(String result) {
        final String provisionInfo = result;
        JSONObject jsonObject = null;
        JSONObject jsonObject2 = null;
        txAllMsg.setText(result);
        try {
            jsonObject = new JSONObject(provisionInfo);
            String provisionState = jsonObject.optString("Network inclusion state");
            jsonObject2 = new JSONObject(provisionState);
            String provisionNode = jsonObject2.optString("Node Id");
            //String[] provisionStateSplit = provisionState.split(":");
            Log.d(LOG_TAG,provisionNode);
            if(provisionNode.equals("0")) {
                Log.d(LOG_TAG,"device is exclusion");
                txAllMsg.setText("device is exclusion");
            } else {
                Log.d(LOG_TAG,"device is not exclusion");
                txAllMsg.setText("device is not exclusion");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //remove Provision List
    private void rmProvisionList() {
        getProvisionNodeFlag = true;
        inputDsk =  selectProvisionList + "\0";
        Log.d(LOG_TAG,inputDsk);
        byte[] dskNumber = inputDsk.getBytes();
        zwaveService.getProvisionListEntry(DeviceInfo.devType,dskNumber);
        zwaveService.rmProvisionListEntry(DeviceInfo.devType,dskNumber);
        provisionListArr.remove(selectProvisionList);
        ArrayAdapter<String> provisionList = new ArrayAdapter<String>(WelcomeActivity.this,
                android.R.layout.simple_spinner_dropdown_item, converProvisionList(provisionListArr));
        spProvisionList.setAdapter(provisionList);
    }

    //insert Provision List
    private void addProvisionList() {

        //String str = "33592-63521-64594-02477-00177-13100-21787-02557";
        //String inputDsk = str + "\0";
        inputDsk = editDsk.getText().toString() + "\0";
        Log.d(LOG_TAG,inputDsk);
        byte[] dskNumber = inputDsk.getBytes();
        if(provisionListArr.contains(editDsk.getText().toString())) {
            Toast.makeText(this,"already Provision List", Toast.LENGTH_SHORT).show();
        } else {
            provisionListArr.add(editDsk.getText().toString());
            zwaveService.addProvisionListEntry(DeviceInfo.devType,dskNumber,DeviceInfo.inclusionState,DeviceInfo.bootMode,DeviceInfo.qrCodeFlag);
            Toast.makeText(this, "add Provision List", Toast.LENGTH_SHORT).show();
        }
        ArrayAdapter<String> provisionList = new ArrayAdapter<String>(WelcomeActivity.this,
                android.R.layout.simple_spinner_dropdown_item, converProvisionList(provisionListArr));
        spProvisionList.setAdapter(provisionList);

        spProvisionList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectProvisionList = spProvisionList.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    //input editDsk limitation
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(cb1.isChecked()) {
            editDsk.setText("");
            editDsk.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        } else if (cb2.isChecked()) {
            editDsk.setText("");
            editDsk.setFilters(new InputFilter[]{new InputFilter.LengthFilter(47)});
        } else if (cb3.isChecked()) {
            editDsk.setText("");
            editDsk.setFilters(new InputFilter[]{new InputFilter.LengthFilter(0)});
        }

    }

    class mTimerTask extends TimerTask {
        public void run() {
            zwaveService.closeController();
            Log.d(LOG_TAG,"timer on schedule");
            Message message = new Message();
            message.what = 2001;
            mHandler.sendMessage(message);
            timerCancel();
        }
    }


    private void alertDialogCancel() {
        if(alertDialog!=null){
            alertDialog.dismiss();
            alertDialog=null;
        }
    }

    // cancel this timer schedule
    private void timerCancel() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    // bind service with zwave control service
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i(LOG_TAG,"onServiceConnected....");
            zwaveService = ((ZwaveControlService.MyBinder)iBinder).getService();
            //register mCallback
            if (zwaveService != null) {
                zwaveService.register(mCallback);       //add & remove and other info call back
                //zwaveService.register(mReqCallBacks);   //use other thread for grantKey pinCode csa req call back
                requestControlUSBPermission();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            zwaveService = null;
        }
    };

    private ServiceConnection req = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i(LOG_TAG,"req_ServiceConnected....");
            zwaveService = ((ZwaveControlService.MyBinder)iBinder).getService();
            //register mCallback
            if (zwaveService != null) {
                zwaveService.register(mReqCallBacks);   //use other thread for grantKey pinCode csa req call back
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
            this.unbindService(req);
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
                Log.i(LOG_TAG, "Result class name = [" + DeviceInfo.className + "] | result = " + DeviceInfo.result);

                while(DeviceInfo.mqttFlag) {
                    try {
                        Log.d(LOG_TAG,"wait for mqtt finish !!!!!!!!!!!!!!!!!!!!!!!!");
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if(className.equals("Sensor Info Report") || className.equals("Node Battery Value") || className.equals("Notification Get Information")) {
                    DeviceInfo.sensorClassName = className;
                    DeviceInfo.sensorResult = result;

                } else {
                    DeviceInfo.className = className;
                    DeviceInfo.result = result;
                }

                if (result.contains("Smart Start Protocol Started")) {
                    Log.d(LOG_TAG,"DeviceInfo.smartStartFlag = true");
                    DeviceInfo.smartStartFlag = true;
                }

                if (className.equals("addDevice") || className.equals("removeDevice")) {

                    addRemoveDevice(result);

                } else if (className.equals("All Node Info Report")) {

                    Log.d(LOG_TAG,result);

                } else if (className.equals("reNameDevice")) {

                    addRemoveDevice(result);
                } else if (className.equals("CSA Pin")) {
                    Log.d(LOG_TAG,"CSA Pin ");

                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(result);
                        DeviceInfo.pinCode = jsonObject.optString("PinCode");
                        Log.d(LOG_TAG,"pinCode : "+DeviceInfo.pinCode);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                DeviceInfo.mqttFlag = true;
            }
        };
    }

    public ZwaveControlService.zwaveControlReq_CallBack mReqCallBacks;
    {
        mReqCallBacks = new ZwaveControlService.zwaveControlReq_CallBack() {
            @Override
            public void zwaveControlReqResultCallBack(String className, String result) {
                Log.i(LOG_TAG, "Req class name = [" + className + "]| result = " + result);

                if (result.contains("Grant Keys Msg")) {


                    String[] grantTmp = result.split(":");
                    Log.d(LOG_TAG,"Grant Keys number : " +grantTmp[2]);
                    if(grantTmp[2].contains("135")) {
                        DeviceInfo.grantKeyNumber = "135";
                    } else if (grantTmp[2].contains("134")) {
                        DeviceInfo.grantKeyNumber = "134";
                    } else if (grantTmp[2].contains("133")) {
                        DeviceInfo.grantKeyNumber = "133";
                    } else if (grantTmp[2].contains("132")) {
                        DeviceInfo.grantKeyNumber = "132";
                        Log.d(LOG_TAG,"Grant Keys number : 132");

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
                    DeviceInfo.reqString = "Grant";
                    Log.d(LOG_TAG,"Grant Keys number : Grant");

                } else if (result.contains("PIN Requested Msg")) {
                    //DeviceInfo.reqKey = 11394;
                    DeviceInfo.reqString = "PIN";
                } else if (result.contains("Client Side Au Msg")) {
                    DeviceInfo.reqString = "Au";
                }
            }
        };
    }

    private void getImaInfo(String result) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(result);
            String ima = jsonObject.optString("RSSI hops value");
            Log.d(LOG_TAG,"rssi: "+ ima);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getOpenControllerInfo(String result) {
        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject(result);
            String NetworkRole = jsonObject.optString("Network Role");
            Log.d(LOG_TAG,"Network Role: "+ NetworkRole);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showCommandClass(String result) {
        String[] resultSplit = result.split("Interface Class\":\"");
        for(int i = 0; i < resultSplit.length; i++) {
            Log.d(LOG_TAG,resultSplit[i]);
            txAllMsg.setText(resultSplit[i]);
        }
    }

    //authenticate security of device is s0 s2 non-security
    private void showSecurityStatus(String result) {
        String[] resultSplit = result.split(",");
        String securityStatus = "";
        for(int i = 10; i < resultSplit.length; i++) { //i =10 不顯示Controller 的 security Status
            if (resultSplit[i].contains("Node security inclusion status")) {
                if (resultSplit[i].contains("S2")) {
                    //Log.i(LOG_TAG, "S2 gino!!!!!!!!!!!!!!!!!!");
                    securityStatus = "Device is S2 security";
                } else if (resultSplit[i].contains("Normal")) {
                    //Log.i(LOG_TAG, "Normal gino!!!!!!!!!!!!!!!!!!");
                    securityStatus = "Device is none security";
                } else if (resultSplit[i].contains("S0")) {
                    //Log.i(LOG_TAG, "S0 gino!!!!!!!!!!!!!!!!!!");
                    securityStatus = "Device is S0 security";
                }
            }
        }
        txAllMsg.setText(securityStatus);
    }

    //show dongle info
    private void showNodeInfo(String result) {
        String[] resultSplit = result.split("Home id");
        txAllMsg.setText(resultSplit[1]);
    }

    //show DSK number
    private void showDsk(String result) {
        final String showDsk = result;
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(showDsk);
            String dsknumber = jsonObject.optString("DSK");
            if(dsknumber.contains("00000")) {
                txDsk.setText("NO DSK number");
            } else {
                SpannableString content = new SpannableString(dsknumber);       //前五碼底線
                content.setSpan(new UnderlineSpan(), 0, 5, 0);
                txDsk.setText(content);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

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
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                txAllMsg.setText("Success");
                                //zwaveService.getDeviceInfo();
                            } else if("Learn Ready".equals(status)){
                                txAllMsg.setText("Please press the trigger button of the device");
                            }else{
                                txAllMsg.setText(status);
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
                        txAllMsg.setText(addRemoveMode+" Success " + " | NodeId = "+tNodeId);


                        if (addRemoveMode.equals("addDevice")) {
                            nodeIdArr.add(Integer.valueOf(tNodeId));
                        } else{
                            if(tNodeId.equals("fail")) {
                                txAllMsg.setText("remove fail");
                            } else {
                                nodeIdArr.remove(Integer.valueOf(tNodeId));
                                txAllMsg.setText("");
                            }
                        }

                        ArrayAdapter<Integer> devList = new ArrayAdapter<Integer>(WelcomeActivity.this,
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
                }
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

    //add & remove provision list to spinner
    private static String[] converProvisionList(List<String> dskString)
    {
        String[] ret = new String[dskString.size()];
        for (int i=0; i < ret.length; i++)
        {
            ret[i] = dskString.get(i);
        }
        return ret;
    }

    private void requestControlUSBPermission() {

        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> usbDevices = manager.getDeviceList();

        UsbDevice dev = null;
        int vid = 0;
        int pid = 0;

        for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
            dev = entry.getValue();
            vid = dev.getVendorId();
            pid = dev.getProductId();
            Log.d(LOG_TAG, "Usb Device Vid = "+ Integer.toHexString(vid) +",Pid = "+ Integer.toHexString(pid));
            if ((vid == 0x0658) && (pid == 0x0200)) {
                Log.d(LOG_TAG, "Usb Device Is CDC Device...");
                if (manager.hasPermission(dev)) {
                    Log.d(LOG_TAG, "Usb Permission Ok....");
                    openController();
                    break;
                } else {
                    Log.e(LOG_TAG, "Usb Permission Na,Try To Request Permission....");
                    PendingIntent mPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    manager.requestPermission(dev, mPendingIntent);
                    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                    usbReceiver = new usbReceiver();
                    registerReceiver(usbReceiver, filter);
                }
            }else{
                dev = null;
                Log.d(LOG_TAG, "Usb Device Is Not CDC Device...");
            }
        }

    }

    //can take off this function !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private class usbReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            openController();
                        }
                    } else {
                        Log.d(LOG_TAG,"USB"+ "permission denied for device " + device);
                        progressDialog.cancel();
                        finish();
                        System.exit(0);
                    }
                }
            }
        }
    }

    //open z-wave dongle
    private void openController() {
        new Thread(openController).start();
    }

    private Runnable openController = new Runnable() {
        @Override
        public void run() {
            zwaveService.openController();
        }
    };

}
