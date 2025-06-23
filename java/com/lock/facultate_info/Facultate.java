package com.lock.facultate_info;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "facultati")
public class Facultate {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String site;
    private String address;
    private String phone;
    private String fax;
    private String email;

    public Facultate(String name, String site, String address, String phone, String fax, String email) {
        this.name = name;
        this.site = site;
        this.address = address;
        this.phone = phone;
        this.fax = fax;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSite() {
        return site;
    }

    public String getAddress() {
        return address;
    }

    public String getFax() {
        return fax;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}