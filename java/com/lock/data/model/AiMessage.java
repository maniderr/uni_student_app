package com.lock.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "AiMessages")
public class AiMessage {
    @PrimaryKey(autoGenerate = true)
    private int id;

    public String username;
    private String role;
    private String content;
    public long timestamp;

    public AiMessage(String role, String content) {
        this.role = role;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername() { this.username =  username;}

    public void setId(int id) {
        this.id = id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public String getRole() {
        return role;
    }
}
