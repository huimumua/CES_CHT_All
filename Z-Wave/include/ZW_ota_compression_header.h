/*************************************************************************** 
* 
* Copyright (c) 2016
* Sigma Designs, Inc. 
* All Rights Reserved 
* 
*--------------------------------------------------------------------------- 
* 
* Description: Definition of the compression header format for OTA updates.
* 
* Author:   Jakob Buron
* 
****************************************************************************/
#ifndef ZW_OTA_COMPRESSION_HEADER_H_
#define ZW_OTA_COMPRESSION_HEADER_H_
#include <ZW_stdint.h>
#include <ZW_typedefs.h>
#include "ZW_firmware_descriptor.h"
#include "ZW_firmware_bootloader_defs.h"

#define PACKED

/* Compression Header types */
#define COMPRESSION_HEADER_TYPE_V1 0x80    /* V1 using FastLZ compression. Arbitrary numbering, not starting from zero because NVM could be initialized to that value*/

//#define ANYSIZE_ARRAY 1

/* Header preceeding the compressed firmware. */
typedef struct s_compressedFirmwareHeader
{
  /* All fields are BIG ENDIAN */
  /* Type of the following header and compression format*/
  uint8_t compressionHeaderType;
  /* Total length of compressed data */
  uint32_t compressedLength;
  /* CRC16 covering this header and the compressed data */
  uint16_t compressedCrc16;
  /* CRC16 covering the uncompressed image.
   * Can be compared to the firmwaredescriptor ApplicationImageCrcValue in code flash. */
  uint16_t uncompressedCrc16;
  /* The scrambling key used for scrambling security keys in the compressed firmware */
  uint8_t scramblingKey[16];
  /* Variable length compressed data follows next */
  uint16_t firmwareDescriptorChecksum;
  /* uint8_t compressedData[ANYSIZE_ARRAY]; */
} PACKED t_compressedFirmwareHeader;

// Used for asserting struct is indeed packed
#define COMPRESSED_FIRMWARE_HEADER_PACKED_LENGTH 27

#endif /* ZW_OTA_COMPRESSION_HEADER_H_ */
