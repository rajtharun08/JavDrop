package com.javdrop.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class DiscoveryClient {

    public static void main(String[] args) {
        // We need to listen on the exact same port the server is shouting on.
        int discoveryPort = 9876;
        System.out.println("Discovery client started. Listening for servers...");

        try (DatagramSocket socket = new DatagramSocket(discoveryPort)) {
            // Create a buffer to hold the incoming message.
            byte[] receiveData = new byte[1024];

            while (true) { // Listen indefinitely
                // This is the "envelope" where we'll receive the letter.
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                
                // This line waits patiently until it hears a shout on the network.
                socket.receive(receivePacket);

                // When we get a packet, we convert its data into a readable string.
                String message = new String(receivePacket.getData(), 0, receivePacket.getLength());

                // We check if it's the secret message we're looking for.
                if (message.equals("JavDrop_Server_Here")) {
                    // Success! The packet itself tells us the sender's IP address.
                    InetAddress serverAddress = receivePacket.getAddress();
                    System.out.println("Found a javDrop server at: " + serverAddress.getHostAddress());

                    // In the final app, we would add this server to a list in the GUI.
                    // For now, we'll just print it and stop listening.
                    break; 
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}