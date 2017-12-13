package com.askey.mobile.zwave.control.guideSetting.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.model.DeviceList;
import com.askey.mobile.zwave.control.deviceContr.net.SocketTransceiver;
import com.askey.mobile.zwave.control.deviceContr.net.TCPReceive;
import com.askey.mobile.zwave.control.deviceContr.net.TcpClient;
import com.askey.mobile.zwave.control.home.activity.HomeActivity;
import com.askey.mobile.zwave.control.login.ui.LogInActivity;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.ImageUtils;
import com.askey.mobile.zwave.control.util.Logg;
import com.askey.mobile.zwave.control.util.PreferencesUtils;
import com.askeycloud.sdk.device.response.AWSIoTCertResponse;
import com.askeycloud.sdk.device.response.IoTDeviceInfoResponse;
import com.askeycloud.webservice.sdk.iot.AskeyIoTUtils;
import com.askeycloud.webservice.sdk.iot.callback.MqttConnectionCallback;
import com.askeycloud.webservice.sdk.iot.callback.MqttServiceConnectedCallback;
import com.askeycloud.webservice.sdk.model.ServicePreference;
import com.askeycloud.webservice.sdk.model.auth.v3.DeviceProvidersQueryOptions;
import com.askeycloud.webservice.sdk.service.device.AskeyIoTDeviceService;
import com.askeycloud.webservice.sdk.service.iot.AskeyIoTService;
import com.askeycloud.webservice.sdk.service.web.AskeyWebService;
import com.askeycloud.webservice.sdk.task.DeviceOAuthApiCallback;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ExecutionException;


public class DeviceGuideHomeActivity extends BaseActivity {
    private static String TAG = "DeviceGuideHomeActivity";
    private String cert ,pk ,userId ;
    private AlertDialog alertDialog;
    private String mqttResult;
    private String back_img_src;
    private static final String BACK_IMG_SRC = "backgroundImg";
    private IoTDeviceInfoResponse mIoTDeviceInfoResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_guide_home);

        showWaitingDialog();

        deviceAuth();

    }

    MqttMessageArrived mqttMessageArrived = new MqttMessageArrived() {
        @Override
        public void mqttMessageArrived(String topic, MqttMessage message) {
            mqttResult = new String(message.getPayload());
            Logg.i(TAG, "=mqttMessageArrived=>=topic=" + topic);
            Logg.i(TAG, "=mqttMessageArrived=>=message=" + mqttResult);
            //解析数据  将数据装入data后展示出来

            getDeviceResult(mqttResult);
            MQTTManagement.getSingInstance().unrigister(mqttMessageArrived);
        }
    };


    TCPReceive tcpReceive = new TCPReceive() {
        @Override
        public void onConnect(SocketTransceiver transceiver) {
            Logg.i(TAG, "=%%%%%%%%%%%%%%%%%%%%%=onConnect=");

        }

        @Override
        public void onConnectFailed() {
            Logg.i(TAG, "=%%%%%%%%%%%%%%%%%%%%%=onConnectFailed=");
        }

        @Override
        public void receiveMessage(SocketTransceiver transceiver, String tcpMassage) {
            Logg.i(TAG, "=TCPReceive=>=receiveMessage=" + tcpMassage);
            //在这里处理结果
            if (tcpMassage.contains("setDefault:0")) {
                Logg.i(TAG, "====setDefault:0====");
/*                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic,"mobile_zwave:getDevices");
                        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getDeviceListCommand("All"));
                    }
                });*/

            }
        }

        @Override
        public void onDisconnect(SocketTransceiver transceiver) {
            Logg.i(TAG, "=%%%%%%%%%%%%%%%%%%%%%=onDisconnect=");
        }


    };

    //mqtt调用返回结果
    private void getDeviceResult(String result) {
        DeviceList deviceList = new Gson().fromJson(result, DeviceList.class);
        List<DeviceList.NodeInfoList> temp = deviceList.getNodeList();
        for (DeviceList.NodeInfoList nodeInfoTemp : temp) {
            String nodeId = nodeInfoTemp.getNodeId();
            String nodeTopic = Const.subscriptionTopic + "Zwave" + nodeId;
            // 订阅新设备的topic为 sn + nodeId
            MQTTManagement.getSingInstance().subscribeToTopic(nodeTopic, null);
        }

        Logg.i(TAG, "====getDeviceResult===result==" + result);

    }


    //tcp连接
    private void tcpConnect(String tcpServer, int tcpPort) {
        try {
            if(!TcpClient.getInstance().isConnected()){
                TcpClient.getInstance().connect(tcpServer, tcpPort);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Logg.i(TAG,"==tcpConnect=Exception="+e.getMessage());
        }
    }

    private void initLocalMqtt() {
        MQTTManagement mqttManagement = MQTTManagement.getSingInstance();
        mqttManagement.initMqttCallback(Const.clientId, Const.serverUri, new MQTTManagement.initMqttCallback() {
            @Override
            public void initMQTT(boolean result) {
                if (result) {
                    stopWaitDialog();
                    Log.d("device", Thread.currentThread().getName());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            guide2HomeActivity();
                        }
                    }).start();
                } else {
                    //目前先不管出错情况
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showFailedConnectMQTTDialog();
                        }
                    });
                }
            }
        });
    }

    private void showFailedConnectMQTTDialog() {
        if (DeviceGuideHomeActivity.this.isFinishing()) {
            return;
        }
        if (alertDialog == null) {
            AlertDialog.Builder addDialog = new AlertDialog.Builder(mContext);
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            View view = layoutInflater.inflate(R.layout.dialog_retry_layout, null);
            addDialog.setView(view);
            alertDialog = addDialog.create();

            TextView title = (TextView) view.findViewById(R.id.title);
            TextView message = (TextView) view.findViewById(R.id.message);
            title.setText("Prompt");
            message.setText("MQTT init faild");
            Button positiveButton = (Button) view.findViewById(R.id.positiveButton);
            Button negativeButton = (Button) view.findViewById(R.id.negativeButton);
            Button go_wan = (Button) view.findViewById(R.id.go_wan);
            positiveButton.setText("retry");
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //点击重试，返回添加设备界面，再次执行添加设备
                    showProgressDialog(mContext, "Initializing，Create an MQTT link...");
                    initLocalMqtt();
                    alertDialogCancel();
                }
            });
            go_wan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Logg.i(TAG, "=showFailedConnectMQTTDialog=goWan=");
                    hideProgressDialog();
                    showProgressDialog(mContext, "Initializing，Create an MQTT link...");
                    Const.isRemote = true;
                    alertDialogCancel();
                }
            });
            negativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //点击取消，返回主页
                    finish();
                    alertDialogCancel();
                }
            });
            alertDialog.show();
        }
    }

    private void alertDialogCancel() {
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

    //登录之后调用
    private void deviceAuth() {
        boolean result = ServicePreference.isAuthV3UserDataExist(mContext);
        Logg.i(TAG, "===deviceAuth===isAuthV3UserDataExist==" + result);
        String tutk_uuid = (String) PreferencesUtils.get(mContext, Const.TUTK_TUUID_TAG, "");

        DeviceProvidersQueryOptions options = new DeviceProvidersQueryOptions();
        Logg.i(TAG, "==deviceAuth====Const.AUTH_APP_ID===" + Const.AUTH_APP_ID);
        Logg.i(TAG, "==deviceAuth====DEVICE_MODEL===" + Const.DEVICE_MODEL);
        Logg.i(TAG, "==deviceAuth====tutk_uuid===" + tutk_uuid);
        options.setDeviceAuthAppId(Const.AUTH_APP_ID);
        options.setDeviceModel(Const.DEVICE_MODEL);
        options.setDeviceAuthUniqueId(tutk_uuid/*"be31eb33253d1cc7"*/);//只要是唯一的字串參數就好

        DeviceOAuthApiCallback deviceOAuthApiCallback = new DeviceOAuthApiCallback() {
            @Override
            public void bindingDeviceSuccess(int i) {
                Logg.i(TAG, "===deviceOAuthApiCallback===success===" + i);

                lookupIoTDevice(mContext);
            }

            @Override
            public void bindingDeviceError(int i, String s, String s1) {
                Logg.i(TAG, "======success===" + " i=" + i + " s=" + s + " s1= " + s1);
            }
        };
        AskeyWebService.getInstance(this).activeDeviceV3(options, Const.AUTH_APP_ID, deviceOAuthApiCallback);
    }

    /**
     * 检查设备是否存在
     */
    public void lookupIoTDevice(final Context mcontext) {
        new Thread(new Runnable() {
            @Override
            public void run() {
//              String deviceId = (String) PreferencesUtils.get(mContext, Const.TOPIC_TAG, "");
                String tutk_uuid = (String) PreferencesUtils.get(mContext, Const.TUTK_TUUID_TAG, "");
                Logg.i(TAG, "=====lookupIoTDevice===tutk_uuid=====" + tutk_uuid);
                mIoTDeviceInfoResponse = AskeyIoTDeviceService.getInstance(mcontext).lookupIoTDevice(Const.DEVICE_MODEL, tutk_uuid);
                if (mIoTDeviceInfoResponse != null && !"".equals(mIoTDeviceInfoResponse)) {
                    int code = mIoTDeviceInfoResponse.getCode();
                    Logg.i(TAG, "===lookupIoTDevice===DEVICE_MODEL====" + Const.DEVICE_MODEL);
                    Logg.i(TAG, "===lookupIoTDevice===deviceId====" + Const.subscriptionTopic);
                    Logg.i(TAG, "===lookupIoTDevice===getCode====" + mIoTDeviceInfoResponse.getCode());
                    Logg.i(TAG, "===lookupIoTDevice===getMessage====" + mIoTDeviceInfoResponse.getMessage());
                    Logg.i(TAG, "===lookupIoTDevice===getAddtionMessage====" + mIoTDeviceInfoResponse.getAddtionMessage());

                    if (400005 == code || 403 == code) {
                        //设备不存在  进行创建
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        lookupIoTDevice(mcontext);
                    } else if (400006 == code) {
                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showLookupIoTDialog();
                            }
                        });
                    } else if (200 == code) {
                        //存在成功
                        getCertificateKey();
                    }

                }

            }
        }).start();

    }


    public void getCertificateKey() {
        AWSIoTCertResponse response = AskeyIoTDeviceService.getInstance(appContext).getIotCert();
        Logg.i(TAG, "==getCertificateKey====response=" + response);
        AskeyIoTService.getInstance(appContext).changeMQTTQos(AWSIotMqttQos.QOS1);
        if (response != null) {
            response = AskeyIoTDeviceService.getInstance(appContext).getIotCert();
            cert = response.getCertificatePem();
            pk = response.getPrivateKey();
            userId = response.getUserid();
            Logg.i(TAG, "==getCertificateKey===getCertificatePem==" + cert);
            Logg.i(TAG, "==getCertificateKey====getPrivateKey==" + pk);
            Logg.i(TAG, "==getCertificateKey====getUserid==" + userId);
            if (userId !=null && pk !=null &&  cert !=null && !"".equals(userId) && !"".equals(cert) && !"".equals(pk)) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (mIoTDeviceInfoResponse != null) {
                            Logg.i(TAG, "=mIoTDeviceInfoResponse.getRestEndpoint()==" + mIoTDeviceInfoResponse.getRestEndpoint());
                            if (mIoTDeviceInfoResponse.getRestEndpoint() != null) {
                                AskeyIoTService.getInstance(appContext).configAWSIot(
                                        AskeyIoTUtils.translatMqttUseEndpoint(mIoTDeviceInfoResponse.getRestEndpoint()),
                                        mqttServiceConnectedCallback
                                );
                            }
                        }
                    }
                }).start();

            }else{
                Logg.i(TAG, "===getCertificateKey===getIotCert =userId=null && pk =null &&  cert=null==");
            }

        }

    }


    MqttServiceConnectedCallback mqttServiceConnectedCallback = new MqttServiceConnectedCallback() {
        @Override
        public void onMqttServiceConnectedSuccess() {
            Logg.i(TAG, "===MqttServiceConnectedCallback===onMqttServiceConnectedSuccess===");

            AskeyIoTService.getInstance(appContext).connectToAWSIot(userId, cert, pk,
                    new MqttConnectionCallback() {
                        @Override
                        public void onConnected() {
                            Logg.i(TAG, "====MqttConnectionCallback==onConnected===");
                            if (mIoTDeviceInfoResponse != null) {
                                HomeActivity.shadowTopic = mIoTDeviceInfoResponse.getShadowTopic();
                                AskeyIoTService.getInstance(getApplicationContext()).subscribeMqtt(mIoTDeviceInfoResponse.getShadowTopic());
                                if (Const.isRemote) {
                                    guide2HomeActivity();
                                } else{
                                    TcpClient.getInstance().rigister(tcpReceive);
                                        tcpConnect(Const.SERVER_IP, Const.TCP_PORT);
                                    initLocalMqtt();
                                }
                            }
                        }

                        @Override
                        public void unConnected(AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus awsIotMqttClientStatus) {
                            Logg.i(TAG, "===MqttConnectionCallback===unConnected ===");

                        }
                    });
        }

        @Override
        public void onMqttServiceConnectedError() {
            Logg.i(TAG, "======onMqttServiceConnectedError===");
        }
    };


    private void showLookupIoTDialog() {
        final AlertDialog.Builder addDialog = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.dialog_normal_layout, null);
        addDialog.setView(view);
        final AlertDialog alertDialog = addDialog.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.color.transparent);

        TextView title = (TextView) view.findViewById(R.id.title);
        TextView message = (TextView) view.findViewById(R.id.message);
        title.setText("Prompt");
        message.setText("This user has no permission, please use other account login");
        TextView positiveButton = (TextView) view.findViewById(R.id.positiveButton);
        TextView negativeButton = (TextView) view.findViewById(R.id.negativeButton);
        positiveButton.setText("OK");
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //重新登录
                Intent intent = new Intent(mContext, LogInActivity.class);
                startActivity(intent);
                alertDialog.dismiss();
                finish();
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击取消，返回主页
                alertDialog.dismiss();
                finish();
                System.exit(0);
            }
        });

        alertDialog.show();
    }


    @Override
    protected void onStop() {
        super.onStop();
        Logg.i(TAG, "===onStop=====");
        unrigister();
        if (alertDialog != null) {
            alertDialog.cancel();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logg.i(TAG, "===onDestroy=====");
    }

    /*
        这个方法为耗时操作
     */
    private void guide2HomeActivity() {
        back_img_src = (String) PreferencesUtils.get(DeviceGuideHomeActivity.this, BACK_IMG_SRC, "");
        if (!back_img_src.equals("")) {
            final File file = new File(back_img_src);
            if (file.exists()) {
                Bitmap myBitmap = null;
                try {
                    myBitmap = Glide.with(DeviceGuideHomeActivity.this)
                            .load(file)
                            .asBitmap() //必须
                            .centerCrop()
                            .into(1080, 1920)
                            .get();

                } catch (InterruptedException e) {
                    e.printStackTrace();

                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                final Bitmap finalMyBitmap = myBitmap;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        BitmapDrawable drawable = new BitmapDrawable(getResources(), finalMyBitmap);
                        ImageUtils.setBackgroundImg(drawable);
                    }
                });
            }
        }
        stopWaitDialog();
        Intent intent = new Intent(mContext, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void unrigister() {
        if (tcpReceive != null) {
            TcpClient.getInstance().unrigister(tcpReceive);
        }
        if (mqttMessageArrived != null) {
            MQTTManagement.getSingInstance().unrigister(mqttMessageArrived);
        }
    }


}
