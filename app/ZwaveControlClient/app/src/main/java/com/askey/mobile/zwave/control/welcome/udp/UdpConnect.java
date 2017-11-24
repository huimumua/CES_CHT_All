package com.askey.mobile.zwave.control.welcome.udp;

import android.content.Context;

import com.askey.mobile.zwave.control.util.Logg;


/***
 * Company: Chengdu Skysoft Info&Tech Co., Ltd.
 * Copyright ©2014-2018 Chengdu Skysoft Info&Tech Co., Ltd.
 * Created by Mark on 2017/2/6.
 *
 * @since:JDK1.6
 * @version:1.0
 * @see
 ***/
public class UdpConnect implements OnGatwayFindListener {
    private static final String TAG = UdpConnect.class.getSimpleName();
    private Runnable onGatwayFindRunnable;
    private Runnable oncanNotFindGWayRunnable;
    private String serverUri;
    private String topic;
    private String tutk_tuuid;


    public String getServerUri() {
        return serverUri;
    }

    public void setServerUri(String serverUri) {
        this.serverUri = serverUri;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTutk_tuuid() {
        return tutk_tuuid;
    }

    public void setTutk_tuuid(String tutk_tuuid) {
        this.tutk_tuuid = tutk_tuuid;
    }

    public void search(Context context, Runnable onGatwayFindRunnable, Runnable oncanNotFindGWayRunnable) {
        this.onGatwayFindRunnable = onGatwayFindRunnable;
        this.oncanNotFindGWayRunnable = oncanNotFindGWayRunnable;
        UDPConnectin uDPConnectin = UDPConnectin.getInstance();
        uDPConnectin.startScan(this, context);
    }

    @Override
    public void onGatwayFind(String server_ip,String data) {
        Logg.i(TAG, "onGatwayFind: server_ip=" + server_ip);
        Logg.i(TAG, "onGatwayFind: data=" + data);
        String str[] = data.split(":");
        setServerUri(server_ip);
        setTopic(str[2]);
        setTutk_tuuid(str[1]);
//        HttpFactory.setServerIp(server_ip);
        if (onGatwayFindRunnable != null) {
            new Thread(onGatwayFindRunnable).start();
        }
    }

    @Override
    public void canNotFindGWay() {
        Logg.i(TAG, "canNotFindGWay");
//        HttpFactory.setServerIp(null);
        if (oncanNotFindGWayRunnable != null) {
            Logg.i(TAG, "canNotFindGWay===1111==");
            new Thread(oncanNotFindGWayRunnable).start();
        }
//        Intent intent=new Intent("show_dialog");
//        ZwaveClientApplication.getInstance().getApplicationContext().sendBroadcast(intent);

    }

}
