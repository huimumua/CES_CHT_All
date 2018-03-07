package com.askey.mobile.zwave.control.deviceContr.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 项目名称：ZwaveControlClient
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2017/10/19 11:35
 * 修改人：skysoft
 * 修改时间：2017/10/19 11:35
 * 修改备注：
 */
public class ScenesInfo implements Parcelable{
    /**
     * 情景模式Id
     * */
    private String scenesId;
    /**
     * 情景模式名称
     * */
    private String scenesName;

    private String iconName;

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(scenesId);
        dest.writeString(scenesName);
        dest.writeString(iconName);
        dest.writeString(nodeId);
        dest.writeString(deviceName);
        dest.writeString(category);
        dest.writeString(targetSatus);
        dest.writeString(currentStatus);
        dest.writeString(targetColor);
        dest.writeString(currentColor);
        dest.writeString(timer);
    }

    public static final Creator<ScenesInfo> CREATOR = new Creator<ScenesInfo>() {
        @Override
        public ScenesInfo createFromParcel(Parcel in) {
            ScenesInfo scenesInfo = new ScenesInfo();
            scenesInfo.scenesId = in.readString();
            scenesInfo.scenesName = in.readString();
            scenesInfo.iconName = in.readString();
            scenesInfo.nodeId = in.readString();
            scenesInfo.deviceName = in.readString();
            scenesInfo.category = in.readString();
            scenesInfo.targetSatus = in.readString();
            scenesInfo.currentStatus = in.readString();
            scenesInfo.targetColor = in.readString();
            scenesInfo.currentColor = in.readString();
            scenesInfo.timer = in.readString();
            return scenesInfo;
        }

        @Override
        public ScenesInfo[] newArray(int size) {
            return new ScenesInfo[size];
        }
    };

    public String getIconName() {
        return iconName;
    }


    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    private String nodeId;

    private String deviceName;

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

    @Override
    public int describeContents() {
        return 0;
    }


}
