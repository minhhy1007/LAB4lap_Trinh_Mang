package server;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for logging server events and activities.
 */
public class ServerLogger {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void log(String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        System.out.println("[" + timestamp + "] [INFO] " + message);
    }

    public static void error(String message, Throwable throwable) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        System.err.println("[" + timestamp + "] [ERROR] " + message);
        if (throwable != null) {
            throwable.printStackTrace();
        }
    }
}
