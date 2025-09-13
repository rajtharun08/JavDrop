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
    private final Consumer<String> errorHandler;
    private final ProgressBar progressBar; // Add field for the ProgressBar

    // Update the constructor to accept the ProgressBar
    public FileSender(String host, int port, File file, Consumer<String> statusUpdater, Consumer<String> errorHandler, ProgressBar progressBar) {
        this.host = host;
        this.port = port;
        this.file = file;
        this.statusUpdater = statusUpdater;
        this.errorHandler=errorHandler;
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
            
            int lastPercentReported = 0; 

            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
                totalBytesSent += bytesRead;
                
                double progress = (double) totalBytesSent / fileSize;
                int percentDone = (int) (progress * 100); 
                
                if (percentDone > lastPercentReported) {
                    lastPercentReported = percentDone;
                    Platform.runLater(() -> progressBar.setProgress(progress));
                }
            }

            dos.flush();
            fis.close();

            Platform.runLater(() -> progressBar.setProgress(1.0)); 
            Platform.runLater(() -> statusUpdater.accept("File '" + file.getName() + "' sent successfully!"));

        } catch (IOException e) { // This is now the simplified, original catch block
            Platform.runLater(() -> {
                errorHandler.accept("Connection lost or file transfer failed: " + e.getMessage());
                progressBar.setVisible(false); // Hide bar on error
            });
            e.printStackTrace();
        }
    }
}