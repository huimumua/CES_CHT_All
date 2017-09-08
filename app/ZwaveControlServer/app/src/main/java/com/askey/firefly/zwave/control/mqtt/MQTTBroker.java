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

import com.askey.zwave.control.IZwaveContrlCallBack;
import com.askey.zwave.control.IZwaveControlInterface;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;

public class MQTTBroker extends Service {

    final String LOG_TAG = "MQTTClient";
    final String TCPSTRING = "firefly_zwave:";
    final String remoteServerUri ="tcp://211.75.141.112:1883";

    //UDPConnectin uDPConnecting = new UDPConnectin(this);
    //Server mqttServer = new Server();

    MqttAndroidClient mqttLocalClient,mqttRemoteClient;

    String mqttClientId = Utils.getPublicTopicName();

    private String publicTopic = mqttClientId;
    private ArrayList<String> remoteSubTopiclist = new ArrayList<>();
    private ArrayList<String> localSubTopiclist = new ArrayList<>();

    //TCP server
    private TCPServer mTCPServer;
    private static int TCPClientPort = 0;

    //AIDL
    IZwaveControlInterface zwaveService;


    @Override
    public void onCreate() {

        super.onCreate();

        /*   launch mqtt server  */
        /*
        try {
            mqttServer.startServer();
        } catch (Exception e){e.printStackTrace();}
        Log.i(LOG_TAG,"MQTT Local Server = [" + mqttServer.getServerStatus() +"]");
        */

        Log.i(LOG_TAG,"ClientID = [" + mqttClientId +"]");

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

        //AIDL
        Intent intent = new Intent();
        intent.setPackage("com.askey.firefly.zwave.control");
        boolean bbindResult = bindService(intent, serviceConection, Context.BIND_AUTO_CREATE);
        Log.i(LOG_TAG,"AIDL status = [" + bbindResult +"]");

        /*  launch udp server */
        /*
        try {
            boolean bResult = uDPConnecting.startReceiver();
            Log.i(LOG_TAG,"UDP server = [" + bResult +"]");
        } catch (Exception e){e.printStackTrace();}
        */
    }

    @Override
    public void onDestroy(){
        Log.i(LOG_TAG,"===onDestroy===");
        super.onDestroy();

        //uDPConnecting.stopConn();
        unsubscribeTopic(publicTopic);

        TCPServer.close();

        try {
            if (zwaveService!=null) {
                zwaveService.closeController();
                zwaveService.unRegisterListener(mCallback);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
        unbindService(serviceConection);

        try {
            if(mqttLocalClient.isConnected()){
                mqttLocalClient.disconnect();
            }
            if(mqttRemoteClient.isConnected()){
                mqttRemoteClient.disconnect();
            }
            /*
            if (mqttServer.getServerStatus()) {
                Log.i(LOG_TAG, "mqttServer.stopServer()");
                mqttServer.stopServer();
            }
            */
        } catch (MqttException e) {
            e.printStackTrace();
        }
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
                        if (TCPClientPort != 0) {
                            mTCPServer.sendMessage(clientID, TCPSTRING + "addDevice:other");
                        } else {

                            try {
                                TCPClientPort = clientID;
                                Log.i(LOG_TAG, "zwaveService.addDevice(mCallback)");
                                zwaveService.addDevice(mCallback);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else if (message.contains("removeDevice")) {
                        if (TCPClientPort != 0) {
                            mTCPServer.sendMessage(clientID, TCPSTRING + "removeDevice:other");
                        } else {

                            try {
                                TCPClientPort = clientID;
                                Log.i(LOG_TAG, "zwaveService.removeDevice(mCallback)");
                                zwaveService.removeDevice(mCallback);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else if (message.contains("openController")) {

                        try {
                            if (zwaveService!=null) {
                                TCPClientPort = clientID;
                                Log.i(LOG_TAG, "["+TCPClientPort+"]zwaveService.openController(mCallback)");
                                zwaveService.openController(mCallback);
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    else if (message.contains("removeFailedDevice")) {

                        String[] tokens = message.split(":");
                        if (tokens.length > 2) {
                            int tNodeId = Integer.parseInt(tokens[2]);
                            try {
                                Log.i(LOG_TAG, "zwaveService.removeFailedDevice(mCallback, " + tNodeId + ")");
                                zwaveService.removeFailedDevice(mCallback, tNodeId);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else if (message.contains("replaceFailedDevice")) {

                        String[] tokens = message.split(":");
                        if (tokens.length > 2) {

                            int tNodeId = Integer.parseInt(tokens[2]);
                            try {
                                Log.i(LOG_TAG, "zwaveService.replaceFailedDevice(mCallback, " + tNodeId + ")");
                                zwaveService.replaceFailedDevice(mCallback, tNodeId);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else if (message.contains("stopAddDevice")) {

                        try {
                            Log.i(LOG_TAG, "zwaveService.stopAddDevice(mCallback)");
                            zwaveService.stopAddDevice(mCallback);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    else if (message.contains("stopRemoveDevice")) {

                        try {
                            Log.i(LOG_TAG, "zwaveService.stopRemoveDevice(mCallback)");
                            zwaveService.stopRemoveDevice(mCallback);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    else if (message.contains("reNameDevice")) {

                        if (TCPClientPort != 0) {
                            mTCPServer.sendMessage(clientID, TCPSTRING + "reNameDevice:other");
                        }
                        else {

                            String[] tokens = message.split(":");
                            if (tokens.length > 4) {
                                TCPClientPort = clientID;
                                String tHomeId = tokens[2];
                                int tDeviceId = Integer.parseInt(tokens[3]);
                                String tNewName = tokens[4];

                                try {
                                    Log.i(LOG_TAG, "zwaveService.reNameDevice(mCallback,"+tHomeId+","+tDeviceId+","+tNewName+")");
                                    zwaveService.reNameDevice(mCallback, tHomeId,tDeviceId,tNewName);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    else if (message.contains("setDefault")) {

                        if (TCPClientPort != 0) {
                            mTCPServer.sendMessage(clientID, TCPSTRING + "setDefault:other");
                        }else {
                            TCPClientPort = clientID;
                            try {
                                Log.i(LOG_TAG, "zwaveService.setDefault(mCallback)");
                                zwaveService.setDefault(mCallback);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else if (message.contains("closeController")) {

                        try {
                            Log.i(LOG_TAG, "zwaveService.closeController(mCallback)");
                            zwaveService.closeController();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        mTCPServer.sendMessage(clientID, TCPSTRING + " Wrong Payload");
                    }
                }
            }
        });
        mTCPServer.start();
        return mTCPServer.isAlive();
    }

    //***** connect to remote mqtt server *****
    private void mqttRemoteConnect(MqttConnectOptions mqttConnectOptions){

        mqttRemoteClient = new MqttAndroidClient(getApplicationContext(), remoteServerUri, mqttClientId);

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
                        subscribeToTopic(publicTopic);
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

        String serverIp = Utils.getIpAddress();
        Log.i(LOG_TAG,"local mqtt server ip = ["+ Utils.getIpAddress() +"]");

        final String localServerUri = "tcp://"+serverIp+":1883";

        mqttLocalClient = new MqttAndroidClient(getApplicationContext(), localServerUri, mqttClientId);

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
                        subscribeToTopic(publicTopic);
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
            try {
                Log.i(LOG_TAG, "zwaveService.getDevices(mCallback)");
                zwaveService.getDevices(mCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        } else {

            String[] tokens = TopicName.split(publicTopic);
            int tNodeid = Integer.parseInt(tokens[1]);

            if (mqttMessage.contains("getStatus")) {

                try {
                    Log.i(LOG_TAG, "zwaveService.getDeviceInfo(mCallback," + tNodeid + ")");
                    zwaveService.getDeviceInfo(mCallback, tNodeid);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else if (mqttMessage.contains("getDeviceBattery")) {

                try {
                    Log.i(LOG_TAG, "zwaveService.getDeviceBattery(mCallback," + tNodeid + ")");
                    zwaveService.getDeviceBattery(mCallback, tNodeid);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else if (mqttMessage.contains("getSensorMultiLevel")) {

                try {
                    Log.i(LOG_TAG, "zwaveService.getSensorMultiLevel(mCallback," + tNodeid + ")");
                    zwaveService.getSensorMultiLevel(mCallback, tNodeid);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else if (mqttMessage.contains("updateNode")) {

                try {
                    Log.i(LOG_TAG, "zwaveService.updateNode(mCallback," + tNodeid + ")");
                    zwaveService.updateNode(mCallback, tNodeid);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else if (mqttMessage.contains("getConfiguration")) {

                tokens = mqttMessage.split(":");
                if (tokens.length > 7) {
                    String tHomeid = tokens[2];
                    int tParamMode = Integer.parseInt(tokens[4]);
                    int tParamNumber = Integer.parseInt(tokens[5]);
                    int tRangeStart = Integer.parseInt(tokens[6]);
                    int tRrangeEnd = Integer.parseInt(tokens[7]);

                    try {
                        Log.i(LOG_TAG, "zwaveService.getConfiguration(mCallback," + tHomeid + "," + tNodeid + ","
                                + tParamMode + "," + tParamNumber + "," + tRangeStart + "," + tRrangeEnd + ")");
                        zwaveService.getConfiguration(mCallback, tHomeid, tNodeid,
                                tParamMode, tParamNumber, tRangeStart, tRrangeEnd);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            } else if (mqttMessage.contains("setConfiguration")) {

                tokens = mqttMessage.split(":");
                if (tokens.length > 7) {
                    String tHomeid = tokens[2];
                    int tParamMode = Integer.parseInt(tokens[4]);
                    int tParamNumber = Integer.parseInt(tokens[5]);
                    int tRangeStart = Integer.parseInt(tokens[6]);
                    int tRrangeEnd = Integer.parseInt(tokens[7]);

                    try {
                        Log.i(LOG_TAG, "zwaveService.setConfiguration(mCallback," + tHomeid + "," + tNodeid + ","
                                + tParamMode + "," + tParamNumber + "," + tRangeStart + "," + tRrangeEnd + ")");
                        zwaveService.setConfiguration(mCallback, tHomeid, tNodeid,
                                tParamMode, tParamNumber, tRangeStart, tRrangeEnd);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            } else if (mqttMessage.contains("getSupportedSwitchType")) {

                try {
                    Log.i(LOG_TAG, "zwaveService.getSupportedSwitchType(mCallback," + tNodeid + ")");
                    zwaveService.getSupportedSwitchType(mCallback, tNodeid);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else if (mqttMessage.contains("startStopSwitchLevelChange")) {

                tokens = mqttMessage.split(":");
                if (tokens.length > 8) {
                    String tHomeid = tokens[2];
                    int tstartLvlVal = Integer.parseInt(tokens[4]);
                    int tduration = Integer.parseInt(tokens[5]);
                    int tpmyChangeDir = Integer.parseInt(tokens[6]);
                    int tsecChangeDir = Integer.parseInt(tokens[7]);
                    int tsecStep = Integer.parseInt(tokens[8]);

                    try {
                        Log.i(LOG_TAG, "zwaveService.startStopSwitchLevelChange(mCallback," + tHomeid + "," + tNodeid + ","
                                + tstartLvlVal + "," + tduration + "," + tpmyChangeDir + "," + tsecChangeDir + "," + tsecStep + ")");
                        zwaveService.startStopSwitchLevelChange(mCallback, tHomeid, tNodeid,
                                tstartLvlVal, tduration, tpmyChangeDir, tsecChangeDir, tsecStep);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            } else if (mqttMessage.contains("getPowerLevel")) {

                try {
                    Log.i(LOG_TAG, "zwaveService.getPowerLevel(mCallback," + tNodeid + ")");
                    zwaveService.getPowerLevel(mCallback, tNodeid);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else if (mqttMessage.contains("setSwitchAllOn")) {

                try {
                    Log.i(LOG_TAG, "zwaveService.setSwitchAllOn(mCallback," + tNodeid + ")");
                    zwaveService.setSwitchAllOn(mCallback, tNodeid);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else if (mqttMessage.contains("setSwitchAllOff")) {

                try {
                    Log.i(LOG_TAG, "zwaveService.setSwitchAllOff(mCallback," + tNodeid + ")");
                    zwaveService.setSwitchAllOff(mCallback, tNodeid);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else if (mqttMessage.contains("getBasic")) {

                try {
                    Log.i(LOG_TAG, "zwaveService.getBasic(mCallback," + tNodeid + ")");
                    zwaveService.getBasic(mCallback, tNodeid);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else if (mqttMessage.contains("setBasic")) {

                tokens = mqttMessage.split(":");
                if (tokens.length > 2) {
                    int tValue = Integer.parseInt(tokens[2]);
                    try {
                        Log.i(LOG_TAG, "zwaveService.setBasic(mCallback," +tNodeid+ "," +tValue+ ")");
                        zwaveService.setBasic(mCallback, tNodeid,tValue);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            } else if (mqttMessage.contains("getSwitchMultiLevel")) {

                try {
                    Log.i(LOG_TAG, "zwaveService.getSwitchMultiLevel(mCallback," + tNodeid + ")");
                    zwaveService.getSwitchMultiLevel(mCallback, tNodeid);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else if (mqttMessage.contains("setSwitchMultiLevel")) {

                tokens = mqttMessage.split(":");
                if (tokens.length > 4) {
                    int tValue = Integer.parseInt(tokens[3]);
                    int tDuration = Integer.parseInt(tokens[4]);
                    try {
                        Log.i(LOG_TAG, "zwaveService.setSwitchMultiLevel(mCallback," +tNodeid+ "," +tValue+ ","+tDuration+")");
                        zwaveService.setSwitchMultiLevel(mCallback, tNodeid,tValue,tDuration);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // Synchronize the subscribe topic of local mqtt server with remote mqtt server
    private void syncSubscribeTopic(){

        for (int idx=0;idx<localSubTopiclist.size();idx++) {
            if (!remoteSubTopiclist.contains(localSubTopiclist.get(idx))) {
                subscribeToTopic(localSubTopiclist.get(idx));
            }
        }

        for (int idx=0;idx<remoteSubTopiclist.size();idx++) {
            if (!localSubTopiclist.contains(remoteSubTopiclist.get(idx))) {
                unsubscribeTopic(localSubTopiclist.get(idx));
            }
        }
    }

    // subscribe mqtt topic
    private void subscribeToTopic(final String TopicName) {
        Log.i(LOG_TAG,"mqttLocalClient.isConnected() = "+mqttLocalClient.isConnected());
        Log.i(LOG_TAG,"mqttRemoteClient.isConnected() = "+mqttRemoteClient.isConnected());

        if (!localSubTopiclist.contains(TopicName)) {
            if (mqttLocalClient.isConnected()) {
                try {
                    mqttLocalClient.subscribe(TopicName, 0, null, new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Log.i(LOG_TAG, "localMQTT Subscribed : \"" + TopicName + "\" topic");
                            localSubTopiclist.add(TopicName);

                            for (int idx = 0; idx < localSubTopiclist.size(); idx++) {
                                Log.i(LOG_TAG, "localSubTopiclist[" + idx + "] = " + localSubTopiclist.get(idx));
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
        if (!remoteSubTopiclist.contains(TopicName)) {
            if (mqttRemoteClient.isConnected()) {
                try {
                    mqttRemoteClient.subscribe(TopicName, 0, null, new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Log.i(LOG_TAG, "remoteMQTT Subscribed : \"" + TopicName + "\" topic");
                            remoteSubTopiclist.add(TopicName);

                            for (int idx = 0; idx < remoteSubTopiclist.size(); idx++) {
                                Log.i(LOG_TAG, "remoteSubTopiclist[" + idx + "] = " + remoteSubTopiclist.get(idx));
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
            if (localSubTopiclist.contains(TopicName)) {
                if (mqttLocalClient.isConnected()) {
                    mqttLocalClient.unsubscribe(TopicName);

                    idx = localSubTopiclist.indexOf(TopicName);
                    if (idx >= 0) {
                        localSubTopiclist.remove(idx);
                    }
                }
            }
            if (remoteSubTopiclist.contains(TopicName)) {
                if (mqttRemoteClient.isConnected()) {
                    mqttRemoteClient.unsubscribe(TopicName);
                    idx = remoteSubTopiclist.indexOf(TopicName);
                    if (idx >= 0) {
                        remoteSubTopiclist.remove(idx);
                    }
                }
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }

        for (idx = 0 ; idx< localSubTopiclist.size(); idx++){
            Log.i(LOG_TAG,"localSubTopiclist["+idx+"] = "+localSubTopiclist.get(idx));
        }
        for (idx = 0 ; idx< remoteSubTopiclist.size(); idx++){
            Log.i(LOG_TAG,"remoteSubTopiclist["+idx+"] = "+remoteSubTopiclist.get(idx));
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
                Log.i(LOG_TAG,"[localmqtt]"+mqttLocalClient.getBufferedMessageCount() + " messages in buffer.");
            }
            if (mqttRemoteClient.isConnected()) {
                mqttRemoteClient.publish(publishTopic, message);
            } else {
                Log.i(LOG_TAG,"[remoteMqtt]"+mqttRemoteClient.getBufferedMessageCount() + " messages in buffer.");
            }
        } catch (MqttException e) {
            System.err.println("MQTT Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // AIDL
    private ServiceConnection serviceConection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            zwaveService = IZwaveControlInterface.Stub.asInterface(service);
            if (zwaveService != null){
                try{
                    Log.i(LOG_TAG,"registerListener AIDL");
                    zwaveService.registerListener(mCallback);

                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }else{
                Log.i(LOG_TAG,"fail to registerListener AIDL");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private void handleNormalAIDLCallback(String tMethodName, String tResult){
        Log.i(LOG_TAG,"AIDLResult "+tMethodName+" : "+tResult);

        ArrayList<String>  NodeId = Utils.searchString(tResult, "Node id");

        for( int idx = 0; idx < NodeId.size(); idx++){
            Log.i(LOG_TAG,tMethodName+" Node id = "+NodeId.get(idx));
            publishMessage(publicTopic+NodeId.get(idx),tResult);
        }
    }

    // AIDLCallBack
    private IZwaveContrlCallBack.Stub mCallback = new IZwaveContrlCallBack.Stub(){
        @Override
        public void addDeviceCallBack(String result) throws RemoteException{

            Log.i(LOG_TAG,"AIDLResult addDevice : "+result);
            mTCPServer.sendMessage(TCPClientPort,result);

            if (result.contains("Success") || result.contains("Failed")){
                TCPClientPort =0;
            }

            if (result.contains("addDevice:")){

                String[] tokens = result.split(":");
                if (tokens.length<3){
                    Log.i(LOG_TAG,"AIDLResult addDevice : wrong format "+result);
                } else {
                    //String tHomeId = tokens[1];
                    String tNodeId = tokens[2];
                    subscribeToTopic(publicTopic + tNodeId);
                    publishMessage(publicTopic, result);
                }
            }
        }

        @Override
        public void removeDeviceCallBack(String result) throws RemoteException{

            Log.i(LOG_TAG,"AIDLResult removeDevice : "+result);
            mTCPServer.sendMessage(TCPClientPort,result);

            if (result.contains("Success") || result.contains("Failed")){
                TCPClientPort =0;
            }

            if (result.contains("removeDevice:")){

                String[] tokens = result.split(":");
                if (tokens.length<3){
                    Log.i(LOG_TAG,"AIDLResult removeDevice : wrong format "+result);
                } else {
                    //String tHomeId = tokens[1];
                    String tNodeId = tokens[2];
                    publishMessage(publicTopic, "removeDevice:" + tNodeId);
                    unsubscribeTopic(publicTopic + tNodeId);
                }
            }
        }

        @Override
        public void getDevicesCallBack(String result) throws RemoteException{

            Log.i(LOG_TAG,"AIDLResult getDevices : "+result);

            ArrayList<String> tmpLine = Utils.searchString(result, "Node id");

            for( int idx = 1; idx < tmpLine.size(); idx++){
                Log.i(LOG_TAG,"Node id ("+idx+") = "+tmpLine.get(idx));
                subscribeToTopic(publicTopic+tmpLine.get(idx));
            }
            //publish result to MQTT public topic
            publishMessage(publicTopic,result);
        }

        @Override
        public void getDevicesInfoCallBack(String result) throws RemoteException{
            String currentMethodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            handleNormalAIDLCallback(currentMethodName,result);
        }

        @Override
        public void openControlCallBack(String result, int length) throws RemoteException{
            //result = result.substring(0, length);
            Log.i(LOG_TAG, "AIDLResult openControl ["+TCPClientPort+"]: " + result);
            mTCPServer.sendMessage(TCPClientPort,result);
            TCPClientPort = 0;
        }

        @Override
        public void removeFailedDeviceCallBack(String result) throws RemoteException{
            Log.i(LOG_TAG,"AIDLResult removeFailedDeviceCallBack : "+result);
            mTCPServer.sendMessage(TCPClientPort,result);
            TCPClientPort = 0;
        }
        @Override
        public void replaceFailedDeviceCallBack(String result) throws RemoteException{
            Log.i(LOG_TAG,"AIDLResult replaceFailedDeviceCallBack : "+result);
            mTCPServer.sendMessage(TCPClientPort,result);
            TCPClientPort = 0;
        }

        @Override
        public void stopAddDeviceCallBack(String result) throws RemoteException {
            Log.i(LOG_TAG,"AIDLResult stopAddDeviceCallBack : "+result);
            mTCPServer.sendMessage(TCPClientPort,result);
            TCPClientPort = 0;
        }

        @Override
        public void stopRemoveDeviceCallBack(String result) throws RemoteException {
            Log.i(LOG_TAG,"AIDLResult stopRemoveDeviceCallBack : "+result);
            mTCPServer.sendMessage(TCPClientPort,result);
            TCPClientPort = 0;
        }

        @Override
        public void getDeviceBatteryCallBack(String result) throws RemoteException {
            String currentMethodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            handleNormalAIDLCallback(currentMethodName,result);
        }

        @Override
        public void getSensorMultiLevelCallBack(String result) throws RemoteException {
            String currentMethodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            handleNormalAIDLCallback(currentMethodName,result);
        }

        @Override
        public void updateNodeCallBack(String result) throws RemoteException {
            String currentMethodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            handleNormalAIDLCallback(currentMethodName,result);
        }

        @Override
        public void reNameDeviceCallBack(String result) throws RemoteException {
            Log.i(LOG_TAG,"AIDLResult reNameDeviceCallBack : "+result);
            mTCPServer.sendMessage(TCPClientPort,result);
            TCPClientPort = 0;
        }

        @Override
        public void setDefaultCallBack(String result) throws RemoteException {
            Log.i(LOG_TAG,"AIDLResult setDefaultCallBack : "+result);
            mTCPServer.sendMessage(TCPClientPort,result);
            TCPClientPort = 0;
        }

        @Override
        public void getConfiguration(String result) throws RemoteException {
            String currentMethodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            handleNormalAIDLCallback(currentMethodName,result);
        }

        @Override
        public void setConfiguration(String result) throws RemoteException {
            String currentMethodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            handleNormalAIDLCallback(currentMethodName,result);
        }

        @Override
        public void getSupportedSwitchType(String result) throws RemoteException {
            handleNormalAIDLCallback(Thread.currentThread().getStackTrace()[2].getMethodName(),result);
        }

        @Override
        public void startStopSwitchLevelChange(String result) throws RemoteException {
            String currentMethodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            handleNormalAIDLCallback(currentMethodName,result);
        }

        @Override
        public void getPowerLevel(String result) throws RemoteException {
            String currentMethodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            handleNormalAIDLCallback(currentMethodName,result);
        }

        @Override
        public void setSwitchAllOn(String result) throws RemoteException {
            String currentMethodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            handleNormalAIDLCallback(currentMethodName,result);
        }

        @Override
        public void setSwitchAllOff(String result) throws RemoteException {
            String currentMethodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            handleNormalAIDLCallback(currentMethodName,result);
        }

        @Override
        public void getBasic(String result) throws RemoteException {
            String currentMethodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            handleNormalAIDLCallback(currentMethodName,result);
        }

        @Override
        public void setBasic(String result) throws RemoteException {
            String currentMethodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            handleNormalAIDLCallback(currentMethodName,result);
        }

        @Override
        public void getSwitchMultiLevel(String result) throws RemoteException {
            String currentMethodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            handleNormalAIDLCallback(currentMethodName,result);
        }

        @Override
        public void setSwitchMultiLevel(String result) throws RemoteException {
            String currentMethodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            handleNormalAIDLCallback(currentMethodName,result);
        }
    };

}


