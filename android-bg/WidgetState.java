package com.coverly.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import java.io.File;
import java.io.FileOutputStream;

/** Последнее состояние плеера для виджета и уведомления (хранится и на диске). */
public class WidgetState {
    static String title = "Coverly";
    static String text = "";
    static boolean playing = false;
    static Bitmap cover = null;
    private static boolean loaded = false;

    private static SharedPreferences sp(Context c) {
        return c.getSharedPreferences("cfp_widget", Context.MODE_PRIVATE);
    }

    static synchronized void load(Context c) {
        if (loaded) return;
        loaded = true;
        SharedPreferences p = sp(c);
        title = p.getString("title", title);
        text = p.getString("text", "");
        playing = p.getBoolean("playing", false);
        File f = new File(c.getFilesDir(), "cover.jpg");
        if (f.exists()) cover = BitmapFactory.decodeFile(f.getAbsolutePath());
    }

    static synchronized void set(Context c, String t, String x, boolean pl) {
        load(c);
        title = t;
        text = x;
        playing = pl;
        sp(c).edit().putString("title", t).putString("text", x).putBoolean("playing", pl).apply();
    }

    static synchronized void setCover(Context c, String b64) {
        load(c);
        File f = new File(c.getFilesDir(), "cover.jpg");
        try {
            if (b64 == null || b64.isEmpty()) {
                cover = null;
                f.delete();
                return;
            }
            byte[] data = Base64.decode(b64, Base64.DEFAULT);
            Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
            if (bm == null) return;
            cover = bm;
            FileOutputStream o = new FileOutputStream(f);
            o.write(data);
            o.close();
        } catch (Exception ignored) {
        }
    }

    static synchronized Bitmap cover(Context c) {
        load(c);
        return cover;
    }
}
