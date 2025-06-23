package com.lock.ai_assistant;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.os.AsyncTask;
import android.widget.Toast;

import com.lock.R;
import com.lock.data.adapters.MessageAdapter;
import com.lock.data.dao.MessageDAO;
import com.lock.data.database.AppDatabase;
import com.lock.data.model.AiChatRequest;
import com.lock.data.model.AiChatResponse;
import com.lock.data.model.AiMessage;

public class AiAssistantActivity extends AppCompatActivity {
    private static final String TAG = "ChatGPT";
    private static final String USERNAME_KEY = "username";

    private EditText questionInput;
    private RecyclerView conversationRecyclerView;
    private MessageAdapter messageAdapter;
    private List<AiMessage> conversationHistory;
    private AppDatabase db;
    private SharedPreferences sharedPreferences;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        username = sharedPreferences.getString(USERNAME_KEY, "Guest");

        questionInput = findViewById(R.id.questionInput);
        Button submitButton = findViewById(R.id.submitButton);

        conversationRecyclerView = findViewById(R.id.conversationRecyclerView);
        conversationHistory = new ArrayList<>();
        messageAdapter = new MessageAdapter(conversationHistory);
        conversationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        conversationRecyclerView.setAdapter(messageAdapter);

        db = AppDatabase.getInstance(this);
        loadMessages();

        submitButton.setOnClickListener(v -> {
            String question = questionInput.getText().toString().trim();
            if (question.isEmpty()) {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
                return;
            }

            sendMessage(question);
            questionInput.setText("");

            // Hide keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(questionInput.getWindowToken(), 0);
        });
    }

    private void sendMessage(String userMessage) {
        AiMessage userMsg = new AiMessage("user", userMessage);
        userMsg.username = username;

        conversationHistory.add(new AiMessage("user", userMessage));
        messageAdapter.notifyItemInserted(conversationHistory.size() - 1);
        conversationRecyclerView.smoothScrollToPosition(conversationHistory.size() - 1);

        List<AiMessage> messagesForApi = new ArrayList<>(conversationHistory);

        insertMessage(userMsg);

        AiChatRequest request = new AiChatRequest("gpt-3.5-turbo", Collections.singletonList(userMsg), 0.7);

        RetrofitClient.getApi().getChatResponse(request).enqueue(new Callback<AiChatResponse>() {
            @Override
            public void onResponse(Call<AiChatResponse> call, Response<AiChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String reply = response.body().getChoices().get(0).getMessage().getContent();

                    AiMessage botMsg = new AiMessage("bot", reply);
                    botMsg.username = username;

                    conversationHistory.add(botMsg);
                    messageAdapter.notifyItemInserted(conversationHistory.size() - 1);
                    conversationRecyclerView.smoothScrollToPosition(conversationHistory.size() - 1);

                    insertMessage(botMsg);
                } else {
                    Toast.makeText(AiAssistantActivity.this, "Error:" + response.errorBody(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AiChatResponse> call, Throwable t) {
                Log.e(TAG, "Failure: " + t.getMessage());
            }
        });
    }

    private void insertMessage(AiMessage message) {
        new InsertMessageAsyncTask(db.messageDao()).execute(message);
    }

    private void loadMessages() {
        new LoadMessagesAsyncTask(db.messageDao(), username).execute();
    }

    private static class InsertMessageAsyncTask extends AsyncTask<AiMessage, Void, Void> {
        private MessageDAO messageDao;

        private InsertMessageAsyncTask(MessageDAO messageDao) {
            this.messageDao = messageDao;
        }

        @Override
        protected Void doInBackground(AiMessage... messages) {
            messageDao.insert(messages[0]);
            return null;
        }
    }

    private class LoadMessagesAsyncTask extends AsyncTask<Void, Void, List<AiMessage>> {
        private MessageDAO messageDao;
        private String username;

        private LoadMessagesAsyncTask(MessageDAO messageDao, String username) {
            this.messageDao = messageDao;
            this.username = username;
        }

        @Override
        protected List<AiMessage> doInBackground(Void... voids) {
            return messageDao.getMessagesByUser(username);
        }

        @Override
        protected void onPostExecute(List<AiMessage> messages) {
            super.onPostExecute(messages);
            conversationHistory.clear();
            conversationHistory.addAll(messages);
            messageAdapter.notifyDataSetChanged();
        }
    }
}
