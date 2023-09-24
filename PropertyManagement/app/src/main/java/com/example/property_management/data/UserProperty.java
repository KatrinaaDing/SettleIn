package com.example.property_management.data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;

public class UserProperty {
    private String propertyId;
    private boolean inspected;
    private LocalDate inspectionDate;
    private LocalTime inspectionTime;
    private String notes;
    private Integer price;
    private HashMap<String, DistanceInfo> distances;
    private HashMap<String, RoomData> inspectedData;

    public UserProperty() {}

    public UserProperty(String propertyId, boolean inspected, LocalDate inspectionDate, LocalTime inspectionTime, String notes, HashMap<String, DistanceInfo> distances, HashMap<String, RoomData> inspectedData, Integer price) {
        this.propertyId = propertyId;
        this.inspected = inspected;
        this.inspectionDate = inspectionDate;
        this.inspectionTime = inspectionTime;
        this.notes = notes;
        this.price = price;
        this.distances = distances;
        this.inspectedData = inspectedData;
    }

    public String getPropertyId() { return propertyId; }

    public void setPropertyId(String propertyId) { this.propertyId = propertyId; }

    public boolean isInspected() { return inspected; }

    public void setInspected(boolean inspected) { this.inspected = inspected; }

    public LocalDate getInspectionDate() { return inspectionDate; }

    public void setInspectionDate(LocalDate inspectionDate) { this.inspectionDate = inspectionDate; }

    public LocalTime getInspectionTime() { return inspectionTime; }

    public void setInspectionTime(LocalTime inspectionTime) { this.inspectionTime = inspectionTime; }

    public String getNotes() { return notes; }

    public void setNotes(String notes) { this.notes = notes; }

    public Integer getPrice() { return price; }

    public void setPrice(Integer price) { this.price = price; }

    public HashMap<String, DistanceInfo> getDistances() { return distances; }

    public void setDistances(HashMap<String, DistanceInfo> distances) { this.distances = distances; }

    public HashMap<String, RoomData> getInspectedData() { return inspectedData; }

    public void setInspectedData(HashMap<String, RoomData> inspectedData) { this.inspectedData = inspectedData; }
}
