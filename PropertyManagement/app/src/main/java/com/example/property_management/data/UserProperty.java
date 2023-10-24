package com.example.property_management.data;

import androidx.annotation.NonNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class UserProperty {
    private String propertyId;
    private boolean inspected;
    private String inspectionDate;
    private String inspectionTime;
    private String notes;
    private int price;
    private Date createdAt;
    private HashMap<String, DistanceInfo> distances;
    private HashMap<String, RoomData> inspectedData;

    public UserProperty() {}

    public UserProperty(String propertyId, boolean inspected, String inspectionDate,
                        String inspectionTime, String notes, HashMap<String, DistanceInfo> distances,
                        HashMap<String, RoomData> inspectedData, int price, Date createdAt) {
        this.propertyId = propertyId;
        this.inspected = inspected;
        this.inspectionDate = inspectionDate;
        this.inspectionTime = inspectionTime;
        this.notes = notes;
        this.price = price;
        this.distances = distances;
        this.inspectedData = inspectedData;
        this.createdAt = createdAt;
    }

    public String getPropertyId() { return propertyId; }

    public void setPropertyId(String propertyId) { this.propertyId = propertyId; }

//    public boolean isInspected() { return inspected; }

    public void setInspected(boolean inspected) { this.inspected = inspected; }

    public String getInspectionDate() { return inspectionDate; }

    public void setInspectionDate(String inspectionDate) { this.inspectionDate = inspectionDate; }

    public String getInspectionTime() { return inspectionTime; }

    public void setInspectionTime(String inspectionTime) { this.inspectionTime = inspectionTime; }

    public String getNotes() { return notes; }

    public void setNotes(String notes) { this.notes = notes; }

    public int getPrice() { return price; }

    public void setPrice(int price) { this.price = price; }

    public HashMap<String, DistanceInfo> getDistances() { return distances; }

    public void setDistances(HashMap<String, DistanceInfo> distances) { this.distances = distances; }

    public HashMap<String, RoomData> getInspectedData() { return inspectedData; }

    public void setInspectedData(HashMap<String, RoomData> inspectedData) { this.inspectedData = inspectedData; }
    public boolean getInspected() { return inspected; }
    public void setInspected() { this.inspected = inspected; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    @NonNull
    @Override
    public String toString() {
        return "UserProperty{" +
                "propertyId='" + propertyId + '\'' +
                ", inspected=" + inspected +
                ", inspectionDate='" + inspectionDate + '\'' +
                ", inspectionTime='" + inspectionTime + '\'' +
                ", notes='" + notes + '\'' +
                ", price=" + price +
                ", createdAt=" + createdAt +
                ", distances=" + distances +
                ", inspectedData=" + inspectedData +
                '}';
    }
}
