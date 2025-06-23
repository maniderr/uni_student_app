package com.lock.public_transp;

import com.lock.data.model.PublicTransportRoute;
import com.lock.data.model.PublicTransportShapePoint;
import com.lock.data.model.PublicTransportStop;
import com.lock.data.model.PublicTransportTrip;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

import java.util.List;

public interface TranzyApiService {
    @GET("stops")
    Call<List<PublicTransportStop>> getStops(
            @Header("X-API-KEY") String apiKey,
            @Header("X-Agency-Id") String agencyId
    );

    @GET("routes")
    Call<List<PublicTransportRoute>> getRoutes(
            @Header("X-API-KEY") String apiKey,
            @Header("X-Agency-Id") String agencyId
    );

    @GET("trips")
    Call<List<PublicTransportTrip>> getTrips(
            @Header("X-API-KEY") String apiKey,
            @Header("X-Agency-Id") String agencyId
    );

    @GET("shapes")
    Call<List<PublicTransportShapePoint>> getShapes(
            @Header("X-API-KEY") String apiKey,
            @Header("X-Agency-Id") String agencyId,
            @Query("shape_id") String shapeId
    );
}
