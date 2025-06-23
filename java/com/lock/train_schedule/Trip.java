package com.lock.train_schedule;

import com.google.gson.annotations.SerializedName;

public class Trip {
    @SerializedName("trip_id")
    private int  trip_id;

    @SerializedName("route_id")
    private int route_id;

    @SerializedName("service_id")
    private String  service_id;

    public int getRouteId() {
        return route_id;
    }

    public int  getTripId() {
        return trip_id;
    }

    public String  getServiceId() {
        return service_id;
    }
}