package com.learninga_z.myfirstapp.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.Date;

@IgnoreExtraProperties
public class Message {

    private String userId;
    private String text;
    private Timestamp sentOn;

    @Exclude
    private boolean isMine;

    public Message(String userId, String text) {
        this.userId = userId;
        this.text = text;
        this.sentOn = new Timestamp(new Date());
    }

    public Message() {}

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

    @Exclude
    public boolean isMine() {
        return isMine;
    }
    public void setIsMine(boolean mine) {
        isMine = mine;
    }
}
