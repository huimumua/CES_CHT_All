package com.askey.mobile.zwave.control.deviceContr.model;

import java.io.Serializable;

/**
 * 项目名称：ZwaveControlClient
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2017/10/19 13:15
 * 修改人：skysoft
 * 修改时间：2017/10/19 13:15
 * 修改备注：
 */
public class RoomInfo implements Serializable{

    /**
     * 房间Id
     * */
    private int RoomId;
    /**
     * 房间名称
     * */
    private String RoomName;

    public int getRoomId() {
        return RoomId;
    }

    public void setRoomId(int roomId) {
        RoomId = roomId;
    }

    public String getRoomName() {
        return RoomName;
    }

    public void setRoomName(String roomName) {
        RoomName = roomName;
    }
}
