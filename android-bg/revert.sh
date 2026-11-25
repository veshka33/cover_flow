#!/bin/bash
# Откат патча, если сборка с ним не удалась.
D=android/app/src/main/java/com/coverly/app
M=android/app/src/main/AndroidManifest.xml
RES=android/app/src/main/res
rm -f "$D/BgService.java" "$D/BgPlugin.java" "$D/WidgetProvider.java" "$D/WidgetState.java" "$D/FilesPlugin.java"
rm -f "$RES/layout/widget_player.xml" "$RES/drawable/widget_bg.xml" "$RES/xml/widget_info.xml"
[ -f /tmp/bgbackup/MainActivity.java ] && cp /tmp/bgbackup/MainActivity.java "$D/MainActivity.java"
[ -f /tmp/bgbackup/AndroidManifest.xml ] && cp /tmp/bgbackup/AndroidManifest.xml "$M"
echo "background patch reverted"
