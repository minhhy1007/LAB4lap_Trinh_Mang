package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * ChatClient - Kết nối đến Server qua TCP Socket và gửi/nhận message (Phase 1).
 */
public class ChatClient {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8888;

    public static void main(String[] args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;

        System.out.println("Đang kết nối đến Server tại " + host + ":" + port + "...");

        // Khởi tạo Socket kết nối tới Server
        try (Socket socket = new Socket(host, port);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Kết nối thành công đến Server!");

            // Gửi message kiểm tra đến Server
            String messageToSend = "CONNECT|Hello Server, tôi là TestClient!";
            System.out.println("Đang gửi đến Server: " + messageToSend);
            writer.println(messageToSend);

            // Nhận phản hồi từ Server
            String serverResponse = reader.readLine();
            System.out.println("Phản hồi từ Server: " + serverResponse);

            System.out.println("Hoàn tất kiểm tra TCP Connection (Phase 1)!");
        } catch (Exception e) {
            System.err.println("Không thể kết nối đến Server: " + e.getMessage());
        }
    }
}
