package com.askey.firefly.zwave.control.ui;

import android.app.Activity;
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
    private TextView txText,txDsk,txAllMsg,txApi;
    private EditText editDsk,editSetApiValue;
    private CheckBox cb1,cb2,cb3;
    private String devType = "Zwave";
    private String inputDsk;
    private int selectNode;
    private String selectProvisionList;
    private int getDeviceInfoFlag = 0;
    private boolean getProvisionListFlag = false;
    private boolean getProvisionNodeFlag = false;
    private boolean InclusionState = true;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        mContext = this;
        initBtn();

        //showProgressDialog(mContext, "Initializing，Open Zwave Controller...");

        Intent MqttIntent = new Intent(WelcomeActivity.this, MQTTBroker.class);
        startService(MqttIntent);

        zwDevManager = ZwaveDeviceManager.getInstance(this);

        // bind service
        Intent serviceIntent = new Intent(this, ZwaveControlService.class);
        this.bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);

        new Thread(checkInitStatus).start();

    }

    //讀取ApiName.txt內容
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
        txText = (TextView) findViewById(R.id.txMsg);
        txAllMsg = (TextView) findViewById(R.id.txAllMsg);
        txApi = (TextView) findViewById(R.id.txApi);

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

    }


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
            //initZwave();
        }
    };

    private void initZwave() {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                timerCancel();
                //Intent intent = new Intent();
                //intent.setClass(mContext, HomeActivity.class);
                //hideProgressDialog();
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
                    inputDsk = editDsk.getText().toString() + "\0";
                    byte[] dskNumber = inputDsk.getBytes();
                    zwaveService.addDevice(devType, dskNumber);
                } else if (editDsk.length() != 0 && editDsk.length() == 5) {
                    //Log.i(LOG_TAG, "call zwaveService.addDevice()");
                    inputDsk = editDsk.getText().toString() + "\0";
                    byte[] dskNumber = inputDsk.getBytes();
                    zwaveService.addDevice(devType, dskNumber);
                } else if (editDsk.length() == 0) {
                    zwaveService.addDevice(devType, DeviceInfo.tempDsk);
                } else
                    Toast.makeText(this, "格式錯誤 !", Toast.LENGTH_SHORT).show();
                getDeviceInfoFlag = 1;
                break;

            case R.id.btnRemove:
                Log.i(LOG_TAG, "call zwaveService.removeDevice()");
                zwaveService.removeDevice(devType, 0);
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
                    zwaveService.ZwController_rmAllProvisionListEntry();
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_battery_get")) {
                    zwaveService.getDeviceBattery(devType, Integer.valueOf(spNodeIdList.getSelectedItem().toString()));
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_sensor_multilevel_get")) {
                    try {
                        zwaveService.getSensorMultiLevel(devType, Integer.valueOf(spNodeIdList.getSelectedItem().toString()));
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
                    zwaveService.getBasic(devType, selectNode);
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_basic_set")) {
                    zwaveService.setBasic(devType, selectNode, Integer.valueOf(editSetApiValue.getText().toString()));
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_switch_multilevel_get")) {
                    zwaveService.getSwitchMultiLevel(devType, selectNode);
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_switch_multilevel_set")) {
                    zwaveService.setSwitchMultiLevel(devType, selectNode, Integer.valueOf(editSetApiValue.getText().toString()), 1);
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_get_support_switch_type")) {
                    zwaveService.getSupportedSwitchType(selectNode);
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_start_stop_switchlevel_change")) {
                    //zwaveService.startStopSwitchLevelChange();
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_configuration_get")) {
                    //zwaveService.getConfiguration();
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_configuration_set")) {
                    //zwaveService.setConfiguration();
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_powerLevel_get")) {
                    zwaveService.getPowerLevel(selectNode);
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_switch_all_on")) {
                    zwaveService.setSwitchAllOn(devType, selectNode);
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_switch_all_off")) {
                    zwaveService.setSwitchAllOff(devType, selectNode);
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
                    zwaveService.getMeter(devType, selectNode, 10);
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
                    zwaveService.getLampColor(devType, selectNode);
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_switch_color_supported_get")) {
                    //zwaveService.
                } else if (spApiList.getSelectedItem().toString().contains("zwcontrol_switch_color_set")) {
                    String[] temp = editSetApiValue.getText().toString().split(",");
                    zwaveService.setLampColor(devType, selectNode, Integer.valueOf(temp[0]), Integer.valueOf(temp[1]), Integer.valueOf(temp[2]));
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
                }
                break;
            case R.id.btnaddProList:
                if (editDsk.length() != 0 && editDsk.length() == 47) {    // editDsk will 5-digit or full code
                    InclusionState = true;
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
                    InclusionState = false;
                    editDsk.setText(selectProvisionList);
                    rmProvisionList();
                    addProvisionList();
                }
        }
    }

    //載入provision list
    private void loadProvisionList(String result) {
        final String allProvisionList = result;
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(allProvisionList);
            String provisinoInfo = jsonObject.optString("Detial provision list");
            String[] dskNumber = provisinoInfo.split(",");
            for(int i = 0; i < dskNumber.length; i++) {
                if (dskNumber[i].contains("DSK")) {
                    String[] tmpDsk = dskNumber[i].split("\"");
                    for(int j = 0; j < tmpDsk.length; j++) {
                        if(j % 4 == 0 && getProvisionListFlag == false) {
                            Log.d(LOG_TAG, tmpDsk[j + 3]);
                            provisionListArr.add(tmpDsk[j + 3]); //抓取DSK值
                        }
                    }
                }
            }

            getProvisionListFlag = true;    //Provision List 載入到 spinner 1次

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //取得Provision List
    private void getProvisionList() {
        getProvisionNodeFlag = false;
        inputDsk =  selectProvisionList + "\0";
        //Log.d(LOG_TAG,inputDsk);
        byte[] dskNumber = inputDsk.getBytes();
        zwaveService.getProvisionListEntry(devType,dskNumber);
    }

    private void getProvisionNode(String result) {
        final String provisionInfo = result;
        JSONObject jsonObject = null;
        JSONObject jsonObject2 = null;
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

    //移除Provision List
    private void rmProvisionList() {
        getProvisionNodeFlag = true;
        inputDsk =  selectProvisionList + "\0";
        Log.d(LOG_TAG,inputDsk);
        byte[] dskNumber = inputDsk.getBytes();
        zwaveService.getProvisionListEntry(devType,dskNumber);
        zwaveService.rmProvisionListEntry(devType,dskNumber);
        provisionListArr.remove(selectProvisionList);
        ArrayAdapter<String> provisionList = new ArrayAdapter<String>(WelcomeActivity.this,
                android.R.layout.simple_spinner_dropdown_item, converProvisionList(provisionListArr));
        spProvisionList.setAdapter(provisionList);
    }

    //新增Provision List
    private void addProvisionList() {

        inputDsk = editDsk.getText().toString() + "\0";
        Log.d(LOG_TAG,inputDsk);
        byte[] dskNumber = inputDsk.getBytes();
        if(provisionListArr.contains(editDsk.getText().toString())) {
            Toast.makeText(this,"already Provision List", Toast.LENGTH_SHORT).show();
        } else {
            provisionListArr.add(editDsk.getText().toString());
            zwaveService.addProvisionListEntry(devType,dskNumber,InclusionState);
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

    //限制editDsk輸入字數
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
            //zwaveService.closeController();
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
                zwaveService.register(mCallback);
                openController();
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
        if (usbReceiver != null)
            unregisterReceiver(usbReceiver);
        try {
            this.unbindService(conn);
        } catch (Exception e) {
            Log.d(LOG_TAG, e.toString());
        }
    }

    public ZwaveControlService.zwaveCallBack mCallback = new ZwaveControlService.zwaveCallBack() {

        //監聽關鍵字回報 從ZwaveControlService.jave zwaveCallBack -> zwcontrol_api.c "MessageType"
        @Override
        public void zwaveControlResultCallBack(String className, String result) {
            Log.i(LOG_TAG,"class name = [" + className + "]| result = " + result);
            if (className.equals("addDevice") || className.equals("removeDevice") ){
                addRemoveResult(result);
            } else if (className.equals("DSK")) {
                showDsk(result);
            } else if (className.equals("getDeviceInfo")) {
                zwaveService.getAllProvisionListEntry();
                if(getDeviceInfoFlag == 1) {
                    showSecurityStatus(result);
                } else if (getDeviceInfoFlag == 2) {
                    showCommandClass(result);
                } else if (getDeviceInfoFlag == 3) {
                    showNodeInfo(result);
                }
            } else if (className.equals("getSensorMultiLevel") || className.equals("getSensorNotification") ||
                    className.equals("getLampColor") || className.equals("getBasic") || className.equals("getPowerLevel")) {
                for(int i = 0; i < result.length(); i++) {
                    txApi.setText(result);
                }
            } else if (className.equals("All Provision List Report")) {
                loadProvisionList(result);
            } else if (className.equals("Provision List Report")) {
                if(getProvisionNodeFlag) {
                    getProvisionNode(result);
                } else {
                    for(int i = 0; i < result.length(); i++) {
                        txApi.setText(result);
                    }
                }
            } else if (className.equals("openController")) {
                getOpenControllerInfo(result);
            } else if (className.equals("Network IMA Info Report")) {
                getImaInfo(result);
            }
        }
    };

    private void getImaInfo(String result) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(result);
            String ima = jsonObject.optString("RSSI hops value");
            txAllMsg.setText("rssi: "+ ima);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getOpenControllerInfo(String result) {
        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject(result);
            String NetworkRole = jsonObject.optString("Network Role");
            txAllMsg.setText("Network Role: "+ NetworkRole);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //顯示command class
    private void showCommandClass(String result) {
        String[] resultSplit = result.split("Interface Class\":\"");
        for(int i = 0; i < resultSplit.length; i++) {
            Log.d(LOG_TAG,resultSplit[i]);
            txApi.setText(resultSplit[i]);
        }
    }

    //辦別新增裝置是s0 s2 non-security
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

    //顯示dongle info
    private void showNodeInfo(String result) {
        String[] resultSplit = result.split("Home id");
        txApi.setText(resultSplit[1]);
    }

    //顯示DSK number
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

    //新增/移除裝置處理
    private void addRemoveResult(String result) {
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
                                txText.setText("Success");
                                zwaveService.getDeviceInfo();   //有delay所以做兩次
                                zwaveService.getDeviceInfo();
                            } else if("Learn Ready".equals(status)){
                                txText.setText("Please press the trigger button of the device");
                            }else{
                                txText.setText(status);
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
                        txText.setText(addRemoveMode+" Success " + " | NodeId = "+tNodeId);


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

                        //Message message = new Message();
                        //Bundle data = new Bundle();
                        //data.putInt("nodeId", Integer.parseInt(tNodeId));
                        //data.putString("homeId", tHomeId);
                        //message.setData(data);
                        //message.what = 2001;
                        //mHandler.sendMessage(message);
                    }
                }
            }
        });
    }

    //新增/移除裝置節點到spinner
    private static Integer[] convertIntegers(List<Integer> integers)
    {
        Integer[] ret = new Integer[integers.size()];
        for (int i=0; i < ret.length; i++)
        {
            ret[i] = integers.get(i).intValue();
        }
        return ret;
    }

    //新增/移除裝置節點到spinner
    private static String[] converProvisionList(List<String> dskString)
    {
        String[] ret = new String[dskString.size()];
        for (int i=0; i < ret.length; i++)
        {
            ret[i] = dskString.get(i);
        }
        return ret;
    }

    //開啟USB權限
    private void requestControlUSBPermission() {
        openController();
        /*
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        final List<UsbSerialDriver> drivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);

        boolean found = false;

        for (final UsbSerialDriver driver : drivers) {
            if (found) break;

            final List<UsbSerialPort> ports = driver.getPorts();

            for (final UsbSerialPort port : ports) {
                if(!usbManager.hasPermission(port.getDriver().getDevice())) {
                    PendingIntent mPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(port.getDriver().getDevice(), mPendingIntent);
                    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                    usbReceiver = new usbReceiver();
                    registerReceiver(usbReceiver, filter);
                } else {
                    openController();
                }
                found = true;
                break;
            }
        }
        */
    }


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
                        Log.d("USB", "permission denied for device " + device);
                        progressDialog.cancel();
                        finish();
                        System.exit(0);
                    }
                }
            }
        }
    }

    //開啟z-wave dongle
    private void openController() {
        /*
        try{
            // delay 1 second
            Thread.sleep(1000);

        } catch(InterruptedException e){
            e.printStackTrace();

        }
        */
        //timer.schedule(new mTimerTask(), 1000 * 120);
        String openResult = zwaveService.openController();
        if (openResult.contains(":0")){
            DeviceInfo.isOpenControllerFinish = true;
        }
    }

    //init sensor 類別裝置,當sensor裝置改變狀態會自動回報
    private void initSensorfunc() {

        List<ZwaveDevice> list = zwDevManager.queryZwaveDeviceList();

        Log.e(LOG_TAG,"######## initSensorfunc start #######");
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
        int getResult = zwaveService.getControllerRssi();
        Log.i(LOG_TAG,"getResult = "+getResult);

        Log.e(LOG_TAG,"######## initSensorfunc end #######");

    }

}
