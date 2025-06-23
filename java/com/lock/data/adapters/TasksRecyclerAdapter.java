package com.lock.data.adapters;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.lock.R;
import com.lock.data.dao.TaskDao;
import com.lock.data.model.TaskItem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TasksRecyclerAdapter extends RecyclerView.Adapter<TasksRecyclerAdapter.TaskViewHolder> {
    private List<TaskItem> tasks;
    private TaskDao taskDao;
    private Executor executor = Executors.newSingleThreadExecutor();
    private OnTaskUpdatedListener taskUpdatedListener;

    public TasksRecyclerAdapter(List<TaskItem> tasks, TaskDao taskDao, OnTaskUpdatedListener listener) {
        this.tasks = tasks;
        this.taskDao = taskDao;
        this.taskUpdatedListener = listener;
    }

    public interface OnTaskUpdatedListener {
        void onTaskUpdated(TaskItem task);
        void onTaskDeleted(TaskItem task);
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskItem task = tasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void updateTasks(List<TaskItem> newTasks) {
        tasks = new ArrayList<>(newTasks); // Create a new list to avoid reference issues
        notifyDataSetChanged();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private TextView taskName, taskCourse, taskDate, textSource;
        private ImageView warningIcon;
        private ImageButton editButton, deleteButton;;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.textTask);
            taskCourse = itemView.findViewById(R.id.textCourse);
            taskDate = itemView.findViewById(R.id.textDate);
            textSource = itemView.findViewById(R.id.textSource);
            warningIcon = itemView.findViewById(R.id.warningIcon);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(TaskItem task) {
            taskName.setText(task.getTaskName());
            taskCourse.setText(task.getCourse());
            taskDate.setText(task.getDate());
            textSource.setText(task.getSource());

            try {
                LocalDate dueDate = LocalDate.parse(task.getDate());
                LocalDate today = LocalDate.now();

                if (dueDate.isBefore(today)) {
                    // Overdue - red color
                    warningIcon.setVisibility(View.VISIBLE);
                    warningIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(),
                                    R.color.red_overdue),
                            PorterDuff.Mode.SRC_IN);
                } else if (dueDate.isEqual(today)) {
                    // Due today - yellow color
                    warningIcon.setVisibility(View.VISIBLE);
                    warningIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(),
                                    R.color.yellow_warning),
                            PorterDuff.Mode.SRC_IN);
                } else {
                    warningIcon.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                warningIcon.setVisibility(View.GONE);
            }

            editButton.setOnClickListener(v -> showEditItemDialog(task));
            deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog(task));
        }

        private void showDeleteConfirmationDialog(TaskItem taskItem) {
            new AlertDialog.Builder(itemView.getContext())
                    .setTitle("Delete Task")
                    .setMessage("Are you sure you want to delete this task?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            executor.execute(() -> {
                                // Delete from database
                                taskDao.delete(taskItem);

                                // Update UI on main thread
                                ((Activity) itemView.getContext()).runOnUiThread(() -> {
                                    // Remove from current list
                                    tasks.remove(position);
                                    notifyItemRemoved(position);

                                    // Notify activity to refresh calendar
                                    if (taskUpdatedListener != null) {
                                        taskUpdatedListener.onTaskUpdated(taskItem);
                                    }
                                });
                            });
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }

        private void showEditItemDialog(TaskItem taskItem) {
            Activity activity = (Activity) itemView.getContext();
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
                // Format the selected date as YYYY-MM-DD
                selectedDate[0] = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                btnDatePicker.setText(selectedDate[0]);
            };

            btnDatePicker.setOnClickListener(v -> {
                String[] parts = currentDate.split("-");
                try {
                    int year = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]) - 1; // Convert to 0-based month
                    int day = Integer.parseInt(parts[2]);

                    DatePickerDialog datePickerDialog = new DatePickerDialog(
                            activity,
                            R.style.CustomDatePickerDialog,
                            dateSetListener,
                            year,
                            month,
                            day
                    );
                    datePickerDialog.show();
                } catch (Exception e) {
                    // Fallback to current date if parsing fails
                    Calendar cal = Calendar.getInstance();
                    new DatePickerDialog(
                            activity,
                            R.style.CustomDatePickerDialog,
                            dateSetListener,
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
                    String oldDate = taskItem.getDate();

                    taskItem.setTaskName(task);
                    taskItem.setCourse(course);
                    taskItem.setDate(selectedDate[0]);
                    taskItem.setSource(source);

                    executor.execute(() -> {
                        taskDao.update(taskItem);
                        activity.runOnUiThread(() -> {
                            notifyDataSetChanged();
                            dialog.dismiss();

                            if (!oldDate.equals(selectedDate[0])) {
                                if (taskUpdatedListener != null) {
                                    taskUpdatedListener.onTaskUpdated(taskItem);
                                }
                            }
                        });
                    });
                } else {
                    Toast.makeText(activity, "Please fill all fields", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}



