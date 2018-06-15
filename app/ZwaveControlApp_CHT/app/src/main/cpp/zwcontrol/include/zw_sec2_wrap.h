#ifndef _ZW_SEC2_WRAP_H
#define _ZW_SEC2_WRAP_H

#include "S2.h"

void sec2_init(uint32_t homeID);
void sec2_destroy();
void sec2_start_add_node();
void sec2_abort_join();

#endif
