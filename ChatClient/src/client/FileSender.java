package client;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * FileSender - Luồng chạy ngầm gửi dữ liệu nhị phân của file tới cổng 8889 qua byte stream.
 * Đọc file theo từng khối 4KB (Chunking) để tiết kiệm RAM.
 */
public class FileSender implements Runnable {
    private final String host;
    private final int port;
    private final String transferId;
    private final File file;

    public FileSender(String host, int port, String transferId, File file) {
        this.host = host;
        this.port = port;
        this.transferId = transferId;
        this.file = file;
    }

    @Override
    public void run() {
        System.out.println("\n>> [ĐANG GỬI FILE]: Bắt đầu truyền file '" + file.getName() + "' (" + (file.length() / 1024 + 1) + " KB)...");

        try (Socket dataSocket = new Socket(host, port);
             PrintWriter handshakeWriter = new PrintWriter(new OutputStreamWriter(dataSocket.getOutputStream(), StandardCharsets.UTF_8), true);
             OutputStream rawOut = dataSocket.getOutputStream();
             FileInputStream fileIn = new FileInputStream(file);
             BufferedInputStream bufIn = new BufferedInputStream(fileIn)) {

            // Gửi thông điệp bắt tay xác định vai trò và transferId
            handshakeWriter.println("SEND|" + transferId);

            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalSent = 0;
            long fileSize = file.length();

            // Đọc và bắn từng chunk dữ liệu nhị phân qua TCP socket
            while ((bytesRead = bufIn.read(buffer)) != -1) {
                rawOut.write(buffer, 0, bytesRead);
                totalSent += bytesRead;
            }
            rawOut.flush();

            System.out.println(">> [THÀNH CÔNG]: Đã gửi xong file '" + file.getName() + "' (" + totalSent + " bytes)!");

        } catch (IOException e) {
            System.err.println(">> [LỖI TRUYỀN FILE]: " + e.getMessage());
        }
    }
}
