package com.javdrop.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("app.fxml"));
        primaryStage.setTitle("javDrop");
        
        Scene scene = new Scene(root, 400, 500); // Create the scene
        
        // Load and apply the CSS stylesheet
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        
        primaryStage.setScene(scene); // Set the scene on the stage
        primaryStage.show();
    }

    public static void main(String[] args) {
        // This is the standard way to launch a JavaFX application.
        launch(args);
    }
}
