add_definitions( -DUNIT_TEST )

unity_test(test_mock_framework)
add_executable(test_mock_framework test_mock_framework.c test_mock_framework_runner.c example_test_mock.c)
target_link_libraries(test_mock_framework unity mock)

