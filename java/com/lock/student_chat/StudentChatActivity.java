package com.lock.student_chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.lock.R;
import com.lock.data.dao.UserDao;
import com.lock.data.database.AppDatabase;
import com.lock.data.model.User;

import java.util.ArrayList;
import java.util.List;

public class StudentChatActivity extends AppCompatActivity {
    private ChatViewModel viewModel;
    private StudentChatMessageAdapter adapter;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private Button btnSend;
    private Handler refreshHandler = new Handler();
    private SharedPreferences sharedPreferences;
    private UserDao userDao;
    private AppDatabase db;
    private final MutableLiveData<String> currentUserFullName = new MutableLiveData<>();
    private static final long REFRESH_INTERVAL = 7000; // 7 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_chat);

        sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        db = AppDatabase.getInstance(this);
        userDao = db.userDao();

        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        setCurrentUserFullName();

        rvMessages = findViewById(R.id.rvMessages);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentChatMessageAdapter(new ArrayList<>(), viewModel.getCurrentUserFullName());
        rvMessages.setAdapter(adapter);

        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        viewModel.getMessages().observe(this, messages -> {
            Log.d("ChatActivity", "Displaying " + messages.size() + " messages");
            adapter.updateMessages(messages);
            rvMessages.scrollToPosition(messages.size() - 1);
        });

        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                viewModel.sendMessage(message, new ChatRepository.MessageCallback() {
                    @Override
                    public void onSuccess(List<ChatMessage> newMessages) {
                        runOnUiThread(() -> {
                            etMessage.setText("");
                            // Immediately refresh after sending
                            viewModel.loadMessages();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() ->
                                Toast.makeText(StudentChatActivity.this, error, Toast.LENGTH_SHORT).show());
                    }
                });
            }
        });

        viewModel.loadMessages();
    }

    private void setCurrentUserFullName() {
        String username = sharedPreferences.getString("username", "Guest");

        if (!username.equals("Guest")) {
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... voids) {
                    User user = userDao.getUserByUsername(username);
                    return user != null ?
                            user.getFirstName() + " " + user.getLastName() :
                            username;
                }

                @Override
                protected void onPostExecute(String fullName) {
                    viewModel.setCurrentUserFullName(fullName);
                    initializeChatAdapter(fullName);
                }
            }.execute();
        } else {
            viewModel.setCurrentUserFullName("Guest");
            initializeChatAdapter("Guest");
        }
    }

    private void initializeChatAdapter(String userFullName) {
        rvMessages = findViewById(R.id.rvMessages);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentChatMessageAdapter(new ArrayList<>(), userFullName);
        rvMessages.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startMessageRefresh();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopMessageRefresh();
    }

    private void startMessageRefresh() {
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL);
    }

    private void stopMessageRefresh() {
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            viewModel.loadMessages();
            refreshHandler.postDelayed(this, REFRESH_INTERVAL);
        }
    };
}