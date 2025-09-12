package com.javdrop.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // This line loads our user interface from the FXML file.
        Parent root = FXMLLoader.load(getClass().getResource("app.fxml"));
        
        // This sets up the main window (called a "Stage") with our UI.
        primaryStage.setTitle("javDrop");
        primaryStage.setScene(new Scene(root, 400, 500)); // Width=400, Height=500
        primaryStage.show();
    }

    public static void main(String[] args) {
        // This is the standard way to launch a JavaFX application.
        launch(args);
    }
}
