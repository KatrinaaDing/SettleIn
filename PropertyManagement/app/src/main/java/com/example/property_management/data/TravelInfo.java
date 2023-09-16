package com.example.property_management.data;

public class TravelInfo {
    private float distance;
    private int driving;
    private int publicTransport;
    private int walking;

    public TravelInfo() {};
    public TravelInfo(float distance, int driving, int publicTransport, int walking) {
        this.distance = distance;
        this.driving = driving;
        this.publicTransport = publicTransport;
        this.walking = walking;
    }

    public float getDistance() { return distance; }

    public void setDistance(float distance) { this.distance = distance; }

    public int getDriving() { return driving; }

    public void setDriving(int driving) { this.driving = driving; }

    public int getPublicTransport() { return publicTransport; }

    public void setPublicTransport(int publicTransport) { this.publicTransport = publicTransport; }

    public int getWalking() { return walking; }

    public void setWalking(int walking) { this.walking = walking; }
}
