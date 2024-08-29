cmake_minimum_required(VERSION 3.0)
project(test)
set (CMAKE_CXX_STANDARD ${SystemC_CXX_STANDARD})

set (SystemC_include_path "/usr/local/systemc-2.3.3/include/")
set (SystemC_AMS_include_path "/usr/local/systemc-ams-2.3/include")
set (SystemC_lib_path "/usr/local/systemc-2.3.3/lib-linux64/libsystemc.a")
set (SystemC_AMS_lib_path "/usr/local/systemc-ams-2.3/lib-linux64/libsystemc-ams.a")

add_executable(test main.cpp modules/transmitter_CLASS.cpp modules/wirelessDevice_CLASS.cpp modules/receiver_CLASS.cpp )
target_include_directories(test PRIVATE ${SystemC_include_path})
target_include_directories(test PRIVATE ${SystemC_AMS_include_path})
target_link_libraries(test ${SystemC_lib_path} ${SystemC_AMS_lib_path})