package com.example.property_management.data;

import java.util.HashMap;

public class Property {
    private String propertyId;
    private String href;
    private String address;
    private float lat;
    private float lng;
    private int numBedrooms;
    private int numBathrooms;
    private int numParking;
    private HashMap<String, RoomData> propertyData;

    public Property() {};
    public Property(String propertyId, String href, String address, float lat, float lng, int numBedrooms, int numBathrooms, int numParking, HashMap<String, RoomData> propertyData) {
        this.propertyId = propertyId;
        this.href = href;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.numBedrooms = numBedrooms;
        this.numBathrooms = numBathrooms;
        this.numParking = numParking;
        this.propertyData = propertyData;
    }

    public String getPropertyId() { return propertyId; }

    public void setPropertyId(String propertyId) { this.propertyId = propertyId; }

    public String getHref() { return href; }

    public void setHref(String href) { this.href = href; }

    public String getAddress() { return address; }

    public void setAddress(String address) { this.address = address; }

    public float getLat() { return lat; }

    public void setLat(float lat) { this.lat = lat; }

    public float getLng() { return lng; }

    public void setLng(float lng) { this.lng = lng; }

    public int getNumBedrooms() { return numBedrooms; }

    public void setNumBedrooms(int numBedrooms) { this.numBedrooms = numBedrooms; }
    public int getNumBathrooms() { return numBathrooms; }

    public void setNumBathrooms(int numBathrooms) { this.numBathrooms = numBathrooms; }

    public int getNumParking() { return numParking; }

    public void setNumParking(int numParking) { this.numParking = numParking; }

    public HashMap<String, RoomData> getPropertyData() { return propertyData; }

    public void setPropertyData(HashMap<String, RoomData> propertyData) { this.propertyData = propertyData; }
}
