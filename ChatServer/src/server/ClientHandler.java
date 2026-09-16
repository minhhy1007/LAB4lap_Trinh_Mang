package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * ClientHandler - Xử lý thông điệp giao thức (Protocol) cho từng Client trên luồng riêng (Chat & File Transfer).
 */
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final UserManager userManager;
    private final FileTransferService fileTransferService;
    private BufferedReader reader;
    private PrintWriter writer;
    private String username;

    public ClientHandler(Socket socket, UserManager userManager, FileTransferService fileTransferService) {
        this.socket = socket;
        this.userManager = userManager;
        this.fileTransferService = fileTransferService;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            String rawLine;
            while ((rawLine = reader.readLine()) != null) {
                rawLine = rawLine.trim();
                if (rawLine.isEmpty()) continue;

                processCommand(rawLine);
            }
        } catch (IOException e) {
            ServerLogger.log((username != null ? username : socket.getRemoteSocketAddress()) + " ngắt kết nối: " + e.getMessage());
        } finally {
            close();
        }
    }

    private void processCommand(String rawLine) {
        String[] parts = rawLine.split(Protocol.SEPARATOR);
        String command = parts[0].toUpperCase();

        switch (command) {
            case Protocol.CMD_LOGIN:
                handleLogin(parts);
                break;

            case Protocol.CMD_GET_USERS:
                handleGetUsers();
                break;

            case Protocol.CMD_MESSAGE:
                handleMessage(rawLine);
                break;

            case Protocol.CMD_FILE_REQUEST:
                handleFileRequest(parts);
                break;

            case Protocol.CMD_FILE_ACCEPT:
                handleFileAccept(parts);
                break;

            case Protocol.CMD_FILE_REJECT:
                handleFileReject(parts);
                break;

            case Protocol.CMD_LOGOUT:
                sendMessage(Protocol.RES_ERROR + Protocol.DELIMITER + "Đăng xuất thành công. Hẹn gặp lại!");
                close();
                break;

            default:
                sendMessage(Protocol.RES_ERROR + Protocol.DELIMITER + "Lệnh không hợp lệ: " + command);
                break;
        }
    }

    private void handleLogin(String[] parts) {
        if (this.username != null) {
            sendMessage(Protocol.RES_LOGIN_FAILED + Protocol.DELIMITER + "Bạn đã đăng nhập với tên: " + this.username);
            return;
        }
        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            sendMessage(Protocol.RES_LOGIN_FAILED + Protocol.DELIMITER + "Tên người dùng không được để trống!");
            return;
        }

        String requestedName = parts[1].trim();
        boolean success = userManager.registerUser(requestedName, this);

        if (success) {
            this.username = requestedName;
            ServerLogger.log("Người dùng đăng nhập thành công: " + this.username + " (" + socket.getRemoteSocketAddress() + ")");
            sendMessage(Protocol.RES_LOGIN_SUCCESS);
            userManager.broadcast(Protocol.RES_USER_JOINED + Protocol.DELIMITER + this.username, this.username);
            handleGetUsers();
        } else {
            ServerLogger.log("Đăng nhập thất bại: Tên '" + requestedName + "' đã được sử dụng.");
            sendMessage(Protocol.RES_LOGIN_FAILED + Protocol.DELIMITER + "Tên '" + requestedName + "' đã có người dùng. Vui lòng chọn tên khác!");
        }
    }

    private void handleGetUsers() {
        String userList = String.join(",", userManager.getOnlineUsers());
        sendMessage(Protocol.RES_USERS + Protocol.DELIMITER + userList);
    }

    private void handleMessage(String rawLine) {
        if (this.username == null) {
            sendMessage(Protocol.RES_ERROR + Protocol.DELIMITER + "Vui lòng đăng nhập trước khi gửi tin nhắn!");
            return;
        }

        String[] parts = rawLine.split(Protocol.SEPARATOR, 3);
        if (parts.length < 3) {
            sendMessage(Protocol.RES_ERROR + Protocol.DELIMITER + "Cú pháp sai. Định dạng: MESSAGE|người_nhận|nội_dung");
            return;
        }

        String receiver = parts[1].trim();
        String content = parts[2];

        if (receiver.equalsIgnoreCase("ALL")) {
            ServerLogger.log("[Broadcast] " + this.username + ": " + content);
            userManager.broadcast(Protocol.RES_MESSAGE_FROM + Protocol.DELIMITER + this.username + " (Chung)" + Protocol.DELIMITER + content, this.username);
            sendMessage("MESSAGE_SENT|ALL|" + content);
        } else {
            ClientHandler targetClient = userManager.getClient(receiver);
            if (targetClient != null) {
                ServerLogger.log("[Private] " + this.username + " -> " + receiver + ": " + content);
                targetClient.sendMessage(Protocol.RES_MESSAGE_FROM + Protocol.DELIMITER + this.username + Protocol.DELIMITER + content);
                sendMessage("MESSAGE_SENT|" + receiver + "|" + content);
            } else {
                sendMessage(Protocol.RES_ERROR + Protocol.DELIMITER + "Người dùng '" + receiver + "' không online hoặc không tồn tại.");
            }
        }
    }

    // Yêu cầu truyền file: FILE_REQUEST|receiver|fileName|fileSize
    private void handleFileRequest(String[] parts) {
        if (this.username == null) {
            sendMessage(Protocol.RES_ERROR + Protocol.DELIMITER + "Vui lòng đăng nhập trước khi gửi file!");
            return;
        }
        if (parts.length < 4) {
            sendMessage(Protocol.RES_ERROR + Protocol.DELIMITER + "Cú pháp FILE_REQUEST không đúng!");
            return;
        }

        String receiver = parts[1].trim();
        String fileName = parts[2].trim();
        long fileSize;
        try {
            fileSize = Long.parseLong(parts[3].trim());
        } catch (NumberFormatException e) {
            sendMessage(Protocol.RES_ERROR + Protocol.DELIMITER + "Kích thước file không hợp lệ!");
            return;
        }

        ClientHandler targetClient = userManager.getClient(receiver);
        if (targetClient == null) {
            sendMessage(Protocol.RES_ERROR + Protocol.DELIMITER + "Người nhận '" + receiver + "' không online!");
            return;
        }

        // Tạo phiên truyền file trung gian qua FileTransferService
        String transferId = fileTransferService.registerTransfer(this.username, receiver, fileName, fileSize);
        ServerLogger.log("Khởi tạo yêu cầu truyền file [" + transferId + "] từ " + this.username + " tới " + receiver + ": " + fileName + " (" + fileSize + " bytes)");

        // Gửi lời mời nhận file tới bên nhận
        targetClient.sendMessage(Protocol.RES_FILE_OFFER + Protocol.DELIMITER + transferId + Protocol.DELIMITER + this.username + Protocol.DELIMITER + fileName + Protocol.DELIMITER + fileSize);
        sendMessage(Protocol.RES_ERROR + Protocol.DELIMITER + "Đã gửi yêu cầu truyền file '" + fileName + "' (" + (fileSize / 1024 + 1) + " KB) tới " + receiver + ". Đang chờ phản hồi...");
    }

    // Chấp nhận nhận file: FILE_ACCEPT|transferId
    private void handleFileAccept(String[] parts) {
        if (parts.length < 2) return;
        String transferId = parts[1].trim();
        FileTransferService.TransferSession session = fileTransferService.getSession(transferId);

        if (session != null && session.receiver.equalsIgnoreCase(this.username)) {
            ClientHandler senderClient = userManager.getClient(session.sender);
            if (senderClient != null) {
                ServerLogger.log("Người nhận " + this.username + " đồng ý nhận file [" + transferId + "]. Chuẩn bị truyền dữ liệu...");
                // Báo cho cả 2 bên kết nối tới cổng 8889 để truyền byte stream
                senderClient.sendMessage(Protocol.RES_FILE_ACCEPT + Protocol.DELIMITER + transferId + Protocol.DELIMITER + session.fileName);
                sendMessage(Protocol.RES_FILE_ACCEPT + Protocol.DELIMITER + transferId + Protocol.DELIMITER + session.fileName);
            } else {
                sendMessage(Protocol.RES_ERROR + Protocol.DELIMITER + "Người gửi '" + session.sender + "' đã offline.");
                fileTransferService.removeSession(transferId);
            }
        }
    }

    // Từ chối nhận file: FILE_REJECT|transferId
    private void handleFileReject(String[] parts) {
        if (parts.length < 2) return;
        String transferId = parts[1].trim();
        FileTransferService.TransferSession session = fileTransferService.getSession(transferId);

        if (session != null) {
            ClientHandler senderClient = userManager.getClient(session.sender);
            if (senderClient != null) {
                senderClient.sendMessage(Protocol.RES_FILE_REJECT + Protocol.DELIMITER + transferId + Protocol.DELIMITER + this.username + " đã từ chối nhận file.");
            }
            fileTransferService.removeSession(transferId);
            sendMessage(Protocol.RES_ERROR + Protocol.DELIMITER + "Bạn đã từ chối nhận file [" + transferId + "].");
        }
    }

    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }

    public String getUsername() {
        return username;
    }

    public void close() {
        try {
            if (username != null) {
                userManager.removeUser(username);
                userManager.broadcast(Protocol.RES_USER_LEFT + Protocol.DELIMITER + username, null);
                ServerLogger.log("Người dùng '" + username + "' đã đăng xuất/ngắt kết nối.");
                username = null;
            }
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            ServerLogger.error("Lỗi khi đóng socket: " + e.getMessage(), e);
        }
    }
}
