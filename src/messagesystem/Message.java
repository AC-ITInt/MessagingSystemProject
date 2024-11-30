/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package messagesystem;

import java.util.Set;

/**
 *
 * @author andre
 */
public class Message {
    String from;
    String body;
    String tags;
    
    public Message(String from, String body, String tags) {
        this.from = from;
        this.body = body;
        this.tags = tags;
    }
}
