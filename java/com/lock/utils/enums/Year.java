package com.lock.utils.enums;

public enum Year {
    YEAR_1(1), YEAR_2(2), YEAR_3(3), YEAR_4(4);

    private final int value;

    Year(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}