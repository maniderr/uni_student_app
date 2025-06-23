package com.lock.ai_assistant;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.lock.BuildConfig;

public class RetrofitClient {
    private static final String BASE_URL = "https://api.openai.com/";
    private static final String API_KEY = BuildConfig.OPENAI_API_KEY;

    private static final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY);

    private static final Interceptor authInterceptor = chain -> {
        Request request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer " + API_KEY)
                .build();
        return chain.proceed(request);
    };

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build();

    private static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build();

    public static ChatGPTApi getApi() {
        return retrofit.create(ChatGPTApi.class);
    }
}

