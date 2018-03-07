package com.askey.mobile.zwave.control.deviceContr.net;

/**
 * 项目名称：ZwaveControlClient
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2017/7/27 16:50
 * 修改人：skysoft
 * 修改时间：2017/7/27 16:50
 * 修改备注：
 */
public interface TCPReceive {
    void onConnect(SocketTransceiver transceiver);
    void onConnectFailed();
    void receiveMessage(SocketTransceiver transceiver, String tcpMassage);
    void onDisconnect(SocketTransceiver transceiver);
}
