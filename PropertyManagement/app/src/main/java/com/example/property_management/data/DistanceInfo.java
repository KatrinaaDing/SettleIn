package com.example.property_management.data;

/**
 * This class is used to store the distance information of a property from a given address.
 */
public class DistanceInfo {
    private String address;
    private String name;
    private String distance;
    private String driving;
    private String transit;
    private String walking;

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

    public String getDriving() { return driving; }

    public String getTransit() { return transit; }

    public String getWalking() { return walking; }
}
