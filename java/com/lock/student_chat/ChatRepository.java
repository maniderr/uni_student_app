package com.lock.student_chat;

import android.util.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRepository {
    private final StudentChatApiService apiService;
    private static final String TAG = "ChatRepository";

    public ChatRepository() {
        apiService = StudentChatSupabaseClient.getClient().create(StudentChatApiService.class);
    }

    public interface MessageCallback {
        void onSuccess(List<ChatMessage> messages);
        void onError(String error);
    }

    public void getMessages(MessageCallback callback) {
        Call<List<ChatMessage>> call = apiService.getMessages("*", "msg_time.asc");
        call.enqueue(new Callback<List<ChatMessage>>() {
            @Override
            public void onResponse(Call<List<ChatMessage>> call, Response<List<ChatMessage>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch messages");
                }
            }

            @Override
            public void onFailure(Call<List<ChatMessage>> call, Throwable t) {
                Log.e(TAG, "Error fetching messages", t);
                callback.onError(t.getMessage());
            }
        });
    }

    public void sendMessage(String user, String message, MessageCallback callback) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String timestamp = dateFormat.format(new Date());

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setUser(user);
        chatMessage.setMessage(message);
        chatMessage.setMsg_time(timestamp);

        Call<List<ChatMessage>> call = apiService.sendMessage(chatMessage);
        call.enqueue(new Callback<List<ChatMessage>>() {
            @Override
            public void onResponse(Call<List<ChatMessage>> call, Response<List<ChatMessage>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e(TAG, "Failed to send message. Code: " + response.code() + ", Error: " + errorBody);
                        callback.onError("Failed to send message: " + response.code());
                    } catch (IOException e) {
                        Log.e(TAG, "Error parsing error response", e);
                        callback.onError("Failed to send message");
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ChatMessage>> call, Throwable t) {
                Log.e(TAG, "Network error sending message", t);
                callback.onError(t.getMessage());
            }
        });
    }
}
