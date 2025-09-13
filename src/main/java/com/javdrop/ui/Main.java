package com.javdrop.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // We must create an FXMLLoader to get access to the controller.
        FXMLLoader loader = new FXMLLoader(getClass().getResource("app.fxml"));
        Parent root = loader.load();
        
        // Get the instance of the controller that the loader created.
        AppController controller = loader.getController();

        primaryStage.setTitle("javDrop");
        Scene scene = new Scene(root, 400, 500); 
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        
        // Set a shutdown hook. This runs when the user clicks the window's [X] button.
        primaryStage.setOnCloseRequest((event) -> {
            controller.shutdown(); // Tell the controller to shut down its threads.
        });
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}