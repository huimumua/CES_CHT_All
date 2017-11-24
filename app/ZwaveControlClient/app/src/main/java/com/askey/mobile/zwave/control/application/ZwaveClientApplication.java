package com.askey.mobile.zwave.control.application;

import android.app.Application;

/**
 * 项目名称：ZwaveControlClient
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2017/7/20 11:29
 * 修改人：skysoft
 * 修改时间：2017/7/20 11:29
 * 修改备注：
 */
public class ZwaveClientApplication extends Application {
    private static ZwaveClientApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

    }

    public static ZwaveClientApplication getInstance() {
        return instance;
    }

}

