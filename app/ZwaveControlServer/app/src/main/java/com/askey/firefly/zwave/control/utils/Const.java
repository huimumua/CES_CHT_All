package com.askey.firefly.zwave.control.utils;

import android.os.Environment;

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
	public static final String PACKAGE_NAME = "com.askey.firefly.zwave.control";
	public static final String DATABASE_NAME = "zwave.db";

	public static final String DATA_PATH = "/data"
			+ Environment.getDataDirectory().getAbsolutePath() + "/"
			+ PACKAGE_NAME  ;

	public static final String ZWCONTROL_CFG_PATH = DATA_PATH + "/" + "zw_api.cfg";
    public static final String FILE_PATH = DATA_PATH + "/";

    public static final String SAVE_NODEINFO_FILE = DATA_PATH + "/" +"zwController_nodeInfo.txt";

	public static final String DBPATH = DATA_PATH + "/" + DATABASE_NAME;

	public static final int TCP_PORT = 48080;
	public static final String remoteServerIP = "211.75.141.112";
	public static final String TCPSTRING = "firefly_zwave:";

	public static String localMQTTServerUri = "tcp://"+ Utils.getIpAddress() + ":1883";
	public static String remoteMQTTServerUri = "tcp://"+ remoteServerIP +":1883";

	public static int TCPClientPort = 0;
	public static String mqttClientId = Utils.getPublicTopicName();
	public static String PublicTopicName = Utils.getPublicTopicName();

	public static boolean remoteMqttFlag = true;
}
