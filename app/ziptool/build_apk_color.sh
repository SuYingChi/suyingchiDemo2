#!/usr/bin/env bash
sed -i "" 's/com.*.R/com.keyboard.colorkeyboard.R/g' config_color.xml
java -jar ./AndResGuard*.jar ../build/outputs/apk/app-color-release.apk -config config_color.xml -out ../build/outputs/apk/app-color-zip
