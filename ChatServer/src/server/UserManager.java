package server;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages user registration, authentication, and online state.
 */
public class UserManager {
    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();

    public boolean registerUser(String username) {
        return onlineUsers.add(username);
    }

    public void removeUser(String username) {
        onlineUsers.remove(username);
    }

    public boolean isUserOnline(String username) {
        return onlineUsers.contains(username);
    }

    public Set<String> getOnlineUsers() {
        return onlineUsers;
    }
}
