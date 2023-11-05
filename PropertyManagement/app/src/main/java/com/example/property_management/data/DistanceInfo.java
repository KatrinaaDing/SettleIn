package com.example.property_management.data;

import java.util.HashMap;

public class DistanceInfo {
    private String address;
    private String name;
    private String distance;
    private String driving;
    private String transit;
    private String walking;

    public DistanceInfo() {}

    public DistanceInfo(String address, String name,String distance, String driving, String transit, String walking) {
        this.address = address;
        this.name = name;
        this.distance = distance;
        this.driving = driving;
        this.transit = transit;
        this.walking = walking;
    }

    public String getAddress() { return address; }

    public String getName() { return name; }

    public String getDistance() { return distance; }

    public void setDistance(String distance) { this.distance = distance; }

    public String getDriving() { return driving; }

    public void setDriving(String driving) { this.driving = driving; }

    public String getTransit() { return transit; }

    public void setTransit(String transit) { this.transit = transit; }

    public String getWalking() { return walking; }

    public void setWalking(String walking) { this.walking = walking; }
}
