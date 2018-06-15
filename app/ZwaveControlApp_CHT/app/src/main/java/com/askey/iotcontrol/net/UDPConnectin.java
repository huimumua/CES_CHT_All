package com.askey.iotcontrol.net;

import android.content.Context;
import android.util.Log;

import com.askey.iotcontrol.utils.Const;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/***
 * Company: Chengdu Skysoft Info&Tech Co., Ltd.
 * Copyright ©2014-2018 成都天软信息技术有限公司
 * Created by mark on 2016/4/26.
 *
 * @since:JDK1.7
 * @version:1.0
 ***/

public class UDPConnectin {
    private static final String LOG_TAG = UDPConnectin.class.getSimpleName();
    private static final String BROADCAST_CLIENT_KEY = "mobile_zwave";
    private static final int CLIENT_SEND_MAX_DATA_PACKET_LENGTH = 40;
    private static final int CLIENT_RECEIVE_MAX_DATA_PACKET_LENGTH = 256;
    private static final int CLIENT_BROADCAST_SEND_PORT = 43708;
    private static final int CLIENT_BROADCAST_RECEIVE_PORT = 43709;
    private DatagramSocket sendUdpSocket;
    private DatagramSocket ReceiveUdpSocket;
    private boolean mInRuning = false;
    private static Context context;


    //private String serverUri = mutils.getAndroidID();//需要更换为真实地址
    private static String topic = Const.mqttClientId + ":" + Const.PublicTopicName;

    public UDPConnectin(Context context) {
        this.context = context;

        Log.i(LOG_TAG, "UDPConnectin:public="+topic);

    }
    private String BROADCAST_SERVICE_DATA = "firfly_zwave:"+ topic ;

    protected boolean startScan() {
        if (!mInRuning) {
            new ReceiveBroadCastUdp().start();
            new BroadCastUdp(BROADCAST_CLIENT_KEY).start();
            return true;
        }

        return false;
    }

    public boolean startReceiver() {
        if (!mInRuning) {
            new ServiceBroadCast(BROADCAST_SERVICE_DATA).start();
            return true;
        }
        return false;
    }

    public void stopConn() {
        mInRuning = false;
        if(sendUdpSocket != null){
            sendUdpSocket.close();
            sendUdpSocket = null;
        }
        if(ReceiveUdpSocket != null){
            ReceiveUdpSocket.close();
            ReceiveUdpSocket = null;
        }
    }

    public class BroadCastUdp extends Thread {
        private String dataString;
        // private DatagramSocket sendUdpSocket;

        public BroadCastUdp(String dataString) {
            this.dataString = dataString;
        }

        public void run() {
            mInRuning = true;
            DatagramPacket dataPacket = null;
            byte[] buffer = new byte[CLIENT_SEND_MAX_DATA_PACKET_LENGTH];
            try {
                sendUdpSocket = new DatagramSocket(CLIENT_BROADCAST_SEND_PORT);
                // sendUdpSocket = new DatagramSocket(BROADCAST_SEND_PORT);
                dataPacket = new DatagramPacket(buffer, CLIENT_SEND_MAX_DATA_PACKET_LENGTH);
                byte[] data = dataString.getBytes();
                dataPacket.setData(data);
                dataPacket.setLength(data.length);
                dataPacket.setPort(CLIENT_BROADCAST_SEND_PORT);

                InetAddress broadcastAddr = InetAddress.getByName("255.255.255.255");
                dataPacket.setAddress(broadcastAddr);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.toString());
            }
            while ((sendUdpSocket != null) && !sendUdpSocket.isClosed()) {
                int delay = 50; // //延时(delay*10)ms发送一个广播
                try {
                    sendUdpSocket.send(dataPacket);
                    while (delay-- > 0 && sendUdpSocket != null && !sendUdpSocket.isClosed())
                        sleep(10);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, e.toString());
                }
            }
            Log.i(LOG_TAG, "BroadCastUdp stop！");

        }
    }

    public class ReceiveBroadCastUdp extends Thread {
        public void run() {
            DatagramPacket udpPacket = null;
            byte[] data = new byte[CLIENT_RECEIVE_MAX_DATA_PACKET_LENGTH];
            try {
                ReceiveUdpSocket = new DatagramSocket(CLIENT_BROADCAST_RECEIVE_PORT);
                udpPacket = new DatagramPacket(data, data.length);
            } catch (SocketException e1) {
                e1.printStackTrace();
            }
            while (true) {
                try {
                    ReceiveUdpSocket.receive(udpPacket);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
                if (null != udpPacket.getAddress()) {
                    String server_ip = udpPacket.getAddress().toString().substring(1);
                    String key = new String(data, 0, udpPacket.getLength());
                    Log.i(LOG_TAG, "gateway response!=" + udpPacket.getAddress() + " client_ip=" + server_ip + " key=" + key);
                    if(key.equals(BROADCAST_SERVICE_DATA)){
                        stopConn();
                        break;
                    }
                }
            }
        }
    }


    public class ServiceBroadCast extends Thread {
        private String replyString;

        public ServiceBroadCast(String dataString) {
            this.replyString = dataString;
        }

        public void run() {
            mInRuning = true;
            DatagramPacket udpPacket;
            byte[] dataReceive = new byte[CLIENT_SEND_MAX_DATA_PACKET_LENGTH]; //这里收的长度是客户端发送的长度
            udpPacket = new DatagramPacket(dataReceive, dataReceive.length);
            try {
                ReceiveUdpSocket = new DatagramSocket(CLIENT_BROADCAST_SEND_PORT); //这里收的是客户端发送的端口
                sendUdpSocket = new DatagramSocket(CLIENT_BROADCAST_RECEIVE_PORT); //这里发的是客户端接收的端口
            } catch (SocketException e1) {
                e1.printStackTrace();
            }
            while (true) {
                try {
					Log.i(LOG_TAG, "ServiceBroadCast wait！");
                    ReceiveUdpSocket.receive(udpPacket);
					Log.i(LOG_TAG, "ServiceBroadCast ok");
                } catch (Exception e) {
                    Log.e(LOG_TAG,"ServiceBroadCast break"+ e.toString());
                    break;
                }
                if (null != udpPacket.getAddress()) {
                    String client_ip = udpPacket.getAddress().toString().substring(1);
                    String key = new String(dataReceive, 0, udpPacket.getLength());
                    Log.i(LOG_TAG, "cliect request!=" + udpPacket.getAddress() + " client_ip=" + client_ip + " key=" + key);

                    if(key.equals(BROADCAST_CLIENT_KEY)) {
                        replyClient(client_ip, replyString);
                    }
                }
            }

            stopConn();
        }
    }

    private void replyClient(final String client_ip, final String replyString) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatagramPacket dataPacket = null;
                byte[] buffer = new byte[CLIENT_RECEIVE_MAX_DATA_PACKET_LENGTH]; //这里发的长度是客户端接收的长度
                try {
                    dataPacket = new DatagramPacket(buffer, CLIENT_RECEIVE_MAX_DATA_PACKET_LENGTH);
                    byte[] dataSend = replyString.getBytes();
                    dataPacket.setData(dataSend);
                    dataPacket.setLength(dataSend.length);
                    dataPacket.setPort(CLIENT_BROADCAST_RECEIVE_PORT);

                    InetAddress broadcastAddr = InetAddress.getByName(client_ip);
                    dataPacket.setAddress(broadcastAddr);
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.toString());
                }
                int sendTime = 500; //广播持续(sendTime*10)s
                while (sendTime-- > 0) {
                    try {
                        if(sendUdpSocket != null && !sendUdpSocket.isClosed()){
                            sendUdpSocket.send(dataPacket);
                        }else {
                            break;
                        }
                        Thread.sleep(10);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(LOG_TAG, e.toString());
                        break;
                    }
                }
            }
        }).start();
    }
}
