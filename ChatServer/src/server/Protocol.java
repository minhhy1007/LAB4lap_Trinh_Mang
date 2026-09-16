package server;

/**
 * Protocol - Định nghĩa giao thức chuẩn giữa Client và Server (Hỗ trợ Chat và Truyền File).
 */
public class Protocol {
    // Client gửi lên Server
    public static final String CMD_LOGIN = "LOGIN";                  // LOGIN|username
    public static final String CMD_GET_USERS = "GET_USERS";          // GET_USERS
    public static final String CMD_MESSAGE = "MESSAGE";              // MESSAGE|receiver|content
    public static final String CMD_LOGOUT = "LOGOUT";                // LOGOUT

    // File transfer (Phase 5)
    public static final String CMD_FILE_REQUEST = "FILE_REQUEST";    // FILE_REQUEST|receiver|fileName|fileSize
    public static final String CMD_FILE_ACCEPT = "FILE_ACCEPT";      // FILE_ACCEPT|transferId
    public static final String CMD_FILE_REJECT = "FILE_REJECT";      // FILE_REJECT|transferId

    // Server phản hồi về Client
    public static final String RES_LOGIN_SUCCESS = "LOGIN_SUCCESS";  // LOGIN_SUCCESS
    public static final String RES_LOGIN_FAILED = "LOGIN_FAILED";    // LOGIN_FAILED|reason
    public static final String RES_USERS = "USERS";                  // USERS|Alice,Bob,Charlie
    public static final String RES_MESSAGE_FROM = "MESSAGE_FROM";    // MESSAGE_FROM|sender|content
    public static final String RES_USER_JOINED = "USER_JOINED";      // USER_JOINED|username
    public static final String RES_USER_LEFT = "USER_LEFT";          // USER_LEFT|username
    public static final String RES_ERROR = "ERROR";                  // ERROR|message

    // File transfer responses
    public static final String RES_FILE_OFFER = "FILE_OFFER";        // FILE_OFFER|transferId|sender|fileName|fileSize
    public static final String RES_FILE_ACCEPT = "FILE_ACCEPT";      // FILE_ACCEPT|transferId|fileName
    public static final String RES_FILE_REJECT = "FILE_REJECT";      // FILE_REJECT|transferId|reason
    public static final String RES_FILE_COMPLETE = "FILE_COMPLETE";  // FILE_COMPLETE|transferId
    public static final String RES_FILE_FAILED = "FILE_FAILED";      // FILE_FAILED|transferId|reason

    public static final String SEPARATOR = "\\|";
    public static final String DELIMITER = "|";
    public static final int FILE_PORT = 8889;                         // Cổng truyền file nhị phân
}
