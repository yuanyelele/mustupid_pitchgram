LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := pitch_detector
LOCAL_SRC_FILES := pitch_detector.c

include $(BUILD_SHARED_LIBRARY)