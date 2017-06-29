#!/usr/bin/env bash
sed -i "" 's/com.*.R/com.masterkeyboard.emoji.R/g' config_master.xml
java -jar ./AndResGuard*.jar ../build/outputs/apk/app-master-release.apk -config config_master.xml -out ../build/outputs/apk/app-master-zip
