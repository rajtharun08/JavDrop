#javDrop 🚀

A cross-platform desktop application built in Java for fast and reliable file transfer over a local area network (LAN).

This application uses a multi-threaded client-server model. UDP broadcasting is used for automatic device discovery, and TCP sockets are used for fast, reliable data transfer. The user interface is built with JavaFX and styled with CSS.

![javDrop App](https-placeholder-for-your-screenshot.png)
*(Recommendation: Take a screenshot of your app, add it to your project, and replace the link above)*

---

## Features
* **Automatic Device Discovery:** No need to manually type IP addresses. The application automatically discovers other `javDrop` clients on the local network.
* **High-Speed File Transfer:** Uses a TCP socket stream for fast and reliable transfer of any file type.
* **Modern User Interface:** A clean, responsive GUI built with JavaFX and styled with a modern dark theme.
* **Real-time Feedback:** Features a progress bar and status label to provide real-time feedback on transfer progress.
* **Multi-Threaded Server:** The file receiver is multi-threaded, allowing it to handle multiple incoming file transfers simultaneously.
* **Robust Error Handling:** The UI gracefully handles connection failures and other transfer errors with user-friendly alerts.

## Tech Stack
* **Core:** Java (JDK 11)
* **GUI:** JavaFX
* **Build:** Apache Maven (with Maven Shade Plugin)
* **Networking:** Java Sockets (TCP for file transfer, UDP for discovery)

## How to Build
This project is built with Maven. The final executable "fat JAR" can be built with the following command:
```bash
mvn clean package
This will generate a file at /target/javdrop-0.0.1-SNAPSHOT.jar.

**How to Use**
This application requires two components to be running: a Receiver (server) and a Sender (client GUI). You only need the single executable JAR file created in the build step.

**1. On the Receiving Computer:**
Open a terminal or command prompt and run the following command to start the FileReceiver in server mode:

Bash

java -cp javdrop-0.0.1-SNAPSHOT.jar com.javdrop.server.FileReceiver
The console will print: "Server is starting... waiting for a client to connect."

**2. On the Sending Computer:**
Simply double-click the executable javdrop-0.0.1-SNAPSHOT.jar file (or run java -jar javdrop-0.0.1-SNAPSHOT.jar) to launch the GUI.

The GUI will automatically discover the running receiver, and its IP address will appear in the list. You can then select the IP, click "Send File," and choose a file to transfer it.