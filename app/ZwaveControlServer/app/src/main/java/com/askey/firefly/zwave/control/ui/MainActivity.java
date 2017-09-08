package com.askey.firefly.zwave.control.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.askey.firefly.zwave.control.R;
import com.askey.firefly.zwave.control.jni.ZwaveControlHelper;
import com.askey.firefly.zwave.control.mqtt.MQTTBroker;
import com.askey.firefly.zwave.control.mqtt.UDPConnectin;
import com.askey.firefly.zwave.control.thirdparty.usbserial.UsbSerial;
import com.askey.zwave.control.IZwaveContrlCallBack;
import com.askey.zwave.control.IZwaveControlInterface;

import org.eclipse.moquette.server.Server;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button btnAdd,btnRemove,btnGet,btnOpen,btnBarr,btnSensor,btnUpdate;
    private TextView txAddResult,txRemoveResult,txDeviceInfo,txOpenResult;
    IZwaveControlInterface zwaveService;
    private EditText etNodeId;
    private Button btnGetPower,btnOn, btnOff;
    private String nodeId;
    private UsbSerial mUsbSerial = new UsbSerial(this);

    private UDPConnectin uDPConnecting = new UDPConnectin(this);
    private Server mqttServer = new Server();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAdd = (Button) findViewById(R.id.btn_add);
        btnRemove = (Button) findViewById(R.id.btn_remove);
        btnGet = (Button) findViewById(R.id.btn_get);
        btnOpen = (Button) findViewById(R.id.btn_open);
        btnBarr = (Button) findViewById(R.id.btn_getBarr);
        btnSensor = (Button) findViewById(R.id.btn_getSenser);
        btnUpdate = (Button) findViewById(R.id.btn_update);
        etNodeId = (EditText) findViewById(R.id.et_node_id);
        btnGetPower = (Button) findViewById(R.id.btn_get_power);
        btnOn = (Button) findViewById(R.id.btn_on);
        btnOff = (Button) findViewById(R.id.btn_off);

        btnAdd.setOnClickListener(this);
        btnRemove.setOnClickListener(this);
        btnGet.setOnClickListener(this);
        btnOpen.setOnClickListener(this);
        btnBarr.setOnClickListener(this);
        btnSensor.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);
        btnGetPower.setOnClickListener(this);
        btnOn.setOnClickListener(this);
        btnOff.setOnClickListener(this);

        txAddResult = (TextView) findViewById(R.id.tx_add_result);
        txRemoveResult = (TextView) findViewById(R.id.tx_remove_result);
        txDeviceInfo = (TextView) findViewById(R.id.tx_get_result);
        txOpenResult = (TextView) findViewById(R.id.tx_open_result);
        //绑定服务
        Intent intent = new Intent();
        intent.setPackage("com.askey.firefly.zwave.control");
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        try {
            mqttServer.startServer();
            boolean bResult = uDPConnecting.startReceiver();
            Log.i("MQTTClient","UDP server = [" + bResult +"]");

        } catch (Exception e){e.printStackTrace();}
        Log.i("MQTTClient","MQTT Local Server = [" + mqttServer.getServerStatus() +"]");


        Intent MqttIntent = new Intent(MainActivity.this,MQTTBroker.class);
        startService(MqttIntent);
    }

    private IZwaveContrlCallBack.Stub mCallback = new IZwaveContrlCallBack.Stub() {

        @Override
        public void addDeviceCallBack(String result) throws RemoteException {
            Log.i("MainActivity", "======addDeviceCallBack=====" + result);
        }

        @Override
        public void openControlCallBack(String result,int length) throws RemoteException {
            Log.i("MainActivity", "======openControlCallBack=====" + result);
        }

        @Override
        public void removeDeviceCallBack(String result) throws RemoteException {
            Log.i("MainActivity", "======removeDeviceCallBack=====" + result);
        }

        @Override
        public void getDevicesCallBack(String result) throws RemoteException {
            Log.i("MainActivity", "======getDevicesCallBack=====" + result);
        }

        @Override
        public void getDevicesInfoCallBack(String result) throws RemoteException {
            Log.i("MainActivity", "======getDevicesInfoCallBack=====" + result);
        }

        @Override
        public void removeFailedDeviceCallBack(String result) throws RemoteException {

        }

        @Override
        public void replaceFailedDeviceCallBack(String result) throws RemoteException {

        }

        @Override
        public void stopAddDeviceCallBack(String result) throws RemoteException {

        }

        @Override
        public void stopRemoveDeviceCallBack(String result) throws RemoteException {

        }

        @Override
        public void getDeviceBatteryCallBack(String result) throws RemoteException {

        }

        @Override
        public void getSensorMultiLevelCallBack(String result) throws RemoteException {

        }

        @Override
        public void updateNodeCallBack(String result) throws RemoteException {

        }

        @Override
        public void reNameDeviceCallBack(String result) throws RemoteException {

        }

        @Override
        public void setDefaultCallBack(String result) throws RemoteException {

        }

        @Override
        public void getConfiguration(String result) throws RemoteException {

        }

        @Override
        public void setConfiguration(String result) throws RemoteException {

        }

        @Override
        public void getSupportedSwitchType(String result) throws RemoteException {

        }

        @Override
        public void startStopSwitchLevelChange(String result) throws RemoteException {

        }

        @Override
        public void getPowerLevel(String result) throws RemoteException {

        }

        @Override
        public void setSwitchAllOn(String result) throws RemoteException {

        }

        @Override
        public void setSwitchAllOff(String result) throws RemoteException {

        }

        @Override
        public void getBasic(String result) throws RemoteException {

        }

        @Override
        public void setBasic(String result) throws RemoteException {

        }

        @Override
        public void getSwitchMultiLevel(String result) throws RemoteException {

        }

        @Override
        public void setSwitchMultiLevel(String result) throws RemoteException {

        }

    };

    private void initfucntion(){
        try {
            zwaveService.openController(mCallback);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            zwaveService = IZwaveControlInterface.Stub.asInterface(iBinder);
            //注册mCallback
            if (zwaveService != null) {
                try {
                    zwaveService.registerListener(mCallback);

                    //initfucntion();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();

        uDPConnecting.stopConn();
        if (mqttServer.getServerStatus()) {
            Log.i("MQTTClient", "mqttServer.stopServer()");
            mqttServer.stopServer();
        }

        Intent intent = new Intent(MainActivity.this,MQTTBroker.class);
        stopService(intent);

        unbindService(serviceConnection);
    }


    @Override
    public void onClick(View view) {
        nodeId = etNodeId.getText().toString();
        switch (view.getId()) {
            case R.id.btn_open:
                if (zwaveService != null) {
                    try {
                        //remove service
                        zwaveService.openController(mCallback);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                }
                break;
            case R.id.btn_add:
                if (zwaveService != null) {
                    try {
                        //remove service
                        zwaveService.addDevice(mCallback);
                        txAddResult.setText("add device result");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                }
                break;
            case R.id.btn_remove:
                if (zwaveService != null) {
                    try {
                        //remove service
                        zwaveService.removeDevice(mCallback);
                        txRemoveResult.setText("remove device result");

                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                }
                break;
            case R.id.btn_get:
                if (zwaveService != null) {
                    try {
                        //remove service
                        zwaveService.getDevices(mCallback);
                        txDeviceInfo.setText("device information result");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                }
                break;
            case R.id.btn_getBarr:
                if (zwaveService != null) {
                    try {
                        //remove service
                        zwaveService.getDeviceBattery(mCallback,5);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                }
                break;
            case R.id.btn_getSenser:
                if (zwaveService != null) {
                    try {
                        //remove service
                        zwaveService.getSensorMultiLevel(mCallback,5);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                }
                break;
            case R.id.btn_update:
                if (zwaveService != null) {
                    try {
                        //remove service
                        zwaveService.updateNode(mCallback,5);
                        ZwaveControlHelper.ZwController_SetSwitchMultiLevel(Integer.parseInt(nodeId),20,5);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                }
                break;
            case R.id.btn_get_power:
                if (!"".equals(nodeId)) {
                    try {
                        zwaveService.getPowerLevel(mCallback, Integer.parseInt(nodeId));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.btn_on:
                if (!"".equals(nodeId)) {
                    try {
                        zwaveService.setSwitchAllOn(mCallback, Integer.parseInt(nodeId));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.btn_off:
                if (!"".equals(nodeId)) {
                    try {
                        zwaveService.setSwitchAllOff(mCallback, Integer.parseInt(nodeId));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

}
