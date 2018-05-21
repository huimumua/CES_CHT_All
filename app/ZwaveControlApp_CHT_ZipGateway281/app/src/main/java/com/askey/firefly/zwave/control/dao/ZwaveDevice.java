package com.askey.firefly.zwave.control.dao;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by skysoft on 2017/8/7.
 */

@Entity(createInDb=false)
public class ZwaveDevice implements Parcelable {
    @Id(autoincrement = true)
    private Long zwaveId;
    @Property(nameInDb = "brand")
    private String brand;
    @Property(nameInDb = "nodeId")
    private Integer nodeId;
    @Property(nameInDb = "name")
    private String name;
    @Property(nameInDb = "devType")
    private String devType;
    @Property(nameInDb = "category")
    private String category;
    @Property(nameInDb = "roomName")
    private String roomName;
    @Property(nameInDb = "isFavorite")
    private String isFavorite;
    @Property(nameInDb = "interfaceId")
    private Integer interfaceId;
    @Property(nameInDb = "nodeInfo")
    private String nodeInfo;
    @Property(nameInDb = "timestamp")
    private Long timestamp;

    @Generated(hash = 2088078994)
    public ZwaveDevice(Long zwaveId, String brand, Integer nodeId, String name, String devType, String category,
            String roomName, String isFavorite, Integer interfaceId, String nodeInfo, Long timestamp) {
        this.zwaveId = zwaveId;
        this.brand = brand;
        this.nodeId = nodeId;
        this.name = name;
        this.devType = devType;
        this.category = category;
        this.roomName = roomName;
        this.isFavorite = isFavorite;
        this.interfaceId = interfaceId;
        this.nodeInfo = nodeInfo;
        this.timestamp = timestamp;
    }

    @Generated(hash = 565410297)
    public ZwaveDevice() {
    }


    protected ZwaveDevice(Parcel in) {
        zwaveId = in.readLong();
        brand = in.readString();
        nodeId = in.readInt();
        name = in.readString();
        devType = in.readString();
        category = in.readString();
        roomName = in.readString();
        isFavorite = in.readString();
        interfaceId = in.readInt();
        nodeInfo = in.readString();
        timestamp = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(zwaveId);
        dest.writeString(brand);
        dest.writeInt(nodeId);
        dest.writeString(name);
        dest.writeString(devType);
        dest.writeString(category);
        dest.writeString(roomName);
        dest.writeString(isFavorite);
        dest.writeInt(interfaceId);
        dest.writeString(nodeInfo);
        dest.writeLong(timestamp);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ZwaveDevice> CREATOR = new Creator<ZwaveDevice>() {
        @Override
        public ZwaveDevice createFromParcel(Parcel in) {
            return new ZwaveDevice(in);
        }

        @Override
        public ZwaveDevice[] newArray(int size) {
            return new ZwaveDevice[size];
        }
    };


    public Long getZwaveId() {
        return this.zwaveId;
    }

    public void setZwaveId(Long zwaveId) {
        this.zwaveId = zwaveId;
    }

    public String getBrand() {
        return this.brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Integer getNodeId() {
        return this.nodeId;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDevType() {
        return this.devType;
    }

    public void setDevType(String devType) {
        this.devType = devType;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRoomName() {
        return this.roomName;
    }

    public void setRoomName(String roomName) {this.roomName = roomName;
    }

    public String getFavorite() {
        return this.isFavorite;
    }

    public void setFavorite(String isFavorite) {
        this.isFavorite = isFavorite;
    }

    public String getNodeInfo() {
        return this.nodeInfo;
    }

    public void setNodeInfo(String nodeInfo) {
        this.nodeInfo = nodeInfo;
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getIsFavorite() {
        return this.isFavorite;
    }

    public void setIsFavorite(String isFavorite) {
        this.isFavorite = isFavorite;
    }

    public Integer getInterfaceId() {
        return this.interfaceId;
    }

    public void setInterfaceId(Integer interfaceId) {
        this.interfaceId = interfaceId;
    }

}
