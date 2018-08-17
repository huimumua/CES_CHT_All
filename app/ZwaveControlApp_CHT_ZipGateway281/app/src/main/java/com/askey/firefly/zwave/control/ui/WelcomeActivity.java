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

public class WelcomeActivity extends BaseActivity{

    private static String LOG_TAG = WelcomeActivity.class.getSimpleName();

    private static AlertDialog alertDialog;
    private static Timer timer = new Timer(true);

    private static ZwaveControlService zwaveService;
    private ZwaveDeviceManager zwDevManager;
    private BroadcastReceiver usbReceiver = null;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static boolean mqttBrokerFlag = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        new UsbSerial(this);
        setContentView(R.layout.activity_welcome);
        mContext = this;

        Intent MqttIntent = new Intent(WelcomeActivity.this, MQTTBroker.class);
        if (!isServiceRunning(mContext,MQTTBroker.class)){
            mqttBrokerFlag = true;
            startService(MqttIntent);
        }

        showProgressDialog(mContext, "Initializing Open Zwave Controller...");

        zwDevManager = ZwaveDeviceManager.getInstance(this);

        // bind service
        new Thread(bindzwaveservice).start();

        new Thread(checkInitStatus).start();
        new Thread(activityZwaveControlService).start();

    }

    private Runnable activityZwaveControlService = new Runnable() {
    @Override
    public void run() {
        boolean circle = false;
        while (!circle) {
            try {
                Thread.sleep(50);
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

    public Runnable bindzwaveservice = new Runnable() {
        @Override
        public void run() {

            Intent serviceIntent = new Intent(WelcomeActivity.this, ZwaveControlService.class);
            bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);
        }
    };

    public Runnable checkInitStatus = new Runnable() {
        @Override
        public void run() {
            while (!DeviceInfo.isOpenControllerFinish) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (mqttBrokerFlag) {initSensorfunc();}
            initZwave();
        }
    };

    private void initZwave() {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                timerCancel();
                Intent intent = new Intent();
                intent.setClass(mContext, HomeActivity.class);
                hideProgressDialog();
                mContext.startActivity(intent);
                finish();
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

    private static void alertDialogCancel() {
        if(alertDialog!=null){
            alertDialog.dismiss();
            alertDialog=null;
        }
    }

    private static void timerCancel() {
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
                zwaveService.register(mCallback);
                zwaveService.register(mReqCallBacks);
                requestControlUSBPermission();

                initSensorfunc();
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
        zwaveUnregister();
    }

    public static ZwaveControlService.zwaveCallBack mCallback = new ZwaveControlService.zwaveCallBack() {

        @Override
        public void zwaveControlResultCallBack(String className, String result) {
            Log.i(LOG_TAG, "Result class name = [" + DeviceInfo.className + "] | result = " + DeviceInfo.result);
            /*
            while(DeviceInfo.mqttFlag) {
                try {
                    Log.d(LOG_TAG,"wait for mqtt finish !!!!!!!!!!!!!!!!!!!!!!!!");
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            */
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
            DeviceInfo.mqttFlag = true;
        }
    };

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
                        Log.d("USB", "permission denied for device " + device);
                        progressDialog.cancel();
                        finish();
                        System.exit(0);
                    }
                }
            }
        }
    }

    private void openController() {
        timer.schedule(new mTimerTask(), 1000 * 120);
        String openResult = zwaveService.doOpenController();
        if (openResult.contains(":0")){
            DeviceInfo.isOpenControllerFinish = true;
        }
    }

    private void initSensorfunc() {

        List<ZwaveDevice> list = zwDevManager.queryZwaveDeviceList();

        if (list.size() == 1){
            return;
        }

        for (int idx = 1; idx < list.size(); idx++) {

            int nodeId = list.get(idx).getNodeId();
            String devCate = list.get(idx).getCategory();

            Log.i(LOG_TAG,"#"+nodeId+" | devCate = "+devCate);

            if (devCate.equals("SENSOR")) {
                String devNodeInfo = list.get(idx).getNodeInfo();

                if (devNodeInfo.contains("COMMAND_CLASS_BATTERY")) {
                    Log.i(LOG_TAG, "BATTERY");
                    zwaveService.getDeviceBattery(Const.zwaveType,nodeId);
                }

                zwaveService.getSensorNotification(nodeId, 0x00, 0x01, 0x00);
                zwaveService.getSensorNotification(nodeId, 0x00, 0x05, 0x00);
                zwaveService.getSensorNotification(nodeId, 0x00, 0x06, 0x00);
                zwaveService.getSensorNotification(nodeId, 0x00, 0x07, 0x00);
                /*
                if (devNodeInfo.contains("COMMAND_CLASS_NOTIFICATION")) {
                    try {
                        JSONObject jsonObject = new JSONObject(devNodeInfo);
                        if (jsonObject.getString("Product id").equals("001F")) {
                            //Water
                            v
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
                */
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

    private void zwaveUnregister() {
        new Thread(){
            @Override
            public void run(){
                zwaveService.unregister(mCallback);
                zwaveService.unregister(mReqCallBacks);
                unbindService(conn);
                if (usbReceiver != null) {
                    Log.d(LOG_TAG,"gino unregisterReceiver");
                    unregisterReceiver(usbReceiver);
                }
            }
        }.start();
    }
}
