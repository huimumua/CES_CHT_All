project(zwapi)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wparentheses -Wno-array-bounds -fPIE -DOS_ANDROID -DOS_LINUX -DZIP_V2 -DUSE_OPENSSL")
set(CMAKE_LD_FLAGS "-fPIE")

set(OPENSSL_LIBRARIES "ssl")
set(LibCRYPTO_LIBRARIES "crypto")

set(OPENSSL_INCLUDE_DIRS "../openssl-1.0.2j/include")

include_directories(
    ${OPENSSL_INCLUDE_DIRS}
    ${CMAKE_CURRENT_SOURCE_DIR}/include
    ${CMAKE_CURRENT_SOURCE_DIR}/include/zwave
)

set(SRC
    src/cjson.c
    src/zip_api_base.c
    src/zip_api_intf.c
    src/zip_api_network.c
    src/zip_api_sm.c
    src/zip_api_util.c
    src/zip_dev_cfg.c
    src/zip_dev_poll.c
    src/zip_event_gen.c
    src/zip_network_save.c
    src/zip_node_sts.c
    src/zip_poll.c
    src/zip_set_poll.c
    src/zip_supervision.c
    src/zip_wkup_poll.c
    lib/zip_application.c
    lib/zip_dtls.c
    lib/zip_frame.c
    lib/zip_mdns.c
    lib/zip_netutil.c
    lib/zip_platform.c
    lib/zip_session.c
    lib/zip_tls_server.c
    lib/zip_transport.c
    lib/zip_util.c
    )

add_library(zwapi SHARED ${SRC})
find_library(log-lib log)
target_link_libraries(zwapi ${OPENSSL_LIBRARIES} ${LibCRYPTO_LIBRARIES} ${log-lib})
