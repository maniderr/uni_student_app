package com.lock.utils.enums;

public enum Role {
    STUDENT("Student"),
    ADMIN("Admin");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
