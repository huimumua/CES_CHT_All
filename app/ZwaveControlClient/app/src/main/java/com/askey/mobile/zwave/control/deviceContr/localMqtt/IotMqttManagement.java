package com.askey.mobile.zwave.control.deviceContr.localMqtt;

/**
 * Created by skysoft on 2017/9/19.
 */

public class IotMqttManagement {
    private IotMqttMessageCallback iotMqttMessageCallback;
    public static IotMqttManagement getInstance() {
        return SingleInstanceHolder.sInstance;
    }
    private static class SingleInstanceHolder {
        private static IotMqttManagement sInstance = new IotMqttManagement();
    }
    public void setIotMqttMessageCallback(IotMqttMessageCallback iotMqttMessageCallback) {
        this.iotMqttMessageCallback = iotMqttMessageCallback;
    }

    public void receiveMqttMessage(String s, String s1, String s2) {
        iotMqttMessageCallback.receiveMqttMessage(s,s1,s2);
    }
}
