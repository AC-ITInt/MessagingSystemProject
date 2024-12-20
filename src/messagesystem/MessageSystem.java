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
    private static LoginDialog loginDlg; //Login dialog
    private static RegisterUser register; //Register dialog
    
    private static String user;
    private static LinkedList<String> allUsers;
    private static LinkedList<String> followingUser;
    private static LinkedList<String> followersUser;
    private static LinkedList<Message> receivedMessages;
    private static LinkedList<Message> sentMessages;
    private static LinkedList<Message> searchMessages;
    private static Queue<Notification> notifQueue;
    private static ClientListener clientListener;
    private static String IPAddress;
    private static String serverIP = "216.159.153.136";//"192.168.1.201";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        loginDlg = new LoginDialog(null, true);
        loginDlg.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        register = new RegisterUser(null, true);
        register.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
//        loginDlg.regDlg = register;
        
        
        allUsers = new LinkedList<String>();
        followingUser = new LinkedList<String>();
        followersUser = new LinkedList<String>();
        
        receivedMessages = new LinkedList<>();
        sentMessages = new LinkedList<>();
        searchMessages = new LinkedList<>();
        
        notifQueue = new LinkedList<>();
        
        loginDlg.setVisible(true);
    }
    
    public static void showRegisterDlg(Boolean show) {
        register.setVisible(show);
    }
    
    public static void showLoginDlg (Boolean show) {
        loginDlg.setVisible(show);
    }
    
    public static String getServerIP() {
        return serverIP;
    }
    
    public static void setUser(String user) {
        user = "admin";
    }
    
    public static boolean isAFollower(String user) {
        return followingUser.contains(user);
    }
    
    public static void addFollower(String user) {
        followingUser.add(user);
    }
    
    public static void removeFollower(String user) {
        followingUser.remove(user);
    }
    
    public static LinkedList<Message> getReceivedMessages(){
        return receivedMessages;
    }
    
    public static LinkedList<Message> getSentMessages(){
        return sentMessages;
    }
    
    public static LinkedList<Message> getMessageList(int list){
        switch (list) {
            case 1 -> {
                return receivedMessages;
            }
            case 2 -> {
                return sentMessages;
            }
            default -> {
                return searchMessages;
            }
        }
    }
    
    public static void addSentMessage(Message msg) {
        sentMessages.add(msg);
    }
    
    
    public static LinkedList getUserList(int type) {
        try {
            Socket server = new Socket(serverIP, 2624);
            
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
            Socket server = new Socket(serverIP, 2624);
            
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
            Socket server = new Socket(serverIP, 2624);
            
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
            Socket server = new Socket(serverIP, 2624);
            
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
                    receivedMessages.clear();
                    System.exit(0);
                } else if (type == 1) {
                    try { //open local connection to close thread
                        Socket server = new Socket("localhost", 2004);
                        
                        InputStream input = server.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                        PrintWriter writer = new PrintWriter(server.getOutputStream(), true);

                        System.out.println("Close ClientListener socket opened");

                        String outgoing = "CLIENTSERVER SHUTDOWN " + user;
                        writer.println(outgoing);
                        String incomingMessage = reader.readLine();
                        if (!incomingMessage.equals("CLIENT VALID SHUTTING DOWN")) {
                            System.out.println("ClientListener shutdown error");
                        } else {
                            MessageSystem.userLogout(frame, username);
                            sentMessages.clear();
                            receivedMessages.clear();
                            loginDlg.setVisible(true);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(MessageSystem.class.getName()).log(Level.SEVERE, null, ex);
                    }
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
            
            Socket server = new Socket(serverIP, 2624);
            
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
    
    public static Boolean removePM(String user, PrivateMessageScreen screen) {
        return clientListener.removePMWindow(user, screen);
    }
}
