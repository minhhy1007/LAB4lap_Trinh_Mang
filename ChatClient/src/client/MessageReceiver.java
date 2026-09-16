package client;

import java.io.IOException;

/**
 * MessageReceiver - Luồng chạy ngầm liên tục lắng nghe thông điệp từ Server và chuyển tiếp về ChatService.
 */
public class MessageReceiver implements Runnable {
    private final ServerConnection connection;
    private final ChatService chatService;
    private volatile boolean isRunning = true;

    public MessageReceiver(ServerConnection connection, ChatService chatService) {
        this.connection = connection;
        this.chatService = chatService;
    }

    @Override
    public void run() {
        try {
            String line;
            while (isRunning && (line = connection.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                chatService.handleIncomingMessage(line);
            }
        } catch (IOException e) {
            if (isRunning) {
                chatService.handleDisconnected();
            }
        } finally {
            isRunning = false;
        }
    }

    public void stop() {
        isRunning = false;
    }
}
