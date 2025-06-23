package com.lock.announcement;

import com.lock.data.model.Announcement;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SupabaseAnnouncementApi {
    @GET("announcements?select=*")
    Call<List<Announcement>> getAnnouncements(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken
    );

    @POST("announcements")
    Call<List<Announcement>> createAnnouncement(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Header("Prefer") String preferHeader,
            @Body Announcement announcement
    );

    @DELETE("announcements")
    Call<Void> deleteAnnouncement(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authToken,
            @Query("id") String idWithOperator
    );
}
