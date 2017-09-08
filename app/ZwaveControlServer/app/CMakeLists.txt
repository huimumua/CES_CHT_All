# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

add_library(usbserial-jni SHARED src/main/cpp/usbserial-jni.cpp)

add_subdirectory("src/main/cpp/openssl-1.0.1p")
add_subdirectory("src/main/cpp/cjson")
add_subdirectory("src/main/cpp/zwcontrol")

include_directories(src/main/cpp)
include_directories(src/main/cpp/zwcontrol/include)
include_directories(…/include/arm-linux-androideabi)

add_definitions (-DOS_LINUX)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             zwcontrol-jni

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/zwcontrol-jni.cpp )


# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

target_link_libraries(usbserial-jni ${log-lib})

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       zwcontrol-jni

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} zwcontrol)