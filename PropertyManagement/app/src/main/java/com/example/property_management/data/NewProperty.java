package com.example.property_management.data;

import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NewProperty {
    private String href;
    private String address;
    private double lat;
    private double lng;
    private int numBedrooms;
    private int numBathrooms;
    private int numParking;
    private int price;
    private ArrayList<String> images;

    public NewProperty() {};

    public NewProperty(String href, int numBedrooms, int numBathrooms, int numParking, String address, double lat, double lng, int price, ArrayList<String> images) {
        this.href = href;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.numBedrooms = numBedrooms;
        this.numBathrooms = numBathrooms;
        this.numParking = numParking;
        this.images = images;
        this.price = price;
    }

    public HashMap<String, Object> toUpdateUserObject(String id) {
        HashMap<String, Object> propertyPayload = new HashMap<>();
        propertyPayload.put("href", href);
        propertyPayload.put("price", price);
        propertyPayload.put("propertyId", id);
        return propertyPayload;
    }
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

    public ArrayList<String> getImages() { return images; }

    public void setImages(ArrayList<String> images) { this.images = images; }

    public int getPrice() { return price; }

    public void setPrice(int price) { this.price = price; }

}
