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
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
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
        btnPlay.setOnClickListener(v -> {
            playMusic();
            updateButtonColor(btnPlay);
        });
        btnPause.setOnClickListener(v -> {
            pauseMusic();
            updateButtonColor(btnPause);
        });
        btnResume.setOnClickListener(v -> {
            resumeMusic();
            updateButtonColor(btnResume);
        });
        btnStop.setOnClickListener(v -> {
            stopMusic();
            updateButtonColor(btnStop);
        });
    }

    // Tô màu nút vừa bấm, các nút còn lại trở về màu mặc định.
    private void updateButtonColor(Button selectedButton) {
        Button[] buttons = {btnPlay, btnPause, btnResume, btnStop};

        for (Button button : buttons) {
            boolean selected = button == selectedButton;
            int background = getColor(selected ? R.color.primary : R.color.surface);
            int text = getColor(selected ? R.color.on_primary : R.color.text);
            button.setBackgroundTintList(ColorStateList.valueOf(background));
            button.setTextColor(text);
        }
    }

    private void playMusic() {
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
        // Service còn bound nên dừng player trực tiếp trước stopService().
        if (musicService != null) {
            musicService.stop();
        }
        stopService(new Intent(this, MyMusicService.class));
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
