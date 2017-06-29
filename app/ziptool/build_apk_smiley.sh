#!/usr/bin/env bash
sed -i "" 's/com.*.R/com.smartkeyboard.emoji.R/g' config_smiley.xml
java -jar ./AndResGuard*.jar ../build/outputs/apk/app-smiley-release.apk -config config_smiley.xml -out ../build/outputs/apk/app-smiley-zip
