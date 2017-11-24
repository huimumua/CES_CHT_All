package com.askey.mobile.zwave.control.deviceContr.model;

/**
 * Created by skysoft on 2017/11/15.
 */

public class RoomInfo_state extends RoomInfo{

    private boolean isSelected;

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
