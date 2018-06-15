package com.askey.iotcontrol.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttLastWillAndTestament;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.AttachPrincipalPolicyRequest;
import com.amazonaws.services.iot.model.AttachThingPrincipalRequest;
import com.amazonaws.services.iot.model.AttributePayload;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateRequest;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateResult;
import com.amazonaws.services.iot.model.CreateThingRequest;
import com.amazonaws.services.iot.model.CreateThingResult;
import com.amazonaws.services.iot.model.DeleteThingRequest;
import com.amazonaws.services.iot.model.DeleteThingResult;
import com.amazonaws.services.iot.model.DetachThingPrincipalRequest;
import com.amazonaws.services.iot.model.ListThingsRequest;
import com.amazonaws.services.iot.model.ListThingsResult;
import com.askey.iotcontrol.application.ZwaveApplication;
import com.askey.iotcontrol.dao.ZwaveDevice;
import com.askey.iotcontrol.dao.ZwaveDeviceManager;
import com.askey.iotcontrol.service.MQTTBroker;
import com.askey.iotcontrol.utils.Const;
import com.askey.iotcontrol.utils.DeviceInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by chiapin on 2018/5/29.
 */

public class awsIoTClient {

    private static String LOG_TAG = awsIoTClient.class.getSimpleName();

    private static String clientId;
    private static String keystorePath;
    private static String keystoreName;
    private static String keystorePassword;
    private static CognitoCachingCredentialsProvider credentialsProvider;

    private static AWSIotClient mIotAndroidClient;
    private static AWSIotMqttManager mqttManager;

    /* awsiot */
    private static final String CUSTOMER_SPECIFIC_ENDPOINT = "aybau2z6s0r8v.iot.us-west-2.amazonaws.com";
    // Cognito pool ID. For this app, pool needs to be unauthenticated pool with
    // AWS IoT permissions.
    private static final String COGNITO_POOL_ID = "us-west-2:4c5ece05-53ea-4272-82e2-41c32114e040";
    // Name of the AWS IoT policy to attach to a newly created certificate
    private static final String AWS_IOT_POLICY_NAME = "ArielPolicy";
    // Region of AWS IoT
    private static final Regions MY_REGION = Regions.US_WEST_2;
    // Filename of KeyStore file on the filesystem
    private static final String KEYSTORE_NAME = "iot_keystore";
    // Password for the private key in the KeyStore
    private static final String KEYSTORE_PASSWORD = "password";
    // Certificate and key aliases in the KeyStore
    private static final String CERTIFICATE_ID = "default";
    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private static String cARN = "arn:aws:iot:us-west-2:719642772703:cert/3d460fdcd31ca50ef7efeb224f05879c2fe97ba544ad44143e634807fd6937a2";

    private static KeyStore clientKeyStore = null;
    private static String certificateId;

    public static void initAWSIot(){
        // MQTT client IDs are required to be unique per AWS IoT account.
        // This UUID is "practically unique" but does not _guarantee_
        // uniqueness.
        clientId = UUID.randomUUID().toString();

        // Initialize the AWS Cognito credentials provider
        credentialsProvider = new CognitoCachingCredentialsProvider(
                ZwaveApplication.getInstance(), // context
                COGNITO_POOL_ID, // Identity Pool ID
                MY_REGION // Region
        );

        Region region = Region.getRegion(MY_REGION);

        // MQTT Client
        mqttManager = new AWSIotMqttManager(clientId, CUSTOMER_SPECIFIC_ENDPOINT);

        // Set keepalive to 10 seconds.  Will recognize disconnects more quickly but will also send
        // MQTT pings every 10 seconds.
        mqttManager.setKeepAlive(10);

        // Set Last Will and Testament for MQTT.  On an unclean disconnect (loss of connection)
        // AWS IoT will publish this message to alert other clients.
        AWSIotMqttLastWillAndTestament lwt = new AWSIotMqttLastWillAndTestament("my/lwt/topic",
                "Android client lost connection", AWSIotMqttQos.QOS1);
        mqttManager.setMqttLastWillAndTestament(lwt);

        // IoT Client (for creation of certificate if needed)
        mIotAndroidClient = new AWSIotClient(credentialsProvider);
        mIotAndroidClient.setRegion(region);

        keystorePath = ZwaveApplication.getInstance().getFilesDir().getPath();
        keystoreName = KEYSTORE_NAME;
        keystorePassword = KEYSTORE_PASSWORD;
        certificateId = CERTIFICATE_ID;

        executorService.submit(new CheckConnection());
    }

    private static void loadCertificate()
    {

        boolean result = false;
        // To load cert/key from keystore on filesystem
        try {
            if (AWSIotKeystoreHelper.isKeystorePresent(keystorePath, keystoreName)) {
                if (AWSIotKeystoreHelper.keystoreContainsAlias(certificateId, keystorePath,
                        keystoreName, keystorePassword)) {
                    Log.i(LOG_TAG, "Certificate " + certificateId
                            + " found in keystore - using for MQTT.");
                    // load keystore from file into memory to pass on connection
                    clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(certificateId,
                            keystorePath, keystoreName, keystorePassword);

                } else {
                    Log.i(LOG_TAG, "Key/cert " + certificateId + " not found in keystore.");
                }
            } else {
                Log.i(LOG_TAG, "Keystore " + keystorePath + "/" + keystoreName + " not found.");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "An error occurred retrieving cert/key from keystore.", e);
        }

        if (clientKeyStore == null) {
            Log.i(LOG_TAG, "Cert/key was not found in keystore - creating new key and certificate.");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Create a new private key and certificate. This call
                        // creates both on the server and returns them to the
                        // device.
                        CreateKeysAndCertificateRequest createKeysAndCertificateRequest =
                                new CreateKeysAndCertificateRequest();
                        createKeysAndCertificateRequest.setSetAsActive(true);
                        final CreateKeysAndCertificateResult createKeysAndCertificateResult;
                        createKeysAndCertificateResult =
                                mIotAndroidClient.createKeysAndCertificate(createKeysAndCertificateRequest);
                        Log.i(LOG_TAG,
                                "Cert ID: " +
                                        createKeysAndCertificateResult.getCertificateId() +
                                        " created.");

                        // store in keystore for use in MQTT client
                        // saved as alias "default" so a new certificate isn't
                        // generated each run of this application

                        AWSIotKeystoreHelper.saveCertificateAndPrivateKey(certificateId,
                                createKeysAndCertificateResult.getCertificatePem(),
                                createKeysAndCertificateResult.getKeyPair().getPrivateKey(),
                                keystorePath, keystoreName, keystorePassword);

                        // load keystore from file into memory to pass on
                        // connection
                        clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(certificateId,
                                keystorePath, keystoreName, keystorePassword);

                        // Attach a policy to the newly created certificate.
                        // This flow assumes the policy was already created in
                        // AWS IoT and we are now just attaching it to the
                        // certificate.

                        AttachPrincipalPolicyRequest policyAttachRequest =
                                new AttachPrincipalPolicyRequest();
                        policyAttachRequest.setPolicyName(AWS_IOT_POLICY_NAME);
                        policyAttachRequest.setPrincipal(createKeysAndCertificateResult
                                .getCertificateArn());
                        mIotAndroidClient.attachPrincipalPolicy(policyAttachRequest);

                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Exception occurred when generating new private key and certificate.", e);
                    }
                }
            }).start();
        }
    }

    public static boolean connectAwsIot()
    {
        Log.d(LOG_TAG, "[connectAwsIot] clientId = " + clientId);
        while(clientKeyStore == null) {
            try {
                Log.i(LOG_TAG, "Waiting generate clientKeyStore !!");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            };
        }
        Log.d(LOG_TAG, "connectAwsIot clientKeyStore = " + clientKeyStore);

        try {
            mqttManager.connect(clientKeyStore, new AWSIotMqttClientStatusCallback() {
                @Override
                public void onStatusChanged(final AWSIotMqttClientStatus status,
                                            final Throwable throwable) {
                    Log.d(LOG_TAG, "Status = " + String.valueOf(status));

                    if (status == AWSIotMqttClientStatus.Connecting) {
                        Log.d(LOG_TAG, "[connectAwsIot] Connecting...");
                    } else if (status == AWSIotMqttClientStatus.Connected) {
                        Log.d(LOG_TAG, "[connectAwsIot] Connected");
                        if(!DeviceInfo.awsIotDevList.isEmpty()) {
                            subscribeThingList();
                        }
                    } else if (status == AWSIotMqttClientStatus.Reconnecting) {
                        if (throwable != null) {
                            Log.e(LOG_TAG, "[connectAwsIot] Connection error.", throwable);
                        }
                        Log.d(LOG_TAG, "[connectAwsIot] Reconnecting");
                    } else if (status == AWSIotMqttClientStatus.ConnectionLost) {
                        if (throwable != null) {
                            Log.e(LOG_TAG, "[connectAwsIot] Connection error.", throwable);

                        }
                        Log.d(LOG_TAG, "[connectAwsIot] Disconnected");
                    } else {
                        Log.d(LOG_TAG, "[connectAwsIot] Disconnected");

                    }
                }
            });
        } catch (final Exception e) {
            Log.e(LOG_TAG, "Connection error.", e);
            return false;
        }

        return true;
    }

    private static void subscribeThingList()
    {
        //if (!DeviceInfo.awsIotDevList.contains(Const.PublicTopicName)){
        //    Log.i(LOG_TAG,"No this device thing on awsIoT, create it!");
        //    createIotThing(Const.PublicTopicName, "OTTBOX"+Const.PublicTopicName);
        //}
        for(int i=0; i<DeviceInfo.awsIotDevList.size(); i++) {
            subscribeAwsMQTT(DeviceInfo.awsIotDevList.get(i));
        }
        //subscribeAwsMQTT(Const.PublicTopicName);
    }

    public static void createIotThing(String thingName, String deviceName) {

        Log.i(LOG_TAG,"createThingResult!!!");

        Map<String, String> device = new HashMap<>();
        device.put("deviceName", deviceName);

        CreateThingRequest createThingRequest = new CreateThingRequest();
        createThingRequest.setThingName(thingName);
        AttributePayload attributePayload = new AttributePayload();
        attributePayload.setAttributes(device);
        createThingRequest.setAttributePayload(attributePayload);
        //createThingRequest.setThingTypeName(thingType);
        CreateThingResult createThingResult = mIotAndroidClient.createThing(createThingRequest);

        Log.i(LOG_TAG,"createThingResult getThingName="+ createThingResult.getThingName());

        mIotAndroidClient.attachThingPrincipal(new AttachThingPrincipalRequest().
                withPrincipal(cARN).withThingName(thingName));

    }


    public static void deleteIotThing(String thingName) {

        Log.i(LOG_TAG,"deleteIotThing" + mIotAndroidClient.getEndpoint());
        Log.i(LOG_TAG,"cARN="+ cARN);

        try {
            mIotAndroidClient.detachThingPrincipal(new DetachThingPrincipalRequest().
                    withPrincipal(cARN).withThingName(thingName));
        } catch (Exception e) { }

        DeleteThingRequest deleteThingRequest = new DeleteThingRequest();
        deleteThingRequest.setThingName(thingName);

        DeleteThingResult deleteThingResult = mIotAndroidClient.deleteThing(deleteThingRequest);
        Log.i(LOG_TAG,"deleteThingRequest="+ deleteThingResult.toString());

    }

    public static boolean disconnectAwsIot()
    {
        try {
            mqttManager.disconnect();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Disconnect error.", e);
            return false;
        }
        return true;
    }

    public static boolean haveInternet()
    {
        boolean result = false;
        ConnectivityManager connManager = (ConnectivityManager) ZwaveApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info=connManager.getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            result = false;
        }
        else {
            if (info.isAvailable()){
                result = true;
            }
        }
        return result;
    }

    private static class CheckConnection implements Runnable {

        @Override
        public void run() {
            boolean checkconnect = true;
            try {
                while(checkconnect) {
                    Log.i(LOG_TAG,"Checking Connection!!");

                    if(haveInternet())
                    {
                        DeviceInfo.awsIotDevList = getIotThingList();
                        Log.i(LOG_TAG, "awsIoTSubTopiclist=" + DeviceInfo.awsIotDevList);
                        Log.i(LOG_TAG, "Already Connect to Internet!! ");
                        loadCertificate();
                        connectAwsIot();
                        checkconnect = false;
                    }
                    Thread.sleep(10000);
                }

            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    public static ArrayList getIotThingList()
    {
        ListThingsRequest listThingsRequest = new ListThingsRequest();
        ListThingsResult thingsResult = mIotAndroidClient.listThings(listThingsRequest);
        ArrayList list = new ArrayList();

        for(int i = 0; i<thingsResult.getThings().size(); i++)
        {
            list.add(thingsResult.getThings().get(i).getThingName());
        }

        Log.i(LOG_TAG,"Things List=" + list);
        return list;
    }


    public static void returnDeviceListToAWS(){
        List<ZwaveDevice> list;

        list = ZwaveDeviceManager.getInstance(ZwaveApplication.getInstance()).queryZwaveDeviceList();

        JSONObject jo = new JSONObject();
        JSONArray Jarray= new JSONArray();

        try {
            jo.put("Interface", "getDeviceList");
            for (int idx = 0; idx < list.size(); idx++) {
                if (list.get(idx).getNodeId() != 1) {
                    JSONObject json = new JSONObject();
                    json.put("brand", list.get(idx).getBrand());
                    json.put("nodeId", String.valueOf(list.get(idx).getNodeId()));
                    json.put("deviceType", list.get(idx).getDevType());
                    json.put("name", list.get(idx).getName());
                    json.put("category", list.get(idx).getCategory());
                    json.put("Room", list.get(idx).getRoomName());
                    json.put("isFavorite", list.get(idx).getFavorite());
                    json.put("timestamp", list.get(idx).getTimestamp());
                    json.put("deviceId", list.get(idx).getDevType()+String.valueOf(list.get(idx).getNodeId()));
                    Jarray.put(json);
                }
            }
            jo.put("deviceList", Jarray);

            JSONObject data=new JSONObject();
            data.put("data", jo);

            JSONObject report=new JSONObject();
            report.put("desired", data);

            JSONObject state=new JSONObject();
            state.put("state", report);


            String subcribe_topic = "$aws/things/"+ Const.PublicTopicName +"/shadow/get";
            mqttManager.publishString("",subcribe_topic, AWSIotMqttQos.QOS1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static boolean subscribeAwsMQTT(String topic)
    {

        Log.d(LOG_TAG, "subscribeAwsMQTT topic = " + topic);
        String subcribe_topic = "$aws/things/"+ topic +"/shadow/update/accepted";

        try {
            mqttManager.subscribeToTopic(subcribe_topic, AWSIotMqttQos.QOS1,
                new AWSIotMqttNewMessageCallback() {
                    @Override
                    public void onMessageArrived(final String topic, final byte[] data) {

                        try {
                            String crt_val = null;

                            String message = new String(data, "UTF-8");
                            Log.d(LOG_TAG, "Message arrived:");
                            Log.d(LOG_TAG, "   Topic: " + topic);
                            Log.d(LOG_TAG, " Message: " + message);
                            String[] split_line = topic.split("/");
                            Log.d(LOG_TAG, "get topic =  " + split_line[2]);

                            if (message.contains("desired")) {

                                JSONObject j = new JSONObject(message);

                                String command = j.getJSONObject("state").getJSONObject("desired").getString("data");
                                Log.i(LOG_TAG, "Remote MQTT data=" + command);

                                MQTTBroker.handleMqttIncomingMessage(split_line[2], command);
                            }

                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        } catch (Exception e) {
            Log.e(LOG_TAG, "Subscription error.", e);
            return false;
        }
        return true;
    }
}
