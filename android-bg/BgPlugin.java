package com.coverflow.player;

import android.content.Intent;
import androidx.core.content.ContextCompat;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "Bg")
public class BgPlugin extends Plugin {
    private static BgPlugin instance;

    @Override
    public void load() {
        instance = this;
    }

    /** Нажатие кнопки в уведомлении / на экране блокировки -> событие "action" в JavaScript. */
    public static void dispatch(String action) {
        if (instance != null) {
            JSObject o = new JSObject();
            o.put("action", action);
            instance.notifyListeners("action", o, true);
        }
    }

    @PluginMethod
    public void start(PluginCall call) {
        try {
            Intent i = new Intent(getContext(), BgService.class);
            i.putExtra("title", call.getString("title", "Cover Flow Player"));
            i.putExtra("text", call.getString("text", ""));
            i.putExtra("playing", call.getBoolean("playing", true));
            ContextCompat.startForegroundService(getContext(), i);
            call.resolve();
        } catch (Exception e) {
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void stop(PluginCall call) {
        getContext().stopService(new Intent(getContext(), BgService.class));
        call.resolve();
    }
}
