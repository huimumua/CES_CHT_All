/***************************************************************************
*
* Copyright (c) 2001-2011
* Sigma Designs, Inc.
* All Rights Reserved
*
*---------------------------------------------------------------------------
*
* Description: DoorLockKeyPad src file
*
* Author:
*
* Last Changed By:  $Author:  $
* Revision:         $Revision:  $
* Last Changed:     $Date:  $
*
****************************************************************************/


/****************************************************************************/
/*                              INCLUDE FILES                               */
/****************************************************************************/
#include "config_app.h"

#include <slave_learn.h>
#include <ZW_slave_api.h>
#ifdef ZW_SLAVE_32
#include <ZW_slave_32_api.h>
#else
#include <ZW_slave_routing_api.h>
#endif  /* ZW_SLAVE_32 */

#include <ZW_classcmd.h>
#include <ZW_mem_api.h>
#include <ZW_TransportLayer.h>

#include <eeprom.h>
#include <ZW_uart_api.h>

#include <misc.h>
#ifdef BOOTLOADER_ENABLED
#include <ota_util.h>
#include <CommandClassFirmwareUpdate.h>
#endif

/*IO control*/
#include <io_zdp03a.h>

#include <ZW_task.h>
#include <ev_man.h>

#ifdef ZW_ISD51_DEBUG
#include "ISD51.h"
#endif

#include <association_plus.h>
#include <agi.h>
#include <CommandClassAssociation.h>
#include <CommandClassAssociationGroupInfo.h>
#include <CommandClassVersion.h>
#include <CommandClassZWavePlusInfo.h>
#include <CommandClassPowerLevel.h>
#include <CommandClassDeviceResetLocally.h>
#include <CommandclassDoorLock.h>
#include <CommandclassUserCode.h>
#include <CommandClassBattery.h>
#include <Commandclasssupervision.h>
#include <CommandClassMultiChan.h>
#include <CommandClassMultiChanAssociation.h>

#include <battery_plus.h>
#include <battery_monitor.h>

#include <nvm_util.h>

#ifdef TEST_INTERFACE_SUPPORT
#include <ZW_test_interface.h>
#include <ZW_uart_api.h>
#include <ZW_string.h>

#endif /*TEST_INTERFACE_SUPPORT*/

/****************************************************************************/
/*                      PRIVATE TYPES and DEFINITIONS                       */
/****************************************************************************/
/**
 * @def ZW_DEBUG_DOORLOCK_SEND_BYTE(data)
 * Transmits a given byte to the debug port.
 * @def ZW_DEBUG_DOORLOCK_SEND_STR(STR)
 * Transmits a given string to the debug port.
 * @def ZW_DEBUG_DOORLOCK_SEND_NUM(data)
 * Transmits a given number to the debug port.
 * @def ZW_DEBUG_DOORLOCK_SEND_WORD_NUM(data)
 * Transmits a given WORD number to the debug port.
 * @def ZW_DEBUG_DOORLOCK_SEND_NL()
 * Transmits a newline to the debug port.
 */
#ifdef ZW_DEBUG_DOORLOCK
#define ZW_DEBUG_DOORLOCK_SEND_BYTE(data) ZW_DEBUG_SEND_BYTE(data)
#define ZW_DEBUG_DOORLOCK_SEND_STR(STR) ZW_DEBUG_SEND_STR(STR)
#define ZW_DEBUG_DOORLOCK_SEND_NUM(data)  ZW_DEBUG_SEND_NUM(data)
#define ZW_DEBUG_DOORLOCK_SEND_WORD_NUM(data) ZW_DEBUG_SEND_WORD_NUM(data)
#define ZW_DEBUG_DOORLOCK_SEND_NL()  ZW_DEBUG_SEND_NL()
#else
#define ZW_DEBUG_DOORLOCK_SEND_BYTE(data)
#define ZW_DEBUG_DOORLOCK_SEND_STR(STR)
#define ZW_DEBUG_DOORLOCK_SEND_NUM(data)
#define ZW_DEBUG_DOORLOCK_SEND_WORD_NUM(data)
#define ZW_DEBUG_DOORLOCK_SEND_NL()
#endif



/**
 * Application events for AppStateManager(..)
 */
typedef enum _EVENT_APP_
{
  EVENT_EMPTY = DEFINE_EVENT_APP_NBR,
  EVENT_APP_INIT,
  EVENT_APP_REFRESH_MMI,
  EVENT_APP_NEXT_EVENT_JOB,
  EVENT_APP_FINISH_EVENT_JOB,
  EVENT_APP_GET_NODELIST,
  EVENT_APP_BATT_LOW,
  EVENT_APP_IS_POWERING_DOWN,
  EVENT_APP_START_USER_CODE_EVENT,
  EVENT_APP_DOORLOCK_JOB, //2A
  EVENT_APP_START_KEYPAD_ACTIVE,
  EVENT_APP_FINISH_KEYPAD_ACTIVE
} EVENT_APP;


/**
 * Application states. Function AppStateManager(..) includes the state
 * event machine.
 */
typedef enum _STATE_APP_
{
  STATE_APP_STARTUP,
  STATE_APP_IDLE,
  STATE_APP_LEARN_MODE,
  STATE_APP_WATCHDOG_RESET,
  STATE_APP_OTA,
  STATE_APP_TRANSMIT_DATA,
  STATE_UNSOLICITED_EVENT,
  STATE_BATT_DEAD,
  STATE_APP_POWERDOWN,
  STATE_APP_USER_KEYPAD
 } STATE_APP;

#define DOOR_LOCK_OPERATION_SET_TIMEOUT_NOT_SUPPORTED 0xFE

#define MAX_KEY_LEN 4

/****************************************************************************/
/*                              PRIVATE DATA                                */
/****************************************************************************/

/**
 * Unsecure node information list.
 * Be sure Command classes are not duplicated in both lists.
 * CHANGE THIS - Add all supported non-secure command classes here
 **/
static code BYTE cmdClassListNonSecureNotIncluded[] =
{
  COMMAND_CLASS_ZWAVEPLUS_INFO,
  COMMAND_CLASS_TRANSPORT_SERVICE_V2,
  COMMAND_CLASS_SECURITY,
  COMMAND_CLASS_SECURITY_2
};


/**
 * Unsecure node information list Secure included.
 * Be sure Command classes are not duplicated in both lists.
 * CHANGE THIS - Add all supported non-secure command classes here
 **/
static code BYTE cmdClassListNonSecureIncludedSecure[] =
{
  COMMAND_CLASS_ZWAVEPLUS_INFO,
  COMMAND_CLASS_TRANSPORT_SERVICE_V2,
  COMMAND_CLASS_SECURITY,
  COMMAND_CLASS_SECURITY_2
};


/**
 * Secure node inforamtion list.
 * Be sure Command classes are not duplicated in both lists.
 * CHANGE THIS - Add all supported secure command classes here
 **/
static code BYTE cmdClassListSecure[] =
{
  COMMAND_CLASS_VERSION,
  COMMAND_CLASS_MANUFACTURER_SPECIFIC,
  COMMAND_CLASS_DEVICE_RESET_LOCALLY,
  COMMAND_CLASS_POWERLEVEL,
  COMMAND_CLASS_BATTERY,
  COMMAND_CLASS_DOOR_LOCK,
  COMMAND_CLASS_USER_CODE,
  COMMAND_CLASS_ASSOCIATION_V2,
  COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V2,
  COMMAND_CLASS_ASSOCIATION_GRP_INFO,
  COMMAND_CLASS_SUPERVISION
#ifdef BOOTLOADER_ENABLED
  ,COMMAND_CLASS_FIRMWARE_UPDATE_MD_V2
#endif
};


/**
 * Structure includes application node information list's and device type.
 */
APP_NODE_INFORMATION m_AppNIF =
{
  cmdClassListNonSecureNotIncluded, sizeof(cmdClassListNonSecureNotIncluded),
  cmdClassListNonSecureIncludedSecure, sizeof(cmdClassListNonSecureIncludedSecure),
  cmdClassListSecure, sizeof(cmdClassListSecure),
  DEVICE_OPTIONS_MASK, GENERIC_TYPE, SPECIFIC_TYPE
};

/**
 * AGI lifeline string
 */
const char GroupName[]   = "Lifeline";

/**
 * Setup AGI lifeline table from app_config.h
 */
CMD_CLASS_GRP  agiTableLifeLine[] = {AGITABLE_LIFELINE_GROUP};

/**
 * Setup AGI root device groups table from app_config.h
 */

AGI_GROUP agiTableRootDeviceGroups[] = {AGITABLE_ROOTDEVICE_GROUPS};

BYTE myNodeID = 0;


/**
 * Application state-machine state.
 */
static STATE_APP currentState = STATE_APP_IDLE;

/**
 * Parameter is used to save wakeup reason after ApplicationInitHW(..)
 */
SW_WAKEUP wakeupReason;

/**
 * Command Class Door lock
 * Following parameters are not supported in application:
 * insideDoorHandleMode: Inside Door Handles Mode (4 bits)
 * outsideDoorHandleMode: Outside Door Handles Mode
 * condition: Door condition
 */
CMD_CLASS_DOOR_LOCK_DATA myDoorLock;


/**
 * Used by the Supervision Get handler. Holds RX options.
 */
static RECEIVE_OPTIONS_TYPE_EX rxOptionSupervision;

/**
 * Used by the Supervision Get handler. Holds Supervision session ID.
 */
static BYTE sessionID;

#ifdef APP_SUPPORTS_CLIENT_SIDE_AUTHENTICATION
#ifndef TEST_INTERFACE_SUPPORT
s_SecurityS2InclusionCSAPublicDSK_t sCSAResponse = { 0, 0, 0, 0};
#endif /* TEST_INTERFACE_SUPPORT */
#endif /* APP_SUPPORTS_CLIENT_SIDE_AUTHENTICATION */

/****************************************************************************/
/*                              EXPORTED DATA                               */
/****************************************************************************/

/****************************************************************************/
/*                            PRIVATE FUNCTIONS                             */
/****************************************************************************/

void ZCB_BattReportSentDone(TRANSMISSION_RESULT * pTransmissionResult);
void ZCB_DeviceResetLocallyDone(TRANSMISSION_RESULT * pTransmissionResult);
STATE_APP GetAppState();
void AppStateManager( EVENT_APP event);
void ChangeState( STATE_APP newState);
void ZCB_JobStatus(TRANSMISSION_RESULT * pTransmissionResult);
static void SaveStatus(void);
BOOL Feedkey(BYTE keyNbr);
void SetCondition( void );
JOB_STATUS DoorLockOperationSupportReport(AGI_PROFILE* pAgiProfile);


#ifdef BOOTLOADER_ENABLED
void ZCB_OTAFinish(OTA_STATUS otaStatus);
BOOL ZCB_OTAStart();
#endif

void ZCB_CommandClassSupervisionGetReceived(SUPERVISION_GET_RECEIVED_HANDLER_ARGS * pArgs);
void ZCB_SupervisionTimerCallback(void);
void StartKeypadApp(void);
void ZCB_AppKeypad(BYTE channel, BYTE * pString);
void DefaultApplicationsSettings(void);
/**
 * @brief See description for function prototype in ZW_basis_api.h.
 */
void
ApplicationRfNotify(BYTE rfState)
{
  UNUSED(rfState);
}

/**
 * @brief See description for function prototype in ZW_basis_api.h.
 */
BYTE
ApplicationInitHW(SW_WAKEUP bWakeupReason)
{
  wakeupReason = bWakeupReason;

  /* hardware initialization */
  ZDP03A_InitHW(ZCB_EventSchedulerEventAdd, &ZCB_SetPowerDownTimeoutWakeUpStateCheck);

  //Read port state and set it before setting port as output.
  Led(ZDP03A_LED_D1, KeyGet((ZDP03A_KEY) ZDP03A_LED_D1));
  SetPinOut(ZDP03A_LED_D1);
  Led(ZDP03A_LED_D2, KeyGet((ZDP03A_KEY) ZDP03A_LED_D2));
  SetPinOut(ZDP03A_LED_D2);
  Led(ZDP03A_LED_D3,OFF);
  SetPinOut(ZDP03A_LED_D3);

  SetPinIn(ZDP03A_KEY_1,TRUE);
  SetPinIn(ZDP03A_KEY_2,TRUE);
  SetPinIn(ZDP03A_KEY_4,TRUE);
  SetPinIn(ZDP03A_KEY_6,TRUE);

  InitBatteryMonitor(wakeupReason);
  Transport_OnApplicationInitHW(bWakeupReason);

  return(TRUE);
}

/**
 * @brief See description for function prototype in ZW_basis_api.h.
 */
BYTE
ApplicationInitSW(ZW_NVM_STATUS nvmStatus)
{
  /* Init state machine*/
  currentState = STATE_APP_STARTUP;
  /* Do not reinitialize the UART if already initialized for ISD51 in ApplicationInitHW() */
#ifndef ZW_ISD51_DEBUG
  ZW_DEBUG_INIT(1152);
#endif
  ZW_DEBUG_DOORLOCK_SEND_STR("AppInitSW ");
  ZW_DEBUG_DOORLOCK_SEND_NUM(wakeupReason);
  ZW_DEBUG_DOORLOCK_SEND_NL();

#ifdef WATCHDOG_ENABLED
  ZW_WatchDogEnable();
#endif

  /*Check battery level*/
  /*Check if battery Monitor has no state change and last time was battery state ST_BATT_DEAD*/
  if((FALSE == BatterySensorRead(NULL)) && (ST_BATT_DEAD ==BatteryMonitorState()))
  {
    ZW_DEBUG_DOORLOCK_SEND_NL();
    ZW_DEBUG_DOORLOCK_SEND_STR("DEAD BATT!");
    ZW_DEBUG_DOORLOCK_SEND_NL();
    /*just power down! woltage to low.*/
    PowerDownNow();
    AppStateManager(EVENT_APP_IS_POWERING_DOWN);
  	return TRUE;
  }


#ifdef BOOTLOADER_ENABLED
  NvmInit(nvmStatus);

#else
  UNUSED(nvmStatus);
#endif


  BatteryInit( BATT_MODE_LISTENING, wakeupReason);

  /*
   * Initialize Event Scheduler.
   */
  EventSchedulerInit(AppStateManager);

  myDoorLock.condition = 0; /* read HW-codition for the door: [door] Open/close,[bolt] Locked/unlocked,[Latch] Open/Closed */
  myDoorLock.insideDoorHandleMode = 0; /* reset handle */
  myDoorLock.outsideDoorHandleMode |= 0x01; /* reset handle */
  if(0 != KeyGet(ZDP03A_KEY_2))
  {
    myDoorLock.outsideDoorHandleMode &= 0x0E;
  }
  /* get stored values */
  if (MemoryGetByte((WORD)&EEOFFSET_MAGIC_far) == APPL_MAGIC_VALUE)
  {
    myDoorLock.condition = MemoryGetByte((WORD)&EEOFFSET_DOOR_LOCK_CONDITION_far);
    myDoorLock.type = MemoryGetByte((WORD)&EEOFFSET_OPERATION_TYPE_far);
    myDoorLock.insideDoorHandleState = MemoryGetByte((WORD) &EEOFFSET_HANDLES_IN_OUT_SITE_DOOR_HANDLES_STATE_far) & 0xF;
    myDoorLock.outsideDoorHandleState = MemoryGetByte((WORD) &EEOFFSET_HANDLES_IN_OUT_SITE_DOOR_HANDLES_STATE_far) >> 4;
    myDoorLock.lockTimeoutMin = MemoryGetByte((WORD) &EEOFFSET_LOCK_MINUTES_far);
    myDoorLock.lockTimeoutSec = MemoryGetByte((WORD) &EEOFFSET_LOCK_SECONDS_far);
    loadStatusPowerLevel( ZCB_StopPowerDownTimer, ZCB_StartPowerDownTimer);

    ZW_DEBUG_DOORLOCK_SEND_STR("*** LOAD DATA!!*** ");
    ZW_DEBUG_DOORLOCK_SEND_NUM(myDoorLock.type);
  }
  else
  {

    ZW_DEBUG_DOORLOCK_SEND_STR("*** DEFAULT DATA!!*** ");
    ZW_MEM_PUT_BYTE((WORD)&EEOFFS_SECURITY_RESERVED.EEOFFS_MAGIC_BYTE_field, EEPROM_MAGIC_BYTE_VALUE);
    DefaultApplicationsSettings();
    ActivateBattNotificationTrigger();

    /* Initialize transport layer NVM */
    Transport_SetDefault();
    /* Reset protocol */
    ZW_SetDefault();
  /* Initialize PowerLevel functionality.*/
    loadInitStatusPowerLevel(ZCB_StopPowerDownTimer, ZCB_StartPowerDownTimer);
    SaveStatus(); /* Now EEPROM should be OK */
  }
  /*1. Set LED condition */
  SetCondition();
  /* Initialize association module */
  AssociationInit(FALSE);
  /* Setup AGI group lists*/
  AGI_Init();
  AGI_LifeLineGroupSetup(agiTableLifeLine, (sizeof(agiTableLifeLine)/sizeof(CMD_CLASS_GRP)), GroupName, ENDPOINT_ROOT);
  AGI_ResourceGroupSetup(agiTableRootDeviceGroups, (sizeof(agiTableRootDeviceGroups)/sizeof(AGI_GROUP)), ENDPOINT_ROOT);

  /* Get this sensors identification on the network */
  MemoryGetID(NULL, &myNodeID);
  /* Initialize manufactory specific module */
  ManufacturerSpecificDeviceIDInit();

#ifdef BOOTLOADER_ENABLED
  /* Initialize OTA module */
  OtaInit( ZCB_OTAStart, NULL, ZCB_OTAFinish);
#endif

  CommandClassSupervisionInit(
      CC_SUPERVISION_STATUS_UPDATES_NOT_SUPPORTED,
      ZCB_CommandClassSupervisionGetReceived,
      NULL);

  Transport_OnApplicationInitSW( &m_AppNIF, &ZCB_SetPowerDownTimeoutWakeUpStateCheck);

  ZCB_EventSchedulerEventAdd((EVENT_WAKEUP)wakeupReason);

  return(TRUE);
}

/**
 * @brief See description for function prototype in ZW_basis_api.h.
 */
void
ApplicationTestPoll(void)
{
}

/**
 * @brief See description for function prototype in ZW_basis_api.h.
 */
void
ApplicationPoll(void)
{

#ifdef WATCHDOG_ENABLED
  ZW_WatchDogKick();
#endif

  TaskApplicationPoll();
}


/**
 * @brief See description for function prototype in ZW_TransportEndpoint.h.
 */
received_frame_status_t
Transport_ApplicationCommandHandlerEx(
  RECEIVE_OPTIONS_TYPE_EX *rxOpt,
  ZW_APPLICATION_TX_BUFFER *pCmd,
  BYTE cmdLength)
{
  received_frame_status_t frame_status = RECEIVED_FRAME_STATUS_NO_SUPPORT;
  ZW_DEBUG_DOORLOCK_SEND_NL();
  ZW_DEBUG_DOORLOCK_SEND_STR("TAppH");
  ZW_DEBUG_DOORLOCK_SEND_NUM(pCmd->ZW_Common.cmdClass);

  /* Call command class handlers */
  switch (pCmd->ZW_Common.cmdClass)
  {
    case COMMAND_CLASS_VERSION:
      frame_status = handleCommandClassVersion(rxOpt, pCmd, cmdLength);
      break;

#ifdef BOOTLOADER_ENABLED
    case COMMAND_CLASS_FIRMWARE_UPDATE_MD_V2:
      frame_status = handleCommandClassFWUpdate(rxOpt, pCmd, cmdLength);
      break;
#endif


    case COMMAND_CLASS_ASSOCIATION_GRP_INFO:
      frame_status = handleCommandClassAssociationGroupInfo( rxOpt, pCmd, cmdLength);
      break;

    case COMMAND_CLASS_ASSOCIATION:
			frame_status = handleCommandClassAssociation(rxOpt, pCmd, cmdLength);
      break;

    case COMMAND_CLASS_POWERLEVEL:
      frame_status = handleCommandClassPowerLevel(rxOpt, pCmd, cmdLength);
      break;

    case COMMAND_CLASS_MANUFACTURER_SPECIFIC:
      frame_status = handleCommandClassManufacturerSpecific(rxOpt, pCmd, cmdLength);
      break;

    case COMMAND_CLASS_ZWAVEPLUS_INFO:
      frame_status = handleCommandClassZWavePlusInfo(rxOpt, pCmd, cmdLength);
      break;

    case COMMAND_CLASS_BATTERY:
      frame_status = handleCommandClassBattery(rxOpt, pCmd, cmdLength);
      break;

    case COMMAND_CLASS_USER_CODE:
      frame_status = handleCommandClassUserCode(rxOpt, pCmd, cmdLength);
      break;

    case COMMAND_CLASS_DOOR_LOCK_V2:
      frame_status = handleCommandClassDoorLock(rxOpt, pCmd, cmdLength);
      break;

    case COMMAND_CLASS_SUPERVISION:
      frame_status = handleCommandClassSupervision(rxOpt, pCmd, cmdLength);
      break;

    case COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V2:
      frame_status = handleCommandClassMultiChannelAssociation(rxOpt, pCmd, cmdLength);
      break;
  }
  return frame_status;
}

/**
 * @brief See description for function prototype in CommandClassVersion.h.
 */
BYTE
handleCommandClassVersionAppl( BYTE cmdClass )
{
  BYTE commandClassVersion = UNKNOWN_VERSION;

  switch (cmdClass)
  {
    case COMMAND_CLASS_VERSION:
     commandClassVersion = CommandClassVersionVersionGet();
      break;

#ifdef BOOTLOADER_ENABLED
    case COMMAND_CLASS_FIRMWARE_UPDATE_MD:
      commandClassVersion = CommandClassFirmwareUpdateMdVersionGet();
      break;
#endif

    case COMMAND_CLASS_POWERLEVEL:
     commandClassVersion = CommandClassPowerLevelVersionGet();
      break;

    case COMMAND_CLASS_MANUFACTURER_SPECIFIC:
     commandClassVersion = CommandClassManufacturerVersionGet();
      break;

    case COMMAND_CLASS_ASSOCIATION:
     commandClassVersion = CommandClassAssociationVersionGet();
      break;

    case COMMAND_CLASS_ASSOCIATION_GRP_INFO:
     commandClassVersion = CommandClassAssociationGroupInfoVersionGet();
      break;

    case COMMAND_CLASS_DEVICE_RESET_LOCALLY:
     commandClassVersion = CommandClassDeviceResetLocallyVersionGet();
      break;

    case COMMAND_CLASS_ZWAVEPLUS_INFO:
     commandClassVersion = CommandClassZWavePlusVersion();
      break;

    case COMMAND_CLASS_DOOR_LOCK_V2:
     commandClassVersion = CommandClassDoorLockVersionGet();
      break;

    case COMMAND_CLASS_BATTERY:
     commandClassVersion = CommandClassBatteryVersionGet();
      break;

    case COMMAND_CLASS_USER_CODE:
     commandClassVersion = CommandClassUserCodeVersionGet();
      break;

    case COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V2:
      commandClassVersion = CmdClassMultiChannelAssociationVersion();
      break;

    case COMMAND_CLASS_SUPERVISION:
      commandClassVersion = CommandClassSupervisionVersionGet();
      break;

    default:
     commandClassVersion = ZW_Transport_CommandClassVersionGet(cmdClass);
  }
  return commandClassVersion;
}


/**
 * @brief See description for function prototype in ZW_slave_api.h.
 */
void
ApplicationSlaveUpdate(
  BYTE bStatus,
  BYTE bNodeID,
  BYTE* pCmd,
  BYTE bLen)
{
  UNUSED(bStatus);
  UNUSED(bNodeID);
  UNUSED(pCmd);
  UNUSED(bLen);
  ZCB_SetPowerDownTimeoutWakeUpStateCheck(SEC_2_POWERDOWNTIMEOUT);
}


/**
 * @brief See description for function prototype in slave_learn.h.
 */
void
LearnCompleted(BYTE bNodeID)
{
  /*If bNodeID= 0xff.. learn mode failed*/
  if(bNodeID != NODE_BROADCAST)
  {
    /*Success*/
    myNodeID = bNodeID;
    if (myNodeID == 0)
    {
      /*Clear association*/
      AssociationInit(TRUE);
      DefaultApplicationsSettings();
      ActivateBattNotificationTrigger();
    }
    else{
      /* We are included! Inform controller device battery status by clearing flag*/
      ActivateBattNotificationTrigger();
    }
  }
  ZCB_EventSchedulerEventAdd((EVENT_APP) EVENT_SYSTEM_LEARNMODE_FINISH);
  Transport_OnLearnCompleted(bNodeID);
}




/**
 * @brief See description for function prototype in misc.h.
 */
BYTE
GetMyNodeID(void)
{
	return myNodeID;
}


/**
 * @brief Returns the current state of the application state machine.
 * @return Current state
 */
STATE_APP
GetAppState(void)
{
  return currentState;
}


/**
 * @brief The core state machine of this sample application.
 * @param event The event that triggered the call of AppStateManager.
 */
void
AppStateManager( EVENT_APP event)
{
   ZW_DEBUG_DOORLOCK_SEND_STR("AppStateMan event ");
   ZW_DEBUG_DOORLOCK_SEND_NUM(event);
   ZW_DEBUG_DOORLOCK_SEND_STR(" st ");
   ZW_DEBUG_DOORLOCK_SEND_NUM(currentState);
   ZW_DEBUG_DOORLOCK_SEND_NL();

   if(EVENT_SYSTEM_WATCHDOG_RESET == event)
   {
     /*Force state change to activate watchdog-reset without taking care of current
       state.*/
     ChangeState(STATE_APP_WATCHDOG_RESET);
   }

  switch(currentState)
  {
    case STATE_APP_STARTUP:
      ChangeState(STATE_APP_IDLE);
      ZCB_EventSchedulerEventAdd(EVENT_APP_REFRESH_MMI);
      break;

    case STATE_APP_IDLE:
      if(EVENT_APP_REFRESH_MMI == event)
      {
        SetCondition();
        break;
      }

      if ((EVENT_KEY_B1_TRIPLE_PRESS == event) ||(EVENT_SYSTEM_LEARNMODE_START == event))
      {
        ZCB_SetPowerDownTimeout(LEARNMODE_POWERDOWNTIMEOUT);
        if (myNodeID){
          ZW_DEBUG_DOORLOCK_SEND_STR("LEARN_MODE_EXCLUSION");
          StartLearnModeNow(LEARN_MODE_EXCLUSION);
        }
        else{
          ZW_DEBUG_DOORLOCK_SEND_STR("LEARN_MODE_INCLUSION");
          StartLearnModeNow(LEARN_MODE_INCLUSION);
        }
        ChangeState(STATE_APP_LEARN_MODE);
        break;
      }

      if((EVENT_KEY_B1_HELD_10_SEC == event) || (EVENT_SYSTEM_RESET ==event))
      {
        AGI_PROFILE lifelineProfile = {ASSOCIATION_GROUP_INFO_REPORT_PROFILE_GENERAL_NA_V2, ASSOCIATION_GROUP_INFO_REPORT_PROFILE_GENERAL_LIFELINE};
        handleCommandClassDeviceResetLocally(&lifelineProfile, ZCB_DeviceResetLocallyDone);
        break;
      }

      if(EVENT_KEY_B2_DOWN == event)
      {
        myDoorLock.outsideDoorHandleMode |= 0x01;
        SetCondition();
        ZW_DEBUG_DOORLOCK_SEND_STR("outsideDoorHandleMode ");
        ZW_DEBUG_DOORLOCK_SEND_NUM((0x01 & myDoorLock.outsideDoorHandleMode));
        ZW_DEBUG_DOORLOCK_SEND_NL();
        SaveStatus();
        break;
      }

      if(EVENT_KEY_B2_UP == event)
      {
        myDoorLock.outsideDoorHandleMode &= 0x0E;
        SetCondition();
        ZW_DEBUG_DOORLOCK_SEND_STR("outsideDoorHandleMode ");
        ZW_DEBUG_DOORLOCK_SEND_NUM((0x01 & myDoorLock.outsideDoorHandleMode));
        ZW_DEBUG_DOORLOCK_SEND_NL();
        SaveStatus();
      }

      if(EVENT_KEY_B4_UP == event)
      {
        ChangeState(STATE_APP_USER_KEYPAD);
        ZCB_EventSchedulerEventAdd(EVENT_APP_START_KEYPAD_ACTIVE);
      }


      if(EVENT_SYSTEM_OTA_START == event)
      {
        ZCB_StopPowerDownTimer();
        ChangeState(STATE_APP_OTA);
      }

      if(EVENT_APP_DOORLOCK_JOB == event)
      {
        ChangeState(STATE_APP_TRANSMIT_DATA);
        ZCB_EventSchedulerEventAdd(EVENT_APP_NEXT_EVENT_JOB);
        ZCB_EventEnqueue(EVENT_APP_DOORLOCK_JOB);
      }

      break;

    case STATE_APP_LEARN_MODE:
      if(EVENT_APP_REFRESH_MMI == event)
      {
        SetCondition();
      }

      if((EVENT_KEY_B1_TRIPLE_PRESS == event)||(EVENT_SYSTEM_LEARNMODE_END == event))
      {
        ZW_DEBUG_DOORLOCK_SEND_STR("\r\n STATE_APP_LEARN_MODE disable");
        StartLearnModeNow(LEARN_MODE_DISABLE);
        ChangeState(STATE_APP_IDLE);
      }
      if(EVENT_SYSTEM_LEARNMODE_FINISH == event)
      {
        ZW_DEBUG_DOORLOCK_SEND_STR("\r\n STATE_APP_LEARN_MODE finish");
        ChangeState(STATE_APP_IDLE);
      }
      break;

    case STATE_APP_WATCHDOG_RESET:
      if(EVENT_APP_REFRESH_MMI == event){}

      ZW_WatchDogEnable(); /*reset asic*/
      for (;;) {}
      break;
    case STATE_APP_OTA:
      if(EVENT_APP_REFRESH_MMI == event){}
      /*OTA state... do nothing until firmware update is finish*/
      break;

    case STATE_APP_POWERDOWN:
      /* Device is powering down.. wait!*/
      break;

    case STATE_APP_TRANSMIT_DATA:
      if(EVENT_APP_REFRESH_MMI == event)
      {
        SetCondition();
        break;
      }

      if(EVENT_APP_NEXT_EVENT_JOB == event)
      {
        BYTE event;
        /*check job-queue*/
        if(TRUE == ZCB_EventDequeue(&event))
        {
          /*Let the event scheduler fire the event on state event machine*/
          ZCB_EventSchedulerEventAdd(event);
        }
        else{
          ZCB_EventSchedulerEventAdd(EVENT_APP_FINISH_EVENT_JOB);
        }
      }

      if(EVENT_APP_DOORLOCK_JOB == event)
      {
        if(JOB_STATUS_SUCCESS != DoorLockOperationSupportReport(&agiTableRootDeviceGroups[0].profile))
        {
          ZCB_EventSchedulerEventAdd(EVENT_APP_NEXT_EVENT_JOB);
        }
      }

      if(EVENT_APP_BATT_LOW == event)
      {
        if (JOB_STATUS_SUCCESS != SendBattReport( ZCB_BattReportSentDone ))
        {
          ActivateBattNotificationTrigger();
          ZCB_EventSchedulerEventAdd(EVENT_APP_NEXT_EVENT_JOB);
        }
      }

      if(EVENT_APP_FINISH_EVENT_JOB == event)
      {
        ChangeState(STATE_APP_IDLE);
      }
      break;

#ifdef TEST_INTERFACE_SUPPORT
      case STATE_APP_USER_KEYPAD:
      if(EVENT_APP_REFRESH_MMI == event)
      {
        SetCondition();
        Led(ZDP03A_LED_D3, ON);
        break;
      }
      if(EVENT_APP_START_KEYPAD_ACTIVE == event)
      {
        StartKeypadApp();
      }
      if(EVENT_APP_FINISH_KEYPAD_ACTIVE == event)
      {
        ChangeState(STATE_APP_IDLE);
      }
      break;
#endif
  }
}

/**
 * @brief Sets the current state to a new, given state.
 * @param newState New state.
 */
static void
ChangeState(STATE_APP newState)
{
 ZW_DEBUG_DOORLOCK_SEND_STR("ChangeState st = ");
 ZW_DEBUG_DOORLOCK_SEND_NUM(currentState);
 ZW_DEBUG_DOORLOCK_SEND_STR(" -> new st = ");
 ZW_DEBUG_DOORLOCK_SEND_NUM(newState);
 ZW_DEBUG_DOORLOCK_SEND_NL();

  currentState = newState;

  /**< Pre-action on new state is to refresh MMI */
  ZCB_EventSchedulerEventAdd(EVENT_APP_REFRESH_MMI);
}

/**
 * @brief Transmission callback for Device Reset Locally call.
 * @param pTransmissionResult Result of each transmission.
 */
PCB(ZCB_DeviceResetLocallyDone)(TRANSMISSION_RESULT * pTransmissionResult)
{
  ZW_DEBUG_DOORLOCK_SEND_BYTE('d');
  ZW_DEBUG_DOORLOCK_SEND_NUM(pTransmissionResult->status);
  if(TRANSMISSION_RESULT_FINISHED == pTransmissionResult->isFinished)
  {
    /* CHANGE THIS - clean your own application data from NVM*/
    ZW_MEM_PUT_BYTE((WORD)&EEOFFSET_MAGIC_far, 1 + APPL_MAGIC_VALUE);
    ZCB_EventSchedulerEventAdd((EVENT_APP) EVENT_SYSTEM_WATCHDOG_RESET);
  }
}


#ifdef BOOTLOADER_ENABLED
/**
 * @brief Called when OTA firmware upgrade is finished. Reboots node to cleanup
 * and starts on new FW.
 * @param OTA_STATUS otaStatus
 */
PCB(ZCB_OTAFinish)(OTA_STATUS otaStatus)
{
  UNUSED(otaStatus);
  /*Just reboot node to cleanup and start on new FW.*/
  ZW_WatchDogEnable(); /*reset asic*/
  while(1);
}

/**
 * @brief Function pointer for KEIL.
 */
code const BOOL (code * ZCB_OTAStart_p)(void) = &ZCB_OTAStart;
/**
 * @brief Called before OTA firmware upgrade starts.
 * @details Checks whether the application is ready for a firmware upgrade.
 * @return FALSE if OTA should be rejected, otherwise TRUE.
 */
BOOL
ZCB_OTAStart(void)
{
  BOOL  status = FALSE;
  if (STATE_APP_IDLE == GetAppState())
  {
    ZCB_EventSchedulerEventAdd((EVENT_APP) EVENT_SYSTEM_OTA_START);
    status = TRUE;
  }
  return status;
}
#endif

/**
 * @brief See description for function prototype in CommandClassVersion.h.
 */
BYTE
handleNbrFirmwareVersions(void)
{
  return 1; /*CHANGE THIS - firmware 0 version*/
}


/**
 * @brief See description for function prototype in CommandClassVersion.h.
 */
void
handleGetFirmwareVersion(
  BYTE bFirmwareNumber,
  VG_VERSION_REPORT_V2_VG *pVariantgroup)
{
  /*firmware 0 version and sub version*/
  if(bFirmwareNumber == 0)
  {
    pVariantgroup->firmwareVersion = APP_VERSION;
    pVariantgroup->firmwareSubVersion = APP_REVISION;
  }
  else
  {
    /*Just set it to 0 if firmware n is not present*/
    pVariantgroup->firmwareVersion = 0;
    pVariantgroup->firmwareSubVersion = 0;
  }
}


/**
 * @brief Function return firmware Id of target n (0 => is device FW ID)
 * n read version of firmware number n (0,1..N-1)
 * @return firmaware ID.
 */
WORD
handleFirmWareIdGet( BYTE n)
{
  if(n == 0)
  {
    return APP_FIRMWARE_ID;
  }
  return 0;
}


/**
 * @brief Send a Doorlock operation-support report
 * @param pAgiProfile Pointer to AGI profile
 * @return jo status
 */
JOB_STATUS
DoorLockOperationSupportReport(AGI_PROFILE* pAgiProfile)
{
  CMD_CLASS_DOOR_LOCK_OPERATION_REPORT operation;

  if(0 == (0x02 & myDoorLock.condition))
  {
    /* if bolt lock -> mode is DOOR_MODE_SECURED*/
    operation.mode = DOOR_MODE_SECURED;
  }
  else
  {
    operation.mode = DOOR_MODE_UNSEC;
  }
  operation.insideDoorHandleMode = myDoorLock.insideDoorHandleMode;
  operation.outsideDoorHandleMode = myDoorLock.outsideDoorHandleMode;
  operation.condition = myDoorLock.condition;
  operation.lockTimeoutMin = myDoorLock.lockTimeoutMin;
  operation.lockTimeoutSec = myDoorLock.lockTimeoutSec;
  return CmdClassDoorLockOperationSupportReport( pAgiProfile, ENDPOINT_ROOT, &operation, ZCB_JobStatus);
}

/**
 * @brief Callback function used in CmdClassDoorLockOperationSupportReport.
 * @param pTransmissionResult Result of each transmission.
 */
PCB(ZCB_JobStatus)(TRANSMISSION_RESULT * pTransmissionResult)
{
  if (TRANSMISSION_RESULT_FINISHED == pTransmissionResult->isFinished)
  {
    ZCB_EventSchedulerEventAdd(EVENT_APP_NEXT_EVENT_JOB);
  }
}

/**
 * @brief validate a code agains idenfier usercode
 * @param[in] identifier user ID
 * @param[in] pCode pointer to pin code for validation agains pin code in NVM
 * @param[in] len length of the pin code
 */
BOOL
ValidateUserCode( BYTE identifier, BYTE* pCode, BYTE len)
{
  BYTE userCode[USERCODE_MAX_LEN];
  USER_ID_STATUS user_id_status = MemoryGetByte((WORD) &EEOFFSET_USERSTATUS_far[identifier - 1]);

  if( (len == MemoryGetByte((WORD)&EEOFFSET_USERCODE_LEN_far[identifier - 1])) &&
      ((USER_ID_OCCUPIED == user_id_status) || (USER_ID_RESERVED == user_id_status)))
  {
    MemoryGetBuffer((WORD) &EEOFFSET_USERCODE_far[identifier - 1], userCode, len);

    if(0 == ZW_memcmp(pCode, userCode, len))
    {
     return TRUE;
    }
  }
  return FALSE;
}

/**
 * @brief See description for function prototype in CommandclassUserCode.h
 */
void
handleCommandClassUserCodeSet(
  BYTE identifier,
  USER_ID_STATUS id,
  BYTE* pUserCode,
  BYTE len,
  BYTE endpoint )
{
  BYTE i;
  UNUSED(endpoint);

  /* If identifier == 0 -> set all user codes! */
  identifier = 01; /* We only have one for DoorLock! */
  if( identifier <= USER_ID_MAX)
  {
    MemoryPutByte((WORD)&EEOFFSET_USERSTATUS_far[identifier - 1], id);
    MemoryPutBuffer((WORD) &EEOFFSET_USERCODE_far[identifier - 1], pUserCode, len, NULL);
    MemoryPutByte((WORD)&EEOFFSET_USERCODE_LEN_far[identifier - 1], len);

    for(i = 0; i < len; i++){
      ZW_DEBUG_DOORLOCK_SEND_NUM(*(pUserCode+i));
    }
    ZW_DEBUG_DOORLOCK_SEND_NL();
  }
}


/**
 * @brief See description for function prototype in CommandclassUserCode.h
 */
BOOL
handleCommandClassUserCodeIdGet(
  BYTE identifier,
  USER_ID_STATUS* pId,
  BYTE endpoint)
{
  UNUSED(endpoint);
  if( identifier <= USER_ID_MAX)
  {
    *pId = (USER_ID_STATUS)MemoryGetByte((WORD) &EEOFFSET_USERSTATUS_far[identifier - 1]);
    return TRUE;
  }
  return FALSE;
}


/**
 * @brief See description for function prototype in CommandclassUserCode.h
 */
BOOL
handleCommandClassUserCodeReport(
  BYTE identifier,
  BYTE* pUserCode,
  BYTE* pLen,
  BYTE endpoint)
{
  BYTE i;
  UNUSED(endpoint);
  *pLen = MemoryGetByte((WORD)&EEOFFSET_USERCODE_LEN_far[identifier - 1]);
  if((identifier <= USER_ID_MAX) && (USERCODE_MAX_LEN >= *pLen))
  {
    MemoryGetBuffer((WORD) &EEOFFSET_USERCODE_far[identifier - 1], pUserCode, *pLen );

    ZW_DEBUG_DOORLOCK_SEND_STR("hCmdUC_Report = ");
    for(i = 0; i < *pLen; i++){
      ZW_DEBUG_DOORLOCK_SEND_NUM(*(pUserCode+i));
    }
    ZW_DEBUG_DOORLOCK_SEND_NL();
    return TRUE;
  }
  return FALSE;
}

/**
 * @brief See description for function prototype in CommandclassUserCode.h
 */
BYTE
handleCommandClassUserCodeUsersNumberReport(BYTE endpoint)
{
  UNUSED(endpoint);
  return USER_ID_MAX;
}


/**
 * @brief See description for function prototype in CommandclassDoorLock.h
 */
void
handleCommandClassDoorLockOperationSet(DOOR_MODE mode)
{
  ZW_DEBUG_DOORLOCK_SEND_STR("OperationSet ");
  ZW_DEBUG_DOORLOCK_SEND_NUM(mode);
  ZW_DEBUG_DOORLOCK_SEND_NL();


  if(DOOR_MODE_SECURED == mode)
  {
    /* lock bolt*/
    myDoorLock.condition &= 0xFD;
    /* disable handle*/
    myDoorLock.outsideDoorHandleState &= 0xFE;

  }
  else if(DOOR_MODE_UNSEC == mode)
  {
    /* unlock bolt*/
    myDoorLock.condition |= 0x02;
    /*enable door handle*/
    myDoorLock.outsideDoorHandleState |= 0x01;
  }

  /*Update condition and LED's*/
  SetCondition();

  SaveStatus();
}


/**
 * @brief See description for function prototype in CommandclassDoorLock.h
 */
void
handleCommandClassDoorLockOperationReport(CMD_CLASS_DOOR_LOCK_OPERATION_REPORT* pData)
{
  ZW_DEBUG_DOORLOCK_SEND_STR("OperationReport");
  ZW_DEBUG_DOORLOCK_SEND_NL();

  if(0 == (0x02 & myDoorLock.condition))
  {
    /* if bolt lock -> mode is DOOR_MODE_SECURED*/
    pData->mode = DOOR_MODE_SECURED;
  }
  else
  {
    pData->mode = DOOR_MODE_UNSEC;
  }

  pData->insideDoorHandleMode = myDoorLock.insideDoorHandleMode;
  pData->outsideDoorHandleMode = myDoorLock.outsideDoorHandleMode;
  pData->condition = myDoorLock.condition;
  pData->lockTimeoutMin = 0xFE; //myDoorLock.lockTimeoutMin;
  pData->lockTimeoutSec = 0xFE; //myDoorLock.lockTimeoutSec;
}


/**
 * @brief See description for function prototype in CommandclassDoorLock.h
 */
void
handleCommandClassDoorLockConfigurationSet(CMD_CLASS_DOOR_LOCK_CONFIGURATION* pData)
{
  /*Sample app do only support DOOR_OPERATION_CONST (1) mode!*/
  if(DOOR_OPERATION_CONST == pData->type)
  {
    myDoorLock.insideDoorHandleState = pData->insideDoorHandleState;
    myDoorLock.outsideDoorHandleState = pData->outsideDoorHandleState;
  }
  //myDoorLock.type = pData->type;
  //myDoorLock.lockTimeoutMin = pData->lockTimeoutMin;
  //myDoorLock.lockTimeoutSec = pData->lockTimeoutSec;

  SaveStatus();
}


/**
 * @brief See description for function prototype in CommandclassDoorLock.h
 */
void
handleCommandClassDoorLockConfigurationReport(CMD_CLASS_DOOR_LOCK_CONFIGURATION* pData)
{
  ZW_DEBUG_DOORLOCK_SEND_STR("ConfigReport");
  ZW_DEBUG_DOORLOCK_SEND_NL();

  pData->type   = myDoorLock.type;
  pData->insideDoorHandleState = myDoorLock.insideDoorHandleState;
  pData->outsideDoorHandleState = myDoorLock.outsideDoorHandleState;
  pData->lockTimeoutMin = 0xFE; //myDoorLock.lockTimeoutMin;
  pData->lockTimeoutSec = 0xFE; //myDoorLock.lockTimeoutSec;
}



/**
 * @brief Set lock conditons (LED) out from door handle mode state
 */
void
SetCondition( void )
{
  if((0x01 == (myDoorLock.outsideDoorHandleMode & 0x01)) && ((myDoorLock.outsideDoorHandleState &0x01) == 0x01))
  {
    myDoorLock.condition &=  0xFB;     /* 0 = Latch Open; 1 = Latch Closed*/
  }
  else
  {
    myDoorLock.condition |=  0x04;     /* 0 = Latch Open; 1 = Latch Closed*/
  }

  ZW_DEBUG_DOORLOCK_SEND_STR("SetCondition con = ");
  ZW_DEBUG_DOORLOCK_SEND_NUM(myDoorLock.condition );
  ZW_DEBUG_DOORLOCK_SEND_NL();

  if(0x4 & myDoorLock.condition)  /*Latch close*/
  {
    Led(ZDP03A_LED_D1,ON);//PIN_OFF(LED1);
  }
  else
  {                               /*Latch open*/
    Led(ZDP03A_LED_D1,OFF); //PIN_ON(LED1);
  }

  if(0x2 & myDoorLock.condition)
  {
    Led(ZDP03A_LED_D2,OFF); //PIN_ON(LED2);                /*Bolt unlocked*/
  }
  else
  {
    Led(ZDP03A_LED_D2,ON); //PIN_OFF(LED2);               /*Bolt locked*/
  }
}


/**
 * @brief Callback function used when sending battery report.
 */
PCB(ZCB_BattReportSentDone)(TRANSMISSION_RESULT * pTransmissionResult)
{
  if (TRANSMIT_COMPLETE_OK != pTransmissionResult->status)
  {
    ActivateBattNotificationTrigger();
  }
  if (TRANSMISSION_RESULT_FINISHED == pTransmissionResult->isFinished)
  {
    ZCB_EventSchedulerEventAdd(EVENT_APP_NEXT_EVENT_JOB);
  }
}


/**
 * @brief Returns whether the application is ready to power down.
 * @return TRUE if ready to power down, FALSE otherwise.
 */
BYTE AppPowerDownReady(void)
{
  BYTE status = FALSE;

//  ZW_DEBUG_DOORLOCK_SEND_NL();
//  ZW_DEBUG_DOORLOCK_SEND_STR("AppPowerDownReady");
  ZW_DEBUG_DOORLOCK_SEND_BYTE('.');

  ZW_DEBUG_DOORLOCK_SEND_NUM(currentState);

  if (STATE_APP_IDLE == GetAppState())
  {
    ZW_DEBUG_DOORLOCK_SEND_BYTE('a');
    /*Check battery before shut down*/
    if (TRUE == TimeToSendBattReport())
    {
      ZW_DEBUG_DOORLOCK_SEND_BYTE('b');
      ChangeState(STATE_APP_TRANSMIT_DATA);

      if(FALSE == ZCB_EventSchedulerEventAdd(EVENT_APP_NEXT_EVENT_JOB))
      {
        ZW_DEBUG_DOORLOCK_SEND_STR("** EVENT_APP_NEXT_EVENT_JOB fail");
        ZW_DEBUG_DOORLOCK_SEND_NL();
      }
      /*Add event's on job-queue*/
      ZCB_EventEnqueue(EVENT_APP_BATT_LOW);

      /*Not ready to power of*/
      status = FALSE;
    }
    else
    {
      ZW_DEBUG_DOORLOCK_SEND_BYTE('c');
      Led(ZDP03A_LED_D3, OFF);
      status = TRUE;
    }
  }
  ZW_DEBUG_DOORLOCK_SEND_NUM(status);
  return status;
}

/**
 * @brief Stores the current status of the lock on/off
 * in the application part of the EEPROM.
 */
static void SaveStatus(void)
{
  ZW_DEBUG_DOORLOCK_SEND_STR("SaveStatus");
  ZW_DEBUG_DOORLOCK_SEND_NL();
  MemoryPutByte((WORD)&EEOFFSET_OPERATION_TYPE_far, myDoorLock.type);
  MemoryPutByte((WORD)&EEOFFSET_DOOR_LOCK_CONDITION_far, myDoorLock.condition);
  MemoryPutByte((WORD)&EEOFFSET_HANDLES_IN_OUT_SITE_DOOR_HANDLES_STATE_far,
                (myDoorLock.insideDoorHandleState) | (myDoorLock.outsideDoorHandleState << 4));
  MemoryPutByte((WORD)&EEOFFSET_LOCK_MINUTES_far, myDoorLock.lockTimeoutMin);
  MemoryPutByte((WORD)&EEOFFSET_LOCK_SECONDS_far, myDoorLock.lockTimeoutSec);
  MemoryPutByte((WORD)&EEOFFSET_MAGIC_far, APPL_MAGIC_VALUE);
}

/**
 * @brief Handles a received Supervision Get command.
 * @details The purpose of Supervision is to inform the source node (controller) when the door lock
 * operation is finished. This sample application runs on a ZDP03A board and therefore has no
 * door lock hardware, but to show how Supervision can be used, a timer is implemented. This timer
 * represents the physical bolt of a door lock.
 * The first Supervision report will be transmitted automatically by the Framework, but transmission
 * of the next report(s) need(s) to be handled by the application.
 */
PCB(ZCB_CommandClassSupervisionGetReceived)(SUPERVISION_GET_RECEIVED_HANDLER_ARGS * pArgs)
{
  if (DOOR_LOCK_OPERATION_SET_V2 == pArgs->cmd && COMMAND_CLASS_DOOR_LOCK_V2 == pArgs->cmdClass)
  {
    /* Status for DOOR_LOCK_OPERATION_SET_V2 */
    pArgs->status = CC_SUPERVISION_STATUS_WORKING;
    pArgs->duration = 2;

    // Save the data
    rxOptionSupervision = *(pArgs->rxOpt);
    sessionID = CC_SUPERVISION_EXTRACT_SESSION_ID(pArgs->properties1);

    if(CC_SUPERVISION_STATUS_UPDATES_SUPPORTED == CC_SUPERVISION_EXTRACT_STATUS_UPDATE(pArgs->properties1))
    {
      pArgs->properties1 = CC_SUPERVISION_ADD_MORE_STATUS_UPDATE(CC_SUPERVISION_MORE_STATUS_UPDATES_REPORTS_TO_FOLLOW) | CC_SUPERVISION_ADD_SESSION_ID(sessionID);
      // Start timer which will send another Supervision report when triggered.
      TimerStart(ZCB_SupervisionTimerCallback, 2000/10, 1);
    }
  }
  else
  {
    /* Status for all other commands */
    pArgs->properties1 = CC_SUPERVISION_ADD_MORE_STATUS_UPDATE(CC_SUPERVISION_MORE_STATUS_UPDATES_THIS_IS_LAST) | CC_SUPERVISION_ADD_SESSION_ID(sessionID);
    pArgs->status = CC_SUPERVISION_STATUS_SUCCESS;
    pArgs->duration = 0;
  }
}

/**
 * @brief Transmits a Supervision report.
 * @details This function is triggered by the timer set in the Supervision Get handler.
 */
PCB(ZCB_SupervisionTimerCallback)(void)
{
  TRANSMIT_OPTIONS_TYPE_SINGLE_EX * pTxOptions;
  RxToTxOptions(&rxOptionSupervision, &pTxOptions);
  CmdClassSupervisionReportSend(
      pTxOptions,
      CC_SUPERVISION_ADD_MORE_STATUS_UPDATE(CC_SUPERVISION_MORE_STATUS_UPDATES_THIS_IS_LAST) | CC_SUPERVISION_ADD_SESSION_ID(sessionID),
      CC_SUPERVISION_STATUS_SUCCESS,
      0,
      NULL);
}


/*
 * @brief Called when protocol needs to inform Application about a Security Event.
 * @details If the app does not need/want the Security Event the handling can be left empty.
 *
 *    Event E_APPLICATION_SECURITY_EVENT_S2_INCLUSION_REQUEST_DSK_CSA
 *          If CSA is needed, the app must do the following when this event occures:
 *             1. Obtain user input with first 4 bytes of the S2 including node DSK
 *             2. Store the user input in a response variable of type s_SecurityS2InclusionCSAPublicDSK_t.
 *             3. Call ZW_SetSecurityS2InclusionPublicDSK_CSA(response)
 *
 */
void
ApplicationSecurityEvent(
  s_application_security_event_data_t *securityEvent)
{
  switch (securityEvent->event)
  {
#ifdef APP_SUPPORTS_CLIENT_SIDE_AUTHENTICATION
    case E_APPLICATION_SECURITY_EVENT_S2_INCLUSION_REQUEST_DSK_CSA:
      {
#ifdef TEST_INTERFACE_SUPPORT
        ti_csa_prompt();
#else /* TEST_INTERFACE_SUPPORT */
        ZW_SetSecurityS2InclusionPublicDSK_CSA(&sCSAResponse);
#endif /* TEST_INTERFACE_SUPPORT */
      }
      break;
#endif /* APP_SUPPORTS_CLIENT_SIDE_AUTHENTICATION */

    default:
      break;
  }
}

#ifdef TEST_INTERFACE_SUPPORT

void
StartKeypadApp(void)
{
  ZW_DEBUG_DOORLOCK_SEND_STR("\r\nStartKeypadApp");
  ZW_test_interface_allocate('c', ZCB_AppKeypad);
  ZW_UART0_tx_send_str("\r\nEnter code 'c xx..x'\r\n");
  ZW_UART0_tx_send_byte('>');
}


PCB(ZCB_AppKeypad)(BYTE channel, BYTE * pString)
{
  BOOL valid = FALSE;
  BYTE stringLength = ZW_strlen(pString);
  UNUSED(channel);
  ZW_DEBUG_DOORLOCK_SEND_STR("\r\nZCB_AppKeypad");
  ZW_DEBUG_DOORLOCK_SEND_NUM(stringLength);

  ZCB_EventSchedulerEventAdd(EVENT_APP_FINISH_KEYPAD_ACTIVE);

  //Validate string length [4,10]
  if((USERCODE_MIN_LEN <= stringLength) && (USERCODE_MAX_LEN >= stringLength))
  {
    BYTE i;

    for(i = 1; i <= USER_ID_MAX; i++ )
    {
      if(TRUE == ValidateUserCode(i, pString, stringLength))
      {
        /*Correct key!*/
        ZW_DEBUG_DOORLOCK_SEND_STR("key valid ");
        if((myDoorLock.condition & 0x02) == 0x02)
        {
          /*lock*/
          myDoorLock.condition &= 0xFD;
        }
        else
        {
          /*unlock*/
          myDoorLock.condition |= 0x02;
        }
        /* Save new status in EEPROM */
        SaveStatus();
        valid = TRUE;
        ZCB_EventSchedulerEventAdd(EVENT_APP_DOORLOCK_JOB);
        break;
      }
    }
  }

  if(FALSE == valid)
  {
    ZW_UART0_tx_send_str("\r\nInvalid key!\r\n");
  }
  else{
    ZW_UART0_tx_send_str("\r\nValid key!\r\n");
  }
  Led(ZDP03A_LED_D3, OFF);
  ZW_DEBUG_DOORLOCK_SEND_NL();
}

#endif



void DefaultApplicationsSettings(void)
{
  BYTE i,j;
  BYTE defaultUserCode[] = DEFAULT_USERCODE;

  ZW_DEBUG_DOORLOCK_SEND_STR("\r\n DefaultApplicationsSettings");

  /* Its alive */
  myDoorLock.type = DOOR_OPERATION_CONST;
  myDoorLock.insideDoorHandleState = 0xf;/*all handles active*/
  myDoorLock.outsideDoorHandleState = 0xf;/*all handles active*/
  myDoorLock.lockTimeoutMin = DOOR_LOCK_OPERATION_SET_TIMEOUT_NOT_SUPPORTED;
  myDoorLock.lockTimeoutSec = DOOR_LOCK_OPERATION_SET_TIMEOUT_NOT_SUPPORTED;
  for (i = 0; i < USER_ID_MAX; i++)
  {
    MemoryPutByte((WORD)&EEOFFSET_USERSTATUS_far[i], USER_ID_OCCUPIED);
    MemoryPutByte((WORD)&EEOFFSET_USERCODE_LEN_far[i], sizeof(defaultUserCode));

    for (j = 0; j < USERCODE_MAX_LEN; j++)
    {
      MemoryPutByte((WORD)&EEOFFSET_USERCODE_far[i][j], defaultUserCode[j]);
    }
  }
  SaveStatus();
}

/**
* Set up security keys to request when joining a network.
* These are taken from the config_app.h header file.
*/
BYTE ApplicationSecureKeysRequested(void)
{
  return REQUESTED_SECURITY_KEYS;
}

/**
* Set up security S2 inclusion authentication to request when joining a network.
* These are taken from the config_app.h header file.
*/
BYTE ApplicationSecureAuthenticationRequested(void)
{
  return REQUESTED_SECURITY_AUTHENTICATION;
}

