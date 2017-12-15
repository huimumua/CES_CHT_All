/*
 * parse_config.h
 *
 *  Created on: Jan 14, 2011
 *      Author: aes
 */

#ifndef PARSE_CONFIG_H_
#define PARSE_CONFIG_H_
#include "ZIP_Router.h"


/**
 * Platform dependent routine which fills the \ref router_config structure
 * */
void ConfigInit();


void config_update(const char* key, const char* value);
#endif /* PARSE_CONFIG_H_ */
