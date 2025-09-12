package com.javdrop.ui;

import com.javdrop.client.FileSender; // Import our new FileSender
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
import javafx.stage.FileChooser; // Import the FileChooser
import javafx.stage.Stage;

public class AppController {

    @FXML
    private ListView<String> deviceListView;
    @FXML
    private Label statusLabel;
    @FXML
    private Button sendFileButton;

    @FXML
    public void initialize() {
        statusLabel.setText("Searching for devices...");
        startDiscovery();
    }

    private void startDiscovery() {
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
                                statusLabel.setText("Device found! Select a device to send a file.");
                            }
                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        Thread discoveryThread = new Thread(discoveryTask);
        discoveryThread.setDaemon(true);
        discoveryThread.start();
    }

    @FXML
    private void handleSendFileButton() {
        // 1. Get the selected device from the list
        String selectedIp = deviceListView.getSelectionModel().getSelectedItem();
        if (selectedIp == null) {
            showAlert("No Device Selected", "Please select a device from the list to send the file to.");
            return;
        }

        // 2. Open a file chooser dialog for the user
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Send");
        File selectedFile = fileChooser.showOpenDialog(new Stage()); // Requires a new Stage/Window

        if (selectedFile != null) {
            // 3. Create and run the FileSender task on a background thread
            statusLabel.setText("Preparing to send " + selectedFile.getName() + "...");
            FileSender senderTask = new FileSender(selectedIp, 6789, selectedFile, statusLabel::setText);
            Thread senderThread = new Thread(senderTask);
            senderThread.setDaemon(true);
            senderThread.start();
        }
    }

    // A helper method to easily show alert pop-ups
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}