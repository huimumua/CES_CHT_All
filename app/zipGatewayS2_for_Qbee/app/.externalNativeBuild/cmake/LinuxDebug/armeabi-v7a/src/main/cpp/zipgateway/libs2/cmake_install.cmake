# Install script for directory: D:/Project/00016_CHT/chiapin_ZipGatewayS2_with_bridge_dongle/app/src/main/cpp/zipgateway/libs2

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

if(NOT CMAKE_INSTALL_LOCAL_ONLY)
  # Include the install script for each subdirectory.
  include("D:/Project/00016_CHT/chiapin_ZipGatewayS2_with_bridge_dongle/app/.externalNativeBuild/cmake/LinuxDebug/armeabi-v7a/src/main/cpp/zipgateway/libs2/TestFramework/cmake_install.cmake")
  include("D:/Project/00016_CHT/chiapin_ZipGatewayS2_with_bridge_dongle/app/.externalNativeBuild/cmake/LinuxDebug/armeabi-v7a/src/main/cpp/zipgateway/libs2/crypto/cmake_install.cmake")
  include("D:/Project/00016_CHT/chiapin_ZipGatewayS2_with_bridge_dongle/app/.externalNativeBuild/cmake/LinuxDebug/armeabi-v7a/src/main/cpp/zipgateway/libs2/protocol/cmake_install.cmake")
  include("D:/Project/00016_CHT/chiapin_ZipGatewayS2_with_bridge_dongle/app/.externalNativeBuild/cmake/LinuxDebug/armeabi-v7a/src/main/cpp/zipgateway/libs2/inclusion/cmake_install.cmake")
  include("D:/Project/00016_CHT/chiapin_ZipGatewayS2_with_bridge_dongle/app/.externalNativeBuild/cmake/LinuxDebug/armeabi-v7a/src/main/cpp/zipgateway/libs2/include/mock/cmake_install.cmake")
  include("D:/Project/00016_CHT/chiapin_ZipGatewayS2_with_bridge_dongle/app/.externalNativeBuild/cmake/LinuxDebug/armeabi-v7a/src/main/cpp/zipgateway/libs2/inclusion/mock/cmake_install.cmake")
  include("D:/Project/00016_CHT/chiapin_ZipGatewayS2_with_bridge_dongle/app/.externalNativeBuild/cmake/LinuxDebug/armeabi-v7a/src/main/cpp/zipgateway/libs2/crypto/mock/cmake_install.cmake")

endif()

