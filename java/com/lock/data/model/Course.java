package com.lock.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.api.client.util.DateTime;
import com.lock.utils.enums.Faculty;
import com.lock.utils.enums.UserGroup;
import com.lock.utils.enums.Year;

import java.util.List;

@Entity(tableName = "courses")
public class Course {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public String hour;
    public String location;
    public String day;
    public int duration;

    public Faculty faculty;
    public Year year;
    public String section;
    public List<UserGroup> groups;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Faculty getFaculty() { return faculty; }

    public void setFaculty(Faculty faculty) { this.faculty = faculty; }

    public Year getYear() { return year; }

    public void setYear(Year year) { this.year = year; }

    public String getSection() { return section; }

    public void setSection(String section) { this.section = section; }

    public List<UserGroup> getGroups() { return groups; }

    public void setGroups(List<UserGroup> groups) { this.groups = groups; }

    public Course(long id, String name, String hour, String location, String day, int duration,
                  Faculty faculty, Year year, String section, List<UserGroup> groups) {

        this.id = id;
        this.name = name;
        this.hour = hour;
        this.location = location;
        this.day = day;
        this.duration = duration;
        this.faculty = faculty;
        this.year = year;
        this.section = section;
        this.groups = groups;
    }

    public Course() {
    }
}
