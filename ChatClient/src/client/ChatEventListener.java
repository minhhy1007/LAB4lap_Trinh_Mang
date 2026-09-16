package client;

import java.util.List;

/**
 * ChatEventListener - Interface Callback phân tách nghiệp vụ mạng Socket khỏi Giao diện đồ họa (GUI).
 */
public interface ChatEventListener {
    void onLoginSuccess(String username);
    void onLoginFailed(String reason);
    void onUsersUpdated(List<String> users);
    void onUserJoined(String username);
    void onUserLeft(String username);
    void onMessageReceived(String sender, String content);
    void onMessageSent(String receiver, String content);
    void onFileOffer(String transferId, String sender, String fileName, long fileSize);
    void onSystemNotification(String notification);
    void onDisconnected();
}
