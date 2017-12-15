
add_definitions( -DUNIT_TEST )

#include_directories( ../crypto/aes )
#add_executable( test_ccm test_ccm.c ../crypto/ccm/ccm.c ../crypto/aes/aes.c )
#add_test( test_ccm test_ccm )

link_directories(${NDK_LIBS_DIR})

# Add test for protocol
unity_test(test_protocol)
add_executable(test_protocol test_protocol_runner.c test_protocol.c )
target_link_libraries(test_protocol s2_controller s2crypto aes unity)

# Add test for AES-CMAC
add_executable(test_aes_cmac test_aes_cmac.c test_aes_cmac_runner.c)
target_link_libraries(test_aes_cmac s2crypto aes unity)
unity_test(test_aes_cmac)

# Add test for Curve25519
include_directories(.)
add_executable(test_curve25519 wc_util.c test_curve25519.c test_curve25519_runner.c)
target_link_libraries(test_curve25519 s2crypto aes unity)
unity_test(test_curve25519)

# Add test for CCM
add_executable(test_ccm test_ccm.c test_ccm_runner.c ../crypto/ccm/ccm.c ../crypto/aes/aes.c)
#target_link_libraries(test_ccm s2crypto unity)
target_link_libraries(test_ccm unity)
unity_test(test_ccm)

if(NOT ${CMAKE_SYSTEM_NAME} MATCHES "C51" AND NOT ${CMAKE_PROJECT_NAME} MATCHES "zipgateway")
 	add_definitions( -DNEW_TEST_T2 )
 	add_executable(new_test_t2 new_test_t2.c ../transport_service/transport_service2.c ../transport_service/transport2_fsm.c)
 	include_directories( ../transport_service/ ../include/ )
 	target_link_libraries(new_test_t2)
endif()

add_definitions( -DRANDLEN=64 )
add_executable(test_ctr_dbrg test_ctr_dbrg_runner.c test_ctr_dbrg.c ../crypto/ctr_drbg/ctr_drbg.c ../crypto/aes/aes.c)
target_link_libraries(test_ctr_dbrg unity)
unity_test(test_ctr_dbrg)

add_executable(test_kderiv test_kderiv_runner.c test_kderiv.c ../crypto/kderiv/kderiv.c ../crypto/aes-cmac/aes_cmac.c ../crypto/aes/aes.c)
target_link_libraries(test_kderiv unity)
unity_test(test_kderiv)




# Disabling unit test for now. Not sure if it works on C51.
#add_definitions ( -DNO_MEM_FUNCTIONS )
#add_definitions ( -DLINUX_TEST )
#add_executable( test_key_scramble test_key_scramble.c ../crypto/key_scramble/ZW_s2_key_scrambler.c ../crypto/aes/aes.c)
#target_link_libraries(test_key_scramble) 
