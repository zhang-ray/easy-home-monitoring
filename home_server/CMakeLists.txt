cmake_minimum_required(VERSION 2.8)

project(home_server)

set (CMAKE_CXX_STANDARD 14)

file(GLOB_RECURSE FILES_SOURCES *.cpp *.c)
file(GLOB_RECURSE FILES_INCLUDE *.hpp *.h)

include_directories(../include/)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)
add_definitions(-DBOOST_LOG_DYN_LINK)



############   fetch git tag   ############
set (git_cmd "git")
#set (git_arg "describe" "--tags")
message(STATUS "git cmd: ${git_cmd}")
execute_process(COMMAND ${git_cmd} ${git_arg}  WORKING_DIRECTORY ${PROJECT_SOURCE_DIR}  RESULT_VARIABLE RESULT_VAR  OUTPUT_VARIABLE GIT_TAG)
message(STATUS "git_result: ${git_result}")
message(STATUS "OUTPUT_VARIABLE: ${GIT_TAG}")
string(REGEX REPLACE "\n$" "" GIT_TAG "${GIT_TAG}") # strip line ending
set (GIT_INFO_HEADER_FILE "../include/git_info.hpp")
file(WRITE ${GIT_INFO_HEADER_FILE} "#pragma once\n")
if (RESULT_VAR STREQUAL 0)
    message(STATUS "RESULT_VAR STREQUAL 0")
    #add_compile_definitions(GIT_TAG=${GIT_TAG}) # need higher cmake
    file(APPEND ${GIT_INFO_HEADER_FILE} "#define GIT_TAG \""  ${GIT_TAG} "\"")
endif()





add_executable(${PROJECT_NAME} ${FILES_INCLUDE} ${FILES_SOURCES})
target_link_libraries(${PROJECT_NAME} boost_system boost_log pthread )
