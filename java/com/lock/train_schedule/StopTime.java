package com.lock.train_schedule;

import com.google.gson.annotations.SerializedName;

public class StopTime {
    @SerializedName("trip_id")
    private int tripId;

    @SerializedName("arrival_time")
    private String arrivalTime;

    @SerializedName("departure_time")
    private String departureTime;

    @SerializedName("stop_id")
    private int stopId;

    @SerializedName("stop_sequence")
    private int stopSequence;

    public StopTime(int tripId, String arrivalTime, String departureTime,
                    int stopId, int stopSequence) {
        this.tripId = tripId;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
        this.stopId = stopId;
        this.stopSequence = stopSequence;
    }


    // Getters and setters
    public int getTripId() { return tripId; }
    public String getArrivalTime() { return arrivalTime; }
    public String getDepartureTime() { return departureTime; }
    public int getStopId() { return stopId; }
    public int getStopSequence() { return stopSequence; }
}
