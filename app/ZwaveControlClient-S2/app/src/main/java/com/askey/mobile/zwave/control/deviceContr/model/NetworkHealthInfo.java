package com.askey.mobile.zwave.control.deviceContr.model;

import java.io.Serializable;

/**
 * Created by skysoft on 2018/6/6.
 */

public class NetworkHealthInfo implements Serializable {

    String directNodeId;
    String networkHealth;
    String rssiHopsValue;

    public String getRssiHopsValue() {
        return rssiHopsValue;
    }

    public void setRssiHopsValue(String rssiHopsValue) {
        this.rssiHopsValue = rssiHopsValue;
    }


    public String getDirectNodeId() {
        return directNodeId;
    }

    public void setDirectNodeId(String directNodeId) {
        this.directNodeId = directNodeId;
    }

    public String getNetworkHealth() {
        return networkHealth;
    }

    public void setNetworkHealth(String networkHealth) {
        this.networkHealth = networkHealth;
    }
}
