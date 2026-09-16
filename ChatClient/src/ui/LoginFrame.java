package ui;

import client.ChatEventListener;
import client.ChatService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

/**
 * LoginFrame - Giao diện Đăng nhập cho Chat Client.
 * Tách biệt hoàn toàn khỏi Socket logic, chỉ tương tác thông qua ChatService.
 */
public class LoginFrame extends JFrame {
    private JTextField hostField;
    private JTextField portField;
    private JTextField usernameField;
    private JButton loginButton;
    private final ChatService chatService;

    public LoginFrame() {
        this.chatService = new ChatService();
        setTitle("Chat Client - Đăng Nhập");
        setSize(380, 240);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("HỆ THỐNG CHAT & TRUYỀN FILE", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 8, 8));

        formPanel.add(new JLabel("Server Host:"));
        hostField = new JTextField("localhost");
        formPanel.add(hostField);

        formPanel.add(new JLabel("Port:"));
        portField = new JTextField("8888");
        formPanel.add(portField);

        formPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        formPanel.add(usernameField);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        loginButton = new JButton("Đăng Nhập");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        loginButton.addActionListener(e -> handleLogin());
        // Nhấn Enter ở ô username sẽ kích hoạt nút đăng nhập
        usernameField.addActionListener(e -> handleLogin());

        mainPanel.add(loginButton, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void handleLogin() {
        String host = hostField.getText().trim();
        String portStr = portField.getText().trim();
        String username = usernameField.getText().trim();

        if (host.isEmpty() || portStr.isEmpty() || username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Host, Port và Username!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Port phải là một số hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Đang kết nối...");

        // Đăng ký Callback lắng nghe sự kiện đăng nhập từ ChatService
        chatService.setListener(new ChatEventListener() {
            @Override
            public void onLoginSuccess(String user) {
                // Đăng nhập thành công -> Mở giao diện Chat và đóng màn hình đăng nhập
                ChatFrame chatFrame = new ChatFrame(chatService, user);
                chatFrame.setVisible(true);
                dispose();
            }

            @Override
            public void onLoginFailed(String reason) {
                JOptionPane.showMessageDialog(LoginFrame.this, reason, "Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
                loginButton.setEnabled(true);
                loginButton.setText("Đăng Nhập");
            }

            @Override
            public void onUsersUpdated(List<String> users) {}
            @Override
            public void onUserJoined(String user) {}
            @Override
            public void onUserLeft(String user) {}
            @Override
            public void onMessageReceived(String sender, String content) {}
            @Override
            public void onMessageSent(String receiver, String content) {}
            @Override
            public void onFileOffer(String transferId, String sender, String fileName, long fileSize) {}
            @Override
            public void onSystemNotification(String notification) {}
            @Override
            public void onDisconnected() {
                loginButton.setEnabled(true);
                loginButton.setText("Đăng Nhập");
            }
        });

        // Kết nối và gửi lệnh đăng nhập
        chatService.connectAndLogin(host, port, username);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
