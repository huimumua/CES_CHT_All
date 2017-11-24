package com.askey.mobile.zwave.control.deviceContr.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.askey.mobile.zwave.control.home.activity.HomeActivity;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SensorManageActivity extends BaseActivity{
    private String TAG = "SensorManageActivity";
    private TextView battery_text,temperature_text,humidity_text,luminance_text,txValue4,txValue5,txValue6;
    private String nodeId,displayName,roomName;
    private DeviceList.NodeInfoList nodeInfoList;
    private List<String> sensorInfo = new ArrayList<>();
    private int luminanceValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_manage);

        init();//初始化控件
        TcpClient.getInstance().rigister(tcpReceive);
        MQTTManagement.getSingInstance().rigister(mqttMessageArrived);
        Intent intent = getIntent();
        nodeInfoList = intent.getParcelableExtra("nodeInfoList");
        nodeId = intent.getStringExtra("nodeId");
        displayName =  intent.getStringExtra("displayName");
        roomName =  intent.getStringExtra("roomName");


        if(nodeInfoList!=null){
            nodeId = nodeInfoList.getNodeId();
            String title = nodeInfoList.getNodeId() + "Manager";
            setTopLayout(true,title,true);
            //预留接口 显示设备信息
//            showProgressDialog(mContext,"正在获取Sensor数据...");
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+nodeId,"mobile_zwave:getSensorMultiLevel:"+nodeId);//deviceId呢

        }else{
            String title = displayName + "Manager";
            setTopLayout(true,title,true);
            //预留接口 显示设备信息
//            showProgressDialog(mContext,"正在获取Sensor数据...");
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic+nodeId,"mobile_zwave:getSensorMultiLevel:"+nodeId);//deviceId呢

        }

    }

    private void init() {
        battery_text = (TextView) this.findViewById(R.id.battery_text);
        temperature_text = (TextView) this.findViewById(R.id.temperature_text);
        humidity_text = (TextView) this.findViewById(R.id.humidity_text);
        luminance_text = (TextView) this.findViewById(R.id.luminance_text);
        txValue4 = (TextView) findViewById(R.id.txValue4);
        txValue5 = (TextView) findViewById(R.id.txValue5);
        txValue6 = (TextView) findViewById(R.id.txValue6);
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
            Logg.i(TAG,"=TCPReceive=>=receiveMessage="+tcpMassage);
            //在这里处理结果
            reNameResult(tcpMassage);
        }

        @Override
        public void onDisconnect(SocketTransceiver transceiver) {

        }

    };

    private void reNameResult(String result) {
        Logg.i(TAG,"=====reNameResult======="+result);
        if(result.contains("reNameDevice")){
            if(result.contains("reNameDevice:0") ){
                unrigister();
                Intent intent = new Intent();
                intent.setClass(mContext,HomeActivity.class);
                startActivity(intent);
            }else{
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext,"ReName Fail ! ",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    MqttMessageArrived mqttMessageArrived = new MqttMessageArrived() {
        @Override
        public void mqttMessageArrived(String topic, MqttMessage message) {
            final String result = new String(message.getPayload());
            Logg.i(TAG,"=mqttMessageArrived=>=topic="+topic);
            Logg.i(TAG,"=mqttMessageArrived=>=message="+result);

            //解析结果
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String str[]  = result.split("#");
                    if(str.length>1){
                        mqttResult(str[1]);
                    }
                }
            });

        }
    };


    private void mqttResult(String result) {
        Logg.i(TAG,"==mqttResult==="+result);
        if(result.contains("getSensorMultiLevel") || result.contains("getDeviceBattery") ){
           return;
        }

        try {
            JSONObject jsonObject = new JSONObject(result);
            String messageType = jsonObject.optString("MessageType");
            if ("Node Battery Value".equals(messageType)) {
                String nodeId = jsonObject.optString("Node id");
                String battery = jsonObject.optString("Battery Value");
                Logg.i(TAG,"=======battery==="+battery);
                battery_text.setText("  "+battery + "%" );
                sensorInfo.add("Battery : "+battery + " %" );
                hideProgressDialog();
            } else if ("Sensor Info Report".equals(messageType)) {//messageType  重命名
                hideProgressDialog();
                String type = jsonObject.optString("type");
                String precision = jsonObject.optString("precision");
                String nodeId = jsonObject.optString("Node id");
                String unit = jsonObject.optString("unit");
                String value = jsonObject.optString("value");
                Logg.i(TAG,"=======type==="+value +"=="+value+unit);
                if(type.endsWith("Temperature sensor")){
                    temperature_text.setText("  "+value+"  "+unit);
                    sensorInfo.add("Temperature : "+value+"  "+unit );
                }else if(type.endsWith("Relative humidity sensor")){
                    humidity_text.setText("  "+value+"  "+unit);
                    sensorInfo.add("Humidity : "+value+"  "+unit );
                }else if(type.endsWith("Luminance sensor")){
                    luminanceValue = Integer.valueOf(value);
                    sensorInfo.add("Luminance : "+value+"  "+unit );
                    if(luminanceValue<50){
                        luminance_text.setTextColor(Color.RED);
                        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic,"mobile_zwave:setScene:"+roomName+":"+nodeId+":"+"LUMINANCE");
                    }else{
                        luminance_text.setTextColor(Color.GREEN);
                    }
                    luminance_text.setText("  "+value+"  "+unit);
                }
            } else if ("Notification Get Information".equals(messageType)) {//messageType  重命名
                hideProgressDialog();
                String notificationStatus = jsonObject.optString("Notification-status");
                String notificationType = jsonObject.optString("Notification-type");
                String nodeId = jsonObject.optString("Node id");
                String notificationEvent = jsonObject.optString("Notification-event");



                if (notificationType.equals("Water alarm")) {
                    if (notificationEvent.contains("detected")) {
                        //turn on all device
                        txValue4.setTextColor(Color.RED);
                        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic,"mobile_zwave:setScene:"+roomName+":"+nodeId+":"+"WATER");
                    }else{
                        txValue4.setTextColor(Color.GREEN);
                    }
                    txValue4.setText(notificationEvent);
                } else if (notificationType.equals("Home security")) {
                    if (!notificationEvent.equals("Tampering. Product covering removed")) {
                        txValue5.setText(notificationEvent);
                    }
                } else if (notificationType.equals("Access control")) {

                    if (notificationEvent.contains("open")) {
                        //turn on all device
                        txValue6.setTextColor(Color.RED);
                        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic,"mobile_zwave:setScene:"+roomName+":"+nodeId+":"+"DOOR");
                    }else{
                        txValue6.setTextColor(Color.RED);
                    }
                    txValue6.setText(notificationEvent);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void openMemu(Context context) {
        super.openMemu(context);
    }

    @Override
    protected void removeDevice() {
        super.removeDevice();
        showRemoveDialog();
    }

    @Override
    protected void replaceDevice() {
        super.replaceDevice();
        unrigister();
        Intent intent = new Intent(mContext, RemoveActivity.class);
        intent.putExtra("title", 2);
        intent.putExtra("nodeInfoList", nodeInfoList);
        startActivity(intent);
    }

    @Override
    protected void reNameDevice() {
        super.reNameDevice();
        showRenameDialog( nodeInfoList.getHomeId(),nodeId);
    }

    @Override
    protected void getBattery() {
        super.getBattery();
//        showProgressDialog(mContext,"获取电池电量需要点击所操作设备的触发按键");
        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic + nodeId,"mobile_zwave:getDeviceBattery:"+nodeId);//deviceId呢
    }

    private void showRemoveDialog() {
        final AlertDialog.Builder addDialog = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.dialog_normal_layout, null);
        addDialog.setView(view);
        final AlertDialog alertDialog = addDialog.create();

        TextView title = (TextView) view.findViewById(R.id.title);
        TextView message = (TextView) view.findViewById(R.id.message);
        title.setText("Prompt");
        message.setText("Delete Device ?");
        Button positiveButton = (Button) view.findViewById(R.id.positiveButton);
        Button negativeButton = (Button) view.findViewById(R.id.negativeButton);

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                alertDialog.dismiss();
                //点击确认，进入删除页面
                unrigister();
                Intent intent = new Intent(mContext, RemoveActivity.class);
                intent.putExtra("title", 1);
                intent.putExtra("nodeInfoList", nodeInfoList);
                startActivity(intent);

            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击取消，返回设备管理界面

                alertDialog.dismiss();

            }
        });


        alertDialog.show();
    }





    private void reName(String homeId,String nodeId,String newName) {
        if (TcpClient.getInstance().isConnected()) {
            TcpClient.getInstance().getTransceiver().send("mobile_zwave:reNameDevice:" + homeId+":"+nodeId+":"+newName);
        }
    }



    private void showRenameDialog(final String homeId,final String nodeId) {
        final AlertDialog.Builder addDialog = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.dialog_add_layout, null);
        addDialog.setView(view);
        final AlertDialog alertDialog = addDialog.create();

        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText("reName");
        final EditText message = (EditText) view.findViewById(R.id.message);
        Button positiveButton = (Button) view.findViewById(R.id.positiveButton);
        Button negativeButton = (Button) view.findViewById(R.id.negativeButton);

        message.setText(nodeId);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //点击确认返回主页
                alertDialog.dismiss();

                //重命名预留接口
                reName(homeId,nodeId,message.getText().toString());
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

    @Override
    protected void onStop() {
        super.onStop();
        Logg.i(TAG,"===onStop=====");
        unrigister();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logg.i(TAG,"===onDestroy=====");
    }

    private void unrigister() {
        if(tcpReceive!=null){
            TcpClient.getInstance().unrigister(tcpReceive);
        }
        if(mqttMessageArrived!=null){
            MQTTManagement.getSingInstance().unrigister(mqttMessageArrived);
        }
    }

}
