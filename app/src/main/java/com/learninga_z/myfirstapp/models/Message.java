package com.learninga_z.myfirstapp.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.Date;

@IgnoreExtraProperties
public class Message {

    @Exclude public static final int TYPE_MESSAGE = 0;
    @Exclude public static final int TYPE_DATE_HEADER = 1;

    private String userId;
    private String text;
    private Timestamp sentOn;

    @Exclude private boolean isMine;
    @Exclude private int messageType = TYPE_MESSAGE;

    public Message(String userId, String text) {
        this(TYPE_MESSAGE, userId, text, new Timestamp(new Date()));
    }

    public Message() {}

    public Message(int type, String userId, String text, Timestamp time) {
        this.messageType = type;
        this.userId = userId;
        this.text = text;
        this.sentOn = time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getSentOn() {
        return sentOn;
    }

    public void setSentOn(Timestamp sentOn) {
        this.sentOn = sentOn;
    }

    public int getMessageType() {
        return messageType;
    }

    public boolean isMine() {
        return isMine;
    }
    public void setIsMine(boolean mine) {
        isMine = mine;
    }

    @Override
    public String toString() {
        return "Message{" +
                "userId='" + userId + '\'' +
                ", text='" + text + '\'' +
                ", sentOn=" + sentOn +
                ", isMine=" + isMine +
                '}';
    }
}
