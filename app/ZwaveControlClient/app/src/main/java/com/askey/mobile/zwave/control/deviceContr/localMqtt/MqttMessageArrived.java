package com.askey.mobile.zwave.control.deviceContr.localMqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * 项目名称：ZwaveControlClient
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2017/7/26 16:33
 * 修改人：skysoft
 * 修改时间：2017/7/26 16:33
 * 修改备注：
 */
public interface MqttMessageArrived {
    String activityKey = "";
    void mqttMessageArrived(String topic,MqttMessage message);
}
