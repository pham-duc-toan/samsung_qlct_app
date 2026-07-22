# Quản Lý Chi Tiêu (Expense Manager)

Ứng dụng Android quản lý thu chi cá nhân — viết bằng **Java**, dữ liệu lưu **offline bằng SQLite**.
Không dùng backend, không dùng Firebase, không cần internet.

- **Ngôn ngữ:** Java
- **minSdk:** 26 (Android 8.0) · **compileSdk/targetSdk:** 34
- **Lưu trữ:** SQLite (`SQLiteOpenHelper`) — toàn bộ dữ liệu nằm trên máy
- **UI:** Material Components, giao diện tối giản, bo góc, gradient, biểu đồ tròn tự vẽ, **hỗ trợ Dark Mode**
- **Cài đặt:** lưu bằng `SharedPreferences` (hạn mức chi tiêu + chế độ giao diện)

## Tính năng

- **Tổng quan (Home):** thẻ số dư gradient, tổng thu / tổng chi, **thẻ hạn mức chi tiêu tháng**
  (thanh tiến trình đổi màu xanh → cam → đỏ khi gần/vượt hạn mức), danh sách giao dịch gần đây.
- **Giao dịch:** xem toàn bộ giao dịch theo từng tháng (nút chuyển tháng), nhóm theo ngày
  (“Hôm nay”, “Hôm qua”, ngày cụ thể), kèm tổng thu/chi của tháng.
  **Có ô tìm kiếm** (theo ghi chú/danh mục) và **bộ lọc** Tất cả / Thu / Chi.
- **Thống kê:** biểu đồ tròn (donut) chi tiêu theo danh mục + danh sách phần trăm,
  và **biểu đồ cột thu–chi 6 tháng gần đây** (tự vẽ, có animation).
- **Thêm / Sửa / Xóa:** chọn loại (chi/thu), nhập số tiền, chọn danh mục (lưới icon nhiều màu),
  ghi chú, chọn ngày. Sửa và xóa ngay trên cùng màn hình.
- **Cài đặt:** đặt **hạn mức tháng** + **hạn mức theo từng danh mục**, bật **nhắc nhở nhập chi tiêu
  hằng ngày** (thông báo theo giờ tự chọn), đổi **giao diện Sáng / Tối / Theo hệ thống**,
  **xuất CSV** (hộp chia sẻ của Android), và **xóa toàn bộ dữ liệu**.
- Có sẵn **vài giao dịch mẫu** khi mở lần đầu để xem giao diện (tạo trong `DatabaseHelper.seedSampleData`).

## Cách chạy

1. Mở **Android Studio** (khuyến nghị bản Giraffe/Hedgehog trở lên) → **Open** → chọn thư mục dự án này.
2. Chờ Android Studio **Gradle Sync** (lần đầu sẽ tự tải Gradle 8.2 và các thư viện — cần internet lúc build).
3. Chọn thiết bị/emulator API 26+ rồi bấm **Run ▶**.

> Nếu chạy bằng dòng lệnh, hãy để Android Studio tạo Gradle wrapper trước (Gradle → wrapper),
> hoặc dùng Gradle 8.2 cài sẵn: `gradle assembleDebug`.

## Cấu trúc thư mục

```
app/src/main/
├── java/com/example/expensemanager/
│   ├── ExpenseApp.java              # Application: áp theme + tạo notification channel
│   ├── MainActivity.java            # Khung 1 Activity + BottomNav (4 tab) + FAB
│   ├── AddTransactionActivity.java  # Thêm / sửa / xóa giao dịch
│   ├── CategoryBudgetActivity.java  # Đặt hạn mức cho từng danh mục
│   ├── model/                       # Transaction, Category
│   ├── db/                          # DatabaseHelper (SQLite: CRUD + tổng hợp + mẫu)
│   ├── adapter/                     # Transaction / Stat / CategoryPick / CategoryBudget
│   ├── fragment/                    # Home, Transactions, Stats, Settings
│   ├── view/
│   │   ├── PieChartView.java        # Biểu đồ tròn (donut) tự vẽ
│   │   └── BarChartView.java        # Biểu đồ cột 6 tháng tự vẽ
│   ├── receiver/
│   │   ├── ReminderReceiver.java    # Hiện thông báo nhắc nhở
│   │   └── BootReceiver.java        # Đặt lại lịch nhắc sau khi khởi động lại máy
│   └── util/
│       ├── CurrencyUtil, DateUtil   # Định dạng tiền VNĐ / ngày tháng
│       ├── Prefs.java               # SharedPreferences: hạn mức, giao diện, nhắc nhở
│       ├── AppDialogs.java          # Hộp thoại nhập số tiền / hạn mức
│       ├── CsvExporter.java         # Xuất CSV + chia sẻ qua FileProvider
│       └── ReminderScheduler.java   # AlarmManager + notification channel
└── res/
    ├── layout/                      # activity_*, fragment_*, item_*, dialog_budget
    ├── drawable/                    # icon vector + nền bo góc, gradient
    ├── values/                      # colors, strings (tiếng Việt), themes, dimens
    ├── values-night/               # colors.xml cho Dark Mode
    ├── xml/                         # file_paths (FileProvider) + backup rules
    └── mipmap-anydpi-v26/           # icon launcher (adaptive)
```

## Ghi chú kỹ thuật

- Danh mục để **cố định trong code** (`Category.java`) cho gọn: mỗi danh mục có key, tên, icon, màu.
  Muốn thêm danh mục mới chỉ cần thêm 1 dòng `register(...)` và 1 icon + 1 màu.
- Số tiền lưu dạng **số dương**, hướng thu/chi phân biệt qua trường `type`.
- **Hạn mức (tháng + từng danh mục), giao diện, cài đặt nhắc nhở** lưu bằng `SharedPreferences`
  (file `settings`), không đụng tới SQLite.
- **Nhắc nhở hằng ngày** dùng `AlarmManager.setInexactRepeating` + `BroadcastReceiver`; thông báo qua
  `NotificationChannel` (tạo trong `ExpenseApp`). Android 13+ sẽ xin quyền `POST_NOTIFICATIONS` khi bật;
  `BootReceiver` đặt lại lịch sau khi khởi động lại máy.
- **Biểu đồ** (tròn & cột) đều là `View` tự vẽ bằng `Canvas`, không dùng thư viện chart ngoài.
- **Dark Mode** dùng theme `DayNight` + `values-night/colors.xml`; chuyển bằng `AppCompatDelegate.setDefaultNightMode`.
- **Xuất CSV** ghi vào `getExternalFilesDir/exports/quan_ly_chi_tieu.csv` rồi chia sẻ qua `FileProvider`
  (khai báo trong `AndroidManifest.xml` + `res/xml/file_paths.xml`).
- Đổi package: sửa `namespace`/`applicationId` trong `app/build.gradle` và tên thư mục `java/...`.
- Xóa dữ liệu mẫu: bỏ lời gọi `seedSampleData(db)` trong `DatabaseHelper.onCreate` rồi gỡ app cài lại.
