package com.example.mymusic;

public final class AppConstants {

    private AppConstants() {
    }

    public static final String CHANNEL_ID = "music_playback";
    public static final int NOTIFICATION_ID = 1;
    public static final int REQUEST_NOTIFICATION_PERMISSION = 1;

    public static final int STATE_STOPPED = 0;
    public static final int STATE_PLAYING = 1;
    public static final int STATE_PAUSED = 2;
    public static final int STATE_RESUMED = 3;
}
