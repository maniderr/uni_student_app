package com.lock.data.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lock.R;
import com.lock.data.model.Course;
import com.lock.utils.enums.UserGroup;

import java.util.ArrayList;
import java.util.List;

public class AdminCourseAdapter extends RecyclerView.Adapter<AdminCourseAdapter.CourseViewHolder> {
    private List<Course> courses;
    private OnCourseActionListener listener;

    public interface OnCourseActionListener {
        void onModifyCourse(Course course);
        void onDeleteCourse(Course course);
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView courseName, courseLocation, courseDay, courseTime, courseDuration;
        TextView courseFaculty, courseYear, courseSection, courseGroups;
        Button modifyButton, deleteButton;

        public CourseViewHolder(View itemView) {
            super(itemView);
            courseName = itemView.findViewById(R.id.courseName);
            courseLocation = itemView.findViewById(R.id.courseLocation);
            courseDay = itemView.findViewById(R.id.courseDay);
            courseTime = itemView.findViewById(R.id.courseTime);
            courseDuration = itemView.findViewById(R.id.courseDuration);

            courseFaculty = itemView.findViewById(R.id.courseFaculty);
            courseYear = itemView.findViewById(R.id.courseYear);
            courseSection = itemView.findViewById(R.id.courseSection);
            courseGroups = itemView.findViewById(R.id.courseGroups);

            modifyButton = itemView.findViewById(R.id.modifyButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    public AdminCourseAdapter(List<Course> courses, OnCourseActionListener listener) {
        this.courses = courses != null ? courses : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(CourseViewHolder holder, int position) {
        Course course = courses.get(position);

        holder.courseName.setText(course.getName());
        holder.courseLocation.setText("Location: " + course.getLocation());
        holder.courseDay.setText("Day: " + course.getDay());
        holder.courseTime.setText("Time: " + course.getHour());
        holder.courseDuration.setText("Duration: " + course.getDuration() + " hours");

        if (course.getFaculty() != null) {
            holder.courseFaculty.setText("Faculty: " + course.getFaculty().getDisplayName());
        } else {
            holder.courseFaculty.setText("Faculty: Not specified");
        }

        if (course.getYear() != null) {
            holder.courseYear.setText("Year: " + course.getYear().getValue());
        } else {
            holder.courseYear.setText("Year: Not specified");
        }

        if (course.getSection() != null && !course.getSection().isEmpty()) {
            holder.courseSection.setText("Section: " + course.getSection());
        } else {
            holder.courseSection.setText("Section: Not specified");
        }

        if (course.getGroups() != null && !course.getGroups().isEmpty()) {
            holder.courseGroups.setText("Groups: " + formatGroups(course.getGroups()));
        } else {
            holder.courseGroups.setText("Groups: Not specified");
        }

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteCourse(course);
            }
        });

        holder.modifyButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onModifyCourse(course);
            }
        });
    }

    public void updateCourses(List<Course> newCourses) {
        this.courses = newCourses;
        notifyDataSetChanged();
    }

    private String formatGroups(List<UserGroup> groups) {
        if (groups == null || groups.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (UserGroup group : groups) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(group.getValue());
        }
        return sb.toString();
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }
}
