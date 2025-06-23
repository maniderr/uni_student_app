package com.lock.announcement;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lock.BuildConfig;
import com.lock.R;
import com.lock.data.adapters.AnnouncementsAdapter;
import com.lock.data.model.Announcement;
import com.lock.student_chat.StudentChatSupabaseClient;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminAnnouncementActivity extends AppCompatActivity implements AnnouncementsAdapter.OnDeleteClickListener{
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private AnnouncementsAdapter adapter;
    private Calendar selectedDate = Calendar.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_announcements);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        FloatingActionButton fabAddAnnouncement = findViewById(R.id.fabAddAnnouncement);

        adapter = new AnnouncementsAdapter(new ArrayList<>(), this, true);
        recyclerView.setAdapter(adapter);

        fetchAnnouncements();

        fabAddAnnouncement.setOnClickListener(view -> showAddAnnouncementDialog());
    }

    @Override
    public void onDeleteClick(long announcementId) {
        showDeleteConfirmationDialog(announcementId);
    }

    private void showDeleteConfirmationDialog(long announcementId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Announcement")
                .setMessage("Are you sure you want to delete this announcement?")
                .setPositiveButton("Delete", (dialog, which) -> deleteAnnouncement(announcementId))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddAnnouncementDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_announcement, null);
        builder.setView(dialogView);

        EditText etText = dialogView.findViewById(R.id.etAnnouncementText);
        TextView tvSelectedDate = dialogView.findViewById(R.id.tvSelectedDate);
        Button btnPickDate = dialogView.findViewById(R.id.btnDatePicker);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmit);

        updateDateText(tvSelectedDate, selectedDate);

        btnPickDate.setOnClickListener(v -> showDatePicker(tvSelectedDate));

        AlertDialog dialog = builder.create();

        btnSubmit.setOnClickListener(v -> {
            String text = etText.getText().toString().trim();

            if (text.isEmpty()) {
                Toast.makeText(this, "Please enter announcement text", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedDate == null) {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
                return;
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String dateString = dateFormat.format(selectedDate.getTime());

            Announcement newAnnouncement = new Announcement();
            newAnnouncement.setAncmnt_text(text);
            newAnnouncement.setAncmnt_date(dateString);

            createAnnouncement(newAnnouncement);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void createAnnouncement(Announcement announcement) {
        progressBar.setVisibility(View.VISIBLE);

        SupabaseAnnouncementApi api = StudentChatSupabaseClient.getClient().create(SupabaseAnnouncementApi.class);
        Call<List<Announcement>> call = api.createAnnouncement(
                BuildConfig.SUPABASE_API_KEY,
                "Bearer " + BuildConfig.SUPABASE_API_KEY,
                "return=representation",
                announcement
        );

        call.enqueue(new Callback<List<Announcement>>() {
            @Override
            public void onResponse(Call<List<Announcement>> call, Response<List<Announcement>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    //Toast.makeText(AdminAnnouncementActivity.this,"Announcement created successfully",Toast.LENGTH_SHORT).show();
                    fetchAnnouncements();
                } else {
                    try {
                        String error = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e("Supabase", "Error: " + error);
                        Toast.makeText(AdminAnnouncementActivity.this,
                                "Failed to create: " + error,
                                Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Log.e("Supabase", "Error parsing error", e);
                        Toast.makeText(AdminAnnouncementActivity.this,
                                "Failed to create announcement",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Announcement>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("Supabase", "Network error", t);
                Toast.makeText(AdminAnnouncementActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchAnnouncements() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        SupabaseAnnouncementApi api = StudentChatSupabaseClient.getClient().create(SupabaseAnnouncementApi.class);
        Call<List<Announcement>> call = api.getAnnouncements(
                BuildConfig.SUPABASE_API_KEY,
                "Bearer " + BuildConfig.SUPABASE_API_KEY
        );

        call.enqueue(new Callback<List<Announcement>>() {
            @Override
            public void onResponse(Call<List<Announcement>> call, Response<List<Announcement>> response) {
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    adapter.setAnnouncements(response.body());
                } else {
                    Toast.makeText(AdminAnnouncementActivity.this,
                            "Failed to fetch announcements", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Announcement>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminAnnouncementActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePicker(TextView dateTextView) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this, R.style.CustomDatePickerDialog,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    updateDateText(dateTextView, selectedDate);
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateText(TextView textView, Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
        textView.setText(dateFormat.format(calendar.getTime()));
    }

    private void deleteAnnouncement(long announcementId) {
        progressBar.setVisibility(View.VISIBLE);

        Log.d("Supabase", "Problematic id " +  announcementId);
        SupabaseAnnouncementApi api = StudentChatSupabaseClient.getClient().create(SupabaseAnnouncementApi.class);
        Call<Void> call = api.deleteAnnouncement(
                BuildConfig.SUPABASE_API_KEY,
                "Bearer " + BuildConfig.SUPABASE_API_KEY,
                "eq." + announcementId
        );

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(AdminAnnouncementActivity.this,
                            "Announcement deleted", Toast.LENGTH_SHORT).show();
                    fetchAnnouncements(); // Refresh the list
                } else {
                    try {
                        String error = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Toast.makeText(AdminAnnouncementActivity.this,
                                "Delete failed: " + error, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Toast.makeText(AdminAnnouncementActivity.this,
                                "Delete failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminAnnouncementActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
