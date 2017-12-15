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

#define REPLACE_FAILED
#include "CC_NetworkManagement.h"
#include "ZW_ZIPApplication.h"
#include "ZW_udp_server.h"
#include "Serialapi.h"
#include "ZW_classcmd_ex.h"
#include "Bridge.h"
#include "dhcpc2.h"
#include "ClassicZIPNode.h"

#include "ZIP_Router.h"
#include "NodeCache.h"
#include "ResourceDirectory.h"
#include "ZW_SendDataAppl.h"
#include "ZW_controller_api.h"
#include "sys/rtimer.h"
#include "ipv46_nat.h"
#include "S2_wrap.h"
#include "s2_keystore.h"
#include "CC_InclusionController.h"
#include "node_queue.h"
#include "Mailbox.h"
#define ADD_REMOVE_TIMEOUT 6000
#define LEARN_TIMEOUT 6000
#define NETWORK_MANAGEMENT_TIMEOUT 2*6000

#ifdef SECURITY_SUPPORT
#include "security_layer.h"
#endif

#include "s2_inclusion.h"

#define ZWAVE_PORT 4123
#define DTLS_PORT 41230
typedef enum
{

  NM_EV_ADD_LEARN_READY = ADD_NODE_STATUS_LEARN_READY,
  NM_EV_ADD_NODE_FOUND = ADD_NODE_STATUS_NODE_FOUND,
  NM_EV_ADD_CONTROOLER = ADD_NODE_STATUS_ADDING_CONTROLLER,
  NM_EV_ADD_PROTOCOL_DONE = ADD_NODE_STATUS_PROTOCOL_DONE,
  NM_EV_ADD_SLAVE = ADD_NODE_STATUS_ADDING_SLAVE,
  NM_EV_ADD_FAILED = ADD_NODE_STATUS_FAILED,
  NM_EV_ADD_NOT_PRIMARY = ADD_NODE_STATUS_NOT_PRIMARY,
  NM_EV_ADD_NODE_STATUS_DONE = ADD_NODE_STATUS_DONE,

  NM_EV_NODE_ADD   = 100,
  NM_NODE_ADD_STOP ,
  NM_EV_TIMEOUT    ,
  NM_EV_SECURITY_DONE,
  NM_EV_S0_STARTED,
  NM_EV_ADD_SECURITY_REQ_KEYS,
  NM_EV_ADD_SECURITY_KEY_CHALLANGE,
  NM_EV_NODE_PROBE_DONE,
  NM_EV_DHCP_DONE,
  NM_EV_NODE_ADD_S2,

  NM_EV_ADD_SECURITY_KEYS_SET, /* 109 */
  NM_EV_ADD_SECURITY_DSK_SET,

  NM_EV_REPLACE_FAILED_START,

  NM_EV_REPLACE_FAILED_STOP, /*112*/
  NM_EV_REPLACE_FAILED_DONE,
  NM_EV_REPLACE_FAILED_FAIL,
  NM_EV_REPLACE_FAILED_START_S2,
  NM_EV_MDNS_EXIT,
  NM_EV_LEARN_SET,
  NM_EV_REQUEST_NODE_LIST,
  NM_EV_PROXY_COMPLETE,
  NM_EV_START_PROXY_INCLUSION,
  NM_EV_START_PROXY_REPLACE,
  NM_EV_NODE_INFO,
  NM_EV_FRAME_RECEIVED,
  NM_EV_ALL_PROBED,
} nm_event_t;

const char * nm_event_name(nm_event_t ev)
{
  static char str[25];
  switch (ev) 
  {
  case NM_EV_ADD_LEARN_READY            :       return  "NM_EV_ADD_LEARN_READY";
  case NM_EV_ADD_NODE_FOUND             :       return  "NM_EV_ADD_NODE_FOUND";
  case NM_EV_ADD_CONTROOLER             :       return  "NM_EV_ADD_CONTROOLER";
  case NM_EV_ADD_PROTOCOL_DONE          :       return  "NM_EV_ADD_PROTOCOL_DONE";
  case NM_EV_ADD_SLAVE                  :       return  "NM_EV_ADD_SLAVE";
  case NM_EV_ADD_FAILED                 :       return  "NM_EV_ADD_FAILED";
  case NM_EV_ADD_NOT_PRIMARY            :       return  "NM_EV_ADD_NOT_PRIMARY";
  case NM_EV_ADD_NODE_STATUS_DONE       :       return  "NM_EV_ADD_NODE_STATUS_DONE";

  case NM_EV_NODE_ADD                   :       return  "NM_EV_NODE_ADD";
  case NM_NODE_ADD_STOP                 :       return  "NM_NODE_ADD_STOP";
  case NM_EV_TIMEOUT                    :       return  "NM_EV_TIMEOUT";
  case NM_EV_SECURITY_DONE              :       return  "NM_EV_SECURITY_DONE";
  case NM_EV_ADD_SECURITY_REQ_KEYS      :       return  "NM_EV_ADD_SECURITY_REQ_KEYS";
  case NM_EV_ADD_SECURITY_KEY_CHALLANGE :       return  "NM_EV_ADD_SECURITY_KEY_CHALLANGE";
  case NM_EV_NODE_PROBE_DONE            :       return  "NM_EV_NODE_PROBE_DONE";
  case NM_EV_DHCP_DONE                  :       return  "NM_EV_DHCP_DONE";
  case NM_EV_NODE_ADD_S2                :       return  "NM_EV_NODE_ADD_S2";

  case NM_EV_ADD_SECURITY_KEYS_SET      :       return  "NM_EV_ADD_SECURITY_KEYS_SET";
  case NM_EV_ADD_SECURITY_DSK_SET       :       return  "NM_EV_ADD_SECURITY_DSK_SET";

  case NM_EV_REPLACE_FAILED_START       :       return  "NM_EV_REPLACE_FAILED_START";

  case NM_EV_REPLACE_FAILED_STOP        :       return  "NM_EV_REPLACE_FAILED_STOP";
  case NM_EV_REPLACE_FAILED_DONE        :       return  "NM_EV_REPLACE_FAILED_DONE";
  case NM_EV_REPLACE_FAILED_FAIL        :       return  "NM_EV_REPLACE_FAILED_FAIL";
  case NM_EV_REPLACE_FAILED_START_S2    :       return  "NM_EV_REPLACE_FAILED_START_S2";
  case NM_EV_MDNS_EXIT                  :       return  "NM_EV_MDNS_EXIT";
  case NM_EV_LEARN_SET                  :       return  "NM_EV_LEARN_SET";
  case NM_EV_REQUEST_NODE_LIST          :       return  "NM_EV_REQUEST_NODE_LIST";
  case NM_EV_PROXY_COMPLETE             :       return  "NM_EV_PROXY_COMPLETE";
  case NM_EV_START_PROXY_INCLUSION      :       return  "NM_EV_START_PROXY_INCLUSION";
  case NM_EV_START_PROXY_REPLACE        :       return  "NM_EV_START_PROXY_REPLACE";
  case NM_EV_NODE_INFO                  :       return  "NM_EV_NODE_INFO";
  case NM_EV_FRAME_RECEIVED                  :       return  "NM_EV_FRAME_RECEIVED";
  case NM_EV_ALL_PROBED                 :       return  "NM_EV_ALL_PROBED";

  default:
    sprintf(str, "%d", ev);
    return str;
  }
}

const char* nm_state_name(nm_state_t state)
{
  static char str[25];
  switch(state)
  {
  case NM_IDLE                             : return "NM_IDLE";
  case NM_WAITING_FOR_ADD                  : return "NM_WAITING_FOR_ADD";
  case NM_NODE_FOUND                       : return "NM_NODE_FOUND";
  case NM_WIAT_FOR_PROTOCOL                : return "NM_WIAT_FOR_PROTOCOL";
  case NM_NETWORK_UPDATE                   : return "NM_NETWORK_UPDATE";
  case NM_WAITING_FOR_PROBE                : return "NM_WAITING_FOR_PROBE";
  case NM_SET_DEFAULT                      : return "NM_SET_DEFAULT";
  case NM_LEARN_MODE                       : return "NM_LEARN_MODE";
  case NM_WAIT_FOR_SECURE_ADD              : return "NM_WAIT_FOR_SECURE_ADD";
  case NM_SENDING_NODE_INFO                : return "NM_SENDING_NODE_INFO";
  case NM_WAITING_FOR_NODE_REMOVAL         : return "NM_WAITING_FOR_NODE_REMOVAL";
  case NM_WAITING_FOR_FAIL_NODE_REMOVAL    : return "NM_WAITING_FOR_FAIL_NODE_REMOVAL";
  case NM_WAITING_FOR_NODE_NEIGH_UPDATE    : return "NM_WAITING_FOR_NODE_NEIGH_UPDATE";
  case NM_WAITING_FOR_RETURN_ROUTE_ASSIGN  : return "NM_WAITING_FOR_RETURN_ROUTE_ASSIGN";
  case NM_WAITING_FOR_RETURN_ROUTE_DELETE  : return "NM_WAITING_FOR_RETURN_ROUTE_DELETE";

  case NM_WAIT_FOR_PROBE_AFTER_ADD         : return "NM_WAIT_FOR_PROBE_AFTER_ADD";
  case NM_WAIT_FOR_SECURE_LEARN            : return "NM_WAIT_FOR_SECURE_LEARN";
  case NM_WAIT_FOR_MDNS                    : return "NM_WAIT_FOR_MDNS";
  case NM_WAIT_FOR_PROBE_BY_SIS            : return "NM_WAIT_FOR_PROBE_BY_SIS";
  case NM_WAIT_DHCP                        : return "NM_WAIT_DHCP";
  case NM_REMOVING_ASSOCIATIONS            : return "NM_REMOVING_ASSOCIATIONS";

  case NM_REPLACE_FAILED_REQ               : return "NM_REPLACE_FAILED_REQ";
  case NM_PREPARE_SUC_INCLISION            : return "NM_PREPARE_SUC_INCLISION";
  case NM_WIAT_FOR_SUC_INCLUSION           : return "NM_WIAT_FOR_SUC_INCLUSION";
  case NM_PROXY_INCLUSION_WAIT_NIF         : return "NM_PROXY_INCLUSION_WAIT_NIF";
  default:
    sprintf(str, "%d", state);
    return str;
  }
}

/****************************  Forward declerations *****************************************/
static void
RemoveNodeStatusUpdate(LEARN_INFO* inf);
static void
AddNodeStatusUpdate(LEARN_INFO* inf);
static void
LearnModeStatus(LEARN_INFO* inf);
static void
ReplaceFailedNodeStatus(BYTE status);
static void
nm_send_reply(void* buf, u8_t len);
static void
SendNodeList(BYTE bStatus);
static void
LearnTimerExpired(void);

static void
NodeProbeDone(rd_ep_database_entry_t* ep, void* user);
static void
SecureInclusionDone(int status);
static void
timeout(void* none);
static void
SendReplyWhenNetworkIsUpdated();
void
mem_replace(unsigned char *buf, char old, char new, size_t len);
size_t
mem_insert(u8_t* dst, const u8_t *src, u8_t find, u8_t add, size_t len, size_t max);

static void
nm_fsm_post_event(nm_event_t ev, void* event_data);

/********************************************************************************************/
#define NMS_FLAG_S2_ADD 1
#define NMS_FLAG_PROXY_INCLUSION 2
#define NMS_FLAG_LEARNMODE_NEW 4
#define NMS_FLAG_LEARNMODE_NWI 8
#define NMS_FLAG_LEARNMODE_NWE 0x10
#define NMS_FLAG_CONTROLLER_REPLICATION 0x20

struct NetworkManagementState
{
  BYTE class;
  BYTE cmd;
  BYTE seq;

  zwave_connection_t conn;

  BYTE addRemoveNodeTimerHandle;
  BYTE networkManagementTimer;
  BYTE txOptions;
  BYTE waiting_for_ipv4_addr;
  BYTE tmp_node;
  /*This buffer is global, because the node info is only present in the */
  nm_state_t state;
  int flags;
  BYTE buf_len;
  uint8_t count;
  ZW_APPLICATION_TX_BUFFER buf;
  struct ctimer timer;
};

int network_management_init_done = 0;

struct NetworkManagementState nms =
{ 0 };

/**
 * Integer log2
 */
static unsigned int
ilog2(int x)
{
  int i = 16;
  do
  {
    i--;
    if ((1 << i) & x)
      return i;
  }
  while (i);
  return 0;
}

static clock_time_t
ageToTime(BYTE age)
{
  return (1 << (age & 0xf)) * 60;
}


static uint8_t
is_cc_in_nif(uint8_t* nif, uint8_t nif_len, uint8_t cc)
{
  int i;
  for (i = 0; i < nif_len; i++)
  {
    if (nif[i] == cc)
    {
      return 1;
    }
  }
  return 0;
}

static void inclusion_controller_complete(int status) {
  nm_fsm_post_event(NM_EV_PROXY_COMPLETE,&status);
}

static void
nm_fsm_post_event(nm_event_t ev, void* event_data)
{
  uint8_t zero = 0;

  DBG_PRINTF( "nm_fsm_post_event event: %s state: %s\n",nm_event_name(ev),nm_state_name(nms.state));
  switch (nms.state)
  {
  case NM_IDLE:
    if (ev == NM_EV_LEARN_SET) {
      ZW_LEARN_MODE_SET_FRAME* f = (ZW_LEARN_MODE_SET_FRAME*) event_data;

      if(f->mode == ZW_SET_LEARN_MODE_CLASSIC) {
        nms.flags = 0;
      } else if(f->mode == ZW_SET_LEARN_MODE_NWI) {
        nms.flags = NMS_FLAG_LEARNMODE_NWI;
        /*Note it is supposed to be MODE CLASSIC*/
      } else if(f->mode == ZW_SET_LEARN_MODE_NWE ) {
        nms.flags = NMS_FLAG_LEARNMODE_NWE;
      } else {
        WRN_PRINTF("Unknown learnmode\n");
        return;
      }

      /* Make temporary NIF, used for inclusion */
      SetPreInclusionNIF(NO_SCHEME);

      nms.state = NM_LEARN_MODE;

      if ((f->reserved & ZW_LEARN_MODE_RETURN_INTERVIEW_STATUS) && (nms.flags != NMS_FLAG_LEARNMODE_NWE))
      {
        nms.flags |= NMS_FLAG_LEARNMODE_NEW;
      }

      nms.count = 0;
      ZW_SetLearnMode(ZW_SET_LEARN_MODE_CLASSIC, LearnModeStatus);

      if (f->mode == ZW_SET_LEARN_MODE_CLASSIC)
      {
          /* Keep the timeout to 20 seconds if the learn mode is CLASSIC */
          ctimer_set(&nms.timer, CLOCK_SECOND*20, timeout, 0);
      }
      else
      {
          /* in case of NWI mode Keeping the timeout to 2 seconds come from recommendation in the
             document SDS11846. We keep the timeout same for both NWI and NWE modes*/
          ctimer_set(&nms.timer, CLOCK_SECOND*2, timeout, 0);
      }

    }else if (ev == NM_EV_NODE_ADD || ev == NM_EV_NODE_ADD_S2)
    {
      ZW_AddNodeToNetwork(*((uint8_t*) event_data), AddNodeStatusUpdate);
      nms.state = NM_WAITING_FOR_ADD;

      memset(&nms.buf, 0, sizeof(nms.buf));
      nms.cmd = NODE_ADD;
      nms.buf.ZW_NodeAddStatus1byteFrame.cmdClass = COMMAND_CLASS_NETWORK_MANAGEMENT_INCLUSION;
      nms.buf.ZW_NodeAddStatus1byteFrame.cmd = NODE_ADD_STATUS;
      nms.buf.ZW_NodeAddStatus1byteFrame.seqNo = nms.seq;
      nms.buf.ZW_NodeAddStatus1byteFrame.nodeInfoLength = 1;
      nms.buf_len = nms.buf.ZW_NodeAddStatus1byteFrame.nodeInfoLength + 6;

      if(ev == NM_EV_NODE_ADD_S2) {
        nms.flags = NMS_FLAG_S2_ADD;
      }
      ctimer_set(&nms.timer, ADD_REMOVE_TIMEOUT * 10, timeout, 0);
    } else if (ev == NM_EV_REPLACE_FAILED_START || ev == NM_EV_REPLACE_FAILED_START_S2)
    {
       ZW_FAILED_NODE_REPLACE_FRAME* f = (ZW_FAILED_NODE_REPLACE_FRAME*) event_data;
       ZW_FAILED_NODE_REPLACE_STATUS_FRAME_EX* reply = (ZW_FAILED_NODE_REPLACE_STATUS_FRAME_EX*) &nms.buf;
       nms.cmd = FAILED_NODE_REPLACE;
       nms.tmp_node = f->nodeId;
       nms.state = NM_REPLACE_FAILED_REQ;

       reply->cmdClass = COMMAND_CLASS_NETWORK_MANAGEMENT_INCLUSION;
       reply->cmd = FAILED_NODE_REPLACE_STATUS;
       reply->seqNo = nms.seq;
       reply->nodeId = nms.tmp_node;
       reply->status = ZW_FAILED_NODE_REPLACE_FAILED;
       reply->kexFailType=0x00;
       reply->grantedKeys =0x00;
       nms.buf_len = sizeof(ZW_FAILED_NODE_REPLACE_STATUS_FRAME_EX);

       if(ev == NM_EV_REPLACE_FAILED_START_S2) {
         nms.flags = NMS_FLAG_S2_ADD;
       }
       DBG_PRINTF("Replace failed, node %i \n",f->nodeId);
       if (ZW_ReplaceFailedNode(f->nodeId, f->txOptions != TRANSMIT_OPTION_LOW_POWER,
           ReplaceFailedNodeStatus) == ZW_FAILED_NODE_REMOVE_STARTED)
       {
         ctimer_set(&nms.timer, ADD_REMOVE_TIMEOUT * 10, timeout, 0);
       } else {
         ERR_PRINTF("replace failed not started\n");
         goto send_reply;
       }
    } else if (ev == NM_EV_REQUEST_NODE_LIST) {
        /*I'm the SUC/SIS or i don't know the SUC/SIS*/
        SendNodeList(ZW_SUC_UPDATE_DONE);
      break;
    } else if (ev == NM_NODE_ADD_STOP) {
      DBG_PRINTF("Event  NM_NODE_ADD_STOP in NM_IDLE state\n");
      memset(&nms.buf, 0, sizeof(nms.buf.ZW_NodeAddStatus1byteFrame));
      nms.buf.ZW_NodeAddStatus1byteFrame.cmdClass = COMMAND_CLASS_NETWORK_MANAGEMENT_INCLUSION;
      nms.buf.ZW_NodeAddStatus1byteFrame.cmd = NODE_ADD_STATUS;
      nms.buf.ZW_NodeAddStatus1byteFrame.seqNo = nms.seq;
      nms.buf.ZW_NodeAddStatus1byteFrame.status = ADD_NODE_STATUS_FAILED;
      nms.buf_len = sizeof(nms.buf.ZW_NodeAddStatus1byteFrame) - 1;
      goto send_reply;
    } else if( ev == NM_EV_START_PROXY_INCLUSION || ev == NM_EV_START_PROXY_REPLACE) {
      nms.tmp_node = *((uint8_t*)event_data);
      nms.cmd = (ev == NM_EV_START_PROXY_INCLUSION) ? NODE_ADD : FAILED_NODE_REPLACE;

      ZW_RequestNodeInfo(nms.tmp_node,0);
      nms.state = NM_PROXY_INCLUSION_WAIT_NIF;
      ctimer_set(&nms.timer,5000,timeout,0);

      /* Send inclusion request to unsolicited destination */
      /* TODO: what if we do not have unsolicited destination set*/
      uip_ipaddr_copy(&nms.conn.lipaddr, &cfg.lan_addr);
      uip_ipaddr_copy(&nms.conn.ripaddr, &cfg.unsolicited_dest);
      nms.conn.rport = UIP_HTONS(cfg.unsolicited_port);

    }
    break;
  case NM_REPLACE_FAILED_REQ:
    if(ev ==NM_EV_TIMEOUT || ev == NM_EV_REPLACE_FAILED_STOP || ev == NM_EV_REPLACE_FAILED_FAIL) {
      ZW_AddNodeToNetwork(ADD_NODE_STOP,0);
      goto send_reply;
    } if(ev == NM_EV_REPLACE_FAILED_DONE) {
      int common_flags;
      nms.state = NM_WAIT_FOR_SECURE_ADD;

      /*Cache security flags*/
      if(nms.flags & NMS_FLAG_PROXY_INCLUSION){
        LEARN_INFO *inf = (LEARN_INFO *) event_data;
        uint8_t* nif = inf->pCmd+3;
        common_flags =0;
        if(is_cc_in_nif(nif,inf->bLen-3,COMMAND_CLASS_SECURITY)) common_flags |=NODE_FLAG_SECURITY0;
        if(is_cc_in_nif(nif,inf->bLen-3,COMMAND_CLASS_SECURITY_2)) common_flags |=(NODE_FLAG_SECURITY2_ACCESS |NODE_FLAG_SECURITY2_UNAUTHENTICATED | NODE_FLAG_SECURITY2_AUTHENTICATED);
      } else {
        common_flags = GetCacheEntryFlag(nms.tmp_node);
      }

      ApplicationControllerUpdate(UPDATE_STATE_DELETE_DONE, nms.tmp_node, 0, 0);

      rd_probe_lock(TRUE);
      rd_register_new_node(nms.tmp_node, TRUE);

#if 0
      /* This is to keep the probe from doing a secure commands supported get */
      SetCacheEntryFlagMasked(nms.tmp_node,
      NODE_FLAG_INFO_ONLY | NODE_FLAG_SECURITY0 | NODE_FLAG_KNOWN_BAD,
      NODE_FLAG_INFO_ONLY | NODE_FLAG_SECURITY0 | NODE_FLAG_KNOWN_BAD);
      rd_probe_lock(FALSE);
#endif
        uint8_t suc_node = ZW_GetSUCNodeID();

        if(suc_node != MyNodeID &&  SupportsCmdClass(suc_node, COMMAND_CLASS_INCLUSION_CONTROLLER)) {
          ctimer_set(&nms.timer,CLOCK_SECOND*2,timeout,0);
          nms.state = NM_PREPARE_SUC_INCLISION;
          return;
        }

      if( (nms.flags & NMS_FLAG_S2_ADD) &&
          (common_flags & (NODE_FLAG_SECURITY2_ACCESS |NODE_FLAG_SECURITY2_UNAUTHENTICATED | NODE_FLAG_SECURITY2_AUTHENTICATED)))
      {
        sec2_start_add_node(nms.tmp_node, SecureInclusionDone);
        return;
      }


      if(common_flags & NODE_FLAG_SECURITY0) {
        if(nms.flags & NMS_FLAG_PROXY_INCLUSION ) {
          inclusion_controller_you_do_it(SecureInclusionDone);
          return;
        } else {
          security_add_begin(nms.tmp_node, nms.txOptions,
              isNodeController(nms.tmp_node), SecureInclusionDone);
          return;
        }
      }

      /*This is a non secure node or the node has already been included securely*/
      ZW_FAILED_NODE_REPLACE_STATUS_FRAME_EX* reply = (ZW_FAILED_NODE_REPLACE_STATUS_FRAME_EX*) &nms.buf;
      reply->status = ZW_FAILED_NODE_REPLACE_DONE;

      nm_fsm_post_event(NM_EV_SECURITY_DONE, &zero);
    }
    break;
  case NM_WAITING_FOR_ADD:
    if (ev == NM_NODE_ADD_STOP || ev == NM_EV_TIMEOUT)
    {
      nms.buf.ZW_NodeAddStatus1byteFrame.status = ADD_NODE_STATUS_FAILED;
      ZW_AddNodeToNetwork(ADD_NODE_STOP, AddNodeStatusUpdate);
      goto send_reply;
    }
    else if (ev == NM_EV_ADD_NODE_FOUND)
    {
      nms.state = NM_NODE_FOUND;
      ctimer_set(&nms.timer,CLOCK_SECOND*60,timeout,0);
    }
    break;
  case NM_NODE_FOUND:
    if (ev == NM_EV_ADD_CONTROOLER || ev == NM_EV_ADD_SLAVE)
    {
      LEARN_INFO *inf = (LEARN_INFO *) event_data;
      if (inf->bLen && (inf->bSource!=0) )
      {
        ctimer_set(&nms.timer,CLOCK_SECOND*60,timeout,0);
        nms.tmp_node = inf->bSource;
        nms.buf.ZW_NodeAddStatus1byteFrame.newNodeId = inf->bSource;
        nms.buf.ZW_NodeAddStatus1byteFrame.nodeInfoLength = inf->bLen + 3;
        memcpy(&(nms.buf.ZW_NodeAddStatus1byteFrame.basicDeviceClass), inf->pCmd, inf->bLen);
        nms.state = NM_WIAT_FOR_PROTOCOL;
      } else {
        nm_fsm_post_event(NM_EV_ADD_FAILED, 0);
      }
    } else if(ev == NM_EV_ADD_FAILED || ev == NM_EV_TIMEOUT ){
      nms.buf.ZW_NodeAddStatus1byteFrame.status = ADD_NODE_STATUS_FAILED;

      /* Add node failed - Application should indicate this to user */
      ZW_AddNodeToNetwork(ADD_NODE_STOP_FAILED, NULL);

      rd_probe_lock(FALSE); //Unlock the probe machine
      nms.buf_len = nms.buf.ZW_NodeAddStatus1byteFrame.nodeInfoLength + 6;
      goto send_reply;
    }
    break;
  case NM_WIAT_FOR_PROTOCOL:
    if (ev == NM_EV_ADD_PROTOCOL_DONE)
    {
      ZW_AddNodeToNetwork(ADD_NODE_STOP, AddNodeStatusUpdate);
    }
    else if (ev == NM_EV_ADD_NODE_STATUS_DONE)
    {
      NODEINFO ni;

      /* It is recommended to stop the process again here */
      ZW_AddNodeToNetwork(ADD_NODE_STOP, NULL);

      /* Get the Capabilities and Security fields. */
      ZW_GetNodeProtocolInfo(nms.tmp_node, &ni);
      nms.buf.ZW_NodeAddStatus1byteFrame.properties1 = ni.capability;
      nms.buf.ZW_NodeAddStatus1byteFrame.properties2 = ni.security;

      nms.state = NM_WAIT_FOR_SECURE_ADD;

      if((rd_node_exists(nms.tmp_node)) && !(nms.flags & NMS_FLAG_PROXY_INCLUSION ))
      {
        int flags = GetCacheEntryFlag(nms.tmp_node);
        ERR_PRINTF("This node has already been included\n");
        /*This node has already been included*/
        nm_fsm_post_event(NM_EV_SECURITY_DONE, &flags);
        return;
      } else {
        uint8_t suc_node = ZW_GetSUCNodeID();

        rd_probe_lock(TRUE);
        rd_register_new_node(nms.tmp_node, TRUE);
        ApplicationControllerUpdate(UPDATE_STATE_NEW_ID_ASSIGNED, nms.tmp_node, 0, 0);

        if(suc_node != MyNodeID &&  SupportsCmdClass(suc_node, COMMAND_CLASS_INCLUSION_CONTROLLER)) {
          ctimer_set(&nms.timer,CLOCK_SECOND*2,timeout,0);
          nms.state = NM_PREPARE_SUC_INCLISION;
          return;
        }


        /*Security 2 inclusion if this nodes supports a S2 key, and we are asked to try an s2 inclusion*/
        if ((nms.flags & NMS_FLAG_S2_ADD) &&
            (GetCacheEntryFlag(MyNodeID) &
            (NODE_FLAG_SECURITY2_ACCESS |NODE_FLAG_SECURITY2_UNAUTHENTICATED | NODE_FLAG_SECURITY2_AUTHENTICATED))
            )
        {
          if (is_cc_in_nif(&nms.buf.ZW_NodeAddStatus1byteFrame.commandClass1,
              nms.buf.ZW_NodeAddStatus1byteFrame.nodeInfoLength - 3, COMMAND_CLASS_SECURITY_2))
          {
            sec2_start_add_node(nms.tmp_node, SecureInclusionDone);
            return;
          }
        }

        if (GetCacheEntryFlag(MyNodeID) & (NODE_FLAG_SECURITY0))
        {
          /*Security 0 inclusion*/
          if (is_cc_in_nif(&nms.buf.ZW_NodeAddStatus1byteFrame.commandClass1,
              nms.buf.ZW_NodeAddStatus1byteFrame.nodeInfoLength - 3, COMMAND_CLASS_SECURITY))
          {
            if(nms.flags & NMS_FLAG_PROXY_INCLUSION ) {
              inclusion_controller_you_do_it(SecureInclusionDone);
            } else {
              security_add_begin(nms.tmp_node, nms.txOptions,
                  isNodeController(nms.tmp_node), SecureInclusionDone);
            }
            return;
          }
        }
        /* This is a non secure node */
        nm_fsm_post_event(NM_EV_SECURITY_DONE, &zero);
      }
    }
    else if (ev == NM_EV_TIMEOUT || ev == NM_EV_ADD_FAILED || ev == NM_EV_ADD_NOT_PRIMARY)
    {
      nms.buf.ZW_NodeAddStatus1byteFrame.status = ADD_NODE_STATUS_FAILED;

      /* Add node failed - Application should indicate this to user */
      ZW_AddNodeToNetwork(ADD_NODE_STOP_FAILED, NULL);

      rd_probe_lock(FALSE); //Unlock the probe machine
      nms.buf_len = nms.buf.ZW_NodeAddStatus1byteFrame.nodeInfoLength + 6;
      goto send_reply;
    }
    break;
  case NM_PREPARE_SUC_INCLISION:
    if(ev == NM_EV_TIMEOUT) {
      nms.state = NM_WIAT_FOR_SUC_INCLUSION;
      request_inclusion_controller_handover(
          nms.tmp_node,
          (nms.cmd == FAILED_NODE_REPLACE),
          &inclusion_controller_complete );
    }
    break;
  case NM_WAIT_FOR_SECURE_ADD:
    if (ev == NM_EV_SECURITY_DONE || ev == NM_NODE_ADD_STOP)
    {
      int inclusion_flags;

      if(ev == NM_NODE_ADD_STOP) {
        sec2_key_grant(0,0,0);
        sec2_dsk_accept(0,0);
        inclusion_flags = NODE_FLAG_KNOWN_BAD;
      } else {
        inclusion_flags = (*(int*) event_data);
      }

      /*If status has not yet already been set use the result of the secure add*/

      if(nms.cmd == NODE_ADD) {
      nms.buf.ZW_NodeAddStatus1byteFrame.status =
          inclusion_flags & NODE_FLAG_KNOWN_BAD ? ADD_NODE_STATUS_SECURITY_FAILED : ADD_NODE_STATUS_DONE;
      } else {
        ZW_FAILED_NODE_REPLACE_STATUS_FRAME_EX* reply = (ZW_FAILED_NODE_REPLACE_STATUS_FRAME_EX*) &nms.buf;

        reply->status =
            inclusion_flags & NODE_FLAG_KNOWN_BAD ? ADD_NODE_STATUS_SECURITY_FAILED : ZW_FAILED_NODE_REPLACE_DONE;
        reply->grantedKeys = sec2_gw_node_flags2keystore_flags(inclusion_flags & 0xFF);
        reply->kexFailType = (inclusion_flags >> 16) & 0xFF; //TODO
      }
      SetCacheEntryFlagMasked(nms.tmp_node, inclusion_flags & 0xFF, NODE_FLAGS_SECURITY);

      /* Create a new ECDH pair for next inclusion.*/
      sec2_create_new_dynamic_ecdh_key();

      nms.state = NM_WAIT_FOR_PROBE_AFTER_ADD;
      rd_register_ep_probe_notifier(nms.tmp_node, 0, &nms, NodeProbeDone);

      /*Re-discover this node as it now has secure classes and its nif might have changed.*/
      rd_probe_lock(FALSE);
    } else if (ev == NM_EV_ADD_SECURITY_REQ_KEYS) { //We dont leave the NM_WAIT_FOR_SECURE_ADD here because here we a just proxying for the Security FSM
      s2_node_inclusion_request_t *req = (s2_node_inclusion_request_t*) (event_data);
      ZW_NODE_ADD_KEYS_REPORT_FRAME_EX f;

      f.cmdClass = COMMAND_CLASS_NETWORK_MANAGEMENT_INCLUSION;
      f.cmd = NODE_ADD_KEYS_REPORT;
      f.seqNo = nms.seq;
      f.requested_keys = req->security_keys;
      f.request_csa = req->csa;
      ZW_SendDataZIP(&nms.conn,&f,sizeof(f),0);
    } else if (ev == NM_EV_ADD_SECURITY_KEY_CHALLANGE) {
      ZW_NODE_ADD_DSK_REPORT_FRAME_EX f;
      s2_node_inclusion_challenge_t *challenge_evt = (s2_node_inclusion_challenge_t*) (event_data);
      f.cmdClass = COMMAND_CLASS_NETWORK_MANAGEMENT_INCLUSION;
      f.cmd = NODE_ADD_DSK_REPORT;
      f.seqNo = nms.seq;
      /* Input DSK length is 2 for ACCESS and AUTHENTICATED keys, zero otherwise */
      if (challenge_evt->granted_keys &
          (KEY_CLASS_S2_ACCESS | KEY_CLASS_S2_AUTHENTICATED))
      {
        f.reserved_dsk_len = 2;
      } else {
        f.reserved_dsk_len = 0;
      }
      memcpy(f.dsk, challenge_evt->public_key,16);
      ZW_SendDataZIP(&nms.conn,&f,sizeof(f),0);
    } else if(ev == NM_EV_ADD_SECURITY_KEYS_SET) {
      ZW_NODE_ADD_KEYS_SET_FRAME_EX* f = (ZW_NODE_ADD_KEYS_SET_FRAME_EX*) event_data;
      sec2_key_grant(f->reserved_accept & NODE_ADD_KEYS_SET_EX_ACCEPT_BIT,f->granted_keys, (f->reserved_accept & NODE_ADD_KEYS_SET_EX_CSA_BIT) > 0 );
    } else if(ev == NM_EV_ADD_SECURITY_DSK_SET) {
      ZW_NODE_ADD_DSK_SET_FRAME_EX* f = (ZW_NODE_ADD_DSK_SET_FRAME_EX*) event_data;
      DBG_PRINTF("DSK accept bit %u, dsk len %u\n",
          f->accet_reserved_dsk_len & NODE_ADD_DSK_SET_EX_ACCEPT_BIT,
          f->accet_reserved_dsk_len & NODE_ADD_DSK_SET_DSK_LEN_MASK);
      sec2_dsk_accept((f->accet_reserved_dsk_len & NODE_ADD_DSK_SET_EX_ACCEPT_BIT)>0, f->dsk );
    }
    break;
   case NM_WAIT_FOR_PROBE_AFTER_ADD:
    if (ev == NM_EV_NODE_PROBE_DONE)
    {
      rd_ep_database_entry_t* ep = (rd_ep_database_entry_t*) event_data;
      ZW_NODE_ADD_STATUS_1BYTE_FRAME* r = (ZW_NODE_ADD_STATUS_1BYTE_FRAME*) &nms.buf;
      uip_ipv4addr_t a;
      int len;
      if(nms.cmd == NODE_ADD) {
        if (ep->state == EP_STATE_PROBE_DONE)
        {
          /* Filled in earlier
           * r->properties1;
           r->properties2;
           */
          r->basicDeviceClass = ep->node->nodeType;
          r->genericDeviceClass = ep->endpoint_info[0];
          r->specificDeviceClass = ep->endpoint_info[1];

          /* Add all occurences of COMMAND_CLASS_ASSOCIATION
           * with _IP_ASSOCIATION */
          size_t max = (sizeof(nms.buf) - offsetof(ZW_NODE_ADD_STATUS_1BYTE_FRAME,commandClass1));
          len = mem_insert(&r->commandClass1, &ep->endpoint_info[2],
          COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_IP_ASSOCIATION, ep->endpoint_info_len - 2, max);

          r->nodeInfoLength = 6 + len ;
        }
        else
        {
          //r->nodeInfoLength = 1;
        }
        nms.buf_len = r->nodeInfoLength + 6;
      }

      ((uint8_t*)&nms.buf)[nms.buf_len] = sec2_gw_node_flags2keystore_flags(GetCacheEntryFlag(nms.tmp_node)); //Granted keys
      ((uint8_t*)&nms.buf)[nms.buf_len+1] = 0;
      nms.buf_len +=2;

      nms.state = NM_WAIT_DHCP;

      /*Check if ip address has already been assigned*/
      if (cfg.ipv4disable || ipv46nat_ipv4addr_of_node(&a, nms.tmp_node))
      {
        goto send_reply;

      }

      ctimer_set(&nms.timer, 5000, timeout, 0);
    }
    break;
  case NM_WAIT_DHCP:
    if (ev == NM_EV_DHCP_DONE && nms.tmp_node == *(uint8_t*) event_data)
    {
      goto send_reply;
    }
    else if (ev == NM_EV_TIMEOUT)
    {
      goto send_reply;
    }
    break;
  case NM_SET_DEFAULT:
    if( ev == NM_EV_MDNS_EXIT ) {
      ApplicationDefaultSet();
      bridge_reset();

      /*Register that we have entered a new network */
      process_post_synch(&zip_process, ZIP_EVENT_NEW_NETWORK, 0);

      /*Create an async application reset */
      process_post(&zip_process, ZIP_EVENT_RESET, 0);
      controller_role = SUC;
      SendReplyWhenNetworkIsUpdated();
    }
    break;
  case NM_WAIT_FOR_MDNS:
    if( ev == NM_EV_MDNS_EXIT ) {
      if(!(nms.flags & NMS_FLAG_CONTROLLER_REPLICATION)) {
        bridge_reset();
      }

      if (nms.flags & NMS_FLAG_LEARNMODE_NEW)
      {
      
        ZW_SendDataZIP(&nms.conn, (BYTE*) &nms.buf, nms.buf_len, 0);

        ZW_LEARN_MODE_SET_STATUS_FRAME_EX *f = (ZW_LEARN_MODE_SET_STATUS_FRAME_EX*) &nms.buf;

        f->status = LEARN_MODE_INTERVIEW_COMPLETED;
        nms.buf_len = sizeof(ZW_LEARN_MODE_SET_STATUS_FRAME_EX);

        nms.state = NM_WAIT_FOR_PROBE_BY_SIS;
        ctimer_set(&nms.timer, 8000, timeout, 0);
      } else {
          process_post(&zip_process, ZIP_EVENT_RESET, 0);
          SendReplyWhenNetworkIsUpdated();    
      }
    }
    break;
  case NM_WAIT_FOR_PROBE_BY_SIS:
    if( ev == NM_EV_TIMEOUT ) {
        /* rd_probe_lock(FALSE); dont unlock here, we will reset and then unlock */
        /*Create an async application reset */
        process_post(&zip_process, ZIP_EVENT_RESET, 0);
        /* If its controller replication wait until probing is done to send LEARN_MODE_INTERVIEW_COMPLETED */ 
        if ((nms.flags & NMS_FLAG_LEARNMODE_NEW ) && !(nms.flags & NMS_FLAG_CONTROLLER_REPLICATION))
        {
            DBG_PRINTF("Sending LEARN_MODE_INTERVIEW_COMPLETED\n");
            SendReplyWhenNetworkIsUpdated();
        }
    } else if (ev  == NM_EV_FRAME_RECEIVED) {
      ctimer_set(&nms.timer, 8000, timeout, 0);
    } else if (ev == NM_EV_ALL_PROBED) {
        /* We get this even only when its controller replication*/
        DBG_PRINTF("Sending LEARN_MODE_INTERVIEW_COMPLETED1\n");
        SendReplyWhenNetworkIsUpdated();
    }
    break;

  case NM_WAIT_FOR_SECURE_LEARN:
    if(ev == NM_EV_S0_STARTED) {


      /* Make temporary NIF, used for inclusion */
      SetPreInclusionNIF(SECURITY_SCHEME_0);

      /*Stop the S2 FSM */
      sec2_abort_join();
    } else if(ev == NM_EV_ADD_SECURITY_KEY_CHALLANGE) {

      /*Update Command classes, according to our new network state. */
      SetPreInclusionNIF(SECURITY_SCHEME_2_ACCESS);

      ZW_LEARN_MODE_SET_STATUS_FRAME_EX *f = (ZW_LEARN_MODE_SET_STATUS_FRAME_EX*) &nms.buf;
      s2_node_inclusion_challenge_t *challenge_evt = (s2_node_inclusion_challenge_t *)event_data;


      /*Fill in the dsk part of the answer, the rest of the answer is filled in later*/
      memcpy(f->dsk,challenge_evt->public_key,32);
      sec2_dsk_accept(1,f->dsk);
    } else if(ev == NM_EV_SECURITY_DONE) {
      ZW_LEARN_MODE_SET_STATUS_FRAME_EX *f = (ZW_LEARN_MODE_SET_STATUS_FRAME_EX*) &nms.buf;
      int inclusion_flags;

      inclusion_flags = *(int*) event_data;

      security_init();
      DBG_PRINTF("inclusion flags ...... %x\n",inclusion_flags);
      SetCacheEntryFlagMasked(MyNodeID, inclusion_flags & 0xFF, NODE_FLAGS_SECURITY);
      f->cmdClass =
      COMMAND_CLASS_NETWORK_MANAGEMENT_BASIC;
      f->cmd = LEARN_MODE_SET_STATUS;
      f->reserved = 0;
      f->seqNo = nms.seq;
      f->newNodeId = MyNodeID;
      f->granted_keys = sec2_gw_node_flags2keystore_flags(inclusion_flags & 0xFF);
      f->kexFailType = (inclusion_flags >> 16) & 0xFF;
      f->status = (NODE_FLAG_KNOWN_BAD & inclusion_flags) ? ADD_NODE_STATUS_SECURITY_FAILED : ADD_NODE_STATUS_DONE; 
      nms.buf_len = sizeof(ZW_LEARN_MODE_SET_STATUS_FRAME_EX);
      nms.state = NM_WAIT_FOR_MDNS;


      /*This is a new network, start sending mDNS goodbye messages,  */
      /* NetworkManagement_mdns_exited will be called at some point */
      rd_exit();
    } else if(ev == NM_EV_LEARN_SET) {
      ZW_LEARN_MODE_SET_FRAME* f = (ZW_LEARN_MODE_SET_FRAME*)event_data;
      if(f->mode == ZW_SET_LEARN_MODE_DISABLE) {
        nms.seq = f->seqNo; //Just because this was how we did in 2.2x
        sec2_abort_join();
      }
    }
    break;
  case NM_WIAT_FOR_SUC_INCLUSION:
    if(ev == NM_EV_PROXY_COMPLETE) {



      int flags = NODE_FLAG_SECURITY0 |
          NODE_FLAG_SECURITY2_UNAUTHENTICATED|
          NODE_FLAG_SECURITY2_AUTHENTICATED|
          NODE_FLAG_SECURITY2_ACCESS; /*TODO this is not a proper view*/
      nms.state = NM_WAIT_FOR_SECURE_ADD;
      nm_fsm_post_event(NM_EV_SECURITY_DONE, &flags);
    }
    break;
  case NM_PROXY_INCLUSION_WAIT_NIF:
    if(ev == NM_EV_TIMEOUT) {
      nms.state = NM_IDLE;
      inclusion_controller_send_report(INCLUSION_CONTROLLER_STEP_FAILED);
    } else if ( ev == NM_EV_NODE_INFO ) {
      NODEINFO ni;

      if(nms.cmd == NODE_ADD) {
        memset(&nms.buf, 0, sizeof(nms.buf));
        nms.buf.ZW_NodeAddStatus1byteFrame.cmdClass = COMMAND_CLASS_NETWORK_MANAGEMENT_INCLUSION;
        nms.buf.ZW_NodeAddStatus1byteFrame.cmd = NODE_ADD_STATUS;
        nms.buf.ZW_NodeAddStatus1byteFrame.seqNo = nms.seq;
        nms.buf.ZW_NodeAddStatus1byteFrame.nodeInfoLength = 1;
        nms.buf_len = nms.buf.ZW_NodeAddStatus1byteFrame.nodeInfoLength + 6;

        /* Get the Capabilities and Security fields. */
        ZW_GetNodeProtocolInfo(nms.tmp_node, &ni);
        nms.buf.ZW_NodeAddStatus1byteFrame.properties1 = ni.capability;
        nms.buf.ZW_NodeAddStatus1byteFrame.properties2 = ni.security;

        nms.flags = NMS_FLAG_S2_ADD| NMS_FLAG_PROXY_INCLUSION;

        /* Stimulate the add process */
        nms.state = NM_NODE_FOUND;
        nm_fsm_post_event(NM_EV_ADD_CONTROOLER,event_data);

        nm_fsm_post_event(NM_EV_ADD_NODE_STATUS_DONE,event_data);

      } else {
        ZW_FAILED_NODE_REPLACE_STATUS_FRAME_EX* reply = (ZW_FAILED_NODE_REPLACE_STATUS_FRAME_EX*) &nms.buf;
        nms.cmd = FAILED_NODE_REPLACE;
        nms.state = NM_REPLACE_FAILED_REQ;
        reply->cmdClass = COMMAND_CLASS_NETWORK_MANAGEMENT_INCLUSION;
        reply->cmd = FAILED_NODE_REPLACE_STATUS;
        reply->seqNo = nms.seq;
        reply->nodeId = nms.tmp_node;
        reply->status = ZW_FAILED_NODE_REPLACE_FAILED;
        reply->kexFailType=0x00;
        reply->grantedKeys =0x00;
        nms.buf_len = sizeof(ZW_FAILED_NODE_REPLACE_STATUS_FRAME_EX);

        nms.flags = NMS_FLAG_S2_ADD| NMS_FLAG_PROXY_INCLUSION;
        nm_fsm_post_event(NM_EV_REPLACE_FAILED_DONE,event_data);
      }

    }
    break;
  case NM_LEARN_MODE:
  {
    if(ev == NM_EV_TIMEOUT) {
      if(nms.count == 0) {
        if(nms.flags & NMS_FLAG_LEARNMODE_NWI) {
          ZW_SetLearnMode(ZW_SET_LEARN_MODE_NWI,LearnModeStatus);
        } else if(nms.flags & NMS_FLAG_LEARNMODE_NWE) {
          ZW_SetLearnMode(ZW_SET_LEARN_MODE_NWE,LearnModeStatus);
        }
      }

      if( (nms.flags & (NMS_FLAG_LEARNMODE_NWI | NMS_FLAG_LEARNMODE_NWE)) && (nms.count < 4)) {
        if(nms.flags & NMS_FLAG_LEARNMODE_NWI) {
          ZW_ExploreRequestInclusion();
        } else {
          ZW_ExploreRequestExclusion();
        }

        int delay =  CLOCK_SECOND*4 + (rand() & 0xFF);
        ctimer_set(&nms.timer,delay, timeout, 0);
        nms.count++;
      } else {
        LearnTimerExpired();
      }
    } else if(ev == NM_EV_LEARN_SET) {
      ZW_LEARN_MODE_SET_FRAME* f = (ZW_LEARN_MODE_SET_FRAME*) event_data;

      if (f->mode == ZW_SET_LEARN_MODE_DISABLE)
      {
        nms.seq = f->seqNo; //Just because this was how we did in 2.2x
        LearnTimerExpired();
      }
    }
    break;
  }
  case NM_LEARN_MODE_STARTED:
  case NM_NETWORK_UPDATE:
  case NM_WAITING_FOR_PROBE:
  case NM_REMOVING_ASSOCIATIONS:
  case NM_SENDING_NODE_INFO:
  case NM_WAITING_FOR_NODE_REMOVAL:
  case NM_WAITING_FOR_FAIL_NODE_REMOVAL:
  case NM_WAITING_FOR_NODE_NEIGH_UPDATE:
  case NM_WAITING_FOR_RETURN_ROUTE_ASSIGN:
  case NM_WAITING_FOR_RETURN_ROUTE_DELETE:

    break;
  }
  return;

  send_reply:
//  nms.state = NM_IDLE; Reset state will sent the FSM to IDLE
  if(nms.flags & NMS_FLAG_PROXY_INCLUSION) {
    if(nms.buf.ZW_NodeAddStatus1byteFrame.status  == ADD_NODE_STATUS_DONE) {
      inclusion_controller_send_report(INCLUSION_CONTROLLER_STEP_OK);
    } else {
      inclusion_controller_send_report(INCLUSION_CONTROLLER_STEP_FAILED);
    }
  }
  nm_send_reply(&nms.buf, nms.buf_len);
}

void NetworkManagement_nif_notify(uint8_t bNodeID,uint8_t* pCmd,uint8_t bLen) {
  LEARN_INFO info;
  info.bStatus = ADD_NODE_STATUS_ADDING_SLAVE;
  info.bSource = bNodeID;
  info.pCmd = pCmd;
  info.bLen = bLen;

  nm_fsm_post_event(NM_EV_NODE_INFO,&info);
}

void NetworkManagement_dsk_challenge(s2_node_inclusion_challenge_t *challenge_evt) {
  nm_fsm_post_event(NM_EV_ADD_SECURITY_KEY_CHALLANGE,challenge_evt);
}

void NetworkManagement_key_request(s2_node_inclusion_request_t* inclusion_request) {
  nm_fsm_post_event(NM_EV_ADD_SECURITY_REQ_KEYS,inclusion_request);
}

void NetworkManagement_start_proxy_inclusion(uint8_t node_id) {
  nm_fsm_post_event(NM_EV_START_PROXY_INCLUSION, &node_id);
}

void NetworkManagement_start_proxy_replace(uint8_t node_id) {
  nm_fsm_post_event(NM_EV_START_PROXY_REPLACE, &node_id);
}

void NetworkManagement_all_nodes_probed()
{
    if (nms.flags & NMS_FLAG_CONTROLLER_REPLICATION) {
         nm_fsm_post_event(NM_EV_ALL_PROBED, 0);
    }
}

static void
SecureInclusionDone(int status)
{
  nm_fsm_post_event(NM_EV_SECURITY_DONE, &status);
}

static void
timeout(void* none)
{
  nm_fsm_post_event(NM_EV_TIMEOUT,0);
}

void
NetworkManagement_IPv4_assigned(u8_t node)
{
  nm_fsm_post_event(NM_EV_DHCP_DONE,&node);

  if (nms.waiting_for_ipv4_addr == node)
  {
    nms.waiting_for_ipv4_addr = 0;
    nm_send_reply(&nms.buf, nms.buf_len);
    ZW_LTimerCancel(nms.networkManagementTimer);
  }
}

void
NetworkManagement_VirtualNodes_removed()
{
  if (nms.state == NM_REMOVING_ASSOCIATIONS)
  {
    nm_send_reply(&nms.buf, nms.buf_len);
    ZW_LTimerCancel(nms.networkManagementTimer);
  }
}

static BYTE networkUpdateStatusFlags;
/**
 * Event trigger called when a node gets a new IPV4 address.
 */
void
NetworkManagement_NetworkUpdateStatusUpdate(u8_t flag)
{
  networkUpdateStatusFlags |= flag;

  if (ipv46nat_all_nodes_has_ip() || cfg.ipv4disable )
  {
    networkUpdateStatusFlags |= NETWORK_UPDATE_FLAG_DHCPv4;
  }

  DBG_PRINTF("update flag %i %i\n",flag,networkUpdateStatusFlags);
  if (networkUpdateStatusFlags
      == (NETWORK_UPDATE_FLAG_DHCPv4 | NETWORK_UPDATE_FLAG_PROBE | NETWORK_UPDATE_FLAG_VIRTUAL))
  {
    nm_send_reply(&nms.buf, nms.buf_len);
    ZW_LTimerCancel(nms.networkManagementTimer);
  }
}

static void
network_update_timeout()
{
  NetworkManagement_NetworkUpdateStatusUpdate(
  NETWORK_UPDATE_FLAG_DHCPv4 | NETWORK_UPDATE_FLAG_PROBE);
}

/**
 * Setup the transmission of nms.buf when the Network update, node probing and Ipv4 assignment of
 * new nodes has completed.
 */
static void
SendReplyWhenNetworkIsUpdated()
{

  if (nms.networkManagementTimer != 0xFF)
  {
    ZW_LTimerCancel(nms.networkManagementTimer);
  }
  nms.state = NM_WAITING_FOR_PROBE;
  networkUpdateStatusFlags = 0;

  if ((bridge_state == initialized) || (controller_role != SUC))
  {
    networkUpdateStatusFlags |= NETWORK_UPDATE_FLAG_VIRTUAL;
  }

  /* Wait 65 secs */
  nms.networkManagementTimer = ZW_LTimerStart(network_update_timeout, 0xFFFF, TIMER_ONE_TIME);

  rd_probe_lock(FALSE);

  /*Check the we actually allocated the timer */
  if (nms.networkManagementTimer == 0xFF)
  {
    network_update_timeout();
  }
}

/**
 * Reset the network managemnet state.
 */
static void
ResetState(BYTE dummy, void* user)
{
  ZW_LTimerCancel(nms.networkManagementTimer);
  nms.networkManagementTimer = 0xFF;
  nms.cmd = 0;
  nms.waiting_for_ipv4_addr = 0;
  nms.buf_len = 0;
  networkUpdateStatusFlags = 0x80;
  nms.state = NM_IDLE;
  process_post(&zip_process, ZIP_EVENT_NETWORK_MANAGEMENT_DONE, 0);
}
static void
__ResetState(BYTE dummy)
{
  ResetState(dummy, 0);
}

/**
 * Timeout of learn mode.
 */
static void
LearnTimerExpired(void)
{
  LEARN_INFO inf;
  LOG_PRINTF("Learn timed out or canceled\n");
  /*Restore command classes as they were */
  ApplicationInitSW();
  ZW_SetLearnMode(ZW_SET_LEARN_MODE_DISABLE, 0);
  inf.bStatus = LEARN_MODE_FAILED;
  LearnModeStatus(&inf);
}

/**
 * Timeout for remove node.
 */
static void
RemoveTimerExpired(void)
{
  LEARN_INFO inf;

  ZW_LTimerCancel(nms.addRemoveNodeTimerHandle);
  LOG_PRINTF("Remove timed out or canceled\n");
  ZW_RemoveNodeFromNetwork(REMOVE_NODE_STOP, 0);
  inf.bStatus = REMOVE_NODE_STATUS_FAILED;
  inf.bSource = 0;
  RemoveNodeStatusUpdate(&inf);
}

/**
 * Generic wrapper to send a reply to the host whom we are talking to.
 */
static void
nm_send_reply(void* buf, u8_t len)
{
  DBG_PRINTF("Sending network management reply\n");
  ZW_SendDataZIP(&nms.conn, (BYTE*) buf, len, ResetState);
}

/**
 * Called after info update of a newly included node. After the secure part of the inclusion.
 */
static void
NodeProbeDone(rd_ep_database_entry_t* ep, void* user)
{
  nm_fsm_post_event(NM_EV_NODE_PROBE_DONE, ep);
}



void
NetworkManagement_s0_started()
{
  nm_fsm_post_event(NM_EV_S0_STARTED, 0);
}


void
NetworkManagement_frame_notify()
{
  nm_fsm_post_event(NM_EV_FRAME_RECEIVED, 0);
}

/**
 * Callback for ZW_AddNodeToNetwork
 */
static void
AddNodeStatusUpdate(LEARN_INFO* inf)
{
  nm_fsm_post_event(inf->bStatus, inf);
}

/**
 * Callback for remove node
 */
static void
RemoveNodeStatusUpdate(LEARN_INFO* inf)
{
  ZW_NODE_REMOVE_STATUS_FRAME* r = (ZW_NODE_REMOVE_STATUS_FRAME*) &nms.buf;
  DBG_PRINTF("RemoveNodeStatusUpdate status=%d node %d\n", inf->bStatus, inf->bSource);
  switch (inf->bStatus)
  {
  case ADD_NODE_STATUS_LEARN_READY:
    memset(&nms.buf, 0, sizeof(nms.buf));
    /* Start remove timer */
    nms.addRemoveNodeTimerHandle = ZW_LTimerStart(RemoveTimerExpired,
    ADD_REMOVE_TIMEOUT, 1);
    break;
  case REMOVE_NODE_STATUS_NODE_FOUND:
    break;
  case REMOVE_NODE_STATUS_REMOVING_SLAVE:
  case REMOVE_NODE_STATUS_REMOVING_CONTROLLER:
    r->nodeid = inf->bSource;
    break;
  case REMOVE_NODE_STATUS_DONE:
    DBG_PRINTF("Node Removed %d\n", r->nodeid);
    nms.state = NM_REMOVING_ASSOCIATIONS;
    /*Application controller update will call remove_ip_association_by_nodeid
     * which will post a ZIP_EVENT_NM_VIRT_NODE_REMOVE_DONE, which will then call
     * NetworkManagement_VirtualNodes_removed */
    ApplicationControllerUpdate(UPDATE_STATE_DELETE_DONE, r->nodeid, 0, 0);
    /*no break*/
  case REMOVE_NODE_STATUS_FAILED:
    r->cmdClass = COMMAND_CLASS_NETWORK_MANAGEMENT_INCLUSION;
    r->cmd = NODE_REMOVE_STATUS;
    r->status = inf->bStatus;
    r->seqNo = nms.seq;

    nms.buf_len = sizeof(ZW_NODE_REMOVE_STATUS_FRAME);
    if (inf->bStatus == REMOVE_NODE_STATUS_FAILED)
    {
      r->nodeid = 0;
    }

    ZW_LTimerCancel(nms.addRemoveNodeTimerHandle);
    ZW_RemoveNodeFromNetwork(REMOVE_NODE_STOP, 0);
    if (r->nodeid == 0)
    {
      nm_send_reply(r, sizeof(ZW_NODE_REMOVE_STATUS_FRAME));
    }
    break;
  }

}

/**
 * Remove failed node callback
 */
static void
RemoveFailedNodeStatus(BYTE status)
{
  ZW_FAILED_NODE_REMOVE_STATUS_FRAME* f = (ZW_FAILED_NODE_REMOVE_STATUS_FRAME*) &nms.buf;
  ;
  f->cmdClass = COMMAND_CLASS_NETWORK_MANAGEMENT_INCLUSION;
  f->cmd = FAILED_NODE_REMOVE_STATUS;
  f->seqNo = nms.seq;
  f->status = status;
  f->nodeId = nms.tmp_node;
  nm_send_reply(f, sizeof(ZW_FAILED_NODE_REMOVE_STATUS_FRAME));

  if (status == ZW_FAILED_NODE_REMOVED)
  {
    DBG_PRINTF("Failed node Removed%d\n", nms.tmp_node);
    /* Trigger the resource directory update */
    ApplicationControllerUpdate(UPDATE_STATE_DELETE_DONE, nms.tmp_node, 0, 0);
  }
}



/*
 * Replace failed node callback
 */
static void
ReplaceFailedNodeStatus(BYTE status)
{
  switch (status)
  {
  case ZW_FAILED_NODE_REPLACE:
    LOG_PRINTF("Ready to replace node....\n");
    break;
  case ZW_FAILED_NODE_REPLACE_DONE:
    nm_fsm_post_event(NM_EV_REPLACE_FAILED_DONE,0);
    break;
  case ZW_NODE_OK:

    /* no break */
  case ZW_FAILED_NODE_REPLACE_FAILED:
    nm_fsm_post_event(NM_EV_REPLACE_FAILED_FAIL,0);
    break;
  }
}

void
NetworkManagement_mdns_exited()
{
  nm_fsm_post_event(NM_EV_MDNS_EXIT,0);
}

/*
 * Set default callback
 */
static void
SetDefaultStatus()
{
  nms.buf.ZW_DefaultSetCompleteFrame.cmdClass =
  COMMAND_CLASS_NETWORK_MANAGEMENT_BASIC;
  nms.buf.ZW_DefaultSetCompleteFrame.cmd = DEFAULT_SET_COMPLETE;
  nms.buf.ZW_DefaultSetCompleteFrame.seqNo = nms.seq;
  nms.buf.ZW_DefaultSetCompleteFrame.status = DEFAULT_SET_DONE;
  nms.buf_len = sizeof(nms.buf.ZW_DefaultSetCompleteFrame);
  DBG_PRINTF("Controller reset done\n");

  rd_exit();
}

/*
 * ZW_RequestNeighborUpdate callback
 */
static void
RequestNodeNeighborUpdateStatus(BYTE status)
{

  switch (status)
  {
  case REQUEST_NEIGHBOR_UPDATE_STARTED:
    break;
  case REQUEST_NEIGHBOR_UPDATE_DONE:
  case REQUEST_NEIGHBOR_UPDATE_FAILED:
    nms.buf.ZW_NodeNeighborUpdateStatusFrame.cmdClass =
    COMMAND_CLASS_NETWORK_MANAGEMENT_INCLUSION;
    nms.buf.ZW_NodeNeighborUpdateStatusFrame.cmd =
    NODE_NEIGHBOR_UPDATE_STATUS;
    nms.buf.ZW_NodeNeighborUpdateStatusFrame.seqNo = nms.seq;
    nms.buf.ZW_NodeNeighborUpdateStatusFrame.status = status;
    nm_send_reply(&nms.buf, sizeof(nms.buf.ZW_NodeNeighborUpdateStatusFrame));
    break;
  }
}
#if 0
static void
reset_delayed()
{
  process_post(&zip_process, ZIP_EVENT_RESET, 0);
}
#endif


/**
 * Return true is this is a clean network containing only the ZIP router and
 * if this is a new network compared to what we have en RAM
 */
static void
isCleanNetwork(BOOL* clean_network, BOOL* new_network)
{
  BYTE ver, capabilities, len;
  BYTE node_list[29];
  BYTE c, v;
  DWORD h;
  BYTE n;
  BYTE i;

  MemoryGetID((BYTE*) &h, &n);
  SerialAPI_GetInitData(&ver, &capabilities, &len, node_list, &c, &v);

  *new_network = (h != homeID);

  node_list[(n - 1) >> 3] &= ~(1 << ((n - 1) & 0x7));
  for (i = 0; i < 29; i++)
  {
    if (node_list[i])
    {
      *clean_network = FALSE;
      return;
    }
  }

  *clean_network = TRUE;
}

const char *learn_mode_status_str(int ev)
{

  static char str[25];
  switch (ev)
  {
  case LEARN_MODE_STARTED              : return  "LEARN_MODE_STARTED";
  case LEARN_MODE_DONE                 : return  "LEARN_MODE_DONE";
  case LEARN_MODE_FAILED               : return  "LEARN_MODE_FAILED";
  case LEARN_MODE_INTERVIEW_COMPLETED  : return  "LEARN_MODE_INTERVIEW_COMPLETED";
  default:
    sprintf(str, "%d", ev);
    return str;
  }

}
static void
LearnModeStatus(LEARN_INFO* inf)
{
  BOOL clean_network, new_network;
  static uint8_t old_nodeid;

  if((nms.state != NM_LEARN_MODE) && (nms.state != NM_LEARN_MODE_STARTED) ) {
    ERR_PRINTF("LearnModeStatus callback while not in learn mode\n");
    return;
  }

  ZW_LEARN_MODE_SET_STATUS_FRAME* f = (ZW_LEARN_MODE_SET_STATUS_FRAME*) &nms.buf;
  DBG_PRINTF("learn mode %s\n", learn_mode_status_str(inf->bStatus));

  switch (inf->bStatus)
  {
  case LEARN_MODE_STARTED:
    rd_probe_lock(TRUE);
    /* Set my nodeID to an invalid value, to keep controller updates from messing things up*/
    old_nodeid = MyNodeID;
    MyNodeID = 0;
    nms.tmp_node = inf->bSource;
    nms.state = NM_LEARN_MODE_STARTED;
    break;
  case LEARN_MODE_DONE:

    /*There are three outcomes of learn mode
     * 1) Controller has been included into a new network
     * 2) Controller has been excluded from a network
     * 3) Controller replication
     * */
    isCleanNetwork(&clean_network, &new_network);

    if((ZW_GetControllerCapabilities() & CONTROLLER_IS_SECONDARY) == 0) {
      // OK, we are actually not really SUC at this point but later on ApplicationInitSW will
      // make us SUC
      controller_role = SUC;
    }

    nms.state = NM_WAIT_FOR_SECURE_LEARN;

    if (clean_network || inf->bSource == 0)
    {
      WRN_PRINTF("Z/IP Gateway has been excluded.\n");

      /*Stop the DHCP process, since its sensitive for NODEid changes*/
      process_exit(&dhcp_client_process);

      MyNodeID = 1;

      SecureInclusionDone(0);

      nms.state = NM_SET_DEFAULT;

      process_start(&dhcp_client_process, 0);
    }
    else if (new_network)
    {
      /*Make sure there is no old entry in the nat */
      ipv46nat_del_entry(inf->bSource);
      ipv6nat_rename_node(old_nodeid,inf->bSource);

      /*Stop the DHCP process, since its sensitive for NODEid changes*/
      process_exit(&dhcp_client_process);
      rd_mark_node_deleted(old_nodeid);

      /*Update home id and node id, security engine needs to know correct nodeid */
      MemoryGetID((BYTE*) &homeID, &MyNodeID);
      MyNodeID = inf->bSource;


      refresh_ipv6_addresses();
      /* Start security here to make sure the timers are started in time */

      security_learn_begin(SecureInclusionDone);
      sec2_start_learn_mode(nms.tmp_node, SecureInclusionDone);

      if( nms.flags & NMS_FLAG_LEARNMODE_NEW) {
        /* Now Add the node who included us to nat table, so it will aquire a new IP address*/
        process_start(&dhcp_client_process,0);
        ipv46nat_add_entry(nms.tmp_node);
      }

      /*Register that we have entered a new network */
      process_post_synch(&zip_process, ZIP_EVENT_NEW_NETWORK, 0);
    }
    else
    {
      nms.flags |= NMS_FLAG_CONTROLLER_REPLICATION;

      /*Update home id and node id, security engine needs to know correct nodeid */
      MemoryGetID((BYTE*) &homeID, &MyNodeID);

      /*This was a controller replication, ie. this is not a new network. */
      WRN_PRINTF("This was a controller replication\n");
      SecureInclusionDone(0);
    }

    return;
  case LEARN_MODE_FAILED:
    rd_probe_lock(FALSE);
    f->cmdClass = COMMAND_CLASS_NETWORK_MANAGEMENT_BASIC;
    f->cmd = LEARN_MODE_SET_STATUS;
    f->seqNo = nms.seq;
    f->status = inf->bStatus;
    f->newNodeId = 0;
    f->reserved = 0;
    nm_send_reply(f, sizeof(ZW_LEARN_MODE_SET_STATUS_FRAME));
    break;
  }
}
#include "uip-debug.h"
static void
SendNodeList(BYTE bStatus)
{
  BYTE ver, capabilities, len, c, v;
  BYTE *nlist;
  int i;
  ZW_NODE_LIST_REPORT_1BYTE_FRAME* f = (ZW_NODE_LIST_REPORT_1BYTE_FRAME*) &nms.buf;

  f->cmdClass = COMMAND_CLASS_NETWORK_MANAGEMENT_PROXY;
  f->cmd = NODE_LIST_REPORT;
  f->seqNo = nms.seq;
  f->nodeListControllerId = ZW_GetSUCNodeID();
  nlist = &(f->nodeListData1);
  f->status = (f->nodeListControllerId == MyNodeID) ?  NODE_LIST_REPORT_LATEST_LIST : NODE_LIST_REPORT_NO_GUARANTEE;

  SerialAPI_GetInitData(&ver, &capabilities, &len, nlist, &c, &v);
  for (i = 0; i < 29; i++)
  {
    nlist[i] &= ~virtual_nodes_mask[i];
  }

  if (f->nodeListControllerId == 0)
  {
    c = ZW_GetControllerCapabilities();
    /*This is a non sis network and I'm a primary */
    if ((c & CONTROLLER_NODEID_SERVER_PRESENT) == 0 && (c & CONTROLLER_IS_SECONDARY) == 0)
    {
      f->status = NODE_LIST_REPORT_LATEST_LIST;
      f->nodeListControllerId = MyNodeID;
    }
  }

  //There are 29 bytes for node list data
  nms.buf_len = 29 + sizeof(ZW_NODE_LIST_REPORT_1BYTE_FRAME) - 1;

  if ((nms.state == NM_NETWORK_UPDATE) && (rd_probe_new_nodes() > 0))
  {
    SendReplyWhenNetworkIsUpdated();
  }
  else
  {
    nm_send_reply(&nms.buf, nms.buf_len);
  }
}

static void
NetworkUpdateCallback(BYTE bStatus)
{
  ZW_NETWORK_UPDATE_REQUEST_STATUS_FRAME *f = (ZW_NETWORK_UPDATE_REQUEST_STATUS_FRAME*) &nms.buf;

  f->cmdClass = COMMAND_CLASS_NETWORK_MANAGEMENT_BASIC;
  f->cmd = NETWORK_UPDATE_REQUEST_STATUS;
  f->seqNo = nms.seq;
  f->status = bStatus;
  nms.buf_len = sizeof(ZW_NETWORK_UPDATE_REQUEST_STATUS_FRAME);

  if (rd_probe_new_nodes() > 0)
  {
    SendReplyWhenNetworkIsUpdated();
  }
  else
  {
    nm_send_reply(&nms.buf, nms.buf_len);
  }
}

static void
AssignReturnRouteStatus(BYTE bStatus)
{

  ZW_RETURN_ROUTE_ASSIGN_COMPLETE_FRAME* f = (ZW_RETURN_ROUTE_ASSIGN_COMPLETE_FRAME*) &nms.buf;

  f->cmdClass = COMMAND_CLASS_NETWORK_MANAGEMENT_INCLUSION;
  f->cmd = RETURN_ROUTE_ASSIGN_COMPLETE;
  f->seqNo = nms.seq;
  f->status = bStatus;
  nm_send_reply(f, sizeof(ZW_RETURN_ROUTE_ASSIGN_COMPLETE_FRAME));
}

static void
DeleteReturnRouteStatus(BYTE bStatus)
{
  ZW_RETURN_ROUTE_DELETE_COMPLETE_FRAME *f = (ZW_RETURN_ROUTE_DELETE_COMPLETE_FRAME *) &nms.buf;
  f->cmdClass = COMMAND_CLASS_NETWORK_MANAGEMENT_INCLUSION;
  f->cmd = RETURN_ROUTE_DELETE_COMPLETE;
  f->seqNo = nms.seq;
  f->status = bStatus;
  nm_send_reply(f, sizeof(ZW_RETURN_ROUTE_DELETE_COMPLETE_FRAME));
}

void
NodeInfoCachedReport(rd_ep_database_entry_t* ep, void* user)
{
  int len;
  uint32_t age_sec;
  u8_t status;
  u8_t security_flags;
  NODEINFO ni;
  ZW_NODE_INFO_CACHED_REPORT_1BYTE_FRAME* f = (ZW_NODE_INFO_CACHED_REPORT_1BYTE_FRAME*) &nms.buf;


  memset(&nms.buf, 0, sizeof(nms.buf));
  f->cmdClass = COMMAND_CLASS_NETWORK_MANAGEMENT_PROXY;
  f->cmd = NODE_INFO_CACHED_REPORT;
  f->seqNo = nms.seq;

  len = 0;
  if (ep)
  {
    ZW_GetNodeProtocolInfo(ep->node->nodeid, &ni);
    security_flags = GetCacheEntryFlag(ep->node->nodeid);
    f->properties2 = ni.capability;
    f->properties3 = ni.security;
    f->reserved = sec2_gw_node_flags2keystore_flags(security_flags & 0xFF);
    f->basicDeviceClass = ni.nodeType.basic;
    f->genericDeviceClass = ni.nodeType.generic;
    f->specificDeviceClass = ni.nodeType.specific;

    status = (ep->node->state == STATUS_DONE ?
    NODE_INFO_CACHED_REPORT_STATUS_STATUS_OK :
                                               NODE_INFO_CACHED_REPORT_STATUS_STATUS_NOT_RESPONDING);
    age_sec = (clock_seconds() - ep->node->lastUpdate);

    f->properties1 = (status << 4) | (ilog2(age_sec/60) & 0xF);

    if (ep->endpoint_info && (ep->endpoint_info_len >= 2))
    {
      size_t max = sizeof(nms.buf) - offsetof(ZW_NODE_INFO_CACHED_REPORT_1BYTE_FRAME, nonSecureCommandClass1);
      len = mem_insert(&f->nonSecureCommandClass1, &ep->endpoint_info[2],
      COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_IP_ASSOCIATION, ep->endpoint_info_len - 2, sizeof(nms.buf));
    }
  }
  else
  {
    f->properties1 = NODE_INFO_CACHED_REPORT_STATUS_STATUS_UNKNOWN << 4;
  }

  nm_send_reply(f, 10 + len);
}

/**
 * This is where network management is actually performed.
 */
static command_handler_codes_t
NetworkManagementAction(ZW_APPLICATION_TX_BUFFER* pCmd, BYTE bDatalen)
{
//  memset(&nms.buf, 0, sizeof(nms.buf));
//  nms.buf.ZW_Common.cmdClass = pCmd->ZW_Common.cmdClass;

  switch (pCmd->ZW_Common.cmdClass)
  {
  case COMMAND_CLASS_NETWORK_MANAGEMENT_PROXY:
    switch (pCmd->ZW_Common.cmd)
    {
    case NODE_LIST_GET:
      nm_fsm_post_event(NM_EV_REQUEST_NODE_LIST,0);
      break;
    case NODE_INFO_CACHED_GET:
      {
        if(nms.state != NM_IDLE) return COMMAND_BUSY; //TODO move into fsm;

        rd_ep_database_entry_t *ep;
        uint32_t age_sec;
        uint8_t maxage_log;
        if (pCmd->ZW_NodeInfoCachedGetFrame.nodeId == 0)
          pCmd->ZW_NodeInfoCachedGetFrame.nodeId = MyNodeID;

        ep = rd_ep_first(pCmd->ZW_NodeInfoCachedGetFrame.nodeId);
        if (ep)
        {

          age_sec = clock_seconds() - ep->node->lastUpdate;
          maxage_log = (pCmd->ZW_NodeInfoCachedGetFrame.properties1 & 0xF);

          DBG_PRINTF("Age is seconds %i\n", age_sec);
          if (!(pCmd->ZW_NodeInfoCachedGetFrame.nodeId == MyNodeID))
          {
             if ((maxage_log != 0xF && (age_sec > ageToTime(maxage_log))) || maxage_log == 0)
             {
               nms.state = NM_WAITING_FOR_PROBE;
               rd_probe_lock(TRUE);
               rd_register_new_node(pCmd->ZW_NodeInfoCachedGetFrame.nodeId,
               FALSE);
               rd_register_ep_probe_notifier(pCmd->ZW_NodeInfoCachedGetFrame.nodeId, 0, &nms, NodeInfoCachedReport);
               rd_probe_lock(FALSE);
               return COMMAND_HANDLED;
             }
          }
        }
        NodeInfoCachedReport(ep, &nms);
      }
      break;
    case NM_MULTI_CHANNEL_END_POINT_GET:
      {
        rd_node_database_entry_t *node_entry;
        ZW_NM_MULTI_CHANNEL_END_POINT_GET_FRAME* get_frame = (ZW_NM_MULTI_CHANNEL_END_POINT_GET_FRAME*)pCmd;
        ZW_NM_MULTI_CHANNEL_END_POINT_REPORT_FRAME* report_frame = (ZW_NM_MULTI_CHANNEL_END_POINT_REPORT_FRAME*) &nms.buf;

        if(nms.state != NM_IDLE) return COMMAND_BUSY; //TODO move into fsm;
        if (bDatalen < 4)
          return COMMAND_PARSE_ERROR;

        node_entry = rd_get_node_dbe(get_frame->nodeID);
        if (!node_entry)
          return COMMAND_PARSE_ERROR;

        memset(&nms.buf, 0, sizeof(nms.buf));
        report_frame->cmdClass = COMMAND_CLASS_NETWORK_MANAGEMENT_PROXY;
        report_frame->cmd = NM_MULTI_CHANNEL_END_POINT_REPORT;
        report_frame->seqNo = nms.seq;
        report_frame->nodeID = get_frame->nodeID;
        /* -1 for the root NIF */
        report_frame->individualEndPointCount = node_entry->nEndpoints - 1 - node_entry->nAggEndpoints;
        report_frame->aggregatedEndPointCount = node_entry->nAggEndpoints;
        rd_free_node_dbe(node_entry);
        nm_send_reply(report_frame, 7);
      }
      break;
    case NM_MULTI_CHANNEL_CAPABILITY_GET:
      {
        rd_ep_database_entry_t *ep_entry;
        ZW_NM_MULTI_CHANNEL_CAPABILITY_GET_FRAME* get_frame = (ZW_NM_MULTI_CHANNEL_CAPABILITY_GET_FRAME*)pCmd;
        ZW_NM_MULTI_CHANNEL_CAPABILITY_REPORT_FRAME* report_frame = (ZW_NM_MULTI_CHANNEL_CAPABILITY_REPORT_FRAME*) &nms.buf;

        if(nms.state != NM_IDLE) return COMMAND_BUSY; //TODO move into fsm;
        if (bDatalen < 5)
          return COMMAND_PARSE_ERROR;

        ep_entry = rd_get_ep(get_frame->nodeID, get_frame->endpoint & 0x7F);

        if (NULL == ep_entry)
        {
          return COMMAND_PARSE_ERROR;
        }

        memset(&nms.buf, 0, sizeof(nms.buf));
        report_frame->cmdClass = COMMAND_CLASS_NETWORK_MANAGEMENT_PROXY;
        report_frame->cmd = NM_MULTI_CHANNEL_CAPABILITY_REPORT;
        report_frame->seqNo = nms.seq;
        report_frame->nodeID = get_frame->nodeID;
        report_frame->commandClassLength = ep_entry->endpoint_info_len - 2;
        report_frame->endpoint = get_frame->endpoint & 0x7F;
        memcpy(&report_frame->genericDeviceClass, ep_entry->endpoint_info, ep_entry->endpoint_info_len);
        nm_send_reply(report_frame, 6 + ep_entry->endpoint_info_len);
      }
      break;
    case NM_MULTI_CHANNEL_AGGREGATED_MEMBERS_GET:
      {
        rd_ep_database_entry_t *ep_entry;
        ZW_NM_MULTI_CHANNEL_AGGREGATED_MEMBERS_GET_FRAME* get_frame = (ZW_NM_MULTI_CHANNEL_AGGREGATED_MEMBERS_GET_FRAME*)pCmd;
        ZW_NM_MULTI_CHANNEL_AGGREGATED_MEMBERS_REPORT_FRAME* report_frame = (ZW_NM_MULTI_CHANNEL_AGGREGATED_MEMBERS_REPORT_FRAME*) &nms.buf;

        if(nms.state != NM_IDLE) return COMMAND_BUSY; //TODO move into fsm;
        if (bDatalen < 5)
          return COMMAND_PARSE_ERROR;

        ep_entry = rd_get_ep(get_frame->nodeID, get_frame->aggregatedEndpoint & 0x7F);
        if (NULL == ep_entry || 0 == ep_entry->endpoint_aggr_len)
        {
          return COMMAND_PARSE_ERROR;
        }

        report_frame->cmdClass = COMMAND_CLASS_NETWORK_MANAGEMENT_PROXY;
        report_frame->cmd = NM_MULTI_CHANNEL_AGGREGATED_MEMBERS_REPORT;
        report_frame->seqNo = nms.seq;
        report_frame->nodeID = get_frame->nodeID;
        report_frame->aggregatedEndpoint = get_frame->aggregatedEndpoint & 0x7F;
        report_frame->memberCount = ep_entry->endpoint_aggr_len;
        memcpy(&report_frame->memberEndpoint1, ep_entry->endpoint_agg, ep_entry->endpoint_aggr_len);
        nm_send_reply(report_frame, 6 + ep_entry->endpoint_aggr_len);
      }
      break;
    default:
      return COMMAND_NOT_SUPPORTED;
    }

    break;
  case COMMAND_CLASS_NETWORK_MANAGEMENT_BASIC:
    switch (pCmd->ZW_Common.cmd)
    {
    case DEFAULT_SET:
      if(nms.state != NM_IDLE) return COMMAND_BUSY; //TODO move into fsm;

      DBG_PRINTF("Setting default\n");
      nms.state = NM_SET_DEFAULT;
      ZW_SetDefault(SetDefaultStatus);
      break;
    case LEARN_MODE_SET:
      nm_fsm_post_event(NM_EV_LEARN_SET,pCmd);
      break;
    case NODE_INFORMATION_SEND:
      if(nms.state != NM_IDLE) return COMMAND_BUSY; //TODO move into fsm;

      if(ZW_SendNodeInformation(pCmd->ZW_NodeInformationSendFrame.destinationNodeId,
          pCmd->ZW_NodeInformationSendFrame.txOptions, __ResetState)) {
            nms.state = NM_SENDING_NODE_INFO;
      } else {
          __ResetState(TRANSMIT_COMPLETE_FAIL);
      }
      break;
    case NETWORK_UPDATE_REQUEST:
      if(nms.state != NM_IDLE) return COMMAND_BUSY; //TODO move into fsm;

      if (ZW_RequestNetWorkUpdate(NetworkUpdateCallback))
      {
        nms.state = NM_NETWORK_UPDATE;
      }
      else
      {
        /*I'm the SUC/SIS or i don't know the SUC/SIS*/
        if (ZW_GetSUCNodeID() > 0)
        {
          NetworkUpdateCallback(ZW_SUC_UPDATE_DONE);
        }
        else
        {
          NetworkUpdateCallback(ZW_SUC_UPDATE_DISABLED);
        }
      }
      break;
    case DSK_GET:
    {
      if(nms.state != NM_IDLE) return COMMAND_BUSY; //TODO move into fsm;

      ZW_DSK_RAPORT_FRAME_EX* f = (ZW_DSK_RAPORT_FRAME_EX*)&nms.buf;
      f->cmdClass = COMMAND_CLASS_NETWORK_MANAGEMENT_BASIC;
      f->cmd = DSK_RAPORT;
      f->seqNo = ((uint8_t*)pCmd)[2];
      f->add_mode = ((uint8_t*)pCmd)[3] & DSK_GET_ADD_MODE_BIT;

      /*Temporary change the NMS state to allow keystore to locate the right key.*/
      if(f->add_mode & DSK_GET_ADD_MODE_BIT) {
        //The value 1 MUST indicate that the node advertises its Add Mode DSK
        nms.state = NM_WAIT_FOR_SECURE_ADD;
      } else {
        nms.state = NM_WAIT_FOR_SECURE_LEARN;
      }

      keystore_public_key_read(f->dsk);
      nms.state = NM_IDLE;
      nm_send_reply(f,sizeof(ZW_DSK_RAPORT_FRAME_EX));
    }
      break;
    default:
      return COMMAND_NOT_SUPPORTED;
    }
    break;
  case COMMAND_CLASS_NETWORK_MANAGEMENT_INCLUSION:
    /*If there is neither a SIS or we are primary controller, we cannot perform inclusion*/
    if (!((ZW_GetControllerCapabilities() & CONTROLLER_NODEID_SERVER_PRESENT) || ZW_IsPrimaryCtrl()))
      return COMMAND_NOT_SUPPORTED;

    switch (pCmd->ZW_Common.cmd)
    {
    case NODE_ADD:
      {
        uint8_t mode = ADD_NODE_ANY;

        if (!(pCmd->ZW_NodeAddFrame.txOptions & TRANSMIT_OPTION_LOW_POWER))
        {
          mode |= ADD_NODE_OPTION_NORMAL_POWER;
        }

        if (pCmd->ZW_NodeAddFrame.txOptions & TRANSMIT_OPTION_EXPLORE)
        {
          mode |= ADD_NODE_OPTION_NETWORK_WIDE;
        }

        nms.txOptions = TRANSMIT_OPTION_ACK | TRANSMIT_OPTION_AUTO_ROUTE | pCmd->ZW_NodeAddFrame.txOptions;

        if (pCmd->ZW_NodeAddFrame.mode == ADD_NODE_STOP)
        {
          DBG_PRINTF("Add node stop\n");
          nm_fsm_post_event(NM_NODE_ADD_STOP, 0);
        }
        else if (pCmd->ZW_NodeAddFrame.mode == ADD_NODE_ANY)
        {
          nm_fsm_post_event(NM_EV_NODE_ADD, &mode);
        }
        else if (pCmd->ZW_NodeAddFrame.mode == ADD_NODE_ANY_S2)
        {
          nm_fsm_post_event(NM_EV_NODE_ADD_S2, &mode);
        }
      }
      break;
    case NODE_REMOVE:
      if(nms.state != NM_IDLE && nms.state != NM_WAITING_FOR_NODE_REMOVAL) return COMMAND_BUSY; //TODO move into fsm;

      if (pCmd->ZW_NodeRemoveFrame.mode == REMOVE_NODE_STOP)
      {
        RemoveTimerExpired();
      }
      else
      {
        ZW_RemoveNodeFromNetwork(pCmd->ZW_NodeRemoveFrame.mode, RemoveNodeStatusUpdate);
        nms.state = NM_WAITING_FOR_NODE_REMOVAL;
      }

      break;
    case FAILED_NODE_REMOVE:
      if(nms.state != NM_IDLE) return COMMAND_BUSY; //TODO move into fsm;

      nms.state = NM_WAITING_FOR_FAIL_NODE_REMOVAL;
      nms.tmp_node = pCmd->ZW_FailedNodeRemoveFrame.nodeId;
      if (ZW_RemoveFailedNode(pCmd->ZW_FailedNodeRemoveFrame.nodeId,
          RemoveFailedNodeStatus) != ZW_FAILED_NODE_REMOVE_STARTED)
      {
        RemoveFailedNodeStatus(ZW_FAILED_NODE_NOT_REMOVED);
      }
      break;
    case FAILED_NODE_REPLACE:
      {
        ZW_FAILED_NODE_REPLACE_FRAME* f = (ZW_FAILED_NODE_REPLACE_FRAME*) pCmd;

        if (f->mode == START_FAILED_NODE_REPLACE)
        {
          nm_fsm_post_event(NM_EV_REPLACE_FAILED_START,pCmd);
        } else if(f->mode == START_FAILED_NODE_REPLACE_S2)
        {
          nm_fsm_post_event(NM_EV_REPLACE_FAILED_START_S2,pCmd);
        }
        else if (f->mode == STOP_FAILED_NODE_REPLACE)
        {
          nm_fsm_post_event(NM_EV_REPLACE_FAILED_STOP,pCmd);
        }
      }
      break;
    case NODE_NEIGHBOR_UPDATE_REQUEST:
      {
        if(nms.state != NM_IDLE) return COMMAND_BUSY; //TODO move into fsm;

        ZW_NODE_NEIGHBOR_UPDATE_REQUEST_FRAME *f = (ZW_NODE_NEIGHBOR_UPDATE_REQUEST_FRAME *) pCmd;
        nms.state = NM_WAITING_FOR_NODE_NEIGH_UPDATE;
        ZW_RequestNodeNeighborUpdate(f->nodeId, RequestNodeNeighborUpdateStatus);
      }
      break;
    case RETURN_ROUTE_ASSIGN:
      {
        if(nms.state != NM_IDLE) return COMMAND_BUSY; //TODO move into fsm;

        ZW_RETURN_ROUTE_ASSIGN_FRAME *f = (ZW_RETURN_ROUTE_ASSIGN_FRAME*) pCmd;
        nms.state = NM_WAITING_FOR_RETURN_ROUTE_ASSIGN;
        if (!ZW_AssignReturnRoute(f->sourceNodeId, f->destinationNodeId, AssignReturnRouteStatus))
        {
          AssignReturnRouteStatus(TRANSMIT_COMPLETE_FAIL);
        }
      }
      break;
    case RETURN_ROUTE_DELETE:
      {
        if(nms.state != NM_IDLE) return COMMAND_BUSY; //TODO move into fsm;

        ZW_RETURN_ROUTE_DELETE_FRAME *f = (ZW_RETURN_ROUTE_DELETE_FRAME*) pCmd;
        nms.state = NM_WAITING_FOR_RETURN_ROUTE_DELETE;
        if(ZW_DeleteReturnRoute(f->nodeId, DeleteReturnRouteStatus) != TRUE) {
           DeleteReturnRouteStatus(TRANSMIT_COMPLETE_FAIL);
        }
      }
      break;
    case NODE_ADD_KEYS_SET:
      nm_fsm_post_event(NM_EV_ADD_SECURITY_KEYS_SET,pCmd);
    break;
    case NODE_ADD_DSK_SET:
      nm_fsm_post_event(NM_EV_ADD_SECURITY_DSK_SET,pCmd);
    break;
    default:
      return COMMAND_NOT_SUPPORTED;
    }
    break;
  default:
    return CLASS_NOT_SUPPORTED;
  }
  return COMMAND_HANDLED;
}

/**
 * Return a appropriate failure code to sender.
 */
static void 
NetworkManagementReturnFail(zwave_connection_t* c, const ZW_APPLICATION_TX_BUFFER* pCmd, BYTE bDatalen)
{
  BYTE len = 0;
  ZW_APPLICATION_TX_BUFFER buf;
  memset(&buf, 0, sizeof(buf));
  buf.ZW_Common.cmdClass = pCmd->ZW_Common.cmdClass;
  buf.ZW_NodeAddFrame.seqNo = pCmd->ZW_NodeAddFrame.seqNo;

  /*Special cases where we have some error code */
  switch (pCmd->ZW_Common.cmdClass)
  {
  case COMMAND_CLASS_NETWORK_MANAGEMENT_BASIC:
    switch (pCmd->ZW_Common.cmd)
    {
    case DEFAULT_SET:
      buf.ZW_DefaultSetCompleteFrame.cmd = DEFAULT_SET_COMPLETE;
      buf.ZW_DefaultSetCompleteFrame.status = DEFAULT_SET_BUSY;
      len = sizeof(buf.ZW_DefaultSetCompleteFrame);
      break;
    case LEARN_MODE_SET:
      buf.ZW_LearnModeSetStatusFrame.cmd = LEARN_MODE_SET_STATUS;
      buf.ZW_LearnModeSetStatusFrame.status = LEARN_MODE_FAILED;
      len = sizeof(buf.ZW_LearnModeSetStatusFrame);
      break;
    case NETWORK_UPDATE_REQUEST:
      {
        ZW_NETWORK_UPDATE_REQUEST_STATUS_FRAME *f = (ZW_NETWORK_UPDATE_REQUEST_STATUS_FRAME*) &buf;
        f->cmd = NETWORK_UPDATE_REQUEST_STATUS;
        f->status = ZW_SUC_UPDATE_ABORT;
        len = sizeof(ZW_NETWORK_UPDATE_REQUEST_STATUS_FRAME);
      }
      break;
    }
    break;
  case COMMAND_CLASS_NETWORK_MANAGEMENT_INCLUSION:
    switch (pCmd->ZW_Common.cmd)
    {
    case NODE_ADD:
      buf.ZW_NodeAddStatus1byteFrame.cmd = NODE_ADD_STATUS;
      buf.ZW_NodeAddStatus1byteFrame.status = ADD_NODE_STATUS_FAILED;
      len = sizeof(buf.ZW_NodeAddStatus1byteFrame) - 1;
      break;
    case NODE_REMOVE:
      buf.ZW_NodeRemoveStatusFrame.cmd = NODE_REMOVE_STATUS;
      buf.ZW_NodeRemoveStatusFrame.status = REMOVE_NODE_STATUS_FAILED;
      len = sizeof(buf.ZW_NodeRemoveStatusFrame);
      break;
    case FAILED_NODE_REMOVE:
      buf.ZW_FailedNodeRemoveStatusFrame.cmd =
      FAILED_NODE_REMOVE_STATUS;
      buf.ZW_FailedNodeRemoveStatusFrame.status = ZW_FAILED_NODE_NOT_REMOVED;
      len = sizeof(buf.ZW_FailedNodeRemoveStatusFrame);
      break;
    case FAILED_NODE_REPLACE:
      buf.ZW_FailedNodeReplaceStatusFrame.cmd =
      FAILED_NODE_REPLACE_STATUS;
      buf.ZW_FailedNodeReplaceStatusFrame.status =
      ZW_FAILED_NODE_REPLACE_FAILED;
      len = sizeof(buf.ZW_FailedNodeReplaceStatusFrame);
      break;
    case NODE_NEIGHBOR_UPDATE_REQUEST:
      {
        ZW_NODE_NEIGHBOR_UPDATE_STATUS_FRAME* f = (ZW_NODE_NEIGHBOR_UPDATE_STATUS_FRAME*) &buf;
        f->cmd = NODE_NEIGHBOR_UPDATE_STATUS;
        f->status = REQUEST_NEIGHBOR_UPDATE_FAILED;
        len = sizeof(ZW_NODE_NEIGHBOR_UPDATE_STATUS_FRAME);
      }
      break;
    }

    break;
  }


  if (len==0)
  {
    buf.ZW_ApplicationBusyFrame.cmdClass = COMMAND_CLASS_APPLICATION_STATUS;
    buf.ZW_ApplicationBusyFrame.cmd = APPLICATION_BUSY;
    buf.ZW_ApplicationBusyFrame.status = APPLICATION_BUSY_TRY_AGAIN_LATER;
    buf.ZW_ApplicationBusyFrame.waitTime = 0;
    len = sizeof(buf.ZW_ApplicationBusyFrame);
  }

  ZW_SendDataZIP(c, (BYTE*) &buf, len, 0);
}

/**
 * Command handler for network management commands
 */
static command_handler_codes_t
NetworkManagementCommandHandler(zwave_connection_t *c, BYTE* pData, uint16_t bDatalen)
{
  ZW_APPLICATION_TX_BUFFER* pCmd = (ZW_APPLICATION_TX_BUFFER*) pData;

  DBG_PRINTF("NetworkManagementCommandHandler %x %x\n",pData[0], pData[1]);


   mb_abort_sending();

   if(get_queue_state() != QS_IDLE) {
     ClassicZIPNode_AbortSending();
   }

   /*FIXME, we could argue that we should not have the second check, in that case it would only
   * be the unsolicited destination, which could accept the inclusion request */
  if(nms.state == NM_IDLE) {
    /* Save info about who we are talking with */
    nms.conn = *c;

    /*Keep the sequence nr which is common for all network management commands*/
    nms.seq = pCmd->ZW_NodeAddFrame.seqNo;
  } else if (uip_ipaddr_cmp(&c->ripaddr, &nms.conn.ripaddr) && (c->rport == nms.conn.rport))
  {
    //Allow
  } else {
    LOG_PRINTF("Another network management session (%dx.%dx) is in progress (%dx.%dx)\n", nms.class, nms.cmd,
        pCmd->ZW_Common.cmdClass, pCmd->ZW_Common.cmd);
    /* Return the proper failure code accoding the the request */
    goto send_fail;
  }

  return NetworkManagementAction((ZW_APPLICATION_TX_BUFFER*) pData, bDatalen);


send_fail:
  NetworkManagementReturnFail(c, pCmd, bDatalen);
  return COMMAND_HANDLED; /*TODO Busy might be more appropriate*/
}

uip_ipaddr_t*
NetworkManagement_getpeer()
{
  return &nms.conn.ripaddr;
}

/* Returns 0 when init is pending on DHCP assignment,
 * 1 when init is complete */
int
NetworkManagement_Init()
{
//  uip_ds6_route_t* a;

  if (network_management_init_done)
    return 1;

  ZW_LTimerCancel(nms.networkManagementTimer);
  nms.networkManagementTimer = 0xFF;
  DBG_PRINTF("NM Init\n");
#if 0
  if (nms.buf_len)
  {
    /**
     * Check if we can deliver the package right now. If not then we expect NetworkManagement_Init
     * to be called again at a later stage.
     */
    if (!uip_ds6_is_addr_onlink(&nms.conn.ripaddr))
    {
      a = uip_ds6_route_lookup(&nms.conn.ripaddr);
      if (a == NULL)
      {
        DBG_PRINTF("No route to peer\n");
        uip_debug_ipaddr_print(&nms.conn.ripaddr);
        nms.networkManagementTimer = ZW_LTimerStart(NetworkManagementTimeout,
            NETWORK_MANAGEMENT_TIMEOUT, TIMER_ONE_TIME);
        return 0;
      }
    }
    if(bridge_state == booting)
    {
      return 0;
    }

    DBG_PRINTF("Sending frame from previous life.\n");
    /*Send the command in the nms.buf its left over from a previous life*/
    nm_send_reply(&nms.buf, nms.buf_len);
    /*FIXME: WARINIG This might give problems when activated from PAN*/
    ResetState(0, 0);
  }
  else
  {
    ResetState(0, 0);
  }
#endif

  ZW_LTimerCancel(nms.networkManagementTimer);

  /*Make sure that the controller is not in any odd state */
  network_management_init_done = 1;
  return 1;
}

/* Replace all occurences of 'old' with 'new' in buffer buf */
void
mem_replace(unsigned char *buf, char old, char new, size_t len)
{
  char *p;
  while ((p = memchr(buf, old, len)) != NULL)
  {
    *p = new;
  }
}

/* Add a character after add, return the new length*/
size_t
mem_insert(u8_t* dst, const u8_t *src, u8_t find, u8_t add, size_t len, size_t max)
{
  size_t k = len;
  while ((len--) && (max--)) //Do not write beyound max
  {
    if (*src == find)
    {
      *dst++ = *src++;
      *dst++ = add;
      k++;
    }
    else
    {
      *dst++ = *src++;
    }
  }
  return k;
}

/**
 * Get the state of the network management module
 * @return
 */
nm_state_t
NetworkManagement_getState()
{
  return nms.state;
}

BOOL
NetworkManagement_is_Unsolicited2_peer()
{
  return (nms.cmd && uip_ipaddr_cmp(&cfg.unsolicited_dest2, &nms.conn.ripaddr)
      && cfg.unsolicited_port2 == UIP_HTONS(nms.conn.rport));
}

BOOL
NetworkManagement_is_Unsolicited_peer()
{
  return (nms.cmd && uip_ipaddr_cmp(&cfg.unsolicited_dest, &nms.conn.ripaddr)
      && cfg.unsolicited_port == UIP_HTONS(nms.conn.rport));
}

/**
 + * Send a Unsolicited NodeList report to the unsolicited destination
 + */
BOOL
NetworkManagement_SendNodeList_To_Unsolicited()
{
  if (nms.state !=NM_IDLE)
  {
    return FALSE;
  }
  nms.cmd = NODE_LIST_GET;
  nms.seq = random_rand() & 0xFF;
  if(!uip_is_addr_unspecified(&cfg.unsolicited_dest)) {
      uip_ipaddr_copy(&nms.conn.lipaddr, &cfg.lan_addr);
      uip_ipaddr_copy(&nms.conn.ripaddr, &cfg.unsolicited_dest);
      nms.conn.rport = UIP_HTONS(cfg.unsolicited_port);
      nms.conn.lport = (nms.conn.rport == UIP_HTONS(ZWAVE_PORT)) ? UIP_HTONS(ZWAVE_PORT): UIP_HTONS(DTLS_PORT);
      SendNodeList(ZW_SUC_UPDATE_DONE);
      DBG_PRINTF("Sending node list to Unsolicited Destination 1\n");
  }
  if(!uip_is_addr_unspecified(&cfg.unsolicited_dest2)) {
      nms.seq++;
      uip_ipaddr_copy(&nms.conn.ripaddr, &cfg.unsolicited_dest2);
      nms.conn.rport = UIP_HTONS(cfg.unsolicited_port2);
      nms.conn.lport = (nms.conn.rport == UIP_HTONS(ZWAVE_PORT)) ? UIP_HTONS(ZWAVE_PORT): UIP_HTONS(DTLS_PORT);
      SendNodeList(ZW_SUC_UPDATE_DONE);
      DBG_PRINTF("Sending node list to Unsolicited Destination 2\n");
  }

  return TRUE;
}

REGISTER_HANDLER(NetworkManagementCommandHandler, 0, COMMAND_CLASS_NETWORK_MANAGEMENT_BASIC, NETWORK_MANAGEMENT_BASIC_VERSION_V2, SECURITY_SCHEME_0);
REGISTER_HANDLER(NetworkManagementCommandHandler, 0, COMMAND_CLASS_NETWORK_MANAGEMENT_PROXY, NETWORK_MANAGEMENT_PROXY_VERSION_V2, SECURITY_SCHEME_0);
REGISTER_HANDLER(NetworkManagementCommandHandler, 0, COMMAND_CLASS_NETWORK_MANAGEMENT_INCLUSION, NETWORK_MANAGEMENT_INCLUSION_VERSION_V2, SECURITY_SCHEME_0);

