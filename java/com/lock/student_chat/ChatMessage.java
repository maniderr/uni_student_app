package com.lock.student_chat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatMessage {
    private Integer id;
    private String user;
    private String message;
    private String msg_time;

    public ChatMessage() {}

    public ChatMessage(String user, String message, String msg_time) {
        this.user = user;
        this.message = message;
        this.msg_time = msg_time;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMsg_time() {
        return msg_time;
    }

    public void setMsg_time(String msg_time) {
        this.msg_time = msg_time;
    }

    public Date getMsgTimeAsDate() {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            return format.parse(msg_time);
        } catch (ParseException e) {
            return null;
        }
    }
}
