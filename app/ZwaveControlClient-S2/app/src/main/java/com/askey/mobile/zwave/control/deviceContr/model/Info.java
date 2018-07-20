package com.askey.mobile.zwave.control.deviceContr.model;

import java.util.List;

/**
 * Created by skysoft on 2018/7/3.
 */

public class Info {
    private String name;
    private List<String>data;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }
}
