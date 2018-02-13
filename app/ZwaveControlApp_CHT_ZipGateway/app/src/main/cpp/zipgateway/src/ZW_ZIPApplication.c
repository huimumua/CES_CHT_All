/* © 2014 Sigma Designs, Inc. This is an unpublished work protected by Sigma
 * Designs, Inc. as a trade secret, and is not to be used or disclosed except as
 * provided Z-Wave Controller Development Kit Limited License Agreement. All
 * rights reserved.
 *
 * Notice: All information contained herein is confidential and/or proprietary to
 * Sigma Designs and may be covered by U.S. and Foreign Patents, patents in
 * process, and are protected by trade secret or copyright law. Dissemination or
 * reproduction of the source code contained herein is expressly forbidden to
 * anyone except Licensees of Sigma Designs  who have executed a Sigma Designs’
 * Z-WAVE CONTROLLER DEVELOPMENT KIT LIMITED LICENSE AGREEMENT. The copyright
 * notice above is not evidence of any actual or intended publication of the
 * source code. THE RECEIPT OR POSSESSION OF  THIS SOURCE CODE AND/OR RELATED
 * INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS  TO REPRODUCE, DISCLOSE OR
 * DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL A PRODUCT THAT IT  MAY
 * DESCRIBE.
 *
 * THE SIGMA PROGRAM AND ANY RELATED DOCUMENTATION OR TOOLS IS PROVIDED TO COMPANY
 * "AS IS" AND "WITH ALL FAULTS", WITHOUT WARRANTY OF ANY KIND FROM SIGMA. COMPANY
 * ASSUMES ALL RISKS THAT LICENSED MATERIALS ARE SUITABLE OR ACCURATE FOR
 * COMPANY’S NEEDS AND COMPANY’S USE OF THE SIGMA PROGRAM IS AT COMPANY’S
 * OWN DISCRETION AND RISK. SIGMA DOES NOT GUARANTEE THAT THE USE OF THE SIGMA
 * PROGRAM IN A THIRD PARTY SERVICE ENVIRONMENT OR CLOUD SERVICES ENVIRONMENT WILL
 * BE: (A) PERFORMED ERROR-FREE OR UNINTERRUPTED; (B) THAT SIGMA WILL CORRECT ANY
 * THIRD PARTY SERVICE ENVIRONMENT OR CLOUD SERVICE ENVIRONMENT ERRORS; (C) THE
 * THIRD PARTY SERVICE ENVIRONMENT OR CLOUD SERVICE ENVIRONMENT WILL OPERATE IN
 * COMBINATION WITH COMPANY’S CONTENT OR COMPANY APPLICATIONS THAT UTILIZE THE
 * SIGMA PROGRAM; (D) OR WITH ANY OTHER HARDWARE, SOFTWARE, SYSTEMS, SERVICES OR
 * DATA NOT PROVIDED BY SIGMA. COMPANY ACKNOWLEDGES THAT SIGMA DOES NOT CONTROL
 * THE TRANSFER OF DATA OVER COMMUNICATIONS FACILITIES, INCLUDING THE INTERNET,
 * AND THAT THE SERVICES MAY BE SUBJECT TO LIMITATIONS, DELAYS, AND OTHER PROBLEMS
 * INHERENT IN THE USE OF SUCH COMMUNICATIONS FACILITIES. SIGMA IS NOT RESPONSIBLE
 * FOR ANY DELAYS, DELIVERY FAILURES, OR OTHER DAMAGE RESULTING FROM SUCH ISSUES.
 * SIGMA IS NOT RESPONSIBLE FOR ANY ISSUES RELATED TO THE PERFORMANCE, OPERATION
 * OR SECURITY OF THE THIRD PARTY SERVICE ENVIRONMENT OR CLOUD SERVICES
 * ENVIRONMENT THAT ARISE FROM COMPANY CONTENT, COMPANY APPLICATIONS OR THIRD
 * PARTY CONTENT. SIGMA DOES NOT MAKE ANY REPRESENTATION OR WARRANTY REGARDING THE
 * RELIABILITY, ACCURACY, COMPLETENESS, CORRECTNESS, OR USEFULNESS OF THIRD PARTY
 * CONTENT OR SERVICE OR THE SIGMA PROGRAM, AND DISCLAIMS ALL LIABILITIES ARISING
 * FROM OR RELATED TO THE SIGMA PROGRAM OR THIRD PARTY CONTENT OR SERVICES. TO THE
 * EXTENT NOT PROHIBITED BY LAW, THESE WARRANTIES ARE EXCLUSIVE. SIGMA OFFERS NO
 * WARRANTY OF NON-INFRINGEMENT, TITLE, OR QUIET ENJOYMENT. NEITHER SIGMA NOR ITS
 * SUPPLIERS OR LICENSORS SHALL BE LIABLE FOR ANY INDIRECT, SPECIAL, INCIDENTAL OR
 * CONSEQUENTIAL DAMAGES OR LOSS (INCLUDING DAMAGES FOR LOSS OF BUSINESS, LOSS OF
 * PROFITS, OR THE LIKE), ARISING OUT OF THIS AGREEMENT WHETHER BASED ON BREACH OF
 * CONTRACT, INTELLECTUAL PROPERTY INFRINGEMENT, TORT (INCLUDING NEGLIGENCE),
 * STRICT LIABILITY, PRODUCT LIABILITY OR OTHERWISE, EVEN IF SIGMA OR ITS
 * REPRESENTATIVES HAVE BEEN ADVISED OF OR OTHERWISE SHOULD KNOW ABOUT THE
 * POSSIBILITY OF SUCH DAMAGES. THERE ARE NO OTHER EXPRESS OR IMPLIED WARRANTIES
 * OR CONDITIONS INCLUDING FOR SOFTWARE, HARDWARE, SYSTEMS, NETWORKS OR
 * ENVIRONMENTS OR FOR MERCHANTABILITY, NONINFRINGEMENT, SATISFACTORY QUALITY AND
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * The Sigma Program  is not fault-tolerant and is not designed, manufactured or
 * intended for use or resale as on-line control equipment in hazardous
 * environments requiring fail-safe performance, such as in the operation of
 * nuclear facilities, aircraft navigation or communication systems, air traffic
 * control, direct life support machines, or weapons systems, in which the failure
 * of the Sigma Program, or Company Applications created using the Sigma Program,
 * could lead directly to death, personal injury, or severe physical or
 * environmental damage ("High Risk Activities").  Sigma and its suppliers
 * specifically disclaim any express or implied warranty of fitness for High Risk
 * Activities.Without limiting Sigma’s obligation of confidentiality as further
 * described in the Z-Wave Controller Development Kit Limited License Agreement,
 * Sigma has no obligation to establish and maintain a data privacy and
 * information security program with regard to Company’s use of any Third Party
 * Service Environment or Cloud Service Environment. For the avoidance of doubt,
 * Sigma shall not be responsible for physical, technical, security,
 * administrative, and/or organizational safeguards that are designed to ensure
 * the security and confidentiality of the Company Content or Company Application
 * in any Third Party Service Environment or Cloud Service Environment that
 * Company chooses to utilize.
 */

#include "ZW_ZIPApplication.h"
#include "ZW_udp_server.h"
#include "CC_NetworkManagement.h"
#include "CC_FirmwareUpdate.h"
#include "CC_Portal.h"
#include "CC_Gateway.h"
#include "command_handler.h"
#include "Mailbox.h"
#include "NodeCache.h"
#include "ZIP_Router.h"
#include "ZW_controller_api.h"
#include <stdlib.h>

//#include "ZW_zip_classcmd.h"
#include "eeprom_layout.h"
#include "Serialapi.h"
#ifdef SECURITY_SUPPORT

#include "security_layer.h"
#endif
#include "ResourceDirectory.h"
#include "ClassicZIPNode.h"

#define DEBUG DEBUG_FULL
#include "net/uip-debug.h"

#include "ipv46_nat.h"
#include "DataStore.h"

#include "security_layer.h"
#include "S2_wrap.h"

security_scheme_t net_scheme;

BYTE IPNIF[0xff];
BYTE IPNIFLen=0;
BYTE IPSecureClasses[64];
BYTE IPnSecureClasses=0;


BYTE MyNIF[0xff];
BYTE MyNIFLen=0;
/* Secure Command classes for LAN */
BYTE SecureClasses[64];
BYTE nSecureClasses=0;
/* Extra Secure Command classes for unsolicited destination, added by portal, advertised only to PAN */BYTE nSecureClassesPAN =
    0;
BYTE SecureClassesPAN[16];

#define NIF ((NODEINFO*) MyNIF)
#define CLASSES ((BYTE*)&MyNIF[sizeof(NODEINFO)])
#define ADD_COMMAND_CLASS(c) { \
    CLASSES[MyNIFLen-sizeof(NODEINFO)] = c; \
    MyNIFLen++; ASSERT(MyNIFLen < sizeof(MyNIF)); }

ZW_APPLICATION_TX_BUFFER txBuf;
static BOOL should_send_nodelist = 0; //Set if the gateway should send a nodelist after the next nodeprobe or reset

/**
 * Command classes which we always support non-secure
 */
const BYTE MyClasses[] = {
    COMMAND_CLASS_ZWAVEPLUS_INFO,
    COMMAND_CLASS_TRANSPORT_SERVICE,
    COMMAND_CLASS_CRC_16_ENCAP,
    COMMAND_CLASS_APPLICATION_STATUS,
    COMMAND_CLASS_SECURITY_2};

const BYTE IpClasses[] = {
    COMMAND_CLASS_ZIP,
    COMMAND_CLASS_ZIP_ND,
};



/****************************************************************************/
/*                              PRIVATE DATA                                */
/****************************************************************************/
/*===========================   ApplicationInitSW   =========================
**    Initialization of the Application Software
**
**    This is an application function example
**
**--------------------------------------------------------------------------*/


/**
 * This function is responsible for setting up the node info
 */BYTE /*RET  TRUE       */
ApplicationInitSW(void) CC_REENTRANT_ARG /* IN   Nothing   */
{
  static uint16_t inclsion_disable_list[] = {COMMAND_CLASS_INCLUSION_CONTROLLER,COMMAND_CLASS_NETWORK_MANAGEMENT_INCLUSION};

  BYTE ver, capabilities, len, chip_type, chip_version;
  BYTE nodelist[32];
  BYTE cap;
  BYTE n;
  BYTE *c;
  DWORD tmpHome; //Not using homeID scince it might have side effects
  int i;

  ZW_GetNodeProtocolInfo(MyNodeID, NIF);
  NIF->nodeType.specific = SPECIFIC_TYPE_GATEWAY;
  NIF->nodeType.generic= GENERIC_TYPE_STATIC_CONTROLLER;
  memcpy(CLASSES,MyClasses,sizeof(MyClasses));
  MyNIFLen = sizeof(MyClasses) + sizeof(NODEINFO);

  SerialAPI_GetInitData(&ver, &capabilities, &len, nodelist, &chip_type,
      &chip_version);

  LOG_PRINTF("%u00 series chip version %u\n",chip_type,chip_version);

  cap = ZW_GetControllerCapabilities();

  if( (cap & (CONTROLLER_NODEID_SERVER_PRESENT | CONTROLLER_IS_SECONDARY) )==0  ) {
     MemoryGetID((BYTE*) &tmpHome,&n);
     LOG_PRINTF("Assigning myself(NodeID %u ) SIS role \n",n);
     ZW_SetSUCNodeID(n,TRUE,FALSE,ZW_SUC_FUNC_NODEID_SERVER,0);
     cap = ZW_GetControllerCapabilities();
  }

  if(capabilities & GET_INIT_DATA_FLAG_SECONDARY_CTRL) {
    LOG_PRINTF("I'am a Secondary controller\n");
    controller_role = SECONDARY;
  }
  if(capabilities & GET_INIT_DATA_FLAG_IS_SUC) {
    LOG_PRINTF("I'am SUC\n");
    controller_role = SUC;
  }
  if(capabilities & GET_INIT_DATA_FLAG_SLAVE_API) {
    LOG_PRINTF("I'am slave\n");
    controller_role = SLAVE;
  }

  MemoryGetID((BYTE*) &homeID,&MyNodeID);
  security_init( );

  uint8_t flags = sec2_get_my_node_flags();
  net_scheme = NO_SCHEME;
  if (get_net_scheme()>0) {
    net_scheme = SECURITY_SCHEME_0;
    ADD_COMMAND_CLASS(COMMAND_CLASS_SECURITY);
  }

  if(flags & NODE_FLAG_SECURITY2_UNAUTHENTICATED) net_scheme = SECURITY_SCHEME_2_UNAUTHENTICATED;
  if(flags & NODE_FLAG_SECURITY2_AUTHENTICATED) net_scheme = SECURITY_SCHEME_2_AUTHENTICATED;
  if(flags & NODE_FLAG_SECURITY2_ACCESS) net_scheme = SECURITY_SCHEME_2_ACCESS;

  LOG_PRINTF("Network shceme is:");
  switch(net_scheme) {
  case NO_SCHEME:
    LOG_PRINTF("NO_SCHEME\n"); break;
  case SECURITY_SCHEME_0:
    LOG_PRINTF("SCHEME 0\n"); break;

  case SECURITY_SCHEME_2_UNAUTHENTICATED:
    LOG_PRINTF("S2 UNAUTHENTICATED\n"); break;

  case SECURITY_SCHEME_2_AUTHENTICATED:
    LOG_PRINTF("S2 AUTHENTIGATED\n"); break;

  case SECURITY_SCHEME_2_ACCESS:
    LOG_PRINTF("S2 ACCESS\n"); break;
  }

  ZW_command_handler_init();

  ZW_command_handler_disable_list(0,0);
  if (cap & CONTROLLER_NODEID_SERVER_PRESENT)
  {
    LOG_PRINTF("I'm a primary or inclusion controller.\n");
  }
  else if ((cap & CONTROLLER_IS_SECONDARY) == 0)
  {
    DBG_PRINTF("I'am a Primary controller\n");
  } else {
    /*Remove the inclusion controller cc from the list*/
    ZW_command_handler_disable_list(inclsion_disable_list,sizeof(inclsion_disable_list) /sizeof(uint16_t));
  }

  /*Build the NIF which we present on the UDP side.*/
  memcpy(IPNIF,NIF,MyNIFLen);
  IPNIFLen = MyNIFLen;

  memcpy(IPNIF+IPNIFLen,IpClasses,sizeof(IpClasses));
  IPNIFLen+= sizeof(IpClasses);

  IPNIFLen+= ZW_command_handler_get_nif(NO_SCHEME, &IPNIF[IPNIFLen],sizeof(MyNIF)-IPNIFLen);
  IPnSecureClasses = ZW_command_handler_get_nif(SECURITY_SCHEME_UDP, &IPSecureClasses[0],sizeof(IPSecureClasses));

  MyNIFLen+= ZW_command_handler_get_nif(NO_SCHEME, &MyNIF[MyNIFLen],sizeof(MyNIF)-MyNIFLen);
  nSecureClasses = ZW_command_handler_get_nif(net_scheme, &SecureClasses[0],sizeof(SecureClasses));

  /*
   * The extra classes from the config file is added securely
   */
  appNodeInfo_CC_Add();
  return 0;
}


/*Initialization of the protocols */
void
ApplicationInitProtocols(void) /* IN   Nothing   */
{
  ZW_SendDataAppl_init();
#ifdef SUPPORTS_MDNS
  rd_init(FALSE);
#endif

  SetCacheEntryFlagMasked(MyNodeID,sec2_get_my_node_flags(),0xFF);

  rd_probe_new_nodes();
}

void
ApplicationControllerUpdate(BYTE bStatus, /*IN  Status event */
BYTE bNodeID, /*IN  Node id of the node that send node info */
BYTE* pCmd, /*IN  Pointer to Application Node information */
BYTE bLen /*IN  Node info length                        */
)CC_REENTRANT_ARG
{
  static struct ctimer update_timer;

  LOG_PRINTF("ApplicationControllerUpdate: status=0x%x node=%u NIF len=%u\n",
      (unsigned )bStatus, (unsigned )bNodeID, bLen);

  if( ((bNodeID < 1) || (bNodeID > ZW_MAX_NODES)) && (bStatus != UPDATE_STATE_NODE_INFO_REQ_FAILED)) {
      ERR_PRINTF("Controller update from invalid nodeID %d",bNodeID);
      return;
  }

  switch (bStatus)
  {

  case UPDATE_STATE_NEW_ID_ASSIGNED:
    should_send_nodelist = TRUE;

#if 0
    if((NetworkManagement_getState() != NM_NETWORK_UPDATE) || (NetworkManagement_getState() != NM_NODE_FOUND)) {
      if(ZW_GetSUCNodeID() == MyNodeID) {
        /* If we are the SUC we get the update right away, therefore Wait for the other controller to complete
         * Its security steps. */
        ctimer_set(&update_timer,5000,update_timeout,0);
      } else {
        ctimer_set(&update_timer,500,update_timeout,0);
      }

    }
#endif
    break;
  case UPDATE_STATE_NODE_INFO_RECEIVED:
      rd_node_is_alive(bNodeID);
      if (bNodeID && bLen)
      {
#ifdef SUPPORTS_MDNS
      rd_nif_request_notify(TRUE,bNodeID,pCmd,bLen);
#endif
      NetworkManagement_nif_notify(bNodeID,pCmd,bLen);
      }
    break;
  case UPDATE_STATE_NODE_INFO_REQ_DONE:
      //NodeInfoRequestDone(UPDATE_STATE_NODE_INFO_REQ_DONE);
	  break;
  case UPDATE_STATE_NODE_INFO_REQ_FAILED:
      //NodeInfoRequestDone(UPDATE_STATE_NODE_INFO_REQ_FAILED);
#ifdef SUPPORTS_MDNS
	  rd_nif_request_notify(FALSE,bNodeID,pCmd,bLen);
#endif
	  break;
  case UPDATE_STATE_ROUTING_PENDING:
    break;
  case UPDATE_STATE_DELETE_DONE:
    should_send_nodelist = TRUE;
    send_nodelist();
#ifdef SUPPORTS_MDNS
    rd_remove_node(bNodeID);
#endif
    remove_ip_association_by_nodeid(bNodeID);
    break;
  case UPDATE_STATE_SUC_ID:
    should_send_nodelist = TRUE;
    /*Create an async application reset */
    if(bNodeID !=0 && bNodeID == MyNodeID) {
      process_post(&zip_process,ZIP_EVENT_RESET,0);
    }
    DBG_PRINTF("SUC node Id updated, new ID is %i...\n",bNodeID);
    break;
  }
}

void
ApplicationNodeInformation(BYTE *deviceOptionsMask, /*OUT Bitmask with application options    */
APPL_NODE_TYPE *nodeType, /*OUT  Device type Generic and Specific   */
BYTE **nodeParm, /*OUT  Device parameter buffer pointer    */
BYTE *parmLength /*OUT  Number of Device parameter bytes   */
)CC_REENTRANT_ARG
{
	/* this is a listening node and it supports optional CommandClasses */
	*deviceOptionsMask = APPLICATION_NODEINFO_LISTENING
			| APPLICATION_NODEINFO_OPTIONAL_FUNCTIONALITY;
	nodeType->generic = NIF->nodeType.generic; /* Generic device type */
	nodeType->specific = NIF->nodeType.specific; /* Specific class */

	*nodeParm = CLASSES; /* Send list of known command classes. */
	*parmLength = MyNIFLen - sizeof(NODEINFO); /* Set length*/
//DBG_PRINTF("MyNIFLen %d\n", (int)MyNIFLen);
}

static void ApplicationNotSupported(zwave_connection_t*c, ZW_APPLICATION_TX_BUFFER* pCmd,BOOL temporary,VOID_CALLBACKFUNC(cbFunc)(BYTE,void*))
{
  txBuf.ZW_CommandCommandClassNotSupportedFrame.cmdClass=COMMAND_CLASS_APPLICATION_CAPABILITY;
  txBuf.ZW_CommandCommandClassNotSupportedFrame.cmd=COMMAND_COMMAND_CLASS_NOT_SUPPORTED;
  txBuf.ZW_CommandCommandClassNotSupportedFrame.offendingCommandClass=pCmd->ZW_Common.cmdClass;
  txBuf.ZW_CommandCommandClassNotSupportedFrame.offendingCommand=pCmd->ZW_Common.cmd;
  txBuf.ZW_CommandCommandClassNotSupportedFrame.properties1=temporary ? 0x80 : 0x0;
  ZW_SendDataZIP(c,(BYTE*)&txBuf,sizeof(txBuf.ZW_CommandCommandClassNotSupportedFrame), cbFunc);
}

static void ApplicationBusy(zwave_connection_t*c)
{
  txBuf.ZW_ApplicationBusyFrame.cmdClass=COMMAND_CLASS_APPLICATION_STATUS;
  txBuf.ZW_ApplicationBusyFrame.cmd=APPLICATION_BUSY;
  txBuf.ZW_ApplicationBusyFrame.status=0;
  ZW_SendDataZIP(c,(BYTE*)&txBuf,sizeof(txBuf.ZW_ApplicationBusyFrame), 0);
}


/**
 * Main application command handler for commands coming both via Z-Wave and IP
 */
void
ApplicationIpCommandHandler(zwave_connection_t *c, void *pData, u16_t bDatalen) REENTRANT
{
	ZW_APPLICATION_TX_BUFFER* pCmd = (ZW_APPLICATION_TX_BUFFER*)pData;
  c->tx_flags =((c->rx_flags & RECEIVE_STATUS_LOW_POWER) ? TRANSMIT_OPTION_LOW_POWER : 0)
          | TRANSMIT_OPTION_ACK | TRANSMIT_OPTION_EXPLORE
					| TRANSMIT_OPTION_AUTO_ROUTE;

  if (bDatalen < 2)
  {
	  //ERR_PRINTF("%s: Package is too small.",__FUNCTION__);
	  ERR_PRINTF("ApplicationIpCommandHandler: Package is too small.\r\n");
	  return;
	}


  if(pCmd->ZW_NodeInfoCachedGetFrame.cmdClass == COMMAND_CLASS_NETWORK_MANAGEMENT_PROXY &&
       pCmd->ZW_NodeInfoCachedGetFrame.cmd== NODE_INFO_CACHED_GET &&
       pCmd->ZW_NodeInfoCachedGetFrame.nodeId == 0) {
    c->scheme = SECURITY_SCHEME_UDP;
    /* Always allow */
  } else  if (
      ((c->rx_flags & RECEIVE_STATUS_TYPE_MASK)  != RECEIVE_STATUS_TYPE_SINGLE) ||
      uip_is_addr_mcast(&c->lipaddr) ) {
    /*Drop all multicast frames except for node info cached get*/
    goto send_to_unsolicited;
  }
  NetworkManagement_frame_notify();

  switch(ZW_command_handler_run(c,pData,bDatalen)) {
  case COMMAND_HANDLED:
    return; //We are done
  case COMMAND_NOT_SUPPORTED:
    /*ApplicationNotSupported(c,pCmd,0,0);
    return TRUE;*/
    /*Just parse it to the unsolicited destination*/
    break;
  case COMMAND_BUSY:
    ApplicationBusy(c);
    return;
  case COMMAND_PARSE_ERROR:
    return; //Just drop
  case CLASS_NOT_SUPPORTED:
    break; //move on
  }

  DBG_PRINTF("Unhandled command  0x%02x:0x%02x from ", pCmd->ZW_Common.cmdClass, pCmd->ZW_Common.cmd);
  PRINT6ADDR(&c->ripaddr);
  DBG_PRINTF("\n");

send_to_unsolicited:
  //We are forwarding the frame to the unsolicited destination.
  if (!uip_is_addr_unspecified(&cfg.unsolicited_dest))
  {
    /*If not for classic me then send to unsolicited destination, and only ask for ACK for^M
     * single cast frames */
    ClassicZIPNode_SendUnsolicited(c, pCmd, bDatalen, &cfg.unsolicited_dest, UIP_HTONS(cfg.unsolicited_port),
        (c->rx_flags & RECEIVE_STATUS_TYPE_MASK) == RECEIVE_STATUS_TYPE_SINGLE);
  }

  if (!uip_is_addr_unspecified(&cfg.unsolicited_dest2))
  {
    ClassicZIPNode_SendUnsolicited(c, pCmd, bDatalen, &cfg.unsolicited_dest2, UIP_HTONS(cfg.unsolicited_port2), FALSE);
  }
}


 static int
 compare_bytes (const void *a, const void *b)
 {
   const uint8_t *da = (const uint8_t *) a;
   const uint8_t *db = (const uint8_t *) b;
   return (*da > *db) - (*da < *db);
 }

 /**
  * Remove duplicates from byte array. This function will
  * sort the list.
  *
  * \param data     input array
  * \param data_len length of array
  * \return         new length of array
  */
 int uniq_byte_array(uint8_t* data, int data_len) {
   qsort( data, data_len, sizeof(uint8_t),compare_bytes);

   uint8_t* s,*d;

   s=d =data;
   do
   {
     *d = *s;
     s++;
     if(*s == *d) s++;
     d++;
   } while(s < (data+data_len));
   return d-data;
 }


void
ApplicationDefaultSet()
{
  rd_exit();
  rd_data_store_invalidate();
  /*Enable SUC/SIS */
  MemoryGetID( (BYTE*)&homeID,&MyNodeID );
  /* ZW_EnableSUC(TRUE,ZW_SUC_FUNC_NODEID_SERVER); call is deprecated */
  ZW_SetSUCNodeID(MyNodeID,TRUE,FALSE,ZW_SUC_FUNC_NODEID_SERVER,0);

  security_set_default();
  sec2_create_new_network_keys();
}

/*
 * Add secure command classes for the unsolicited destination to GW.
 * Subsequent calls to this function will overwrite the existing secure CCs in the list.
 *  These CCs are only advertized on the PAN. */
void
AddSecureUnsocDestCCsToGW(BYTE *ccList, BYTE ccCount) CC_REENTRANT_ARG
/* Reentrant to conserve XDATA on ASIX C51 */
{
  BYTE idx,i;
  for (idx = 0; idx < ccCount; idx++)
  {
    SecureClasses[nSecureClasses++] = ccList[idx];
    IPSecureClasses[IPnSecureClasses++] = ccList[idx];
  }

   // FIXME this will not work well with extended command classes
  nSecureClasses = uniq_byte_array(SecureClasses,nSecureClasses);
  IPnSecureClasses = uniq_byte_array(IPSecureClasses,IPnSecureClasses);


/*  for(i=0 ; i < IPnSecureClasses; i++) {
    printf("AddSecureUnsocDestCCsToGW Sec IP class %x\n",IPSecureClasses[i]);
  }*/

}

/*
 *   Add CCs to PAN Node Info Frame. Useful for advertising capabilities
 *   of the unsolicited destination.
 *   Subsequent calls to this function will overwrite previously added CCs.
 */
void AddUnsocDestCCsToGW(BYTE *ccList, BYTE ccCount) CC_REENTRANT_ARG
/* Reentrant to conserve XDATA on ASIX C51 */
{
  int idx,i;
  for (idx = 0; idx < ccCount; idx++)
  {
    ADD_COMMAND_CLASS( ccList[idx] );
    IPNIF[IPNIFLen++] =  ccList[idx];
  }

  // FIXME this will not work well with extended command classes
  MyNIFLen = uniq_byte_array( &MyNIF[sizeof(NODEINFO)], MyNIFLen - sizeof(NODEINFO) ) + sizeof(NODEINFO);
  IPNIFLen = uniq_byte_array( &IPNIF[sizeof(NODEINFO)], IPNIFLen - sizeof(NODEINFO) ) + sizeof(NODEINFO);

  /*Make Z-Wave+ info appear first */
  for(idx = sizeof(NODEINFO); idx < MyNIFLen; idx++) {

    if(MyNIF[idx] == COMMAND_CLASS_ZWAVEPLUS_INFO) {
      MyNIF[idx] = MyNIF[sizeof(NODEINFO)];
      MyNIF[sizeof(NODEINFO)] = COMMAND_CLASS_ZWAVEPLUS_INFO;
    }
  }
}

void CommandClassesUpdated() {
  BYTE listening;
  APPL_NODE_TYPE nodeType;
  BYTE *nodeParm;
  BYTE parmLength;

  LOG_PRINTF("Command classes updated\n");
  ApplicationNodeInformation(&listening,&nodeType,&nodeParm,&parmLength);
  SerialAPI_ApplicationNodeInformation(listening,nodeType,nodeParm,parmLength);
  security_set_supported_classes( SecureClasses , nSecureClasses );
  rd_register_new_node(MyNodeID,0);
}


void send_nodelist() {
  if(should_send_nodelist && (!uip_is_addr_unspecified(&cfg.unsolicited_dest) || !uip_is_addr_unspecified(&cfg.unsolicited_dest2))) {

    if(NetworkManagement_SendNodeList_To_Unsolicited()) {
      should_send_nodelist =FALSE;
    }
  }
}

void SetPreInclusionNIF(security_scheme_t target_scheme) {
  const APPL_NODE_TYPE nodeType = {GENERIC_TYPE_STATIC_CONTROLLER,SPECIFIC_TYPE_GATEWAY};
  security_scheme_t scheme_bak;
  uint8_t tmpNIF[64];
  uint8_t tmpNIFlen;

  LOG_PRINTF("Setting pre-inclusion NIF\n");
  memcpy(tmpNIF,MyClasses,sizeof(MyClasses));
  tmpNIFlen= sizeof(MyClasses);
  /*We also need this */
  tmpNIF[tmpNIFlen++] = COMMAND_CLASS_SECURITY;

  scheme_bak = net_scheme;
  net_scheme = target_scheme;

  tmpNIFlen+=ZW_command_handler_get_nif( NO_SCHEME, tmpNIF+tmpNIFlen,sizeof(tmpNIF)-tmpNIFlen);
  net_scheme=scheme_bak;

  for(int i=0; i < cfg.extra_classes_len; i++) {
    tmpNIF[tmpNIFlen++] = cfg.extra_classes[i];
  }

  SerialAPI_ApplicationNodeInformation(TRUE,nodeType,tmpNIF,tmpNIFlen);
}
