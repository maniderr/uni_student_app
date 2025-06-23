package com.lock;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.lock.data.dao.UserDao;
import com.lock.data.database.AppDatabase;
import com.lock.data.model.User;
import com.lock.tasks.TaskCreationActivity;
import com.lock.utils.PasswordUtils;

public class LoginActivity extends Activity implements View.OnClickListener {
    private Button btnLogin, btnRegister;
    private EditText textUsername, textPassword;
    private SharedPreferences sharedPreferences;
    private AppDatabase db;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        db = AppDatabase.getInstance(this);
        userDao = db.userDao();

        String savedUsername = sharedPreferences.getString("username", null);
        if (savedUsername != null) {
            switchActivities();
            return;
        }

        textUsername = (EditText) findViewById(R.id.editTextUsername);
        textPassword = (EditText) findViewById(R.id.editTextPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);

        btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void switchActivities() {
        Intent switchActivityIntent = new Intent(this, TaskCreationActivity.class);
        startActivity(switchActivityIntent);
        finish();
    }

    @Override
    public void onClick(View v) {
        String username = textUsername.getText().toString().trim();
        String password = textPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both username and password!", Toast.LENGTH_SHORT).show();

        }
        new Thread(() -> {
            User user = userDao.getUserByUsername(username);

            runOnUiThread(() -> {
                if (user != null) {

                    String inputPasswordHash = PasswordUtils.hashPassword(password);
                    if (inputPasswordHash.equals(user.getPassword())) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("username", username);
                        editor.putLong("user_id", user.getId());
                        editor.apply();
                        switchActivities();
                    } else {
                        Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}
