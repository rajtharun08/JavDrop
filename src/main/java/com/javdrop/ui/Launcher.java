package com.javdrop.ui;


 //This is a "wrapper" class. Its only purpose is to start the main JavaFX application.
 //This is a standard workaround for the JavaFX/JAR modularity issue.
public class Launcher {
    public static void main(String[] args) {
        Main.main(args);
    }
}