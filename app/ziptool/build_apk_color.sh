#!/usr/bin/env bash
sed -i "" 's/com.*.R/com.keyboard.colorkeyboard.R/g' config.xml
java -jar ./AndResGuard*.jar ../build/outputs/apk/app-color-armeabi-v7a-release.apk -config config.xml -signature ../KeyboardArts zdhszyzs zdhszyzs keyboardarts -out ../build/outputs/apk/color/release/app-color-zip -7zip ./SevenZip-osx-x86_64.exe -zipalign ./zipalign
