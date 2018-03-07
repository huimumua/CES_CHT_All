package com.askey.mobile.zwave.control.util;

import com.askey.mobile.zwave.control.R;

/***
 * 常量配置类
 * Company: Chengdu Skysoft Info&Tech Co., Ltd.
 * Copyright ©2014-2018 成都天软信息技术有限公司
 * @since:JDK1.7
 * @version:1.0
 * @see
 * @author charles
 ***/
public class Const {

    /**
     * 是否是DEBUG模式
     */
    public static final boolean DEBUG = true;

    public static final int TCP_PORT = 48080;
    public static final long TCP_TIMER_TIMEOUT = 1000*90;
    public static String remoteServerIP = "211.75.141.112";

    public static String SERVER_URI_TAG = "SERVER_URI";
    public static String serverUri = "";
    public static String clientId = "unknown";
    public static String subscriptionTopic = "unknown";
    public static String publishTopic = "unknown";
    public static String TUTK_TUUID = "";
    public static final String TOPIC_TAG = "subscriptionTopic";
    public static final String TUTK_TUUID_TAG = "Tutk_tuuid";
    public static String SERVER_IP_TAG = "SERVER_IP_ADDRESS";
    public static String SERVER_IP = "";
    public static final int MQTT_PORT = 1883;
    public static final int TCP_TIMEOUT = 10002;

    public static String activityIndex = "";
    public static boolean isRemote = false;
    public static final String AUTH_APP_ID = "askey.nas.firefly.api";
    public static String currentRoomName = "";

    public static final String DEVICE_MODEL = "FIREFLY";

    private static boolean isDataChange = false;

    public static void setIsDataChange(boolean tag) {
        isDataChange = tag;
    }

    public static boolean getIsDataChange() {
        return isDataChange;
    }

    public static int[] switch_step = new int[]{R.drawable.smart_switch_01,R.drawable.smart_switch_02};
    public static int[] Bulb_step = new int[]{R.drawable.buld_01,R.drawable.buld_02};
    public static int[] Wallmote_step = new int[]{R.drawable.wallmote_step_1,R.drawable.wallmote_step_2};
    public static int[] Extender_step = new int[]{R.drawable.exdenter_step_1,R.drawable.exdenter_step_2};

    public static int[] switch_notify = new int[]{R.string.switch_1,R.string.switch_2};
    public static int[] Bulb_notify = new int[]{R.string.bulb_1,R.string.bulb_2};
    public static int[] Wallmote_notify = new int[]{R.string.wallmote_1,R.string.wallmote_2};
    public static int[] Extender_notify = new int[]{R.string.extender_1,R.string.extender_2};
}
