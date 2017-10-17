package com.askey.firefly.zwave.control.utils;

import com.askey.firefly.zwave.control.mqtt.zwNodeMember;

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
    public static String[] deviceType = {"BULB", "DIMMER", "PLUG", "SENSOR", "OTHER"};
}
