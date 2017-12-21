#ifndef ZWCONTROL_API_H
#define ZWCONTROL_API_H

#include "zip_api_pte.h"
#include "zip_api.h"

///
/// Container for different types of descriptor
typedef struct  _desc_cont
{
    struct  _desc_cont  *next;      ///< The next (same level) descriptor
    struct  _desc_cont  *down;      ///< The child (one lower level) descriptor
    uint32_t            id;         ///< Unique descriptor id
    uint32_t            type;       ///< The descriptor type in this container
    uint8_t             desc[1];    ///< Place holder for the descriptor

} desc_cont_t;

///
/// Test statistic
typedef struct
{
    uint32_t            rx_seq_num_err;     ///< Number of times received sequence number error
    uint32_t            rx_seq_num_frm;     ///< Number of sequence number frames received
    uint32_t            tx_seq_num_frm;     ///< Number of sequence number frames sent
    uint32_t            tx_multi_lvl_frm;   ///< Number of multi-level switch "set level" frames sent

} test_stat_t;

///
/// High-level application context
typedef struct
{
    volatile int    init_status;   ///< Network initialization status. 0=unknown; 1=done
    volatile unsigned   sec2_cb_enter;///< Control security 2 callback entry bitmask, see SEC2_ENTER_XXX. bit set = allowed callback, 0 = not allowed
    volatile int    sec2_cb_exit;  ///< Security 2 callback status. 1 = exited callback, 0 = waiting or still in the callback
    uint8_t         sec2_add_node; ///< Flag to determine whether to use security 2 when adding node
    sec2_add_prm_t  sec2_add_prm;  ///< Add node with security 2 parameters

    int             use_ipv4;      ///< Flag to indicate whether to use IPv4 or IPv6. 1=IPv4; 0=IPv6
    zwnet_p         zwnet;         ///< Network handle
    uint8_t         zip_gw_ip[16]; ///< Z/IP gateway address in IPv4 or IPv6

    volatile    int32_t is_to_run_test;             ///< Flag to control the test thread to run test
    int32_t             port_number;                ///< The comm port number to use for communication with controller
    uint32_t            exp_seq_num;                ///< Expected received sequence number
    uint32_t            seq_num;                    ///< Sequence number to send
    uint32_t            home_id;                    ///< The Z-wave network home-id
    int32_t             (*stress_tst_func)(struct _hl_appl_ctx   *hl_appl);   ///< Pointer to the stress test function.
    int32_t             is_stress_tst_run;          ///< Flag to indicate whether stress test is running
    int32_t             is_ni_stress_tst_run;       ///< Flag to indicate whether node update stress test is running
    uint8_t             basis_api_ver[20];          ///< Basis API version
    uint32_t            dst_desc_id;                ///< The destination descriptor id where commands are sent
    uint32_t            suc_node_id;                ///< The SUC node id
    uint32_t            failed_node_id;             ///< The failed node id to be removed/replaced
    uint32_t            rep_desc_id;                ///< The report receiving interface descriptor id
    uint32_t            intf_desc_id;               ///< Interface descriptor id
    uint32_t            node_desc_id;               ///< Node descriptor id
    uint32_t            temp_desc;                  ///< Temporary descriptor id
    uint32_t            wkup_interval;              ///< Wake up interval in seconds
    uint32_t            ep_desc_id[5];              ///< Endpoint descriptor id
    uint32_t            desc_id;                    ///< Descriptor id
    uint8_t             lvl;                        ///< Multi-level value
    uint8_t             cap;                        ///< Capabilities of controller
    uint8_t             lib_type;                   ///< Library type of basis API
    uint8_t             mul_lvl_dur;                ///< Multi level dimming duration
    uint8_t             mul_lvl_dir;                ///< Multi level primary switch change direction
    uint8_t             mul_lvl_sec_dir;            ///< Multi level secondary switch change direction
    uint8_t             mul_lvl_sec_step;           ///< Multi level secondary switch step size
    uint8_t             mul_lvl_change_started;     ///< Flag to indicate whether multi level change started
    uint16_t            mul_lvl_val;                ///< Multi level set value
    uint16_t            cfg_range_start;            ///< Configuration parameter number range start
    uint16_t            cfg_range_end;              ///< Configuration parameter number range end
    uint8_t             cfg_param_mode;             ///< Configuration parameter number mode: 0 = single, 1=range
    uint8_t             cfg_param;                  ///< Configuration parameter number
    uint8_t             cfg_size;                   ///< Configuration parameter size
    uint8_t             cfg_value_default;          ///< Configuration parameter value flag: 1=use device default, 0=use cfg_value
    int32_t             cfg_value;                  ///< Configuration parameter value
    uint8_t             bin_state;                  ///< Binary switch state: 0 = off, 1 = on
    uint8_t             cmd_q_ctl;                  ///< Commmand queuing control: 0 = disable, 1 = enable
    uint16_t            basic_val;                  ///< Basic command value
    uint16_t            ind_val;                    ///< Indicator value
    uint8_t             local_prot;                 ///< Local protection state
    uint8_t             rf_prot;                    ///< RF protection state
    uint8_t             time;                       ///< Time
    uint8_t             mul_cmd_ctl;                ///< Multi Command Encapsulation control: 0 = off, 1 = on
    uint8_t             group_id;                   ///< Group id
    uint8_t             node_id;                    ///< Node id
    uint8_t             command[22];                ///< Command and parameters
    uint8_t             cmd_len;                    ///< Command length
    uint8_t             meter_unit;                 ///< Preferred meter reading unit
    uint8_t             usr_id;                     ///< User id for user code
    uint8_t             dlck_mode;                  ///< Door lock operation mode
    uint8_t             broadcast;                  ///< Flag to determine whether to transmit frame in broadcast mode
    uint8_t             alrm_vtype;                 ///< Vendor specific alarm type
    uint8_t             alrm_ztype;                 ///< Z-wave alarm type
    uint8_t             alrm_evt;                   ///< Z-wave alarm event
    uint8_t             alrm_sts;                   ///< Alarm status
    uint8_t             sensor_type;                ///< Sensor type
    uint8_t             sensor_unit;                ///< Sensor unit
    uint8_t             weekday;                    ///< Day of a week
    uint8_t             hour;                       ///< Hour (24 hours format)
    uint8_t             minute;                     ///< Minute
    uint8_t             thrmo_md;                   ///< Thermostat operating mode
    uint8_t             thrmo_fan_md;               ///< Thermostat fan operating mode
    uint8_t             thrmo_fan_off;              ///< Turn thermostat fan off (depend on thrmo_fan_off_cap)
    uint8_t             thrmo_fan_off_cap;          ///< Thermostat fan off mode capability: 0 = not supported, 1 = supported
    uint8_t             thrmo_setb_sta;             ///< Thermostat setback state
    uint8_t             thrmo_setb_typ;             ///< Thermostat setback type
    int8_t              thrmo_setb_deg;             ///< Thermostat setback in units of 1/10 degree
    uint8_t             thrmo_setp_typ;             ///< Thermostat setpoint type
    zwsetp_t            thrmo_setp_data;            ///< Thermostat setpoint data
    zwcc_shed_t         clmt_ctl_schd;              ///< Climate control schedule
    zwcc_shed_ovr_t     clmt_ctl_schd_ovr;          ///< Climate control schedule override
    zwdlck_cfg_t        dlck_config;                ///< Door lock configuration
    zwusrcod_t          usr_code;                   ///< User code
    test_stat_t         test_stat;                  ///< Test statistic
    zwnoded_t           node_updt_desc;             ///< Store the descriptor of the node pending for update info
    zwnoded_t           node_add_desc;              ///< Store the descriptor of the last added node
    zwnoded_t           node_rp_desc;               ///< Store the descriptor of the replaced node
    //appl_hci_cap_t      hci_cap;                    ///< HCI capabilities
    //appl_init_dat_t     init_dat;                   ///< Initialization data from the controller
    appl_layer_ctx_t    appl_ctx;                   ///< Z-wave HCI application layer context
    //zwnet_p             zwnet;                      ///< Z-wave network
    //zw_nameloc_t        nameloc;                    ///< The name location of a node
    char                meter_admin[ZW_ADMIN_STR_MAX + 1];    ///< Meter admin name
    desc_cont_t         *desc_cont_hd;              ///< Head of descriptor container
    void                *desc_cont_mtx;             ///< Mutex to access descriptor container
    char                node_info_file[200];        ///< file name of the node information file
    char                save_file[200];             ///< file name where the node information to be saved
    int                 load_ni_file;               ///< flag to determine whether to load node info file on init. 1=yes, 0=no
    int                 save_ni_file;               ///< flag to determine whether to save node info file on exit. 1=yes, 0=no
    int                 enable_rec_cmd;             ///< Flag to indicate whether to enable command recording
    void                *plt_ctx;                   ///< Platform context for printing of output text messages
    uint16_t            av_btn_down;                ///< Flag to indicate whether the AV button is down
    uint16_t            av_btn_ctl;                 ///< AV Button control code
    char                comm_port_name[80];         ///< Comm port name
    uint16_t            fw_vid;                     ///< Vendor/Manufacturer id
    uint16_t            fw_id;                      ///< Firmware id
    uint16_t            fw_frag_sz;                 ///< Meta data fragment size for firmware update
    uint8_t             fw_tgt;                     ///< Firmware target to update
    uint8_t             poll_ctl;                   ///< Polling control: 0 = off, 1 = on
    zwpoll_req_t        poll_req;                   ///< Polling request
    uint32_t            poll_id;                    ///< Polling request identifier
    int                 poll_id_type;               ///< Polling request identifier type: 0=handle, 1=token
    uint8_t             pow_lvl;                    ///< Power level indicator value
    uint8_t             pow_lvl_timeout;            ///< Power level timeout value: 1-255
    uint8_t             pow_lvl_test_node_desc_id;      ///< Power level test node descriptor ID
    uint16_t            pow_lvl_test_wframcnt;      ///< Power level test frame count
#ifdef  USER_APPL_DEVICE_CFG
    dev_cfg_buf_t       dev_cfg_bufs[4];            ///< buffers that store an arrays of device specific configurations
                                                    ///< with the first buffer has the highest priority for device matching
#endif
    uint8_t            sw_color_lvl_change_started; ///< Flag to indicate whether switch color level change started

} hl_appl_ctx_t;

typedef struct
{
    hl_appl_ctx_t   *hl_appl;   //< high level application context
    uint16_t        sts;        //< status of current operation
    uint8_t         op;         //< network operation ZWNET_OP_XXX

} nw_notify_msg_t;

typedef int (*ResCallBack)(const char*);

int zwcontrol_init(hl_appl_ctx_t *hl_appl, const char *resPath, const char* infoPath, uint8_t* result);
int zwcontrol_setcallback(ResCallBack callBack);
int zwcontrol_add_node(hl_appl_ctx_t *hl_appl, const char* dsk, int dsklen);
int zwcontrol_rm_node(hl_appl_ctx_t *hl_appl);
int zwcontrol_exit(hl_appl_ctx_t *hl_appl);
int zwcontrol_get_node_list(hl_appl_ctx_t *hl_appl);
int zwcontrol_get_node_info(hl_appl_ctx_t* hl_appl);
int zwcontrol_default_set(hl_appl_ctx_t *hl_appl);
int zwcontrol_save_nodeinfo(hl_appl_ctx_t *hl_appl, const char* filepath);
//Add by jay.k >>start
int  zwcontrol_stop_op(hl_appl_ctx_t *hl_appl);
int  zwcontrol_update_node(hl_appl_ctx_t *hl_appl, uint8_t nodeId);
int  zwcontrol_start_learn_mode(hl_appl_ctx_t* hl_appl);
//Add by jay.k <<end

/*
 **  Command Class Battery
 */
int  zwcontrol_battery_get(hl_appl_ctx_t *hl_appl, uint32_t nodeId);

/*
 **  Command Class Baisc ver 1~2
 */
int  zwcontrol_basic_get(hl_appl_ctx_t *hl_appl, int nodeId);
int  zwcontrol_basic_set(hl_appl_ctx_t *hl_appl, int nodeId, int value);


/*
 **  Command Class Configuration
 */
int  zwcontrol_configuration_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t paramMode, uint8_t paramNumber,
                                 uint16_t rangeStart, uint16_t rangeEnd);
int  zwcontrol_configuration_set(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t paramNumber,
                                 uint8_t paramSize, uint8_t useDefault, int32_t paramValue);

/*
 **  For command queue control
 */
int  zwcontrol_command_queue_state_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId);
int  zwcontrol_command_queue_turn_on_off(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t state);
int  zwcontrol_command_queue_view(hl_appl_ctx_t* hl_appl, uint32_t nodeId);
int  zwcontrol_command_queue_cancel(hl_appl_ctx_t* hl_appl, uint32_t nodeId);


/*
 **  Command Class Meter
 */
int  zwcontrol_meter_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t meter_unit);
int  zwcontrol_meter_supported_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId);
int  zwcontrol_meter_reset(hl_appl_ctx_t* hl_appl, uint32_t nodeId);


/*
 **  Command Class Power Level
 */
int  zwcontrol_powerLevel_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId);


/*
 **  Command Class Sensor Multi-Level
 */
int  zwcontrol_sensor_multilevel_get(hl_appl_ctx_t *hl_appl, uint32_t nodeId/*, uint8_t sensor_type, uint8_t unit*/);


/*
 **  Command Class Notification version 4
 **  
 */
int  zwcontrol_notification_set(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t notificationType, uint8_t status);
int  zwcontrol_notification_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t alarmType, uint8_t notificationType, uint8_t state);
int  zwcontrol_notification_supported_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId);
int  zwcontrol_notification_supported_event_get(hl_appl_ctx_t* hl_appl, uint32_t nodeId, uint8_t notificationType);


#endif