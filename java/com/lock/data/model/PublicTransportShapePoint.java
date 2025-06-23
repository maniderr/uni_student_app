package com.lock.data.model;

import com.google.gson.annotations.SerializedName;

public class PublicTransportShapePoint {
    @SerializedName("shape_id")
    private String shapeId;

    @SerializedName("shape_pt_lat")
    private double shapeLat;

    @SerializedName("shape_pt_lon")
    private double shapeLon;

    @SerializedName("shape_pt_sequence")
    private int sequence;

    public String getShapeId() { return shapeId; }
    public double getShapeLat() { return shapeLat; }
    public double getShapeLon() { return shapeLon; }
    public int getSequence() { return sequence; }
}
