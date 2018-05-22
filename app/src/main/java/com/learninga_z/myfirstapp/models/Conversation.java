package com.learninga_z.myfirstapp.models;

import com.google.firebase.Timestamp;

import java.util.Date;

public class Conversation {

    public String conversationId;
    public String creatorUserId;
    public String otherUserId;
    public String name;
    public String latestMessage;
    public Timestamp updatedOn;
    public Timestamp createdOn;

    public Conversation(String id, String creatorUserId, String otherUserId, String latestMessage) {
        this.conversationId = id;
        this.creatorUserId = creatorUserId;
        this.otherUserId = otherUserId;
        this.latestMessage = latestMessage;
        this.createdOn = new Timestamp(new Date());
    }

    public Conversation(String creatorUserId, String otherUserId) {
        this(null, creatorUserId, otherUserId, null);
    }

    public Conversation() {

    }
}
