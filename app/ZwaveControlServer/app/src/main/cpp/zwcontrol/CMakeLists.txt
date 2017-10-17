project(zwcontrol C )

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wparentheses -Wno-array-bounds -fPIE")
set(CMAKE_LD_FLAGS "-fPIE")

set(OPENSSL_LIBRARIES "ssl")
set(LIBCRYPTO_LIBRARIES "crypto")

add_subdirectory("src/libs2")

add_definitions (-DOS_LINUX -DFLASH_PROG_API)

include_directories(
      include
      include/zwave
      ../openssl-1.0.1p/include
      ../cjson
      ..
	  .)

set(SRC
    src/zw_api_base.c
    src/zw_api_intf.c
    src/zw_api_network.c
    src/zw_api_util.c
    src/zw_hci_application.c
    src/zw_hci_fl_prog.c
    src/zw_hci_frame.c
    src/zw_hci_platform.c
    src/zw_hci_session.c
    src/zw_hci_transport.c
    src/zw_hci_util.c
    src/zw_poll.c
    src/zw_sec_aes.c
    src/zw_sec_layer.c
    src/zw_sec_random.c
    src/zw_sec2_wrap.c
    src/zw_sec2_keystore.c
    zwcontrol_api.c
    )

add_library(zwcontrol SHARED ${SRC})

find_library(log-lib log)

target_link_libraries(zwcontrol s2_controller ${OPENSSL_LIBRARIES} ${LIBCRYPTO_LIBRARIES} usbserial-jni cjson ${log-lib})
