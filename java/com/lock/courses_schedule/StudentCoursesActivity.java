package com.lock.courses_schedule;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lock.R;
import com.lock.data.dao.UserDao;
import com.lock.data.database.AppDatabase;
import com.lock.data.model.Course;
import com.lock.data.model.User;
import com.lock.utils.enums.UserGroup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StudentCoursesActivity extends AppCompatActivity {
    private RecyclerView calendarGrid;
    private CourseAdapter adapter;
    private List<Course> courses = new ArrayList<>();
    private List<String> days = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
    private List<String> daysShort = Arrays.asList("Mon", "Tue", "Wed", "Thu", "Fri");
    private List<String> timeSlots = new ArrayList<>();
    private AppDatabase db;
    private static final String USERNAME_KEY = "username";
    private SharedPreferences sharedPreferences;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_calendar);

        sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        username = sharedPreferences.getString(USERNAME_KEY, "Guest");

        initializeTimeSlots();

        db = AppDatabase.getInstance(this);

        calendarGrid = findViewById(R.id.calendarGrid);
        setupCalendarView();

        loadFilteredCourses();
    }

    private void initializeTimeSlots() {
        for (int hour = 8; hour <= 22; hour++) {
            timeSlots.add(String.format(Locale.getDefault(), "%02d:00", hour));
        }
    }

    private void loadFilteredCourses() {
        new Thread(() -> {
            User currentUser = db.userDao().getUserByUsername(username);

            if (currentUser == null) {
                runOnUiThread(() -> Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show());
                return;
            }

            String facultyStr = currentUser.getFaculty() != null ? currentUser.getFaculty().name() : null;
            int year = currentUser.getYear() != null ? currentUser.getYear().getValue() : -1;
            String section = currentUser.getSection();
            String group = currentUser.getGroup() != null ? currentUser.getGroup().getValue() : null;

            List<Course> filteredCourses = db.courseDao().getCoursesForStudent(
                    facultyStr, year, section, group);

            runOnUiThread(() -> {
                courses.clear();
                courses.addAll(filteredCourses);
                adapter.setCourses(courses);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void setupCalendarView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 6);
        calendarGrid.setLayoutManager(layoutManager);

        adapter = new CourseAdapter(days, timeSlots);
        calendarGrid.setAdapter(adapter);
    }

    private void showCourseDialog(Course course) {
        String message = String.format(
                "Start hour: %s:00\n" +
                        "Day: %s\n" +
                        "Duration: %d hours\n" +
                        "Location: %s\n" +
                        "Faculty: %s\n" +
                        "Year: %s\n" +
                        "Section: %s\n" +
                        "Groups: %s",
                course.getHour(),
                course.getDay(),
                course.getDuration(),
                course.getLocation(),
                course.getFaculty() != null ? course.getFaculty().getDisplayName() : "All",
                course.getYear() != null ? String.valueOf(course.getYear().getValue()) : "All",
                course.getSection() != null ? course.getSection() : "All",
                course.getGroups() != null ? formatGroups(course.getGroups()) : "All");

        new AlertDialog.Builder(this)
                .setTitle(course.getName())
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private String formatGroups(List<UserGroup> groups) {
        if (groups == null || groups.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (UserGroup group : groups) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(group.getValue());
        }
        return sb.toString();
    }

    private class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CalendarViewHolder> {
        private List<String> days;
        private List<String> timeSlots;
        private List<Course> courses = new ArrayList<>();

        public CourseAdapter(List<String> days, List<String> timeSlots) {
            this.days = days;
            this.timeSlots = timeSlots;
        }

        public void setCourses(List<Course> courses) {
            this.courses = courses;
        }

        @Override
        public int getItemViewType(int position) {
            if (position < 6) return 0;
            if (position % 6 == 0) return 1;
            return 2;
        }

        @NonNull
        @Override
        public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            if (viewType == 0) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.calendar_header_cell, parent, false);
            } else if (viewType == 1) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.calendar_time_cell, parent, false);
            } else {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.calendar_course_cell, parent, false);
            }
            return new CalendarViewHolder(view, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
            int row = position / 6;
            int col = position % 6;

            if (row == 0) {
                if (col == 0) {
                    holder.textView.setText("Time");
                } else if (col <= daysShort.size()) {
                    holder.textView.setText(daysShort.get(col - 1));
                }
                return;
            }

            if (col == 0) {
                if (row - 1 < timeSlots.size()) {
                    holder.textView.setText(timeSlots.get(row - 1));
                }
                return;
            }

            String currentDay = days.get(col - 1);
            String currentTime = timeSlots.get(row - 1);

            List<Course> matchingCourses = findCoursesForCell(currentDay, currentTime);

            if (!matchingCourses.isEmpty()) {
                Course course = matchingCourses.get(0);
                holder.courseView.setVisibility(View.VISIBLE);
                holder.courseView.setBackgroundColor(getColorForCourse(course));

                if(isStartingCell(course, currentTime)) {
                    holder.courseName.setText(course.getName());
                    holder.courseLocation.setText(course.getLocation());
                } else {
                    holder.courseName.setText("");
                    holder.courseLocation.setText("");
                }

                holder.itemView.setPadding(0, 0, 0, 0);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
                params.setMargins(0, 0, 0, 0);
                holder.itemView.setLayoutParams(params);

                boolean isContinuation = isContinuationCell(course, currentDay, currentTime);
                holder.itemView.setBackgroundResource(isContinuation ? R.drawable.course_cell_no_divider : R.drawable.course_cell_with_divider);

                holder.itemView.setOnClickListener(v -> showCourseDialog(course));
            } else {
                holder.courseView.setVisibility(View.GONE);
                holder.itemView.setBackgroundResource(R.drawable.course_cell_with_divider);
            }
        }

        private boolean isStartingCell(Course course, String currentTime) {
            try {
                String courseTimeStr = formatTimeString(course.getHour());
                String cellTimeStr = formatTimeString(currentTime);
                return courseTimeStr.equals(cellTimeStr);
            } catch (Exception e) {
                return false;
            }
        }

        private boolean isContinuationCell(Course course, String currentDay, String currentTime) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Date current = sdf.parse(currentTime);
                Date courseStart = sdf.parse(formatTimeString(course.getHour()));
                long endTime = courseStart.getTime() + (course.getDuration() * 60 * 60 * 1000);
                Date nextHour = new Date(current.getTime() + (60 * 60 * 1000));

                return nextHour.getTime() < endTime;
            } catch (Exception e) {
                return false;
            }
        }

        private List<Course> findCoursesForCell(String day, String time) {
            List<Course> result = new ArrayList<>();
            for (Course course : courses) {
                if (course.getDay().equalsIgnoreCase(day)) {
                    try {
                        String courseTimeStr = formatTimeString(course.getHour());
                        String cellTimeStr = formatTimeString(time);

                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        Date courseTime = sdf.parse(courseTimeStr);
                        Date cellTime = sdf.parse(cellTimeStr);

                        long endTime = courseTime.getTime() + (course.getDuration() * 60 * 60 * 1000);
                        if (cellTime.getTime() >= courseTime.getTime() &&
                                cellTime.getTime() < endTime) {
                            result.add(course);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                        Log.e("CourseAdapter", "Error parsing time for course: " + course.getName(), e);
                    }
                }
            }
            return result;
        }

        private String formatTimeString(String time) {
            if (time.matches("\\d+")) {
                return String.format(Locale.getDefault(), "%02d:00", Integer.parseInt(time));
            }
            if (time.matches("\\d:[0-5]\\d")) {
                return "0" + time;
            }
            return time;
        }

        private int getColorForCourse(Course course) {
            return Color.HSVToColor(new float[]{
                    Math.abs(course.getName().hashCode()) % 360,
                    0.3f,
                    0.9f
            });
        }

        @Override
        public int getItemCount() {
            return (timeSlots.size() + 1) * 6;
        }

        class CalendarViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            View courseView;
            TextView courseName;
            TextView courseLocation;

            public CalendarViewHolder(@NonNull View itemView, int viewType) {
                super(itemView);

                if (viewType == 0) {
                    textView = itemView.findViewById(R.id.headerText);
                } else if (viewType == 1) {
                    textView = itemView.findViewById(R.id.timeText);
                } else {
                    textView = itemView.findViewById(R.id.cellText);
                    courseView = itemView.findViewById(R.id.courseView);
                    courseName = itemView.findViewById(R.id.courseName);
                    courseLocation = itemView.findViewById(R.id.courseLocation);
                }
            }
        }
    }
}
