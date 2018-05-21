package com.askey.firefly.zwave.control.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.askey.firefly.zwave.control.dao.ZwaveDeviceManager;
import com.askey.firefly.zwave.control.dao.ZwaveDeviceSceneManager;
import com.askey.firefly.zwave.control.jni.ZwaveControlHelper;
import com.askey.firefly.zwave.control.net.TCPServer;
import com.askey.firefly.zwave.control.net.UDPConnectin;
import com.askey.firefly.zwave.control.utils.Const;
import com.askey.firefly.zwave.control.utils.DeviceInfo;
import com.askey.firefly.zwave.control.utils.Utils;

import org.eclipse.moquette.server.Server;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class MQTTBroker extends Service {

    private static String LOG_TAG = MQTTBroker.class.getSimpleName();

    MqttAndroidClient mqttLocalClient, mqttRemoteClient;

    private UDPConnectin uDPConnecting = new UDPConnectin(this);
    private Server mqttServer = new Server();

    //TCP server
    private TCPServer mTCPServer;
    // for get/set scene from db
    private ZwaveDeviceSceneManager zwSceneManager;
    // for get/set device info from db
    private ZwaveDeviceManager zwDevManager;

    private byte[] dskNumber;

    private boolean setDefaultFlag = false;




    @Override
    public void onCreate() {

        super.onCreate();

        try {
            mqttServer.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean bResult = uDPConnecting.startReceiver();
        Log.i("MQTTClient", "UDP server = [" + bResult + "]");
        Log.i("MQTTClient", "MQTT Local Server = [" + mqttServer.getServerStatus() + "]");


        /*   set MQTT broker parmeter  */
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(true);


        /*  connect to remote mqtt server */
        /*
        if (Const.remoteMqttFlag) {
            mqttRemoteConnect(mqttConnectOptions);
        }
        */
        /*  connect to local mqtt server */
        mqttLocalConnect(mqttConnectOptions);

        new Thread(initMqtt).start();


        /*  launch tcp server and handle the tcp message */
        Log.i(LOG_TAG, "TCPServer = [" + handleTCPMessage() + "]");

        // init zwSceneManager and zwDevManager
        zwSceneManager = ZwaveDeviceSceneManager.getInstance(this);
        zwDevManager = ZwaveDeviceManager.getInstance(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                receive();
            }

        }).start();

    }

    public static int jni() {
        while (!DeviceInfo.reqFlag){
            try {
                Thread.sleep(100);
                //Log.d(LOG_TAG,"!DeviceInfo.reqFlag !!!!!!!!!!!!!!!!!");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        DeviceInfo.reqFlag = false;
        return DeviceInfo.reqKey;
    }


    public Runnable initMqtt = new Runnable() {
        @Override
        public void run() {
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setAutomaticReconnect(true);
            mqttConnectOptions.setCleanSession(true);
            mqttLocalConnect(mqttConnectOptions);
        }
    };


    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, "===== MQTTBroker onDestroy =====");
        super.onDestroy();

        unsubscribeTopic(Const.PublicTopicName);

        TCPServer.close();


        uDPConnecting.stopConn();
        if (mqttServer.getServerStatus()) {
            Log.i("MQTTClient", "mqttServer.stopServer()");
            mqttServer.stopServer();
        }

        Log.i(LOG_TAG, "===== MQTTBroker endof onDestroy =====");

    }


    @Nullable

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // launch tcp server and handle the tcp message
    // creates the object OnMessageReceived asked by the TCPServer constructor
    private boolean handleTCPMessage() {

        mTCPServer = new TCPServer(new TCPServer.OnMessageReceived() {
            @Override
            //TCPServer class (at while)
            public void messageReceived(int clientID, String message) {
                Const.TCPClientPort = clientID;
                Log.i(LOG_TAG, "TCP received , client ID = " + clientID + " |  message : " + message);
                if (message.contains("mobile_zwave")) {
                    if (message.contains("addDevice")) {
                        /*
                        if (Const.TCPClientPort != 0) {
                            mTCPServer.sendMessage(clientID, Const.TCPSTRING + "addDevice:other"); //TCP format
                        } else {
                            String[] tokens = message.split(":");
                            String devType = tokens[2];
                            Const.TCPClientPort = clientID;
                            Log.i(LOG_TAG, "deviceService.addDevice(mCallback)");
                            DeviceInfo.getMqttPayload = "addDevice";
                        }
                        */
                        String[] tokens = message.split(":");
                        String devType = tokens[2];
                        //Const.TCPClientPort = clientID;
                        Log.i(LOG_TAG, "deviceService.addDevice(mCallback)");
                        DeviceInfo.getMqttPayload = "addDevice";

                    } else if (message.contains("removeDevice")) {
                        /*
                        if (Const.TCPClientPort != 0) {
                            Log.i(LOG_TAG, "removeDevice other!");
                            mTCPServer.sendMessage(clientID, Const.TCPSTRING + "removeDevice:other");  //TCP format
                        } else {

                            String[] tokens = message.split(":");
                            String devType = tokens[2];
                            Const.TCPClientPort = clientID;
                            Log.i(LOG_TAG, "deviceService.removeDevice(mCallback)");
                            DeviceInfo.getMqttPayload = "removeDevice";
                        }
                        */
                        String[] tokens = message.split(":");
                        String devType = tokens[2];
                        //Const.TCPClientPort = clientID;
                        Log.i(LOG_TAG, "deviceService.removeDevice(mCallback)");
                        DeviceInfo.getMqttPayload = "removeDevice";

                    } else if (message.contains("stopAddDevice")) {

                        Log.i(LOG_TAG, "deviceService.stopAddDevice(mCallback)");
                        //Const.TCPClientPort = 0;
                        String[] tokens = message.split(":");
                        String devType = tokens[2];
                        DeviceInfo.getMqttPayload = "stopAddDevice";

                    } else if (message.contains("stopRemoveDevice")) {
                        //Const.TCPClientPort = clientID;
                        Log.i(LOG_TAG, "deviceService.stopRemoveDevice(mCallback)");
                        //Const.TCPClientPort = 0;
                        String[] tokens = message.split(":");
                        String devType = tokens[2];
                        DeviceInfo.getMqttPayload = "stopRemoveDevice";
                        /* 手動移除失敗主動發送TCP
                        Log.d(LOG_TAG, "stopRemoveDevice = " + "\n" + "\t" +"\"MessageType\":" + "\t" + "\"Node Remove Status\"," + "\n" + "\t" + "\"Status\":" +
                                "\t" + "\"Failed\"");
                        mTCPServer.sendMessage(Const.TCPClientPort, "\n" + "\t"+ "\"MessageType\":" + "\t" + "\"Node Remove Status\"," + "\n" + "\t" + "\"Status\":" +
                                "\t" + "\"Failed\""); //TCP format
                        */
                    }

                } else if (message.contains("GrantKeys")) {
                    String[] tmp = message.split(":");
                    if (Integer.valueOf(tmp[1]) == 87) {
                        DeviceInfo.reqKey = 0x87;
                    } else if (Integer.valueOf(tmp[1]) == 86) {
                        DeviceInfo.reqKey = 0x86;
                    } else if (Integer.valueOf(tmp[1]) == 85) {
                        DeviceInfo.reqKey = 0x85;
                    } else if (Integer.valueOf(tmp[1]) == 84) {
                        DeviceInfo.reqKey = 0x84;
                    } else if (Integer.valueOf(tmp[1]) == 83) {
                        DeviceInfo.reqKey = 0x83;
                    } else if (Integer.valueOf(tmp[1]) == 82) {
                        DeviceInfo.reqKey = 0x82;
                    } else if (Integer.valueOf(tmp[1]) == 81) {
                        DeviceInfo.reqKey = 0x81;
                    } else if (Integer.valueOf(tmp[1]) == 80) {
                        DeviceInfo.reqKey = 0x80;
                    } else if (Integer.valueOf(tmp[1]) == 07 || Integer.valueOf(tmp[1]) == 7) {
                        DeviceInfo.reqKey = 0x07;
                    } else if (Integer.valueOf(tmp[1]) == 07 || Integer.valueOf(tmp[1]) == 6) {
                        DeviceInfo.reqKey = 0x06;
                    } else if (Integer.valueOf(tmp[1]) == 07 || Integer.valueOf(tmp[1]) == 5) {
                        DeviceInfo.reqKey = 0x05;
                    } else if (Integer.valueOf(tmp[1]) == 04 || Integer.valueOf(tmp[1]) == 4) {
                        DeviceInfo.reqKey = 0x04;
                    } else if (Integer.valueOf(tmp[1]) == 02 || Integer.valueOf(tmp[1]) == 3) {
                        DeviceInfo.reqKey = 0x03;
                    } else if (Integer.valueOf(tmp[1]) == 02 || Integer.valueOf(tmp[1]) == 2) {
                        DeviceInfo.reqKey = 0x02;
                    } else if (Integer.valueOf(tmp[1]) == 01 || Integer.valueOf(tmp[1]) == 1) {
                        DeviceInfo.reqKey = 0x01;
                    }
                    Log.i(LOG_TAG, "req grant : "+ Integer.valueOf(tmp[1]));
                    DeviceInfo.reqFlag = true;

                } else if (message.contains("dsk")) {
                    String[] tmp = message.split(":");
                    DeviceInfo.reqKey = Integer.valueOf(tmp[1]);
                    Log.i(LOG_TAG, "req dsk : "+ Integer.valueOf(tmp[1]));
                    DeviceInfo.reqFlag = true;

                } else if (message.contains("CSA")) {
                    String[] tmp = message.split(":");
                    DeviceInfo.reqKey = Integer.valueOf(tmp[1]);
                    Log.i(LOG_TAG, "req CSA : "+ Integer.valueOf(tmp[1]));
                    DeviceInfo.reqFlag = true;

                }
            }
        });
        mTCPServer.start();
        return mTCPServer.isAlive();
    }

/*
    //***** connect to remote mqtt server *****
    private void mqttRemoteConnect(MqttConnectOptions mqttConnectOptions) {

        mqttRemoteClient = new MqttAndroidClient(getApplicationContext(), Const.remoteMQTTServerUri, Const.mqttClientId);

        try {
            Log.i(LOG_TAG, " RemoteMClient status = " + "[connecting...]");
            mqttRemoteClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttRemoteClient.setBufferOpts(disconnectedBufferOptions);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(LOG_TAG, " RemoteMClient status = " + "[failed to connect]");
                }
            });

            mqttRemoteClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    if (reconnect) {
                        Log.i(LOG_TAG, " RemoteMClient status = " + "[reconnected]");
                        syncSubscribeTopic();
                    } else {
                        Log.i(LOG_TAG, " RemoteMClient status = " + "[connected]");
                        subscribeToTopic(Const.PublicTopicName);
                    }
                }

                @Override
                public void connectionLost(Throwable cause) {
                    Log.i(LOG_TAG, " RemoteMClient status = " + "[connection was lost]");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String mqttMessage = new String(message.getPayload());
                    mqttMessage = mqttMessage.replaceAll("\n", "");
                    Log.i(LOG_TAG, "Remote MQTT Incoming [" + topic + "] : " + mqttMessage);

                    if (mqttMessage.contains("desired")) {

                        JSONObject jsonObject = new JSONObject(mqttMessage);

                        String data = jsonObject.getJSONObject("state").getJSONObject("desired").getString("data");
                        Log.i(LOG_TAG, "Local MQTT data=" + data);

                        handleMqttIncomingMessage(topic, data);
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });

        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }
*/
    //***** connect to local mqtt server *****
    private void mqttLocalConnect(MqttConnectOptions mqttConnectOptions) {

        Log.i(LOG_TAG, "local mqtt server ip = [" + Const.localMQTTServerUri + "]");

        mqttLocalClient = new MqttAndroidClient(getApplicationContext(), Const.localMQTTServerUri, Const.mqttClientId);

        try {
            Log.i(LOG_TAG, " LocalMClient status = " + "[connecting....]");
            mqttLocalClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(false);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttLocalClient.setBufferOpts(disconnectedBufferOptions);

                    if (!DeviceInfo.isMQTTInitFinish) {
                        Log.i(LOG_TAG, " === isMQTTInitFinish = true ===");
                        DeviceInfo.isMQTTInitFinish = true;
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(LOG_TAG, " LocalMClient status = " + "[failed to connect]");
                }
            });

            mqttLocalClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    if (reconnect) {
                        Log.i(LOG_TAG, " LocalMClient status = " + "[reconnected]");
                    } else {
                        Log.i(LOG_TAG, " LocalMClient status = " + "[connected]");
                        subscribeToTopic(Const.PublicTopicName);
                    }
                }

                @Override
                public void connectionLost(Throwable cause) {
                    Log.i(LOG_TAG, " LocalMClient status = " + "[connection was lost]");
                }

                //receive mqtt payload msg
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String mqttMessage = new String(message.getPayload());
                    mqttMessage = mqttMessage.replaceAll("\n", "");
                    //Log.i(LOG_TAG, "Local MQTT Incoming [" + topic + "] : " + mqttMessage);

                    if (mqttMessage.contains("desired")) {

                        JSONObject jsonObject = new JSONObject(mqttMessage);

                        String data = jsonObject.getJSONObject("state").getJSONObject("desired").getString("data");
                        Log.i(LOG_TAG, "Local MQTT data=" + data);

                        handleMqttIncomingMessage(topic, data);
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });

        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }

    //handle mobile msg
    private void handleMqttIncomingMessage(String TopicName, String mqttMessage) throws JSONException {
        //send aidl message to zwave control app
        Log.i(LOG_TAG, "MQTT receive Message , publishTopic : "+ TopicName + " Message : " + mqttMessage);

        String[] tokens = TopicName.split(Const.PublicTopicName);
        String[] devInfo = new String[2];
        String devType = "Zwave";

 /*
        if (!TopicName.equals(Const.PublicTopicName)) {
            if (tokens[1].contains("Zwave")) {
                devInfo = tokens[1].split("Zwave");
                devType = "Zwave";
            }

            else if (tokens[1].contains("BT")) {
                devInfo = tokens[1].split("BT");
                devType = "BT";
                tNodeid = Integer.parseInt(devInfo[1]);
            }

        }
*/
        JSONObject payload = new JSONObject(mqttMessage);

        if (mqttMessage.contains("function")) {
            String function = payload.getString("function");
            Log.i(LOG_TAG, "gino " + function);

            switch (function) {
                case "removeDeviceFromRoom":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    Log.i(LOG_TAG, "deviceService.removeDeviceFromRoom");
                    DeviceInfo.getMqttPayload = "removeDeviceFromRoom";
                    break;

                case "getDeviceList": //public channel
                    DeviceInfo.room = payload.getString("Room");
                    Log.i(LOG_TAG, "deviceService.getDevices tRoom= " + DeviceInfo.room);
                    DeviceInfo.getMqttPayload = "getDeviceList";
                    break;

                case "editNodeInfo":
                    Log.d(LOG_TAG, "deviceService.editNodeInfo");

                    //DeviceInfo.mqttTmp = Integer.parseInt(payload.getString("parameter"));
                    DeviceInfo.mqttString = payload.getJSONObject("parameter").getString("Room");
                    DeviceInfo.mqttString2 = payload.getJSONObject("parameter").getString("isFavorite");
                    DeviceInfo.mqttString3 = payload.getJSONObject("parameter").getString("name");
                    DeviceInfo.mqttString4 = payload.getJSONObject("parameter").getString("type");
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    //Log.d(LOG_TAG, "deviceService.editNodeInfo"+Room+isFavorite+name+type);

                    DeviceInfo.getMqttPayload = "editNodeInfo";
                    break;

                case "getRecentDeviceList": //public channel
                    Log.i(LOG_TAG, "deviceService.getRecentDeviceList");
                    DeviceInfo.mqttTmp = Integer.parseInt(payload.getString("number"));
                    DeviceInfo.getMqttPayload = "getRecentDeviceList";
                    break;

                case "addRoom": //public channel
                    Log.i(LOG_TAG, "deviceService.addRoom");
                    DeviceInfo.mqttString = payload.getString("RoomName");
                    DeviceInfo.getMqttPayload = "addRoom";
                    break;

                case "getRooms": //public channel
                    Log.i(LOG_TAG, "deviceService.getRooms");
                    DeviceInfo.getMqttPayload = "getRooms";
                    break;

                case "editRoom": //public channel
                    Log.i(LOG_TAG, "deviceService.editRoom");
                    DeviceInfo.mqttString = payload.getString("removeRoom");
                    DeviceInfo.mqttString2 = payload.getString("toRoom");
                    DeviceInfo.getMqttPayload = "editRoom";
                    break;

                case "removeRoom": //public channel
                    Log.i(LOG_TAG, "deviceService.removeRoom");
                    DeviceInfo.mqttString = payload.getString("RoomName");
                    DeviceInfo.getMqttPayload = "removeRoom";
                    break;

                case "getBasic":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    Log.i(LOG_TAG, "deviceService.getBasic" + DeviceInfo.mqttDeviceId);
                    DeviceInfo.getMqttPayload = "getBasic";
                    break;

                case "setBasic":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    DeviceInfo.mqttValue = Integer.parseInt(payload.getString("value"));
                    Log.i(LOG_TAG, "deviceService.setBasic nodeId= " + DeviceInfo.mqttDeviceId + " value = " + DeviceInfo.mqttValue);
                    DeviceInfo.getMqttPayload = "setBasic";
                    break;

                case "getSwitchMultilevel":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    Log.i(LOG_TAG, "deviceService.getBrightness" + DeviceInfo.mqttDeviceId);
                    DeviceInfo.getMqttPayload = "getSwitchMultilevel";
                    break;

                case "setSwitchMultilevel":
                    DeviceInfo.mqttValue = Integer.parseInt(payload.getString("value"));
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    DeviceInfo.mqttTmp = Integer.parseInt(payload.getString("duration"));
                    Log.i(LOG_TAG, "deviceService.setSwitchMultilevel nodeId= " + DeviceInfo.mqttDeviceId + " value = " + DeviceInfo.mqttValue + "duration " + DeviceInfo.mqttTmp);
                    DeviceInfo.getMqttPayload = "setSwitchMultilevel";
                    break;

                case "getSwitchColor":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    DeviceInfo.mqttTmp = Integer.parseInt(payload.getString("compId"));
                    Log.i(LOG_TAG, "deviceService.getLampColor" + DeviceInfo.mqttDeviceId + DeviceInfo.mqttTmp);
                    DeviceInfo.getMqttPayload = "getSwitchColor";
                    break;

                case "setSwitchColor":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    DeviceInfo.mqttTmp = Integer.parseInt(payload.getString("colorId"));
                    DeviceInfo.mqttTmp2 = Integer.parseInt(payload.getString("colorValue"));
                    Log.i(LOG_TAG, "deviceService.setSwitchColor" + DeviceInfo.mqttDeviceId + DeviceInfo.mqttTmp + DeviceInfo.mqttTmp2);
                    DeviceInfo.getMqttPayload = "setSwitchColor";
                    break;

                case "getSupportedColor":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    Log.i(LOG_TAG, "deviceService.getSupportedColor" + DeviceInfo.mqttDeviceId);
                    DeviceInfo.getMqttPayload = "getSupportedColor";
                    break;

                case "startStopColorLevelChange":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    DeviceInfo.mqttTmp = Integer.parseInt(payload.getString("dir"));
                    DeviceInfo.mqttTmp2 = Integer.parseInt(payload.getString("ignore"));
                    DeviceInfo.mqttTmp3 = Integer.parseInt(payload.getString("colorId"));
                    DeviceInfo.mqttTmp4 = Integer.parseInt(payload.getString("startLevel"));

                    Log.i(LOG_TAG, "deviceService.startStopColorLevelChange" + DeviceInfo.mqttDeviceId + DeviceInfo.mqttTmp + DeviceInfo.mqttTmp2 + DeviceInfo.mqttTmp3 + DeviceInfo.mqttTmp4);
                    DeviceInfo.getMqttPayload = "startStopColorLevelChange";
                    break;


                case "getConfiguration":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    DeviceInfo.mqttTmp = Integer.parseInt(payload.getString("paramMode"));
                    DeviceInfo.mqttTmp2 = Integer.parseInt(payload.getString("paramNumber"));
                    DeviceInfo.mqttTmp3 = Integer.parseInt(payload.getString("rangeStart"));
                    DeviceInfo.mqttTmp4 = Integer.parseInt(payload.getString("rangeEnd"));
                    Log.i(LOG_TAG, "deviceService.getConfiguration");
                    DeviceInfo.getMqttPayload = "getConfiguration";
                    break;

                case "setConfiguration":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    DeviceInfo.mqttTmp = Integer.parseInt(payload.getString("paramNumber"));
                    DeviceInfo.mqttTmp2 = Integer.parseInt(payload.getString("paramSize"));
                    DeviceInfo.mqttTmp3 = Integer.parseInt(payload.getString("useDefault"));
                    DeviceInfo.mqttTmp4 = Integer.parseInt(payload.getString("paramValue"));
                    Log.i(LOG_TAG, "deviceService.setConfiguration");
                    DeviceInfo.getMqttPayload = "setConfiguration";
                    break;

                case "getMeter":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    DeviceInfo.mqttTmp = Integer.parseInt(payload.getString("meterUnit"));
                    Log.i(LOG_TAG, "deviceService.getMeter" + DeviceInfo.mqttDeviceId + DeviceInfo.mqttTmp);
                    DeviceInfo.getMqttPayload = "getMeter";
                    break;

                case "resetMeter":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    Log.i(LOG_TAG, "deviceService.resetMeter" + DeviceInfo.mqttDeviceId);
                    DeviceInfo.getMqttPayload = "resetMeter";
                    break;

                case "getGroupInfo":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    DeviceInfo.mqttTmp = Integer.parseInt(payload.getString("groupId"));
                    DeviceInfo.mqttTmp2 = Integer.parseInt(payload.getString("endpointId"));

                    Log.i(LOG_TAG, "deviceService.getGroupInfo");

                    DeviceInfo.getMqttPayload = "getGroupInfo";
                    break;

                case "addEndpointsToGroup":
                    /*
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    DeviceInfo.mqttTmp = Integer.parseInt(payload.getString("groupId"));
                    //DeviceInfo.mqttTmp2 = Integer.parseInt(payload.getString("arr"));
                    DeviceInfo.mqttTmp3 = Integer.parseInt(payload.getString("endpointId"));

                    Log.i(LOG_TAG, "deviceService.addEndpointsToGroup");

                    String arr = payload.optString("arr");
                    JSONArray ja = new JSONArray(arr);
                    ArrayList<Integer> arrList = new ArrayList<>();
                    for(int j=0; j<ja.length(); j++){
                        JSONObject json = ja.getJSONObject(j);
                        arrList.add(Integer.valueOf(json.getString("controlNodeId").toString()));
                    }
                    DeviceInfo.getMqttPayload = "addEndpointsToGroup";
                    */
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));

                    DeviceInfo.mqttTmp = Integer.parseInt(payload.getString("groupId"));
                    DeviceInfo.mqttTmp2 = Integer.parseInt(payload.getString("endpointId"));

                    DeviceInfo.mqttString = payload.optString("arr");
                    JSONArray ja = new JSONArray(DeviceInfo.mqttString);
                    DeviceInfo.arrList = new ArrayList<>();
                    for (int j = 0; j < ja.length(); j++) {
                        JSONObject json = ja.getJSONObject(j);
                        DeviceInfo.arrList.add(Integer.valueOf(json.getString("controlNodeId").toString()));
                    }
                    DeviceInfo.getMqttPayload = "addEndpointsToGroup";
                    break;

                case "removeEndpointsFromGroup":
                    /*
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    DeviceInfo.mqttTmp = Integer.parseInt(payload.getString("groupId"));
                    //DeviceInfo.mqttTmp2 = Integer.parseInt(payload.getString("arr"));
                    DeviceInfo.mqttTmp3 = Integer.parseInt(payload.getString("endpointId"));

                    Log.i(LOG_TAG, "deviceService.removeEndpointsFromGroup");

                    arr = payload.optString("arr");
                    ja = new JSONArray(arr);
                    arrList = new ArrayList<>();
                    for(int j=0; j<ja.length(); j++){
                        JSONObject json = ja.getJSONObject(j);
                        arrList.add(Integer.valueOf(json.getString("controlNodeId").toString()));
                    }

                    DeviceInfo.getMqttPayload = "removeEndpointsFromGroup";
                    */
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    DeviceInfo.mqttTmp = Integer.parseInt(payload.getString("groupId"));
                    DeviceInfo.mqttTmp2 = Integer.parseInt(payload.getString("endpointId"));

                    DeviceInfo.mqttString = payload.optString("arr");
                    ja = new JSONArray(DeviceInfo.mqttString);
                    DeviceInfo.arrList = new ArrayList<>();
                    for (int j = 0; j < ja.length(); j++) {
                        JSONObject json = ja.getJSONObject(j);
                        DeviceInfo.arrList.add(Integer.valueOf(json.getString("controlNodeId").toString()));
                    }

                    DeviceInfo.getMqttPayload = "removeEndpointsFromGroup";
                    break;

                case "getMaxSupperedGroups":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    DeviceInfo.mqttTmp = Integer.parseInt(payload.getString("endpointId"));

                    Log.i(LOG_TAG, "deviceService.getMaxSupportedGroups");
                    DeviceInfo.getMqttPayload = "getMaxSupperedGroups";
                    break;

                case "setScheduleActive":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    DeviceInfo.mqttString = payload.getString("active");
                    Log.i(LOG_TAG, "deviceService.setScheduleActive " + DeviceInfo.mqttString);
                    DeviceInfo.getMqttPayload = "setScheduleActive";
                    break;

                case "getScheduleList":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    Log.i(LOG_TAG, "deviceService.getScheduleList");
                    DeviceInfo.getMqttPayload = "getScheduleList";
                    break;

                case "removeSchedule":
                    DeviceInfo.mqttString = payload.getString("dayOfWeek");
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    Log.i(LOG_TAG, "deviceService.removeSchedule " + DeviceInfo.mqttString);
                    DeviceInfo.getMqttPayload = "removeSchedule";
                    break;

                case "setSchedule":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    DeviceInfo.mqttString = payload.getString("dayOfWeek");
                    DeviceInfo.mqttString2 = payload.getString("active");
                    DeviceInfo.mqttString3 = payload.getString("variableValue");
                    DeviceInfo.mqttString4 = payload.getString("StartTime");
                    DeviceInfo.mqttString5 = payload.getString("EndTime");
                    Log.i(LOG_TAG, "deviceService.setSchedule " + DeviceInfo.mqttString);
                    DeviceInfo.getMqttPayload = "setSchedule";
                    break;

                case "getFavoriteList": //public channel
                    Log.i(LOG_TAG, "deviceService.getFavoriteList");
                    DeviceInfo.getMqttPayload = "getFavoriteList";
                    break;

                case "editFavoriteList": //public channel
                    Log.i(LOG_TAG, "deviceService.editFavoriteList");

                    DeviceInfo.mqttString = payload.optString("addFavorite");
                    ja = new JSONArray(DeviceInfo.mqttString);
                    DeviceInfo.addList = new ArrayList<>();
                    for (int idx = 0; idx < ja.length(); idx++) {
                        JSONObject json = ja.getJSONObject(idx);
                        DeviceInfo.addList.add(json.getString("nodeId").toString());
                    }

                    DeviceInfo.mqttString2 = payload.optString("removeFavorite");
                    ja = new JSONArray(DeviceInfo.mqttString2);
                    DeviceInfo.removeList = new ArrayList<>();
                    for (int idx = 0; idx < ja.length(); idx++) {
                        JSONObject json = ja.getJSONObject(idx);
                        DeviceInfo.removeList.add(json.getString("nodeId").toString());
                    }

                    DeviceInfo.getMqttPayload = "editFavoriteList";
                    break;

                case "setSceneAction":
                    String targetColor, cuuentColor = "";
                    String sceneName = payload.getString("sceneName");
                    String iconName = payload.getString("iconName");
                    String nodeId = payload.getJSONObject("condition").getString("nodeId");
                    String category = payload.getJSONObject("condition").getString("category");
                    String targetStatus = payload.getJSONObject("condition").getString("targetStatus");
                    String currentStatus = payload.getJSONObject("condition").getString("currentStatus");
                    if (category.equals("bulb")) {
                        targetColor = payload.getJSONObject("condition").getString("targetColor");
                        currentStatus = payload.getJSONObject("condition").getString("currentColor");
                    }
                    String timer = payload.getJSONObject("condition").getString("timer");
                    Log.i(LOG_TAG, "deviceService.setSceneAction " + sceneName + " | nodeId = " + nodeId);
                    DeviceInfo.getMqttPayload = "setSceneAction";
                    break;

                case "getSceneList": //public channel
                    Log.i(LOG_TAG, "deviceService.getScene");
                    DeviceInfo.getMqttPayload = "getSceneList";
                    break;

                case "removeSceneAction":
                    DeviceInfo.mqttString = payload.getString("sceneName");
                    DeviceInfo.mqttString2 = payload.getJSONObject("condition").getString("nodeId");
                    Log.i(LOG_TAG, "deviceService.removeSceneAction " + DeviceInfo.mqttString + " | nodeId = " + DeviceInfo.mqttString2);
                    DeviceInfo.getMqttPayload = "removeSceneAction";
                    break;

                case "removeScene":
                    DeviceInfo.mqttString = payload.getString("sceneName");
                    Log.i(LOG_TAG, "deviceService.removeScene " + DeviceInfo.mqttString);
                    DeviceInfo.getMqttPayload = "removeScene";
                    break;

                case "editScene":
                    DeviceInfo.mqttString = payload.getString("sceneName");
                    DeviceInfo.mqttString2 = payload.getString("iconName");
                    DeviceInfo.mqttString3 = payload.getString("newSceneName");
                    DeviceInfo.mqttString4 = payload.getString("newIconName");
                    Log.i(LOG_TAG, "deviceService.editScene " + DeviceInfo.mqttString + " to " + DeviceInfo.mqttString3 + "" +
                            " |iconName = " + DeviceInfo.mqttString2 + " to " + DeviceInfo.mqttString4);
                    DeviceInfo.getMqttPayload = "editScene";
                    break;

                case "executeScene":
                    DeviceInfo.mqttString = payload.getString("action");
                    DeviceInfo.mqttString2 = payload.getString("sceneName");
                    Log.i(LOG_TAG, "deviceService.removeScene " + DeviceInfo.mqttString2 + " action = " + DeviceInfo.mqttString);
                    DeviceInfo.getMqttPayload = "executeScene";
                    break;

                case "addProvisionListEntry":
                    Log.i(LOG_TAG, "deviceService.addProvisionListEntry");
                    DeviceInfo.mqttString = payload.getString("BootMode");
                    if (DeviceInfo.mqttString.contains("01")) {
                        DeviceInfo.bootMode = true;
                        Log.d(LOG_TAG, "DeviceInfo.bootMode = true");

                    } else if (DeviceInfo.mqttString.contains("00")) {
                        DeviceInfo.bootMode = false;
                        Log.d(LOG_TAG, "DeviceInfo.bootMode = false");
                    }

                    Log.i(LOG_TAG, "BootMode : " + DeviceInfo.bootMode);


                    DeviceInfo.dskNumber = payload.getString("dsk");
                    DeviceInfo.mqttString2 = payload.getString("dsk") + '\0';
                    Log.i(LOG_TAG, DeviceInfo.mqttString2);
                    //dskNumber = DeviceInfo.mqttString2.getBytes();
                    DeviceInfo.getMqttPayload = "addProvisionListEntry";
                    break;

                case "rmProvisionListEntry":
                    Log.i(LOG_TAG, "deviceService.rmProvisionListEntry");
                    DeviceInfo.dskNumber = payload.getString("dsk");
                    DeviceInfo.mqttString = payload.getString("dsk") + '\0';
                    //dskNumber = DeviceInfo.mqttString.getBytes();
                    DeviceInfo.getMqttPayload = "rmProvisionListEntry";
                    break;

                case "rmAllProvisionListEntry":
                    Log.i(LOG_TAG, "deviceService.rmAllProvisionListEntry");
                    DeviceInfo.getMqttPayload = "rmAllProvisionListEntry";
                    break;

                case "editProvisionListEntry":
                    Log.i(LOG_TAG, "deviceService.editProvisionListEntry");
                    DeviceInfo.dskNumber = payload.getString("originalDsk");
                    DeviceInfo.mqttString = payload.getString("originalDsk") + '\0';
                    Log.i(LOG_TAG, DeviceInfo.mqttString);
                    dskNumber = DeviceInfo.mqttString.getBytes();
                    DeviceInfo.getMqttPayload = "rmProvisionListEntry";
                    DeviceInfo.dskNumber = payload.getString("dsk");
                    DeviceInfo.mqttString = payload.getString("dsk") + '\0';
                    Log.i(LOG_TAG, DeviceInfo.mqttString);
                    dskNumber = DeviceInfo.mqttString.getBytes();
                    DeviceInfo.mqttTmp = Integer.parseInt(payload.getString("inclusionState"));
                    DeviceInfo.mqttTmp2 = Integer.parseInt(payload.getString("boot_mode"));

                    if (DeviceInfo.mqttTmp == 0) {
                        DeviceInfo.getMqttPayload = "addProvisionListEntry";

                    } else {
                        DeviceInfo.getMqttPayload = "addProvisionListEntry";

                    }

                    break;

                case "getAllProvisionListEntry":
                    Log.i(LOG_TAG, "deviceService.getAllProvisionListEntry");
                    DeviceInfo.getMqttPayload = "getAllProvisionListEntry";
                    break;

                case "getProvisionListEntry":
                    Log.i(LOG_TAG, "deviceService.getProvisionListEntry");
                    DeviceInfo.dskNumber = payload.getString("dsk");
                    DeviceInfo.mqttString = payload.getString("dsk") + '\0';
                    Log.i(LOG_TAG, DeviceInfo.mqttString);
                    //dskNumber = DeviceInfo.mqttString.getBytes();
                    DeviceInfo.getMqttPayload = "getProvisionListEntry";
                    break;

                case "getRssiState":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    Log.i(LOG_TAG, "deviceService.getRssiState" + DeviceInfo.mqttDeviceId);
                    DeviceInfo.getMqttPayload = "getRssiState";
                    break;

                case "getBattery":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    Log.i(LOG_TAG, "deviceService.getBattery" + DeviceInfo.mqttDeviceId);
                    DeviceInfo.getMqttPayload = "getBattery";
                    break;

                case "getSensorMultilevel":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    Log.i(LOG_TAG, "deviceService.getSensorMultilevel" + DeviceInfo.mqttDeviceId);
                    DeviceInfo.getMqttPayload = "getSensorMultilevel";
                    break;

                case "getSupportSwitchType":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    Log.i(LOG_TAG, "deviceService.getSupportSwitchType" + DeviceInfo.mqttDeviceId);
                    DeviceInfo.getMqttPayload = "getSupportSwitchType";
                    break;

                case "startStopSwitchLevelChange":
                    Log.i(LOG_TAG, "deviceService.startStopSwitchLevelChange");
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    DeviceInfo.mqttTmp = Integer.parseInt(payload.getString("startLvlVal"));
                    DeviceInfo.mqttTmp2 = Integer.parseInt(payload.getString("duration"));
                    DeviceInfo.mqttTmp3 = Integer.parseInt(payload.getString("pmyChangeDir"));
                    DeviceInfo.mqttTmp4 = Integer.parseInt(payload.getString("secChangeDir"));
                    DeviceInfo.mqttTmp5 = Integer.parseInt(payload.getString("secStep"));

                    Log.i(LOG_TAG, "deviceService.startStopSwitchLevelChange" + DeviceInfo.mqttDeviceId + DeviceInfo.mqttTmp + DeviceInfo.mqttTmp2 + DeviceInfo.mqttTmp3 + DeviceInfo.mqttTmp4 + DeviceInfo.mqttTmp5);
                    DeviceInfo.getMqttPayload = "startStopSwitchLevelChange";
                    break;

                case "setSwitchAllOffBroadcast":
                    Log.i(LOG_TAG, "deviceService.setSwitchAllOffBroadcast");
                    DeviceInfo.getMqttPayload = "setSwitchAllOffBroadcast";
                    break;

                case "getPowerLevel":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    Log.i(LOG_TAG, "deviceService.getPowerLevel" + DeviceInfo.mqttDeviceId);
                    DeviceInfo.getMqttPayload = "getPowerLevel";
                    break;

                case "switchAllOn":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    Log.i(LOG_TAG, "deviceService.switchAllOn" + DeviceInfo.mqttDeviceId);
                    DeviceInfo.getMqttPayload = "switchAllOn";
                    break;

                case "switchAllOff":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    Log.i(LOG_TAG, "deviceService.switchAllOff" + DeviceInfo.mqttDeviceId);
                    DeviceInfo.getMqttPayload = "switchAllOff";
                    break;

                case "setSwitchAll":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    DeviceInfo.mqttTmp = Integer.parseInt(payload.getString("value"));

                    Log.i(LOG_TAG, "deviceService.setSwitchAll" + DeviceInfo.mqttDeviceId);
                    DeviceInfo.getMqttPayload = "setSwitchAll";
                    break;

                case "getSwitchAll":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));

                    Log.i(LOG_TAG, "deviceService.GetSwitchAll" + DeviceInfo.mqttDeviceId);
                    DeviceInfo.getMqttPayload = "getSwitchAll";
                    break;

                case "getSensorBinary":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    DeviceInfo.mqttTmp = Integer.parseInt(payload.getString("sensorType"));
                    Log.i(LOG_TAG, "deviceService.getSensorBinary");
                    DeviceInfo.getMqttPayload = "getSensorBinary";
                    break;

                case "getSensorBinarySupportedSensor":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    Log.i(LOG_TAG, "deviceService.getSensorBinarySupportedSensor" + DeviceInfo.mqttDeviceId);
                    DeviceInfo.getMqttPayload = "getSensorBinarySupportedSensor";
                    break;

                case "getMeterSupported":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    Log.i(LOG_TAG, "deviceService.getMeterSupported" + DeviceInfo.mqttDeviceId);
                    DeviceInfo.getMqttPayload = "getMeterSupported";
                    break;

                case "getSpecificGroup":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    DeviceInfo.mqttTmp = Integer.parseInt(payload.getString("endpointId"));

                    Log.i(LOG_TAG, "deviceService.getSpecificGroup" + DeviceInfo.mqttDeviceId + DeviceInfo.mqttTmp);
                    DeviceInfo.getMqttPayload = "getSpecificGroup";
                    break;

                case "getNotification":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    DeviceInfo.mqttTmp = Integer.parseInt(payload.getString("alarmType"));
                    DeviceInfo.mqttTmp2 = Integer.parseInt(payload.getString("notifType"));
                    DeviceInfo.mqttTmp3 = Integer.parseInt(payload.getString("status"));

                    Log.i(LOG_TAG, "deviceService.getNotification" + DeviceInfo.mqttDeviceId + DeviceInfo.mqttTmp + DeviceInfo.mqttTmp2 + DeviceInfo.mqttTmp3);
                    DeviceInfo.getMqttPayload = "getNotification";
                    break;


                case "setNotification":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    DeviceInfo.mqttTmp = Integer.parseInt(payload.getString("notificationType"));
                    DeviceInfo.mqttTmp2 = Integer.parseInt(payload.getString("status"));

                    Log.i(LOG_TAG, "deviceService.setNotification" + DeviceInfo.mqttDeviceId + DeviceInfo.mqttTmp + DeviceInfo.mqttTmp2);
                    DeviceInfo.getMqttPayload = "setNotification";
                    break;

                case "getSupportedNotification":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    Log.i(LOG_TAG, "deviceService.getSupportedNotification" + DeviceInfo.mqttDeviceId);
                    DeviceInfo.getMqttPayload = "getSupportedNotification";
                    break;

                case "getSupportedEventNotification":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    DeviceInfo.mqttTmp = Integer.parseInt(payload.getString("notifType"));
                    Log.i(LOG_TAG, "deviceService.getSupportedEventNotification" + DeviceInfo.mqttDeviceId + DeviceInfo.mqttTmp);
                    DeviceInfo.getMqttPayload = "getSupportedEventNotification";
                    break;

                case "getSpecifyDeviceInfo":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    Log.i(LOG_TAG, "deviceService.getSpecifyDeviceInfo");
                    Log.i(LOG_TAG, "nodeId: " + DeviceInfo.mqttDeviceId);
                    DeviceInfo.getMqttPayload = "getSpecifyDeviceInfo";
                    break;

                case "removeFailDevice":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    Log.i(LOG_TAG, "deviceService.removeFailDevice");
                    Log.i(LOG_TAG, "DeviceInfo.mqttDeviceId: " + DeviceInfo.mqttDeviceId);
                    DeviceInfo.getMqttPayload = "removeFailDevice";
                    break;

                case "checkNodeIsFailed":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    Log.i(LOG_TAG, "deviceService.checkNodeIsFailed");
                    Log.i(LOG_TAG, "DeviceInfo.mqttDeviceId: " + DeviceInfo.mqttDeviceId);
                    DeviceInfo.getMqttPayload = "checkNodeIsFailed";
                    break;

                case "setDefault":
                    Log.i(LOG_TAG, "deviceService.setDefault");
                    DeviceInfo.getMqttPayload = "setDefault";
                    break;

                case "replaceFailDevice":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    Log.i(LOG_TAG, "deviceService.replaceFailDevice");
                    Log.i(LOG_TAG, "DeviceInfo.mqttDeviceId: " + DeviceInfo.mqttDeviceId);
                    DeviceInfo.getMqttPayload = "replaceFailDevice";
                    break;

                case "startLearnMode":
                    Log.i(LOG_TAG, "deviceService.startLearnMode");
                    DeviceInfo.getMqttPayload = "startLearnMode";
                    break;

                case "getSceneActuatorConf":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    DeviceInfo.mqttTmp = Integer.parseInt(payload.getString("sceneId"));
                    Log.i(LOG_TAG, "deviceService.getSceneActuatorConf");

                    DeviceInfo.getMqttPayload = "getSceneActuatorConf";
                    break;

                case "getSupportedCentralScene":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    DeviceInfo.mqttTmp = Integer.parseInt(payload.getString("endpointId"));
                    Log.i(LOG_TAG, "deviceService.getSupportedCentralScene");

                    DeviceInfo.getMqttPayload = "getSupportedCentralScene";
                    break;

                case "getDoorLockOperation":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    Log.i(LOG_TAG, "deviceService.getDoorLockOperation");

                    DeviceInfo.getMqttPayload = "getDoorLockOperation";
                    break;

                case "setDoorLockOperation":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    DeviceInfo.mqttTmp = Integer.parseInt(payload.getString("mode"));
                    Log.i(LOG_TAG, "deviceService.setDoorLockOperation");

                    DeviceInfo.getMqttPayload = "setDoorLockOperation";
                    break;

                case "getDoorLockConfig":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    Log.i(LOG_TAG, "deviceService.getDoorLockConfiguration");

                    DeviceInfo.getMqttPayload = "getDoorLockConfig";
                    break;

                case "setDoorLockConfig":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    DeviceInfo.mqttTmp = Integer.parseInt(payload.getString("type"));
                    DeviceInfo.mqttTmp2 = Integer.parseInt(payload.getString("outSta"));
                    DeviceInfo.mqttTmp3 = Integer.parseInt(payload.getString("insta"));
                    DeviceInfo.mqttTmp4 = Integer.parseInt(payload.getString("tmoutMin"));
                    DeviceInfo.mqttTmp5 = Integer.parseInt(payload.getString("tmoutSec"));
                    Log.i(LOG_TAG, "deviceService.setDoorLockConfig");

                    DeviceInfo.getMqttPayload = "setDoorLockConfig";
                    break;

                case "setBinarySwitchState":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    DeviceInfo.mqttTmp = Integer.parseInt(payload.getString("state"));
                    DeviceInfo.mqttTmp2 = Integer.parseInt(payload.getString("duration"));
                    Log.i(LOG_TAG, "deviceService.setBinarySwitchState");

                    DeviceInfo.getMqttPayload = "setBinarySwitchState";
                    break;

                case "getBinarySwitchState":
                    DeviceInfo.mqttDeviceId = Integer.parseInt(payload.getString("nodeId"));
                    Log.i(LOG_TAG, "deviceService.getBinarySwitchState");

                    DeviceInfo.getMqttPayload = "getBinarySwitchState";
                    break;


                default:
                    Log.e(LOG_TAG, "no support this function, please make sure this mqtt message " + function);
                    break;

            }
        } else {
            Log.e(LOG_TAG, "wrong mqtt format, please make sure this mqtt message" + mqttMessage);
        }

    }
/*
    // Synchronize the subscribe topic of local mqtt server with remote mqtt server
    private void syncSubscribeTopic() {

        for (int idx = 0; idx < DeviceInfo.localSubTopiclist.size(); idx++) {
            if (!DeviceInfo.remoteSubTopiclist.contains(DeviceInfo.localSubTopiclist.get(idx))) {
                subscribeToTopic(DeviceInfo.localSubTopiclist.get(idx));
            }
        }

        for (int idx = 0; idx < DeviceInfo.remoteSubTopiclist.size(); idx++) {
            if (!DeviceInfo.localSubTopiclist.contains(DeviceInfo.remoteSubTopiclist.get(idx))) {
                unsubscribeTopic(DeviceInfo.localSubTopiclist.get(idx));
            }
        }
    }
*/
    // subscribe mqtt topic
    private void subscribeToTopic(final String TopicName) {
        //Log.i(LOG_TAG,"mqttLocalClient.isConnected() = "+mqttLocalClient.isConnected());

        if (!DeviceInfo.localSubTopiclist.contains(TopicName)) {
            if (mqttLocalClient.isConnected()) {
                try {
                    mqttLocalClient.subscribe(TopicName, 0, null, new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Log.i(LOG_TAG, "localMQTT Subscribed : \"" + TopicName + "\" topic");
                            DeviceInfo.localSubTopiclist.add(TopicName);

                            for (int idx = 0; idx < DeviceInfo.localSubTopiclist.size(); idx++) {
                                Log.i(LOG_TAG, "localSubTopiclist[" + idx + "] = " + DeviceInfo.localSubTopiclist.get(idx));
                            }
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            Log.i(LOG_TAG, "localMQTT Failed to subscribe : " + TopicName);
                        }
                    });
                } catch (MqttException ex) {
                    Log.e(LOG_TAG, "localMQTT Exception whilst subscribing");
                    ex.printStackTrace();
                }
            } else {
                Log.i(LOG_TAG, "local mqtt server is disconnect...");
            }
        }
        /*
        if (Const.remoteMqttFlag) {
            Log.i(LOG_TAG, "mqttRemoteClient.isConnected() = " + mqttRemoteClient.isConnected());
            if (!DeviceInfo.remoteSubTopiclist.contains(TopicName)) {
                if (mqttRemoteClient.isConnected()) {
                    try {
                        mqttRemoteClient.subscribe(TopicName, 0, null, new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                Log.i(LOG_TAG, "remoteMQTT Subscribed : \"" + TopicName + "\" topic");
                                DeviceInfo.remoteSubTopiclist.add(TopicName);

                                for (int idx = 0; idx < DeviceInfo.remoteSubTopiclist.size(); idx++) {
                                    Log.i(LOG_TAG, "remoteSubTopiclist[" + idx + "] = " + DeviceInfo.remoteSubTopiclist.get(idx));
                                }
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                Log.i(LOG_TAG, "remoteMQTT Failed to subscribe : " + TopicName);
                            }
                        });
                    } catch (MqttException ex) {
                        Log.e(LOG_TAG, "remoteMQTT Exception whilst subscribing");
                        ex.printStackTrace();
                    }
                } else {
                    Log.i(LOG_TAG, "remote mqtt server is disconnect...");
                 }
            }
        }
        */
    }

    // unsubscribe mqtt topic
    private void unsubscribeTopic(final String TopicName) {
        int idx;
        Log.i(LOG_TAG, "unsubscribeTopic : " + TopicName);
        try {
            if (DeviceInfo.localSubTopiclist.contains(TopicName)) {
                if (mqttLocalClient.isConnected()) {
                    mqttLocalClient.unsubscribe(TopicName);

                    idx = DeviceInfo.localSubTopiclist.indexOf(TopicName);
                    if (idx >= 0) {
                        DeviceInfo.localSubTopiclist.remove(idx);
                    }
                }
            }
            /*
            if (Const.remoteMqttFlag) {
                if (DeviceInfo.remoteSubTopiclist.contains(TopicName)) {
                    if (mqttRemoteClient.isConnected()) {
                        mqttRemoteClient.unsubscribe(TopicName);
                        idx = DeviceInfo.remoteSubTopiclist.indexOf(TopicName);
                        if (idx >= 0) {
                            DeviceInfo.remoteSubTopiclist.remove(idx);
                        }
                    }
                }
            }
            */
        } catch (MqttException e) {
            e.printStackTrace();
        }

        for (idx = 0; idx < DeviceInfo.localSubTopiclist.size(); idx++) {
            Log.i(LOG_TAG, "localSubTopiclist[" + idx + "] = " + DeviceInfo.localSubTopiclist.get(idx));
        }
        /*
        if (Const.remoteMqttFlag) {
            for (idx = 0; idx < DeviceInfo.remoteSubTopiclist.size(); idx++) {
                Log.i(LOG_TAG, "remoteSubTopiclist[" + idx + "] = " + DeviceInfo.remoteSubTopiclist.get(idx));
            }
        }
        */
    }

    // publish message to mqtt server
    public void publishMessage(String publishTopic, String publishMessage) {

        try {

            //Log.i(LOG_TAG, "Public LOACAL MESSAGE" + ":" + publishMessage);

            JSONObject payload = new JSONObject(publishMessage);
            JSONObject json = new JSONObject();
            json.put("reported", payload);

            //Log.i(LOG_TAG, "Public LOACAL MESSAGE" + ":" + json.toString());
            MqttMessage message = new MqttMessage();
            message.setPayload(json.toString().getBytes());
            Log.i(LOG_TAG, "MQTT send Message , publishTopic : "+ publishTopic + " Message : " + publishMessage);


            if (mqttLocalClient.isConnected()) {
                mqttLocalClient.publish(publishTopic, message);
            } else {
                Log.e(LOG_TAG, "[LocalMqttClient] fail to connect local mqtt server");
            }
            /*
            if (Const.remoteMqttFlag) {
                if (mqttRemoteClient.isConnected()) {
                    mqttRemoteClient.publish(publishTopic, message);
                } else {
                    Log.e(LOG_TAG, "[LocalMqttClient] fail to connect local mqtt server");
                }
            }
            */
        } catch (MqttException e) {
            System.err.println("MQTT Error Publishing: " + e.getMessage());
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    //callback
    //receive message to do something
    public void receive() {
        boolean circle = false;
        while (!circle) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            JSONObject message = new JSONObject();
            //Log.i(LOG_TAG, "class name = [" + DeviceInfo.className + "]| result = " + DeviceInfo.result);

            if (DeviceInfo.className.equals("addDevice") || DeviceInfo.className.equals("removeDevice")) {
                addRemoveDevice(message);
                DeviceInfo.className = "";


            } else if (DeviceInfo.className.equals("All Node Info Report")) {
                getDeviceInfo(message);
                DeviceInfo.getMqttPayload = "getDeviceList";
                DeviceInfo.className = "";

            } else if (DeviceInfo.result.contains("Remove Failed Node")) {

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String status = jsonObject.optString("Status");

                    message.put("MessageType", "Remove Failed Node");
                    message.put("Status", status);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Replace Failed Node")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String status = jsonObject.optString("Status");

                    message.put("MessageType", "Replace Failed Node");
                    message.put("Status", status);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Node Is Failed Check Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String nodeId = jsonObject.optString("Node id");
                    String status = jsonObject.optString("Status");

                    message.put("MessageType", "Node Is Failed Check Report");
                    message.put("Node id", nodeId);
                    message.put("Status", status);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Replace Failed Node")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String status = jsonObject.optString("Status");

                    message.put("MessageType", "Replace Failed Node");
                    message.put("Status", status);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Controller Reset Status")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String status = jsonObject.optString("Status");

                    message.put("MessageType", "Controller Reset Status");
                    message.put("Status", status);
                    if(status.equals("Failed")) {
                        mTCPServer.sendMessage(Const.TCPClientPort, "\n" + "\t" + "\"MessageType\":" + "\t" + "\"Controller Reset Status\"," + "\n" + "\t" + "\"Status\":" +
                                "\t" + DeviceInfo.callResult); //TCP format
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.getMqttPayload = "getDeviceInfo";
                setDefaultFlag = true;
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Controller Attribute")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String homeId = jsonObject.optString("Home Id");
                    String nodeId = jsonObject.optString("Node Id");
                    String role = jsonObject.optString("Network Role");
                    String vendorId = jsonObject.optString("Vendor Id");
                    String proType = jsonObject.optString("Vendor Product Type");
                    String libType = jsonObject.optString("Z-wave Library Type");
                    String proId = jsonObject.optString("Product Id");
                    String protocolVersion = jsonObject.optString("Z-wave Protocol Version");
                    String appVersion = jsonObject.optString("Application Version");

                    message.put("MessageType", "Controller Attribute");
                    message.put("Home Id", homeId);
                    message.put("Node Id", nodeId);
                    message.put("Network Role", role);
                    message.put("Vendor Id", vendorId);
                    message.put("Vendor Product Type", proType);
                    message.put("Z-wave Library Type", libType);
                    message.put("Product Id", proId);
                    message.put("Z-wave Protocol Version", protocolVersion);
                    message.put("Application Version", appVersion);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("All Node List Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String list = jsonObject.optString("Added Node List");

                    message.put("MessageType", "All Node List Report");
                    message.put("Added Node List", list);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Specify Node Info")) {

                specifyNodeInfo(DeviceInfo.result);
                /*
                ArrayList<String> tmpLine = Utils.searchString(result, "Node id");

                String tmp = "";
                for (int idx = 0; idx < tmpLine.size(); idx++) {

                    tmp = tmp + tmpLine.get(idx) + ",";

                }

                String[] supportColor = tmp.split("\t");


                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    //String Info = jsonObject.optString("Detialed Node Info");

                    message.put("MessageType", "Specify Node Info");
                    message.put("Detialed Node Info", supportColor[0]);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";
                */

            } else if (DeviceInfo.result.contains("Controller Init Status")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String status = jsonObject.optString("Status");

                    message.put("MessageType", "Controller Init Status");
                    message.put("Status", status);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Node Battery Value")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String nodeId = jsonObject.optString("Node id");
                    String id = jsonObject.optString("EndPoint Id");
                    String value = jsonObject.optString("Battery Value");

                    message.put("MessageType", "Node Battery Value");
                    message.put("Node id", nodeId);
                    message.put("EndPoint Id", id);
                    message.put("Battery Value", value);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Switch Multi-lvl Report Information")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String nodeId = jsonObject.optString("Node id");
                    String val = jsonObject.optString("Cur Val");

                    message.put("MessageType", "Switch Multi-lvl Report Information");
                    message.put("Node id", nodeId);
                    message.put("Cur Val", val);
                    message.put("Tgt Val", "Unsupported");
                    message.put("Durration", "Unsupported");


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Power Level Get Information")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String nodeId = jsonObject.optString("Node id");
                    String level = jsonObject.optString("Power Level");

                    message.put("MessageType", "Power Level Get Information");
                    message.put("Node id", nodeId);
                    message.put("Power Level", level);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Switch All Get Information")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String nodeId = jsonObject.optString("Node id");
                    String mode = jsonObject.optString("mode");

                    message.put("MessageType", "Switch All Get Information");
                    message.put("Node id", nodeId);
                    message.put("mode", mode);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Binary Sensor Information")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String nodeId = jsonObject.optString("Node id");
                    String type = jsonObject.optString("Event Type");
                    String state = jsonObject.optString("state");

                    message.put("MessageType", "Binary Sensor Information");
                    message.put("Node id", nodeId);
                    message.put("Event Type", type);
                    message.put("state", state);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Binary Sensor Support Get Information")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String nodeId = jsonObject.optString("Node id");
                    String type = jsonObject.optString("Supported type");

                    message.put("MessageType", "Binary Sensor Support Get Information");
                    message.put("Node id", nodeId);
                    message.put("Supported type", type);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Meter Report Information")) {

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String nodeId = jsonObject.optString("Node id");
                    String mType = jsonObject.optString("Meter type");
                    String rType = jsonObject.optString("Rate type");
                    String mRead = jsonObject.optString("Meter reading");
                    String mUnit = jsonObject.optString("Meter unit");

                    message.put("MessageType", "Meter Report Information");
                    message.put("Node id", nodeId);
                    message.put("Meter type", mType);
                    message.put("Rate type", rType);
                    message.put("Meter reading", mRead);
                    message.put("Meter unit", mUnit);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Meter Cap Information")) {
                ArrayList<String> tmpLine = Utils.searchString(DeviceInfo.result, "unit");

                String tmp = "";
                for (int idx = 0; idx < tmpLine.size(); idx++) {
                    tmp = tmp + tmpLine.get(idx) + ",";

                }
                String[] supportUnit = tmp.split("\t");


                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String nodeId = jsonObject.optString("Node id");
                    String mType = jsonObject.optString("Meter type");
                    String reset = jsonObject.optString("Can be reset?");
                    //String unit = jsonObject.optString("Supported unit");

                    message.put("MessageType", "Meter Cap Information");
                    message.put("Node id", nodeId);
                    message.put("Meter type", mType);
                    message.put("Can be reset?", reset);
                    message.put("Supported unit", supportUnit[0]);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

                //getMeterInfo(result);

            } else if (DeviceInfo.result.contains("Binary Switch Get Information")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String nodeId = jsonObject.optString("Node id");
                    String val = jsonObject.optString("Cur Val");

                    message.put("MessageType", "Binary Switch Get Information");
                    message.put("Node id", nodeId);
                    message.put("Cur Val", val);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Wake Up Cap Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String wakeUpSetting = jsonObject.optString("Wake up settings");

                    message.put("MessageType", "Wake Up Cap Report");
                    message.put("Wake up settings", wakeUpSetting);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Door Lock Operation Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String nodeId = jsonObject.optString("Node id");
                    String doorLockMode = jsonObject.optString("Door Lock op mode");
                    String outsideMode = jsonObject.optString("Outside Door mode");
                    String insideMode = jsonObject.optString("Inside Door mode");
                    String doorCondition = jsonObject.optString("Door Condition");

                    message.put("MessageType", "Door Lock Operation Report");
                    message.put("Node Id", nodeId);
                    message.put("Door Lock op mode", doorLockMode);
                    message.put("Outside Door mode", outsideMode);
                    message.put("Inside Door mode", insideMode);
                    message.put("Door Condition", doorCondition);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Door Lock Configuration Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String nodeId = jsonObject.optString("Node id");
                    String type = jsonObject.optString("Door Lock op type");
                    String state = jsonObject.optString("Outside Door state");
                    String insideState = jsonObject.optString("Inside Door state");

                    message.put("MessageType", "Door Lock Configuration Report");
                    message.put("Node Id", nodeId);
                    message.put("Door Lock op type", type);
                    message.put("Outside Door state", state);
                    message.put("Inside Door state", insideState);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Switch Color Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String nodeId = jsonObject.optString("Node id");
                    String component = jsonObject.optString("component id");
                    String value = jsonObject.optString("value");

                    message.put("MessageType", "Switch Color Report");
                    message.put("Node Id", nodeId);
                    message.put("component id", component);
                    message.put("value", value);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Supported Color Report")) {
                ArrayList<String> tmpLine = Utils.searchString(DeviceInfo.result, "color");

                String tmp = "";
                for (int idx = 0; idx < tmpLine.size(); idx++) {

                    tmp = tmp + tmpLine.get(idx) + ",";

                }

                String[] supportColor = tmp.split("\t");

                JSONObject jsonObject = null;

                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String nodeId = jsonObject.optString("Node id");
                    //String supportColor = jsonObject.optString("Supported Color");

                    message.put("MessageType", "Supported Color Report");
                    message.put("Node Id", nodeId);
                    message.put("Supported Color", supportColor[0]);


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Group Info Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String nodeId = jsonObject.optString("Node id");
                    String groupId = jsonObject.optString("Group id");
                    String maxSupport = jsonObject.optString("Max Supported endpoints");
                    String groupMember = jsonObject.optString("Group members");

                    message.put("MessageType", "Group Info Report");
                    message.put("Node Id", nodeId);
                    message.put("Group id", groupId);
                    message.put("Max Supported endpoints", maxSupport);
                    message.put("Group members", groupMember);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Configuration Get Information")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String nodeId = jsonObject.optString("Node id");
                    String number = jsonObject.optString("Parameter number");
                    String value = jsonObject.optString("Parameter value");
                    String interFace = jsonObject.optString("Interface");
                    String devType = jsonObject.optString("devType");

                    message.put("MessageType", "Configuration Get Information");
                    message.put("Node Id", nodeId);
                    message.put("Parameter number", number);
                    message.put("Parameter value", value);
                    message.put("Interface", interFace);
                    message.put("devType", devType);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            }  else if (DeviceInfo.className.contains("getDeviceList")) {
                getDeviceList(DeviceInfo.result);
                /*
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String deviceList = jsonObject.optString("deviceList");

                    message.put("Interface", "getDeviceList");
                    message.put("deviceList", deviceList);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";
                */
            } else if (DeviceInfo.result.contains("Supported Groupings Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String nodeId = jsonObject.optString("Node id");
                    String maxNumber = jsonObject.optString("Max number of groupings");

                    message.put("MessageType", "Supported Groupings Report");
                    message.put("Node Id", nodeId);
                    message.put("Max number of groupings", maxNumber);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Active Groups Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String nodeId = jsonObject.optString("Node id");
                    String currentActive = jsonObject.optString("Current active group");

                    message.put("MessageType", "Active Groups Report");
                    message.put("Node Id", nodeId);
                    message.put("Current active group", currentActive);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Notification Get Information")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String nodeId = jsonObject.optString("Node id");
                    String status = jsonObject.optString("Notification-status");
                    String type = jsonObject.optString("Notification-type");
                    String event = jsonObject.optString("Notification-event");

                    message.put("MessageType", "Notification Get Information");
                    message.put("Node Id", nodeId);
                    message.put("Notification-status", status);
                    message.put("Notification-type", type);
                    message.put("Notification-event", event);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Notification Supported Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String nodeId = jsonObject.optString("Node id");
                    String type = jsonObject.optString("Have alarm type");
                    String support = jsonObject.optString("supported notification");

                    message.put("MessageType", "Notification Supported Report");
                    message.put("Node Id", nodeId);
                    message.put("Have alarm type", type);
                    message.put("supported notification", support);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Supported Notification Event Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String nodeId = jsonObject.optString("Node id");
                    String type = jsonObject.optString("Notification Type");
                    String event = jsonObject.optString("event");

                    message.put("MessageType", "Supported Notification Event Report");
                    message.put("Node Id", nodeId);
                    message.put("Notification Type", type);
                    message.put("event", event);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Central Scene Supported Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String nodeId = jsonObject.optString("Node id");
                    String Scenes = jsonObject.optString("Supported Scenes");
                    String attributes = jsonObject.optString("Is Same Key Attributes");
                    String supportKey = jsonObject.optString("Supported Key Attr");

                    message.put("MessageType", "Central Scene Supported Report");
                    message.put("Node Id", nodeId);
                    message.put("Supported Scenes", Scenes);
                    message.put("Is Same Key Attributes", attributes);
                    message.put("Supported Key Attr", supportKey);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Central Scene Notification")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String nodeId = jsonObject.optString("Node id");
                    String attrKey = jsonObject.optString("key attr");
                    String sceneNumber = jsonObject.optString("Scene number");

                    message.put("MessageType", "Central Scene Notification");
                    message.put("Node Id", nodeId);
                    message.put("key attr", attrKey);
                    message.put("Scene number", sceneNumber);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Firmware Info Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String nodeId = jsonObject.optString("Node id");
                    String vendorId = jsonObject.optString("Vendor id");
                    String firmwareId = jsonObject.optString("Firmware id");
                    String Checksum = jsonObject.optString("Checksum");
                    String maxSize = jsonObject.optString("Max fragment size");
                    String sizeFixed = jsonObject.optString("Size fixed");
                    String Upgradable = jsonObject.optString("Upgradable");
                    String otherFirmwareTarger = jsonObject.optString("Other Firmware targer");
                    String otherFirmwareId = jsonObject.optString("Other firmware id");

                    message.put("MessageType", "Firmware Info Report");
                    message.put("Node Id", nodeId);
                    message.put("Vendor id", vendorId);
                    message.put("Firmware id", firmwareId);
                    message.put("Checksum", Checksum);
                    message.put("Max fragment size", maxSize);
                    message.put("Size fixed", sizeFixed);
                    message.put("Upgradable", Upgradable);
                    message.put("Other Firmware targer", otherFirmwareTarger);
                    message.put("Other firmware id", otherFirmwareId);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Firmware Update Status Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String nodeId = jsonObject.optString("Node id");
                    String status = jsonObject.optString("Update status");

                    message.put("MessageType", "Firmware Update Status Report");
                    message.put("Node Id", nodeId);
                    message.put("Update status", status);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Firmware Update Completion Status Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String nodeId = jsonObject.optString("Node id");
                    String status = jsonObject.optString("Update status");

                    message.put("MessageType", "Firmware Update Completion Status Report");
                    message.put("Node Id", nodeId);
                    message.put("Update status", status);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Firmware Update restart Status Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String nodeId = jsonObject.optString("Node id");
                    String status = jsonObject.optString("Restart status");

                    message.put("MessageType", "Firmware Update restart Status Report");
                    message.put("Node Id", nodeId);
                    message.put("Restart status", status);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Sensor Info Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String nodeId = jsonObject.optString("Node id");
                    String precision = jsonObject.optString("type");
                    String type = jsonObject.optString("precision");
                    String unit = jsonObject.optString("unit");
                    String value = jsonObject.optString("value");

                    message.put("MessageType", "Sensor Info Report");
                    message.put("Node Id", nodeId);
                    message.put("type", type);
                    message.put("precision", precision);
                    message.put("unit", unit);
                    message.put("value", value);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Command Queue State Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String nodeId = jsonObject.optString("Node id");
                    String state = jsonObject.optString("command state");


                    message.put("MessageType", "Command Queue State Report");
                    message.put("Node Id", nodeId);
                    message.put("command state", state);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Command Queue Info Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String nodeId = jsonObject.optString("Node id");
                    String queue = jsonObject.optString("command queue");

                    message.put("MessageType", "Command Queue Info Report");
                    message.put("Node Id", nodeId);
                    message.put("command queue", queue);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Network Health Check")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String status = jsonObject.optString("Status");

                    message.put("MessageType", "Network Health Check");
                    message.put("Status", status);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Network IMA Info Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String nodeid = jsonObject.optString("Direct nodeid");
                    String health = jsonObject.optString("Network Health");
                    String number = jsonObject.optString("RSSI hops number");
                    String value = jsonObject.optString("RSSI hops value");
                    String channel = jsonObject.optString("Transmit channel");

                    message.put("MessageType", "Network IMA Info Report");
                    message.put("Direct nodeid", nodeid);
                    message.put("Network Health", health);
                    message.put("RSSI hops number", number);
                    message.put("RSSI hops value", value);
                    message.put("Transmit channel", channel);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Network RSSI Info Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String channel1 = jsonObject.optString("Value of channel 1");
                    String channel2 = jsonObject.optString("Value of channel 2");

                    message.put("MessageType", "Network RSSI Info Report");
                    message.put("Value of channel 1", channel1);
                    message.put("Value of channel 2", channel2);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.result.contains("Provision List Report")) {
                if (DeviceInfo.result.contains("Error")) {
                    try {
                        message.put("MessageType", "All Provision List Report");
                        message.put("Error", "No list entry");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    publishMessage(Const.PublicTopicName, message.toString());
                    DeviceInfo.className = "";
                    DeviceInfo.result = "";
                    DeviceInfo.resultToMqttBroker = "";

                } else {
                    allProvisionListReport(DeviceInfo.result);
                    DeviceInfo.className = "";
                    DeviceInfo.result = "";
                    DeviceInfo.resultToMqttBroker = "";
                }

            } else if (DeviceInfo.result.contains("All Provision List Report")) {
                if (DeviceInfo.result.contains("Error")) {
                    try {
                        message.put("MessageType", "All Provision List Report");
                        message.put("Error", "No list entry");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    publishMessage(Const.PublicTopicName, message.toString());
                    DeviceInfo.className = "";
                    DeviceInfo.result = "";
                    DeviceInfo.resultToMqttBroker = "";

                } else {
                    allProvisionListReport(DeviceInfo.result);
                    DeviceInfo.className = "";
                    DeviceInfo.result = "";
                    DeviceInfo.resultToMqttBroker = "";
                }

            } else if (DeviceInfo.result.contains("Controller DSK Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(DeviceInfo.result);
                    String dsk = jsonObject.optString("DSK");

                    message.put("MessageType", "Controller DSK Report");
                    message.put("DSK", dsk);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.reqString.equals("Grant")) {
                Log.d(LOG_TAG,DeviceInfo.grantKeyNumber);
                mTCPServer.sendMessage(Const.TCPClientPort, "GrantKeys:" + DeviceInfo.grantKeyNumber); //TCP format
                DeviceInfo.reqString = "";
            } else if (DeviceInfo.reqString.equals("PIN")) {
                mTCPServer.sendMessage(Const.TCPClientPort, "dsk:"); //TCP format
                DeviceInfo.reqString = "";
            } else if (DeviceInfo.reqString.equals("Au")) {
                mTCPServer.sendMessage(Const.TCPClientPort, "CSA:CSA"); //TCP format
                DeviceInfo.reqString = "";
            } else if (DeviceInfo.className.equals("DSK")) {
                mTCPServer.sendMessage(Const.TCPClientPort, "CSA:CSA"); //TCP format
                DeviceInfo.className = "";
            } else if (DeviceInfo.resultToMqttBroker.equals("editNodeInfoTrue")) {
                try {
                    message.put("Interface", "editNodeInfo");
                    message.put("NodeId", DeviceInfo.mqttDeviceId);
                    message.put("Result", "true");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.resultToMqttBroker.equals("editNodeInfoFail")) {
                try {
                    message.put("Interface", "editNodeInfo");
                    message.put("NodeId", DeviceInfo.mqttDeviceId);
                    message.put("Result", "false");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.resultToMqttBroker.equals("setConfigurationTrue")) {
                try {
                    message.put("MessageType", "setConfiguration");
                    message.put("result", "true");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.resultToMqttBroker.equals("setConfigurationFail")) {
                try {
                    message.put("MessageType", "setConfiguration");
                    message.put("result", "false");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.resultToMqttBroker.equals("getMeterTrue")) {
                try {
                    message.put("MessageType", "getMeter");
                    message.put("result", "true");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.resultToMqttBroker.equals("getMeterFail")) {
                try {
                    message.put("MessageType", "getMeter");
                    message.put("result", "false");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.resultToMqttBroker.equals("resetMeterTrue")) {
                try {
                    message.put("MessageType", "resetMeter");
                    message.put("result", "true");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.resultToMqttBroker.equals("resetMeterFail")) {
                try {
                    message.put("MessageType", "resetMeter");
                    message.put("result", "false");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.resultToMqttBroker.equals("startStopSwitchLevelChangeTrue")) {
                try {
                    message.put("MessageType", "startStopSwitchLevelChange");
                    message.put("result", "true");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.resultToMqttBroker.equals("startStopSwitchLevelChangeFail")) {
                try {
                    message.put("MessageType", "startStopSwitchLevelChange");
                    message.put("result", "false");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.resultToMqttBroker.equals("switchAllOnTrue")) {
                try {
                    message.put("MessageType", "switchAllOn");
                    message.put("result", "true");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.resultToMqttBroker.equals("switchAllOnFail")) {
                try {
                    message.put("MessageType", "switchAllOn");
                    message.put("result", "false");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.resultToMqttBroker.equals("switchAllOffTrue")) {
                try {
                    message.put("MessageType", "switchAllOff");
                    message.put("result", "true");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.resultToMqttBroker.equals("switchAllOffFail")) {
                try {
                    message.put("MessageType", "switchAllOff");
                    message.put("result", "false");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.resultToMqttBroker.equals("setSwitchAllTrue")) {
                try {
                    message.put("MessageType", "setSwitchAll");
                    message.put("result", "true");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.resultToMqttBroker.equals("setSwitchAllFail")) {
                try {
                    message.put("MessageType", "setSwitchAll");
                    message.put("result", "false");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.resultToMqttBroker.equals("getSwitchAllTrue")) {
                try {
                    message.put("MessageType", "getSwitchAll");
                    message.put("result", "true");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.resultToMqttBroker.equals("getSwitchAllFail")) {
                try {
                    message.put("MessageType", "getSwitchAll");
                    message.put("result", "false");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.resultToMqttBroker.equals("setNotificationTrue")) {
                try {
                    message.put("MessageType", "setNotification");
                    message.put("result", "true");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.resultToMqttBroker.equals("setNotificationFail")) {
                try {
                    message.put("MessageType", "setNotification");
                    message.put("result", "false");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.resultToMqttBroker.equals("setDoorLockOperationTrue")) {
                try {
                    message.put("MessageType", "setDoorLockOperation");
                    message.put("result", "true");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.resultToMqttBroker.equals("setDoorLockOperationFail")) {
                try {
                    message.put("MessageType", "setDoorLockOperation");
                    message.put("result", "false");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.resultToMqttBroker.equals("setDoorLockConfigTrue")) {
                try {
                    message.put("MessageType", "setDoorLockConfig");
                    message.put("result", "true");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.resultToMqttBroker.equals("setDoorLockConfigFail")) {
                try {
                    message.put("MessageType", "setDoorLockConfig");
                    message.put("result", "false");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.resultToMqttBroker.equals("setBinarySwitchStateTrue")) {
                try {
                    message.put("MessageType", "setBinarySwitchState");
                    message.put("result", "true");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.resultToMqttBroker.equals("setBinarySwitchStateFail")) {
                try {
                    message.put("MessageType", "setBinarySwitchState");
                    message.put("result", "false");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.resultToMqttBroker.equals("getMeterSupportedTrue")) {
                try {
                    message.put("MessageType", "getMeterSupported");
                    message.put("result", "true");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.resultToMqttBroker.equals("getMeterSupportedFail")) {
                try {
                    message.put("MessageType", "getMeterSupported");
                    message.put("result", "false");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.resultToMqttBroker.equals("setDefaultTrue")) {
                try {
                    message.put("MessageType", "setDefault");
                    message.put("result", "true");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.resultToMqttBroker.equals("setDefaultFail")) {
                try {
                    message.put("MessageType", "setDefault");
                    message.put("result", "false");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";

            } else if (DeviceInfo.className.equals("openController")) {
                DeviceInfo.getMqttPayload = "getDeviceInfo";
                DeviceInfo.isOpenControllerFinish = true;
                Log.i(LOG_TAG, " === isOpenControllerFinish = true ===");
                DeviceInfo.className = "";
                DeviceInfo.result = "";
                DeviceInfo.resultToMqttBroker = "";
            } else if (DeviceInfo.resultToMqttBroker.contains("dongleBusy")) {
                String[] tmp = DeviceInfo.resultToMqttBroker.split(":");
                Log.d(LOG_TAG,"-17 !!!!!!!!!!!!!" + tmp[1]);
                if(tmp[1].equals("addDevice")) {
                    mTCPServer.sendMessage(Const.TCPClientPort, "\n" + "\t"+ "\"MessageType\":" + "\t" + "\"Node Add Status\"," + "\n" + "\t" + "\"Status\":" +
                            "\t" + tmp[2]); //TCP format
                } else if(tmp[1].equals("removeDevice")) {
                    mTCPServer.sendMessage(Const.TCPClientPort, "\n" + "\t"+ "\"MessageType\":" + "\t" + "\"Node Remove Status\"," + "\n" + "\t" + "\"Status\":" +
                            "\t" + tmp[2]); //TCP format
                } else if(tmp[1].equals("stopAddDevice")) {
                    mTCPServer.sendMessage(Const.TCPClientPort, "\n" + "\t"+ "\"MessageType\":" + "\t" + "\"Node StopAdd Status\"," + "\n" + "\t" + "\"Status\":" +
                            "\t" + tmp[2]); //TCP format
                } else if(tmp[1].equals("stopRemoveDevice")) {
                    mTCPServer.sendMessage(Const.TCPClientPort, "\n" + "\t"+ "\"MessageType\":" + "\t" + "\"Node StopRemove Status\"," + "\n" + "\t" + "\"Status\":" +
                            "\t" + tmp[2]); //TCP format
                } else if(tmp[1].equals("getRssiState")) {
                    mTCPServer.sendMessage(Const.TCPClientPort, "\n" + "\t"+ "\"MessageType\":" + "\t" + "\"Network Health Check\"," + "\n" + "\t" + "\"Status\":" +
                            "\t" + tmp[2]); //TCP format
                } else if(tmp[1].equals("removeFailDevice")) {
                    mTCPServer.sendMessage(Const.TCPClientPort, "\n" + "\t"+ "\"MessageType\":" + "\t" + "\"Remove Failed Node\"," + "\n" + "\t" + "\"Status\":" +
                            "\t" + tmp[2]); //TCP format
                } else if(tmp[1].equals("replaceFailDevice")) {
                    mTCPServer.sendMessage(Const.TCPClientPort, "\n" + "\t"+ "\"MessageType\":" + "\t" + "\"Replace Failed Node\"," + "\n" + "\t" + "\"Status\":" +
                            "\t" + tmp[2]); //TCP format
                }
                DeviceInfo.resultToMqttBroker = "";
            } else {
               if(DeviceInfo.className != "") {
                   try {
                       //Log.i(LOG_TAG, "result = " + DeviceInfo.result);
                       message = new JSONObject(DeviceInfo.result);
                       if (DeviceInfo.result.contains("NodeId")) {
                           //String devType = message.getString("devType");
                           String NodeId = message.getString("NodeId");
                           publishMessage(Const.PublicTopicName + NodeId, DeviceInfo.result);
                           DeviceInfo.className = "";
                           DeviceInfo.result = "";

                       } else if (DeviceInfo.result.contains("Node id")) {
                           //String devType = message.getString("devType");
                           String NodeId = message.getString("Node id");
                           publishMessage(Const.PublicTopicName + NodeId, DeviceInfo.result);
                           DeviceInfo.className = "";
                           DeviceInfo.result = "";

                       } else {
                           publishMessage(Const.PublicTopicName, DeviceInfo.result);
                           DeviceInfo.className = "";
                           DeviceInfo.result = "";
                       }

                   } catch (JSONException e) {
                       e.printStackTrace();
                   }
               }
            }

        }
    };

    private void getDeviceInfo(JSONObject message) {
        Log.i(LOG_TAG, "getDeviceInfo");
        ArrayList<String> tmpLine = Utils.searchString(DeviceInfo.result, "Node id");

        for (int idx = 1; idx < tmpLine.size(); idx++) {
            Log.i(LOG_TAG, "Node id (" + idx + ") = " + tmpLine.get(idx));
            subscribeToTopic(Const.PublicTopicName + "Zwave" + tmpLine.get(idx));
        }
        //publish result to MQTT public topic
        publishMessage(Const.PublicTopicName, DeviceInfo.result);
        Log.i(LOG_TAG, "node cnt = " + DeviceInfo.localSubTopiclist.size());
        if (!DeviceInfo.isZwaveInitFinish) {
            Log.i(LOG_TAG, " ===== isZwaveInitFinish  = true ====");
            DeviceInfo.isZwaveInitFinish = true;
        }

        String[] resultSplit = DeviceInfo.result.split(",");
        for (int i = 10; i < resultSplit.length; i++) { //i =10 no display security Status of Controller
            if (resultSplit[i].contains("Node security inclusion status")) {
                if (resultSplit[i].contains("S2")) {
                    try {
                        message.put("Interface", "Node security inclusion status");
                        message.put("Result", "Device is S2 security");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else if (resultSplit[i].contains("Normal")) {
                    try {
                        message.put("Interface", "Node security inclusion status");
                        message.put("Result", "Device is none security");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (resultSplit[i].contains("S0")) {
                    try {
                        message.put("Interface", "Node security inclusion status");
                        message.put("Result", "Device is S0 security");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                publishMessage(Const.PublicTopicName, message.toString());

            }
        }
    }

    private void getDeviceList(String result) {
        Log.i(LOG_TAG, "into getDeviceList");
        ArrayList<String> tmpLine = Utils.searchString(result, "deviceList");

        for (int idx = 0; idx < tmpLine.size(); idx++) {
            Log.i(LOG_TAG, "Node id (" + idx + ") = " + tmpLine.get(idx));
            //subscribeToTopic(Const.PublicTopicName + "Zwave" + tmpLine.get(idx));
        }
        //publish result to MQTT public topic
        publishMessage(Const.PublicTopicName, result);
        DeviceInfo.className = "";
        DeviceInfo.result = "";
        DeviceInfo.resultToMqttBroker = "";

    }

    private void specifyNodeInfo(String result) {
        Log.i(LOG_TAG, "getSpecifyDeviceInfo");
        ArrayList<String> tmpLine = Utils.searchString(result, "Node id");

        for (int idx = 0; idx < tmpLine.size(); idx++) {
            Log.i(LOG_TAG, "Node id (" + idx + ") = " + tmpLine.get(idx));
            //subscribeToTopic(Const.PublicTopicName + "Zwave" + tmpLine.get(idx));
        }
        //publish result to MQTT public topic
        publishMessage(Const.PublicTopicName, result);
        DeviceInfo.className = "";
        DeviceInfo.result = "";
        DeviceInfo.resultToMqttBroker = "";

    }

    private void allProvisionListReport(String result) {
        Log.i(LOG_TAG, "allProvisionListReport");
        ArrayList<String> tmpLine = Utils.searchString(result, "DSK");

        for (int idx = 0; idx < tmpLine.size(); idx++) {
            Log.i(LOG_TAG, "Node id (" + idx + ") = " + tmpLine.get(idx));
            //subscribeToTopic(Const.PublicTopicName + "Zwave" + tmpLine.get(idx));
        }
        //publish result to MQTT public topic
        publishMessage(Const.PublicTopicName, result);
        DeviceInfo.className = "";
        DeviceInfo.result = "";
        DeviceInfo.resultToMqttBroker = "";

    }

    private void addRemoveDevice(JSONObject message) {
        String[] tokens = DeviceInfo.result.split(":");
        String devType = tokens[1];
        String tNodeId = tokens[2];

        //Log.i(LOG_TAG, "gino result :   " + DeviceInfo.result);
        mTCPServer.sendMessage(Const.TCPClientPort, DeviceInfo.result); //TCP format
        Log.i(LOG_TAG, "into addRemoveDevice");
/*
        if(DeviceInfo.result.contains("Failed")) {
            Log.i(LOG_TAG, "addRemoveDevice Failed");
            DeviceInfo.result = "";
            DeviceInfo.className = "";
        }
*/

        if (DeviceInfo.result.contains("addDevice:") || DeviceInfo.result.contains("removeDevice:")) {

            if (tokens.length < 3) {
                Log.i(LOG_TAG, "addRemoveDevice length < 3");
            } else {
                Log.i(LOG_TAG, "addRemoveDevice length > 3");


                if (DeviceInfo.className.contains("addDevice")) {
                    Log.i(LOG_TAG, "addDevice:  Result");
                    try {
                        message.put("Interface", "addDevice");
                        message.put("NodeId", tNodeId);
                        if (tNodeId.equals("fail")) {
                            Log.i(LOG_TAG, "addRemoveDevice Result fail");
                            message.put("Result", "fail");
                        } else {
                            subscribeToTopic(Const.PublicTopicName + devType + tNodeId);
                            Log.i(LOG_TAG, "addRemoveDevice Result true");
                            message.put("Result", "true");
                            DeviceInfo.getMqttPayload = "getDeviceList";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    publishMessage(Const.PublicTopicName, message.toString());
                    DeviceInfo.className = "";
                } else {
                    Log.i(LOG_TAG, "removeDevice:  Result");
                    if(!setDefaultFlag) {
                        try {
                            message.put("Interface", "removeDevice");
                            message.put("NodeId", tNodeId);
                            if (tNodeId.equals("fail")) {
                                Log.i(LOG_TAG, "addRemoveDevice Result fail");
                                message.put("Result", "fail");
                            } else {
                                unsubscribeTopic(Const.PublicTopicName + devType + tNodeId);
                                Log.i(LOG_TAG, "addRemoveDevice Result true");
                                message.put("Result", "true");
                                DeviceInfo.getMqttPayload = "getDeviceList";
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        publishMessage(Const.PublicTopicName, message.toString());
                        DeviceInfo.className = "";
                    } else {
                        setDefaultFlag = false;
                        DeviceInfo.getMqttPayload = "getDeviceList";
                    }
                }
                //Const.TCPClientPort = 0;
            }
        }
        //不再DB裡面刪除或添加,回傳mqtt payload
        else if (DeviceInfo.className.equals("removeDevice")) {
            Log.i(LOG_TAG, "removeDevice  Result");
            if(DeviceInfo.result.contains("Success")) {
                try {
                    message.put("Interface", "removeDevice");
                    message.put("Result", "true");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
                DeviceInfo.getMqttPayload = "getDeviceList";
            } else if (DeviceInfo.result.contains("Failed")) {
                try {
                    message.put("Interface", "removeDevice");
                    message.put("Result", "fail");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
            }
        }

        //add fail send mqtt payload
        else if (DeviceInfo.className.equals("addDevice")) {
            Log.i(LOG_TAG, "addDevice  Result");
            if (DeviceInfo.result.contains("Failed")) {
                try {
                    message.put("Interface", "addDevice");
                    message.put("Result", "fail");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());
            }
        }
        /*
        else if (DeviceInfo.result.contains("NewAdded")) {
                String[] tmpAdd = DeviceInfo.result.split(",");
            if(tmpAdd[1].contains("No")) {
                Log.d(LOG_TAG, "second add device = " + tmpAdd[0] + "," + "\n" + "\t" + "\"Status\":" +
                        "\t" + "\"Failed\"");
                mTCPServer.sendMessage(Const.TCPClientPort, tmpAdd[0] + "," + "\n" + "\t" + "\"Status\":" +
                        "\t" + "\"Failed\""); //TCP format
            }
        }
        */
    }

}


