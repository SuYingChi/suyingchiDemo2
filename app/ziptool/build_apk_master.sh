#!/usr/bin/env bash
sed -i "" 's/com.*.R/com.masterkeyboard.emoji.R/g' config.xml
java -jar ./AndResGuard*.jar ../build/outputs/apk/app-master-release.apk -config config.xml -signature ../GlowDesign zdhszyzs zdhszyzs glowdesign -out ../build/outputs/apk/app-master-zip -7zip ./SevenZip-osx-x86_64.exe -zipalign ./zipalign
