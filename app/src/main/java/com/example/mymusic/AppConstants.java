package com.example.mymusic;

// Quản lý tập trung hằng số dùng trong Activity và Service.
public final class AppConstants {

    private AppConstants() {
        // Không tạo đối tượng từ class chỉ chứa hằng số.
    }

    public static final String CHANNEL_ID = "music_playback";
    public static final String ACTION_STOP = "com.example.mymusic.STOP";
    public static final String ACTION_STATE_CHANGED = "com.example.mymusic.STATE_CHANGED";

    public static final int NOTIFICATION_ID = 1;
    public static final int REQUEST_NOTIFICATION_PERMISSION = 1;
    public static final int REQUEST_OPEN_PLAYER = 0;
    public static final int REQUEST_STOP_PLAYER = 1;

    public static final float ALPHA_ENABLED = 1f;
    public static final float ALPHA_DISABLED = 0.45f;
}
