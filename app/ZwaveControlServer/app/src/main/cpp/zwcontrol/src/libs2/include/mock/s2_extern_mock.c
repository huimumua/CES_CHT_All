/* © 2017 Sigma Designs, Inc. This is an unpublished work protected by Sigma
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
/*
 * s2_extern_mock.c
 *
 *  Created on: Aug 31, 2015
 *      Author: trasmussen
 */

#include "s2_protocol.h"

#include <stdint.h>

#include "S2.h"
#include "s2_protocol.h"
#include "mock_control.h"

#define MOCK_FILE "s2_extern_mock.c"



// TODO: Extend comparison of S2 struct on need to have basis.
#define MOCK_CALL_COMPARE_INPUT_STRUCT_S2(P_MOCK, ARGUMENT, P_RECV_S2) do {                               \
  MOCK_CALL_COMPARE_STRUCT_MEMBER_UINT32(P_MOCK, ARGUMENT, P_RECV_S2, struct S2, my_home_id);             \
  MOCK_CALL_COMPARE_STRUCT_ARRAY_LENGTH_UINT8(P_MOCK, ARGUMENT, P_RECV_S2, struct S2, sg[0].enc_key, 16); \
  } while (0)

#define MOCK_CALL_COMPARE_INPUT_STRUCT_S2_CONNECTION(P_MOCK, ARGUMENT, P_ACTUAL) do {            \
  MOCK_CALL_COMPARE_STRUCT_MEMBER_UINT8(P_MOCK, ARGUMENT, P_ACTUAL, s2_connection_t, l_node);    \
  MOCK_CALL_COMPARE_STRUCT_MEMBER_UINT8(P_MOCK, ARGUMENT, P_ACTUAL, s2_connection_t, r_node);    \
  MOCK_CALL_COMPARE_STRUCT_MEMBER_UINT8(P_MOCK, ARGUMENT, P_ACTUAL, s2_connection_t, tx_options);\
  } while (0)


/**
 * This section contains mocking of the functions that are expeted to be implemented externally of
 * the s2 library.
 */
void S2_send_done_event(struct S2* ctxt, s2_tx_status_t status)
{
  mock_t * p_mock;
  MOCK_CALL_RETURN_VOID_IF_USED_AS_STUB();
  MOCK_CALL_FIND_RETURN_VOID_ON_FAILURE(p_mock);

  MOCK_CALL_ACTUAL(p_mock, ctxt, status);

  MOCK_CALL_COMPARE_INPUT_STRUCT_S2(p_mock, ARG0, ctxt);

  MOCK_CALL_COMPARE_INPUT_UINT8(p_mock, ARG1, status);
}

void S2_msg_received_event(struct S2* ctxt,s2_connection_t* src , uint8_t* buf, uint16_t len)
{
  mock_t * p_mock;

  MOCK_CALL_RETURN_VOID_IF_USED_AS_STUB();
  MOCK_CALL_FIND_RETURN_VOID_ON_FAILURE(p_mock);

  MOCK_CALL_ACTUAL(p_mock, ctxt, src, buf, len);

  MOCK_CALL_COMPARE_INPUT_STRUCT_S2(p_mock, ARG0, ctxt);
  MOCK_CALL_COMPARE_INPUT_STRUCT_S2_CONNECTION(p_mock, ARG1, src);
  MOCK_CALL_COMPARE_INPUT_UINT8_ARRAY(p_mock, ARG2, p_mock->expect_arg[3].v, buf, len);
}

uint8_t S2_send_frame(struct S2* ctxt,const s2_connection_t* conn, uint8_t* buf, uint16_t len)
{
  mock_t * p_mock;

  MOCK_CALL_RETURN_IF_USED_AS_STUB(0x01);
  MOCK_CALL_FIND_RETURN_ON_FAILURE(p_mock, 0x00);

  MOCK_CALL_ACTUAL(p_mock, ctxt, conn, buf, len);

  MOCK_CALL_COMPARE_INPUT_STRUCT_S2(p_mock, ARG0, ctxt);
  MOCK_CALL_COMPARE_INPUT_STRUCT_S2_CONNECTION(p_mock, ARG1, conn);
  MOCK_CALL_COMPARE_INPUT_UINT8_ARRAY(p_mock, ARG2, p_mock->expect_arg[3].v, buf, len);

  MOCK_CALL_RETURN_VALUE(p_mock, uint8_t);
}

void S2_set_timeout(struct S2* ctxt, uint32_t interval)
{
  mock_t * p_mock;

  MOCK_CALL_RETURN_VOID_IF_USED_AS_STUB();
  MOCK_CALL_FIND_RETURN_VOID_ON_FAILURE(p_mock);

  MOCK_CALL_ACTUAL(p_mock, ctxt, interval);

  MOCK_CALL_COMPARE_INPUT_STRUCT_S2(p_mock, ARG0, ctxt);
  MOCK_CALL_COMPARE_INPUT_UINT32(p_mock, ARG1, interval);
}

void S2_get_hw_random(uint8_t *buf, uint8_t len)
{
  mock_t * p_mock;

  MOCK_CALL_RETURN_VOID_IF_USED_AS_STUB();
  MOCK_CALL_FIND_RETURN_VOID_ON_FAILURE(p_mock);

  MOCK_CALL_ACTUAL(p_mock, buf, len);

  MOCK_CALL_COMPARE_INPUT_UINT8(p_mock, ARG1, len);

  MOCK_CALL_SET_OUTPUT_ARRAY(p_mock->output_arg[0].p, buf, len, uint8_t);
}
