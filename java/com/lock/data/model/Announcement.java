package com.lock.data.model;

public class Announcement {
    private Long id;
    private String ancmnt_text;
    private String ancmnt_date;

    public Long getId() {
        return id;
    }

    public String getAncmnt_text() {
        return ancmnt_text;
    }

    public void setAncmnt_text(String ancmnt_text) {
        this.ancmnt_text = ancmnt_text;
    }

    public String getAncmnt_date() {
        return ancmnt_date;
    }

    public void setAncmnt_date(String ancmnt_date) {
        this.ancmnt_date = ancmnt_date;
    }
}
