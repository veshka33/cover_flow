#!/bin/bash
# Откат патча, если сборка с ним не удалась.
D=android/app/src/main/java/com/coverflow/player
M=android/app/src/main/AndroidManifest.xml
rm -f "$D/BgService.java" "$D/BgPlugin.java"
[ -f /tmp/bgbackup/MainActivity.java ] && cp /tmp/bgbackup/MainActivity.java "$D/MainActivity.java"
[ -f /tmp/bgbackup/AndroidManifest.xml ] && cp /tmp/bgbackup/AndroidManifest.xml "$M"
echo "background patch reverted"
