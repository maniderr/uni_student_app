package com.lock.utils.enums;

public enum UserGroup {
    GROUP_1("1"), GROUP_2("2"), GROUP_3("3"), GROUP_4("4"), GROUP_5("5"), GROUP_6("6");

    private final String value;

    UserGroup(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}