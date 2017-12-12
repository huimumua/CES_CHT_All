package com.askey.mobile.zwave.control.deviceContr.model;

/**
 * 项目名称：ZwaveControlClient
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2017/10/19 11:35
 * 修改人：skysoft
 * 修改时间：2017/10/19 11:35
 * 修改备注：
 */
public class ScenesInfo {
    /**
     * 情景模式Id
     * */
    private String scenesId;
    /**
     * 情景模式名称
     * */
    private String scenesName;

    private String nodeId;

    private String category;

    private String targetSatus;

    private String currentStatus;

    private String targetColor;

    private String currentColor;

    private String timer;

    public String getScenesId() {
        return scenesId;
    }

    public void setScenesId(String scenesId) {
        this.scenesId = scenesId;
    }

    public String getScenesName() {
        return scenesName;
    }

    public void setScenesName(String scenesName) {
        this.scenesName = scenesName;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTargetSatus() {
        return targetSatus;
    }

    public void setTargetSatus(String targetSatus) {
        this.targetSatus = targetSatus;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getTargetColor() {
        return targetColor;
    }

    public void setTargetColor(String targetColor) {
        this.targetColor = targetColor;
    }

    public String getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(String currentColor) {
        this.currentColor = currentColor;
    }

    public String getTimer() {
        return timer;
    }

    public void setTimer(String timer) {
        this.timer = timer;
    }
}
