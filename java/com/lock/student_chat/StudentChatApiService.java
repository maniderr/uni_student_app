package com.lock.student_chat;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface StudentChatApiService {
    @GET("chat")
    Call<List<ChatMessage>> getMessages(
            @Query("select") String select,
            @Query("order") String order
    );

    @POST("chat")
    Call<List<ChatMessage>> sendMessage(
            @Body ChatMessage message
    );
}