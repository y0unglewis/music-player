# Bound Service — Pause / Resume

Slide and4 (1).pdf trang 3–4: Started và Bound Service; trang 6: mẫu LocalBinder và NotificationCompat.Builder. Slide không có code Pause/Resume hoàn chỉnh.

## MyMusicService
```java
private final IBinder binder = new LocalBinder();

@Override
public IBinder onBind(Intent intent) {
    return binder;
}

public class LocalBinder extends Binder {
    public MyMusicService getService() {
        return MyMusicService.this;
    }
}
```

Mẫu Foreground Service trang 6 cũng được giữ trong startMusicForeground(): tạo NotificationCompat.Builder rồi startForeground(AppConstants.NOTIFICATION_ID, notification). NotificationChannel và quyền notification bổ sung để tương thích Android mới.

## MainActivity
ServiceConnection nhận binder trong onServiceConnected và lấy musicService = binder.getService(). bindService() trong onStart; unbindService() trong onStop. Play gọi startService(); Pause và Resume gọi musicService.pause() và musicService.resume() trực tiếp.

## MediaPlayer
- play(): create, setLooping, start.
- pause(): pause, giữ player và vị trí.
- resume(): start lại player cũ.
- stop(): release, đặt player = null, xóa notification. release tự dừng âm thanh.
- onDestroy(): stop để giải phóng tài nguyên.

Stop của Activity gọi stop() rồi stopService(), vì còn binding thì onDestroy() chưa nhất thiết chạy ngay. Started Service tiếp tục chạy khi Activity unbind, trừ khi bị dừng hoặc hệ thống hủy.

UI bốn nút. Service lưu trạng thái bằng các hằng số trong AppConstants và gọi Runnable khi trạng thái đổi; Activity đọc getState() và đổi màu bằng if/else. Listener được gỡ khi unbind. Không dùng BroadcastReceiver. Kiểm thử: Pause/Resume trước Play không đổi màu; Play → Pause → Resume → Stop, sau đó Resume không đổi màu; rời Activity rồi quay lại vẫn hiện đúng trạng thái của Service. Kiểm thử build/emulator chưa thực hiện.
