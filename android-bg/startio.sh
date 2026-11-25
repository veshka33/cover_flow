#!/bin/bash
# Подключает рекламу Start.io к Android-проекту: зависимость SDK, App ID в манифесте, плагин баннера.
# Можно запускать повторно (ничего не дублирует).
set -e
python3 - <<'P'
import re,shutil
app_id='205489527'   # тестовый App ID Start.io
try:
    s=open('www/ads-config.js',encoding='utf-8').read()
    m=re.search(r"startioAppId\s*:\s*'([^']+)'",s)
    if m: app_id=m.group(1)
except Exception:
    pass
# 1) манифест
p='android/app/src/main/AndroidManifest.xml'
s=open(p,encoding='utf-8').read()
if 'com.startapp.sdk.APPLICATION_ID' not in s:
    meta=('        <meta-data android:name="com.startapp.sdk.APPLICATION_ID" android:value="%s" />\n'
          '        <meta-data android:name="com.startapp.sdk.RETURN_ADS_ENABLED" android:value="false" />\n')%app_id
    assert '</application>' in s
    s=s.replace('</application>',meta+'    </application>',1)
    open(p,'w',encoding='utf-8').write(s)
    print('Start.io App ID added:',app_id)
# 2) зависимость
p='android/app/build.gradle'
s=open(p,encoding='utf-8').read()
if 'com.startapp:inapp-sdk' not in s:
    i=s.index('dependencies {')
    j=s.index('\n',i)+1
    s=s[:j]+"    implementation 'com.startapp:inapp-sdk:5.4.0'\n"+s[j:]
    open(p,'w',encoding='utf-8').write(s)
    print('Start.io SDK dependency added')
# 3) плагин
d='android/app/src/main/java/com/coverly/app'
shutil.copy('android-bg/AdsPlugin.java',d+'/AdsPlugin.java')
# 4) регистрация плагина в MainActivity
p=d+'/MainActivity.java'
s=open(p,encoding='utf-8').read()
if 'AdsPlugin' not in s:
    if 'onCreate' in s:
        s=re.sub(r'(\n\s*)super\.onCreate\(',r'\1registerPlugin(AdsPlugin.class);\1super.onCreate(',s,count=1)
    else:
        s=s.rstrip()
        assert s.endswith('}')
        s=s[:-1]+'''
    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        registerPlugin(AdsPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
'''
    open(p,'w',encoding='utf-8').write(s)
    print('AdsPlugin registered')
P
