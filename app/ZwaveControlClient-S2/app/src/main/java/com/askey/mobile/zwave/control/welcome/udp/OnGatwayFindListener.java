package com.askey.mobile.zwave.control.welcome.udp;

/***
 * Company: Chengdu Skysoft Info&Tech Co., Ltd.
 * Copyright ©2014-2018 成都天软信息技术有限公司
 * Created by mark on 2016/4/26.
 *
 * @since:JDK1.7
 * @version:1.0
 ***/
public interface OnGatwayFindListener {
    void onGatwayFind(String server_ip,String data);
    void canNotFindGWay();
}
