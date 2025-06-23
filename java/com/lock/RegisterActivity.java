package com.lock;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.lock.data.adapters.HintAdapter;
import com.lock.data.dao.UserDao;
import com.lock.data.database.AppDatabase;
import com.lock.data.model.User;
import com.lock.utils.PasswordUtils;
import com.lock.utils.enums.Role;
import com.lock.utils.enums.Faculty;
import com.lock.utils.enums.UserGroup;
import com.lock.utils.enums.Year;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText editTextFirstName, editTextLastName, editTextUsername, editTextPassword;
    private Spinner spinnerGroup, spinnerYear, spinnerFaculty, spinnerSection, spinnerRole;
    private TextView textViewGroup, textViewYear, textViewFaculty, textViewSection;
    private Button btnRegister;
    private AppDatabase db;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = AppDatabase.getInstance(this);
        userDao = db.userDao();

        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);

        spinnerGroup = findViewById(R.id.spinnerGroup);
        spinnerYear = findViewById(R.id.spinnerYear);
        spinnerFaculty = findViewById(R.id.spinnerFaculty);
        spinnerSection = findViewById(R.id.spinnerSection);
        spinnerRole = findViewById(R.id.spinnerRole);

        textViewGroup = findViewById(R.id.textViewGroup);
        textViewYear = findViewById(R.id.textViewYear);
        textViewFaculty = findViewById(R.id.textViewFaculty);
        textViewSection = findViewById(R.id.textViewSection);

        btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(this);

        setupSpinners();
        setupRoleListener();

        spinnerRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    spinnerGroup.setVisibility(View.GONE);
                    textViewGroup.setVisibility(View.GONE);
                    spinnerYear.setVisibility(View.GONE);
                    textViewYear.setVisibility(View.GONE);
                    spinnerFaculty.setVisibility(View.GONE);
                    textViewFaculty.setVisibility(View.GONE);
                    spinnerSection.setVisibility(View.GONE);
                    textViewSection.setVisibility(View.GONE);
                    return;
                }

                Role selectedRole = (Role) parent.getSelectedItem();
                if (selectedRole == Role.ADMIN) {
                    spinnerGroup.setVisibility(View.GONE);
                    textViewGroup.setVisibility(View.GONE);
                    spinnerYear.setVisibility(View.GONE);
                    textViewYear.setVisibility(View.GONE);
                    spinnerFaculty.setVisibility(View.GONE);
                    textViewFaculty.setVisibility(View.GONE);
                    spinnerSection.setVisibility(View.GONE);
                    textViewSection.setVisibility(View.GONE);

                    spinnerGroup.setSelection(1);
                    spinnerYear.setSelection(1);
                    spinnerFaculty.setSelection(1);
                    if (spinnerFaculty.getSelectedItem() != null) {
                        Faculty selectedFaculty = (Faculty) spinnerFaculty.getSelectedItem();
                        spinnerSection.setAdapter(new ArrayAdapter<>(RegisterActivity.this,
                                android.R.layout.simple_spinner_dropdown_item,
                                selectedFaculty.getSections()));
                        spinnerSection.setSelection(0);
                    }
                } else {
                    spinnerGroup.setVisibility(View.VISIBLE);
                    textViewGroup.setVisibility(View.VISIBLE);
                    spinnerYear.setVisibility(View.VISIBLE);
                    textViewYear.setVisibility(View.VISIBLE);
                    spinnerFaculty.setVisibility(View.VISIBLE);
                    textViewFaculty.setVisibility(View.VISIBLE);
                    spinnerSection.setVisibility(View.VISIBLE);
                    textViewSection.setVisibility(View.VISIBLE);
                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupSpinners() {
        spinnerRole.setAdapter(new HintAdapter<>(
                this,
               R.layout.item_register_spinner_dropdown,
                Role.values(),
                "Select Role"
        ));

        spinnerGroup.setAdapter(new HintAdapter<>(
                this,
                R.layout.item_register_spinner_dropdown,
                UserGroup.values(),
                "Select Group"
        ));

        spinnerYear.setAdapter(new HintAdapter<>(
                this,
                R.layout.item_register_spinner_dropdown,
                Year.values(),
                "Select Year"
        ));

        spinnerFaculty.setAdapter(new HintAdapter<>(
                this,
                R.layout.item_register_spinner_dropdown,
                Faculty.values(),
                "Select Faculty"
        ));

        ArrayAdapter<String> sectionAdapter = new ArrayAdapter<>(
                this,
                R.layout.item_register_spinner_dropdown,
                new String[]{"Select Section"}
        ){
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                if (position == 0) {
                    textView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
                    textView.setBackgroundResource(R.drawable.register_spinner_hint_background);
                } else {
                    textView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
                    textView.setBackgroundResource(R.drawable.register_spinner_hint_background);
                }
                return view;
            }
        };
        spinnerSection.setAdapter(sectionAdapter);

        spinnerFaculty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    Faculty selectedFaculty = (Faculty) spinnerFaculty.getSelectedItem();
                    List<String> sections = new ArrayList<>();
                    sections.add("Select Section");
                    sections.addAll(Arrays.asList(selectedFaculty.getSections()));

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            RegisterActivity.this,
                            R.layout.item_register_spinner_dropdown,
                            sections
                    ) {
                        @Override
                        public View getDropDownView(int position, View convertView, ViewGroup parent) {
                            View view = super.getDropDownView(position, convertView, parent);
                            TextView textView = (TextView) view;
                            if (position == 0) {
                                textView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
                                textView.setBackgroundResource(R.drawable.register_spinner_hint_background);
                            } else {
                                textView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
                                textView.setBackgroundResource(R.drawable.register_spinner_hint_background);
                            }
                            return view;
                        }
                    };
                    spinnerSection.setAdapter(adapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupRoleListener() {
        spinnerRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) { // Hint is selected
                    spinnerFaculty.setVisibility(View.GONE);
                    textViewFaculty.setVisibility(View.GONE);
                    spinnerSection.setVisibility(View.GONE);
                    textViewSection.setVisibility(View.GONE);
                    spinnerYear.setVisibility(View.GONE);
                    textViewYear.setVisibility(View.GONE);
                    spinnerGroup.setVisibility(View.GONE);
                    textViewGroup.setVisibility(View.GONE);
                    return;
                }

                Role selectedRole = (Role) spinnerRole.getSelectedItem();
                if (selectedRole == Role.ADMIN) {
                    spinnerFaculty.setVisibility(View.GONE);
                    textViewFaculty.setVisibility(View.GONE);
                    spinnerSection.setVisibility(View.GONE);
                    textViewSection.setVisibility(View.GONE);
                    spinnerYear.setVisibility(View.GONE);
                    textViewYear.setVisibility(View.GONE);
                    spinnerGroup.setVisibility(View.GONE);
                    textViewGroup.setVisibility(View.GONE);

                    spinnerFaculty.setSelection(1);
                    spinnerYear.setSelection(1);
                    spinnerGroup.setSelection(1);
                } else {
                    spinnerFaculty.setVisibility(View.VISIBLE);
                    textViewFaculty.setVisibility(View.VISIBLE);
                    spinnerSection.setVisibility(View.VISIBLE);
                    textViewSection.setVisibility(View.VISIBLE);
                    spinnerYear.setVisibility(View.VISIBLE);
                    textViewYear.setVisibility(View.VISIBLE);
                    spinnerGroup.setVisibility(View.VISIBLE);
                    textViewGroup.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    public void onClick(View v) {
        if (spinnerRole.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            return;
        }

        Role selectedRole = (Role) spinnerRole.getSelectedItem();

        if (selectedRole == Role.STUDENT) {
            if (spinnerGroup.getSelectedItemPosition() == 0 ||
                    spinnerYear.getSelectedItemPosition() == 0 ||
                    spinnerFaculty.getSelectedItemPosition() == 0 ||
                    spinnerSection.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Please fill all student fields", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        new Thread(() -> {
            try {
                UserGroup group = spinnerGroup.getVisibility() == View.VISIBLE ?
                        (UserGroup) spinnerGroup.getSelectedItem() :
                        (UserGroup) spinnerGroup.getAdapter().getItem(1);

                Year year = spinnerYear.getVisibility() == View.VISIBLE ?
                        (Year) spinnerYear.getSelectedItem() :
                        (Year) spinnerYear.getAdapter().getItem(1);

                Faculty faculty = spinnerFaculty.getVisibility() == View.VISIBLE ?
                        (Faculty) spinnerFaculty.getSelectedItem() :
                        (Faculty) spinnerFaculty.getAdapter().getItem(1);

                String section = spinnerSection.getVisibility() == View.VISIBLE ?
                        spinnerSection.getSelectedItem().toString() :
                        ((Faculty) spinnerFaculty.getAdapter().getItem(1)).getSections()[0];

                String password = editTextPassword.getText().toString().trim();
                String hashedPassword = PasswordUtils.hashPassword(password);

                User newUser = new User(0,
                        editTextFirstName.getText().toString().trim(),
                        editTextLastName.getText().toString().trim(),
                        editTextUsername.getText().toString().trim(),
                        hashedPassword,
                        group,
                        year,
                        faculty,
                        section,
                        selectedRole);

                userDao.insert(newUser);
                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this,
                            "Account created successfully!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(RegisterActivity.this,
                                "Error creating account",
                                Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }
        }).start();
    }
}

