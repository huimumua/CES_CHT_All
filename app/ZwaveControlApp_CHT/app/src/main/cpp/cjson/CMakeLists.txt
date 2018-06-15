project(cJSON C)

add_definitions(-DCJSON_EXPORT_SYMBOLS -DCJSON_API_VISIBILITY)

set(CJSON_LIB cjson)

set(SOURCES cJSON.c)

include_directories(".")

add_library("${CJSON_LIB}" SHARED "${SOURCES}")
