package com.example.mymusic;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

public class MyMusicService extends Service {
    private static final String TAG = "MyMusicService";
    private static boolean playing = false;
    private MediaPlayer player;

    public static boolean isPlaying() {
        return playing;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if (intent != null && AppConstants.ACTION_STOP.equals(intent.getAction())) {
            stopSelf(); // Dừng Service khi bấm Stop từ notification.
            return START_NOT_STICKY;
        }
        // Foreground Service cần notification đang hoạt động.
        startForeground(AppConstants.NOTIFICATION_ID, createNotification());
        play();
        return START_NOT_STICKY; // Không tự khởi động lại nhạc khi hệ thống hủy Service.
    }

    private void play() {
        if (playing) return; // Không tạo nhiều MediaPlayer khi nhấn Play liên tục.
        try {
            player = MediaPlayer.create(this, R.raw.rain_sound);
            if (player == null) {
                stopSelf();
                return;
            }
            player.setLooping(true);
            player.setOnErrorListener((mp, what, extra) -> {
                stopSelf();
                return true;
            });
            player.start(); // MediaPlayer không có hàm play(): start() mới là API phát.
            playing = true;
            notifyStateChanged();
        } catch (RuntimeException e) {
            Log.e(TAG, "Không thể phát nhạc", e);
            stopSelf();
        }
    }

    private void stop() {
        if (player != null) {
            try {
                if (player.isPlaying()) player.stop();
            } catch (IllegalStateException e) {
                Log.w(TAG, "MediaPlayer đã ở trạng thái lỗi", e);
            } finally {
                player.release(); // Giải phóng bộ nhớ và tài nguyên âm thanh.
                player = null;
            }
        }
        playing = false;
        notifyStateChanged();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        stop();
        stopForeground(STOP_FOREGROUND_REMOVE);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Bài này là Started Service, không dùng Bound Service.
    }

    private void notifyStateChanged() {
        Intent intent = new Intent(AppConstants.ACTION_STATE_CHANGED);
        intent.setPackage(getPackageName());
        sendBroadcast(intent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(AppConstants.CHANNEL_ID,
                    getString(R.string.channel_name), NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        Intent openIntent = new Intent(this, MainActivity.class);
        openIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent open = PendingIntent.getActivity(this, AppConstants.REQUEST_OPEN_PLAYER, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Intent stopIntent = new Intent(this, MyMusicService.class).setAction(AppConstants.ACTION_STOP);
        PendingIntent stop = PendingIntent.getService(this, AppConstants.REQUEST_STOP_PLAYER, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, AppConstants.CHANNEL_ID) : new Notification.Builder(this);
        return builder.setSmallIcon(R.drawable.ic_music)
                .setContentTitle(getString(R.string.track_name))
                .setContentText(getString(R.string.notification_text))
                .setContentIntent(open)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_LOW)
                .addAction(new Notification.Action.Builder(R.drawable.ic_stop,
                        getString(R.string.stop), stop).build())
                .build();
    }
}
