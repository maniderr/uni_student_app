package com.lock.student_chat;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;

public class ChatViewModel extends ViewModel {
    private final ChatRepository repository;
    private final MutableLiveData<List<ChatMessage>> messages = new MutableLiveData<>();
    private String currentUserFullName;

    public ChatViewModel() {
        repository = new ChatRepository();
        messages.setValue(new ArrayList<>());
        loadMessages();
    }

    public LiveData<List<ChatMessage>> getMessages() {
        return messages;
    }

    public String getCurrentUserFullName() {
        return currentUserFullName;
    }

    public void setCurrentUserFullName(String fullName) {
        this.currentUserFullName = fullName;
    }

    public void loadMessages() {
        repository.getMessages(new ChatRepository.MessageCallback() {
            @Override
            public void onSuccess(List<ChatMessage> newMessages) {
                messages.postValue(newMessages);
            }

            @Override
            public void onError(String error) {
                Log.e("ChatViewModel", error);
            }
        });
    }

    public void sendMessage(String message, ChatRepository.MessageCallback callback) {
        repository.sendMessage(currentUserFullName, message, new ChatRepository.MessageCallback() {
            @Override
            public void onSuccess(List<ChatMessage> newMessages) {
                loadMessages();
                callback.onSuccess(newMessages);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
}
