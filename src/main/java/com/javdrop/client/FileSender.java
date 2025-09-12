package com.javdrop.client;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;

public class FileSender implements Runnable {
    private final String host;
    private final int port;
    private final File file;
    private final Consumer<String> statusUpdater;
    private final ProgressBar progressBar; // Add field for the ProgressBar

    // Update the constructor to accept the ProgressBar
    public FileSender(String host, int port, File file, Consumer<String> statusUpdater, ProgressBar progressBar) {
        this.host = host;
        this.port = port;
        this.file = file;
        this.statusUpdater = statusUpdater;
        this.progressBar = progressBar;
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(host, port)) {
            Platform.runLater(() -> statusUpdater.accept("Connected. Sending file..."));
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            FileInputStream fis = new FileInputStream(file);
            dos.writeUTF(file.getName());
            dos.writeLong(file.length());

            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesSent = 0;
            long fileSize = file.length();

            // This is the main transfer loop
            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
                totalBytesSent += bytesRead;
                // Calculate progress as a value between 0.0 and 1.0
                double progress = (double) totalBytesSent / fileSize;
                // Safely update the progress bar on the UI thread
                Platform.runLater(() -> progressBar.setProgress(progress));
            }
            fis.close();
            Platform.runLater(() -> statusUpdater.accept("File sent successfully!"));
        } catch (IOException e) {
            Platform.runLater(() -> {
                statusUpdater.accept("Error: " + e.getMessage());
                progressBar.setVisible(false); // Hide bar on error
            });
            e.printStackTrace();
        }
    }
}