/**
 * @file PowerStrip.c
 * @brief Z-Wave Power Strip Sample Application
 * @author Thomas Roll
 * @author Christian Salmony Olsen
 * @copyright Copyright (c) 2001-2015
 * Sigma Designs, Inc.
 * All Rights Reserved
 * @details This sample application mimics a power strip with three outlets.
 * Outlet one and two each act as an on/off switch while outlet 3 is a dimming
 * outlet. S2 and S4 locally controls the outlets respectively.
 *
 * Last changed by: $Author: $
 * Revision:        $Revision: $
 * Last changed:    $Date: $
 */

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
#include <nvm_util.h>

/*IO control*/
#include <io_zdp03a.h>
#include <ZW_task.h>
#include <ev_man.h>
#include <appl_timer_api.h>

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
#include <CommandClassBasic.h>
#include <CommandClassBinarySwitch.h>
#include <CommandClassNotification.h>
#include <CommandClassMultiLevelSwitch.h>
#include <CommandClassSupervision.h>
#include <CommandClassMultiChan.h>
#include <CommandClassMultiChanAssociation.h>
#include <CommandClassSecurity.h>

#include <notification.h>
#include <multilevel_switch.h>
#include <endpoint_lookup.h>



/****************************************************************************/
/*                      PRIVATE TYPES and DEFINITIONS                       */
/****************************************************************************/

/**
 * @def ZW_DEBUG_APP_SEND_BYTE(data)
 * Transmits a given byte to the debug port.
 * @def ZW_DEBUG_APP_SEND_STR(STR)
 * Transmits a given string to the debug port.
 * @def ZW_DEBUG_APP_SEND_NUM(data)
 * Transmits a given number to the debug port.
 * @def ZW_DEBUG_APP_SEND_WORD_NUM(data)
 * Transmits a given WORD number to the debug port.
 * @def ZW_DEBUG_APP_SEND_NL()
 * Transmits a newline to the debug port.
 */
#ifdef ZW_DEBUG_APP
#define ZW_DEBUG_APP_SEND_BYTE(data) ZW_DEBUG_SEND_BYTE(data)
#define ZW_DEBUG_APP_SEND_STR(STR) ZW_DEBUG_SEND_STR(STR)
#define ZW_DEBUG_APP_SEND_NUM(data)  ZW_DEBUG_SEND_NUM(data)
#define ZW_DEBUG_APP_SEND_WORD_NUM(data) ZW_DEBUG_SEND_WORD_NUM(data)
#define ZW_DEBUG_APP_SEND_NL()  ZW_DEBUG_SEND_NL()
#else
#define ZW_DEBUG_APP_SEND_BYTE(data)
#define ZW_DEBUG_APP_SEND_STR(STR)
#define ZW_DEBUG_APP_SEND_NUM(data)
#define ZW_DEBUG_APP_SEND_WORD_NUM(data)
#define ZW_DEBUG_APP_SEND_NL()
#endif

typedef enum _EVENT_APP_
{
  EVENT_EMPTY = DEFINE_EVENT_APP_NBR,
  EVENT_APP_INIT,
  EVENT_APP_REFRESH_MMI,
  EVENT_APP_NEXT_EVENT_JOB,
  EVENT_APP_FINISH_EVENT_JOB,
  EVENT_APP_GET_NODELIST,
  EVENT_APP_SEND_OVERLOAD_NOTIFICATION

} EVENT_APP;

typedef enum _STATE_APP_
{
  STATE_APP_STARTUP,
  STATE_APP_IDLE,
  STATE_APP_LEARN_MODE,
  STATE_APP_WATCHDOG_RESET,
  STATE_APP_OTA,
  STATE_APP_TRANSMIT_DATA
} STATE_APP;

#define LEVEL_MIN           (1)
#define LEVEL_MAX           (99)
#define DIMMER_FREQ         (10)
#define DIM_SPEED           (10)
#define SWITCH_ON           (0x01)
#define SWITCH_DIM_UP       (0x02)
#define SWITCH_IS_DIMMING   (0x04)
#define SWITCH_OFF          (0x00)
#define BIN_SWITCH_1        (ENDPOINT_1 - 1)
#define BIN_SWITCH_2        (ENDPOINT_2 - 1)
#define MULTILEVEL_SWITCH_1 (ENDPOINT_3 - 1)

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
  COMMAND_CLASS_SWITCH_BINARY,
  COMMAND_CLASS_SWITCH_MULTILEVEL,
  COMMAND_CLASS_ASSOCIATION,
  COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V2,
  COMMAND_CLASS_ASSOCIATION_GRP_INFO,
  COMMAND_CLASS_NOTIFICATION_V3,
  COMMAND_CLASS_TRANSPORT_SERVICE_V2,
  COMMAND_CLASS_VERSION,
  COMMAND_CLASS_MANUFACTURER_SPECIFIC,
  COMMAND_CLASS_DEVICE_RESET_LOCALLY,
  COMMAND_CLASS_POWERLEVEL,
  COMMAND_CLASS_SECURITY,
  COMMAND_CLASS_SECURITY_2,
  COMMAND_CLASS_MULTI_CHANNEL_V4,
  COMMAND_CLASS_SUPERVISION
#ifdef BOOTLOADER_ENABLED
  ,COMMAND_CLASS_FIRMWARE_UPDATE_MD_V2
#endif
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
  COMMAND_CLASS_SWITCH_BINARY,
  COMMAND_CLASS_SWITCH_MULTILEVEL,
  COMMAND_CLASS_NOTIFICATION_V3,
  COMMAND_CLASS_ASSOCIATION,
  COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V2,
  COMMAND_CLASS_ASSOCIATION_GRP_INFO,
  COMMAND_CLASS_MULTI_CHANNEL_V3,
  COMMAND_CLASS_MANUFACTURER_SPECIFIC,
  COMMAND_CLASS_DEVICE_RESET_LOCALLY,
  COMMAND_CLASS_POWERLEVEL,
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
static BYTE ep1_naming[] = EP1_LIFELINE_NAME;
static BYTE ep2_naming[] = EP2_LIFELINE_NAME;
static BYTE ep3_naming[] = EP3_LIFELINE_NAME;


static code BYTE ep12_noSec_InclNonSecure[] =
{
  COMMAND_CLASS_ZWAVEPLUS_INFO,
  COMMAND_CLASS_SWITCH_BINARY,
  COMMAND_CLASS_ASSOCIATION,
  COMMAND_CLASS_ASSOCIATION_GRP_INFO,
  COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V2,
  COMMAND_CLASS_NOTIFICATION_V3,
  COMMAND_CLASS_SUPERVISION
};


static code BYTE ep12_noSec_InclSecure[] =
{
    COMMAND_CLASS_ZWAVEPLUS_INFO,
    COMMAND_CLASS_SECURITY,
    COMMAND_CLASS_SECURITY_2
};

static code BYTE ep12_sec_InclSecure[] =
{
  COMMAND_CLASS_SWITCH_BINARY,
  COMMAND_CLASS_ASSOCIATION,
  COMMAND_CLASS_ASSOCIATION_GRP_INFO,
  COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V2,
  COMMAND_CLASS_NOTIFICATION_V3,
  COMMAND_CLASS_SUPERVISION
};


static code BYTE ep3_noSec_InclNonSecure[] =
{
  COMMAND_CLASS_ZWAVEPLUS_INFO,
  COMMAND_CLASS_SWITCH_MULTILEVEL,
  COMMAND_CLASS_ASSOCIATION,
  COMMAND_CLASS_ASSOCIATION_GRP_INFO,
  COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V2,
  COMMAND_CLASS_NOTIFICATION_V3,
  COMMAND_CLASS_SUPERVISION
};


static code BYTE ep3_noSec_InclSecure[] =
{
    COMMAND_CLASS_ZWAVEPLUS_INFO,
    COMMAND_CLASS_SECURITY,
    COMMAND_CLASS_SECURITY_2
};

static code BYTE ep3_sec_InclSecure[] =
{
    COMMAND_CLASS_SWITCH_MULTILEVEL,
    COMMAND_CLASS_ASSOCIATION,
    COMMAND_CLASS_ASSOCIATION_GRP_INFO,
    COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V2,
    COMMAND_CLASS_NOTIFICATION_V3,
    COMMAND_CLASS_SUPERVISION
};


static EP_NIF endpointsNIF[NUMBER_OF_ENDPOINTS] =
{  /*Endpoint 1*/
  { GENERIC_TYPE_SWITCH_BINARY, SPECIFIC_TYPE_POWER_SWITCH_BINARY,
    {
      {ep12_noSec_InclNonSecure, sizeof(ep12_noSec_InclNonSecure)},
      {{ep12_noSec_InclSecure, sizeof(ep12_noSec_InclSecure)}, {ep12_sec_InclSecure, sizeof(ep12_sec_InclSecure)}}
    }
  }, /*Endpoint 2*/
  { GENERIC_TYPE_SWITCH_BINARY, SPECIFIC_TYPE_POWER_SWITCH_BINARY,
    {
      {ep12_noSec_InclNonSecure, sizeof(ep12_noSec_InclNonSecure)},
      {{ep12_noSec_InclSecure, sizeof(ep12_noSec_InclSecure)}, {ep12_sec_InclSecure, sizeof(ep12_sec_InclSecure)}}
    }
  }, /*Endpoint 3*/
  { GENERIC_TYPE_SWITCH_MULTILEVEL, SPECIFIC_TYPE_POWER_SWITCH_MULTILEVEL,
    {
      {ep3_noSec_InclNonSecure, sizeof(ep3_noSec_InclNonSecure)},
      {{ep3_noSec_InclSecure, sizeof(ep3_noSec_InclSecure)}, {ep3_sec_InclSecure, sizeof(ep3_sec_InclSecure)}}
    }
  }
};

EP_FUNCTIONALITY_DATA endPointFunctionality =
{
  NUMBER_OF_INDIVIDUAL_ENDPOINTS,     /**< nbrIndividualEndpoints 7 bit*/
  RES_ZERO,                           /**< resIndZeorBit 1 bit*/
  NUMBER_OF_AGGREGATED_ENDPOINTS,     /**< nbrAggregatedEndpoints 7 bit*/
  RES_ZERO,                           /**< resAggZeorBit 1 bit*/
  RES_ZERO,                           /**< resZero 6 bit*/
  ENDPOINT_IDENTICAL_DEVICE_CLASS_NO,/**< identical 1 bit*/
  ENDPOINT_DYNAMIC_NO                /**< dynamic 1 bit*/
};

/**
 * Setup AGI lifeline table from app_config.h
 */
CMD_CLASS_GRP  agiTableLifeLine[] = {AGITABLE_LIFELINE_GROUP};
CMD_CLASS_GRP  agiTableLifeLineEP1_2_3[] = {AGITABLE_LIFELINE_GROUP_EP1_2_3};

/**
 * Setup AGI root device groups table from app_config.h
 */
AGI_GROUP agiTableRootDeviceGroups[] = {AGITABLE_ROOTDEVICE_GROUPS};
AGI_GROUP agiTableEndpoint1Groups[] = {AGITABLE_ENDPOINT_1_GROUPS};
AGI_GROUP agiTableEndpoint2Groups[] = {AGITABLE_ENDPOINT_2_GROUPS};
AGI_GROUP agiTableEndpoint3Groups[] = {AGITABLE_ENDPOINT_3_GROUPS};

ST_ENDPOINT_ICONS ZWavePlusEndpointIcons[] = {ENDPOINT_ICONS};

/**
 * Application node ID
 */
BYTE myNodeID = 0;

/**
 * Handle only one event!
 */
static EVENT_APP eventQueue = EVENT_EMPTY;

/**
 * Application state-machine state.
 */
static STATE_APP currentState = STATE_APP_IDLE;

/**
 * Parameter is used to save wakeup reason after ApplicationInitHW(..)
 */
SW_WAKEUP wakeupReason;

typedef struct _BIN_SWITCH_{
  BYTE switchStatus;
}BIN_SWITCH;

typedef struct  _MULTILEVEL_SWITCH_{
  BYTE switchStatus;
  BYTE level;
}MULTILEVEL_SWITCH;

typedef struct _POWER_STRIP_{
  BIN_SWITCH binSwitch[2];
  MULTILEVEL_SWITCH dimmer;
}POWER_STRIP;

//NODE_LIST nList;

POWER_STRIP powerStrip;

static bTimerHandle_t notificationOverLoadTimerHandle = 0xFF;
static BOOL notificationOverLoadActiveState =  FALSE;
static BYTE notificationOverLoadendpoint = 0;

/**
 * root grp, endpoint, endpoint group
 */
ASSOCIATION_ROOT_GROUP_MAPPING rootGroupMapping[] = { ASSOCIATION_ROOT_GROUP_MAPPING_CONFIG};

BYTE multiLevelEndpointIDList = ENDPOINT_3;

BYTE supportedEvents = NOTIFICATION_EVENT_POWER_MANAGEMENT_OVERLOADED_DETECTED;

#ifdef APP_SUPPORTS_CLIENT_SIDE_AUTHENTICATION
s_SecurityS2InclusionCSAPublicDSK_t sCSAResponse = { 0, 0, 0, 0};
#endif /* APP_SUPPORTS_CLIENT_SIDE_AUTHENTICATION */

/****************************************************************************/
/*                              EXPORTED DATA                               */
/****************************************************************************/

// Nothing here.

/****************************************************************************/
/*                            PRIVATE FUNCTIONS                             */
/****************************************************************************/
void ZCB_DeviceResetLocallyDone(TRANSMISSION_RESULT * pTransmissionResult);
void AppStateManager( EVENT_APP event);
void ChangeState( STATE_APP newState);

#ifdef BOOTLOADER_ENABLED
void ZCB_OTAFinish(OTA_STATUS otaStatus);
BOOL ZCB_OTAStart(void);
#endif

void ZCB_JobStatus(TRANSMISSION_RESULT * pTransmissionResult);
void ZCB_NotificationTimer(void);
static void SetLed(BYTE OnOff, BYTE endpoint);
static void ToggleSwitch(BYTE switchID);
static void UpdateSwitchLevel(BYTE switchID, BYTE level );
static void notificationToggle(void);

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
  /* Setup Button S1 on the development board */
  /* CHANGE THIS - Set up your external hardware here */
  wakeupReason = bWakeupReason;
  /* hardware initialization */

  ZDP03A_InitHW(ZCB_EventSchedulerEventAdd, NULL);
  SetPinIn(ZDP03A_KEY_1,TRUE);
  SetPinIn(ZDP03A_KEY_2,TRUE);
  SetPinIn(ZDP03A_KEY_4,TRUE);
  SetPinIn(ZDP03A_KEY_6,TRUE);

  SetPinOut(ZDP03A_LED_D1);
  SetPinOut(ZDP03A_LED_D2);
  SetPinOut(ZDP03A_LED_D3);
  SetPinOut(ZDP03A_LED_D4);
  SetPinOut(ZDP03A_LED_D5);
  SetPinOut(ZDP03A_LED_D6);


  Led(ZDP03A_LED_D1, OFF);
  Led(ZDP03A_LED_D2, OFF);
  Led(ZDP03A_LED_D3, OFF);
  Led(ZDP03A_LED_D4, OFF);
  Led(ZDP03A_LED_D5, OFF);
  Led(ZDP03A_LED_D6, OFF);

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
  notificationOverLoadTimerHandle = 0xFF;
  /* Do not reinitialize the UART if already initialized for ISD51 in ApplicationInitHW() */
#ifndef ZW_ISD51_DEBUG
  ZW_DEBUG_INIT(1152);
#endif

  ZW_DEBUG_APP_SEND_NL();
  ZW_DEBUG_APP_SEND_STR("*** Power Strip sample application ***");
  ZW_DEBUG_APP_SEND_NL();
  ZW_DEBUG_APP_SEND_NUM(wakeupReason);
  ZW_DEBUG_APP_SEND_NUM(nvmStatus);
  ZW_DEBUG_APP_SEND_NL();

#ifdef WATCHDOG_ENABLED
  ZW_WatchDogEnable();
#endif

#ifdef BOOTLOADER_ENABLED
  NvmInit(nvmStatus);
#else
  UNUSED(nvmStatus);
#endif

  ApplTimerInit();

  /* Initialize the NVM if needed */
  if (MemoryGetByte((WORD)&EEOFFSET_MAGIC_far) == APPL_MAGIC_VALUE)
  {
    /* Initialize PowerLevel functionality*/
    loadStatusPowerLevel(NULL,NULL);
  }
  else
  {
    /* Initialize transport layer NVM */
    Transport_SetDefault();
    /* Reset protocol */
    ZW_SetDefault();
    /* Initialize PowerLevel functionality.*/
    loadInitStatusPowerLevel(NULL, NULL);

    /* Force reset of associations. */
    AssociationInit(TRUE);

    ZW_MEM_PUT_BYTE((WORD)&EEOFFSET_MAGIC_far, APPL_MAGIC_VALUE);
    ZW_MEM_PUT_BYTE((WORD)&EEOFFS_SECURITY_RESERVED.EEOFFS_MAGIC_BYTE_field, EEPROM_MAGIC_BYTE_VALUE);
    DefaultNotifactionStatus(NOTIFICATION_STATUS_UNSOLICIT_ACTIVED);
  }

  /* Initialize association module */
  AssociationInitEndpointSupport(FALSE,
                                 rootGroupMapping,
                                 sizeof(rootGroupMapping)/sizeof(ASSOCIATION_ROOT_GROUP_MAPPING));

  // Initialize AGI and set up groups.
  AGI_Init();
  // Root device
  AGI_LifeLineGroupSetup(agiTableLifeLine,
                         (sizeof(agiTableLifeLine)/sizeof(CMD_CLASS_GRP)),
                         GroupName,
                         ENDPOINT_ROOT);
  AGI_ResourceGroupSetup(agiTableRootDeviceGroups,
                         (sizeof(agiTableRootDeviceGroups)/sizeof(AGI_GROUP)),
                         ENDPOINT_ROOT);

  // Endpoint 1
  AGI_LifeLineGroupSetup(agiTableLifeLineEP1_2_3,
                         (sizeof(agiTableLifeLineEP1_2_3)/sizeof(CMD_CLASS_GRP)),
                         ep1_naming,
                         ENDPOINT_1);
  AGI_ResourceGroupSetup(&agiTableEndpoint1Groups,
                         (sizeof(agiTableEndpoint1Groups)/sizeof(AGI_GROUP)),
                         ENDPOINT_1);

  // Endpoint 2
  AGI_LifeLineGroupSetup(agiTableLifeLineEP1_2_3,
                         (sizeof(agiTableLifeLineEP1_2_3)/sizeof(CMD_CLASS_GRP)),
                         ep2_naming,
                         ENDPOINT_2);
  AGI_ResourceGroupSetup(agiTableEndpoint2Groups,
                         (sizeof(agiTableEndpoint2Groups)/sizeof(AGI_GROUP)),
                         ENDPOINT_2);

  // Endpoint 3
  AGI_LifeLineGroupSetup(agiTableLifeLineEP1_2_3,
                         (sizeof(agiTableLifeLineEP1_2_3)/sizeof(CMD_CLASS_GRP)),
                         ep3_naming,
                         ENDPOINT_3);
  AGI_ResourceGroupSetup(agiTableEndpoint3Groups,
                         (sizeof(agiTableEndpoint3Groups)/sizeof(AGI_GROUP)),
                         ENDPOINT_3);

  InitNotification();
  {
    AddNotification(&(agiTableEndpoint1Groups->profile),
                    NOTIFICATION_TYPE_POWER_MANAGEMENT,
                    &supportedEvents,
                    1,
                    FALSE,
                    ENDPOINT_1);

    AddNotification(&(agiTableEndpoint2Groups->profile),
                    NOTIFICATION_TYPE_POWER_MANAGEMENT,
                    &supportedEvents,
                    1,
                    FALSE,
                    ENDPOINT_2);

    AddNotification(&(agiTableEndpoint3Groups->profile),
                    NOTIFICATION_TYPE_POWER_MANAGEMENT,
                    &supportedEvents,
                    1,
                    FALSE,
                    ENDPOINT_3);
  }

  CommandClassZWavePlusInfoInit(ZWavePlusEndpointIcons,
                                sizeof(ZWavePlusEndpointIcons)/sizeof(ST_ENDPOINT_ICONS));

  MultiLevelSwitchInit(1, &multiLevelEndpointIDList);
  SetSwitchHwLevel(ENDPOINT_3, 0x00);
  powerStrip.dimmer.level = 0;
  powerStrip.dimmer.switchStatus = SWITCH_OFF;
  powerStrip.binSwitch[0].switchStatus = SWITCH_OFF;
  powerStrip.binSwitch[1].switchStatus = SWITCH_OFF;

  /* Get this sensors identification on the network */
  MemoryGetID(NULL, &myNodeID);

  /* Initialize manufactory specific module */
  ManufacturerSpecificDeviceIDInit();

#ifdef BOOTLOADER_ENABLED
  /* Initialize OTA module */
  OtaInit( ZCB_OTAStart, NULL, ZCB_OTAFinish);
#endif

  /*
   * Initialize Event Scheduler.
   */
  EventSchedulerInit(AppStateManager);

  Transport_OnApplicationInitSW( &m_AppNIF, NULL);
  Transport_AddEndpointSupport( &endPointFunctionality, endpointsNIF, NUMBER_OF_ENDPOINTS);

  /* Init state machine*/
  ZCB_EventSchedulerEventAdd((EVENT_WAKEUP)wakeupReason);

  return(TRUE);
}

/**
 * @brief See description for function prototype in ZW_basis_api.h.
 */
void
ApplicationTestPoll(void)
{
  // Nothing here
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

    case COMMAND_CLASS_BASIC:
      frame_status = handleCommandClassBasic(rxOpt, pCmd, cmdLength);
      break;

    case COMMAND_CLASS_SWITCH_BINARY:
      frame_status = handleCommandClassBinarySwitch(rxOpt, pCmd, cmdLength);
      break;

    case COMMAND_CLASS_NOTIFICATION_V3:
      frame_status = handleCommandClassNotification(rxOpt, pCmd, cmdLength);
      break;

    case COMMAND_CLASS_SWITCH_MULTILEVEL:
      frame_status = handleCommandClassMultiLevelSwitch(rxOpt,pCmd, cmdLength);
      break;

    case COMMAND_CLASS_SUPERVISION:
      frame_status = handleCommandClassSupervision(rxOpt, pCmd, cmdLength);
      break;

    case COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V2:
      frame_status = handleCommandClassMultiChannelAssociation(rxOpt, pCmd, cmdLength);
      break;
    case COMMAND_CLASS_SECURITY:
    case COMMAND_CLASS_SECURITY_2:
      frame_status = handleCommandClassSecurity(rxOpt, pCmd, cmdLength);
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

    case COMMAND_CLASS_BASIC:
     commandClassVersion =  CommandClassBasicVersionGet();
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
    case COMMAND_CLASS_SWITCH_BINARY:
     commandClassVersion = CommandClassBinarySwitchVersionGet();
      break;
    case COMMAND_CLASS_NOTIFICATION_V3:
      commandClassVersion = CommandClassNotificationVersionGet();
      break;
    case COMMAND_CLASS_SWITCH_MULTILEVEL:
      commandClassVersion = CommandClassMultiLevelSwitchVersionGet();
      break;
    case COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V2:
      commandClassVersion = CmdClassMultiChannelAssociationVersion();
      break;
    case COMMAND_CLASS_MULTI_CHANNEL_V3:
      commandClassVersion = CmdClassMultiChannelGet();
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
      DefaultNotifactionStatus(NOTIFICATION_STATUS_UNSOLICIT_ACTIVED);
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
AppStateManager(EVENT_APP event)
{
/*
  ZW_DEBUG_APP_SEND_NL();
  ZW_DEBUG_APP_SEND_STR("ASM st ");
  ZW_DEBUG_APP_SEND_NUM(currentState);
  ZW_DEBUG_APP_SEND_STR(" ev ");
  ZW_DEBUG_APP_SEND_NUM(event);
*/
  if(EVENT_SYSTEM_WATCHDOG_RESET == event)
  {
    /*Force state change to activate watchdog-reset without taking care of current
      state.*/
    ChangeState(STATE_APP_WATCHDOG_RESET);
  }


  switch(currentState)
  {
    case STATE_APP_STARTUP:
      if (EVENT_APP_REFRESH_MMI == event)
      {
        break;
      }
      ChangeState(STATE_APP_IDLE);
     break;

    case STATE_APP_IDLE:
      //ZW_DEBUG_APP_SEND_NL();
      //ZW_DEBUG_APP_SEND_STR(" STATE_APP_IDLE ");

      if (EVENT_APP_REFRESH_MMI == event)
      {
        // Nothing here
      }

      if ((EVENT_KEY_B1_TRIPLE_PRESS == event) || (EVENT_SYSTEM_LEARNMODE_START == event))
      {
        ZW_DEBUG_APP_SEND_STR(" EVENT_KEY_B1_TRIPLE_PRESS ");
        if (myNodeID)
        {
          ZW_DEBUG_APP_SEND_STR("LEARN_MODE_EXCLUSION");
          StartLearnModeNow(LEARN_MODE_EXCLUSION);
        }
        else
        {
          ZW_DEBUG_APP_SEND_STR("LEARN_MODE_INCLUSION");
          StartLearnModeNow(LEARN_MODE_INCLUSION);
        }
        ChangeState(STATE_APP_LEARN_MODE);
      }

      if ((EVENT_KEY_B1_HELD_10_SEC == event) || (EVENT_SYSTEM_RESET == event))
      {
        AGI_PROFILE lifelineProfile = {
                ASSOCIATION_GROUP_INFO_REPORT_PROFILE_GENERAL_NA_V2,
                ASSOCIATION_GROUP_INFO_REPORT_PROFILE_GENERAL_LIFELINE
        };
        ZW_DEBUG_APP_SEND_NL();
        ZW_DEBUG_APP_SEND_STR("Call locally reset");

        handleCommandClassDeviceResetLocally(&lifelineProfile, ZCB_DeviceResetLocallyDone);
        break;
      }

      if (EVENT_KEY_B2_PRESS == event)
      {
        ZW_DEBUG_APP_SEND_STR("+Toggle SW 1");
        ToggleSwitch(BIN_SWITCH_1);
      }

      if (EVENT_KEY_B4_UP == event)
      {
        ZW_DEBUG_APP_SEND_STR("+Stop DM 1");
        StopSwitchDimming(ENDPOINT_3);
        if (powerStrip.dimmer.switchStatus & SWITCH_IS_DIMMING)
        {
          ZW_DEBUG_APP_SEND_STR("+Stop DM 1");
          powerStrip.dimmer.switchStatus &= ~SWITCH_IS_DIMMING;
        }
        else
        {
          ZW_DEBUG_APP_SEND_STR("+Toggle DM 1");
          ToggleSwitch(MULTILEVEL_SWITCH_1);
        }
      }

      if (EVENT_KEY_B4_HELD == event)
      {
        ZW_DEBUG_APP_SEND_STR("+DM 1 held ");
        StopSwitchDimming(ENDPOINT_3);
        powerStrip.dimmer.switchStatus ^= SWITCH_DIM_UP;
        powerStrip.dimmer.switchStatus |= SWITCH_IS_DIMMING;
        if (powerStrip.dimmer.switchStatus & SWITCH_DIM_UP)
        {
          ZCB_CommandClassMultiLevelSwitchSupportSet(99, 1, ENDPOINT_3);
        }
        else
        {
          ZCB_CommandClassMultiLevelSwitchSupportSet(0, 1, ENDPOINT_3);
        }
      }

      if (EVENT_KEY_B6_PRESS == event)
      {
        /*
         * Pressing the B6/S6 key will toggle the overload timer. This timer
         * will transmit a notification every 30th second.
         */
        notificationToggle();
      }
      break;

    case STATE_APP_LEARN_MODE:
      if(EVENT_APP_REFRESH_MMI == event){}
      if((EVENT_KEY_B1_TRIPLE_PRESS == event)||(EVENT_SYSTEM_LEARNMODE_END == event))
      {
        StartLearnModeNow(LEARN_MODE_DISABLE);
        ChangeState(STATE_APP_IDLE);
      }

      if(EVENT_SYSTEM_LEARNMODE_FINISH == event)
      {
        ChangeState(STATE_APP_IDLE);
      }
      break;

    case STATE_APP_WATCHDOG_RESET:
      if(EVENT_APP_REFRESH_MMI == event){}

      ZW_DEBUG_APP_SEND_STR("STATE_APP_WATCHDOG_RESET");
      ZW_DEBUG_APP_SEND_NL();
      ZW_WatchDogEnable(); /*reset asic*/
      for (;;) {}
      break;
    case STATE_APP_OTA:
      if(EVENT_APP_REFRESH_MMI == event){}
      /*OTA state... do nothing until firmware update is finish*/
      break;

    case STATE_APP_TRANSMIT_DATA:
      if (EVENT_APP_FINISH_EVENT_JOB == event)
      {
        ClearLastNotificationAction(&agiTableRootDeviceGroups[notificationOverLoadendpoint - 1].profile, notificationOverLoadendpoint);
        ChangeState(STATE_APP_IDLE);
      }

      if (EVENT_KEY_B6_PRESS == event)
      {
        /*
         * Pressing the B6/S6 key will toggle the overload timer. This timer
         * will transmit a notification every 30th second.
         */
        notificationToggle();
      }
      break;
  }
}

/**
 * @brief Sets the current state to a new, given state.
 * @param newState New state.
 */
static void
ChangeState(STATE_APP newState)
{
  /*
  ZW_DEBUG_APP_SEND_NL();
  ZW_DEBUG_APP_SEND_STR("ChangeState st ");
  ZW_DEBUG_APP_SEND_NUM(currentState);
  ZW_DEBUG_APP_SEND_STR(" -> new st ");
  ZW_DEBUG_APP_SEND_NUM(newState);
  */

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
  if (STATE_APP_IDLE == currentState)
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

WORD
handleFirmWareIdGet(BYTE n)
{
  return ((0 == n) ? APP_FIRMWARE_ID : 0);
}

/**
 * @brief Handler for basic set. Handles received basic set commands.
 * @param val Parameter dependent of the application device class.
 */
void
handleBasicSetCommand(  BYTE val, BYTE endpoint )
{
  /* CHANGE THIS - Fill in your application code here */
  if(ENDPOINT_3 ==  endpoint)
  {
    /*Dimmer freq 1 sec!*/
    ZCB_CommandClassMultiLevelSwitchSupportSet(val, 1,endpoint);
  }
  else
  {
    CommandClassBinarySwitchSupportSet(val, endpoint);
  }
}

/**
 * @brief Handler for basic get. Handles received basic get commands.
 */
BYTE
getAppBasicReport(BYTE endpoint)
{
  if(ENDPOINT_3 ==  endpoint)
  {
    return CommandClassMultiLevelSwitchGet(endpoint);
  }
  return handleAppltBinarySwitchGet(endpoint);
}

/**
 * @brief Report the target value
 * @return target value.
 */
BYTE
getAppBasicReportTarget(BYTE endpoint)
{
  if(ENDPOINT_3 ==  endpoint)
  {
    return GetTargetLevel(endpoint);
  }
  return handleAppltBinarySwitchGet(endpoint);
}

/**
 * @brief Report transition duration time.
 * @return duration time.
 */BYTE
getAppBasicReportDuration(BYTE endpoint)
{
  if(ENDPOINT_3 ==  endpoint)
  {
    return GetCurrentDuration(endpoint);
  }
  return 0;
}

/*========================   sendApplReport   ======================
**    Handling of a Application specific Binary Switch Report
**
**   Side effects: none
**--------------------------------------------------------------------------*/
BYTE
handleAppltBinarySwitchGet(BYTE endpoint)
{
  BYTE bEndpoint = endpoint;

  ZW_DEBUG_APP_SEND_STR("App: handleAppltBinarySwitchGet ");
  ZW_DEBUG_APP_SEND_NUM((BYTE)endpoint);

  /* If endpoint 0 return status on endpoint 1*/
  if(2 >= endpoint )
  {
    if(0 == endpoint ){
      bEndpoint = 1;
    }
    ZW_DEBUG_APP_SEND_NUM((BYTE) powerStrip.binSwitch[bEndpoint-1].switchStatus);
    ZW_DEBUG_APP_SEND_NL();
    return powerStrip.binSwitch[bEndpoint-1].switchStatus;
  }
  ZW_DEBUG_APP_SEND_BYTE('%');
  ZW_DEBUG_APP_SEND_NL();
  return 0;
}

/**
 * @brief See prototype.
 */
void
handleApplBinarySwitchSet(
  CMD_CLASS_BIN_SW_VAL val,
  BYTE endpoint
)
{
  ZW_DEBUG_APP_SEND_STR("App: handleApplBinarySwitchSet");
  ZW_DEBUG_APP_SEND_NUM((BYTE)endpoint);
  ZW_DEBUG_APP_SEND_NUM((BYTE)val);
  ZW_DEBUG_APP_SEND_NL();

  if(2 >= endpoint)
  {
    if (0 == endpoint)
    {
    	endpoint = 1;
    }
    powerStrip.binSwitch[endpoint - 1].switchStatus = val;
    SetLed(val, endpoint);
  }
}

/**
 * @brief See prototype.
 */
MULTILEVEL_SWITCH_TYPE
CommandClassMultiLevelSwitchPrimaryTypeGet(BYTE endpoint)
{
  /*we have only one mutlilevel switch so we map root device endpoint to the multilevel switch*/
  if( ENDPOINT_3 == endpoint || 0 == endpoint)
  {
    /*we have only one mutlilevel switch so we ignore the endpoint parmeter*/
    return MULTILEVEL_SWITCH_DOWN_UP;
  }
  return 0;
}

/**
 * @brief See prototype.
 */
BYTE
CommandClassMultiLevelSwitchGet( BYTE endpoint)
{
  /*we have only one mutlilevel switch so we map root device endpoint to the multilevel switch*/
  if( ENDPOINT_3 == endpoint || 0 == endpoint)
  {
    if (powerStrip.dimmer.switchStatus & SWITCH_ON)
      return powerStrip.dimmer.level;
  }
  return 0;
}

/**
 * @brief See prototype.
 */
void
CommandClassMultiLevelSwitchSet( BYTE bLevel, BYTE endpoint)
{
  /*we have only one mutlilevel switch so we map root device endpoint to the multilevel switch*/
  if( ENDPOINT_3 == endpoint || 0 == endpoint)
  {
    UpdateSwitchLevel(MULTILEVEL_SWITCH_1,bLevel);
  }
}

/**
 * @brief See prototype.
 */
BYTE
GetFactoryDefaultDimmingDuration( BOOL boIsSetCmd, BYTE endpoint )
{
  UNUSED(endpoint);
  /*we have only one mutlilevel switch so we ignore the endpoint parmeter*/
  return (boIsSetCmd ? 1 : 10);
}

PCB(ZCB_JobStatus)(TRANSMISSION_RESULT * pTransmissionResult)
{
  ZW_DEBUG_APP_SEND_NL();
  ZW_DEBUG_APP_SEND_STR("TX CB for N");
  ZW_DEBUG_APP_SEND_NUM(pTransmissionResult->nodeId);

  if (TRANSMISSION_RESULT_FINISHED == pTransmissionResult->isFinished)
  {
    ZCB_EventSchedulerEventAdd(EVENT_APP_FINISH_EVENT_JOB);
  }
}

/**
 * @brief Called every time the notification timer triggers.
 */
PCB(ZCB_NotificationTimer)(void)
{
  JOB_STATUS jobStatus;

  ZW_DEBUG_APP_SEND_NL();
  ZW_DEBUG_APP_SEND_STR("Ntfctn timer");

  if (FALSE == notificationOverLoadActiveState)
  {
    /*Find new endpoint to send notification*/
    notificationOverLoadendpoint++;
    if (notificationOverLoadendpoint > NUMBER_OF_ENDPOINTS)
    {
      notificationOverLoadendpoint = 1;
    }

    ZW_DEBUG_APP_SEND_NL();
    ZW_DEBUG_APP_SEND_STR("Ntfctn enable EP");
    ZW_DEBUG_APP_SEND_NUM(notificationOverLoadendpoint);

    notificationOverLoadActiveState = TRUE;

    NotificationEventTrigger(&agiTableRootDeviceGroups[notificationOverLoadendpoint -1].profile,
                             supportedEvents,
                             NULL, 0,
                             notificationOverLoadendpoint);
  }
  else
  {
    ZW_DEBUG_APP_SEND_NL();
    ZW_DEBUG_APP_SEND_STR("Ntfctn disable EP");
    ZW_DEBUG_APP_SEND_NUM(notificationOverLoadendpoint);

    notificationOverLoadActiveState = FALSE;

    NotificationEventTrigger(&agiTableRootDeviceGroups[notificationOverLoadendpoint - 1].profile,
                             NOTIFICATION_EVENT_POWER_MANAGEMENT_NO_EVENT,
                             &supportedEvents, 1,
                             notificationOverLoadendpoint);
  }

  //@ [NOTIFICATION_TRANSMIT]
  jobStatus = UnsolicitedNotificationAction(
      &agiTableRootDeviceGroups[notificationOverLoadendpoint - 1].profile,
      notificationOverLoadendpoint,
      ZCB_JobStatus);
  //@ [NOTIFICATION_TRANSMIT]

  if (JOB_STATUS_SUCCESS != jobStatus)
  {
    TRANSMISSION_RESULT transmissionResult;

    ZW_DEBUG_APP_SEND_NL();
    ZW_DEBUG_APP_SEND_STR("X");
    ZW_DEBUG_APP_SEND_NUM(jobStatus);

    transmissionResult.status = FALSE;
    transmissionResult.nodeId = 0;
    transmissionResult.isFinished = TRANSMISSION_RESULT_FINISHED;

    ZCB_JobStatus(&transmissionResult);
  }

  ChangeState(STATE_APP_TRANSMIT_DATA);
}

static void UpdateSwitchLevel(BYTE switchID, BYTE level)
{
  UNUSED(switchID);
  powerStrip.dimmer.level = level;
  if (level == 0)
  {
    powerStrip.dimmer.switchStatus &= ~SWITCH_ON;
    Led(ZDP03A_LED_D3, OFF); //LED_OFF(3);
    Led(ZDP03A_LED_D4, OFF); //LED_OFF(4);
    Led(ZDP03A_LED_D5, OFF); //LED_OFF(5);
    Led(ZDP03A_LED_D6, OFF); //LED_OFF(6);
  }
  else
  {
    BYTE n = level/ 25;
    powerStrip.dimmer.switchStatus |= SWITCH_ON;

    switch (n)
    {
      case 0: //from 1 to 24
        Led(ZDP03A_LED_D3, ON); //LED_ON(3);
        Led(ZDP03A_LED_D4, OFF); //LED_OFF(4);
        Led(ZDP03A_LED_D5, OFF); //LED_OFF(5);
        Led(ZDP03A_LED_D6, OFF); //LED_OFF(6);
        break;
      case 1: /*from 25 to 49*/
        Led(ZDP03A_LED_D3, ON); //LED_ON(3);
        Led(ZDP03A_LED_D4, ON); //LED_ON(4);
        Led(ZDP03A_LED_D5, OFF); //LED_OFF(5);
        Led(ZDP03A_LED_D6, OFF); //LED_OFF(6);
        break;
     case 2: /*from 50 to 74*/
       Led(ZDP03A_LED_D3, ON); //LED_ON(3);
       Led(ZDP03A_LED_D4, ON); //LED_ON(4);
       Led(ZDP03A_LED_D5, ON); //LED_ON(5);
       Led(ZDP03A_LED_D6, OFF); //LED_OFF(6);
       break;
     case 3: /*from 75 to 99*/
       Led(ZDP03A_LED_D3, ON); //LED_ON(3);
       Led(ZDP03A_LED_D4, ON); //LED_ON(4);
       Led(ZDP03A_LED_D5, ON); //LED_ON(5);
       Led(ZDP03A_LED_D6, ON); //LED_ON(6);
       break;
     default:
       // Do nothing.
       break;
    }
  }
}

static void ToggleSwitch(BYTE switchID)
{
  ZW_DEBUG_APP_SEND_NUM(switchID);
  switch (switchID)
  {
    case BIN_SWITCH_1:
    case BIN_SWITCH_2:
      if (powerStrip.binSwitch[switchID].switchStatus & SWITCH_ON)
      {
        powerStrip.binSwitch[switchID].switchStatus &= ~SWITCH_ON;
        if (BIN_SWITCH_1 == switchID)
        {
          Led(ZDP03A_LED_D1, OFF); //LED_OFF(1);
        }
        else if (BIN_SWITCH_2 == switchID)
        {
          Led(ZDP03A_LED_D2, OFF); //LED_OFF(2);
        }
      }
      else
      {
        powerStrip.binSwitch[switchID].switchStatus |= SWITCH_ON;
        if (BIN_SWITCH_1 == switchID)
        {
          Led(ZDP03A_LED_D1, ON); //LED_ON(1);
        }
        else if (BIN_SWITCH_2 == switchID)
        {
          Led(ZDP03A_LED_D2, ON); //LED_ON(2);
        }
      }

      break;
    case MULTILEVEL_SWITCH_1:
     ZW_DEBUG_APP_SEND_NUM(powerStrip.dimmer.switchStatus & SWITCH_ON );
     ZW_DEBUG_APP_SEND_NUM(powerStrip.dimmer.level);
     /*stop the dimmer first if it is running*/
       StopSwitchDimming(ENDPOINT_3);
      if (powerStrip.dimmer.switchStatus & SWITCH_ON)
      {
        /*turn off*/
        UpdateSwitchLevel(switchID, 0);
      }
      else
      {
        /*turn on*/
        /*go back to the last level or turn it full on*/
        if (powerStrip.dimmer.level == 0)
          powerStrip.dimmer.level = LEVEL_MAX;
        UpdateSwitchLevel(switchID, powerStrip.dimmer.level);
      }
      break;
    default:
      /*this is expection*/
      break;
  }
}

/**
 * @brief Sets an LED based on the corresponding endpoint.
 * @param OnOff Given state of the LED.
 * @param endpoint Given endpoint.
 */
static void SetLed(BYTE OnOff, BYTE endpoint)
{
  LED_ACTION ledAction;

  ledAction = (0 == OnOff) ? OFF : ON;

  switch (endpoint)
  {
    case 1:
      Led(ZDP03A_LED_D1, ledAction);
      break;
    case 2:
      Led(ZDP03A_LED_D2, ledAction);
      break;
    default:
      // Do nothing
      break;
  }
}

/**
 * @brief Toggles the notification timer.
 */
static void notificationToggle(void)
{
  ZW_DEBUG_APP_SEND_NL();
  ZW_DEBUG_APP_SEND_STR("Ntfctn toggle");


  if (0xFF == notificationOverLoadTimerHandle)
  {
    ZW_DEBUG_APP_SEND_NL();
    ZW_DEBUG_APP_SEND_STR("Ntfctn start");

    notificationOverLoadActiveState = FALSE;

    notificationOverLoadTimerHandle = ZW_TimerLongStart(&ZCB_NotificationTimer, 31000, TIMER_FOREVER);

    /*
     * The timer will transmit the first notification in 30 seconds. We
     * call the callback function directly to transmit the first
     * notification right now.
     */
    ZCB_NotificationTimer();
  }
  else
  {
    ZW_DEBUG_APP_SEND_NL();
    ZW_DEBUG_APP_SEND_STR("Ntfctn stop");
    /* Deactivate overload timer */
    ZW_TimerLongCancel(notificationOverLoadTimerHandle);
    notificationOverLoadTimerHandle = 0xFF;
  }
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
        ZW_SetSecurityS2InclusionPublicDSK_CSA(&sCSAResponse);
      }
      break;
#endif /* APP_SUPPORTS_CLIENT_SIDE_AUTHENTICATION */

    default:
      break;
  }
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

