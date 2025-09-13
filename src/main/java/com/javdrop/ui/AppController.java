package com.javdrop.ui;

import com.javdrop.client.FileSender;
import com.javdrop.server.DiscoveryServer;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar; // Import the ProgressBar component
import javafx.scene.control.ToggleButton;
import javafx.stage.FileChooser; // Import the FileChooser for selecting files
import javafx.stage.Stage;

public class AppController {

    // --- FXML Components ---
    // These variables are linked to the components in app.fxml using their fx:id.

    // The list that will display discovered IP addresses.
    @FXML
    private ListView<String> deviceListView;

    // A label at the bottom to show status messages to the user.
    @FXML
    private Label statusLabel;

    // The main "Send File" button.
    @FXML
    private Button sendFileButton;

    // The new progress bar to show file transfer progress.
    @FXML
    private ProgressBar progressBar;

    @FXML
    private ToggleButton listenToggle; //Link to our ToggleButton

    // --- Threads and Sockets for our server ---
    private Thread discoveryThread;
    private Thread receiverThread;
    private ServerSocket serverSocket;
    /*
     * This method is automatically called by JavaFX after the FXML file has been loaded.
     * It's the perfect place to set up the initial state of your UI.
     */
    @FXML
    public void initialize() {
        statusLabel.setText("Searching for devices...");
        progressBar.setVisible(false); // Hide the progress bar when the app starts.
        startDiscoveryClient();  // Begin searching for other devices on the network.
    }

    /**
     * This method starts the client-side discovery (listening for other servers)
     */
    private void startDiscoveryClient() {
        // This logic remains the same (copied from our previous version)
        Runnable discoveryTask = () -> {
            try (DatagramSocket socket = new DatagramSocket(9876)) {
                byte[] receiveData = new byte[1024];
                while (true) {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    socket.receive(receivePacket);
                    String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    if (message.equals("JavDrop_Server_Here")) {
                        String serverIp = receivePacket.getAddress().getHostAddress();
                        Platform.runLater(() -> {
                            if (!deviceListView.getItems().contains(serverIp)) {
                                deviceListView.getItems().add(serverIp);
                            }
                        });
                    }
                }
            } catch (IOException e) {
                if(!Thread.currentThread().isInterrupted()) {
                    e.printStackTrace();
                }
            }
        };
        Thread discoveryClientThread = new Thread(discoveryTask);
        discoveryClientThread.setDaemon(true);
        discoveryClientThread.start();
    }

    /**
     * NEW: This method is called when the ToggleButton is clicked.
     * It starts or stops the server components.
     */
    @FXML
    private void handleListenToggle() {
        if (listenToggle.isSelected()) {
            // --- USER WANTS TO START THE SERVER ---
            listenToggle.setText("Listening...");
            statusLabel.setText("Server started. Listening for files.");

            // 1. Start the DiscoveryServer (broadcasting our presence)
            discoveryThread = new Thread(new DiscoveryServer());
            discoveryThread.setDaemon(true);
            discoveryThread.start();

            // 2. Start the FileReceiver (listening for incoming files)
            receiverThread = new Thread(() -> {
                try {
                    serverSocket = new ServerSocket(6789);
                    while (!serverSocket.isClosed()) {
                        Socket clientSocket = serverSocket.accept(); // Wait for a client
                        Platform.runLater(() -> statusLabel.setText("Client connected: " + clientSocket.getInetAddress().getHostAddress()));
                        
                        // Handle the file transfer in yet another new thread
                        new Thread(() -> handleIncomingFile(clientSocket)).start();
                    }
                } catch (IOException e) {
                    if (serverSocket != null && !serverSocket.isClosed()) {
                         e.printStackTrace();
                    } else {
                        System.out.println("Server socket closed, receiver thread shutting down.");
                    }
                }
            });
            receiverThread.setDaemon(true);
            receiverThread.start();

        } else {
            // --- USER WANTS TO STOP THE SERVER ---
            listenToggle.setText("Start Listening for Files");
            statusLabel.setText("Server stopped. Ready.");
            stopServerThreads(); // Call our new shutdown helper method
        }
    }

    /**
     * NEW: This helper method contains the logic from our old FileReceiver.
     * It runs on its own thread for each incoming file.
     */
    private void handleIncomingFile(Socket clientSocket) {
        try (Socket activeSocket = clientSocket) {
            DataInputStream dis = new DataInputStream(activeSocket.getInputStream());
            String fileName = dis.readUTF();
            long fileSize = dis.readLong();
            
            Platform.runLater(() -> statusLabel.setText("Receiving file: " + fileName));

            FileOutputStream fos = new FileOutputStream("received_" + fileName);
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesRead = 0;

            while (totalBytesRead < fileSize && (bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalBytesRead))) != -1) {
                fos.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
            }

            fos.close();
            Platform.runLater(() -> statusLabel.setText("File received: " + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSendFileButton() {
        // This entire method remains the same as our last correct version
        String selectedIp = deviceListView.getSelectionModel().getSelectedItem();
        if (selectedIp == null) {
            showAlert("No Device Selected", "Please select a device from the list.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Send");
        File selectedFile = fileChooser.showOpenDialog(new Stage());

        if (selectedFile != null) {
            sendFileButton.setDisable(true);
            deviceListView.setDisable(true);
            statusLabel.setText("Preparing to send " + selectedFile.getName() + "...");
            progressBar.setVisible(true);
            progressBar.setProgress(0.0);

            Consumer<String> errorHandler = (errorMessage) -> {
                showAlert("Transfer Error", errorMessage);
                sendFileButton.setDisable(false);
                deviceListView.setDisable(false);
            };
            
            Consumer<String> successHandler = (successMessage) -> {
                statusLabel.setText(successMessage);
                sendFileButton.setDisable(false);
                deviceListView.setDisable(false);
            };

            FileSender senderTask = new FileSender(selectedIp, 6789, selectedFile,  successHandler, errorHandler, progressBar);
            Thread senderThread = new Thread(senderTask);
            senderThread.setDaemon(true);
            senderThread.start();
        }
    }
    
    private void showAlert(String title, String message) {
        // This method also remains the same
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // --- NEW HELPER METHOD ---
    // Contains the logic to cleanly shut down our server threads.
    private void stopServerThreads() {
        // 1. Stop the DiscoveryServer broadcast
        if (discoveryThread != null) {
            discoveryThread.interrupt();
        }
        // 2. Stop the FileReceiver loop
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close(); // This will cause the .accept() loop to throw an exception
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // --- NEW SHUTDOWN METHOD ---
    /**
     * This public method is called by Main.java when the user closes the window.
     * This ensures all background server threads are stopped gracefully.
     */
    public void shutdown() {
        System.out.println("Shutting down servers...");
        stopServerThreads(); // Stop the server threads
        // The client-side discovery thread is already a daemon, so it will close automatically.
    }
}