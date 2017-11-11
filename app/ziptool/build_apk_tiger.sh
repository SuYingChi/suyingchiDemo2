#!/usr/bin/env bash
sed -i "" 's/com.*.R/com.mobipioneer.emojikeyboard.R/g' config.xml
java -jar ./AndResGuard*.jar ../build/outputs/apk/app-tiger-armeabi-v7a-release.apk -config config.xml -signature ../Mobipioneer zdhszyzs zdhszyzs mobipioneer -out ../build/outputs/apk/app-tiger-zip -7zip ./SevenZip-osx-x86_64.exe -zipalign ./zipalign
