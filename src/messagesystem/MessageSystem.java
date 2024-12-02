/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package messagesystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author andre
 */
public class MessageSystem {
    static LoginDialog loginDlg; //Login dialog
    static RegisterUser register; //Register dialog
    
    public static String user;
    public static LinkedList<String> allUsers;
    public static LinkedList<String> followingUser;
    public static LinkedList<String> followersUser;
    public static LinkedList<Message> receivedMessages;
    public static LinkedList<Message> sentMessages;
    public static LinkedList<Message> searchMessages;
    private static Queue<Notification> notifQueue;
    private static ClientListener clientListener;
    private static String IPAddress;
    public static String ServerIP = "192.168.1.201";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        loginDlg = new LoginDialog(null, true);
        loginDlg.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        register = new RegisterUser(null, true, loginDlg);
        register.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        loginDlg.regDlg = register;
        
        
        allUsers = new LinkedList<String>();
        followingUser = new LinkedList<String>();
        followersUser = new LinkedList<String>();
        
        receivedMessages = new LinkedList<>();
        sentMessages = new LinkedList<>();
        searchMessages = new LinkedList<>();
        
        notifQueue = new LinkedList<>();
        
        loginDlg.setVisible(true);
    }
    
    public static LinkedList getUserList(int type) {
        try {
            Socket server = new Socket(ServerIP, 2624);
            
            InputStream input = server.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            
            PrintWriter writer = new PrintWriter(server.getOutputStream(), true);
            
            System.out.println("UserList Socket Opened");
            
            String listType;
            LinkedList chosenList;
                      
            switch (type) {
                case 1 -> {
                    listType = "FOLLOWING " + user;
                    chosenList = followingUser;
                }
                case 2 -> {
                    listType = "FOLLOWERS " + user;
                    chosenList = followersUser;
                }
                default -> {
                    listType = "ALL";
                    chosenList = allUsers;
                }
            }
            
            chosenList.clear();
            
            String outgoing = "SERVER USERLIST " + listType; //All
            System.out.println(outgoing);
            writer.println(outgoing);
            String incomingMessage = reader.readLine();
            if (incomingMessage.equals("CLIENT VALID SENDING USERLIST")) {
                outgoing = "SERVER USERLIST CONTINUE";
                writer.println(outgoing);
                incomingMessage = reader.readLine();
                while (!incomingMessage.equals("CLIENT USERLIST DONE")) {
                    String[] messageArray = incomingMessage.split(" ");
                    if (incomingMessage.startsWith("CLIENT USERLIST") && messageArray.length > 2) {
                        chosenList.add(messageArray[2].trim());
                    }
                    outgoing = "SERVER USERLIST CONTINUE";
                    writer.println(outgoing);
                    incomingMessage = reader.readLine();
                }
                return chosenList;
            }
            
        } catch (IOException ex) {
            Logger.getLogger(LoginDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new LinkedList(null);
    }
    
    public static Boolean searchPublicMessages(String tags) {
        searchMessages.clear();
        String encodedTags = Base64.getEncoder().encodeToString(tags.getBytes());
        try {
            Socket server = new Socket(ServerIP, 2624);
            
            InputStream input = server.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            
            PrintWriter writer = new PrintWriter(server.getOutputStream(), true);
            
            System.out.println("Search Message Socket Opened");
            
            String outgoing = "SERVER RETRIEVE MESSAGES LIKE " + encodedTags; 
            System.out.println(outgoing);
            writer.println(outgoing);
            String incomingMessage = reader.readLine();
            if (incomingMessage.equals("CLIENT VALID SENDING MESSAGES")) {
                outgoing = "SERVER MESSAGE RETRIEVAL CONTINUE";
                writer.println(outgoing);
                incomingMessage = reader.readLine();
                while (!incomingMessage.equals("CLIENT MESSAGE RETRIEVAL DONE")) {
                    String[] messageArray = incomingMessage.split(" ");
                    if (incomingMessage.startsWith("CLIENT MESSAGE") && messageArray.length > 4) {
                        String from = messageArray[2].trim();
                        String encodedBody = messageArray[3];
                        String body = new String(Base64.getDecoder().decode(encodedBody));
                        String encodedmsgTags = messageArray[4];
                        String msgtags = new String(Base64.getDecoder().decode(encodedmsgTags));
                        
                        Message msg = new Message(from, body, msgtags);
                        searchMessages.add(msg);
                        System.out.println("Message from " + from);
                    }
                    outgoing = "SERVER MESSAGE RETRIEVAL CONTINUE";
                    writer.println(outgoing);
                    incomingMessage = reader.readLine();
                }
                return true;
            }
            
        } catch (IOException ex) {
            Logger.getLogger(LoginDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public static Boolean retrieveNotifications() {
        try {
            Socket server = new Socket(ServerIP, 2624);
            
            InputStream input = server.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            
            PrintWriter writer = new PrintWriter(server.getOutputStream(), true);
            
            System.out.println("Retrieve Notification Socket Opened");
            
            String outgoing = "SERVER RETRIEVE NOTIFICATIONS TO " + user; 
            System.out.println(outgoing);
            writer.println(outgoing);
            String incomingMessage = reader.readLine();
            if (incomingMessage.equals("CLIENT VALID SENDING NOTIFICATIONS")) {
                outgoing = "SERVER NOTIFICATION RETRIEVAL CONTINUE";
                writer.println(outgoing);
                incomingMessage = reader.readLine();
                while (!incomingMessage.equals("CLIENT NOTIFICATION RETRIEVAL DONE")) {
                    String[] messageArray = incomingMessage.split(" ");
                    if (incomingMessage.startsWith("CLIENT NOTIFICATION") && messageArray.length > 3) {
                        int type = Integer.parseInt(messageArray[2]);
                        String encodedBody = messageArray[3];
                        String body = new String(Base64.getDecoder().decode(encodedBody));
                        
                        Notification notif = new Notification(type, body);
                        notifQueue.add(notif);
                        System.out.println("NOTIFICATION " + body);
                    }
                    outgoing = "SERVER NOTIFICATION RETRIEVAL CONTINUE";
                    writer.println(outgoing);
                    incomingMessage = reader.readLine();
                }
                return true;
            }
            
        } catch (IOException ex) {
            Logger.getLogger(LoginDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public static int getNotifQueueSize() {
        return notifQueue.size();
    }
    
    public static Notification pollNotification() {
        return notifQueue.poll();
    }
    
    public static Boolean retrievePublicMessages() {
        try {
            Socket server = new Socket(ServerIP, 2624);
            
            InputStream input = server.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            
            PrintWriter writer = new PrintWriter(server.getOutputStream(), true);
            
            System.out.println("Retrieve Message Socket Opened");
            
            String outgoing = "SERVER RETRIEVE MESSAGES TO " + user; 
            System.out.println(outgoing);
            writer.println(outgoing);
            String incomingMessage = reader.readLine();
            if (incomingMessage.equals("CLIENT VALID SENDING MESSAGES")) {
                outgoing = "SERVER MESSAGE RETRIEVAL CONTINUE";
                writer.println(outgoing);
                incomingMessage = reader.readLine();
                while (!incomingMessage.equals("CLIENT MESSAGE RETRIEVAL DONE")) {
                    String[] messageArray = incomingMessage.split(" ");
                    if (incomingMessage.startsWith("CLIENT MESSAGE") && messageArray.length > 4) {
                        String from = messageArray[2].trim();
                        String encodedBody = messageArray[3];
                        String body = new String(Base64.getDecoder().decode(encodedBody));
                        String encodedTags = messageArray[4];
                        String tags = new String(Base64.getDecoder().decode(encodedTags));
                        
                        Message msg = new Message(from, body, tags);
                        receivedMessages.add(msg);
                        System.out.println("Message from " + from);
                    }
                    outgoing = "SERVER MESSAGE RETRIEVAL CONTINUE";
                    writer.println(outgoing);
                    incomingMessage = reader.readLine();
                }
                return true;
            }
            
        } catch (IOException ex) {
            Logger.getLogger(LoginDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public static void exitProgram(JFrame frame, String username, int type) {
        if (type == 2) {
            System.exit(0);
        }
        int choice = JOptionPane.showConfirmDialog(null, "Logout " + username +"?", "Confirm Logout", JOptionPane.OK_CANCEL_OPTION);
        switch (choice) {
            case JOptionPane.OK_OPTION -> {
                if (type == 0) {
                    MessageSystem.userLogout(frame, username);
                    sentMessages.clear();
                    System.exit(0);
                } else if (type == 1) {
                    MessageSystem.userLogout(frame, username);
                    sentMessages.clear();
                    loginDlg.setVisible(true);
                }
            }
            case JOptionPane.CANCEL_OPTION -> {
                System.out.println("User canceled logout.");
                return;
            }
            default -> System.out.println("Logout dialog closed.");
        }
    }
    
//    public static void viewUserScreen(JFrame frame) {
//        UserListScreen screen = new UserListScreen(user);
//        screen.setVisible(true);
//        frame.dispose();
//    }
//    
//    public static void viewHomeScreen(JFrame frame) {
//        
//    }
    
    public static void userLogin(String username, String IP) {
        user = username;
        IPAddress = IP;
        
        retrieveNotifications();
        
        UserPublicScreen homeScreen = new UserPublicScreen(user);
        homeScreen.setVisible(true);
//        homeScreen.setAlwaysOnTop(true);
        
        clientListener = new ClientListener(2004);
    }
    
    public static Boolean userLogout(JFrame frame, String username) {
        String user = username;
        if (user.equals("admin")) {
            frame.dispose();
            return true;
        }
        try {
            
            Socket server = new Socket(ServerIP, 2624);
            
            InputStream input = server.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            
            PrintWriter writer = new PrintWriter(server.getOutputStream(), true);
            
            System.out.println("Logout Socket Opened");
            
            String outgoing = "SERVER LOGOUT " + user;
            writer.println(outgoing);
            System.out.println(outgoing);
            String incomingMessage = reader.readLine();
            System.out.println(incomingMessage);
            if (incomingMessage.equals("CLIENT VALID LOGGED OUT")) {
                System.out.println("Successful Logout");
                frame.dispose();
                
                clientListener.kill();
                clientListener = null;
                return true;
            } else {
                System.out.println("Logout Error");
                return false;
            }
        } catch (IOException ex) {
            Logger.getLogger(LoginDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public static String getLocalIP() {
        return IPAddress;
    }
    
    public static String getUser() {
        return user;
    }
    
    public static Boolean addPM(String user, PrivateMessageScreen screen) {
        return clientListener.addPMWindow(user, screen);
    }
}
