#include <stdlib.h>
#include <unistd.h>
#include <malloc.h>
#include <memory.h>
#include <string.h>
#include <stdint.h>
#include <stdio.h>

#include "zwcontrol_api.h"

extern int  StartZipGateWay(const char *resPath);
extern void StopZipGateWay();

static ResCallBack resCallBack = NULL;

#define MAX_DTLS_PSK  64  //Maximum DTLS pre-shared key hex string length

#define ADD_NODE_STS_UNKNOWN    0   ///<Add node status: unknown
#define ADD_NODE_STS_PROGRESS   1   ///<Add node status: in progress
#define ADD_NODE_STS_DONE       2   ///<Add node status: done

#define RM_NODE_STS_UNKNOWN    0   ///<Remove node status: unknown
#define RM_NODE_STS_PROGRESS   1   ///<Remove node status: in progress
#define RM_NODE_STS_DONE       2   ///<Remove node status: done

#define SEC2_ENTER_KEY_REQ  1   ///< Bit-mask for allowing S2 key request callback
#define SEC2_ENTER_DSK      2   ///< Bit-mask for allowing S2 DSK callback

#define  DESC_TYPE_NODE     1
#define  DESC_TYPE_EP       2
#define  DESC_TYPE_INTF     3

/**
hl_if_plt_ctx_get - Get platform context
@return         platform context
*/
static void* hl_if_plt_ctx_get(zwifd_p ifd)
{
    zwnetd_t    *net_desc;
    //Get and save the context
    net_desc = zwnet_get_desc(ifd->net);
    return net_desc->plt_ctx;
}

/**
hl_class_str_get - Get command class string
@param[in]  cls         class
@param[in]  ver         version of the command class
@return     Command class string if found, else return string "UNKNOWN"
*/
static char *hl_class_str_get(uint16_t cls, uint8_t ver)
{
    switch (cls)
    {
            case COMMAND_CLASS_BASIC:
            {
                return "COMMAND_CLASS_BASIC";
            }
            break;

            case COMMAND_CLASS_SWITCH_MULTILEVEL:
            {
                return "COMMAND_CLASS_SWITCH_MULTILEVEL";
            }
            break;

            case COMMAND_CLASS_SWITCH_BINARY:
            {
                return "COMMAND_CLASS_SWITCH_BINARY";
            }
            break;

            case COMMAND_CLASS_SWITCH_ALL:
            {
                return "COMMAND_CLASS_SWITCH_ALL";
            }
            break;

            case COMMAND_CLASS_MANUFACTURER_SPECIFIC:
            {
                return "COMMAND_CLASS_MANUFACTURER_SPECIFIC";
            }
            break;

            case COMMAND_CLASS_VERSION:
            {
                return "COMMAND_CLASS_VERSION";
            }
            break;

            case COMMAND_CLASS_POWERLEVEL:
            {
                return "COMMAND_CLASS_POWERLEVEL";
            }
            break;

            case COMMAND_CLASS_CONTROLLER_REPLICATION:
            {
                return "COMMAND_CLASS_CONTROLLER_REPLICATION";
            }
            break;

            case COMMAND_CLASS_NODE_NAMING:
            {
                return "COMMAND_CLASS_NODE_NAMING";
            }
            break;

            case COMMAND_CLASS_SENSOR_BINARY:
            {
                return "COMMAND_CLASS_SENSOR_BINARY";
            }
            break;

            case COMMAND_CLASS_SENSOR_MULTILEVEL:
            {
                return "COMMAND_CLASS_SENSOR_MULTILEVEL";
            }
            break;

            case COMMAND_CLASS_ASSOCIATION:
            {
                return "COMMAND_CLASS_ASSOCIATION";
            }
            break;

            case COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V2:
            {
                if (ver >= 2)
                {
                    return "COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION";
                }
                return "COMMAND_CLASS_MULTI_INSTANCE_ASSOCIATION";
            }
            break;

            case COMMAND_CLASS_ASSOCIATION_COMMAND_CONFIGURATION:
            {
                return "COMMAND_CLASS_ASSOCIATION_COMMAND_CONFIGURATION";
            }
            break;

            case COMMAND_CLASS_NO_OPERATION:
            {
                return "COMMAND_CLASS_NO_OPERATION";
            }
            break;

            case COMMAND_CLASS_MULTI_CHANNEL_V2:
            {
                if (ver >= 2)
                {
                    return "COMMAND_CLASS_MULTI_CHANNEL";
                }
                return "COMMAND_CLASS_MULTI_INSTANCE";
            }
            break;

            case COMMAND_CLASS_WAKE_UP:
            {
                return "COMMAND_CLASS_WAKE_UP";
            }
            break;

            case COMMAND_CLASS_MANUFACTURER_PROPRIETARY:
            {
                return "COMMAND_CLASS_MANUFACTURER_PROPRIETARY";
            }
            break;

            case COMMAND_CLASS_METER_TBL_MONITOR:
            {
                return "COMMAND_CLASS_METER_TBL_MONITOR";
            }
            break;

            case COMMAND_CLASS_METER_TBL_CONFIG:
            {
                return "COMMAND_CLASS_METER_TBL_CONFIG";
            }
            break;

            case COMMAND_CLASS_METER:
            {
                return "COMMAND_CLASS_METER";
            }
            break;

            case COMMAND_CLASS_METER_PULSE:
            {
                return "COMMAND_CLASS_METER_PULSE";
            }
            break;

            case COMMAND_CLASS_SIMPLE_AV_CONTROL:
            {
                return "COMMAND_CLASS_SIMPLE_AV_CONTROL";
            }
            break;

            case COMMAND_CLASS_CONFIGURATION:
            {
                return "COMMAND_CLASS_CONFIGURATION";
            }
            break;

            case COMMAND_CLASS_INDICATOR:
            {
                return "COMMAND_CLASS_INDICATOR";
            }
            break;

            case COMMAND_CLASS_SECURITY:
            {
                return "COMMAND_CLASS_SECURITY";
            }
            break;

            case COMMAND_CLASS_SECURITY_2:
            {
                return "COMMAND_CLASS_SECURITY_2";
            }
            break;

            case COMMAND_CLASS_HAIL:
            {
                return "COMMAND_CLASS_HAIL";
            }
            break;

            case COMMAND_CLASS_PROTECTION:
            {
                return "COMMAND_CLASS_PROTECTION";
            }
            break;

            case COMMAND_CLASS_SWITCH_TOGGLE_BINARY:
            {
                return "COMMAND_CLASS_SWITCH_TOGGLE_BINARY";
            }
            break;

            case COMMAND_CLASS_BATTERY:
            {
                return "COMMAND_CLASS_BATTERY";
            }
            break;

            case COMMAND_CLASS_DOOR_LOCK:
            {
                return "COMMAND_CLASS_DOOR_LOCK";
            }
            break;

            case COMMAND_CLASS_USER_CODE:
            {
                return "COMMAND_CLASS_USER_CODE";
            }
            break;

            case COMMAND_CLASS_ALARM:
            {
                if (ver >= 3)
                {
                    return "COMMAND_CLASS_NOTIFICATION";
                }
                return "COMMAND_CLASS_ALARM";
            }
            break;

            case COMMAND_CLASS_SCHEDULE_ENTRY_LOCK:
            {
                return "COMMAND_CLASS_SCHEDULE_ENTRY_LOCK";
            }
            break;

            case COMMAND_CLASS_DOOR_LOCK_LOGGING:
            {
                return "COMMAND_CLASS_DOOR_LOCK_LOGGING";
            }
            break;

            case COMMAND_CLASS_TIME_PARAMETERS:
            {
                return "COMMAND_CLASS_TIME_PARAMETERS";
            }
            break;

            case COMMAND_CLASS_CRC_16_ENCAP:
            {
                return "COMMAND_CLASS_CRC_16_ENCAP";
            }
            break;

            case COMMAND_CLASS_TRANSPORT_SERVICE:
            {
                return "COMMAND_CLASS_TRANSPORT_SERVICE";
            }
            break;

            case COMMAND_CLASS_ZIP:
            {
                return "COMMAND_CLASS_ZIP";
            }
            break;

            case COMMAND_CLASS_NETWORK_MANAGEMENT_PROXY:
            {
                return "COMMAND_CLASS_NETWORK_MANAGEMENT_PROXY";
            }
            break;

            case COMMAND_CLASS_NETWORK_MANAGEMENT_INCLUSION:
            {
                return "COMMAND_CLASS_NETWORK_MANAGEMENT_INCLUSION";
            }
            break;

            case COMMAND_CLASS_NETWORK_MANAGEMENT_BASIC:
            {
                return "COMMAND_CLASS_NETWORK_MANAGEMENT_BASIC";
            }
            break;

            case COMMAND_CLASS_NETWORK_MANAGEMENT_PRIMARY:
            {
                return "COMMAND_CLASS_NETWORK_MANAGEMENT_PRIMARY";
            }
            break;

            case COMMAND_CLASS_THERMOSTAT_FAN_MODE:
            {
                return "COMMAND_CLASS_THERMOSTAT_FAN_MODE";
            }
            break;

            case COMMAND_CLASS_THERMOSTAT_FAN_STATE:
            {
                return "COMMAND_CLASS_THERMOSTAT_FAN_STATE";
            }
            break;

            case COMMAND_CLASS_THERMOSTAT_MODE:
            {
                return "COMMAND_CLASS_THERMOSTAT_MODE";
            }
            break;

            case COMMAND_CLASS_THERMOSTAT_OPERATING_STATE:
            {
                return "COMMAND_CLASS_THERMOSTAT_OPERATING_STATE";
            }
            break;

            case COMMAND_CLASS_THERMOSTAT_SETPOINT:
            {
                return "COMMAND_CLASS_THERMOSTAT_SETPOINT";
            }
            break;

            case COMMAND_CLASS_THERMOSTAT_SETBACK:
            {
                return "COMMAND_CLASS_THERMOSTAT_SETBACK";
            }
            break;

            case COMMAND_CLASS_CLOCK:
            {
                return "COMMAND_CLASS_CLOCK";
            }
            break;

            case COMMAND_CLASS_LOCK:
            {
                return "COMMAND_CLASS_LOCK";
            }
            break;

            case COMMAND_CLASS_CLIMATE_CONTROL_SCHEDULE:
            {
                return "COMMAND_CLASS_CLIMATE_CONTROL_SCHEDULE";
            }
            break;

            case COMMAND_CLASS_MULTI_CMD:
            {
                return "COMMAND_CLASS_MULTI_CMD";
            }
            break;

            case COMMAND_CLASS_APPLICATION_STATUS:
            {
                return "COMMAND_CLASS_APPLICATION_STATUS";
            }
            break;

            case COMMAND_CLASS_FIRMWARE_UPDATE_MD:
            {
                return "COMMAND_CLASS_FIRMWARE_UPDATE_MD";
            }
            break;

            case COMMAND_CLASS_ZWAVEPLUS_INFO:
            {
                return "COMMAND_CLASS_ZWAVEPLUS_INFO";
            }
            break;

            case COMMAND_CLASS_DEVICE_RESET_LOCALLY:
            {
                return "COMMAND_CLASS_DEVICE_RESET_LOCALLY";
            }
            break;

            case COMMAND_CLASS_ASSOCIATION_GRP_INFO:
            {
                return "COMMAND_CLASS_ASSOCIATION_GRP_INFO";
            }
            break;

            case COMMAND_CLASS_SCENE_ACTIVATION:
            {
                return "COMMAND_CLASS_SCENE_ACTIVATION";
            }
            break;

            case COMMAND_CLASS_SCENE_ACTUATOR_CONF:
            {
                return "COMMAND_CLASS_SCENE_ACTUATOR_CONF";
            }
            break;

            case COMMAND_CLASS_SCENE_CONTROLLER_CONF:
            {
                return "COMMAND_CLASS_SCENE_CONTROLLER_CONF";
            }
            break;

            case COMMAND_CLASS_ZIP_GATEWAY:
            {
                return "COMMAND_CLASS_ZIP_GATEWAY";
            }
            break;

            case COMMAND_CLASS_ZIP_PORTAL:
            {
                return "COMMAND_CLASS_ZIP_PORTAL";
            }
#ifdef  TEST_EXT_CMD_CLASS
            case COMMAND_CLASS_EXT_TEST:        //Testing of extended command class
            {
                return "COMMAND_CLASS_EXT_TEST";
            }
            break;
#endif
            /******************skysoft******************/
            case COMMAND_CLASS_SWITCH_COLOR:
            {
                return "COMMAND_CLASS_SWITCH_COLOR";
            }
            break;

            case COMMAND_CLASS_BASIC_TARIFF_INFO:
            {
                return "COMMAND_CLASS_BASIC_TARIFF_INFO";
            }
            break;

            case COMMAND_CLASS_BARRIER_OPERATOR:
            {
                return "COMMAND_CLASS_BARRIER_OPERATOR";
            }
            break;

            case COMMAND_CLASS_LANGUAGE:
            {
                return "COMMAND_CLASS_LANGUAGE";
            }
            break;

            case COMMAND_CLASS_CENTRAL_SCENE:
            {
                return "COMMAND_CLASS_CENTRAL_SCENE";
            }
            break;

            case COMMAND_CLASS_ZIP_NAMING:
            {
                return "COMMAND_CLASS_ZIP_NAMING";
            }
            break;

            case COMMAND_CLASS_IP_ASSOCIATION:
            {
                return "COMMAND_CLASS_IP_ASSOCIATION";
            }
            break;

            /******************skysoft******************/
        default:
            return "UNKNOWN";
    }
}

/**
hl_bin2str - convert binary string to hex string
@param[in]  buf      The buffer that stores the binary string
@param[in]  len      The length of the binary string.
@param[out] hex_str  The buffer that stores the output string.
@param[in]  hex_str_len      The length of the output buffer.
@return
*/
void hl_bin2str(void * buf, uint32_t len, char *hex_str, uint32_t hex_str_len)
{
    uint8_t     *bin_byte = (uint8_t *)buf;
    char        tmp[8];

    hex_str[0] = '\0';

    //Convert a line of binary data into hex string
    while (len-- > 0)
    {
        sprintf(tmp,"%02X ",(unsigned) *bin_byte++);
        strcat(hex_str, tmp);
    }
}

static int hex2bin(char c)
{
    if (c >= '0' && c <= '9')
    {
        return c-'0';
    }
    else if (c >= 'a' && c <= 'f')
    {
        return c-'a' + 10;
    }
    else if (c >= 'A' && c <= 'F')
    {
        return c-'A' + 10;
    }
    else
    {
        return -1;
    }
}

static int hexstring_to_bin(char *psk_str, int psk_len, uint8_t *psk_bin)
{
    int i = 0;
    int val;

    while(psk_len > 0)
    {
        val = hex2bin(*psk_str++);
        if(val < 0)
            return -1;
        psk_bin[i]  = (val & 0x0F) << 4;

        val = hex2bin(*psk_str++);
        if(val < 0)
            return -1;
        psk_bin[i] |= (val & 0x0F);

        i++;
        psk_len -= 2;
    }

    return 0;
}

static int config_param_get(char *cfg_file, uint16_t *host_port, char *router, char *psk)
{
    FILE        *file;
    const char  delimiters[] = " =\r\n";
    char        line[384];
    char        *prm_name;
    char        *prm_val;

    //Initialize output
    *router = '\0';
    *psk = '\0';
    *host_port = 0;

    //Open config file
    if (!cfg_file)
    {
        return ZW_ERR_FILE_OPEN;
    }

    file = fopen(cfg_file, "rt");
    if (!file)
    {
        return ZW_ERR_FILE_OPEN;
    }

    while (fgets(line, 384, file))
    {
        if (*line == '#')
        {   //Skip comment line
            continue;
        }

        //Check if '=' exists
        if (strchr(line, '='))
        {
            //Get the parameter name and value
            prm_name = strtok(line, delimiters);

            if (prm_name)
            {
                prm_val = strtok(NULL, delimiters);

                if (!prm_val)
                {
                    continue;
                }

                //Compare the parameter name
                if (strcmp(prm_name, "ZipLanPort") == 0)
                {
                    unsigned port;
                    if (sscanf(prm_val, "%u", &port) == 1)
                    {
                        *host_port = (uint16_t)port;
                    }
                }
                else if (strcmp(prm_name, "ZipRouterIP") == 0)
                {
                    strcpy(router, prm_val);
                }
                else if (strcmp(prm_name, "DTLSPSK") == 0)
                {
                    strcpy(psk, prm_val);
                }
            }
        }
    }

    fclose(file);

    return 0;
}

static void hl_nw_tx_cb(void *user, uint8_t tx_sts)
{
    static const char    *tx_cmplt_sts[] = {"ok",
                                            "timeout: no ACK received",
                                            "system error",
                                            "destination host needs long response time",
                                            "frame failed to reach destination host"
    };

    if (tx_sts == TRANSMIT_COMPLETE_OK)
    {
        //printf("Higher level appl send data completed successfully\n");
    }
    else
    {
        ALOGE("Higher level appl send data completed with error:%s\n",
               (tx_sts < sizeof(tx_cmplt_sts)/sizeof(char *))? tx_cmplt_sts[tx_sts]  : "unknown");
    }
}

/**
hl_desc_cont_del - delete node descriptor container for the specified id
@param[in]  head        The head of the descriptor container linked-list
@param[in]  desc_id     Descriptor id of the node descriptor container
@return
@pre        Caller must lock the desc_cont_mtx before calling this function.
*/
static void hl_desc_cont_del(desc_cont_t **head, uint32_t desc_id)
{
    desc_cont_t     *last_node_cont;
    desc_cont_t     *last_ep_cont;
    desc_cont_t     *last_intf_cont;
    desc_cont_t     *next_desc_cont;
    desc_cont_t     *prev_desc_cont;    //Previous descriptor container
    zwifd_p         ifd;

    //Start searching from the first node
    last_node_cont = *head;
    prev_desc_cont = NULL;

    while (last_node_cont)
    {
        if (last_node_cont->id == desc_id)
        {
            //Delete endpoint
            last_ep_cont = last_node_cont->down;

            while (last_ep_cont)
            {

                //Delete interface
                last_intf_cont = last_ep_cont->down;

                while (last_intf_cont)
                {
                    //Store the next interface container
                    next_desc_cont = last_intf_cont->next;

                    //Free command class specific data
                    ifd = (zwifd_p)last_intf_cont->desc;

                    if (ifd->data_cnt > 0)
                    {
                        free(ifd->data);
                    }

                    //Free interface container
                    free(last_intf_cont);

                    //Get the next interface
                    last_intf_cont = next_desc_cont;
                }

                //Store the next endpoint container
                next_desc_cont = last_ep_cont->next;

                free(last_ep_cont);

                //Get the next endpoint
                last_ep_cont = next_desc_cont;
            }
            //Check whether this is the first node
            if (!prev_desc_cont)
            {   //First node
                *head = last_node_cont->next;
                free(last_node_cont);
                return;
            }
            //Not the first node
            prev_desc_cont->next = last_node_cont->next;
            free(last_node_cont);
            return;

        }

        //Get the next node
        prev_desc_cont = last_node_cont;
        last_node_cont = last_node_cont->next;
    }
}

/**
hl_desc_id_gen - genarate unique descriptor id
@param[in]  nw      Network
@return         Generated descriptor id
*/
static uint32_t  hl_desc_id_gen(zwnet_p nw)
{
    hl_appl_ctx_t       *hl_appl;
    zwnetd_t            *net_desc;

    net_desc = zwnet_get_desc(nw);

    hl_appl = (hl_appl_ctx_t *)net_desc->user;

    if (hl_appl->desc_id == 0)
    {   //ID of zero is invalid
        hl_appl->desc_id++;
    }
    return hl_appl->desc_id++;
}

/**
hl_desc_init - Initialize the descriptor linked-list
@param[in]  head    The head of the descriptor container linked-list
@param[in]  nw      Network
@return Zero on success; else return ZW_ERR_XXX
@pre        Caller must lock the desc_cont_mtx before calling this function.
*/
static int hl_desc_init(desc_cont_t **head, zwnet_p nw)
{
    int         result;
    zwnoded_t   noded;
    zwepd_t     ep_desc;
    zwifd_t     ifd;
    zwnoded_p   node;
    zwepd_p     ep;
    zwifd_p     intf;
    desc_cont_t *last_node_cont;
    desc_cont_t *last_ep_cont;
    desc_cont_t *last_intf_cont;

    *head = (desc_cont_t *)calloc(1, sizeof(desc_cont_t) + sizeof(zwnoded_t) - 1);
    if (*head == NULL)
    {
        return ZW_ERR_MEMORY;
    }
    (*head)->type = DESC_TYPE_NODE;
    (*head)->id = hl_desc_id_gen(nw);
    node = (zwnoded_p)(*head)->desc;
    last_node_cont = *head;

    result = zwnet_get_node(nw, node);
    if (result != 0)
    {
        //plt_msg_show("hl_desc_init get controller node with error:%d", result);
        return result;
    }

    while (node)
    {
        last_ep_cont = (desc_cont_t *)calloc(1, sizeof(desc_cont_t) + sizeof(zwepd_t) - 1);
        if (!last_ep_cont)
        {
            return ZW_ERR_MEMORY;
        }
        last_ep_cont->type = DESC_TYPE_EP;
        last_ep_cont->id = hl_desc_id_gen(nw);
        ep = (zwepd_p)last_ep_cont->desc;
        zwnode_get_ep(node, ep);
        last_node_cont->down = last_ep_cont;

        while (ep)
        {
            if (zwep_get_if(ep, &ifd) < 0)
            {
                break;
            }

            //Add interfaces
            last_intf_cont = (desc_cont_t *)calloc(1, sizeof(desc_cont_t) + sizeof(zwifd_t) - 1);
            if (!last_intf_cont)
            {
                return ZW_ERR_MEMORY;
            }

            last_intf_cont->type = DESC_TYPE_INTF;
            last_intf_cont->id = hl_desc_id_gen(nw);
            intf = (zwifd_p)last_intf_cont->desc;
            *intf = ifd;
            last_ep_cont->down = last_intf_cont;

            while (intf)
            {
                //Get the next interface
                result = zwif_get_next(intf, &ifd);
                if (result == 0)
                {
                    desc_cont_t     *intf_cont;
                    zwifd_p         ifdp;

                    intf = &ifd;
                    intf_cont = (desc_cont_t *)calloc(1, sizeof(desc_cont_t) + sizeof(zwifd_t) - 1);
                    if (!intf_cont)
                    {
                        return ZW_ERR_MEMORY;
                    }
                    intf_cont->type = DESC_TYPE_INTF;
                    intf_cont->id = hl_desc_id_gen(nw);
                    ifdp = (zwifd_p)intf_cont->desc;
                    *ifdp = ifd;
                    last_intf_cont->next = intf_cont;
                    last_intf_cont = intf_cont;
                }
                else
                {
                    intf = NULL;
                }
            }

            //Get the next endpoint
            result = zwep_get_next(ep, &ep_desc);
            if (result == 0)
            {
                desc_cont_t     *ep_cont;
                zwepd_p         epp;

                ep = &ep_desc;
                ep_cont = (desc_cont_t *)calloc(1, sizeof(desc_cont_t) + sizeof(zwepd_t) - 1);
                if (!ep_cont)
                {
                    return ZW_ERR_MEMORY;
                }
                ep_cont->type = DESC_TYPE_EP;
                ep_cont->id = hl_desc_id_gen(nw);
                epp = (zwepd_p)ep_cont->desc;
                *epp = ep_desc;
                last_ep_cont->next = ep_cont;
                last_ep_cont = ep_cont;

            }
            else
            {
                ep = NULL;
            }
        }

        //Get the next node
        result = zwnode_get_next(node, &noded);
        if (result == 0)
        {
            desc_cont_t     *node_cont;
            zwnoded_p       nodedp;

            node = &noded;

            node_cont = (desc_cont_t *)calloc(1, sizeof(desc_cont_t) + sizeof(zwnoded_t) - 1);
            if (!node_cont)
            {
                return ZW_ERR_MEMORY;
            }
            node_cont->type = DESC_TYPE_NODE;
            node_cont->id = hl_desc_id_gen(nw);
            nodedp = (zwnoded_p)node_cont->desc;
            *nodedp = noded;
            last_node_cont->next = node_cont;
            last_node_cont = node_cont;
        }
        else
        {
            node = NULL;
        }
    }
    return 0;
}

/**
hl_desc_cont_rm_all - remove the whole descriptor container linked-list
@param[in]  head        The head of the descriptor container linked-list
@return
@pre        Caller must lock the desc_cont_mtx before calling this function.
*/
static void hl_desc_cont_rm_all(desc_cont_t **head)
{
    while (*head)
    {
        hl_desc_cont_del(head, (*head)->id);
    }
}

/**
hl_node_desc_get - get node descriptor from descriptor container
@param[in]  head        The head of the descriptor container linked-list
@param[in]  desc_id     Unique descriptor id
@return     Node descriptor if found; else return NULL
@pre        Caller must lock the desc_cont_mtx before calling this function.
*/
static zwnoded_p  hl_node_desc_get(desc_cont_t *head, uint32_t desc_id)
{
    desc_cont_t     *last_node_cont;

    //Start searching from the first node
    last_node_cont = head;

    while (last_node_cont)
    {
        if (last_node_cont->id == desc_id)
        {
            if (last_node_cont->type == DESC_TYPE_NODE)
            {
                return(zwnoded_p)last_node_cont->desc;
            }
            //plt_msg_ts_show("hl_node_desc_get desc id:%u is not type node", desc_id);
            return NULL;
        }

        //Get the next node
        last_node_cont = last_node_cont->next;
    }
    //plt_msg_ts_show("hl_node_desc_get invalid desc id:%u", desc_id);
    return NULL;
}

/**
hl_node_updt - Update a node information
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
static int32_t hl_node_updt(hl_appl_ctx_t *hl_appl)
{
    int32_t     result;
    zwnoded_p   noded;

    //Get the node descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    noded = hl_node_desc_get(hl_appl->desc_cont_hd, hl_appl->dst_desc_id);

    if (!noded)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_NODE_NOT_FOUND;
    }

    hl_appl->node_updt_desc = *noded;
    ALOGD("node update, id= %d",noded->nodeid);
    result = zwnode_update(noded);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_node_updt with error:%d", result);
    }
    return result;
}

/**
hl_desc_id_get - get node descriptor id
@param[in]  head        The head of the descriptor container linked-list
@param[in]  noded   Node descriptor
@return     Node descriptor id if found; else return 0
@pre        Caller must lock the desc_cont_mtx before calling this function.
*/
static uint32_t  hl_desc_id_get(desc_cont_t *head, zwnoded_p noded)
{
    desc_cont_t     *curr_node_cont;
    zwnoded_p       curr_node_desc;     //current node descriptor

    //Start searching from the first node
    curr_node_cont = head;

    while (curr_node_cont)
    {
        curr_node_desc = (zwnoded_p)curr_node_cont->desc;

        if (curr_node_desc->nodeid == noded->nodeid)
        {
            return curr_node_cont->id;
        }

        //Get the next node
        curr_node_cont = curr_node_cont->next;
    }

    return 0;
}

/**
hl_desc_cont_add - add a node into the descriptor container linked-list
@param[in]  head    The head of the descriptor container linked-list
@param[in]  noded   Node descriptor
@return Zero on success; else return ZW_ERR_XXX
@pre        Caller must lock the desc_cont_mtx before calling this function.
*/
static int  hl_desc_cont_add(desc_cont_t **head, zwnoded_p noded)
{
    uint32_t    desc_id;
    int         result;
    zwepd_t     ep_desc;
    zwifd_t     ifd;
    zwnoded_t   updt_node;
    zwnoded_p   node;
    zwepd_p     ep;
    zwifd_p     intf;
    desc_cont_t     *last_node_cont;
    desc_cont_t     *last_ep_cont;
    desc_cont_t     *last_intf_cont;
    desc_cont_t     *new_node_cont;

    //Check whether the node already exists
    desc_id = hl_desc_id_get(*head, noded);

    if (desc_id)
    {   //Delete the existing node container
        hl_desc_cont_del(head, desc_id);
    }

    //Get the updated node descriptor
    if (zwnode_get_ep(noded, &ep_desc) < 0)
    {
        return ZW_ERR_EP_NOT_FOUND;
    }

    if (zwep_get_node(&ep_desc, &updt_node) < 0)
    {
        return ZW_ERR_NODE_NOT_FOUND;
    }

    //Check whether this is the first node to be added
    if (*head == NULL)
    {   //This is the first node to be added
        new_node_cont = (desc_cont_t *)calloc(1, sizeof(desc_cont_t)
                                                 + sizeof(zwnoded_t) - 1);
        if (!new_node_cont)
        {
            return ZW_ERR_MEMORY;
        }
        *head = new_node_cont;

    }
    else
    {   //Add new node container at the end of the list
        last_node_cont = *head;

        while (last_node_cont->next)
        {
            //Get the next node
            last_node_cont = last_node_cont->next;
        }
        new_node_cont = (desc_cont_t *)calloc(1, sizeof(desc_cont_t)
                                                 + sizeof(zwnoded_t) - 1);
        if (!new_node_cont)
        {
            return ZW_ERR_MEMORY;
        }

        last_node_cont->next = new_node_cont;
    }
    //Init the node container
    new_node_cont->type = DESC_TYPE_NODE;
    new_node_cont->id = hl_desc_id_gen(noded->net);
    node = (zwnoded_p)new_node_cont->desc;
    *node = updt_node;

    //Add endpoints
    last_ep_cont = (desc_cont_t *)calloc(1, sizeof(desc_cont_t) + sizeof(zwepd_t) - 1);
    if (!last_ep_cont)
    {
        return ZW_ERR_MEMORY;
    }
    last_ep_cont->type = DESC_TYPE_EP;
    last_ep_cont->id = hl_desc_id_gen(noded->net);
    ep = (zwepd_p)last_ep_cont->desc;
    zwnode_get_ep(node, ep);
    new_node_cont->down = last_ep_cont;

    while (ep)
    {
        if (zwep_get_if(ep, &ifd) < 0)
        {
            break;
        }

        //Add interfaces
        last_intf_cont = (desc_cont_t *)calloc(1, sizeof(desc_cont_t) + sizeof(zwifd_t) - 1);
        if (!last_intf_cont)
        {
            return ZW_ERR_MEMORY;
        }

        last_intf_cont->type = DESC_TYPE_INTF;
        last_intf_cont->id = hl_desc_id_gen(noded->net);
        intf = (zwifd_p)last_intf_cont->desc;
        *intf = ifd;
        last_ep_cont->down = last_intf_cont;

        while (intf)
        {
            //Get the next interface
            result = zwif_get_next(intf, &ifd);
            if (result == 0)
            {
                desc_cont_t     *intf_cont;
                zwifd_p         ifdp;

                intf = &ifd;
                intf_cont = (desc_cont_t *)calloc(1, sizeof(desc_cont_t) + sizeof(zwifd_t) - 1);
                if (!intf_cont)
                {
                    return ZW_ERR_MEMORY;
                }
                intf_cont->type = DESC_TYPE_INTF;
                intf_cont->id = hl_desc_id_gen(noded->net);
                ifdp = (zwifd_p)intf_cont->desc;
                *ifdp = ifd;
                last_intf_cont->next = intf_cont;
                last_intf_cont = intf_cont;
            }
            else
            {
                intf = NULL;
            }
        }

        //Get the next endpoint
        result = zwep_get_next(ep, &ep_desc);
        if (result == 0)
        {
            desc_cont_t     *ep_cont;
            zwepd_p         epp;

            ep = &ep_desc;
            ep_cont = (desc_cont_t *)calloc(1, sizeof(desc_cont_t) + sizeof(zwepd_t) - 1);
            if (!ep_cont)
            {
                return ZW_ERR_MEMORY;
            }
            ep_cont->type = DESC_TYPE_EP;
            ep_cont->id = hl_desc_id_gen(noded->net);
            epp = (zwepd_p)ep_cont->desc;
            *epp = ep_desc;
            last_ep_cont->next = ep_cont;
            last_ep_cont = ep_cont;
        }
        else
        {
            ep = NULL;
        }
    }

    return 0;
}

static void hl_nw_notify_hdlr(nw_notify_msg_t *notify_msg)
{
    hl_appl_ctx_t    *hl_appl = notify_msg->hl_appl;
    int              result;

    //Check to display progress of get detailed node info
    if (notify_msg->sts & OP_GET_NI_TOTAL_NODE_MASK)
    {
        uint16_t    total_nodes;
        uint16_t    cmplt_nodes;

        total_nodes = (uint16_t)((notify_msg->sts & OP_GET_NI_TOTAL_NODE_MASK) >> 8);
        cmplt_nodes = (uint16_t)(notify_msg->sts & OP_GET_NI_NODE_CMPLT_MASK);
        ALOGI("hl_nw_notify_cb op:%u, get node info %u/%u completed",
                        (unsigned)notify_msg->op, cmplt_nodes, total_nodes);
        return;
    }

    ALOGI("hl_nw_notify_cb op:%u, status:%u", (unsigned)notify_msg->op, notify_msg->sts);

    switch (notify_msg->op)
    {
        case ZWNET_OP_UPDATE://TODO: update all the nodes, instead of rebuilding.
        case ZWNET_OP_INITIALIZE:
        case ZWNET_OP_INITIATE:
        case ZWNET_OP_RESET:

            if (notify_msg->sts == ZW_ERR_NONE)
            {
                hl_appl->init_status = 1;

                int result = zwnode_intf_reset(&hl_appl->zwnet->ctl);
                if(result == 0)
                {
                    ALOGI("reset node intf.");
                }

                result = zwnet_load(hl_appl->zwnet,
                        (hl_appl->load_ni_file)? hl_appl->node_info_file : NULL);

                if (result != 0)
                {
                    ALOGE("hl_init load node info with error:%d", result);
                } else if(result == 0){
                    ALOGI("Init: load node info successfully");
                }

               //Rebuild the descriptor container linked-list
                plt_mtx_lck(hl_appl->desc_cont_mtx);
                hl_desc_cont_rm_all(&hl_appl->desc_cont_hd);
                result = hl_desc_init(&hl_appl->desc_cont_hd, hl_appl->zwnet);
                if (result != 0)
                {
                    ALOGE("hl_desc_init with error:%d", result);
                }

                plt_mtx_ulck(hl_appl->desc_cont_mtx);
            }

            //ALOGI("Network initialized!  Setting up unsolicited address, please wait ...\n");
            break;

        case ZWNET_OP_NODE_UPDATE:
            {
                //Check whether stress test in progress
                if (hl_appl->is_ni_stress_tst_run)
                {
                    hl_node_updt(hl_appl);
                    break;
                }

                //Update the node descriptor container
                plt_mtx_lck(hl_appl->desc_cont_mtx);
                //hl_desc_cont_updt(&hl_appl->desc_cont_hd, &hl_appl->node_updt_desc);
                hl_desc_cont_add(&hl_appl->desc_cont_hd, &hl_appl->node_updt_desc);
                plt_mtx_ulck(hl_appl->desc_cont_mtx);
            }
            break;

        case ZWNET_OP_ADD_NODE:
        case ZWNET_OP_MIGRATE:
        case ZWNET_OP_MIGRATE_SUC:
            if (notify_msg->sts == ZW_ERR_NONE)
            {
                //Update the node descriptor container
                plt_mtx_lck(hl_appl->desc_cont_mtx);
                hl_desc_cont_add(&hl_appl->desc_cont_hd, &hl_appl->node_add_desc);
                plt_mtx_ulck(hl_appl->desc_cont_mtx);
            }

            if(notify_msg->op == ZWNET_OP_ADD_NODE)
            {
                if(notify_msg->sts == ZW_ERR_NONE)
                {
                    hl_appl->sec2_cb_enter = 0;
                    hl_appl->sec2_cb_exit = 1;
                    //hl_appl->add_status = ADD_NODE_STS_DONE;
                }
                else if (notify_msg->sts == OP_FAILED)
                {   //Clear add node DSK callback control & status
                    hl_appl->sec2_cb_enter = 0;
                    hl_appl->sec2_cb_exit = 1;
                   //hl_appl->add_status = ADD_NODE_STS_UNKNOWN;
               }
            }

            break;

        case ZWNET_OP_RP_NODE:
            if (notify_msg->sts == ZW_ERR_NONE)
            {
                //Update the node descriptor container
                plt_mtx_lck(hl_appl->desc_cont_mtx);
                hl_desc_cont_add(&hl_appl->desc_cont_hd, &hl_appl->node_rp_desc);
                plt_mtx_ulck(hl_appl->desc_cont_mtx);
            }
            break;
        case ZWNET_OP_RM_NODE:
            if (notify_msg->sts == OP_DONE)
            {
                //hl_appl->rm_status = RM_NODE_STS_DONE;
            }
            else if (notify_msg->sts == OP_FAILED)
            {
                //hl_appl->rm_status = RM_NODE_STS_UNKNOWN;
            }
            break;
    }
}

static void dummy_post_msg(void *msg)
{
    hl_nw_notify_hdlr((nw_notify_msg_t *)msg);
    free(msg);
}

static char* hl_nw_create_op_msg(uint8_t op, uint16_t sts)
{
    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return NULL;
    }

    if(op == ZWNET_OP_ADD_NODE)
    {
        cJSON_AddStringToObject(jsonRoot, "MessageType", "Node Add Status");

        if(sts == OP_FAILED)
        {
            cJSON_AddStringToObject(jsonRoot, "Status", "Failed");
        }
        else if(sts == OP_ADD_NODE_ADDING)
        {
            cJSON_AddStringToObject(jsonRoot, "Status", "Adding");
        }
        else if(sts == OP_ADD_NODE_PROTOCOL_DONE)
        {
            cJSON_AddStringToObject(jsonRoot, "Status", "Protocol Done");
        }
        else if(sts == OP_ADD_NODE_LEARN_READY)
        {
            cJSON_AddStringToObject(jsonRoot, "Status", "Learn Ready");
        }
        else if(sts == OP_ADD_NODE_SEC_INCD)
        {
            cJSON_AddStringToObject(jsonRoot, "Status", "Adding Node Securely");
        }
        else if(sts == OP_ADD_NODE_GET_NODE_INFO)
        {
            cJSON_AddStringToObject(jsonRoot, "Status", "Getting Node Information");
        }
        else if(sts == OP_ADD_NODE_FOUND)
        {
            cJSON_AddStringToObject(jsonRoot, "Status", "Node Found");
        }
        else if(sts == OP_DONE)
        {
            cJSON_AddStringToObject(jsonRoot, "Status", "Success");
        }

        char *p = cJSON_Print(jsonRoot);

        if(p == NULL)
        {
            cJSON_Delete(jsonRoot);
            return NULL;
        }

        cJSON_Delete(jsonRoot);

        return p;
    }
    else if(op == ZWNET_OP_RM_NODE)
    {
        cJSON_AddStringToObject(jsonRoot, "MessageType", "Node Remove Status");

        if(sts == OP_FAILED)
        {
            cJSON_AddStringToObject(jsonRoot, "Status", "Failed");
        }
        else if(sts == OP_RM_NODE_REMOVING)
        {
            cJSON_AddStringToObject(jsonRoot, "Status", "Removing");
        }
        else if(sts == OP_RM_NODE_LEARN_READY)
        {
            cJSON_AddStringToObject(jsonRoot, "Status", "Learn Ready");
        }
        else if(sts == OP_RM_NODE_FOUND)
        {
            cJSON_AddStringToObject(jsonRoot, "Status", "Node Found");
        }
        else if(sts == OP_DONE)
        {
            cJSON_AddStringToObject(jsonRoot, "Status", "Success");
        }

        char *p = cJSON_Print(jsonRoot);

        if(p == NULL)
        {
            cJSON_Delete(jsonRoot);
            return NULL;
        }

        cJSON_Delete(jsonRoot);

        return p;
    }
    else if(op == ZWNET_OP_INITIALIZE)
    {
        cJSON_AddStringToObject(jsonRoot, "MessageType", "Network Init Status");
        if(sts == OP_DONE)
        {
            cJSON_AddStringToObject(jsonRoot, "Status", "Success");
        }

        char *p = cJSON_Print(jsonRoot);

        if(p == NULL)
        {
            cJSON_Delete(jsonRoot);
            return NULL;
        }

        cJSON_Delete(jsonRoot);

        return p;
    }

    return NULL;
}

static void hl_nw_notify_cb(void *user, uint8_t op, uint16_t sts, zwnet_sts_t *info)
{
    nw_notify_msg_t  *nw_notify;

    nw_notify = (nw_notify_msg_t *)malloc(sizeof(nw_notify_msg_t));

    if (nw_notify)
    {
        nw_notify->hl_appl = (hl_appl_ctx_t *)user;
        nw_notify->op = op;
        nw_notify->sts = sts;

        //Post message to main windows
        //PostMessage(ghWnd, MSG_ZWAPI_NOTIFY, 0, (LPARAM )nw_notify);
        dummy_post_msg(nw_notify);

        char *str = hl_nw_create_op_msg(op, sts);

        if(str != NULL)
        {
            if(resCallBack)
            {
                resCallBack(str);
            }
            free(str);
        }
    }
}

static char  *prompt_str(hl_appl_ctx_t *hl_appl, const char *disp_str, int out_buf_sz, char *out_str)
{
    int retry;

    puts(disp_str);
    retry = 3;
    while (retry-- > 0)
    {
        if (fgets(out_str, out_buf_sz, stdin) && (*out_str) && ((*out_str) != '\n'))
        {
            char *newline;
            //Remove newline character

            newline = strchr(out_str, '\n');
            if (newline)
            {
                *newline = '\0';
            }
            return out_str;
        }
    }
    return NULL;
}

static unsigned prompt_hex(hl_appl_ctx_t *hl_appl, char *str)
{
    char user_input_str[36];
    unsigned  ret;

    if (prompt_str(hl_appl, str, 36, user_input_str))
    {
#ifdef USE_SAFE_VERSION
        if (sscanf_s(user_input_str, "%x", &ret) == 1)
        {
            return ret;
        }
#else
        if (sscanf(user_input_str, "%x", &ret) == 1)
        {
            return ret;
        }
#endif
    }
    return 0;
}

static void hl_add_node_s2_cb(void *usr_param, sec2_add_cb_prm_t *cb_param)
{
    hl_appl_ctx_t *hl_appl = (hl_appl_ctx_t *)usr_param;
    int           res;

    if (cb_param->cb_type == S2_CB_TYPE_REQ_KEY)
    {
        uint8_t granted_key;
        uint8_t grant_csa;

        if (hl_appl->sec2_cb_enter & SEC2_ENTER_KEY_REQ)
        {   //Requested keys callback is allowed
            hl_appl->sec2_cb_enter &= ~SEC2_ENTER_KEY_REQ;
        }
        else
        {
            ALOGE("\nNot allowed to processed Security 2 requested keys callback!\n");
            return;
        }

        ALOGD("\nDevice requested keys bit-mask: %02Xh\n", cb_param->cb_prm.req_key.req_keys);

        ALOGD("Key (bit-mask in hex) :\n");
        ALOGD("                      Security 2 key 0 (01)\n");
        ALOGD("                      Security 2 key 1 (02)\n");
        ALOGD("                      Security 2 key 2 (04)\n");
        ALOGD("                      Security 0       (80)\n");

        granted_key = prompt_hex(hl_appl, "Grant keys bit-mask (hex):");

        grant_csa = 0;
        if (cb_param->cb_prm.req_key.req_csa)
        {
            ALOGD("Device requested for client-side authentication (CSA)\n");

            grant_csa = 1;
            ALOGD("Please enter this 10-digit CSA Pin into the joining device:%s\n", cb_param->cb_prm.req_key.csa_pin);

            //No DSK callback when in CSA mode
            hl_appl->sec2_cb_enter &= ~SEC2_ENTER_DSK;

        }

        res = zwnet_add_sec2_grant_key(hl_appl->zwnet, granted_key, grant_csa);

        if (res != 0)
        {
            ALOGE("zwnet_add_sec2_grant_key with error: %d\n", res);
        }

        //Check whether if there is DSK callback pending
        if (!(hl_appl->sec2_cb_enter))
        {   //No callback pending
            hl_appl->sec2_cb_exit = 1;
        }
    }
    else
    {
        sec2_dsk_cb_prm_t   *dsk_prm;
        int                 accept;
        char                dsk_str[200];

        if (hl_appl->sec2_cb_enter & SEC2_ENTER_DSK)
        {   //DSK callback is allowed
            hl_appl->sec2_cb_enter &= ~SEC2_ENTER_DSK;
        }
        else
        {
            ALOGE("\nNot allowed to processed Security 2 DSK callback!\n");
            return;
        }

        dsk_prm = &cb_param->cb_prm.dsk;

        if (dsk_prm->pin_required)
        {
            ALOGD("\nReceived DSK: XXXXX%s\n", dsk_prm->dsk);
        }
        else
        {
            ALOGD("\nReceived DSK: %s\n", dsk_prm->dsk);
        }

        accept = 1;

        ALOGD("You %s the device.\n", (accept)? "accepted" : "rejected");

        if (accept && dsk_prm->pin_required)
        {
            if (prompt_str(hl_appl, "Enter 5-digit PIN that matches the received DSK:", 200, dsk_str))
            {

#ifdef USE_SAFE_VERSION
                strcat_s(dsk_str, 200, dsk_prm->dsk);
#else
                strcat(dsk_str, dsk_prm->dsk);
#endif
            }
        }

        res = zwnet_add_sec2_accept(hl_appl->zwnet, accept, (dsk_prm->pin_required)? dsk_str : dsk_prm->dsk);

        if (res != 0)
        {
            ALOGE("zwnet_add_sec2_accept with error: %d\n", res);
        }

        hl_appl->sec2_cb_exit = 1;
    }
}

/**
hl_nw_node_cb - Callback function to notify node is added, deleted, or updated
@param[in]  user        The high-level api context
@param[in]  noded   Node
@param[in]  mode        The node status
@return
*/
static void hl_nw_node_cb(void *user, zwnoded_p noded, int mode)
{
    hl_appl_ctx_t   *hl_appl = (hl_appl_ctx_t *)user;

    switch (mode)
    {
        case ZWNET_NODE_ADDED:
            {
                ALOGI("hl_nw_node_cb node:%u added", (unsigned)noded->nodeid);
                //Store the last added node descriptor
                hl_appl->node_add_desc = *noded;

                //Add node descriptor container
                plt_mtx_lck(hl_appl->desc_cont_mtx);
                //hl_desc_cont_add(&hl_appl->desc_cont_hd, noded); //djnakata
                plt_mtx_ulck(hl_appl->desc_cont_mtx);
            }
            break;

        case ZWNET_NODE_REMOVED:
            {
                ALOGI("hl_nw_node_cb node:%u removed", (unsigned)noded->nodeid);
                //Remove the node descriptor container
                plt_mtx_lck(hl_appl->desc_cont_mtx);
                hl_desc_cont_del(&hl_appl->desc_cont_hd, hl_desc_id_get(hl_appl->desc_cont_hd, noded));
                plt_mtx_ulck(hl_appl->desc_cont_mtx);
            }
            break;

        case ZWNET_NODE_UPDATED:
            {
                ALOGI("hl_nw_node_cb node:%u updated", (unsigned)noded->nodeid);
                //Store the last replaced node descriptor
                hl_appl->node_rp_desc = *noded;

                //Update the node descriptor container
                plt_mtx_lck(hl_appl->desc_cont_mtx);
                //hl_desc_cont_updt(&hl_appl->desc_cont_hd, noded);
                hl_desc_cont_add(&hl_appl->desc_cont_hd, noded);
                plt_mtx_ulck(hl_appl->desc_cont_mtx);
            }
            break;

        default:
            break;
    }
}

static int lib_init(hl_appl_ctx_t *hl_appl, uint16_t host_port, uint8_t *zip_router_ip, int use_ipv4,
             char *dev_cfg_file_name, uint8_t *dtls_psk, uint8_t dtls_psk_len, char *pref_dir)
{
    int                 result;
    zwnet_init_t        zw_init = {0};

    zw_init.user = hl_appl; //high-level application context
    zw_init.node = hl_nw_node_cb;
    zw_init.notify = hl_nw_notify_cb;
    zw_init.appl_tx = hl_nw_tx_cb;
    zw_init.pref_dir = pref_dir;
    zw_init.print_txt_fn = NULL;
    zw_init.net_info_dir = NULL;
    zw_init.host_port = host_port;
    zw_init.use_ipv4 = use_ipv4;
    memcpy(zw_init.zip_router, zip_router_ip, (use_ipv4)? IPV4_ADDR_LEN : IPV6_ADDR_LEN);
    zw_init.dev_cfg_file = dev_cfg_file_name;
    zw_init.dev_cfg_usr = NULL;
    zw_init.dtls_psk_len = dtls_psk_len;
    if (dtls_psk_len)
    {
        memcpy(zw_init.dtls_psk, dtls_psk, dtls_psk_len);
    }
    //Unhandled command handler
    zw_init.unhandled_cmd = NULL;

    //Init ZW network
    result = zwnet_init(&zw_init, &hl_appl->zwnet);

    if (result != 0)
    {
        ALOGE("zwnet_init with error:%d\n", result);

        //Display device configuration file error
        if (zw_init.err_loc.dev_ent)
        {
            ALOGE("Parsing device configuration file error loc:\n");
            ALOGE("Device entry number:%u\n", zw_init.err_loc.dev_ent);
            if (zw_init.err_loc.ep_ent)
            {
                ALOGE("Endpoint entry number:%u\n", zw_init.err_loc.ep_ent);
            }

            if (zw_init.err_loc.if_ent)
            {
                ALOGE("Interface entry number:%u\n", zw_init.err_loc.if_ent);
            }
        }
        return result;
    }

    return 0;
}

static int nw_init(const char* resPath, hl_appl_ctx_t *hl_appl)
{
    int             ret;
    char            zip_gw_addr_str[100];
    uint16_t        host_port;                  ///< Host listening port
    char            psk_str[384];
    int             psk_len;
    uint8_t         dtls_psk[MAX_DTLS_PSK/2];   ///< DTLS pre-shared key
    uint8_t         zip_gw_ip[16];              ///< Z/IP gateway address in IPv4 or IPv6

    char cfgfile[500] = {0};
    sprintf(cfgfile, "%s/app.cfg", resPath);

    //Read config file to get configuration parameters
    ret = config_param_get(cfgfile, &host_port, zip_gw_addr_str, psk_str);
    if (ret != 0)
    {
        ALOGE("Error: couldn't get config param from file: app.cfg\n");
        return ret;
    }

    //Check DTLS pre-shared key validity
    psk_len = strlen(psk_str);

    if (psk_len > 0)
    {
        if (psk_len > MAX_DTLS_PSK)
        {
            ALOGE("PSK string length is too long\n");
            return ZW_ERR_VALUE;
        }
        if (psk_len % 2)
        {
            ALOGE("PSK string length should be even\n");
            return ZW_ERR_VALUE;
        }
        //Convert ASCII hexstring to binary string
        ret = hexstring_to_bin(psk_str, psk_len, dtls_psk);
        if (ret != 0)
        {
            ALOGE("PSK string is not hex string\n");
            return ZW_ERR_VALUE;
        }
    }

    //Convert IPv4 / IPv6 address string to numeric equivalent
    ret = zwnet_ip_aton(zip_gw_addr_str, zip_gw_ip, &hl_appl->use_ipv4);

    if (ret != 0)
    {
        ALOGE("Invalid Z/IP router IP address:%s\n", zip_gw_addr_str);
        return ZW_ERR_IP_ADDR;
    }

    char device_rec[500] = {0};
    sprintf(device_rec, "%s/zwave_device_rec.txt", resPath);

    //Initialize library
    ret = lib_init(hl_appl, host_port, zip_gw_ip, hl_appl->use_ipv4, device_rec,
                   dtls_psk, psk_len/2, NULL);

    if (ret < 0)
    {
        ALOGE("lib_init with error: %d\n", ret);
    }
    return ret;
}

static int hl_add_node(hl_appl_ctx_t *hl_appl, const char* dsk, int dsklen)
{
    int     res;
    char    dsk_str[200];
    zwnetd_p netdesc;

    netdesc = zwnet_get_desc(hl_appl->zwnet);

    if (netdesc->ctl_cap & ZWNET_CTLR_CAP_S2)
    {
        ALOGD("Controller supports security 2.\n");
        hl_appl->sec2_add_node = 1;
    }
    else
    {
        hl_appl->sec2_add_node = 0;
    }

    if (hl_appl->sec2_add_node)
    {
        hl_appl->sec2_add_prm.dsk = NULL;

        if(dsk != NULL && dsklen != 0)
        {
            memcpy(dsk_str, dsk, 200);
            hl_appl->sec2_add_prm.dsk = dsk_str;
        }

        hl_appl->sec2_add_prm.usr_param = hl_appl;
        hl_appl->sec2_add_prm.cb = hl_add_node_s2_cb;
    }

    res = zwnet_add(hl_appl->zwnet, 1, (hl_appl->sec2_add_node)? &hl_appl->sec2_add_prm : NULL, 0);

    if (res == 0)
    {
        if (hl_appl->sec2_add_node)
        {
            int wait_count;

            hl_appl->sec2_cb_enter = SEC2_ENTER_KEY_REQ;

            if (!hl_appl->sec2_add_prm.dsk)
            {   //No pre-entered DSK, requires DSK callback
                hl_appl->sec2_cb_enter |= SEC2_ENTER_DSK;
            }

            hl_appl->sec2_cb_exit = 0;

            ALOGD("Waiting for Requested keys and/or DSK callback ...\n");

            //Wait for S2 callback to exit
            wait_count = 600;    //Wait for 60 seconds
            while (wait_count-- > 0)
            {
                if (hl_appl->sec2_cb_exit == 1)
                    break;
                plt_sleep(100);
            }
        }
    }

    return res;
}

int zwcontrol_init(hl_appl_ctx_t *hl_appl, const char *resPath, const char* infopath, uint8_t* result)
{
    if(hl_appl->init_status != 0)
    {
        return 0;
    }

    if(StartZipGateWay(resPath) != 0)
    {
        return -1;
    }

    if(infopath == NULL)
    {
        return -1;
    }

    memset(hl_appl, 0, sizeof(hl_appl_ctx_t));

    if(access(infopath,F_OK) == 0)
    {
        hl_appl->load_ni_file = 1;
        hl_appl->save_ni_file = 1;
        ALOGD("nodeinfo file exists, will load it.");
        strcpy(hl_appl->save_file,infopath);
        strcpy(hl_appl->node_info_file,infopath);
    }else{
        ALOGD("nodeinfo file not exists, first init");
    }

    sleep(2);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        goto INIT_ERROR;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Controller Attribute");

    hl_appl->use_ipv4 = 1; //Use IPv4

    //Init high-level appl layer
    if (!plt_mtx_init(&hl_appl->desc_cont_mtx))
    {
        goto INIT_ERROR;
    }

    if(nw_init(resPath, hl_appl) != 0)
        goto INIT_ERROR;

    int num = 1;

    while(num <= 20)
    {
        if(hl_appl->init_status == 1)
            break;

        usleep(500000);

        ++num;
    }

    if(num > 20)
    {
        return -1;
    }

    {
        zwnode_p    zw_node;
        zwif_p      intf;
        zwep_p      ep;
        char str[50] = {0};
        plt_mtx_lck(hl_appl->zwnet->mtx);

        zw_node = &hl_appl->zwnet->ctl;

        if(zw_node)
        {
            sprintf(str, "%08X", (unsigned)hl_appl->zwnet->homeid);
            cJSON_AddStringToObject(jsonRoot, "Home Id", str);
            cJSON_AddNumberToObject(jsonRoot, "Node Id", (unsigned)zw_node->nodeid);

            sprintf(str, "%04X", zw_node->vid);
            cJSON_AddStringToObject(jsonRoot, "Vendor Id", str);

            sprintf(str, "%04x", zw_node->vtype);
            cJSON_AddStringToObject(jsonRoot, "Vendor Product Type", str);

            cJSON_AddNumberToObject(jsonRoot, "Z-wave Library Type", zw_node->lib_type);

            sprintf(str, "%04x", zw_node->pid);
            cJSON_AddStringToObject(jsonRoot, "Product Id", str);

            sprintf(str, "%u.%02u", (unsigned)(zw_node->proto_ver >> 8), (unsigned)(zw_node->proto_ver & 0xFF));
            cJSON_AddStringToObject(jsonRoot, "Z-wave Protocol Version", str);

            sprintf(str, "%u.%02u", (unsigned)(zw_node->app_ver >> 8), (unsigned)(zw_node->app_ver & 0xFF));
            cJSON_AddStringToObject(jsonRoot, "Application Version", str);
        }

        plt_mtx_ulck(hl_appl->zwnet->mtx);
    }

    if(result != NULL)
    {
        char *p = cJSON_Print(jsonRoot);

        if(p != NULL)
        {
            strcpy((char*)result, p);
            free(p);
        }
    }

    return 0;

INIT_ERROR:
    StopZipGateWay();
    return -1;
}

int zwcontrol_setcallback(ResCallBack callBack)
{
    resCallBack = callBack;
    return 0;
}

int zwcontrol_add_node(hl_appl_ctx_t *hl_appl, const char* dsk, int dsklen)
{
    if(hl_appl->init_status == 0)
    {
        return -1;
    }

    int result = -1;

    result = hl_add_node(hl_appl, dsk, dsklen);

    if(hl_appl->sec2_add_node)
    {
        if (result == 0)
        {
            ALOGD("Add node in progress, please wait for status ...\n");
        }
        else
        {
            ALOGE("Add node with error:%d\n", result);
        }
    }

    return result;
}

int zwcontrol_exit(hl_appl_ctx_t *hl_appl)
{
    if(hl_appl->init_status == 0)
    {
        return -1;
    }

    zwnet_exit(hl_appl->zwnet);

    StopZipGateWay();

    return 0;
}

int  zwcontrol_rm_node(hl_appl_ctx_t *hl_appl)
{
    if (hl_appl->init_status == 0){
        ALOGE("Controller not open, please open it and try again");
        return -1;
    }

    ALOGD("Controller remove node");
    int result;
    result = zwnet_add(hl_appl->zwnet, 0, NULL, 0);

    if (result != 0)
    {
        ALOGE("zwcontrol_rm_node with error:%d", result);
    }

    return result;
}

/**
hl_plt_ctx_get - Get platform context
@return         platform context
*/
static void  *hl_plt_ctx_get(hl_appl_ctx_t *hl_appl)
{
    if (!hl_appl->plt_ctx)
    {
        zwnetd_t    *net_desc;
        //Get and save the context
        net_desc = zwnet_get_desc(hl_appl->zwnet);

        hl_appl->plt_ctx = net_desc->plt_ctx;
    }

    return hl_appl->plt_ctx;
}

const char *sensor_type_str[] =
        {
                "undefined",
                "Temperature sensor",
                "General purpose sensor",
                "Luminance sensor",
                "Power sensor",
                "Relative humidity sensor",
                "Velocity sensor",
                "Direction sensor",
                "Atmospheric pressure sensor",
                "Barometric pressure sensor",
                "Solar radiation sensor",
                "Dew point sensor",
                "Rain rate sensor",
                "Tide level sensor",
                "Weight sensor",
                "Voltage sensor",
                "Current sensor",
                "CO2-level sensor",
                "Air flow sensor",
                "Tank capacity sensor",
                "Distance sensor",
                "Angle Position sensor",
                "Rotation sensor",
                "Water temperature sensor",
                "Soil temperature sensor",
                "Seismic intensity sensor",
                "Seismic magnitude sensor",
                "Ultraviolet sensor",
                "Electrical resistivity sensor",
                "Electrical conductivity sensor",
                "Loudness sensor",
                "Moisture sensor",
                "Frequency sensor",
                "Time sensor",
                "Target temperature sensor"
        };
const char *sensor_unit_str[] =
        {
                NULL,
                NULL,
                NULL,
                NULL,
                "Celsius (C)",    //Air temperature unit
                "Fahrenheit (F)",
                NULL,
                NULL,
                "Percentage",   //General purpose unit
                "Dimensionless value",
                NULL,
                NULL,
                "Percentage",   //Luminance unit
                "Lux",
                NULL,
                NULL,
                "W",      //Power unit
                "Btu/h",
                NULL,
                NULL,
                "Percentage",   //Relative humidity unit
                "g/m3",
                NULL,
                NULL,
                "m/s",      //Velocity unit
                "mph",
                NULL,
                NULL,
                "degrees",      //Direction unit
                NULL,
                NULL,
                NULL,
                "kPa",        //Atmospheric pressure unit
                "inches of Mercury",
                NULL,
                NULL,
                "kPa",        //Barometric pressure unit
                "inches of Mercury",
                NULL,
                NULL,
                "W/m2",         //Solar radiation unit
                NULL,
                NULL,
                NULL,
                "Celsius (C)",    //Dew point unit
                "Fahrenheit (F)",
                NULL,
                NULL,
                "mm/h",   //Rain rate unit
                "in/h",
                NULL,
                NULL,
                "m",      //Tide level unit
                "feet",
                NULL,
                NULL,
                "kg",       //Weight unit
                "pounds",
                NULL,
                NULL,
                "V",      //Voltage unit
                "mV",
                NULL,
                NULL,
                "A",   //Current unit
                "mA",
                NULL,
                NULL,
                "ppm",    //CO2-level unit
                NULL,
                NULL,
                NULL,
                "m3/h",   //Air flow unit
                "cfm",
                NULL,
                NULL,
                "l",    //Tank capacity unit
                "cbm",
                "US gallons",
                NULL,
                "m",      //Distance unit
                "cm",
                "feet",
                NULL,
                "Percentage",     //Angle Position unit
                "Degrees rel. to north pole",
                "Degrees rel. to south pole",
                NULL,
                "rpm",   //Rotation unit
                "Hz",
                NULL,
                NULL,
                "Celsius (C)",    //Water temperature unit
                "Fahrenheit (F)",
                NULL,
                NULL,
                "Celsius (C)",    //Soil temperature unit
                "Fahrenheit (F)",
                NULL,
                NULL,
                "Mercalli",       //Seismic intensity unit
                "European Macroseismic",
                "Liedu",
                "Shindo",
                "Local (ML)",     //Seismic magnitude unit
                "Moment (MW)",
                "Surface wave (MS)",
                "Body wave (MB)",
                "UV index",    //Ultraviolet unit
                NULL,
                NULL,
                NULL,
                "ohm metre",    //Electrical resistivity unit
                NULL,
                NULL,
                NULL,
                "siemens per metre",    //Electrical conductivity unit
                NULL,
                NULL,
                NULL,
                "Absolute loudness (dB)", //Loudness unit
                "A-weighted decibels (dBA)",
                NULL,
                NULL,
                "Percentage", //Moisture unit
                "Volume water content (m3/m3)",
                "Impedance (k ohm)",
                "Water activity (aw)",
                "Hertz (Hz)",           //Frequency unit
                "Kilo Hertz (KHz)",
                NULL,
                NULL,
                "Second (s)",           //Time unit
                NULL,
                NULL,
                NULL,
                "Celsius (C)",          //Target temperature unit
                "Fahrenheit (F)",
                NULL,
                NULL
        };

/**
hl_ext_ver_show - Show extended version information
@param[in]  hl_appl   The high-level api context
@param[in]  node      Node
@return
*/
static void hl_ext_ver_show(hl_appl_ctx_t *hl_appl, zwnoded_p node, cJSON *Node)
{
    ext_ver_t   *ext_ver;
    int         i;
    char str[50] = {0};

    ext_ver = zwnode_get_ext_ver(node);
    if (ext_ver)
    {
        ALOGI("Hardware version:%u", (unsigned)(ext_ver->hw_ver));
        cJSON_AddNumberToObject(Node, "Hardware version", ext_ver->hw_ver);

        for (i=0; i<ext_ver->fw_cnt; i++)
        {
            ALOGI("Firmware %d version:%u.%02u", i+1, (unsigned)(ext_ver->fw_ver[i] >> 8),
                         (unsigned)(ext_ver->fw_ver[i] & 0xFF));

            cJSON_AddNumberToObject(Node, "Firmware", i+1);
            sprintf(str, "%u.%02u", ext_ver->fw_ver[i] >> 8, ext_ver->fw_ver[i] & 0xFF);
            cJSON_AddStringToObject(Node, "Firmware version", str);
        }
        free(ext_ver);
    }
}

/**
hl_dev_id_show - Show device id
@param[in]  hl_appl   The high-level api context
@param[in]  node      Node
@return
*/
static void hl_dev_id_show(hl_appl_ctx_t *hl_appl, dev_id_t *dev_id)
{
    const char *dev_id_type_str[] =
            {
                    "Device id oem",
                    "Device serial number",
                    "Device id unknown type"
            };
    uint8_t   id_type;

    id_type = (uint8_t)((dev_id->type > DEV_ID_TYPE_SN)? 2 : dev_id->type);

    if (dev_id->format == DEV_ID_FMT_UTF)
    {   //UTF-8
        ALOGI("%s:%s", dev_id_type_str[id_type], dev_id->dev_id);
    }
    else if (dev_id->format == DEV_ID_FMT_BIN)
    {   //Binary
        char hex_string[(32*3)+1];

        hl_bin2str(dev_id->dev_id, dev_id->len, hex_string, (32*3)+1);
        ALOGI("%s:h'%s", dev_id_type_str[id_type], hex_string);
    }
}

// Transfer zwave plus icon type to device type
static char* hl_zwaveplus_icon_to_device_type(uint16_t  usr_icon)
{
    switch(usr_icon)
    {
        case ICON_TYPE_GENERIC_ON_OFF_POWER_SWITCH:
        {
            return "On/Off Power Switch";
        }
        break;
        case ICON_TYPE_SPECIFIC_DIMMER_WALL_SWITCH_FOUR_BUTTONS:
        {
            return "Wall Switch-4 buttons";
        }
        break;
        case ICON_TYPE_GENERIC_LIGHT_DIMMER_SWITCH:
        {
            return "Light Dimmer Switch";
        }
        break;
        case ICON_TYPE_SPECIFIC_LIGHT_DIMMER_SWITCH_PLUGIN:
        {
            return "Light Dimmer with plugin";
        }
        break;
        case ICON_TYPE_SPECIFIC_SENSOR_NOTIFICATION_SMOKE_ALARM:
        {
            return "Smoke Alarm Notification Sensor";
        }
        break;
        case ICON_TYPE_SPECIFIC_SENSOR_NOTIFICATION_WATER_ALARM:
        {
            return "Water Alarm Notification Sensor";
        }
        break;
        case ICON_TYPE_SPECIFIC_SENSOR_NOTIFICATION_ACCESS_CONTROL:
        {
            return "Access Control Notification Sensor";
        }
        break;
        case ICON_TYPE_SPECIFIC_SENSOR_NOTIFICATION_HOME_SECURITY:
        {
            return "Home Security Notification Sensor";
        }
        break;
        case ICON_TYPE_SPECIFIC_SENSOR_NOTIFICATION_POWER_MANAGEMENT:
        {
            return "Power Management Notification Sensor";
        }
        break;
        case ICON_TYPE_SPECIFIC_SENSOR_NOTIFICATION_SYSTEM:
        {
            return "System Notification Sensor";
        }
        break;
        case ICON_TYPE_SPECIFIC_SENSOR_NOTIFICATION_CLOCK:
        {
            return "Clock Notification Sensor";
        }
        break;
        case ICON_TYPE_GENERIC_SENSOR_MULTILEVEL:
        {
            return "Sensor Multilevel";
        }
        break;
        case ICON_TYPE_SPECIFIC_SENSOR_MULTILEVEL_AIR_TEMPERATURE:
        {
            return "Air Temperature Sensor";
        }
        break;
        case ICON_TYPE_SPECIFIC_SENSOR_MULTILEVEL_LUMINANCE:
        {
            return "Luminance Sensor";
        }
        break;
        case ICON_TYPE_SPECIFIC_SENSOR_MULTILEVEL_POWER:
        {
            return "Power Sensor";
        }
        break;
        case ICON_TYPE_SPECIFIC_SENSOR_MULTILEVEL_HUMIDITY:
        {
            return "Humidity Sensor";
        }
        break;
        case ICON_TYPE_GENERIC_REPEATER:
        {
            return "Repeater";
        }
        break;

        case ICON_TYPE_UNASSIGNED:
        {
            return "unknown device";
        }
        break;
        default:
            return "unknown device";
    }
}

// Indicate whether the device security inclusion success or not
static char* hl_is_security_inclusion(uint16_t  status)
{
    switch(status)
    {
        case 0:
        {
            return "Success";
        }
        case 1:
        {
            return "Error";
        }
        case 2:
        {
            return "Normal";
        }

        default:
            return "Unknown";
    }
}

/**
hl_sup_sensor_show - Show supported sensor types and units
@param[in]  intf      Multilevel sensor interface
@return
*/
static void  hl_sup_sensor_show(zwifd_p intf, cJSON *interfaceInfo)
{
    int                 i;
    int                 j;
    uint8_t             type;
    uint8_t             sensor_cnt;
    if_sensor_data_t    *sup_sensor;
    const char          **unit_str;

    if (zwif_sensor_sup_cache_get(intf, &sup_sensor, &sensor_cnt) != 0)
    {
        return;
    }

    cJSON * sensorInfoArray =  cJSON_CreateArray();

    if(sensorInfoArray == NULL)
    {
        return;
    }

    cJSON_AddItemToObject(interfaceInfo, "Sensor Info List", sensorInfoArray);

    for (i=0; i<sensor_cnt; i++)
    {
        cJSON *sensorInfo = cJSON_CreateObject();

        if(sensorInfo == NULL)
        {
            return;
        }

        cJSON_AddItemToArray(sensorInfoArray, sensorInfo);

        type = sup_sensor[i].sensor_type;

        if (type > ZW_SENSOR_TYPE_TGT_TEMP)
        {
            type = 0;
        }

        ALOGI("                        Supported sensor type:%s, sensor units:", sensor_type_str[type]);

        cJSON_AddStringToObject(sensorInfo, "sensor type", sensor_type_str[type]);

        cJSON * unitArray =  cJSON_CreateArray();

        if(unitArray == NULL)
        {
            return;
        }

        cJSON_AddItemToObject(sensorInfo, "Unit List", unitArray);

        for (j=0; j<4; j++)
        {
            if (sup_sensor[i].sensor_unit & (0x01 << j))
            {
                unit_str = sensor_unit_str + (type * 4) + j;

                if (*unit_str == NULL)
                {
                    *unit_str = "undefined";
                }

                ALOGI("                                                         %s", *unit_str);

                cJSON_AddStringToObject(unitArray, "unit", *unit_str);
            }
        }
    }
}

static void hl_grp_info_show(zwifd_p intf, cJSON *interfaceInfo)
{
    int                 j;
    int                 i;
    int                 result;
    if_grp_info_dat_t   *grp_info;
    void                *plt_ctx;
    zw_grp_info_p       grp_info_ent;
    char str[50] = {0};

    result = zwif_group_info_get(intf, &grp_info);

    plt_ctx = hl_if_plt_ctx_get(intf);

    if (result == 0)
    {
        ALOGI("                        Group info type:%s", (grp_info->dynamic)? "dynamic" : "static");
        ALOGI("                        Maximum supported groups:%u", grp_info->group_cnt);
        ALOGI("                        Valid groups:%u", grp_info->valid_grp_cnt);

        cJSON_AddStringToObject(interfaceInfo, "Group info type", (grp_info->dynamic)? "dynamic" : "static");
        cJSON_AddNumberToObject(interfaceInfo, "Maximum supported groups", grp_info->group_cnt);
        cJSON_AddNumberToObject(interfaceInfo, "Valid groups", grp_info->valid_grp_cnt);

        cJSON * grpInfoArray =  cJSON_CreateArray();

        if(grpInfoArray == NULL)
        {
            return;
        }

        cJSON_AddItemToObject(interfaceInfo, "Group Info List", grpInfoArray);

        for (i=0; i<grp_info->valid_grp_cnt; i++)
        {
            grp_info_ent = grp_info->grp_info[i];

            if (grp_info_ent)
            {
                cJSON *grpInfo = cJSON_CreateObject();

                if(grpInfo == NULL)
                {
                    return;
                }

                cJSON_AddItemToArray(grpInfoArray, grpInfo);

                ALOGI("                        --------------------------------------------");
                ALOGI("                        Group id:%u, profile:%04xh, event code:%04xh,",
                             grp_info_ent->grp_num, grp_info_ent->profile, grp_info_ent->evt_code);
                ALOGI("                        name:%s, command list:",
                             grp_info_ent->name);

                cJSON_AddNumberToObject(grpInfo, "Group id", grp_info_ent->grp_num);

                sprintf(str, "%04xh", grp_info_ent->profile);
                cJSON_AddStringToObject(grpInfo, "profile", str);

                sprintf(str, "%04xh", grp_info_ent->evt_code);
                cJSON_AddStringToObject(grpInfo, "event code", str);

                cJSON * cmdInfoArray =  cJSON_CreateArray();

                if(cmdInfoArray == NULL)
                {
                    return;
                }

                cJSON_AddItemToObject(grpInfo, "Cmd Info List", cmdInfoArray);

                for (j=0; j<grp_info_ent->cmd_ent_cnt; j++)
                {
                    cJSON *cmdInfo = cJSON_CreateObject();

                    if(cmdInfo == NULL)
                    {
                        return;
                    }

                    cJSON_AddItemToArray(cmdInfoArray, cmdInfo);

                    ALOGI("                        command class:%04xh(%s), command:%02xh",
                                 grp_info_ent->cmd_lst[j].cls,
                                 hl_class_str_get(grp_info_ent->cmd_lst[j].cls, 1),
                                 grp_info_ent->cmd_lst[j].cmd);

                    cJSON_AddStringToObject(cmdInfo, "command class", hl_class_str_get(grp_info_ent->cmd_lst[j].cls, 1));
                    sprintf(str, "%02xh", grp_info_ent->cmd_lst[j].cmd);
                    cJSON_AddStringToObject(cmdInfo, "command", str);
                }
            }
        }

        //Free group info
        zwif_group_info_free(grp_info);
    }
}

/**
hl_zwaveplus_show - Show Z-Wave+ information
@param[in]  hl_appl   The high-level api context
@param[in]  info      Z-wave+ information
@return
*/
static void hl_zwaveplus_show(hl_appl_ctx_t *hl_appl, zwplus_info_t *info, cJSON *EpInfo)
{
    int         idx;
    const char *zwplus_node_type_str[] =
            {
                    "Z-Wave+ node",
                    "Z-Wave+ for IP router",
                    "Z-Wave+ for IP gateway",
                    "Z-Wave+ for IP - client IP node",
                    "Z-Wave+ for IP - client Z-Wave node",
                    "unknown"
            };

    const char *zwplus_role_type_str[] =
            {
                    "Central Static Controller",
                    "Sub Static Controller",
                    "Portable Controller",
                    "Portable Reporting Controller",
                    "Portable Slave",
                    "Always On Slave",
                    "Sleeping Reporting Slave",
                    "Reachable_Sleeping_Slave",
                    "unknown"
            };

    ALOGI("ZWave+ version:%u", (unsigned)(info->zwplus_ver));
    cJSON_AddNumberToObject(EpInfo, "ZWave+ version", info->zwplus_ver);

    idx = (info->node_type <= 4)? info->node_type : 5;
    ALOGI("ZWave+ node type:%s", zwplus_node_type_str[idx]);
    cJSON_AddStringToObject(EpInfo, "ZWave+ node type", zwplus_node_type_str[idx]);

    idx = (info->role_type <= 7)? info->role_type : 8;
    ALOGI("ZWave+ role type:%s", zwplus_role_type_str[idx]);
    cJSON_AddStringToObject(EpInfo, "ZWave+ role type", zwplus_role_type_str[idx]);

    char str[50] = {0};

    ALOGI("ZWave+ installer icon:%04Xh", (unsigned)(info->instr_icon));
    sprintf(str, "%04Xh", info->instr_icon);
    cJSON_AddStringToObject(EpInfo, "ZWave+ installer icon", str);

    ALOGI("ZWave+ user icon:%04Xh", (unsigned)(info->usr_icon));
    //sprintf(str, "%04Xh", info->usr_icon);
    cJSON_AddStringToObject(EpInfo, "ZWave+ device type", hl_zwaveplus_icon_to_device_type(info->usr_icon));
}

/**
hl_int_get - get integer value from a byte stream
@param[in]  byte_buf    The buffer that holds the bytes
@param[in]  size        The integer size
@param[out] int_val     The result of the conversion from bytes to integer
@return     1 on success; else return 0
*/
int  hl_int_get(uint8_t *byte_buf,  uint8_t size, int32_t *int_val)
{
    if (size == 1)
    {
        int8_t  val8;

        val8 = byte_buf[0];
        *int_val = val8;
        return 1;
    }
    else if (size == 2)
    {
        int16_t val16;

        val16 = byte_buf[0];
        val16 = (val16 << 8) | byte_buf[1];
        *int_val = val16;
        return 1;
    }
    else
    {
        int32_t val32;

        val32 = ((int32_t)(byte_buf[0])) << 24
                | ((int32_t)(byte_buf[1])) << 16
                | ((int32_t)(byte_buf[2])) << 8
                | byte_buf[3];

        *int_val = val32;
        return 1;
    }
    return 0;
}

/**
hl_float_get - get floating point integer string from an integer with specified precision
@param[in]  int_value    Integer value
@param[in]  precision    Number of decimal points
@param[in]  buf_size     Output buffer size
@param[out] out_buf      Output buffer that contains the converted string.
@return
*/
void  hl_float_get(int32_t int_value,  uint8_t precision, uint8_t buf_size, char *out_buf)
{
    int i;
    int32_t   divisor;
    int32_t   whole_num;
    int32_t   dec_num;
    char      format_str[20];

    divisor = 1;
    for (i=0 ; i < precision; i++)
    {
        divisor *= 10;
    }

    whole_num = int_value / divisor;

    if (int_value < 0)
    {
        dec_num = (int_value * (-1)) % divisor;
    }
    else
    {
        dec_num = int_value % divisor;
    }

    if (precision > 0)
    {
        sprintf(format_str, "%%d.%%.%ud", precision);
        sprintf(out_buf, format_str, whole_num, dec_num);
    }
    else
    {
        sprintf(out_buf, "%d", int_value);
    }
}

static void hl_ml_snsr_rep_cb_1(zwifd_p ifd, zwsensor_t *value)
{
    int32_t         snsr_value;
    const char      **unit_str;

    if (value->type > ZW_SENSOR_TYPE_TGT_TEMP)
    {
        value->type = 0;
    }

    unit_str = sensor_unit_str + (value->type * 4) + value->unit;

    if (*unit_str == NULL)
    {
        *unit_str = "undefined";
    }

    ALOGD("Multi-level sensor report, type:%s, precision:%u, unit:%s",
                    sensor_type_str[value->type], value->precision, *unit_str);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Sensor Info Report");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    cJSON_AddStringToObject(jsonRoot, "type", sensor_type_str[value->type]);
    cJSON_AddNumberToObject(jsonRoot, "precision", value->precision);
    cJSON_AddStringToObject(jsonRoot, "unit", *unit_str);

    if (!hl_int_get(value->data, value->size, &snsr_value))
    {   //Error, default to zero
        snsr_value = 0;
    }

    char float_str[80] = {0};

    if (value->precision == 0)
    {
        ALOGD("Sensor reading:%d", snsr_value);
        cJSON_AddNumberToObject(jsonRoot, "value", snsr_value);
    }
    else
    {
        hl_float_get(snsr_value, value->precision, 80, float_str);
        ALOGD("Sensor reading:%s", float_str);
        cJSON_AddStringToObject(jsonRoot, "value", float_str);
    }

    if(resCallBack)
    {
        char *p = cJSON_Print(jsonRoot);

        if(p)
        {
            resCallBack(p);
            free(p);
        }
    }

    cJSON_Delete(jsonRoot);
}


/**
hl_node_desc_dump - dump the node descriptor info
@param[in]  hl_appl     The high-level api context
@return
*/
static int hl_node_desc_dump(hl_appl_ctx_t *hl_appl, cJSON *jsonRoot)
{
    int         result;
    zwnetd_p    net_desc;
    zwnoded_p   node;
    zwepd_p     ep;
    zwifd_p     intf;
    desc_cont_t *last_node_cont;
    desc_cont_t *last_ep_cont;
    desc_cont_t *last_intf_cont;
    char str[100] = {0};

    if(jsonRoot == NULL)
    {
        return -1;
    }

    plt_mtx_lck(hl_appl->desc_cont_mtx);

    //Check whether the descriptor container linked list is initialized
    if (!hl_appl->desc_cont_hd)
    {
        result = hl_desc_init(&hl_appl->desc_cont_hd, hl_appl->zwnet);
        if (result != 0)
        {
            ALOGI("hl_desc_init with error:%d", result);
            return result;
        }
    }

    cJSON * NodeInfoArray = cJSON_CreateArray();

    if(NodeInfoArray == NULL)
    {
        return -1;
    }

    cJSON_AddItemToObject(jsonRoot, "Node Info List", NodeInfoArray);

    //Get the first node (local controller) and home id
    last_node_cont = hl_appl->desc_cont_hd;

    net_desc = zwnet_get_desc(hl_appl->zwnet);

    while (last_node_cont)
    {
        cJSON *NodeInfo = cJSON_CreateObject();

        if(NodeInfo == NULL)
        {
            return -1;
        }

        cJSON_AddItemToArray(NodeInfoArray, NodeInfo);

        if (last_node_cont->type != DESC_TYPE_NODE)
        {
            ALOGI("node: wrong desc type:%u", last_node_cont->type);
        }

        node = (zwnoded_p)last_node_cont->desc;

        ALOGI("__________________________________________________________________________");
        ALOGI("Node id:%u[%u], Home id:%08X", (unsigned)node->nodeid,
                     last_node_cont->id, (unsigned)net_desc->id);
        //plt_msg_show(hl_plt_ctx_get(hl_appl), "Node status:%s", (node->alive)?  "alive" : "down/sleeping");

        sprintf(str, "%08X", net_desc->id);
        cJSON_AddStringToObject(NodeInfo, "Home id", str);
        cJSON_AddNumberToObject(NodeInfo, "Node id", node->nodeid);
        //cJSON_AddStringToObject(NodeInfo, "Node status", (node->alive)?  "alive" : "down/sleeping");

        if (node->sleep_cap)
        {
            ALOGI("Node is capable to sleep with wakeup interval:%us", node->wkup_intv);

            sprintf(str, "%us", node->wkup_intv);
            cJSON_AddStringToObject(NodeInfo, "wakeup interval", str);
        }

        if (node->sensor)
        {
            ALOGI("Node is FLIRS");
        }

        //plt_msg_show(hl_plt_ctx_get(hl_appl), "Node security inclusion status:%s", hl_is_security_inclusion(node->sec_incl_failed));
        ALOGI("Vendor id:%04X", node->vid);
        ALOGI("Product type id:%04X", node->type);
        ALOGI("Product id:%04X", node->pid);
        /*plt_msg_show(hl_plt_ctx_get(hl_appl), "Category:%s", (node->category <= DEV_WALL_CTLR)?
                                                             dev_category_str[node->category] : "unknown");*/
        ALOGI("Z-wave library type:%u", node->lib_type);
        ALOGI("Z-wave protocol version:%u.%02u\n", (unsigned)(node->proto_ver >> 8),
                     (unsigned)(node->proto_ver & 0xFF));
        ALOGI("Application version:%u.%02u\n", (unsigned)(node->app_ver >> 8),
                     (unsigned)(node->app_ver & 0xFF));

        //cJSON_AddStringToObject(NodeInfo, "Node security inclusion status", hl_is_security_inclusion(node->sec_incl_failed));

        sprintf(str, "%04X", node->vid);
        cJSON_AddStringToObject(NodeInfo, "Vendor id", str);

        sprintf(str, "%04X", node->type);
        cJSON_AddStringToObject(NodeInfo, "Product type id", str);

        sprintf(str, "%04X", node->pid);
        cJSON_AddStringToObject(NodeInfo, "Product id", str);

        /*cJSON_AddStringToObject(NodeInfo, "Category", (node->category <= DEV_WALL_CTLR)?
                                                  dev_category_str[node->category] : "unknown");*/
        cJSON_AddNumberToObject(NodeInfo, "Z-wave library type", node->lib_type);

        sprintf(str, "%u.%02u", node->proto_ver >> 8, node->proto_ver & 0xFF);
        cJSON_AddStringToObject(NodeInfo, "Z-wave protocol version", str);

        sprintf(str, "%u.%02u", node->app_ver >> 8, node->app_ver & 0xFF);
        cJSON_AddStringToObject(NodeInfo, "Application version", str);

        hl_ext_ver_show(hl_appl, node, NodeInfo);

        if (node->dev_id.len > 0)
        {
            hl_dev_id_show(hl_appl, &node->dev_id);
        }

        cJSON * EpInfoArray =  cJSON_CreateArray();

        if(EpInfoArray == NULL)
        {
            return -1;
        }

        cJSON_AddItemToObject(NodeInfo, "EndPoint List", EpInfoArray);

        //Get endpoint
        last_ep_cont = last_node_cont->down;

        while (last_ep_cont)
        {
            cJSON *EpInfo = cJSON_CreateObject();

            if(EpInfo == NULL)
            {
                return -1;
            }

            cJSON_AddItemToArray(EpInfoArray, EpInfo);

            if (last_ep_cont->type != DESC_TYPE_EP)
            {
                ALOGI("ep: wrong desc type:%u", last_ep_cont->type);
            }

            ep = (zwepd_p)last_ep_cont->desc;

            ALOGI("Endpoint id:%u[%u]", ep->epid, last_ep_cont->id);
            ALOGI("Device class: generic:%02X, specific:%02X",
                         ep->generic, ep->specific);
            //ALOGI("Endpoint name:%s", ep->name);
            //ALOGI("Endpoint location:%s", ep->loc);

            cJSON_AddNumberToObject(EpInfo, "Endpoint id", ep->epid);
            cJSON_AddNumberToObject(EpInfo, "Endpoint interface id", last_ep_cont->id);

            sprintf(str, "%02X", ep->generic);
            cJSON_AddStringToObject(EpInfo, "Device class generic", str);

            sprintf(str, "%02X", ep->specific);
            cJSON_AddStringToObject(EpInfo, "Device class specific", str);
            //cJSON_AddStringToObject(EpInfo, "Endpoint name", ep->name);
            //cJSON_AddStringToObject(EpInfo, "Endpoint location", ep->loc);

            if (ep->zwplus_info.zwplus_ver)
            {
                hl_zwaveplus_show(hl_appl, &ep->zwplus_info, EpInfo);
            }

            cJSON * InterfaceInfoArray =  cJSON_CreateArray();

            if(InterfaceInfoArray == NULL)
            {
                return -1;
            }

            cJSON_AddItemToObject(EpInfo, "Interface List", InterfaceInfoArray);

            //Get interface
            last_intf_cont = last_ep_cont->down;

            while (last_intf_cont)
            {
                cJSON *InterfaceInfo = cJSON_CreateObject();

                if(InterfaceInfo == NULL)
                {
                    return -1;
                }

                cJSON_AddItemToArray(InterfaceInfoArray, InterfaceInfo);

                if (last_intf_cont->type != DESC_TYPE_INTF)
                {
                    ALOGI("interface: wrong desc type:%u", last_intf_cont->type);
                }

                intf = (zwifd_p)last_intf_cont->desc;

                ALOGI("              Interface: %02Xv%u:%s [%u]%c%c",
                             (unsigned)intf->cls, intf->real_ver, hl_class_str_get(intf->cls, intf->real_ver),
                             last_intf_cont->id, (intf->propty & IF_PROPTY_SECURE)? '*' : ' ',
                             (intf->propty & IF_PROPTY_UNSECURE)? '^' : ' ');

                cJSON_AddStringToObject(InterfaceInfo, "Interface Class", hl_class_str_get(intf->cls, intf->ver));
                cJSON_AddNumberToObject(InterfaceInfo, "Interface Id", last_intf_cont->id);

                if (intf->cls == COMMAND_CLASS_SENSOR_MULTILEVEL)
                {
                    // hl_sup_sensor_show(intf, InterfaceInfo);
                   // result = zwif_sensor_rpt_set(intf, hl_ml_snsr_rep_cb_1);
                }
                else if (intf->cls == COMMAND_CLASS_ASSOCIATION_GRP_INFO)
                {
                    hl_grp_info_show(intf, InterfaceInfo);
                }
                else if (intf->cls == COMMAND_CLASS_METER)
                {
                    //hl_meter_info_show(intf, InterfaceInfo);
                }
                else if (intf->cls == COMMAND_CLASS_NOTIFICATION_V4)
                {
                    //hl_notification_info_show(intf, InterfaceInfo);
                    //result = zwif_notification_rpt_set(intf, hl_notification_get_report_cb1);
                }

                //Get the next interface
                last_intf_cont = last_intf_cont->next;
            }

            //Get the next endpoint
            last_ep_cont = last_ep_cont->next;
        }

        //Get the next node
        last_node_cont = last_node_cont->next;
    }
    ALOGI("__________________________________________________________________________");

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    return 0;
}

/**
hl_node_list_dump - dump the node list info
@param[in]  hl_appl     The high-level api context
@return
*/
static int hl_node_list_dump(hl_appl_ctx_t *hl_appl, cJSON *jsonRoot)
{
    int         result;
    zwnetd_p    net_desc;
    zwnoded_p   node;
    zwepd_p     ep;
    zwifd_p     intf;
    desc_cont_t *last_node_cont;

    if(jsonRoot == NULL)
    {
        return -1;
    }

    plt_mtx_lck(hl_appl->desc_cont_mtx);

    //Check whether the descriptor container linked list is initialized
    if (!hl_appl->desc_cont_hd)
    {
        result = hl_desc_init(&hl_appl->desc_cont_hd, hl_appl->zwnet);
        if (result != 0)
        {
            ALOGI("hl_desc_init with error:%d", result);
            return result;
        }
    }

    cJSON * NodeInfoArray = cJSON_CreateArray();

    if(NodeInfoArray == NULL)
    {
        return -1;
    }

    cJSON_AddItemToObject(jsonRoot, "Added Node List", NodeInfoArray);

    //Get the first node (local controller) and home id
    last_node_cont = hl_appl->desc_cont_hd;

    net_desc = zwnet_get_desc(hl_appl->zwnet);

    while (last_node_cont)
    {
        if (last_node_cont->type != DESC_TYPE_NODE)
        {
            ALOGI("node: wrong desc type:%u", last_node_cont->type);
        }

        node = (zwnoded_p)last_node_cont->desc;

        ALOGI("__________________________________________________________________________");
        ALOGI("Node id:%u[%u], Home id:%08X", (unsigned)node->nodeid,
                     last_node_cont->id, (unsigned)net_desc->id);
        //plt_msg_show(hl_plt_ctx_get(hl_appl), "Node status:%s", (node->alive)?  "alive" : "down/sleeping");

        cJSON_AddNumberToObject(NodeInfoArray, "Node id", (unsigned)node->nodeid);

        if (node->sleep_cap)
        {
            ALOGI("Node is capable to sleep with wakeup interval:%us", node->wkup_intv);
        }

        if (node->sensor)
        {
            ALOGI("Node is FLIRS");
        }

        //plt_msg_show(hl_plt_ctx_get(hl_appl), "Node security inclusion status:%s", hl_is_security_inclusion(node->sec_incl_failed));
        ALOGI("Vendor id:%04X", node->vid);
        ALOGI("Product type id:%04X", node->type);
        ALOGI("Product id:%04X", node->pid);
       /* plt_msg_show(hl_plt_ctx_get(hl_appl), "Category:%s", (node->category <= DEV_WALL_CTLR)?
                                                             dev_category_str[node->category] : "unknown");*/
        ALOGI("Z-wave library type:%u", node->lib_type);
        ALOGI("Z-wave protocol version:%u.%02u\n", (unsigned)(node->proto_ver >> 8),
                     (unsigned)(node->proto_ver & 0xFF));
        ALOGI("Application version:%u.%02u\n", (unsigned)(node->app_ver >> 8),
                     (unsigned)(node->app_ver & 0xFF));

        //Get the next node
        last_node_cont = last_node_cont->next;
    }
    ALOGI("__________________________________________________________________________");

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    return 0;
}

int  zwcontrol_get_node_list(hl_appl_ctx_t *hl_appl)
{
    if (hl_appl->init_status == 0){
        ALOGE("Controller not open, please open it and try again");
        return -1;
    }

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return -1;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "All Node List Report");

    int res =  hl_node_list_dump(hl_appl, jsonRoot);

    if(res == 0)
    {
        char *p = cJSON_Print(jsonRoot);

        if(p != NULL)
        {
            if(resCallBack)
            {
                resCallBack(p);
            }

            free(p);
        }
    }

    cJSON_Delete(jsonRoot);

    return res;
}

int  zwcontrol_get_node_info(hl_appl_ctx_t *hl_appl)
{
    if (hl_appl->init_status == 0){
        ALOGE("Controller not open, please open it and try again");
        return -1;
    }

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return -1;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "All Node Info Report");

    int res =  hl_node_desc_dump(hl_appl, jsonRoot);

    if(res == 0)
    {
        char *p = cJSON_Print(jsonRoot);

        if(p != NULL)
        {
            if(resCallBack)
            {
                resCallBack(p);
            }

            free(p);
        }
    }

    cJSON_Delete(jsonRoot);

    return res;
}

/**
hl_deflt_set - Restore factory default
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
static int32_t hl_deflt_set(hl_appl_ctx_t   *hl_appl)
{
    int result;

    result = zwnet_reset(hl_appl->zwnet);
    if (result != 0)
    {
        ALOGE("controller default set with error:%d", result);
    }
    return result;
}

int  zwcontrol_default_set(hl_appl_ctx_t *hl_appl)
{
    if (hl_appl->init_status == 0){
        ALOGE("Controller not open, please open it and try again");
        return -1;
    }

    ALOGD("controller set default");
    if(remove(hl_appl->save_file) == 0)
    {
        ALOGD("remove NodeInfo file successfully");
    } else{
        ALOGE("remove NodeInfo file error");
    }
    if (hl_appl->init_status)
    {
        return hl_deflt_set(hl_appl);
    }

    return -1;
}

/**
hl_save - Save settings into a file
@param[in]  hl_appl     The high-level api context
@return
*/
int    hl_save(hl_appl_ctx_t   *hl_appl)
{
    int result;

    result = zwnet_save(hl_appl->zwnet, hl_appl->save_file);
    if (result != 0)
    {
        ALOGE("save NodeInfo with error:%d", result);
    }
    if(result == 0)
        ALOGD("save nodeinfo successfully");
    return result;
}

int  zwcontrol_save_nodeinfo(hl_appl_ctx_t *hl_appl, const char* filepath)
{
    ALOGD("zwcontrol_save_nodeinfo started");
    if (hl_appl->init_status)
    {
        strcpy(hl_appl->save_file,filepath);
        //hl_appl->save_file = "zwController_nodeInfo.txt";
        return hl_save(hl_appl);
    }
    return -1;
}
//Add by jay.k >>start
int  zwcontrol_stop_op(hl_appl_ctx_t *hl_appl)
{

    if(!hl_appl->init_status)
    {
        return -1;
    }

    return zwnet_abort(hl_appl->zwnet);
}

int  zwcontrol_update_node(hl_appl_ctx_t *hl_appl, uint8_t nodeId)
{
    ALOGD("zwcontrol_update_node, nodeId:%d",nodeId);
    int         result, find = 0;
    zwnoded_p   node;
    desc_cont_t *last_node_cont;

    plt_mtx_lck(hl_appl->desc_cont_mtx);

    //Check whether the descriptor container linked list is initialized
    if (!hl_appl->desc_cont_hd)
    {
        result = hl_desc_init(&hl_appl->desc_cont_hd, hl_appl->zwnet);
        if (result != 0)
        {
            plt_msg_ts_show(hl_plt_ctx_get(hl_appl), "zwcontrol_update_node with error:%d", result);
            plt_mtx_ulck(hl_appl->desc_cont_mtx);
            return -1;
        }
    }

    //Get the first node (local controller) and home id
    last_node_cont = hl_appl->desc_cont_hd;

    while (last_node_cont)
    {
        if (last_node_cont->type != DESC_TYPE_NODE)
        {
            plt_msg_ts_show(hl_plt_ctx_get(hl_appl), "node: wrong desc type:%u", last_node_cont->type);
        }

        node = (zwnoded_p)last_node_cont->desc;

        if(node->nodeid == nodeId)
        {
            hl_appl->dst_desc_id = last_node_cont->id;
            find = 1;
            break;
        }

        last_node_cont = last_node_cont->next;
    }

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    result = -1;

    if(find == 1)
    {
        result = hl_node_updt(hl_appl);
    }

    return result;
}
//Callback function for zwnet_initiate.
void cb_get_dsk_fn(void *usr_ctx, char *dsk){


}

int  zwcontrol_start_learn_mode(hl_appl_ctx_t* hl_appl)
{
    int result;

    result = zwnet_initiate(hl_appl->zwnet,cb_get_dsk_fn,hl_appl);

    if (result != 0)
    {
        plt_msg_ts_show(hl_plt_ctx_get(hl_appl), "hl_lrn_mod_set with error:%d", result);
    }

    return result;
}
//Add by jay.k <<end
