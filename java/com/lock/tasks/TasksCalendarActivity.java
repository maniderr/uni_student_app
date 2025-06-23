package com.lock.tasks;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lock.R;
import com.lock.data.adapters.TasksRecyclerAdapter;
import com.lock.data.dao.TaskDao;
import com.lock.data.database.AppDatabase;
import com.lock.data.model.TaskItem;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import org.threeten.bp.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.text.style.ForegroundColorSpan;
import android.util.Log;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.jakewharton.threetenabp.AndroidThreeTen;

public class TasksCalendarActivity extends AppCompatActivity implements TasksRecyclerAdapter.OnTaskUpdatedListener{
    private MaterialCalendarView calendarView;
    private List<TaskItem> tasks;
    private SharedPreferences sharedPreferences;
    private Set<CalendarDay> taskDates;
    private RecyclerView tasksRecyclerView;
    private TasksRecyclerAdapter tasksAdapter;
    private String currentUsername;
    private AppDatabase db;
    private TaskDao taskDao;
    private Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidThreeTen.init(this);
        setContentView(R.layout.activity_tasks_calendar);

        sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        currentUsername = sharedPreferences.getString("username", null);

        db = AppDatabase.getInstance(this);
        taskDao = db.taskDao();

        calendarView = findViewById(R.id.calendarView);
        tasksRecyclerView = findViewById(R.id.tasksRecyclerView);

        tasks = new ArrayList<>();
        taskDates = new HashSet<>();

        tasksAdapter = new TasksRecyclerAdapter(new ArrayList<>(), taskDao, this);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tasksRecyclerView.setAdapter(tasksAdapter);

        loadTasksForCurrentUser();
        clearCalendarColors();
        highlightTaskDates();

        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(MaterialCalendarView widget, CalendarDay date, boolean selected) {
                checkTasksForDate(date.getYear(), date.getMonth(), date.getDay());
            }
        });
    }

    @Override
    public void onTaskUpdated(TaskItem task) {
        loadTasksForCurrentUser();
    }

    @Override
    public void onTaskDeleted(TaskItem task) {
        loadTasksForCurrentUser();

        CalendarDay selectedDate = calendarView.getSelectedDate();
        if (selectedDate != null) {
            checkTasksForDate(selectedDate.getYear(),
                    selectedDate.getMonth(),
                    selectedDate.getDay());
        }
    }

    private void loadTasksForCurrentUser() {
        executor.execute(() -> {
            List<TaskItem> loadedTasks = taskDao.getTasksByUsername(currentUsername);
            runOnUiThread(() -> {
                tasks.clear();
                tasks.addAll(loadedTasks);

                taskDates.clear();
                for (TaskItem task : tasks) {
                    try {
                        LocalDate localDate = LocalDate.parse(task.getDate());
                        taskDates.add(CalendarDay.from(localDate));
                    } catch (Exception e) {
                        Log.e("TaskLoad", "Invalid date format for task: " + task.getDate(), e);
                    }
                }

                calendarView.invalidateDecorators();
            });
        });
    }

    private void showTasksInRecyclerView(List<TaskItem> tasksForDate) {
        if (tasksForDate.isEmpty()) {
            tasksRecyclerView.setVisibility(View.GONE);
            Toast.makeText(this, "There are no tasks for this date", Toast.LENGTH_SHORT).show();
        } else {
            tasksRecyclerView.setVisibility(View.VISIBLE);
            tasksAdapter.updateTasks(tasksForDate);
        }
    }

    private void highlightTaskDates() {

        calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return taskDates.contains(day);
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.addSpan(new DotSpan(5, ContextCompat.getColor(TasksCalendarActivity.this, R.color.colorAccent)));

                int defaultTextColor = new TextView(TasksCalendarActivity.this).getTextColors().getDefaultColor();
                view.addSpan(new ForegroundColorSpan(Color.RED));
            }
        });
    }

    private void clearCalendarColors() {

        calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return !taskDates.contains(day);
            }

            @Override
            public void decorate(DayViewFacade view) {
                int defaultTextColor = new TextView(TasksCalendarActivity.this).getTextColors().getDefaultColor();
                view.addSpan(new ForegroundColorSpan(defaultTextColor));
            }
        });
    }

    private void checkTasksForDate(int year, int month, int dayOfMonth) {
        String selectedDateStr = String.format(Locale.US, "%04d-%02d-%02d", year, month, dayOfMonth);

        executor.execute(() -> {
            List<TaskItem> tasksForDate = taskDao.getTasksByUsernameAndDate(currentUsername, selectedDateStr);
            runOnUiThread(() -> showTasksInRecyclerView(tasksForDate));
        });
    }
}