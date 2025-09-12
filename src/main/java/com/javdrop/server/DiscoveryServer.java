package com.javdrop.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

// We make this class "Runnable" so it can run in its own thread
// without blocking our main file-receiving code.
public class DiscoveryServer implements Runnable {

    @Override
    public void run() {
        // A DatagramSocket is used for UDP, which is like sending letters
        // without knowing if they arrive. Perfect for broadcasting.
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);

            // This is our unique "shout" message. The client will listen for this exact message.
            String message = "JavDrop_Server_Here";
            byte[] sendData = message.getBytes();

            // We send the packet to a special "broadcast" address (255.255.255.255)
            // on a specific port. Any device listening on this port will hear us.
            int discoveryPort = 9876; // Different from our file transfer port
            InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcastAddress, discoveryPort);

            System.out.println("Discovery server started. Broadcasting our presence...");

            // This loop will run forever, shouting our message every 3 seconds.
            while (true) {
                socket.send(sendPacket);
                Thread.sleep(3000); // Wait for 3 seconds to avoid spamming the network
            }
        } catch (IOException | InterruptedException e) {
            // If something goes wrong, we'll print an error.
            e.printStackTrace();
        }
    }
}