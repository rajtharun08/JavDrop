package com.javdrop.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class AppController {

    // The @FXML annotation links these variables to the components in app.fxml
    @FXML
    private ListView<String> deviceListView;

    @FXML
    private Label statusLabel;

    @FXML
    private Button sendFileButton;

    // This method is called automatically when the UI is first loaded.
    @FXML
    public void initialize() {
        // We can set up initial states here.
        statusLabel.setText("Welcome to javDrop!");
        // In the next step, we will start the discovery client here.
    }

    // This method is linked to the "onAction" of the button in app.fxml
    @FXML
    private void handleSendFileButton() {
        System.out.println("Send File button clicked!");
        statusLabel.setText("Attempting to send a file...");
        // In the next step, this will open a file chooser and start the file transfer.
    }
}
