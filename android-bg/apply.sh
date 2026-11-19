#!/bin/bash
# Экспериментальный патч: фоновое воспроизведение на Android (foreground service).
set -e
D=android/app/src/main/java/com/coverflow/player
M=android/app/src/main/AndroidManifest.xml
mkdir -p /tmp/bgbackup
cp "$D/MainActivity.java" /tmp/bgbackup/MainActivity.java
cp "$M" /tmp/bgbackup/AndroidManifest.xml
cp android-bg/MainActivity.java android-bg/BgPlugin.java android-bg/BgService.java "$D/"
python3 - <<'P'
p='android/app/src/main/AndroidManifest.xml'
s=open(p,encoding='utf-8').read()
perms='''    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
'''
svc='''        <service android:name=".BgService" android:exported="false" android:foregroundServiceType="mediaPlayback" />
'''
assert '<application' in s and '</application>' in s
s=s.replace('<application',perms+'    <application',1)
s=s.replace('</application>',svc+'    </application>',1)
open(p,'w',encoding='utf-8').write(s)
P
echo "background patch applied"
