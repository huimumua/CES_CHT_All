package com.askey.mobile.zwave.control.home.activity.addDevice;

import com.askey.mobile.zwave.control.interf.DeleteDeviceListener;

/**
 * Created by skysoft on 2018/4/11.
 */

public class DeleteDevice {
    private static DeleteDeviceListener deleteDeviceListener;
    public static void setDeleteDeviceListener(DeleteDeviceListener listener){
        deleteDeviceListener = listener;
    }

    static void deleteSuccess(String roomName) {
        if (deleteDeviceListener != null) {
            deleteDeviceListener.deleteSuccess(roomName);
        }
    }
}
