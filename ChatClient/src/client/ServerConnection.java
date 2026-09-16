package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * ServerConnection - Quản lý kết nối TCP Socket cấp thấp tới Chat Server.
 */
public class ServerConnection {
    private Socket socket;
    private final String host;
    private final int port;
    private BufferedReader reader;
    private PrintWriter writer;

    public ServerConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() throws IOException {
        this.socket = new Socket(host, port);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
    }

    public void send(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }

    public String readLine() throws IOException {
        return reader != null ? reader.readLine() : null;
    }

    public void disconnect() {
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {}
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
