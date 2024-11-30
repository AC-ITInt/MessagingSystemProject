/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package messagesystem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * ClientListener listens for server notifications and private messages.
 * Each connection is handled in a separate thread.
 * 
 * @author andre
 */
public class ClientListener implements Runnable {
    private int port;
    private Boolean active = true;

    public ClientListener(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("ClientListener started on port " + port);

            while (active) {
                // Accept an incoming connection
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection received.");

                // Handle the connection in a new thread
                Thread clientHandler = new Thread(new ConnectionHandler(clientSocket));
                clientHandler.start();
            }
        } catch (Exception e) {
            System.err.println("Error in ClientListener: " + e.getMessage());
        }
    }

    /**
     * ConnectionHandler handles individual client connections in a separate thread.
     */
    private static class ConnectionHandler implements Runnable {
        private Socket socket;

        public ConnectionHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String incomingMessage;
                while ((incomingMessage = reader.readLine()) != null) {
                    handleIncomingMessage(incomingMessage);
                }
            } catch (Exception e) {
                System.err.println("Error while handling connection: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (Exception e) {
                    System.err.println("Error closing socket: " + e.getMessage());
                }
            }
        }

        /**
         * Processes the incoming message.
         * 
         * @param message The message from the client or server.
         */
        private void handleIncomingMessage(String message) {
            // Logic to process messages (notifications or private messages)
            if (message.startsWith("NOTIFICATION:")) {
                System.out.println("Server Notification: " + message.substring("NOTIFICATION:".length()));
            } else if (message.startsWith("PRIVATE:")) {
                System.out.println("Private Message: " + message.substring("PRIVATE:".length()));
            } else {
                System.out.println("Unknown message type: " + message);
            }
        }
    }
}