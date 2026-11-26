package com.example.mymusic;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

public class MainActivity extends Activity {
    private TextView status;
    private Button btnPlay, btnPauseResume, btnStop;
    private MyMusicService musicService;
    private boolean bindingRequested;

    // Nhận LocalBinder từ onBind() theo mẫu của cô.
    // ServiceConnection là phần cần thêm để Activity sử dụng Binder.
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyMusicService.LocalBinder binder = (MyMusicService.LocalBinder) service;
            musicService = binder.getService();
            updateUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
            updateUI();
        }
    };

    // Cập nhật UI khi Service đổi trạng thái (kể cả Stop từ notification).
    private final BroadcastReceiver musicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        setupListeners();
    }

    private void initViews() {
        status = findViewById(R.id.tvStatus);
        btnPlay = findViewById(R.id.btnPlay);
        btnPauseResume = findViewById(R.id.btnPauseResume);
        btnStop = findViewById(R.id.btnStop);
    }

    private void setupListeners() {
        btnPlay.setOnClickListener(v -> playMusic());
        btnPauseResume.setOnClickListener(v -> togglePauseResume());
        btnStop.setOnClickListener(v -> stopMusic());
    }

    private void playMusic() {
        if (musicService == null) return;
        Intent intent = new Intent(this, MyMusicService.class);
        startService(intent); // Started Service vẫn chạy sau khi Activity unbind.
        requestNotificationPermission();
    }

    private void togglePauseResume() {
        if (musicService == null) return;
        if (musicService.isPlaying()) {
            musicService.pause();
        } else if (musicService.isPaused()) {
            musicService.resume();
        }
        updateUI();
    }

    private void stopMusic() {
        if (musicService == null) return;
        // Service vẫn còn bound, nên phải dừng âm thanh ngay, không chờ onDestroy().
        musicService.stopPlayback();
        stopService(new Intent(this, MyMusicService.class));
        updateUI();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    AppConstants.REQUEST_NOTIFICATION_PERMISSION);
        }
    }

    private void updateUI() {
        boolean connected = musicService != null;
        boolean playing = connected && musicService.isPlaying();
        boolean paused = connected && musicService.isPaused();
        if (playing) {
            status.setText(R.string.status_playing);
        } else if (paused) {
            status.setText(R.string.status_paused);
        } else {
            status.setText(R.string.status_stopped);
        }
        btnPauseResume.setText(paused ? R.string.resume : R.string.pause);
        setButtonEnabled(btnPlay, connected && !playing && !paused);
        setButtonEnabled(btnPauseResume, playing || paused);
        setButtonEnabled(btnStop, playing || paused);
    }

    private void setButtonEnabled(Button button, boolean enabled) {
        button.setEnabled(enabled);
        button.setAlpha(enabled ? AppConstants.ALPHA_ENABLED : AppConstants.ALPHA_DISABLED);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerMusicReceiver();
        connectMusicService();
        updateUI();
    }

    private void connectMusicService() {
        Intent intent = new Intent(this, MyMusicService.class);
        bindingRequested = bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    private void registerMusicReceiver() {
        IntentFilter filter = new IntentFilter(AppConstants.ACTION_STATE_CHANGED);
        ContextCompat.registerReceiver(this, musicReceiver, filter,
                ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onStop() {
        disconnectMusicService();
        unregisterReceiver(musicReceiver);
        super.onStop();
    }

    private void disconnectMusicService() {
        if (bindingRequested) {
            unbindService(connection);
            bindingRequested = false;
        }
        musicService = null;
        // Không stopService() tại đây: nhạc tiếp tục phát khi rời Activity.
    }
}
