package com.lock.courses_schedule;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lock.R;
import com.lock.data.adapters.AdminCourseAdapter;
import com.lock.data.database.AppDatabase;
import com.lock.data.model.Course;
import com.lock.utils.enums.Faculty;
import com.lock.utils.enums.Year;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdminCourseManagementActivity extends AppCompatActivity implements AdminCourseAdapter.OnCourseActionListener{
    private RecyclerView coursesRecyclerView;
    private AdminCourseAdapter adminCourseAdapter;
    private List<Course> courses = new ArrayList<>();
    private List<Course> allCourses = new ArrayList<>();
    private static final int CREATE_COURSE_REQUEST = 1;
    private static final int MODIFY_COURSE_REQUEST = 2;
    private Faculty currentFacultyFilter = null;
    private Year currentYearFilter = null;
    private String currentNameFilter = null;
    private String currentDayFilter = null;
    private String currentLocationFilter = null;
    private String currentSectionFilter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_courses);

        coursesRecyclerView = findViewById(R.id.coursesRecyclerView);
        coursesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        adminCourseAdapter = new AdminCourseAdapter(courses, this);
        coursesRecyclerView.setAdapter(adminCourseAdapter);

        fetchCoursesFromDatabase();

        findViewById(R.id.createCourseButton).setOnClickListener(v -> {
            Intent intent = new Intent(AdminCourseManagementActivity.this, CreateCourseActivity.class);
            startActivityForResult(intent, CREATE_COURSE_REQUEST);
        });

        findViewById(R.id.filterButton).setOnClickListener(v -> showFilterDialog());
    }

    @Override
    public void onModifyCourse(Course course) {
        Intent intent = new Intent(this, CreateCourseActivity.class);
        intent.putExtra("COURSE_ID", course.getId());
        startActivityForResult(intent, MODIFY_COURSE_REQUEST );
    }

    @Override
    public void onDeleteCourse(Course course) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Course")
                .setMessage("Are you sure you want to delete this course?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    new Thread(() -> {
                        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                        db.courseDao().delete(course);
                        runOnUiThread(this::fetchCoursesFromDatabase);
                    }).start();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void fetchCoursesFromDatabase() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            List<Course> fetchedCourses = db.courseDao().getAllCourses();

            Log.d("CourseFetch", "Fetched " + fetchedCourses.size() + " courses");
            for (Course course : fetchedCourses) {
                Log.d("CourseFetch", "Course: " + course.getName());
            }

            runOnUiThread(() -> {
                allCourses = fetchedCourses;
                courses = new ArrayList<>(fetchedCourses);
                adminCourseAdapter.updateCourses(courses);

                Log.d("CourseFetch", "Adapter item count: " + adminCourseAdapter.getItemCount());
            });
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == CREATE_COURSE_REQUEST || requestCode == MODIFY_COURSE_REQUEST ) && resultCode == RESULT_OK) {
            fetchCoursesFromDatabase();
        }
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filter_courses, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        Spinner facultySpinner = dialogView.findViewById(R.id.facultySpinner);
        Spinner yearSpinner = dialogView.findViewById(R.id.yearSpinner);
        Spinner sectionSpinner = dialogView.findViewById(R.id.sectionSpinner);
        TextView sectionFilterLabel = dialogView.findViewById(R.id.sectionFilterLabel);
        EditText nameFilterEditText = dialogView.findViewById(R.id.nameFilterEditText);
        EditText dayFilterEditText = dialogView.findViewById(R.id.dayFilterEditText);
        EditText locationFilterEditText = dialogView.findViewById(R.id.locationFilterEditText);
        Button applyFilterButton = dialogView.findViewById(R.id.applyFilterButton);
        Button clearFilterButton = dialogView.findViewById(R.id.clearFilterButton);

        populateFilterSpinners(facultySpinner, yearSpinner);

        facultySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                @SuppressWarnings("unchecked")
                FilterOption<Faculty> selected = (FilterOption<Faculty>) parent.getItemAtPosition(position);
                updateSectionSpinner(selected.getValue(), sectionSpinner, sectionFilterLabel);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                sectionSpinner.setVisibility(View.GONE);
                sectionFilterLabel.setVisibility(View.GONE);
            }
        });

        nameFilterEditText.setText(currentNameFilter);
        dayFilterEditText.setText(currentDayFilter);
        locationFilterEditText.setText(currentLocationFilter);

        applyFilterButton.setOnClickListener(v -> {
            @SuppressWarnings("unchecked")
            FilterOption<Faculty> facultyOption = (FilterOption<Faculty>) facultySpinner.getSelectedItem();
            @SuppressWarnings("unchecked")
            FilterOption<Year> yearOption = (FilterOption<Year>) yearSpinner.getSelectedItem();
            @SuppressWarnings("unchecked")
            FilterOption<String> sectionOption = (FilterOption<String>) sectionSpinner.getSelectedItem();

            String nameFilter = nameFilterEditText.getText().toString().trim();
            String dayFilter = dayFilterEditText.getText().toString().trim();
            String locationFilter = locationFilterEditText.getText().toString().trim();

            applyFilters(
                    facultyOption.getValue(),
                    yearOption.getValue(),
                    sectionOption != null ? sectionOption.getValue() : null,
                    nameFilter.isEmpty() ? null : nameFilter,
                    dayFilter.isEmpty() ? null : dayFilter,
                    locationFilter.isEmpty() ? null : locationFilter
            );
            dialog.dismiss();
        });

        clearFilterButton.setOnClickListener(v -> {
            applyFilters(null, null, null, null, null, null);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void populateFilterSpinners(Spinner facultySpinner, Spinner yearSpinner) {
        List<FilterOption<Faculty>> facultyOptions = new ArrayList<>();
        facultyOptions.add(new FilterOption<>(null, "All Faculties"));
        for (Faculty faculty : Faculty.values()) {
            facultyOptions.add(new FilterOption<>(faculty, faculty.getDisplayName()));
        }

        List<FilterOption<Year>> yearOptions = new ArrayList<>();
        yearOptions.add(new FilterOption<>(null, "All Years"));
        for (Year year : Year.values()) {
            yearOptions.add(new FilterOption<>(year, "Year " + year.getValue()));
        }

        ArrayAdapter<FilterOption<Faculty>> facultyAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, facultyOptions);
        facultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        facultySpinner.setAdapter(facultyAdapter);

        ArrayAdapter<FilterOption<Year>> yearAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, yearOptions);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        for (int i = 0; i < facultyOptions.size(); i++) {
            if (facultyOptions.get(i).getValue() == currentFacultyFilter) {
                facultySpinner.setSelection(i);
                break;
            }
        }

        for (int i = 0; i < yearOptions.size(); i++) {
            if (yearOptions.get(i).getValue() == currentYearFilter) {
                yearSpinner.setSelection(i);
                break;
            }
        }
    }

    private void updateSectionSpinner(Faculty faculty, Spinner sectionSpinner, TextView sectionFilterLabel) {
        if (faculty == null) {
            sectionSpinner.setVisibility(View.GONE);
            sectionFilterLabel.setVisibility(View.GONE);
            return;
        }

        List<FilterOption<String>> sectionOptions = new ArrayList<>();
        sectionOptions.add(new FilterOption<>(null, "All Sections"));

        for (String section : faculty.getSections()) {
            sectionOptions.add(new FilterOption<>(section, section));
        }

        ArrayAdapter<FilterOption<String>> sectionAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, sectionOptions);
        sectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sectionSpinner.setAdapter(sectionAdapter);

        if (currentSectionFilter != null) {
            for (int i = 0; i < sectionOptions.size(); i++) {
                if (currentSectionFilter.equals(sectionOptions.get(i).getValue())) {
                    sectionSpinner.setSelection(i);
                    break;
                }
            }
        }

        sectionSpinner.setVisibility(View.VISIBLE);
        sectionFilterLabel.setVisibility(View.VISIBLE);
    }

    private void applyFilters(Faculty faculty, Year year, String section, String name, String day, String location) {
        this.currentFacultyFilter = faculty;
        this.currentYearFilter = year;
        this.currentSectionFilter = section;
        this.currentNameFilter = name;
        this.currentDayFilter = day;
        this.currentLocationFilter = location;

        List<Course> filteredCourses = new ArrayList<>();

        for (Course course : allCourses) {
            boolean matchesFaculty = faculty == null ||
                    (course.getFaculty() != null && course.getFaculty() == faculty);

            boolean matchesYear = year == null ||
                    (course.getYear() != null && course.getYear() == year);

            boolean matchesSection = section == null ||
                    (course.getSection() != null && course.getSection().equalsIgnoreCase(section));

            boolean matchesName = name == null ||
                    (course.getName() != null && course.getName().toLowerCase().contains(name.toLowerCase()));

            boolean matchesDay = day == null ||
                    (course.getDay() != null && course.getDay().toLowerCase().contains(day.toLowerCase()));

            boolean matchesLocation = location == null ||
                    (course.getLocation() != null && course.getLocation().toLowerCase().contains(location.toLowerCase()));

            if (matchesFaculty && matchesYear && matchesSection && matchesName && matchesDay && matchesLocation) {
                filteredCourses.add(course);
            }
        }

        adminCourseAdapter.updateCourses(filteredCourses);
    }

    private static class FilterOption<T> {
        private final T value;
        private final String displayText;

        public FilterOption(T value, String displayText) {
            this.value = value;
            this.displayText = displayText;
        }

        public T getValue() {
            return value;
        }

        @Override
        public String toString() {
            return displayText;
        }
    }
}
