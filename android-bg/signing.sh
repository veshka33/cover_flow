#!/bin/bash
# Настраивает подписанную релизную сборку (.aab / .apk) и номер версии в android/app/build.gradle.
# Нужны переменные окружения: KEYSTORE_PATH, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD, VERSION_CODE, VERSION_NAME.
set -e
python3 - <<'P'
import re
p='android/app/build.gradle'
s=open(p,encoding='utf-8').read()
if 'signingConfigs' not in s:
    sign='''    signingConfigs {
        release {
            storeFile file(System.getenv("KEYSTORE_PATH") ?: "upload-keystore.jks")
            storePassword System.getenv("KEYSTORE_PASSWORD")
            keyAlias System.getenv("KEY_ALIAS")
            keyPassword System.getenv("KEY_PASSWORD")
        }
    }
'''
    i=s.index('    buildTypes {')
    s=s[:i]+sign+s[i:]
    j=s.index('    buildTypes {')
    k=s.index('release {',j)
    k_end=k+len('release {')
    s=s[:k_end]+'\n            signingConfig signingConfigs.release'+s[k_end:]
s=re.sub(r'versionCode\s+\d+',"versionCode (System.getenv('VERSION_CODE') ?: '1').toInteger()",s,count=1)
s=re.sub(r'versionName\s+"[^"]*"',"versionName (System.getenv('VERSION_NAME') ?: '1.0')",s,count=1)
open(p,'w',encoding='utf-8').write(s)
print('signing + version configured')
P
