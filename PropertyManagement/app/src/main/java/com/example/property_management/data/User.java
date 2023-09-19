package com.example.property_management.data;

import java.util.ArrayList;

public class User {
    private String userId;
    private String userName;
    private String userEmail;

    private ArrayList<String> interestedFacilities;

    private ArrayList<UserProperty> properties;

    public User() {}

    public User(String userId, String userName, String userEmail, ArrayList<String> interestedFacilities, ArrayList<UserProperty> properties) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.interestedFacilities = interestedFacilities;
        this.properties = properties;
    }

    public String getUserId() { return userId; }

    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }

    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }

    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public ArrayList<String> getInterestedFacilities() { return interestedFacilities; }

    public void setInterestedFacilities(ArrayList<String> interestedFacilities) { this.interestedFacilities = interestedFacilities; }

    public ArrayList<UserProperty> getProperties() { return properties; }

    public void setProperties(ArrayList<UserProperty> properties) { this.properties = properties; }
}
