package com.example.property_management.data;

import java.util.HashMap;

public class DistanceInfo {
    private String businessName;
    private double distance;
    private int driving;
    private int publicTransport;
    private int walking;

    public DistanceInfo() {}

    public DistanceInfo(String businessName, double distance, int driving, int publicTransport, int walking) {
        this.businessName = businessName;
        this.distance = distance;
        this.driving = driving;
        this.publicTransport = publicTransport;
        this.walking = walking;
    }

    public String getBusinessName() { return businessName; }

    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public double getDistance() { return distance; }

    public void setDistance(double distance) { this.distance = distance; }

    public int getDriving() { return driving; }

    public void setDriving(int driving) { this.driving = driving; }

    public int getPublicTransport() { return publicTransport; }

    public void setPublicTransport(int publicTransport) { this.publicTransport = publicTransport; }

    public int getWalking() { return walking; }

    public void setWalking(int walking) { this.walking = walking; }
}
