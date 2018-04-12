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

        //serverusername="";
        //serverpassword="";

        /*   set MQTT broker parmeter  */
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(true);
        //mqttConnectOptions.setUserName(serverusername);
        //mqttConnectOptions.setPassword(serverpassword.toCharArray());

        /*  connect to remote mqtt server */
        if (Const.remoteMqttFlag) {
            mqttRemoteConnect(mqttConnectOptions);
        }

        /*  connect to local mqtt server */
        mqttLocalConnect(mqttConnectOptions);

        /*  launch tcp server and handle the tcp message */
        Log.i(LOG_TAG, "TCPServer = [" + handleTCPMessage() + "]");

        //bind service with ZwaveControlService
        Intent serviceIntent = new Intent(this, ZwaveControlService.class);
        this.bindService(serviceIntent, ZWserviceConn, Context.BIND_AUTO_CREATE);
        //Log.i(LOG_TAG, "AIDL status = [" + bbindResult + "]");

        // init zwSceneManager and zwDevManager
        zwSceneManager = ZwaveDeviceSceneManager.getInstance(this);
        zwDevManager = ZwaveDeviceManager.getInstance(this);
    }

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
                    } else if (message.contains("removeFailedDevice")) {

                        String[] tokens = message.split(":");
                        if (tokens.length > 2) {
                            int tNodeId = Integer.parseInt(tokens[2]);
                            Log.i(LOG_TAG, "deviceService.removeFailedDevice(mCallback, " + tNodeId + ")");
                            zwaveService.removeFailedDevice(tNodeId);
                        }
                    } else if (message.contains("replaceFailedDevice")) {

                        String[] tokens = message.split(":");
                        if (tokens.length > 2) {

                            int tNodeId = Integer.parseInt(tokens[2]);
                            Log.i(LOG_TAG, "deviceService.replaceFailedDevice(mCallback, " + tNodeId + ")");
                            zwaveService.replaceFailedDevice(tNodeId);
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
                    } else if (message.contains("setDefault")) {

                        Const.TCPClientPort = clientID;

                        Log.i(LOG_TAG, "deviceService.setDefault(mCallback)");
                        zwaveService.setDefault();

                    } else {
                        mTCPServer.sendMessage(clientID, Const.TCPSTRING + " Wrong Payload");  //TCP format
                    }
                } else if (message.contains("GrantKeys")) {
                    String[] tmp = message.split(":");
                    if (tmp[1].equals("87")) {
                        mTCPServer.sendMessage(clientID, "dsk:1"); //TCP format
                    }
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
        String devType = null;
        int value = 0;

        if (!TopicName.equals(Const.PublicTopicName)) {
            if (tokens[1].contains("Zwave")) {
                devInfo = tokens[1].split("Zwave");
                devType = "Zwave";
                currentNodeId = Integer.valueOf(devInfo[1]);
                Log.d(LOG_TAG,"tNodeid: "+currentNodeId);
            }
            /*
            else if (tokens[1].contains("BT")) {
                devInfo = tokens[1].split("BT");
                devType = "BT";
                tNodeid = Integer.parseInt(devInfo[1]);
            }
            */
        }

        JSONObject payload = new JSONObject(mqttMessage);

        if (mqttMessage.contains("function")) {
            String function = payload.getString("function");

            switch (function) {
                case "addDevice":
                    Log.i(LOG_TAG, "deviceService.removeDeviceFromRoom");
                    zwaveService.addDevice("Zwave");
                    break;

                case "removeDeviceFromRoom":
                    Log.i(LOG_TAG, "deviceService.removeDeviceFromRoom");
                    zwaveService.removeDeviceFromRoom(currentNodeId);
                    break;

                case "getDeviceList": //public channel
                    String tRoom = payload.getString("Room");
                    Log.i(LOG_TAG, "deviceService.getDevices tRoom=" + tRoom);
                    zwaveService.getDeviceList(tRoom);
                    break;

                case "editNodeInfo":
                    Log.i(LOG_TAG, "deviceService.editNodeInfo payload:" + payload);

                    String Room = payload.getJSONObject("parameter").getString("Room");
                    String isFavorite = payload.getJSONObject("parameter").getString("isFavorite");
                    String name = payload.getJSONObject("parameter").getString("name");
                    String type = payload.getJSONObject("parameter").getString("type");

                    zwaveService.editNodeInfo("", currentNodeId, name, devType, type, Room, isFavorite);
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
                    zwaveService.getBasic(devType, currentNodeId);
                    break;

                case "setBasic":
                    String switchStatus = payload.getString("value");
                    Log.i(LOG_TAG, "deviceService.setSwitchStatus value=" + value);
                    zwaveService.setBasic(devType, currentNodeId, Integer.parseInt(switchStatus));
                    break;

                case "getSwitchMultilevel":
                    Log.i(LOG_TAG, "deviceService.getBrightness");
                    zwaveService.getSwitchMultiLevel(devType, currentNodeId);
                    break;

                case "setSwitchMultilevel":
                    Log.i(LOG_TAG, "deviceService.setBrightness value=" + value);
                    value = Integer.parseInt(payload.getString("value"));
                    zwaveService.setSwitchMultiLevel(devType, currentNodeId, value, 0);
                    break;

                case "getSwitchColor":
                    Log.i(LOG_TAG, "deviceService.getLampColor");
                    zwaveService.getLampColor(devType, currentNodeId);
                    break;

                case "setSwitchColor":
                    Log.i(LOG_TAG, "deviceService.setLampColor");
                    String lampcolor = payload.getString("lampcolor");
                    Log.i(LOG_TAG,"Lampcolor = "+lampcolor);
                    switch (lampcolor) {
                        case "RGB":
                            int r_value = Integer.parseInt(payload.getString("R"));
                            int g_value = Integer.parseInt(payload.getString("G"));
                            int b_value = Integer.parseInt(payload.getString("B"));
                            zwaveService.setLampColor(devType, currentNodeId, r_value, g_value, b_value);
                            break;
                        case "warmWhite":
                            zwaveService.setLampToWarmWhite(devType, currentNodeId);
                            break;
                        case "coldWhite":
                            zwaveService.setLampToColdWhite(devType, currentNodeId);
                            break;
                        default:
                            break;
                    }
                    break;

                case "getConfiguration":
                    Log.i(LOG_TAG, "deviceService.getConfiguration");
                    int parameter = Integer.parseInt(payload.getString("parameter"));
                    zwaveService.getConfiguration(currentNodeId,0,parameter,0,0);
                    break;

                case "setConfiguration":
                    Log.i(LOG_TAG, "deviceService.setConfiguration");
                    parameter = Integer.parseInt(payload.getString("parameter"));
                    //zwaveService.setConfiguration(); many parameter
                    break;

                case "getMeter":
                    Log.i(LOG_TAG, "deviceService.getPower");
                    zwaveService.getMeter(devType, currentNodeId, 0);
                    zwaveService.getMeter(devType, currentNodeId, 2);
                    zwaveService.getMeter(devType, currentNodeId, 5);
                    break;

                case "getGroupInfo":
                    int maxGroupId = Integer.parseInt(payload.getString("maxGroupId"));
                    int EndpointId = Integer.parseInt(payload.getString("endpointId"));
                    Log.i(LOG_TAG, "deviceService.getGroupInfo max="+maxGroupId+" | endpoint="+EndpointId);

                    zwaveService.getGroupInfo(devType, currentNodeId);
                    break;

                case "addEndpointsToGroup":
                    Log.i(LOG_TAG, "deviceService.addEndpointsToGroup");

                    int GroupId = Integer.parseInt(payload.getString("groupId"));
                    EndpointId = Integer.parseInt(payload.getString("endpointId"));

                    String arr = payload.optString("arr");
                    JSONArray ja = new JSONArray(arr);
                    ArrayList<Integer> arrList = new ArrayList<>();
                    for(int j=0; j<ja.length(); j++){
                        JSONObject json = ja.getJSONObject(j);
                        arrList.add(Integer.valueOf(json.getString("controlNodeId").toString()));
                    }
                    zwaveService.addEndpointsToGroup(devType,currentNodeId,GroupId,Utils.convertIntegers(arrList),EndpointId);
                    break;

                case "removeEndpointsFromGroup":
                    Log.i(LOG_TAG, "deviceService.removeEndpointsFromGroup");

                    GroupId = Integer.parseInt(payload.getString("groupId"));
                    EndpointId = Integer.parseInt(payload.getString("endpointId"));

                    arr = payload.optString("arr");
                    ja = new JSONArray(arr);
                    arrList = new ArrayList<>();
                    for(int j=0; j<ja.length(); j++){
                        JSONObject json = ja.getJSONObject(j);
                        arrList.add(Integer.valueOf(json.getString("controlNodeId").toString()));
                    }

                    zwaveService.removeEndpointsFromGroup(devType,currentNodeId,GroupId,Utils.convertIntegers(arrList),EndpointId);
                    break;

                case "getMaxSupportedGroups":
                    Log.i(LOG_TAG, "deviceService.getMaxSupportedGroups");
                    EndpointId = Integer.parseInt(payload.getString("endpointId"));
                    zwaveService.getMaxSupportedGroups(currentNodeId,EndpointId);
                    break;

                case "setScheduleActive":
                    String active = payload.getString("active");
                    Log.i(LOG_TAG, "deviceService.setScheduleActive "+active);
                    zwaveService.setScheduleActive(devType,currentNodeId,active);
                    break;

                case "getScheduleList":
                    Log.i(LOG_TAG, "deviceService.getScheduleList");
                    zwaveService.getScheduleList(devType,currentNodeId);
                    break;

                case "removeSchedule":
                    String Day = payload.getString("dayOfWeek");
                    Log.i(LOG_TAG, "deviceService.removeSchedule " +Day);
                    zwaveService.removeSchedule(devType,currentNodeId,Day);
                    break;

                case "setSchedule":
                    Day = payload.getString("dayOfWeek");
                    active = payload.getString("active");
                    String variableValue = payload.getString("variableValue");
                    String startTime = payload.getString("StartTime");
                    String endTime = payload.getString("EndTime");
                    Log.i(LOG_TAG, "deviceService.setSchedule " +Day);
                    zwaveService.setSchedule(devType,currentNodeId,Day,startTime,endTime,Integer.valueOf(variableValue),active);
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
                    Log.i(LOG_TAG, str);
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
                    zwaveService.addProvisionListEntry("Zwave",dskNumber,true);
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
                    Log.i(LOG_TAG, "deviceService.getRssiState");
                    zwaveService.startNetworkHealthCheck();
                    break;

                case "getBattery":
                    Log.i(LOG_TAG, "deviceService.getBattery");
                    zwaveService.getDeviceBattery(devType,currentNodeId);
                    break;

                case "getSensorMultilevel":
                    Log.i(LOG_TAG, "deviceService.getSensorMultilevel");
                    try {
                        zwaveService.getSensorMultiLevel(devType,currentNodeId);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;

                case "getSupportSwitchType":
                    Log.i(LOG_TAG, "deviceService.getSupportSwitchType");
                    zwaveService.getSupportedSwitchType(currentNodeId);
                    break;

                case "startStopSwitchLevelChange":
                    Log.i(LOG_TAG, "deviceService.startStopSwitchLevelChange");
                    zwaveService.startStopSwitchLevelChange(currentNodeId,99,2,1,1,50);
                    break;

                case "getPowerLevel":
                    Log.i(LOG_TAG, "deviceService.getPowerLevel");
                    zwaveService.getPowerLevel(currentNodeId);
                    break;

                case "setSwitchAllOn":
                    Log.i(LOG_TAG, "deviceService.setSwitchAllOn");
                    zwaveService.setSwitchAllOn(devType,currentNodeId);
                    break;

                case "setSwitchAllOff":
                    Log.i(LOG_TAG, "deviceService.setSwitchAllOff");
                    zwaveService.setSwitchAllOff(devType,currentNodeId);
                    break;

                case "getSensorBinary":
                    Log.i(LOG_TAG, "deviceService.getSensorBinary");
                    //zwaveService.getSensorBasic(currentNodeId,sensorType); no sensorType
                    break;

                case "getSensorBinarySupportedSensor":
                    Log.i(LOG_TAG, "deviceService.getSensorBinarySupportedSensor");
                    zwaveService.GetSensorBinarySupportedSensor(currentNodeId);
                    break;

                case "getMeterSupported":
                    Log.i(LOG_TAG, "deviceService.getMeterSupported");
                    zwaveService.getMeterSupported(currentNodeId);
                    break;

                case "getSpecificGroup":
                    Log.i(LOG_TAG, "deviceService.getSpecificGroup");
                    EndpointId = Integer.parseInt(payload.getString("endpointId"));
                    zwaveService.getSpecificGroup(currentNodeId,EndpointId);
                    break;

                case "getNotification":
                    Log.i(LOG_TAG, "deviceService.getNotification");
                    //zwaveService.getSensorNotification(); many parameter
                    break;

                case "getSupportedNotification":
                    Log.i(LOG_TAG, "deviceService.getSupportedNotification");
                    zwaveService.getSupportedNotification(currentNodeId);
                    break;

                case "getSupportedEventNotification":
                    Log.i(LOG_TAG, "deviceService.getSupportedEventNotification");
                    // zwaveService.getSupportedEventNotification(currentNodeId,typeDef); no typeDef
                    break;

                case "getSpecifyDeviceInfo":
                    Log.i(LOG_TAG, "deviceService.getSpecifyDeviceInfo");
                    Log.i(LOG_TAG, "currentNodeId: "+currentNodeId + "GINO!!!!!");
                    zwaveService.getSpecifyDeviceInfo(currentNodeId);
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
        } catch (MqttException e) {
            e.printStackTrace();
        }

        for (idx = 0; idx < DeviceInfo.localSubTopiclist.size(); idx++) {
            Log.i(LOG_TAG, "localSubTopiclist[" + idx + "] = " + DeviceInfo.localSubTopiclist.get(idx));
        }
        if (Const.remoteMqttFlag) {
            for (idx = 0; idx < DeviceInfo.remoteSubTopiclist.size(); idx++) {
                Log.i(LOG_TAG, "remoteSubTopiclist[" + idx + "] = " + DeviceInfo.remoteSubTopiclist.get(idx));
            }
        }
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
            if (Const.remoteMqttFlag) {
                if (mqttRemoteClient.isConnected()) {
                    mqttRemoteClient.publish(publishTopic, message);
                } else {
                    Log.e(LOG_TAG, "[LocalMqttClient] fail to connect local mqtt server");
                }
            }
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
                zwaveService.register(ZWCtlReq);

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


    // ZwaveControlService CallBack

    public ZwaveControlService.zwaveControlReq_CallBack ZWCtlReq = new ZwaveControlService.zwaveControlReq_CallBack() {
        @Override
        public void zwaveControlReqResultCallBack(String className, String result) {
            Log.d(LOG_TAG,"class : "+ className + " result: "+result);
            JSONObject obj = new JSONObject();
            JSONObject message = new JSONObject();

            if (result.contains("Grant Keys Msg")) {
                grantKey(result);
                //sleep();

            } else if (result.contains("PIN Requested Msg")) {
                //sleep();

            }
        }
    };

    public ZwaveControlService.zwaveCallBack ZWCtlCB = new ZwaveControlService.zwaveCallBack() {
        @Override
        public void zwaveControlResultCallBack(String className, String result) {

            JSONObject obj = new JSONObject();
            JSONObject message = new JSONObject();

            if (className.equals("addDevice") || className.equals("removeDevice")) {
                addRemoveDevice(className,result,message);
            } else if (className.equals("getDeviceInfo")) {
                getDeviceInfo(result,message);
            } else if (className.equals("removeFailedDevice") ||
                    className.equals("replaceFailedDevice") || className.equals("stopAddDevice") ||
                    className.equals("stopRemoveDevice")) {

                mTCPServer.sendMessage(Const.TCPClientPort, result); //TCP format
                Const.TCPClientPort = 0;

            } else if (className.equals("reNameDevice")) {
                reNameDevice(result);
            } else if (result.contains("Network Role")) {
                String[] roleTmp = result.split(",");
                networkRole(roleTmp[3], message);
                Log.i(LOG_TAG,"Network Role = "+roleTmp[3]);
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

    private void getAllProvisionListEntry(String result, JSONObject message) {
        try {
            message.put("Interface", "getAllProvision2ListEntry");
            message.put("Result", result + " gino");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        publishMessage(Const.PublicTopicName, message.toString());
    }

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
        Log.i(LOG_TAG,result);

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
                        //  obj.put("reported", message);
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

            Timer timer = new Timer(true);
            timer.schedule(new mTimerTask(), 5000);

            Const.TCPClientPort = 0;

        }
    }

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2001:
                    Log.d(LOG_TAG,"grant: start---------------------");
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    DeviceInfo.reqKeyFlag = true;
                    Log.d(LOG_TAG,"grant: end---------------------");
                    break;
            }
        }
    };

    class mTimerTask extends TimerTask {
        public void run() {
            Message message = new Message();
            message.what = 2001;
            mHandler.sendMessage(message);
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
        Log.d(LOG_TAG,"Network Role:"+ networkRole + " gino!!!!!!!!!!");

        Const.TCPClientPort = 0;

    }

    private void sleep() {
        Log.i(LOG_TAG,"------------------------sleep---------------------------------");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}


