package com.askey.mobile.zwave.control.home.activity.addDevice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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
import com.askey.mobile.zwave.control.util.ToastShow;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class InstallDeviceActivity extends BaseActivity implements View.OnClickListener {
    public static String LOG_TAG = "InstallDeviceActivity";
    private ImageView step_iv;
    private TextView step_title, step_notify, addStstus;
    private ImageView step_anim;
    private Button finishButton;
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
                    stopWaitDialog();
                    Logg.i(LOG_TAG, " TCP_TIMEOUT == add device time out");
                    timerCancel();
                    if (TcpClient.getInstance().isConnected()) {
                        Logg.i(LOG_TAG, "TcpClient -> send -> mobile_zwave:stopAddDevice:Zwave");
                        TcpClient.getInstance().getTransceiver().send("mobile_zwave:stopAddDevice:Zwave");
                    }
                    if (!InstallDeviceActivity.this.isFinishing()) {
                        ((InstallDeviceActivity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showFailedAddZaveDialog(mContext.getResources().getString(R.string.fail_add_notify));
                            }
                        });
                        //finish();
                    }
                    break;
            }
        }
    };


    private void timerCancel() {
        if (timer != null) {
            timer.cancel();
            timer = null;
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

        brand = getIntent().getStringExtra("brand");
        deviceType = getIntent().getStringExtra("deviceType");

        initView();

        MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
        TcpClient.getInstance().rigister(tcpReceive);

        Logg.i(LOG_TAG, "onCreate- > ---- ");
        //预留的接口mqtt
        if (TcpClient.getInstance().isConnected()) {
            Logg.i(LOG_TAG, "TcpClient - > isConnected ");
            TcpClient.getInstance().getTransceiver().send("mobile_zwave:addDevice:Zwave");
            //tcpTimeoutThread.start();

        }


    }

    @Override
    protected void onStop() {
        super.onStop();

        tcpTimeoutThread.interrupt(); //停止线程
        try {
            tcpTimeoutThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    Thread tcpTimeoutThread = new Thread(new Runnable() {
        @Override
        public void run() {
            timer = new Timer(true);
            timer.schedule(new InstallDeviceActivity.RemoteTimerTask(), Const.TCP_TIMER_TIMEOUT); //延时1000ms后执行，1000ms执行一次
        }
    });

    MqttMessageArrived mMqttMessageArrived = new MqttMessageArrived() {
        @Override
        public void mqttMessageArrived(String topic, MqttMessage message) {
            final String result = new String(message.getPayload());
            Logg.i(LOG_TAG, "=mqttMessageArrived=>=topic=" + topic);
            Logg.i(LOG_TAG, "=mqttMessageArrived=>=message=" + result);
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        String reported = jsonObject.optString("reported");
                        JSONObject reportedObject = new JSONObject(reported);
                        String Interface = reportedObject.optString("Interface");
                        if (Interface.equals("addDevice")) {
                            String result = reportedObject.optString("Result");
                            String NodeId = reportedObject.optString("NodeId");
                            if (result.equals("true")) {
                                Intent intent = new Intent();
                                intent.setClass(mContext, InstallSuccessActivity.class);
                                intent.putExtra("brand", brand);
                                intent.putExtra("deviceType", deviceType);
                                intent.putExtra("nodeId", NodeId);
                                Log.i("InstallDeviceActivity", "nodeId = " + NodeId);
                                startActivity(intent);
                                finish();
                            }
                            // MQTT返回Result : fail时处理，因为TCP处理了fail，所以这里暂时不处理
//                            else if(result.equals("fail")) {
//                                showAddFailDialog();
//                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    /**
     * add device新增流程
     * 发送addDeivce后等侍tcp返回
     * <p>
     * 返回是否输入DSK。   数据格式 dsk:dsk
     * 输入dsk后上传   数据格式 dsk:12345
     * <p>
     * 返回是否有Grant key 数据格式 GrantKeys:value
     * 先择Grant key后上传格式：Grant Keys:87
     * <p>
     * 返回是否有CSA 数据格式 CSA:CSA
     * 选择CSA后,数据格式  CSA:1 （1为YES，0为NO）
     * <p>
     * 如果以上三种（DSK、GrantKey、CSA）都不支持，addDevice()直接返回result:true/false
     */
    TCPReceive tcpReceive = new TCPReceive() {
        @Override
        public void onConnect(SocketTransceiver transceiver) {

        }

        @Override
        public void onConnectFailed() {

        }

        @Override
        public void receiveMessage(SocketTransceiver transceiver, String tcpMassage) {
            //在这里处理结果
            addDeviceResult(tcpMassage);
        }

        @Override
        public void onDisconnect(SocketTransceiver transceiver) {

        }

    };

    private void addDeviceResult(final String result) {
        try {
            Logg.i(LOG_TAG, "=====result==" + result);

            //格式dsk:dsk  GrantKeys:value CSA:CSA  截取 ：前面的字符
            String msgType = result.substring(0, result.lastIndexOf(":"));
            Log.i(LOG_TAG, "=======msgType==" + msgType);
            if (msgType.equals("dsk")) {
                //DSK输入框显示，step_iv按钮隐藏
                Intent intent = new Intent(mContext, DskActivity.class);
                startActivity(intent);
            } else if (msgType.equals("GrantKeys")) {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addStstus.setText("Processing....");
                    }
                });
                //消息格式Grant Keys:value ,截取 ：后面的字符串
                String keys = result.substring(result.indexOf(":") + 1);
                if (keys.equals("0")) {
                    //提示用户请求超时
                    Toast.makeText(mContext, "Network request timeout !", Toast.LENGTH_LONG).show();
                } else {
                    //将keys传到SecurityLevelActivity
                    Intent intent = new Intent(mContext, GrantKeyActivity.class);
                    intent.putExtra("SAFE_LEVEL", keys);
                    startActivity(intent);
                }
            } else if (msgType.equals("CSA")) {
                Intent intent = new Intent(mContext, CsaActivity.class);
                startActivity(intent);
            } else if (result.contains("mobile_zwave:addDevice:Zwave")) {//接口发送的时候自已的反馈
                return;
            } else if (result.contains("firefly_zwave:addDevice:other")) {//服务端返回的：有其他接口正在调用，没有处理完成
                timerCancel();
                final String addResult = mContext.getResources().getString(R.string.add_device_result);
                ToastShow.showToastOnUiThread(mContext, addResult);
                if (TcpClient.getInstance().isConnected()) {
                    TcpClient.getInstance().getTransceiver().send("mobile_zwave:stopAddDevice:Zwave");
                }
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addStstus.setText(addResult);
                    }
                });
//                finish();
            } else if (result.contains("addDevice:Zwave:")) {

            } else { //会返回添加过程的一些状态
                final JSONObject jsonObject = new JSONObject(result);
                //final String nodeId = jsonObject.optString("NodeID");
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String messageType = jsonObject.optString("MessageType");
                        String status = "";
                        String isAdded = "";
                        if (result.contains("Status")) {
                            status = jsonObject.optString("Status");
                        }
                        if(result.contains("NewAdded")){
                            isAdded = jsonObject.optString("NewAdded");
                        }

                        if ("Node Add Status".equals(messageType)) {

                            Log.i(LOG_TAG, "=======status:"+status);
                            if("No".equals(isAdded)){
                                addStstus.setText(getResources().getString(R.string.node_added)); //This node has already been included
                                //progressBar.setIndeterminate(false);//参数为true时，进度条采用不明确显示进度的‘模糊模式’
                                progressBar.setVisibility(View.GONE);
                                step_iv.setVisibility(View.GONE);
                                finishButton.setVisibility(View.VISIBLE);

                                //停止TCP超时的timer。不然90s后会报错
                                timerCancel();
                                Logg.i(LOG_TAG, "This node has already been included");

                            } else if ("Success".equals(status) && "Yes".equals(isAdded)) {
                                timerCancel();
                                Toast.makeText(mContext, "add Success", Toast.LENGTH_SHORT).show();
                                Logg.i(LOG_TAG, "=====result==" + "Success");
                                addStstus.setText(getResources().getString(R.string.add_device_success));//Success, Please wait a moment to rename
                                progressBar.setIndeterminate(false);
                            } else if ("Failed".equals(status)) {
                                Logg.i(LOG_TAG, "=====result==" + "Fail");
                                timerCancel();
                                showAddFailDialog(getResources().getString(R.string.add_failed));
                                progressBar.setIndeterminate(false);
                            } else if ("Learn Ready".equals(status)) {
                                addStstus.setText(getResources().getString(R.string.add_device_learn_ready));//Please press the trigger button of the device
                                //10S后 超时后 调用StopAddDevice();
                                //  timerCancel();
                            } else if ("Timeout".equals(status)) {
                                TcpClient.getInstance().getTransceiver().send("mobile_zwave:stopAddDevice:Zwave");
                                showFailedAddZaveDialog(getResources().getString(R.string.fail_add_notify));
                            } else if ("-17".equals(status)) {
                                Logg.i(LOG_TAG, "================jnierror=="+status);
                                timerCancel();
                                showAddFailDialog(getResources().getString(R.string.prompt_try_again));
                            } else {

                                boolean digit = false;

                                //whether error code
                                try {
                                    Integer.parseInt(status);
                                    digit = true;
                                }
                                catch (NumberFormatException e) {
                                    digit = false;
                                }

                                if(digit == true)
                                {
                                    timerCancel();
                                    String str = "Error Code: " + status;
                                    showAddFailDialog(str);
                                }
                                else {

                                    if ("Getting Node Information".equals(status)) {
                                        timerCancel();
                                    }
                                    addStstus.setText(status);
                                }
                            }

                        }
                    }
                });
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Logg.i(LOG_TAG, "errorJson------>" + result);
        }

    }

    private void showAddFailDialog(String message) {
        final AlertDialog.Builder addDialog = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.dialog_normal_layout, null);
        addDialog.setView(view);
        final AlertDialog alertDialog = addDialog.create();

        TextView title = (TextView) view.findViewById(R.id.title);
        TextView promptMessage = (TextView) view.findViewById(R.id.message);
        title.setText("Prompt");
        promptMessage.setText(message);
        TextView positiveButton = (TextView) view.findViewById(R.id.positiveButton);
        TextView negativeButton = (TextView) view.findViewById(R.id.negativeButton);
        positiveButton.setText("retry");
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击重试，返回添加设备界面，再次执行添加设备
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addStstus.setText("Please wait a moment...");
                    }
                });

                progressBar.setIndeterminate(true);
                //预留的接口mqtt
                if (TcpClient.getInstance().isConnected()) {
                    TcpClient.getInstance().getTransceiver().send("mobile_zwave:addDevice:Zwave");
                }

                alertDialog.dismiss();
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击取消，返回主页
                if (TcpClient.getInstance().isConnected()) {
                    TcpClient.getInstance().getTransceiver().send("mobile_zwave:stopAddDevice:Zwave"); //停止底层的所有操作
                }
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
        message.setText(titleStr);
        TextView positiveButton = (TextView) view.findViewById(R.id.positiveButton);
        TextView negativeButton = (TextView) view.findViewById(R.id.negativeButton);
        positiveButton.setText("Again");
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击重试，返回添加设备界面，再次执行添加设备
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addStstus.setText("Please wait a moment...");
                    }
                });
                //预留的接口mqtt
                if (TcpClient.getInstance().isConnected()) {
                    Logg.i(LOG_TAG, "TcpClient - > isConnected ");
                    TcpClient.getInstance().getTransceiver().send("mobile_zwave:addDevice:Zwave");
                    timer = new Timer(true);
                    timer.schedule(new InstallDeviceActivity.RemoteTimerTask(), Const.TCP_TIMER_TIMEOUT); //延时1000ms后执行，1000ms执行一次
                    alertDialog.dismiss();
                }
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                startActivity(new Intent(InstallDeviceActivity.this, SelectBrandActivity.class));
                finish();
            }
        });
        if (!alertDialog.isShowing()) {
            alertDialog.show();
        }
    }


    private void initView() {
        step_iv = (ImageView) findViewById(R.id.step_iv);
        step_iv.setOnClickListener(this);
        finishButton = (Button) findViewById(R.id.button_finish);
        finishButton.setOnClickListener(this);
        step_title = (TextView) findViewById(R.id.step_title);
        step_notify = (TextView) findViewById(R.id.step_notify);
        step_anim = (ImageView) findViewById(R.id.step_anim);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        addStstus = (TextView) findViewById(R.id.add_status);
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.color_e2231a), android.graphics.PorterDuff.Mode.MULTIPLY);
        if (deviceType.equals("BULB")) {
            step_notify.setText(R.string.bulb_installing);
        }
        if (deviceType.equals("PLUG")) {
            step_notify.setText(R.string.switch_installing);
        }
        if (deviceType.equals("WALLMOTE")) {
            step_notify.setText(R.string.wallmote_installing);
        }
        if (deviceType.equals("EXTENDER")) {
            step_notify.setText(R.string.extender_installing);
        }
    }

    /*
    此处为安装成功的跳转，若安装失败则弹出一个dialog
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.step_iv:
                if (!nodeId.equals("")) {
                    Intent intent = new Intent();
                    intent.setClass(this, InstallSuccessActivity.class);
                    intent.putExtra("brand", brand);
                    intent.putExtra("deviceType", deviceType);
                    intent.putExtra("nodeId", nodeId);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(mContext, "Please wait a moment...", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.button_finish:
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unrigister();
        Logg.i(LOG_TAG, "===onDestroy=====");
    }

    private void unrigister() {
        if (tcpReceive != null) {
            TcpClient.getInstance().unrigister(tcpReceive);
        }
        if (mMqttMessageArrived != null) {
            MQTTManagement.getSingInstance().unrigister(mMqttMessageArrived);
        }
    }

    /**
     * 监听Back键按下事件,方法2:
     * 注意:
     * 返回值表示:是否能完全处理该事件
     * 在此处返回false,所以会继续传播该事件.
     * 在具体项目中此处的返回值视情况而定.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (TcpClient.getInstance().isConnected()) {
                Logg.i(LOG_TAG, "TcpClient -> send -> mobile_zwave:stopAddDevice:Zwave");
                TcpClient.getInstance().getTransceiver().send("mobile_zwave:stopAddDevice:Zwave");
            }
            finish();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
            Logg.i(LOG_TAG, "onTouchEvent=====");
            return true;
        }
        return true;
    }


}
