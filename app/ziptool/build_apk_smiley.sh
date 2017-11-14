#!/usr/bin/env bash
sed -i "" 's/com.*.R/com.smartkeyboard.emoji.R/g' config.xml
java -jar ./AndResGuard*.jar ../build/outputs/apk/smiley/release/app-smiley-armeabi-release.apk -config config.xml -signature ../EmojiLabs zdhszyzs zdhszyzs emoji -out ../build/outputs/apk/smiley/release/app-smiley-zip -7zip ./SevenZip-osx-x86_64.exe -zipalign ./zipalign
