package com.askey.iotcontrol.dao;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

/**
 * Created by edison_chang on 11/29/2017.
 */

@Entity(createInDb = false)
public class ZwaveDeviceScene implements Parcelable {
    @Id(autoincrement = true)
    private Long deviceSceneId;
    @Property(nameInDb = "sceneName")
    private String sceneName;
    @Property(nameInDb = "nodeId")
    private Integer nodeId;
    @Property(nameInDb = "white")
    private String white;
    @Property(nameInDb = "warm")
    private String warm;
    @Property(nameInDb = "rgb")
    private String rgb;
    @Property(nameInDb = "variableValue")
    private Integer variableValue;
    @Property(nameInDb = "timerTime")
    private String timerTime;

    @Generated(hash = 1349053215)
    public ZwaveDeviceScene(Long deviceSceneId, String sceneName, Integer nodeId, String white, String warm,
            String rgb, Integer variableValue, String timerTime) {
        this.deviceSceneId = deviceSceneId;
        this.sceneName = sceneName;
        this.nodeId = nodeId;
        this.white = white;
        this.warm = warm;
        this.rgb = rgb;
        this.variableValue = variableValue;
        this.timerTime = timerTime;
    }

    protected ZwaveDeviceScene(Parcel in) {
        deviceSceneId = in.readLong();
        sceneName = in.readString();
        nodeId = in.readInt();
        white = in.readString();
        warm = in.readString();
        rgb = in.readString();
        variableValue = in.readInt();
        timerTime = in.readString();
    }

    @Generated(hash = 1179282909)
    public ZwaveDeviceScene() {
    }

    public static final Creator<ZwaveDeviceScene> CREATOR = new Creator<ZwaveDeviceScene>() {
        @Override
        public ZwaveDeviceScene createFromParcel(Parcel in) {
            return new ZwaveDeviceScene(in);
        }

        @Override
        public ZwaveDeviceScene[] newArray(int size) {
            return new ZwaveDeviceScene[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(deviceSceneId);
        dest.writeString(sceneName);
        dest.writeInt(nodeId);
        dest.writeString(white);
        dest.writeString(warm);
        dest.writeString(rgb);
        dest.writeInt(variableValue);
        dest.writeString(timerTime);
    }

    public Integer getVariableValue() {
        return this.variableValue;
    }

    public void setVariableValue(Integer variableValue) {
        this.variableValue = variableValue;
    }

    public String getRgb() {
        return this.rgb;
    }

    public void setRgb(String rgb) {
        this.rgb = rgb;
    }

    public Integer getNodeId() {
        return this.nodeId;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public String getSceneName() {
        return this.sceneName;
    }

    public void setSceneName(String sceneName) {
        this.sceneName = sceneName;
    }

    public Long getDeviceSceneId() {
        return this.deviceSceneId;
    }

    public void setDeviceSceneId(Long deviceSceneId) {
        this.deviceSceneId = deviceSceneId;
    }

    public String getWarm() {
        return this.warm;
    }

    public void setWarm(String warm) {
        this.warm = warm;
    }

    public String getWhite() {
        return this.white;
    }

    public void setWhite(String white) {
        this.white = white;
    }

    public String getTimerTime() {
        return this.timerTime;
    }

    public void setTimerTime(String timerTime) {
        this.timerTime = timerTime;
    }
}
