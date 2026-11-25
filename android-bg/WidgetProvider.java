package com.coverly.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {
    static final String A_TOGGLE = "cfp.w.toggle";
    static final String A_NEXT = "cfp.w.next";
    static final String A_PREV = "cfp.w.prev";

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) manager.updateAppWidget(id, build(context));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String a = intent != null ? intent.getAction() : null;
        if (A_TOGGLE.equals(a) || A_NEXT.equals(a) || A_PREV.equals(a)) {
            if (BgPlugin.isReady()) {
                BgPlugin.dispatch(A_TOGGLE.equals(a) ? "toggle" : A_NEXT.equals(a) ? "next" : "prev");
            } else {
                // приложение не запущено: просто открываем его
                try {
                    Intent launch = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                    if (launch != null) {
                        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(launch);
                    }
                } catch (Exception ignored) {
                }
            }
            return;
        }
        super.onReceive(context, intent);
    }

    private static PendingIntent click(Context c, String action, int code) {
        Intent i = new Intent(c, WidgetProvider.class);
        i.setAction(action);
        return PendingIntent.getBroadcast(c, code, i, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Bitmap scaled(Bitmap b, int max) {
        if (b == null) return null;
        int w = b.getWidth(), h = b.getHeight();
        if (w <= max && h <= max) return b;
        float k = (float) max / Math.max(w, h);
        return Bitmap.createScaledBitmap(b, Math.max(1, Math.round(w * k)), Math.max(1, Math.round(h * k)), true);
    }

    static RemoteViews build(Context c) {
        WidgetState.load(c);
        RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget_player);
        v.setTextViewText(R.id.w_title, WidgetState.title);
        v.setTextViewText(R.id.w_text, WidgetState.text);
        Bitmap cover = scaled(WidgetState.cover(c), 200);
        if (cover != null) v.setImageViewBitmap(R.id.w_cover, cover);
        v.setImageViewResource(R.id.w_play, WidgetState.playing ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
        v.setOnClickPendingIntent(R.id.w_prev, click(c, A_PREV, 11));
        v.setOnClickPendingIntent(R.id.w_play, click(c, A_TOGGLE, 12));
        v.setOnClickPendingIntent(R.id.w_next, click(c, A_NEXT, 13));
        Intent launch = c.getPackageManager().getLaunchIntentForPackage(c.getPackageName());
        if (launch != null) {
            v.setOnClickPendingIntent(R.id.w_cover,
                    PendingIntent.getActivity(c, 10, launch, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));
        }
        return v;
    }

    public static void refresh(Context c) {
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        int[] ids = m.getAppWidgetIds(new ComponentName(c, WidgetProvider.class));
        for (int id : ids) m.updateAppWidget(id, build(c));
    }
}
