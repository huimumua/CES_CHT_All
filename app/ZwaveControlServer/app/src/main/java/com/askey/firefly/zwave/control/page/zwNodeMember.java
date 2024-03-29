package com.askey.firefly.zwave.control.page;

/**
 * Created by chiapin on 2017/9/27.
 */

public class zwNodeMember {

    private int nodeId;
    private String homeId;
    private String devType;
    private String name;
    private String nodeInfo;
    private String roomName;
    private boolean status;


    public zwNodeMember(int nodeId,String homeId, String devType, String name,String roomName,boolean status ,String nodeInfo) {
        super();
        this.nodeId = nodeId;
        this.homeId = homeId;
        this.devType = devType;
        this.name = name;
        this.roomName = roomName;
        this.nodeInfo = nodeInfo;
        this.status = status;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getNodeId() {
        return nodeId;
    }
    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public String getHomeId() {
        return homeId;
    }
    public void setHomeId(String homeId) {
        this.homeId = homeId;
    }

    public String getDeviceType() {
        return devType;
    }

    public String getNodeInfo() {
        return nodeInfo;
    }
    public String getRoomName() {
        return roomName;
    }
    public boolean getNodeStatus() {return status;}
    public void setNodeStatus(boolean status) {this.status = status;}
}
