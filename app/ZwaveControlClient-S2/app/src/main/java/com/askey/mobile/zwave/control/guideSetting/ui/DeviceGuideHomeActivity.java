package com.askey.mobile.zwave.control.guideSetting.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.net.SocketTransceiver;
import com.askey.mobile.zwave.control.deviceContr.net.TCPReceive;
import com.askey.mobile.zwave.control.deviceContr.net.TcpClient;
import com.askey.mobile.zwave.control.home.activity.HomeActivity;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;
import com.askey.mobile.zwave.control.util.PreferencesUtils;


public class DeviceGuideHomeActivity extends BaseActivity {
    private static String TAG = "DeviceGuideHomeActivity";
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_guide_home);

        showWaitingDialog();

        //这里需要检测看是否已被绑定，如被绑定直接进入，未绑定则进行绑定操作

        String serverIp = (String) PreferencesUtils.get(mContext, Const.SERVER_IP_TAG, "");
        if(!"".equals(serverIp)){
            Const.SERVER_IP = serverIp;
        }
        TcpClient.getInstance().rigister(tcpReceive);
        tcpConnect(Const.SERVER_IP, Const.TCP_PORT);
        initLocalMqtt();


    }

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
            }
        }

        @Override
        public void onDisconnect(SocketTransceiver transceiver) {
            Logg.i(TAG, "=%%%%%%%%%%%%%%%%%%%%%=onDisconnect=");
        }


    };

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
        String serverUri ;
        if( Const.isRemote ){
            serverUri =   "tcp://211.75.141.112:"+ Const.MQTT_PORT;
            Log.i(TAG, "~~~~~~~~~~~94~initLocalMqtt: ");
        }else{
            Log.i(TAG, "~~~~~~~~~~~96~initLocalMqtt: ");
            serverUri = (String) PreferencesUtils.get(mContext, Const.SERVER_URI_TAG, "");
        }

        if(!"".equals(serverUri)){
            Const.serverUri = serverUri;
        }
        Logg.i(TAG,"===initLocalMqtt===Const.serverUri====="+Const.serverUri);
        Logg.i(TAG,"===initLocalMqtt===Const.clientId====="+Const.clientId);
        MQTTManagement mqttManagement = MQTTManagement.getSingInstance();
        mqttManagement.initMqttCallback(Const.clientId, Const.serverUri, new MQTTManagement.initMqttCallback() {
            @Override
            public void initMQTT(boolean result) {
                Log.i(TAG, "~~~~~~~~initMQTTresult107== "+result);
                if (result) {
                    stopWaitDialog();
                    Log.d(TAG, "==initMqttCallback=="+Thread.currentThread().getName());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            guide2HomeActivity(DeviceGuideHomeActivity.this);
                        }
                    }).start();
                } else {
                    //目前先不管出错情况
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                Log.i(TAG, "~~~~~~~~~~~~~~showFailedConnectMQTTDialog");
                            showFailedConnectMQTTDialog();
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void guide2HomeActivity(Context mContext) {
        super.guide2HomeActivity(mContext);
        Intent intent = new Intent(mContext, HomeActivity.class);
        startActivity(intent);
        finish();
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
                   stopWaitDialog();
                    showWaitingDialog( "Initializing，Create an MQTT link...");
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


    private void unrigister() {
        if (tcpReceive != null) {
            TcpClient.getInstance().unrigister(tcpReceive);
        }
    }


}
