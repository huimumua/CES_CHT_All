package com.askey.mobile.zwave.control.welcome.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.net.TcpClient;
import com.askey.mobile.zwave.control.home.activity.HomeActivity;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;
import com.askey.mobile.zwave.control.util.PreferencesUtils;
import com.askey.mobile.zwave.control.welcome.udp.UdpConnect;

public class WelcomeActivity extends BaseActivity {
    private static final int REQUEST_CAMERA = 0;
    private String TAG = "WelcomeActivity";
    private UdpConnect mUdpConnect;
    //延时时间，用于由欢迎界面进入另外的页面的延时效果
    private static final int TO_TIME_OUT = 1000;
    private static final int TO_MAIN = 100001;
    private static final int TO_GUIDE = 100002;
    private AlertDialog alertDialog;

    //由于不能在主线程中直接延时，所以用一个Handler来处理发送过来的消息
     Handler myHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case TO_MAIN:
                    stopWaitDialog();
                    Intent intent = new Intent(mContext, HomeActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case TO_GUIDE:
                    stopWaitDialog();
                    Intent i2 = new Intent(mContext, GuideActivity.class);
                    startActivity(i2);
                    break;
            }
        }
    };


    public Runnable onGatwayFindRunnable = new Runnable() {
        @Override
        public void run() {
            //udp广播已获取到相关信息
            Const.serverUri = mUdpConnect.getServerUri();
            Const.subscriptionTopic = mUdpConnect.getTopic();
            Const.TUTK_TUUID = mUdpConnect.getTutk_tuuid();
            Log.i(TAG, "==onGatwayFindRunnable===android Id=====" + Const.subscriptionTopic);
            Log.i(TAG, "==onGatwayFindRunnable===TUTK_TUUID=====" + Const.TUTK_TUUID);
            Log.i(TAG, "==onGatwayFindRunnable===getServerUri=====" + Const.serverUri);
            Log.i(TAG, "==onGatwayFindRunnable===getServerip=====" + Const.SERVER_IP);
            //保存topic供外网使用
            PreferencesUtils.put(mContext, Const.SERVER_URI_TAG, mUdpConnect.getServerUri());
            PreferencesUtils.put(mContext, Const.SERVER_IP_TAG, Const.SERVER_IP);
            PreferencesUtils.put(mContext, Const.TUTK_TUUID_TAG, mUdpConnect.getTutk_tuuid());
            PreferencesUtils.put(mContext, Const.TOPIC_TAG, mUdpConnect.getTopic());
            Logg.i(TAG, "serverUri ->  : " + Const.serverUri + "topic ->  : " + Const.subscriptionTopic);

            myHandler.sendEmptyMessageDelayed(TO_GUIDE, TO_TIME_OUT);

        }
    };

    Runnable oncanNotFindGWayRunnable = new Runnable() {
        @Override
        public void run() {
            String deviceId = (String) PreferencesUtils.get(mContext, Const.TOPIC_TAG, "");
            Logg.i(TAG, "=====oncanNotFindGWayRunnable===deviceId=====" + deviceId);
            String tutk_uuid = (String) PreferencesUtils.get(mContext, Const.TUTK_TUUID_TAG, "");
            Logg.i(TAG, "=====oncanNotFindGWayRunnable===tutk_uuid=====" + tutk_uuid);
            Const.isRemote = true;
            if (!deviceId.equals("") && !tutk_uuid.equals("")) {
                Const.TUTK_TUUID = tutk_uuid;
                Const.subscriptionTopic = deviceId;
            }

            myHandler.sendEmptyMessageDelayed(TO_GUIDE, TO_TIME_OUT);

            //以下代码是不展示引导页至直接进入系统前应该的操作

//            String serverIp = (String) PreferencesUtils.get(mContext, Const.SERVER_IP_TAG, "");
//            if (!"".equals(serverIp)) {
//                Const.SERVER_IP = serverIp;
//            }
//            tcpConnect(Const.SERVER_IP, Const.TCP_PORT);
//            initLocalMqtt();

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        if (Build.VERSION.SDK_INT >= 23) {
            getPermission();
        } else {
            showWaitingDialog();
            mUdpConnect = new UdpConnect();
            mUdpConnect.search(this, onGatwayFindRunnable, oncanNotFindGWayRunnable);
        }



    }

    private void getPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            //未授权，提起权限申请
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                //用于开发者提示用户权限的用途
                Toast.makeText(this, "您已禁止该权限，需要重新开启。", Toast.LENGTH_SHORT).show();
            } else {
                //申请权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_CAMERA);

            }

        } else {
            //权限已授权，功能操作
            showWaitingDialog();
            mUdpConnect = new UdpConnect();
            mUdpConnect.search(this, onGatwayFindRunnable, oncanNotFindGWayRunnable);
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        //判断请求码，确定当前申请的权限
        if (requestCode == REQUEST_CAMERA) {
            //判断权限是否申请通过
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //授权成功
                showWaitingDialog();
                mUdpConnect = new UdpConnect();
                mUdpConnect.search(this, onGatwayFindRunnable, oncanNotFindGWayRunnable);
            } else {
                //授权失败
                Toast.makeText(this, "您已禁止该权限，需要重新开启。", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    private void initLocalMqtt() {
        String serverUri = (String) PreferencesUtils.get(mContext, Const.SERVER_URI_TAG, "");
        if (!"".equals(serverUri)) {
            Const.serverUri = serverUri;
        }
        Logg.i(TAG, "===initLocalMqtt===Const.serverUri=====" + Const.serverUri);
        Logg.i(TAG, "===initLocalMqtt===Const.clientId=====" + Const.clientId);
        MQTTManagement mqttManagement = MQTTManagement.getSingInstance();
        mqttManagement.initMqttCallback(Const.clientId, Const.serverUri, new MQTTManagement.initMqttCallback() {
            @Override
            public void initMQTT(boolean result) {
                if (result) {
                    stopWaitDialog();
                    Log.d(TAG, "==initMqttCallback==" + Thread.currentThread().getName());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            guide2HomeActivity(WelcomeActivity.this);
                            myHandler.sendEmptyMessageDelayed(TO_MAIN, TO_TIME_OUT);
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




    //tcp连接
    private void tcpConnect(String tcpServer, int tcpPort) {
        try {
            if (!TcpClient.getInstance().isConnected()) {
                TcpClient.getInstance().connect(tcpServer, tcpPort);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Logg.e(TAG, "==tcpConnect=Exception=" + e.getMessage());
        }
    }


    private void showFailedConnectMQTTDialog() {
        if (WelcomeActivity.this.isFinishing()) {
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
                    showWaitingDialog("Initializing，Create an MQTT link...");
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

}
