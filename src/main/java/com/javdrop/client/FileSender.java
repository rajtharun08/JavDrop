package com.javdrop.client;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import com.javdrop.server.DiscoveryServer;

public class FileSender {
    public static void main(String[] args) {
        // Start the discovery service in a separate, background thread.
        Thread discoveryThread = new Thread(new DiscoveryServer());
        discoveryThread.setDaemon(true); // This makes it a background thread
        discoveryThread.start();
        // -----------------------

        int port = 6789;
        System.out.println("FileReceiver is starting... waiting for a client to connect.");

        String serverIp = "127.0.0.1"; // Use receiver's IP. 127.0.0.1 is for testing on the same machine.
        String filePath = "C:/path/to/your/file.txt";// Use a real file path on your system.
        
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("File not found: " + filePath);
            return;
        }

        try (Socket socket = new Socket(serverIp, port)) {
            System.out.println("Connected to server: " + serverIp);

            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            FileInputStream fis = new FileInputStream(file);

            dos.writeUTF(file.getName());
            dos.writeLong(file.length());
            System.out.println("Sending file: " + file.getName() + " (" + file.length() + " bytes)");

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }

            dos.flush();
            fis.close();
            dos.close();
            System.out.println("File sent successfully!");
        } catch (UnknownHostException e) {
            System.err.println("Server not found: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}