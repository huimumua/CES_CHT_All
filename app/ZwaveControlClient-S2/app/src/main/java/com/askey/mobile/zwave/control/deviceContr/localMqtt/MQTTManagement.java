package com.askey.mobile.zwave.control.deviceContr.localMqtt;

import android.content.Context;

import com.askey.mobile.zwave.control.application.ZwaveClientApplication;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;


/**
 * 项目名称：ZwaveControlClient
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2017/7/26 11:22
 * 修改人：skysoft
 * 修改时间：2017/7/26 11:22
 * 修改备注：
 */
public class MQTTManagement  {
    private static String TAG = "MQTTManagement";
    private Context mContext;
    private static MQTTManagement instance;
    private static MqttAndroidClient mqttAndroidClient;

    private MQTTManagement (Context context) {
        this.mContext= context;


    }

    public static MQTTManagement getSingInstance() {
        if(instance == null)
            instance = new MQTTManagement(ZwaveClientApplication.getInstance());
        return instance;
    }



    /**
     * 回调接口
     * @author Administrator
     *
     */
    public interface initMqttCallback {
        public void initMQTT(boolean result);
    }


    public void initMqttCallback(String clientId, String serverUri,final initMqttCallback initMqttCallback){
        mqttClient(clientId,serverUri,initMqttCallback);
    }

    private ArrayList<MqttMessageArrived> meaasgemap = new ArrayList<MqttMessageArrived>();

    public void rigister(MqttMessageArrived callback){
        clearMessageArrived();
        if (meaasgemap != null) {
            meaasgemap.add(callback);
        }
    }

    public void unrigister(MqttMessageArrived callback){
        if (meaasgemap != null) {
            meaasgemap.remove(callback);
        }
    }
    public void clearMessageArrived(){
        if (meaasgemap != null) {
            meaasgemap.clear();
        }
    }

    public void mqttClient(String clientId, String serverUri,final initMqttCallback initMqttCallback ) {
        clientId = clientId + System.currentTimeMillis();
        Logg.i(TAG,"mqttClient -> serverUri : " + serverUri);
        mqttAndroidClient = new MqttAndroidClient(mContext, serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    Logg.i(TAG,"mqttClient -> Reconnected to : " + serverURI);
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopic(Const.subscriptionTopic,initMqttCallback);
                } else {
                    Logg.i(TAG,"mqttClient -> Connected to : " + serverURI);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Logg.i(TAG,"The Connection was lost.");
                initMqttCallback.initMQTT(false);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Logg.i(TAG,"Incoming message: " + new String(message.getPayload()));



            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Logg.i(TAG,"deliveryComplete " +token);
            }
        });

        try {
            //addToHistory("Connecting to " + serverUri);
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setAutomaticReconnect(true);
            mqttConnectOptions.setCleanSession(false);
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic(Const.subscriptionTopic,initMqttCallback);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Logg.i(TAG,"Failed to connect to: serverUri" );
                    initMqttCallback.initMQTT(false);
                }
            });
        } catch (MqttException ex){
            ex.printStackTrace();
            Logg.e(TAG,"=====MqttException===="+ex.getMessage());
        }
    }

    public void subscribeToTopic(String topic,final initMqttCallback initMqttCallback){
        try {
            mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Logg.i(TAG,"Subscribed!");
                    // 这里需要做openZwave的操作
                    if( null != initMqttCallback){
                        initMqttCallback.initMQTT(true);
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Logg.i(TAG,"Failed to subscribe");
                    if(null != initMqttCallback){
                        initMqttCallback.initMQTT(false);
                    }
                }
            });

            // THIS DOES NOT WORK!
            mqttAndroidClient.subscribe(topic, 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // message Arrived!
                    Logg.i(TAG,"Message: " + topic + " : " + new String(message.getPayload()));
                    try {
                        if(meaasgemap.size()>0){
                            meaasgemap.get(0).mqttMessageArrived(topic,message);
                        }
                    }catch (Exception e){
                        Logg.e(TAG,"=mqttMessageArrived==Exception="+e.getMessage());
                    }
                }
            });

        } catch (MqttException ex){
            Logg.e(TAG,"subscribeToTopic -> Exception whilst subscribing");
            ex.printStackTrace();
        }
    }


    public static  void publishMessage(String topic,String command){
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(command.getBytes());
            if(mqttAndroidClient!=null){
                mqttAndroidClient.publish(topic, message);
                Logg.i(TAG,"Message Published");
                if(!mqttAndroidClient.isConnected()){
                    Logg.i(TAG,mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
                }
            }
        } catch (MqttException e) {
            e.printStackTrace();
            Logg.e(TAG,"publishMessage -> Error Publishing: " + e.getMessage());
        }
    }

    public void closeMqtt(){
        if(mqttAndroidClient!=null){
            try {
                mqttAndroidClient.close();
                mqttAndroidClient.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }

        }
    }

}
