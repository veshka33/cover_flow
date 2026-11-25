#!/bin/bash
# Экспериментальный патч: фоновое воспроизведение, кнопки в уведомлении и виджет рабочего стола (Android).
set -e
D=android/app/src/main/java/com/coverly/app
M=android/app/src/main/AndroidManifest.xml
RES=android/app/src/main/res
mkdir -p /tmp/bgbackup
cp "$D/MainActivity.java" /tmp/bgbackup/MainActivity.java
cp "$M" /tmp/bgbackup/AndroidManifest.xml
cp android-bg/MainActivity.java android-bg/BgPlugin.java android-bg/BgService.java android-bg/WidgetProvider.java android-bg/WidgetState.java android-bg/FilesPlugin.java "$D/"
mkdir -p "$RES/layout" "$RES/drawable" "$RES/xml"
cp android-bg/res/layout/widget_player.xml "$RES/layout/"
cp android-bg/res/drawable/widget_bg.xml "$RES/drawable/"
cp android-bg/res/xml/widget_info.xml "$RES/xml/"
python3 - <<'P'
p='android/app/src/main/AndroidManifest.xml'
s=open(p,encoding='utf-8').read()
perms='''    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
'''
svc='''        <service android:name=".BgService" android:exported="false" android:foregroundServiceType="mediaPlayback" />
        <receiver android:name=".WidgetProvider" android:exported="true">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>
            <meta-data android:name="android.appwidget.provider" android:resource="@xml/widget_info" />
        </receiver>
'''
assert '<application' in s and '</application>' in s
s=s.replace('<application',perms+'    <application',1)
s=s.replace('</application>',svc+'    </application>',1)
open(p,'w',encoding='utf-8').write(s)
P
echo "background patch applied"
