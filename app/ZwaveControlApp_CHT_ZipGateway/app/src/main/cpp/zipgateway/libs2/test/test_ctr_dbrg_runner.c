 
/* AUTOGENERATED FILE. DO NOT EDIT. */


  #include "unity.h"
  #include "unity_print.h"
#ifndef __codasip__
  #include "string.h"
#endif /* __codasip__ */


int verbose;

void  test_ctr_dbrg();


#ifdef __C51__
#include "reg51.h"

void setUp() {
#if 1
SCON  = 0x50;                   /* SCON: mode 1, 8-bit UART, enable rcvr    */
TMOD |= 0x20;                   /* TMOD: timer 1, mode 2, 8-bit reload      */
TH1   = 0xf3;                   /* TH1:  reload value for 2400 baud         */
TR1   = 1;                      /* TR1:  timer 1 run                        */
TI    = 1;                      /* TI:   set TI to send first char of UART  */
#else
  int bBaudRate;
  WATCHDOG_DISABLE;
  bBaudRate = 1152;
  bBaudRate = (80000/bBaudRate ) + (((80000 %bBaudRate ) >= (bBaudRate >> 1))  ? 1:0);
  UART0_SET_BAUD_RATE(68);
  UART0_TX_ENABLE;
  
  OPEN_IOS
  UART0BUF = '*';
#endif

}

void tearDown() {

}

extern int main(void) {
    int i;
    int ret;
    
    verbose=0;
    unity_print_init();
    UNITY_BEGIN();


    UnityDefaultTestRun(&test_ctr_dbrg, "test_ctr_dbrg",  344 );

    ret = UNITY_END();
    unity_print_close();
    while(1);
}
#else
int main(int argc, char** argp) {
    int ret;
    unity_print_init();
    UNITY_BEGIN();


    UnityDefaultTestRun(&test_ctr_dbrg, "test_ctr_dbrg",  344 );

    ret = UNITY_END();
    unity_print_close();
    return ret;
}
#endif


