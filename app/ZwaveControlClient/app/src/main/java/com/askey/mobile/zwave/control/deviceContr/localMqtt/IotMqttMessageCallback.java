package com.askey.mobile.zwave.control.deviceContr.localMqtt;

/**
 * Created by skysoft on 2017/9/19.
 */

public interface IotMqttMessageCallback {
    void receiveMqttMessage(String s,String s1, String s2);
}
