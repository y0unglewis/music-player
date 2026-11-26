# My Music — Android Service thực hành

Bài thực hành Java + XML: kết hợp Started Service và Bound Service phát nhạc bằng MediaPlayer. Không cần API, Internet khi phát nhạc hoặc database.

## Chạy project

1. Clone repository: `git clone https://github.com/y0unglewis/music-player.git`.
2. Android Studio > File > Open > thư mục music-player chứa settings.gradle.
3. Cài SDK Platform 35, sử dụng Gradle JDK 17, Sync và Run app. Gradle Wrapper 8.9 đã có sẵn; AGP 8.7.3, minSdk 24, targetSdk 34.
4. Windows: `gradlew.bat :app:assembleDebug`. Linux/macOS: `./gradlew :app:assembleDebug`.
5. Android 13+: cấp quyền thông báo khi nhấn Play để thấy notification.

## Các file quan trọng

| File | Nội dung |
|---|---|
| MainActivity.java | Intent, startService(), stopService(), bindService(), unbindService(), ServiceConnection và UI |
| AppConstants.java | Action của Intent, channel ID, notification ID, request code và alpha dùng chung |
| MyMusicService.java | extends Service, MediaPlayer, play(), pause(), resume(), stopPlayback(), onCreate(), onStartCommand(), onDestroy(), onBind() |
| AndroidManifest.xml | Khai báo Activity, Service và quyền foreground media playback |
| activity_main.xml | Giao diện Play/Pause/Resume/Stop |
| res/raw/rain_sound.ogg | Bản ghi tiếng mưa thật, phát lặp |
| res/drawable và res/values | Hình đĩa nhạc, icon, theme, chuỗi và màu |

## Quy ước code

Xem [AGENTS.md](AGENTS.md) để nắm các quy tắc Java + XML, AppConstants, onCreate và Intent áp dụng cho bài thực hành. Bài này chỉ gửi lệnh cho Service, không có đối tượng dữ liệu cần truyền qua putExtra.

## Note bài giảng

- Service là Android component không có UI riêng, không mặc định chạy trên Background Thread.
- Foreground Service có notification; Background Service không có notification foreground và chịu giới hạn từ Android 8.
- startService(intent) -> onCreate() lần đầu -> onStartCommand() mỗi lần start -> RUNNING -> stopService(intent) hoặc stopSelf() -> onDestroy() khi dừng bình thường.
- onStart() của Service đã deprecated. onStart() của Activity vẫn hợp lệ.
- MyMusicService extends Service, không extends MediaPlayer. MediaPlayer là đối tượng hỗ trợ phát âm thanh.
- play() gọi MediaPlayer.create(), setLooping(true), start(). stopPlayback() dừng và release() player; pause() giữ vị trí, resume() gọi start().
- onBind() trả LocalBinder, Activity lấy đối tượng Service để điều khiển pause()/resume().
- Giữ startService() từ Activity đang hiển thị theo yêu cầu bài tập. Trong ứng dụng thực tế Android khuyến nghị startForegroundService() khi khởi chạy foreground service trên API 26+, rồi gọi startForeground().
- MediaPlayer.create() chuẩn bị đồng bộ; với dữ liệu nặng nên dùng prepareAsync() hoặc giải pháp phù hợp.

## Luồng thực hiện

```java
Intent intent = new Intent(this, MyMusicService.class);
startService(intent);
```

Dừng:

```java
stopService(new Intent(this, MyMusicService.class));
```

Service gọi startForeground() và play(). Activity bind trong onStart() và unbind trong onStop(); Started Service vẫn phát khi Activity rời màn hình. Pause/Resume gọi trực tiếp qua LocalBinder. Stop gọi stopPlayback() rồi stopService(), vì Service có thể vẫn còn bound và chưa chạy onDestroy(). BroadcastReceiver dùng ContextCompat.RECEIVER_NOT_EXPORTED để cập nhật UI; không dùng biến static cho trạng thái phát.

## Checklist kiểm thử trên thiết bị

- [ ] Mở app: Đã dừng, Play sáng, Stop mờ.
- [ ] Play: nghe nhạc, trạng thái Đang phát, notification xuất hiện nếu được cấp quyền.
- [ ] Pause: âm thanh dừng tạm, trạng thái Tạm dừng, nút đổi thành Resume.
- [ ] Resume: phát tiếp từ vị trí cũ, nút đổi thành Pause.
- [ ] Bấm Home: nhạc vẫn phát nếu đang Play; vẫn tạm dừng nếu đang Pause.
- [ ] Quay lại app: trạng thái đúng; Stop dừng nhạc, xóa notification.
- [ ] Play lại: không phát chồng.
- [ ] Stop từ notification: đồng bộ UI.
- [ ] Logcat lọc MyMusicService: thấy onCreate, onBind, onStartCommand, onUnbind, onDestroy.

Chưa kiểm thử emulator tại thời điểm đưa source lên GitHub. Bài demo không triển khai audio focus, MediaSession, playlist, seek hay khôi phục tiến trình.

Tài liệu: https://developer.android.com/develop/background-work/services và https://developer.android.com/media/platform/mediaplayer/basics

## Nguồn âm thanh

`rain_sound.ogg` là bản ghi tiếng mưa `calpo_rain.ogg` của Pablo Jiménez Mateo, phát hành theo [CC0 1.0](https://creativecommons.org/publicdomain/zero/1.0/). [Nguồn và giấy phép](https://github.com/pablojimenezmateo/Rainyrainsounds#sound-licenses). File âm thanh giữ nguyên nội dung, chỉ đổi tên.
