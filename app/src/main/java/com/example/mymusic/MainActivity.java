package com.example.mymusic;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;

public class MainActivity extends Activity {

    private Button btnPlay, btnPause, btnResume, btnStop;
    private MyMusicService musicService;
    private boolean bound;

    // Nhận đối tượng Service thông qua LocalBinder.
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyMusicService.LocalBinder binder = (MyMusicService.LocalBinder) service;
            musicService = binder.getService();
            musicService.setStateListener(() -> updateButtonColor());
            updateButtonColor();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
            updateButtonColor();
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
        btnPlay = findViewById(R.id.btnPlay);
        btnPause = findViewById(R.id.btnPause);
        btnResume = findViewById(R.id.btnResume);
        btnStop = findViewById(R.id.btnStop);
    }

    private void setupListeners() {
        btnPlay.setOnClickListener(v -> playMusic());
        btnPause.setOnClickListener(v -> pauseMusic());
        btnResume.setOnClickListener(v -> resumeMusic());
        btnStop.setOnClickListener(v -> stopMusic());
    }

    // Đổi màu theo trạng thái mà Service đã xử lý.
    private void updateButtonColor() {
        Button selectedButton = btnStop;

        if (musicService != null) {
            int state = musicService.getState();
            if (state == AppConstants.STATE_PLAYING) {
                selectedButton = btnPlay;
            } else if (state == AppConstants.STATE_PAUSED) {
                selectedButton = btnPause;
            } else if (state == AppConstants.STATE_RESUMED) {
                selectedButton = btnResume;
            }
        }

        Button[] buttons = {btnPlay, btnPause, btnResume, btnStop};
        for (Button button : buttons) {
            if (button == selectedButton) {
                button.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.primary)));
                button.setTextColor(getColor(R.color.on_primary));
            } else {
                button.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.surface)));
                button.setTextColor(getColor(R.color.text));
            }
        }
    }

    private void playMusic() {
        if (musicService == null) return;
        Intent intent = new Intent(this, MyMusicService.class);
        startService(intent);
        requestNotificationPermission();
    }

    private void pauseMusic() {
        if (musicService != null) {
            musicService.pause();
        }
    }

    private void resumeMusic() {
        if (musicService != null) {
            musicService.resume();
        }
    }

    private void stopMusic() {
        if (musicService != null) {
            // Service còn bound nên dừng player trước stopService().
            musicService.stop();
            stopService(new Intent(this, MyMusicService.class));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        connectService();
    }

    private void connectService() {
        Intent intent = new Intent(this, MyMusicService.class);
        bound = bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        disconnectService();
        super.onStop();
    }

    private void disconnectService() {
        if (musicService != null) {
            musicService.setStateListener(null);
        }
        if (bound) {
            unbindService(connection);
            bound = false;
        }
        musicService = null;
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    AppConstants.REQUEST_NOTIFICATION_PERMISSION);
        }
    }
}
