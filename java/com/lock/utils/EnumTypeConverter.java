package com.lock.utils;

import com.lock.utils.enums.*;
import androidx.room.TypeConverter;

public class EnumTypeConverter {
    @TypeConverter
    public static String fromUserGroup(UserGroup group) {
        return group == null ? null : group.getValue();
    }

    @TypeConverter
    public static UserGroup toUserGroup(String value) {
        for (UserGroup group : UserGroup.values()) {
            if (group.getValue().equals(value)) {
                return group;
            }
        }
        return null;
    }

    @TypeConverter
    public static Integer fromYear(Year year) {
        return year == null ? null : year.getValue();
    }

    @TypeConverter
    public static Year toYear(Integer value) {
        for (Year year : Year.values()) {
            if (year.getValue() == value) {
                return year;
            }
        }
        return null;
    }

    @TypeConverter
    public static String fromFaculty(Faculty faculty) {
        return faculty == null ? null : faculty.getDisplayName();
    }

    @TypeConverter
    public static Faculty toFaculty(String value) {
        for (Faculty faculty : Faculty.values()) {
            if (faculty.getDisplayName().equals(value)) {
                return faculty;
            }
        }
        return null;
    }

    @TypeConverter
    public static String fromRole(Role role) {
        return role == null ? null : role.getValue();
    }

    @TypeConverter
    public static Role toRole(String value) {
        for (Role role : Role.values()) {
            if (role.getValue().equals(value)) {
                return role;
            }
        }
        return null;
    }
}
