package com.lock.data.adapters;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.lock.R;
import com.lock.data.dao.TaskDao;
import com.lock.data.model.TaskItem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TaskAdapter extends ArrayAdapter<TaskItem> {
    private ArrayList<TaskItem> taskList;
    private TaskDao taskDao;
    private Executor executor = Executors.newSingleThreadExecutor();
    private Activity activity;

    public TaskAdapter(Activity activity, ArrayList<TaskItem> taskItems, TaskDao taskDao) {
        super(activity, 0, taskItems);
        this.activity = activity;
        this.taskList = taskItems;
        this.taskDao = taskDao;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        TaskItem taskItem = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_list_task, parent, false);
        }

        TextView taskName = convertView.findViewById(R.id.textTask);
        TextView taskCourse = convertView.findViewById(R.id.textCourse);
        TextView taskDate = convertView.findViewById(R.id.textDate);
        TextView textSource = convertView.findViewById(R.id.textSource);
        ImageView warningIcon = convertView.findViewById(R.id.warningIcon);
        ImageButton editButton = convertView.findViewById(R.id.editButton);
        ImageButton deleteButton = convertView.findViewById(R.id.btnDelete);

        assert taskItem != null;
        taskName.setText(taskItem.getTaskName());
        taskCourse.setText(taskItem.getCourse());
        taskDate.setText(taskItem.getDate());
        textSource.setText(taskItem.getSource());

        String dueDateStr = taskItem.getDate();
        try {
            LocalDate dueDate = LocalDate.parse(dueDateStr);
            LocalDate today = LocalDate.now();

            if (dueDate.isBefore(today)) {
                // Overdue - red color
                warningIcon.setVisibility(View.VISIBLE);
                warningIcon.setColorFilter(ContextCompat.getColor(getContext(),
                                R.color.red_overdue),
                        PorterDuff.Mode.SRC_IN);
            } else if (dueDate.isEqual(today)) {
                // Due today - yellow color
                warningIcon.setVisibility(View.VISIBLE);
                warningIcon.setColorFilter(ContextCompat.getColor(getContext(),
                                R.color.yellow_warning),
                        PorterDuff.Mode.SRC_IN);
            } else {
                warningIcon.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            warningIcon.setVisibility(View.GONE);
            Log.e("TaskAdapter", "Invalid date format: " + dueDateStr, e);
        }

        editButton.setOnClickListener(v -> showEditItemDialog(taskItem, position));
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog(taskItem, position));

        return convertView;
    }

    private void showEditItemDialog(TaskItem taskItem, int position) {
        Activity activity = (Activity) getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_item, null);
        builder.setView(dialogView);

        EditText editTextTask = dialogView.findViewById(R.id.editTextTask);
        EditText editTextCourse = dialogView.findViewById(R.id.editTextCourse);
        Button btnDatePicker = dialogView.findViewById(R.id.btnDatePicker);
        EditText editTextSource = dialogView.findViewById(R.id.editTextSource);
        Button btnSaveItem = dialogView.findViewById(R.id.btnSaveItem);

        String currentDate = taskItem.getDate();
        btnDatePicker.setText(currentDate);

        editTextTask.setText(taskItem.getTaskName());
        editTextCourse.setText(taskItem.getCourse());
        editTextSource.setText(taskItem.getSource());

        final String[] selectedDate = {currentDate};

        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            // Format YYYY-MM-DD
            selectedDate[0] = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            btnDatePicker.setText(selectedDate[0]);
        };

        btnDatePicker.setOnClickListener(v -> {
            try {
                String[] parts = currentDate.split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]) - 1; // Convert to 0-based month
                int day = Integer.parseInt(parts[2]);

                new DatePickerDialog(activity, R.style.CustomDatePickerDialog, dateSetListener, year, month, day).show();
            } catch (Exception e) {
                Calendar cal = Calendar.getInstance();
                new DatePickerDialog(activity, R.style.CustomDatePickerDialog, dateSetListener,
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                ).show();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        btnSaveItem.setOnClickListener(v -> {
            String task = editTextTask.getText().toString().trim();
            String course = editTextCourse.getText().toString().trim();
            String source = editTextSource.getText().toString().trim();

            if (!task.isEmpty() && !course.isEmpty() && !source.isEmpty()) {
                taskItem.setTaskName(task);
                taskItem.setCourse(course);
                taskItem.setDate(selectedDate[0]);
                taskItem.setSource(source);

                executor.execute(() -> {
                    taskDao.update(taskItem);
                    activity.runOnUiThread(() -> {
                        notifyDataSetChanged();
                        dialog.dismiss();
                    });
                });
            } else {
                Toast.makeText(activity, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmationDialog(TaskItem taskItem, int position) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    executor.execute(() -> {
                        taskDao.delete(taskItem);
                        activity.runOnUiThread(() -> {
                            taskList.remove(position);
                            notifyDataSetChanged();
                            Toast.makeText(getContext(), "Task deleted", Toast.LENGTH_SHORT).show();
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}