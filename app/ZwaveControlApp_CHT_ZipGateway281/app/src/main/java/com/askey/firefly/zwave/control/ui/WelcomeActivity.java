package com.askey.firefly.zwave.control.ui;

import android.app.Activity;
import android.bluetooth.BluetoothClass;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.askey.firefly.zwave.control.R;
import com.askey.firefly.zwave.control.dao.ZwaveDevice;
import com.askey.firefly.zwave.control.dao.ZwaveDeviceManager;
import com.askey.firefly.zwave.control.service.MQTTBroker;
import com.askey.firefly.zwave.control.service.ZwaveControlService;
import com.askey.firefly.zwave.control.utils.Const;
import com.askey.firefly.zwave.control.utils.DeviceInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by chiapin on 2017/9/7.
 */

public class WelcomeActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static String LOG_TAG = WelcomeActivity.class.getSimpleName();

    private AlertDialog alertDialog;
    private Timer timer = new Timer(true);

    private ZwaveControlService zwaveService;
    private ZwaveDeviceManager zwDevManager;
    private BroadcastReceiver usbReceiver = null;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";


    private ArrayList<Integer> nodeIdArr = new ArrayList<>();
    private ArrayList<String> provisionListArr = new ArrayList<>();
    private Button btnAdd,btnRemove,btnButton,btnAddPorList,btnRmProList,btnEditProvision,btnPassive,btnLearn;
    private Spinner spNodeIdList,spApiList,spProvisionList;
    private TextView txDsk,txAllMsg;
    private EditText editDsk,editSetApiValue;
    private CheckBox cb1,cb2,cb3;
    private String inputDsk;
    private int selectNode;
    private String selectProvisionList;
    private int getDeviceInfoFlag = 0;
    private boolean getProvisionListFlag = false;
    private boolean getProvisionNodeFlag = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        mContext = this;

        showProgressDialog(mContext, "Initializing，Open Zwave Controller...");

        Intent MqttIntent = new Intent(WelcomeActivity.this, MQTTBroker.class);
        startService(MqttIntent);

        zwDevManager = ZwaveDeviceManager.getInstance(this);

        // bind service
        Intent serviceIntent = new Intent(this, ZwaveControlService.class);
        this.bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);

        new Thread(reqCallBack).start();

        new Thread(checkInitStatus).start();
        initBtn();

    }

    //read content of ApiName.txt
    private String loadTextFile(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bytes = new byte[4096];
        int len = 0;
        while ((len = inputStream.read(bytes)) > 0) {
            byteArrayOutputStream.write(bytes, 0, len);
        }
        return new String(byteArrayOutputStream.toByteArray(), "UTF8");
    }


    private void initBtn() {
        Log.i(LOG_TAG, "initBtn");

        editDsk = (EditText) findViewById(R.id.editText);
        editSetApiValue = (EditText) findViewById(R.id.editSetApiValue);

        btnAdd  = (Button) findViewById(R.id.btnAdd);
        btnRemove  = (Button) findViewById(R.id.btnRemove);
        btnLearn = (Button) findViewById(R.id.btnLearn);
        btnButton = (Button) findViewById(R.id.btnButton);
        btnAddPorList = (Button) findViewById(R.id.btnaddProList);
        btnRmProList = (Button) findViewById(R.id.btnrmProList);
        btnEditProvision = (Button) findViewById(R.id.btnEditProvision);
        btnPassive = (Button) findViewById(R.id.btnPassive);

        cb1 = (CheckBox) findViewById(R.id.cb1);
        cb2 = (CheckBox) findViewById(R.id.cb2);
        cb3 = (CheckBox) findViewById(R.id.cb3);

        txDsk = (TextView) findViewById(R.id.txDsk);
        txAllMsg = (TextView) findViewById(R.id.txAllMsg);

        spNodeIdList = (Spinner) findViewById(R.id.nodeIdList);
        spApiList = (Spinner) findViewById(R.id.spApiList);
        spProvisionList = (Spinner) findViewById(R.id.provisionList);

        btnAdd.setOnClickListener(this);
        btnRemove.setOnClickListener(this);
        btnLearn.setOnClickListener(this);
        btnButton.setOnClickListener(this);
        btnAddPorList.setOnClickListener(this);
        btnRmProList.setOnClickListener(this);
        btnEditProvision.setOnClickListener(this);
        btnPassive.setOnClickListener(this);

        cb1.setOnCheckedChangeListener(this);
        cb2.setOnCheckedChangeListener(this);
        cb3.setOnCheckedChangeListener(this);

        AssetManager assetManager = getAssets();
        InputStream inputStream = null;

        //載入assets/ApiName.txt 內容到spinner
        try {
            inputStream = assetManager.open("ApiName.txt");
            String text = loadTextFile(inputStream);
            String[] apiName = text.split(",");

            ArrayAdapter<String> getApiList = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item,apiName);

            spApiList.setAdapter(getApiList);
        } catch (IOException e) {
            e.printStackTrace();
        }

        InputMethodManager imm = (InputMethodManager)getSystemService(WelcomeActivity.this.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editDsk, 0);

    }

    // only execute one time
    public Runnable reqCallBack = new Runnable() {
        @Override
        public void run() {
            Intent serviceIntent = new Intent(WelcomeActivity.this, ZwaveControlService.class);
            WelcomeActivity.this.bindService(serviceIntent, req, Context.BIND_AUTO_CREATE);
        }
    };

    // only execute one time
    public Runnable checkInitStatus = new Runnable() {
        @Override
        public void run() {
            while (!DeviceInfo.isZwaveInitFinish || !DeviceInfo.isMQTTInitFinish) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            initSensorfunc();
            initZwave();        //maybe can cancel by gino
        }
    };

    // not use !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private void initZwave() {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                timerCancel();
                //Intent intent = new Intent();
                //intent.setClass(mContext, HomeActivity.class);
                hideProgressDialog();                               //close progress dialog
                //mContext.startActivity(intent);
                //finish();
            }
        });
    }


    private void showZwaveControlTimeOutDialog(String titleStr) {
        if(alertDialog == null) {
            AlertDialog.Builder addDialog = new AlertDialog.Builder(mContext);
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            View view = layoutInflater.inflate(R.layout.dialog_retry_layout, null);
            addDialog.setView(view);
            alertDialog = addDialog.create();
            alertDialog.show();

            TextView title = (TextView) view.findViewById(R.id.title);
            TextView message = (TextView) view.findViewById(R.id.message);
            title.setText("Prompt");
            message.setText(titleStr);
            Button positiveButton = (Button) view.findViewById(R.id.positiveButton);
            Button negativeButton = (Button) view.findViewById(R.id.negativeButton);

            positiveButton.setText("retry");
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //retry to opencontroller
                    openController();
                    showProgressDialog(mContext, "OpenController....");
                    alertDialogCancel();
                }
            });

            negativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //tap cancel button and exit to main screen
                    finish();
                    timerCancel();

                    alertDialogCancel();
                    DeviceInfo.isMQTTInitFinish = false;
                    DeviceInfo.isOpenControllerFinish = false;
                    DeviceInfo.isZwaveInitFinish = false;
                    System.exit(0);
                }
            });
        }
    }


    // permanent thread
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2001:
                    timerCancel();
                    hideProgressDialog();
                    showZwaveControlTimeOutDialog("Zwave OpenController Timeout");
                    break;
            }
        }
    };

    @Override
    public void onClick(View view) {
        //Log.d(LOG_TAG,"node ID : " + selectNode);
        switch (view.getId()) {
            case R.id.btnAdd:

                if (editDsk.length() != 0 && editDsk.length() == 47) {    // editDsk will 5-digit or full code
                    //Log.i(LOG_TAG, "call zwaveService.addDevice()");
                    DeviceInfo.reqKey = Integer.valueOf(editDsk.getText().toString());
                    zwaveService.addDevice(DeviceInfo.devType);
                } else if (editDsk.length() != 0 && editDsk.length() == 5) {
                    //Log.i(LOG_TAG, "call zwaveService.addDevice()");
                    DeviceInfo.reqKey = Integer.valueOf(editDsk.getText().toString());
                    zwaveService.addDevice(DeviceInfo.devType);
                } else if (editDsk.length() == 0) {
                    zwaveService.addDevice(DeviceInfo.devType);
                } else
                    Toast.makeText(this, "格式錯誤 !", Toast.LENGTH_SHORT).show();
                /*
                inputDsk = "11394" + "\0";
                byte[] dskNumber = inputDsk.getBytes();
                zwaveService.addDevice(DeviceInfo.devType, dskNumber);
                */
                getDeviceInfoFlag = 1;
                break;

            case R.id.btnRemove:
                Log.i(LOG_TAG, "call zwaveService.removeDevice()");
                zwaveService.removeDevice(DeviceInfo.devType,0);
                break;

            case R.id.btnLearn:
                zwaveService.StartLearnMode();
                break;

            case R.id.btnButton:
                if (spApiList.getSelectedItem().toString().contains("ZwController_startNetworkHealthCheck")) {
                    zwaveService.startNetworkHealthCheck();
                } else if (spApiList.getSelectedItem().toString().contains("ZwController_getProvisionListEntry")) {
                    getProvisionList();
                } else if (spApiList.getSelectedItem().toString().contains("ZwController_getAllProvisionListEntry")) {
                    zwaveService.getAllProvisionListEntry();
                } else if (spApiList.getSelectedItem().toString().contains("ZwController_rmAllProvisionListEntry")) {
                    zwaveService.rmAllProvisionListEntry();
                } else if (spApiList.getSelectedItem().toString().contains("ZwController_getDeviceList")) {
                    zwaveService.getDeviceList("My Home");
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_battery_get")) {
                    zwaveService.getDeviceBattery(DeviceInfo.devType, Integer.valueOf(spNodeIdList.getSelectedItem().toString()));
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_sensor_multilevel_get")) {
                    try {
                        zwaveService.getSensorMultiLevel(DeviceInfo.devType, Integer.valueOf(spNodeIdList.getSelectedItem().toString()));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_Command_Class_get")) {
                    getDeviceInfoFlag = 2;
                    zwaveService.getDeviceInfo();
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_Device_get")) {
                    getDeviceInfoFlag = 3;
                    zwaveService.getDeviceInfo();
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_basic_get")) {
                    zwaveService.getBasic(DeviceInfo.devType, selectNode);
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_basic_set")) {
                    zwaveService.setBasic(DeviceInfo.devType, selectNode, Integer.valueOf(editSetApiValue.getText().toString()));
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_switch_multilevel_get")) {
                    zwaveService.getSwitchMultiLevel(DeviceInfo.devType, selectNode);
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_switch_multilevel_set")) {
                    zwaveService.setSwitchMultiLevel(DeviceInfo.devType, selectNode, Integer.valueOf(editSetApiValue.getText().toString()), 1);
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_get_support_switch_type")) {
                    zwaveService.getSupportedSwitchType(selectNode);
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_start_stop_switchlevel_change")) {
                    //zwaveService.startStopSwitchLevelChange(); many parameter
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_configuration_get")) {
                    zwaveService.getConfiguration(selectNode,0,0,0,0);
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_configuration_set")) {
                    //zwaveService.setConfiguration(); many parameter
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_powerLevel_get")) {
                    zwaveService.getPowerLevel(selectNode);
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_switch_all_on")) {
                    zwaveService.setSwitchAllOn(DeviceInfo.devType, selectNode);
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_switch_all_off")) {
                    zwaveService.setSwitchAllOff(DeviceInfo.devType, selectNode);
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_switch_all_set")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_switch_all_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_switch_all_on_broadcast")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_switch_all_off_broadcast")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_switch_binary_set")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_switch_binary_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_sensor_binary_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_sensor_binary_supported_sensor_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_meter_get")) {
                    zwaveService.getMeter(DeviceInfo.devType, selectNode, 10);
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_meter_supported_get")) {
                    zwaveService.getMeterSupported(selectNode);
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_meter_reset")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_wake_up_interval_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_wake_up_interval_set")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_door_lock_operation_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_door_lock_operation_set")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_door_lock_config_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_door_lock_config_set")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_user_code_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_user_code_set")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_user_code_number_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_protection_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_protection_set")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_supported_protection_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_protection_exclusive_control_node_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_protection_exclusive_control_node_set")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_protection_timeout_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_protection_timeout_set")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_indicator_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_indicator_set")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_door_lock_logging_supported_records_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_door_lock_logging_records_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_language_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_language_set")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_switch_color_get")) {
                    zwaveService.getLampColor(DeviceInfo.devType, selectNode);
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_switch_color_supported_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_switch_color_set")) {
                    String[] temp = editSetApiValue.getText().toString().split(",");
                    zwaveService.setLampColor(DeviceInfo.devType, selectNode, Integer.valueOf(temp[0]), Integer.valueOf(temp[1]), Integer.valueOf(temp[2]));
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_start_stop_color_levelchange")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_barrier_operator_set")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_barrier_operator_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_barrier_operator_signal_set")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_barrier_operator_signal_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_barrier_operator_signal_supported_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_basic_tariff_info_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_get_group_info")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_add_endpoints_to_group")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_remove_endpoints_from_group")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_get_max_supported_groups")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_get_specific_group")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_notification_set")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_notification_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_notification_supported_get")) {
                    zwaveService.getSupportedNotification(selectNode);
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_notification_supported_event_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_central_scene_supported_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("hl_central_scene_notification_report_cb")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_scene_actuator_conf_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_firmwareupdate_info_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_firmwareupdate_request")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_multi_cmd_encap")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("ZwController_getSpecifyDeviceInfo")) {
                    zwaveService.getSpecifyDeviceInfo(selectNode);
                } else if (spApiList.getSelectedItem().toString().contains("ZwController_SetDefault")) {
                    zwaveService.setDefault();
                } else if (spApiList.getSelectedItem().toString().contains("ZwController_checkNodeIsFailed")) {
                    zwaveService.checkNodeIsFailed(selectNode);
                } else if (spApiList.getSelectedItem().toString().contains("ZwController_RemoveFailedDevice")) {
                    zwaveService.removeFailedDevice(selectNode);
                    nodeIdArr.remove(Integer.valueOf(selectNode));

                    ArrayAdapter<Integer> devList = new ArrayAdapter<Integer>(WelcomeActivity.this,
                            android.R.layout.simple_spinner_dropdown_item,
                            convertIntegers(nodeIdArr));
                    spNodeIdList.setAdapter(devList);


                } else if (spApiList.getSelectedItem().toString().contains("ZwController_ReplaceFailedDevice")) {
                    zwaveService.replaceFailedDevice(selectNode);
                }
                break;
            case R.id.btnaddProList:
                if (editDsk.length() != 0 && editDsk.length() == 47) {    // editDsk will 5-digit or full code
                    DeviceInfo.InclusionState = true;
                    addProvisionList();
                } else
                    Toast.makeText(this, "格式錯誤 !", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnrmProList:
                if (selectProvisionList != null) {    // editDsk will 5-digit or full code
                    rmProvisionList();
                }
                break;
            case R.id.btnEditProvision:
                if (selectProvisionList != null) {
                    editDsk.setText(selectProvisionList);
                    rmProvisionList();
                } else {
                    txAllMsg.setText("Provision List is null");
                }
                break;
            case R.id.btnPassive:
                if (selectProvisionList != null) {
                    DeviceInfo.InclusionState = false;
                    editDsk.setText(selectProvisionList);
                    rmProvisionList();
                    addProvisionList();
                }
        }
    }

    //load exist provision list
    private void loadProvisionList(String result) {
        if(result.contains("No list entry found"))
        {
            Log.d(LOG_TAG,"No list entry found !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }

        else {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(result);
                String provisinoInfo = jsonObject.optString("Detial provision list");
                String[] dskNumber = provisinoInfo.split(",");
                for (int i = 0; i < dskNumber.length; i++) {
                    //txAllMsg.setText(result);
                    if (dskNumber[i].contains("DSK")) {
                        String[] tmpDsk = dskNumber[i].split("\"");
                        for (int j = 0; j < tmpDsk.length; j++) {
                            if (j % 4 == 0 && getProvisionListFlag == false) {
                                Log.d(LOG_TAG, tmpDsk[j + 3]);
                                provisionListArr.add(tmpDsk[j + 3]); //抓取DSK值
                                //txAllMsg.setText(tmpDsk[j + 3]);
                            }
                        }
                    }
                }

                getProvisionListFlag = true;    //Provision List 載入到 spinner 1次

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    //get Provision List
    private void getProvisionList() {
        getProvisionNodeFlag = false;
        inputDsk =  selectProvisionList + "\0";
        //Log.d(LOG_TAG,inputDsk);
        byte[] dskNumber = inputDsk.getBytes();
        zwaveService.getProvisionListEntry(DeviceInfo.devType,dskNumber);
    }

    //check provision node whether exist
    private void getProvisionNode(String result) {
        final String provisionInfo = result;
        JSONObject jsonObject = null;
        JSONObject jsonObject2 = null;
        txAllMsg.setText(result);
        try {
            jsonObject = new JSONObject(provisionInfo);
            String provisionState = jsonObject.optString("Network inclusion state");
            jsonObject2 = new JSONObject(provisionState);
            String provisionNode = jsonObject2.optString("Node Id");
            //String[] provisionStateSplit = provisionState.split(":");
            Log.d(LOG_TAG,provisionNode);
            if(provisionNode.equals("0")) {
                Log.d(LOG_TAG,"device is exclusion");
                txAllMsg.setText("device is exclusion");
            } else {
                Log.d(LOG_TAG,"device is not exclusion");
                txAllMsg.setText("device is not exclusion");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //remove Provision List
    private void rmProvisionList() {
        getProvisionNodeFlag = true;
        inputDsk =  selectProvisionList + "\0";
        Log.d(LOG_TAG,inputDsk);
        byte[] dskNumber = inputDsk.getBytes();
        zwaveService.getProvisionListEntry(DeviceInfo.devType,dskNumber);
        zwaveService.rmProvisionListEntry(DeviceInfo.devType,dskNumber);
        provisionListArr.remove(selectProvisionList);
        ArrayAdapter<String> provisionList = new ArrayAdapter<String>(WelcomeActivity.this,
                android.R.layout.simple_spinner_dropdown_item, converProvisionList(provisionListArr));
        spProvisionList.setAdapter(provisionList);
    }

    //insert Provision List
    private void addProvisionList() {

        inputDsk = editDsk.getText().toString() + "\0";
        Log.d(LOG_TAG,inputDsk);
        byte[] dskNumber = inputDsk.getBytes();
        if(provisionListArr.contains(editDsk.getText().toString())) {
            Toast.makeText(this,"already Provision List", Toast.LENGTH_SHORT).show();
        } else {
            provisionListArr.add(editDsk.getText().toString());
            zwaveService.addProvisionListEntry(DeviceInfo.devType,dskNumber,DeviceInfo.InclusionState);
            Toast.makeText(this, "add Provision List", Toast.LENGTH_SHORT).show();
        }
        ArrayAdapter<String> provisionList = new ArrayAdapter<String>(WelcomeActivity.this,
                android.R.layout.simple_spinner_dropdown_item, converProvisionList(provisionListArr));
        spProvisionList.setAdapter(provisionList);

        spProvisionList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectProvisionList = spProvisionList.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    //input editDsk limitation
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(cb1.isChecked()) {
            editDsk.setText("");
            editDsk.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        } else if (cb2.isChecked()) {
            editDsk.setText("");
            editDsk.setFilters(new InputFilter[]{new InputFilter.LengthFilter(47)});
        } else if (cb3.isChecked()) {
            editDsk.setText("");
            editDsk.setFilters(new InputFilter[]{new InputFilter.LengthFilter(0)});
        }

    }

    class mTimerTask extends TimerTask {
        public void run() {
            zwaveService.closeController();
            Log.d(LOG_TAG,"timer on schedule");
            Message message = new Message();
            message.what = 2001;
            mHandler.sendMessage(message);
            timerCancel();
        }
    }


    private void alertDialogCancel() {
        if(alertDialog!=null){
            alertDialog.dismiss();
            alertDialog=null;
        }
    }

    // cancel this timer schedule
    private void timerCancel() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    // bind service with zwave control service
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i(LOG_TAG,"onServiceConnected....");
            zwaveService = ((ZwaveControlService.MyBinder)iBinder).getService();
            //register mCallback
            if (zwaveService != null) {
                zwaveService.register(mCallback);       //add & remove and other info call back
                //zwaveService.register(mReqCallBacks);   //use other thread for grantKey pinCode csa req call back
                openController();

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            zwaveService = null;
        }
    };

    private ServiceConnection req = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i(LOG_TAG,"req_ServiceConnected....");
            zwaveService = ((ZwaveControlService.MyBinder)iBinder).getService();
            //register mCallback
            if (zwaveService != null) {
                zwaveService.register(mReqCallBacks);   //use other thread for grantKey pinCode csa req call back
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            zwaveService = null;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        zwaveService.unregister(mCallback);
        zwaveService.unregister(mReqCallBacks);

        if (usbReceiver != null)
            unregisterReceiver(usbReceiver);
        try {
            this.unbindService(conn);
            this.unbindService(req);
        } catch (Exception e) {
            Log.d(LOG_TAG, e.toString());
        }
    }

    public ZwaveControlService.zwaveCallBack mCallback;
    {
        mCallback = new ZwaveControlService.zwaveCallBack() {

            //監聽關鍵字回報 從ZwaveControlService.jave zwaveCallBack -> zwcontrol_api.c "MessageType"
            @Override
            public void zwaveControlResultCallBack(String className, String result) {

                Log.i(LOG_TAG, "class name = [" + className + "]| result = " + result);

                if (className.equals("addDevice") || className.equals("removeDevice")) {

                    addRemoveDevice(result);

                } else if (className.equals("All Node Info Report")) {

                    Log.d(LOG_TAG,result);

                } else if (className.equals("reNameDevice")) {

                    addRemoveDevice(result);

                } else if (result.contains("Remove Failed Node")) {

                    if(result.contains("Success")) {

                        Log.d("MessageType", "Remove Failed Node");
                        Log.d("Status", "Success");
                    }

                } else if (result.contains("Replace Failed Node")) {

                    if(result.contains("Success")) {

                        Log.d("MessageType", "Replace Failed Node");
                        Log.d("Status", "Success");

                    } else if (result.contains("Unknown or Failed")) {

                        Log.d("MessageType", "Replace Failed Node");
                        Log.d("Status", "Unknown or Failed");
                    }
                } else if (result.contains("Node Is Failed Check Report")) {
                    String[] tmpSplit = result.split(":");
                    if(result.contains("Alive")) {

                        Log.d("MessageType", "Node Is Failed Check Report");
                        Log.d("Node Id", tmpSplit[2]);
                        Log.d("Status", "Alive");

                    } else if (result.contains("Down(failed)")) {

                        Log.d("MessageType", "Node Is Failed Check Report");
                        Log.d("Node Id", tmpSplit[2]);
                        Log.d("Status", "Down(failed)");
                    }

                }  else if (result.contains("Replace Failed Node")) {

                    if(result.contains("Success")) {

                        Log.d("MessageType", "Replace Failed Node");
                        Log.d("Status", "Success");

                    } else if (result.contains("Unknown or Failed")) {

                        Log.d("MessageType", "Replace Failed Node");
                        Log.d("Status", "Unknown or Failed");
                    }

                }  else if (result.contains("Controller Reset Status")) {

                    if(result.contains("Success")) {

                        Log.d("MessageType", "Controller Reset Status");
                        Log.d("Status", "Success");


                    } else if (result.contains("Failed")) {

                        Log.d("MessageType", "Controller Reset Status");
                        Log.d("Status", "Failed");
                    }

                } else if (result.contains("Controller Attribute")) {
                    String[] tmpSplit = result.split(",");
                    String homeId = tmpSplit[1];
                    String nodeId = tmpSplit[2];
                    String role = tmpSplit[3];
                    String vendorId = tmpSplit[4];
                    String proType = tmpSplit[5];
                    String libType = tmpSplit[6];
                    String protocolVersion = tmpSplit[7];
                    String appVersion = tmpSplit[8];
                    Log.d("MessageType", "Controller Attribute");
                    Log.d("Home id", homeId);
                    Log.d("Node id", nodeId);
                    Log.d("Network Role", role);
                    Log.d("Vendor Id",vendorId);
                    Log.d("Vendor Product Type",proType);
                    Log.d("Z-wave Library Type",libType);
                    Log.d("Z-wave Protocol Version",protocolVersion);
                    Log.d("Application Version",appVersion);

                } else if (result.contains("All Node List Report")) {
                    String[] tmpSplit = result.split(",");
                    String nodeList = tmpSplit[1];
                    Log.d("MessageType", "All Node List Report");
                    Log.d("Added Node List", nodeList);

                }  else if (result.contains("Specify Node Info")) {
                    //String[] tmpSplit = result.split("Detialed Node Info");
                    //Log.d("MessageType", "Specify Node Info");
                    //Log.d("Detialed Node Info", tmpSplit[1]);

                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(result);
                        String nodeInfo = jsonObject.optString("Detialed Node Info");
                        Log.d(LOG_TAG,"gino: "+ nodeInfo);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else if (result.contains("Controller Init Status")) {
                    if(result.contains("Success")) {
                        Log.d("MessageType", "Controller Init Status");
                        Log.d("Status", "Success");
                    }

                } else if (result.contains("Node Battery Value")) {
                    String[] tmpSplit = result.split(",");
                    String nodeId = tmpSplit[1];
                    String endPointId = tmpSplit[2];
                    String batteryValue = tmpSplit[3];

                    Log.d("MessageType", "Node Battery Value");
                    Log.d("Node Id", nodeId);
                    Log.d("EndPoint Id", endPointId);
                    Log.d("Battery Value", batteryValue);

                } else if (result.contains("Switch Multi-lvl Report Information")) {
                    String[] tmpSplit = result.split(",");
                    String nodeId = tmpSplit[1];
                    String curValue = tmpSplit[2];

                    Log.d("MessageType", "Switch Multi-lvl Report Information");
                    Log.d("Node Id", nodeId);
                    Log.d("Cur Val", curValue);
                    Log.d("Tgt Val", "Unsupported");
                    Log.d("Durration", "Unsupported");

                } else if (result.contains("Power Level Get Information")) {
                    String[] tmpSplit = result.split(",");
                    String nodeId = tmpSplit[1];
                    String powerLevel = tmpSplit[2];
                    Log.d("MessageType", "Power Level Get Information");
                    Log.d("Node Id", nodeId);
                    Log.d("Power Level", powerLevel);

                } else if (result.contains("Switch All Get Information")) {
                    String[] tmpSplit = result.split(",");
                    String nodeId = tmpSplit[1];
                    String mode = tmpSplit[2];

                    Log.d("MessageType", "Switch All Get Information");
                    Log.d("Node Id", nodeId);
                    Log.d("mode", mode);

                } else if (result.contains("Binary Sensor Information")) {
                    String[] tmpSplit = result.split(",");
                    String nodeId = tmpSplit[1];
                    String eventType = tmpSplit[2];
                    String state = tmpSplit[3];

                    Log.d("MessageType", "Binary Sensor Information");
                    Log.d("Node Id", nodeId);
                    Log.d("Event Type", eventType);
                    Log.d("state", state);

                } else if (result.contains("Binary Sensor Support Get Information")) {
                    String[] tmpSplit = result.split(",");
                    String nodeId = tmpSplit[1];
                    String supportType = tmpSplit[2];
                    Log.d("MessageType", "Binary Sensor Support Get Information");
                    Log.d("Node Id", nodeId);
                    Log.d("Supported type", supportType);


                }  else if (result.contains("Meter Report Information")) {
                    String[] tmpSplit = result.split(",");
                    String nodeId = tmpSplit[1];
                    String meterType = tmpSplit[2];
                    String canReset = tmpSplit[3];
                    String supportUnit = tmpSplit[4];

                    Log.d("MessageType", "Meter Report Information");
                    Log.d("Node Id", nodeId);
                    Log.d("Meter type", meterType);
                    Log.d("Can be reset?", canReset);
                    Log.d("Supported unit", supportUnit);

                } else if (result.contains("Meter Cap Information")) {
                    String[] tmpSplit = result.split(",");
                    String nodeId = tmpSplit[1];
                    String meterType = tmpSplit[2];
                    String canReset = tmpSplit[3];
                    String supportUnit = tmpSplit[4];

                    Log.d("MessageType", "Meter Cap Information");
                    Log.d("Node Id", nodeId);
                    Log.d("Meter type", meterType);
                    Log.d("Can be reset?", canReset);
                    Log.d("Supported unit", supportUnit);



                } else if (result.contains("Wake Up Cap Report")) {
                    String[] tmpSplit = result.split(",");
                    String wakeUpSetting = tmpSplit[1];

                    Log.d("MessageType", "Wake Up Cap Report");
                    Log.d("Wake up settings", wakeUpSetting);


                } else if (result.contains("Door Lock Operation Report")) {
                    String[] tmpSplit = result.split(",");
                    String nodeId = tmpSplit[1];
                    String doorLockMode = tmpSplit[2];
                    String outsideMode = tmpSplit[3];
                    String insideMode = tmpSplit[4];
                    String doorCondition = tmpSplit[4];

                    Log.d("MessageType", "Door Lock Operation Report");
                    Log.d("Node Id", nodeId);
                    Log.d("Door Lock op mode", doorLockMode);
                    Log.d("Outside Door mode", outsideMode);
                    Log.d("Inside Door mode", insideMode);
                    Log.d("Door Condition", doorCondition);


                } else if (result.contains("Door Lock Configuration Report")) {
                    String[] tmpSplit = result.split(",");
                    String nodeId = tmpSplit[1];
                    String doorLockMode = tmpSplit[2];
                    String outsideMode = tmpSplit[3];
                    String insideMode = tmpSplit[4];

                    Log.d("MessageType", "Door Lock Configuration Report");
                    Log.d("Node Id", nodeId);
                    Log.d("Door Lock op mode", doorLockMode);
                    Log.d("Outside Door mode", outsideMode);
                    Log.d("Inside Door mode", insideMode);


                } else if (result.contains("Switch Color Report")) {
                    String[] tmpSplit = result.split(",");
                    String nodeId = tmpSplit[1];
                    String component = tmpSplit[2];
                    String value = tmpSplit[3];

                    Log.d("MessageType", "Switch Color Report");
                    Log.d("Node Id", nodeId);
                    Log.d("component id", component);
                    Log.d("value", value);


                } else if (result.contains("Supported Color Report")) {
                    String[] tmpSplit = result.split(",");
                    String nodeId = tmpSplit[1];
                    String supportColor = tmpSplit[2];

                    Log.d("MessageType", "Supported Color Report");
                    Log.d("Node Id", nodeId);
                    Log.d("Supported Color", supportColor);


                } else if (result.contains("Group Info Report")) {
                    String[] tmpSplit = result.split(",");
                    String nodeId = tmpSplit[1];
                    String groupId = tmpSplit[2];
                    String maxSupport = tmpSplit[3];
                    String groupMember = tmpSplit[4];

                    Log.d("MessageType", "Group Info Report");
                    Log.d("Node Id", nodeId);
                    Log.d("Group id", groupId);
                    Log.d("Max Supported endpoints", maxSupport);
                    Log.d("Group members",groupMember);


                } else if (result.contains("Supported Groupings Report")) {
                    String[] tmpSplit = result.split(",");
                    String nodeId = tmpSplit[1];
                    String maxNumber = tmpSplit[2];

                    Log.d("MessageType", "Supported Groupings Report");
                    Log.d("Node Id", nodeId);
                    Log.d("Max number of groupings", maxNumber);


                }  else if (result.contains("Active Groups Report")) {
                    String[] tmpSplit = result.split(",");
                    String nodeId = tmpSplit[1];
                    String currentActive = tmpSplit[2];

                    Log.d("MessageType", "Active Groups Report");
                    Log.d("Node Id", nodeId);
                    Log.d("Current active group", currentActive);


                } else if (result.contains("Notification Get Information")) {
                    String[] tmpSplit = result.split(",");
                    String nodeId = tmpSplit[1];
                    String status = tmpSplit[2];
                    String type = tmpSplit[3];
                    String event = tmpSplit[4];

                    Log.d("MessageType", "Notification Get Information");
                    Log.d("Node Id", nodeId);
                    Log.d("Notification-status", status);
                    Log.d("Notification-type", type);
                    Log.d("Notification-event",event);


                } else if (result.contains("Notification Supported Report")) {
                    String[] tmpSplit = result.split(",");
                    String nodeId = tmpSplit[1];
                    String type = tmpSplit[2];
                    String support = tmpSplit[3];

                    Log.d("MessageType", "Notification Supported Report");
                    Log.d("Node Id", nodeId);
                    Log.d("Have alarm type", type);
                    Log.d("supported notification", support);

                } else if (result.contains("Supported Notification Event Report")) {
                    String[] tmpSplit = result.split(",");
                    String nodeId = tmpSplit[1];
                    String type = tmpSplit[2];
                    String event = tmpSplit[3];

                    Log.d("MessageType", "Supported Notification Event Report");
                    Log.d("Node Id", nodeId);
                    Log.d("Notification Type", type);
                    Log.d("event", event);


                } else if (result.contains("Central Scene Supported Report")) {
                    String[] tmpSplit = result.split(",");
                    String nodeId = tmpSplit[1];
                    String Scenes = tmpSplit[2];
                    String attributes = tmpSplit[3];
                    String supportKey = tmpSplit[4];

                    Log.d("MessageType", "Central Scene Supported Report");
                    Log.d("Node Id", nodeId);
                    Log.d("Supported Scenes", Scenes);
                    Log.d("Is Same Key Attributes", attributes);
                    Log.d("Supported Key Attr", supportKey);


                } else if (result.contains("Central Scene Notification")) {
                    String[] tmpSplit = result.split(",");
                    String nodeId = tmpSplit[1];
                    String attrKey = tmpSplit[2];
                    String sceneNumber = tmpSplit[3];

                    Log.d("MessageType", "Central Scene Notification");
                    Log.d("Node Id", nodeId);
                    Log.d("key attr", attrKey);
                    Log.d("Scene number", sceneNumber);


                } else if (result.contains("Firmware Info Report")) {
                    String[] tmpSplit = result.split(",");
                    String nodeId = tmpSplit[1];
                    String vendorId = tmpSplit[2];
                    String firmwareId = tmpSplit[3];
                    String Checksum = tmpSplit[4];
                    String maxSize = tmpSplit[5];
                    String sizeFixed = tmpSplit[6];
                    String Upgradable = tmpSplit[7];
                    String otherFirmwareTarger = tmpSplit[8];
                    String otherFirmwareId = tmpSplit[9];

                    Log.d("MessageType", "Firmware Info Report");
                    Log.d("Node Id", nodeId);
                    Log.d("Vendor id", vendorId);
                    Log.d("Firmware id", firmwareId);
                    Log.d("Checksum", Checksum);
                    Log.d("Max fragment size", maxSize);
                    Log.d("Size fixed", sizeFixed);
                    Log.d("Upgradable", Upgradable);
                    Log.d("Other Firmware targer", otherFirmwareTarger);
                    Log.d("Other firmware id", otherFirmwareId);


                } else if (result.contains("Firmware Update Status Report")) {
                    String[] tmpSplit = result.split(",");
                    String nodeId = tmpSplit[1];
                    String status = tmpSplit[2];

                    Log.d("MessageType", "Firmware Update Status Report");
                    Log.d("Node Id", nodeId);
                    Log.d("Update status", status);


                } else if (result.contains("Firmware Update Completion Status Report")) {
                    String[] tmpSplit = result.split(",");
                    String nodeId = tmpSplit[1];
                    String status = tmpSplit[2];

                    Log.d("MessageType", "Firmware Update Completion Status Report");
                    Log.d("Node Id", nodeId);
                    Log.d("Update status", status);


                } else if (result.contains("Firmware Update restart Status Report")) {
                    String[] tmpSplit = result.split(",");
                    String nodeId = tmpSplit[1];
                    String status = tmpSplit[2];

                    Log.d("MessageType", "Firmware Update restart Status Report");
                    Log.d("Node Id", nodeId);
                    Log.d("Restart status", status);


                } else if (result.contains("Sensor Info Report")) {
                    String[] tmpSplit = result.split(",");
                    String nodeId = tmpSplit[1];
                    String type = tmpSplit[2];
                    String precision = tmpSplit[3];
                    String unit = tmpSplit[4];
                    String value = tmpSplit[5];

                    Log.d("MessageType", "Sensor Info Report");
                    Log.d("Node Id", nodeId);
                    Log.d("type", type);
                    Log.d("precision", precision);
                    Log.d("unit",unit);
                    Log.d("value",value);


                } else if (result.contains("Command Queue State Report")) {
                    String[] tmpSplit = result.split(",");
                    String nodeId = tmpSplit[1];
                    String state = tmpSplit[2];

                    Log.d("MessageType", "Command Queue State Report");
                    Log.d("Node Id", nodeId);
                    Log.d("command state", state);


                } else if (result.contains("Command Queue Info Report")) {
                    String[] tmpSplit = result.split(",");
                    String nodeId = tmpSplit[1];
                    String queue = tmpSplit[2];

                    Log.d("MessageType", "Command Queue Info Report");
                    Log.d("Node Id", nodeId);
                    Log.d("command queue", queue);


                } else if (result.contains("Network Health Check")) {
                    String[] tmpSplit = result.split(",");
                    String status = tmpSplit[1];

                    Log.d("MessageType", "Network Health Check");
                    Log.d("Status", status);


                } else if (result.contains("Network IMA Info Report")) {
                    String[] tmpSplit = result.split(",");
                    String nodeId = tmpSplit[1];
                    String health = tmpSplit[2];
                    String number = tmpSplit[3];
                    String value = tmpSplit[4];
                    String channel = tmpSplit[5];

                    Log.d("MessageType", "Network IMA Info Report");
                    Log.d("Direct nodeid", nodeId);
                    Log.d("Network Health", health);
                    Log.d("RSSI hops number", number);
                    Log.d("RSSI hops value", value);
                    Log.d("Transmit channel", channel);


                } else if (result.contains("Network RSSI Info Report")) {
                    String[] tmpSplit = result.split(",");
                    String channel1 = tmpSplit[1];
                    String channel2 = tmpSplit[2];

                    Log.d("MessageType", "Network RSSI Info Report");
                    Log.d("Value of channel 1", channel1);
                    Log.d("Value of channel 2", channel2);


                } else if (result.contains("Provision List Report")) {
                    String[] tmpSplit = result.split(",");
                    String dsk = tmpSplit[1];
                    String type = tmpSplit[2];
                    String id = tmpSplit[3];
                    String bootMode = tmpSplit[4];
                    String state = tmpSplit[5];
                    String location = tmpSplit[6];
                    String name = tmpSplit[7];

                    if(result.contains("Error")) {
                        Log.d("MessageType", "Provision List Report");
                        Log.d("Error", "No list entry");

                    } else {
                        Log.d("MessageType", "Provision List Report");
                        Log.d("DSK", dsk);
                        Log.d("Device type info", type);
                        Log.d("Device id info", id);
                        Log.d("Device Boot Mode", bootMode);
                        Log.d("Device Inclusion state", state);
                        Log.d("Device Location", location);
                        Log.d("Device Name", name);

                    }

                } else if (result.contains("All Provision List Report")) {
                    String[] tmpSplit = result.split("Detial provision list");
                    String detialProvisionList = tmpSplit[1];

                    if(result.contains("Error")) {
                        Log.d("MessageType", "All Provision List Report");
                        Log.d("Error", "No list entry found");

                    } else {
                        Log.d("MessageType", "All Provision List Report");
                        Log.d("Detial provision list", detialProvisionList);

                    }

                } else if (result.contains("Controller DSK Report")) {
                    String[] tmpSplit = result.split(",");
                    String dsk = tmpSplit[1];

                    Log.d("MessageType", "Controller DSK Report");
                    Log.d("DSK", dsk);

                }
            }

        };

    }

    public ZwaveControlService.zwaveControlReq_CallBack mReqCallBacks;
    {
        mReqCallBacks = new ZwaveControlService.zwaveControlReq_CallBack() {
            @Override
            public void zwaveControlReqResultCallBack(String className, String result) {
                Log.i(LOG_TAG, "class name = [" + className + "]| result = " + result);

                if (result.contains("Grant Keys Msg")) {

                    Log.i(LOG_TAG,"holdTime");


                } else if (result.contains("PIN Requested Msg")) {
                    //DeviceInfo.reqKey = 11394;

                } else if (result.contains("Client Side Au Msg")) {
                    //DeviceInfo.reqKey = 0;
                }
            }
        };
    }

    private void getImaInfo(String result) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(result);
            String ima = jsonObject.optString("RSSI hops value");
            Log.d(LOG_TAG,"rssi: "+ ima);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getOpenControllerInfo(String result) {
        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject(result);
            String NetworkRole = jsonObject.optString("Network Role");
            Log.d(LOG_TAG,"Network Role: "+ NetworkRole);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showCommandClass(String result) {
        String[] resultSplit = result.split("Interface Class\":\"");
        for(int i = 0; i < resultSplit.length; i++) {
            Log.d(LOG_TAG,resultSplit[i]);
            txAllMsg.setText(resultSplit[i]);
        }
    }

    //authenticate security of device is s0 s2 non-security
    private void showSecurityStatus(String result) {
        String[] resultSplit = result.split(",");
        String securityStatus = "";
        for(int i = 10; i < resultSplit.length; i++) { //i =10 不顯示Controller 的 security Status
            if (resultSplit[i].contains("Node security inclusion status")) {
                if (resultSplit[i].contains("S2")) {
                    //Log.i(LOG_TAG, "S2 gino!!!!!!!!!!!!!!!!!!");
                    securityStatus = "Device is S2 security";
                } else if (resultSplit[i].contains("Normal")) {
                    //Log.i(LOG_TAG, "Normal gino!!!!!!!!!!!!!!!!!!");
                    securityStatus = "Device is none security";
                } else if (resultSplit[i].contains("S0")) {
                    //Log.i(LOG_TAG, "S0 gino!!!!!!!!!!!!!!!!!!");
                    securityStatus = "Device is S0 security";
                }
            }
        }
        txAllMsg.setText(securityStatus);
    }

    //show dongle info
    private void showNodeInfo(String result) {
        String[] resultSplit = result.split("Home id");
        txAllMsg.setText(resultSplit[1]);
    }

    //show DSK number
    private void showDsk(String result) {
        final String showDsk = result;
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(showDsk);
            String dsknumber = jsonObject.optString("DSK");
            if(dsknumber.contains("00000")) {
                txDsk.setText("NO DSK number");
            } else {
                SpannableString content = new SpannableString(dsknumber);       //前五碼底線
                content.setSpan(new UnderlineSpan(), 0, 5, 0);
                txDsk.setText(content);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void addRemoveDevice(String result) {
        final String addRemoveResult = result;
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (addRemoveResult.contains("MessageType")){
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(addRemoveResult);
                        String messageType = jsonObject.optString("MessageType");
                        String status = jsonObject.optString("Status");
                        if ("Node Add Status".equals(messageType)) {
                            if ("Success".equals(status)) {
                                txAllMsg.setText("Success");
                                zwaveService.getDeviceInfo();   //有delay所以做兩次
                                zwaveService.getDeviceInfo();
                            } else if("Learn Ready".equals(status)){
                                txAllMsg.setText("Please press the trigger button of the device");
                            }else{
                                txAllMsg.setText(status);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }else{

                    String addRemoveMode = "";
                    if (addRemoveResult.contains("addDevice")){
                        addRemoveMode = "addDevice";
                    }else if (addRemoveResult.contains("removeDevice")){
                        addRemoveMode = "removeDevice";
                    }
                    String[] tokens = addRemoveResult.split(":");
                    if (tokens.length<3){
                        Log.i(LOG_TAG,addRemoveMode+" : wrong format "+addRemoveResult);
                    } else {
                        String tHomeId = tokens[1];
                        String tNodeId = tokens[2];

                        Log.i(LOG_TAG,addRemoveMode+" HomeId = "+tHomeId+" | NodeId = "+tNodeId);
                        txAllMsg.setText(addRemoveMode+" Success " + " | NodeId = "+tNodeId);


                        if (addRemoveMode.equals("addDevice")) {
                            nodeIdArr.add(Integer.valueOf(tNodeId));
                        } else{
                            nodeIdArr.remove(Integer.valueOf(tNodeId));
                            txAllMsg.setText("");
                        }

                        ArrayAdapter<Integer> devList = new ArrayAdapter<Integer>(WelcomeActivity.this,
                                android.R.layout.simple_spinner_dropdown_item,
                                convertIntegers(nodeIdArr));

                        spNodeIdList.setAdapter(devList);
                        spNodeIdList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                selectNode = Integer.valueOf(spNodeIdList.getSelectedItem().toString());
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                    }
                }
            }
        });
    }

    //add & remove node of device to spinner
    private static Integer[] convertIntegers(List<Integer> integers)
    {
        Integer[] ret = new Integer[integers.size()];
        for (int i=0; i < ret.length; i++)
        {
            ret[i] = integers.get(i).intValue();
        }
        return ret;
    }

    //add & remove provision list to spinner
    private static String[] converProvisionList(List<String> dskString)
    {
        String[] ret = new String[dskString.size()];
        for (int i=0; i < ret.length; i++)
        {
            ret[i] = dskString.get(i);
        }
        return ret;
    }


    //can take off this function !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private class usbReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            openController();
                        }
                    } else {
                        Log.d(LOG_TAG,"USB"+ "permission denied for device " + device);
                        progressDialog.cancel();
                        finish();
                        System.exit(0);
                    }
                }
            }
        }
    }

    //open z-wave dongle
    private void openController() {
        timer.schedule(new mTimerTask(), 1000 * 120);
        String openResult = zwaveService.openController();
        if (openResult.contains(":0")){
            DeviceInfo.isOpenControllerFinish = true;
        }
    }

    //init sensor 類別裝置,當sensor裝置改變狀態會自動回報
    private void initSensorfunc() {

        List<ZwaveDevice> list = zwDevManager.queryZwaveDeviceList();

        for (int idx = 1; idx < list.size(); idx++) {

            int nodeId = list.get(idx).getNodeId();
            String devType = list.get(idx).getDevType();

            Log.i(LOG_TAG,"#"+nodeId+" | devType = "+devType);

            if (devType.equals("SENSOR")) {
                String devNodeInfo = list.get(idx).getNodeInfo();

                if (devNodeInfo.contains("COMMAND_CLASS_BATTERY")) {
                    Log.i(LOG_TAG, "BATTERY");
                    zwaveService.getDeviceBattery(Const.zwaveType,nodeId);
                }

                if (devNodeInfo.contains("COMMAND_CLASS_NOTIFICATION")) {
                    try {
                        JSONObject jsonObject = new JSONObject(devNodeInfo);
                        if (jsonObject.getString("Product id").equals("001F")) {
                            //Water
                            zwaveService.getSensorNotification(nodeId, 0x00, 0x05, 0x00);
                        } else if (jsonObject.getString("Product id").equals("000C")) {
                            //Motion
                            zwaveService.getSensorNotification(nodeId, 0x00, 0x07, 0x00);
                            //Door/Window
                            zwaveService.getSensorNotification(nodeId, 0x00, 0x06, 0x00);
                        } else if (jsonObject.getString("Product id").equals("0036")) {
                            //Door/Window
                            zwaveService.getSensorNotification(nodeId, 0x00, 0x06, 0x00);
                        } else if (jsonObject.getString("Product id").equals("001E")) {
                            //SMOKE
                            zwaveService.getSensorNotification(nodeId, 0x00, 0x01, 0x00);
                        } else if (jsonObject.getString("Product id").equals("0050")) {
                            //Motion
                            zwaveService.getSensorNotification(nodeId, 0x00, 0x07, 0x00);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (devNodeInfo.contains("COMMAND_CLASS_SENSOR_MULTILEVEL")) {
                    try {
                        zwaveService.getSensorMultiLevel(Const.zwaveType,nodeId);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                zwaveService.getMeterSupported(nodeId);
                zwaveService.GetSensorBinarySupportedSensor(nodeId);

            }
        }

    }

    private void showAddDialog() {

        final android.app.AlertDialog.Builder addDialog = new android.app.AlertDialog.Builder(WelcomeActivity.this);
        LayoutInflater layoutInflater = LayoutInflater.from(WelcomeActivity.this);
        View view = layoutInflater.inflate(R.layout.dialog_add_layout, null);
        addDialog.setView(view);

        final android.app.AlertDialog alertDialog = addDialog.create();
        alertDialog.show();
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText("Add Device Success");

        // type spinner
        final EditText message = (EditText) view.findViewById(R.id.message);
        final Spinner spDevType = (Spinner) view.findViewById(R.id.spDevType);
        Button positiveButton = (Button) view.findViewById(R.id.positiveButton);
        Button negativeButton = (Button) view.findViewById(R.id.negativeButton);

        ArrayAdapter<String> devTypeList = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                DeviceInfo.deviceType);

        spDevType.setAdapter(devTypeList);

        // room spinner

        final Spinner spRoom = (Spinner) view.findViewById(R.id.spAllRoom);

        ArrayAdapter<String> roomList = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                DeviceInfo.allRoomName);

        spRoom.setAdapter(roomList);

        //message.setText(nodeId);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                alertDialog.dismiss();
                backToHomeActivity();
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                //finish();
            }
        });

        alertDialog.show();

    }

}
