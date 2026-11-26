# Quy ước code Android — My Music

Áp dụng quy tắc giảng viên đã yêu cầu ở các bài thực hành trước, trong phạm vi bài Service.

- Java + XML; không tự chuyển sang Kotlin, Compose hoặc kiến trúc phức tạp.
- onCreate() ngắn: gọi super, setContentView (Activity), rồi các hàm nhỏ như initViews(), setupListeners(). Tách xử lý theo chức năng, đặt tên dễ hiểu.
- Dùng AppConstants.java cho action/key của Intent, key SharedPreferences (nếu có), ID và request code dùng chung. Truy cập trực tiếp AppConstants.* ở nơi gửi và nhận. Không hardcode key trong putExtra/get...Extra. Các constant Android SDK dùng trực tiếp, ví dụ Build.VERSION_CODES.O.
- Khi có nghiệp vụ truyền object, truyền nguyên object phù hợp qua Intent thay vì tách từng thuộc tính; dùng Serializable/Parcelable khi cần. Bài này không có object cần truyền; không tạo model hoặc extra giả chỉ để tuân thủ quy tắc.
- Dùng startService() từ Activity đang hiển thị theo đề bài, stopService() để dừng; MyMusicService extends Service, dùng MediaPlayer.start()/stop()/release().
- Tài nguyên UI dùng XML: strings.xml, colors.xml, drawable, styles. Giữ giao diện đơn giản, nhất quán; không hardcode text/màu trong layout.
- Tên class PascalCase, hàm/biến camelCase, constant UPPER_SNAKE_CASE. Comment tiếng Việt cho đoạn logic quan trọng; không comment lặp lại từng dòng hiển nhiên.
- Không tự đổi nghiệp vụ, thêm tính năng ngoài đề hoặc xóa source bài cũ. Chỉ sửa file liên quan, bảo toàn lịch sử Git; không force-push/reset-hard.
- Kiểm tra build và chạy emulator trước khi kết luận app hoạt động. Không commit local.properties, build output hoặc thông tin bí mật.

Phần UI hiện dùng Android XML với theme Material có sẵn của hệ thống; không bắt buộc thêm thư viện Material Components chỉ để làm bài Service.
