package com.askey.mobile.zwave.control.home.activity.addDevice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.net.SocketTransceiver;
import com.askey.mobile.zwave.control.deviceContr.net.TCPReceive;
import com.askey.mobile.zwave.control.deviceContr.net.TcpClient;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class InstallDeviceActivity extends BaseActivity implements View.OnClickListener {
    public static String LOG_TAG = "InstallDeviceActivity";
    private ImageView step_iv;
    private TextView step_title, step_notify,addStstus;
    private ImageView step_anim;
    private ProgressBar progressBar;
    private String brand = "";
    private String deviceType = "";
    private String nodeId = "";
    private Timer timer;

    private Handler mHandler = new Handler() {
        // 重写handleMessage()方法，此方法在UI线程运行
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Const.TCP_TIMEOUT:
                    hideProgressDialog();
                    timerCancel();
                    if (!InstallDeviceActivity.this.isFinishing()) {
                        showFailedAddZaveDialog("Pairing time out");
                    }
                    break;
            }
        }
    };


    private void timerCancel() {
        if(timer!=null){
            timer.cancel();
            timer=null;
        }
    }

    class RemoteTimerTask extends TimerTask {
        public void run() {
            Message message = new Message();
            message.what = Const.TCP_TIMEOUT;
            mHandler.sendMessage(message);
            timerCancel();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_device);
        initView();

        brand = getIntent().getStringExtra("brand");
        deviceType = getIntent().getStringExtra("deviceType");

        MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
        TcpClient.getInstance().rigister(tcpReceive);

        Logg.i(LOG_TAG,"onCreate- > ---- ");
        //预留的接口mqtt
        if (TcpClient.getInstance().isConnected()) {
            Logg.i(LOG_TAG,"TcpClient - > isConnected ");
            TcpClient.getInstance().getTransceiver().send("mobile_zwave:addDevice:Zwave");
            timer = new Timer(true);
            timer.schedule(new InstallDeviceActivity.RemoteTimerTask(),1000*60); //延时1000ms后执行，1000ms执行一次
        }


    }


    MqttMessageArrived mMqttMessageArrived = new MqttMessageArrived() {
        @Override
        public void mqttMessageArrived(String topic, MqttMessage message) {
            final String result = new String(message.getPayload());
            Logg.i(LOG_TAG,"=mqttMessageArrived=>=topic="+topic);
            Logg.i(LOG_TAG,"=mqttMessageArrived=>=message="+result);
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        String reported = jsonObject.optString("reported");
                        JSONObject reportedObject = new JSONObject(reported);
                        String Interface = reportedObject.optString("Interface");
                        if(Interface.equals("addDevice")){
                            String result = reportedObject.optString("Result");
                            String NodeId = reportedObject.optString("NodeId");
                            if(result.equals("true")){
                                Intent intent = new Intent();
                                intent.setClass(mContext,InstallSuccessActivity.class);
                                intent.putExtra("brand",brand);
                                intent.putExtra("deviceType",deviceType);
                                intent.putExtra("nodeId",NodeId);
                                startActivity(intent);
                                finish();
                            }else{
                                ((Activity) mContext).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(mContext,"Add Device Fail ! ",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    TCPReceive tcpReceive = new TCPReceive() {
        @Override
        public void onConnect(SocketTransceiver transceiver) {

        }

        @Override
        public void onConnectFailed() {

        }

        @Override
        public void receiveMessage(SocketTransceiver transceiver, String tcpMassage) {
            Logg.i(LOG_TAG,"=TCPReceive=>=receiveMessage="+tcpMassage);
            //在这里处理结果
            addDeviceResult(tcpMassage);
        }

        @Override
        public void onDisconnect(SocketTransceiver transceiver) {

        }

    };

    //mqtt调用返回结果
    private void addDeviceResult(String result) {
        try {
            Logg.i(LOG_TAG,"=====result=="+result);
            if(result.contains("addDevice")){
                return;
            }else{
                final JSONObject jsonObject = new JSONObject(result);
//            final String nodeId = jsonObject.optString("NodeID");
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String messageType = jsonObject.optString("MessageType");
                        String status = jsonObject.optString("Status");
                        if ("Node Add Status".equals(messageType)) {
                            if ("Success".equals(status)) {
                                Toast.makeText(mContext,"add Success",Toast.LENGTH_SHORT).show();
                                Logg.i(LOG_TAG,"=====result=="+"Success");
                                addStstus.setText("Success, Please wait a moment to rename");
                                progressBar.setIndeterminate(false);
                            } else if ("Failed".equals(status)) {
                                Logg.i(LOG_TAG,"=====result=="+"Fail");
                                showAddFailDialog();
                                progressBar.setIndeterminate(false);
                            } else if("Learn Ready".equals(status)){
                                addStstus.setText("Please press the trigger button of the device");
                                //10S后 超时后 调用StopAddDevice();


                                timerCancel();
                            }else{
                                Logg.i(LOG_TAG,"=====result=="+status);
                                addStstus.setText(status);
                            }
                        }
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void showAddFailDialog() {
        final AlertDialog.Builder addDialog = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.dialog_normal_layout, null);
        addDialog.setView(view);
        final AlertDialog alertDialog = addDialog.create();

        TextView title = (TextView) view.findViewById(R.id.title);
        TextView message = (TextView) view.findViewById(R.id.message);
        title.setText("Prompt");
        message.setText("Add faild");
        TextView positiveButton = (TextView) view.findViewById(R.id.positiveButton);
        TextView negativeButton = (TextView) view.findViewById(R.id.negativeButton);
        positiveButton.setText("retry");
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击重试，返回添加设备界面，再次执行添加设备
                progressBar.setIndeterminate(true);

                //预留的接口mqtt
                if (TcpClient.getInstance().isConnected()) {
                    TcpClient.getInstance().getTransceiver().send("mobile_zwave:addDevice");
                }

                alertDialog.dismiss();
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击取消，返回主页

                alertDialog.dismiss();
                finish();

            }
        });

        alertDialog.show();
    }

    public void showFailedAddZaveDialog(final String titleStr) {
        final android.support.v7.app.AlertDialog.Builder addDialog = new android.support.v7.app.AlertDialog.Builder(mContext);
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.dialog_normal_layout, null);
        addDialog.setView(view);
        final android.support.v7.app.AlertDialog alertDialog = addDialog.create();
        //设置dialog背景透明，目的是为了实现弹窗的圆角
        alertDialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
        TextView title = (TextView) view.findViewById(R.id.title);
        TextView message = (TextView) view.findViewById(R.id.message);
        title.setText("Pairing time out");
        message.setText(titleStr);
        TextView positiveButton = (TextView) view.findViewById(R.id.positiveButton);
        TextView negativeButton = (TextView) view.findViewById(R.id.negativeButton);
        positiveButton.setText("Again");
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击重试，返回添加设备界面，再次执行添加设备
                //预留的接口mqtt
                if (TcpClient.getInstance().isConnected()) {
                    Logg.i(LOG_TAG, "TcpClient - > isConnected ");
                    TcpClient.getInstance().getTransceiver().send("mobile_zwave:addDevice");
//                    timer = new Timer(true);
//                    timer.schedule(task,1000*60); //延时1000ms后执行，1000ms执行一次
                    alertDialog.dismiss();
                }
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }


    private void initView() {
        step_iv = (ImageView) findViewById(R.id.step_iv);
        step_iv.setOnClickListener(this);
        step_title = (TextView) findViewById(R.id.step_title);
        step_notify = (TextView) findViewById(R.id.step_notify);
        step_anim = (ImageView) findViewById(R.id.step_anim);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        addStstus = (TextView) findViewById(R.id.add_status);
    }

    /*
    此处为安装成功的跳转，若安装失败则弹出一个dialog
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.step_iv:
                if( !nodeId.equals("")){
                    Intent intent = new Intent();
                    intent.setClass(this,InstallSuccessActivity.class);
                    intent.putExtra("brand",brand);
                    intent.putExtra("deviceType",deviceType);
                    intent.putExtra("nodeId",nodeId);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(mContext,"Please wait a moment...",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logg.i(LOG_TAG,"===onStop=====");
        unrigister();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logg.i(LOG_TAG,"===onDestroy=====");
    }

    private void unrigister() {
        if(tcpReceive!=null){
            TcpClient.getInstance().unrigister(tcpReceive);
        }
        if(mMqttMessageArrived!=null){
            MQTTManagement.getSingInstance().unrigister(mMqttMessageArrived);
        }
    }



}
