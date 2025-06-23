package com.lock.location;

public class PlaceItem {
    private final String placeId;
    private final String name;

    public PlaceItem(String placeId, String name) {
        this.placeId = placeId;
        this.name = name;
    }

    public String getPlaceId() {
        return placeId;
    }

    public String getName() {
        return name;
    }
}
