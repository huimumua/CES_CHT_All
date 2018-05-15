/**
@file   zip_network_save.c - Z-wave High Level API for saving network information to persistent storage implementation.

@author David Chow

@version    1.0 22-9-15  Initial release

@copyright � 2015 SIGMA DESIGNS, INC. THIS IS AN UNPUBLISHED WORK PROTECTED BY SIGMA DESIGNS, INC.
AS A TRADE SECRET, AND IS NOT TO BE USED OR DISCLOSED EXCEPT AS PROVIDED Z-WAVE CONTROLLER DEVELOPMENT KIT
LIMITED LICENSE AGREEMENT. ALL RIGHTS RESERVED.

NOTICE: ALL INFORMATION CONTAINED HEREIN IS CONFIDENTIAL AND/OR PROPRIETARY TO SIGMA DESIGNS
AND MAY BE COVERED BY U.S. AND FOREIGN PATENTS, PATENTS IN PROCESS, AND ARE PROTECTED BY TRADE SECRET
OR COPYRIGHT LAW. DISSEMINATION OR REPRODUCTION OF THE SOURCE CODE CONTAINED HEREIN IS EXPRESSLY FORBIDDEN
TO ANYONE EXCEPT LICENSEES OF SIGMA DESIGNS  WHO HAVE EXECUTED A SIGMA DESIGNS' Z-WAVE CONTROLLER DEVELOPMENT KIT
LIMITED LICENSE AGREEMENT. THE COPYRIGHT NOTICE ABOVE IS NOT EVIDENCE OF ANY ACTUAL OR INTENDED PUBLICATION OF
THE SOURCE CODE. THE RECEIPT OR POSSESSION OF  THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR
IMPLY ANY RIGHTS  TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL A PRODUCT
THAT IT  MAY DESCRIBE.


THE SIGMA PROGRAM AND ANY RELATED DOCUMENTATION OR TOOLS IS PROVIDED TO COMPANY "AS IS" AND "WITH ALL FAULTS",
WITHOUT WARRANTY OF ANY KIND FROM SIGMA. COMPANY ASSUMES ALL RISKS THAT LICENSED MATERIALS ARE SUITABLE OR ACCURATE
FOR COMPANY'S NEEDS AND COMPANY'S USE OF THE SIGMA PROGRAM IS AT COMPANY'S OWN DISCRETION AND RISK. SIGMA DOES NOT
GUARANTEE THAT THE USE OF THE SIGMA PROGRAM IN A THIRD PARTY SERVICE ENVIRONMENT OR CLOUD SERVICES ENVIRONMENT WILL BE:
(A) PERFORMED ERROR-FREE OR UNINTERRUPTED; (B) THAT SIGMA WILL CORRECT ANY THIRD PARTY SERVICE ENVIRONMENT OR
CLOUD SERVICE ENVIRONMENT ERRORS; (C) THE THIRD PARTY SERVICE ENVIRONMENT OR CLOUD SERVICE ENVIRONMENT WILL
OPERATE IN COMBINATION WITH COMPANY'S CONTENT OR COMPANY APPLICATIONS THAT UTILIZE THE SIGMA PROGRAM;
(D) OR WITH ANY OTHER HARDWARE, SOFTWARE, SYSTEMS, SERVICES OR DATA NOT PROVIDED BY SIGMA. COMPANY ACKNOWLEDGES
THAT SIGMA DOES NOT CONTROL THE TRANSFER OF DATA OVER COMMUNICATIONS FACILITIES, INCLUDING THE INTERNET, AND THAT
THE SERVICES MAY BE SUBJECT TO LIMITATIONS, DELAYS, AND OTHER PROBLEMS INHERENT IN THE USE OF SUCH COMMUNICATIONS
FACILITIES. SIGMA IS NOT RESPONSIBLE FOR ANY DELAYS, DELIVERY FAILURES, OR OTHER DAMAGE RESULTING FROM SUCH ISSUES.
SIGMA IS NOT RESPONSIBLE FOR ANY ISSUES RELATED TO THE PERFORMANCE, OPERATION OR SECURITY OF THE THIRD PARTY SERVICE
ENVIRONMENT OR CLOUD SERVICES ENVIRONMENT THAT ARISE FROM COMPANY CONTENT, COMPANY APPLICATIONS OR THIRD PARTY CONTENT.
SIGMA DOES NOT MAKE ANY REPRESENTATION OR WARRANTY REGARDING THE RELIABILITY, ACCURACY, COMPLETENESS, CORRECTNESS, OR
USEFULNESS OF THIRD PARTY CONTENT OR SERVICE OR THE SIGMA PROGRAM, AND DISCLAIMS ALL LIABILITIES ARISING FROM OR RELATED
TO THE SIGMA PROGRAM OR THIRD PARTY CONTENT OR SERVICES. TO THE EXTENT NOT PROHIBITED BY LAW, THESE WARRANTIES ARE EXCLUSIVE.
SIGMA OFFERS NO WARRANTY OF NON-INFRINGEMENT, TITLE, OR QUIET ENJOYMENT. NEITHER SIGMA NOR ITS SUPPLIERS OR LICENSORS
SHALL BE LIABLE FOR ANY INDIRECT, SPECIAL, INCIDENTAL OR CONSEQUENTIAL DAMAGES OR LOSS (INCLUDING DAMAGES FOR LOSS OF
BUSINESS, LOSS OF PROFITS, OR THE LIKE), ARISING OUT OF THIS AGREEMENT WHETHER BASED ON BREACH OF CONTRACT,
INTELLECTUAL PROPERTY INFRINGEMENT, TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY, PRODUCT LIABILITY OR OTHERWISE,
EVEN IF SIGMA OR ITS REPRESENTATIVES HAVE BEEN ADVISED OF OR OTHERWISE SHOULD KNOW ABOUT THE POSSIBILITY OF SUCH DAMAGES.
THERE ARE NO OTHER EXPRESS OR IMPLIED WARRANTIES OR CONDITIONS INCLUDING FOR SOFTWARE, HARDWARE, SYSTEMS, NETWORKS OR
ENVIRONMENTS OR FOR MERCHANTABILITY, NONINFRINGEMENT, SATISFACTORY QUALITY AND FITNESS FOR A PARTICULAR PURPOSE.

The Sigma Program  is not fault-tolerant and is not designed, manufactured or intended for use or resale as on-line control
equipment in hazardous environments requiring fail-safe performance, such as in the operation of nuclear facilities,
aircraft navigation or communication systems, air traffic control, direct life support machines, or weapons systems,
in which the failure of the Sigma Program, or Company Applications created using the Sigma Program, could lead directly
to death, personal injury, or severe physical or environmental damage ("High Risk Activities").  Sigma and its suppliers
specifically disclaim any express or implied warranty of fitness for High Risk Activities.Without limiting Sigma's obligation
of confidentiality as further described in the Z-Wave Controller Development Kit Limited License Agreement, Sigma has no
obligation to establish and maintain a data privacy and information security program with regard to Company's use of any
Third Party Service Environment or Cloud Service Environment. For the avoidance of doubt, Sigma shall not be responsible
for physical, technical, security, administrative, and/or organizational safeguards that are designed to ensure the
security and confidentiality of the Company Content or Company Application in any Third Party Service Environment or
Cloud Service Environment that Company chooses to utilize.
*/

#include <stdlib.h>
#ifndef OS_MAC_X
#include <malloc.h>
#endif
#include <memory.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include "../include/zip_api_pte.h"
#include "../include/zip_api_util.h"
#include "../include/zip_network_save.h"


/**
@defgroup Nw_Save Saving network information APIs
Used to save network information to persistent storage
@ingroup zwarecapi
@{
*/


/**
zwsave_tmr_tick_cb - Timer tick timeout callback
@param[in] data     Pointer to network save context
@return
*/
static void    zwsave_tmr_tick_cb(void *data)
{
    zwsave_ctx_t    *zwsave_ctx = (zwsave_ctx_t *)data;
    int             msg_type;

    //Send timer tick event
    msg_type = ZWSAVE_EVT_TYPE_TIMER;

    util_msg_loop_send(zwsave_ctx->evt_msg_loop_ctx, (void *)&msg_type, sizeof(msg_type));
}


/**
zwsave_evt_msg_hdlr - Process message loop on message arrival
@param[in]	usr_prm	    user parameter supplied during initialization
@param[in]	msg	        message
return      1 to exit message loop; else return 0 to continue the message loop
*/
static int zwsave_evt_msg_hdlr(void *usr_prm, void *msg)
{
    zwsave_ctx_t    *zwsave_ctx = (zwsave_ctx_t *)usr_prm;
    int             *msg_type = (int *)msg;
    zwnet_p         nw = zwsave_ctx->net;

    if ((*msg_type == ZWSAVE_EVT_TYPE_TIMER) || (*msg_type == ZWSAVE_EVT_TYPE_SAVE))
    {
        if (*msg_type == ZWSAVE_EVT_TYPE_SAVE)
        {   //Set flag to write to persistent storage
            zwsave_ctx->save_nw = 1;
        }

        if (zwsave_ctx->save_nw && nw->node_info_file && (nw->homeid != 0) && (!nw->nw_init_failed))
        {
            plt_mtx_lck(nw->mtx);
            if (nw->curr_op == ZWNET_OP_NONE)
            {
                nw->curr_op = ZWNET_OP_SAVE_NW;
            }
            plt_mtx_ulck(nw->mtx); //Don't hold any lock while writing to disk

            if (nw->curr_op == ZWNET_OP_SAVE_NW)
            {
                int result;

                result = zwutl_ni_save(nw, nw->node_info_file);
                if (result < 0)
                {
                    debug_zwapi_msg(&nw->plt_ctx, "Save node information file '%s' failed: %d", nw->node_info_file, result);
                }
                else
                {
                    debug_zwapi_msg(&nw->plt_ctx, "Network info saved ...");
                }

                // Reset flag
                zwsave_ctx->save_nw = 0;
                plt_mtx_lck(nw->mtx);
                nw->curr_op = ZWNET_OP_NONE;
                plt_mtx_ulck(nw->mtx);

            }
        }
    }

    return 0;
}


/**
zwsave_save - Save network information into persistent storage
@param[in]	zwsave_ctx	    Network save context
@return
*/
void zwsave_save(zwsave_ctx_t *zwsave_ctx)
{
    int msg_type;

    //Send save request event
    msg_type = ZWSAVE_EVT_TYPE_SAVE;

    util_msg_loop_send(zwsave_ctx->evt_msg_loop_ctx, (void *)&msg_type, sizeof(msg_type));
}


/**
zwsave_init - Initialize the saving network information to persistent storage facility
@param[in]	zwsave_ctx	    Network save context
@return  0 on success; negative error number on failure
@note  Must call zwsave_shutdown followed by zwsave_exit to shutdown and clean up the facility
*/
int zwsave_init(zwsave_ctx_t *zwsave_ctx)
{
    int     res;

    res = util_msg_loop_init(zwsave_evt_msg_hdlr, zwsave_ctx, NULL, 2, &zwsave_ctx->evt_msg_loop_ctx);

    if (res != 0)
    {
        goto l_ZWSAVE_INIT_ERROR1;
    }

    zwsave_ctx->tick_tmr_ctx = plt_periodic_start(&zwsave_ctx->net->plt_ctx, ZWSAVE_TIMER_TICK, zwsave_tmr_tick_cb, zwsave_ctx);
    if (!zwsave_ctx->tick_tmr_ctx)
        goto l_ZWSAVE_INIT_ERROR2;

    return ZW_ERR_NONE;

l_ZWSAVE_INIT_ERROR2:
    util_msg_loop_shutdown(zwsave_ctx->evt_msg_loop_ctx, 0);
    util_msg_loop_exit(zwsave_ctx->evt_msg_loop_ctx);
l_ZWSAVE_INIT_ERROR1:
    return ZW_ERR_NO_RES;

}


/**
zwsave_shutdown - Shutdown the saving network information to persistent storage facility
@param[in]	zwsave_ctx	    Network save context
@return
*/
void zwsave_shutdown(zwsave_ctx_t *zwsave_ctx)
{
    //Stop timer
    plt_tmr_stop(&zwsave_ctx->net->plt_ctx, zwsave_ctx->tick_tmr_ctx);

    //Stop message loop
    util_msg_loop_shutdown(zwsave_ctx->evt_msg_loop_ctx, 0);
}


/**
zwsave_exit - Clean up
@param[in]	zwsave_ctx	    Network save context
@return
*/
void zwsave_exit(zwsave_ctx_t *zwsave_ctx)
{
    util_msg_loop_exit(zwsave_ctx->evt_msg_loop_ctx);
}


/**
@}
*/















