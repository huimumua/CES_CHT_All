package com.askey.firefly.zwave.control.jni;

import android.util.Log;

import com.askey.firefly.zwave.control.service.ZwaveControlService;
import com.askey.firefly.zwave.control.application.ZwaveProvisionList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 项目名称：ZwaveControl
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2017/7/10 13:33
 * 修改人：skysoft
 * 修改时间：2017/7/10 13:33
 * 修改备注：
 */
public class ZwaveControlHelper {

    public final static String LOG_TAG = "ZwaveControlHelper";

    static {
        System.loadLibrary("zwcontrol-jni");
//        initZwave();
    }

    public ZwaveControlHelper() {
    }

    public static void ZwaveControlRes_CallBack(byte[] result, int len)
    {
        ZwaveControlService.getInstance().zwaveCallBack(result,len);
        //Log.d("ZwaveControlHelper", "ZwaveControlRes_CallBack " + new String(result));

//        ZwaveSendBroadcast.getInstance().zwaveCallBack(result,len);//发广播 add remove

    }

    public static int ZwaveControlReq_CallBack(byte[] result, int len)
    {
        android.util.Log.d("ZwaveControlHelper", "ZwaveControlReq_CallBack " + new String(result));
        String jniResult = new String(result);
        String messageType = null;
        JSONObject jsonObject = null;

        String grantKeysMsg = null;
        String csaMsg = null;
        String pinReq = null;

        try {
            jsonObject = new JSONObject(jniResult);
            grantKeysMsg = jsonObject.optString("Grant Keys Msg");
            Log.i(LOG_TAG,"  grantKeysMsg:"+grantKeysMsg);
            pinReq = jsonObject.optString("PIN Requested Msg");
            csaMsg = jsonObject.optString("Client Side Au Msg");
        } catch (JSONException e) {
            android.util.Log.i(LOG_TAG, "JSONException");
            e.printStackTrace();
        }

        if("Request Keys".equals(grantKeysMsg))
        {
            String keys = null;
            try {
                keys = jsonObject.getString("Keys");
            } catch (JSONException e) {
                e.printStackTrace();
            }
      
            int intkey = Integer.parseInt(keys);
            android.util.Log.i(LOG_TAG," grant keys: "+keys+"   int:"+intkey);
            return intkey;
        }

        if("Enter 5-digit PIN".equals(pinReq))
        {
            android.util.Log.i(LOG_TAG," user input pin code: "+(Integer.toString(30008)));
            return 16993;
        }

        if("Request CSA".equals(csaMsg))
        {
            android.util.Log.i(LOG_TAG," user csa result:"+"yes");
            return 1;
        }

        android.util.Log.w(LOG_TAG," should not go here!!!!");
        return  0;
    }

    public native static int CreateZwController();
    public native static int OpenZwController(String FilePath, String NodeInfoPath, byte[] result);
    public native static int CloseZwController();
    public native static int DestoryZwController();

    /**
    ** zwave controller jni interface
    ** controller/device control
    **/
    public native static int ZwController_AddDevice();
    public native static int ZwController_RemoveDevice();
    public native static int ZwController_GetDeviceInfo();
    public native static int ZwController_GetDeviceList();
    public native static int ZwController_getSpecifyDeviceInfo(int deviceId);
    public native static int ZwController_RemoveFailedDevice(int deviceId);
    public native static int ZwController_ReplaceFailedDevice(int deviceId);
    public native static int ZwController_SetDefault();
    // NOTE: when controller going to inclusion process, it's can not be stoped
    public native static int ZwController_StopAddDevice();
    public native static int ZwController_StopRemoveDevice();
    public native static int ZwController_UpdateNode(int deviceId);
    public native static int ZwController_saveNodeInfo(String NodeInfoPath);

    /**
    ** zwave controller jni interface
    ** support CC: COMMAND_CLASS_BATTERY
    **/
    public native static int ZwController_GetDeviceBattery(int deviceId);

    /**
    ** zwave controller jni interface
    ** support CC: COMMAND_CLASS_SENSOR_MULTILEVEL
    **/
    public native static int ZwController_GetSensorMultiLevel(int deviceId);
    //public native static int ZwController_GetSensorMultiLevel(int deviceId, int sensor_type, int unit);

    /**
    ** zwave controller jni interface
    ** support CC: COMMAND_CLASS_BASIC
    **/
    public native static int ZwController_GetBasic(int deviceId);
    public native static int ZwController_SetBasic(int deviceId, int value);

    /**
    ** zwave controller jni interface
    ** support CC: COMMAND_CLASS_SWITCH_MULTILEVEL
    **/
    public native static int ZwController_GetSwitchMultiLevel(int deviceId);
    public native static int ZwController_SetSwitchMultiLevel(int deviceId, int value, int duration);
    public native static int ZwController_GetSupportedSwitchType(int deviceId);
    /**
     * 控制灯泡亮暗
     * deviceId  ： nodeId
     * startLvlVal  1-99 或 255 亮度
     * duration  变化的时间 1-99 单位秒
     * pmyChangeDir  变化方向 0 变亮 1 变暗  3 不变
     * secChangeDir  0  1  3
     * secStep  1-99 或 255
     * */
    public native static int ZwController_startStopSwitchLevelChange(int deviceId, int startLvlVal, int duration,
                                                                     int pmyChangeDir, int secChangeDir, int secStep);

    /**
    ** zwave controller jni interface
    ** support CC: COMMAND_CLASS_CONFIGURATION
    **/
    public native static int ZwController_GetConfiguration(int deviceId, int paramMode, int paramNumber,
                                                           int rangeStart, int rangeEnd);
    public native static int ZwController_SetConfiguration(int deviceId, int paramNumber, int paramSize,
                                                           int useDefault, int paramValue);
    /**
     * versing 2
     * deviceId     nodeId
     * offset1      the MSB bit about the start param number
     * offset2      the LSB bit about the start param number
     * paramNumber  the number of param will be set
     * paramSize    the param unit, 1,2 or 4 bytes
     * useDefault   weather to use param default value, 1 means use it's default value
     * paramValue   the value array will be set, the array size depends by paramNumber
     * */
    public native static int ZwController_SetConfigurationBulk(int deviceId, int offset1, int offset2, int paramNumber, int paramSize,
                                                           int useDefault, int[] paramValue);

    /**
    ** zwave controller jni interface
    ** support CC: COMMAND_CLASS_POWERLEVEL
    **/
    public native static int ZwController_GetPowerLevel(int deviceId);
    public native static int ZwController_SetPowerLevel(int deviceId, int powerLvl, int timeout);

    /**
    ** zwave controller jni interface
    ** support CC: COMMAND_CLASS_SWITCH_ALL
    **/
    public native static int ZwController_SetSwitchAllOn(int deviceId);
    public native static int ZwController_SetSwitchAllOff(int deviceId);
    public native static int ZwController_SetSwitchAll(int deviceId, int value);
    public native static int ZwController_GetSwitchAll(int deviceId);
    public native static int ZwController_StartLearnMode();
    // For broadcast request
    //public native static int ZwController_SetSwitchAllOnBroadcast();
    //public native static int ZwController_SetSwitchAllOffBroadcast();

    /**
    ** zwave controller jni interface
    ** support CC: COMMAND_CLASS_SWITCH_BINARY
    **/
    public native static int ZwController_SetBinarySwitchState(int deviceId, int state);
    public native static int ZwController_GetBinarySwitchState(int deviceId);

    /**
    ** zwave controller jni interface
    ** support CC: COMMAND_CLASS_SENSOR_BINARY
    **/
    public native static int ZwController_GetSensorBinary(int deviceId, int sensorType);
    public native static int ZwController_GetSensorBinarySupportedSensor(int deviceId);

    /**
    ** zwave controller jni interface
    ** support CC: COMMAND_CLASS_METER
    **
    **    static const char *meter_type[] = { "unknown", "electric", "gas", "water",
    **                                    "heating", "cooling"};
    **    static const char *meter_rate[] = { "unknown", "import(consumed)", "export(produced)"};
    **
    **    static const char *units[][7] = {
    **    {"KWh", "kVAh", "W", "Pulse Count", "V", "A", "Power factor"},  // electric
    **    {"Cubic meters", "Cubic feet", "unknown", "Pulse Count", "unknown", "unknown", "unknown"},   // gas
    **    {"Cubic meters", "Cubic feet", "US gallons", "Pulse Count", "unknown", "unknown", "unknown"} // water
    **    };
    **/
    public native static int ZwController_GetMeter(int deviceId, int meterUnit);
    public native static int ZwController_resetMeter(int deviceId);
    public native static int ZwController_getMeterSupported(int deviceId);

    /**
    ** zwave controller jni interface
    ** support CC: COMMAND_CLASS_WAKE_UP
    **/
    public native static int ZwController_getWakeUpSettings(int deviceId);
    public native static int ZwController_setWakeUpInterval(int deviceId, int interval);

    /**
    ** zwave controller jni interface
    ** support CC: COMMAND_CLASS_DOOR_LOCK
    **/
    public native static int ZwController_getDoorLockOperation(int deviceId);

    /**
    ** @param mode (hex)
    ** (0) Door Unsecured, (1) Door Unsecured with timeout
    ** (10) Door Unsecured for inside Door Handles, 16
    ** (11) Door Unsecured for inside Door Handles with timeout, 17
    ** (20) Door Unsecured for outside Door Handles, 32
    ** (21) Door Unsecured for outside Door Handles with timeout, 33
    ** (FE) Door/Lock State Unknown
    ** (FF) Door Secured
    **/
    public native static int ZwController_setDoorLockOperation(int deviceId, int mode);
    public native static int ZwController_getDoorLockConfiguration(int deviceId);
    public native static int ZwController_setDoorLockConfiguration(int deviceId, int type, int out_sta,
                                                                   int in_sta, int tmout_min, int tmout_sec);

    /**
    ** zwave controller jni interface
    ** support CC: COMMAND_CLASS_USER_CODE
    **/
    /*public native static int ZwController_getUserCode(int deviceId, int user_id);
    public native static int ZwController_setUserCode(int deviceId, int user_id, int status);
    public native static int ZwController_getUserCodeNumber(int deviceId);*/

    /**
    ** zwave controller jni interface
    ** support CC: COMMAND_CLASS_PROTECTION
    **/
    public native static int ZwController_getProtection(int deviceId);

    /**
    ** @param localPortState local protection state
    **        value: (0) Unprotected, (1) Protection by sequence, (2) No operation possible
    ** @param rfPortState RF Protection State (version 2)
    **        value: (0) Unprotected (1) No RF control (2) No RF control and response
    **/
    public native static int ZwController_setProtection(int deviceId, int localPortState, int rfPortState);
    public native static int ZwController_getSupportedProtection(int deviceId);
    public native static int ZwController_getProtectionExcControlNode(int deviceId);
    public native static int ZwController_setProtectionExcControlNode(int deviceId, int controlNodeid);
    public native static int ZwController_getProtectionTimeout(int deviceId);

    /** 
    ** @param unit Timeout unit
    ** (0) seconds (1 to 60) 
    ** (1) minutes (2 to 191) 
    ** (2) No timeout (always protected)
    **/
    public native static int ZwController_setProtectionTimeout(int deviceId, int unit, int time);


    /**
    ** zwave controller jni interface
    ** support CC: COMMAND_CLASS_INDICATOR
    **/
    public native static int ZwController_getIndicator(int deviceId);

    /** 
    ** @param value indicator unit
    ** 00 = off, disable; FF = on, enable; Other value: 1 to 63h
    **/
    public native static int ZwController_setIndicator(int deviceId, int value);


    /**
    ** zwave controller jni interface
    ** support CC: COMMAND_CLASS_DOOR_LOCK_LOGGING
    **/
    /*public native static int ZwController_getDoorLockLoggingSupportedRecords(int deviceId);
    public native static int ZwController_getDoorLockLoggingRecords(int deviceId, int rec_num);*/

    /**
    ** zwave controller jni interface
    ** support CC: COMMAND_CLASS_LANGUAGE
    **/
    //public native static int ZwController_getLanguage(int deviceId);

    /**
    ** zwave controller jni interface
    ** support CC: COMMAND_CLASS_SWITCH_COLOR
    **/
    public native static int ZwController_getSwitchColor(int deviceId, int compID);
    public native static int ZwController_getSupportedSwitchColor(int deviceId);
    /** 
    ** @param colorId  0-8
    **        {"Warm Write", "Cold Write", "Red","Green", "Blue",
    **         "Amber", "Cyan", "Purple", "Indexed Color"}
    ** @param colorValue 0x00 means 0%, 0xFF means 100%
    **/
    public native static int ZwController_setSwitchColor(int deviceId, int colorId, int colorValue);

    /** color 渐变
    ** @param colorId    0-8
    **                    {"Warm Write", "Cold Write", "Red","Green", "Blue",
    **                     "Amber", "Cyan", "Purple", "Indexed Color"}
    ** @param dir         change direction, 0 for increasing, 1 for decreasing
    ** @param ignore      flag indicate wheather ignore the start_value
    ** @param startValue the color change start value 0x00 means 0%, 0xFF means 100%
    **/
    public native static int ZwController_startStopSwitchColorLevelChange(int deviceId, int dir, int ignore, int colorId, int startValue);

    /**
    ** zwave controller jni interface
    ** support CC: COMMAND_CLASS_BARRIER_OPERATOR
    **/
    /*public native static int ZwController_setBarrierOperator(int deviceId, int value);
    public native static int ZwController_getBarrierOperator(int deviceId);
    public native static int ZwController_setBarrierOperatorSignal(int deviceId, int subSysType, int state);
    public native static int ZwController_getBarrierOperatorSignal(int deviceId, int subSysType);
    public native static int ZwController_getSupportedBarrierOperatorSignal(int deviceId);*/

    /**
    ** zwave controller jni interface
    ** support CC: COMMAND_CLASS_BASIC_TARIFF_INFO
    **/
    //public native static int ZwController_getBasicTariffInfo(int deviceId);

    /**
    ** zwave controller jni interface
    ** support CC: COMMAND_CLASS_ASSOCIATION
    **/
    public native static int ZwController_getGroupInfo(int deviceId, int groupId, int endpointId);
    public native static int ZwController_addEndpointsToGroup(int deviceId, int groupId, int[] arr, int endpointId);
    public native static int ZwController_removeEndpointsFromGroup(int deviceId, int groupId, int[] arr, int endpointId);
    public native static int ZwController_getMaxSupportedGroups(int deviceId, int endpointId);
    public native static int ZwController_getSpecificGroup(int deviceId, int endpointId);

    /**
    ** zwave controller jni interface
    ** support CC: COMMAND_CLASS_NOTIFICATION
    **/
    public native static int ZwController_setNotification(int deviceId, int notifyType, int status);
    public native static int ZwController_getNotification(int deviceId, int alarmType, int notifyType, int evt);
    public native static int ZwController_getSupportedNotification(int deviceId);
    public native static int ZwController_getSupportedEventNotification(int deviceId, int notifyType);

    /**
    ** zwave controller jni interface
    ** support CC: COMMAND_CLASS_CENTRAL_SCENE
    **/
    public native static int ZwController_getSupportedCentralScene(int deviceId, int endpointId);

    /**
    ** zwave controller jni interface
    ** support CC: COMMAND_CLASS_SCENE_ACTUATOR_CONF
    **/
    public native static int ZwController_getSceneActuatorConf(int deviceId, int sceneId);

    /**
    ** @param sceneId     The scene id  0-255
    ** @param dimDuration 变化的时间 1-99 单位秒
    ** @param override    
    **        If the Override bit is set to 0, the current settings in the device is associated with the Scene ID. 
    **        If the Override bit is set to 1, the Level value in the Command is associated to the Scene ID.
    ** @param level 0x00-0xFF
    **/
    public native static int ZwController_setSceneActuatorConf(int deviceId, int sceneId, int dimDuration, int override, int level);

    /**
    ** zwave controller jni interface
    ** support CC: COMMAND_CLASS_FIRMWARE_UPDATE_MD
    **/
    public native static int ZwController_getFirmwareUpdateInfo(int deviceId);
    public native static int ZwController_requestFirmwareUpdate(int nodeId, int vendorId, int firmwareId,int firmwareTarget,
                                                                int hwVer, String firmwareFile);

    /**
    ** zwave controller jni interface
    ** support CC: COMMAND_CLASS_MULTI_CMD
    **/
    public native static int ZwController_multiCmdEncap(int deviceId);

    /**
    ** zwave controller jni interface
    ** Add for command queue control
    **/
    public native static int ZwController_getCommandQueueState(int deviceId);
    // Commmand queuing control state: 0 = disable, 1 = enable
    public native static int ZwController_controlCommandQueue(int deviceId, int state);
    public native static int ZwController_viewCommandQueue(int deviceId);
    public native static int ZwController_cancelAllCommandQueue(int deviceId);

    /**
    ** zwave controller jni interface
    ** Add for RSSI info get
    **/
    //public native static int ZwController_getControllerNetworkRssiInfo();
    //public native static int ZwController_getDeviceNetworkRssiInfo(int deviceId);
    public native static int ZwController_startNetworkHealthCheck();

    /**
    ** zwave controller jni interface
    ** Smart Start, Provision list operation
    **/
    public native static int ZwController_addProvisionListEntry(byte[] dsk, int dsklen, Object[] plInfo, int infoCount);
    public native static int ZwController_rmProvisionListEntry(byte[] dsk, int dsklen);
    public native static int ZwController_getProvisionListEntry(byte[] dsk, int dsklen);
    public native static int ZwController_getAllProvisionListEntry();
    public native static int ZwController_rmAllProvisionListEntry();

    public native static int ZwController_checkNodeIsFailed(int deviceId);

    public native static int ZwController_getSecurity2CmdSupported(int deviceId);
    public native static int ZwController_sendNodeInformationFrame(int deviceId, int broadcastFlag);

}
