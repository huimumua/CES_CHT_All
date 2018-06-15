package com.askey.iotcontrol.page;

/**
 * Created by chiapin on 2017/9/27.
 */

public class zwNodeMember {

    private int nodeId;
    private String brandId;
    private String devType;
    private String devCate;
    private String name;
    private String nodeInfo;
    private String roomName;
    private boolean status;


    public zwNodeMember(int nodeId, String brandId, String devType,String devCate, String name,String roomName,boolean status ,String nodeInfo) {
        super();
        this.nodeId = nodeId;
        this.brandId = brandId;
        this.devType = devType;
        this.devCate = devCate;
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

    public String getBrandId() {
        return brandId;
    }
    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    public String getDeviceType() {
        return devType;
    }
    public String getDeviceCate() { return devCate; }

    public String getNodeInfo() {
        return nodeInfo;
    }
    public String getRoomName() {
        return roomName;
    }
    public boolean getNodeStatus() {return status;}
    public void setNodeStatus(boolean status) {this.status = status;}
}
