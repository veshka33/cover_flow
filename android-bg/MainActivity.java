package com.coverflow.player;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(BgPlugin.class);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            getBridge().getWebView().onResume();
            getBridge().getWebView().resumeTimers();
        } catch (Exception ignored) {}
    }
}
