#include "zw_api.h"
#include "s2_classcmd.h"
#include "zw_sec2_wrap.h"

uint8_t ecdh_dynamic_key[32];
static struct S2* s2_ctx = NULL;

static void sec2_event_handler(zwave_event_t* ev)
{
    uint16_t response;
    uint8_t flags;
    ALOGW("S2 inclusion event %i\n",ev->event_type);

    switch(ev->event_type)
    {
        case S2_NODE_INCLUSION_INITIATED_EVENT:
            break;

        case S2_NODE_INCLUSION_PUBLIC_KEY_CHALLENGE_EVENT:
            break;

        case S2_NODE_INCLUSION_KEX_REPORT_EVENT:
            ALOGW("csa %i keys %i\n",ev->evt.s2_event.s2_data.kex_report.csa, ev->evt.s2_event.s2_data.kex_report.security_keys);
            break;

        case S2_NODE_INCLUSION_COMPLETE_EVENT:
            break;

        case S2_NODE_JOINING_COMPLETE_EVENT:
            break;

        case S2_NODE_INCLUSION_FAILED_EVENT:
            break;

        default:
            break;
    }
}

void sec2_create_new_dynamic_ecdh_key()
{
    AES_CTR_DRBG_Generate(&s2_ctr_drbg, ecdh_dynamic_key);
    AES_CTR_DRBG_Generate(&s2_ctr_drbg, &ecdh_dynamic_key[16]);
}

void sec2_init(uint32_t homeID)
{
    static uint8_t s2_cmd_class_sup_report[64];

    if(s2_ctx)
    {
        S2_destroy(s2_ctx);
        s2_ctx = NULL;
    }

    s2_inclusion_init(SECURITY_2_SCHEME_1_SUPPORT,KEX_REPORT_CURVE_25519,
                      SECURITY_2_SECURITY_2_CLASS_0|SECURITY_2_SECURITY_2_CLASS_1
                      | SECURITY_2_SECURITY_2_CLASS_2 | SECURITY_2_SECURITY_0_NETWORK_KEY);

    s2_ctx = S2_init_ctx(homeID);

    s2_inclusion_set_event_handler(&sec2_event_handler);
    sec2_create_new_dynamic_ecdh_key();
}

void sec2_destroy()
{
    if(s2_ctx)
    {
        S2_destroy(s2_ctx);
        s2_ctx = NULL;
    }
}

void sec2_start_add_node()
{
    if(s2_ctx)
    {
        s2_connection_t s2_con;
        s2_inclusion_including_start(s2_ctx, &s2_con);
    }
}

uint8_t S2_send_frame_multi(struct S2* ctxt,s2_connection_t* conn, uint8_t* buf, uint16_t len){
    return 0;
}

void S2_set_timeout(struct S2* ctxt, uint32_t interval) {
    ALOGW("S2_set_timeout interval =%i ms\n",interval );
}

void S2_get_hw_random(uint8_t *buf, uint8_t len) {
}

void S2_get_commands_supported(uint8_t lnode,uint8_t class_id, uint8_t** cmdClasses, uint8_t* length) {

}

void S2_msg_received_event(struct S2* ctxt,s2_connection_t* src , uint8_t* buf, uint16_t len) {

}

uint8_t S2_send_frame(struct S2* ctxt,const s2_connection_t* conn, uint8_t* buf, uint16_t len) {

    return 0;
}

void S2_send_done_event(struct S2* ctxt, s2_tx_status_t status) {

}

uint8_t s2_inclusion_set_timeout(struct S2* ctxt, uint32_t interval) {
    ALOGW("s2_inclusion_set_timeout interval =%i ms\n",interval * 10);
    return 0;
}

void sec2_abort_join()
{
    ALOGD("S2 inclusion was aborted\n");

    if(!s2_ctx)
        return;

    s2_inclusion_abort(s2_ctx);
}