package com.coverflow.player;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.IBinder;

public class BgService extends Service {
    private static final String CH = "cfp_playback";
    private static final String A_PLAY = "cfp.play";
    private static final String A_PAUSE = "cfp.pause";
    private static final String A_NEXT = "cfp.next";
    private static final String A_PREV = "cfp.prev";

    private MediaSession session;
    private String title = "Cover Flow Player";
    private String text = "";
    private boolean playing = true;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        session = new MediaSession(this, "CoverFlowPlayer");
        session.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        session.setCallback(new MediaSession.Callback() {
            @Override public void onPlay() { BgPlugin.dispatch("play"); }
            @Override public void onPause() { BgPlugin.dispatch("pause"); }
            @Override public void onSkipToNext() { BgPlugin.dispatch("next"); }
            @Override public void onSkipToPrevious() { BgPlugin.dispatch("prev"); }
        });
        session.setActive(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;
        if (action != null) {
            // нажатие кнопки в уведомлении: передаём в плеер, состояние вернётся из JavaScript
            if (A_PLAY.equals(action)) BgPlugin.dispatch("play");
            else if (A_PAUSE.equals(action)) BgPlugin.dispatch("pause");
            else if (A_NEXT.equals(action)) BgPlugin.dispatch("next");
            else if (A_PREV.equals(action)) BgPlugin.dispatch("prev");
            return START_STICKY;
        }
        if (intent != null) {
            if (intent.getStringExtra("title") != null) title = intent.getStringExtra("title");
            if (intent.getStringExtra("text") != null) text = intent.getStringExtra("text");
            playing = intent.getBooleanExtra("playing", true);
        }
        show();
        return START_STICKY;
    }

    private PendingIntent act(String action, int code) {
        Intent i = new Intent(this, BgService.class);
        i.setAction(action);
        return PendingIntent.getService(this, code, i, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void show() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(new NotificationChannel(CH, "Playback", NotificationManager.IMPORTANCE_LOW));
        }
        Intent open = new Intent(this, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, open, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        session.setMetadata(new MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, title)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, text)
                .build());
        session.setPlaybackState(new PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PAUSE | PlaybackState.ACTION_PLAY_PAUSE
                        | PlaybackState.ACTION_SKIP_TO_NEXT | PlaybackState.ACTION_SKIP_TO_PREVIOUS)
                .setState(playing ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED,
                        PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                .build());

        Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(this, CH) : new Notification.Builder(this);
        b.setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentIntent(pi)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setShowWhen(false)
                .setOngoing(playing);
        b.addAction(new Notification.Action.Builder(android.R.drawable.ic_media_previous, "Previous", act(A_PREV, 1)).build());
        if (playing) {
            b.addAction(new Notification.Action.Builder(android.R.drawable.ic_media_pause, "Pause", act(A_PAUSE, 2)).build());
        } else {
            b.addAction(new Notification.Action.Builder(android.R.drawable.ic_media_play, "Play", act(A_PLAY, 3)).build());
        }
        b.addAction(new Notification.Action.Builder(android.R.drawable.ic_media_next, "Next", act(A_NEXT, 4)).build());
        b.setStyle(new Notification.MediaStyle()
                .setMediaSession(session.getSessionToken())
                .setShowActionsInCompactView(0, 1, 2));
        Notification n = b.build();

        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(1, n);
        }
        if (!playing) {
            // на паузе уведомление остаётся (с кнопкой «играть»), но его можно смахнуть
            stopForeground(false);
            nm.notify(1, n);
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        if (session != null) {
            session.setActive(false);
            session.release();
        }
        super.onDestroy();
    }
}
