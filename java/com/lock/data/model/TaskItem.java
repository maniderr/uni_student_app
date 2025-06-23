package com.lock.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "taskItems")
public class TaskItem {
    @PrimaryKey(autoGenerate = true)
    public long id;

    private String taskName;
    private String course;
    private String date;
    private String source;
    private String username;

    public TaskItem(long id, String taskName, String course, String  date, String source, String username) {
        this.id = id;
        this.taskName = taskName;
        this.course = course;
        this.date = date;
        this.source = source;
        this.username = username;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getCourse() {
        return course;
    }

    public String getDate() {
        return date;
    }

    public String getSource() {
        return source;
    }

    public String getUsername() {
        return username;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
