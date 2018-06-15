package com.askey.iotcontrol.ui;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.askey.iotcontrol.R;
import com.askey.iotcontrol.dao.ZwaveDevice;
import com.askey.iotcontrol.dao.ZwaveDeviceManager;
import com.askey.iotcontrol.dao.ZwaveDeviceSceneManager;
import com.askey.iotcontrol.service.ZwaveControlService;
import com.askey.iotcontrol.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chiapin on 2017/10/27.
 */

public class SensorFragment extends Fragment {

    private static String LOG_TAG = SensorFragment.class.getSimpleName();

    private View view;
    private int sensorNodeId;
    private String roomName,sensorNodeInfo,sensorCondition;
    private TextView txView;
    private ZwaveControlService zwaveService;
    private ZwaveDeviceManager zwDevManager;
    private ZwaveDeviceSceneManager zwDevSceneManager;
    private JSONObject jsonObject = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.sensor_fragment, container, false);
        // bind service
        Intent serviceIntent = new Intent(getActivity(), ZwaveControlService.class);
        getActivity().bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);

        roomName = (String) getArguments().get("SceneName");

        zwDevSceneManager = ZwaveDeviceSceneManager.getInstance(getActivity());
        zwDevManager = ZwaveDeviceManager.getInstance(getActivity());

        txView = (TextView) view.findViewById(R.id.txParameter1);

        //ZwaveDeviceScene tmpScene = zwDevSceneManager.getScene(roomName);
        //sensorNodeId = tmpScene.getSensorNodeId();
        //sensorCondition = tmpScene.getCondition();

        txView.setText("Condition = "+roomName);

        Log.i(LOG_TAG,"SensorNodeId = "+sensorNodeId+" | sensorCondition = "+sensorCondition);

        ZwaveDevice zwSensor = zwDevManager.queryZwaveDevices(sensorNodeId);
        sensorNodeInfo = zwSensor.getNodeInfo();

        try {
            jsonObject = new JSONObject(sensorNodeInfo);

            if (jsonObject.getString("Product id").equals("001F")) {

            } else if (jsonObject.getString("Product id").equals("000C")) {

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();
        zwaveService.unregister(mCallback);

        try {
            getActivity().unbindService(conn);
        } catch (Exception e) {
            Log.d(LOG_TAG, e.toString());
        }
    }

    // bind service with zwave control service
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            zwaveService = ((ZwaveControlService.MyBinder) iBinder).getService();
            //register mCallback
            if (zwaveService != null) {
                zwaveService.register(mCallback);

                /*
                if (sensorNodeInfo.contains("COMMAND_CLASS_BATTERY")) {
                    Log.i(LOG_TAG, "BATTERY");
                    zwaveService.getDeviceBattery(Const.zwaveType,sensorNodeId);
                }

                if (sensorNodeInfo.contains("COMMAND_CLASS_NOTIFICATION")) {
                    try {
                        //JSONObject jsonObject = new JSONObject(nodeInfo);
                        if (jsonObject.getString("Product id").equals("001F")) {
                            //Water
                            zwaveService.getSensorNotification(sensorNodeId, 0x00, 0x05, 0x00);
                        } else if (jsonObject.getString("Product id").equals("000C")) {
                            //Motion
                            zwaveService.getSensorNotification(sensorNodeId, 0x00, 0x07, 0x00);
                            //Door/Window
                            zwaveService.getSensorNotification(sensorNodeId, 0x00, 0x06, 0x00);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (sensorNodeInfo.contains("COMMAND_CLASS_SENSOR_MULTILEVEL")){
                    try {
                        zwaveService.getSensorMultiLevel(Const.zwaveType,sensorNodeId);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

                zwaveService.getMeterSupported(sensorNodeId);
                zwaveService.GetSensorBinarySupportedSensor(sensorNodeId);
                */

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            zwaveService = null;
        }
    };

    private ZwaveControlService.zwaveCallBack mCallback = new ZwaveControlService.zwaveCallBack() {

        @Override
        public void zwaveControlResultCallBack(String className, String result) {

            if (Utils.isGoodJson(result)) {
                try {
                    final JSONObject jsonObject = new JSONObject(result);
                    final String messageType = jsonObject.optString("MessageType");
                    final int nodeId = jsonObject.optInt("Node id");
                    txView.setText("sensorNodeId = "+sensorNodeId+" | nodeId = "+nodeId+" | messageType = " + messageType);

                    if (sensorNodeId == nodeId) {

                        if (messageType.equals("Notification Get Information")) {
                            String notificationType = jsonObject.optString("Notification-type");
                            String notificationEvent = jsonObject.optString("Notification-event");

                            if (sensorCondition.equals("DOOR")) {
                                if (notificationType.equals("Access control")) {
                                    txView.setText("Access control = " + notificationEvent);
                                    if (notificationEvent.contains("open")) {
                                        //turn on all device
                                        setRoomDevice(true);
                                    } else {
                                        //turn off all device
                                        setRoomDevice(false);
                                    }
                                }
                            } else if (sensorCondition.equals("WATER")) {
                                 if (notificationType.equals("Water alarm")) {
                                     updateUi("Water alarm ");
                                     if (notificationEvent.contains("detected")) {
                                         //turn on all device
                                         setRoomDevice(true);
                                     } else {
                                         //turn off all device
                                         setRoomDevice(false);
                                     }
                                 }
                            }
                        } else if (messageType.equals("Sensor Info Report")) {

                            if (sensorCondition.equals("LUMINANCE")) {
                                String txType = jsonObject.optString("type");
                                String txValue = jsonObject.optString("value");

                                txView.setText(txType + " : " + txValue + " %");
                                if (Integer.valueOf(txValue) < 60) {
                                    //turn on all device
                                    setRoomDevice(true);
                                } else {
                                    //turn off all device
                                    setRoomDevice(false);
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    };


    private void setRoomDevice(boolean status){
        /*

        List<ZwaveDevice> roomNode = zwDevManager.getSceneDevicesList(roomName);
        for (int idx = 0; idx < roomNode.size(); idx++) {
            if (!roomNode.get(idx).getDevType().equals("SENSOR")) {
                if (status) {
                    Log.i(LOG_TAG, "Set device#" + roomNode.get(idx).getNodeId() + " to on");
                    zwaveService.setBasic(roomNode.get(idx).getNodeId(), 255);

                } else {
                    Log.i(LOG_TAG, "Set device#" + roomNode.get(idx).getNodeId() + " to off");
                    zwaveService.setBasic(roomNode.get(idx).getNodeId(), 0);
                }
            }
        }
        */
    }

    private void updateUi(final String tmptext){

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(LOG_TAG,"set txView = "+tmptext);
                txView.setText(tmptext);
            }
        });

    }
}
