package client;

/**
 * Defines communication protocol constants and commands between client and server.
 */
public class Protocol {
    public static final String CMD_LOGIN = "/login";
    public static final String CMD_LOGOUT = "/logout";
    public static final String CMD_MSG = "/msg";
    public static final String CMD_PRIVATE_MSG = "/pmsg";
    public static final String CMD_USERS = "/users";
    public static final String CMD_FILE_SEND = "/sendfile";
    public static final String CMD_FILE_ACCEPT = "/acceptfile";
    public static final String CMD_FILE_DENY = "/denyfile";

    // Response codes/status
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";
}
