package com.lock.ai_assistant;

import com.lock.data.model.AiChatRequest;
import com.lock.data.model.AiChatResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ChatGPTApi {
    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    Call<AiChatResponse> getChatResponse(@Body AiChatRequest request);
}


