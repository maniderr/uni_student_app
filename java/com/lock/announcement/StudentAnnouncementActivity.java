package com.lock.announcement;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.lock.BuildConfig;
import com.lock.R;
import com.lock.data.adapters.AnnouncementsAdapter;
import com.lock.data.model.Announcement;
import com.lock.student_chat.StudentChatSupabaseClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentAnnouncementActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private AnnouncementsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_announcements);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        adapter = new AnnouncementsAdapter(new ArrayList<>(), null, false);
        recyclerView.setAdapter(adapter);
        fetchAnnouncements();
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
                    Toast.makeText(StudentAnnouncementActivity.this,
                            "Failed to fetch announcements", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Announcement>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(StudentAnnouncementActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
