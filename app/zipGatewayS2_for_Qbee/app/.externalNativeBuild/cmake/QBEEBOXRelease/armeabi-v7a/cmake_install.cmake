# Install script for directory: D:/Project/00016_CHT/chiapin_ZipGatewayS2_with_bridge_dongle/app

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
    set(CMAKE_INSTALL_CONFIG_NAME "Release")
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
  include("D:/Project/00016_CHT/chiapin_ZipGatewayS2_with_bridge_dongle/app/.externalNativeBuild/cmake/QBEEBOXRelease/armeabi-v7a/src/main/cpp/libusb/cmake_install.cmake")
  include("D:/Project/00016_CHT/chiapin_ZipGatewayS2_with_bridge_dongle/app/.externalNativeBuild/cmake/QBEEBOXRelease/armeabi-v7a/src/main/cpp/openssl-1.0.2l/cmake_install.cmake")
  include("D:/Project/00016_CHT/chiapin_ZipGatewayS2_with_bridge_dongle/app/.externalNativeBuild/cmake/QBEEBOXRelease/armeabi-v7a/src/main/cpp/zipgateway_android/cmake_install.cmake")
  include("D:/Project/00016_CHT/chiapin_ZipGatewayS2_with_bridge_dongle/app/.externalNativeBuild/cmake/QBEEBOXRelease/armeabi-v7a/src/main/cpp/zwcontrol/cmake_install.cmake")

endif()

if(CMAKE_INSTALL_COMPONENT)
  set(CMAKE_INSTALL_MANIFEST "install_manifest_${CMAKE_INSTALL_COMPONENT}.txt")
else()
  set(CMAKE_INSTALL_MANIFEST "install_manifest.txt")
endif()

string(REPLACE ";" "\n" CMAKE_INSTALL_MANIFEST_CONTENT
       "${CMAKE_INSTALL_MANIFEST_FILES}")
file(WRITE "D:/Project/00016_CHT/chiapin_ZipGatewayS2_with_bridge_dongle/app/.externalNativeBuild/cmake/QBEEBOXRelease/armeabi-v7a/${CMAKE_INSTALL_MANIFEST}"
     "${CMAKE_INSTALL_MANIFEST_CONTENT}")
