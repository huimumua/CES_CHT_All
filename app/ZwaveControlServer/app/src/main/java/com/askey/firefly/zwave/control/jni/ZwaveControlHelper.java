package com.askey.firefly.zwave.control.jni;

import com.askey.firefly.zwave.control.service.ZwaveControlService;
import com.askey.firefly.zwave.control.service.ZwaveSendBroadcast;


import android.content.Intent;
import android.util.Log;

import java.nio.ByteBuffer;

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

    public native static int CreateZwController();
    public native static int OpenZwController(String CfgFile , String FilePath, String NodeInfoPath, byte[] result);
    public native static int CloseZwController();
    public native static int DestoryZwController();

    /**
    ** zwave controller jni interface
    ** controller/device control
    **/
    public native static int ZwController_AddDevice();
    public native static int ZwController_RemoveDevice();
    public native static int ZwController_GetDeviceInfo(int deviceId);
    public native static int ZwController_GetDeviceList();
    public native static int ZwController_RemoveFailedDevice(int deviceId);
    public native static int ZwController_ReplaceFailedDevice(int deviceId);
    public native static int ZwController_SetDefault();
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
    ** zwave controller jni interface
    ** support CC: COMMAND_CLASS_POWERLEVEL
    **/
    public native static int ZwController_GetPowerLevel(int deviceId);

    /**
    ** zwave controller jni interface
    ** support CC: COMMAND_CLASS_SWITCH_ALL
    **/
    public native static int ZwController_SetSwitchAllOn(int deviceId);
    public native static int ZwController_SetSwitchAllOff(int deviceId);
    public native static int ZwController_SetSwitchAll(int deviceId, int value);
    public native static int ZwController_GetSwitchAll(int deviceId);
}
