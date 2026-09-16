package server;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UserManager - Quản lý danh sách người dùng đang online và điều phối gửi thông điệp (Directory Service).
 */
public class UserManager {
    // Lưu trữ ánh xạ từ username -> ClientHandler tương ứng
    private final Map<String, ClientHandler> onlineUsers = new ConcurrentHashMap<>();

    // Đăng ký người dùng đăng nhập
    public boolean registerUser(String username, ClientHandler handler) {
        if (username == null || username.trim().isEmpty() || username.contains("|") || username.contains(",")) {
            return false;
        }
        // Thêm an toàn đa luồng: Chỉ thành công nếu tên chưa tồn tại
        return onlineUsers.putIfAbsent(username.trim(), handler) == null;
    }

    // Xóa người dùng khi đăng xuất hoặc ngắt kết nối
    public void removeUser(String username) {
        if (username != null) {
            onlineUsers.remove(username.trim());
        }
    }

    // Kiểm tra trạng thái online
    public boolean isUserOnline(String username) {
        return username != null && onlineUsers.containsKey(username.trim());
    }

    // Lấy tập hợp danh sách các username đang online
    public Set<String> getOnlineUsers() {
        return Collections.unmodifiableSet(onlineUsers.keySet());
    }

    // Lấy handler của một người dùng cụ thể
    public ClientHandler getClient(String username) {
        return username != null ? onlineUsers.get(username.trim()) : null;
    }

    // Gửi thông điệp tới toàn bộ người dùng online (Broadcast)
    public void broadcast(String message, String excludeUsername) {
        for (Map.Entry<String, ClientHandler> entry : onlineUsers.entrySet()) {
            if (excludeUsername == null || !entry.getKey().equalsIgnoreCase(excludeUsername)) {
                entry.getValue().sendMessage(message);
            }
        }
    }
}
