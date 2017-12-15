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

static jint open_controller(JNIEnv *env, jclass object, jstring path, jstring InfoPath, jbyteArray result)
{
    if(env == NULL)
    {
        return -1;
    }

    const char *resPath = env->GetStringUTFChars(path, 0);
    const char *infoFile = env->GetStringUTFChars(InfoPath, 0);

    uint8_t str[1024] = {0};

    if(zwcontrol_init(&appl_ctx, resPath, infoFile, str) != 0)
    {
        env->ReleaseStringUTFChars(path, resPath);
        env->ReleaseStringUTFChars(InfoPath, infoFile);
        return -1;
    }

    env->ReleaseStringUTFChars(path, resPath);
    env->ReleaseStringUTFChars(InfoPath, infoFile);

    zwcontrol_setcallback(ZwControlResCallBack);

    int len = (int)strlen((char*)str);

    env->SetByteArrayRegion(result, 0, len, (jbyte*)str);

    return 0;
}

static int close_controller(JNIEnv *env, jclass object)
{
    if(zwcontrol_exit(&appl_ctx) == 0)
    {
        memset(&appl_ctx, 0x00, sizeof(hl_appl_ctx_t));
    }

    return 0;
}

static int destroy_controller(JNIEnv *env, jclass object)
{
    if(ZwControlServiceClass != NULL)
    {
        env->DeleteGlobalRef(ZwControlServiceClass);
    }

    return 0;
}

static int controller_adddevice(JNIEnv *env, jclass object, jbyteArray dsk, jint dsklen)
{
    char array[200];

    env->GetByteArrayRegion(dsk, 0, dsklen, (jbyte*)array);

    int result = zwcontrol_add_node(&appl_ctx, array, dsklen);

    return result;
}

static int controller_removedevice(JNIEnv *env, jclass object)
{
    return zwcontrol_rm_node(&appl_ctx);
}

static int controller_getDeviceList(JNIEnv *env, jclass object)
{
    return zwcontrol_get_node_list(&appl_ctx);
}

static int controller_getDeviceInfo(JNIEnv *env, jclass object)
{
    return zwcontrol_get_node_info(&appl_ctx);
}

static int controller_removeFailedDevice(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_replaceFailedDevice(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_setDefault(JNIEnv *env, jclass object)
{
    return zwcontrol_default_set(&appl_ctx);
}

static int controller_stopAddDevice(JNIEnv *env, jclass object)
{
    return 0;
}

static int controller_stopRemoveDevice(JNIEnv *env, jclass object)
{
    return 0;
}

static int controller_getDeviceBattery(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_getSensorMultiLevel(JNIEnv *env, jclass object, jint nodeId/*, jint sensor_type, jint unit*/)
{
    return 0;
}

static int control_update_node(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
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
    return 0;
}

static int control_setBasic(JNIEnv *env, jclass object, jint nodeId, jint value)
{
    return 0;
}

static int controller_getSwitchMultiLevel(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_setSwitchMultiLevel(JNIEnv *env, jclass object, jint nodeId, jint levValue, jint duration)
{
    return 0;
}

static int controller_getSupportedSwitchType(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_getConfiguration(JNIEnv *env, jclass object, jint nodeId, jint paramMode, jint paramNumber,
                                       jint rangeStart, jint rangeEnd)
{
    return 0;
}

static int controller_setConfiguration(JNIEnv *env, jclass object, jint nodeId, jint paramNumber, jint paramSize,
                                       jint useDefault, jint paramValue)
{
    return 0;
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
    int result = 0;
    env->ReleaseIntArrayElements(paramValue, param_value, 0);
    return result;
}

static int controller_startStopSwitchLevelChange(JNIEnv *env, jclass object, jint nodeId, jint startLvlVal, jint duration,
                                       jint pmyChangeDir, jint secChangeDir, jint secStep)
{
    return 0;
}

static int controller_getPowerLevel(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_setSwitchAllOn(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_setSwitchAllOnBroadcast(JNIEnv *env, jclass object)
{
    return 0;
}

static int controller_setSwitchAllOff(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_setSwitchAllOffBroadcast(JNIEnv *env, jclass object)
{
    return 0;
}

static int controller_setSwitchAll(JNIEnv *env, jclass object, jint nodeId, jint value)
{
    return 0;
}

static int controller_getSwitchAll(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_startLearnMode(JNIEnv *env, jclass object)
{
    return 0;
}

static int controller_setBinarySwitchState(JNIEnv *env, jclass object, jint nodeId, jint state, jint duration)
{
    return 0;
}

static int controller_getBinarySwitchState(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_getSensorBinary(JNIEnv *env, jclass object, jint nodeId, jint sensor_type)
{
    return 0;
}

static int controller_getSensorBinarySupportedSensor(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_getMeter(JNIEnv *env, jclass object, jint nodeId, jint meter_unit)
{
    return 0;
}

static int controller_resetMeter(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_getMeterSupported(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_getWakeUpSettings(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_setWakeUpInterval(JNIEnv *env, jclass object, jint nodeId, jint interval)
{
    return 0;
}

static int controller_getDoorLockOperation(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_setDoorLockOperation(JNIEnv *env, jclass object, jint nodeId, jint mode)
{
    return 0;
}

static int controller_getDoorLockConfiguration(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_setDoorLockConfiguration(JNIEnv *env, jclass object, jint nodeId, jint type, jint out_sta,
                                               jint in_sta, jint tmout_min, jint tmout_sec)
{
    return 0;
}

static int controller_getUserCode(JNIEnv *env, jclass object, jint nodeId, jint user_id)
{
    return 0;
}

static int controller_setUserCode(JNIEnv *env, jclass object, jint nodeId, jint user_id, jint status)
{
    return 0;
}

static int controller_getUserCodeNumber(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_getProtection(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_setProtection(JNIEnv *env, jclass object, jint nodeId, jint local_port, jint rf_port)
{
    return 0;
}

static int controller_getSupportedProtection(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_getProtectionExcControlNode(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_getIndicator(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_setIndicator(JNIEnv *env, jclass object, jint nodeId, jint value)
{
    return 0;
}

static int controller_setProtectionExcControlNode(JNIEnv *env, jclass object, jint nodeId, jint node_id)
{
    return 0;
}

static int controller_getProtectionTimeout(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_setProtectionTimeout(JNIEnv *env, jclass object, jint nodeId, jint unit, jint time)
{
    return 0;
}

static int controller_getDoorLockLoggingSupportedRecords(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_getDoorLockLoggingRecords(JNIEnv *env, jclass object, jint nodeId, jint rec_num)
{
    return 0;
}

static int controller_getLanguage(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_getSwitchColor(JNIEnv *env, jclass object, jint nodeId, jint compId)
{
    return 0;
}

static int controller_getSupportedSwitchColor(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_setSwitchColor(JNIEnv *env, jclass object, jint nodeId, jint compId, jint value)
{
    return 0;
}

static int controller_startStopSwitchColorLevelChange(JNIEnv *env, jclass object, jint nodeId, jint dir, jint ignore,
                                                      jint compId, jint startLvlVal)
{
    return 0;
}

static int controller_setBarrierOperator(JNIEnv *env, jclass object, jint nodeId, jint value)
{
    return 0;
}

static int controller_getBarrierOperator(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_setBarrierOperatorSignal(JNIEnv *env, jclass object, jint nodeId, jint subType, jint state)
{
    return 0;
}

static int controller_getBarrierOperatorSignal(JNIEnv *env, jclass object, jint nodeId, jint subType)
{
    return 0;
}

static int controller_getSupportedBarrierOperatorSignal(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_getBasicTariffInfo(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_getGroupInfo(JNIEnv *env, jclass object, jint nodeId, jint groupId, jint endpointId)
{
    return 0;
}

static int controller_addEndpointsToGroup(JNIEnv *env, jclass object, jint nodeId, jint groupId, jintArray intArray, jint endpointId)
{
    jint *node_list = NULL;
    node_list = env->GetIntArrayElements(intArray, 0);
    if(node_list == NULL) 
    {
        return -1;
    }

    int result = 0;
    env->ReleaseIntArrayElements(intArray, node_list, 0);
    return result;
}

static int controller_removeEndpointsFromGroup(JNIEnv *env, jclass object, jint nodeId, jint groupId, jintArray intArray, jint endpointId)
{
    jint *node_list = NULL;
    node_list = env->GetIntArrayElements(intArray, JNI_FALSE);  
    if(node_list == NULL) 
    {
        ALOGE("GetIntArrayElements Failed");
        return -1;
    }
    int result = 0;
    env->ReleaseIntArrayElements(intArray, node_list, 0);
    return result;
}

static int controller_getMaxSupportedGroups(JNIEnv *env, jclass object, jint nodeId, jint endpointId)
{
    return 0;
}

static int controller_getSpecificGroup(JNIEnv *env, jclass object, jint nodeId, jint endpointId)
{
    return 0;
}

static int controller_setNotification(JNIEnv *env, jclass object, jint nodeId, jint type, jint status)
{
    return 0;
}

static int controller_getNotification(JNIEnv *env, jclass object, jint nodeId, jint alarmType, jint notifType, jint status)
{
    return 0;
}

static int controller_getSupportedNotification(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_getSupportedEventNotification(JNIEnv *env, jclass object, jint nodeId, jint notifType)
{
    return 0;
}

static int controller_getSupportedCentralScene(JNIEnv *env, jclass object, jint nodeId, jint endpointId)
{
    return 0;
}

static int controller_getSceneActuatorConf(JNIEnv *env, jclass object, jint nodeId, jint sceneId)
{
    return 0;
}

static int controller_setSceneActuatorConf(JNIEnv *env, jclass object, jint nodeId, jint sceneId, jint dimDuration, jint override, jint level)
{
    return 0;
}

static int controller_multiCmdEncap(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_getFirmwareUpdateInfo(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_requestFirmwareUpdate(JNIEnv *env, jclass object, jint nodeId, jint vendorId, jint firmwareId,
                                            jint firmwareTarget, jint fragmentSize, jstring firmwareFile)
{
    const char *updateFile = env->GetStringUTFChars(firmwareFile, 0);
    int result = 0;

    env->ReleaseStringUTFChars(firmwareFile, updateFile);

    return result;
}

static int controller_getCommandQueueState(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_controlCommandQueue(JNIEnv *env, jclass object, jint nodeId, jint state)
{
    return 0;
}

static int controller_viewCommandQueue(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static int controller_cancelAllCommandQueue(JNIEnv *env, jclass object, jint nodeId)
{
    return 0;
}

static const JNINativeMethod gMethods[] = {
        {"CreateZwController",     "()I", (void *)create_controller},
        {"OpenZwController",       "(Ljava/lang/String;Ljava/lang/String;[B)I", (void *)open_controller},
        {"CloseZwController",    "()I", (void *)close_controller},
        {"DestoryZwController",    "()I", (void *)destroy_controller},
        {"ZwController_AddDevice", "([BI)I", (void*)controller_adddevice},
        {"ZwController_RemoveDevice", "()I", (void *)controller_removedevice},
        {"ZwController_GetDeviceList", "()I", (void *)controller_getDeviceList},
        {"ZwController_GetDeviceInfo", "()I", (void *)controller_getDeviceInfo},
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
        {"ZwController_SetSwitchAllOnBroadcast", "()I", (void*)controller_setSwitchAllOnBroadcast},
        {"ZwController_SetSwitchAllOffBroadcast", "()I", (void*)controller_setSwitchAllOffBroadcast},
        {"ZwController_SetSwitchAll", "(II)I", (void*)controller_setSwitchAll},
        {"ZwController_GetSwitchAll", "(I)I", (void*)controller_getSwitchAll},
        {"ZwController_StartLearnMode", "()I", (void*)controller_startLearnMode},
        {"ZwController_SetBinarySwitchState", "(III)I", (void*)controller_setBinarySwitchState},
        {"ZwController_GetBinarySwitchState", "(I)I", (void*)controller_getBinarySwitchState},
        {"ZwController_GetSensorBinary", "(II)I", (void*)controller_getSensorBinary},
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
        {"ZwController_setSwitchColor", "(III)I", (void*)controller_setSwitchColor},
        {"ZwController_startStopSwitchColorLevelChange", "(IIIII)I", (void*)controller_startStopSwitchColorLevelChange},
        {"ZwController_setBarrierOperator", "(II)I", (void*)controller_setBarrierOperator},
        {"ZwController_getBarrierOperator", "(I)I", (void*)controller_getBarrierOperator},
        {"ZwController_setBarrierOperatorSignal", "(III)I", (void*)controller_setBarrierOperatorSignal},
        {"ZwController_getBarrierOperatorSignal", "(II)I", (void*)controller_getBarrierOperatorSignal},
        {"ZwController_getSupportedBarrierOperatorSignal", "(I)I", (void*)controller_getSupportedBarrierOperatorSignal},
        {"ZwController_getBasicTariffInfo", "(I)I", (void*)controller_getBasicTariffInfo},
        {"ZwController_getGroupInfo", "(III)I", (void*)controller_getGroupInfo},
        {"ZwController_addEndpointsToGroup", "(II[II)I", (void*)controller_addEndpointsToGroup},
        {"ZwController_removeEndpointsFromGroup", "(II[II)I", (void*)controller_removeEndpointsFromGroup},
        {"ZwController_getMaxSupportedGroups", "(II)I", (void*)controller_getMaxSupportedGroups},
        {"ZwController_getSpecificGroup", "(II)I", (void*)controller_getSpecificGroup},
        {"ZwController_setNotification", "(III)I", (void*)controller_setNotification},
        {"ZwController_getNotification", "(IIII)I", (void*)controller_getNotification},
        {"ZwController_getSupportedNotification", "(I)I", (void*)controller_getSupportedNotification},
        {"ZwController_getSupportedEventNotification", "(II)I", (void*)controller_getSupportedEventNotification},
        {"ZwController_getSupportedCentralScene", "(II)I", (void*)controller_getSupportedCentralScene},
        {"ZwController_getSceneActuatorConf", "(II)I", (void*)controller_getSceneActuatorConf},
        {"ZwController_setSceneActuatorConf", "(IIIII)I", (void*)controller_setSceneActuatorConf},
        {"ZwController_multiCmdEncap", "(I)I", (void*)controller_multiCmdEncap},
        {"ZwController_getFirmwareUpdateInfo", "(I)I", (void*)controller_getFirmwareUpdateInfo},
        {"ZwController_requestFirmwareUpdate", "(IIIIILjava/lang/String;)I" , (void*)controller_requestFirmwareUpdate},
        {"ZwController_getCommandQueueState", "(I)I", (void*)controller_getCommandQueueState},
        {"ZwController_controlCommandQueue", "(II)I", (void*)controller_controlCommandQueue},
        {"ZwController_viewCommandQueue", "(I)I", (void*)controller_viewCommandQueue},
        {"ZwController_cancelAllCommandQueue", "(I)I", (void*)controller_cancelAllCommandQueue},

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