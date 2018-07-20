package com.askey.mobile.zwave.control.util;

/**
 * Created by skysoft on 2018/7/13.
 */

public class TimeUtils {
    /*
 * 获取时间戳
 */
    public static String gettimeStamp() {
        long timeStamp = System.currentTimeMillis();
        return String.valueOf(timeStamp);
    }
}
