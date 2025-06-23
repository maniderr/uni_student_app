package com.lock.tasks;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.lock.announcement.AdminAnnouncementActivity;
import com.lock.announcement.StudentAnnouncementActivity;
import com.lock.app_usage.AppUsageActivity;
import com.lock.LoginActivity;
import com.lock.data.adapters.TaskAdapter;
import com.lock.data.dao.TaskDao;
import com.lock.data.database.AppDatabase;
import com.lock.courses_schedule.AdminCourseManagementActivity;
import com.lock.courses_schedule.StudentCoursesActivity;
import com.lock.data.model.TaskItem;
import com.lock.data.model.User;
import com.lock.facultate_info.StudentContactInfoActivity;
import com.lock.public_transp.PublicTransportActivity;
import com.lock.settings.SettingsActivity;
import com.lock.train_schedule.TrainScheduleActivity;
import com.lock.facultate_info.AdminContactInfoActivity;
import com.lock.R;
import com.lock.ai_assistant.AiAssistantActivity;
import com.lock.location.CityLocationActivity;
import com.lock.student_chat.StudentChatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TaskCreationActivity extends AppCompatActivity {

    private ListView listView;
    private ImageButton btnAddItem;
    private ArrayList<TaskItem> taskList;
    private TaskAdapter adapter;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    public NavigationView navigationView;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String USERNAME_KEY = "username";

    private AppDatabase db;
    private TaskDao taskDao;
    private Executor executor = Executors.newSingleThreadExecutor();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_creation);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String currentUsername = sharedPreferences.getString(USERNAME_KEY, null);

        db = AppDatabase.getInstance(this);
        taskDao = db.taskDao();

        listView = findViewById(R.id.listView);
        btnAddItem = findViewById(R.id.btnAddItem);

        taskList = new ArrayList<>();
        adapter = new TaskAdapter(TaskCreationActivity.this, taskList, taskDao);
        listView.setAdapter(adapter);

        loadTasksForCurrentUser(currentUsername);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        updateNavigationHeader();
        startListeners();
    }

    private void startListeners() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Log.d("Navigation", "Item selected: " + id);

                if (id == R.id.nav_app_usage) {
                    Intent switchActivityIntent = new Intent(TaskCreationActivity.this, AppUsageActivity.class);
                    startActivity(switchActivityIntent);
                } else if (id == R.id.nav_lock_options) {
                    Intent switchActivityIntent = new Intent(TaskCreationActivity.this, TrainScheduleActivity.class);
                    startActivity(switchActivityIntent);
                } else if (id == R.id.nav_calendar_tasks) {
                    Intent switchActivityIntent = new Intent(TaskCreationActivity.this, TasksCalendarActivity.class);
                    startActivity(switchActivityIntent);
                } else if (id == R.id.nav_location) {
                    Intent switchActivityIntent = new Intent(TaskCreationActivity.this, CityLocationActivity.class);
                    startActivity(switchActivityIntent);
                } else if (id == R.id.nav_change_contact) {
                    Log.d("Navigation", "UPT Contact Info selected");
                    fetchUserRoleAndNavigate(AdminContactInfoActivity.class, StudentContactInfoActivity.class);
                } else if (id == R.id.nav_yearly_schedule) {
                    Log.d("Navigation", "Yearly Schedule selected");
                    fetchUserRoleAndNavigate(AdminCourseManagementActivity.class, StudentCoursesActivity.class);
                } else if (id == R.id.nav_chatbot) {
                    Intent switchActivityIntent = new Intent(TaskCreationActivity.this, AiAssistantActivity.class);
                    startActivity(switchActivityIntent);
                } else if (id == R.id.nav_public_transport) {
                    Intent switchActivityIntent = new Intent(TaskCreationActivity.this, PublicTransportActivity.class);
                    startActivity(switchActivityIntent);
                } else if (id == R.id.nav_student_chat) {
                    Intent switchActivityIntent = new Intent(TaskCreationActivity.this, StudentChatActivity.class);
                    startActivity(switchActivityIntent);
                } else if (id == R.id.nav_settings) {
                    Intent switchActivityIntent = new Intent(TaskCreationActivity.this, SettingsActivity.class);
                    startActivity(switchActivityIntent);
                } else if (id == R.id.nav_ancmnt) {
                    fetchUserRoleAndNavigate(AdminAnnouncementActivity.class, StudentAnnouncementActivity.class);
                } else if (id == R.id.nav_logout) {
                    SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove("username");
                    editor.apply();

                    Intent switchActivityIntent = new Intent(TaskCreationActivity.this, LoginActivity.class);
                    switchActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(switchActivityIntent);
                    finish();
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        btnAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MainActivity", "Add Item button clicked");
                showAddItemDialog();
            }
        });
    }

    private void loadTasksForCurrentUser(String username) {
        executor.execute(() -> {
            List<TaskItem> tasks = taskDao.getTasksByUsername(username);
            runOnUiThread(() -> {
                taskList.clear();
                taskList.addAll(tasks);
                adapter.notifyDataSetChanged();
            });
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_item, null);
        builder.setView(dialogView);

        EditText editTextTask = dialogView.findViewById(R.id.editTextTask);
        EditText editTextCourse = dialogView.findViewById(R.id.editTextCourse);
        Button btnDatePicker = dialogView.findViewById(R.id.btnDatePicker);
        EditText editTextSource = dialogView.findViewById(R.id.editTextSource);
        Button btnSaveItem = dialogView.findViewById(R.id.btnSaveItem);

        final String[] selectedDate = { new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()) };
        btnDatePicker.setText(selectedDate[0]);

        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                selectedDate[0] = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                btnDatePicker.setText(selectedDate[0]);
            }
        };

        btnDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        TaskCreationActivity.this,
                        R.style.CustomDatePickerDialog,
                        dateSetListener,
                        year, month, day
                );
                datePickerDialog.show();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        btnSaveItem.setOnClickListener(v -> {
            String task = editTextTask.getText().toString().trim();
            String course = editTextCourse.getText().toString().trim();
            String source = editTextSource.getText().toString().trim();
            String username = sharedPreferences.getString(USERNAME_KEY, null);

            if (!task.isEmpty() && !course.isEmpty() && !source.isEmpty() && username != null) {
                TaskItem newItem = new TaskItem(0, task, course, selectedDate[0], source, username);
                executor.execute(() -> {
                    long id = taskDao.insert(newItem);
                    newItem.setId(id);
                    runOnUiThread(() -> {
                        taskList.add(newItem);
                        adapter.notifyDataSetChanged();
                    });
                });
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateNavigationHeader() {
        String username = sharedPreferences.getString(USERNAME_KEY, "Guest");
        View headerView = navigationView.getHeaderView(0);
        TextView usernameTextView = headerView.findViewById(R.id.textViewUsername);

        if (usernameTextView != null) {
            usernameTextView.setText(username);
        }
    }

    private void fetchUserRoleAndNavigate(Class<?> adminActivity, Class<?> studentActivity) {
        new Thread(() -> {
            try {
                Log.d("RoleFetch", "Starting navigation check...");

                // 1. Verify user ID exists
                SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                long userId = sharedPreferences.getLong("user_id", -1);
                Log.d("RoleFetch", "User ID from SharedPrefs: " + userId);

                if (userId == -1) {
                    Log.e("RoleFetch", "No user ID found in SharedPreferences");
                    showToast("Please login again");
                    return;
                }

                // 2. Verify database access
                if (db == null) {
                    Log.e("RoleFetch", "Database is null");
                    showToast("Database error");
                    return;
                }

                // 3. Verify user exists
                User user = db.userDao().getUserById(userId);
                if (user == null) {
                    Log.e("RoleFetch", "No user found with ID: " + userId);
                    showToast("User data not found");
                    return;
                }

                // 4. Verify role
                String role = user.getRole().getValue();
                Log.d("RoleFetch", "Retrieved role: " + role);

                if (role == null) {
                    Log.e("RoleFetch", "User role is null");
                    showToast("Role not assigned");
                    return;
                }

                // 5. Prepare navigation
                Class<?> targetActivity = "Admin".equalsIgnoreCase(role) ? adminActivity : studentActivity;
                Log.d("RoleFetch", "Preparing to navigate to: " + targetActivity.getSimpleName());

                runOnUiThread(() -> {
                    try {
                        Log.d("RoleFetch", "Starting activity...");
                        Intent intent = new Intent(TaskCreationActivity.this, targetActivity);
                        startActivity(intent);
                        Log.d("RoleFetch", "Activity started successfully");
                    } catch (Exception e) {
                        Log.e("RoleFetch", "Activity start failed", e);
                        showToast("Navigation error");
                    }
                });

            } catch (Exception e) {
                Log.e("RoleFetch", "Unexpected error", e);
                showToast("System error");
            }
        }).start();
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(TaskCreationActivity.this, message, Toast.LENGTH_SHORT).show());
    }
}
