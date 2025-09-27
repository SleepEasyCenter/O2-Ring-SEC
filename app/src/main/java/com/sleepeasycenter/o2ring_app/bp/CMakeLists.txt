cmake_minimum_required(VERSION 3.22.1)
project(bp_native)

add_library(
  bp_native
  SHARED
  bp_bridge.cpp
  mega_bp_step.c
)

# if mega_bp_step.c includes math, link m on some ABIs (often not needed on Android)
target_link_libraries(bp_native android log)
