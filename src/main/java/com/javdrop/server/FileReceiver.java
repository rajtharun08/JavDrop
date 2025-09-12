package com.javdrop.server;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FileReceiver {
    public static void main(String[] args) {
        // Start the discovery service in a separate, background thread.
        Thread discoveryThread = new Thread(new DiscoveryServer());
        discoveryThread.setDaemon(true); // This makes it a background thread
        discoveryThread.start();
        
        int port = 6789;
        System.out.println("Server is starting... waiting for a client to connect.");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) { // Loop to accept multiple connections
                Socket clientSocket = serverSocket.accept(); // This waits for a new client
                System.out.println("Client connected from: " + clientSocket.getInetAddress().getHostAddress());

                // Create a new thread to handle the file transfer for this client
                // This allows the server to accept another client while the file is transferring
                new Thread(() -> {
                    try (Socket activeSocket = clientSocket) { // Use the socket for this specific client
                        DataInputStream dis = new DataInputStream(activeSocket.getInputStream());
                        String fileName = dis.readUTF();
                        long fileSize = dis.readLong();
                        System.out.println("Receiving file: " + fileName + " (" + fileSize + " bytes)");

                        FileOutputStream fos = new FileOutputStream("received_" + fileName);
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        long totalBytesRead = 0;

                        while (totalBytesRead < fileSize && (bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalBytesRead))) != -1) {
                            fos.write(buffer, 0, bytesRead);
                            totalBytesRead += bytesRead;
                        }

                        fos.close();
                        System.out.println("File received successfully from " + activeSocket.getInetAddress().getHostAddress());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}