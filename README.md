# My Music — Bài thực hành Android Service

Java + XML. Phát tiếng mưa offline bằng MediaPlayer; kết hợp Started Service và Bound Service theo slide and4, trang 3–6.

## Chạy project
Clone repo, mở thư mục bằng Android Studio, SDK 35, Gradle JDK 17, Sync và Run. Gradle Wrapper 8.9, AGP 8.7.3, minSdk 24, targetSdk 34.
Windows: `gradlew.bat :app:assembleDebug`.
Android 13+: cấp quyền notification nếu muốn thấy thông báo foreground.

## File chính
- MainActivity.java: startService, stopService, bindService, unbindService, ServiceConnection; bốn nút Play/Pause/Resume/Stop.
- MyMusicService.java: LocalBinder, MediaPlayer, onCreate, onStartCommand, onBind, onDestroy và NotificationCompat.Builder.
- AppConstants.java: channel ID, notification ID, request code.
- AndroidManifest.xml: khai báo Service và quyền foreground.
- activity_main.xml: LinearLayout, TextView, ImageView và bốn Button mặc định, theme xanh lá tối.
- res/raw/rain_sound.ogg: tiếng mưa, phát lặp.

## Luồng
Play gọi startService(); onStartCommand() tạo foreground notification rồi phát âm thanh. Activity kết nối trong onStart và ngắt trong onStop. Pause/Resume gọi trực tiếp phương thức Service qua LocalBinder. Stop gọi musicService.stop() rồi stopService(), vì Service có thể vẫn còn bound. release() dừng và giải phóng MediaPlayer; lần Play sau tạo player mới.

Không dùng BroadcastReceiver, PendingIntent, nút notification hay biến static cho playback. Notification chỉ minh họa Foreground Service theo slide. Không có putExtra vì không cần truyền dữ liệu. Service cập nhật state sau khi thao tác MediaPlayer hợp lệ và gọi một Runnable để Activity cập nhật màu nút. Activity xóa listener khi unbind và đọc lại state khi kết nối. Ban đầu Stop sáng; Pause/Resume trước Play hoặc sau Stop không làm gì và không đổi màu.

## Kiểm thử
Play → Pause → Resume → Stop; Play lại; Home khi đang phát và quay lại; Home khi đang Pause và quay lại; bấm các nút trước Play; xoay màn hình.
Chưa xác nhận build/emulator. Không triển khai MediaSession, audio focus, khôi phục tiến trình hay playlist.

[Quy ước code](AGENTS.md) · [Bound Service theo slide](docs/BOUND_SERVICE.md)

## Nguồn âm thanh
`rain_sound.ogg` là bản ghi `calpo_rain.ogg` của Pablo Jiménez Mateo, giấy phép [CC0 1.0](https://creativecommons.org/publicdomain/zero/1.0/). [Nguồn](https://github.com/pablojimenezmateo/Rainyrainsounds#sound-licenses). Nội dung âm thanh giữ nguyên, chỉ đổi tên.
