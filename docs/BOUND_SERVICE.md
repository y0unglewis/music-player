# Bound Service — Pause / Resume

Theo slide and4 (1).pdf, trang 3–4 mô tả Started và Bound Service, trang 6 minh họa LocalBinder. Slide không cung cấp logic pause/resume hoàn chỉnh; phần MediaPlayer dưới đây là áp dụng cho project.

## 1. MyMusicService.java
- LocalBinder extends Binder: getService() trả chính MyMusicService.this.
- onBind(Intent): trả binder, thay cho null.
- pause(): player.pause(), giữ MediaPlayer và vị trí đang phát.
- resume(): player.start(), phát tiếp.
- stopPlayback(): stop(), release(), xóa notification và cập nhật trạng thái.

## 2. MainActivity.java
- ServiceConnection: onServiceConnected nhận IBinder, ép về LocalBinder và lấy Service.
- onStart: bindService(intent, connection, BIND_AUTO_CREATE).
- onStop: unbindService(connection). Không dừng Started Service.
- togglePauseResume: gọi trực tiếp musicService.pause() hoặc musicService.resume().
- Play vẫn dùng startService(); Stop gọi stopPlayback() trước stopService(), vì còn binding thì onDestroy() chưa chắc chạy ngay.
- ContextCompat.registerReceiver(..., RECEIVER_NOT_EXPORTED) sửa Lint flag và tương thích API cũ. AndroidX Core 1.13.1 khai báo trong app/build.gradle.

## 3. Kiểm thử
Play → Pause → Resume → Stop; Pause rồi Home và mở lại; Play rồi Home và mở lại; Stop từ notification lúc đang bound hoặc unbound; xoay màn hình khi đang phát; Logcat lọc MyMusicService. Chưa xác nhận build/emulator.

## 4. Câu hỏi
- Vì sao vừa startService vừa bindService? Started để tiếp tục chạy ngoài Activity; Bound để Activity gọi phương thức điều khiển.
- Pause khác Stop thế nào? Pause giữ MediaPlayer và vị trí; Stop giải phóng và lần Play sau bắt đầu lại.
- Khi unbind thì Service chết không? Chỉ khi không còn client và không còn trạng thái started.
- Binder có tạo Background Thread không? Không. LocalBinder trong cùng process gọi trực tiếp trên luồng gọi.
