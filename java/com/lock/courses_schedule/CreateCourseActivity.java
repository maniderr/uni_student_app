package com.lock.courses_schedule;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lock.R;
import com.lock.data.adapters.GroupAdapter;
import com.lock.data.adapters.HintAdapter;
import com.lock.data.database.AppDatabase;
import com.lock.data.model.Course;
import com.lock.utils.enums.Faculty;
import com.lock.utils.enums.UserGroup;
import com.lock.utils.enums.Year;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CreateCourseActivity extends AppCompatActivity {
    private EditText courseNameEditText, courseLocationEditText, courseTimeEditText, courseDurationEditText;
    private Spinner courseDaySpinner;
    private long courseId = -1;
    private ArrayAdapter<String> dayAdapter;
    private Spinner facultySpinner, yearSpinner, sectionSpinner;
    private RecyclerView groupsRecyclerView;
    private GroupAdapter groupAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_course);

        courseNameEditText = findViewById(R.id.courseNameEditText);
        courseLocationEditText = findViewById(R.id.courseLocationEditText);
        courseDaySpinner = findViewById(R.id.courseDaySpinner);
        courseTimeEditText = findViewById(R.id.courseTimeEditText);
        courseDurationEditText = findViewById(R.id.courseDurationEditText);

        setupDaySpinner();

        courseTimeEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        courseDurationEditText.setInputType(InputType.TYPE_CLASS_NUMBER);

        findViewById(R.id.saveCourseButton).setOnClickListener(v -> {
            if (validateInput()) {
                saveCourse();
            }
        });

        facultySpinner = findViewById(R.id.facultySpinner);
        yearSpinner = findViewById(R.id.yearSpinner);
        sectionSpinner = findViewById(R.id.sectionSpinner);
        groupsRecyclerView = findViewById(R.id.groupsRecyclerView);
        setupGroupsRecyclerView();

        setupFacultySpinner();
        setupYearSpinner();

        facultySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Skip hint selection
                    updateSectionsSpinner((Faculty) facultySpinner.getSelectedItem());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        if (getIntent().hasExtra("COURSE_ID")) {
            courseId = getIntent().getLongExtra("COURSE_ID", -1);
            loadCourseData(courseId);
        }
    }

    private void setupGroupsRecyclerView() {
        groupAdapter = new GroupAdapter(Arrays.asList(UserGroup.values()));
        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        groupsRecyclerView.setAdapter(groupAdapter);
    }

    private void setupFacultySpinner() {
        HintAdapter<Faculty> adapter = new HintAdapter<>(
                this,
                R.layout.item_register_spinner_dropdown,
                Faculty.values(),
                "Select Faculty"
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        facultySpinner.setAdapter(adapter);
    }

    private void setupYearSpinner() {
        HintAdapter<Year> adapter = new HintAdapter<>(
                this,
                R.layout.item_register_spinner_dropdown,
                Year.values(),
                "Select Year"
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(adapter);
    }

    private void updateSectionsSpinner(Faculty faculty) {
        HintAdapter<String> adapter = new HintAdapter<>(
                this,
                R.layout.item_register_spinner_dropdown,
                faculty.getSections(),
                "Select Section"
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sectionSpinner.setAdapter(adapter);
    }

    private void setupDaySpinner() {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, days);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseDaySpinner.setAdapter(dayAdapter);
    }

    private void loadCourseData(long courseId) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            Course course = db.courseDao().getCourseById(courseId);

            runOnUiThread(() -> {
                if (course != null) {
                    courseNameEditText.setText(course.getName());
                    courseLocationEditText.setText(course.getLocation());

                    int position = dayAdapter.getPosition(course.getDay());
                    if (position >= 0) {
                        courseDaySpinner.setSelection(position);
                    }

                    courseTimeEditText.setText(course.getHour());
                    courseDurationEditText.setText(String.valueOf(course.getDuration()));

                    if (course.getFaculty() != null) {
                        int facultyPos = ((HintAdapter<Faculty>) facultySpinner.getAdapter())
                                .getPosition(course.getFaculty());
                        facultySpinner.setSelection(facultyPos + 1);
                    }

                    updateSectionsSpinner(course.getFaculty());

                    if (course.getYear() != null) {
                        int yearPos = ((HintAdapter<Year>) yearSpinner.getAdapter())
                                .getPosition(course.getYear());
                        yearSpinner.setSelection(yearPos + 1);
                    }

                    if (course.getSection() != null) {
                        HintAdapter<String> sectionAdapter = (HintAdapter<String>) sectionSpinner.getAdapter();
                        int sectionPos = sectionAdapter.getPosition(course.getSection());
                        sectionSpinner.setSelection(sectionPos + 1);
                    }

                    if (course.getGroups() != null) {
                        groupAdapter.setSelectedGroups(course.getGroups());
                    }
                }
            });
        }).start();
    }

    private void saveCourse() {
        String name = courseNameEditText.getText().toString();
        String location = courseLocationEditText.getText().toString();
        String day = courseDaySpinner.getSelectedItem().toString();
        String hour = courseTimeEditText.getText().toString();
        int duration = Integer.parseInt(courseDurationEditText.getText().toString());

        Faculty faculty = facultySpinner.getSelectedItemPosition() == 0 ?
                null : (Faculty) facultySpinner.getSelectedItem();
        Year year = yearSpinner.getSelectedItemPosition() == 0 ?
                null : (Year) yearSpinner.getSelectedItem();
        String section = sectionSpinner.getSelectedItemPosition() == 0 ?
                null : (String) sectionSpinner.getSelectedItem();

        List<UserGroup> selectedGroups = groupAdapter.getSelectedGroups();

        Course course;
        if (courseId == -1) {
            course = new Course();
        } else {
            course = new Course();
            course.setId(courseId);
        }

        course.setName(name);
        course.setLocation(location);
        course.setDay(day);
        course.setHour(hour);
        course.setDuration(duration);
        course.setFaculty(faculty);
        course.setYear(year);
        course.setSection(section);
        course.setGroups(selectedGroups);

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            if (courseId == -1) {
                db.courseDao().insert(course);
            } else {
                db.courseDao().update(course);
            }
            runOnUiThread(() -> {
                Toast.makeText(CreateCourseActivity.this, "Course saved!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        }).start();
    }

    private boolean validateInput() {
        String name = courseNameEditText.getText().toString().trim();
        if (name.isEmpty()) {
            courseNameEditText.setError("Course name is required");
            return false;
        }

        String location = courseLocationEditText.getText().toString().trim();
        if (location.isEmpty()) {
            courseLocationEditText.setError("Location is required");
            return false;
        }

        String hourStr = courseTimeEditText.getText().toString().trim();
        if (hourStr.isEmpty()) {
            courseTimeEditText.setError("Hour is required");
            return false;
        }

        try {
            int hour = Integer.parseInt(hourStr);
            if (hour < 8 || hour > 21) {
                courseTimeEditText.setError("Hour must be between 8 and 21");
                return false;
            }
        } catch (NumberFormatException e) {
            courseTimeEditText.setError("Invalid hour format");
            return false;
        }

        String durationStr = courseDurationEditText.getText().toString().trim();
        if (durationStr.isEmpty()) {
            courseDurationEditText.setError("Duration is required");
            return false;
        }

        try {
            int duration = Integer.parseInt(durationStr);
            if (duration < 1 || duration > 14) {
                courseDurationEditText.setError("Duration must be between 1 and 14 hours");
                return false;
            }
        } catch (NumberFormatException e) {
            courseDurationEditText.setError("Invalid duration format");
            return false;
        }

        try {
            int hour = Integer.parseInt(hourStr);
            int duration = Integer.parseInt(durationStr);
            if (hour + duration > 22) {
                courseDurationEditText.setError("Course would end after 22:00 (10PM)");
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }

        if (facultySpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a faculty", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (yearSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a year", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (sectionSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a section", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (groupAdapter.getSelectedGroups().isEmpty()) {
            Toast.makeText(this, "Please select at least one group", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            int startTime = Integer.parseInt(hourStr);
            int duration = Integer.parseInt(durationStr);
            String day = courseDaySpinner.getSelectedItem().toString();
            Faculty faculty = (Faculty) facultySpinner.getSelectedItem();
            Year year = (Year) yearSpinner.getSelectedItem();
            String section = (String) sectionSpinner.getSelectedItem();

            for (UserGroup group : groupAdapter.getSelectedGroups()) {
                List<Course> overlappingCourses = checkOverlappingCourses(
                        day, faculty, year, section, startTime, duration, group);

                if (!overlappingCourses.isEmpty()) {
                    showOverlapError(overlappingCourses);
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    private List<Course> checkOverlappingCourses(String day, Faculty faculty, Year year,
                                                 String section, int startTime, int duration,
                                                 UserGroup group) {
        final List<Course>[] result = new List[]{new ArrayList<>()};
        CountDownLatch latch = new CountDownLatch(1);

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            String groupValue = String.valueOf(group.getValue());

            result[0] = db.courseDao().findOverlappingCourses(
                    day,
                    faculty,
                    year,
                    section,
                    startTime,
                    duration,
                    groupValue,
                    courseId);

            latch.countDown();
        }).start();

        try {
            latch.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result[0];
    }

    private void showOverlapError(List<Course> overlappingCourses) {
        StringBuilder errorMessage = new StringBuilder("Time overlaps with: ");
        for (Course course : overlappingCourses) {
            errorMessage.append(course.getName())
                    .append(" (")
                    .append(course.getHour())
                    .append(":00-")
                    .append(Integer.parseInt(course.getHour()) + course.getDuration())
                    .append(":00), ");
        }
        errorMessage.setLength(errorMessage.length() - 2);

        courseTimeEditText.setError(errorMessage.toString());
        courseDurationEditText.setError(errorMessage.toString());
    }
}
