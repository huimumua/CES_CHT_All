package com.askey.firefly.zwave.control.dao;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.ToOne;

import java.util.List;

/**
 * Created by edison_chang on 10/27/2017.
 */

@Entity(createInDb=false)
public class ZwaveDeviceScene implements Parcelable {
    @Id(autoincrement = true)
    private Long sceneId;
    @Property(nameInDb = "scene")
    private String scene;
    @Property(nameInDb = "condition")
    private String condition;
    @ToOne(joinProperty = "scene")
    private ZwaveDevice zwaveDevice;

    @Generated(hash = 2022827838)
    public ZwaveDeviceScene(Long sceneId, String scene, String condition) {
        this.sceneId = sceneId;
        this.scene = scene;
        this.condition = condition;
    }

    protected ZwaveDeviceScene(Parcel in) {
        sceneId = in.readLong();
        scene = in.readString();
        condition = in.readString();
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
        dest.writeLong(sceneId);
        dest.writeString(scene);
        dest.writeString(condition);
    }

    public Long getSceneId() {
        return this.sceneId;
    }

    public void setSceneId(Long sceneId) { this.sceneId = sceneId; }

    public String getScene() {
        return this.scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public String getCondition() {
        return this.condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public ZwaveDevice getZwaveDevice() {
        return this.zwaveDevice;
    }
}
