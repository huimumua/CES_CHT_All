package com.askey.firefly.zwave.control.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.askey.firefly.zwave.control.dao.ZwaveDeviceManager;
import com.askey.firefly.zwave.control.dao.ZwaveDeviceSceneManager;
import com.askey.firefly.zwave.control.net.TCPServer;
import com.askey.firefly.zwave.control.net.UDPConnectin;
import com.askey.firefly.zwave.control.ui.WelcomeActivity;
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
import java.util.Timer;
import java.util.TimerTask;

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
    public ZwaveControlService zwaveService;

    private byte[] dskNumber;
    private int currentNodeId = 0;
    private int mqttDeviceId = 0;
    private int mqttValue = 0;
    private int mqttTmp = 0;
    private int mqttTmp2 = 0;
    private int mqttTmp3 = 0;
    private int mqttTmp4 = 0;
    private int mqttTmp5 = 0;


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

        /*  launch tcp server and handle the tcp message */
        Log.i(LOG_TAG, "TCPServer = [" + handleTCPMessage() + "]");

        //bind service with ZwaveControlService
        Intent serviceIntent = new Intent(this, ZwaveControlService.class);
        this.bindService(serviceIntent, ZWserviceConn, Context.BIND_AUTO_CREATE);
        //this.bindService(serviceIntent, ZWserviceReq, Context.BIND_AUTO_CREATE);

       new Thread(reqCallBack).start();

        // init zwSceneManager and zwDevManager
        zwSceneManager = ZwaveDeviceSceneManager.getInstance(this);
        zwDevManager = ZwaveDeviceManager.getInstance(this);
    }

    // only execute one time
    public Runnable reqCallBack = new Runnable() {
        @Override
        public void run() {
            Intent serviceIntent = new Intent(MQTTBroker.this, ZwaveControlService.class);
            MQTTBroker.this.bindService(serviceIntent, ZWserviceReq, Context.BIND_AUTO_CREATE);
        }
    };


    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, "===== MQTTBroker onDestroy =====");
        super.onDestroy();

        unsubscribeTopic(Const.PublicTopicName);

        TCPServer.close();
        if (zwaveService != null) {
            zwaveService.closeController();
            //zwaveService.unRegisterListener(ZWCtlCB);
        }

        unbindService(ZWserviceConn);
        unbindService(ZWserviceReq);

        uDPConnecting.stopConn();
        if (mqttServer.getServerStatus()) {
            Log.i("MQTTClient", "mqttServer.stopServer()");
            mqttServer.stopServer();
        }

        zwaveService.unregister(ZWCtlCB);
        zwaveService.unregister(ZWCtlReq);
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

                Log.i(LOG_TAG, "TCP received , client ID = " + clientID + " |  message : " + message);
                if (message.contains("mobile_zwave")) {
                    if (message.contains("addDevice")) {
                        if (Const.TCPClientPort != 0) {
                            mTCPServer.sendMessage(clientID, Const.TCPSTRING + "addDevice:other"); //TCP format
                        } else {
                            String[] tokens = message.split(":");
                            String devType = tokens[2];
                            Const.TCPClientPort = clientID;
                            Log.i(LOG_TAG, "deviceService.addDevice(mCallback)");
                            zwaveService.addDevice(devType);

                        }
                    } else if (message.contains("removeDevice")) {
                        if (Const.TCPClientPort != 0) {
                            Log.i(LOG_TAG,"removeDevice other!");
                            mTCPServer.sendMessage(clientID, Const.TCPSTRING + "removeDevice:other");  //TCP format
                        } else {

                            String[] tokens = message.split(":");
                            String devType = tokens[2];
                            Const.TCPClientPort = clientID;
                            Log.i(LOG_TAG, "deviceService.removeDevice(mCallback)");
                            zwaveService.removeDevice(devType, 1);
                        }
                    } else if (message.contains("stopAddDevice")) {

                        Log.i(LOG_TAG, "deviceService.stopAddDevice(mCallback)");
                        Const.TCPClientPort = 0;
                        String[] tokens = message.split(":");
                        String devType = tokens[2];
                        zwaveService.stopAddDevice(devType);
                    } else if (message.contains("stopRemoveDevice")) {

                        Log.i(LOG_TAG, "deviceService.stopRemoveDevice(mCallback)");
                        Const.TCPClientPort = 0;
                        String[] tokens = message.split(":");
                        String devType = tokens[2];
                        zwaveService.stopRemoveDevice(devType);
                    } else if (message.contains("reNameDevice")) {

                        String[] tokens = message.split(":");
                        if (tokens.length > 6) {
                            Const.TCPClientPort = clientID;
                            String tHomeId = tokens[2];
                            int tDeviceId = Integer.parseInt(tokens[3]);
                            String tNewName = tokens[4];
                            String tDevType = tokens[5];
                            String tRoomName = tokens[6];

                            Log.i(LOG_TAG, "deviceService.editNodeInfo(mCallback," + tHomeId + "," + tDeviceId + "," + tNewName + "," +
                                    tDevType + "," + tRoomName + ")");
                            zwaveService.editNodeInfo(tHomeId, tDeviceId, tNewName, tDevType, tRoomName, "", "");

                        }
                    } else {
                        mTCPServer.sendMessage(clientID, Const.TCPSTRING + " Wrong Payload");  //TCP format
                    }
                } else if (message.contains("GrantKeys")) {
                    String[] tmp = message.split(":");
                    /*
                    if (tmp[1].equals("87")) {
                        mTCPServer.sendMessage(clientID, "dsk:1"); //TCP format
                    }
                    */
                    DeviceInfo.reqKey = Integer.valueOf(tmp[1]);


                } else if (message.contains("dsk")) {
                    String[] tmp = message.split(":");
                    DeviceInfo.reqKey = Integer.valueOf(tmp[1]);
                    mTCPServer.sendMessage(clientID, "CSA:CSA"); //TCP format

                } else if (message.contains("CSA")) {
                    String[] tmp = message.split(":");
                    DeviceInfo.reqKey = Integer.valueOf(tmp[1]);

                }
            }
        });
        mTCPServer.start();
        return mTCPServer.isAlive();
    }


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

                    if (zwaveService != null && mqttMessage.contains("desired")) {

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

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String mqttMessage = new String(message.getPayload());
                    mqttMessage = mqttMessage.replaceAll("\n", "");
                    Log.i(LOG_TAG, "Local MQTT Incoming [" + topic + "] : " + mqttMessage);

                    if (zwaveService != null && mqttMessage.contains("desired")) {

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

    private void handleMqttIncomingMessage(String TopicName, String mqttMessage) throws JSONException {
        //send aidl message to zwave control app

        String[] tokens = TopicName.split(Const.PublicTopicName);
        String[] devInfo = new String[2];
        String devType = "Zwave";
 /*
        if (!TopicName.equals(Const.PublicTopicName)) {
            if (tokens[1].contains("Zwave")) {
                devInfo = tokens[1].split("Zwave");
                devType = "Zwave";
                currentNodeId = Integer.valueOf(devInfo[1]);
                Log.d(LOG_TAG,"tNodeid: "+currentNodeId);
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

            switch (function) {
                case "addDevice":
                    Log.i(LOG_TAG, "deviceService.removeDeviceFromRoom");
                    zwaveService.addDevice("Zwave");
                    break;

                case "removeDeviceFromRoom":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    Log.i(LOG_TAG, "deviceService.removeDeviceFromRoom");
                    zwaveService.removeDeviceFromRoom(mqttDeviceId);
                    break;

                case "getDeviceList": //public channel
                    String tRoom = payload.getString("Room");
                    Log.i(LOG_TAG, "deviceService.getDevices tRoom=" + tRoom);
                    zwaveService.getDeviceList(tRoom);
                    break;

                case "editNodeInfo":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    Log.i(LOG_TAG, "deviceService.editNodeInfo payload:" + payload);

                    String Room = payload.getJSONObject("parameter").getString("Room");
                    String isFavorite = payload.getJSONObject("parameter").getString("isFavorite");
                    String name = payload.getJSONObject("parameter").getString("name");
                    String type = payload.getJSONObject("parameter").getString("type");

                    zwaveService.editNodeInfo("", mqttDeviceId, name, devType, type, Room, isFavorite);
                    break;

                case "getRecentDeviceList": //public channel
                    Log.i(LOG_TAG, "deviceService.getRecentDeviceList");
                    int number = Integer.parseInt(payload.getString("number"));
                    zwaveService.getRecentDeviceList(number);
                    break;

                case "addRoom": //public channel
                    Log.i(LOG_TAG, "deviceService.addRoom");
                    String roomName = payload.getString("RoomName");
                    zwaveService.addRoom(roomName);
                    break;

                case "getRooms": //public channel
                    Log.i(LOG_TAG, "deviceService.getRooms");
                    zwaveService.getRooms();
                    break;

                case "editRoom": //public channel
                    Log.i(LOG_TAG, "deviceService.editRoom");
                    String removeRoom = payload.getString("removeRoom");
                    String toRoom = payload.getString("toRoom");
                    zwaveService.editRoom(removeRoom, toRoom);
                    break;

                case "removeRoom": //public channel
                    Log.i(LOG_TAG, "deviceService.removeRoom");
                    roomName = payload.getString("RoomName");
                    zwaveService.removeRoom(roomName);
                    break;

                case "getBasic":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    Log.i(LOG_TAG, "deviceService.getBasic"+mqttDeviceId);
                    zwaveService.getBasic(devType, mqttDeviceId);
                    break;

                case "setBasic":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    mqttValue = Integer.parseInt(payload.getString("value"));
                    Log.i(LOG_TAG, "deviceService.setBasic deviceId= " + mqttDeviceId + " value = "+mqttValue);
                    zwaveService.setBasic(devType, mqttDeviceId, mqttValue);
                    break;

                case "getSwitchMultilevel":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    Log.i(LOG_TAG, "deviceService.getBrightness"+mqttDeviceId);
                    zwaveService.getSwitchMultiLevel(devType, mqttDeviceId);
                    break;

                case "setSwitchMultilevel":
                    mqttValue = Integer.parseInt(payload.getString("value"));
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    mqttTmp = Integer.parseInt(payload.getString("duration"));
                    Log.i(LOG_TAG, "deviceService.setSwitchMultilevel deviceId= " + mqttDeviceId + " value = "+mqttValue + "duration "+mqttTmp);
                    zwaveService.setSwitchMultiLevel(devType, mqttDeviceId, mqttValue, mqttTmp);
                    break;

                case "getSwitchColor":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    mqttTmp = Integer.parseInt(payload.getString("compId"));
                    Log.i(LOG_TAG, "deviceService.getLampColor"+mqttDeviceId+mqttTmp);
                    zwaveService.getSwitchColor(devType, mqttDeviceId,mqttTmp);
                    break;

                case "setSwitchColor":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    mqttTmp = Integer.parseInt(payload.getString("colorId"));
                    mqttTmp2 = Integer.parseInt(payload.getString("colorValue"));
                    Log.i(LOG_TAG, "deviceService.setSwitchColor"+mqttDeviceId+mqttTmp+mqttTmp2);
                    zwaveService.setSwitchColor(devType,mqttDeviceId,mqttTmp,mqttTmp2);
                    break;

                case "getSupportedColor":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    Log.i(LOG_TAG, "deviceService.getSupportedColor"+mqttDeviceId);
                    zwaveService.getSupportColor(devType,mqttDeviceId);
                    break;

                case "startStopColorLevelChange":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    mqttTmp = Integer.parseInt(payload.getString("dir"));
                    mqttTmp2 = Integer.parseInt(payload.getString("ignore"));
                    mqttTmp3 = Integer.parseInt(payload.getString("colorId"));
                    mqttTmp4 = Integer.parseInt(payload.getString("startLevel"));

                    Log.i(LOG_TAG, "deviceService.startStopColorLevelChange"+mqttDeviceId+mqttTmp+mqttTmp2+mqttTmp3+mqttTmp4);
                    zwaveService.startStopColorLevelChange(devType,mqttDeviceId,mqttTmp,mqttTmp2,mqttTmp3,mqttTmp4);
                    break;


                case "getConfiguration":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    mqttTmp = Integer.parseInt(payload.getString("paramMode"));
                    mqttTmp2 = Integer.parseInt(payload.getString("paramNumber"));
                    mqttTmp3 = Integer.parseInt(payload.getString("rangeStart"));
                    mqttTmp4 = Integer.parseInt(payload.getString("rangeEnd"));
                    Log.i(LOG_TAG, "deviceService.getConfiguration");
                    zwaveService.getConfiguration(mqttDeviceId,mqttTmp,mqttTmp2,mqttTmp3,mqttTmp4);
                    break;

                case "setConfiguration":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    mqttTmp = Integer.parseInt(payload.getString("paramNumber"));
                    mqttTmp2 = Integer.parseInt(payload.getString("paramSize"));
                    mqttTmp3 = Integer.parseInt(payload.getString("useDefault"));
                    mqttTmp4 = Integer.parseInt(payload.getString("paramValue"));
                    Log.i(LOG_TAG, "deviceService.setConfiguration");
                    try {
                        zwaveService.setConfiguration(mqttDeviceId,mqttTmp,mqttTmp2,mqttTmp3,mqttTmp4);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;

                case "getMeter":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    mqttTmp = Integer.parseInt(payload.getString("meterUnit"));
                    Log.i(LOG_TAG, "deviceService.getPower"+mqttDeviceId+mqttTmp);
                    zwaveService.getMeter(devType, mqttDeviceId, mqttTmp);
                    break;

                case "resetMeter":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    Log.i(LOG_TAG, "deviceService.resetMeter"+mqttDeviceId);
                    zwaveService.resetMeter(devType, mqttDeviceId);
                    break;

                case "getGroupInfo":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    mqttTmp = Integer.parseInt(payload.getString("groupId"));
                    mqttTmp2 = Integer.parseInt(payload.getString("endpointId"));
                    Log.i(LOG_TAG, "deviceService.getGroupInfo");

                    zwaveService.getGroupInfo(devType, mqttDeviceId,mqttTmp,mqttTmp2);
                    break;

                case "addEndpointsToGroup":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    mqttTmp = Integer.parseInt(payload.getString("groupId"));
                    //mqttTmp2 = Integer.parseInt(payload.getString("arr"));
                    mqttTmp3 = Integer.parseInt(payload.getString("endpointId"));

                    Log.i(LOG_TAG, "deviceService.addEndpointsToGroup");

                    String arr = payload.optString("arr");
                    JSONArray ja = new JSONArray(arr);
                    ArrayList<Integer> arrList = new ArrayList<>();
                    for(int j=0; j<ja.length(); j++){
                        JSONObject json = ja.getJSONObject(j);
                        arrList.add(Integer.valueOf(json.getString("controlNodeId").toString()));
                    }
                    zwaveService.addEndpointsToGroup(devType,mqttDeviceId,mqttTmp,Utils.convertIntegers(arrList),mqttTmp3);
                    break;

                case "removeEndpointsFromGroup":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    mqttTmp = Integer.parseInt(payload.getString("groupId"));
                    //mqttTmp2 = Integer.parseInt(payload.getString("arr"));
                    mqttTmp3 = Integer.parseInt(payload.getString("endpointId"));

                    Log.i(LOG_TAG, "deviceService.removeEndpointsFromGroup");

                    arr = payload.optString("arr");
                    ja = new JSONArray(arr);
                    arrList = new ArrayList<>();
                    for(int j=0; j<ja.length(); j++){
                        JSONObject json = ja.getJSONObject(j);
                        arrList.add(Integer.valueOf(json.getString("controlNodeId").toString()));
                    }

                    zwaveService.removeEndpointsFromGroup(devType,mqttDeviceId,mqttTmp,Utils.convertIntegers(arrList),mqttTmp3);
                    break;

                case "getMaxSupportedGroups":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    mqttTmp = Integer.parseInt(payload.getString("endpointId"));

                    Log.i(LOG_TAG, "deviceService.getMaxSupportedGroups");
                    zwaveService.getMaxSupportedGroups(mqttDeviceId,mqttTmp);
                    break;

                case "setScheduleActive":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    String active = payload.getString("active");
                    Log.i(LOG_TAG, "deviceService.setScheduleActive "+active);
                    zwaveService.setScheduleActive(devType,mqttDeviceId,active);
                    break;

                case "getScheduleList":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    Log.i(LOG_TAG, "deviceService.getScheduleList");
                    zwaveService.getScheduleList(devType,mqttDeviceId);
                    break;

                case "removeSchedule":
                    String Day = payload.getString("dayOfWeek");
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    Log.i(LOG_TAG, "deviceService.removeSchedule " +Day);
                    zwaveService.removeSchedule(devType,mqttDeviceId,Day);
                    break;

                case "setSchedule":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    Day = payload.getString("dayOfWeek");
                    active = payload.getString("active");
                    String variableValue = payload.getString("variableValue");
                    String startTime = payload.getString("StartTime");
                    String endTime = payload.getString("EndTime");
                    Log.i(LOG_TAG, "deviceService.setSchedule " +Day);
                    zwaveService.setSchedule(devType,mqttDeviceId,Day,startTime,endTime,Integer.valueOf(variableValue),active);
                    break;

                case "getFavoriteList": //public channel
                    Log.i(LOG_TAG, "deviceService.getFavoriteList");
                    zwaveService.getFavoriteList();
                    break;

                case "editFavoriteList": //public channel
                    Log.i(LOG_TAG, "deviceService.editFavoriteList");

                    String addarr = payload.optString("addFavorite");
                    ja = new JSONArray(addarr);
                    ArrayList<String> addList = new ArrayList<>();
                    for(int idx=0; idx<ja.length(); idx++){
                        JSONObject json = ja.getJSONObject(idx);
                        addList.add(json.getString("nodeId").toString());
                    }

                    String removearr = payload.optString("removeFavorite");
                    ja = new JSONArray(removearr);
                    ArrayList<String> removeList = new ArrayList<>();
                    for(int idx=0; idx<ja.length(); idx++){
                        JSONObject json = ja.getJSONObject(idx);
                        removeList.add(json.getString("nodeId").toString());
                    }

                    zwaveService.editFavoriteList(addList,removeList);
                    break;

                case "setSceneAction":
                    String targetColor,cuuentColor = "";
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
                    Log.i(LOG_TAG, "deviceService.setSceneAction "+sceneName+" | nodeId = "+nodeId);
                    //zwaveService.setSceneAction();
                    break;

                case "getSceneList": //public channel
                    Log.i(LOG_TAG, "deviceService.getScene");
                    zwaveService.getScene();
                    break;

                case "removeSceneAction":
                    sceneName = payload.getString("sceneName");
                    nodeId = payload.getJSONObject("condition").getString("nodeId");
                    Log.i(LOG_TAG, "deviceService.removeSceneAction "+sceneName+" | nodeId = "+nodeId);
                    //zwaveService.removeSceneAction();
                    break;

                case "removeScene":
                    sceneName = payload.getString("sceneName");
                    Log.i(LOG_TAG, "deviceService.removeScene " +sceneName);
                    //zwaveService.removeScene(sceneName);
                    break;

                case "editScene":
                    sceneName = payload.getString("sceneName");
                    iconName = payload.getString("iconName");
                    String newSceneName = payload.getString("newSceneName");
                    String newIconName = payload.getString("newIconName");
                    Log.i(LOG_TAG, "deviceService.editScene " +sceneName+" to "+newSceneName+"" +
                            " |iconName = "+iconName+" to "+newIconName);
                    //zwaveService.editScene(sceneName);
                    break;

                case "executeScene":
                    String action = payload.getString("action");
                    sceneName = payload.getString("sceneName");
                    Log.i(LOG_TAG, "deviceService.removeScene " +sceneName + " action = "+action);
                    //zwaveService.editScene(sceneName);
                    break;

                case "addProvisionListEntry":
                    Log.i(LOG_TAG, "deviceService.addProvisionListEntry");
                    String bootMode = payload.getString("BootMode");
                    if(bootMode.contains("01")) {
                        DeviceInfo.bootMode = true;
                        Log.d(LOG_TAG,"DeviceInfo.bootMode = true");

                    } else if (bootMode.contains("00")) {
                        DeviceInfo.bootMode = false;
                        Log.d(LOG_TAG,"DeviceInfo.bootMode = false");
                    }

                    Log.i(LOG_TAG, "BootMode : "+ DeviceInfo.bootMode);


                    DeviceInfo.dskNumber = payload.getString("dsk");
                    String str = payload.getString("dsk") +'\0';
                    Log.i(LOG_TAG, str);
                    dskNumber = str.getBytes();
                    zwaveService.addProvisionListEntry("Zwave",dskNumber,true);
                    break;

                case "rmProvisionListEntry":
                    Log.i(LOG_TAG, "deviceService.rmProvisionListEntry");
                    DeviceInfo.dskNumber = payload.getString("dsk");
                    str = payload.getString("dsk") +'\0';
                    dskNumber = str.getBytes();
                    zwaveService.rmProvisionListEntry("Zwave",dskNumber);
                    break;

                case "rmAllProvisionListEntry":
                    Log.i(LOG_TAG, "deviceService.rmAllProvisionListEntry");
                    zwaveService.rmAllProvisionListEntry();
                    break;

                case "editProvisionListEntry":
                    Log.i(LOG_TAG, "deviceService.editProvisionListEntry");
                    DeviceInfo.dskNumber = payload.getString("originalDsk");
                    str = payload.getString("originalDsk") +'\0';
                    Log.i(LOG_TAG, str);
                    dskNumber = str.getBytes();
                    zwaveService.rmProvisionListEntry("Zwave",dskNumber);
                    DeviceInfo.dskNumber = payload.getString("dsk");
                    str = payload.getString("dsk") +'\0';
                    Log.i(LOG_TAG, str);
                    dskNumber = str.getBytes();
                    mqttTmp = Integer.parseInt(payload.getString("inclusionState"));
                    mqttTmp2 = Integer.parseInt(payload.getString("boot_mode"));

                    if(mqttTmp == 0 ){
                        zwaveService.addProvisionListEntry("Zwave",dskNumber,false);
                    } else {
                        zwaveService.addProvisionListEntry("Zwave",dskNumber,true);
                    }

                    break;

                case "getAllProvisionListEntry":
                    Log.i(LOG_TAG, "deviceService.getAllProvisionListEntry");
                    zwaveService.getAllProvisionListEntry();
                    break;

                case "getProvisionListEntry":
                    Log.i(LOG_TAG, "deviceService.getProvisionListEntry");
                    DeviceInfo.dskNumber = payload.getString("dsk");
                    str = payload.getString("dsk") +'\0';
                    Log.i(LOG_TAG, str);
                    dskNumber = str.getBytes();
                    zwaveService.getProvisionListEntry("Zwave",dskNumber);
                    break;

                case "getRssiState":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    Log.i(LOG_TAG, "deviceService.getRssiState"+mqttDeviceId);
                    zwaveService.startNetworkHealthCheck();
                    break;

                case "getBattery":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    Log.i(LOG_TAG, "deviceService.getBattery" +mqttDeviceId);
                    zwaveService.getDeviceBattery(devType,mqttDeviceId);
                    break;

                case "getSensorMultilevel":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    Log.i(LOG_TAG, "deviceService.getSensorMultilevel"+mqttDeviceId);
                    try {
                        zwaveService.getSensorMultiLevel(devType,mqttDeviceId);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;

                case "getSupportSwitchType":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    Log.i(LOG_TAG, "deviceService.getSupportSwitchType"+mqttDeviceId);
                    zwaveService.getSupportedSwitchType(mqttDeviceId);
                    break;

                case "startStopSwitchLevelChange":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    mqttTmp = Integer.parseInt(payload.getString("startLvlVal"));
                    mqttTmp2 = Integer.parseInt(payload.getString("duration"));
                    mqttTmp3 = Integer.parseInt(payload.getString("pmyChangeDir"));
                    mqttTmp4 = Integer.parseInt(payload.getString("secChangeDir "));
                    mqttTmp5 = Integer.parseInt(payload.getString("secStep"));

                    Log.i(LOG_TAG, "deviceService.startStopSwitchLevelChange"+mqttDeviceId + mqttTmp+ mqttTmp2+mqttTmp3+mqttTmp4+mqttTmp5);
                    zwaveService.startStopSwitchLevelChange(mqttDeviceId,mqttTmp,mqttTmp2,mqttTmp3,mqttTmp4,mqttTmp5);
                    break;

                case "getPowerLevel":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    Log.i(LOG_TAG, "deviceService.getPowerLevel"+mqttDeviceId);
                    zwaveService.getPowerLevel(mqttDeviceId);
                    break;

                case "switchAllOn":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    Log.i(LOG_TAG, "deviceService.switchAllOn"+mqttDeviceId);
                    zwaveService.setSwitchAllOn(devType,mqttDeviceId);
                    break;

                case "switchAllOff":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    Log.i(LOG_TAG, "deviceService.switchAllOff"+mqttDeviceId);
                    zwaveService.setSwitchAllOff(devType,mqttDeviceId);
                    break;

                case "setSwitchAll":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    mqttTmp = Integer.parseInt(payload.getString("value"));

                    Log.i(LOG_TAG, "deviceService.setSwitchAll"+mqttDeviceId);
                    zwaveService.setSwitchAll(devType,mqttDeviceId,mqttTmp);
                    break;

                case "getSwitchAll":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));

                    Log.i(LOG_TAG, "deviceService.GetSwitchAll"+mqttDeviceId);
                    zwaveService.getSwitchAll(devType,mqttDeviceId);
                    break;

                case "getSensorBinary":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    mqttTmp = Integer.parseInt(payload.getString("sensorType"));
                    Log.i(LOG_TAG, "deviceService.getSensorBinary");
                    zwaveService.getSensorBasic(mqttDeviceId,mqttTmp);
                    break;

                case "getSensorBinarySupportedSensor":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    Log.i(LOG_TAG, "deviceService.getSensorBinarySupportedSensor"+mqttDeviceId);
                    zwaveService.GetSensorBinarySupportedSensor(mqttDeviceId);
                    break;

                case "getMeterSupported":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    Log.i(LOG_TAG, "deviceService.getMeterSupported"+mqttDeviceId);
                    zwaveService.getMeterSupported(mqttDeviceId);
                    break;

                case "getSpecificGroup":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    mqttTmp = Integer.parseInt(payload.getString("endpointId"));

                    Log.i(LOG_TAG, "deviceService.getSpecificGroup"+mqttDeviceId+mqttTmp);
                    zwaveService.getSpecificGroup(mqttDeviceId,mqttTmp);
                    break;

                case "getNotification":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    mqttTmp = Integer.parseInt(payload.getString("alarmType"));
                    mqttTmp2 = Integer.parseInt(payload.getString("notifType"));
                    mqttTmp3 = Integer.parseInt(payload.getString("status"));

                    Log.i(LOG_TAG, "deviceService.getNotification"+mqttDeviceId+mqttTmp+mqttTmp2+mqttTmp3);
                    zwaveService.getSensorNotification(mqttDeviceId,mqttTmp,mqttTmp2,mqttTmp3);
                    break;


                case "setNotification":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    mqttTmp = Integer.parseInt(payload.getString("notificationType"));
                    mqttTmp2 = Integer.parseInt(payload.getString("status"));

                    Log.i(LOG_TAG, "deviceService.setNotification"+mqttDeviceId+mqttTmp+mqttTmp2);
                    zwaveService.setNotification(mqttDeviceId,mqttTmp,mqttTmp2);
                    break;

                case "getSupportedNotification":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    Log.i(LOG_TAG, "deviceService.getSupportedNotification"+mqttDeviceId);
                    zwaveService.getSupportedNotification(mqttDeviceId);
                    break;

                case "getSupportedEventNotification":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    mqttTmp = Integer.parseInt(payload.getString("notifType"));
                    Log.i(LOG_TAG, "deviceService.getSupportedEventNotification"+mqttDeviceId+mqttTmp);
                    zwaveService.getSupportedEventNotification(mqttDeviceId,mqttTmp);
                    break;

                case "getSpecifyDeviceInfo":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    Log.i(LOG_TAG, "deviceService.getSpecifyDeviceInfo");
                    Log.i(LOG_TAG, "mqttDeviceId: "+mqttDeviceId);
                    zwaveService.getSpecifyDeviceInfo(mqttDeviceId);
                    break;

                case "removeFailDevice":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    Log.i(LOG_TAG, "deviceService.removeFailDevice");
                    Log.i(LOG_TAG, "mqttDeviceId: "+mqttDeviceId);
                    zwaveService.removeFailedDevice(mqttDeviceId);
                    break;

                case "checkNodeIsFailed":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    Log.i(LOG_TAG, "deviceService.checkNodeIsFailed");
                    Log.i(LOG_TAG, "mqttDeviceId: "+mqttDeviceId);
                    zwaveService.checkNodeIsFailed(mqttDeviceId);
                    break;

                case "setDefault":
                    Log.i(LOG_TAG, "deviceService.setDefault");
                    zwaveService.setDefault();
                    break;

                case "replaceFailDevice":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    Log.i(LOG_TAG, "deviceService.replaceFailDevice");
                    Log.i(LOG_TAG, "mqttDeviceId: "+mqttDeviceId);
                    zwaveService.replaceFailedDevice(mqttDeviceId);
                    break;

                case "startLearnMode":
                    Log.i(LOG_TAG, "deviceService.startLearnMode");
                    zwaveService.StartLearnMode();
                    break;

                case "getSceneActuatorConf":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    mqttTmp = Integer.parseInt(payload.getString("sceneId"));
                    Log.i(LOG_TAG, "deviceService.getSceneActuatorConf");

                    zwaveService.getSceneActuatorConf(mqttDeviceId,mqttTmp);
                    break;

                case "getSupportedCentralScene":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    mqttTmp = Integer.parseInt(payload.getString("endpointId"));
                    Log.i(LOG_TAG, "deviceService.getSupportedCentralScene");

                    zwaveService.getSupportedCentralScene(mqttDeviceId,mqttTmp);
                    break;

                case "getDoorLockOperation":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    Log.i(LOG_TAG, "deviceService.getDoorLockOperation");

                    zwaveService.getDoorLockOperation(mqttDeviceId);
                    break;

                case "setDoorLockOperation":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    mqttTmp = Integer.parseInt(payload.getString("mode"));
                    Log.i(LOG_TAG, "deviceService.setDoorLockOperation");

                    zwaveService.setDoorLockOperation(mqttDeviceId,mqttTmp);
                    break;

                case "getDoorLockConfiguration":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    Log.i(LOG_TAG, "deviceService.getDoorLockConfiguration");

                    zwaveService.getDoorLockConfiguration(mqttDeviceId);
                    break;

                case "setDoorLockConfig":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    mqttTmp = Integer.parseInt(payload.getString("type"));
                    mqttTmp2 = Integer.parseInt(payload.getString("outSta"));
                    mqttTmp3 = Integer.parseInt(payload.getString("insta"));
                    mqttTmp4 = Integer.parseInt(payload.getString("tmoutMin"));
                    mqttTmp5 = Integer.parseInt(payload.getString("tmoutSec"));
                    Log.i(LOG_TAG, "deviceService.setDoorLockConfig");

                    zwaveService.setDoorLockConfiguration(mqttDeviceId,mqttTmp,mqttTmp2,mqttTmp3,mqttTmp4,mqttTmp5);
                    break;

                case "setBinarySwitchState":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    mqttTmp = Integer.parseInt(payload.getString("state"));
                    mqttTmp2 = Integer.parseInt(payload.getString("duration"));
                    Log.i(LOG_TAG, "deviceService.setBinarySwitchState");

                    zwaveService.SetBinarySwitchState(devType,mqttDeviceId,mqttTmp);
                    break;

                case "getBinarySwitchState":
                    mqttDeviceId = Integer.parseInt(payload.getString("deviceId"));
                    Log.i(LOG_TAG, "deviceService.getBinarySwitchState");

                    zwaveService.GetBinarySwitchState(devType,mqttDeviceId);
                    break;


                default:
                    Log.e(LOG_TAG,"no support this function, please make sure this mqtt message "+ function);
                    break;

            }
        }else{
            Log.e(LOG_TAG,"wrong mqtt format, please make sure this mqtt message" + mqttMessage);
        }

    }

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
    private void publishMessage(String publishTopic, String publishMessage){

        try {

            Log.i(LOG_TAG, "Public LOACAL MESSAGE"+ ":" + publishMessage);

            JSONObject payload=new JSONObject(publishMessage);
            JSONObject json=new JSONObject();
            json.put("reported", payload);

            Log.i(LOG_TAG, "Public LOACAL MESSAGE"+ ":" + json.toString());
            MqttMessage message = new MqttMessage();
            message.setPayload(json.toString().getBytes());
            Log.i(LOG_TAG, publishTopic + ":" + publishMessage);


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
        }  catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // AIDL
    private ServiceConnection ZWserviceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            zwaveService = ((ZwaveControlService.MyBinder) service).getService();
            if (zwaveService != null) {

                Log.i(LOG_TAG, "bind service with ZWaveControlService");
                zwaveService.register(ZWCtlCB);
                //zwaveService.register(ZWCtlReq);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (!DeviceInfo.isMQTTInitFinish || !DeviceInfo.isOpenControllerFinish) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        zwaveService.getDeviceInfo();

                    }
                }).start();
            } else {
                Log.i(LOG_TAG, "Failed to bind service with ZWaveControlService");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    // AIDL
    private ServiceConnection ZWserviceReq = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            zwaveService = ((ZwaveControlService.MyBinder) service).getService();

            if (zwaveService != null) {

                Log.d(LOG_TAG, "bind req service with ZWaveControlService");
                zwaveService.register(ZWCtlReq);

            } else {
                Log.i(LOG_TAG, "Failed to bind service with ZWaveControlService");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };


    // ZwaveControlService CallBack

    public ZwaveControlService.zwaveControlReq_CallBack ZWCtlReq = new ZwaveControlService.zwaveControlReq_CallBack() {
        @Override
        public void zwaveControlReqResultCallBack(String className, String result) {
            JSONObject message = new JSONObject();

            if (result.contains("Grant Keys Msg")) {
                grantKey(result);

            } else if (result.contains("PIN Requested Msg")) {

            }
        }
    };

    //receive message to do something
    public ZwaveControlService.zwaveCallBack ZWCtlCB = new ZwaveControlService.zwaveCallBack() {
        @Override
        public void zwaveControlResultCallBack(String className, String result) {

            JSONObject message = new JSONObject();

            if (className.equals("addDevice") || className.equals("removeDevice")) {

                addRemoveDevice(className,result,message);

            } else if (className.equals("All Node Info Report")) {

                getDeviceInfo(result,message);

            } else if (className.equals("reNameDevice")) {

                reNameDevice(result);

            } else if (result.contains("Remove Failed Node")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    String status = jsonObject.optString("Status");

                    message.put("MessageType", "Remove Failed Node");
                    message.put("Status", status);

                    Log.d(LOG_TAG,"gino: "+ status);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());

            } else if (result.contains("Replace Failed Node")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    String status = jsonObject.optString("Status");

                    message.put("MessageType", "Replace Failed Node");
                    message.put("Status", status);

                    Log.d(LOG_TAG,"gino: "+ status);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());

            } else if (result.contains("Node Is Failed Check Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    String nodeId = jsonObject.optString("Node id");
                    String status = jsonObject.optString("Status");

                    message.put("MessageType", "Node Is Failed Check Report");
                    message.put("Node id", nodeId);
                    message.put("Status", status);

                    Log.d(LOG_TAG,"gino: "+ nodeId);
                    Log.d(LOG_TAG,"gino: "+ status);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());

            }  else if (result.contains("Replace Failed Node")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    String status = jsonObject.optString("Status");

                    message.put("MessageType", "Replace Failed Node");
                    message.put("Status", status);

                    Log.d(LOG_TAG,"gino: "+ status);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());

            }  else if (result.contains("Controller Reset Status")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    String status = jsonObject.optString("Status");

                    message.put("MessageType", "Controller Reset Status");
                    message.put("Status", status);

                    Log.d(LOG_TAG,"gino: "+ status);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());

            } else if (result.contains("Controller Attribute")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    String homeId = jsonObject.optString("Home id");
                    String nodeId = jsonObject.optString("Node id");
                    String role = jsonObject.optString("Network Role");
                    String vendorId = jsonObject.optString("Vendor Id");
                    String proType = jsonObject.optString("Vendor Product Type");
                    String libType = jsonObject.optString("Z-wave Library Type");
                    String protocolVersion = jsonObject.optString("Z-wave Protocol Version");
                    String appVersion = jsonObject.optString("Application Version");

                    message.put("MessageType", "Controller Attribute");
                    message.put("Home id", homeId);
                    message.put("Node id", nodeId);
                    message.put("Network Role", role);
                    message.put("Vendor Id",vendorId);
                    message.put("Vendor Product Type",proType);
                    message.put("Z-wave Library Type",libType);
                    message.put("Z-wave Protocol Version",protocolVersion);
                    message.put("Application Version",appVersion);

                    Log.d(LOG_TAG,"gino: "+ homeId);
                    Log.d(LOG_TAG,"gino: "+ nodeId);
                    Log.d(LOG_TAG,"gino: "+ role);
                    Log.d(LOG_TAG,"gino: "+ vendorId);
                    Log.d(LOG_TAG,"gino: "+ proType);
                    Log.d(LOG_TAG,"gino: "+ libType);
                    Log.d(LOG_TAG,"gino: "+ protocolVersion);
                    Log.d(LOG_TAG,"gino: "+ appVersion);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());

            } else if (result.contains("All Node List Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    String list = jsonObject.optString("Added Node List");

                    message.put("MessageType", "All Node List Report");
                    message.put("Added Node List", list);

                    Log.d(LOG_TAG,"gino: "+ list);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());

            }  else if (result.contains("Specify Node Info")) {
                /*
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(result);
                        String nodeInfo = jsonObject.optString("Detialed Node Info");
                        String tmp = jsonObject.getString("Detialed Node Info");

                        message.put("MessageType", "Specify Node Info");
                        message.put("Detialed Node Info", nodeInfo);
                        message.put("Detialed Node Info 2 ", tmp);


                        Log.d(LOG_TAG,"gino: "+ nodeInfo);
                        Log.d(LOG_TAG,"gino2: "+ tmp);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    publishMessage(Const.PublicTopicName, message.toString());
                */
                getDeviceInfo(result,message);
            } else if (result.contains("Controller Init Status")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    String status = jsonObject.optString("Status");

                    message.put("MessageType", "Controller Init Status");
                    message.put("Status", status);

                    Log.d(LOG_TAG,"gino: "+ status);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());

            } else if (result.contains("Node Battery Value")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    String nodeId = jsonObject.optString("Node id");
                    String id = jsonObject.optString("EndPoint Id");
                    String value = jsonObject.optString("Battery Value");

                    message.put("MessageType", "Node Battery Value");
                    message.put("Node id", nodeId);
                    message.put("EndPoint Id", id);
                    message.put("Battery Value", value);

                    Log.d(LOG_TAG,"gino: "+ nodeId);
                    Log.d(LOG_TAG,"gino: "+ id);
                    Log.d(LOG_TAG,"gino: "+ value);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());

            } else if (result.contains("Switch Multi-lvl Report Information")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    String nodeId = jsonObject.optString("Node id");
                    String val = jsonObject.optString("Cur Val");

                    message.put("MessageType", "Switch Multi-lvl Report Information");
                    message.put("Node id", nodeId);
                    message.put("Cur Val", val);
                    message.put("Tgt Val", "Unsupported");
                    message.put("Durration", "Unsupported");

                    Log.d(LOG_TAG,"gino: "+ nodeId);
                    Log.d(LOG_TAG,"gino: "+ val);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());

            } else if (result.contains("Power Level Get Information")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    String nodeId = jsonObject.optString("Node id");
                    String level = jsonObject.optString("Power Level");

                    message.put("MessageType", "Power Level Get Information");
                    message.put("Node id", nodeId);
                    message.put("Power Level", level);

                    Log.d(LOG_TAG,"gino: "+ nodeId);
                    Log.d(LOG_TAG,"gino: "+ level);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());

            } else if (result.contains("Switch All Get Information")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    String nodeId = jsonObject.optString("Node id");
                    String mode = jsonObject.optString("mode");

                    message.put("MessageType", "Switch All Get Information");
                    message.put("Node id", nodeId);
                    message.put("mode", mode);

                    Log.d(LOG_TAG,"gino: "+ nodeId);
                    Log.d(LOG_TAG,"gino: "+ mode);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());

            } else if (result.contains("Binary Sensor Information")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    String nodeId = jsonObject.optString("Node id");
                    String type = jsonObject.optString("Event Type");
                    String state = jsonObject.optString("state");

                    message.put("MessageType", "Binary Sensor Information");
                    message.put("Node id", nodeId);
                    message.put("Event Type", type);
                    message.put("state", state);

                    Log.d(LOG_TAG,"gino: "+ nodeId);
                    Log.d(LOG_TAG,"gino: "+ type);
                    Log.d(LOG_TAG,"gino: "+ state);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());

            } else if (result.contains("Binary Sensor Support Get Information")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    String nodeId = jsonObject.optString("Node id");
                    String type = jsonObject.optString("Supported type");

                    message.put("MessageType", "Binary Sensor Support Get Information");
                    message.put("Node id", nodeId);
                    message.put("Supported type", type);

                    Log.d(LOG_TAG,"gino: "+ nodeId);
                    Log.d(LOG_TAG,"gino: "+ type);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());

            }  else if (result.contains("Meter Report Information")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
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

                    Log.d(LOG_TAG,"gino: "+ nodeId);
                    Log.d(LOG_TAG,"gino: "+ mType);
                    Log.d(LOG_TAG,"gino: "+ rType);
                    Log.d(LOG_TAG,"gino: "+ mRead);
                    Log.d(LOG_TAG,"gino: "+ mUnit);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());

            } else if (result.contains("Meter Cap Information")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    String nodeId = jsonObject.optString("Node id");
                    String mType = jsonObject.optString("Meter type");
                    String reset = jsonObject.optString("Can be reset?");
                    String unit = jsonObject.optString("Supported unit");

                    message.put("MessageType", "Meter Cap Information");
                    message.put("Node id", nodeId);
                    message.put("Meter type", mType);
                    message.put("Can be reset?", reset);
                    message.put("Supported unit", unit);

                    Log.d(LOG_TAG,"gino: "+ nodeId);
                    Log.d(LOG_TAG,"gino: "+ mType);
                    Log.d(LOG_TAG,"gino: "+ reset);
                    Log.d(LOG_TAG,"gino: "+ unit);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());

            } else if (result.contains("Wake Up Cap Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    String wakeUpSetting = jsonObject.optString("Wake up settings");

                    message.put("MessageType", "Wake Up Cap Report");
                    message.put("Wake up settings", wakeUpSetting);

                    Log.d(LOG_TAG,"gino: "+ wakeUpSetting);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());

            } else if (result.contains("Door Lock Operation Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
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

            } else if (result.contains("Door Lock Configuration Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    String nodeId = jsonObject.optString("Node id");
                    String doorLockMode = jsonObject.optString("Door Lock op mode");
                    String outsideMode = jsonObject.optString("Outside Door mode");
                    String insideMode = jsonObject.optString("Inside Door mode");

                    message.put("MessageType", "Door Lock Configuration Report");
                    message.put("Node Id", nodeId);
                    message.put("Door Lock op mode", doorLockMode);
                    message.put("Outside Door mode", outsideMode);
                    message.put("Inside Door mode", insideMode);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());

            } else if (result.contains("Switch Color Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
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

            } else if (result.contains("Supported Color Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    String nodeId = jsonObject.optString("Node id");
                    String supportColor = jsonObject.optString("Supported Color");

                    message.put("MessageType", "Supported Color Report");
                    message.put("Node Id", nodeId);
                    message.put("Supported Color", supportColor);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());

            } else if (result.contains("Group Info Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    String nodeId = jsonObject.optString("Node id");
                    String groupId = jsonObject.optString("Group id");
                    String maxSupport = jsonObject.optString("Max Supported endpoints");
                    String groupMember = jsonObject.optString("Group members");

                    message.put("MessageType", "Group Info Report");
                    message.put("Node Id", nodeId);
                    message.put("Group id", groupId);
                    message.put("Max Supported endpoints", maxSupport);
                    message.put("Group members",groupMember);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());

            } else if (result.contains("Supported Groupings Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    String nodeId = jsonObject.optString("Node id");
                    String maxNumber = jsonObject.optString("Max number of groupings");

                    message.put("MessageType", "Supported Groupings Report");
                    message.put("Node Id", nodeId);
                    message.put("Max number of groupings", maxNumber);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());

            }  else if (result.contains("Active Groups Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    String nodeId = jsonObject.optString("Node id");
                    String currentActive = jsonObject.optString("Current active group");

                    message.put("MessageType", "Active Groups Report");
                    message.put("Node Id", nodeId);
                    message.put("Current active group", currentActive);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());

            } else if (result.contains("Notification Get Information")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    String nodeId = jsonObject.optString("Node id");
                    String status = jsonObject.optString("Notification-status");
                    String type = jsonObject.optString("Notification-type");
                    String event = jsonObject.optString("Notification-event");

                    message.put("MessageType", "Notification Get Information");
                    message.put("Node Id", nodeId);
                    message.put("Notification-status", status);
                    message.put("Notification-type", type);
                    message.put("Notification-event",event);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());

            } else if (result.contains("Notification Supported Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
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

            } else if (result.contains("Supported Notification Event Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
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

            } else if (result.contains("Central Scene Supported Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
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

            } else if (result.contains("Central Scene Notification")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
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

            } else if (result.contains("Firmware Info Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
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

            } else if (result.contains("Firmware Update Status Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    String nodeId = jsonObject.optString("Node id");
                    String status = jsonObject.optString("Update status");

                    message.put("MessageType", "Firmware Update Status Report");
                    message.put("Node Id", nodeId);
                    message.put("Update status", status);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());

            } else if (result.contains("Firmware Update Completion Status Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    String nodeId = jsonObject.optString("Node id");
                    String status = jsonObject.optString("Update status");

                    message.put("MessageType", "Firmware Update Completion Status Report");
                    message.put("Node Id", nodeId);
                    message.put("Update status", status);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());

            } else if (result.contains("Firmware Update restart Status Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    String nodeId = jsonObject.optString("Node id");
                    String status = jsonObject.optString("Restart status");

                    message.put("MessageType", "Firmware Update restart Status Report");
                    message.put("Node Id", nodeId);
                    message.put("Restart status", status);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());

            } else if (result.contains("Sensor Info Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    String nodeId = jsonObject.optString("Node id");
                    String precision = jsonObject.optString("type");
                    String type = jsonObject.optString("precision");
                    String unit = jsonObject.optString("unit");
                    String value = jsonObject.optString("value");

                    message.put("MessageType", "Sensor Info Report");
                    message.put("Node Id", nodeId);
                    message.put("type", type);
                    message.put("precision", precision);
                    message.put("unit",unit);
                    message.put("value",value);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());

            } else if (result.contains("Command Queue State Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    String nodeId = jsonObject.optString("Node id");
                    String state = jsonObject.optString("command state");


                    message.put("MessageType", "Command Queue State Report");
                    message.put("Node Id", nodeId);
                    message.put("command state", state);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());

            } else if (result.contains("Command Queue Info Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    String nodeId = jsonObject.optString("Node id");
                    String queue = jsonObject.optString("command queue");

                    message.put("MessageType", "Command Queue Info Report");
                    message.put("Node Id", nodeId);
                    message.put("command queue", queue);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());

            } else if (result.contains("Network Health Check")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    String status = jsonObject.optString("Status");

                    message.put("MessageType", "Network Health Check");
                    message.put("Status", status);

                    Log.d(LOG_TAG,"gino: "+ status);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());

            } else if (result.contains("Network IMA Info Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
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

                    Log.d(LOG_TAG,"gino: "+ nodeid);
                    Log.d(LOG_TAG,"gino: "+ health);
                    Log.d(LOG_TAG,"gino: "+ number);
                    Log.d(LOG_TAG,"gino: "+ value);
                    Log.d(LOG_TAG,"gino: "+ channel);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                publishMessage(Const.PublicTopicName, message.toString());

            } else if (result.contains("Network RSSI Info Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    String channel1 = jsonObject.optString("Value of channel 1");
                    String channel2 = jsonObject.optString("Value of channel 2");

                    message.put("MessageType", "Network RSSI Info Report");
                    message.put("Value of channel 1", channel1);
                    message.put("Value of channel 2", channel2);

                    Log.d(LOG_TAG,"gino: "+ channel1);
                    Log.d(LOG_TAG,"gino: "+ channel1);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                publishMessage(Const.PublicTopicName, message.toString());

            } else if (result.contains("Provision List Report")) {
                if(result.contains("Error")) {
                    try {
                        message.put("MessageType", "Provision List Report");
                        message.put("Error", "No list entry");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    publishMessage(Const.PublicTopicName, message.toString());
                } else {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(result);
                        String dsk = jsonObject.optString("DSK");
                        String type = jsonObject.optString("Device type info");
                        String id = jsonObject.optString("Device id info");
                        String bootMode = jsonObject.optString("Device Boot Mode");
                        String state = jsonObject.optString("Device Inclusion state");
                        String location = jsonObject.optString("Device Location");
                        String name = jsonObject.optString("Device Name");

                        message.put("MessageType", "Provision List Report");
                        message.put("DSK", dsk);
                        message.put("Device type info", type);
                        message.put("Device id info", id);
                        message.put("Device Boot Mode", bootMode);
                        message.put("Device Inclusion state", state);
                        message.put("Device Location", location);
                        message.put("Device Name", name);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    publishMessage(Const.PublicTopicName, message.toString());
                }

            } else if (result.contains("All Provision List Report")) {
                if(result.contains("Error")) {
                    try {
                        message.put("MessageType", "All Provision List Report");
                        message.put("Error", "No list entry");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    publishMessage(Const.PublicTopicName, message.toString());
                }

                else {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(result);
                        String list = jsonObject.optString("Detial provision list");

                        message.put("MessageType", "All Provision List Report");
                        message.put("Detial provision list", list);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    publishMessage(Const.PublicTopicName, message.toString());
                }


            } else if (result.contains("Controller DSK Report")) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    String dsk = jsonObject.optString("DSK");

                    message.put("MessageType", "Controller DSK Report");
                    message.put("DSK", dsk);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                publishMessage(Const.PublicTopicName, message.toString());

            }

            else {

                try {
                    Log.i(LOG_TAG,"result = "+result);
                    message = new JSONObject(result);
                    if (result.contains("NodeId")) {
                        String devType = message.getString("devType");
                        String NodeId = message.getString("NodeId");
                        publishMessage(Const.PublicTopicName + devType + NodeId, result);
                    }
                    else if (result.contains("devType") && result.contains("Node id")){
                        String devType = message.getString("devType");
                        String NodeId = message.getString("Node id");
                        publishMessage(Const.PublicTopicName + devType + NodeId, result);
                    } else {

                        publishMessage(Const.PublicTopicName, result);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void reNameDevice(String result) {
        try {
            result = result.substring(13);
            JSONObject message = new JSONObject(result);
            String devType = message.getString("devType");
            String NodeId = message.getString("NodeId");
            Log.i(LOG_TAG, "devType=" + devType + " NodeId=" + NodeId);

            publishMessage(Const.PublicTopicName + devType + NodeId, result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getDeviceInfo(String result, JSONObject message) {
        Log.i(LOG_TAG, "getDeviceInfo");
        ArrayList<String> tmpLine = Utils.searchString(result, "Node id");

        for (int idx = 1; idx < tmpLine.size(); idx++) {
            Log.i(LOG_TAG, "Node id (" + idx + ") = " + tmpLine.get(idx));
            subscribeToTopic(Const.PublicTopicName + "Zwave" + tmpLine.get(idx));
        }
        //publish result to MQTT public topic
        publishMessage(Const.PublicTopicName, result);
        Log.i(LOG_TAG, "node cnt = " + DeviceInfo.localSubTopiclist.size());
        if (!DeviceInfo.isZwaveInitFinish) {
            Log.i(LOG_TAG, " ===== isZwaveInitFinish  = true ====");
            DeviceInfo.isZwaveInitFinish = true;
        }

        String[] resultSplit = result.split(",");
        for(int i = 10; i < resultSplit.length; i++) { //i =10 no display security Status of Controller
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

    private void addRemoveDevice(String className, String result, JSONObject message) {
        mTCPServer.sendMessage(Const.TCPClientPort, result); //TCP format
        Log.i(LOG_TAG,"gino result :   " +result);

        if (result.contains("addDevice:") || result.contains("removeDevice:")) {
            String[] tokens = result.split(":");
            if (tokens.length < 3) {
                Log.i(LOG_TAG, "AIDLResult " + className + " : wrong format " + result);
            }
            else
            {
                String devType = tokens[1];
                String tNodeId = tokens[2];

                if (className.equals("addDevice")) {
                    try {
                        message.put("Interface", "addDevice");
                        message.put("NodeId", tNodeId);
                        message.put("Result", "true");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    subscribeToTopic(Const.PublicTopicName  + tNodeId);
                    publishMessage(Const.PublicTopicName, message.toString());
                } else {
                    try {
                        message.put("Interface", "removeDevice");
                        message.put("NodeId", tNodeId);
                        if (tNodeId.equals("fail")){
                            message.put("Result", "fail");
                        }else {
                            unsubscribeTopic(Const.PublicTopicName + devType + tNodeId);
                            message.put("Result", "true");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    publishMessage(Const.PublicTopicName, message.toString());
                }

                Const.TCPClientPort = 0;
            }
        }


    }

    private void grantKey(String result) {
        if (result.contains("Grant Keys Msg")) {
            Log.d(LOG_TAG,result);
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(result);
                String keyValue = jsonObject.optString("Keys");
                mTCPServer.sendMessage(Const.TCPClientPort, "GrantKeys:"+keyValue); //TCP format
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Const.TCPClientPort = 0;
        }
    }

    private void networkRole(String result, JSONObject message) {

        String networkRole = "";

        if(result.contains("Primary")) {
            networkRole = "Primary role";
        } else if(result.contains("SIS")) {
            networkRole = "SIS role";
        } else if(result.contains("Include")) {
            networkRole = "Include role";
        }

        mTCPServer.sendMessage(Const.TCPClientPort, "Network Role:"+ networkRole); //TCP format
        Const.TCPClientPort = 0;

    }
}


