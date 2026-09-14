package client;

/**
 * Service responsible for client-side chat business logic and coordinating communication.
 */
public class ChatService {
    private final ServerConnection serverConnection;

    public ChatService(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }

    public void sendMessage(String message) {
        // Send public message
    }

    public void sendPrivateMessage(String recipient, String message) {
        // Send private message
    }
}
