/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package messagesystem;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 *
 * @author andre
 */
public class PrivateMessageScreen extends javax.swing.JFrame {
    private String user;
    private String IP;
    private Boolean disabled = false;

    /**
     * Creates new form PrivateMessageScreen
     */
    public PrivateMessageScreen(String user, String IP) {
        initComponents();
        
        this.user = user;
        this.IP = IP;
        
        jLabel1.setText("Private Chat With " + user);
        jPanel1.setLayout(new BoxLayout(jPanel1,BoxLayout.Y_AXIS));
        
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }
    
    public void receiveMessage(String msg) {
        JLabel receivedMsg = new JLabel(msg);
        receivedMsg.setForeground(Color.BLUE);
        receivedMsg.setVisible(true);
        jPanel1.add(receivedMsg);
        jPanel1.revalidate();
    }
    
    public void addOutgoing(String msg) {
        JLabel receivedMsg = new JLabel(msg);
        receivedMsg.setForeground(Color.red);
        receivedMsg.setVisible(true);
        jPanel1.add(receivedMsg);
        jPanel1.revalidate();
    }
    
    public void sendFirstMessage() {
        while (true) {
            String message = JOptionPane.showInputDialog(null, "Enter your message:", "Send Message to " + user, JOptionPane.PLAIN_MESSAGE);

            // If the user cancels the dialog, exit the method
            if (message == null) {
                return;
            }

            // If the message is empty, show an error dialog and loop again
            if (message.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Message cannot be empty. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            // Proceed if a valid message is entered
            String encodedMessage = Base64.getEncoder().encodeToString(message.getBytes());

            try {
                Socket server = new Socket(IP, 2004);

                InputStream input = server.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                PrintWriter writer = new PrintWriter(server.getOutputStream(), true);

                System.out.println("ClientServer Socket Opened");

                String outgoing = "PRIVATE MESSAGE REQUEST FROM " + MessageSystem.getUser();
                writer.println(outgoing);
                System.out.println(outgoing);

                String incomingMessage = reader.readLine();
                System.out.println(incomingMessage);

                if ("CLIENT CONFIRMED SEND MESSAGE".equals(incomingMessage)) {
                    outgoing = "PRIVATE MESSAGE REQUEST ANSWER " + encodedMessage + " " + MessageSystem.getLocalIP();
                    writer.println(outgoing);
                    System.out.println(outgoing);

                    incomingMessage = reader.readLine();
                    if ("CLIENT PRIVATE MESSAGE RECEIVED".equals(incomingMessage)) {
                        System.out.println("PM Sent and Confirmed");
                        this.setVisible(true);
                        MessageSystem.addPM(user, this);
                        addOutgoing(message);
                    }
                }

            } catch (IOException ex) {
                Logger.getLogger(PrivateMessageScreen.class.getName()).log(Level.SEVERE, null, ex);
            }

            break; // Exit the loop after successful message sending
        }
    }

    
    public void sendDisconnect() {
        System.out.println(disabled.toString());
        if (!disabled) {
            try {
                Socket server = new Socket(IP, 2004);

                InputStream input = server.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                PrintWriter writer = new PrintWriter(server.getOutputStream(), true);

                System.out.println("ClientServer Socket Opened");

                String outgoing = "PRIVATE MESSAGE CLOSING CHAT " + MessageSystem.getUser();
                writer.println(outgoing);
                System.out.println(outgoing);
                
                MessageSystem.removePM(user, this);
                server.close();
                this.dispose();


            } catch (IOException ex) {
                Logger.getLogger(PrivateMessageScreen.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            this.dispose();
        }
    }
    
    public void disableChat() {
        jButton1.setVisible(false);
        jTextField1.setEnabled(false);
        disabled = true;
    }
    
    public Boolean isDisabled() {
        return disabled;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jButton1.setText("Send");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Back");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel1.setText("Private Chat With <user>");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 267, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 165, Short.MAX_VALUE)
        );

        jTextField1.setText("jTextField1");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(43, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(129, 129, 129))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(62, 62, 62))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jTextField1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton2)
                                .addGap(65, 65, 65)
                                .addComponent(jButton1)))
                        .addGap(86, 86, 86))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addGap(20, 20, 20))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        String message = jTextField1.getText();
        if (message != null) {
            String encodedMessage = Base64.getEncoder().encodeToString(message.getBytes());
            try {
                Socket server = new Socket(IP, 2004);

                InputStream input = server.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                PrintWriter writer = new PrintWriter(server.getOutputStream(), true);

                System.out.println("ClientServer Socket Opened");

                String outgoing = "PRIVATE MESSAGE FROM " + MessageSystem.getUser() + " " + encodedMessage;
                writer.println(outgoing);
                System.out.println(outgoing);
                String incomingMessage = reader.readLine();
                System.out.println(incomingMessage);
                if (incomingMessage.equals("CLIENT PRIVATE MESSAGE RECEIVED")) {
                    addOutgoing(message);
                } else {
                    System.out.println("Error unsent");
                }


            } catch (IOException ex) {
                Logger.getLogger(PrivateMessageScreen.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        sendDisconnect();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        sendDisconnect();
        System.out.println("Sent disconnect");
    }//GEN-LAST:event_formWindowClosing

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(PrivateMessageScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PrivateMessageScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PrivateMessageScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PrivateMessageScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PrivateMessageScreen(null, null).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
