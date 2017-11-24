package com.askey.mobile.zwave.control.deviceContr.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.deviceContr.model.DeviceList;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.deviceContr.net.SocketTransceiver;
import com.askey.mobile.zwave.control.deviceContr.net.TCPReceive;
import com.askey.mobile.zwave.control.deviceContr.net.TcpClient;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;
import com.askeycloud.sdk.core.api.response.BasicResponse;
import com.askeycloud.webservice.sdk.service.device.AskeyIoTDeviceService;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class RemoveActivity extends BaseActivity implements View.OnClickListener{
    private String LOG_TAG = "RemoveActivity";
    private ImageView ivBack;
    private Button btnCancel;
    private TextView tvTitle,tvStatus;
    private ProgressBar proBar;
    private String nodeId;
    private DeviceList.NodeInfoList nodeInfoList;
    private String index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove);

        Const.activityIndex = LOG_TAG;
        initView();
        Intent intent = getIntent();
        int title = intent.getIntExtra("title", 0);
        index = intent.getStringExtra("index");
        if (title == 0) {
            tvTitle.setText("Remove Faild Device");
            btnCancel.setVisibility(View.INVISIBLE);
        } else if (title == 1) {
            tvTitle.setText("Delete Device");
        }else if (title == 2) {
            tvTitle.setText("Replace Device");
        }
        proBar.setIndeterminate(true);

        if (Const.isRemote) {
            removePublish();
        } else {
            TcpClient.getInstance().rigister(tcpReceive);
            MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);

            nodeInfoList = intent.getParcelableExtra("nodeInfoList");
            if(nodeInfoList!=null){
                nodeId = nodeInfoList.getNodeId();
            }
            removeDevice(nodeId,title);
        }
    }

    private void removePublish() {
        Logg.i(LOG_TAG,"=========removePublish============");
        //删除有两种方式
//        BasicResponse response =
//                AskeyIoTDeviceService.getInstance(mContext).removeIoTDevice(mIoTDeviceInfoResponse);

        //or
        String deviceId = getIntent().getStringExtra("deviceId");
  /*      BasicResponse response =
                AskeyIoTDeviceService.getInstance(mContext)
                        .removeIoTDevice(mIoTDeviceInfoResponse.getDeviceid()); */
        BasicResponse response =
                AskeyIoTDeviceService.getInstance(mContext)
                        .removeIoTDevice(deviceId);
        if (response.getCode() == 200) {

        } else {

        }

    }


    MqttMessageArrived mMqttMessageArrived = new MqttMessageArrived() {
        @Override
        public void mqttMessageArrived(String topic, MqttMessage message) {
            String result = new String(message.getPayload());
            Logg.i(LOG_TAG,"=mqttMessageArrived=>=topic="+topic);
            Logg.i(LOG_TAG,"=mqttMessageArrived=>=message="+result);
            if(message.equals("removeDevice")){
                return;
            }



        }
    };

    private void initView() {
        ivBack = (ImageView) findViewById(R.id.img_back);
        btnCancel = (Button) findViewById(R.id.btn_cancel);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvStatus = (TextView) findViewById(R.id.tv_status);
        proBar = (ProgressBar) findViewById(R.id.proBar);
        ivBack.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    private void removeDevice(String nodeId,int type) {
        //删除成功则返回主页，否则提示删除失败，返回设备管理界面
        //点击取消，返回设备管理界面

        //调用删除或替换
        if(type == 0){
            if (TcpClient.getInstance().isConnected()) {
                Logg.i(LOG_TAG,"=removeDevice="+"mobile_zwave:removeFailedDevice:" + nodeId);
                TcpClient.getInstance().getTransceiver().send("mobile_zwave:removeFailedDevice:" + nodeId);
            }
        }else if(type==1){
            if (TcpClient.getInstance().isConnected()) {
                Logg.i(LOG_TAG,"=removeDevice="+"mobile_zwave:removeDevice");
                TcpClient.getInstance().getTransceiver().send("mobile_zwave:removeDevice");
            }
        }else if(type==2){
            if (TcpClient.getInstance().isConnected()) {
                Logg.i(LOG_TAG,"=removeDevice="+"mobile_zwave:replaceFailedDevice:" + nodeId);
                TcpClient.getInstance().getTransceiver().send("mobile_zwave:replaceFailedDevice:" + nodeId);
            }
        }



        //区分调用removeFail还是remove接口 removeFail还没写

    }


    TCPReceive tcpReceive = new TCPReceive() {
        @Override
        public void onConnect(SocketTransceiver transceiver) {

        }

        @Override
        public void onConnectFailed() {

        }

        @Override
        public void receiveMessage(SocketTransceiver transceiver, String tcpMassage) {
            //处理结果
            removeDeviceResult(tcpMassage);
        }

        @Override
        public void onDisconnect(SocketTransceiver transceiver) {

        }

    };

    //mqtt调用返回结果
    private void removeDeviceResult(String result) {
        if(result.contains("removeDevice:other")){
            return;
        }
        try {
            final JSONObject jsonObject = new JSONObject(result);
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String messageType = jsonObject.optString("MessageType");
                    Logg.i(LOG_TAG,"=====messageType=="+messageType);
                    if(messageType.equals("Node Remove Status")){
                        String status = jsonObject.optString("Status");
//            String nodeId = jsonObject.optString("NodeID");
                        tvStatus.setText(status);
                        if ("Success".equals(status)) {
                            //删除成功则返回主页，否则提示删除失败，返回设备管理界面
                            tvStatus.setText("Success");
                            proBar.setIndeterminate(false);
                            if(index!=null && index.equals("HomePageActivity")){
                                unrigister();
                            }else{
                                unrigister();
                                Intent intent = new Intent(mContext,SensorManageActivity.class);
                                intent.putExtra("nodeInfoList", nodeInfoList);
                                startActivity(intent);
                            }
                        } else if ("Failed".equals(status)) {
                            Toast.makeText(mContext,"Delete Faild",Toast.LENGTH_SHORT).show();
                            proBar.setIndeterminate(false);
                            cancelRemove();
                        }else if("Learn Ready".equals(status)){
                            tvStatus.setText("Please press the trigger button of the device");
                        }else{
                            Logg.i(LOG_TAG,"=====result=="+status);
                            tvStatus.setText(status);
                        }
                    }

                }
            });


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //预留的接口
    private void replaceDevice1() {

        String commandStr = "mobile_zwave:replaceDevice";
        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+nodeId,commandStr);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.btn_cancel:

                //预留的mqtt的接口
                cancelRemove();
                break;
        }
    }

    private void cancelRemove() {
//mqtt发送命令
        unrigister();

        Intent intent = new Intent(mContext,SensorManageActivity.class);
        intent.putExtra("nodeInfoList", nodeInfoList);
        startActivity(intent);

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
