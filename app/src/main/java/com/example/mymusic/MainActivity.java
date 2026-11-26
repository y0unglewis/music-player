package com.example.mymusic;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
    private TextView status;
    private Button btnPlay, btnStop;

    // Nhận thông báo khi Service thay đổi trạng thái, kể cả khi dừng từ notification.
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
        btnStop = findViewById(R.id.btnStop);
    }

    private void setupListeners() {
        btnPlay.setOnClickListener(v -> playMusic());
        btnStop.setOnClickListener(v -> stopMusic());
    }

    private void playMusic() {
        // Intent chỉ định Service cần khởi chạy, giống cách mở Activity.
        Intent intent = new Intent(this, MyMusicService.class);
        startService(intent); // Gọi từ Activity đang hiển thị, đúng yêu cầu bài tập.
        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
    }

    private void stopMusic() {
        Intent intent = new Intent(this, MyMusicService.class);
        stopService(intent); // Android gọi onDestroy() của Service.
    }

    private void updateUI() {
        boolean playing = MyMusicService.isPlaying();
        status.setText(playing ? R.string.status_playing : R.string.status_stopped);
        btnPlay.setEnabled(!playing);
        btnStop.setEnabled(playing);
        btnPlay.setAlpha(playing ? 0.45f : 1f);
        btnStop.setAlpha(playing ? 1f : 0.45f);
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(MyMusicService.ACTION_STATE_CHANGED);
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(musicReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(musicReceiver, filter);
        }
        updateUI();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(musicReceiver);
        super.onStop();
        // Không dừng Service ở đây: nhạc vẫn phát khi rời màn hình.
    }
}
