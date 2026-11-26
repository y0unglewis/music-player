package com.example.mymusic;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class MyMusicService extends Service {

    private MediaPlayer player;
    private final IBinder binder = new LocalBinder();
    private int state = AppConstants.STATE_STOPPED;
    private Runnable stateListener;

    // Mẫu Bound Service ở trang 6 của slide.
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        public MyMusicService getService() {
            return MyMusicService.this;
        }
    }

    public int getState() {
        return state;
    }

    public void setStateListener(Runnable listener) {
        stateListener = listener;
    }

    // Báo cho Activity cập nhật màu sau khi phát, tạm dừng hoặc dừng nhạc.
    private void changeState(int newState) {
        state = newState;
        if (stateListener != null) {
            stateListener.run();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startMusicForeground();
        play();
        return START_NOT_STICKY;
    }

    // Mẫu Foreground Service ở trang 6 của slide.
    private void startMusicForeground() {
        Notification notification = new NotificationCompat.Builder(this, AppConstants.CHANNEL_ID)
                .setContentTitle(getString(R.string.track_name))
                .setContentText(getString(R.string.notification_text))
                .setSmallIcon(R.drawable.ic_music)
                .build();

        startForeground(AppConstants.NOTIFICATION_ID, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    AppConstants.CHANNEL_ID,
                    getString(R.string.channel_name),
                    NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    private void play() {
        if (player != null) return;

        player = MediaPlayer.create(this, R.raw.rain_sound);
        if (player == null) {
            stopSelf();
            stopForeground(STOP_FOREGROUND_REMOVE);
            return;
        }
        player.setLooping(true);
        player.setOnErrorListener((mp, what, extra) -> {
            stop();
            stopSelf();
            return true;
        });
        player.start();
        changeState(AppConstants.STATE_PLAYING);
    }

    // Tạm dừng nhưng không giải phóng player.
    public void pause() {
        if (player != null && player.isPlaying()) {
            player.pause();
            changeState(AppConstants.STATE_PAUSED);
        }
    }

    // Tiếp tục phát tại vị trí đã tạm dừng.
    public void resume() {
        if (player != null && !player.isPlaying()) {
            player.start();
            changeState(AppConstants.STATE_RESUMED);
        }
    }

    public void stop() {
        if (player != null) {
            // release() cũng dừng âm thanh; luôn giải phóng tài nguyên.
            player.release();
            player = null;
        }
        stopForeground(STOP_FOREGROUND_REMOVE);
        changeState(AppConstants.STATE_STOPPED);
    }

    @Override
    public void onDestroy() {
        stop();
        super.onDestroy();
    }
}
