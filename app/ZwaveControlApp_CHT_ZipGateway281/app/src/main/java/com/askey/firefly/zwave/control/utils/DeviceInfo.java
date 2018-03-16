package com.askey.firefly.zwave.control.utils;

import com.askey.firefly.zwave.control.page.zwNodeMember;
import com.askey.firefly.zwave.control.page.zwScenceMember;

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
    public static String pinCode = "00000\0";
    public static byte[] tempDsk = pinCode.getBytes();
    public static String dskNumber = null;

}
