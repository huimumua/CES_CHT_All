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
import com.askey.firefly.zwave.control.utils.Const;
import com.askey.firefly.zwave.control.utils.DeviceInfo;
import com.askey.firefly.zwave.control.utils.Logg;
import com.askey.firefly.zwave.control.application.ZwaveProvisionList;

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
    private static ArrayList <zwaveControlReq_CallBack> mReqCallBacks = new ArrayList<>();


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
        ImportData.importFile(this,"zipgateway_provisioning_list.cfg");
        ImportData.importFile(this,"cmd_class.cfg");
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

    public void register(zwaveControlReq_CallBack callReqback){
        mReqCallBacks.add(callReqback);
    }

    public void unregister(zwaveCallBack callback){
        mCallBacks.remove(callback);
    }

    public void unregister(zwaveControlReq_CallBack callReqback){
        mReqCallBacks.remove(callReqback);
    }

    public String openController(){
        return doOpenController();
    }

    public int  StartLearnMode(){
        return ZwaveControlHelper.ZwController_StartLearnMode();
    }

    public int addDevice(String devType){
        //if (devType.equals(zwaveType)) {
        return ZwaveControlHelper.ZwController_AddDevice();
        //} else if (devType.equals(btType)){
            /*
            try {
                btControlService.getScanDeviceList();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            */
        //}
    }

    public int removeDevice(String devType, int nodeId){
        //Log.i(LOG_TAG,"removeDevice devType = "+devType+" | nodeId = "+nodeId);
        //if (devType.equals(zwaveType)) {
        return ZwaveControlHelper.ZwController_RemoveDevice();
        //} else if (devType.equals(btType)){
            /*
            try {
                btControlService.deleteDevice(nodeId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            */
        //}
    }

    public void addProvisionListEntry (String devType, byte[] dskNumber,String InclusionState, String bootMode) {
        if (devType.equals(zwaveType)) {
            //updateTimestamp(deviceId);
            /*
            String str = "11394-65466-64100-20934-53255-51784-15710-22718";
            byte[] bstr = str.getBytes();
            byte[] dsk = new byte[str.length()+1];

            for(int i = 0; i < bstr.length; ++i)
                dsk[i] = bstr[i];
            dsk[str.length()] = '\0';
            */
            ZwaveProvisionList[] plList = new ZwaveProvisionList[6];
            if(plList == null){
                Log.i(LOG_TAG,"======================");
            }

            for(int i = 0; i < 6; i++)
            {
                plList[i] = new ZwaveProvisionList();
            }


            if(InclusionState.equals("Pending")) {
                plList[0].setType(ZwaveProvisionList.PL_INFO_TYPE_INCL_STS);
                plList[0].setInclusionState(ZwaveProvisionList.PL_INCL_STS_PENDING);
                Log.d(LOG_TAG,"PENDING mode");
            } else if (InclusionState.equals("Passive")){
                plList[0].setType(ZwaveProvisionList.PL_INFO_TYPE_INCL_STS);
                plList[0].setInclusionState(ZwaveProvisionList.PL_INCL_STS_PASSIVE);
                Log.d(LOG_TAG,"PASSIVE mode");
            } else if (InclusionState.equals("Ignored")){
                plList[0].setType(ZwaveProvisionList.PL_INFO_TYPE_INCL_STS);
                plList[0].setInclusionState(ZwaveProvisionList.PL_INCL_STS_IGNORED);
                Log.d(LOG_TAG,"IGNORED mode");
            }

            if(bootMode.equals("Smart Start")) {
                plList[1].setType(ZwaveProvisionList.PL_INFO_TYPE_BOOT_MODE);
                plList[1].setBootMode(ZwaveProvisionList.PL_BOOT_MODE_SMART_STRT);
                Log.d(LOG_TAG,"BOOT_MODE_SMART_STRT");
            } else if (bootMode.equals("Security 2")){
                plList[1].setType(ZwaveProvisionList.PL_INFO_TYPE_BOOT_MODE);
                plList[1].setBootMode(ZwaveProvisionList.PL_BOOT_MODE_S2);
                Log.d(LOG_TAG,"BOOT_MODE_S2");
            }

            //plList[1].setType(ZwaveProvisionList.PL_INFO_TYPE_BOOT_MODE);
            //plList[1].setBootMode(ZwaveProvisionList.PL_BOOT_MODE_SMART_STRT);

            plList[2].setType(ZwaveProvisionList.PL_INFO_TYPE_NAME);
            plList[2].stProvisionList.name = "skysoft";

            plList[3].setType(ZwaveProvisionList.PL_INFO_TYPE_LOC);
            plList[3].stProvisionList.loc = "complany";

            plList[4].setType(ZwaveProvisionList.PL_INFO_TYPE_PROD_TYPE);
            plList[4].stProvisionList.pti.generic_cls = DeviceInfo.qrCodeDeviceType;
            plList[4].stProvisionList.pti.specific_cls = DeviceInfo.qrCodeDeviceType2;
            plList[4].stProvisionList.pti.icon_type = DeviceInfo.qrCodeIcon;

            plList[5].setType(ZwaveProvisionList.PL_INFO_TYPE_PROD_ID);
            plList[5].stProvisionList.pii.manf_id = DeviceInfo.qrCodeVendorId;
            plList[5].stProvisionList.pii.prod_type = DeviceInfo.qrCodeProcuctType;
            plList[5].stProvisionList.pii.prod_id = DeviceInfo.qrCodeProcuctId;
            plList[5].stProvisionList.pii.app_ver = DeviceInfo.qrCodeAppVersion;
            plList[5].stProvisionList.pii.app_sub_ver = DeviceInfo.qrCodeAppVersion2;
            Log.d(LOG_TAG,"plList = " + plList.toString());
            String result = "false";
            int res = ZwaveControlHelper.ZwController_addProvisionListEntry(dskNumber, dskNumber.length, plList,6);
            if (res == 0){
                result = "true";
            }

            JSONObject jsonResult = new JSONObject();
            try {
                jsonResult.put("Interface","addProvisionListEntry");
                //jsonResult.put("nodeId",new Integer(deviceId));
                //jsonResult.put("deviceType",zwaveType);
                jsonResult.put("result", result);

                zwaveControlResultCallBack("addProvisionListEntry",jsonResult.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

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

    public void rmProvisionListEntry (String devType,byte[] dskNumber) {
        if (devType.equals(zwaveType)) {
            /*
            String str = "11394-65466-64100-20934-53255-51784-15710-22718";
            byte[] bstr = str.getBytes();
            byte[] dsk = new byte[str.length()+1];

            for(int i = 0; i < bstr.length; ++i)
                dsk[i] = bstr[i];
            dsk[str.length()] = '\0';
            */

            String result = "false";
            int res =  ZwaveControlHelper.ZwController_rmProvisionListEntry(dskNumber, dskNumber.length);
            if (res == 0){
                result = "true";
            }

            JSONObject jsonResult = new JSONObject();
            try {
                jsonResult.put("Interface","rmProvisionListEntry");
                //jsonResult.put("nodeId",new Integer(deviceId));
                //jsonResult.put("deviceType",zwaveType);
                jsonResult.put("result", result);

                zwaveControlResultCallBack("rmProvisionListEntry",jsonResult.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

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

    public void getProvisionListEntry(String devType,byte[] dskNumber) {
        if (devType.equals(zwaveType)) {
            /*
            String str = "11394-65466-64100-20934-53255-51784-15710-22718";
            byte[] bstr = str.getBytes();
            byte[] dsk = new byte[str.length()+1];

            for(int i = 0; i < bstr.length; ++i)
                dsk[i] = bstr[i];
            dsk[str.length()] = '\0';
            */
            ZwaveControlHelper.ZwController_getProvisionListEntry(dskNumber, dskNumber.length);
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

    public void getAllProvisionListEntry(){
        ZwaveControlHelper.ZwController_getAllProvisionListEntry();
    }

    public void rmAllProvisionListEntry(){
        String result = "false";
        int res = ZwaveControlHelper.ZwController_rmAllProvisionListEntry();
        if (res >= 0){
            result = "true";
        }

        JSONObject jsonResult = new JSONObject();
        try {
            jsonResult.put("Interface","rmAllProvisionListEntry");
            //jsonResult.put("nodeId",new Integer(deviceId));
            //jsonResult.put("deviceType",zwaveType);
            jsonResult.put("result", result);

            zwaveControlResultCallBack("rmAllProvisionListEntry",jsonResult.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*
        public void getDeviceNetworkRssiInfo (int noid) {
            ZwaveControlHelper.ZwController_getDeviceNetworkRssiInfo(noid);
        }
    */
    public int startNetworkHealthCheck () {
        return ZwaveControlHelper.ZwController_startNetworkHealthCheck();
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

        //list = zwaveDeviceManager.getRoomDevicesList(Room);

        Log.i(LOG_TAG, "get room "+Room+ " | LIST SIZE = " + list.size());

        JSONObject jo = new JSONObject();
        JSONArray Jarray= new JSONArray();

        try {
            jo.put("Interface","getDeviceList");
            for (int idx = 0; idx < list.size(); idx++) {
                //if (list.get(idx).getNodeId()!=1) {
                JSONObject json = new JSONObject();
                json.put("brand", list.get(idx).getBrand());
                json.put("nodeId", String.valueOf(list.get(idx).getNodeId()));
                json.put("deviceType", list.get(idx).getDevType());
                json.put("name", list.get(idx).getName());
                json.put("category", list.get(idx).getCategory());
                json.put("room", list.get(idx).getRoomName());
                json.put("isFavorite", list.get(idx).getFavorite());
                json.put("timestamp", list.get(idx).getTimestamp());
                //json.put("nodeInfo", list.get(idx).getNodeInfo());
                Jarray.put(json);
                //}
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
                json.put("room",list.get(idx).getRoomName());
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
            //Log.i(LOG_TAG,"add editFavoriteList"+Integer.valueOf(addNode.get(idx)));

            if (zwaveDevice != null) {
                zwaveDevice.setFavorite("1");
            }
            zwaveDeviceManager.updateZwaveDevice(zwaveDevice);
        }
        for (int idx=0; idx<removeNode.size(); idx++){
            ZwaveDevice zwaveDevice = zwaveDeviceManager.queryZwaveDevices(Integer.valueOf(removeNode.get(idx)));
            //Log.i(LOG_TAG,"remove editFavoriteList"+Integer.valueOf(addNode.get(idx)));

            if (zwaveDevice != null) {
                zwaveDevice.setFavorite("0");
            }
            zwaveDeviceManager.updateZwaveDevice(zwaveDevice);
        }

        JSONObject jo = new JSONObject();
        try {
            jo.put("Interface","editFavoriteList");
            jo.put("result","true");
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
                    json.put("room", list.get(idx).getRoomName());
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
        //Log.i(LOG_TAG,"getRooms START");
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
            jo.put("result","true");
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
                jo.put("result","false");
            } else {
                zwaveDeviceManager.changeRoomName(oriName,tarName);
                roomManager.changeRoomName(oriName,tarName);
                jo.put("result","true");
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
                jo.put("result","false");
            } else {
                roomManager.deleteRoom(roomName);
                jo.put("result","true");
            }
            zwaveControlResultCallBack("removeRoom", jo.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getDeviceInfo(){
        Log.d(LOG_TAG,"into getDeviceInfo");
        return ZwaveControlHelper.ZwController_GetDeviceInfo();
    }

    public void getSpecifyDeviceInfo(int deviceId) {
        ZwaveControlHelper.ZwController_getSpecifyDeviceInfo(deviceId);
    }

    public int removeFailedDevice(int deviceId){
        return ZwaveControlHelper.ZwController_RemoveFailedDevice(deviceId);
    }

    public int replaceFailedDevice(int deviceId){
        return ZwaveControlHelper.ZwController_ReplaceFailedDevice(deviceId);
    }

    public int stopAddDevice(String devType){
        //if (devType.equals(zwaveType)) {
        return ZwaveControlHelper.ZwController_StopAddDevice();
        //}
    }

    public int stopRemoveDevice(String devType){
        //if (devType.equals(zwaveType)) {
        return  ZwaveControlHelper.ZwController_StopRemoveDevice();
        //}
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
            int result = ZwaveControlHelper.ZwController_GetSensorMultiLevel(deviceId);
            JSONObject jsonResult = new JSONObject();
            try {
                jsonResult.put("Interface","getSensorMultiLevel");
                //jsonResult.put("nodeId",new Integer(deviceId));
                //jsonResult.put("deviceType",zwaveType);
                jsonResult.put("result", result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            zwaveControlResultCallBack("getSensor", jsonResult.toString());
        } else if (devType.equals(btType)){
            //btControlService.
        }
    }

    public int setDefault(){
        return ZwaveControlHelper.ZwController_SetDefault();
    }

    public int checkNodeIsFailed(int deviceId) {
        return ZwaveControlHelper.ZwController_checkNodeIsFailed(deviceId);
    }

    public int getConfiguration(int deviceId, int paramMode, int paramNumber, int rangeStart, int rangeEnd){
        updateTimestamp(deviceId);
        return ZwaveControlHelper.ZwController_GetConfiguration(deviceId, paramMode, paramNumber, rangeStart, rangeEnd);
    }

    public int setConfiguration(int deviceId, int paramNumber, int paramSize, int useDefault, int paramValue) throws RemoteException {
        String result = "false";
        updateTimestamp(deviceId);
        return ZwaveControlHelper.ZwController_SetConfiguration(deviceId, paramNumber, paramSize, useDefault, paramValue);

        /*
        if (res==0) {result = "true";}
        JSONObject jsonResult = new JSONObject();
        try {
            jsonResult.put("Interface","setConfiguration");
            jsonResult.put("nodeId",new Integer(deviceId));
            jsonResult.put("deviceType",zwaveType);
            jsonResult.put("result", result);

            zwaveControlResultCallBack("setConfiguration",jsonResult.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        */
    }

    public int getSupportedSwitchType(int deviceId){
        updateTimestamp(deviceId);
        return ZwaveControlHelper.ZwController_GetSupportedSwitchType(deviceId);
    }

    public int startStopSwitchLevelChange(int deviceId, int startLvlVal, int duration, int pmyChangeDir, int secChangeDir, int secStep){
        updateTimestamp(deviceId);
        return ZwaveControlHelper.ZwController_startStopSwitchLevelChange(deviceId,startLvlVal,duration,pmyChangeDir,secChangeDir,secStep);
    }

    public int getPowerLevel(int deviceId){
        updateTimestamp(deviceId);
        return ZwaveControlHelper.ZwController_GetPowerLevel(deviceId);
    }

    public int setPowerLevel(int deviceId, int powerLvl, int timeout){
        updateTimestamp(deviceId);
        return ZwaveControlHelper.ZwController_SetPowerLevel(deviceId,powerLvl,timeout);
    }

    public int setSwitchAllOn(String devType, int deviceId){
        updateTimestamp(deviceId);
        //if (devType.equals(zwaveType)) {
        return ZwaveControlHelper.ZwController_SetSwitchAllOn(deviceId);
        //} else if (devType.equals(btType)) {

        //}
    }

    public int  setSwitchAll(String devType, int deviceId, int value){
        updateTimestamp(deviceId);
        //if (devType.equals(zwaveType)) {
        return ZwaveControlHelper.ZwController_SetSwitchAll(deviceId,value);
        //} else if (devType.equals(btType)) {

        //}
    }

    public int getSwitchAll(String devType, int deviceId){
        updateTimestamp(deviceId);
        //if (devType.equals(zwaveType)) {
        return ZwaveControlHelper.ZwController_GetSwitchAll(deviceId);
        //} else if (devType.equals(btType)) {

        //}
    }

    public int  setBinarySwitchState(String devType, int deviceId, int state){
        updateTimestamp(deviceId);
        //if (devType.equals(zwaveType)) {
        return ZwaveControlHelper.ZwController_SetBinarySwitchState(deviceId,state);
        //} else if (devType.equals(btType)) {

        //}
    }

    public void getBinarySwitchState(String devType, int deviceId){
        updateTimestamp(deviceId);
        if (devType.equals(zwaveType)) {
            ZwaveControlHelper.ZwController_GetBinarySwitchState(deviceId);
        } else if (devType.equals(btType)) {

        }
    }


    public int setSwitchAllOff(String devType, int deviceId){
        updateTimestamp(deviceId);
        //if (devType.equals(zwaveType)) {
        return ZwaveControlHelper.ZwController_SetSwitchAllOff(deviceId);
        //} else if (devType.equals(btType)){
        //btControlService.
        //}
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
            jsonResult.put("nodeId",new Integer(deviceId));
            //jsonResult.put("deviceType",zwaveType);
            jsonResult.put("result", result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        zwaveControlResultCallBack("setBasic", jsonResult.toString());
    }

    public void getSwitchMultiLevel(String devType, int deviceId){
        updateTimestamp(deviceId);
        //if (devType.equals(zwaveType)) {
        Log.i(LOG_TAG,"getSwitchMultiLevel device ID="+deviceId);
        ZwaveControlHelper.ZwController_GetSwitchMultiLevel(deviceId);
        //} else if (devType.equals(btType)){
            //btControlService.
        //}
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
            jsonResult.put("Interface","setBrightness");
            jsonResult.put("nodeId",new Integer(deviceId));
            //jsonResult.put("deviceType",zwaveType);
            jsonResult.put("result", result);

            zwaveControlResultCallBack("setSwitchMultiLevel",jsonResult.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void getSupportColor(String devType, int deviceId){
        updateTimestamp(deviceId);
        if (devType.equals(zwaveType)) {
            ZwaveControlHelper.ZwController_getSupportedSwitchColor(deviceId);
        } else if (devType.equals(btType)){
            //btControlService.
        }
    }

    public void startStopColorLevelChange(String devType, int deviceId, int dir, int ignore, int color_id, int start_value){
        updateTimestamp(deviceId);
        if (devType.equals(zwaveType)) {
            ZwaveControlHelper.ZwController_startStopSwitchColorLevelChange(deviceId,dir,ignore,color_id,start_value);
        } else if (devType.equals(btType)){
            //btControlService.
        }
    }


    public int getMeter(String devType,int deviceId, int meterUnit) {
        updateTimestamp(deviceId);
        Log.i(LOG_TAG, "getMeter #" + deviceId + " , " + meterUnit);
        return ZwaveControlHelper.ZwController_GetMeter(deviceId, meterUnit);
    }

    public int resetMeter(String devType, int deviceId){
        updateTimestamp(deviceId);
        //if (devType.equals(zwaveType)) {
        Log.i(LOG_TAG,"resetMeter #"+deviceId);
        return ZwaveControlHelper.ZwController_resetMeter(deviceId);
        //} else if (devType.equals(btType)){
            /*
            try {
                btControlService.getPlugPower(deviceId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            */
        //}
    }

    public int  getMeterSupported(int deviceId){
        updateTimestamp(deviceId);
        int result = ZwaveControlHelper.ZwController_getMeterSupported(deviceId);
        return result;
    }

    public int  getSecurity2CmdSupported(int deviceId){
        updateTimestamp(deviceId);
        int result = ZwaveControlHelper.ZwController_getSecurity2CmdSupported(deviceId);
        return result;
    }


    public void UpdateNode(int deviceId){
        updateTimestamp(deviceId);
        ZwaveControlHelper.ZwController_UpdateNode(deviceId);
    }

    public void getWakeUpSettings(int deviceId){
        updateTimestamp(deviceId);
        ZwaveControlHelper.ZwController_getWakeUpSettings(deviceId);
    }

    public void setWakeUpInterval(int deviceId, int interval){
        updateTimestamp(deviceId);
        ZwaveControlHelper.ZwController_setWakeUpInterval(deviceId, interval);
    }

    public void getDoorLockOperation(int deviceId){
        updateTimestamp(deviceId);
        ZwaveControlHelper.ZwController_getDoorLockOperation(deviceId);
    }

    public int  setDoorLockOperation(int deviceId, int mode){
        updateTimestamp(deviceId);
        return ZwaveControlHelper.ZwController_setDoorLockOperation(deviceId,mode);
    }

    public void getDoorLockConfiguration(int deviceId){
        updateTimestamp(deviceId);
        ZwaveControlHelper.ZwController_getDoorLockConfiguration(deviceId);
    }

    public int setDoorLockConfiguration(int deviceId, int type, int out_sta,
                                        int in_sta, int tmout_min, int tmout_sec) {
        updateTimestamp(deviceId);
        return ZwaveControlHelper.ZwController_setDoorLockConfiguration(deviceId,type,out_sta,in_sta,tmout_min,tmout_sec);
    }

    public void getProtection(int deviceId){
        updateTimestamp(deviceId);
        ZwaveControlHelper.ZwController_getProtection(deviceId);
    }

    public void setProtection(int deviceId, int localPortState, int rfPortState){
        updateTimestamp(deviceId);
        ZwaveControlHelper.ZwController_setProtection(deviceId,localPortState,rfPortState);
    }

    public void getSupportedProtection(int deviceId){
        updateTimestamp(deviceId);
        ZwaveControlHelper.ZwController_getSupportedProtection(deviceId);
    }

    public void getProtectionExcControlNode(int deviceId){
        updateTimestamp(deviceId);
        ZwaveControlHelper.ZwController_getProtectionExcControlNode(deviceId);
    }

    public void setProtectionExcControlNode(int deviceId, int control_nodeid){
        updateTimestamp(deviceId);
        ZwaveControlHelper.ZwController_setProtectionExcControlNode(deviceId,control_nodeid);
    }

    public void getProtectionTimeout(int deviceId){
        updateTimestamp(deviceId);
        ZwaveControlHelper.ZwController_getProtectionTimeout(deviceId);
    }

    public void setProtectionTimeout(int deviceId, int unit, int time){
        updateTimestamp(deviceId);
        ZwaveControlHelper.ZwController_setProtectionTimeout(deviceId,unit,time);
    }

    public void getIndicator(int deviceId){
        updateTimestamp(deviceId);
        ZwaveControlHelper.ZwController_getIndicator(deviceId);
    }

    public void setIndicator(int deviceId, int value){
        updateTimestamp(deviceId);
        ZwaveControlHelper.ZwController_setIndicator(deviceId,value);
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
            jsonResult.put("nodeId",new Integer(deviceId));
            jsonResult.put("deviceType",zwaveType);
            jsonResult.put("result", "true");

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
                jsonResult.put("nodeId",new Integer(deviceId));
                jsonResult.put("deviceType",zwaveType);
                jsonResult.put("result", "true");

                zwaveControlResultCallBack("setLampColor",jsonResult.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (devType.equals(btType)){

        }
    }
    public void setSwitchColor(String devType, int deviceId, int compId, int value){
        updateTimestamp(deviceId);
        if (devType.equals(zwaveType)){

            ZwaveControlHelper.ZwController_setSwitchColor(deviceId,compId,value);

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
                jsonResult.put("nodeId",new Integer(deviceId));
                //jsonResult.put("deviceType",zwaveType);
                jsonResult.put("result", "true");

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

    public void getSwitchColor(String devType, int deviceId, int compId){
        updateTimestamp(deviceId);
        if (devType.equals(zwaveType)){
            ZwaveControlHelper.ZwController_getSwitchColor(deviceId,compId);

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
        return ZwaveControlHelper.ZwController_getNotification(deviceId, alarm_type, notif_type, status);
    }

    public int setNotification(int deviceId, int type, int status){
        updateTimestamp(deviceId);
        return ZwaveControlHelper.ZwController_setNotification(deviceId, type, status);
    }

    public void getSupportedCentralScene(int deviceId, int endpointId){
        updateTimestamp(deviceId);
        ZwaveControlHelper.ZwController_getSupportedCentralScene(deviceId, endpointId);
    }

    public void getSceneActuatorConf(int deviceId, int scene_id){
        updateTimestamp(deviceId);
        ZwaveControlHelper.ZwController_getSceneActuatorConf(deviceId, scene_id);
    }

    public void setSceneActuatorConf(int deviceId, int scene_id, int dim_duration, int override, int level){
        updateTimestamp(deviceId);
        ZwaveControlHelper.ZwController_setSceneActuatorConf(deviceId, scene_id,dim_duration,override,level);
    }

    public void requestFirmwareUpdate(int nodeId, int vendorId, int firmwareId,int firmwareTarget,
                                      int hwVer, String firmwareFile){
        //updateTimestamp(deviceId);
        ZwaveControlHelper.ZwController_requestFirmwareUpdate(nodeId, vendorId,firmwareId,firmwareTarget,hwVer,firmwareFile);
    }

    public void getFirmwareUpdateInfo(int deviceId){
        updateTimestamp(deviceId);
        ZwaveControlHelper.ZwController_getFirmwareUpdateInfo(deviceId);
    }

    public void multiCmdEncap(int deviceId){
        updateTimestamp(deviceId);
        ZwaveControlHelper.ZwController_multiCmdEncap(deviceId);
    }

    public void getCommandQueueState(int deviceId){
        updateTimestamp(deviceId);
        ZwaveControlHelper.ZwController_getCommandQueueState(deviceId);
    }

    public void controlCommandQueue(int deviceId, int state){
        updateTimestamp(deviceId);
        ZwaveControlHelper.ZwController_controlCommandQueue(deviceId,state);
    }

    public void viewCommandQueue(int deviceId){
        updateTimestamp(deviceId);
        ZwaveControlHelper.ZwController_viewCommandQueue(deviceId);
    }

    public void cancelAllCommandQueue(int deviceId){
        updateTimestamp(deviceId);
        ZwaveControlHelper.ZwController_cancelAllCommandQueue(deviceId);
    }

    public void GetDeviceList(){
        //updateTimestamp(deviceId);
        ZwaveControlHelper.ZwController_GetDeviceList();
    }

    public void getGroupInfo(String devType, int deviceId,int groupId ,int endpointId){
        Log.i(LOG_TAG,"=====getGroupInfo==deviceId==="+deviceId);

        updateTimestamp(deviceId);

        List<Integer> list;
        list = devGroupManager.getZwaveDeviceGroupListByNodeId(deviceId);
        Log.i(LOG_TAG, "GROUP LIST SIZE = " + list.size());

        JSONObject jo = new JSONObject();
        JSONArray Jarray= new JSONArray();

        try {
            jo.put("Interface","getGroupInfo");
            jo.put("nodeId",String.valueOf(deviceId));
            jo.put("deviceType",devType);
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
        ZwaveControlHelper.ZwController_getGroupInfo(deviceId,groupId,endpointId);
        zwaveControlResultCallBack("GroupInfo", jo.toString());
    }

    public void addEndpointsToGroup(String devType, int deviceId, int groupId, int[] arr, int endpointId){
        //updateTimestamp(deviceId);
        Log.i(LOG_TAG,"=====addEndpointsToGroup== deviceId="+deviceId+"| groupId="+groupId+"| endpointId="+endpointId);


        ZwaveControlHelper.ZwController_getGroupInfo(deviceId,groupId,endpointId);

        int[] nodeifid = new int[arr.length];
        nodeifid[arr.length-1] = arr[arr.length-1];
        Log.i(LOG_TAG,"arr.length = "+arr.length);
        for (int idx=0;idx<(arr.length -1);idx++){
            nodeifid[idx]= getDeviceEnpointInterfaceId(arr[idx]);
            Log.i(LOG_TAG,"arr["+idx+"] = "+arr[idx]+" | nodeifid["+idx+"] = "+nodeifid[idx]);
        }

        int result = ZwaveControlHelper.ZwController_addEndpointsToGroup(deviceId,groupId,nodeifid,endpointId);
        //Log.i(LOG_TAG,"addEndpointsToGroup result = "+result);

        JSONObject jsonResult = new JSONObject();
        try {
            jsonResult.put("Interface","addEndpointsToGroup");
            //jsonResult.put("deviceType",devType);
            //jsonResult.put("nodeId",new Integer(deviceId));
            if (result >= 0 ) {
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

        ZwaveControlHelper.ZwController_getGroupInfo(deviceId,groupId,endpointId);

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
            //jsonResult.put("deviceType", devType);
            //jsonResult.put("nodeId", new Integer(deviceId));
            if (result >= 0 ) {
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

    public int getMaxSupportedGroups(int deviceId,int endpointId) {
        Logg.i(LOG_TAG, "=====getMaxSupportedGroups==deviceId===" + deviceId + "===endpointId=" + endpointId);
        int result = ZwaveControlHelper.ZwController_getMaxSupportedGroups(deviceId, endpointId);
        JSONObject jo = new JSONObject();
        if (result >= 0) {
            try {
                jo.put("Interface", "getMaxSupportedGroups");
                jo.put("result", "true");

            } catch (JSONException e) {
                e.printStackTrace();
            }
            zwaveControlResultCallBack("getMaxSupportedGroups", jo.toString());
        } else {
            try {
                jo.put("Interface", "getMaxSupportedGroups");
                jo.put("result", "false");

            } catch (JSONException e) {
                e.printStackTrace();
            }
            zwaveControlResultCallBack("getMaxSupportedGroups", jo.toString());
        }
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
            jo.put("deviceType",devType);

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
            jo.put("deviceType",devType);
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
            jo.put("deviceType",devType);
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
            jo.put("deviceType",devType);
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

    public int editNodeInfo(String brand, int deviceId, String newName, String devType, String type,
                            String roomName, String isFavorite) {
        //Log.i(LOG_TAG, "=====zwaveDevice.editNodeInfo====="+type);

        int result;
        ZwaveDevice zwaveDevice = zwaveDeviceManager.queryZwaveDevices(deviceId);

        if (zwaveDevice != null) {
            //Log.i(LOG_TAG, "=====zwaveDevice.editNodeInfo("+deviceId+")====");
            //Log.i(LOG_TAG, "=====zwaveDevice.editNodeInfo(setCategory)====="+type);
            java.util.Date date = new java.util.Date();
            zwaveDevice.setBrand(brand);
            zwaveDevice.setName(newName);
            zwaveDevice.setDevType(devType);
            zwaveDevice.setRoomName(roomName);
            zwaveDevice.setCategory(type);
            zwaveDevice.setFavorite(isFavorite);
            zwaveDevice.setTimestamp(date.getTime());
            zwaveDeviceManager.updateZwaveDevice(zwaveDevice);
            //initZwaveDevfunc(deviceId);
            result = 1;
            return result;
        } else {
            result = 0;
            return result;
        }

    }
    /*
        public int  getControllerRssi(){
            return ZwaveControlHelper.ZwController_getControllerNetworkRssiInfo();
        }
    */
    public interface zwaveCallBack {
        void zwaveControlResultCallBack(String className, String result);
    }

    private void zwaveControlResultCallBack(String className, String result) {
        for (zwaveCallBack callback : mCallBacks) {
            callback.zwaveControlResultCallBack(className, result);
        }
    }

    public interface zwaveControlReq_CallBack {
        void zwaveControlReqResultCallBack(String className, String result);
    }

    private void zwaveControlReqResultCallBack(String className, String result) {
        for (zwaveControlReq_CallBack callReqback : mReqCallBacks) {
            callReqback.zwaveControlReqResultCallBack(className, result);
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
        Log.i(LOG_TAG, tmpString);

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
    }
    /*
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

                    if (!zwaveDevice.getNodeId().toString().trim().equals(nodeInfoTemp.getNodeId().toString().trim())) {
                        i++;
                    }

                    // remove grop DB
                    if (temp.size() == i) {
                        removeResult = removeDevfromDB(devType, Integer.parseInt(nodeInfoTemp.getNodeId()));

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
    */
    private String removeDevfromDB(String devType, int deviceId){

        ZwaveDevice zwaveDevice = zwaveDeviceManager.queryZwaveDevices(deviceId);

        String removeResult = "removeDevice:"+devType+ ":" + deviceId;
        zwaveDeviceManager.deleteZwaveDevice(zwaveDevice.getZwaveId());

        if (zwaveDevice.getCategory().equals("WALLMOTE")){
            devGroupManager.deleteZwaveDeviceGroupByNodeId(deviceId);
        }else{
            devGroupManager.deleteZwaveDeviceGroupByInGropNodeId(deviceId);
        }
        // remove schedule DB
        List<ZwaveSchedule> scheduleList = zwSchManager.getZwaveScheduleList(deviceId);

        if (scheduleList != null) {
            for (int idx = 0; idx < scheduleList.size(); idx++) {
                scheduleJobManager.cancelSchedule(scheduleList.get(idx).getJobId());
            }
        }

        // update room DB
        if (zwaveDevice.getCategory().equals("SENSOR")) {

            List<String> tmpNodeList = zwaveDeviceManager.getRoomNameList();
            if (tmpNodeList != null) {

                for (int idx = 0; idx < tmpNodeList.size(); idx++) {

                    ZwaveDeviceRoom removeScene = new ZwaveDeviceRoom();
                    ZwaveDeviceRoom tmpScene = roomManager.getRoom(tmpNodeList.get(idx));
                    if (tmpScene != null && tmpScene.getSensorNodeId() == deviceId) {
                        removeScene.setSensorNodeId(null);
                        removeScene.setCondition(null);
                        roomManager.updateRoom(removeScene, tmpNodeList.get(idx));
                    }
                }
            }
        }
        return removeResult;
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


    //Device寫入DB
    private void insertDevice(String devType, String result, int status) {
        Log.i(LOG_TAG, "=======insertDevice==");
        Gson gson = new Gson();
        DeviceList deviceList = gson.fromJson(result, DeviceList.class);
        List<DeviceList.NodeInfoList> temp = deviceList.getNodeList();
        Log.i(LOG_TAG, "==insertDevice=====temp.size()==" + temp.size());

        List<ZwaveDevice> DBlist = zwaveDeviceManager.queryZwaveDeviceList();
        Log.i(LOG_TAG,"get device cnt from DB = "+DBlist.size());

        for (int index =0;index < DBlist.size(); index++){
            for (int idx =0; idx < temp.size(); idx++) {
                //Log.i(LOG_TAG,"DB #"+index+" nodeid = "+DBlist.get(index).getNodeId()+" | "
                //        +" dongle #"+idx+" nodeid = "+temp.get(idx).getNodeId());
                if (DBlist.get(index).getNodeId().toString().equals(temp.get(idx).getNodeId().toString())){
                    break;
                }
                if (idx ==temp.size()-1) {
                    Log.i(LOG_TAG,"remove node#"+DBlist.get(index).getNodeId()+" from DB");
                    removeDevfromDB(devType, DBlist.get(index).getNodeId());
                }
            }
        }
        DBlist = zwaveDeviceManager.queryZwaveDeviceList();
        Log.e(LOG_TAG,"get device cnt from DB = "+DBlist.size());

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
                zwaveDevice.setRoomName("My Home");
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



    public void zwaveControlReq_CallBack(byte[] result, int len) {
        // jni callback
        String jniResult = new String(result);
        JSONObject jsonObject = null;
        Log.d(LOG_TAG, "zwaveControlReq_CallBack jniResult===" + jniResult);
        String grantKeysMsg = null;
        String csaMsg = null;
        String pinReq = null;

        try {
            jsonObject = new JSONObject(jniResult);
            grantKeysMsg = jsonObject.optString("Grant Keys Msg");
            csaMsg = jsonObject.optString("Client Side Au Msg");
            pinReq = jsonObject.optString("PIN Requested Msg");


        } catch (JSONException e) {
            Log.i(LOG_TAG, "JSONException");
            e.printStackTrace();
        }

        if ("Request Keys".contains(grantKeysMsg)) {
            zwaveControlReqResultCallBack("Grant Keys Msg", jniResult);
        } else if ("Request CSA".contains(csaMsg)) {
            zwaveControlReqResultCallBack("Request CSA", jniResult);
        } else if ("PIN Requested Msg".contains(pinReq)) {
            zwaveControlReqResultCallBack("PIN Requested Msg", jniResult);
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

        } else if (messageType.equals("Remove Failed Node")) {
            zwaveControlResultCallBack("Remove Failed Node", jniResult);
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
                zwaveControlResultCallBack("All Node Info Report", jsonResult);
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
            zwaveControlResultCallBack("Node Battery Value", jniResult);
        } else if ("Sensor Information".equals(messageType)) {
            zwaveControlResultCallBack("getSensorMultiLevel", jniResult);
            //} else if (messageType.equals("All Node Info Report")) {
            //    zwaveControlResultCallBack("updateNode", jniResult);
        } else if ("Configuration Get Information".equals(messageType)) {
            zwaveControlResultCallBack("Configuration Get Information", jsonObject.toString());
        } else if ("Power Level Get Information".equals(messageType)) {
            zwaveControlResultCallBack("Power Level Get Information", jniResult);
        } else if ("Basic Information".equals(messageType)) {
            try {

                JSONObject payload = new JSONObject(jniResult);
                jsonObject = new JSONObject();

                jsonObject.put("Interface","getSwitchStatus");
                //jsonObject.put("deviceType",zwaveType);
                jsonObject.put("nodeId",String.valueOf(payload.getInt("Node id")));

                String switchStatus = payload.getString("value");
                Log.d(LOG_TAG,"gino value"+switchStatus);
                if (switchStatus.equals("00")){
                    jsonObject.put("switchStatus","off");
                } else{
                    jsonObject.put("switchStatus","on");
                }
                zwaveControlResultCallBack("getBasic", jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (messageType.equals("Sensor Info Report")) {
            zwaveControlResultCallBack("Sensor Info Report", jniResult);
        } else if (messageType.equals("Notification Get Information")) {
            zwaveControlResultCallBack("Notification Get Information", jniResult);
        } else if (messageType.equals("Supported Groupings Report")) {
            try {
                jsonObject = new JSONObject(jniResult);
                jsonObject.put("Interface","getMaxSupportedGroups");
                jsonObject.put("deviceType",zwaveType);
                zwaveControlResultCallBack("Supported Groupings Report", jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (messageType.equals("Binary Sensor Support Get Information")) {
            zwaveControlResultCallBack("Binary Sensor Support Get Information", jniResult);
        } else if (messageType.equals("Group Info Report")) {

            try {
                jsonObject = new JSONObject(jniResult);
                jsonObject.put("Interface","getGroupInfo");
                jsonObject.put("deviceType",zwaveType);
                zwaveControlResultCallBack("Group Info Report", jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if (messageType.equals("Notification Supported Report")) {
            zwaveControlResultCallBack("Notification Supported Report", jniResult);
        } else if (messageType.equals("Node Is Failed Check Report")) {
            zwaveControlResultCallBack("Node Is Failed Check Report", jniResult);
        } else if (messageType.equals("Meter Report Information")) {
            zwaveControlResultCallBack("Meter Report Information", jniResult);
        } else if (messageType.equals("Specify Node Info")) {
            zwaveControlResultCallBack("Specify Node Info", jniResult);
        } else if (messageType.equals("Controller Network RSSI Report")) {
            zwaveControlResultCallBack("getControllerRssi", jniResult);
        } else if ("DSK Report".equals(messageType)) {
            zwaveControlResultCallBack("DSK", jniResult);
        } else if ("Provision List Report".equals(messageType)) {
            zwaveControlResultCallBack("Provision List Report", jniResult);
        } else if ("All Provision List Report".equals(messageType)) {
            zwaveControlResultCallBack("All Provision List Report", jniResult);
        } else if ("Network Health Check".equals(messageType)) {
            zwaveControlResultCallBack("Network Health Check", jniResult);
        } else if ("Network IMA Info Report".equals(messageType)) {
            zwaveControlResultCallBack("Network IMA Info Report", jniResult);
        } else if ("Network RSSI Info Report".equals(messageType)) {
            zwaveControlResultCallBack("Network RSSI Info Report", jniResult);
        } else if ("Switch All Get Information".equals(messageType)) {
            zwaveControlResultCallBack("Switch All Get Information", jniResult);
        } else if ("Binary Sensor Information".equals(messageType)) {
            zwaveControlResultCallBack("Binary Sensor Information", jniResult);
        } else if ("Meter Cap Information".equals(messageType)) {
            zwaveControlResultCallBack("Meter Cap Information", jniResult);
        } else if ("Wake Up Cap Report".equals(messageType)) {
            zwaveControlResultCallBack("Wake Up Cap Report", jniResult);
        } else if ("Supported Color Report".equals(messageType)) {
            zwaveControlResultCallBack("Supported Color Report", jniResult);
        } else if ("Active Groups Report".equals(messageType)) {
            zwaveControlResultCallBack("Active Groups Report", jniResult);
        } else if ("Supported Notification Event Report".equals(messageType)) {
            zwaveControlResultCallBack("Supported Notification Event Report", jniResult);
        } else if ("Central Scene Supported Report".equals(messageType)) {
            zwaveControlResultCallBack("Central Scene Supported Report", jniResult);
        } else if ("Central Scene Notification".equals(messageType)) {
            zwaveControlResultCallBack("Central Scene Notification", jniResult);
        } else if ("Firmware Info Report".equals(messageType)) {
            zwaveControlResultCallBack("Firmware Info Report", jniResult);
        } else if ("Firmware Update Status Report".equals(messageType)) {
            zwaveControlResultCallBack("Firmware Update Status Report", jniResult);
        } else if ("Firmware Update Completion Status Report".equals(messageType)) {
            zwaveControlResultCallBack("Firmware Update Completion Status Report", jniResult);
        } else if ("Firmware Update restart Status Report".equals(messageType)) {
            zwaveControlResultCallBack("Firmware Update restart Status Report", jniResult);
        } else if ("Command Queue State Report".equals(messageType)) {
            zwaveControlResultCallBack("Command Queue State Report", jniResult);
        } else if ("Command Queue Info Report".equals(messageType)) {
            zwaveControlResultCallBack("Command Queue Info Report", jniResult);
        } else if ("All Node List Report".equals(messageType)) {
            zwaveControlResultCallBack("All Node List Report", jniResult);
        } else if ("openController".equals(messageType)) {
            zwaveControlResultCallBack("openController", jniResult);
        } else if ("setSwitchAllOn".equals(messageType)) {
            zwaveControlResultCallBack("setSwitchAllOn", jniResult);
        } else if ("setSwitchAllOff".equals(messageType)) {
            zwaveControlResultCallBack("setSwitchAllOff", jniResult);
        } else if ("Replace Failed Node".equals(messageType)) {
            zwaveControlResultCallBack("Replace Failed Node", jniResult);
        } else if ("Controller Init Status".equals(messageType)) {
            if ("Success".equals(status)) {
                flag = 2;
                Log.i(LOG_TAG, "=======Controller Init Status======");
                ZwaveControlHelper.ZwController_saveNodeInfo(SAVE_NODEINFO_FILE);
                ZwaveControlHelper.ZwController_GetDeviceInfo();
            }
            zwaveControlResultCallBack("Controller Init Status", jniResult);
        } else if ("Controller Attribute".equals(messageType)) {
            zwaveControlResultCallBack("Controller Attribute", jniResult);
        } else if ("Controller Reset Status".equals(messageType)) {
            zwaveControlResultCallBack("Controller Reset Status", jniResult);
            if ("Success".equals(status)) {
                Log.i(LOG_TAG, "=======Node setDefault Status=Success=");
                flag = 3;
                ZwaveControlHelper.ZwController_saveNodeInfo(SAVE_NODEINFO_FILE);
                //ZwaveControlHelper.ZwController_GetDeviceInfo();
            }
        } else if ("Door Lock Operation Report".equals(messageType)) {
            zwaveControlResultCallBack("Door Lock Operation Report", jniResult);
        } else if ("Door Lock Configuration Report".equals(messageType)) {
            zwaveControlResultCallBack("Door Lock Configuration Report", jniResult);
        } else if ("Controller DSK Report".equals(messageType)) {
            zwaveControlResultCallBack("Controller DSK Report", jniResult);
        } else if ("Binary Switch Get Information".equals(messageType)) {
            zwaveControlResultCallBack("Binary Switch Get Information", jniResult);
        } else if ("CSA Pin".equals(messageType)) {
            zwaveControlResultCallBack("CSA Pin", jniResult);
        } else if ("Supported S2 Cmd Report".equals(messageType)) {
            zwaveControlResultCallBack("Supported S2 Cmd Report", jniResult);
        } else if ("Switch Multi-lvl Report Information".equals(messageType)) {
            try {
                JSONObject payload = new JSONObject(jniResult);
                jsonObject = new JSONObject();

                jsonObject.put("Interface","getBrightness");
                //jsonObject.put("deviceType",zwaveType);
                jsonObject.put("nodeId",String.valueOf(payload.getInt("Node id")));
                jsonObject.put("brigthness",String.valueOf(payload.getInt("Cur Val")));
                if(String.valueOf(payload.getInt("Cur Val")).equals("0"))
                    jsonObject.put("switchStatus", "off");
                else
                    jsonObject.put("switchStatus", "on");

                zwaveControlResultCallBack("getBrightness", jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if ("Switch Color Report".equals(messageType)) {
            zwaveControlResultCallBack("Switch Color Report", jniResult);
        }
    }
}