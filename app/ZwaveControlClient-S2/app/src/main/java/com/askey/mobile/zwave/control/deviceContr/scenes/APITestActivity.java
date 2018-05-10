package com.askey.mobile.zwave.control.deviceContr.scenes;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.askey.mobile.zwave.control.R;
import com.askey.mobile.zwave.control.base.BaseActivity;
import com.askey.mobile.zwave.control.data.APIListData;
import com.askey.mobile.zwave.control.data.LocalMqttData;
import com.askey.mobile.zwave.control.deviceContr.adapter.ApiArrayAdapter;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.localMqtt.MqttMessageArrived;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 项目名称：ZwaveControlClient-S2
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2018/3/21 13:37
 * 修改人：skysoft
 * 修改时间：2018/3/21 13:37
 * 修改备注：
 */
public class APITestActivity extends BaseActivity {
    private final String TAG = APITestActivity.class.getSimpleName();
    private Spinner mySpinner;
    private String title;
    private LinearLayout parameterLayout;
    private Button testButton;
    private EditText result;
    private ArrayList<String> data_list = new ArrayList<String>();
    private static String api;
    private static String dsk = "51525-35455-41424-34445-31323-33435-21222-32425";
    private String deviceId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api_test);

        title = getIntent().getStringExtra("title");
//        dsk = getIntent().getStringExtra("dsk");
        deviceId = getIntent().getStringExtra("nodeId");
        Log.i(TAG,"device " + deviceId);
        TextView titleView = (TextView) this.findViewById(R.id.api_test_title);
        parameterLayout = (LinearLayout) this.findViewById(R.id.parameter_layout);
        mySpinner = (Spinner) this.findViewById(R.id.spinner2);
        titleView.setText(title);
        result = (EditText) findViewById(R.id.editText3);
        initData();
        testButton = (Button) findViewById(R.id.test_api);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "~~~~~~~~~~test api~~~~~~~~~" + api);
                requestMqtt();
            }
        });


        MQTTManagement.getSingInstance().rigister(mMqttMessageArrived);
    }

    private void addParamsLayout(String lable) {
        LinearLayout layout = new LinearLayout(this);//为本Activity创建一个线性布局对象

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams textviewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams editviewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView textView = new TextView(this);
        textView.setText(lable + " : ");
        layout.addView(textView, textviewParams);//加入的同时，也就设置了TextView相对于

        EditText editText1 = new EditText(this);
        layout.addView(editText1, editviewParams);//加入的同时，也就设置了TextView相对于

        parameterLayout.addView(layout, layoutParams);
    }

    private void initData() {
        if (title.equals("Command Queue相关接口")) {
            data_list = APIListData.getCommandQueueAPIList();
        } else if (title.equals("Network Health Check功能")) {
            data_list = APIListData.getNetworkHealthCheckAPIList();
        } else if (title.equals("Smart Start相关API")) {
            data_list = APIListData.getSmartStartAPIList();
        } else if (title.equals("Controller相关接口")) {
            data_list = APIListData.getControllerAPIList();
        } else if (title.equals("COMMAND_CLASS_BATTERY")) {
            data_list = APIListData.getBatteryAPIList();
        } else if (title.equals("COMMAND_CLASS_BASIC")) {
            data_list = APIListData.getBasicVerAPIList();
        } else if (title.equals("COMMAND_CLASS_SWITCH_MULTILEVEL")) {
            data_list = APIListData.getSwitchMultiLevelAPIList();
        } else if (title.equals("COMMAND_CLASS_CONFIGURATION")) {
            data_list = APIListData.getConfigurationAPIList();
        } else if (title.equals("COMMAND_CLASS_POWERLEVEL")) {
            data_list = APIListData.getPowerLevelAPIList();
        } else if (title.equals("COMMAND_CLASS_SWITCH_ALL")) {
            data_list = APIListData.getSwitchAllAPIList();
        } else if (title.equals("COMMAND_CLASS_SWITCH_BINARY")) {
            data_list = APIListData.getSwitchBinaryAPIList();
        } else if (title.equals("COMMAND_CLASS_SENSOR_BINARY")) {
            data_list = APIListData.getSensorBinaryV2APIList();
        } else if (title.equals("COMMAND_CLASS_METER")) {
            data_list = APIListData.getMeterV3APIList();
        } else if (title.equals("COMMAND_CLASS_WAKE_UP")) {
            data_list = APIListData.getWakeUpAPIList();
        } else if (title.equals("COMMAND_CLASS_DOOR_LOCK")) {
            data_list = APIListData.getDoorLockAPIList();
        } else if (title.equals("COMMAND_CLASS_USER_CODE")) {
            data_list = APIListData.getUserCodeAPIList();
        } else if (title.equals("COMMAND_CLASS_PROTECTION")) {
            data_list = APIListData.getProtectionAPIList();
        } else if (title.equals("COMMAND_CLASS_INDICATOR")) {
            data_list = APIListData.getIndicatorAPIList();
        } else if (title.equals("COMMAND_CLASS_DOOR_LOCK_LOOGING")) {
            data_list = APIListData.getDoorLockLoogingAPIList();
        } else if (title.equals("COMMAND_CLASS_LANGUAGE")) {
            data_list = APIListData.getLanguageAPIList();
        } else if (title.equals("COMMAND_CLASS_SWITCH_COLOR")) {
            data_list = APIListData.getSwitchColorAPIList();
        } else if (title.equals("COMMAND_CLASS_BARRIER_OPERATOR")) {
            data_list = APIListData.getBarrierOperatorAPIList();
        } else if (title.equals("COMMAND_CLASS_BASIC_TARIFF_INFO")) {
            data_list = APIListData.getBasicTariffInfoAPIList();
        } else if (title.equals("COMMAND_CLASS_ASSOCIATION") || title.equals("COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION")) {
            data_list = APIListData.getAssociationAPIList();
        } else if (title.equals("COMMAND_CLASS_NOTIFICATION")) {
            data_list = APIListData.getNotificationAPIList();
        } else if (title.equals("COMMAND_CLASS_CENTRAL_SCENE_VERSION")) {
            data_list = APIListData.getCentralSceneVersionAPIList();
        } else if (title.equals("COMMAND_CLASS_SCENE_ACTUATOR")) {
            data_list = APIListData.getSceneActuatorConfAPIList();
        } else if (title.equals("COMMAND_CLASS_FIRMWARE_UPGRADE_MD")) {
            data_list = APIListData.getFirmwareUpdateMdAPIList();
        } else if (title.equals("COMMAND_CLASS_MUTILCMD")) {
            data_list = APIListData.getMultiCmdAPIList();
        } else if (title.equals("COMMAND_CLASS_SENSOR_MULTILEVEL")) {
            data_list = APIListData.getSensorMultiLevelAPIList();
        } else if (title.equals("COMMAND_CLASS_ZWAVEPLUS_INFO")) {
            data_list.add("This CmdClass handled by zwave protocol directly");
        } else if (title.equals("COMMAND_CLASS_VERSION")) {
            data_list.add("This CmdClass handled by zwave protocol directly");
        } else if (title.equals("COMMAND_CLASS_MANUFACTURER_SPECIFIC")) {
            data_list.add("This CmdClass handled by zwave protocol directly");
        } else if (title.equals("COMMAND_CLASS_IP_ASSOCIATION")) {
            data_list.add("This CmdClass handled by zwave protocol directly");
        } else if (title.equals("COMMAND_CLASS_ZIP")) {
            data_list.add("This CmdClass handled by zwave protocol directly");
        } else if (title.equals("COMMAND_CLASS_ZIP_NAMING")) {
            data_list.add("This CmdClass handled by zwave protocol directly");
        } else if (title.equals("COMMAND_CLASS_PROPRIETARY")) {
            data_list.add("This CmdClass handled by zwave protocol directly");
        } else if (title.equals("COMMAND_CLASS_DOOR_LOCK_LOGGING")) {
            data_list.add("This CmdClass handled by zwave protocol directly");
        } else if (title.equals("COMMAND_CLASS_SCENE_ACTUATOR_CONF")) {
            data_list.add("This CmdClass handled by zwave protocol directly");
        }

        if (data_list != null && data_list.size() > 0) {
            //适配器
            ApiArrayAdapter arrAdapter = new ApiArrayAdapter(this, data_list);
            //加载适配器
            mySpinner.setAdapter(arrAdapter);
            mySpinner.setSelection(0, true);
            String currentApi = data_list.get(0).toString();
            setInterfaceName(currentApi);
            Logg.i(TAG, "============currentApi==" + currentApi);
            mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String apiName = data_list.get(position).toString();
                    Logg.i(TAG, "==a=========piName==" + apiName);
                    setInterfaceName(apiName);
                    result.setText("");
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

    }

    /**
     * 判断接口名称
     *
     * @param name
     */
    private void setInterfaceName(String name) {
        parameterLayout.removeAllViews();
        if (name.equals("zwcontrol_network_health_check")) { //Network Health Check功能
            api = "getRssiState";
        } else if (name.equals("zwcontrol_get_all_provision_list_entry")) { //Smart Start相关api 5个
            api = "getAllProvisionListEntry";
        } else if (name.equals("zwcontrol_add_provision_list_entry")) {
            api = "addProvisionListEntry";
        } else if (name.equals("zwcontrol_rm_provision_list_entry")) {
            api = "rmProvisionListEntry";
        } else if (name.equals("zwcontrol_get_provision_list_entry")) {
            api = "getProvisionListEntry";
        } else if (name.equals("zwcontrol_rm_all_provision_list_entry")) {
            api = "rmAllProvisionListEntry";
        } else if (name.equals("zwcontrol_init")) {//controller相关接口14个
            api = "";
        } else if (name.equals("zwcontrol_battery_get")) {//Command Class Battery
            api = "getBattery";
        } else if (name.equals("zwcontrol_sensor_multilevel_get")) {
            api = "getSensorMultilevel";
        } else if (name.equals("zwcontrol_basic_get")) {
            api = "getBasic";
        } else if (name.equals("zwcontrol_basic_set")) {
            api = "setBasic";
            addParamsLayout("value");
        } else if (name.equals("zwcontrol_start_stop_switchlevel_change")) {
            api = "startStopSwitchLevelChange";
            addParamsLayout("startLvlVal");
            addParamsLayout("duration");
            addParamsLayout("pmyChangeDir");
            addParamsLayout("secChangeDir");
            addParamsLayout("secStep");
        } else if (name.equals("zwcontrol_switch_all_set")) {
            api = "setSwitchAll";
            addParamsLayout("value");
        }else if (name.equals("zwcontrol_switch_all_get")) {
            api = "getSwitchAll";
        }else if (name.equals("zwcontrol_switch_all_on_broadcast")) {
            api = "setSwitchAllOnBroadcast";
        }else if (name.equals("zwcontrol_switch_all_off_broadcast")) {
            api = "setSwitchAllOffBroadcast";
        }else if (name.equals("zwcontrol_sensor_binary_get")) { //Command Class Sensor Binary v2
            api = "getSensorBinary";
            addParamsLayout("sensorType");
        }else if (name.equals("zwcontrol_sensor_binary_supported_sensor_get")) {
            api = "getSensorBinarySupportedSensor";
        }else if (name.equals("zwcontrol_meter_get")) { //Command Class Meter v3
            api = "getMeter";
            addParamsLayout("meterUnit");
        }else if (name.equals("zwcontrol_meter_supported_get")) { //Command Class Meter v3
            api = "getMeterSupported";
        }else if (name.equals("zwcontrol_meter_reset")) { //Command Class Meter v3
            api = "resetMeter";
        }else if (name.equals("zwcontrol_wake_up_interval_get")) { //Command Class Wake Up
            api = "getWakeUpInterval";
        }else if (name.equals("zwcontrol_wake_up_interval_set")) { //
            api = "setWakeUpInterval";
            addParamsLayout("wakeuptime");
        }else if (name.equals("zwcontrol_door_lock_operation_get")) { //Command Class Door Lock
            api = "getDoorLockOperation";
        }else if (name.equals("zwcontrol_door_lock_operation_set")) { //
            api = "setDoorLockOperation";
             addParamsLayout("mode");
        }else if (name.equals("zwcontrol_door_lock_config_get")) { //
            api = "getDoorLockConfig";
        } else if (name.equals("zwcontrol_door_lock_config_set")) { //
            api = "setDoorLockConfig";
             addParamsLayout("type");
             addParamsLayout("outSta");
             addParamsLayout("insta");
             addParamsLayout("tmoutMin");
             addParamsLayout("tmoutSec");
        } else if (name.equals("zwcontrol_get_specific_group")) {
            api = "getSpecificGroup";
            addParamsLayout("endpointId");
        } else if (name.equals("zwcontrol_notification_get")) {
            api = "getNotification";
            addParamsLayout("alarmType");
            addParamsLayout("notifType");
            addParamsLayout("evt");
        } else if (name.equals("zwcontrol_notification_supported_get")) {
            api = "getSupportedNotification";
        } else if (name.equals("zwcontrol_notification_supported_event_get")) {
            api = "getSupportedEventNotification";
            addParamsLayout("notifType");
        } else if (name.equals("zwcontrol_switch_multilevel_get")) {
            api = "getSwitchMultilevel";
        } else if (name.equals("zwcontrol_switch_multilevel_set")) {
            api = "setSwitchMultilevel";
            addParamsLayout("value");
            addParamsLayout("duration");
        } else if (name.equals("zwcontrol_get_support_switch_type")) {
            api = "getSupportSwitchType";
        }  else if (name.equals("zwcontrol_configuration_get")) {
            api = "getConfiguration";
            addParamsLayout("paramMode");
            addParamsLayout("paramNumber");
            addParamsLayout("rangeStart");
            addParamsLayout("rangeEnd");
        } else if (name.equals("zwcontrol_configuration_set")) {
            api = "setConfiguration";
            addParamsLayout("paramNumber");
            addParamsLayout("paramSize");
            addParamsLayout("useDefault");
            addParamsLayout("paramValue");
        } else if (name.equals("zwcontrol_powerLevel_get")) {
            api = "getPowerLevel";
        } else if (name.equals("zwcontrol_switch_all_on")) { //command class switch all //command class switch all Binaryver1~2
            api = "switchAllOn";
        } else if (name.equals("zwcontrol_switch_all_off")) {
            api = "switchAllOff";
        } else if (name.equals("zwcontrol_user_code_get")) { //Command Class User Code
            api = "";
        } else if (name.equals("zwcontrol_user_code_set")) {
            api = "";
        } else if (name.equals("zwcontrol_user_code_number_get")) {
            api = "";
        } else if (name.equals("zwcontrol_protection_get")) {//Command Class Protection v1-v3
            api = "";
        } else if (name.equals("zwcontrol_protection_set")) {
            api = "";
        } else if (name.equals("zwcontrol_supported_protection_get")) {
            api = "";
        } else if (name.equals("zwcontrol_protection_exclusive_control_node_get")) {
            api = "";
        } else if (name.equals("zwcontrol_protection_exclusive_control_node_set")) {
            api = "";
        } else if (name.equals("zwcontrol_protection_timeout_get")) {
            api = "";
        } else if (name.equals("zwcontrol_protection_timeout_set")) {
            api = "";
        } else if (name.equals("zwcontrol_indicator_get")) { //Command Class Indicator v1
            api = "";
        } else if (name.equals("zwcontrol_indicator_set")) {
            api = "";
        } else if (name.equals("zwcontrol_door_lock_logging_supported_records_get")) { //Command Class Door Lock Looging
            api = "";
        } else if (name.equals("zwcontrol_door_lock_logging_records_get")) {
            api = "";
        } else if (name.equals("zwcontrol_language_get")) {//Command Class Language
            api = "";
        } else if (name.equals("zwcontrol_language_set")) {
            api = "";
        } else if (name.equals("zwcontrol_switch_color_get")) {//Command Class Switch Color
            api = "getSwitchColor";
            addParamsLayout("compId");
        } else if (name.equals("zwcontrol_switch_color_supported_get")) {
            api = "getSupportedColor";
        } else if (name.equals("zwcontrol_switch_color_set")) {
            api = "setSwitchColor";
            addParamsLayout("colorId");
            addParamsLayout("colorValue");
        } else if (name.equals("zwcontrol_start_stop_color_levelchange")) {
            api = "startStopColorLevelChange";
            addParamsLayout("dir");
            addParamsLayout("ignore");
            addParamsLayout("colorId");
            addParamsLayout("startLevel");
        } else if (name.equals("zwcontrol_barrier_operator_set")) {//Command Class Barrier Operator
            api = "";
        } else if (name.equals("zwcontrol_barrier_operator_get")) {
            api = "";
        } else if (name.equals("zwcontrol_barrier_operator_signal_set")) {
            api = "";
        } else if (name.equals("zwcontrol_barrier_operator_signal_get")) {
            api = "";
        } else if (name.equals("zwcontrol_barrier_operator_signal_supported_get")) {
            api = "";
        } else if (name.equals("zwcontrol_basic_tariff_info_get")) {//Command Class Basic Tariff Info
            api = "";
        } else if (name.equals("zwcontrol_get_group_info")) {//Command Class Association & Multi-Channel Association
            api = "getGroupInfo";
            addParamsLayout("groupId");
            addParamsLayout("endpointId");
        } else if (name.equals("zwcontrol_add_endpoints_to_group")) {
            api = "addEndpointsToGroup";
            addParamsLayout("endpointId");
            addParamsLayout("groupId");
            addParamsLayout("controlNodeId");
        } else if (name.equals("zwcontrol_remove_endpoints_from_group")) {
            api = "removeEndpointsFromGroup";
            addParamsLayout("endpointId");
            addParamsLayout("groupId");
            addParamsLayout("controlNodeId");
        } else if (name.equals("zwcontrol_get_max_supported_groups")) {
            api = "getMaxSupportedGroups";
            addParamsLayout("endpointId");
        } else if (name.equals("zwcontrol_notification_set")) {//Command Class Notification version 4
            api = "setNotification";
            addParamsLayout("notificationType");
            addParamsLayout("status");
        } else if (name.equals("zwcontrol_central_scene_supported_get")) {//Command Class Central Scene version 2
            api = "getsupportedCentralScene";
            addParamsLayout("endpoindId");
        } else if (name.equals("hl_central_scene_notification_report_cb")) {
            api = "central";
        } else if (name.equals("zwcontrol_scene_actuator_conf_get")) {//Command Class Scene Actuator Conf ver 1
            api = "getSceneActuatorConf";
             addParamsLayout("sceneId");
        } else if (name.equals("zwcontrol_scene_actuator_conf_set")) {
            api = "setSceneActuatorConf";
            addParamsLayout("sceneId");
            addParamsLayout("dimDuration");
            addParamsLayout("override");
            addParamsLayout("level");
        } else if (name.equals("zwcontrol_firmwareupdate_info_get")) {//Command Class Firmware Update Md
            api = "";
        } else if (name.equals("zwcontrol_firmwareupdate_request")) {
            api = "";
        } else if (name.equals("zwcontrol_multi_cmd_encap")) {//Command Class Multi Cmd
            api = "";
        } else if (name.equals("zwcontrol_sensor_binary_get")){
            api = "getSensorBinary";
        } else if (name.equals("zwcontrol_switch_binary_set")){
            api = "setBinarySwitchState";
             addParamsLayout("state");
             addParamsLayout("duration");
        } else if (name.equals("zwcontrol_switch_binary_get")){
         api = "getBinarySwitchState";
        }
    }

    //getSupportSwitchType  getSensorBinarySupportedSensor  getSensorBinary  startStopSwitchLevelChange可以发出去
    private void requestMqtt() {
        if (api.equals("getBattery")
                || api.equals("getSensorMultilevel")
                || api.equals("getBasic")
                || api.equals("switchAllOn")
                || api.equals("switchAllOff")
                || api.equals("getSwitchMultilevel")
                || api.equals("getRssiState")
                || api.equals("getPowerLevel")
                || api.equals("getMeterSupported")
                || api.equals("getSupportSwitchType")
                || api.equals("getSensorBinarySupportedSensor")
                || api.equals("getWakeUpInterval")
                || api.equals("getBinarySwitchState")
                || api.equals("getSupportedColor")
                || api.equals("getSwitchAll")
                || api.equals("resetMeter")
                || api.equals("getDoorLockOperation")
                || api.equals("getDoorLockConfig")
                || api.equals("getSupportedNotification")) {

            Log.i(TAG, "requestMqtt api: " + api);
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setMqttDataJson(api, deviceId));//只传两个参数的接口

        } else if(api.equals("setBasic")){
            String value = ((EditText)((LinearLayout)parameterLayout.getChildAt(0)).getChildAt(1)).getText().toString();
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setMqttDataJson(api, deviceId, "value",value));
        } else if(api.equals("setSwitchMultilevel")){
            String value = ((EditText)((LinearLayout)parameterLayout.getChildAt(0)).getChildAt(1)).getText().toString();
            String duration = ((EditText)((LinearLayout)parameterLayout.getChildAt(1)).getChildAt(1)).getText().toString();
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setMqttDataJson(api, deviceId,
                    "value", value,
                    "duration", duration));
        } else if(api.equals("getProvisionListEntry")){
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getProvisionListEntry(api,deviceId));
        } else if(api.equals("addProvisionListEntry")){
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.addProvisionList(deviceId,"47","","1"));
        } else if(api.equals("rmProvisionListEntry")){
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.rmProvisionListEntry(api,deviceId));
        } else if (api.equals("getAllProvisionListEntry") || api.equals("rmAllProvisionListEntry")) {
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setMqttDataJson(api));
        } else if (api.equals("startStopSwitchLevelChange")) {//7个参数
            String startLvlVal = ((EditText)((LinearLayout)parameterLayout.getChildAt(0)).getChildAt(1)).getText().toString();
            String duration = ((EditText)((LinearLayout)parameterLayout.getChildAt(1)).getChildAt(1)).getText().toString();
            String pmyChangeDir = ((EditText)((LinearLayout)parameterLayout.getChildAt(2)).getChildAt(1)).getText().toString();
            String secChangeDir = ((EditText)((LinearLayout)parameterLayout.getChildAt(3)).getChildAt(1)).getText().toString();
            String secStep = ((EditText)((LinearLayout)parameterLayout.getChildAt(4)).getChildAt(1)).getText().toString();
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setMqttDataJson(api,deviceId,
                    "startLvlVal", startLvlVal,
                    "duration", duration,
                    "pmyChangeDir", pmyChangeDir,
                    "secChangeDir",secChangeDir,
                    "secStep",secStep));
        } else if (api.equals("getSensorBinary")) {//3个参数
            String sensorType = ((EditText)((LinearLayout)parameterLayout.getChildAt(0)).getChildAt(1)).getText().toString();
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setMqttDataJson(api,deviceId,
                    "sensorType",sensorType));
        } else if (api.equals("getSpecificGroup")) {
            String endpointId = ((EditText)((LinearLayout)parameterLayout.getChildAt(0)).getChildAt(1)).getText().toString();
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setMqttDataJson(api,deviceId,
                    "endpointId",endpointId));
        } else if (api.equals("getNotification")) {
            String alarmType = ((EditText)((LinearLayout)parameterLayout.getChildAt(0)).getChildAt(1)).getText().toString();
            String notifType = ((EditText)((LinearLayout)parameterLayout.getChildAt(1)).getChildAt(1)).getText().toString();
            String status = ((EditText)((LinearLayout)parameterLayout.getChildAt(2)).getChildAt(1)).getText().toString();
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setMqttDataJson(api,deviceId,
                    "alarmType",alarmType,
                    "notifType",notifType,
                    "status",status));
        } else if (api.equals("getSupportedEventNotification")) {
            String notifType = ((EditText)((LinearLayout)parameterLayout.getChildAt(0)).getChildAt(1)).getText().toString();
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setMqttDataJson(api,deviceId,
                    "notifType",notifType));
        } else if (api.equals("getSwitchColor")) {
            String compId = ((EditText)((LinearLayout)parameterLayout.getChildAt(0)).getChildAt(1)).getText().toString();
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setMqttDataJson(api, deviceId,
                    "compId", compId));
        } else if (api.equals("setSwitchColor")) {
            String colorId = ((EditText)((LinearLayout)parameterLayout.getChildAt(0)).getChildAt(1)).getText().toString();
            String colorValue = ((EditText)((LinearLayout)parameterLayout.getChildAt(1)).getChildAt(1)).getText().toString();
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setMqttDataJson(api, deviceId,
                    "colorId", colorId,
                    "colorValue", colorValue));
        } else if (api.equals("getMeter")) {
            String meterUnit = ((EditText)((LinearLayout)parameterLayout.getChildAt(0)).getChildAt(1)).getText().toString();
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setMqttDataJson(api, deviceId,
                    "meterUnit", meterUnit));
        } else if (api.equals("getMaxSupportedGroups")) {//非新增接口
            String endpointId = ((EditText)((LinearLayout)parameterLayout.getChildAt(0)).getChildAt(1)).getText().toString();
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getMaxSupperedGroups(deviceId, endpointId));
        } else if (api.equals("getGroupInfo")) {//非新增接口
            String groupId = ((EditText)((LinearLayout)parameterLayout.getChildAt(0)).getChildAt(1)).getText().toString();
            String endpointId = ((EditText)((LinearLayout)parameterLayout.getChildAt(1)).getChildAt(1)).getText().toString();
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.getGroupInfo(deviceId, groupId, endpointId));
        } else if (api.equals("addEndpointsToGroup")) {//非新增接口
            String endpointId = ((EditText)((LinearLayout)parameterLayout.getChildAt(0)).getChildAt(1)).getText().toString();
            String groupId = ((EditText)((LinearLayout)parameterLayout.getChildAt(1)).getChildAt(1)).getText().toString();
            String controlNodeId = ((EditText)((LinearLayout)parameterLayout.getChildAt(2)).getChildAt(1)).getText().toString();

            ArrayList nodeInterFaceList = new ArrayList();
//            Map<String, Object> device = new HashMap<>();
//            device.put("controlNodeId", controlNodeId);
            //device.put("controlNodeId", "5");//可以添加多条信息
            nodeInterFaceList.add(controlNodeId);
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.addEndpointsToGroup(deviceId, endpointId, groupId, nodeInterFaceList));

        } else if (api.equals("removeEndpointsFromGroup")) {//非新增接口
            String endpointId = ((EditText)((LinearLayout)parameterLayout.getChildAt(0)).getChildAt(1)).getText().toString();
            String groupId = ((EditText)((LinearLayout)parameterLayout.getChildAt(1)).getChildAt(1)).getText().toString();
            String controlNodeId = ((EditText)((LinearLayout)parameterLayout.getChildAt(2)).getChildAt(1)).getText().toString();
            ArrayList nodeInterFaceList = new ArrayList();
//            Map<String, Object> device = new HashMap<>();
//            device.put("controlNodeId", controlNodeId);
            //device.put("controlNodeId", "6");
            nodeInterFaceList.add(controlNodeId);
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.removeEndpointsFromGroup(deviceId, endpointId, groupId, nodeInterFaceList));

        } else if (api.equals("getConfiguration")) {
            String paramMode = ((EditText)((LinearLayout)parameterLayout.getChildAt(0)).getChildAt(1)).getText().toString();
            String paramNumber = ((EditText)((LinearLayout)parameterLayout.getChildAt(1)).getChildAt(1)).getText().toString();
            String rangeStart = ((EditText)((LinearLayout)parameterLayout.getChildAt(2)).getChildAt(1)).getText().toString();
            String rangeEnd = ((EditText)((LinearLayout)parameterLayout.getChildAt(3)).getChildAt(1)).getText().toString();
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setMqttDataJson(api, deviceId,
                    "paramMode", paramMode,
                    "paramNumber", paramNumber,
                    "rangeStart", rangeStart,
                    "rangeEnd", rangeEnd));
        } else if (api.equals("setConfiguration")) {
            String paramNumber = ((EditText)((LinearLayout)parameterLayout.getChildAt(0)).getChildAt(1)).getText().toString();
            String paramSize = ((EditText)((LinearLayout)parameterLayout.getChildAt(1)).getChildAt(1)).getText().toString();
            String useDefault = ((EditText)((LinearLayout)parameterLayout.getChildAt(2)).getChildAt(1)).getText().toString();
            String paramValue = ((EditText)((LinearLayout)parameterLayout.getChildAt(3)).getChildAt(1)).getText().toString();
            MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setMqttDataJson(api, deviceId,
                    "paramNumber", paramNumber,
                    "paramSize", paramSize,
                    "useDefault", useDefault,
                    "paramValue", paramValue));
        } else if (api.equals("setWakeUpInterval")){
             String wakeuptime = ((EditText)((LinearLayout)parameterLayout.getChildAt(0)).getChildAt(1)).getText().toString();
             MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setMqttDataJson(api, deviceId,
                    "wakeuptime", wakeuptime));
        } else if (api.equals("setBinarySwitchState")){
                String state = ((EditText)((LinearLayout)parameterLayout.getChildAt(0)).getChildAt(1)).getText().toString();
                String duration = ((EditText)((LinearLayout)parameterLayout.getChildAt(1)).getChildAt(1)).getText().toString();
             MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setMqttDataJson(api, deviceId,
                    "state", state,
                    "duration",duration));
        } else if (api.equals("startStopColorLevelChange")){
                String dir = ((EditText)((LinearLayout)parameterLayout.getChildAt(0)).getChildAt(1)).getText().toString();
                String ignore = ((EditText)((LinearLayout)parameterLayout.getChildAt(1)).getChildAt(1)).getText().toString();
                String colorId = ((EditText)((LinearLayout)parameterLayout.getChildAt(2)).getChildAt(1)).getText().toString();
                String startLevel = ((EditText)((LinearLayout)parameterLayout.getChildAt(3)).getChildAt(1)).getText().toString();
             MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setMqttDataJson(api, deviceId,
                    "dir", dir,
                    "ignore", ignore,
                    "colorId", colorId,
                    "startLevel",startLevel));
        } else if (api.equals("setNotification")){
               String notificationType = ((EditText)((LinearLayout)parameterLayout.getChildAt(0)).getChildAt(1)).getText().toString();
                String status = ((EditText)((LinearLayout)parameterLayout.getChildAt(1)).getChildAt(1)).getText().toString();
             MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setMqttDataJson(api, deviceId,
                    "notificationType", notificationType,
                    "status",status));
        } else if (api.equals("setSwitchAll")){
             String value = ((EditText)((LinearLayout)parameterLayout.getChildAt(0)).getChildAt(1)).getText().toString();
             MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setMqttDataJson(api, deviceId,"value",value));

        } else if (api.equals("setSwitchAllOnBroadcast")
        || api.equals("setSwitchAllOffBroadcast")){
        MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setMqttDataJson(api));//只传两个参数的接口
        } else if (api.equals("setDoorLockOperation")){
         String mode = ((EditText)((LinearLayout)parameterLayout.getChildAt(0)).getChildAt(1)).getText().toString();
             MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setMqttDataJson(api, deviceId,"mode",mode));
        } else if (api.equals("setDoorLockConfig")){
            String type = ((EditText)((LinearLayout)parameterLayout.getChildAt(0)).getChildAt(1)).getText().toString();
                String outSta = ((EditText)((LinearLayout)parameterLayout.getChildAt(1)).getChildAt(1)).getText().toString();
                String insta = ((EditText)((LinearLayout)parameterLayout.getChildAt(2)).getChildAt(1)).getText().toString();
                String tmoutMin = ((EditText)((LinearLayout)parameterLayout.getChildAt(3)).getChildAt(1)).getText().toString();
                String tmoutSec = ((EditText)((LinearLayout)parameterLayout.getChildAt(4)).getChildAt(1)).getText().toString();
             MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setMqttDataJson(api, deviceId,
                    "type", type,
                    "outSta", outSta,
                    "insta", insta,
                    "tmoutMin", tmoutMin,
                    "tmoutSec",tmoutSec));
        } else if (api.equals("getsupportedCentralScene")){
        String endpoindId = ((EditText)((LinearLayout)parameterLayout.getChildAt(0)).getChildAt(1)).getText().toString();
             MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setMqttDataJson(api, deviceId,"endpoindId",endpoindId));
        }else if (api.equals("getSceneActuatorConf")){
        String sceneId = ((EditText)((LinearLayout)parameterLayout.getChildAt(0)).getChildAt(1)).getText().toString();
             MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setMqttDataJson(api, deviceId,"sceneId",sceneId));
        } else if (api.equals("setSceneActuatorConf")){
                String sceneId = ((EditText)((LinearLayout)parameterLayout.getChildAt(0)).getChildAt(1)).getText().toString();
                String dimDuration = ((EditText)((LinearLayout)parameterLayout.getChildAt(1)).getChildAt(1)).getText().toString();
                String override = ((EditText)((LinearLayout)parameterLayout.getChildAt(2)).getChildAt(1)).getText().toString();
                String level = ((EditText)((LinearLayout)parameterLayout.getChildAt(3)).getChildAt(1)).getText().toString();
             MQTTManagement.getSingInstance().publishMessage(Const.subscriptionTopic, LocalMqttData.setMqttDataJson(api, deviceId,
                    "sceneId", sceneId,
                    "dimDuration", dimDuration,
                    "override", override,
                    "level",level));
        }
    }

    MqttMessageArrived mMqttMessageArrived = new MqttMessageArrived() {
        @Override
        public void mqttMessageArrived(String topic, MqttMessage message) {
            final String result = new String(message.getPayload());
            Logg.i(TAG, "=mqttMessageArrived=>=topic=" + topic);
            Logg.i(TAG, "=mqttMessageArrived=>=message=" + result);

            if (result.contains("desired")) {
                return;
            }
            mqttMessageResult(result);
            Logg.i(TAG, "=mqttMessageArrived=>=message=");

        }
    };

    private void mqttMessageResult(final String mqttResult) {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonObject = new JSONObject(mqttResult);
                    String reported = jsonObject.optString("reported");
                    JSONObject reportedObject = new JSONObject(reported);
                    String messageType = reportedObject.optString("MessageType");
                    //String result = reportedObject.optString("Result");
                    Log.i(TAG, "~~~~~~~~~mqttMessageResult" + reported);
                     if (api.equals("getRssiState")
                || api.equals("getWakeUpInterval") ) {

                            result.setText(mqttResult);
        } else if(api.equals("getSensorBinarySupportedSensor") && "Binary Sensor Support Get Information".equals(messageType)){
                          result.setText(mqttResult);
        } else if(api.equals("getSupportSwitchType") && "Multi Level Switch Type Information".equals(messageType)){
                          result.setText(mqttResult);
         } else if(api.equals("getBinarySwitchState") && "Binary Switch Get Information".equals(messageType)){
                          result.setText(mqttResult);
        } else if(api.equals("switchAllOn") && mqttResult.contains("switchAllOn")){
                          result.setText(mqttResult);
          } else if(api.equals("resetMeter") && mqttResult.contains("resetMeter")){
                          result.setText(mqttResult);
         } else if(api.equals("switchAllOff") && mqttResult.contains("switchAllOff")){
                          result.setText(mqttResult);
        } else if(api.equals("getBasic") && mqttResult.contains("getSwitchStatus")){
                          result.setText(mqttResult);
        } else if(api.equals("setBasic") && mqttResult.contains("setSwitchStatus")){
                          result.setText(mqttResult);
        } else if(api.equals("getSwitchMultilevel") && "Switch Multi-lvl Report Information".equals(messageType)){
                          result.setText(mqttResult);
        } else if(api.equals("setSwitchMultilevel") && ("Switch Multi-lvl Report Information".equals(messageType)
                    || mqttResult.contains("setBrigtness"))){
                          result.setText(mqttResult);
        } else if(api.equals("getProvisionListEntry")){
                          result.setText(mqttResult);
        } else if(api.equals("addProvisionListEntry")){
                          result.setText(mqttResult);
        } else if(api.equals("rmProvisionListEntry")){
                          result.setText(mqttResult);
        } else if (api.equals("getAllProvisionListEntry") || api.equals("rmAllProvisionListEntry")) {
                          result.setText(mqttResult);
        } else if (api.equals("startStopSwitchLevelChange") && mqttResult.contains("startStopSwitchLevelChange")) {//7个参数
                            result.setText(mqttResult);
        } else if (api.equals("getSensorBinary") && "Binary Sensor Information".equals(messageType)) {//3个参数
                         result.setText(mqttResult);
        } else if (api.equals("getSpecificGroup") && "Active Groups Report".equals(messageType)){
                          result.setText(mqttResult);
        } else if (api.equals("getNotification")  && "Notification Get Information".equals(messageType)) {
                          result.setText(mqttResult);
        } else if (api.equals("getSupportedNotification")  && "Notification Supported Report".equals(messageType)) {
                          result.setText(mqttResult);
        } else if (api.equals("getSupportedEventNotification") && "Supported Notification Event Report".equals(messageType)) {
                          result.setText(mqttResult);
        } else if (api.equals("getSwitchColor") && ("Switch Color Report".equals(messageType))) {
                          result.setText(mqttResult);
         } else if (api.equals("getSupportedColor") && ("Supported Color Report".equals(messageType))) {
                          result.setText(mqttResult);
        } else if (api.equals("setSwitchColor") && ("Switch Color Report".equals(messageType))) {
                         result.setText(mqttResult);
        } else if (api.equals("getMeter") && ("Meter Report Information".equals(messageType))) {
                          result.setText(mqttResult);
        } else if (api.equals("getMaxSupportedGroups") && ("Supported Groupings Report".equals(messageType))) {//非新增接口
                         result.setText(mqttResult);
        } else if (api.equals("getGroupInfo") && ("Group Info Report".equals(messageType))) {//非新增接口
                        result.setText(mqttResult);
        } else if (api.equals("addEndpointsToGroup") && mqttResult.contains("addEndpointsToGroup")) {//非新增接口
                        result.setText(mqttResult);
        } else if (api.equals("removeEndpointsFromGroup") && mqttResult.contains("removeEndpointsFromGroup")) {//非新增接口
                         result.setText(mqttResult);
        } else if (api.equals("getConfiguration") && "Configuration Get Information".equals(messageType)) {
                          result.setText(mqttResult);
        } else if (api.equals("setConfiguration") && mqttResult.contains("setConfiguration")) {
                          result.setText(mqttResult);
        } else if (api.equals("setWakeUpInterval")){
                          result.setText(mqttResult);
        } else if (api.equals("setBinarySwitchState") && mqttResult.contains("setBinarySwitchState")){
                          result.setText(mqttResult);
        } else if (api.equals("startStopColorLevelChange") && ("Switch Color Report".equals(messageType))){
                          result.setText(mqttResult);
        } else if (api.equals("setNotification") && mqttResult.contains("setNotification")){
                          result.setText(mqttResult);
        } else if (api.equals("setSwitchAll") && mqttResult.contains("setSwitchAll")){
                          result.setText(mqttResult);
        } else if (api.equals("setSwitchAllOnBroadcast") && mqttResult.contains("setSwitchAllOnBroadcast")) {
                          result.setText(mqttResult);
        } else if (api.equals("setSwitchAllOffBroadcast") && mqttResult.contains("setSwitchAllOffBroadcast")){
                          result.setText(mqttResult);
        } else if (api.equals("setDoorLockOperation") && mqttResult.contains("setDoorLockOperation")){
                          result.setText(mqttResult);
        } else if (api.equals("setDoorLockConfig") && mqttResult.contains("setDoorLockConfig")){
                         result.setText(mqttResult);
        } else if (api.equals("getsupportedCentralScene") && ("Central Scene Supported Report".equals(messageType))){
                   result.setText(mqttResult);
        }else if (api.equals("getSceneActuatorConf")){
                      result.setText(mqttResult);
        } else if (api.equals("setSceneActuatorConf")){
                 result.setText(mqttResult);
         } else if (api.equals("getBattery") && ("Node Battery Value".equals(messageType))){
                 result.setText(mqttResult);
         } else if (api.equals("getPowerLevel") && ("Power Level Get Information".equals(messageType))){
                 result.setText(mqttResult);
         } else if (api.equals("getSwitchAll") && ("Switch All Get Information".equals(messageType))){
                 result.setText(mqttResult);
          } else if (api.equals("getMeterSupported") && ("Meter Cap Information".equals(messageType))){
                 result.setText(mqttResult);
         } else if (api.equals("getSensorBinary") && ("Binary Sensor Information".equals(messageType))){
                 result.setText(mqttResult);
         } else if (api.equals("getDoorLockOperation") && ("Door Lock Operation Report".equals(messageType))){
                 result.setText(mqttResult);
          } else if (api.equals("getDoorLockConfig") && ("Door Lock Configuration Report".equals(messageType))){
                 result.setText(mqttResult);
          } else if (api.equals("getSensorMultilevel") && ("Sensor Info Report".equals(messageType))){
                 result.setText(mqttResult);
        } else{
                       //  result.setText(".....");

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void unrigister() {
        if (mMqttMessageArrived != null) {
            MQTTManagement.getSingInstance().unrigister(mMqttMessageArrived);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unrigister();
    }
}
