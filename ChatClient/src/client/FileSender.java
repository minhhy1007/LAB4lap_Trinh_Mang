package client;

import java.io.File;

/**
 * Handles uploading/sending files from the client to the server or peer.
 */
public class FileSender implements Runnable {
    private final File file;
    private final String recipient;

    public FileSender(File file, String recipient) {
        this.file = file;
        this.recipient = recipient;
    }

    @Override
    public void run() {
        // Send file data
    }
}
