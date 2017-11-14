#!/usr/bin/env bash
sed -i "" 's/com.*.R/com.keyboard.font.theme.emoji.R/g' config.xml
java -jar ./AndResGuard*.jar ../build/outputs/apk/grin/release/app-grin-armeabi-release.apk -config config.xml -signature ../KeyboardArts zdhszyzs zdhszyzs keyboardarts -out ../build/outputs/apk/grin/release/app-grin-zip -7zip ./SevenZip-osx-x86_64.exe -zipalign ./zipalign
