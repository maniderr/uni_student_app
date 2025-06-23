package com.lock.settings;

import android.content.SharedPreferences;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.lock.data.adapters.HintAdapter;
import com.lock.R;
import com.lock.data.dao.UserDao;
import com.lock.data.database.AppDatabase;
import com.lock.data.model.User;
import com.lock.utils.PasswordUtils;
import com.lock.utils.enums.Faculty;
import com.lock.utils.enums.UserGroup;
import com.lock.utils.enums.Year;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText editTextFirstName, editTextLastName;
    private Spinner spinnerGroup, spinnerYear, spinnerFaculty, spinnerSection;
    private TextView textViewUsername;
    private Button btnUpdate, btnChangePassword;
    private AppDatabase db;
    private UserDao userDao;
    private static final String USERNAME_KEY = "username";
    private SharedPreferences sharedPreferences;
    private String username;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        username = sharedPreferences.getString(USERNAME_KEY, "Guest");

        db = AppDatabase.getInstance(this);
        userDao = db.userDao();

        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        textViewUsername = findViewById(R.id.textViewUsernameDisplay);

        spinnerGroup = findViewById(R.id.spinnerGroup);
        spinnerYear = findViewById(R.id.spinnerYear);
        spinnerFaculty = findViewById(R.id.spinnerFaculty);
        spinnerSection = findViewById(R.id.spinnerSection);

        btnUpdate = findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(this);

        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        setupSpinners();
        loadUserData();
    }

    private void loadUserData() {
        new Thread(() -> {
            currentUser = userDao.getUserByUsername(username);
            if (currentUser != null) {
                runOnUiThread(() -> {
                    editTextFirstName.setText(currentUser.getFirstName());
                    editTextLastName.setText(currentUser.getLastName());
                    textViewUsername.setText(currentUser.getUsername());

                    safeSetSpinnerSelection(spinnerGroup, currentUser.getGroup());
                    safeSetSpinnerSelection(spinnerYear, currentUser.getYear());
                    safeSetSpinnerSelection(spinnerFaculty, currentUser.getFaculty());

                    Faculty faculty = currentUser.getFaculty();
                    List<String> sections = new ArrayList<>();
                    sections.add("Select Section");
                    sections.addAll(Arrays.asList(faculty.getSections()));

                    ArrayAdapter<String> sectionAdapter = new ArrayAdapter<>(
                            this,
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
                    spinnerSection.setAdapter(sectionAdapter);
                    safeSetSpinnerSelection(spinnerSection, currentUser.getSection());
                });
            }
        }).start();
    }

    private <T> void safeSetSpinnerSelection(Spinner spinner, T value) {
        if (value == null) return;

        for (int i = 0; i < spinner.getCount(); i++) {
            Object item = spinner.getItemAtPosition(i);
            if (item != null && item.equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void setupSpinners() {
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
                            SettingsActivity.this,
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

    @Override
    public void onClick(View v) {
        if (spinnerGroup.getSelectedItemPosition() == 0 ||
                spinnerYear.getSelectedItemPosition() == 0 ||
                spinnerFaculty.getSelectedItemPosition() == 0 ||
                spinnerSection.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please fill all student fields", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                // Get current user again in case it changed
                User userToUpdate = userDao.getUserByUsername(username);
                if (userToUpdate == null) {
                    runOnUiThread(() ->
                            Toast.makeText(SettingsActivity.this,
                                    "User not found!",
                                    Toast.LENGTH_SHORT).show());
                    return;
                }

                userToUpdate.setFirstName(editTextFirstName.getText().toString().trim());
                userToUpdate.setLastName(editTextLastName.getText().toString().trim());

                Object groupItem = spinnerGroup.getSelectedItem();
                Object yearItem = spinnerYear.getSelectedItem();
                Object facultyItem = spinnerFaculty.getSelectedItem();
                Object sectionItem = spinnerSection.getSelectedItem();

                if (groupItem instanceof UserGroup) {
                    userToUpdate.setGroup((UserGroup) groupItem);
                }
                if (yearItem instanceof Year) {
                    userToUpdate.setYear((Year) yearItem);
                }
                if (facultyItem instanceof Faculty) {
                    userToUpdate.setFaculty((Faculty) facultyItem);
                }
                if (sectionItem != null) {
                    userToUpdate.setSection(sectionItem.toString());
                }

                userDao.update(userToUpdate);

                runOnUiThread(() -> {
                    Toast.makeText(SettingsActivity.this,
                            "Profile updated successfully!",
                            Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(SettingsActivity.this,
                                "Error updating profile",
                                Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }
        }).start();
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        TextInputEditText editCurrentPassword = dialogView.findViewById(R.id.editCurrentPassword);
        TextInputEditText editNewPassword = dialogView.findViewById(R.id.editNewPassword);
        TextInputEditText editConfirmPassword = dialogView.findViewById(R.id.editConfirmPassword);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmPasswordChange);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelPasswordChange);

        btnConfirm.setOnClickListener(v -> {
            String currentPassword = editCurrentPassword.getText().toString().trim();
            String newPassword = editNewPassword.getText().toString().trim();
            String confirmPassword = editConfirmPassword.getText().toString().trim();

            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "New passwords don't match", Toast.LENGTH_SHORT).show();
                return;
            }

            /*if (newPassword.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }*/

            new Thread(() -> {
                User user = userDao.getUserByUsername(username);
                if (user != null) {
                    String currentPasswordHash = PasswordUtils.hashPassword(currentPassword);
                    if (currentPasswordHash.equals(user.getPassword())) {
                        String newPasswordHash = PasswordUtils.hashPassword(newPassword);
                        user.setPassword(newPasswordHash);
                        userDao.update(user);

                        runOnUiThread(() -> {
                            Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        });
                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show());
                }
            }).start();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
