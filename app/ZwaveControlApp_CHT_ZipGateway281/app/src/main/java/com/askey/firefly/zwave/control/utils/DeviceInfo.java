package com.askey.firefly.zwave.control.utils;

import com.askey.firefly.zwave.control.page.zwNodeMember;
import com.askey.firefly.zwave.control.page.zwScenceMember;
import com.askey.firefly.zwave.control.service.ZwaveControlService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chiapin on 2017/9/25.
 */

public class DeviceInfo {

    public static boolean isZwaveInitFinish = false;
    public static boolean isOpenControllerFinish = false;
    public static boolean isMQTTInitFinish = false;
    public static ArrayList<String> remoteSubTopiclist = new ArrayList<>();
    public static ArrayList<String> localSubTopiclist = new ArrayList<>();

    public static List<zwNodeMember> memberList = new ArrayList<>();
    public static List<zwScenceMember> roomList = new ArrayList<>();
    public static String[] deviceType = {"BULB", "DIMMER", "PLUG", "SENSOR", "CONTROL", "OTHER"};
    public static String[] allRoomName = {"My Home","Living Room","Bedroom"};
    public static String dskNumber = null;
    public static int reqKey;
    public static boolean reqFlag = false;
    public static String devType = "Zwave";
    public static boolean bootMode = false;
    public static boolean InclusionState = true;

    public static String className = "";
    public static String result = "";

    public static String getMqttPayload = "";
    public static String reqString = "";
    public static String grantKeyNumber = "";


    public static String mqttString = "";
    public static String mqttString2 = "";
    public static String mqttString3 = "";
    public static String mqttString4 = "";
    public static String mqttString5 = "";


    public static ArrayList<Integer> arrList;
    public static ArrayList<String> addList;
    public static ArrayList<String> removeList;


    public static int mqttDeviceId = 0;
    public static int mqttValue = 0;
    public static int mqttTmp = 0;
    public static int mqttTmp2 = 0;
    public static int mqttTmp3 = 0;
    public static int mqttTmp4 = 0;
    public static int mqttTmp5 = 0;
    public static int callResult = -1;

    public static String resultToMqttBroker = "";

    public static String room = "";



}
