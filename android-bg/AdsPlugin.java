package com.coverly.app;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.adsbase.StartAppSDK;

/** Небольшой рекламный баннер Start.io внизу экрана. Размер сообщается странице, чтобы она освободила место. */
@CapacitorPlugin(name = "StartIo")
public class AdsPlugin extends Plugin {
    private Banner banner;
    private int lastHeight = -1;

    @PluginMethod
    public void show(final PluginCall call) {
        final boolean testing = Boolean.TRUE.equals(call.getBoolean("testing", false));
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    StartAppSDK.setTestAdsEnabled(testing);
                    ViewGroup content = getActivity().findViewById(android.R.id.content);
                    if (banner == null) {
                        banner = new Banner(getActivity());
                        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
                        content.addView(banner, lp);
                        banner.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                            @Override
                            public void onLayoutChange(View v, int l, int t, int r, int b, int oldL, int oldT, int oldR, int oldB) {
                                report();
                            }
                        });
                    }
                    banner.setVisibility(View.VISIBLE);
                    place();
                    call.resolve();
                } catch (Throwable e) {
                    call.reject(String.valueOf(e.getMessage()));
                }
            }
        });
    }

    @PluginMethod
    public void hide(final PluginCall call) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (banner != null) banner.setVisibility(View.GONE);
                notifyHeight(0);
                call.resolve();
            }
        });
    }

    /** Поднимает баннер над системной панелью навигации, если содержимое окна заходит под неё. */
    private void place() {
        final ViewGroup content = getActivity().findViewById(android.R.id.content);
        content.post(new Runnable() {
            @Override
            public void run() {
                try {
                    WindowInsetsCompat w = ViewCompat.getRootWindowInsets(content);
                    int inset = w != null ? w.getInsets(WindowInsetsCompat.Type.systemBars()).bottom : 0;
                    int[] loc = new int[2];
                    content.getLocationInWindow(loc);
                    int gap = Math.max(0, content.getRootView().getHeight() - (loc[1] + content.getHeight()));
                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) banner.getLayoutParams();
                    lp.bottomMargin = Math.max(0, inset - gap);
                    banner.setLayoutParams(lp);
                } catch (Throwable ignored) {
                }
            }
        });
    }

    private void report() {
        if (banner == null) return;
        int h = banner.getVisibility() == View.VISIBLE ? banner.getHeight() : 0;
        notifyHeight(Math.round(h / getContext().getResources().getDisplayMetrics().density));
    }

    private void notifyHeight(int dp) {
        if (dp == lastHeight) return;
        lastHeight = dp;
        JSObject o = new JSObject();
        o.put("height", dp);
        notifyListeners("bannerSize", o, true);
    }
}
