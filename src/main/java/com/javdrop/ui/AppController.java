package com.javdrop.ui;

import com.javdrop.client.FileSender; // Used to create the file sending task
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar; // Import the ProgressBar component
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

    /*
     * This method is automatically called by JavaFX after the FXML file has been loaded.
     * It's the perfect place to set up the initial state of your UI.
     */
    @FXML
    public void initialize() {
        statusLabel.setText("Searching for devices...");
        progressBar.setVisible(false); // Hide the progress bar when the app starts.
        startDiscovery(); // Begin searching for other devices on the network.
    }

    /**
     * Starts a background thread to listen for broadcast messages from servers.
     * This runs continuously without freezing the user interface.
     */
    private void startDiscovery() {
        // Define the discovery task that will be run on a separate thread.
        Runnable discoveryTask = () -> {
            try (DatagramSocket socket = new DatagramSocket(9876)) {
                byte[] receiveData = new byte[1024];
                while (true) { // Loop forever to continuously listen for new devices
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    socket.receive(receivePacket); // This line waits until a packet is received.
                    String message = new String(receivePacket.getData(), 0, receivePacket.getLength());

                    // Check if the message is the one we are looking for.
                    if (message.equals("JavDrop_Server_Here")) {
                        String serverIp = receivePacket.getAddress().getHostAddress();

                        // Use Platform.runLater to safely update the UI from this background thread.
                        Platform.runLater(() -> {
                            // To avoid duplicates, only add the IP if it's not already in the list.
                            if (!deviceListView.getItems().contains(serverIp)) {
                                deviceListView.getItems().add(serverIp);
                                statusLabel.setText("Device found! Select a device to send a file.");
                            }
                        });
                    }
                }
            } catch (IOException e) {
                // In a real app, you might want to show an error to the user here.
                e.printStackTrace();
            }
        };

        // Create the background thread, set it as a daemon, and start it.
        Thread discoveryThread = new Thread(discoveryTask);
        discoveryThread.setDaemon(true); // Ensures the thread stops when the main app closes.
        discoveryThread.start();
    }

    //This method is called when the "Send File" button is clicked.
    @FXML
    private void handleSendFileButton() {
        // 1. Get the device the user has selected from the list.
        String selectedIp = deviceListView.getSelectionModel().getSelectedItem();
        if (selectedIp == null) {
            showAlert("No Device Selected", "Please select a device from the list to send the file to.");
            return; // Stop the method if nothing is selected.
        }

        // 2. Open a standard "Open File" dialog to let the user choose a file.
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Send");
        File selectedFile = fileChooser.showOpenDialog(new Stage()); // Requires a new Stage/Window.

        // 3. If the user selected a file (and didn't click cancel), proceed.
        if (selectedFile != null) {
            // Update the UI to show that we are starting the transfer.
            statusLabel.setText("Preparing to send " + selectedFile.getName() + "...");
            progressBar.setVisible(true); // Make the progress bar visible.
            progressBar.setProgress(0.0);   // Reset its progress to the beginning.

            // 4. Create and run the FileSender task on a new background thread.
            // We pass it the IP, port, file, a way to update the status label, and the progress bar.
            FileSender senderTask = new FileSender(selectedIp, 6789, selectedFile, statusLabel::setText, progressBar);
            Thread senderThread = new Thread(senderTask);
            senderThread.setDaemon(true);
            senderThread.start();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null); // We don't need a header.
        alert.setContentText(message);
        alert.showAndWait(); // This shows the alert and waits for the user to close it.
    }
}