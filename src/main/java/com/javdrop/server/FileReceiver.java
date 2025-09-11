package com.javdrop.server;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FileReceiver {
    public static void main(String[] args) {
        int port = 6789;
        System.out.println("Server is starting... waiting for a client to connect.");

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected from: " + clientSocket.getInetAddress().getHostAddress());

            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
            String fileName = dis.readUTF();
            long fileSize = dis.readLong();
            System.out.println("Receiving file: " + fileName + " (" + fileSize + " bytes)");

            FileOutputStream fos = new FileOutputStream("received_" + fileName); // Prepend to avoid overwriting
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesRead = 0;

            while (totalBytesRead < fileSize && (bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalBytesRead))) != -1) {
                fos.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
            }

            fos.close();
            dis.close();
            System.out.println("File received successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}