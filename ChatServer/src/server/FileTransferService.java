package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FileTransferService - Dịch vụ điều phối và chuyển tiếp dữ liệu nhị phân của File qua cổng 8889.
 * Sử dụng bộ đệm byte[] (4KB) streaming trực tiếp, không load toàn bộ file vào RAM.
 */
public class FileTransferService {
    private final Map<String, TransferSession> activeTransfers = new ConcurrentHashMap<>();
    private ServerSocket fileServerSocket;

    public static class TransferSession {
        public final String transferId;
        public final String sender;
        public final String receiver;
        public final String fileName;
        public final long fileSize;

        public Socket senderSocket;
        public Socket receiverSocket;

        public TransferSession(String transferId, String sender, String receiver, String fileName, long fileSize) {
            this.transferId = transferId;
            this.sender = sender;
            this.receiver = receiver;
            this.fileName = fileName;
            this.fileSize = fileSize;
        }
    }

    // Khởi động luồng lắng nghe kết nối dữ liệu File tại cổng riêng (8889)
    public void start(int port) {
        new Thread(() -> {
            try {
                fileServerSocket = new ServerSocket(port);
                ServerLogger.log("File Transfer Service đang lắng nghe tại port " + port + "...");

                while (!fileServerSocket.isClosed()) {
                    Socket dataSocket = fileServerSocket.accept();
                    handleDataConnection(dataSocket);
                }
            } catch (IOException e) {
                if (fileServerSocket != null && !fileServerSocket.isClosed()) {
                    ServerLogger.error("Lỗi FileTransferService: " + e.getMessage(), e);
                }
            }
        }, "FileTransferServerThread").start();
    }

    // Đăng ký một phiên truyền file mới và cấp transferId
    public String registerTransfer(String sender, String receiver, String fileName, long fileSize) {
        String transferId = "F" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        TransferSession session = new TransferSession(transferId, sender, receiver, fileName, fileSize);
        activeTransfers.put(transferId, session);
        return transferId;
    }

    public TransferSession getSession(String transferId) {
        return activeTransfers.get(transferId);
    }

    public void removeSession(String transferId) {
        activeTransfers.remove(transferId);
    }

    // Xử lý khi một client kết nối tới cổng truyền file
    private void handleDataConnection(Socket socket) {
        new Thread(() -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                String handshake = reader.readLine(); // "SEND|transferId" hoặc "RECV|transferId"
                if (handshake == null) return;

                String[] parts = handshake.split(Protocol.SEPARATOR);
                if (parts.length < 2) return;

                String role = parts[0].toUpperCase();
                String transferId = parts[1].trim();

                TransferSession session = activeTransfers.get(transferId);
                if (session == null) {
                    socket.close();
                    return;
                }

                synchronized (session) {
                    if ("SEND".equals(role)) {
                        session.senderSocket = socket;
                    } else if ("RECV".equals(role)) {
                        session.receiverSocket = socket;
                    }

                    // Khi cả 2 bên (Sender & Receiver) đều đã kết nối -> bắt đầu chuyển tiếp byte stream
                    if (session.senderSocket != null && session.receiverSocket != null) {
                        pipeFileStream(session);
                    }
                }
            } catch (IOException e) {
                ServerLogger.error("Lỗi bắt tay Data Socket: " + e.getMessage(), e);
            }
        }).start();
    }

    // Luân chuyển byte stream từ Sender sang Receiver với buffer 4KB
    private void pipeFileStream(TransferSession session) {
        new Thread(() -> {
            ServerLogger.log("Bắt đầu chuyển tiếp file: " + session.fileName + " (" + session.fileSize + " bytes) từ " + session.sender + " tới " + session.receiver);
            try (InputStream in = session.senderSocket.getInputStream();
                 OutputStream out = session.receiverSocket.getOutputStream()) {

                byte[] buffer = new byte[4096];
                long remaining = session.fileSize;

                while (remaining > 0) {
                    int toRead = (int) Math.min(buffer.length, remaining);
                    int bytesRead = in.read(buffer, 0, toRead);
                    if (bytesRead == -1) break;

                    out.write(buffer, 0, bytesRead);
                    remaining -= bytesRead;
                }
                out.flush();
                ServerLogger.log("Hoàn thành chuyển tiếp file: " + session.fileName);
            } catch (IOException e) {
                ServerLogger.error("Lỗi trong quá trình stream file: " + e.getMessage(), e);
            } finally {
                try { session.senderSocket.close(); } catch (Exception ignored) {}
                try { session.receiverSocket.close(); } catch (Exception ignored) {}
                removeSession(session.transferId);
            }
        }, "FileStreamer-" + session.transferId).start();
    }
}
