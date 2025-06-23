package com.lock.facultate_info;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lock.R;

import java.util.ArrayList;

public class StudentContactInfoActivity extends AppCompatActivity {
    private FacultateViewModel facultateViewModel;
    private FacultateAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_contact_info);

        RecyclerView recyclerView = findViewById(R.id.facultateRecyclerView);

        adapter = new FacultateAdapter(new ArrayList<>(), true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        facultateViewModel = new ViewModelProvider(this).get(FacultateViewModel.class);
        facultateViewModel.getAllFacultati().observe(this, facultati -> {
            adapter.setFacultateList(facultati);
            adapter.notifyDataSetChanged();
        });
    }
}
