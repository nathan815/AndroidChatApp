package com.learninga_z.myfirstapp;

public class Message {
    private String text;
    private MemberData member;
    private boolean isMine;

    public Message(String text, MemberData member, boolean isMine) {
        this.text = text;
        this.member = member;
        this.isMine = isMine;
    }

    public String getText() {
        return text;
    }

    public MemberData getMember() {
        return member;
    }

    public boolean isMine() {
        return isMine;
    }
}
