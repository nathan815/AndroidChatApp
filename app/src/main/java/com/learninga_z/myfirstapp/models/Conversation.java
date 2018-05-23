package com.learninga_z.myfirstapp.models;

import com.google.firebase.Timestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Conversation {

    public String conversationId;
    public String name;
    public String color;
    public String latestMessage;
    public Timestamp createdOn;
    public Timestamp updatedOn;
    public Map<String, Boolean> users;

    public Conversation(String id) {
        this.conversationId = id;
        this.createdOn = new Timestamp(new Date());
        this.color = generateRandomColor();
        this.users = new HashMap<>();
    }

    public Conversation() {}

    private String generateRandomColor() {
        Random r = new Random();
        StringBuffer sb = new StringBuffer("#");
        while(sb.length() < 7){
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, 7);
    }
}
