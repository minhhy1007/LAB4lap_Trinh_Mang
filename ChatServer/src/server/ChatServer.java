package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * ChatServer - Khởi tạo ServerSocket và lắng nghe kết nối từ Client (Phase 1).
 */
public class ChatServer {
    private static final int DEFAULT_PORT = 8888;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        ServerLogger.log("Khởi động Chat Server tại port " + port + "...");

        // Mở ServerSocket lắng nghe trên port 8888
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            ServerLogger.log("Server đã sẵn sàng lắng nghe kết nối...");

            while (true) {
                ServerLogger.log("Đang chờ Client kết nối đến...");
                // Chờ Client kết nối đến Server (chặn luồng cho đến khi có Client kết nối)
                Socket clientSocket = serverSocket.accept();
                ServerLogger.log("Client đã kết nối từ: " + clientSocket.getRemoteSocketAddress());

                // Tạo luồng đọc và ghi dữ liệu dạng text
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

                // Đọc tin nhắn gửi từ Client
                String clientMessage = reader.readLine();
                ServerLogger.log("Nhận từ Client: " + clientMessage);

                // Gửi phản hồi xác nhận lại cho Client
                String responseMessage = "CONNECTED|Server chào bạn! Kết nối TCP thành công.";
                writer.println(responseMessage);
                ServerLogger.log("Đã gửi phản hồi cho Client: " + responseMessage);

                // Đóng kết nối phiên kiểm tra Phase 1
                clientSocket.close();
                ServerLogger.log("Đã đóng kết nối Client hiện tại. Tiếp tục chờ kết nối mới...\n");
            }
        } catch (Exception e) {
            ServerLogger.error("Lỗi xảy ra tại Server: " + e.getMessage(), e);
        }
    }
}
