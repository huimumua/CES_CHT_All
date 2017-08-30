/****************************************************************************
 *
 * Copyright (c) 2001-2013
 * Sigma Designs, Inc.
 * All Rights Reserved
 *
 *---------------------------------------------------------------------------
 *
 * Description: Z-Wave Slave node application interface
 *
 * Author:   Ivar Jeppesen
 *
 * Last Changed By:  $Author: jsi $
 * Revision:         $Revision: 34915 $
 * Last Changed:     $Date: 2016-11-29 16:16:27 +0100 (ti, 29 nov 2016) $
 *
 ****************************************************************************/
#ifndef _ZW_SLAVE_API_H_
#define _ZW_SLAVE_API_H_

#ifndef ZW_SLAVE
#define ZW_SLAVE
#endif

/****************************************************************************/
/*                              INCLUDE FILES                               */
/****************************************************************************/
#include <ZW_basis_api.h>


/****************************************************************************/
/*                     EXPORTED TYPES and DEFINITIONS                       */
/****************************************************************************/

/* ApplicationcSlaveUpdate status */
#define UPDATE_STATE_NODE_INFO_RECEIVED     0x84
#define UPDATE_STATE_NOP_POWER_RECEIVED     0x83

/*Defines used to handle inclusion and exclusion of node*/
#define ASSIGN_COMPLETE           0x00
#define ASSIGN_NODEID_DONE        0x01  /*Node ID have been assigned*/
#define ASSIGN_RANGE_INFO_UPDATE  0x02  /*Node is doing Neighbor discovery*/

/* Mode parameters to ZW_SetLearnMode */
#define ZW_SET_LEARN_MODE_DISABLE                       0x00
#define ZW_SET_LEARN_MODE_CLASSIC                       0x01
#define ZW_SET_LEARN_MODE_NWI                           0x02
#define ZW_SET_LEARN_MODE_NWE                           0x03


/*===========================   ZW_SetLearnMode   ===========================
**    Enable/Disable home/node ID learn mode.
**    When learn mode is enabled, received "Assign ID's Command" are handled:
**    If the current stored ID's are zero, the received ID's will be stored.
**    If the received ID's are zero the stored ID's will be set to zero.
**
**    The learnFunc is called when the received assign command has been handled.
**    The returned parameter is the learned Node ID.
**
** void           RET  Nothing
** ZW_SetLearnMode(
** BYTE mode,                IN  learnMode bitmask
** VOID_CALLBACKFUNC(learnFunc)(BYTE)); IN  Node learn call back function.
**--------------------------------------------------------------------------*/
#define ZW_SET_LEARN_MODE(mode, func) ZW_SetLearnMode(mode, func)


/*===========================   ZW_SetDefault   ================================
**    Remove all Nodes and timers from the EEPROM memory.
**    Reset the homeID and nodeID
**    Side effects:
**
**--------------------------------------------------------------------------*/
#define ZW_SET_DEFAULT     ZW_SetDefault


/****************************************************************************/
/*                              EXPORTED DATA                               */
/****************************************************************************/

/****************************************************************************/
/*                           EXPORTED FUNCTIONS                             */
/*                 Implemented within the application moduls                */
/****************************************************************************/


/*==========================   ApplictionSlaveUpdate   =======================
**   Inform a slave application that a node information is received.
**   Called from the slave command handler when a node information frame
**   is received and the Z-Wave protocol is not in a state where it is needed.
**
**--------------------------------------------------------------------------*/

/**
 * \ingroup COMMON
 * The Z Wave protocol MAY notify a slave application by calling
 * \ref ApplicationSlaveUpdate when a Node Information Frame has been received.
 * The Z Wave protocol MAY refrain from calling the function if the protocol is
 * currently expecting node information.
 *
 * All slave libraries requires this function implemented by the application.
 *
 * Declared in: ZW_slave_api.h
 *
 * \param[in] bStatus The status, value could be one of the following:
 *  - UPDATE_STATE_NODE_INFO_RECEIVED A node has sent its Node Info while the
 *                                    Z Wave protocol is idle.
 * \param[in] bNodeID The updated node's node ID (1..232).
 * \param[in] pCmd Pointer of the updated node's node info.
 * \param[in] bLen  The length of the pCmd parameter.
 * \serialapi{ZW->HOST: REQ | 0x49 | bStatus | bNodeID | bLen | basic | generic | specific | commandclasses[ ]}
 *
 */
extern void
ApplicationSlaveUpdate(
  BYTE bStatus,     /*IN  Status event */
  BYTE bNodeID,     /*IN  Node id of the node that send node info */
  BYTE* pCmd,       /*IN  Pointer to Application Node information */
  BYTE bLen);       /*IN  Node info length                        */


/****************************************************************************/
/*                           EXPORTED FUNCTIONS                             */
/*                 Implemented within the Z-Wave slave modules              */
/****************************************************************************/


/*===========================   ZW_SetLearnMode   ===========================
**    Enable/Disable home/node ID learn mode.
**    When learn mode is enabled, received "Assign ID's Command" are handled:
**    If the current stored ID's are zero, the received ID's will be stored.
**    If the received ID's are zero the stored ID's will be set to zero.
**
**    The learnFunc is called when the received assign command has been handled.
**
**--------------------------------------------------------------------------*/
extern void         /*RET  Nothing        */
ZW_SetLearnMode(
  BYTE mode,                                       /* IN  learnMode bitmask */
  VOID_CALLBACKFUNC(learnFunc)(BYTE bStatus, BYTE nodeID));  /*IN  Node learn call back function. */


/*===========================   ZW_SetDefault   ================================
**    Reset the slave to its default state.
**    Delete all routes in routing slave
**    Reset the homeID and nodeID
**    Side effects:
**
**--------------------------------------------------------------------------*/
void           /*RET  Nothing        */
ZW_SetDefault(void);

#endif /* _ZW_SLAVE_API_H_ */

