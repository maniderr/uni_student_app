package com.lock.train_schedule;

import com.google.gson.annotations.SerializedName;

public class Route {
    @SerializedName("route_id")
    private int route_id;

    @SerializedName("departure")
    private String departure;

    @SerializedName("destination")
    private String destination;

    @SerializedName("full_route")
    private String fullRoute;

    public String getFullRoute() {
        return fullRoute;
    }

    public int getRouteId() {
        return route_id;
    }

    public String getDeparture() {
        return departure;
    }

    public String getDestination() {
        return destination;
    }
}