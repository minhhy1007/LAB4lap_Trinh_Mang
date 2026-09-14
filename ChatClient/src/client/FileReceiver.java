package client;

import java.io.File;

/**
 * Handles downloading/receiving files sent to the client.
 */
public class FileReceiver implements Runnable {
    private final File destination;
    private final long fileSize;

    public FileReceiver(File destination, long fileSize) {
        this.destination = destination;
        this.fileSize = fileSize;
    }

    @Override
    public void run() {
        // Receive and save file data
    }
}
