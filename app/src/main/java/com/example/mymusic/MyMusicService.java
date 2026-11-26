package com.example.mymusic;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class MyMusicService extends Service {
    private static final String TAG = "MyMusicService";
    private final IBinder binder = new LocalBinder();
    private boolean playing;
    private MediaPlayer player;

    // Mẫu LocalBinder ở trang 6 của slide and4.
    public class LocalBinder extends Binder {
        public MyMusicService getService() {
            return MyMusicService.this;
        }
    }

    public boolean isPlaying() {
        return playing;
    }

    public boolean isPaused() {
        return player != null && !playing;
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
            stopPlayback();
            stopSelf(); // Dừng phần Started Service khi bấm Stop từ notification.
            return START_NOT_STICKY;
        }
        startMusicForeground();
        play();
        return START_NOT_STICKY; // Không tự khởi động lại nhạc khi hệ thống hủy Service.
    }

    // Mẫu Foreground Service ở trang 6: tạo Notification rồi startForeground().
    private void startMusicForeground() {
        Notification notification = createNotification();
        startForeground(AppConstants.NOTIFICATION_ID, notification);
    }

    private void play() {
        if (player != null) return; // Không tạo lại player đang phát hoặc tạm dừng.
        try {
            player = MediaPlayer.create(this, R.raw.rain_sound);
            if (player == null) {
                stopPlayback();
                stopSelf();
                return;
            }
            player.setLooping(true);
            player.setOnErrorListener((mp, what, extra) -> {
                stopPlayback();
                stopSelf();
                return true;
            });
            player.start(); // MediaPlayer không có hàm play(): start() mới là API phát.
            playing = true;
            refreshPlaybackState();
        } catch (RuntimeException e) {
            Log.e(TAG, "Không thể phát nhạc", e);
            stopPlayback();
            stopSelf();
        }
    }

    // Pause giữ nguyên MediaPlayer và vị trí hiện tại.
    public void pause() {
        if (!isPlaying() || player == null) return;
        try {
            player.pause();
            playing = false;
            refreshPlaybackState();
        } catch (IllegalStateException e) {
            Log.e(TAG, "Không thể tạm dừng", e);
            stopPlayback();
            stopSelf();
        }
    }

    // Resume dùng start(), không tạo MediaPlayer mới và không phát lại từ đầu.
    public void resume() {
        if (!isPaused()) return;
        try {
            player.start();
            playing = true;
            refreshPlaybackState();
        } catch (IllegalStateException e) {
            Log.e(TAG, "Không thể tiếp tục phát", e);
            stopPlayback();
            stopSelf();
        }
    }

    private void refreshPlaybackState() {
        getSystemService(NotificationManager.class)
                .notify(AppConstants.NOTIFICATION_ID, createNotification());
        notifyStateChanged();
    }

    public void stopPlayback() {
        if (player != null) {
            try {
                player.stop(); // Dừng cả khi MediaPlayer đang Pause.
            } catch (IllegalStateException e) {
                Log.w(TAG, "MediaPlayer đã ở trạng thái lỗi", e);
            } finally {
                player.release(); // Giải phóng bộ nhớ và tài nguyên âm thanh.
                player = null;
            }
        }
        playing = false;
        stopForeground(STOP_FOREGROUND_REMOVE);
        notifyStateChanged();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        stopPlayback();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return super.onUnbind(intent);
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
        // Dùng NotificationCompat.Builder giống code mẫu của cô.
        Notification notification = new NotificationCompat.Builder(this, AppConstants.CHANNEL_ID)
                .setContentTitle(getString(R.string.track_name))
                .setContentText(getString(isPaused()
                        ? R.string.status_paused : R.string.notification_text))
                .setSmallIcon(R.drawable.ic_music)
                .setContentIntent(open)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .addAction(R.drawable.ic_stop, getString(R.string.stop), stop)
                .build();
        return notification;
    }
}
