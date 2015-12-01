LOCAL_PATH := $(call my-dir)

# Edit this line
# Linux & MAC OSX
GSTREAMER_ROOT_ANDROID := /Users/songhochan/Android/library/gstreamer-1.0-android-arm-release-1.4.4
# Windows
#GSTREAMER_ROOT_ANDROID := D:\Android\AndroidStudioProjects\gstreamer-1.0-android-arm-release-1.4.4

SHELL := PATH=/usr/bin:/bin:/usr/sbin:/sbin:/usr/local/bin:/opt/local/bin/ /bin/bash

include $(CLEAR_VARS)

LOCAL_MODULE    := CarViewStreamer
LOCAL_SRC_FILES := CarViewStreamer.c
LOCAL_SHARED_LIBRARIES := gstreamer_android
LOCAL_LDLIBS := -llog -landroid
include $(BUILD_SHARED_LIBRARY)



ifndef GSTREAMER_ROOT
ifndef GSTREAMER_ROOT_ANDROID
$(error GSTREAMER_ROOT_ANDROID is not defined!)
endif
GSTREAMER_ROOT        := $(GSTREAMER_ROOT_ANDROID)
endif
GSTREAMER_NDK_BUILD_PATH  := $(GSTREAMER_ROOT)/share/gst-android/ndk-build


include $(GSTREAMER_NDK_BUILD_PATH)/plugins.mk
GSTREAMER_PLUGINS         := $(GSTREAMER_PLUGINS_CORE) $(GSTREAMER_PLUGINS_PLAYBACK) $(GSTREAMER_PLUGINS_CODECS) $(GSTREAMER_PLUGINS_NET) $(GSTREAMER_PLUGINS_SYS) $(GSTREAMER_PLUGINS_CODECS_RESTRICTED) $(GSTREAMER_PLUGINS_EFFECTS)
G_IO_MODULES              := gnutls
GSTREAMER_EXTRA_DEPS      := gstreamer-video-1.0

include $(GSTREAMER_NDK_BUILD_PATH)/gstreamer-1.0.mk
