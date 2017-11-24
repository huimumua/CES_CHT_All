package com.askey.mobile.zwave.control.welcome.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.home.activity.HomeActivity;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;
import com.askey.mobile.zwave.control.util.PreferencesUtils;
import com.askey.mobile.zwave.control.welcome.udp.UdpConnect;

public class WelcomeActivity extends BaseActivity {
    private String TAG = "WelcomeActivity";
    private UdpConnect mUdpConnect;
    //判断用户是否是第一次使用该应用
    private boolean isFirstUse = false;
    //延时时间，用于由欢迎界面进入另外的页面的延时效果
    private static final int TIME = 2*1000;
    private static final int TO_MAIN = 100001;
    private static final int TO_GUIDE = 100002;
    private AlertDialog alertDialog;


    //由于不能在主线程中直接延时，所以用一个Handler来处理发送过来的消息
   Handler myHandler = new Handler(){
       public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                 case TO_MAIN:


                   break;
                case TO_GUIDE:
                    Intent i2 = new Intent(mContext,GuideActivity.class);
                    startActivity(i2);
                    finish();
                 break;
                 }
            };
        };


    public Runnable onGatwayFindRunnable = new Runnable() {
        @Override
        public void run() {
            //udp广播已获取到相关信息
            Logg.i(TAG, "=====onGatwayFindRunnable========");
            Const.serverUri = mUdpConnect.getServerUri();
            Const.subscriptionTopic = mUdpConnect.getTopic();
            Const.TUTK_TUUID = mUdpConnect.getTutk_tuuid();
            Log.i(TAG, "=====android Id=====" + Const.subscriptionTopic);
            Log.i(TAG, "=====TUTK_TUUID=====" + Const.TUTK_TUUID);
            //保存topic供外网使用
            PreferencesUtils.put(mContext,Const.TUTK_TUUID_TAG,mUdpConnect.getTutk_tuuid());
            PreferencesUtils.put(mContext,Const.TOPIC_TAG,mUdpConnect.getTopic());
            Logg.i(TAG, "serverUri ->  : " + Const.serverUri + "topic ->  : " + Const.subscriptionTopic);
            myHandler.sendEmptyMessageDelayed(TO_GUIDE, TIME);
//            initLocalMqtt();
        }
    };

    Runnable oncanNotFindGWayRunnable = new Runnable() {
        @Override
        public void run() {
            Logg.i(TAG, "=====oncanNotFindGWayRunnable========");
            String deviceId = (String) PreferencesUtils.get(mContext, Const.TOPIC_TAG, "");
            Logg.i(TAG, "=====oncanNotFindGWayRunnable===deviceId====="+deviceId);
            String tutk_uuid = (String) PreferencesUtils.get(mContext, Const.TUTK_TUUID_TAG, "");
            Logg.i(TAG, "=====oncanNotFindGWayRunnable===tutk_uuid====="+tutk_uuid);
            Const.isRemote = true;
            if(!deviceId.equals("") && !tutk_uuid.equals("")  ){
                Const.TUTK_TUUID = tutk_uuid;
                Const.subscriptionTopic = deviceId ;
            }
            myHandler.sendEmptyMessageDelayed(TO_GUIDE, TIME);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mUdpConnect = new UdpConnect();
        mUdpConnect.search(this, onGatwayFindRunnable, oncanNotFindGWayRunnable);

//       String str =  LocalMqttData.getDeviceListCommand("ALL");
//        Logg.i(TAG,"==str===" + str );

    }

    private void init() {
       //将用户是否是第一次使用的值用SharedPreferences存储到本地
        isFirstUse = (boolean)PreferencesUtils.get(this,"isFirstUse",true);
        if (!isFirstUse) {
            myHandler.sendEmptyMessageDelayed(TO_MAIN, TIME);
        }else{
            myHandler.sendEmptyMessageDelayed(TO_GUIDE, TIME);
            PreferencesUtils.put(this,"",false);
        }
      }


    private void initLocalMqtt() {
        MQTTManagement mqttManagement = MQTTManagement.getSingInstance();
        mqttManagement.initMqttCallback(Const.clientId, Const.serverUri, new MQTTManagement.initMqttCallback() {
            @Override
            public void initMQTT(boolean result) {
                if (result) {
                    Intent intent = new Intent();
//                    intent.setClass(mContext, HomePageActivity.class);
                    intent.setClass(mContext, HomeActivity.class);
                    mContext.startActivity(intent);
                    finish();
                } else {
                    //目前先不管出错情况
                    init();
                    showFailedConnectMQTTDialog();
                }
            }
        });
    }

    private void showFailedConnectMQTTDialog() {
        if(alertDialog == null){
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
                    Logg.i(TAG,"=showFailedConnectMQTTDialog=goWan=");
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
        if(alertDialog!=null){
            alertDialog.dismiss();
            alertDialog=null;
        }
    }

}
