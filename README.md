# Project 01 - Java Socket Chat Application

Hệ thống ứng dụng Chat Client - Server và truyền nhận file qua giao thức TCP Socket bằng ngôn ngữ Java.

---

## 📁 Cấu trúc thư mục (Project Structure)

```text
Project_01_Chat/
│
├── ChatServer/
│   └── src/server/
│       ├── ChatServer.java           # Lớp khởi chạy Server chính
│       ├── ClientHandler.java        # Xử lý kết nối từ từng Client (đa luồng)
│       ├── ConnectionManager.java    # Quản lý danh sách kết nối đang hoạt động
│       ├── UserManager.java          # Quản lý trạng thái tài khoản và người dùng online
│       ├── MessageService.java       # Xử lý logic gửi tin nhắn (Broadcast / Private)
│       ├── FileTransferService.java  # Xử lý truyền nhận file trung gian qua server
│       ├── ServerLogger.java         # Ghi nhật ký hoạt động của Server
│       └── Protocol.java             # Định nghĩa các lệnh giao thức giao tiếp
│
├── ChatClient/
│   └── src/
│       ├── client/
│       │   ├── ChatClient.java       # Lớp khởi chạy Client chính
│       │   ├── ServerConnection.java # Quản lý kết nối socket tới server
│       │   ├── MessageReceiver.java  # Luồng lắng nghe dữ liệu/tin nhắn từ server
│       │   ├── ChatService.java      # Xử lý logic nghiệp vụ chat phía client
│       │   ├── FileSender.java       # Luồng gửi dữ liệu file
│       │   ├── FileReceiver.java     # Luồng nhận và lưu file
│       │   └── Protocol.java         # Định nghĩa các lệnh giao thức giao tiếp
│       └── ui/
│           ├── LoginFrame.java       # Giao diện đăng nhập / kết nối tới server
│           └── ChatFrame.java        # Giao diện phòng chat chính
│
├── docs/
│   ├── architecture/                 # Tài liệu thiết kế kiến trúc hệ thống
│   ├── protocol/                     # Tài liệu đặc tả giao thức mạng
│   ├── test-cases/                   # Các kịch bản và kết quả kiểm thử
│   └── user-guide/                   # Hướng dẫn cài đặt và sử dụng
│
├── logs/                             # Lưu trữ log hệ thống server/client
├── receive_files/                    # Thư mục lưu trữ các file tải về
└── README.md                         # Tài liệu giới thiệu tổng quan dự án
```

---

## 🚀 Tính năng dự kiến

- [x] Kiến trúc Client - Server trên nền tảng Socket (TCP/IP).
- [ ] Đăng nhập và xác thực định danh người dùng.
- [ ] Nhắn tin công khai trong phòng chat chung (Broadcast).
- [ ] Nhắn tin riêng giữa hai người dùng (Private Message / Whisper).
- [ ] Gửi và nhận file đính kèm với thanh tiến trình.
- [ ] Giao diện đồ họa người dùng (Swing UI).
- [ ] Ghi log nhật ký hoạt động hệ thống.
