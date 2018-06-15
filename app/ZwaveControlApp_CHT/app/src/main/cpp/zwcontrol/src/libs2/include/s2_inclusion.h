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
 * s2_inclusion.h
 *
 *  Created on: Aug 28, 2015
 *      Author: trasmussen
 */

#ifndef _S2_INCLUSION_H_
#define _S2_INCLUSION_H_

#include "S2.h"
#include "s2_event.h"
#include <stdint.h>

/**
 * @defgroup s2incl S2 inclusion
 * Hooks to start the S2 inclusion state machine. This state machine will handle both
 * add mode and learn mode.
 *
 * The upper layers must be able to act on certain events during inclusion.
 *
 * When in learn mode
 * the upper layer must accept the DSK of the controller. A node may choose to present the DSK
 * on a display to allow the user to verify the including controller.
 *
 * When in add mode, the controller must accept the requested keys as well as the DSK of the
 * joining node.
 *
 * \section add_mode_fsm Add Mode State Machine
 * Diagram showing the state machine during an inclusion when in Add Mode.
 * Transitions colored in red indicates that the upper layer will also be notified with an \ref s2_event_t."
 *
 * \dot
 * digraph finite_state_machine {
 * size="18,8"
 * node [shape = ellipse];
 * {rank=same; 1}
 * {rank=same; 2 10 9}
 * {rank=same; 3 8}
 * {rank=same; 4 7}
 * {rank=same; 5 6}
 *
 * 1 [label="    Idle    "]
 * 2 [label="Wait\n Kex Report"]
 * 3 [label="Wait\n User Accept"]
 * 4 [label="Wait\n Public Key"]
 * 5 [label="Wait\n User Accept"]
 * 6 [label="Wait\n Echo Kex Set"]
 * 7 [label="Wait\n Net Key Get"]
 * 8 [label="Wait\n Net Key Verify"]
 * 9 [label="Wait\n Transfer End"]
 * 10 [label="Error Sent"]
 *
 * 1 -> 2  [ label = "Including Start" ];
 * 2 -> 3  [ label = "KEX Report",color="0.002 0.999 0.999", headport=n ];
 * 3 -> 4  [ label = "Including Accept" ];
 * 4 -> 5  [ label = "Public Key",color="0.002 0.999 0.999" ];
 * 5 -> 6  [ label = "DSK Input\n Including Accept"];
 *
 * 1 -> 9  [ label = "Transfer End",color="0.002 0.999 0.999", dir = "back" ];
 * 9 -> 8  [ label = "Net Key Verify\n (Final Key)", dir = "back" ];
 * 8 -> 7  [ label = "Net Key Get\n ", dir = "back" ];
 * 7 -> 6  [ label = "Echo Kex Set", dir = "back" ];
 * 7 -> 8  [ label = "\n Net Key Verify", dir = "back" ];
 *
 * 10 -> 5  [ label = "Including Reject", dir = "back" ];
 * 10 -> 3  [ label = "Including Reject", dir = "back" headport=ne];
 * 10 -> 1  [ label = "\n \n Send Done",color="0.002 0.999 0.999"];
 *
 * 2 -> 2  [ label = "Frame Send Retry", tailport=nw headport=sw ];
 * 3 -> 3  [ label = "KEX Report", tailport=nw headport=sw ];
 * 4 -> 4  [ label = "Frame Send Retry\n KEX Report", tailport=nw headport=sw ];
 * 5 -> 5  [ label = "Frame Send Retry\n KEX Report\n Public Key", tailport=nw headport=sw ];
 * 6 -> 6  [ label = "Frame Send Retry\n KEX Report\n Public Key\n ", tailport=ne headport=se ];
 * 7 -> 7  [ label = "Frame Send Retry\n KEX Report\n Public Key\n Echo Kex Set\n Net Key Verify", tailport=ne headport=se ];
 * 8 -> 8  [ label = "Frame Send Retry\n KEX Report\n Public Key\n Echo Kex Set\n Net Key Get", tailport=ne headport=se ];
 * 9 -> 9  [ label = "Frame Send Retry\n KEX Report\n Public Key\n Echo Kex Set\n Net Key Get\n Net Key Verify", tailport=ne headport=se ];
 *
 * e11  [label="Idle"]
 * e12  [label="Any State"]
 * e13  [label="Error Sent"]
 *
 * subgraph cluster_0 {
 *   node [shape = ellipse];
 *	 e12 -> e11  [ label = "Timeout", color="0.002 0.999 0.999" ];
 *	 e12 -> e13  [ label = "Error Sent" ];
 *	 e13 -> e11  [ label = "Send Done\n Send Failed\n Timeout     ",color="0.002 0.999 0.999" ];
 *
 *   label = "Error / Timeout handling";
 *   color = black;
 *  }
 * }
 * \enddot
 *
 * \section learn_mode_fsm Learn Mode State Machine
 * Diagram showing the state machine during an inclusion when in Learn Mode.
 *
 * \dot
 * digraph finite_state_machine {
 * size="18,8"
 * node [shape = ellipse];
 * {rank=same; 1}
 * {rank=same; 2 9}
 * {rank=same; 3 8}
 * {rank=same; 4 7}
 * {rank=same; 5 6}
 *
 * 1 [label="    Idle    "]
 * 2 [label="Wait\n Kex Get"]
 * 3 [label="Wait\n Kex Set"]
 * 4 [label="Wait\n Public Key"]
 * 5 [label="Wait\n User Accept"]
 * 6 [label="Sending\n Echo Kex Set"]
 * 7 [label="Wait\n Net Key Report"]
 * 8 [label="Key Exchanged"]
 * 9 [label="Sending\n Transfer End"]
 *
 * 1 -> 2  [ label = "\nIncluding Reject", dir = "back" ];
 * 1 -> 2  [ label = "Joining Start" ];
 * #2 -> 1  [ label = "Joining Start", dir="back" ];
 * 2 -> 3  [ label = " KEX Get", color="0.002 0.999 0.999" ];
 * 3 -> 4  [ label = " KEX Set" ];
 * 4 -> 5  [ label = " Public Key",color="0.002 0.999 0.999" ];
 * 5 -> 6  [ label = "Including Accept" ];
 *
 * 1 -> 9  [ label = " Send Done", color="0.002 0.999 0.999", dir = "back" ];
 * 9 -> 8  [ label = " Final Transfer End", dir = "back" ];
 * 8 -> 7  [ label = " Net Key Report\n ", dir = "back" ];
 * 7 -> 6  [ label = " Echo Kex Report", dir = "back" ];
 * 7 -> 8  [ label = "\n Transfer End", dir = "back" ];
 *
 * #1 -> 2  [ label = "", style="invis" ];
 * #2 -> 1  [ label = " Including Reject" ];
 * 5 -> 1  [ label = "    Including Reject", color="0.002 0.999 0.999" ];
 * #6 -> 1  [ label = "Including Reject", color="0.002 0.999 0.999" ];
 * #1 -> 6  [ label = "Including Reject", dir = "back", color="0.002 0.999 0.999" headport=nw ];
 * 1 -> 6  [ dir = "back", color="0.002 0.999 0.999" headport=nw ];
 *
 * 2 -> 2  [ label = "Discovery Complete", tailport=nw headport=sw ];
 * 3 -> 3  [ label = "Frame Send Retry\n KEX Get", tailport=nw headport=sw ];
 * 4 -> 4  [ label = "Frame Send Retry\n KEX Get\n KEX Set", tailport=nw headport=sw ];
 * 5 -> 5  [ label = "Frame Send Retry\n KEX Get\n KEX Set\n Public Key", tailport=nw headport=sw ];
 * 6 -> 6  [ label = "Frame Send Retry\n Send Failed\n Send Done\n KEX Get\n KEX Set\n Public Key\n ", tailport=ne headport=se ];
 * 7 -> 7  [ label = "Frame Send Retry\n KEX Get\n KEX Set\n Public Key\n Echo Kex Report", tailport=ne headport=se ];
 * 8 -> 8  [ label = "Frame Send Retry\n KEX Get\n KEX Set\n Public Key\n Echo Kex Report\n Net Key Report", tailport=ne headport=se ];
 * 9 -> 9  [ label = "Frame Send Retry\n KEX Get\n KEX Set\n Public Key\n Echo Kex Report\n Net Key Report\n (Final) Transfer End", tailport=ne headport=se ];
 *
 * e11  [label="Idle"]
 * e12  [label="Any State"]
 * e13  [label="Error Sent"]
 *
 * subgraph cluster_0 {
 *   node [shape = ellipse];
 *	 e12 -> e11  [ label = "Timeout", color="0.002 0.999 0.999" ];
 *	 e12 -> e13  [ label = "Error Sent" ];
 *	 e13 -> e11  [ label = "Send Done\n Send Failed\n Timeout     ",color="0.002 0.999 0.999" ];
 *
 *   label = "Error / Timeout handling";
 *   color = black;
 *  }
 * }
 * \enddot
 * Transitions colored in red indicates that the upper layer will also be notified with an \ref s2_event_t."
 *
 * @{
 */

#ifdef SINGLE_CONTEXT

#define s2_inclusion_challenge_response(a, b, c)    s2_inclusion_challenge_response(b, c)
#define s2_inclusion_neighbor_discovery_complete(a) s2_inclusion_neighbor_discovery_complete()
#define s2_inclusion_abort(a)                       s2_inclusion_abort()
#define s2_inclusion_set_timeout(a, b)              s2_inclusion_set_timeout(b)
#define s2_inclusion_joining_start(a, b, c)         s2_inclusion_joining_start(b, c)
#define s2_inclusion_including_start(a, b)          s2_inclusion_including_start(b)
#ifdef ZW_CONTROLLER
#define s2_inclusion_key_grant(a, b, c, d)          s2_inclusion_key_grant(b, c, d)
#define s2_restore_keys(a)                          s2_restore_keys()
#ifdef __C51__
#define ZCB_s2_inclusion_notify_timeout(a)          ZCB_s2_inclusion_notify_timeout()
#else
#define s2_inclusion_notify_timeout(a)              s2_inclusion_notify_timeout()
#endif
#endif

#else // SINGLE_CONTEXT

#define MP_CTX_DEF mp_context = p_context;

#endif // SINGLE_CONTEXT


/** @brief Function for setting event handler in S2 inclusion module.
 *
 *  @details The @ref s2_inclusion_event_handler is used for notifying the applications of any
 *           event related to S2 inclusion security.
 *
 *  @param[in] evt_handler  Event handler to which events should be sent.
 */
void s2_inclusion_set_event_handler(s2_event_handler_t evt_handler);

/** @brief This function handles the acceptance and response for the the first 2 bytes of the DSK.
 *
 *  @details This function must be called as a response to the
 *           @ref S2_NODE_INCLUSION_PUBLIC_KEY_CHALLENGE_EVENT from the inclusion layer in order
 *           to proceed with node inclusion.
 *
 *  @note This function is only valid for a Z-Wave controller capable of including Z-Wave nodes.
 *
 *  @param[in] p_context Pointer to the context to which the response belongs.
 *  @param[in] include   Boolean flag deciding if the joining node is allowed into the network,
 *                       0 = reject node from inclusion, 1 = accept inclusion of node.
 *  @param[in] response  Response value to the challenge, that is the first n bytes of the DSK.
 */
void s2_inclusion_challenge_response(struct S2 *p_context, uint8_t include, const uint8_t* response);

#ifdef ZW_CONTROLLER
/** @brief This function handles the acceptance and key granting for a joining node.
 *
 *  @details This function must be called as a response to the
 *           @ref S2_NODE_INCLUSION_KEX_REPORT_EVENT from the inclusion layer in order to proceed
 *           with node inclusion.
 *
 *  @note This function is only valid for a Z-Wave controller capable of including Z-Wave nodes.
 *
 *  @param[in] p_context Pointer to the context to which the response belongs.
 *  @param[in] include   Boolean flag deciding if the joining node is allowed into the network,
 *                       0 = reject node from inclusion, 1 = accept inclusion of node.
 *  @param[in] csa       Boolean, true is allow client side authentication.
 *  @param[in] keys      Granted keys.
 */
void s2_inclusion_key_grant(struct S2 *p_context, uint8_t include, uint8_t keys,uint8_t csa);

/** @brief This function requests a controller as including node to setup a secure relationship
 *         with a joining node.
 *
 *  @details  This will start the S2 add node process. This should be called on the
 *            ADD_NODE_STATUS_DONE event.
 *
 *            The s2 event handler will be called
 *            multiple times during the add process.
 *            \li When the device requests keys
 *            \li When the public key of the including controller/slave has been received
 *            \li When inclusion has completed either with
 *            success or failure. See \ref zwave_event_codes_t.
 *
 *            If the including controller wish to abort the inclusion after if has
 *            called \ref s2_inclusion_including_start, it must call \ref s2_inclusion_abort. This should
 *            result in a \ref S2_NODE_INCLUSION_FAILED_EVENT or in some cases a \ref S2_NODE_INCLUSION_COMPLETE_EVENT.
 *
 *            The following figure explains the flow of events under a successful node inclusion.
 *            \msc
 *             Application,libs2;
 *
 *            Application->libs2 [label="s2_inclusion_including_start()"];
 *            Application<-libs2 [label="S2_NODE_INCLUSION_KEX_REPORT_EVENT"];
 *            Application->libs2 [label="s2_inclusion_key_grant()"];
 *            Application<-libs2 [label="S2_NODE_INCLUSION_PUBLIC_KEY_CHALLENGE_EVENT"];
 *            Application->libs2 [label="s2_inclusion_challenge_response()"];
 *            Application<-libs2 [label="S2_NODE_INCLUSION_COMPLETE_EVENT"];
 *            \endmsc
 *
 *
 *  @param[in] p_context Pointer to the context with information of peer to include.
 *  @param[in] p_peer    The peer to include
 */
void s2_inclusion_including_start(struct S2 *p_context, s2_connection_t * p_peer);
#endif

/** @brief This function requests a controller/slave to continue a secure inclusion as joining
 *         node.
 *
 *  @details  This will start the S2 learn process. This function should be called when
 *            Z-Wave learnmode is complete, ie when the node got the LEARN_MODE_DONE event.
 *            See INS13478 for details.
 *
 *            The s2 event handler will be called
 *            multiple times during the inclusion. Once when the public key of the including
 *            controller has been received, and once inclusion has completed either with
 *            success or failure. See \ref zwave_event_codes_t.
 *
 *            \note that action must be taken on \ref S2_NODE_INCLUSION_PUBLIC_KEY_CHALLENGE_EVENT
 *
 *            The following figure explains the flow of events when the node is successfully included
 *             by another controller.
 *            @msc
 *            Application,libs2;
 *            Application->libs2 [label="s2_inclusion_joining_start()"];
 *            Application<-libs2 [label="S2_NODE_INCLUSION_PUBLIC_KEY_CHALLENGE_EVENT"];
 *            Application->libs2 [label="s2_inclusion_challenge_response()"];
 *            Application<-libs2 [label="S2_NODE_JOINING_COMPLETE_EVENT"];
 *            @endmsc
 *
 *  @param[in] p_context    Pointer to the context with information of peer to join.
 *  @param[in] p_connection Reference to including node for controllers. Set to NULL in slaves
 *                          and call S2_set_inclusion_peer() later.
 *  @param[in] csa          If true client side authentication will be used.
 */
void s2_inclusion_joining_start(struct S2 *p_context,s2_connection_t *p_connection, uint8_t csa);

/** @brief This function notifies a joining node that neighbor discovering is complete.
 *
 *  @param[in] p_context    Pointer to the context with information of peer to join.
 */
void s2_inclusion_neighbor_discovery_complete(struct S2 *p_context);

/** @brief This function aborts current inclusion process.
 *
 *  @details On nodes supporting both Security 0 and Security 2 secure learn mode, both secure
 *           learn modes may be initiated. In the event that including node continues with
 *           Security 0 inclusion, then @ref s2_inclusion_abort can be used to abort Security 2
 *           inclusion.
 *
 *  @param[in] p_context Pointer to the context with information of peer to join.
 */
void s2_inclusion_abort(struct S2 *p_context);

/**@brief Function for handling timeouts from the timer.
 *
 * @param[in] ctxt       Structure identifying the context for current inclusion.
 */
#ifdef __C51__
void ZCB_s2_inclusion_notify_timeout(struct S2 *p_context);
#else
void s2_inclusion_notify_timeout(struct S2* ctxt);
#endif


/** Function for initializing the S2 inclusion state machine with supported schemes/curves/keys.
 *
 * @details All bit fields used in the parameter list are fully described in the SDS11274 document.
 *
 * @param[in] schemes Bit fields specifying supported schemes in the system.
 *                    Currently only scheme 1 exists.
 *                    Bit 1: Security 1 support. See @ref SECURITY_2_SCHEME_1_SUPPORT.
 * @param[in] curves  Bit fields specifying supported curves in the system.
 *                    Bit 0: Curve25519 support. See @ref KEX_REPORT_CURVE_25519.
 * @param[in] keys    Bit fields specifying supported key classes in the system.
 *                    Bit 0: Security 2 - Class 0 support. See @ref SECURITY_2_KEY_2_CLASS_0.
 *                    Bit 1: Security 2 - Class 1 support. See @ref SECURITY_2_KEY_2_CLASS_1.
 *                    Bit 7: Security 0 support. See @ref SECURITY_2_KEY_0.
 *
 * @retval 0 On success
 * @retval KEX_FAIL_SCHEME If the value for schemes is invalid.
 * @retval KEX_FAIL_CURVES If the value for curves is invalid.
 * @retval KEX_FAIL_KEYS   If the value for keys is invalid.
 */
uint8_t s2_inclusion_init(uint8_t schemes, uint8_t curves, uint8_t keys);

/*************************************************************************************************
 * Below are functions defined which is used be S2 inclusion but must be implemented (wrapped)   *
 * outside S2 library depending on the platform for which the S2 library is being compiled.      *
 * An example is the ZW_timer API on embedded nodes.                                             *
 *************************************************************************************************/

/**@brief Function to set a timeout value from the S2 inclusion handling.
 *
 * @details Must be implemented elsewhere maps to ZW_TimerStart. Note that this must start the same
 *          timer every time. i.e. two calls to this function must reset the timer to a new value.
 *          On timout @ref S2_inclusion_timeout_event must be called.
 *
 * @param[in] S2       Structure identifying the context for current inclusion.
 * @param[in] timeout  Timeout value in 10 ms resolution. Thus a value of 50 correspond to a
 *                     timeout of 500 ms (50 * 10 ms).
 *
 * @return handle of timer which must be used for cancellation of timer.
 */
extern uint8_t s2_inclusion_set_timeout(struct S2* ctxt, uint32_t timeout);


/**
 * @}
 */

#endif // _S2_INCLUSION_H_
