/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package messagesystem;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.HashMap;
import javax.swing.JOptionPane;

/**
 * ClientListener listens for server notifications and private messages.
 * Each connection is handled in a separate thread.
 * 
 * @author andre
 */
public class ClientListener implements Runnable {
    private int port;
    private Boolean active = true;
    private static String[] messageArray;
    private static HashMap<String, PrivateMessageScreen> privateWindowMap = new HashMap<>();
    private static Thread thread;

    public ClientListener(int port) {
        this.port = port;
        
        thread = new Thread(this);
        thread.start();
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
            serverSocket.close();
        } catch (Exception e) {
            System.err.println("Error in ClientListener: " + e.getMessage());
        }
    }
    
    public void kill(){
        active = false;
        thread.interrupt();
    }
    
    public Boolean addPMWindow(String user, PrivateMessageScreen window) {
        privateWindowMap.putIfAbsent(user, window);
        return privateWindowMap.containsKey(user);
    }
    
    public Boolean removePMWindow(String user, PrivateMessageScreen window) {
        return privateWindowMap.remove(user, window);
    }

    /**
    * ConnectionHandler handles individual client connections in a separate thread.
    */
    private static class ConnectionHandler implements Runnable {
        private final Socket socket;
        private static String[] messageArray;

        public ConnectionHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                
                String incomingMessage;
                while ((incomingMessage = reader.readLine()) != null) {
                    // Determine message type and process accordingly
                    
                    System.out.println(incomingMessage);
                    if (incomingMessage.startsWith("NOTIFICATION ")) {
                        messageArray = incomingMessage.split(" ");
                        System.out.println("Server Notification: " + incomingMessage.substring("NOTIFICATION:".length()));

                        if (messageArray.length > 2) {
                            JOptionPane.showMessageDialog(null, messageArray[2] + " has followed you!", "New Notification", Integer.parseInt(messageArray[1]));
                        } else {
                            JOptionPane.showMessageDialog(null, incomingMessage);
                        }
                    } else if (incomingMessage.startsWith("PRIVATE MESSAGE REQUEST FROM")) {
                        messageArray = incomingMessage.split(" ");
                        if (messageArray.length > 4) {
                            String sendingUser = messageArray[4].trim();
                            int confirmRequest = JOptionPane.showConfirmDialog(null, "Private Message From " + sendingUser, incomingMessage, JOptionPane.OK_CANCEL_OPTION);

                            if (confirmRequest == JOptionPane.OK_OPTION) {
                                String outgoing = "CLIENT CONFIRMED SEND MESSAGE";
                                writer.println(outgoing);
                                System.out.println(outgoing);

                                incomingMessage = reader.readLine();
                                System.out.println(incomingMessage);

                                if (incomingMessage.startsWith("PRIVATE MESSAGE REQUEST ANSWER")) {
                                    messageArray = incomingMessage.split(" ");
                                    if (messageArray.length > 5) {
                                        String body = new String(Base64.getDecoder().decode(messageArray[4]));
                                        String IP = messageArray[5];

                                        PrivateMessageScreen privateMsg = new PrivateMessageScreen(sendingUser, IP);
                                        privateMsg.receiveMessage(body);
                                        privateMsg.setVisible(true);
                                        privateWindowMap.putIfAbsent(sendingUser, privateMsg);
                                        
                                        outgoing = "CLIENT PRIVATE MESSAGE RECEIVED";
                                        writer.println(outgoing);
                                        System.out.println(outgoing);
//                                        socket.close();
                                    }
                                }
                            }
                        }
                    } else if (incomingMessage.startsWith("PRIVATE MESSAGE FROM")) {
                        messageArray = incomingMessage.split(" ");
                        if (messageArray.length > 4) {
                            String sendingUser = messageArray[3].trim();
                            PrivateMessageScreen privateMsg = privateWindowMap.get(sendingUser);
                            if (privateMsg != null) {
                                String body = new String(Base64.getDecoder().decode(messageArray[4]));
                                privateMsg.receiveMessage(body);
                                
                                String outgoing = "CLIENT PRIVATE MESSAGE RECEIVED";
                                writer.println(outgoing);
                                System.out.println(outgoing);
                            }
                        }
                    } else if (incomingMessage.startsWith("PRIVATE MESSAGE CLOSING CHAT")) {
                        messageArray = incomingMessage.split(" ");
                        if (messageArray.length > 4) {
                            String sendingUser = messageArray[4].trim();
                            PrivateMessageScreen privateMsg = privateWindowMap.get(sendingUser);
                            if (privateMsg != null) {
                                privateMsg.receiveMessage("USER HAS DISCONNECTED");
                                
                                String outgoing = "CLIENT PRIVATE MESSAGE CHAT DISCONNECTED";
                                writer.println(outgoing);
                                System.out.println(outgoing);
                                
                                privateMsg.disableChat();
                                privateWindowMap.remove(sendingUser, privateMsg);
                            }
                        }
                    } else {
                        System.out.println("Unknown message type: " + incomingMessage);
                    }
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
    }
}