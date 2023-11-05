package com.example.property_management.data;

import java.util.ArrayList;
import java.util.HashMap;

public class User {
    private String userId;

    private ArrayList<String> interestedFacilities;
    private ArrayList<String> interestedLocations;
    private ArrayList<String> locationNames;

    private HashMap<String, UserProperty> properties;

    public User() {}

    public User(String userId, ArrayList<String> interestedFacilities, ArrayList<String> interestedLocations, ArrayList<String> locationNames, HashMap<String, UserProperty> properties) {
        this.userId = userId;
        this.interestedFacilities = interestedFacilities;
        this.interestedLocations = interestedLocations;
        this.locationNames = locationNames;
        this.properties = properties;
    }

    public String getUserId() { return userId; }

    public void setUserId(String userId) { this.userId = userId; }

    public ArrayList<String> getInterestedFacilities() { return interestedFacilities; }

    public void setInterestedFacilities(ArrayList<String> interestedFacilities) { this.interestedFacilities = interestedFacilities; }

    public HashMap<String, UserProperty> getProperties() { return properties; }

    public void setProperties(HashMap<String, UserProperty> properties) { this.properties = properties; }

    public ArrayList<String> getInterestedLocations() { return interestedLocations; }

    public void setInterestedLocations(ArrayList<String> interestedLocations) { this.interestedLocations = interestedLocations; }

    public ArrayList<String> getLocationNames() { return locationNames; }
}
