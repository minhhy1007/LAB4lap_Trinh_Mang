package server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Manages active client socket connections.
 */
public class ConnectionManager {
    private final Map<String, ClientHandler> activeClients = new ConcurrentHashMap<>();

    public void addClient(String username, ClientHandler handler) {
        activeClients.put(username, handler);
    }

    public void removeClient(String username) {
        activeClients.remove(username);
    }

    public ClientHandler getClient(String username) {
        return activeClients.get(username);
    }

    public Map<String, ClientHandler> getActiveClients() {
        return activeClients;
    }
}
