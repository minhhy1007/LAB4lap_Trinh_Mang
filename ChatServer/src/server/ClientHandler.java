package server;

import java.net.Socket;

/**
 * Handles communication with a connected client.
 */
public class ClientHandler implements Runnable {
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        // Handle client messages
    }
}
