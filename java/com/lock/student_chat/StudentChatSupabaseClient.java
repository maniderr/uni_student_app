package com.lock.student_chat;

import com.lock.BuildConfig;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class StudentChatSupabaseClient {
    private static final String BASE_URL = "https://zugevozpasdvufmjpsoc.supabase.co/rest/v1/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request.Builder requestBuilder = original.newBuilder()
                                .header("apikey", BuildConfig.SUPABASE_API_KEY)
                                .header("Authorization", "Bearer " + BuildConfig.SUPABASE_API_KEY)
                                .header("Content-Type", "application/json")
                                .header("Prefer", "return=representation");

                        Request request = requestBuilder.build();
                        return chain.proceed(request);
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
