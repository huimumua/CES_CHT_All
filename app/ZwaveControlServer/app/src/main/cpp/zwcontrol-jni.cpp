#include <string.h>
#include <stdint.h>
#include <jni.h>
#include <android/log.h>

extern "C"
{
    #include "zwcontrol/zwcontrol_api.h"
}

#define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))

static hl_appl_ctx_t appl_ctx = {0};

static jclass  ZwControlServiceClass;
static JavaVM  *ZwControlServiceVM = NULL;

static jmethodID CallBackMethodID = NULL;

static void check_and_clear_exceptions(JNIEnv* env, const char* method_name)
{
    if (!env->ExceptionCheck())
    {
        return;
    }

    ALOGE("An exception was thrown by '%s'.", method_name);
    env->ExceptionClear();
}

static JNIEnv* getJNIEnv(int* needsDetach)
{
    *needsDetach = 0;

    JNIEnv* env;

    if(ZwControlServiceVM == NULL)
    {
        return NULL;
    }

    if (ZwControlServiceVM->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK)
    {
        env =  NULL;
    }

    if (env == NULL)
    {
        JavaVMAttachArgs args = {JNI_VERSION_1_4, NULL, NULL};

        int result = ZwControlServiceVM->AttachCurrentThread(&env, (void*) &args);

        if (result != JNI_OK)
        {
            ALOGE("thread attach failed: %#x", result);
            return NULL;
        }

        *needsDetach = 1;
    }

    return env;
}

static void detachJNI()
{
    if(ZwControlServiceVM == NULL)
    {
        return;
    }

    int result = ZwControlServiceVM->DetachCurrentThread();

    if (result != JNI_OK)
    {
        ALOGE("thread detach failed: %#x", result);
    }
}

static int ZwControlResCallBack(const char* res)
{
    if(ZwControlServiceClass == NULL)
        return -1;

    int needDetach;

    JNIEnv* env = getJNIEnv(&needDetach);

    if(env == NULL)
    {
        return -1;
    }

    if(CallBackMethodID == NULL)
    {
        CallBackMethodID = env->GetStaticMethodID(ZwControlServiceClass, "ZwaveControlRes_CallBack", "([BI)V");

        if(CallBackMethodID == NULL)
        {
            if(needDetach)
            {
                detachJNI();
            }

            return -1;
        }
    }

    int len = strlen(res);

    jbyteArray bytes = env->NewByteArray(len);
    env->SetByteArrayRegion(bytes, 0, len, (jbyte*)res);
    env->CallStaticVoidMethod(ZwControlServiceClass, CallBackMethodID, bytes, len);
    check_and_clear_exceptions(env, __FUNCTION__);
    env->DeleteLocalRef(bytes);

    if(needDetach)
    {
        detachJNI();
    }

    return 0;
}

static int create_controller(JNIEnv *env, jclass object)
{
    if(ZwControlServiceClass == NULL)
    {
        ZwControlServiceClass = (jclass)env->NewGlobalRef(object);
    }

    return 0;
}

static jint open_controller(JNIEnv *env, jclass object, jstring cfgFilePath, jstring Path, jstring InfoPath, jbyteArray result)
{
    if(env == NULL)
    {
        return -1;
    }

    const char *cfgFile  = env->GetStringUTFChars(cfgFilePath, 0);
    const char *filePath = env->GetStringUTFChars(Path, 0);
    const char *infoFile = env->GetStringUTFChars(InfoPath, 0);

    uint8_t str[1024] = {0};

    if(zwcontrol_init(&appl_ctx, cfgFile, filePath, infoFile, str) != 0)
    {
        env->ReleaseStringUTFChars(cfgFilePath, cfgFile);
        env->ReleaseStringUTFChars(Path, filePath);
        env->ReleaseStringUTFChars(InfoPath, infoFile);
        return -1;
    }

    env->ReleaseStringUTFChars(cfgFilePath, cfgFile);
    env->ReleaseStringUTFChars(Path, filePath);
    env->ReleaseStringUTFChars(InfoPath, infoFile);

    zwcontrol_setcallback(ZwControlResCallBack);

    int len = (int)strlen((char*)str);

    env->SetByteArrayRegion(result, 0, len, (jbyte*)str);

    return 0;
}

static int close_controller(JNIEnv *env, jclass object)
{
    zwcontrol_exit(&appl_ctx);
    return 0;
}

static int destroy_controller(JNIEnv *env, jclass object)
{
    zwcontrol_exit(&appl_ctx);

    if(ZwControlServiceClass != NULL)
    {
        env->DeleteGlobalRef(ZwControlServiceClass);
    }

    return 0;
}

static int controller_adddevice(JNIEnv *env, jclass object)
{
    return zwcontrol_add_node(&appl_ctx);
}

static int controller_removedevice(JNIEnv *env, jclass object)
{
    return zwcontrol_rm_node(&appl_ctx);
}

static int controller_getDeviceList(JNIEnv *env, jclass object)
{
    return zwcontrol_get_node_list(&appl_ctx);
}

static int controller_removeFailedDevice(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_rm_failed_node(&appl_ctx,(uint32_t)nodeId);
}

static int controller_replaceFailedDevice(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_rp_failed_node(&appl_ctx, (uint32_t)nodeId);
}

static int controller_setDefault(JNIEnv *env, jclass object)
{
    return zwcontrol_default_set(&appl_ctx);
}

static int controller_stopAddDevice(JNIEnv *env, jclass object)
{
    return zwcontrol_stop_op(&appl_ctx);
}

static int controller_stopRemoveDevice(JNIEnv *env, jclass object)
{
    return zwcontrol_stop_op(&appl_ctx);
}

static int controller_getDeviceBattery(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_battery_get(&appl_ctx, (uint32_t)nodeId);
}

static int controller_getSensorMultiLevel(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_sensor_multilevel_get(&appl_ctx, (uint32_t)nodeId);
}

static int control_update_node(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_update_node(&appl_ctx, (uint8_t)nodeId);
}

static int control_saveNodeInfo(JNIEnv *env, jclass object, jstring infoFile)
{
    const char *infoPath = env->GetStringUTFChars(infoFile, 0);
    if(zwcontrol_save_nodeinfo(&appl_ctx, infoPath))
    {
        env->ReleaseStringUTFChars(infoFile, infoPath);
        return -1;
    }
    env->ReleaseStringUTFChars(infoFile, infoPath);
    return 0;
}

static int control_getBasic(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_basic_get(&appl_ctx, (uint32_t)nodeId);
}

static int control_setBasic(JNIEnv *env, jclass object, jint nodeId, jint value)
{
    return zwcontrol_basic_set(&appl_ctx, nodeId, value);
}

static int controller_getSwitchMultiLevel(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_switch_multilevel_get(&appl_ctx, (uint32_t)nodeId);
}

static int controller_setSwitchMultiLevel(JNIEnv *env, jclass object, jint nodeId, jint levValue, jint duration)
{
    return zwcontrol_switch_multilevel_set(&appl_ctx, (uint32_t)nodeId,(uint16_t)levValue, (uint8_t)duration);
}

static int controller_getSupportedSwitchType(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_get_support_switch_type(&appl_ctx, (uint32_t)nodeId);
}

static int controller_getConfiguration(JNIEnv *env, jclass object, jint nodeId, jint paramMode, jint paramNumber,
                                       jint rangeStart, jint rangeEnd)
{
    return zwcontrol_configuration_get(&appl_ctx, (uint32_t)nodeId,(uint8_t)paramMode, (uint8_t)paramNumber,
                                       (uint16_t)rangeStart, (uint16_t)rangeEnd);
}

static int controller_setConfiguration(JNIEnv *env, jclass object, jint nodeId, jint paramNumber, jint paramSize,
                                       jint useDefault, jint paramValue)
{
    return zwcontrol_configuration_set(&appl_ctx, (uint32_t)nodeId, (uint8_t)paramNumber, (uint8_t)paramSize,
                                       (uint8_t)useDefault, (int32_t)paramValue);
}

static int controller_setConfigurationBulk(JNIEnv *env, jclass object, jint nodeId, jint offset1, jint offset2, jint paramNumber, jint paramSize,
                                           jint useDefault, jintArray paramValue)
{
    jint *param_value;
    param_value = env->GetIntArrayElements(paramValue, JNI_FALSE);  
    if(param_value == NULL) 
    {
        return -1;
    }
    int result = zwcontrol_configuration_bulk_set(&appl_ctx, (uint32_t)nodeId, (uint8_t)offset1, (uint8_t)offset2,
                                                  (uint8_t)paramNumber, (uint8_t)paramSize, (uint8_t)useDefault, (uint32_t*)&param_value);
    env->ReleaseIntArrayElements(paramValue, param_value, 0);
    return result;
}

static int controller_startStopSwitchLevelChange(JNIEnv *env, jclass object, jint nodeId, jint startLvlVal, jint duration,
                                       jint pmyChangeDir, jint secChangeDir, jint secStep)
{
    return zwcontrol_start_stop_switchlevel_change(&appl_ctx, (uint32_t)nodeId, (uint16_t)startLvlVal, 
                                       (uint8_t)duration, (uint8_t)pmyChangeDir, (uint8_t)secChangeDir, (uint8_t)secStep);
}

static int controller_getPowerLevel(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_powerLevel_get(&appl_ctx, (uint32_t)nodeId);
}

static int controller_setSwitchAllOn(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_swith_all_on(&appl_ctx,(uint32_t)nodeId);
}

static int controller_setSwitchAllOff(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_swith_all_off(&appl_ctx,(uint32_t)nodeId);
}

static int controller_setSwitchAll(JNIEnv *env, jclass object, jint nodeId, jint value)
{
    return zwcontrol_swith_all_set(&appl_ctx, (uint32_t)nodeId, (uint8_t)value);
}

static int controller_getSwitchAll(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_swith_all_get(&appl_ctx, (uint32_t)nodeId);
}

static int controller_startLearnMode(JNIEnv *env, jclass object)
{
    return zwcontrol_start_learn_mode(&appl_ctx);
}

static int controller_setBinarySwitchState(JNIEnv *env, jclass object, jint nodeId, jint state, jint duration)
{
    return zwcontrol_switch_binary_set(&appl_ctx, (uint32_t)nodeId, (uint8_t)state, (uint8_t)duration);
}

static int controller_getBinarySwitchState(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_switch_binary_get(&appl_ctx, (uint32_t)nodeId);
}

static int controller_getSensorBinary(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_sensor_binary_get(&appl_ctx, (uint32_t)nodeId);
}

static int controller_getSensorBinarySupportedSensor(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_sensor_binary_supported_sensor_get(&appl_ctx, (uint32_t)nodeId);
}

static int controller_getMeter(JNIEnv *env, jclass object, jint nodeId, jint meter_unit)
{
    return zwcontrol_meter_get(&appl_ctx, (uint32_t)nodeId, (uint8_t)meter_unit);
}

static int controller_resetMeter(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_meter_reset(&appl_ctx, (uint32_t)nodeId);
}

static int controller_getMeterSupported(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_meter_supported_get(&appl_ctx, (uint32_t)nodeId);
}

static int controller_getWakeUpSettings(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_wake_up_interval_get(&appl_ctx, (uint32_t)nodeId);
}

static int controller_setWakeUpInterval(JNIEnv *env, jclass object, jint nodeId, jint interval)
{
    return zwcontrol_wake_up_interval_set(&appl_ctx, (uint32_t)nodeId, (uint32_t)interval);
}

static int controller_getDoorLockOperation(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_door_lock_operation_get(&appl_ctx, (uint32_t)nodeId);
}

static int controller_setDoorLockOperation(JNIEnv *env, jclass object, jint nodeId, jint mode)
{
    return zwcontrol_door_lock_operation_set(&appl_ctx, (uint32_t)nodeId, (uint8_t)mode);
}

static int controller_getDoorLockConfiguration(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_door_lock_config_get(&appl_ctx, (uint32_t)nodeId);
}

static int controller_setDoorLockConfiguration(JNIEnv *env, jclass object, jint nodeId, jint type, jint out_sta,
                                               jint in_sta, jint tmout_min, jint tmout_sec)
{
    return zwcontrol_door_lock_config_set(&appl_ctx, (uint32_t)nodeId, (uint8_t)type, (uint8_t)out_sta,
                                          (uint8_t)in_sta, (uint8_t)tmout_min, (uint8_t)tmout_sec);
}

static int controller_getUserCode(JNIEnv *env, jclass object, jint nodeId, jint user_id)
{
    return zwcontrol_user_code_get(&appl_ctx, (uint32_t)nodeId, (uint8_t)user_id);
}

static int controller_setUserCode(JNIEnv *env, jclass object, jint nodeId, jint user_id, jint status)
{
    return zwcontrol_user_code_set(&appl_ctx,(uint32_t)nodeId, (uint8_t)user_id, (uint8_t)status);
}

static int controller_getUserCodeNumber(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_user_code_number_get(&appl_ctx, (uint32_t)nodeId);
}

static int controller_getProtection(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_protection_get(&appl_ctx, (uint32_t)nodeId);
}

static int controller_setProtection(JNIEnv *env, jclass object, jint nodeId, jint local_port, jint rf_port)
{
    return zwcontrol_protection_set(&appl_ctx, (uint32_t)nodeId, (uint8_t)local_port, (uint8_t)rf_port);
}

static int controller_getSupportedProtection(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_supported_protection_get(&appl_ctx, (uint32_t)nodeId);
}

static int controller_getProtectionExcControlNode(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_protection_exclusive_control_node_get(&appl_ctx, (uint32_t)nodeId);
}

static int controller_getIndicator(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_indicator_get(&appl_ctx, (uint32_t)nodeId);
}

static int controller_setIndicator(JNIEnv *env, jclass object, jint nodeId, jint value)
{
    return zwcontrol_indicator_set(&appl_ctx, (uint32_t)nodeId, (uint16_t)value);
}

static int controller_setProtectionExcControlNode(JNIEnv *env, jclass object, jint nodeId, jint node_id)
{
    return zwcontrol_protection_exclusive_control_node_set(&appl_ctx, (uint32_t)nodeId, (uint8_t)node_id);
}

static int controller_getProtectionTimeout(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_protection_timeout_get(&appl_ctx, (uint32_t)nodeId);
}

static int controller_setProtectionTimeout(JNIEnv *env, jclass object, jint nodeId, jint unit, jint time)
{
    return zwcontrol_protection_timeout_set(&appl_ctx, (uint32_t)nodeId, (uint8_t)unit, (uint8_t)time);
}

static int controller_getDoorLockLoggingSupportedRecords(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_door_lock_logging_supported_records_get(&appl_ctx, (uint32_t)nodeId);
}

static int controller_getDoorLockLoggingRecords(JNIEnv *env, jclass object, jint nodeId, jint rec_num)
{
    return zwcontrol_door_lock_logging_records_get(&appl_ctx, (uint32_t)nodeId, (uint8_t)rec_num);
}

static int controller_getLanguage(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_language_get(&appl_ctx, (uint32_t)nodeId);
}

static int controller_getSwitchColor(JNIEnv *env, jclass object, jint nodeId, jint compId)
{
    return zwcontrol_switch_color_get(&appl_ctx, (uint32_t)nodeId, (uint8_t)compId);
}

static int controller_getSupportedSwitchColor(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_switch_color_supported_get(&appl_ctx, (uint32_t)nodeId);
}

static int controller_setBarrierOperator(JNIEnv *env, jclass object, jint nodeId, jint value)
{
    return zwcontrol_barrier_operator_set(&appl_ctx, (uint32_t)nodeId, (uint8_t)value);
}

static int controller_getBarrierOperator(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_barrier_operator_get(&appl_ctx, (uint32_t)nodeId);
}

static int controller_setBarrierOperatorSignal(JNIEnv *env, jclass object, jint nodeId, jint subType, jint state)
{
    return zwcontrol_barrier_operator_signal_set(&appl_ctx, (uint32_t)nodeId, (uint8_t)subType, (uint8_t)state);
}

static int controller_getBarrierOperatorSignal(JNIEnv *env, jclass object, jint nodeId, jint subType)
{
    return zwcontrol_barrier_operator_signal_get(&appl_ctx, (uint32_t)nodeId, (uint8_t)subType);
}

static int controller_getSupportedBarrierOperatorSignal(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_barrier_operator_signal_supported_get(&appl_ctx, (uint32_t)nodeId);
}

static int controller_getBasicTariffInfo(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_basic_tariff_info_get(&appl_ctx, (uint32_t)nodeId);
}

static int controller_getGroupInfo(JNIEnv *env, jclass object, jint nodeId, jint groupId)
{
    return zwcontrol_get_group_info(&appl_ctx, (uint32_t)nodeId, (uint8_t)groupId);
}

static int controller_addEndpointsToGroup(JNIEnv *env, jclass object, jint nodeId, jint groupId, jintArray arr)
{
    jint *nodeList;
    nodeList = env->GetIntArrayElements(arr, JNI_FALSE);  
    if(nodeList == NULL) 
    {
        return -1;
    }
    int result = zwcontrol_add_endpoints_to_group(&appl_ctx, (uint32_t)nodeId, (uint8_t)groupId, (uint32_t*)&nodeList);
    env->ReleaseIntArrayElements(arr, nodeList, 0);
    return result;
}

static int controller_removeEndpointsFromGroup(JNIEnv *env, jclass object, jint nodeId, jint groupId, jintArray arr)
{
    jint *removeNodeList;
    removeNodeList = env->GetIntArrayElements(arr, JNI_FALSE);  
    if(removeNodeList == NULL) 
    {
        ALOGE("GetIntArrayElements Failed");
        return -1;
    }
    int result = zwcontrol_remove_endpoints_from_group(&appl_ctx, (uint32_t)nodeId, (uint8_t)groupId, (uint32_t*)&removeNodeList);
    env->ReleaseIntArrayElements(arr, removeNodeList, 0);
    return result;
}

static int controller_getMaxSupportedGroups(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_get_max_supported_groups(&appl_ctx, (uint32_t)nodeId);
}

static int controller_getSpecificGroup(JNIEnv *env, jclass object, jint nodeId)
{
    return zwcontrol_get_specific_group(&appl_ctx, (uint32_t)nodeId);
}

static const JNINativeMethod gMethods[] = {
        {"CreateZwController",     "()I", (void *)create_controller},
        {"OpenZwController",       "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[B)I", (void *)open_controller},
        {"CloseZwController",    "()I", (void *)close_controller},
        {"DestoryZwController",    "()I", (void *)destroy_controller},
        {"ZwController_AddDevice", "()I", (void*)controller_adddevice},
        {"ZwController_RemoveDevice",    "()I", (void *)controller_removedevice},
        {"ZwController_GetDeviceList",    "()I", (void *)controller_getDeviceList},
        {"ZwController_RemoveFailedDevice",    "(I)I", (void *)controller_removeFailedDevice},
        {"ZwController_ReplaceFailedDevice",    "(I)I", (void *)controller_replaceFailedDevice},
        {"ZwController_SetDefault", "()I", (void*)controller_setDefault},
        {"ZwController_StopAddDevice", "()I", (void*)controller_stopAddDevice},
        {"ZwController_StopRemoveDevice", "()I", (void*)controller_stopRemoveDevice},
        {"ZwController_GetDeviceBattery", "(I)I", (void*)controller_getDeviceBattery},
        {"ZwController_GetSensorMultiLevel", "(I)I", (void*)controller_getSensorMultiLevel},
        {"ZwController_UpdateNode", "(I)I", (void*)control_update_node},
        {"ZwController_saveNodeInfo", "(Ljava/lang/String;)I", (void*)control_saveNodeInfo},
        {"ZwController_GetBasic", "(I)I", (void*)control_getBasic},
        {"ZwController_SetBasic", "(II)I", (void*)control_setBasic},
        {"ZwController_GetSwitchMultiLevel", "(I)I", (void*)controller_getSwitchMultiLevel},
        {"ZwController_SetSwitchMultiLevel", "(III)I", (void*)controller_setSwitchMultiLevel},
        {"ZwController_GetSupportedSwitchType", "(I)I", (void*)controller_getSupportedSwitchType},
        {"ZwController_GetConfiguration", "(IIIII)I", (void*)controller_getConfiguration},
        {"ZwController_SetConfiguration", "(IIIII)I", (void*)controller_setConfiguration},
        {"ZwController_SetConfigurationBulk", "(IIIIII[I)I", (void*)controller_setConfigurationBulk},
        {"ZwController_startStopSwitchLevelChange", "(IIIIII)I", (void*)controller_startStopSwitchLevelChange},
        {"ZwController_GetPowerLevel", "(I)I", (void*)controller_getPowerLevel},
        {"ZwController_SetSwitchAllOn", "(I)I", (void*)controller_setSwitchAllOn},
        {"ZwController_SetSwitchAllOff", "(I)I", (void*)controller_setSwitchAllOff},
        {"ZwController_SetSwitchAll", "(II)I", (void*)controller_setSwitchAll},
        {"ZwController_GetSwitchAll", "(I)I", (void*)controller_getSwitchAll},
        {"ZwController_StartLearnMode", "()I", (void*)controller_startLearnMode},
        {"ZwController_SetBinarySwitchState", "(III)I", (void*)controller_setBinarySwitchState},
        {"ZwController_GetBinarySwitchState", "(I)I", (void*)controller_getBinarySwitchState},
        {"ZwController_GetSensorBinary", "(I)I", (void*)controller_getSensorBinary},
        {"ZwController_GetSensorBinarySupportedSensor", "(I)I", (void*)controller_getSensorBinarySupportedSensor},
        {"ZwController_GetMeter", "(II)I", (void*)controller_getMeter},
        {"ZwController_resetMeter", "(I)I", (void*)controller_resetMeter},
        {"ZwController_getMeterSupported", "(I)I", (void*)controller_getMeterSupported},
        {"ZwController_getWakeUpSettings", "(I)I", (void*)controller_getWakeUpSettings},
        {"ZwController_setWakeUpInterval", "(II)I", (void*)controller_setWakeUpInterval},
        {"ZwController_getDoorLockOperation", "(I)I", (void*)controller_getDoorLockOperation},
        {"ZwController_setDoorLockOperation", "(II)I", (void*)controller_setDoorLockOperation},
        {"ZwController_getDoorLockConfiguration", "(I)I", (void*)controller_getDoorLockConfiguration},
        {"ZwController_setDoorLockConfiguration", "(IIIIII)I", (void*)controller_setDoorLockConfiguration},
        {"ZwController_getUserCode", "(II)I", (void*)controller_getUserCode},
        {"ZwController_setUserCode", "(III)I", (void*)controller_setUserCode},
        {"ZwController_getUserCodeNumber", "(I)I", (void*)controller_getUserCodeNumber},
        {"ZwController_getProtection", "(I)I", (void*)controller_getProtection},
        {"ZwController_setProtection", "(III)I", (void*)controller_setProtection},
        {"ZwController_getSupportedProtection", "(I)I", (void*)controller_getSupportedProtection},
        {"ZwController_getProtectionExcControlNode", "(I)I", (void*)controller_getProtectionExcControlNode},
        {"ZwController_setProtectionExcControlNode", "(II)I", (void*)controller_setProtectionExcControlNode},
        {"ZwController_getProtectionTimeout", "(I)I", (void*)controller_getProtectionTimeout},
        {"ZwController_setProtectionTimeout", "(III)I", (void*)controller_setProtectionTimeout},
        {"ZwController_getIndicator", "(I)I", (void*)controller_getIndicator},
        {"ZwController_setIndicator", "(II)I", (void*)controller_setIndicator},
        {"ZwController_getDoorLockLoggingSupportedRecords", "(I)I", (void*)controller_getDoorLockLoggingSupportedRecords},
        {"ZwController_getDoorLockLoggingRecords", "(II)I", (void*)controller_getDoorLockLoggingRecords},
        {"ZwController_getLanguage", "(I)I", (void*)controller_getLanguage},
        {"ZwController_getSwitchColor", "(II)I", (void*)controller_getSwitchColor},
        {"ZwController_getSupportedSwitchColor", "(I)I", (void*)controller_getSupportedSwitchColor},
        {"ZwController_setBarrierOperator", "(II)I", (void*)controller_setBarrierOperator},
        {"ZwController_getBarrierOperator", "(I)I", (void*)controller_getBarrierOperator},
        {"ZwController_setBarrierOperatorSignal", "(III)I", (void*)controller_setBarrierOperatorSignal},
        {"ZwController_getBarrierOperatorSignal", "(II)I", (void*)controller_getBarrierOperatorSignal},
        {"ZwController_getSupportedBarrierOperatorSignal", "(I)I", (void*)controller_getSupportedBarrierOperatorSignal},
        {"ZwController_getBasicTariffInfo", "(I)I", (void*)controller_getBasicTariffInfo},
        {"ZwController_getGroupInfo", "(II)I", (void*)controller_getGroupInfo},
        {"ZwController_addEndpointsToGroup", "(II[I)I", (void*)controller_addEndpointsToGroup},
        {"ZwController_removeEndpointsFromGroup", "(II[I)I", (void*)controller_removeEndpointsFromGroup},
        {"ZwController_getMaxSupportedGroups", "(I)I", (void*)controller_getMaxSupportedGroups},
        {"ZwController_getSpecificGroup", "(I)I", (void*)controller_getSpecificGroup},

};

static int registerNativeMethods(JNIEnv* env, const char* className,
                                 const JNINativeMethod* methods, int numMethods)
{
    jclass clazz;
    clazz = env->FindClass(className);

    if (clazz == NULL)
    {
        return JNI_FALSE;
    }

    if (env->RegisterNatives(clazz, methods, numMethods) < 0)
    {
        return JNI_FALSE;
    }

    env->DeleteLocalRef(clazz);

    return JNI_TRUE;
}

JNIEXPORT jint JNI_OnLoad(JavaVM* g_javaVM, void* reserved)
{
    int status;

    if(g_javaVM == NULL)
    {
        return -1;
    }

    JNIEnv* env = NULL;

    status = g_javaVM->GetEnv((void **) &env, JNI_VERSION_1_4);

    if(status < 0)
    {
        status = g_javaVM->AttachCurrentThread(&env, NULL);

        if(status < 0)
        {
            env = NULL;
        }

        return -1;
    }

    if(registerNativeMethods(env, "com/askey/firefly/zwave/control/jni/ZwaveControlHelper",
                             gMethods, NELEM(gMethods)) < 0)
    {
        return -1;
    }

    ZwControlServiceVM = g_javaVM;

    return JNI_VERSION_1_4;
}