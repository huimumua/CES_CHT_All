# Install script for directory: D:/Project/00016_CHT/chiapin_ZipGatewayS2/app/src/main/cpp/zipgateway/libs2/inclusion/test

# Set the install prefix
if(NOT DEFINED CMAKE_INSTALL_PREFIX)
  set(CMAKE_INSTALL_PREFIX "C:/Program Files (x86)/Project")
endif()
string(REGEX REPLACE "/$" "" CMAKE_INSTALL_PREFIX "${CMAKE_INSTALL_PREFIX}")

# Set the install configuration name.
if(NOT DEFINED CMAKE_INSTALL_CONFIG_NAME)
  if(BUILD_TYPE)
    string(REGEX REPLACE "^[^A-Za-z0-9_]+" ""
           CMAKE_INSTALL_CONFIG_NAME "${BUILD_TYPE}")
  else()
    set(CMAKE_INSTALL_CONFIG_NAME "Debug")
  endif()
  message(STATUS "Install configuration: \"${CMAKE_INSTALL_CONFIG_NAME}\"")
endif()

# Set the component getting installed.
if(NOT CMAKE_INSTALL_COMPONENT)
  if(COMPONENT)
    message(STATUS "Install component: \"${COMPONENT}\"")
    set(CMAKE_INSTALL_COMPONENT "${COMPONENT}")
  else()
    set(CMAKE_INSTALL_COMPONENT)
  endif()
endif()

# Install shared libraries without execute permission?
if(NOT DEFINED CMAKE_INSTALL_SO_NO_EXE)
  set(CMAKE_INSTALL_SO_NO_EXE "0")
endif()

if("${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/test" TYPE EXECUTABLE FILES "D:/Project/00016_CHT/chiapin_ZipGatewayS2/app/.externalNativeBuild/cmake/debug/armeabi-v7a/src/main/cpp/zipgateway/libs2/inclusion/test/test_inclusion_including_node")
  if(EXISTS "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/test/test_inclusion_including_node" AND
     NOT IS_SYMLINK "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/test/test_inclusion_including_node")
    if(CMAKE_INSTALL_DO_STRIP)
      execute_process(COMMAND "D:/Android-ndk-r14b/toolchains/arm-linux-androideabi-4.9/prebuilt/windows-x86_64/bin/arm-linux-androideabi-strip.exe" "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/test/test_inclusion_including_node")
    endif()
  endif()
endif()

if("${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/test" TYPE EXECUTABLE FILES "D:/Project/00016_CHT/chiapin_ZipGatewayS2/app/.externalNativeBuild/cmake/debug/armeabi-v7a/src/main/cpp/zipgateway/libs2/inclusion/test/test_inclusion_joining_node_slave")
  if(EXISTS "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/test/test_inclusion_joining_node_slave" AND
     NOT IS_SYMLINK "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/test/test_inclusion_joining_node_slave")
    if(CMAKE_INSTALL_DO_STRIP)
      execute_process(COMMAND "D:/Android-ndk-r14b/toolchains/arm-linux-androideabi-4.9/prebuilt/windows-x86_64/bin/arm-linux-androideabi-strip.exe" "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/test/test_inclusion_joining_node_slave")
    endif()
  endif()
endif()

if("${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/test" TYPE EXECUTABLE FILES "D:/Project/00016_CHT/chiapin_ZipGatewayS2/app/.externalNativeBuild/cmake/debug/armeabi-v7a/src/main/cpp/zipgateway/libs2/inclusion/test/test_inclusion_joining_node_controller")
  if(EXISTS "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/test/test_inclusion_joining_node_controller" AND
     NOT IS_SYMLINK "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/test/test_inclusion_joining_node_controller")
    if(CMAKE_INSTALL_DO_STRIP)
      execute_process(COMMAND "D:/Android-ndk-r14b/toolchains/arm-linux-androideabi-4.9/prebuilt/windows-x86_64/bin/arm-linux-androideabi-strip.exe" "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/test/test_inclusion_joining_node_controller")
    endif()
  endif()
endif()

if("${CMAKE_INSTALL_COMPONENT}" STREQUAL "Unspecified" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/test" TYPE EXECUTABLE FILES "D:/Project/00016_CHT/chiapin_ZipGatewayS2/app/.externalNativeBuild/cmake/debug/armeabi-v7a/src/main/cpp/zipgateway/libs2/inclusion/test/test_inclusion")
  if(EXISTS "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/test/test_inclusion" AND
     NOT IS_SYMLINK "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/test/test_inclusion")
    if(CMAKE_INSTALL_DO_STRIP)
      execute_process(COMMAND "D:/Android-ndk-r14b/toolchains/arm-linux-androideabi-4.9/prebuilt/windows-x86_64/bin/arm-linux-androideabi-strip.exe" "$ENV{DESTDIR}${CMAKE_INSTALL_PREFIX}/test/test_inclusion")
    endif()
  endif()
endif()

