package client;

/**
 * Background thread/worker responsible for receiving incoming messages from the server.
 */
public class MessageReceiver implements Runnable {
    private final ServerConnection connection;

    public MessageReceiver(ServerConnection connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        // Continuously listen for incoming server messages
    }
}
