package com.askey.mobile.zwave.control.welcome.udp;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;

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
    private static final String TAG = "UDPConnectin";
    private static final String BROADCAST_CLIENT_KEY = "mobile_zwave";
    private static final String BROADCAST_SERVICE_KEY = "firfly_zwave";
    private static final int CLIENT_SEND_MAX_DATA_PACKET_LENGTH = 40;
    private static final int CLIENT_RECEIVE_MAX_DATA_PACKET_LENGTH = 256;
    private static final int CLIENT_BROADCAST_SEND_PORT = 43708;
    private static final int CLIENT_BROADCAST_RECEIVE_PORT = 43709;
    private DatagramSocket sendUdpSocket;
    private DatagramSocket ReceiveUdpSocket;
    private boolean mInRuning = false;
    private static UDPConnectin mInstance;
    private OnGatwayFindListener activity;
    private Context context;
    private static final int SCNA_TIME = 50; //50*(10*10)ms
    private static int restScantime = SCNA_TIME; //ms

    private UDPConnectin() { }

    public static UDPConnectin getInstance() {
        if (mInstance == null)
            mInstance = new UDPConnectin();

        return mInstance;
    }

    public boolean startScan(OnGatwayFindListener listener, Context context) {
        this.activity = listener;
        this.context = context;
        restScantime = SCNA_TIME;
        if (!mInRuning) {
            mInRuning = true;
            new ReceiveBroadCastUdp().start();
            new BroadCastUdp(BROADCAST_CLIENT_KEY).start();
            Logg.i(TAG, "startScan: LocalIPAddress="+getLocalIPAddress());
            return true;
        }
        Logg.i(TAG, "startScan: already in scanning");

        return false;
    }

    public boolean startReceiver() {
        if (!mInRuning) {
            mInRuning = true;
            new ServiceBroadCast(BROADCAST_SERVICE_KEY).start();
            return true;
        }

        return false;
    }

    public void stopConn() {
        if(sendUdpSocket != null){
            sendUdpSocket.close();
            sendUdpSocket = null;
        }
        if(ReceiveUdpSocket != null){
            ReceiveUdpSocket.close();
            ReceiveUdpSocket = null;
        }
        mInRuning = false;
    }

    public boolean isInRuning() {
        return mInRuning;
    }

    private String getLocalIPAddress() {
//		try {
//			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
//				NetworkInterface intf = en.nextElement();
//				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
//					InetAddress inetAddress = enumIpAddr.nextElement();
//					if (!inetAddress.isLoopbackAddress()) {
//						return inetAddress.getHostAddress().toString();
//					}
//				}
//			}
//		} catch (SocketException ex) {
//			Logg.e(TAG, ex.toString());
//		}
//		return null;

        //获取wifi服务
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return intToIp(ipAddress);
    }

    private static String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
                + "." + (i >> 24 & 0xFF);
    }

    public class BroadCastUdp extends Thread {
        private String dataString;

        public BroadCastUdp(String dataString) {
            this.dataString = dataString;
        }

        public void run() {
            DatagramPacket dataPacket = null;
            byte[] buffer = new byte[CLIENT_SEND_MAX_DATA_PACKET_LENGTH];
            try {
                sendUdpSocket = new DatagramSocket(CLIENT_BROADCAST_SEND_PORT);
                dataPacket = new DatagramPacket(buffer, CLIENT_SEND_MAX_DATA_PACKET_LENGTH);
                byte[] data = dataString.getBytes();
                dataPacket.setData(data);
                dataPacket.setLength(data.length);
                dataPacket.setPort(CLIENT_BROADCAST_SEND_PORT);

                InetAddress broadcastAddr = InetAddress.getByName("255.255.255.255");
                dataPacket.setAddress(broadcastAddr);
            } catch (Exception e) {
                Logg.e(TAG, e.toString());
            }
            while ((sendUdpSocket != null) && !sendUdpSocket.isClosed() && restScantime-- > 0) {
                int delay = 10; // //延时(delay*10)ms发送一个广播
                try {
                    sendUdpSocket.send(dataPacket);
                    while (delay-- > 0 && sendUdpSocket != null && !sendUdpSocket.isClosed())
                        sleep(10);
                } catch (Exception e) {
                    e.printStackTrace();
                    Logg.e(TAG, e.toString());
                }
            }
            if((sendUdpSocket != null && !sendUdpSocket.isClosed()) && restScantime < 0)
                activity.canNotFindGWay();
            stopConn();
            Logg.i(TAG, "BroadCastUdp: stop");
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
//                    e.printStackTrace();
                    break;
                }
                if (null != udpPacket.getAddress()) {
                    String server_ip = udpPacket.getAddress().toString().substring(1);
                    String key = new String(data, 0, udpPacket.getLength());
                    if(key.contains(BROADCAST_SERVICE_KEY)){
                        Logg.i(TAG, "ReceiveBroadCastUdp: gatway find, " + udpPacket.getAddress() + " server_ip=" + server_ip + " key=" + key);
                        Const.SERVER_IP = server_ip;
                        activity.onGatwayFind("tcp://"+server_ip+":"+ Const.MQTT_PORT,key);
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
//					Logg.i(TAG, "ServiceBroadCast: wait");
                    ReceiveUdpSocket.receive(udpPacket);
//					Logg.i(TAG, "ServiceBroadCast: receive");
                } catch (Exception e) {
//					Logg.i(TAG, "ServiceBroadCast: break");
                    Logg.e(TAG, e.toString());
                    break;
                }
                if (null != udpPacket.getAddress() && (ReceiveUdpSocket != null && !ReceiveUdpSocket.isClosed())) {
                    String client_ip = udpPacket.getAddress().toString().substring(1);
                    String key = new String(dataReceive, 0, udpPacket.getLength());
                    Logg.i(TAG, "ServiceBroadCast: cliect request, " + udpPacket.getAddress() + " client_ip=" + client_ip + " key=" + key);

                    if(key.equals(BROADCAST_CLIENT_KEY))
                        replyClient(client_ip, replyString);
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
                    Logg.e(TAG, e.toString());
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
                        Logg.e(TAG, e.toString());
                        break;
                    }
                }
            }
        }).start();
    }
}
