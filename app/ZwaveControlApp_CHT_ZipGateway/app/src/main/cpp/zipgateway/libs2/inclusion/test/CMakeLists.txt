add_definitions( -DUNIT_TEST )

link_directories(${NDK_LIBS_DIR})

unity_test(test_inclusion_including_node)
unity_test(test_inclusion_joining_node)
unity_test(test_inclusion)
# TDOD: Cleanup in future to ensure we build a lib and link to that instead of directly compiling source under test.
#       Outline is presented below.
#add_executable(test_kex test_kex.c test_kex_runner.c)
#target_link_libraries(test_kex kex unity)
# For now build everything together.
add_executable(test_inclusion_including_node ../s2_inclusion.c test_inclusion_including_node.c test_inclusion_including_node_runner.c )
set_target_properties(test_inclusion_including_node PROPERTIES COMPILE_DEFINITIONS "ZW_CONTROLLER")
target_link_libraries(test_inclusion_including_node unity mock s2_mock s2_inclusion_mocks crypto_mocks)

add_executable(test_inclusion_joining_node ../s2_inclusion.c test_inclusion_joining_node.c test_inclusion_joining_node_runner.c )
target_link_libraries(test_inclusion_joining_node unity mock s2_mock s2_inclusion_mocks crypto_mocks)

add_executable(test_inclusion ../s2_inclusion.c test_inclusion.c test_inclusion_runner.c )
set_target_properties(test_inclusion PROPERTIES COMPILE_DEFINITIONS "ZW_CONTROLLER")
target_link_libraries(test_inclusion  unity mock s2_mock s2_inclusion_mocks s2crypto aes)
