package client;

import java.io.IOException;
import java.net.Socket;

/**
 * Manages the TCP socket connection to the chat server.
 */
public class ServerConnection {
    private Socket socket;
    private String host;
    private int port;

    public ServerConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() throws IOException {
        this.socket = new Socket(host, port);
    }

    public void disconnect() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    public Socket getSocket() {
        return socket;
    }
}
