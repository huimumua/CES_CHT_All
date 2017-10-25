package com.askey.firefly.zwave.control.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.askey.firefly.zwave.control.bean.DeviceList;
import com.askey.firefly.zwave.control.dao.ImportData;
import com.askey.firefly.zwave.control.dao.ZwaveDevice;
import com.askey.firefly.zwave.control.dao.ZwaveDeviceManager;
import com.askey.firefly.zwave.control.jni.ZwaveControlHelper;
import com.askey.firefly.zwave.control.thirdparty.usbserial.UsbSerial;
import com.askey.firefly.zwave.control.utils.Logg;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.askey.firefly.zwave.control.utils.Const.FILE_PATH;
import static com.askey.firefly.zwave.control.utils.Const.SAVE_NODEINFO_FILE;
import static com.askey.firefly.zwave.control.utils.Const.ZWCONTROL_CFG_PATH;
/**
 * 项目名称：ZwaveControl
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2017/7/10 11:36
 * 修改人：skysoft
 * 修改时间：2017/7/10 11:36
 * 修改备注：
 */
public class ZwaveControlService extends Service {

    private final String TAG = "ZwaveControlService";
    private UsbSerial mUsbSerial = new UsbSerial(this);
    public static ZwaveControlService mService;
    private int flag;
    private int nodeId;
    private ZwaveDeviceManager zwaveDeviceManager;
    private static ArrayList <zwaveCallBack> mCallBacks = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mService = this;
        ImportData.importFile(this,"zw_api.cfg");
        ImportData.importFile(this,"device_settings.csv");
        ImportData.importDatabase(this);
        zwaveDeviceManager = ZwaveDeviceManager.getInstance(this);
    }

    public static ZwaveControlService getInstance() {
        if (mService != null) {
            return mService;
        }
        return null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Logg.i(TAG, "=====onBind=========");

        int creatResult = ZwaveControlHelper.CreateZwController(); //测试返回0
        if(creatResult==0){
            Logg.i(TAG,"==CreateZwController=creatResult="+creatResult);
        }else{
            Logg.e(TAG,"==CreateZwController=creatResult="+creatResult);
        }
        return myBinder;
    }

    public MyBinder myBinder = new MyBinder();

    public class MyBinder extends Binder {
        public ZwaveControlService getService() {
            return ZwaveControlService.this;
        }
    }


    @Override
    public void onDestroy() {
        Logg.i(TAG, "=====onDestroy=========");
        super.onDestroy();
    }

    public void register(zwaveCallBack callback){
        mCallBacks.add(callback);
    }

    public void unregister(zwaveCallBack callback){
        mCallBacks.remove(callback);
    }

    public String openController(){
        return doOpenController();
    }

    public int addDevice(){
        return ZwaveControlHelper.ZwController_AddDevice();
    }

    public int removeDevice(){
        return ZwaveControlHelper.ZwController_RemoveDevice();
    }

    public int getDevices(){
        return ZwaveControlHelper.ZwController_GetDeviceList();
    }

    public int getDeviceInfo(int deviceId){
        return ZwaveControlHelper.ZwController_GetDeviceInfo(deviceId);
    }

    public int removeFailedDevice(int deviceId){
        return ZwaveControlHelper.ZwController_RemoveFailedDevice(deviceId);
    }

    public int replaceFailedDevice(int deviceId){
        return ZwaveControlHelper.ZwController_ReplaceFailedDevice(deviceId);
    }

    public int stopAddDevice(){
        return ZwaveControlHelper.ZwController_StopAddDevice();
    }

    public int stopRemoveDevice(){
        return ZwaveControlHelper.ZwController_StopRemoveDevice();
    }

    public int getDeviceBattery(int deviceId){
        Logg.i(TAG,"=====getDeviceBattery==deviceId==="+deviceId);
        return ZwaveControlHelper.ZwController_GetDeviceBattery(deviceId);
    }

    public int getSensorMultiLevel(int deviceId) throws RemoteException {
        Logg.i(TAG,"=====getSensorMultiLevel==deviceId==="+deviceId);
        return ZwaveControlHelper.ZwController_GetSensorMultiLevel(deviceId);
    }

    public int updateNode(int deviceId){
        Logg.i(TAG,"=====updateNode==deviceId==="+deviceId);
        return ZwaveControlHelper.ZwController_UpdateNode(deviceId);
    }

    public String reNameDevice(String homeId, int deviceId, String newName, String devType){
        //数据库
        Logg.i(TAG,"=====reNameDevice==homeId==="+homeId +"=deviceId=="+deviceId +"=newName=="+newName+"=devType=="+devType);
        return updateName(homeId,deviceId,newName,devType);
    }

    public int setDefault(){
        return ZwaveControlHelper.ZwController_SetDefault();
    }

    public int getConfiguration(int deviceId, int paramMode, int paramNumber, int rangeStart, int rangeEnd){
        return ZwaveControlHelper.ZwController_GetConfiguration(deviceId, paramMode, paramNumber, rangeStart, rangeEnd);
    }

    public int setConfiguration(int deviceId, int paramNumber, int paramSize, int useDefault, int paramValue) throws RemoteException {
        int result = ZwaveControlHelper.ZwController_SetConfiguration(deviceId, paramNumber, paramSize, useDefault, paramValue);
        zwaveControlResultCallBack("setConfiguration",String.valueOf(result));
        return result;
    }

    public int getSupportedSwitchType(int deviceId){
        return ZwaveControlHelper.ZwController_GetSupportedSwitchType(deviceId);
    }

    public int startStopSwitchLevelChange(String homeId, int deviceId, int startLvlVal, int duration, int pmyChangeDir, int secChangeDir, int secStep){
        int result = ZwaveControlHelper.ZwController_startStopSwitchLevelChange(deviceId,startLvlVal,duration,pmyChangeDir,secChangeDir,secStep);
        zwaveControlResultCallBack("startStopSwitchLevelChange",String.valueOf(result));
        return result;
    }

    public int getPowerLevel(int deviceId){
        return ZwaveControlHelper.ZwController_GetPowerLevel(deviceId);
    }

    public int setSwitchAllOn(int deviceId){
        int result = ZwaveControlHelper.ZwController_SetSwitchAllOn(deviceId);
        zwaveControlResultCallBack("setSwitchAllOn",String.valueOf(result));
        return result;
    }

    public int setSwitchAllOff(int deviceId){
        int result = ZwaveControlHelper.ZwController_SetSwitchAllOff(deviceId);
        zwaveControlResultCallBack("setSwitchAllOff",String.valueOf(result));
        return result;
    }

    public int getBasic(int deviceId){
        return ZwaveControlHelper.ZwController_GetBasic(deviceId);
    }

    public int setBasic(int deviceId, int value){
        int result = ZwaveControlHelper.ZwController_SetBasic(deviceId,value);
        zwaveControlResultCallBack("setBasic",String.valueOf(result));
        return result;
    }

    public int getSwitchMultiLevel(int deviceId){
        return ZwaveControlHelper.ZwController_GetSwitchMultiLevel(deviceId);
    }

    public int setSwitchMultiLevel(int deviceId, int value, int duration){
        int result = ZwaveControlHelper.ZwController_SetSwitchMultiLevel(deviceId, value, duration);
        zwaveControlResultCallBack("setSwitchMultiLevel",String.valueOf(result));
        return result;
    }

    public int getMeter(int deviceId,int meterUnit){
        int result = ZwaveControlHelper.ZwController_GetMeter(deviceId,meterUnit);
        return result;
    }

    public int getMeterSupported(int deviceId){
        int result = ZwaveControlHelper.ZwController_getMeterSupported(deviceId);
        return result;
    }

    public int getSensorBasic(int deviceId, int sensorType){
        Logg.i(TAG,"=====getSensorBasic==deviceId==="+deviceId+"sensor_type="+sensorType);
        int result = ZwaveControlHelper.ZwController_GetSensorBinary(deviceId,sensorType);
        return result;
    }

    public int GetSensorBinarySupportedSensor(int deviceId){
        int result = ZwaveControlHelper.ZwController_GetSensorBinarySupportedSensor(deviceId);
        return result;
    }

    public int setSwitchColor(int deviceId, int parameter, int value){
        int result = ZwaveControlHelper.ZwController_setSwitchColor(deviceId,parameter,value);
        return result;
    }

    public int getSwitchColor(int deviceId, int parameter){
        int result = ZwaveControlHelper.ZwController_getSwitchColor(deviceId,parameter);
        return result;
    }

    public int getSensorNotification(int deviceId, int alarm_type, int notif_type, int status){
        Logg.i(TAG,"=====getSensorNotification==deviceId==="+deviceId+"alarm_type="+alarm_type+"notif_type="+notif_type+"status="+status);
        int result = ZwaveControlHelper.ZwController_getNotification(deviceId, alarm_type, notif_type, status);
        return result;
    }

    public int closeController(){
        return ZwaveControlHelper.CloseZwController();
    }

    private String updateName(String homeId, int deviceId, String newName, String devType) {
        Logg.i(TAG,"=====updateName==homeId==="+homeId +"=deviceId=="+deviceId +"=newName=="+newName+"=devType=="+devType);

        int result;
        ZwaveDevice zwaveDevice = zwaveDeviceManager.queryZwaveDevices(homeId, deviceId);
        if (zwaveDevice != null) {
            Logg.i(TAG,"=====zwaveDevice.setName(newName)====");
            zwaveDevice.setName(newName);
            zwaveDevice.setDevType(devType);
            zwaveDeviceManager.updateZwaveDevice(zwaveDevice);
            result = 0;
        } else {
            result = -1;
        }
        String reNameResult = "reNameDevice:"+result;
        Log.i(TAG,"reNameResult = "+reNameResult);
        return reNameResult;
    }

    public interface zwaveCallBack {

        void zwaveControlResultCallBack(String className, String result);
    }


    private void zwaveControlResultCallBack(String className, String result){

        //Log.i(TAG," === "+className+"CallBack ===" + result);
        for (zwaveCallBack callback : mCallBacks) {
            callback.zwaveControlResultCallBack(className,result);
        }
    }

    public String doOpenController() {
        Logg.i(TAG, "=====doOpenController=========");
        if (mCallBacks == null) {
            return null;
        }
        byte[] result = new byte[500];
        int isOK = ZwaveControlHelper.OpenZwController(ZWCONTROL_CFG_PATH, FILE_PATH, SAVE_NODEINFO_FILE,result);

        Logg.i(TAG,"===isOK=="+isOK);
        String openResult = "openController:"+isOK;

        String tmpString = new String( result );
        //Log.d("ZwaveControlService", "ZwaveControlService " + tmpString.substring(0, tmpString.indexOf("}")+1) + isOK);

        zwaveControlResultCallBack("openController",new String(tmpString.substring(0, tmpString.indexOf("}")+1)));
        return openResult;
    }

    private void insertHomeDevice(String result) {
        try {
            JSONObject json = new JSONObject(result);
           String homeId = json.optString("Home Id");
           int nodeId = json.optInt("Node Id");
            ZwaveDevice homeDevice = zwaveDeviceManager.queryZwaveDevices(homeId, nodeId);
            if (homeDevice == null) {
                if (!homeId.toString().trim().equals("") && nodeId == 1) {
                    ZwaveDevice device = new ZwaveDevice();
                    device.setHomeId(homeId);
                    device.setNodeId(nodeId);
                    device.setName(String.valueOf(nodeId));
                    zwaveDeviceManager.insertZwaveDevice(device);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void zwaveCallBack(byte[] result, int len)  {
        // jni callback
        String jniResult = new String(result);
        Logg.i(TAG," jniResult==="+jniResult);
        String messageType = null;
        String status = null;
        try {
            JSONObject jsonObject = new JSONObject(jniResult);
            messageType = jsonObject.optString("MessageType");
            status = jsonObject.optString("Status");

        } catch (JSONException e) {
            Log.i(TAG,"JSONExceptionJSONExceptionJSONException");
            e.printStackTrace();
        }

        if ("Node Add Status".equals(messageType)) {

            zwaveControlResultCallBack("addDevice",jniResult);
            if ("Success".equals(status)) {
               flag = 2;
                Logg.i(TAG,"=======Node Add Status=Success=");
                ZwaveControlHelper.ZwController_saveNodeInfo(SAVE_NODEINFO_FILE);
                ZwaveControlHelper.ZwController_GetDeviceList();
            }
        } else if ("Node Remove Status".equals(messageType)) {
            zwaveControlResultCallBack("removeDevice",jniResult);
            if ("Success".equals(status)) {
                Logg.i(TAG,"=======Node Remove Status=Success=");
                flag = 3;
                ZwaveControlHelper.ZwController_saveNodeInfo(SAVE_NODEINFO_FILE);
                ZwaveControlHelper.ZwController_GetDeviceList();
            }
        } else if ("Node List Report".equals(messageType)) {
            if (flag == 1) {
                flag = 0;
                zwaveControlResultCallBack("getDeviceInfo",getDeviceInfo(jniResult));
            } else if (flag == 0) {
                String jsonResult = getDeviceList(jniResult);//json 添加name
                zwaveControlResultCallBack("getDeviceList",jsonResult);
            } else if (flag == 2) {
                flag = 0;
                insertDevice(jniResult);
            } else if (flag == 3) {
                flag = 0;
                deleteDevice(jniResult);
            }
        } else if ("Node List Report".equals(messageType)) {/////
            zwaveControlResultCallBack("removeFail",jniResult);
            if(jniResult.contains("Success")){
                ZwaveControlHelper.ZwController_saveNodeInfo(SAVE_NODEINFO_FILE);
            }
        } else if ("Node List Report".equals(messageType)) {/////
            zwaveControlResultCallBack("replaceFail",jniResult);
            if(jniResult.contains("Success")){
                ZwaveControlHelper.ZwController_saveNodeInfo(SAVE_NODEINFO_FILE);
            }
        } else if ("Node List Report".equals(messageType)) {/////
            zwaveControlResultCallBack("stopAddDevice",jniResult);
        } else if ("Node List Report".equals(messageType)) {/////
            zwaveControlResultCallBack("stopRemoveDevice",jniResult);
        } else if ("Node Battery Value".equals(messageType)) {
            zwaveControlResultCallBack("getDeviceBattery",jniResult);
        } else if ("Sensor Information".equals(messageType)) {
            zwaveControlResultCallBack("getSensorMultiLevel",jniResult);
        } else if ("Node List Report".equals(messageType)) {/////
            zwaveControlResultCallBack("updateNode",jniResult);
        } else if ("Configuration Get Information".equals(messageType)) {
            zwaveControlResultCallBack("getConfiguration",jniResult);
        } else if ("  ".equals(messageType)) {////
            zwaveControlResultCallBack("getSupportedSwitchType",jniResult);
        } else if ("Power Level Get Information".equals(messageType)) {
            zwaveControlResultCallBack("getPowerLevel",jniResult);
        } else if ("Basic Information".equals(messageType)) {
            zwaveControlResultCallBack("getBasic",jniResult);
        } else if ("Meter report Information".equals(messageType)){
            zwaveControlResultCallBack("getMeter",jniResult);
        } else if ("  ".equals(messageType)) {////
            zwaveControlResultCallBack("getSwitchMultiLevel", jniResult);
        } else if ("Switch Color Report".equals(messageType)){
            zwaveControlResultCallBack("getSwitchColor", jniResult);
        } else if (messageType.equals("Sensor Info Report")) {
            zwaveControlResultCallBack("getSensorMultiLevel", jniResult);
        } else if (messageType.equals("Notification Get Information")) {
            zwaveControlResultCallBack("getSensorNotification", jniResult);
        }
    }

    private String getDeviceList(String Result) {
        JSONObject deviceListResult;
        try {
            deviceListResult = new JSONObject(Result);
            JSONArray list = deviceListResult.getJSONArray("Node Info List");
            for (int i = 0; i < list.length();i++ ) {
                JSONObject temp = list.getJSONObject(i);
                String homeId = temp.getString("Home id");
                String nodeId = temp.getString("Node id");
               ZwaveDevice zwaveDevice = zwaveDeviceManager.queryZwaveDevices(homeId, Integer.parseInt(nodeId));
                if (zwaveDevice != null) {
                    temp.put("deviceName", zwaveDevice.getName());
                } else {
                    temp.put("deviceName", "");
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return deviceListResult.toString();
    }

    private void deleteDevice(String result) {
        Logg.i(TAG,"=======deleteDevice==");
        Gson gson = new Gson();
        DeviceList deviceList =gson.fromJson(result, DeviceList.class);
        List<DeviceList.NodeInfoList> temp =  deviceList.getNodeList();
        List<ZwaveDevice> list = zwaveDeviceManager.queryZwaveDeviceList();
        Logg.i(TAG,"===deleteDevice====temp.size()=="+temp.size());
        Logg.i(TAG,"==deleteDevice=====list.size()=="+list.size());
        int i,j;
        for (ZwaveDevice zwaveDevice :list) {//db
            i = 0;
            for (DeviceList.NodeInfoList nodeInfoTemp : temp) {//jni

                if (zwaveDevice.getHomeId().toString().trim().equals(nodeInfoTemp.getHomeId().toString().trim())) {
                    if(!zwaveDevice.getNodeId().toString().trim().equals(nodeInfoTemp.getNodeId().toString().trim())){
                        i++;
                    }
                }

                if (temp.size() == i) {
                    zwaveDeviceManager.deleteZwaveDevice(zwaveDevice.getZwaveId());
                    String removeResult = "removeDevice:" + zwaveDevice.getHomeId() + ":" + zwaveDevice.getNodeId();
                    Logg.i(TAG,"==deleteDevice=====removeResult=="+removeResult);
                    zwaveControlResultCallBack("removeDevice",removeResult);
                }
            }
        }
    }

    private void insertDevice(String result) {
        Logg.i(TAG,"=======insertDevice==");
        Gson gson = new Gson();
        DeviceList deviceList =gson.fromJson(result, DeviceList.class);
        List<DeviceList.NodeInfoList> temp =  deviceList.getNodeList();
        Logg.i(TAG,"==insertDevice=====temp.size()=="+temp.size());
        for (DeviceList.NodeInfoList nodeInfoTemp : temp) {
            Logg.i(TAG,"==insertDevice====nodeInfoTemp.getHomeId()=="+nodeInfoTemp.getHomeId());
            Logg.i(TAG,"==insertDevice====nodeInfoTemp.getNodeId()=="+nodeInfoTemp.getNodeId());
            ZwaveDevice device = zwaveDeviceManager.queryZwaveDevices(nodeInfoTemp.getHomeId(),
                    Integer.parseInt(nodeInfoTemp.getNodeId()));
            Logg.i(TAG,"==insertDevice====device==");
            if (device == null ) {
                ZwaveDevice zwaveDevice = new ZwaveDevice();
                zwaveDevice.setHomeId(nodeInfoTemp.getHomeId());
                zwaveDevice.setNodeId(Integer.valueOf(nodeInfoTemp.getNodeId()));
                zwaveDevice.setNodeInfo(gson.toJson(nodeInfoTemp));
                zwaveDevice.setName(nodeInfoTemp.getNodeId());
                zwaveDeviceManager.insertZwaveDevice(zwaveDevice);
                Logg.i(TAG,"===#########=="+nodeInfoTemp.getNodeId());
                Logg.i(TAG,"===####nodeInfoTemp.getNodeId().equals(1)#####=="+nodeInfoTemp.getNodeId().equals("1"));
                Logg.i(TAG,"===#########=="+nodeInfoTemp.getNodeId().toString().trim().equals("1"));
                if(!nodeInfoTemp.getNodeId().toString().trim().equals("1")){
                    String addResult = "addDevice:" + nodeInfoTemp.getHomeId() + ":" + nodeInfoTemp.getNodeId();
                    Logg.i(TAG,"===insertDevice====addResult=="+addResult);
                    zwaveControlResultCallBack("addDevice",addResult);
                    break;
                }
            }else{
                Logg.i(TAG,"==insertDevice====device=="+device.getHomeId()+"===="+device.getNodeId());
            }
        }
    }

    private String getDeviceInfo(String jniResult) {
        Gson gson = new Gson();
        DeviceList deviceList =gson.fromJson(jniResult, DeviceList.class);
        List<DeviceList.NodeInfoList> temp =  deviceList.getNodeList();
        DeviceList.NodeInfoList nodeInfo = null;
        for (DeviceList.NodeInfoList nodeInfoTemp : temp) {
            if (nodeInfoTemp.getNodeId().toString().trim().equals(String.valueOf(nodeId).toString().trim())) {
                nodeInfo = nodeInfoTemp;
            }
        }
        return gson.toJson(nodeInfo);
    }

}