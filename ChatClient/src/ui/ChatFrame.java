package ui;

import client.ChatEventListener;
import client.ChatService;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ChatFrame - Giao diện phòng Chat và Truyền File chính.
 * Hỗ trợ tách biệt lịch sử hội thoại: Phòng Chung và từng cuộc trò chuyện Riêng 1-1.
 */
public class ChatFrame extends JFrame implements ChatEventListener {
    private final ChatService chatService;
    private final String myUsername;

    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private JButton sendFileButton;
    private DefaultListModel<String> userListModel;
    private JList<String> userList;
    private JLabel targetLabel;

    public static final String ALL_USERS_ITEM = "[Tất cả - Phòng chung]";

    // Quản lý lịch sử tin nhắn riêng biệt cho từng người dùng/phòng (Key: Username hoặc ALL_USERS_ITEM)
    private final Map<String, StringBuilder> chatHistories = new ConcurrentHashMap<>();

    public ChatFrame(ChatService chatService, String myUsername) {
        this.chatService = chatService;
        this.myUsername = myUsername;

        // Khởi tạo sẵn lịch sử cho phòng chung
        chatHistories.put(ALL_USERS_ITEM, new StringBuilder());

        setTitle("Phòng Chat Socket - Tài khoản: " + myUsername);
        setSize(780, 520);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();

        chatService.setListener(this);
        chatService.requestUserList();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                chatService.logout();
            }
        });
    }

    private void initComponents() {
        setLayout(new BorderLayout(8, 8));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(8, 8, 8, 8));

        // 1. Khu vực hiển thị tin nhắn (Center)
        JPanel chatPanel = new JPanel(new BorderLayout(5, 5));

        targetLabel = new JLabel("Đang trò chuyện: " + ALL_USERS_ITEM);
        targetLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chatPanel.add(targetLabel, BorderLayout.NORTH);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);

        add(chatPanel, BorderLayout.CENTER);

        // 2. Danh sách người dùng Online (East)
        JPanel usersPanel = new JPanel(new BorderLayout(5, 5));
        usersPanel.setBorder(new TitledBorder("Người dùng Online"));
        usersPanel.setPreferredSize(new Dimension(200, 0));

        userListModel = new DefaultListModel<>();
        userListModel.addElement(ALL_USERS_ITEM);

        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setSelectedIndex(0);
        userList.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Khi người dùng click chọn người trong danh sách Online
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                switchConversationView();
            }
        });

        usersPanel.add(new JScrollPane(userList), BorderLayout.CENTER);
        add(usersPanel, BorderLayout.EAST);

        // 3. Thanh nhập nội dung và các nút chức năng (South)
        JPanel bottomPanel = new JPanel(new BorderLayout(6, 6));

        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        inputField.addActionListener(e -> handleSendMessage());
        bottomPanel.add(inputField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        sendButton = new JButton("Gửi");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        sendButton.addActionListener(e -> handleSendMessage());

        sendFileButton = new JButton("Gửi File");
        sendFileButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        sendFileButton.addActionListener(e -> handleSendFile());

        buttonPanel.add(sendButton);
        buttonPanel.add(sendFileButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    // Chuyển đổi khung nhìn hội thoại khi chọn user khác
    private void switchConversationView() {
        String selected = userList.getSelectedValue();
        if (selected == null || selected.equals(ALL_USERS_ITEM)) {
            selected = ALL_USERS_ITEM;
            targetLabel.setText("Đang trò chuyện: " + ALL_USERS_ITEM);
        } else {
            targetLabel.setText("Đang nhắn tin RIÊNG với: @" + selected);
        }

        // Tải lại toàn bộ lịch sử tin nhắn của người được chọn
        StringBuilder history = chatHistories.computeIfAbsent(selected, k -> new StringBuilder());
        chatArea.setText(history.toString());
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private String getSelectedTarget() {
        String selected = userList.getSelectedValue();
        return (selected == null) ? ALL_USERS_ITEM : selected;
    }

    // Lưu tin nhắn vào lịch sử của đối tượng tương ứng và cập nhật UI nếu đang mở
    private void appendMessageToConversation(String conversationKey, String message) {
        StringBuilder history = chatHistories.computeIfAbsent(conversationKey, k -> new StringBuilder());
        history.append(message).append("\n");

        // Nếu người dùng đang mở đúng cuộc trò chuyện này thì hiển thị lên màn hình ngay
        if (getSelectedTarget().equalsIgnoreCase(conversationKey)) {
            chatArea.append(message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        }
    }

    // Xử lý gửi tin nhắn
    private void handleSendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        String selectedTarget = getSelectedTarget();
        if (selectedTarget.equals(ALL_USERS_ITEM)) {
            // Gửi tới phòng chung
            chatService.sendBroadcastMessage(text);
        } else {
            // Gửi riêng 1-1
            chatService.sendPrivateMessage(selectedTarget, text);
        }

        inputField.setText("");
    }

    // Xử lý gửi file bằng JFileChooser
    private void handleSendFile() {
        String selectedTarget = getSelectedTarget();
        if (selectedTarget.equals(ALL_USERS_ITEM)) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn 1 người dùng cụ thể trong danh sách Online bên phải để gửi file!",
                    "Nhắc nhở", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn file cần gửi cho " + selectedTarget);
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile != null && selectedFile.exists()) {
                chatService.sendFileRequest(selectedTarget, selectedFile);
                appendMessageToConversation(selectedTarget, "[HỆ THỐNG]: Đang gửi yêu cầu truyền file '" + selectedFile.getName() + "' tới @" + selectedTarget + "...");
            }
        }
    }

    // ==========================================
    // CÁC CALLBACK TỪ ChatEventListener
    // ==========================================

    @Override
    public void onLoginSuccess(String username) {}

    @Override
    public void onLoginFailed(String reason) {}

    @Override
    public void onUsersUpdated(List<String> users) {
        String currentSelected = userList.getSelectedValue();
        userListModel.clear();
        userListModel.addElement(ALL_USERS_ITEM);

        for (String user : users) {
            user = user.trim();
            if (!user.isEmpty() && !user.equalsIgnoreCase(myUsername)) {
                userListModel.addElement(user);
            }
        }

        if (currentSelected != null && userListModel.contains(currentSelected)) {
            userList.setSelectedValue(currentSelected, true);
        } else {
            userList.setSelectedIndex(0);
        }
    }

    @Override
    public void onUserJoined(String username) {
        appendMessageToConversation(ALL_USERS_ITEM, ">> [ONLINE]: '" + username + "' đã tham gia phòng chat.");
    }

    @Override
    public void onUserLeft(String username) {
        appendMessageToConversation(ALL_USERS_ITEM, ">> [OFFLINE]: '" + username + "' đã rời khỏi phòng.");
    }

    @Override
    public void onMessageReceived(String sender, String content) {
        // Phân biệt tin nhắn phòng chung hay tin nhắn riêng 1-1
        if (sender.endsWith("(Chung)")) {
            String cleanSender = sender.replace("(Chung)", "").trim();
            appendMessageToConversation(ALL_USERS_ITEM, "[" + cleanSender + "]: " + content);
        } else {
            // Tin nhắn riêng 1-1 từ 'sender'
            appendMessageToConversation(sender, "[" + sender + "]: " + content);

            // Nếu người nhận đang ở phòng chung hoặc chat với người khác, báo nhẹ một dòng thông báo
            if (!getSelectedTarget().equalsIgnoreCase(sender)) {
                appendMessageToConversation(ALL_USERS_ITEM, ">> [TIN NHẮN MỚI]: Bạn có tin nhắn riêng từ @" + sender + " (click chọn @" + sender + " bên phải để xem).");
            }
        }
    }

    @Override
    public void onMessageSent(String receiver, String content) {
        if (receiver.equalsIgnoreCase("ALL")) {
            appendMessageToConversation(ALL_USERS_ITEM, "[Tôi]: " + content);
        } else {
            // Tin nhắn riêng của Tôi gửi cho 'receiver' -> chỉ lưu vào cuộc trò chuyện với 'receiver'
            appendMessageToConversation(receiver, "[Tôi -> @" + receiver + "]: " + content);
        }
    }

    @Override
    public void onFileOffer(String transferId, String sender, String fileName, long fileSize) {
        String msg = "Người dùng @" + sender + " muốn gửi cho bạn file:\n"
                + "• Tên file: " + fileName + "\n"
                + "• Dung lượng: " + (fileSize / 1024 + 1) + " KB (" + fileSize + " bytes)\n\n"
                + "Bạn có đồng ý nhận file này không?";

        int choice = JOptionPane.showConfirmDialog(this, msg, "Yêu cầu nhận file",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            chatService.acceptFile(transferId);
            appendMessageToConversation(sender, "[TRUYỀN FILE]: Bạn đã đồng ý nhận file '" + fileName + "'. Đang tải về...");
        } else {
            chatService.rejectFile(transferId);
            appendMessageToConversation(sender, "[TRUYỀN FILE]: Bạn đã từ chối nhận file '" + fileName + "'.");
        }
    }

    @Override
    public void onSystemNotification(String notification) {
        appendMessageToConversation(getSelectedTarget(), ">> [THÔNG BÁO]: " + notification);
    }

    @Override
    public void onDisconnected() {
        appendMessageToConversation(getSelectedTarget(), ">> [CẢNH BÁO]: Đã mất kết nối tới Server!");
        JOptionPane.showMessageDialog(this, "Mất kết nối tới Server!", "Ngắt kết nối", JOptionPane.ERROR_MESSAGE);
    }
}
