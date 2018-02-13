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
import com.askey.firefly.zwave.control.dao.ZwaveDeviceGroup;
import com.askey.firefly.zwave.control.dao.ZwaveDeviceGroupManager;
import com.askey.firefly.zwave.control.dao.ZwaveDeviceManager;
import com.askey.firefly.zwave.control.dao.ZwaveDeviceRoom;
import com.askey.firefly.zwave.control.dao.ZwaveDeviceRoomManager;
import com.askey.firefly.zwave.control.dao.ZwaveDeviceScene;
import com.askey.firefly.zwave.control.dao.ZwaveScene;
import com.askey.firefly.zwave.control.dao.ZwaveSceneManager;
import com.askey.firefly.zwave.control.dao.ZwaveSchedule;
import com.askey.firefly.zwave.control.dao.ZwaveScheduleManager;
import com.askey.firefly.zwave.control.jni.ZwaveControlHelper;
import com.askey.firefly.zwave.control.scheduler.ScheduleJobManager;
import com.askey.firefly.zwave.control.thirdparty.usbserial.UsbSerial;
import com.askey.firefly.zwave.control.utils.DeviceInfo;
import com.askey.firefly.zwave.control.utils.Logg;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.askey.firefly.zwave.control.utils.Const.FILE_PATH;
import static com.askey.firefly.zwave.control.utils.Const.SAVE_NODEINFO_FILE;
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

    private static String LOG_TAG = ZwaveControlService.class.getSimpleName();
    public static ZwaveControlService mService;
    private int flag;
    private final String zwaveType = "Zwave";
    private final String btType = "BT";
    private ZwaveDeviceManager zwaveDeviceManager;
    private ZwaveScheduleManager zwSchManager;
    private ScheduleJobManager scheduleJobManager;
    private ZwaveDeviceGroupManager devGroupManager;
    private ZwaveSceneManager sceneManager;
    private ZwaveDeviceRoomManager roomManager;
    private static ArrayList <zwaveCallBack> mCallBacks = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        new UsbSerial(this);
        mService = this;

        ImportData.importFile(this,"app.cfg");
        ImportData.importFile(this,"zipgateway.cfg");
        ImportData.importFile(this,"Portal.ca_x509.pem");
        ImportData.importFile(this,"ZIPR.key_1024.pem");
        ImportData.importFile(this,"ZIPR.x509_1024.pem");
        ImportData.importFile(this,"zwave_device_rec.txt");
        ImportData.importDatabase(this);

        zwaveDeviceManager = ZwaveDeviceManager.getInstance(this);
        zwSchManager = ZwaveScheduleManager.getInstance(this);
        scheduleJobManager = ScheduleJobManager.getInstance(this);
        devGroupManager = ZwaveDeviceGroupManager.getInstance(this);
        sceneManager = ZwaveSceneManager.getInstance(this);
        roomManager = ZwaveDeviceRoomManager.getInstance(this);
    }

    public static ZwaveControlService getInstance() {
        if (mService != null) {
            return mService;
        }
        return null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Logg.i(LOG_TAG, "=====onBind=========");

        int creatResult = ZwaveControlHelper.CreateZwController(); //测试返回0
        if (creatResult == 0) {
            Logg.i(LOG_TAG, "==CreateZwController=creatResult=" + creatResult);
        } else {
            Logg.e(LOG_TAG, "==CreateZwController=creatResult=" + creatResult);
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
        Logg.i(LOG_TAG, "=====onDestroy=========");
        super.onDestroy();
    }

    private void initZwave(){

        Log.i(LOG_TAG,"================= initZwave ================= ");
        List<ZwaveDevice> list = zwaveDeviceManager.queryZwaveDeviceList();

        for (int idx = 1; idx < list.size(); idx++) {
            int nodeId = list.get(idx).getNodeId();
            String devType = list.get(idx).getDevType();
            String cate = list.get(idx).getCategory();

            if (devType.equals(zwaveType) && cate.equals("SENSOR")) {
                String devNodeInfo = list.get(idx).getNodeInfo();

                if (devNodeInfo!=null && devNodeInfo.contains("COMMAND_CLASS_BATTERY")) {
                    Log.i(LOG_TAG, "BATTERY");
                    getDeviceBattery(zwaveType,nodeId);
                }

                if (devNodeInfo!=null && devNodeInfo.contains("COMMAND_CLASS_SENSOR_MULTILEVEL")) {
                    try {
                        getSensorMultiLevel(zwaveType,nodeId);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            } else if (devType.equals(zwaveType) && cate.equals("PLUG")) {

                getMeter(zwaveType,nodeId,0x00);
                getMeter(zwaveType,nodeId,0x02);
                getMeter(zwaveType,nodeId,0x05);
                //getConfiguration(nodeId, 0, 7, 0, 0);

                getMeterSupported(nodeId);
                GetSensorBinarySupportedSensor(nodeId);
            }
        }
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

    public void addDevice(String devType,byte[] dskNumber){
        if (devType.equals(zwaveType)) {
             ZwaveControlHelper.ZwController_AddDevice(dskNumber, dskNumber.length);
        } else if (devType.equals(btType)){
            /*
            try {
                btControlService.getScanDeviceList();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            */
        }
    }

    public void removeDevice(String devType, int nodeId){
        Log.i(LOG_TAG,"removeDevice devType = "+devType+" | nodeId = "+nodeId);
        if (devType.equals(zwaveType)) {
            ZwaveControlHelper.ZwController_RemoveDevice();
        } else if (devType.equals(btType)){
            /*
            try {
                btControlService.deleteDevice(nodeId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            */
        }
    }

    public void removeDeviceFromRoom(int deviceId){
        ZwaveDevice zwaveDevice = zwaveDeviceManager.queryZwaveDevices(deviceId);
        zwaveDevice.setRoomName("");
    }

    public void getDeviceList(String Room){

        List<ZwaveDevice> list;

        if (Room.equals("ALL")) {
            list = zwaveDeviceManager.queryZwaveDeviceList();
        }
        else {
            list = zwaveDeviceManager.getRoomDevicesList(Room);
        }

        Log.i(LOG_TAG, "LIST SIZE = " + list.size());

        JSONObject jo = new JSONObject();
        JSONArray Jarray= new JSONArray();

        try {
            jo.put("Interface","getDeviceList");
            for (int idx = 0; idx < list.size(); idx++) {
                if (list.get(idx).getNodeId()!=1) {
                    JSONObject json = new JSONObject();
                    json.put("brand", list.get(idx).getBrand());
                    json.put("nodeId", String.valueOf(list.get(idx).getNodeId()));
                    json.put("deviceType", list.get(idx).getDevType());
                    json.put("name", list.get(idx).getName());
                    json.put("category", list.get(idx).getCategory());
                    json.put("Room", list.get(idx).getRoomName());
                    json.put("isFavorite", list.get(idx).getFavorite());
                    json.put("timestamp", list.get(idx).getTimestamp());
                    //json.put("nodeInfo", list.get(idx).getNodeInfo());
                    Jarray.put(json);
                }
            }
            jo.put("deviceList",Jarray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        zwaveControlResultCallBack("getDeviceList", jo.toString());
    }

    public void getFavoriteList(){

        List<ZwaveDevice> list;

        list = zwaveDeviceManager.getDeviceIsFavorite("1");
        Log.i(LOG_TAG, "LIST SIZE = " + list.size());

        JSONObject jo = new JSONObject();
        JSONArray Jarray= new JSONArray();

        try {
            jo.put("Interface","getFavoriteList");
            for (int idx = 0; idx < list.size(); idx++) {
                JSONObject json = new JSONObject();
                json.put("brand",list.get(idx).getBrand());
                json.put("nodeId",String.valueOf(list.get(idx).getNodeId()));
                json.put("deviceType",list.get(idx).getDevType());
                json.put("name",list.get(idx).getName());
                json.put("category",list.get(idx).getCategory());
                json.put("Room",list.get(idx).getRoomName());
                json.put("isFavorite",list.get(idx).getFavorite());
                json.put("timestamp",list.get(idx).getTimestamp());
                //json.put("nodeInfo",list.get(idx).getNodeInfo());
                Jarray.put(json);
            }
            jo.put("deviceList",Jarray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        zwaveControlResultCallBack("getFavoriteList", jo.toString());
    }

    public void editFavoriteList(ArrayList<String> addNode, ArrayList<String> removeNode){
        Log.i(LOG_TAG,"editFavoriteList");
        for (int idx=0; idx<addNode.size(); idx++){
            ZwaveDevice zwaveDevice = zwaveDeviceManager.queryZwaveDevices(Integer.valueOf(addNode.get(idx)));
            if (zwaveDevice != null) {
                zwaveDevice.setFavorite("1");
            }
            zwaveDeviceManager.updateZwaveDevice(zwaveDevice);
        }
        for (int idx=0; idx<removeNode.size(); idx++){
            ZwaveDevice zwaveDevice = zwaveDeviceManager.queryZwaveDevices(Integer.valueOf(removeNode.get(idx)));
            if (zwaveDevice != null) {
                zwaveDevice.setFavorite("0");
            }
            zwaveDeviceManager.updateZwaveDevice(zwaveDevice);
        }

        JSONObject jo = new JSONObject();
        try {
            jo.put("Interface","editFavoriteList");
            jo.put("Result","true");
            zwaveControlResultCallBack("editFavoriteList", jo.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getRecentDeviceList(int num){
        List<ZwaveDevice> list = zwaveDeviceManager.getRecentDeviceList(num);

        JSONObject jo = new JSONObject();
        JSONArray Jarray= new JSONArray();

        try {
            jo.put("Interface","getRecentDeviceList");
            for (int idx = 0; idx < list.size(); idx++) {
                if (list.get(idx).getNodeId()!=1) {
                    JSONObject json = new JSONObject();
                    json.put("brand", list.get(idx).getBrand());
                    json.put("nodeId", String.valueOf(list.get(idx).getNodeId()));
                    json.put("deviceType", list.get(idx).getDevType());
                    json.put("name", list.get(idx).getName());
                    json.put("category", list.get(idx).getCategory());
                    json.put("Room", list.get(idx).getRoomName());
                    json.put("isFavorite", list.get(idx).getFavorite());
                    json.put("timestamp", list.get(idx).getTimestamp());
                    //json.put("nodeInfo",list.get(idx).getNodeInfo());
                    Jarray.put(json);
                }
            }
            jo.put("deviceList",Jarray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        zwaveControlResultCallBack("getRecentDeviceList", jo.toString());
    }

    public void getRooms(){
        Log.i(LOG_TAG,"getRooms START");
        List <ZwaveDeviceRoom> roomList = roomManager.getRoom();
        JSONObject jo = new JSONObject();
        JSONArray Jarray= new JSONArray();
        try {
            jo.put("Interface","getRooms");
            for (int idx = 0; idx < roomList.size(); idx++) {
                JSONObject json = new JSONObject();
                json.put("name",roomList.get(idx).getRoomName());
                Jarray.put(json);
            }
            jo.put("roomList",Jarray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        zwaveControlResultCallBack("getRooms", jo.toString());
    }

    public void addRoom(String roomName){

        ZwaveDeviceRoom zwaveDeviceRoom = new ZwaveDeviceRoom();
        zwaveDeviceRoom.setRoomName(roomName);
        roomManager.insertDeviceRoom(zwaveDeviceRoom);

        JSONObject jo = new JSONObject();
        try {
            jo.put("Interface","addRoom");
            jo.put("status","Success");
            zwaveControlResultCallBack("addRoom", jo.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void editRoom(String oriName, String tarName){

        JSONObject jo = new JSONObject();
        try {
            jo.put("Interface","editRoom");
            if (oriName.equals("My Home")){
                jo.put("status","fail");
            } else {
                zwaveDeviceManager.changeRoomName(oriName,tarName);
                roomManager.changeRoomName(oriName,tarName);
                jo.put("status","Success");
            }
            zwaveControlResultCallBack("editRoom", jo.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void removeRoom(String roomName){

        JSONObject jo = new JSONObject();
        try {
            jo.put("Interface","removeRoom");
            if (roomName.equals("My Home")){
                jo.put("status","fail");
            } else {
                roomManager.deleteRoom(roomName);
                jo.put("status","Success");
            }
            zwaveControlResultCallBack("removeRoom", jo.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getDeviceInfo(){
        return ZwaveControlHelper.ZwController_GetDeviceInfo();
    }

    public int removeFailedDevice(int deviceId){
        return ZwaveControlHelper.ZwController_RemoveFailedDevice(deviceId);
    }

    public int replaceFailedDevice(int deviceId){
        byte[] aaa = new byte[0];
        return ZwaveControlHelper.ZwController_ReplaceFailedDevice(deviceId,aaa,aaa.length);
    }

    public void stopAddDevice(String devType){
        if (devType.equals(zwaveType)) {
            ZwaveControlHelper.ZwController_StopAddDevice();
        }
    }

    public void stopRemoveDevice(String devType){
        if (devType.equals(zwaveType)) {
            ZwaveControlHelper.ZwController_StopRemoveDevice();
        }
    }

    public void getDeviceBattery(String devType, int deviceId){

        updateTimestamp(deviceId);

        Log.i(LOG_TAG,"=====getDeviceBattery==deviceId==="+deviceId+"| devType = "+devType);
        if (devType.equals(zwaveType)) {
            ZwaveControlHelper.ZwController_GetDeviceBattery(deviceId);
        }else if (devType.equals(btType)){
            //
        }else{
            Log.d(LOG_TAG,"getDeviceBattery with wrong deviceType");
        }
    }

    public void getSensorMultiLevel(String devType, int deviceId) throws RemoteException {
        updateTimestamp(deviceId);

        Log.i(LOG_TAG,"=====getSensorMultiLevel==deviceId==="+deviceId);

        if (devType.equals(zwaveType)) {
            ZwaveControlHelper.ZwController_GetSensorMultiLevel(deviceId);
        } else if (devType.equals(btType)){
            //btControlService.
        }
    }

    public int setDefault(){
        return ZwaveControlHelper.ZwController_SetDefault();
    }

    public int getConfiguration(int deviceId, int paramMode, int paramNumber, int rangeStart, int rangeEnd){
        updateTimestamp(deviceId);
        return ZwaveControlHelper.ZwController_GetConfiguration(deviceId, paramMode, paramNumber, rangeStart, rangeEnd);
    }

    public void setConfiguration(int deviceId, int paramNumber, int paramSize, int useDefault, int paramValue) throws RemoteException {
        String result = "false";
        updateTimestamp(deviceId);
        int res = ZwaveControlHelper.ZwController_SetConfiguration(deviceId, paramNumber, paramSize, useDefault, paramValue);
        if (res==0) {result = "true";}
        JSONObject jsonResult = new JSONObject();
        try {
            jsonResult.put("Interface","setConfiguration");
            jsonResult.put("NodeId",new Integer(deviceId));
            jsonResult.put("devType",zwaveType);
            jsonResult.put("Result", result);

            zwaveControlResultCallBack("setConfiguration",jsonResult.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getSupportedSwitchType(int deviceId){
        updateTimestamp(deviceId);
        return ZwaveControlHelper.ZwController_GetSupportedSwitchType(deviceId);
    }

    public int startStopSwitchLevelChange(String homeId, int deviceId, int startLvlVal, int duration, int pmyChangeDir, int secChangeDir, int secStep){
        updateTimestamp(deviceId);
        int result = ZwaveControlHelper.ZwController_startStopSwitchLevelChange(deviceId,startLvlVal,duration,pmyChangeDir,secChangeDir,secStep);
        zwaveControlResultCallBack("startStopSwitchLevelChange",String.valueOf(result));
        return result;
    }

    public int getPowerLevel(int deviceId){
        updateTimestamp(deviceId);
        return ZwaveControlHelper.ZwController_GetPowerLevel(deviceId);
    }

    public void setSwitchAllOn(String devType, int deviceId){

        Log.i(LOG_TAG,"=====setSwitchAllOn==deviceId==="+deviceId+"| devType = "+devType);
        updateTimestamp(deviceId);

        if (devType.equals(zwaveType)) {
            int result = ZwaveControlHelper.ZwController_SetSwitchAllOn(deviceId);
            zwaveControlResultCallBack("setSwitchAllOn", String.valueOf(result));
        } else if (devType.equals(btType)) {
            /*
            if (zwaveDeviceManager.getDeviceCategory(deviceId).equals("PLUG")) {

                try {
                    btControlService.setPlugOnOff(deviceId,true);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }else{
                try {
                    btControlService.setLampOnOff(deviceId,true);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            */
        }
    }

    public void setSwitchAllOff(String devType, int deviceId){
        updateTimestamp(deviceId);
        if (devType.equals(zwaveType)) {
            int result = ZwaveControlHelper.ZwController_SetSwitchAllOff(deviceId);
            zwaveControlResultCallBack("setSwitchAllOff",String.valueOf(result));
        } else if (devType.equals(btType)){
            //btControlService.
        }
    }

    public void getBasic(String devType, int deviceId){
        updateTimestamp(deviceId);
        if (devType.equals(zwaveType)) {
            ZwaveControlHelper.ZwController_GetBasic(deviceId);
        }else if (devType.equals(btType)){
            //btControlService
        }
    }
    public void setBasic(String devType, int deviceId, int value){
        String result = "false";
        updateTimestamp(deviceId);
        Log.i(LOG_TAG,"setBasic device ID="+deviceId + "value="+value);
        if (devType.equals(zwaveType)) {
            int res = ZwaveControlHelper.ZwController_SetBasic(deviceId, value);
            if (res == 0){
                result = "true";
            }
        } else if (devType.equals(btType)){
            //btControlService
        }
        JSONObject jsonResult = new JSONObject();
        try {
            jsonResult.put("Interface","setSwitchStatus");
            jsonResult.put("NodeId",new Integer(deviceId));
            jsonResult.put("devType",zwaveType);
            jsonResult.put("Result", result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        zwaveControlResultCallBack("setBasic", jsonResult.toString());
    }

    public void getSwitchMultiLevel(String devType, int deviceId){
        updateTimestamp(deviceId);
        if (devType.equals(zwaveType)) {
            ZwaveControlHelper.ZwController_GetSwitchMultiLevel(deviceId);
        } else if (devType.equals(btType)){
            //btControlService.
        }
    }

    public void setSwitchMultiLevel(String devType,int deviceId, int value, int duration){
        String result = "false";
        updateTimestamp(deviceId);
        Log.i(LOG_TAG,"setSwitchMultiLevel device ID="+deviceId + "| value="+value+" | devType = "+devType+"|duration = "+duration );
        if (devType.equals(zwaveType)) {
            int res = ZwaveControlHelper.ZwController_SetSwitchMultiLevel(deviceId, value, 1);
            if (res == 0){
                result = "true";
            }
        } else if (devType.equals(btType)) {
            //btControlService.
        }
        JSONObject jsonResult = new JSONObject();
        try {
            jsonResult.put("Interface","setBrigtness");
            jsonResult.put("NodeId",new Integer(deviceId));
            jsonResult.put("devType",zwaveType);
            jsonResult.put("Result", result);

            zwaveControlResultCallBack("setSwitchMultiLevel",jsonResult.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void getMeter(String devType, int deviceId, int meterUnit){
        updateTimestamp(deviceId);
        if (devType.equals(zwaveType)) {
            Log.i(LOG_TAG,"getMeter #"+deviceId+" , " +meterUnit);
            ZwaveControlHelper.ZwController_GetMeter(deviceId, meterUnit);
        } else if (devType.equals(btType)){
            /*
            try {
                btControlService.getPlugPower(deviceId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            */
        }
    }

    public int getMeterSupported(int deviceId){
        updateTimestamp(deviceId);
        int result = ZwaveControlHelper.ZwController_getMeterSupported(deviceId);
        return result;
    }

    public int getSensorBasic(int deviceId, int sensorType){
        Logg.i(LOG_TAG,"=====getSensorBasic==deviceId==="+deviceId+"sensor_type="+sensorType);
        int result = ZwaveControlHelper.ZwController_GetSensorBinary(deviceId,sensorType);
        return result;
    }

    public int GetSensorBinarySupportedSensor(int deviceId){
        int result = ZwaveControlHelper.ZwController_GetSensorBinarySupportedSensor(deviceId);
        return result;
    }

    public void setLampToWarmWhite(String devType, int deviceId){
        updateTimestamp(deviceId);
        if (devType.equals(zwaveType)){
            ZwaveControlHelper.ZwController_setSwitchColor(deviceId,0x00,255);
        } else if (devType.equals(btType)){

        }
        JSONObject jsonResult = new JSONObject();
        try {
            jsonResult.put("Interface","setLampColor");
            jsonResult.put("NodeId",new Integer(deviceId));
            jsonResult.put("devType",zwaveType);
            jsonResult.put("Result", "true");

            zwaveControlResultCallBack("setLampColor",jsonResult.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setLampToColdWhite(String devType, int deviceId){
        updateTimestamp(deviceId);
        if (devType.equals(zwaveType)){
            ZwaveControlHelper.ZwController_setSwitchColor(deviceId,0x01,255);
            JSONObject jsonResult = new JSONObject();
            try {
                jsonResult.put("Interface","setLampColor");
                jsonResult.put("NodeId",new Integer(deviceId));
                jsonResult.put("devType",zwaveType);
                jsonResult.put("Result", "true");

                zwaveControlResultCallBack("setLampColor",jsonResult.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (devType.equals(btType)){

        }
    }

    public void setLampColor(String devType, int deviceId, int r_value, int g_value, int b_value){
        updateTimestamp(deviceId);
        if (devType.equals(zwaveType)){

            ZwaveControlHelper.ZwController_setSwitchColor(deviceId,0x02,r_value);
            ZwaveControlHelper.ZwController_setSwitchColor(deviceId,0x03,g_value);
            ZwaveControlHelper.ZwController_setSwitchColor(deviceId,0x04,b_value);
            ZwaveControlHelper.ZwController_setSwitchColor(deviceId,0x00,0);
            ZwaveControlHelper.ZwController_setSwitchColor(deviceId,0x01,0);

            JSONObject jsonResult = new JSONObject();
            try {
                jsonResult.put("Interface","setLampColor");
                jsonResult.put("NodeId",new Integer(deviceId));
                jsonResult.put("devType",zwaveType);
                jsonResult.put("Result", "true");

                zwaveControlResultCallBack("setLampColor",jsonResult.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            zwaveControlResultCallBack("setSwitchMultiLevel",jsonResult.toString());

        } else if (devType.equals(btType)){
            /*
            try {
                btControlService.setLampRGB(deviceId,r_value,g_value,b_value);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            */
        }
    }

    public void getLampColor(String devType, int deviceId){
        updateTimestamp(deviceId);
        if (devType.equals(zwaveType)){
            ZwaveControlHelper.ZwController_getSwitchColor(deviceId,0x02);
            ZwaveControlHelper.ZwController_getSwitchColor(deviceId,0x03);
            ZwaveControlHelper.ZwController_getSwitchColor(deviceId,0x04);

        } else if (devType.equals(btType)){
            /*
            try {
                btControlService.getLampRGB(deviceId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            */
        }
    }

    public int getSensorNotification(int deviceId, int alarm_type, int notif_type, int status){
        updateTimestamp(deviceId);
        Log.i(LOG_TAG,"=====getSensorNotification==deviceId==="+deviceId+"alarm_type="+alarm_type+"notif_type="+notif_type+"status="+status);
        int result = ZwaveControlHelper.ZwController_getNotification(deviceId, alarm_type, notif_type, status);
        return result;
    }

    public void getGroupInfo(String devType, int deviceId ,int maxGroupId){
        Log.i(LOG_TAG,"=====getGroupInfo==deviceId==="+deviceId);
        updateTimestamp(deviceId);

        List<Integer> list;
        list = devGroupManager.getZwaveDeviceGroupListByNodeId(deviceId);
        Log.i(LOG_TAG, "GROUP LIST SIZE = " + list.size());

        JSONObject jo = new JSONObject();
        JSONArray Jarray= new JSONArray();

        try {
            jo.put("Interface","getGroupInfo");
            jo.put("NodeId",String.valueOf(deviceId));
            jo.put("devType",devType);
            for (int idx = 0; idx < list.size(); idx++) {

                JSONObject json = new JSONObject();
                json.put("Group id", list.get(idx));

                List <ZwaveDeviceGroup> groupList = devGroupManager.getZwaveDeviceGroupListByNodeIdAndGroupId(deviceId,list.get(idx));
                Log.i(LOG_TAG, "#"+deviceId+" G#"+list.get(idx)+" GROUP NODEID LIST SIZE = " + groupList.size());

                JSONArray nodeArray= new JSONArray();
                for (int _idx = 0; _idx< groupList.size();_idx++) {

                    JSONObject nodeJson = new JSONObject();
                    nodeJson.put("controlNodeId", String.valueOf(groupList.get(_idx).getInGroupNodeId()));
                    nodeArray.put(nodeJson);
                }
                json.put("Group members",nodeArray);
                json.put("Max Supported endpoints", "5");
                json.put("endpoint id", "0");
                Jarray.put(json);
            }
            jo.put("GroupInfo",Jarray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        zwaveControlResultCallBack("GroupInfo", jo.toString());
    }

    public void addEndpointsToGroup(String devType, int deviceId, int groupId, int[] arr, int endpointId){
        //updateTimestamp(deviceId);
        Log.i(LOG_TAG,"=====addEndpointsToGroup== deviceId="+deviceId+"| groupId="+groupId+"| endpointId="+endpointId);


        ZwaveControlHelper.ZwController_getGroupInfo(deviceId,deviceId,0);

        int[] nodeifid = new int[arr.length];
        nodeifid[arr.length-1] = arr[arr.length-1];
        Log.i(LOG_TAG,"arr.length = "+arr.length);
        for (int idx=0;idx<(arr.length -1);idx++){
            nodeifid[idx]= getDeviceEnpointInterfaceId(arr[idx]);
            Log.i(LOG_TAG,"arr["+idx+"] = "+arr[idx]+" | nodeifid["+idx+"] = "+nodeifid[idx]);
        }

        int result = ZwaveControlHelper.ZwController_addEndpointsToGroup(deviceId,groupId,nodeifid,0);
        Log.i(LOG_TAG,"addEndpointsToGroup result = "+result);

        JSONObject jsonResult = new JSONObject();
        try {
            jsonResult.put("Interface","addEndpointsToGroup");
            jsonResult.put("devType",devType);
            jsonResult.put("NodeId",new Integer(deviceId));
            if (result == 0 ) {
                jsonResult.put("result","true");

                for (int idx=0;idx<arr.length-1;idx++){
                    ZwaveDeviceGroup zwaveDeviceGroup = new ZwaveDeviceGroup();
                    zwaveDeviceGroup.setNodeId(deviceId);
                    zwaveDeviceGroup.setGroupId(groupId);
                    zwaveDeviceGroup.setEndpointId(endpointId);
                    zwaveDeviceGroup.setInGroupNodeId(arr[idx]);
                    zwaveDeviceGroup.setInterfaceId(nodeifid[idx]);
                    Log.i(LOG_TAG,"addEndpointsToGroup #"+idx+" nodeid="+deviceId+ " groupnodeid = "+arr[idx] +" groupIfid"+nodeifid[idx]);
                    devGroupManager.addDeviceGroup(zwaveDeviceGroup);
                }
            }else{
                jsonResult.put("result","false");
            }
            zwaveControlResultCallBack("addEndpointsToGroup", jsonResult.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void removeEndpointsFromGroup(String devType, int deviceId, int groupId, int[] arr, int endpointId) {
        updateTimestamp(deviceId);
        Log.i(LOG_TAG, "=====removeEndpointsFromGroup==deviceId=" + deviceId + "| groupId=" + groupId + "| endpointId=" + endpointId);

        ZwaveControlHelper.ZwController_getGroupInfo(deviceId,deviceId,0);

        int[] nodeifid = new int[arr.length];
        nodeifid[arr.length-1] = arr[arr.length-1];
        for (int idx=0;idx<arr.length-1;idx++){
            nodeifid[idx]= getDeviceEnpointInterfaceId(arr[idx]);
            Log.i(LOG_TAG,"arr["+idx+"] = "+arr[idx]+" | nodeifid["+idx+"] = "+nodeifid[idx]);
        }

        int result = ZwaveControlHelper.ZwController_removeEndpointsFromGroup(deviceId, groupId, nodeifid, endpointId);
        Log.i(LOG_TAG,"removeEndpointsFromGroup result = "+result);

        JSONObject jsonResult = new JSONObject();
        try {
            jsonResult.put("Interface", "removeEndpointsFromGroup");
            jsonResult.put("devType", devType);
            jsonResult.put("NodeId", new Integer(deviceId));
            if (result == 0 ) {
                jsonResult.put("result","true");
                for (int idx=0;idx<arr.length-1;idx++){
                    Log.i(LOG_TAG,"#"+idx+" | del groupId="+arr[idx]+" | interfaceId = "+nodeifid[idx]);
                    devGroupManager.deleteZwaveDeviceGroupByInGropNodeId(arr[idx]);

                }
            }else{
                jsonResult.put("result","false");
            }
            zwaveControlResultCallBack("removeEndpointsFromGroup", jsonResult.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getMaxSupportedGroups(int deviceId,int endpointId){
        Logg.i(LOG_TAG,"=====getMaxSupportedGroups==deviceId==="+deviceId+"===endpointId="+endpointId);
        int result = ZwaveControlHelper.ZwController_getMaxSupportedGroups(deviceId,endpointId);
        return result;
    }

    public int getSpecificGroup(int deviceId,int endpointId){
        Logg.i(LOG_TAG,"=====getSpecificGroup==deviceId==="+deviceId);
        int result = ZwaveControlHelper.ZwController_getSpecificGroup(deviceId,endpointId);
        return result;
    }

    public int getSupportedNotification(int deviceId){
        Logg.i(LOG_TAG,"=====getSupportedNotification==deviceId==="+deviceId);
        int result = ZwaveControlHelper.ZwController_getSupportedNotification(deviceId);
        return result;
    }

    public int getSupportedEventNotification(int deviceId, int typeDef){
        Logg.i(LOG_TAG,"=====ZwController_getSupportedEventNotification==deviceId==="+deviceId+"===typeDef==="+typeDef);
        int result = ZwaveControlHelper.ZwController_getSupportedEventNotification(deviceId,typeDef);
        return result;
    }

    public void getScheduleList(String devType,int deviceId){
        updateTimestamp(deviceId);

        List<ZwaveSchedule> list = zwSchManager.getZwaveScheduleList(deviceId);

        JSONObject jo = new JSONObject();
        JSONArray Jarray= new JSONArray();
        try {
            jo.put("Interface","getScheduleList");
            jo.put("nodeId",String.valueOf(deviceId));
            jo.put("devType",devType);

            if ( list.size()!=0 ) {
                jo.put("active",list.get(0).getActive());
                jo.put("variableValue",list.get(0).getVariableValueStart());
                for (int idx = 0; idx < list.size(); idx++) {
                    JSONObject json = new JSONObject();
                    json.put("dayOfWeek",list.get(idx).getDay());
                    json.put("StartTime",list.get(idx).getStartTime());
                    json.put("EndTime",list.get(idx).getEndTime());
                    Jarray.put(json);
                }
                jo.put("day",Jarray);
            } else {
                jo.put("active","false");
                jo.put("variableValue","0");
                JSONObject json = new JSONObject();
                Jarray.put(json);
                jo.put("day",Jarray);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        zwaveControlResultCallBack("getScheduleList", jo.toString());
    }

    public void setSchedule(String devType, int deviceId, String Day,
                            String startTime, String endTime, int value, String active){
        updateTimestamp(deviceId);

        List<ZwaveSchedule> list = zwSchManager.getZwaveScheduleList(deviceId);
        if (list.size()!=0) {
            for (int idx = 0; idx < list.size(); idx++) {
                if (list.get(idx).getDay().equals(Day)){
                    Log.i(LOG_TAG,"deleteZwaveSchedule #"+list.get(idx).getNodeId()+" : JobId "+list.get(idx).getJobId());
                    scheduleJobManager.cancelSchedule(list.get(idx).getJobId());
                    break;
                }
            }
        }

        Log.i(LOG_TAG,"setSchedule #"+deviceId+" | Day = "+Day +" | value = "+value+" | active = "+active);

        int endValue;
        if (value==0){
            endValue = 255;
        } else {
            value = 255;
            endValue =0;
        }

        ZwaveSchedule tmpSch = new ZwaveSchedule();
        tmpSch.setNodeId(deviceId);
        tmpSch.setStartTime(startTime);
        tmpSch.setEndTime(endTime);
        tmpSch.setVariableValueStart(value);
        tmpSch.setActive(active);
        scheduleJobManager.addSchedule(deviceId,value,endValue,startTime,endTime,Day,active);

        JSONObject jo = new JSONObject();

        try {
            jo.put("Interface","setSchedule");
            jo.put("nodeId",String.valueOf(deviceId));
            jo.put("devType",devType);
            jo.put("result","true");
            zwaveControlResultCallBack("setSchedule", jo.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void removeSchedule(String devType, int deviceId, String Day){
        updateTimestamp(deviceId);

        List<ZwaveSchedule> list = zwSchManager.getZwaveScheduleList(deviceId);

        JSONObject jo = new JSONObject();
        try {
            jo.put("Interface","removeSchedule");
            jo.put("nodeId",String.valueOf(deviceId));
            jo.put("devType",devType);
            if (list != null) {
                for (int idx = 0; idx < list.size(); idx++) {
                    if (list.get(idx).getDay().equals(Day)){
                        zwSchManager.deleteZwaveSchedule(list.get(idx).getJobId());

                        jo.put("result","true");
                        break;
                    }
                }
            }else{
                jo.put("result","false");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        zwaveControlResultCallBack("removeSchedule", jo.toString());
    }

    public void setScheduleActive(String devType, int deviceId, String active){
        updateTimestamp(deviceId);

        zwSchManager.updateZwaveScheduleActive(deviceId,active);
        scheduleJobManager.setScheduleActive(deviceId,active);
        JSONObject jo = new JSONObject();

        try {
            jo.put("Interface","setScheduleActive");
            jo.put("nodeId",String.valueOf(deviceId));
            jo.put("devType",devType);
            jo.put("result","true");
            zwaveControlResultCallBack("setScheduleActive", jo.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getScene(){
        List<ZwaveScene> sceneList = sceneManager.getZwaveScene();
        if (sceneList!=null){
            for (int idx=0; idx<sceneList.size(); idx++){
                String sceneName = sceneList.get(idx).getSceneName();
                String sceneIcon = sceneList.get(idx).getSceneIcon();
                Log.i(LOG_TAG,"#"+idx+" sceneName = "+sceneName+" | sceneIcon = "+sceneIcon);
                List<ZwaveDeviceScene> devSceneList = sceneList.get(idx).getZwaveDeviceSceneList();
                if (devSceneList!=null){
                    for (int _idx=0;_idx<devSceneList.size();_idx++){
                        int NodeId = devSceneList.get(_idx).getNodeId();
                        String timerTime = devSceneList.get(_idx).getTimerTime();


                    }
                }
            }
        }
    }

    public int closeController(){
        return ZwaveControlHelper.CloseZwController();
    }

    public void editNodeInfo(String brand, int deviceId, String newName, String devType, String category,
                             String roomName, String isFavorite) {

        boolean result;
        ZwaveDevice zwaveDevice = zwaveDeviceManager.queryZwaveDevices(deviceId);

        if (zwaveDevice != null) {
            Log.i(LOG_TAG, "=====zwaveDevice.editNodeInfo("+deviceId+")====");
            Log.i(LOG_TAG, "=====zwaveDevice.editNodeInfo(setCategory)====="+category);
            java.util.Date date = new java.util.Date();
            zwaveDevice.setBrand(brand);
            zwaveDevice.setName(newName);
            zwaveDevice.setDevType(devType);
            zwaveDevice.setRoomName(roomName);
            zwaveDevice.setCategory(category);
            zwaveDevice.setFavorite(isFavorite);
            zwaveDevice.setTimestamp(date.getTime());
            zwaveDeviceManager.updateZwaveDevice(zwaveDevice);
            initZwaveDevfunc(deviceId);
            result = true;
        } else {
            result = false;
        }

        JSONObject jsonResult = new JSONObject();
        try {
            jsonResult.put("Interface","editNodeInfo");
            jsonResult.put("devType",devType);
            jsonResult.put("NodeId",new Integer(deviceId));
            jsonResult.put("Result",String.valueOf(result));
            zwaveControlResultCallBack("reNameDevice", "reNameDevice:" + jsonResult);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int  getControllerRssi(){
        return ZwaveControlHelper.ZwController_getControllerNetworkRssiInfo();
    }

    public interface zwaveCallBack {
        void zwaveControlResultCallBack(String className, String result);
    }

    private void zwaveControlResultCallBack(String className, String result) {
        for (zwaveCallBack callback : mCallBacks) {
            callback.zwaveControlResultCallBack(className, result);
        }
    }

    public String doOpenController() {
        Log.i(LOG_TAG, "=====doOpenController=========");
        if (mCallBacks == null) {
            return null;
        }
        byte[] result = new byte[500];
        int isOK = ZwaveControlHelper.OpenZwController(FILE_PATH, SAVE_NODEINFO_FILE, result);

        Log.i(LOG_TAG, "===isOK==" + isOK);
        String openResult = "openController:" + isOK;

        String tmpString = new String(result);

        zwaveControlResultCallBack("openController", new String(tmpString.substring(0, tmpString.indexOf("}") + 1)));
        return openResult;
    }

    private String getDeviceInfo(String Result) {
        JSONObject deviceListResult;
        try {
            deviceListResult = new JSONObject(Result);
            JSONArray list = deviceListResult.getJSONArray("Node Info List");
            for (int i = 0; i < list.length(); i++) {
                JSONObject temp = list.getJSONObject(i);
                String nodeId = temp.getString("Node id");
                ZwaveDevice zwaveDevice = zwaveDeviceManager.queryZwaveDevices(Integer.parseInt(nodeId));
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

    private void deleteDevice(String devType, String result) {
        Log.i(LOG_TAG, "=======deleteDevice==");
        Gson gson = new Gson();
        DeviceList deviceList = gson.fromJson(result, DeviceList.class);
        List<DeviceList.NodeInfoList> temp = deviceList.getNodeList();
        List<ZwaveDevice> list = zwaveDeviceManager.queryZwaveDeviceList();
        Log.i(LOG_TAG, "===deleteDevice====temp.size()==" + temp.size());
        Log.i(LOG_TAG, "==deleteDevice=====list.size()==" + list.size());
        int i;
        JSONObject jsonObject = null;
        String removeResult = null;
        for (ZwaveDevice zwaveDevice : list) {//db
            i = 0;
            for (DeviceList.NodeInfoList nodeInfoTemp : temp) {//jni

                //if (zwaveDevice.getHomeId().toString().trim().equals(nodeInfoTemp.getHomeId().toString().trim())) {
                if (!zwaveDevice.getNodeId().toString().trim().equals(nodeInfoTemp.getNodeId().toString().trim())) {
                    i++;
                }
                //}
                // remove grop DB
                if (temp.size() == i) {
                    if (zwaveDevice.getCategory().equals("WALLMOTE")){
                        devGroupManager.deleteZwaveDeviceGroupByNodeId(zwaveDevice.getNodeId());
                    }else{
                        devGroupManager.deleteZwaveDeviceGroupByInGropNodeId(zwaveDevice.getNodeId());
                    }
                    // remove schedule DB
                    List<ZwaveSchedule> scheduleList = zwSchManager.getZwaveScheduleList(zwaveDevice.getNodeId());

                    if (list != null) {
                        for (int idx = 0; idx < scheduleList.size(); idx++) {
                            scheduleJobManager.cancelSchedule(scheduleList.get(idx).getJobId());
                        }
                    }

                    removeResult = "removeDevice:"+devType+ ":" + zwaveDevice.getNodeId();
                    Log.i(LOG_TAG, "==deleteDevice=="+zwaveDevice.getCategory()+"===removeResult==" + removeResult);
                    zwaveDeviceManager.deleteZwaveDevice(zwaveDevice.getZwaveId());

                    zwaveControlResultCallBack("removeDevice", removeResult);
                }
            }
        }
        if (removeResult == null){
            removeResult = "removeDevice:"+devType+ ":" + "fail";
            Log.e(LOG_TAG, "This zwaveDevice does not exist in DB");
            zwaveControlResultCallBack("removeDevice", removeResult);
        }
    }

    private  int getDeviceEnpointInterfaceId(int nodeId) {
        Log.i(LOG_TAG,"getDeviceEnpointInterfaceId #"+nodeId);
        ZwaveDevice zwaveDevice = zwaveDeviceManager.queryZwaveDevices(nodeId);
        Log.i(LOG_TAG,"getDeviceEnpointInterfaceId #"+nodeId+" interfaceId = "+zwaveDevice.getInterfaceId());
        return zwaveDevice.getInterfaceId();
    }

    private int setEndpointInterfaceId(String nodeInfo){

        try {
            JSONObject json = new JSONObject(nodeInfo);

            String tmpEndpointInfo = json.optString("EndPoint List");
            JSONArray endpointInfo = new JSONArray(tmpEndpointInfo);

            for(int _idx=0; _idx< endpointInfo.length(); _idx++){
                JSONObject jjson = endpointInfo.getJSONObject(_idx);

                if (jjson.getInt("Endpoint id") ==0 ){
                    Log.i(LOG_TAG,"Endpoint0 = " + jjson.getInt("Endpoint interface id"));
                    return jjson.getInt("Endpoint interface id");
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void insertDevice(String devType, String result, int status) {
        Log.i(LOG_TAG, "=======insertDevice==");
        Gson gson = new Gson();
        DeviceList deviceList = gson.fromJson(result, DeviceList.class);
        List<DeviceList.NodeInfoList> temp = deviceList.getNodeList();
        Log.i(LOG_TAG, "==insertDevice=====temp.size()==" + temp.size());
        for (DeviceList.NodeInfoList nodeInfoTemp : temp) {

            ZwaveDevice device = zwaveDeviceManager.queryZwaveDevices(Integer.parseInt(nodeInfoTemp.getNodeId()));

            if (device == null) {
                Log.i(LOG_TAG, "==insertDevice====device#"+nodeInfoTemp.getNodeId());
                ZwaveDevice zwaveDevice = new ZwaveDevice();
                zwaveDevice.setBrand("");
                zwaveDevice.setNodeId(Integer.valueOf(nodeInfoTemp.getNodeId()));
                zwaveDevice.setNodeInfo(gson.toJson(nodeInfoTemp));
                zwaveDevice.setName(nodeInfoTemp.getNodeId());
                zwaveDevice.setDevType("");
                zwaveDevice.setCategory("unknown");
                zwaveDevice.setRoomName("");
                zwaveDevice.setFavorite("0");
                zwaveDevice.setInterfaceId(setEndpointInterfaceId(gson.toJson(nodeInfoTemp).toString()));
                zwaveDeviceManager.insertZwaveDevice(zwaveDevice);

                Log.i(LOG_TAG, "===#########==" + nodeInfoTemp.getNodeId());
                Log.i(LOG_TAG, "===####nodeInfoTemp.getNodeId().equals(1)#####==" + nodeInfoTemp.getNodeId().equals("1"));
                Log.i(LOG_TAG, "===#########==" + nodeInfoTemp.getNodeId().toString().trim().equals("1"));
                if (!nodeInfoTemp.getNodeId().toString().trim().equals("1")) {

                    updateTimestamp(Integer.valueOf(nodeInfoTemp.getNodeId()));
                    if (status == 2) {
                        String addResult = "addDevice:"+devType+":" + nodeInfoTemp.getNodeId();
                        Log.i(LOG_TAG, "===insertDevice====addResult==" + addResult);
                        zwaveControlResultCallBack("addDevice", addResult);
                        break;
                    }
                }
            } else {
                Log.i(LOG_TAG, "==insertDevice====device==" + device.getNodeId());
            }
        }
    }

    private void updateTimestamp(int deviceId){
        ZwaveDevice zwaveDevice = zwaveDeviceManager.queryZwaveDevices(deviceId);
        java.util.Date date = new java.util.Date();
        zwaveDevice.setTimestamp(date.getTime());
        zwaveDeviceManager.updateZwaveDevice(zwaveDevice);
    }

    private void initZwaveDevfunc(int nodeId){
        ZwaveDevice zwSensor = zwaveDeviceManager.queryZwaveDevices(nodeId);
        if (zwSensor.getDevType().equals(zwaveType)) {
            String devNodeInfo = zwSensor.getNodeInfo();

            if (devNodeInfo != null && devNodeInfo.contains("COMMAND_CLASS_BATTERY")) {
                Log.i(LOG_TAG, "BATTERY");
                getDeviceBattery(zwaveType,nodeId);
            }

            if (devNodeInfo != null && devNodeInfo.contains("COMMAND_CLASS_SENSOR_MULTILEVEL")) {
                try {
                    getSensorMultiLevel(zwaveType,nodeId);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            if (zwSensor.getCategory().equals("PLUG")){

                getMeter(zwaveType,nodeId,0x00);
                getMeter(zwaveType,nodeId,0x02);
                getMeter(zwaveType,nodeId,0x05);
                getConfiguration(nodeId,0,7,0,0);

                getMeterSupported(nodeId);
                GetSensorBinarySupportedSensor(nodeId);
            }
        }
    }

    public void zwaveCallBack(byte[] result, int len) {
        // jni callback
        String jniResult = new String(result);
        Logg.showLongLog(LOG_TAG, "zwaveCallBack jniResult===" + jniResult);
        JSONObject jsonObject = null;
        String messageType = null;
        String status = null;
        try {
            jsonObject = new JSONObject(jniResult);
            messageType = jsonObject.optString("MessageType");
            status = jsonObject.optString("Status");

        } catch (JSONException e) {
            Log.i(LOG_TAG, "JSONException");
            e.printStackTrace();
        }

        if ("Node Add Status".equals(messageType)) {
            zwaveControlResultCallBack("addDevice", jniResult);
            if ("Success".equals(status)) {
                flag = 2;
                Log.i(LOG_TAG, "=======Node Add Status=Success=");
                ZwaveControlHelper.ZwController_saveNodeInfo(SAVE_NODEINFO_FILE);
                ZwaveControlHelper.ZwController_GetDeviceInfo();
            }
        } else if ("Node Remove Status".equals(messageType)) {
            zwaveControlResultCallBack("removeDevice", jniResult);
            if ("Success".equals(status)) {
                Log.i(LOG_TAG, "=======Node Remove Status=Success=");
                flag = 3;
                ZwaveControlHelper.ZwController_saveNodeInfo(SAVE_NODEINFO_FILE);
                ZwaveControlHelper.ZwController_GetDeviceInfo();
            }
        } else if (messageType.equals("All Node Info Report")) {
            if (flag == 0) {
                String jsonResult = getDeviceInfo(jniResult);

                try {
                    JSONObject payload = new JSONObject(jniResult);

                    String listArray = payload.optString("Node Info List");

                    if (listArray.contains("Endpoint interface id")) {
                        JSONArray endpointArray = new JSONArray(listArray);

                        Log.i(LOG_TAG,"endpointArray.length() = "+endpointArray.length());
                        for(int idx=0; idx< endpointArray.length(); idx++){
                            JSONObject json = endpointArray.getJSONObject(idx);
                            String tmpEndpointInfo = json.optString("EndPoint List");
                            int NodeId = json.getInt("Node id");

                            JSONArray endpointInfo = new JSONArray(tmpEndpointInfo);

                            for(int _idx=0; _idx< endpointArray.length(); _idx++){
                                JSONObject jjson = endpointInfo.getJSONObject(_idx);

                                if (jjson.getInt("Endpoint id") ==0 && NodeId!=0){
                                    ZwaveDevice zwaveDevice = zwaveDeviceManager.queryZwaveDevices(NodeId);
                                    if (zwaveDevice!=null) {
                                        zwaveDevice.setInterfaceId(jjson.getInt("Endpoint interface id"));
                                        Log.i(LOG_TAG," #" + NodeId + " Endpoint0 = " + zwaveDevice.getInterfaceId());
                                        zwaveDeviceManager.updateZwaveDevice(zwaveDevice);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                insertDevice(zwaveType,jniResult,flag);
                zwaveControlResultCallBack("getDeviceInfo", jsonResult);
            } else if (flag == 2) {
                insertDevice(zwaveType,jniResult,flag);
                flag = 0;
            } else if (flag == 3) {
                flag = 0;
                deleteDevice(zwaveType,jniResult);
            }
        /*
        } else if (messageType.equals("All Node Info Report")) {
            zwaveControlResultCallBack("removeFail", jniResult);
            if (jniResult.contains("Success")) {
                ZwaveControlHelper.ZwController_saveNodeInfo(SAVE_NODEINFO_FILE);
            }
        } else if (messageType.equals("All Node Info Report")) {
            zwaveControlResultCallBack("replaceFail", jniResult);
            if (jniResult.contains("Success")) {
                ZwaveControlHelper.ZwController_saveNodeInfo(SAVE_NODEINFO_FILE);
            }
        */
        } else if ("Node Battery Value".equals(messageType)) {
            zwaveControlResultCallBack("getDeviceBattery", jniResult);
        } else if ("Sensor Information".equals(messageType)) {
            zwaveControlResultCallBack("getSensorMultiLevel", jniResult);
            //} else if (messageType.equals("All Node Info Report")) {
            //    zwaveControlResultCallBack("updateNode", jniResult);
        } else if ("Configuration Get Information".equals(messageType)) {
            try {
                jsonObject = new JSONObject(jniResult);
                jsonObject.put("Interface","getConfigure");
                jsonObject.put("devType",zwaveType);
                zwaveControlResultCallBack("getConfiguration", jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if ("  ".equals(messageType)) {
            zwaveControlResultCallBack("getSupportedSwitchType", jniResult);
        } else if ("Power Level Get Information".equals(messageType)) {
            zwaveControlResultCallBack("getPowerLevel", jniResult);
        } else if ("Basic Information".equals(messageType)) {
            try {

                JSONObject payload = new JSONObject(jniResult);
                jsonObject = new JSONObject();

                jsonObject.put("Interface","getSwitchStatus");
                jsonObject.put("devType",zwaveType);
                jsonObject.put("Node",String.valueOf(payload.getInt("Node id")));

                String switchStatus = payload.getString("value");
                if (switchStatus.equals("00h")){
                    jsonObject.put("switchStatus","off");
                } else{
                    jsonObject.put("switchStatus","on");
                }
                zwaveControlResultCallBack("getBasic", jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if ("Meter report Information".equals(messageType)) {
            try {
                jsonObject = new JSONObject(jniResult);
                jsonObject.put("Interface","getPower");
                jsonObject.put("devType",zwaveType);
                zwaveControlResultCallBack("getMeter", jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if ("Generic Report Information".equals(messageType)) {

            try {
                JSONObject payload = new JSONObject(jniResult);
                jsonObject = new JSONObject();

                jsonObject.put("Interface","getBrightness");
                jsonObject.put("devType",zwaveType);
                jsonObject.put("Node",String.valueOf(payload.getInt("Node id")));

                String brightness = payload.getString("level");
                if (brightness.equals("00h")){
                    jsonObject.put("switchStatus","off");
                } else{
                    jsonObject.put("switchStatus","on");
                }
                jsonObject.put("brightness",String.valueOf(Integer.parseInt(brightness.substring(0,2), 16)));

                zwaveControlResultCallBack("getSwitchMultiLevel", jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if ("Switch Color Report".equals(messageType)) {
            try {
                jsonObject = new JSONObject(jniResult);
                jsonObject.put("Interface","getLampColor");
                jsonObject.put("devType",zwaveType);
                zwaveControlResultCallBack("getLampColor", jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (messageType.equals("Sensor Info Report")) {
            zwaveControlResultCallBack("getSensorMultiLevel", jniResult);
        } else if (messageType.equals("Notification Get Information")) {
            zwaveControlResultCallBack("getSensorNotification", jniResult);
        } else if (messageType.equals("Supported Groupings Report")) {
            try {
                jsonObject = new JSONObject(jniResult);
                jsonObject.put("Interface","getMaxSupportedGroups");
                jsonObject.put("devType",zwaveType);
                zwaveControlResultCallBack("getMaxSupportedGroups", jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (messageType.equals("Binary Sensor Support Get Information")) {
            zwaveControlResultCallBack("GetSensorBinarySupportedSensor", jniResult);
        } else if (messageType.equals("Notification Supported Report")) {
            zwaveControlResultCallBack("getSupportedNotification", jniResult);
        /*
        } else if (messageType.equals("Group Info Report")) {

            try {
                jsonObject = new JSONObject(jniResult);
                jsonObject.put("Interface","getGroupInfo");
                jsonObject.put("devType",zwaveType);
                zwaveControlResultCallBack("getGroupInfo", jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        */
        } else if (messageType.equals("Notification Supported Report")) {
            zwaveControlResultCallBack("getSupportedNotification", jniResult);
        } else if (messageType.equals("Controller Network RSSI Report")) {
            zwaveControlResultCallBack("getControllerRssi", jniResult);
        } else if ("DSK Report".equals(messageType)) {
            zwaveControlResultCallBack("DSK", jniResult);
        }

    }
}