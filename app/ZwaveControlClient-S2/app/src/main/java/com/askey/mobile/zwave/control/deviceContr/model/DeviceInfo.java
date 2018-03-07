package com.askey.mobile.zwave.control.deviceContr.model;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * 项目名称：ZwaveControlClient
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2017/10/19 10:39
 * 修改人：skysoft
 * 修改时间：2017/10/19 10:39
 * 修改备注：
 */
public class DeviceInfo implements Comparable<DeviceInfo>, Serializable {
    /**
     * 设备Id
     */
    private String DeviceId;
    /**
     * 设备唯一Id
     */
    private String UniqueId;
    /**
     * 设备品牌
     */
    private String Brand;
    /**
     * 设备类型
     */
    private String DeviceType;
    /**
     * 设备名称
     */
    private String DisplayName;

    private String DeviceModel;
    /**
     * 所属情景模式 Id
     */
    private String ScenesId;
    /**
     * 所属房间 Id
     */
    private String RoomId;
    /**
     * 所属房间 home livingroom 等
     */
    private String Rooms;
    /**
     * 是否移除此房间 home不可移除
     * 0 未移除
     * 1 移除
     */
    private String IsRemove;
    /**
     * 1是0否为最爱
     */
    private String IsFavorite;
    /**
     * 最近使用时间
     */
    private String RecentlyUseTime;
    /**
     * 通知触发条件
     * -1 没有触发
     * 0 High Consumption
     * 1 Atypical Consumption
     * 2 device switched on
     * 3 switch switched on
     */
    private String TriggerCondition;
    /**
     * 通知类型
     * -1 不做任何推送
     * 0 push Notification
     * 1 Email
     * 3 push Notification and Email
     */
    private String TriggerType;
    /**
     * 开始触发时间
     */
    private String ScheduleStartTime;
    /**
     * 结束触发时间
     */
    private String ScheduleEndTime;
    /**
     * 持续天数
     */
    private String ScheduleDurationTime;
    /**
     * 是否模拟家中有人
     * 0 不开启
     * 1 开启
     */
    private String IsLookInhabited;
    /**
     * 触发设备类型
     * -1 都关闭不触发
     * 0 if leaving 离开时关闭
     * 1 if entering 返回时打开
     * 2  都打开触发
     */
    private String DeviceTriggersType;

    private String nodeInfo;

    private String topic;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getNodeInfo() {
        return nodeInfo;
    }

    public void setNodeInfo(String nodeInfo) {
        this.nodeInfo = nodeInfo;
    }

    public String getDeviceId() {
        return DeviceId;
    }

    public void setDeviceId(String deviceId) {
        DeviceId = deviceId;
    }

    public String getUniqueId() {
        return UniqueId;
    }

    public void setUniqueId(String uniqueId) {
        UniqueId = uniqueId;
    }

    public String getBrand() {
        return Brand;
    }

    public void setBrand(String brand) {
        Brand = brand;
    }

    public String getDeviceType() {
        return DeviceType;
    }

    public void setDeviceType(String deviceType) {
        DeviceType = deviceType;
    }

    public String getDisplayName() {
        return DisplayName;
    }

    public void setDisplayName(String displayName) {
        DisplayName = displayName;
    }

    public String getDeviceModel() {
        return DeviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        DeviceModel = deviceModel;
    }

    public String getScenesId() {
        return ScenesId;
    }

    public void setScenesId(String scenesId) {
        ScenesId = scenesId;
    }

    public String getRoomId() {
        return RoomId;
    }

    public void setRoomId(String roomId) {
        RoomId = roomId;
    }

    public String getRooms() {
        return Rooms;
    }

    public void setRooms(String rooms) {
        Rooms = rooms;
    }

    public String getIsRemove() {
        return IsRemove;
    }

    public void setIsRemove(String isRemove) {
        IsRemove = isRemove;
    }

    public String getIsFavorite() {
        return IsFavorite;
    }

    public void setIsFavorite(String isFavorite) {
        IsFavorite = isFavorite;
    }

    public String getRecentlyUseTime() {
        return RecentlyUseTime;
    }

    public void setRecentlyUseTime(String recentlyUseTime) {
        RecentlyUseTime = recentlyUseTime;
    }

    public String getTriggerCondition() {
        return TriggerCondition;
    }

    public void setTriggerCondition(String triggerCondition) {
        TriggerCondition = triggerCondition;
    }

    public String getTriggerType() {
        return TriggerType;
    }

    public void setTriggerType(String triggerType) {
        TriggerType = triggerType;
    }

    public String getScheduleStartTime() {
        return ScheduleStartTime;
    }

    public void setScheduleStartTime(String scheduleStartTime) {
        ScheduleStartTime = scheduleStartTime;
    }

    public String getScheduleEndTime() {
        return ScheduleEndTime;
    }

    public void setScheduleEndTime(String scheduleEndTime) {
        ScheduleEndTime = scheduleEndTime;
    }

    public String getScheduleDurationTime() {
        return ScheduleDurationTime;
    }

    public void setScheduleDurationTime(String scheduleDurationTime) {
        ScheduleDurationTime = scheduleDurationTime;
    }

    public String getIsLookInhabited() {
        return IsLookInhabited;
    }

    public void setIsLookInhabited(String isLookInhabited) {
        IsLookInhabited = isLookInhabited;
    }

    public String getDeviceTriggersType() {
        return DeviceTriggersType;
    }

    public void setDeviceTriggersType(String deviceTriggersType) {
        DeviceTriggersType = deviceTriggersType;
    }


    @Override
    public int compareTo(@NonNull DeviceInfo o) {
        return (Long.parseLong(this.RecentlyUseTime) > Long.parseLong(o.RecentlyUseTime) ? -1 : (Long.parseLong(this.RecentlyUseTime) == Long.parseLong(o.RecentlyUseTime) ? 0 : 1));
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "RecentlyUseTime='" + RecentlyUseTime + '\'' +
                '}';
    }
}
