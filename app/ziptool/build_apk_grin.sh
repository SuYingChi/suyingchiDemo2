#!/usr/bin/env bash
sed -i "" 's/com.*.R/com.keyboard.font.theme.emoji.R/g' config.xml
java -jar ./AndResGuard*.jar ../build/outputs/apk/app-grin-release.apk -config config.xml -signature ../KeyboardArts zdhszyzs zdhszyzs keyboardarts -out ../build/outputs/apk/app-grin-zip -7zip ./SevenZip-osx-x86_64.exe -zipalign ./zipalign
