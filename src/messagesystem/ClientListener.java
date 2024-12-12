/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package messagesystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Base64;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * ClientListener listens for server notifications and private messages.
 * Each connection is handled in a separate thread.
 * 
 * @author andre
 */
public class ClientListener implements Runnable {
    private static int port;
    private static Boolean active;
    private static String[] messageArray;
    private static HashMap<String, PrivateMessageScreen> privateWindowMap = new HashMap<>();
    private static Thread thread;
    private static String user;

    public ClientListener(int port) {
        this.port = port;
        active = true;
        
        user = MessageSystem.getUser();
        
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
                if (active) {
                    Thread clientHandler = new Thread(new ConnectionHandler(clientSocket));
                    clientHandler.start();
                }
            }
            System.out.println("ClientListener socket closing");
            serverSocket.close();
        } catch (Exception e) {
            System.err.println("Error in ClientListener: " + e.getMessage());
        }
    }
    
    public static void kill(){
        privateWindowMap.forEach((key, value) -> {
            value.sendDisconnect();
            value.dispose();
        });
        privateWindowMap.clear();
        System.out.println("ClientListener Killed");
        
        active = false;
        try {
            Socket self = new Socket("localhost", 2004);
            //thread.interrupt();
            //Dispose JFrames
            
            //in logout connect to own port and send logout
        } catch (IOException ex) {
            Logger.getLogger(ClientListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Boolean addPMWindow(String user, PrivateMessageScreen window) {
        privateWindowMap.putIfAbsent(user, window);
        return privateWindowMap.containsKey(user);
    }
    
    public Boolean removePMWindow(String user, PrivateMessageScreen window) {
        return privateWindowMap.remove(user, window);
    }
    
    public static Boolean chatExists(String user) {
        return privateWindowMap.containsKey(user);
    }
    
    public static PrivateMessageScreen getChatScreen(String user) {
        return privateWindowMap.get(user);
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
                //While incomingMessage != QUIT connect to yourself and shut down

                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

                boolean clientConnected = true;
                
                while (clientConnected) {
                    // Determine message type and process accordingly
                    try {
                        String incomingMessage = reader.readLine();
                        System.out.println(incomingMessage);
                        if (incomingMessage == null) {
                                // Client disconnected
                                System.out.println("Client disconnected: " + socket.getInetAddress().getHostAddress());
                                clientConnected = false;
                                break;
                        } else if (incomingMessage.equals("CLIENTSERVER SHUTDOWN " + user)) {
                            String outgoing = "CLIENT VALID SHUTTING DOWN"; 
                            System.out.println(outgoing);
                            writer.println(outgoing);
                            kill();
                        } else if (incomingMessage.startsWith("NOTIFICATION ")) {
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
                                            PrivateMessageScreen privateMsg;
                                            if (privateWindowMap.containsKey(sendingUser)) {
                                                privateMsg = privateWindowMap.get(sendingUser);
                                                privateMsg.receiveMessage(body);
                                                outgoing = "CLIENT PRIVATE MESSAGE CONTINUE RECEIVED";
                                                writer.println(outgoing);
                                                System.out.println(outgoing);
                                                
                                                socket.close();
                                            } else {
                                                privateMsg = new PrivateMessageScreen(sendingUser, IP);
                                                privateMsg.receiveMessage(body);
                                                privateMsg.setVisible(true);
                                                privateWindowMap.put(sendingUser, privateMsg);

                                                outgoing = "CLIENT PRIVATE MESSAGE RECEIVED";
                                                writer.println(outgoing);
                                                System.out.println(outgoing);
                                                socket.close();
                                            }
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
                                    socket.close();
                                }
                            }
                        } else if (incomingMessage.startsWith("PRIVATE MESSAGE CLOSING CHAT")) {
                            socket.close();
                            messageArray = incomingMessage.split(" ");
                            if (messageArray.length > 4) {
                                String sendingUser = messageArray[4].trim();
                                PrivateMessageScreen privateMsg = privateWindowMap.get(sendingUser);
                                if (privateMsg != null) {
                                    privateMsg.receiveMessage("USER HAS DISCONNECTED");

                                    privateMsg.disableChat();
                                    privateWindowMap.remove(sendingUser, privateMsg);
                                }
                            }
                        } else {
                            System.out.println("Unknown message type: " + incomingMessage);
                        }
                    } catch (SocketException se) {
                        System.out.println("Connection was closed by the client.");
                        clientConnected = false;
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error while handling connection: " + e.getMessage());
            }
        }
    }
}