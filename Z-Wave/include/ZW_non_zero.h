/*******************************  ZW_non_zero.H  *******************************
 *           #######
 *           ##  ##
 *           #  ##    ####   #####    #####  ##  ##   #####
 *             ##    ##  ##  ##  ##  ##      ##  ##  ##
 *            ##  #  ######  ##  ##   ####   ##  ##   ####
 *           ##  ##  ##      ##  ##      ##   #####      ##
 *          #######   ####   ##  ##  #####       ##  #####
 *                                           #####
 *          Z-Wave, the wireless language.
 *
 *              Copyright (c) 2001
 *              Zensys A/S
 *              Denmark
 *
 *              All Rights Reserved
 *
 *    This source file is subject to the terms and conditions of the
 *    Zensys Software License Agreement which restricts the manner
 *    in which it may be used.
 *
 *---------------------------------------------------------------------------
 *
 * Description: This module define an address range in XRAM reserved for uninitialized variables.
 *
 * Author:   Samer Seoud
 *
 * Last Changed By:  $Author: jsi $
 * Revision:         $Revision: 33893 $
 * Last Changed:     $Date: 2016-06-24 15:13:48 +0200 (fr, 24 jun 2016) $
 *
 ****************************************************************************/
#ifndef _ZW_NON_ZERO_H_
#define _ZW_NON_ZERO_H_

/****************************************************************************/
/*                              INCLUDE FILES                               */
/****************************************************************************/

/****************************************************************************/
/*                     EXPORTED TYPES and DEFINITIONS                       */
/****************************************************************************/
#define XDATA_LENGTH	0x1000
#define NON_ZERO_SIZE    64
#define NON_ZERO_START_ADDR   0x1240

#endif /* _ZW_NON_ZERO_H_ */
