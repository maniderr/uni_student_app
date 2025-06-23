package com.lock.train_schedule;

import com.google.gson.annotations.SerializedName;

public class TrainStop {
    @SerializedName("stop_id")
    private int stopId;

    @SerializedName("stop_name")
    private String stopName;

    @SerializedName("stop_lat")
    private double stopLat;

    @SerializedName("stop_lon")
    private double stopLon;

    public int getStopId() { return stopId; }
    public String getStopName() { return stopName; }
    public double getStopLat() { return stopLat; }
    public double getStopLon() { return stopLon; }
}