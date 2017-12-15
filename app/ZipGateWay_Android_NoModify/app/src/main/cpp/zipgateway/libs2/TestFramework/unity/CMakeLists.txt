
CROSS_SYSTEM_DIR("." "" CROSS_SYSTEM_DIR)

SET(SOURCES
  unity.c
  ${CROSS_SYSTEM_DIR}/unity_print.c
)

# unity target may be imported be several projects, e.g. 700 series and libs2 simultaneously.
# This test ensures that the target named 'unity' will only be declared if it is not already declared.
if (NOT TARGET unity)
  add_library(unity ${SOURCES} )
  set_target_properties(unity PROPERTIES COMPILE_DEFINITIONS "UNITY_OUTPUT_CHAR=ZW_putchar")
  target_include_directories(unity PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}")
endif (NOT TARGET unity)

