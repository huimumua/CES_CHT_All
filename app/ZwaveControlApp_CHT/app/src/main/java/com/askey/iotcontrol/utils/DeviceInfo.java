package com.askey.iotcontrol.utils;

import com.askey.iotcontrol.page.zwNodeMember;
import com.askey.iotcontrol.page.RoomMember;

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
    public static ArrayList<String> awsIotDevList = new ArrayList<>();

    public static List<zwNodeMember> memberList = new ArrayList<>();
    public static List<RoomMember> roomList = new ArrayList<>();
    public static String[] deviceType = {"BULB","DIMMER","PLUG","SENSOR","WALLMOTE","OTHER"};
    public static String[] allRoomName = {"My Home","Living Room","Kitchen Room"};
    public static String pinCode = "00000\0";
    public static byte[] tempDsk = pinCode.getBytes();
    public static String dskNumber = null;

}
