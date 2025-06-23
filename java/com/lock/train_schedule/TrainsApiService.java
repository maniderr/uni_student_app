package com.lock.train_schedule;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface TrainsApiService {
    @Headers({
            "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inp1Z2V2b3pwYXNkdnVmbWpwc29jIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDI3MzY0MzAsImV4cCI6MjA1ODMxMjQzMH0.X1f-rs_3WeMEFttmAwl_NDFFU9PJ8J_MPGu-YEhrt-I",
            "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inp1Z2V2b3pwYXNkdnVmbWpwc29jIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDI3MzY0MzAsImV4cCI6MjA1ODMxMjQzMH0.X1f-rs_3WeMEFttmAwl_NDFFU9PJ8J_MPGu-YEhrt-I"
    })
    @GET("train_stops")
    Call<List<TrainStop>> getTrainStops(
            @Query("select") String select,
            @Query("limit") int limit,
            @Query("offset") int offset
    );

    @GET("routes")
    Call<List<Route>> getRoute(
            @Query("select") String select,
            @Query("limit") int limit,
            @Query("offset") int offset,
            @Header("Authorization") String authToken,
            @Header("apikey") String apiKey
    );

    @GET("trips")
    Call<List<Trip>> getTrips(
            @Query("select") String select,
            @Query("route_id") String routeIds,
            @Header("Authorization") String authToken,
            @Header("apikey") String apiKey
    );

    @GET("stop_times")
    Call<List<StopTime>> getStopTimes(
            @Query("select") String select,
            @Query("limit") int limit,
            @Query("offset") int offset,
            @Header("Authorization") String authToken,
            @Header("apikey") String apiKey
    );
}
