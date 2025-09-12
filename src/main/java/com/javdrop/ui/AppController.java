package com.javdrop.ui;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class AppController {

    @FXML
    private ListView<String> deviceListView;

    @FXML
    private Label statusLabel;

    @FXML
    private Button sendFileButton;

    // This method is called automatically when the UI is first loaded.
    @FXML
    public void initialize() {
        statusLabel.setText("Searching for devices...");
        startDiscovery(); // We start our background search here.
    }

    private void startDiscovery() {
        // 1. Define the task to be run in the background.
        // We have copied the logic from our old DiscoveryClient here.
        Runnable discoveryTask = () -> {
            try (DatagramSocket socket = new DatagramSocket(9876)) {
                byte[] receiveData = new byte[1024];
                while (true) { // This loop runs forever in the background
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    socket.receive(receivePacket); // This waits for a server to broadcast
                    String message = new String(receivePacket.getData(), 0, receivePacket.getLength());

                    if (message.equals("JavDrop_Server_Here")) {
                        String serverIp = receivePacket.getAddress().getHostAddress();
                        
                        // 3. Safely update the UI from the background thread.
                        Platform.runLater(() -> {
                            if (!deviceListView.getItems().contains(serverIp)) {
                                deviceListView.getItems().add(serverIp);
                                statusLabel.setText("Device found!");
                            }
                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        // 2. Create the background thread, make it a daemon, and start it.
        Thread discoveryThread = new Thread(discoveryTask);
        discoveryThread.setDaemon(true);
        discoveryThread.start();
    }

    @FXML
    private void handleSendFileButton() {
        System.out.println("Send File button clicked!");
        statusLabel.setText("Attempting to send a file...");
    }
}