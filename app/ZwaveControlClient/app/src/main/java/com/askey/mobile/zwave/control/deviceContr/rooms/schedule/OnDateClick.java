package com.askey.mobile.zwave.control.deviceContr.rooms.schedule;

/**
 * Created by skysoft on 2017/10/30.
 */

public interface OnDateClick {
    // 点击的日期为可选
    void onClick(int year, int month, int data);
}
