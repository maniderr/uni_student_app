package com.lock.facultate_info;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lock.R;
import java.util.ArrayList;

public class AdminContactInfoActivity extends AppCompatActivity {
    private FacultateViewModel facultateViewModel;
    private FacultateAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_contact_info);

        RecyclerView recyclerView = findViewById(R.id.facultateRecyclerView);
        FloatingActionButton fab = findViewById(R.id.fab_add);

        adapter = new FacultateAdapter(new ArrayList<>(), false);

        adapter.setOnFacultateClickListener(new FacultateAdapter.OnFacultateClickListener() {
            @Override
            public void onUpdateClick(Facultate facultate) {
                showUpdatePopup(facultate);
            }

            @Override
            public void onDeleteClick(Facultate facultate) {
                new AlertDialog.Builder(AdminContactInfoActivity.this)
                        .setTitle("Delete Facultate")
                        .setMessage("Are you sure you want to delete this entry?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            facultateViewModel.delete(facultate);
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        facultateViewModel = new ViewModelProvider(this).get(FacultateViewModel.class);
        facultateViewModel.getAllFacultati().observe(this, facultati -> {
            adapter.setFacultateList(facultati);
            adapter.notifyDataSetChanged();
        });

        fab.setOnClickListener(view -> showAddFacultatePopup());
    }

    private void showAddFacultatePopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_facultate, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        EditText nameInput = dialogView.findViewById(R.id.nameTextView);
        EditText siteInput = dialogView.findViewById(R.id.siteTextView);
        EditText addressInput = dialogView.findViewById(R.id.addressTextView);
        EditText phoneInput = dialogView.findViewById(R.id.phoneTextView);
        EditText faxInput = dialogView.findViewById(R.id.faxTextView);
        EditText emailInput = dialogView.findViewById(R.id.emailTextView);
        Button saveButton = dialogView.findViewById(R.id.buttonSave);

        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String site = siteInput.getText().toString().trim();
            String address = addressInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
            String fax = faxInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(site)) {
                Toast.makeText(this, "Name and site are required!", Toast.LENGTH_SHORT).show();
                return;
            }

            Facultate facultate = new Facultate(name, site, address, phone, fax, email);
            facultateViewModel.insert(facultate);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showUpdatePopup(Facultate facultate) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.dialog_add_facultate, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(popupView);
        AlertDialog dialog = builder.create();

        EditText nameInput = popupView.findViewById(R.id.nameTextView);
        EditText siteInput = popupView.findViewById(R.id.siteTextView);
        EditText addressInput = popupView.findViewById(R.id.addressTextView);
        EditText phoneInput = popupView.findViewById(R.id.phoneTextView);
        EditText faxInput = popupView.findViewById(R.id.faxTextView);
        EditText emailInput = popupView.findViewById(R.id.emailTextView);
        Button btnSave = popupView.findViewById(R.id.buttonSave);

        // Pre-fill the data
        nameInput.setText(facultate.getName());
        siteInput.setText(facultate.getSite());
        addressInput.setText(facultate.getAddress());
        phoneInput.setText(facultate.getPhone());
        faxInput.setText(facultate.getFax());
        emailInput.setText(facultate.getEmail());

        btnSave.setOnClickListener(v -> {
            facultate.setName(nameInput.getText().toString());
            facultate.setSite(siteInput.getText().toString());
            facultate.setAddress(addressInput.getText().toString());
            facultate.setPhone(phoneInput.getText().toString());
            facultate.setFax(faxInput.getText().toString());
            facultate.setEmail(emailInput.getText().toString());

            facultateViewModel.update(facultate);

            dialog.dismiss();
        });

        dialog.show();
    }
}
