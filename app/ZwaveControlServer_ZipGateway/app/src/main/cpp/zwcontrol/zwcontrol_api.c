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

static int gw_intf_get(zwnet_p net, zwifd_t *gw_if)
{
    int         result;
    zwnoded_t   node;
    zwepd_t     ep;
    zwifd_t     intf;

    //Get first node (controller node)
    result = zwnet_get_node(net, &node);
    if (result != 0)
    {
        return result;
    }

    if (!zwnode_get_ep(&node, &ep)) //get first endpoint of the node
    {
        if (!zwep_get_if(&ep, &intf)) //get first interface of the endpoint
        {
            do
            {
                if (intf.cls == COMMAND_CLASS_ZIP_GATEWAY)
                {   //Found
                    *gw_if = intf;
                    return 0;
                }

            }while (!zwif_get_next(&intf, &intf)); //get next interface
        }
    }

    return  ZW_ERR_INTF_NOT_FOUND;
}

/**
hl_unsolicited_addr_setup - Setup unsolicited address to receive unsolicited report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
static int hl_unsolicited_addr_setup(hl_appl_ctx_t *hl_appl)
{
    int         result;
    uint8_t     local_ip[16];
    uint16_t    local_port;
    zwifd_t     gw_ifd;

    //Get local Z/IP client listening address and port
    result = zwnet_local_addr_get(hl_appl->zwnet, hl_appl->zip_gw_ip, local_ip, hl_appl->use_ipv4);

    if (result != 0)
    {
        printf("Error: couldn't get local Z/IP client listening address: %d\n", result);
        return result;
    }

    local_port = zwnet_listen_port_get(hl_appl->zwnet);

    if (hl_appl->use_ipv4)
    {   //Convert to IPv4-mapped IPv6 address
        uint8_t unsolicit_ipv4[4];

        //Save the IPv4 address
        memcpy(unsolicit_ipv4, local_ip, 4);

        //Convert the IPv4 address to IPv4-mapped IPv6 address
        memset(local_ip, 0, 10);
        local_ip[10] = 0xFF;
        local_ip[11] = 0xFF;
        memcpy(&local_ip[12], unsolicit_ipv4, 4);
    }

    result = gw_intf_get(hl_appl->zwnet, &gw_ifd);
    if (result != 0)
    {
        printf("Error: couldn't find Z/IP gateway interface: %d\n", result);
        return result;
    }

    result = zwif_gw_unsolicit_set(&gw_ifd, local_ip, local_port);

    if (result != 0)
    {
        printf("Error: couldn't set unsolicited address: %d\n", result);
    }

    return result;

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

            if (notify_msg->op == ZWNET_OP_INITIALIZE && notify_msg->sts == ZW_ERR_NONE)
            {
                hl_appl->init_status = 1;

                // Only init process will load nodeinfo
                int result = zwnode_intf_reset(&hl_appl->zwnet->ctl);
                if(result == 0)
                {
                    ALOGI("reset node intf.");
                }

                result = zwnet_load(hl_appl->zwnet,
                        (hl_appl->load_ni_file)? hl_appl->node_info_file : NULL);

                if (result != 0)
                {
                    ALOGE("Init: load node info with error:%d", result);
                } else if(result == 0){
                    ALOGI("Init: load node info successfully");
                }
            }

            if(notify_msg->op == ZWNET_OP_RESET && notify_msg->sts == ZW_ERR_NONE)
            {
                // Report controller info
                cJSON *jsonRoot;
                jsonRoot = cJSON_CreateObject();

                if(jsonRoot == NULL)
                {
                    return ;
                }

                cJSON_AddStringToObject(jsonRoot, "MessageType", "Controller(reset) Attribute");
                {
                    zwnode_p    zw_node;
                    char str[50] = {0};
                    plt_mtx_lck(hl_appl->zwnet->mtx);

                    zw_node = &hl_appl->zwnet->ctl;

                    if(zw_node)
                    {
                        sprintf(str, "%08X", (unsigned)hl_appl->zwnet->homeid);
                        cJSON_AddStringToObject(jsonRoot, "Home Id", str);
                        cJSON_AddNumberToObject(jsonRoot, "Node Id", (unsigned)zw_node->nodeid);
                        ALOGI("________________________________________________________");
                        ALOGI("Controller Reset done, attribute:");
                        ALOGI("               Home Id: %s",str);

                        sprintf(str, "%04X", zw_node->vid);
                        cJSON_AddStringToObject(jsonRoot, "Vendor Id", str);
                        ALOGI("               Vendor Id: %s",str);

                        sprintf(str, "%04X", zw_node->vtype);
                        cJSON_AddStringToObject(jsonRoot, "Vendor Product Type", str);
                        ALOGI("               Vendor Product Type: %s",str);

                        cJSON_AddNumberToObject(jsonRoot, "Z-wave Library Type", zw_node->lib_type);
                        ALOGI("               Z-wave Library Type: %d", zw_node->lib_type);

                        sprintf(str, "%04X", zw_node->pid);
                        cJSON_AddStringToObject(jsonRoot, "Product Id", str);
                        ALOGI("               Product Id: %s",str);

                        sprintf(str, "%u.%02u", (unsigned)(zw_node->proto_ver >> 8), (unsigned)(zw_node->proto_ver & 0xFF));
                        cJSON_AddStringToObject(jsonRoot, "Z-wave Protocol Version", str);
                        ALOGI("               Z-wave Protocol Version: %s",str);

                        sprintf(str, "%u.%02u", (unsigned)(zw_node->app_ver >> 8), (unsigned)(zw_node->app_ver & 0xFF));
                        cJSON_AddStringToObject(jsonRoot, "Application Version", str);
                        ALOGI("               Application Version: %s",str);
                        ALOGI("________________________________________________________");
                    }

                    plt_mtx_ulck(hl_appl->zwnet->mtx);
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

            //Rebuild the descriptor container linked-list
            plt_mtx_lck(hl_appl->desc_cont_mtx);
            hl_desc_cont_rm_all(&hl_appl->desc_cont_hd);
            result = hl_desc_init(&hl_appl->desc_cont_hd, hl_appl->zwnet);
            if (result != 0)
            {
                ALOGE("hl_desc_init with error:%d", result);
            }

            plt_mtx_ulck(hl_appl->desc_cont_mtx);

            // tiny
            /*ALOGI("Network initialized!  Setting up unsolicited address, please wait ...\n");
            if (hl_unsolicited_addr_setup(hl_appl) == 0)
            {
                ALOGI("Setting up unsolicited address success");
            }*/
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
    else if(op == ZWNET_OP_RESET)
    {
        if(sts == OP_DONE)
        {
            cJSON_AddStringToObject(jsonRoot, "MessageType", "Controller Reset Status");
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

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Controller(open) Attribute");

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
        ALOGE("controller wait network init_status timeout");
        return -1;
    }

    {
        zwnode_p    zw_node;
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
    int      i;
    uint8_t  sensor_cnt;
    uint8_t  sup_sensor[256];

    if (zwif_sensor_sup_cache_get(intf, &sensor_cnt, &sup_sensor) != 0)
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

        if (sup_sensor[i] > ZW_SENSOR_TYPE_TGT_TEMP)
        {
            sup_sensor[i] = 0;
        }

        ALOGI("                        Supported sensor type:%s", sensor_type_str[sup_sensor[i]]);

        cJSON_AddStringToObject(sensorInfo, "sensor type", sensor_type_str[sup_sensor[i]]);
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

const static char *notif_type[] = 
{
    "Unknown", "Smoke alarm", "CO alarm", "CO2 alarm", "Heat alarm", "Water alarm", "Access control",
    "Home security", "Power Management", "system", "emergency alarm", "Clock", "Appliance", "Home health"
};

const static char * access_control_evt[] = 
{
    "Unknown", "Manual lock operation", "Manual unlock operation", "RF lock operation", "RF unlock operation", "Keypad lock operation", "Keypad unlock operation",
    "Manual not fully locked operation", "RF not fully locked operation", "Auto lock locked operation", "Auto lock not fully locked operation",
    "Lock jammed", "All user codes deleted", "Single user code deleted", "New user code added", "New user code not added due to duplicate code",
    "Keypad temporary disabled", "Keypad busy", "New program code entered : unique code for lock configuration",
    "Manually enter user access code exceeds code limit", "Unlock by RF with invalid user code", "Locked by RF with invalid user code", "Window/door is open", "Window/door is closed", "Window/door handle is open",
    "Window/door handle is closed", "Barrier performing initialization process"
};

const static char * home_security_evt[] =
{
    "State idle", "Intrusion (location provided)", "Intrusion", "Tampering, product cover removed", "Tampering, invalid code",
    "Glass breakage (location provided)", "Glass breakage", "Motion detection (location provided)", "Motion detection(unknown location)",
    "Tampering, product moved", "Unknown event/state"
};

const static char *water_alarm_evt[] =
{
    "State idle", "Water leak detected (location provided)", "Water leak detected(unknown location)", "Water level dropped (location provided)",
    "Water level dropped", "Replace water filter", "Water flow alarm", "Water pressure alarm", "Water temperature alarm", "Water level alarm",
    "Sump pump active", "Sump pump failure", "Unknown event/state"
};

static void hl_notification_get_report_cb(zwifd_p ifd, zwalrm_p param, time_t ts);
static void hl_battery_report_cb(zwifd_p ifd, uint8_t value, time_t ts);

/**
hl_notification_info_show - Show device notification info
@param[in]  hl_appl   The high-level api context
@return
*/
static void hl_notification_info_show(zwifd_p intf, cJSON *interfaceInfo)
{
    int                 i, j, k;
    uint8_t             notify_cnt;
    if_alarm_data_t     *sup_notify;
    uint8_t             sup_evt[248];
    uint8_t             evt_len;

    if (zwif_notification_info_cache_get(intf, &sup_notify, &notify_cnt) != 0)
    {
        return;
    }

    cJSON * notificationInfoArray =  cJSON_CreateArray();

    if(notificationInfoArray == NULL)
    {
        return;
    }

    cJSON_AddItemToObject(interfaceInfo, "Notification Info", notificationInfoArray);

    for (i=0; i<notify_cnt; i++)
    {
        cJSON *notificationInfo = cJSON_CreateObject();

        if(notificationInfo == NULL)
        {
            return;
        }

        cJSON_AddItemToArray(notificationInfoArray, notificationInfo);

        // dump supported notification
        for (j=0; j<sup_notify->type_evt_cnt; j++)
        {
            ALOGI("                        Supported notification type: %s",notif_type[sup_notify[i].type_evt[j].ztype]);
            
            cJSON_AddStringToObject(notificationInfo, "Supported notifications", notif_type[sup_notify[i].type_evt[j].ztype]);evt_len = 0;

            for(k = 0; k < sup_notify[i].type_evt[j].evt_len * 8; k++)
            {
                if ((sup_notify[i].type_evt[j].evt[(k>>3)] >> (k & 0x07)) & 0x01)
                {
                    sup_evt[evt_len++] = k;
                }
            }

            // dump supported event for notification type
            cJSON *eventInfo = cJSON_CreateObject();

            if(eventInfo == NULL)
            {
                return;
            }
            cJSON_AddItemToObject(notificationInfo, "Supported event", eventInfo);

            for(int l=0; l< evt_len; l++)
            {
                if(sup_notify[i].type_evt[j].ztype == 0x06)
                {
                    cJSON_AddStringToObject(eventInfo, "event", access_control_evt[sup_evt[l]]);
                    ALOGI("                           Supported event:%s", access_control_evt[sup_evt[l]]);
                }
                if(sup_notify[i].type_evt[j].ztype == 0x07)
                {
                    cJSON_AddStringToObject(eventInfo, "event", home_security_evt[sup_evt[l]]);
                    ALOGI("                           Supported event:%s",home_security_evt[sup_evt[l]]);
                }
                if(sup_notify[i].type_evt[j].ztype == 0x05)
                {
                    cJSON_AddStringToObject(eventInfo, "event", water_alarm_evt[sup_evt[l]]);
                    ALOGI("                           Supported event:%s",water_alarm_evt[sup_evt[l]]);
                }
            } // dump supported event for notification type end
        }
    }
}

static const char *meter_type[] = { "unknown", "electric", "gas", "water",/*ver 2-4*/
                                    "heating", "cooling"/*ver 5*/};
static const char *meter_rate[] = { "unknown", "import(consumed)", "export(produced)"};

static const char *units[][7] = {
    {"KWh", "kVAh", "W", "Pulse Count", "V", "A", "Power factor"},
    {"Cubic meters", "Cubic feet", "unknown", "Pulse Count", "unknown", "unknown", "unknown"},
    {"Cubic meters", "Cubic feet", "US gallons", "Pulse Count", "unknown", "unknown", "unknown"}
};

static void  hl_meter_info_show(zwifd_p intf, cJSON *interfaceInfo)
{
    int                 i, j;
    uint8_t             meter_cnt;
    zwmeter_cap_t      *sup_meter;

    if (zwif_meter_info_cache_get(intf, &sup_meter, &meter_cnt) != 0)
    {
        return;
    }

    cJSON * meterInfoArray =  cJSON_CreateArray();

    if(meterInfoArray == NULL)
    {
        return;
    }

    cJSON_AddItemToObject(interfaceInfo, "Meter Info", meterInfoArray);

    for (i=0; i<meter_cnt; i++)
    {
        cJSON *meterrInfo = cJSON_CreateObject();

        if(meterrInfo == NULL)
        {
            return;
        }

        cJSON_AddItemToArray(meterInfoArray, meterrInfo);

        if (sup_meter[i].type > 0xFF)
        {
            sup_meter[i].type = 0;
        }

        ALOGI("                        Supported meter type:%s, support reset:%s, meter unit bitmask:%x", meter_type[sup_meter[i].type], (sup_meter[i].reset_cap > 0? "yes":"no"), sup_meter[i].unit_sup);
        
        cJSON_AddStringToObject(meterrInfo, "Meter type", meter_type[sup_meter[i].type]);

        for (j=0; j<8; j++)
        {
            if (sup_meter[i].unit_sup & (0x01 << j))
            {
                ALOGI("                        supported meter units:%s",units[sup_meter[i].type-1][j]);
                cJSON_AddStringToObject(meterrInfo, "Supported unit", units[sup_meter[i].type-1][j]);
            }
        }
    }
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

static void hl_ml_snsr_rep_cb_1(zwifd_p ifd, zwsensor_t *value, time_t ts)
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
                if(node->nodeid == 1)
                    break;
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
                    hl_sup_sensor_show(intf, InterfaceInfo);
                    result = zwif_sensor_rpt_set(intf, hl_ml_snsr_rep_cb_1);
                }
                else if (intf->cls == COMMAND_CLASS_ASSOCIATION_GRP_INFO)
                {
                    hl_grp_info_show(intf, InterfaceInfo);
                }
                else if (intf->cls == COMMAND_CLASS_METER)
                {
                    hl_meter_info_show(intf, InterfaceInfo);
                }
                else if (intf->cls == COMMAND_CLASS_NOTIFICATION_V4)
                {
                    hl_notification_info_show(intf, InterfaceInfo);
                    result = zwif_notification_rpt_set(intf, hl_notification_get_report_cb);
                }
                else if (intf->cls == COMMAND_CLASS_BATTERY)
                {
                    result = zwif_battery_rpt_set(intf, hl_battery_report_cb);
                }
                else if (intf->cls == COMMAND_CLASS_SENSOR_BINARY)
                {
                    //result = zwif_bsensor_rpt_set(intf, hl_bin_snsr_rep_cb);
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


static int hl_destid_get(hl_appl_ctx_t *hl_appl, int nodeId, int cmd, uint8_t endpoindId)
{
    int result, find = 0;
    zwnetd_p    net_desc;
    zwnoded_p   node;
    zwepd_p     ep;
    zwifd_p     intf;
    desc_cont_t *last_node_cont;
    desc_cont_t *last_ep_cont;
    desc_cont_t *last_intf_cont;

    plt_mtx_lck(hl_appl->desc_cont_mtx);

    //Check whether the descriptor container linked list is initialized
    if (!hl_appl->desc_cont_hd)
    {
        result = hl_desc_init(&hl_appl->desc_cont_hd, hl_appl->zwnet);
        if (result != 0)
        {
            ALOGD("hl_destid_get with error:%d", result);
            plt_mtx_ulck(hl_appl->desc_cont_mtx);
            return -1;
        }
    }

    //Get the first node (local controller) and home id
    last_node_cont = hl_appl->desc_cont_hd;

    net_desc = zwnet_get_desc(hl_appl->zwnet);

    while (last_node_cont)
    {
        if (last_node_cont->type != DESC_TYPE_NODE)
        {
            ALOGD("node: wrong desc type:%u", last_node_cont->type);
        }

        node = (zwnoded_p)last_node_cont->desc;

        if((unsigned)node->nodeid == nodeId)
        {
            ALOGD("Get Basic node found, id %d",nodeId);
            break;
        }
        else{
            //Get the next node
            last_node_cont = last_node_cont->next;
        }
    }

    if(last_node_cont == NULL)
    {
        ALOGE("The request node isn't found, please try another");
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return -1;
    }

    last_ep_cont = last_node_cont->down;

    while (last_ep_cont)
    {
        if (last_ep_cont->type != DESC_TYPE_EP)
        {
            ALOGD("ep: wrong desc type:%u", last_ep_cont->type);
        }

        ep = (zwepd_p)last_ep_cont->desc;

        //Get interface
        last_intf_cont = last_ep_cont->down;

        while (last_intf_cont)
        {
            if (last_intf_cont->type != DESC_TYPE_INTF)
            {
                ALOGD("interface: wrong desc type:%u", last_intf_cont->type);
            }

            intf = (zwifd_p)last_intf_cont->desc;

/*            ALOGD("              Interface: %02Xv%u:%s [%u]%c%c",
                  (unsigned)intf->cls, intf->ver, hl_class_str_get(intf->cls, intf->ver),
                  last_intf_cont->id, (intf->propty & IF_PROPTY_SECURE)? '*' : ' ',
                  (intf->propty & IF_PROPTY_UNSECURE)? '^' : ' ');*/

            if((intf->epid == endpoindId) && (unsigned)intf->cls == cmd)
            {
                ALOGD("required interface found, nodeid:%d, cmd intf id:%d, EndPoint id:%d(%d)",nodeId,last_intf_cont->id,intf->epid, last_ep_cont->id);
                hl_appl->dst_desc_id = last_intf_cont->id;
                hl_appl->rep_desc_id = last_intf_cont->id;
                hl_appl->temp_desc = last_intf_cont->id;
                find = 1;
                break;
            }

            //Get the next interface
            last_intf_cont = last_intf_cont->next;
        }

        //Get the next endpoint
        last_ep_cont = last_ep_cont->next;
    }

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if(find == 1)
    {
        return 0;
    }

    return -1;
}

/**
hl_desc_cont_get - get the descriptor container for the specified id
@param[in]  head        The head of the descriptor container linked-list
@param[in]  desc_id         Unique descriptor id
@return     Descriptor container if found; else return NULL
@pre        Caller must lock the desc_cont_mtx before calling this function.
*/
static desc_cont_t    *hl_desc_cont_get(desc_cont_t *head, uint32_t desc_id)
{
    desc_cont_t     *last_node_cont;
    desc_cont_t     *last_ep_cont;
    desc_cont_t     *last_cls_cont;
    desc_cont_t     *last_intf_cont;

    //Start searching from the first node
    last_node_cont = head;

    while (last_node_cont)
    {
        if (last_node_cont->id == desc_id)
        {
            return last_node_cont;
        }

        //Search endpoint
        last_ep_cont = last_node_cont->down;

        while (last_ep_cont)
        {
            if (last_ep_cont->id == desc_id)
            {
                return last_ep_cont;
            }

            //Search class
            last_cls_cont = last_ep_cont->down;

            while (last_cls_cont)
            {
                if (last_cls_cont->id == desc_id)
                {
                    return last_cls_cont;
                }

                //Search interface
                last_intf_cont = last_cls_cont->down;

                while (last_intf_cont)
                {
                    if (last_intf_cont->id == desc_id)
                    {
                        return last_intf_cont;
                    }
                    //Get the next interface
                    last_intf_cont = last_intf_cont->next;
                }
                //Get the next class
                last_cls_cont = last_cls_cont->next;
            }
            //Get the next endpoint
            last_ep_cont = last_ep_cont->next;
        }
        //Get the next node
        last_node_cont = last_node_cont->next;
    }
    return NULL;
}

/**
hl_intf_desc_get - get interface descriptor from descriptor container
@param[in]  head        The head of the descriptor container linked-list
@param[in]  desc_id     Unique descriptor id
@return     Interface descriptor if found; else return NULL
@pre        Caller must lock the desc_cont_mtx before calling this function.
*/
zwifd_p    hl_intf_desc_get(desc_cont_t *head, uint32_t desc_id)
{
    desc_cont_t *desc_cont;

    //Get the interface descriptor
    desc_cont = hl_desc_cont_get(head, desc_id);
    if (!desc_cont)
    {
        ALOGD("hl_intf_desc_get invalid desc id:%u\n",desc_id);
        //plt_msg_ts_show("hl_intf_desc_get invalid desc id:%u", desc_id);
        return NULL;
    }

    if (desc_cont->type != DESC_TYPE_INTF)
    {
        ALOGD("hl_intf_desc_get desc id:%u is not type interface\n", desc_id);
        //plt_msg_ts_show("hl_intf_desc_get desc id:%u is not type interface", desc_id);
        return NULL;
    }

    return(zwifd_p)desc_cont->desc;
}

/*
 **  Command Class Battery
 */
static void hl_battery_report_cb(zwifd_p ifd, uint8_t value, time_t ts)
{
    if (/*(value >= 0) &&*/ (value <= 100))
    {
        ALOGD("Battery level is %u%%", value);
    }
    else if (value == 0xFF)
    {
        ALOGD("Battery low warning!");
    }

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Node Battery Value");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    cJSON_AddNumberToObject(jsonRoot, "EndPoint Id", ifd->epid);
    cJSON_AddNumberToObject(jsonRoot, "Battery Value", value);

    if(resCallBack)
    {
        char *p = cJSON_Print(jsonRoot);

        if(p != NULL)
        {
            resCallBack(p);
            free(p);
        }
    }

    cJSON_Delete(jsonRoot);
}


int32_t hl_battery_rep_set_get(hl_appl_ctx_t   *hl_appl)
{
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->rep_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_battery_rpt_set(ifd, hl_battery_report_cb);

    if (result == 0)
    {
        result = zwif_battery_get(ifd, ZWIF_GET_BMSK_LIVE);
    }

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0 && result != 1)
    {
        ALOGD("hl_battery_rep_setup with error:%d", result);
    }

    return result;
}

int  zwcontrol_battery_get(hl_appl_ctx_t *hl_appl, uint32_t nodeId)
{
    if(!hl_appl->init_status)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_BATTERY, 0))
    {
        return -1;
    }

    int result = hl_battery_rep_set_get(hl_appl);

    if(result == 1)
    {
        ALOGI("zwcontrol_battery_get command queued");
    }

    return result;
}


/*
 **  Command Class Baisc ver 1~2
 */

void hl_basic_report_cb(zwifd_p ifd, zwbasic_p val, time_t ts)
{
     ALOGI("Basic command value is %02X", val->curr_val);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Basic Information");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);

    char str[50] = {0};
    sprintf(str, "%02X", val->curr_val);
    cJSON_AddStringToObject(jsonRoot, "value", str);
    
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
hl_basic_report_v2_cb - basic command report version 2 callback
*/
void hl_basic_report_v2_cb(zwifd_p ifd, zwbasic_p val, time_t ts)
{
    ALOGI("Basic command version 2 report, current value is %02Xh, target value is %02x, duration is %02x",
          val->curr_val, val->tgt_val, val->dur);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Basic get v2 Information");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);

    char str[50] = {0};
    sprintf(str, "%02Xh", val->curr_val);
    cJSON_AddStringToObject(jsonRoot, "current value", str);

    char str1[50] = {0};
    sprintf(str1, "%02Xh", val->tgt_val);
    cJSON_AddStringToObject(jsonRoot, "target value", str1);

    char str2[50] = {0};
    sprintf(str2, "%02Xh", val->dur);
    cJSON_AddStringToObject(jsonRoot, "duration", str2);

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
hl_basic_rep_setup - Setup basic command report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_basic_rep_set_get(hl_appl_ctx_t   *hl_appl)
{
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->rep_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    if(ifd->ver == 2)
    {
        result = zwif_basic_rpt_set_v2(ifd, hl_basic_report_v2_cb);
    }
    else
    {
        result = zwif_basic_rpt_set(ifd, hl_basic_report_cb);
    }

    if(result == 0)
    {
        result = zwif_basic_get(ifd, ZWIF_GET_BMSK_LIVE);
    }

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0 && result != 1)
    {
        ALOGE("hl_basic_rep_setup with error:%d", result);
    }

    return result;
}

int  zwcontrol_basic_get(hl_appl_ctx_t *hl_appl, int nodeId)
{
    if(!hl_appl->init_status)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_BASIC, 0))
    {
        return -1;
    }

    int result = hl_basic_rep_set_get(hl_appl);
    if(result == 1)
    {
        ALOGI("zwcontrol_basic_get command queued");
    }

    return result;
}

/**
hl_basic_set - basic command set value
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_basic_set(hl_appl_ctx_t   *hl_appl)
{
    int         result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->dst_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_basic_set(ifd, (uint8_t)hl_appl->basic_val);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0 && result != 1)
    {
        ALOGI("hl_basic_set with error:%d", result);
    }

    return result;

}

int  zwcontrol_basic_set(hl_appl_ctx_t *hl_appl, int nodeId, int value)
{
    if(!hl_appl->init_status)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_BASIC, 0))
    {
        return -1;
    }

    hl_appl->basic_val = (uint16_t)value;

    int result = hl_basic_set(hl_appl);

    return result;
}


/*
 **  Command Class Configuration
 */

static void hl_cfg_report_cb(zwifd_p ifd, zwconfig_p param)
{
    int32_t         param_value;
    zwnetd_p        net_desc;
    hl_appl_ctx_t   *hl_appl;

    if (!hl_int_get(param->data, param->size, &param_value))
    {   //Error, default to zero
        param_value = 0;
    }
    ALOGI("Configuration parameter:%u, value:%x", param->param_num, param_value);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Configuration Get Information");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);

    cJSON_AddNumberToObject(jsonRoot, "Parameter number", param->param_num);
    char str[50] = {0};
    sprintf(str, "%X", param_value);
    cJSON_AddStringToObject(jsonRoot, "Parameter value", str);

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

    //Check whether to get another report
    net_desc = zwnet_get_desc(ifd->net);

    hl_appl = (hl_appl_ctx_t *)net_desc->user;

    if (hl_appl->cfg_param_mode == 1)
    {
        if (param->param_num < hl_appl->cfg_range_end)
        {
            zwif_config_get(ifd, param->param_num + 1);
        }
    }
}

/**
hl_cfg_rep_setup - Setup a configuration parameter report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_cfg_rep_setup(hl_appl_ctx_t   *hl_appl)
{
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->rep_desc_id);

    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_config_rpt_set(ifd, hl_cfg_report_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_cfg_rep_setup with error:%d", result);
    }

    return result;
}

/**
hl_cfg_get - Get configuration parameter
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_cfg_get(hl_appl_ctx_t   *hl_appl)
{
    int         result;
    zwifd_p     ifd;
    uint8_t     param_num;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->temp_desc);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    //Check whether to get single or a range of parameter value
    if (hl_appl->cfg_param_mode == 0)
    {   //Single
        param_num = hl_appl->cfg_param;
    }
    else
    {
        //Multiple
        if (hl_appl->cfg_range_start > hl_appl->cfg_range_end)
        {
            hl_appl->cfg_range_end = hl_appl->cfg_range_start;
        }
        param_num = (uint8_t)hl_appl->cfg_range_start;

    }

    result = zwif_config_get(ifd, param_num);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0 && result != 1)
    {
        ALOGE("hl_cfg_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_configuration_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t paramMode, uint8_t paramNumber,
                                 uint16_t rangeStart, uint16_t rangeEnd)
{
    if(!hl_appl->init_status)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_CONFIGURATION, 0))
    {
        return -1;
    }

    int result = hl_cfg_rep_setup(hl_appl);
    if(result != 0)
    {
        ALOGE("hl_cfg_rep_setup with error:%d",result);
        return result;
    }
    // How many parameters to get: (0) Single, (1) Range
    hl_appl->cfg_param_mode = paramMode;
    if (hl_appl->cfg_param_mode == 0)
    {
        ALOGI("configurations get, param:%d", paramNumber);
        // Parameter number
        hl_appl->cfg_param = paramNumber;
    }
    else
    {
        ALOGI("configurations get, param range %d to %d", rangeStart,rangeEnd);
        // Parameter range start
        hl_appl->cfg_range_start = rangeStart;
        // Parameter range end
        hl_appl->cfg_range_end = rangeEnd;
    }

    result = hl_cfg_get(hl_appl);
    if(result == 1)
    {
        ALOGE("zwcontrol_configuration_get command queued");
    }
    return result;
}

/**
hl_cfg_set - Set configuration parameter
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_cfg_set(hl_appl_ctx_t   *hl_appl)
{
    int         result;
    zwifd_p     ifd;
    zwconfig_t  param;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->temp_desc);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    //Check for invalid size
    switch (hl_appl->cfg_size)
    {
        case 1:
            param.data[0] = (uint8_t)(hl_appl->cfg_value & 0xFF);
            break;

        case 2:
            param.data[0] = (uint8_t)((hl_appl->cfg_value >> 8) & 0xFF);
            param.data[1] = (uint8_t)(hl_appl->cfg_value & 0xFF);
            break;

        case 4:
            param.data[0] = (uint8_t)((hl_appl->cfg_value >> 24) & 0xFF);
            param.data[1] = (uint8_t)((hl_appl->cfg_value >> 16) & 0xFF);
            param.data[2] = (uint8_t)((hl_appl->cfg_value >> 8) & 0xFF);
            param.data[3] = (uint8_t)(hl_appl->cfg_value & 0xFF);
            break;

        default:
            ALOGW("Invalid config param size:%u", hl_appl->cfg_size);
            return ZW_ERR_VALUE;
    }

    param.param_num = hl_appl->cfg_param;
    param.size = hl_appl->cfg_size;
    param.use_default = hl_appl->cfg_value_default;

    result = zwif_config_set(ifd, &param);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_cfg_set with error:%d", result);
    }

    return result;
}

int  zwcontrol_configuration_set(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t paramNumber,
                                 uint8_t paramSize, uint8_t useDefault, int32_t paramValue)
{
    if(!hl_appl->init_status)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_CONFIGURATION, 0))
    {
        return -1;
    }
    // Parameter number
    hl_appl->cfg_param =  paramNumber;
    // Parameter value size (1, 2 or 4 bytes)
    hl_appl->cfg_size = paramSize;
    // Use default value
    hl_appl->cfg_value_default = useDefault;
    if (!hl_appl->cfg_value_default)
    {
        // Parameter value
        hl_appl->cfg_value = paramValue;
    }
    else
    {
        hl_appl->cfg_value = 0;
    }
    ALOGI("configuration set, param:%d, useDefault:%d, value:%d",paramNumber,useDefault,hl_appl->cfg_value);

    int result = hl_cfg_set(hl_appl);
    if (result == 1)
    {
        ALOGE("zwcontrol_configuration_set command queued");
    }

    return result;
}


/*
 **  For Dump Command Queue
 */

static int hl_get_node_interface_id(hl_appl_ctx_t *hl_appl, uint8_t nodeId)
{
    int         result, find = 0;
    zwnetd_p    net_desc;
    zwnoded_p   node;
    desc_cont_t *last_node_cont;

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
        
        if (node->sleep_cap)
        {
            ALOGI("Node is capable to sleep with wakeup interval:%us", node->wkup_intv);
        }

        if (node->nodeid == nodeId)
        {
            ALOGI("Always alive node found, node id %d.", nodeId);
            ALOGI("Node id:%u[%u], Home id:%08X", (unsigned)node->nodeid,
                     last_node_cont->id, (unsigned)net_desc->id);
            //ALOGI("Node status:%s", (node->alive)?  "alive" : "down/sleeping");
            find = 1;
            break;
        }

        //Get the next node
        last_node_cont = last_node_cont->next;
    }

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (find)
    {
        return last_node_cont->id;
    }
    return 0;
}

int32_t hl_cmd_q_ctl_get(hl_appl_ctx_t   *hl_appl, uint8_t *q_ctl_state)
{
    int         result;
    zwnoded_p   noded;

    //Get the node descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    noded = hl_node_desc_get(hl_appl->desc_cont_hd, hl_appl->dst_desc_id);
    if (!noded)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_NODE_NOT_FOUND;
    }

    result = zwnode_cmd_q_ena_get(noded, q_ctl_state);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return ZW_ERR_MEMORY;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Command Queue State Report");
    cJSON_AddNumberToObject(jsonRoot, "Node id", noded->nodeid);

    if (result != 0)
    {
        ALOGE("hl_cmd_q_ctl_get with error:%d", result);
        cJSON_AddStringToObject(jsonRoot, "Queue state", "error");
    }
    else
    {
        ALOGI("Command queuing is:%s", (*q_ctl_state)? "on" : "off");
        cJSON_AddStringToObject(jsonRoot, "Queue state", (*q_ctl_state)? "on" : "off");
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

    return result;
}

int  zwcontrol_command_queue_state_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->init_status)
    {
        return -1;
    }

    hl_appl->dst_desc_id = hl_get_node_interface_id(hl_appl, nodeId);
    ALOGI("get command queue state for node id %d, interface id: %d", nodeId, hl_appl->dst_desc_id);

    int result = hl_cmd_q_ctl_get(hl_appl, &hl_appl->cmd_q_ctl);

    if (result != 0)
    {
        ALOGE("zwcontrol_command_queue_state_get with error:%d",result);
    }

    return result;
}

int32_t hl_cmd_q_ctl_set(hl_appl_ctx_t   *hl_appl)
{
    int         result;
    zwnoded_p   noded;

    //Get the node descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    noded = hl_node_desc_get(hl_appl->desc_cont_hd, hl_appl->temp_desc);
    if (!noded)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_NODE_NOT_FOUND;
    }

    result = zwnode_cmd_q_ena_set(noded, hl_appl->cmd_q_ctl);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("hl_cmd_q_ctl_set with error:%d", result);
    }

    return result;
}

int  zwcontrol_command_queue_turn_on_off(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t state)
{
    if(!hl_appl->init_status)
    {
        return -1;
    }

    hl_appl->temp_desc = hl_get_node_interface_id(hl_appl, nodeId);

    if (!hl_appl->temp_desc)
        return ZW_ERR_NODE_NOT_FOUND;

    hl_appl->cmd_q_ctl = state;
    ALOGI("zwcontrol_command_queue_turn_on_off, state: %s", (state? "on":"off"));
    int result = hl_cmd_q_ctl_set(hl_appl);

    return result;
}

int hl_cmd_q_view(hl_appl_ctx_t   *hl_appl)
{
    int         result;
    zwnoded_p   noded;
    uint16_t    *cmd_queue;
    uint16_t    i;

    //Get the node descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    noded = hl_node_desc_get(hl_appl->desc_cont_hd, hl_appl->dst_desc_id);
    if (!noded)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_NODE_NOT_FOUND;
    }

    result = zwnode_cmd_q_get(noded, &cmd_queue);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return ZW_ERR_MEMORY;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Command Queue Info Report");
    cJSON_AddNumberToObject(jsonRoot, "Node id", noded->nodeid);

    cJSON *commandQueue;
    commandQueue = cJSON_CreateObject();

    if(commandQueue == NULL)
    {
        return ZW_ERR_MEMORY;
    }

    cJSON_AddItemToObject(jsonRoot, "command queue", commandQueue);

    if (result > 0)
    {
        ALOGI("Command queue:");
        for (i=0; i<result; i++)
        {
            cJSON_AddNumberToObject(commandQueue,"command id", cmd_queue[i]);
            ALOGI("%u", cmd_queue[i]);
        }
        free(cmd_queue);
    }
    else
    {
        cJSON_AddStringToObject(commandQueue,"command id", "is empty");
        plt_msg_show(hl_plt_ctx_get(hl_appl), "Command queue is empty");
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

    return ZW_ERR_NONE;
}

int  zwcontrol_command_queue_view(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->init_status)
    {
        return -1;
    }

    hl_appl->dst_desc_id = hl_get_node_interface_id(hl_appl, nodeId);
    if (!hl_appl->dst_desc_id)
        return ZW_ERR_NODE_NOT_FOUND;

    int result = hl_cmd_q_view(hl_appl);

    return result;
}

int hl_cmd_q_cancel(hl_appl_ctx_t   *hl_appl)
{
    int         result;
    zwnoded_p   noded;

    //Get the node descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    noded = hl_node_desc_get(hl_appl->desc_cont_hd, hl_appl->dst_desc_id);
    if (!noded)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_NODE_NOT_FOUND;
    }

    result = zwnode_cmd_q_cancel(noded);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGI("hl_cmd_q_cancel with error:%d", result);
    }
    return result;
}

int  zwcontrol_command_queue_cancel(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->init_status)
    {
        return -1;
    }

    hl_appl->dst_desc_id = hl_get_node_interface_id(hl_appl, nodeId);
    if (!hl_appl->dst_desc_id)
        return ZW_ERR_NODE_NOT_FOUND;

    int result = hl_cmd_q_cancel(hl_appl);

    if(result == 0)
    {
        ALOGI("zwcontrol_command_queue_cancel success, node id: %d",nodeId);
    }

    return result;
}


/*
 **  Command Class Meter
 */

/**
hl_meter_rep_cb - meter report callback
@param[in]  ifd     The interface that received the report
@param[in]  data    current value and unit of the meter
@return
*/
static void hl_meter_rep_cb(zwifd_p ifd, zwmeter_dat_p value, time_t ts)
{
    int32_t meter_value;
    ALOGI("Meter type:%d, precision:%d, rate type:%d, delta_time:%d ", value->type, value->precision,value->rate_type,value->delta_time);
    ALOGI("Meter type:%s, precision:%d, unit:%s, rate type:%s",
                 meter_type[value->type], value->precision, (units[value->type-1][value->unit]),
                 meter_rate[value->rate_type]);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Meter Report Information");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    cJSON_AddStringToObject(jsonRoot, "Meter type", meter_type[value->type]);
    cJSON_AddStringToObject(jsonRoot, "Rate type", meter_rate[value->rate_type]);

    if (!hl_int_get(value->data, value->size, &meter_value))
    {   //Error, default to zero
        meter_value = 0;
    }

    if (value->precision == 0)
    {
        ALOGI("Meter reading:%d %s", meter_value, units[value->type-1][value->unit]);
        cJSON_AddNumberToObject(jsonRoot, "Meter reading", meter_value);
    }
    else
    {
        char    float_str[80];
        hl_float_get(meter_value, value->precision, 80, float_str);
        ALOGI("Meter reading:%s %s", float_str, units[value->type-1][value->unit]);
        cJSON_AddStringToObject(jsonRoot, "Meter reading", float_str);
    }

    //Check if to display previous reading, condition: delta time > 0
    if (value->delta_time > 0)
    {
        if (!hl_int_get(value->prv_data, value->size, &meter_value))
        {   //Error, default to zero
            meter_value = 0;
        }

        if (value->precision == 0)
        {
            ALOGI("Previous Meter reading:%d %s, taken %us ago", meter_value, units[value->type-1][value->unit], value->delta_time);
            cJSON_AddNumberToObject(jsonRoot, "Previous meter reading", meter_value);
            cJSON_AddNumberToObject(jsonRoot, "Taken time(sec)", value->delta_time);
        }
        else
        {
            char    float_str[80];
            hl_float_get(meter_value, value->precision, 80, float_str);
            ALOGI("Previous Meter reading:%s %s, taken %us ago", float_str, units[value->type-1][value->unit], value->delta_time);
            cJSON_AddStringToObject(jsonRoot, "Previous meter reading", float_str);
            cJSON_AddNumberToObject(jsonRoot, "Taken time(sec)", value->delta_time);
        }
    }
    cJSON_AddStringToObject(jsonRoot, "Meter unit", units[value->type-1][value->unit]);

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
hl_meter_rep_set_get - Setup meter report, and get the report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_meter_rep_set_get(hl_appl_ctx_t   *hl_appl)
{
    int         result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->rep_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_meter_rpt_set(ifd, hl_meter_rep_cb);

    if(result == 0)
    {
        // flag: ZWIF_GET_BMSK_LIVE, ZWIF_GET_BMSK_CACHE
        result = zwif_meter_get(ifd, hl_appl->meter_unit, ZWIF_GET_BMSK_LIVE);
    }

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result < 0)
    {
        ALOGE("hl_meter_rep_set_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_meter_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t meter_unit)
{
    if(!hl_appl->init_status)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_METER_V3, 0))
    {
        return -1;
    }

    //puts("Preferred unit:");
    //puts("For electric meter: (0) kWh (1) kVAh (2) W (3) Pulse count");
    //puts("For gas meter: (0) cubic meter (1) cubic feet  (3) Pulse count");
    //puts("For water meter: (0) cubic meter (1) cubic feet (2) US gallons (3) Pulse count");
    hl_appl->meter_unit = meter_unit;

    int result = hl_meter_rep_set_get(hl_appl);
    if(result == 1)
    {
        ALOGI("zwcontrol_meter_get command queued");
    }

    return result;
}

/**
hl_meter_sup_cb - report callback for meter capabilities
@param[in]  ifd         interface
@param[in]  meter_cap   meter capabilities
*/
static void hl_meter_sup_cb(zwifd_p ifd, zwmeter_cap_p meter_cap, int valid)
{
    ALOGI("        meter type:%s, meter supported unit bit-mask:%02x", meter_type[meter_cap->type], meter_cap->unit_sup);
    ALOGI("        meter %s be reset", (meter_cap->reset_cap)? "can" : "can not");

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Meter Cap Information");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    cJSON_AddStringToObject(jsonRoot, "Meter type", meter_type[meter_cap->type]);
    cJSON_AddStringToObject(jsonRoot, "Can be reset?", (meter_cap->reset_cap)? "can" : "can not");

    cJSON *sup_unit_attr;
    sup_unit_attr = cJSON_CreateObject();

    if(sup_unit_attr == NULL)
    {
        return;
    }

    cJSON_AddItemToObject(jsonRoot, "Supported unit", sup_unit_attr);

    for (int i=0; i<8; i++)
    {
        if (meter_cap->unit_sup & (0x01 << i))
        {
            cJSON_AddStringToObject(sup_unit_attr,"unit", units[meter_cap->type-1][i]);
            ALOGI("        supported meter units:%s",units[meter_cap->type-1][i]);
        }
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
hl_meter_sup - get information on the meter capabilities
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_meter_sup(hl_appl_ctx_t   *hl_appl)

{
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->dst_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    //int zwif_meter_sup_get(zwifd_p ifd,  zwrep_meter_sup_fn cb, int cache)
    result = zwif_meter_sup_get(ifd, hl_meter_sup_cb, 1);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result < 0)
    {
        ALOGE("hl_meter_sup get with error:%d", result);
    }

    return result;
}

int  zwcontrol_meter_supported_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->init_status)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_METER_V3, 0))
    {
        return -1;
    }

    int result = hl_meter_sup(hl_appl);
    if(result == 1)
    {
        ALOGE("zwcontrol_meter_supported_get command queued");
    }

    return result;
}

/**
hl_meter_reset - Reset all accumulated values stored in the meter device
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_meter_reset(hl_appl_ctx_t   *hl_appl)
{
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->dst_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_meter_reset(ifd);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result < 0)
    {
        ALOGE("hl_meter_reset with error:%d", result);
    }

    return result;
}

int  zwcontrol_meter_reset(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->init_status)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_METER_V3, 0))
    {
        return -1;
    }

    int result = hl_meter_reset(hl_appl);
    if(result != 0)
    {
        ALOGE("zwcontrol_meter_reset with error:%d",result);
    }

    return result;
}


/*
 **  Command Class Power Level
 */

const char *power_level_ind_str[] =
{
    "Normal power",
    "minus 1dBm",
    "minus 2dBm",
    "minus 3dBm",
    "minus 4dBm",
    "minus 5dBm",
    "minus 6dBm",
    "minus 7dBm",
    "minus 8dBm",
    "minus 9dBm"
};

/**
hl_power_level_report_cb - power level command report callback
@param[in]  ifd     The interface that received the report
@param[in]  bylvl   Power level indicator value
@param[in]  bytimeout   Time out value
@return
*/
void hl_power_level_report_cb(zwifd_p ifd, uint8_t bylvl, uint8_t bytimeout)
{
    ALOGI("Power level is %s", power_level_ind_str[bylvl]);
    if (bylvl != POWERLEVEL_REPORT_NORMALPOWER)
    {
        ALOGW("Time out is %u seconds.", bytimeout);
    }

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Power Level Get Information");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);

    cJSON_AddStringToObject(jsonRoot, "Power Level", power_level_ind_str[bylvl]);

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
hl_power_level_rep_set_get - Setup power level command report and get report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_power_level_rep_set_get(hl_appl_ctx_t   *hl_appl)
{
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->rep_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_power_level_rpt_set(ifd, hl_power_level_report_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if(result == 0)
    {
        result = zwif_power_level_get(ifd);
    }

    if (result != 0 && result != 1)
    {
        ALOGE("hl_power_level_rep_setup with error:%d for set up power level report",
                        result);
    }

    return result;
}

int  zwcontrol_powerLevel_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->init_status)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_POWERLEVEL, 0))
    {
        return -1;
    }

    int result = hl_power_level_rep_set_get(hl_appl);
    if(result == 1)
    {
        ALOGD("zwcontrol_powerLevel_get command queued.");
    }

    return result;
}


/*
 **  Command Class Sensor Multi-Level
 */

/**
hl_ml_snsr_rep_cb - multi-level sensor report callback
@param[in]  ifd interface
@param[in]  value       The current value and unit of the sensor.
*/
static void hl_ml_snsr_rep_cb(zwifd_p ifd, zwsensor_t *value, time_t ts)
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
hl_ml_snsr_rep_setup - Setup multi-level sensor report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_ml_snsr_rep_setup(hl_appl_ctx_t   *hl_appl)
{
    ALOGD("Setup multi-level sensor report, id %d\n", hl_appl->rep_desc_id);
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->rep_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        ALOGD("report setup, interface not found\n");
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_sensor_rpt_set(ifd, hl_ml_snsr_rep_cb);

    if(result == 0)
    {
        result = zwif_sensor_get(ifd, hl_appl->sensor_type, hl_appl->sensor_unit, ZWIF_GET_BMSK_LIVE);
    }

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0 && result != 1)
    {
        ALOGD("hl_ml_snsr_rep_setup with error:%d", result);
    }

    return result;
}

int zwcontrol_sensor_multilevel_get(hl_appl_ctx_t *hl_appl, uint32_t nodeId/*, uint8_t sensor_type, uint8_t unit*/)
{
    if(!hl_appl->init_status)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_SENSOR_MULTILEVEL, 0))
    {
        return -1;
    }

    /*hl_appl->sensor_type = sensor_type;
      hl_appl->sensor_unit = unit;*/

    int result = hl_ml_snsr_rep_setup(hl_appl);

    if(result == 1)
    {
        ALOGI("zwcontrol_sensor_multilevel_get command queued.");
    }

    return result;
}


/*
 **  Command Class Notification version 4
 **  
 */

/**
hl_notification_set - notification command set value
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_notification_set(hl_appl_ctx_t   *hl_appl, uint8_t notificationType, uint8_t status)
{
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->dst_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_notification_set(ifd, (uint8_t)notificationType, (uint8_t)status);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGW("hl_notification_set with error:%d", result);
    }

    return result;
}

int  zwcontrol_notification_set(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t notificationType, uint8_t status)
{
    if(!hl_appl->init_status)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_NOTIFICATION_V4, 0))
    {
        return -1;
    }
    int result = hl_notification_set(hl_appl, (uint8_t)notificationType, (uint8_t)status);
    if(result != 0)
    {
        ALOGW("hl_notification_set with error:%d",result);
    }
    return result;
}

/**
hl_notification_get_report_cb - notification command report callback
@param[in]  ifd     The interface that received the report
@param[in]  mode   Notification indicator value
@return
*/
static void hl_notification_get_report_cb(zwifd_p ifd, zwalrm_p param, time_t ts)
{

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    ALOGD("V1 Alarm type:%x", param->type);
    ALOGD("V1 Alarm level:%x", param->level);
    ALOGD("Unsolicited notification status:%x", param->ex_status);
    ALOGD("Notification type:%X", param->ex_type);
    ALOGD("Notification event:%X", param->ex_event);

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Notification Get Information");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);

/*    cJSON_AddNumberToObject(jsonRoot, "Alarm-type", param->type);
    cJSON_AddNumberToObject(jsonRoot, "Alarm-level", param->level);*/
    cJSON_AddNumberToObject(jsonRoot, "Notification-status", param->ex_status);
    cJSON_AddStringToObject(jsonRoot, "Notification-type", notif_type[param->ex_type]);
    if(param->ex_type == 0x06)
    {
        cJSON_AddStringToObject(jsonRoot, "Notification-event", access_control_evt[param->ex_event]);
    }
    else if (param->ex_type == 0x07)
    {
        cJSON_AddStringToObject(jsonRoot, "Notification-event", home_security_evt[param->ex_event]);
    }
    else if (param->ex_type == 0x05)
    {
        cJSON_AddStringToObject(jsonRoot, "Notification-event", water_alarm_evt[param->ex_event]);
    }
    /*cJSON_AddNumberToObject(jsonRoot, "Notification-event-length", param->ex_evt_len);
    cJSON_AddNumberToObject(jsonRoot, "Notification-event-param-type", param->ex_evt_type);*/
    //cJSON_AddNumberToObject(jsonRoot, "Notification-param", param->ex_evt_prm);

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
hl_notification_get_rep_setup - Setup notification get command report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_notification_get_rep_setup(hl_appl_ctx_t   *hl_appl)
{
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->rep_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_notification_rpt_set(ifd, hl_notification_get_report_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0)
    {
        ALOGE("zwif_notification_get_rep_setup with error:%d for set up notification get report",
                        result);
    }

    return result;
}

/**
hl_notification_rep_get - Get the notification
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_notification_rep_get(hl_appl_ctx_t   *hl_appl, uint8_t alarmType, uint8_t notificationType, uint8_t state)
{
    int     result;
    zwifd_p ifd;

    ALOGD("notification report get,hl_appl->dst_desc_id= %d",hl_appl->dst_desc_id);
    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->dst_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_notification_get(ifd, (uint8_t) alarmType, (uint8_t) notificationType, (uint8_t) state);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result == 1)
    {
        ALOGE("zwif_notification_get command queued.");
    }
    else if (result != 0)
    {
        ALOGE("zwif_notification_get with error:%d", result);
    }

    return result;
}

int  zwcontrol_notification_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t alarmType, uint8_t notificationType, uint8_t state)
{
    if(!hl_appl->init_status)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_NOTIFICATION_V4, 0))
    {
        return -1;
    }
    int result = hl_notification_get_rep_setup(hl_appl);
    if(result == 0)
    {
        ALOGW("hl_notification_set done, alarmType:%x, notificationType:%x, event:%x", alarmType, notificationType, state);
        result = hl_notification_rep_get(hl_appl, (uint8_t) alarmType, (uint8_t) notificationType, (uint8_t) state);
    }
    return result;
}

void hl_notification_sup_get_report_cb(zwifd_p ifd, uint8_t have_vtype, uint8_t ztype_len, uint8_t *ztype, int valid)
{
    ALOGI("Notification supported report, valarm type:%u, type len:%d, ", have_vtype, ztype_len);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Notification Supported Report");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    cJSON_AddNumberToObject(jsonRoot, "Have alarm type", have_vtype);

    cJSON *notification_type;
    notification_type = cJSON_CreateObject();

    if(notification_type == NULL)
    {
        return;
    }

    cJSON_AddItemToObject(jsonRoot, "supported notification",notification_type);
    for (int i = 0; i< ztype_len; i++)
    {
        ALOGI("Supported notification type is:%s", notif_type[ztype[i]]);
        cJSON_AddStringToObject(notification_type, "type", notif_type[ztype[i]]);
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
hl_notification_sup_get - Setup notification supported get
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_notification_sup_get(hl_appl_ctx_t   *hl_appl)
{
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->rep_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_notification_sup_get(ifd, hl_notification_sup_get_report_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0 && result != 1)
    {
        ALOGE("zwif_notification_sup_get with error:%d",result);
    }

    return result;
}

int  zwcontrol_notification_supported_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->init_status)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_NOTIFICATION_V4, 0))
    {
        return -1;
    }

    int result = hl_notification_sup_get(hl_appl);
    if (result == 1)
    {
        ALOGE("zwcontrol_notification_supported_get command queued");
    }

    return result;
}

void hl_notification_sup_evt_get_report_cb(zwifd_p ifd, uint8_t ztype, uint8_t evt_len, uint8_t *evt, int valid)
{
    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Supported Notification Event Report");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    cJSON_AddStringToObject(jsonRoot, "Notification Type", notif_type[ztype]);

    ALOGI("Supported notification event report, type: %s , len:%d ",notif_type[ztype], evt_len);
    if (ztype == 0x06)
    {
        for (int i =0; i< evt_len; i++){
            ALOGI("supported Access control event: %s",access_control_evt[evt[i]]);
            cJSON_AddStringToObject(jsonRoot, "event", access_control_evt[evt[i]]);
        }
    }
    else if (ztype == 0x07)
    {
        for (int i = 0; i< evt_len;i++)
        {
            ALOGI("supported home security event: %s", home_security_evt[evt[i]]);
            cJSON_AddStringToObject(jsonRoot, "event", home_security_evt[evt[i]]);
        }
    }
    else if (ztype == 0x05)
    {
        for (int i = 0; i<evt_len; i++)
        {
            ALOGI("supported water alarm event: %s", water_alarm_evt[evt[i]]);
            cJSON_AddStringToObject(jsonRoot, "event", water_alarm_evt[evt[i]]);
        }
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
hl_notification_sup_evt_get - Setup notification supported event get
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_notification_sup_evt_get(hl_appl_ctx_t   *hl_appl, uint8_t ztype)
{
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->rep_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_notification_sup_evt_get(ifd, ztype, hl_notification_sup_evt_get_report_cb);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0 && result != 1)
    {
        ALOGE("zwif_notification_sup_evt_get with error:%d",result);
    }

    return result;
}

int  zwcontrol_notification_supported_event_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t notificationType)
{
    if(!hl_appl->init_status)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_NOTIFICATION_V4, 0))
    {
        return -1;
    }

    int result = hl_notification_sup_evt_get(hl_appl, notificationType);
    if (result == 1)
    {
        ALOGE("zwcontrol_notification_supported_event_get command queued");
    }

    return result;
}


/*
 **  Command Class Central Scene version 2
 **
 */

static const char* key_attr[] =
{
    "Key Pressed 1 time", "Key Released", "Key Held Down",
    "Key Pressed 2 times", "Key Pressed 3 times", "Key Pressed 4 times",
    "Key Pressed 5 times", "Unknown"
};

static void hl_central_scene_sup_get_report_cb(zwifd_p ifd, uint8_t scene_cnt, uint8_t sameKA, uint8_t KA_array_len, uint8_t *KA_array, uint8_t slow_rfsh, int valid)
{
    ALOGI("Central scene supported report.");
    ALOGI("Supported scenes: %d, Is same KA: %s, Slow refresh:%d",scene_cnt, (sameKA? "yes":"no"),slow_rfsh);
    ALOGI("KA_array_len= %d", KA_array_len);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Central Scene Supported Report");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    cJSON_AddNumberToObject(jsonRoot, "Supported Scenes", scene_cnt);
    cJSON_AddStringToObject(jsonRoot, "Is Same Key Attributes?", sameKA? "Yes": "No");

    cJSON *sup_key_attr;
    sup_key_attr = cJSON_CreateObject();

    if(sup_key_attr == NULL)
    {
        return;
    }

    cJSON_AddItemToObject(jsonRoot, "Supported Key Attr", sup_key_attr);
    ALOGI("Valid keys: %d",KA_array[0]);
    for (int i = 1; i < KA_array_len; i++)
    {
        ALOGI("Supported Key Attributes:%s", key_attr[KA_array[i]]);
        cJSON_AddStringToObject(sup_key_attr, "key attr", key_attr[KA_array[i]]);
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

void zwcontrol_central_scene_notification_report_cb(zwifd_p ifd, zwcsc_notif_p data, time_t ts)
{
    ALOGI("Central scene notification report");
    ALOGI("sequence number: %d, key attr:%s, scene number: %d", data->seqNo, key_attr[data->keyAttr], data->sceneNo);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Central Scene Notification");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    cJSON_AddStringToObject(jsonRoot, "key attr", key_attr[data->keyAttr]);
    cJSON_AddNumberToObject(jsonRoot, "Scene number", data->sceneNo);

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
hl_central_scene_sup_get - Setup central scene supported get
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int hl_central_scene_sup_get(hl_appl_ctx_t   *hl_appl)
{
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->rep_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_csc_sup_get(ifd, hl_central_scene_sup_get_report_cb, 0);

    if (result == 0)
    {
        result = zwif_csc_rpt_set(ifd, zwcontrol_central_scene_notification_report_cb);
    }

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0 && result != 1)
    {
        ALOGE("zwif_central_scene_sup_get with error:%d",result);
    }

    return result;
}

int  zwcontrol_central_scene_supported_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t endpoindId)
{
    ALOGI("zwcontrol_central_scene_supported_get started");
    if(!hl_appl->init_status)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_CENTRAL_SCENE, endpoindId))
    {
        return -1;
    }

    int result = hl_central_scene_sup_get(hl_appl);

    if (result == 1)
    {
        ALOGE("zwcontrol_central_scene_supported_get command queued");
    }

    return result;
}


/*
 **  Command Class Switch Binary ver 1~2
 */

/**
hl_bin_set - Turn binary switch on/off
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_bin_set(hl_appl_ctx_t   *hl_appl)
{
    int         result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->temp_desc);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_switch_set(ifd, hl_appl->bin_state, NULL,NULL);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0 && result != 1)
    {
        ALOGE("zwif_switch_set with error:%d", result);
    }

    return result;

}

int zwcontrol_switch_binary_set(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t bin_state)
{
    if(!hl_appl->init_status)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_SWITCH_BINARY, 0))
    {
        return -1;
    }

    hl_appl->bin_state = bin_state;
    int result = hl_bin_set(hl_appl);
    if(result == 1)
    {
        ALOGW("zwcontrol_switch_binary_set command queued");
    }
    return result;
}

/**
hl_bin_report_cb - binary switch report callback
@param[in]  ifd The interface that received the report
@param[in]  on          0=off, else on
@return
*/
static void hl_bin_report_cb(zwifd_p ifd, zwswitch_p val, time_t ts)
{
    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Binary Switch Get Information");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    cJSON_AddNumberToObject(jsonRoot, "Cur Val", val->curr_val);
    cJSON_AddNumberToObject(jsonRoot, "Tar Val", val->tgt_val);
    cJSON_AddNumberToObject(jsonRoot, "Durration", val->dur);

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
hl_bin_rep_setup - Setup binary switch report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_bin_rep_setup(hl_appl_ctx_t   *hl_appl)
{
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->rep_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_switch_rpt_set(ifd, hl_bin_report_cb);

    if(result == 0)
    {
        result = zwif_switch_get(ifd, ZWIF_GET_BMSK_LIVE);
    }

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0 && result != 1)
    {
        ALOGE("hl_bin_rep_setup with error:%d", result);
    }

    return result;
}

int  zwcontrol_switch_binary_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->init_status)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_SWITCH_BINARY, 0))
    {
        return -1;
    }

    int result = hl_bin_rep_setup(hl_appl);

    if(result == 1)
    {
        ALOGI("zwcontrol_switch_binary_get command queued");
    }

    return result;
}


/*
 **  Command Class Sensor Binary v2
 */
 const static char *binary_sensor_type[]=
 {
    "unknown", "General purpose", "Smoke","CO",
    "CO2", "Heat", "Water", "freeze", 
    "Tamper", "Aux", "Door/Window", "Tilt",
    "Motion", "Glass break"
};

/**
hl_bin_snsr_rep_cb - binary sensor report callback
@param[in]  ifd The interface that received the report
@param[in]  state       The state of the sensor: 0=idle, else event detected
@return
*/
static void hl_bin_snsr_rep_cb(zwifd_p ifd, uint8_t value, uint8_t type, time_t ts)
{
    ALOGI("Binary sensor value :%s, sensor type :%d ", (value == 0)? "idle" : "event detected",type);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Binary Sensor Information");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    cJSON_AddStringToObject(jsonRoot, "Event Type", binary_sensor_type[type]);
    if(value == 0)
    {
        cJSON_AddStringToObject(jsonRoot, "state", "idle");  
    } 
    else
        cJSON_AddStringToObject(jsonRoot, "state", "event detected");

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
hl_bin_snsr_rep_setup - Setup binary sensor report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_bin_snsr_rep_setup(hl_appl_ctx_t   *hl_appl, uint8_t sensor_type)
{
    int         result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->rep_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_bsensor_rpt_set(ifd, hl_bin_snsr_rep_cb);

    if(result == 0)
    {
        result = zwif_bsensor_get(ifd, (uint8_t)sensor_type, ZWIF_GET_BMSK_LIVE);
    }

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result != 0 && result != 1)
    {
        ALOGE("hl_bin_snsr_rep_setup with error:%d", result);
    }

    return result;
}

int  zwcontrol_sensor_binary_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t sensor_type)
{
    if(!hl_appl->init_status)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_SENSOR_BINARY_V2, 0))
    {
        return -1;
    }

    int result = hl_bin_snsr_rep_setup(hl_appl, sensor_type);
    if(result == 0)
    {
        ALOGI("zwcontrol_sensor_binary_get command queued");
    }

    return result;
}

/**
hl_bin_snsr_sup_rep_cb - binary sensor support report callback
@param[in]  ifd The interface that received the report
@param[in]  
@return
*/
static void hl_bin_snsr_sup_rep_cb(zwifd_p ifd, uint8_t type_len, uint8_t *type, int valid)
{
    ALOGI("Binary sensor supported number: %d",type_len);        

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Binary Sensor Support Get Information");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);
    for(int i = 0; i < type_len; i++)
    {
        ALOGI("Supported binary sensor type is :%s",binary_sensor_type[type[i]]);
        cJSON_AddStringToObject(jsonRoot, "Supported type", binary_sensor_type[type[i]]);
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
hl_bin_snsr_sup_rep_setup - Setup binary sensor support report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_bin_snsr_sup_rep_setup(hl_appl_ctx_t   *hl_appl)
{
    int result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->rep_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_bsensor_sup_get(ifd, hl_bin_snsr_sup_rep_cb, 0);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result < 0)
    {
        ALOGE("hl_bin_snsr_rep_setup with error:%d", result);
    }

    return result;
}

int  zwcontrol_sensor_binary_supported_sensor_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId)
{
    if(!hl_appl->init_status)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_SENSOR_BINARY_V2, 0))
    {
        return -1;
    }

    int result = hl_bin_snsr_sup_rep_setup(hl_appl);
    if(result == 1)
    {
        ALOGI("zwcontrol_sensor_binary_support_sensor_get command queued");
    }

    return result;
}


/*
 **  Command Class Switch Multi-Level
 */

/**
hl_sw_mul_lvl_report_cb - switch multi level report callback
@param[in]  ifd The interface that received the report
@param[in]  level       The reported level
@return
*/
static void hl_sw_mul_lvl_report_cb(zwifd_p ifd, zwlevel_dat_p val, time_t ts)
{
    ALOGI("Switch Multi-level report, current value:%02X", val->curr_val);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Switch Multi-lvl Report Information");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);

    cJSON_AddNumberToObject(jsonRoot, "Cur Val", val->curr_val);
    cJSON_AddNumberToObject(jsonRoot, "Tgt Val", val->tgt_val);
    cJSON_AddNumberToObject(jsonRoot, "Durration", val->dur);

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
hl_multi_lvl_rep_setup - Setup multi-level switch report
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_multi_lvl_rep_setup(hl_appl_ctx_t   *hl_appl)
{
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->rep_desc_id);

    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_level_rpt_set(ifd, hl_sw_mul_lvl_report_cb);

    if(result == 0)
    {
        result = zwif_level_get(ifd, ZWIF_GET_BMSK_LIVE);
    }

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result < 0)
    {
        ALOGE("hl_multi_lvl_rep_setup with error:%d", result);
    }

    return result;
}

int zwcontrol_switch_multilevel_get(hl_appl_ctx_t* hl_appl, int nodeId)
{
    if(!hl_appl->init_status)
    {
        return -1;
    }

    ALOGI("zwcontroller_switch_multilevel_get started");
    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_SWITCH_MULTILEVEL, 0))
    {
        return -1;
    }

    int result = hl_multi_lvl_rep_setup(hl_appl);
    if(result == 1)
    {
        ALOGD("zwcontrol_switch_multilevel_get command queued");
    }

    return result;
}

/**
hl_multi_lvl_sup_cb - multi level switch type callback
@param[in]  ifd interface
@param[in]  pri_type    primary switch type, SW_TYPE_XX
@param[in]  sec_type    secondary switch type , SW_TYPE_XX.
@return
*/
static void hl_multi_lvl_sup_cb(zwifd_p ifd,  uint8_t pri_type, uint8_t sec_type, int valid)
{
    ALOGI("Primary switch type:%u", pri_type);
    ALOGI("Secondary switch type:%u", sec_type);

    cJSON *jsonRoot;
    jsonRoot = cJSON_CreateObject();

    if(jsonRoot == NULL)
    {
        return;
    }

    cJSON_AddStringToObject(jsonRoot, "MessageType", "Multi Level Switch Type Information");
    cJSON_AddNumberToObject(jsonRoot, "Node id", ifd->nodeid);

    cJSON_AddNumberToObject(jsonRoot, "Primary Switch Type", pri_type);
    cJSON_AddNumberToObject(jsonRoot, "Secondary Switch Type", sec_type);

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
hl_multi_lvl_sup - Get switch type
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_multi_lvl_sup(hl_appl_ctx_t   *hl_appl)
{
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->rep_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_level_sup_get(ifd, hl_multi_lvl_sup_cb, 0);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result < 0)
    {
        ALOGE("zwif_level_sup_get with error:%d", result);
    }

    return result;
}

int zwcontrol_get_support_switch_type(hl_appl_ctx_t* hl_appl, int nodeId)
{
    if(!hl_appl->init_status)
    {
        return -1;
    }

    ALOGI("zwcontroller_get_support_switch started");
    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_SWITCH_MULTILEVEL, 0))
    {
        return -1;
    }

    int result = hl_multi_lvl_sup(hl_appl);
    if(result == 1)
    {
        ALOGD("zwcontrol_get_support_switch_type command queued");
    }
    return result;
}

/**
hl_multi_lvl_set - Set multi-level switch level
@param[in]  hl_appl     The high-level api context
@return  0 on success, negative error number on failure
*/
int32_t hl_multi_lvl_set(hl_appl_ctx_t   *hl_appl)
{
    int         result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->dst_desc_id);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        return ZW_ERR_INTF_NOT_FOUND;
    }

    result = zwif_level_set(ifd, (uint8_t)hl_appl->mul_lvl_val, hl_appl->mul_lvl_dur, NULL, NULL);

    plt_mtx_ulck(hl_appl->desc_cont_mtx);

    if (result < 0)
    {
        ALOGE("hl_multi_lvl_set with error:%d", result);
    }

    return result;

}

int zwcontrol_switch_multilevel_set(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint16_t levelValue, uint8_t duration)
{
    if(!hl_appl->init_status)
    {
        return -1;
    }

    ALOGI("zwcontrol_switch_level_set started");
    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_SWITCH_MULTILEVEL, 0))
    {
        return -1;
    }
    hl_appl->mul_lvl_val = levelValue;
    hl_appl->mul_lvl_dur = duration;

    int result = hl_multi_lvl_set(hl_appl);
    if(result == 1)
    {
        ALOGE("zwcontrol_switch_level_set command queued");
    }

    return result;
}

/**
hl_multi_lvl_chg - toggle between start and stop level change
@param[in]  hl_appl     The high-level api context
@return
*/
void    hl_multi_lvl_chg(hl_appl_ctx_t   *hl_appl)
{
    int     result;
    zwifd_p ifd;

    //Get the interface descriptor
    plt_mtx_lck(hl_appl->desc_cont_mtx);
    ifd = hl_intf_desc_get(hl_appl->desc_cont_hd, hl_appl->temp_desc);
    if (!ifd)
    {
        plt_mtx_ulck(hl_appl->desc_cont_mtx);
        ALOGE("hl_multi_lvl_chg: interface descriptor:%u not found", hl_appl->temp_desc);
        return ;
    }

    if (hl_appl->mul_lvl_change_started == 0)
    {
        zwlevel_t lvl_ctl;

        lvl_ctl.dur = hl_appl->mul_lvl_dur;
        lvl_ctl.pri_dir = hl_appl->mul_lvl_dir;
        lvl_ctl.pri_level = (uint8_t)hl_appl->mul_lvl_val;
        lvl_ctl.pri_ignore_lvl = (uint8_t)((hl_appl->mul_lvl_val == 0xFF)? 1:0);
        lvl_ctl.sec_dir = hl_appl->mul_lvl_sec_dir;
        lvl_ctl.sec_step = hl_appl->mul_lvl_sec_step;

        //Change state to start level change
        result = zwif_level_start(ifd, &lvl_ctl);

        plt_mtx_ulck(hl_appl->desc_cont_mtx);

        if (result != 0)
        {
            ALOGE("zwif_level_start with error:%d", result);
            return;
        }

        ALOGI("Start level change ...");
        hl_appl->mul_lvl_change_started = 1;
    }
    else
    {
        //Change state to start level change
        result = zwif_level_stop(ifd);

        plt_mtx_ulck(hl_appl->desc_cont_mtx);

        if (result != 0)
        {
            ALOGE("zwif_level_stop with error:%d", result);
            return;
        }

        ALOGI("Stop level change ...");
        hl_appl->mul_lvl_change_started = 0;
    }
}

int zwcontrol_start_stop_switchlevel_change(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint16_t startLvlVal, 
                                            uint8_t duration, uint8_t pmyChangeDir, uint8_t secChangeDir, uint8_t secStep)
{
    if(!hl_appl->init_status)
    {
        return -1;
    }

    if(hl_destid_get(hl_appl, nodeId, COMMAND_CLASS_SWITCH_MULTILEVEL, 0))
    {
        return -1;
    }

    if(hl_appl->mul_lvl_change_started == 1){
        ALOGI("level change already started, now stop level change");
        hl_multi_lvl_chg(hl_appl);
        return 0;
    }
    // zwcontrol_start_stop_switch_change(hl_appl, 1, 255, 30, 0, 0, 255);
    // Primary switch start level, enter 0 to 99 or 255 to use device current level
    hl_appl->mul_lvl_val = startLvlVal;
    // Dimming duration in seconds which is the interval it takes to dim from level 0 to 99
    hl_appl->mul_lvl_dur = duration;
    // destination desc id
    // hl_appl->temp_desc = 0;
    // Primary switch change dir: (0) Up, (1) Down, (3) No change (for version 3 switch)
    hl_appl->mul_lvl_dir = pmyChangeDir;
    // Secondary switch change dir: (0) Up, (1) Down, (3) No change
    hl_appl->mul_lvl_sec_dir = secChangeDir;
    // Secondary switch step size, enter 0 to 99 or 255 to use default value
    hl_appl->mul_lvl_sec_step = secStep;
    if(secChangeDir == 3)
    {
        hl_appl->mul_lvl_sec_step = 0;
    }

    ALOGD("startLvlVal:%d, duration:%d, pmyChangeDir:%d, secChangeDir:%d, secStep:%d",
           startLvlVal, duration, pmyChangeDir, secChangeDir, secStep);

    hl_multi_lvl_chg(hl_appl);

    return 0;
}
