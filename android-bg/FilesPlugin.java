package com.coverly.app;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Base64;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/** Сохранение файла резервной копии: в папку «Загрузки/Coverly» (Android 10+) или в папку приложения (старые версии). */
@CapacitorPlugin(name = "CoverlyFiles")
public class FilesPlugin extends Plugin {
    private OutputStream out;
    private Uri uri;
    private String where = "";

    @PluginMethod
    public void saveBegin(PluginCall call) {
        try {
            closeQuietly();
            String name = call.getString("name", "Coverly-backup.zip");
            String mime = call.getString("mime", "application/zip");
            uri = null;
            if (Build.VERSION.SDK_INT >= 29) {
                ContentValues v = new ContentValues();
                v.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
                v.put(MediaStore.MediaColumns.MIME_TYPE, mime);
                v.put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/Coverly");
                v.put(MediaStore.MediaColumns.IS_PENDING, 1);
                ContentResolver r = getContext().getContentResolver();
                uri = r.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v);
                if (uri == null) {
                    call.reject("cannot create file");
                    return;
                }
                out = r.openOutputStream(uri);
                where = "Download/Coverly/" + name;
            } else {
                File dir = getContext().getExternalFilesDir(null);
                if (dir == null) dir = getContext().getFilesDir();
                File f = new File(dir, name);
                out = new FileOutputStream(f);
                where = f.getAbsolutePath();
            }
            call.resolve();
        } catch (Exception e) {
            call.reject(String.valueOf(e.getMessage()));
        }
    }

    @PluginMethod
    public void saveChunk(PluginCall call) {
        try {
            String data = call.getString("data");
            if (out == null || data == null) {
                call.reject("no open file");
                return;
            }
            out.write(Base64.decode(data, Base64.DEFAULT));
            call.resolve();
        } catch (Exception e) {
            call.reject(String.valueOf(e.getMessage()));
        }
    }

    @PluginMethod
    public void saveEnd(PluginCall call) {
        try {
            if (out != null) {
                out.flush();
                out.close();
                out = null;
            }
            if (uri != null) {
                ContentValues v = new ContentValues();
                v.put(MediaStore.MediaColumns.IS_PENDING, 0);
                getContext().getContentResolver().update(uri, v, null, null);
            }
            JSObject r = new JSObject();
            r.put("path", where);
            call.resolve(r);
        } catch (Exception e) {
            call.reject(String.valueOf(e.getMessage()));
        }
    }

    private void closeQuietly() {
        try {
            if (out != null) out.close();
        } catch (Exception ignored) {
        }
        out = null;
    }
}
