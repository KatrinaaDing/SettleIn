package com.example.property_management.data;

import java.util.HashMap;

public class DistanceInfo {
    private String businessName;
    private HashMap<String, TravelInfo> travelInfo;

    public DistanceInfo() {}

    public DistanceInfo(String businessName, HashMap<String, TravelInfo> travelInfo) {
        this.businessName = businessName;
        this.travelInfo = travelInfo;
    }

    public String getBusinessName() { return businessName; }

    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public HashMap<String, TravelInfo> getTravelInfo() { return travelInfo; }

    public void setTravelInfo(HashMap<String, TravelInfo> travelInfo) { this.travelInfo = travelInfo; }
}
