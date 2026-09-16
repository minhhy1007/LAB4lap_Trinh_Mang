package client;

import javax.swing.SwingUtilities;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ChatService - Đóng gói toàn bộ nghiệp vụ Client (kết nối, đăng nhập, chat, truyền file).
 * Đảm bảo GUI (Swing) hoàn toàn tách biệt khỏi logic Socket.
 */
public class ChatService {
    private ServerConnection connection;
    private ChatEventListener listener;
    private MessageReceiver receiver;
    private String currentUsername;

    private final Map<String, File> pendingSendFiles = new ConcurrentHashMap<>();
    private final Map<String, OfferInfo> pendingOffers = new ConcurrentHashMap<>();

    public static class OfferInfo {
        public final String sender;
        public final String fileName;
        public final long fileSize;

        public OfferInfo(String sender, String fileName, long fileSize) {
            this.sender = sender;
            this.fileName = fileName;
            this.fileSize = fileSize;
        }
    }

    public void setListener(ChatEventListener listener) {
        this.listener = listener;
    }

    // Kết nối đến Server và gửi yêu cầu đăng nhập
    public void connectAndLogin(String host, int port, String username) {
        new Thread(() -> {
            try {
                connection = new ServerConnection(host, port);
                connection.connect();

                this.currentUsername = username;
                receiver = new MessageReceiver(connection, this);
                new Thread(receiver, "MessageReceiverThread").start();

                // Gửi lệnh LOGIN
                connection.send(Protocol.CMD_LOGIN + Protocol.DELIMITER + username);

            } catch (IOException e) {
                notifyUI(() -> {
                    if (listener != null) listener.onLoginFailed("Không thể kết nối đến Server tại " + host + ":" + port + " (" + e.getMessage() + ")");
                });
            }
        }).start();
    }

    // Gửi tin nhắn phòng chung
    public void sendBroadcastMessage(String message) {
        if (connection != null && connection.isConnected()) {
            connection.send(Protocol.CMD_MESSAGE + Protocol.DELIMITER + "ALL" + Protocol.DELIMITER + message);
        }
    }

    // Gửi tin nhắn riêng 1-1
    public void sendPrivateMessage(String recipient, String message) {
        if (connection != null && connection.isConnected()) {
            connection.send(Protocol.CMD_MESSAGE + Protocol.DELIMITER + recipient + Protocol.DELIMITER + message);
        }
    }

    // Yêu cầu gửi file
    public void sendFileRequest(String recipient, File file) {
        if (connection != null && connection.isConnected() && file != null && file.exists()) {
            pendingSendFiles.put(file.getName(), file);
            connection.send(Protocol.CMD_FILE_REQUEST + Protocol.DELIMITER + recipient + Protocol.DELIMITER + file.getName() + Protocol.DELIMITER + file.length());
        }
    }

    // Chấp nhận nhận file
    public void acceptFile(String transferId) {
        if (connection != null && connection.isConnected()) {
            connection.send(Protocol.CMD_FILE_ACCEPT + Protocol.DELIMITER + transferId);
        }
    }

    // Từ chối nhận file
    public void rejectFile(String transferId) {
        if (connection != null && connection.isConnected()) {
            connection.send(Protocol.CMD_FILE_REJECT + Protocol.DELIMITER + transferId);
            pendingOffers.remove(transferId);
        }
    }

    // Yêu cầu cập nhật danh sách người dùng online
    public void requestUserList() {
        if (connection != null && connection.isConnected()) {
            connection.send(Protocol.CMD_GET_USERS);
        }
    }

    // Đăng xuất và đóng kết nối
    public void logout() {
        if (connection != null && connection.isConnected()) {
            connection.send(Protocol.CMD_LOGOUT);
            connection.disconnect();
        }
    }

    // Xử lý các thông điệp nhận từ Server theo Protocol
    public void handleIncomingMessage(String rawLine) {
        String[] parts = rawLine.split(Protocol.SEPARATOR);
        String header = parts[0].toUpperCase();

        switch (header) {
            case Protocol.RES_LOGIN_SUCCESS:
                notifyUI(() -> {
                    if (listener != null) listener.onLoginSuccess(currentUsername);
                });
                break;

            case Protocol.RES_LOGIN_FAILED:
                String reason = parts.length > 1 ? parts[1] : "Lỗi đăng nhập";
                notifyUI(() -> {
                    if (listener != null) listener.onLoginFailed(reason);
                });
                break;

            case Protocol.RES_USERS:
                String userString = parts.length > 1 ? parts[1] : "";
                List<String> users = Arrays.asList(userString.split(","));
                notifyUI(() -> {
                    if (listener != null) listener.onUsersUpdated(users);
                });
                break;

            case Protocol.RES_USER_JOINED:
                String joinedUser = parts.length > 1 ? parts[1] : "";
                notifyUI(() -> {
                    if (listener != null) listener.onUserJoined(joinedUser);
                });
                requestUserList(); // Cập nhật lại danh sách
                break;

            case Protocol.RES_USER_LEFT:
                String leftUser = parts.length > 1 ? parts[1] : "";
                notifyUI(() -> {
                    if (listener != null) listener.onUserLeft(leftUser);
                });
                requestUserList();
                break;

            case Protocol.RES_MESSAGE_FROM:
                String sender = parts.length > 1 ? parts[1] : "Ẩn danh";
                String msg = parts.length > 2 ? parts[2] : "";
                notifyUI(() -> {
                    if (listener != null) listener.onMessageReceived(sender, msg);
                });
                break;

            case "MESSAGE_SENT":
                String receiver = parts.length > 1 ? parts[1] : "";
                String sentMsg = parts.length > 2 ? parts[2] : "";
                notifyUI(() -> {
                    if (listener != null) listener.onMessageSent(receiver, sentMsg);
                });
                break;

            case Protocol.RES_FILE_OFFER:
                if (parts.length >= 5) {
                    String transferId = parts[1];
                    String offerSender = parts[2];
                    String offerFileName = parts[3];
                    long offerFileSize = Long.parseLong(parts[4]);

                    pendingOffers.put(transferId, new OfferInfo(offerSender, offerFileName, offerFileSize));

                    notifyUI(() -> {
                        if (listener != null) listener.onFileOffer(transferId, offerSender, offerFileName, offerFileSize);
                    });
                }
                break;

            case Protocol.RES_FILE_ACCEPT:
                if (parts.length >= 3) {
                    String transferId = parts[1];
                    String acceptedFileName = parts[2];

                    if (pendingOffers.containsKey(transferId)) {
                        // Chúng ta là Bên Nhận
                        OfferInfo info = pendingOffers.remove(transferId);
                        FileReceiver fileReceiver = new FileReceiver(connection.getHost(), Protocol.FILE_PORT, transferId, info.fileName, info.fileSize);
                        new Thread(fileReceiver, "FileReceiver-" + transferId).start();
                    } else if (pendingSendFiles.containsKey(acceptedFileName)) {
                        // Chúng ta là Bên Gửi
                        File fileToSend = pendingSendFiles.remove(acceptedFileName);
                        FileSender fileSender = new FileSender(connection.getHost(), Protocol.FILE_PORT, transferId, fileToSend);
                        new Thread(fileSender, "FileSender-" + transferId).start();
                    }
                }
                break;

            case Protocol.RES_FILE_REJECT:
                String rejectReason = parts.length > 2 ? parts[2] : "Bị từ chối";
                notifyUI(() -> {
                    if (listener != null) listener.onSystemNotification("Truyền file bị từ chối: " + rejectReason);
                });
                break;

            case Protocol.RES_ERROR:
                String errMsg = parts.length > 1 ? parts[1] : "";
                notifyUI(() -> {
                    if (listener != null) listener.onSystemNotification(errMsg);
                });
                break;
        }
    }

    public void handleDisconnected() {
        notifyUI(() -> {
            if (listener != null) listener.onDisconnected();
        });
    }

    // Đảm bảo việc cập nhật UI luôn diễn ra trên Swing Event Dispatch Thread (EDT)
    private void notifyUI(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }

    public String getCurrentUsername() {
        return currentUsername;
    }
}
