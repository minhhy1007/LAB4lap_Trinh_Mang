package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ChatServer - Khởi tạo ServerSocket, quản lý UserManager, FileTransferService và phân phối luồng.
 */
public class ChatServer {
    private static final int DEFAULT_PORT = 8888;
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();
    // Quản lý trạng thái và danh sách người dùng toàn Server
    private static final UserManager userManager = new UserManager();
    // Dịch vụ truyền nhận file chuyên dụng (Port 8889)
    private static final FileTransferService fileTransferService = new FileTransferService();

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        ServerLogger.log("Khởi động Chat Server (Phase 5: File Transfer) tại port " + port + "...");

        // Khởi động dịch vụ truyền nhận file tại port 8889
        fileTransferService.start(Protocol.FILE_PORT);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            ServerLogger.log("Server Chat đã sẵn sàng phục vụ tại port " + port + "...");

            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                ServerLogger.log("Chấp nhận kết nối Socket từ: " + clientSocket.getRemoteSocketAddress());

                // Giao việc xử lý kết nối cho luồng riêng với UserManager và FileTransferService
                ClientHandler clientHandler = new ClientHandler(clientSocket, userManager, fileTransferService);
                threadPool.execute(clientHandler);
            }
        } catch (IOException e) {
            ServerLogger.error("Lỗi ServerSocket: " + e.getMessage(), e);
        } finally {
            threadPool.shutdown();
            ServerLogger.log("Chat Server đã dừng.");
        }
    }
}
