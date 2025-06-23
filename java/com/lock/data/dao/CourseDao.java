package com.lock.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverter;
import androidx.room.Update;

import com.lock.data.model.Course;
import com.lock.utils.enums.Faculty;
import com.lock.utils.enums.UserGroup;
import com.lock.utils.enums.Year;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface CourseDao {

    @Insert
    void insert(Course course);

    @Insert
    void insertAll(List<Course> courses);

    @Query("SELECT *FROM courses")
    List<Course> getAllCourses();

    @Query("SELECt *from courses WHERE id = :courseId")
    Course getCourseById(long courseId);

    @Update
    void update(Course course);

    @Delete
    void delete(Course course);

    public class Converters {
        @TypeConverter
        public static Faculty fromFacultyString(String value) {
            return value == null ? null : Faculty.valueOf(value);
        }

        @TypeConverter
        public static String facultyToString(Faculty faculty) {
            return faculty == null ? null : faculty.name();
        }

        @TypeConverter
        public static Year fromYearInt(int value) {
            for (Year year : Year.values()) {
                if (year.getValue() == value) {
                    return year;
                }
            }
            return null;
        }

        @TypeConverter
        public static int yearToInt(Year year) {
            return year == null ? -1 : year.getValue();
        }

        @TypeConverter
        public static List<UserGroup> fromGroupString(String value) {
            if (value == null || value.isEmpty()) {
                return new ArrayList<>();
            }
            String[] groupNames = value.split(",");
            List<UserGroup> groups = new ArrayList<>();
            for (String name : groupNames) {
                groups.add(UserGroup.valueOf("GROUP_" + name));
            }
            return groups;
        }

        @TypeConverter
        public static String groupsToString(List<UserGroup> groups) {
            if (groups == null || groups.isEmpty()) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            for (UserGroup group : groups) {
                if (sb.length() > 0) sb.append(",");
                sb.append(group.getValue());
            }
            return sb.toString();
        }
    }

    @Query("SELECT * FROM courses WHERE " +
            "(faculty IS NULL OR faculty = :faculty) AND " +
            "(year IS NULL OR year = :year) AND " +
            "(section IS NULL OR section = :section) AND " +
            "(groups IS NULL OR groups LIKE '%' || :group || '%')")
    List<Course> getCoursesForStudent(String faculty, int year, String section, String group);

    @Query("SELECT * FROM courses WHERE " +
            "day = :day AND " +
            "faculty = :faculty AND " +
            "year = :year AND " +
            "section = :section AND " +
            "groups LIKE '%' || :groupValue || '%' AND " +
            "((:startTime >= CAST(hour AS INTEGER) AND :startTime < CAST(hour AS INTEGER) + duration) OR " +
            "(CAST(hour AS INTEGER) >= :startTime AND CAST(hour AS INTEGER) < :startTime + :duration)) AND " +
            "id != :excludeId")
    List<Course> findOverlappingCourses(String day, Faculty faculty, Year year, String section,
                                        int startTime, int duration, String groupValue, long excludeId);

}
