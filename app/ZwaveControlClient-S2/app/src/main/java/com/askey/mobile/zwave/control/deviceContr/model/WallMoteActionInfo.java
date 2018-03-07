package com.askey.mobile.zwave.control.deviceContr.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 项目名称：ZwaveControlClient
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2017/11/30 11:40
 * 修改人：skysoft
 * 修改时间：2017/11/30 11:40
 * 修改备注：
 */
public class WallMoteActionInfo implements Serializable{

    private String nodeId;
    private String endpointId;
    private String groupId;
    private String icon;
    private String type;
    private String name;
    private String nodeInfo;
    private ArrayList nodeInterFaceList;


    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNodeInfo() {
        return nodeInfo;
    }

    public void setNodeInfo(String nodeInfo) {
        this.nodeInfo = nodeInfo;
    }

    public ArrayList getNodeInterFaceList() {
        return nodeInterFaceList;
    }

    public void setNodeInterFaceList(ArrayList nodeInterFaceList) {
        this.nodeInterFaceList = nodeInterFaceList;
    }
}
