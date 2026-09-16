# Project 01 - Java Socket Chat & File Transfer Application

Hệ thống ứng dụng Chat Client - Server và Truyền nhận file nhị phân qua giao thức TCP Socket bằng ngôn ngữ Java theo kiến trúc **Hybrid (Napster / Skype Model)**.

---

## 📁 Cấu trúc thư mục (Project Structure)

```text
Project_01_Chat/
│
├── ChatServer/
│   └── src/server/
│       ├── ChatServer.java           # Khởi chạy Server chính (Port 8888 & Port 8889)
│       ├── ClientHandler.java        # Xử lý kết nối từ từng Client (đa luồng Worker)
│       ├── ConnectionManager.java    # Quản lý danh sách kết nối đang hoạt động
│       ├── UserManager.java          # Quản lý trạng thái tài khoản và người dùng online
│       ├── FileTransferService.java  # Dịch vụ truyền nhận file trung gian qua cổng 8889
│       ├── ServerLogger.java         # Ghi nhật ký hoạt động của Server
│       └── Protocol.java             # Định nghĩa giao thức chuẩn giao tiếp (Protocol)
│
├── ChatClient/
│   └── src/
│       ├── client/
│       │   ├── ChatClient.java       # Khởi chạy ứng dụng Client GUI
│       │   ├── ChatService.java      # Nghiệp vụ Chat & File (tách biệt Socket khỏi UI)
│       │   ├── ChatEventListener.java# Interface callback sự kiện mạng cho GUI
│       │   ├── ServerConnection.java # Quản lý kết nối TCP Socket cấp thấp
│       │   ├── MessageReceiver.java  # Luồng ngầm liên tục lắng nghe tin nhắn Server
│       │   ├── FileSender.java       # Luồng gửi dữ liệu file dạng byte[] (Chunking 4KB)
│       │   ├── FileReceiver.java     # Luồng nhận và lưu file vào receive_files/
│       │   └── Protocol.java         # Định nghĩa giao thức chuẩn đồng bộ với Server
│       │
│       └── ui/
│           ├── LoginFrame.java       # Giao diện đăng nhập (Host, Port, Username)
│           └── ChatFrame.java        # Giao diện phòng chat, danh sách online & truyền file
│
├── receive_files/                    # Thư mục lưu trữ các file tải về
├── test_demo.txt                     # File mẫu dùng để test tính năng gửi nhận file
└── README.md                         # Tài liệu hướng dẫn sử dụng và giới thiệu dự án
```

---

## 🚀 Tính năng hoàn thành (100% Hoàn Tất)

- [x] **Kiến trúc Client - Server trên nền tảng TCP Socket.**
- [x] **Xử lý đa luồng (Multithreading):** Server dùng `ExecutorService` (ThreadPool) xử lý hàng trăm Client cùng lúc không nghẽn.
- [x] **Đăng nhập & Xác thực định danh:** Kiểm tra trùng lặp tài khoản, cập nhật trạng thái Online/Offline tức thời.
- [x] **Nhắn tin công khai trong phòng chat chung (Broadcast):** Mọi người dùng cùng phòng đều nhận được.
- [x] **Nhắn tin riêng tư giữa 2 người dùng (Private Message 1-1):** Tách biệt cuộc trò chuyện riêng, không hiển thị lẫn vào phòng chung.
- [x] **Truyền nhận file nhị phân (Binary Stream):**
  - Tách riêng kênh lệnh (Port `8888`) và kênh dữ liệu File (Port `8889`).
  - Đọc/ghi theo từng khối 4KB (`byte[4096]`), không đọc tràn RAM đối với file lớn.
  - Hộp thoại chọn file (`JFileChooser`) và hộp thoại xác nhận nhận file (`JOptionPane`).
  - Tự động lưu file tải về vào thư mục `receive_files/`.
- [x] **Giao diện đồ họa người dùng (Java Swing UI):** Thiết kế trực quan, phân tách hoàn toàn nghiệp vụ mạng và tầng hiển thị.
- [x] **Ghi log nhật ký hoạt động hệ thống (Server Logger).**

---

## 🛠 Hướng dẫn chạy và Demo

1. **Khởi động Server:**
   - Mở `ChatServer/src/server/ChatServer.java` $\rightarrow$ Nhấn **Run**.
   - Server sẽ mở cổng `8888` (Chat & Lệnh) và cổng `8889` (File Data).

2. **Khởi động Client:**
   - Mở `ChatClient/src/client/ChatClient.java` $\rightarrow$ Nhấn **Run**.
   - Nhập thông tin: `Host: localhost`, `Port: 8888`, `Username: Alice` $\rightarrow$ Bấm **Đăng Nhập**.
   - Chạy tiếp `ChatClient.java` lần 2 với Username: `Bob`.

3. **Thao tác trên giao diện:**
   - **Chat Chung:** Chọn `[Tất cả - Phòng chung]` ở danh sách bên phải $\rightarrow$ Nhập tin nhắn và gửi.
   - **Chat Riêng 1-1:** Click chọn tên người nhận cụ thể (ví dụ: `Alice` hoặc `Bob`) $\rightarrow$ Nhập tin nhắn và gửi.
   - **Gửi File:** Click chọn người nhận $\rightarrow$ Bấm **Gửi File** $\rightarrow$ Chọn file qua cửa sổ `JFileChooser` $\rightarrow$ Bên nhận bấm **Yes** để tải về.
