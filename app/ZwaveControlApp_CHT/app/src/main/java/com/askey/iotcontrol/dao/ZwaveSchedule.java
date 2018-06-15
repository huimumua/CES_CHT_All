package com.askey.iotcontrol.dao;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

/**
 * Created by edison_chang on 11/23/2017.
 */

@Entity(createInDb = false)
public class ZwaveSchedule implements Parcelable {
    @Id(autoincrement = true)
    private Long scheduleId;
    @Property(nameInDb = "nodeId")
    private Integer nodeId;
    @Property(nameInDb = "jobId")
    private Integer jobId;
    @Property(nameInDb = "variableValueStart")
    private Integer variableValueStart;
    @Property(nameInDb = "variableValueEnd")
    private Integer variableValueEnd;
    @Property(nameInDb = "startTime")
    private String startTime;
    @Property(nameInDb = "endTime")
    private String endTime;
    @Property(nameInDb = "day")
    private String day;
    @Property(nameInDb = "active")
    private String active;

    @Generated(hash = 322272163)
    public ZwaveSchedule(Long scheduleId, Integer nodeId, Integer jobId, Integer variableValueStart, Integer variableValueEnd, String startTime, String endTime, String day, String active) {
        this.scheduleId = scheduleId;
        this.nodeId = nodeId;
        this.jobId = jobId;
        this.variableValueStart = variableValueStart;
        this.variableValueEnd = variableValueEnd;
        this.startTime = startTime;
        this.endTime = endTime;
        this.day = day;
        this.active = active;
    }

    protected ZwaveSchedule(Parcel in) {
        scheduleId = in.readLong();
        nodeId = in.readInt();
        jobId = in.readInt();
        variableValueStart = in.readInt();
        variableValueEnd = in.readInt();
        startTime = in.readString();
        endTime = in.readString();
        day = in.readString();
        active = in.readString();
    }

    @Generated(hash = 2018455896)
    public ZwaveSchedule() {
    }

    public static final Creator<ZwaveSchedule> CREATOR = new Creator<ZwaveSchedule>() {
        @Override
        public ZwaveSchedule createFromParcel(Parcel in) {
            return new ZwaveSchedule(in);
        }

        @Override
        public ZwaveSchedule[] newArray(int size) {
            return new ZwaveSchedule[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(scheduleId);
        dest.writeInt(nodeId);
        dest.writeInt(jobId);
        dest.writeInt(variableValueStart);
        dest.writeInt(variableValueEnd);
        dest.writeString(startTime);
        dest.writeString(endTime);
        dest.writeString(day);
        dest.writeString(active);
    }

    public Long getScheduleId() {
        return this.scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public Integer getNodeId() {
        return this.nodeId;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public String getDay() {
        return this.day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public Integer getJobId() {
        return this.jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public String getActive() {
        return this.active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getEndTime() {
        return this.endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStartTime() {
        return this.startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public Integer getVariableValueEnd() {
        return this.variableValueEnd;
    }

    public void setVariableValueEnd(Integer variableValueEnd) {
        this.variableValueEnd = variableValueEnd;
    }

    public Integer getVariableValueStart() {
        return this.variableValueStart;
    }

    public void setVariableValueStart(Integer variableValueStart) {
        this.variableValueStart = variableValueStart;
    }
}
