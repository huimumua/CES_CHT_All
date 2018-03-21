package com.askey.mobile.zwave.control.data;

import com.askey.mobile.zwave.control.deviceContr.localMqtt.MQTTManagement;
import com.askey.mobile.zwave.control.deviceContr.model.DeviceInfo;
import com.askey.mobile.zwave.control.util.Const;
import com.askey.mobile.zwave.control.util.Logg;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 项目名称：ZwaveControlClient-S2
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2018/3/21 16:16
 * 修改人：skysoft
 * 修改时间：2018/3/21 16:16
 * 修改备注：
 */
public class APIListData {

    public static String LOG_TAG = "APIListData";

    public static ArrayList getSmartStartAPIList() {
        ArrayList<String> data_list = new ArrayList<String>();
        data_list.add("zwcontrol_add_provision_list_entry");
        data_list.add("zwcontrol_rm_provision_list_entry");
        data_list.add("zwcontrol_get_provision_list_entry");
        data_list.add("zwcontrol_get_all_provision_list_entry");
        data_list.add("zwcontrol_rm_all_provision_list_entry");
        return data_list;
    }


    public static ArrayList<String> getCommandQueueAPIList() {
        ArrayList<String> data_list = new ArrayList<String>();
        data_list.add("zwcontrol_command_queue_state_get");
        data_list.add("zwcontrol_command_queue_turn_on_off");
        data_list.add("zwcontrol_command_queue_view");
        data_list.add("zwcontrol_command_queue_cancel");
        return data_list;
    }

    public static ArrayList<String> getNetworkHealthCheckAPIList() {
        ArrayList<String> data_list = new ArrayList<String>();
        data_list.add("zwcontrol_network_health_check");
        return data_list;
    }

    public static ArrayList<String> getControllerAPIList() {
        ArrayList<String> data_list = new ArrayList<String>();
        data_list.add("zwcontrol_init");
        data_list.add("zwcontrol_setcallback");
        data_list.add("zwcontrol_exit");
        data_list.add("zwcontrol_add_node");
        data_list.add("zwcontrol_rm_node");
        data_list.add("zwcontrol_get_node_list");
        data_list.add("zwcontrol_get_node_info");
        data_list.add("zwcontrol_rm_failed_node");
        data_list.add("zwcontrol_rp_failed_node");
        data_list.add("zwcontrol_stop_op");
        data_list.add("zwcontrol_default_set");
        data_list.add("zwcontrol_update_node");
        data_list.add("zwcontrol_save_nodeinfo");
        data_list.add("zwcontrol_start_learn_mode");
        return data_list;
    }

    public static ArrayList<String> getBatteryAPIList() {
        ArrayList<String> data_list = new ArrayList<String>();
        data_list.add("zwcontrol_battery_get");
        data_list.add("zwcontrol_sensor_multilevel_get");
        return data_list;
    }

    public static ArrayList<String> getBasicVerAPIList() {
        ArrayList<String> data_list = new ArrayList<String>();
        data_list.add("zwcontrol_basic_get");
        data_list.add("zwcontrol_basic_set");
        return data_list;
    }

    public static ArrayList<String> getSwitchMultiLevelAPIList() {
        ArrayList<String> data_list = new ArrayList<String>();
        data_list.add("zwcontrol_switch_multilevel_get");
        data_list.add("zwcontrol_switch_multilevel_set");
        data_list.add("zwcontrol_get_support_switch_type");
        data_list.add("zwcontrol_start_stop_switchlevel_change");
        return data_list;
    }

    public static ArrayList<String> getConfigurationAPIList() {
        ArrayList<String> data_list = new ArrayList<String>();
        data_list.add("zwcontrol_configuration_get");
        data_list.add("zwcontrol_configuration_set");
        return data_list;
    }

    public static ArrayList<String> getPowerLevelAPIList() {
        ArrayList<String> data_list = new ArrayList<String>();
        data_list.add("zwcontrol_powerLevel_get");
        return data_list;
    }

    public static ArrayList<String> getSwitchAllAPIList() {
        ArrayList<String> data_list = new ArrayList<String>();
        data_list.add("zwcontrol_switch_all_on");
        data_list.add("zwcontrol_switch_all_off");
        data_list.add("zwcontrol_switch_all_set");
        data_list.add("zwcontrol_switch_all_get");
        data_list.add("zwcontrol_switch_all_on_broadcast");
        data_list.add("zwcontrol_switch_all_off_broadcast");
        return data_list;
    }

    public static ArrayList<String> getSensorBinaryV2APIList() {
        ArrayList<String> data_list = new ArrayList<String>();
        data_list.add("zwcontrol_sensor_binary_get");
        data_list.add("zwcontrol_sensor_binary_supported_sensor_get");
        return data_list;
    }

    public static ArrayList<String> getMeterV3APIList() {
        ArrayList<String> data_list = new ArrayList<String>();
        data_list.add("zwcontrol_meter_get");
        data_list.add("zwcontrol_meter_supported_get");
        data_list.add("zwcontrol_meter_reset");
        return data_list;
    }

    public static ArrayList<String> getWakeUpAPIList() {
        ArrayList<String> data_list = new ArrayList<String>();
        data_list.add("zwcontrol_wake_up_interval_get");
        data_list.add("zwcontrol_wake_up_interval_set");
        return data_list;
    }

    public static ArrayList<String> getDoorLockAPIList() {
        ArrayList<String> data_list = new ArrayList<String>();
        data_list.add("zwcontrol_door_lock_operation_get");
        data_list.add("zwcontrol_door_lock_operation_set");
        data_list.add("zwcontrol_door_lock_config_get");
        data_list.add("zwcontrol_door_lock_config_set");
        return data_list;
    }

    public static ArrayList<String> getUserCodeAPIList() {
        ArrayList<String> data_list = new ArrayList<String>();
        data_list.add("zwcontrol_user_code_get");
        data_list.add("zwcontrol_user_code_set");
        data_list.add("zwcontrol_user_code_number_get");
        return data_list;
    }

    public static ArrayList<String> getProtectionAPIList() {
        ArrayList<String> data_list = new ArrayList<String>();
        data_list.add("zwcontrol_protection_get");
        data_list.add("zwcontrol_protection_set");
        data_list.add("zwcontrol_supported_protection_get");
        data_list.add("zwcontrol_protection_exclusive_control_node_get");
        data_list.add("zwcontrol_protection_exclusive_control_node_set");
        data_list.add("zwcontrol_protection_timeout_get");
        data_list.add("zwcontrol_protection_timeout_set");
        return data_list;
    }

    public static ArrayList<String> getIndicatorAPIList() {
        ArrayList<String> data_list = new ArrayList<String>();
        data_list.add("zwcontrol_indicator_get");
        data_list.add("zwcontrol_indicator_set");
        return data_list;
    }

    public static ArrayList<String> getDoorLockLoogingAPIList() {
        ArrayList<String> data_list = new ArrayList<String>();
        data_list.add("zwcontrol_door_lock_logging_supported_records_get");
        data_list.add("zwcontrol_door_lock_logging_records_get");
        return data_list;
    }

    public static ArrayList<String> getLanguageAPIList() {
        ArrayList<String> data_list = new ArrayList<String>();
        data_list.add("zwcontrol_language_get");
        data_list.add("zwcontrol_language_set");
        return data_list;
    }

    public static ArrayList<String> getSwitchColorAPIList() {
        ArrayList<String> data_list = new ArrayList<String>();
        data_list.add("zwcontrol_switch_color_get");
        data_list.add("zwcontrol_switch_color_supported_get");
        data_list.add("zwcontrol_switch_color_set");
        data_list.add("zwcontrol_start_stop_color_levelchange");
        return data_list;
    }

    public static ArrayList<String> getBarrierOperatorAPIList() {
        ArrayList<String> data_list = new ArrayList<String>();
        data_list.add("zwcontrol_barrier_operator_set");
        data_list.add("zwcontrol_barrier_operator_get");
        data_list.add("zwcontrol_barrier_operator_signal_set");
        data_list.add("zwcontrol_barrier_operator_signal_get");
        data_list.add("zwcontrol_barrier_operator_signal_supported_get");
        return data_list;
    }

    public static ArrayList<String> getBasicTariffInfoAPIList() {
        ArrayList<String> data_list = new ArrayList<String>();
        data_list.add("zwcontrol_basic_tariff_info_get");
        return data_list;
    }

    public static ArrayList<String> getAssociationAPIList() {
        ArrayList<String> data_list = new ArrayList<String>();
        data_list.add("zwcontrol_get_group_info");
        data_list.add("zwcontrol_add_endpoints_to_group");
        data_list.add("zwcontrol_remove_endpoints_from_group");
        data_list.add("zwcontrol_get_max_supported_groups");
        data_list.add("zwcontrol_get_specific_group");
        return data_list;
    }

    public static ArrayList<String> getNotificationAPIList() {
        ArrayList<String> data_list = new ArrayList<String>();
        data_list.add("zwcontrol_notification_set");
        data_list.add("zwcontrol_notification_get");
        data_list.add("zwcontrol_notification_supported_get");
        data_list.add("zwcontrol_notification_supported_event_get");
        return data_list;
    }

    public static ArrayList<String> getCentralSceneVersionAPIList() {
        ArrayList<String> data_list = new ArrayList<String>();
        data_list.add("zwcontrol_central_scene_supported_get");
        data_list.add("hl_central_scene_notification_report_cb");
        return data_list;
    }

    public static ArrayList<String> getSceneActuatorConfAPIList() {
        ArrayList<String> data_list = new ArrayList<String>();
        data_list.add("zwcontrol_scene_actuator_conf_get");
        data_list.add("zwcontrol_scene_actuator_conf_set");
        return data_list;
    }

    public static ArrayList<String> getFirmwareUpdateMdAPIList() {
        ArrayList<String> data_list = new ArrayList<String>();
        data_list.add("zwcontrol_firmwareupdate_info_get");
        data_list.add("zwcontrol_firmwareupdate_request");
        return data_list;
    }

    public static ArrayList<String> getMultiCmdAPIList() {
        ArrayList<String> data_list = new ArrayList<String>();
        data_list.add("zwcontrol_multi_cmd_encap");
        return data_list;
    }
}
