package com.lock.data.model;

import com.google.gson.annotations.SerializedName;

public class PublicTransportTrip {
    @SerializedName("route_id")
    private int routeId;

    @SerializedName("trip_id")
    private String tripId;

    @SerializedName("trip_headsign")
    private String tripHeadsign;

    @SerializedName("shape_id")
    private String shapeId;

    @SerializedName("direction_id")
    private int direction_id;

    public int getRouteId() { return routeId; }
    public String getTripId() { return tripId; }
    public String getTripHeadsign() { return tripHeadsign; }
    public String getShapeId() { return shapeId; }

    public String getFormattedName() {
        if(direction_id == 0) {
            return "Way";
        }
        else return "Roundway";

    }
}