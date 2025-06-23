package com.lock.data.model;

import com.google.gson.annotations.SerializedName;

public class PublicTransportRoute {
    @SerializedName("route_id")
    private int routeId;

    @SerializedName("route_short_name")
    private String routeShortName;

    @SerializedName("route_long_name")
    private String routeLongName;

    public int getRouteId() { return routeId; }
    public String getRouteShortName() { return routeShortName; }
    public String getRouteLongName() { return routeLongName; }

    public String getFormattedName() {
        return routeShortName + " " + routeLongName;
    }
}
