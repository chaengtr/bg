package com.example.myapplication.model;

import java.util.Date;

public class Message {
    private String sender, message;
    private Date time;

    public Message() {
    }

    public Message(String sender, String message, Date time) {
        this.sender = sender;
        this.message = message;
        this.time = time;
    }

    public String getSender() {
        return sender;
    }


    public String getMessage() {
        return message;
    }

    public Date getTime() {
        return time;
    }
}
