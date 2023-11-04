package com.example.property_management.data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Property {
    private String propertyId;
    private String href;
    private String address;
    private double lat;
    private double lng;
    private int numBedrooms;
    private int numBathrooms;
    private int numParking;
    // public property sensor data
    private HashMap<String, RoomData> propertyData;
    private int price;
    private ArrayList<String> images;
    private boolean inspected;
    private Date createdAt;
    private LocalDate inspectionDate;
    private LocalTime inspectionTime;

    public Property() {};
    public Property(String propertyId, String href, String address, double lat, double lng, int numBedrooms, int numBathrooms, int numParking, HashMap<String, RoomData> propertyData, ArrayList<String> images, int price) {
        this.propertyId = propertyId;
        this.href = href;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.numBedrooms = numBedrooms;
        this.numBathrooms = numBathrooms;
        this.numParking = numParking;
        this.propertyData = propertyData;
        this.images = images;
        this.price = price;
    }

    public String getPropertyId() { return propertyId; }

    public void setPropertyId(String propertyId) { this.propertyId = propertyId; }

    public String getHref() { return href; }

    public void setHref(String href) { this.href = href; }

    public String getAddress() { return address; }

    public void setAddress(String address) { this.address = address; }

    public double getLat() { return lat; }

    public void setLat(double lat) { this.lat = lat; }

    public double getLng() { return lng; }

    public void setLng(double lng) { this.lng = lng; }

    public int getNumBedrooms() { return numBedrooms; }

    public void setNumBedrooms(int numBedrooms) { this.numBedrooms = numBedrooms; }

    public int getNumBathrooms() { return numBathrooms; }

    public void setNumBathrooms(int numBathrooms) { this.numBathrooms = numBathrooms; }

    public int getNumParking() { return numParking; }

    public void setNumParking(int numParking) { this.numParking = numParking; }

    public HashMap<String, RoomData> getPropertyData() { return propertyData; }

    public void setPropertyData(HashMap<String, RoomData> propertyData) { this.propertyData = propertyData; }

    public ArrayList<String> getImages() { return images; }

    public void setImages(ArrayList<String> images) { this.images = images; }

    public int getPrice() { return price; }

    public void setPrice(int price) { this.price = price; }

    public void setInspected(boolean isInspected) { this.inspected = isInspected; }

    public boolean getInspected() { return inspected; }

    public Date getCreatedAt() { return createdAt; }

    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public LocalDate getInspectionDate() { return inspectionDate; }

    public void setInspectionDate(LocalDate inspectionDate) { this.inspectionDate = inspectionDate; }

    public LocalTime getInspectionTime() { return inspectionTime; }

    public void setInspectionTime(LocalTime inspectionTime) { this.inspectionTime = inspectionTime; }

    public String toformattedString() {
        return
                "Bedrooms: " + numBedrooms + "\n" +
                "Bathrooms: " + numBathrooms + "\n" +
                "Parking spaces: " + numParking + "\n" +
                "Price: " + price + "\n" +
                "Inspected: " + inspected + "\n" +
                "Added at: " + createdAt
                ;
    }
}
