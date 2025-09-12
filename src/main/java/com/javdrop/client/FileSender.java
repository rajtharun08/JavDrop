package com.javdrop.client;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.function.Consumer;
import javafx.application.Platform;

// We make this class "Runnable" so we can easily run it in a background thread.
public class FileSender implements Runnable {

    private final String host;
    private final int port;
    private final File file;
    private final Consumer<String> statusUpdater; // A way to send status messages back to the UI

    public FileSender(String host, int port, File file, Consumer<String> statusUpdater) {
        this.host = host;
        this.port = port;
        this.file = file;
        this.statusUpdater = statusUpdater;
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(host, port)) {
            // Update UI: Connected
            Platform.runLater(() -> statusUpdater.accept("Connected to " + host + ". Sending file..."));

            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            FileInputStream fis = new FileInputStream(file);

            // Send file metadata
            dos.writeUTF(file.getName());
            dos.writeLong(file.length());

            // Send file content
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }

            dos.flush();
            fis.close();

            // Update UI: Success
            Platform.runLater(() -> statusUpdater.accept("File '" + file.getName() + "' sent successfully!"));

        } catch (IOException e) {
            // Update UI: Error
            Platform.runLater(() -> statusUpdater.accept("Error: " + e.getMessage()));
            e.printStackTrace();
        }
    }
}