package com.askey.firefly.zwave.control.mqtt;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.askey.firefly.zwave.control.net.TCPServer;
import com.askey.firefly.zwave.control.net.UDPConnectin;
import com.askey.firefly.zwave.control.service.ZwaveControlService;
import com.askey.firefly.zwave.control.utils.Const;
import com.askey.firefly.zwave.control.utils.DeviceInfo;
import com.askey.firefly.zwave.control.utils.Utils;
//import com.askey.zwave.control.IZwaveContrlCallBack;
//import com.askey.zwave.control.IZwaveControlInterface;

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

import java.io.IOException;
import java.util.ArrayList;

public class MQTTBroker extends Service {

    private static String LOG_TAG = MQTTBroker.class.getSimpleName();

    MqttAndroidClient mqttLocalClient,mqttRemoteClient;

    private UDPConnectin uDPConnecting = new UDPConnectin(this);
    private Server mqttServer = new Server();

    //TCP server
    private TCPServer mTCPServer;

    //AIDL
    //IZwaveControlInterface zwaveService;
    public ZwaveControlService zwaveService;

    @Override
    public void onCreate() {

        super.onCreate();

        try {
            mqttServer.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean bResult = uDPConnecting.startReceiver();
        Log.i("MQTTClient","UDP server = [" + bResult +"]");
        Log.i("MQTTClient","MQTT Local Server = [" + mqttServer.getServerStatus() +"]");

        //serverusername="";
        //serverpassword="";

        /*   set MQTT broker parmeter  */
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(true);
        //mqttConnectOptions.setUserName(serverusername);
        //mqttConnectOptions.setPassword(serverpassword.toCharArray());

        /*  connect to remote mqtt server */
        mqttRemoteConnect(mqttConnectOptions);

        /*  connect to local mqtt server */
        mqttLocalConnect(mqttConnectOptions);

        /*  launch tcp server and handle the tcp message */
        Log.i(LOG_TAG,"TCPServer = [" + handleTCPMessage() +"]");

        //bind service with ZwaveControlService
        Intent serviceIntent = new Intent(this, ZwaveControlService.class);
        this.bindService(serviceIntent, ZWserviceConn, Context.BIND_AUTO_CREATE);
        //Log.i(LOG_TAG, "AIDL status = [" + bbindResult + "]");

    }

    @Override
    public void onDestroy(){
        Log.i(LOG_TAG,"===== MQTTBroker onDestroy =====");
        super.onDestroy();

        unsubscribeTopic(Const.PublicTopicName);

        TCPServer.close();

            if (zwaveService!=null) {
                zwaveService.closeController();
                //zwaveService.unRegisterListener(ZWCtlCB);
            }

        unbindService(ZWserviceConn);

        try {
            if(mqttLocalClient.isConnected()){
                mqttLocalClient.disconnect();
            }
            if(mqttRemoteClient.isConnected()){
                mqttRemoteClient.disconnect();
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }

        uDPConnecting.stopConn();
        if (mqttServer.getServerStatus()) {
            Log.i("MQTTClient", "mqttServer.stopServer()");
            mqttServer.stopServer();
        }

        zwaveService.unregister(ZWCtlCB);
        Log.i(LOG_TAG,"===== MQTTBroker endof onDestroy =====");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // launch tcp server and handle the tcp message
    // creates the object OnMessageReceived asked by the TCPServer constructor
    private boolean handleTCPMessage(){

        mTCPServer = new TCPServer(new TCPServer.OnMessageReceived() {
            @Override
            //TCPServer class (at while)
            public void messageReceived(int clientID,String message) {

                Log.i(LOG_TAG, "TCP received , client ID = " + clientID + " |  message : " + message);
                if (message.contains("mobile_zwave")) {
                    if (message.contains("addDevice")) {
                        if (Const.TCPClientPort != 0) {
                            mTCPServer.sendMessage(clientID, Const.TCPSTRING + "addDevice:other");
                        } else {

                            Const.TCPClientPort = clientID;
                            Log.i(LOG_TAG, "zwaveService.addDevice(mCallback)");
                            zwaveService.addDevice();
                        }
                    }
                    else if (message.contains("removeDevice")) {
                        if (Const.TCPClientPort != 0) {
                            mTCPServer.sendMessage(clientID, Const.TCPSTRING + "removeDevice:other");
                        } else {

                            Const.TCPClientPort = clientID;
                            Log.i(LOG_TAG, "zwaveService.removeDevice(mCallback)");
                            zwaveService.removeDevice();
                        }
                    }
                    else if (message.contains("openController")) {

                        if (zwaveService!=null) {
                            Const.TCPClientPort = clientID;
                            Log.i(LOG_TAG, "[" + Const.TCPClientPort + "]zwaveService.openController(mCallback)");
                            zwaveService.openController();
                        }
                    }
                    else if (message.contains("removeFailedDevice")) {

                        String[] tokens = message.split(":");
                        if (tokens.length > 2) {
                            int tNodeId = Integer.parseInt(tokens[2]);
                            Log.i(LOG_TAG, "zwaveService.removeFailedDevice(mCallback, " + tNodeId + ")");
                            zwaveService.removeFailedDevice(tNodeId);
                        }
                    }
                    else if (message.contains("replaceFailedDevice")) {

                        String[] tokens = message.split(":");
                        if (tokens.length > 2) {

                            int tNodeId = Integer.parseInt(tokens[2]);
                            Log.i(LOG_TAG, "zwaveService.replaceFailedDevice(mCallback, " + tNodeId + ")");
                            zwaveService.replaceFailedDevice(tNodeId);
                        }
                    }
                    else if (message.contains("stopAddDevice")) {

                        Log.i(LOG_TAG, "zwaveService.stopAddDevice(mCallback)");
                        zwaveService.stopAddDevice();
                    }
                    else if (message.contains("stopRemoveDevice")) {

                        Log.i(LOG_TAG, "zwaveService.stopRemoveDevice(mCallback)");
                        zwaveService.stopRemoveDevice();
                    }
                    else if (message.contains("reNameDevice")) {

                        if (Const.TCPClientPort != 0) {
                            mTCPServer.sendMessage(clientID, Const.TCPSTRING + "reNameDevice:other");
                        }
                        else {

                            String[] tokens = message.split(":");
                            if (tokens.length > 4) {
                                Const.TCPClientPort = clientID;
                                String tHomeId = tokens[2];
                                int tDeviceId = Integer.parseInt(tokens[3]);
                                String tNewName = tokens[4];

                                Log.i(LOG_TAG, "zwaveService.reNameDevice(mCallback,"+tHomeId+","+tDeviceId+","+tNewName+")");
                                zwaveService.reNameDevice(tHomeId,tDeviceId,tNewName,"");

                            }
                        }
                    }
                    else if (message.contains("setDefault")) {

                        if (Const.TCPClientPort != 0) {
                            mTCPServer.sendMessage(clientID, Const.TCPSTRING + "setDefault:other");
                        }else {
                            Const.TCPClientPort = clientID;

                            Log.i(LOG_TAG, "zwaveService.setDefault(mCallback)");
                            zwaveService.setDefault();
                        }
                    }
                    else if (message.contains("closeController")) {

                        Log.i(LOG_TAG, "zwaveService.closeController(mCallback)");
                        zwaveService.closeController();
                    }
                    else {
                        mTCPServer.sendMessage(clientID, Const.TCPSTRING + " Wrong Payload");
                    }
                }
            }
        });
        mTCPServer.start();
        return mTCPServer.isAlive();
    }


    //***** connect to remote mqtt server *****
    private void mqttRemoteConnect(MqttConnectOptions mqttConnectOptions){

        mqttRemoteClient = new MqttAndroidClient(getApplicationContext(), Const.remoteMQTTServerUri, Const.mqttClientId);

        try {
            Log.i(LOG_TAG," RemoteMClient status = " + "[connecting...]");
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
                    Log.i(LOG_TAG," RemoteMClient status = " + "[failed to connect]");
                }
            });

            mqttRemoteClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    if (reconnect) {
                        Log.i(LOG_TAG," RemoteMClient status = " + "[reconnected]");
                        syncSubscribeTopic();
                    } else {
                        Log.i(LOG_TAG," RemoteMClient status = " + "[connected]");
                        subscribeToTopic(Const.PublicTopicName);
                    }
                }

                @Override
                public void connectionLost(Throwable cause) {
                    Log.i(LOG_TAG," RemoteMClient status = " + "[connection was lost]");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String mqttMessage = new String(message.getPayload());
                    Log.i(LOG_TAG,"Remote MQTT Incoming [" + topic +"] : "+ mqttMessage);

                    if (zwaveService!=null){
                        handleMqttIncomingMessage(topic,mqttMessage);
                    }else{
                        Log.e(LOG_TAG, "zwaveService is null");
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
    private void mqttLocalConnect(MqttConnectOptions mqttConnectOptions){

        Log.i(LOG_TAG,"local mqtt server ip = ["+ Const.localMQTTServerUri +"]");

        mqttLocalClient = new MqttAndroidClient(getApplicationContext(), Const.localMQTTServerUri, Const.mqttClientId);

        try {
            Log.i(LOG_TAG," LocalMClient status = " + "[connecting....]");
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
                    Log.i(LOG_TAG," LocalMClient status = " + "[failed to connect]");
                }
            });

            mqttLocalClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    if (reconnect) {
                        Log.i(LOG_TAG," LocalMClient status = " + "[reconnected]");
                    } else {
                        Log.i(LOG_TAG," LocalMClient status = " + "[connected]");
                        subscribeToTopic(Const.PublicTopicName);
                    }
                }

                @Override
                public void connectionLost(Throwable cause) {
                    Log.i(LOG_TAG," LocalMClient status = " + "[connection was lost]");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String mqttMessage = new String(message.getPayload());
                    mqttMessage = mqttMessage.replaceAll("\n","");
                    Log.i(LOG_TAG,"Local MQTT Incoming [" + topic +"] : "+ mqttMessage);

                    if (zwaveService!=null){
                        handleMqttIncomingMessage(topic,mqttMessage);
                    }else{
                        Log.e(LOG_TAG, "zwaveService is null");
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

    private void handleMqttIncomingMessage(String TopicName, String mqttMessage) {
        //send aidl message to zwave control app
        if (mqttMessage.contains("getDevices")) {
            Log.i(LOG_TAG, "zwaveService.getDevices()");
            zwaveService.getDevices();

        } else {

            String[] tokens = TopicName.split(Const.PublicTopicName);
            int tNodeid = Integer.parseInt(tokens[1]);

            if (mqttMessage.contains("getStatus")) {

                Log.i(LOG_TAG, "zwaveService.getDeviceInfo(mCallback," + tNodeid + ")");
                zwaveService.getDeviceInfo(tNodeid);
            } else if (mqttMessage.contains("getDeviceBattery")) {

                Log.i(LOG_TAG, "zwaveService.getDeviceBattery(mCallback," + tNodeid + ")");
                zwaveService.getDeviceBattery(tNodeid);
            } else if (mqttMessage.contains("getSensorMultiLevel")) {

                Log.i(LOG_TAG, "zwaveService.getSensorMultiLevel(mCallback," + tNodeid + ")");
                try {
                    zwaveService.getSensorMultiLevel(tNodeid);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else if (mqttMessage.contains("updateNode")) {

                Log.i(LOG_TAG, "zwaveService.updateNode(mCallback," + tNodeid + ")");
                zwaveService.updateNode(tNodeid);
            } else if (mqttMessage.contains("getConfiguration")) {

                tokens = mqttMessage.split(":");
                if (tokens.length > 7) {
                    String tHomeid = tokens[2];
                    int tParamMode = Integer.parseInt(tokens[4]);
                    int tParamNumber = Integer.parseInt(tokens[5]);
                    int tRangeStart = Integer.parseInt(tokens[6]);
                    int tRrangeEnd = Integer.parseInt(tokens[7]);

                    Log.i(LOG_TAG, "zwaveService.getConfiguration(mCallback," + tHomeid + "," + tNodeid + ","
                                + tParamMode + "," + tParamNumber + "," + tRangeStart + "," + tRrangeEnd + ")");
                        zwaveService.getConfiguration(tHomeid, tNodeid,
                                tParamMode, tParamNumber, tRangeStart, tRrangeEnd);

                }
            } else if (mqttMessage.contains("setConfiguration")) {

                tokens = mqttMessage.split(":");
                if (tokens.length > 7) {
                    String tHomeid = tokens[2];
                    int tParamMode = Integer.parseInt(tokens[4]);
                    int tParamNumber = Integer.parseInt(tokens[5]);
                    int tRangeStart = Integer.parseInt(tokens[6]);
                    int tRrangeEnd = Integer.parseInt(tokens[7]);

                    Log.i(LOG_TAG, "zwaveService.setConfiguration(mCallback," + tHomeid + "," + tNodeid + ","
                                + tParamMode + "," + tParamNumber + "," + tRangeStart + "," + tRrangeEnd + ")");
                    try {
                        zwaveService.setConfiguration(tHomeid, tNodeid,
                                tParamMode, tParamNumber, tRangeStart, tRrangeEnd);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            } else if (mqttMessage.contains("getSupportedSwitchType")) {

                Log.i(LOG_TAG, "zwaveService.getSupportedSwitchType(mCallback," + tNodeid + ")");
                zwaveService.getSupportedSwitchType(tNodeid);
            } else if (mqttMessage.contains("startStopSwitchLevelChange")) {

                tokens = mqttMessage.split(":");
                if (tokens.length > 8) {
                    String tHomeid = tokens[2];
                    int tstartLvlVal = Integer.parseInt(tokens[4]);
                    int tduration = Integer.parseInt(tokens[5]);
                    int tpmyChangeDir = Integer.parseInt(tokens[6]);
                    int tsecChangeDir = Integer.parseInt(tokens[7]);
                    int tsecStep = Integer.parseInt(tokens[8]);

                    Log.i(LOG_TAG, "zwaveService.startStopSwitchLevelChange(mCallback," + tHomeid + "," + tNodeid + ","
                            + tstartLvlVal + "," + tduration + "," + tpmyChangeDir + "," + tsecChangeDir + "," + tsecStep + ")");
                    zwaveService.startStopSwitchLevelChange(tHomeid, tNodeid,
                            tstartLvlVal, tduration, tpmyChangeDir, tsecChangeDir, tsecStep);
                }
            } else if (mqttMessage.contains("getPowerLevel")) {

                Log.i(LOG_TAG, "zwaveService.getPowerLevel(mCallback," + tNodeid + ")");
                zwaveService.getPowerLevel(tNodeid);
            } else if (mqttMessage.contains("setSwitchAllOn")) {

                Log.i(LOG_TAG, "zwaveService.setSwitchAllOn(mCallback," + tNodeid + ")");
                zwaveService.setSwitchAllOn(tNodeid);
            } else if (mqttMessage.contains("setSwitchAllOff")) {

                Log.i(LOG_TAG, "zwaveService.setSwitchAllOff(mCallback," + tNodeid + ")");
                zwaveService.setSwitchAllOff(tNodeid);
            } else if (mqttMessage.contains("getBasic")) {

                Log.i(LOG_TAG, "zwaveService.getBasic(mCallback," + tNodeid + ")");
                zwaveService.getBasic(tNodeid);
            } else if (mqttMessage.contains("setBasic")) {

                tokens = mqttMessage.split(":");
                if (tokens.length > 2) {
                    int tValue = Integer.parseInt(tokens[2]);

                    Log.i(LOG_TAG, "zwaveService.setBasic(mCallback," +tNodeid+ "," +tValue+ ")");
                    zwaveService.setBasic(tNodeid,tValue);
                }
            } else if (mqttMessage.contains("getSwitchMultiLevel")) {

                Log.i(LOG_TAG, "zwaveService.getSwitchMultiLevel(mCallback," + tNodeid + ")");
                zwaveService.getSwitchMultiLevel(tNodeid);
            } else if (mqttMessage.contains("setSwitchMultiLevel")) {

                tokens = mqttMessage.split(":");
                if (tokens.length > 4) {
                    int tValue = Integer.parseInt(tokens[3]);
                    int tDuration = Integer.parseInt(tokens[4]);
                    Log.i(LOG_TAG, "zwaveService.setSwitchMultiLevel(mCallback," +tNodeid+ "," +tValue+ ","+tDuration+")");
                    zwaveService.setSwitchMultiLevel(tNodeid,tValue,tDuration);
                }
            }
        }
    }

    // Synchronize the subscribe topic of local mqtt server with remote mqtt server
    private void syncSubscribeTopic(){

        for (int idx = 0; idx< DeviceInfo.localSubTopiclist.size(); idx++) {
            if (!DeviceInfo.remoteSubTopiclist.contains(DeviceInfo.localSubTopiclist.get(idx))) {
                subscribeToTopic(DeviceInfo.localSubTopiclist.get(idx));
            }
        }

        for (int idx=0;idx<DeviceInfo.remoteSubTopiclist.size();idx++) {
            if (!DeviceInfo.localSubTopiclist.contains(DeviceInfo.remoteSubTopiclist.get(idx))) {
                unsubscribeTopic(DeviceInfo.localSubTopiclist.get(idx));
            }
        }
    }

    // subscribe mqtt topic
    private void subscribeToTopic(final String TopicName) {
        //Log.i(LOG_TAG,"mqttLocalClient.isConnected() = "+mqttLocalClient.isConnected());
        Log.i(LOG_TAG,"mqttRemoteClient.isConnected() = "+mqttRemoteClient.isConnected());

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
            }else{
                Log.i(LOG_TAG,"remote mqtt server is disconnect...");
            }
        }
    }

    // unsubscribe mqtt topic
    private void unsubscribeTopic(final String TopicName){
        int idx;
        Log.i(LOG_TAG,"unsubscribeTopic : "+TopicName);
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
            if (DeviceInfo.remoteSubTopiclist.contains(TopicName)) {
                if (mqttRemoteClient.isConnected()) {
                    mqttRemoteClient.unsubscribe(TopicName);
                    idx = DeviceInfo.remoteSubTopiclist.indexOf(TopicName);
                    if (idx >= 0) {
                        DeviceInfo.remoteSubTopiclist.remove(idx);
                    }
                }
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }

        for (idx = 0 ; idx< DeviceInfo.localSubTopiclist.size(); idx++){
            Log.i(LOG_TAG,"localSubTopiclist["+idx+"] = "+DeviceInfo.localSubTopiclist.get(idx));
        }
        for (idx = 0 ; idx< DeviceInfo.remoteSubTopiclist.size(); idx++){
            Log.i(LOG_TAG,"remoteSubTopiclist["+idx+"] = "+DeviceInfo.remoteSubTopiclist.get(idx));
        }
    }

    // publish message to mqtt server
    private void publishMessage(String publishTopic, String publishMessage) {

        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(publishMessage.getBytes());
            Log.i(LOG_TAG,publishTopic + ":" + publishMessage);
            if (mqttLocalClient.isConnected()) {
                mqttLocalClient.publish(publishTopic, message);
            } else {
                Log.i(LOG_TAG,"[LocalMqttClient] fail to connect local mqtt server");
                //Log.i(LOG_TAG,"[localmqtt]"+mqttLocalClient.getBufferedMessageCount() + " messages in buffer.");
            }
            if (mqttRemoteClient.isConnected()) {
                mqttRemoteClient.publish(publishTopic, message);
            } else {
                //Log.i(LOG_TAG,"[remoteMqtt]"+mqttRemoteClient.getBufferedMessageCount() + " messages in buffer.");
                Log.i(LOG_TAG,"[LocalMqttClient] fail to connect local mqtt server");
            }
        } catch (MqttException e) {
            System.err.println("MQTT Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // AIDL
    private ServiceConnection ZWserviceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            zwaveService = ((ZwaveControlService.MyBinder)service).getService();
            if (zwaveService != null){

                Log.i(LOG_TAG,"bind service with ZWaveControlService");
                zwaveService.register(ZWCtlCB);

                new Thread(new Runnable() {
                        @Override
                        public void run() {
                        String openResult = zwaveService.openController();
                        if (openResult.contains(":0")){
                            DeviceInfo.isOpenControllerFinish = true;
                        }

                        for (int idx = 0;idx<250;idx++){
                            if (DeviceInfo.isMQTTInitFinish == true && DeviceInfo.isOpenControllerFinish == true){
                                break;
                            }
                            try {
                                //Log.i(LOG_TAG,"idx = "+idx+"|isOpenControllerFinish = "+DeviceInfo.isOpenControllerFinish);
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        zwaveService.getDevices();
                        }
                    }).start();

            }else{
                Log.i(LOG_TAG,"Failed to bind service with ZWaveControlService");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private void handleNormalCallback(String tMethodName, String tResult){

        ArrayList<String>  NodeId = Utils.searchString(tResult, "Node id");

        for( int idx = 0; idx < NodeId.size(); idx++){
            Log.i(LOG_TAG,tMethodName+" Node id = "+NodeId.get(idx));
            publishMessage(Const.PublicTopicName+NodeId.get(idx),tResult);
        }
    }

    // AIDLCallBack

    public ZwaveControlService.zwaveCallBack ZWCtlCB = new ZwaveControlService.zwaveCallBack() {

        @Override
        public void zwaveControlResultCallBack(String className, String result) {

            Log.i(LOG_TAG,"AIDLResult " +className+ " ["+Const.TCPClientPort+"]: "+result);

            if (className.equals("addDevice") || className.equals("removeDevice")){

                mTCPServer.sendMessage(Const.TCPClientPort,result);

                if (result.contains("Success") || result.contains("Failed")){
                    Const.TCPClientPort =0;
                }

                if (result.contains("addDevice:") || result.contains("removeDevice:")){
                    String[] tokens = result.split(":");
                    if (tokens.length<3){
                        Log.i(LOG_TAG,"AIDLResult " +className+" : wrong format "+result);
                    } else {
                        //String tHomeId = tokens[1];
                        String tNodeId = tokens[2];
                        if (className.equals("addDevice")) {
                            subscribeToTopic(Const.PublicTopicName + tNodeId);
                            publishMessage(Const.PublicTopicName, result);
                        }else{
                            publishMessage(Const.PublicTopicName, "removeDevice:" + tNodeId);
                            unsubscribeTopic(Const.PublicTopicName + tNodeId);
                        }
                    }
                }

            } else if (className.equals("getDeviceList")){

                ArrayList<String> tmpLine = Utils.searchString(result, "Node id");

                for( int idx = 1; idx < tmpLine.size(); idx++){
                    Log.i(LOG_TAG,"Node id ("+idx+") = "+tmpLine.get(idx));
                    subscribeToTopic(Const.PublicTopicName+tmpLine.get(idx));
                }
                //publish result to MQTT public topic
                publishMessage(Const.PublicTopicName,result);

                Log.i(LOG_TAG,"node cnt = "+DeviceInfo.localSubTopiclist.size());
                if (!DeviceInfo.isZwaveInitFinish) {
                    Log.i(LOG_TAG, " ===== isZwaveInitFinish  = true ====");
                    DeviceInfo.isZwaveInitFinish = true;
                }

            }else if ( className.equals("openController")|| className.equals("removeFailedDevice") ||
                className.equals("replaceFailedDevice")|| className.equals("stopAddDevice") ||
                className.equals("stopRemoveDevice") ){

                mTCPServer.sendMessage(Const.TCPClientPort,result);
                Const.TCPClientPort = 0;

            }else{
                handleNormalCallback(className,result);
            }

        }
    };
}


