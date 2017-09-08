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
        {"ZwController_startStopSwitchLevelChange", "(IIIIII)I", (void*)controller_startStopSwitchLevelChange},
        {"ZwController_GetPowerLevel", "(I)I", (void*)controller_getPowerLevel},
        {"ZwController_SetSwitchAllOn", "(I)I", (void*)controller_setSwitchAllOn},
        {"ZwController_SetSwitchAllOff", "(I)I", (void*)controller_setSwitchAllOff},
        {"ZwController_SetSwitchAll", "(II)I", (void*)controller_setSwitchAll},
        {"ZwController_GetSwitchAll", "(I)I", (void*)controller_getSwitchAll}
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