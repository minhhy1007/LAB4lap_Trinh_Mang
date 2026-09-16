package client;

import ui.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * ChatClient - Điểm khởi chạy chính của ứng dụng Client (Giao diện đồ họa Swing).
 */
public class ChatClient {
    public static void main(String[] args) {
        // Thiết lập giao diện theo phong cách hệ thống (System Look and Feel) cho hiện đại
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Khởi chạy cửa sổ đăng nhập trên luồng Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
