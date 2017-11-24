package com.askey.mobile.zwave.control.util;

/***
 * 常量配置类
 * Company: Chengdu Skysoft Info&Tech Co., Ltd.
 * Copyright ©2014-2018 成都天软信息技术有限公司
 * @since:JDK1.7
 * @version:1.0
 * @see
 * @author  charles
 ***/
public class Const {

	/**
	 * 是否是DEBUG模式
	 */
	public static final boolean DEBUG = true;

   public static final int TCP_PORT = 48080;
	public static  String remoteServerIP = "211.75.141.112";

	public static  String serverUri = "";
	public static  String clientId = "unknown";
	public static  String subscriptionTopic = "unknown";
	public static  String publishTopic = "unknown";
	public static  String TUTK_TUUID = "";
	public static final String TOPIC_TAG = "subscriptionTopic";
	public static final String TUTK_TUUID_TAG = "Tutk_tuuid";
	public static  String SERVER_IP = "";
	public static final int MQTT_PORT = 1883;
	public static final int TCP_TIMEOUT = 10002;

	public static String activityIndex = "";
	public static boolean isRemote = false;
	public static final String AUTH_APP_ID = "askey.nas.firefly.api";
	public static String currentRoomName = "";

	public static final String DEVICE_MODEL = "FIREFLY";

}
