#!/usr/bin/env bash
# chmod u+x relink.sh
git submodule update --init
# in app
rm -rf ./libs/libAppFramework.jar
rm -rf ./libs/android-support-v4.jar
rm -rf ./libs/libNativeAdsBase.jar
rm -rf ./libs/libNativeAdsPool.jar
cd libs
ln -s ../../lib_keyboard_android/libs/ihs/libAppFramework/libAppFramework.jar .
ln -s ../../lib_keyboard_android/libs/ihs/libAppFramework/android-support-v4.jar .
ln -s ../../lib_keyboard_android/libs/ihs/libNativeAds/libNativeAdsBase.jar .
ln -s ../../lib_keyboard_android/libs/ihs/libNativeAds/libNativeAdsPool.jar .
#customTheme
cd ../../customTheme/libs
rm -rf ./libs/libAppFramework.jar
rm -rf ./libs/android-support-v4.jar
rm -rf ./libs/libNativeAdsBase.jar
rm -rf ./libs/libNativeAdsPool.jar
ln -s ../../lib_keyboard_android/libs/ihs/libAppFramework/libAppFramework.jar .
ln -s ../../lib_keyboard_android/libs/ihs/libAppFramework/android-support-v4.jar .
ln -s ../../lib_keyboard_android/libs/ihs/libNativeAds/libNativeAdsBase.jar .
ln -s ../../lib_keyboard_android/libs/ihs/libNativeAds/libNativeAdsPool.jar .


#submodule
cd ../../lib_keyboard_android/libKeyboard
rm -rf ./libs/libAppFramework.jar
rm -rf ./libs/android-support-v4.jar
rm -rf ./libs/libNativeAdsBase.jar
rm -rf ./libs/libNativeAdsPool.jar
git submodule update --init
cd libs
ln -s ../../libs/ihs/libAppFramework/libAppFramework.jar .
ln -s ../../libs/ihs/libAppFramework/android-support-v4.jar .
ln -s ../../libs/ihs/libNativeAds/libNativeAdsBase.jar .
ln -s ../../libs/ihs/libNativeAds/libNativeAdsPool.jar .
