package com.askey.mobile.zwave.control.deviceContr.model;

/**
 * 项目名称：ZwaveControlClient
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2017/11/28 17:09
 * 修改人：skysoft
 * 修改时间：2017/11/28 17:09
 * 修改备注：
 */
public class ScheduleInfo {

    private String dateName;

    private String startTime;

    private String endTime;

    public String getDateName() {
        return dateName;
    }

    public void setDateName(String dateName) {
        this.dateName = dateName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
