package client;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * FileReceiver - Luồng chạy ngầm nhận dữ liệu nhị phân từ cổng 8889 và lưu vào thư mục receive_files/.
 * Ghi file theo từng khối 4KB (Chunking) để tối ưu hiệu năng.
 */
public class FileReceiver implements Runnable {
    private final String host;
    private final int port;
    private final String transferId;
    private final String fileName;
    private final long fileSize;

    public FileReceiver(String host, int port, String transferId, String fileName, long fileSize) {
        this.host = host;
        this.port = port;
        this.transferId = transferId;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    @Override
    public void run() {
        System.out.println("\n>> [ĐANG NHẬN FILE]: Bắt đầu tải file '" + fileName + "' (" + (fileSize / 1024 + 1) + " KB)...");

        // Đảm bảo thư mục lưu trữ receive_files tồn tại
        File receiveDir = new File("receive_files");
        if (!receiveDir.exists()) {
            receiveDir.mkdirs();
        }

        File targetFile = new File(receiveDir, fileName);

        try (Socket dataSocket = new Socket(host, port);
             PrintWriter handshakeWriter = new PrintWriter(new OutputStreamWriter(dataSocket.getOutputStream(), StandardCharsets.UTF_8), true);
             InputStream rawIn = dataSocket.getInputStream();
             FileOutputStream fileOut = new FileOutputStream(targetFile);
             BufferedOutputStream bufOut = new BufferedOutputStream(fileOut)) {

            // Gửi thông điệp bắt tay xác định vai trò nhận
            handshakeWriter.println("RECV|" + transferId);

            byte[] buffer = new byte[4096];
            long remaining = fileSize;
            long totalReceived = 0;

            // Nhận đúng chính xác fileSize byte nhị phân từ stream
            while (remaining > 0) {
                int toRead = (int) Math.min(buffer.length, remaining);
                int bytesRead = rawIn.read(buffer, 0, toRead);
                if (bytesRead == -1) break;

                bufOut.write(buffer, 0, bytesRead);
                totalReceived += bytesRead;
                remaining -= bytesRead;
            }
            bufOut.flush();

            System.out.println(">> [THÀNH CÔNG]: Đã nhận file '" + fileName + "' (" + totalReceived + " bytes)!");
            System.out.println(">> File đã được lưu an toàn tại: " + targetFile.getAbsolutePath());

        } catch (IOException e) {
            System.err.println(">> [LỖI NHẬN FILE]: " + e.getMessage());
        }
    }
}
